package comum;

import java.io.Serializable;

// foto da partida vista por um jogador. no tabuleiroAdversario só aparece AGUA, ACERTO ou DESCONHECIDA
public record EstadoPartida(String partidaId, Fase fase, String nomeAdversario,
                            String jogadorDaVezId, long versao,
                            EstadoCelula[][] meuTabuleiro,
                            EstadoCelula[][] tabuleiroAdversario,
                            String vencedorId) implements Serializable {
}
