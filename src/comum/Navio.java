package comum;

import java.io.Serializable;

public record Navio(TipoNavio tipo, int x, int y, Orientacao orientacao)
        implements Serializable {
}
