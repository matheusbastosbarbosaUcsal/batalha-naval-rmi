package comum;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface BatalhaNavalService extends Remote {
    String entrarNoLobby(String nomeJogador, ClienteCallbackInterface callback)
            throws RemoteException, NomeInvalidoException;

    void posicionarFrota(String partidaId, String jogadorId, List<Navio> frota)
            throws RemoteException, PartidaNaoEncontradaException,
                   JogadorInvalidoException, FaseInvalidaException,
                   FrotaInvalidaException;

    ResultadoTiro dispararTiro(String partidaId, String jogadorId, int x, int y)
            throws RemoteException, PartidaNaoEncontradaException,
                   JogadorInvalidoException, FaseInvalidaException,
                   ForaDoTurnoException, JogadaInvalidaException;

    EstadoPartida obterEstado(String partidaId, String jogadorId)
            throws RemoteException, PartidaNaoEncontradaException,
                   JogadorInvalidoException;

    void sair(String jogadorId) throws RemoteException;
}
