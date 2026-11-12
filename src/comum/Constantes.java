package comum;

public final class Constantes {
    public static final int PORTA_REGISTRY = 1099;
    public static final int PORTA_SERVICO = 1100;
    public static final String NOME_SERVICO = "BatalhaNavalService";

    public static final int TAMANHO_TABULEIRO = 10;
    public static final int TAMANHO_MAXIMO_NOME = 20;

    public static final long INTERVALO_HEARTBEAT_MS = 5_000;
    public static final long TIMEOUT_RESPOSTA_MS = 5_000;

    private Constantes() { }

    public static String urlServico(String ipServidor) {
        return "rmi://" + ipServidor + ":" + PORTA_REGISTRY + "/" + NOME_SERVICO;
    }
}
