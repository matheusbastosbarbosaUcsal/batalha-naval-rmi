package servidor;

import comum.ClienteCallbackInterface;

import java.util.concurrent.atomic.AtomicBoolean;

public class Jogador {
    private final String id;
    private final String nome;
    private final ClienteCallbackInterface callback;

    private volatile String partidaId;

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

    public boolean marcarDesconectado() { return conectado.compareAndSet(true, false); }
}
