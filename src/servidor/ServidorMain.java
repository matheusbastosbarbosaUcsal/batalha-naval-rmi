package servidor;

import comum.Constantes;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Ponto de entrada do servidor.
 * Uso: java -cp bin servidor.ServidorMain [IP_DESTA_MAQUINA]
 */
public class ServidorMain {

    public static void main(String[] args) throws Exception {
        String ip = args.length > 0 ? args[0] : "127.0.0.1";

        // Endereço gravado nas referências remotas que este processo exporta.
        // Sem isso, clientes em outras máquinas podem receber 127.0.0.1.
        System.setProperty("java.rmi.server.hostname", ip);
        // Tempo-limite das chamadas de callback (cliente travado vira RemoteException).
        System.setProperty("sun.rmi.transport.tcp.responseTimeout",
                String.valueOf(Constantes.TIMEOUT_RESPOSTA_MS));

        // TODO (commit "filtro de desserialização"): exportar com ObjectInputFilter.
        BatalhaNavalServiceImpl servico = new BatalhaNavalServiceImpl();

        Registry registry = LocateRegistry.createRegistry(Constantes.PORTA_REGISTRY);
        registry.rebind(Constantes.NOME_SERVICO, servico);

        System.out.println("Servidor Batalha Naval no ar.");
        System.out.println("  Registry : " + ip + ":" + Constantes.PORTA_REGISTRY);
        System.out.println("  Serviço  : " + Constantes.urlServico(ip)
                + " (objeto na porta " + Constantes.PORTA_SERVICO + ")");
        System.out.println("Pressione Ctrl+C para encerrar.");
    }
}
