package comum;

/** Nome vazio, longo demais ou já em uso. */
public class NomeInvalidoException extends JogoException {
    private static final long serialVersionUID = 1L;

    public NomeInvalidoException(String mensagem) {
        super(mensagem);
    }
}
