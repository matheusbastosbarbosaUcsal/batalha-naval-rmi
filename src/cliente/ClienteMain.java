package cliente;

import cliente.ui.JanelaPrincipal;
import comum.BatalhaNavalService;
import comum.Constantes;

import javax.swing.SwingUtilities;
import comum.Rede;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;

public class ClienteMain {
    public static void main(String[] args) throws Exception {
        String ipServidor = args.length > 0 ? args[0] : "127.0.0.1";
        String meuIp = args.length > 1 ? args[1] : Rede.ipLocalPara(ipServidor);

        System.setProperty("java.rmi.server.hostname", meuIp);

        BatalhaNavalService servico =
                (BatalhaNavalService) Naming.lookup(Constantes.urlServico(ipServidor));

        ControladorCliente controlador = new ControladorCliente(servico);
        ClienteCallbackImpl callback = exportarCallback(controlador);
        controlador.setCallback(callback);

        System.out.println("Conectado a " + Constantes.urlServico(ipServidor));
        System.out.println("Callbacks em " + meuIp + ":" + callback.porta());

        SwingUtilities.invokeLater(() -> {
            JanelaPrincipal janela = new JanelaPrincipal(controlador, ipServidor);
            controlador.setJanela(janela);
            janela.setVisible(true);
        });
    }

    private static ClienteCallbackImpl exportarCallback(ControladorCliente controlador) throws RemoteException {
        ExportException ultimoErro = null;
        int ultima = Constantes.PORTA_CALLBACK + Constantes.QUANTIDADE_PORTAS_CALLBACK;
        for (int porta = Constantes.PORTA_CALLBACK; porta < ultima; porta++) {
            try {
                return new ClienteCallbackImpl(controlador, porta);
            } catch (ExportException e) {
                ultimoErro = e;
            }
        }
        throw ultimoErro;
    }
}
