package servidor;

import comum.Constantes;
import comum.EstadoCelula;
import comum.FrotaInvalidaException;
import comum.JogadaInvalidaException;
import comum.Navio;
import comum.Orientacao;
import comum.ResultadoTiro;
import comum.TipoNavio;
import comum.TipoResultado;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Tabuleiro {
    private static final int N = Constantes.TAMANHO_TABULEIRO;

    private final TipoNavio[][] ocupacao = new TipoNavio[N][N];

    private final boolean[][] atingida = new boolean[N][N];

    private final Map<TipoNavio, Integer> celulasRestantes = new EnumMap<>(TipoNavio.class);

    private int naviosRestantes;
    private boolean posicionada;

    public void posicionar(List<Navio> frota) throws FrotaInvalidaException {
        if (posicionada) {
            throw new IllegalStateException("A frota deste tabuleiro já foi posicionada.");
        }
        if (frota == null || frota.size() != TipoNavio.values().length) {
            throw new FrotaInvalidaException(
                    "A frota deve ter exatamente " + TipoNavio.values().length + " navios.");
        }

        TipoNavio[][] grade = new TipoNavio[N][N];
        Map<TipoNavio, Integer> tamanhos = new EnumMap<>(TipoNavio.class);

        for (Navio navio : frota) {
            if (navio == null || navio.tipo() == null || navio.orientacao() == null) {
                throw new FrotaInvalidaException("A frota contém um navio incompleto.");
            }
            TipoNavio tipo = navio.tipo();
            if (tamanhos.containsKey(tipo)) {
                throw new FrotaInvalidaException("Navio repetido: " + nomeDe(tipo) + ".");
            }
            tamanhos.put(tipo, tipo.tamanho);

            for (int i = 0; i < tipo.tamanho; i++) {
                int x = navio.x() + (navio.orientacao() == Orientacao.HORIZONTAL ? i : 0);
                int y = navio.y() + (navio.orientacao() == Orientacao.VERTICAL ? i : 0);
                if (!dentro(x, y)) {
                    throw new FrotaInvalidaException(nomeDe(tipo) + " fica fora do tabuleiro.");
                }
                if (grade[y][x] != null) {
                    throw new FrotaInvalidaException(
                            nomeDe(tipo) + " sobrepõe " + nomeDe(grade[y][x]) + ".");
                }
                grade[y][x] = tipo;
            }
        }

        for (int y = 0; y < N; y++) {
            System.arraycopy(grade[y], 0, ocupacao[y], 0, N);
        }
        celulasRestantes.putAll(tamanhos);
        naviosRestantes = tamanhos.size();
        posicionada = true;
    }

    public ResultadoTiro receberTiro(int x, int y) throws JogadaInvalidaException {
        if (!posicionada) {
            throw new IllegalStateException("A frota ainda não foi posicionada.");
        }
        if (!dentro(x, y)) {
            throw new JogadaInvalidaException("Coordenada fora do tabuleiro.");
        }
        if (atingida[y][x]) {
            throw new JogadaInvalidaException("Essa posição já foi atingida.");
        }

        atingida[y][x] = true;
        TipoNavio alvo = ocupacao[y][x];

        if (alvo == null) {
            return new ResultadoTiro(x, y, TipoResultado.AGUA, null, false);
        }

        int restantes = celulasRestantes.merge(alvo, -1, Integer::sum);
        if (restantes > 0) {
            return new ResultadoTiro(x, y, TipoResultado.ACERTO, null, false);
        }

        naviosRestantes--;
        return new ResultadoTiro(x, y, TipoResultado.AFUNDOU, alvo, naviosRestantes == 0);
    }

    public boolean frotaPosicionada() {
        return posicionada;
    }

    public boolean frotaDestruida() {
        return posicionada && naviosRestantes == 0;
    }

    public EstadoCelula[][] visaoDoDono() {
        EstadoCelula[][] visao = new EstadoCelula[N][N];
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                boolean temNavio = ocupacao[y][x] != null;
                if (atingida[y][x]) {
                    visao[y][x] = temNavio ? EstadoCelula.ACERTO : EstadoCelula.AGUA;
                } else {
                    visao[y][x] = temNavio ? EstadoCelula.NAVIO : EstadoCelula.VAZIA;
                }
            }
        }
        return visao;
    }

    public EstadoCelula[][] visaoDoAdversario() {
        EstadoCelula[][] visao = new EstadoCelula[N][N];
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                if (!atingida[y][x]) {
                    visao[y][x] = EstadoCelula.DESCONHECIDA;
                } else {
                    visao[y][x] = ocupacao[y][x] != null ? EstadoCelula.ACERTO : EstadoCelula.AGUA;
                }
            }
        }
        return visao;
    }

    private static boolean dentro(int x, int y) {
        return x >= 0 && x < N && y >= 0 && y < N;
    }

    private static String nomeDe(TipoNavio tipo) {
        return switch (tipo) {
            case PORTA_AVIOES -> "Porta-aviões";
            case ENCOURACADO -> "Encouraçado";
            case CRUZADOR -> "Cruzador";
            case SUBMARINO -> "Submarino";
            case CONTRATORPEDEIRO -> "Contratorpedeiro";
        };
    }
}
