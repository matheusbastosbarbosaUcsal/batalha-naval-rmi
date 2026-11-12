package cliente;

import cliente.ui.JanelaPrincipal;
import comum.BatalhaNavalService;
import comum.Constantes;

import javax.swing.SwingUtilities;
import java.rmi.Naming;

/**
 * Ponto de entrada do cliente.
 * Uso: java -cp bin cliente.ClienteMain [IP_DO_SERVIDOR] [IP_DESTA_MAQUINA]
 */
public class ClienteMain {

    public static void main(String[] args) throws Exception {
        String ipServidor = args.length > 0 ? args[0] : "127.0.0.1";
        String meuIp = args.length > 1 ? args[1] : "127.0.0.1";

        // Endereço que o servidor vai usar para chamar os callbacks deste cliente.
        System.setProperty("java.rmi.server.hostname", meuIp);

        BatalhaNavalService servico =
                (BatalhaNavalService) Naming.lookup(Constantes.urlServico(ipServidor));

        ControladorCliente controlador = new ControladorCliente(servico);
        ClienteCallbackImpl callback = new ClienteCallbackImpl(controlador);
        controlador.setCallback(callback);

        System.out.println("Conectado a " + Constantes.urlServico(ipServidor));

        SwingUtilities.invokeLater(() -> {
            JanelaPrincipal janela = new JanelaPrincipal(controlador, ipServidor);
            controlador.setJanela(janela);
            janela.setVisible(true);
        });
    }
}
