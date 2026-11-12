package cliente;

import cliente.ui.JanelaPrincipal;
import comum.BatalhaNavalService;
import comum.ClienteCallbackInterface;
import comum.MotivoFim;
import comum.ResultadoTiro;

/**
 * Liga a interface gráfica às chamadas remotas.
 * Regra: chamadas ao servidor rodam em SwingWorker (fora da EDT);
 * os métodos "ao..." são chamados já na EDT pelo ClienteCallbackImpl.
 */
public class ControladorCliente {

    private final BatalhaNavalService servico;
    private ClienteCallbackInterface callback;
    private JanelaPrincipal janela;

    private String jogadorId;
    private String partidaId;

    public ControladorCliente(BatalhaNavalService servico) {
        this.servico = servico;
    }

    public void setCallback(ClienteCallbackInterface callback) { this.callback = callback; }
    public void setJanela(JanelaPrincipal janela) { this.janela = janela; }

    // ---------- ações do usuário (chamam o servidor) ----------

    public void entrarNoLobby(String nome) {
        // TODO: SwingWorker -> jogadorId = servico.entrarNoLobby(nome, callback)
    }

    public void dispararTiro(int x, int y) {
        // TODO: SwingWorker -> servico.dispararTiro(partidaId, jogadorId, x, y)
    }

    // ---------- eventos do servidor (já na EDT) ----------

    public void aoEncontrarPartida(String partidaId, String nomeAdversario) {
        this.partidaId = partidaId;
        // TODO: abrir a tela de posicionamento
    }

    public void aoIniciarBatalha(String jogadorDaVezId) {
        // TODO: abrir a tela de batalha; habilitar cliques se jogadorDaVezId == jogadorId
    }

    public void aoSofrerTiro(int x, int y, ResultadoTiro resultado, String novoTurno) {
        // TODO: marcar o tiro em "minha frota"; habilitar cliques se novoTurno == jogadorId
    }

    public void aoTerminarPartida(String vencedorId, MotivoFim motivo) {
        // TODO: mostrar vitória ou derrota
    }
}
