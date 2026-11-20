package servidor;

import comum.ClienteCallbackInterface;
import comum.MotivoFim;
import comum.ResultadoTiro;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;

public class Notificador {
    @FunctionalInterface
    private interface ChamadaRemota {
        void executar(ClienteCallbackInterface callback) throws RemoteException;
    }

    // uma fila com uma thread só pra cada jogador. os avisos chegam na mesma ordem em que
    // aconteceram, e se um cliente travar só a fila dele atrasa
    private final Map<String, ExecutorService> filas = new ConcurrentHashMap<>();
    private volatile Consumer<Jogador> aoFalhar = jogador -> { };

    public void setAoFalhar(Consumer<Jogador> aoFalhar) {
        this.aoFalhar = aoFalhar;
    }

    public void partidaEncontrada(Jogador destino, String partidaId, String nomeAdversario) {
        enviar(destino, "partidaEncontrada", cb -> cb.partidaEncontrada(partidaId, nomeAdversario));
    }

    public void inicioDaBatalha(Jogador destino, String jogadorDaVezId) {
        enviar(destino, "inicioDaBatalha", cb -> cb.inicioDaBatalha(jogadorDaVezId));
    }

    public void tiroSofrido(Jogador destino, int x, int y, ResultadoTiro resultado, String novoTurno) {
        enviar(destino, "notificarTiroSofrido", cb -> cb.notificarTiroSofrido(x, y, resultado, novoTurno));
    }

    public void fimDePartida(Jogador destino, String vencedorId, MotivoFim motivo) {
        enviar(destino, "fimDePartida", cb -> cb.fimDePartida(vencedorId, motivo));
    }

    public void encerrarFila(Jogador jogador) {
        ExecutorService fila = filas.remove(jogador.id());
        if (fila != null) {
            fila.shutdown();
        }
    }

    public void encerrarTudo() {
        filas.values().forEach(ExecutorService::shutdownNow);
        filas.clear();
    }

    private void enviar(Jogador destino, String metodo, ChamadaRemota chamada) {
        if (!destino.estaConectado()) {
            return;
        }
        ExecutorService fila = filas.computeIfAbsent(destino.id(), id -> criarFila(destino));
        try {
            fila.execute(() -> {
                try {
                    chamada.executar(destino.callback());
                } catch (RemoteException e) {
                    // callback que falhou ou passou do responseTimeout: o jogador é tratado como desconectado
                    Log.info("falha ao chamar %s em %s: %s", metodo, destino.nome(), e.getClass().getSimpleName());
                    aoFalhar.accept(destino);
                }
            });
        } catch (RejectedExecutionException e) {
            Log.info("fila de %s já encerrada, %s descartado", destino.nome(), metodo);
        }
    }

    private static ExecutorService criarFila(Jogador jogador) {
        return Executors.newSingleThreadExecutor(tarefa -> {
            Thread thread = new Thread(tarefa, "notificador-" + jogador.nome());
            thread.setDaemon(true);
            return thread;
        });
    }
}
