package servidor;

import comum.BatalhaNavalService;
import comum.ClienteCallbackInterface;
import comum.Constantes;
import comum.EstadoPartida;
import comum.FaseInvalidaException;
import comum.ForaDoTurnoException;
import comum.FrotaInvalidaException;
import comum.JogadaInvalidaException;
import comum.JogadorInvalidoException;
import comum.Navio;
import comum.NomeInvalidoException;
import comum.PartidaNaoEncontradaException;
import comum.ResultadoTiro;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class BatalhaNavalServiceImpl extends UnicastRemoteObject implements BatalhaNavalService {
    private static final long serialVersionUID = 1L;

    private final transient GerenciadorPartidas gerenciador = new GerenciadorPartidas();
    private final transient Notificador notificador = new Notificador();
    private final transient Lobby lobby = new Lobby(gerenciador, notificador);
    private final transient Heartbeat heartbeat = new Heartbeat(gerenciador);

    public BatalhaNavalServiceImpl() throws RemoteException {
        super(Constantes.PORTA_SERVICO);
        heartbeat.iniciar();
    }

    @Override
    public String entrarNoLobby(String nomeJogador, ClienteCallbackInterface callback)
            throws RemoteException, NomeInvalidoException {
        throw new UnsupportedOperationException("entrarNoLobby ainda não implementado");
    }

    @Override
    public void posicionarFrota(String partidaId, String jogadorId, List<Navio> frota)
            throws RemoteException, PartidaNaoEncontradaException, JogadorInvalidoException,
                   FaseInvalidaException, FrotaInvalidaException {
        throw new UnsupportedOperationException("posicionarFrota ainda não implementado");
    }

    @Override
    public ResultadoTiro dispararTiro(String partidaId, String jogadorId, int x, int y)
            throws RemoteException, PartidaNaoEncontradaException, JogadorInvalidoException,
                   FaseInvalidaException, ForaDoTurnoException, JogadaInvalidaException {
        throw new UnsupportedOperationException("dispararTiro ainda não implementado");
    }

    @Override
    public EstadoPartida obterEstado(String partidaId, String jogadorId)
            throws RemoteException, PartidaNaoEncontradaException, JogadorInvalidoException {
        throw new UnsupportedOperationException("obterEstado ainda não implementado");
    }

    @Override
    public void sair(String jogadorId) throws RemoteException {
        throw new UnsupportedOperationException("sair ainda não implementado");
    }
}
