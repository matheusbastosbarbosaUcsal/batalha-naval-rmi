package comum;

import java.io.Serializable;

public record ResultadoTiro(int x, int y, TipoResultado tipo,
                            TipoNavio navioAfundado, boolean fimDeJogo)
        implements Serializable {
}
