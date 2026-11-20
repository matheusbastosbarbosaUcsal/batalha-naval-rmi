package servidor;

import comum.Constantes;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Heartbeat {
    private final GerenciadorPartidas gerenciador;
    private final Consumer<Jogador> aoDesconectar;
    private final ScheduledExecutorService agendador =
            Executors.newSingleThreadScheduledExecutor(tarefa -> daemon(tarefa, "heartbeat"));
    private final ExecutorService pings =
            Executors.newCachedThreadPool(tarefa -> daemon(tarefa, "ping"));

    public Heartbeat(GerenciadorPartidas gerenciador, Consumer<Jogador> aoDesconectar) {
        this.gerenciador = gerenciador;
        this.aoDesconectar = aoDesconectar;
    }

    public void iniciar() {
        long intervalo = Constantes.INTERVALO_HEARTBEAT_MS;
        agendador.scheduleWithFixedDelay(this::verificar, intervalo, intervalo, TimeUnit.MILLISECONDS);
    }

    public void parar() {
        agendador.shutdownNow();
        pings.shutdownNow();
    }

    // de 5 em 5 segundos chama ping() em todo jogador conectado. cada ping vai numa thread separada,
    // assim um cliente que não responde não atrasa a verificação dos outros
    private void verificar() {
        try {
            for (Jogador jogador : gerenciador.todosJogadores()) {
                if (jogador.estaConectado()) {
                    pings.execute(() -> pingar(jogador));
                }
            }
        } catch (RuntimeException e) {
            Log.info("erro no heartbeat: %s", e);
        }
    }

    private void pingar(Jogador jogador) {
        try {
            jogador.callback().ping();
        } catch (RemoteException e) {
            Log.info("%s não respondeu ao ping", jogador.nome());
            aoDesconectar.accept(jogador);
        }
    }

    private static Thread daemon(Runnable tarefa, String nome) {
        Thread thread = new Thread(tarefa, nome);
        thread.setDaemon(true);
        return thread;
    }
}
