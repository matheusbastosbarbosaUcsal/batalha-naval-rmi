package cliente;

import cliente.ui.JanelaPrincipal;
import comum.BatalhaNavalService;
import comum.ClienteCallbackInterface;
import comum.MotivoFim;
import comum.ResultadoTiro;

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

    public void entrarNoLobby(String nome) {
    }

    public void dispararTiro(int x, int y) {
    }

    public void aoEncontrarPartida(String partidaId, String nomeAdversario) {
        this.partidaId = partidaId;
    }

    public void aoIniciarBatalha(String jogadorDaVezId) {
    }

    public void aoSofrerTiro(int x, int y, ResultadoTiro resultado, String novoTurno) {
    }

    public void aoTerminarPartida(String vencedorId, MotivoFim motivo) {
    }
}
