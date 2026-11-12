package comum;

/** Coordenada fora de 0 a 9 ou célula já atingida. */
public class JogadaInvalidaException extends JogoException {
    private static final long serialVersionUID = 1L;

    public JogadaInvalidaException(String mensagem) {
        super(mensagem);
    }
}
