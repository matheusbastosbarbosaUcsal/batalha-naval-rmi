package comum;

/** Base das exceções de regra de negócio (verificadas e serializáveis). */
public abstract class JogoException extends Exception {
    private static final long serialVersionUID = 1L;

    protected JogoException(String mensagem) {
        super(mensagem);
    }
}
