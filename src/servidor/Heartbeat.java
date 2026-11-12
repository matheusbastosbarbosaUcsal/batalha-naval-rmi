package servidor;

/** Chama ping() em cada jogador a cada 5 s; falha = desconexão (W.O. para o adversário). */
public class Heartbeat {

    private final GerenciadorPartidas gerenciador;

    public Heartbeat(GerenciadorPartidas gerenciador) {
        this.gerenciador = gerenciador;
    }

    public void iniciar() {
        // TODO: ScheduledExecutorService com scheduleWithFixedDelay(INTERVALO_HEARTBEAT_MS)
    }

    public void parar() {
        // TODO
    }
}
