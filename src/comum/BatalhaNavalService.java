package comum;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface remota do servidor (cliente -> servidor).
 * Especificada na Seção 3 do relatório (Quadro 2).
 */
public interface BatalhaNavalService extends Remote {

    /** Entra na fila de espera; devolve o jogadorId gerado pelo servidor. */
    String entrarNoLobby(String nomeJogador, ClienteCallbackInterface callback)
            throws RemoteException, NomeInvalidoException;

    /** Envia a frota do jogador (fase POSICIONAMENTO). */
    void posicionarFrota(String partidaId, String jogadorId, List<Navio> frota)
            throws RemoteException, PartidaNaoEncontradaException,
                   JogadorInvalidoException, FaseInvalidaException,
                   FrotaInvalidaException;

    /** Dispara um tiro na coluna x e linha y do tabuleiro adversário (0 a 9). */
    ResultadoTiro dispararTiro(String partidaId, String jogadorId, int x, int y)
            throws RemoteException, PartidaNaoEncontradaException,
                   JogadorInvalidoException, FaseInvalidaException,
                   ForaDoTurnoException, JogadaInvalidaException;

    /** Devolve o estado da partida visível para o jogador. */
    EstadoPartida obterEstado(String partidaId, String jogadorId)
            throws RemoteException, PartidaNaoEncontradaException,
                   JogadorInvalidoException;

    /** Sai da fila ou da partida (o adversário vence por W.O.). Idempotente. */
    void sair(String jogadorId) throws RemoteException;
}
