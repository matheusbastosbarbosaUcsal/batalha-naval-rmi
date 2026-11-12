package comum;

import java.io.Serializable;

/**
 * Cópia do estado da partida visível para um jogador (retorno de obterEstado).
 * O servidor monta uma cópia nova a cada chamada.
 */
public record EstadoPartida(String partidaId, Fase fase, String nomeAdversario,
                            String jogadorDaVezId, long versao,
                            EstadoCelula[][] meuTabuleiro,
                            EstadoCelula[][] tabuleiroAdversario,
                            String vencedorId) implements Serializable {
}
