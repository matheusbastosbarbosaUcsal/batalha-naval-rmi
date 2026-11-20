package cliente;

import cliente.ui.JanelaPrincipal;
import comum.*;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.List;

public class ControladorCliente {
    private final BatalhaNavalService servico;
    private ClienteCallbackInterface callback;
    private JanelaPrincipal janela;

    private String jogadorId;
    private String partidaId;
    private String nomeAdversario;
    private String meuNome;

    public ControladorCliente(BatalhaNavalService servico) {
        this.servico = servico;
    }

    public void setCallback(ClienteCallbackInterface callback) { this.callback = callback; }
    public void setJanela(JanelaPrincipal janela) { this.janela = janela; }

    public String getJogadorId() { return jogadorId; }
    public String getPartidaId() { return partidaId; }
    public String getNomeAdversario() { return nomeAdversario; }

    public void entrarNoLobby(String nome) {
        this.meuNome = nome;
        new Thread(() -> {
            try {
                String id = servico.entrarNoLobby(nome, callback);
                this.jogadorId = id;
                SwingUtilities.invokeLater(() -> janela.mostrarAguardando("Conectado! Aguardando oponente no lobby..."));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(janela,
                        "Erro ao entrar no lobby: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    public void posicionarFrota(List<Navio> frota) {
        new Thread(() -> {
            try {
                servico.posicionarFrota(partidaId, jogadorId, frota);
                SwingUtilities.invokeLater(() -> janela.mostrarAguardandoBatalha("Frota enviada! Aguardando oponente posicionar..."));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(janela,
                        "Erro ao posicionar frota: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    public void dispararTiro(int x, int y) {
        new Thread(() -> {
            try {
                ResultadoTiro res = servico.dispararTiro(partidaId, jogadorId, x, y);
                EstadoPartida estado = servico.obterEstado(partidaId, jogadorId);
                boolean minhaVez = jogadorId.equals(estado.jogadorDaVezId());
                SwingUtilities.invokeLater(() -> janela.atualizarEstadoBatalha(estado, minhaVez, "Tiro em (" + x + "," + y + "): " + res.tipo()));
            } catch (ForaDoTurnoException e) {
                SwingUtilities.invokeLater(() -> janela.adicionarLog("Não é a sua vez!"));
            } catch (JogadaInvalidaException e) {
                SwingUtilities.invokeLater(() -> janela.adicionarLog("Jogada inválida: " + e.getMessage()));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(janela,
                        "Erro ao disparar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    public void sair() {
        if (jogadorId != null) {
            try {
                servico.sair(jogadorId);
            } catch (Exception ignored) {
            }
        }
        this.jogadorId = null;
        this.partidaId = null;
        this.nomeAdversario = null;
        SwingUtilities.invokeLater(() -> janela.mostrarLobby());
    }

    public void aoEncontrarPartida(String partidaId, String nomeAdversario) {
        this.partidaId = partidaId;
        this.nomeAdversario = nomeAdversario;
        janela.mostrarTelaPosicionamento(nomeAdversario);
    }

    public void aoIniciarBatalha(String jogadorDaVezId) {
        try {
            EstadoPartida estado = servico.obterEstado(partidaId, jogadorId);
            boolean minhaVez = jogadorId.equals(jogadorDaVezId);
            janela.mostrarTelaBatalha(estado, minhaVez);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(janela, "Erro ao iniciar batalha: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void aoSofrerTiro(int x, int y, ResultadoTiro resultado, String novoTurno) {
        try {
            EstadoPartida estado = servico.obterEstado(partidaId, jogadorId);
            boolean minhaVez = jogadorId.equals(novoTurno);
            String msg = "Adversário atirou em (" + x + "," + y + ") -> " + resultado.tipo();
            if (resultado.navioAfundado() != null) {
                msg += " (Afundou " + resultado.navioAfundado() + "!)";
            }
            janela.atualizarEstadoBatalha(estado, minhaVez, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void aoTerminarPartida(String vencedorId, MotivoFim motivo) {
        boolean venci = jogadorId != null && jogadorId.equals(vencedorId);
        janela.mostrarFimDePartida(venci, motivo, nomeAdversario);
        this.partidaId = null;
        this.nomeAdversario = null;
    }
}
