package comum;

import java.io.Serializable;

public record EstadoPartida(String partidaId, Fase fase, String nomeAdversario,
                            String jogadorDaVezId, long versao,
                            EstadoCelula[][] meuTabuleiro,
                            EstadoCelula[][] tabuleiroAdversario,
                            String vencedorId) implements Serializable {
}
