package comum;

/** Tipos de navio da frota e seus tamanhos (17 células no total). */
public enum TipoNavio {
    PORTA_AVIOES(5),
    ENCOURACADO(4),
    CRUZADOR(3),
    SUBMARINO(3),
    CONTRATORPEDEIRO(2);

    public final int tamanho;

    TipoNavio(int tamanho) {
        this.tamanho = tamanho;
    }
}
