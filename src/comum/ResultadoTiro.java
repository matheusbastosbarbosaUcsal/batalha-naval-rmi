package comum;

import java.io.Serializable;

/** Resultado de um tiro; navioAfundado é null quando tipo != AFUNDOU. */
public record ResultadoTiro(int x, int y, TipoResultado tipo,
                            TipoNavio navioAfundado, boolean fimDeJogo)
        implements Serializable {
}
