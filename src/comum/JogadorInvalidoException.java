package comum;

public class JogadorInvalidoException extends JogoException {
    private static final long serialVersionUID = 1L;

    public JogadorInvalidoException(String mensagem) {
        super(mensagem);
    }
}
