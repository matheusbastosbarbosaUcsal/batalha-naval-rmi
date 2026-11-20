package servidor;

import java.util.ArrayDeque;
import java.util.Deque;

public class Lobby {
    private final Deque<Jogador> fila = new ArrayDeque<>();
    private final GerenciadorPartidas gerenciador;
    private final Notificador notificador;

    public Lobby(GerenciadorPartidas gerenciador, Notificador notificador) {
        this.gerenciador = gerenciador;
        this.notificador = notificador;
    }

    // synchronized pra dois jogadores que entram ao mesmo tempo não pegarem o mesmo adversário
    public synchronized void entrar(Jogador jogador) {
        fila.removeIf(j -> !j.estaConectado());
        fila.addLast(jogador);
        Log.info("%s entrou no lobby (%d na fila)", jogador.nome(), fila.size());

        // fila FIFO: os dois que estão esperando há mais tempo formam a partida
        if (fila.size() >= 2) {
            Jogador a = fila.pollFirst();
            Jogador b = fila.pollFirst();
            Partida partida = new Partida(a, b, notificador, gerenciador);
            a.setPartidaId(partida.id());
            b.setPartidaId(partida.id());
            gerenciador.registrarPartida(partida);
            Log.info("partida=%s criada: %s x %s", partida.id().substring(0, 8), a.nome(), b.nome());
            notificador.partidaEncontrada(a, partida.id(), b.nome());
            notificador.partidaEncontrada(b, partida.id(), a.nome());
        }
    }

    public synchronized boolean remover(Jogador jogador) {
        return fila.remove(jogador);
    }
}
