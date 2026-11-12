package comum;

import java.io.Serializable;

/** Um navio da frota: tipo, posição da proa (x = coluna, y = linha) e orientação. */
public record Navio(TipoNavio tipo, int x, int y, Orientacao orientacao)
        implements Serializable {
}
