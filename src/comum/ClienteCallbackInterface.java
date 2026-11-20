package comum;

import java.rmi.Remote;
import java.rmi.RemoteException;

// o caminho contrário: o servidor chama esses métodos no cliente pra avisar o que aconteceu,
// por isso o cliente também exporta um objeto remoto. o ping é usado pelo heartbeat
public interface ClienteCallbackInterface extends Remote {
    void partidaEncontrada(String partidaId, String nomeAdversario)
            throws RemoteException;

    void inicioDaBatalha(String jogadorDaVezId) throws RemoteException;

    void notificarTiroSofrido(int x, int y, ResultadoTiro resultado,
                              String novoTurno) throws RemoteException;

    void fimDePartida(String vencedorId, MotivoFim motivo)
            throws RemoteException;

    void ping() throws RemoteException;
}
