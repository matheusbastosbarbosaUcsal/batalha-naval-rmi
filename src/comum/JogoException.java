package comum;

public abstract class JogoException extends Exception {
    private static final long serialVersionUID = 1L;

    protected JogoException(String mensagem) {
        super(mensagem);
    }
}
