package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

public final class FabricaSocketServidor implements RMIServerSocketFactory {
    private static final int FILA_DE_CONEXOES = 512;

    @Override
    public ServerSocket createServerSocket(int porta) throws IOException {
        return new ServerSocket(porta, FILA_DE_CONEXOES);
    }

    @Override
    public boolean equals(Object outro) {
        return outro instanceof FabricaSocketServidor;
    }

    @Override
    public int hashCode() {
        return FabricaSocketServidor.class.hashCode();
    }
}
