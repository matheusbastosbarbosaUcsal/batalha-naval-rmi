package servidor;

import comum.ClienteCallbackInterface;

import java.util.concurrent.atomic.AtomicBoolean;

/** Jogador conectado ao servidor. */
public class Jogador {

    private final String id;
    private final String nome;
    private final ClienteCallbackInterface callback;

    /** Partida atual (null enquanto está no lobby). Escrito e lido por threads diferentes. */
    private volatile String partidaId;

    /** compareAndSet(true, false) garante que a desconexão seja processada uma única vez. */
    private final AtomicBoolean conectado = new AtomicBoolean(true);

    public Jogador(String id, String nome, ClienteCallbackInterface callback) {
        this.id = id;
        this.nome = nome;
        this.callback = callback;
    }

    public String id() { return id; }
    public String nome() { return nome; }
    public ClienteCallbackInterface callback() { return callback; }

    public String partidaId() { return partidaId; }
    public void setPartidaId(String partidaId) { this.partidaId = partidaId; }

    public boolean estaConectado() { return conectado.get(); }

    /** @return true somente para a primeira thread que marcar a desconexão. */
    public boolean marcarDesconectado() { return conectado.compareAndSet(true, false); }
}
