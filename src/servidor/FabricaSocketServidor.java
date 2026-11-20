package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

// o ServerSocket padrão do RMI aceita só 50 conexões esperando na fila. no teste com 100 clientes
// entrando juntos algumas eram recusadas, então aumentamos pra 512
public final class FabricaSocketServidor implements RMIServerSocketFactory {
    private static final int FILA_DE_CONEXOES = 512;

    @Override
    public ServerSocket createServerSocket(int porta) throws IOException {
        return new ServerSocket(porta, FILA_DE_CONEXOES);
    }

    // o RMI compara as fábricas pra saber se dois objetos podem dividir a mesma porta
    @Override
    public boolean equals(Object outro) {
        return outro instanceof FabricaSocketServidor;
    }

    @Override
    public int hashCode() {
        return FabricaSocketServidor.class.hashCode();
    }
}
