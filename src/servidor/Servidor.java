package servidor;

import comum.Constantes;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public final class Servidor {
    private final Registry registry;
    private final BatalhaNavalServiceImpl servico;

    private Servidor(Registry registry, BatalhaNavalServiceImpl servico) {
        this.registry = registry;
        this.servico = servico;
    }

    public static Servidor iniciar(String ip) throws RemoteException {
        System.setProperty("java.rmi.server.hostname", ip);
        System.setProperty("sun.rmi.transport.tcp.responseTimeout",
                String.valueOf(Constantes.TIMEOUT_RESPOSTA_MS));

        BatalhaNavalServiceImpl servico = new BatalhaNavalServiceImpl();
        Remote stub = UnicastRemoteObject.exportObject(servico, Constantes.PORTA_SERVICO,
                null, new FabricaSocketServidor(), FiltroDesserializacao.criar());

        Registry registry = LocateRegistry.createRegistry(Constantes.PORTA_REGISTRY);
        registry.rebind(Constantes.NOME_SERVICO, stub);
        servico.iniciar();
        return new Servidor(registry, servico);
    }

    public void parar() {
        servico.parar();
        try {
            registry.unbind(Constantes.NOME_SERVICO);
        } catch (RemoteException | NotBoundException ignorada) {
        }
        try {
            UnicastRemoteObject.unexportObject(servico, true);
            UnicastRemoteObject.unexportObject(registry, true);
        } catch (NoSuchObjectException ignorada) {
        }
    }
}
