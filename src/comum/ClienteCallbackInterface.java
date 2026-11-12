package comum;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface remota implementada pelo cliente (servidor -> cliente).
 * Especificada na Seção 3 do relatório (Quadro 3).
 */
public interface ClienteCallbackInterface extends Remote {

    void partidaEncontrada(String partidaId, String nomeAdversario)
            throws RemoteException;

    void inicioDaBatalha(String jogadorDaVezId) throws RemoteException;

    /** novoTurno = jogadorId de quem joga a seguir (null se a partida acabou). */
    void notificarTiroSofrido(int x, int y, ResultadoTiro resultado,
                              String novoTurno) throws RemoteException;

    void fimDePartida(String vencedorId, MotivoFim motivo)
            throws RemoteException;

    /** Usado pelo heartbeat do servidor para detectar desconexão. */
    void ping() throws RemoteException;
}
