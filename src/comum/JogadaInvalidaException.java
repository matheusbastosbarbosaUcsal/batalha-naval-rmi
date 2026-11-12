package comum;

public class JogadaInvalidaException extends JogoException {
    private static final long serialVersionUID = 1L;

    public JogadaInvalidaException(String mensagem) {
        super(mensagem);
    }
}
