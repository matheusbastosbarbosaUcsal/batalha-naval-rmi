package cliente;

import comum.ClienteCallbackInterface;
import comum.MotivoFim;
import comum.ResultadoTiro;

import javax.swing.SwingUtilities;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClienteCallbackImpl extends UnicastRemoteObject implements ClienteCallbackInterface {
    private static final long serialVersionUID = 1L;

    private final transient ControladorCliente controlador;

    public ClienteCallbackImpl(ControladorCliente controlador) throws RemoteException {
        super(0);
        this.controlador = controlador;
    }

    @Override
    public void partidaEncontrada(String partidaId, String nomeAdversario) {
        SwingUtilities.invokeLater(() -> controlador.aoEncontrarPartida(partidaId, nomeAdversario));
    }

    @Override
    public void inicioDaBatalha(String jogadorDaVezId) {
        SwingUtilities.invokeLater(() -> controlador.aoIniciarBatalha(jogadorDaVezId));
    }

    @Override
    public void notificarTiroSofrido(int x, int y, ResultadoTiro resultado, String novoTurno) {
        SwingUtilities.invokeLater(() -> controlador.aoSofrerTiro(x, y, resultado, novoTurno));
    }

    @Override
    public void fimDePartida(String vencedorId, MotivoFim motivo) {
        SwingUtilities.invokeLater(() -> controlador.aoTerminarPartida(vencedorId, motivo));
    }

    @Override
    public void ping() {
    }
}
