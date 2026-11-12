package servidor;

import comum.EstadoPartida;
import comum.Fase;
import comum.JogoException;
import comum.MotivoFim;
import comum.Navio;
import comum.ResultadoTiro;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Estado de uma partida. TODA leitura ou escrita dos campos abaixo
 * acontece com o lock adquirido (padrão lock / try / finally unlock).
 */
public class Partida {

    private final String id = UUID.randomUUID().toString();
    private final ReentrantLock lock = new ReentrantLock(true); // modo justo

    private final Jogador[] jogadores;
    private final Tabuleiro[] tabuleiros = { new Tabuleiro(), new Tabuleiro() };
    private final Notificador notificador;

    private Fase fase = Fase.POSICIONAMENTO;
    private int vez;                 // índice (0 ou 1) do jogador da vez
    private long numeroJogada;       // versão do estado
    private String vencedorId;
    private MotivoFim motivoFim;

    public Partida(Jogador a, Jogador b, Notificador notificador) {
        this.jogadores = new Jogador[] { a, b };
        this.notificador = notificador;
    }

    public String id() {
        return id;
    }

    public void posicionarFrota(String jogadorId, List<Navio> frota) throws JogoException {
        lock.lock();
        try {
            // TODO: validar jogador e fase, tabuleiro.posicionar(frota);
            //       se as duas frotas estão prontas: sortear vez, fase = BATALHA, notificar.
            throw new UnsupportedOperationException("posicionarFrota ainda não implementado");
        } finally {
            lock.unlock();
        }
    }

    public ResultadoTiro processarTiro(String jogadorId, int x, int y) throws JogoException {
        lock.lock();
        try {
            // TODO: implementar conforme a Seção 4.3 do relatório.
            throw new UnsupportedOperationException("processarTiro ainda não implementado");
        } finally {
            lock.unlock();
        }
    }

    public EstadoPartida estadoPara(String jogadorId) throws JogoException {
        lock.lock();
        try {
            // TODO: montar uma cópia nova do estado visível para o jogador.
            throw new UnsupportedOperationException("estadoPara ainda não implementado");
        } finally {
            lock.unlock();
        }
    }

    /** Encerra por desistência ou desconexão; não faz nada se já estiver ENCERRADA. */
    public void encerrarPorAbandono(String jogadorQueSaiu, MotivoFim motivo) {
        lock.lock();
        try {
            // TODO: se fase != ENCERRADA, adversário vence e recebe fimDePartida.
            throw new UnsupportedOperationException("encerrarPorAbandono ainda não implementado");
        } finally {
            lock.unlock();
        }
    }
}
