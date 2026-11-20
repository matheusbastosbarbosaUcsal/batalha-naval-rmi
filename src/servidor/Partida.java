package servidor;

import comum.EstadoPartida;
import comum.Fase;
import comum.FaseInvalidaException;
import comum.ForaDoTurnoException;
import comum.FrotaInvalidaException;
import comum.JogadaInvalidaException;
import comum.JogadorInvalidoException;
import comum.MotivoFim;
import comum.Navio;
import comum.ResultadoTiro;
import comum.TipoResultado;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

public class Partida {
    private final String id = UUID.randomUUID().toString();
    // um lock por partida, então partidas diferentes não esperam uma pela outra.
    // o true deixa o lock justo: quem pediu primeiro entra primeiro
    private final ReentrantLock lock = new ReentrantLock(true);

    private final Jogador[] jogadores;
    private final Tabuleiro[] tabuleiros = { new Tabuleiro(), new Tabuleiro() };
    private final Notificador notificador;
    private final GerenciadorPartidas gerenciador;

    private Fase fase = Fase.POSICIONAMENTO;
    private int vez;
    // sobe a cada jogada aceita. vai junto no EstadoPartida como versão,
    // e o cliente usa pra jogar fora um estado mais velho que chegou atrasado
    private long numeroJogada;
    private String vencedorId;

    public Partida(Jogador a, Jogador b, Notificador notificador, GerenciadorPartidas gerenciador) {
        this.jogadores = new Jogador[] { a, b };
        this.notificador = notificador;
        this.gerenciador = gerenciador;
    }

    public String id() {
        return id;
    }

    public void posicionarFrota(String jogadorId, List<Navio> frota)
            throws JogadorInvalidoException, FaseInvalidaException, FrotaInvalidaException {
        lock.lock();
        try {
            int i = indiceDo(jogadorId);
            exigirFase(Fase.POSICIONAMENTO);
            if (tabuleiros[i].frotaPosicionada()) {
                throw new FaseInvalidaException("Sua frota já foi enviada.");
            }
            tabuleiros[i].posicionar(frota);
            numeroJogada++;
            Log.info("partida=%s v=%d frota de %s posicionada", idCurto(), numeroJogada, jogadores[i].nome());

            if (tabuleiros[1 - i].frotaPosicionada()) {
                // os avisos não são enviados aqui dentro do lock: o notificador só põe na fila
                // de cada jogador e outra thread faz a chamada. assim um cliente lento não segura a partida
                vez = ThreadLocalRandom.current().nextInt(2);
                fase = Fase.BATALHA;
                String daVez = jogadores[vez].id();
                Log.info("partida=%s batalha iniciada, começa %s", idCurto(), jogadores[vez].nome());
                for (Jogador jogador : jogadores) {
                    notificador.inicioDaBatalha(jogador, daVez);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public ResultadoTiro processarTiro(String jogadorId, int x, int y)
            throws JogadorInvalidoException, FaseInvalidaException,
                   ForaDoTurnoException, JogadaInvalidaException {
        lock.lock();
        try {
            int i = indiceDo(jogadorId);
            exigirFase(Fase.BATALHA);
            // a conferência da vez e o tiro acontecem dentro do mesmo lock,
            // então dois tiros ao mesmo tempo nunca passam os dois
            if (i != vez) {
                throw new ForaDoTurnoException("Não é a sua vez.");
            }
            Tabuleiro alvo = tabuleiros[1 - i];
            ResultadoTiro resultado = alvo.receberTiro(x, y);
            numeroJogada++;
            Jogador adversario = jogadores[1 - i];
            Log.info("partida=%s v=%d %s atirou em (%d,%d): %s", idCurto(), numeroJogada,
                    jogadores[i].nome(), x, y, resultado.tipo());

            if (resultado.fimDeJogo()) {
                notificador.tiroSofrido(adversario, x, y, resultado, null);
                encerrar(jogadorId, MotivoFim.FROTA_DESTRUIDA);
                return resultado;
            }

            // regra do tiro em sequência: quem acerta continua jogando,
            // a vez só passa para o outro quando o tiro cai na água
            if (resultado.tipo() == TipoResultado.AGUA) {
                vez = 1 - i;
            }
            notificador.tiroSofrido(adversario, x, y, resultado, jogadores[vez].id());
            return resultado;
        } finally {
            lock.unlock();
        }
    }

    public EstadoPartida estadoPara(String jogadorId) throws JogadorInvalidoException {
        lock.lock();
        try {
            int i = indiceDo(jogadorId);
            String daVez = fase == Fase.BATALHA ? jogadores[vez].id() : null;
            return new EstadoPartida(id, fase, jogadores[1 - i].nome(), daVez, numeroJogada,
                    tabuleiros[i].visaoDoDono(), tabuleiros[1 - i].visaoDoAdversario(), vencedorId);
        } finally {
            lock.unlock();
        }
    }

    public void encerrarPorAbandono(String jogadorQueSaiu, MotivoFim motivo) {
        lock.lock();
        try {
            int i = indice(jogadorQueSaiu);
            if (i < 0 || fase == Fase.ENCERRADA) {
                return;
            }
            encerrar(jogadores[1 - i].id(), motivo);
        } finally {
            lock.unlock();
        }
    }

    // tira a partida e os dois jogadores do servidor na hora, pra liberar os nomes
    // e não sobrar partida esquecida na memória
    private void encerrar(String vencedor, MotivoFim motivo) {
        fase = Fase.ENCERRADA;
        vencedorId = vencedor;
        Log.info("partida=%s encerrada: venceu %s (%s)", idCurto(), nomeDe(vencedor), motivo);

        for (Jogador jogador : jogadores) {
            notificador.fimDePartida(jogador, vencedorId, motivo);
            jogador.setPartidaId(null);
            gerenciador.removerJogador(jogador);
            notificador.encerrarFila(jogador);
        }
        gerenciador.removerPartida(id);
    }

    private void exigirFase(Fase esperada) throws FaseInvalidaException {
        if (fase == esperada) {
            return;
        }
        if (fase == Fase.ENCERRADA) {
            throw new FaseInvalidaException("A partida já terminou.");
        }
        if (esperada == Fase.BATALHA) {
            throw new FaseInvalidaException("A batalha ainda não começou.");
        }
        throw new FaseInvalidaException("A fase de posicionamento já terminou.");
    }

    private int indiceDo(String jogadorId) throws JogadorInvalidoException {
        int i = indice(jogadorId);
        if (i < 0) {
            throw new JogadorInvalidoException("Jogador não pertence a esta partida.");
        }
        return i;
    }

    private int indice(String jogadorId) {
        for (int k = 0; k < jogadores.length; k++) {
            if (jogadores[k].id().equals(jogadorId)) {
                return k;
            }
        }
        return -1;
    }

    private String nomeDe(String jogadorId) {
        int i = indice(jogadorId);
        return i < 0 ? "?" : jogadores[i].nome();
    }

    private String idCurto() {
        return id.substring(0, 8);
    }
}
