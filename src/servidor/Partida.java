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

public class Partida {
    private final String id = UUID.randomUUID().toString();
    private final ReentrantLock lock = new ReentrantLock(true);

    private final Jogador[] jogadores;
    private final Tabuleiro[] tabuleiros = { new Tabuleiro(), new Tabuleiro() };
    private final Notificador notificador;

    private Fase fase = Fase.POSICIONAMENTO;
    private int vez;
    private long numeroJogada;
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
            throw new UnsupportedOperationException("posicionarFrota ainda não implementado");
        } finally {
            lock.unlock();
        }
    }

    public ResultadoTiro processarTiro(String jogadorId, int x, int y) throws JogoException {
        lock.lock();
        try {
            throw new UnsupportedOperationException("processarTiro ainda não implementado");
        } finally {
            lock.unlock();
        }
    }

    public EstadoPartida estadoPara(String jogadorId) throws JogoException {
        lock.lock();
        try {
            throw new UnsupportedOperationException("estadoPara ainda não implementado");
        } finally {
            lock.unlock();
        }
    }

    public void encerrarPorAbandono(String jogadorQueSaiu, MotivoFim motivo) {
        lock.lock();
        try {
            throw new UnsupportedOperationException("encerrarPorAbandono ainda não implementado");
        } finally {
            lock.unlock();
        }
    }
}
