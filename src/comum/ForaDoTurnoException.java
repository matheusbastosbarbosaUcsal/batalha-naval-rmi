package comum;

/** Tiro enviado quando não é a vez do jogador. */
public class ForaDoTurnoException extends JogoException {
    private static final long serialVersionUID = 1L;

    public ForaDoTurnoException(String mensagem) {
        super(mensagem);
    }
}
