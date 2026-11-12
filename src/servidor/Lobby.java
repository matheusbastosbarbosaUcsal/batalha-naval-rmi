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

    public synchronized void entrar(Jogador jogador) {
        throw new UnsupportedOperationException("Lobby.entrar ainda não implementado");
    }

    public synchronized boolean remover(Jogador jogador) {
        return fila.remove(jogador);
    }
}
