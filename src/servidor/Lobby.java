package servidor;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Fila de espera. Inserir e emparelhar acontecem no mesmo método synchronized,
 * para que um jogador nunca seja pareado duas vezes nem esquecido na fila.
 */
public class Lobby {

    private final Deque<Jogador> fila = new ArrayDeque<>();
    private final GerenciadorPartidas gerenciador;
    private final Notificador notificador;

    public Lobby(GerenciadorPartidas gerenciador, Notificador notificador) {
        this.gerenciador = gerenciador;
        this.notificador = notificador;
    }

    /** Coloca o jogador na fila e, se houver dois, cria a partida. */
    public synchronized void entrar(Jogador jogador) {
        // TODO: fila.addLast(jogador); se fila.size() >= 2, retirar o par,
        //       criar Partida, registrar no gerenciador e enfileirar partidaEncontrada.
        throw new UnsupportedOperationException("Lobby.entrar ainda não implementado");
    }

    /** Remove o jogador da fila, se ele ainda estiver esperando. */
    public synchronized boolean remover(Jogador jogador) {
        return fila.remove(jogador);
    }
}
