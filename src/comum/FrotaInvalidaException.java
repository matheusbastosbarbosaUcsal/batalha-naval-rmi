package comum;

/** Frota incompleta, repetida, fora do tabuleiro ou com sobreposição. */
public class FrotaInvalidaException extends JogoException {
    private static final long serialVersionUID = 1L;

    public FrotaInvalidaException(String mensagem) {
        super(mensagem);
    }
}
