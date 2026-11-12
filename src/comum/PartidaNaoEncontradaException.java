package comum;

/** partidaId inexistente ou de partida já encerrada e removida. */
public class PartidaNaoEncontradaException extends JogoException {
    private static final long serialVersionUID = 1L;

    public PartidaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
