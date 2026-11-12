package servidor;

import comum.Constantes;

public class ServidorMain {
    public static void main(String[] args) throws Exception {
        String ip = args.length > 0 ? args[0] : "127.0.0.1";

        Servidor.iniciar(ip);

        System.out.println("Servidor Batalha Naval no ar.");
        System.out.println("  Registry : " + ip + ":" + Constantes.PORTA_REGISTRY);
        System.out.println("  Serviço  : " + Constantes.urlServico(ip)
                + " (objeto na porta " + Constantes.PORTA_SERVICO + ")");
        System.out.println("Pressione Ctrl+C para encerrar.");
    }
}
