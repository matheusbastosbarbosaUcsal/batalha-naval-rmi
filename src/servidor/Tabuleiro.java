package servidor;

import comum.FrotaInvalidaException;
import comum.JogadaInvalidaException;
import comum.Navio;
import comum.ResultadoTiro;

import java.util.List;

/**
 * Tabuleiro 10x10 de um jogador. NÃO é thread-safe: só é acessado
 * por quem estiver com o lock da Partida dona dele.
 */
public class Tabuleiro {

    public void posicionar(List<Navio> frota) throws FrotaInvalidaException {
        // TODO: conferir 1 navio de cada tipo, limites e sobreposição; ocupar as células.
        throw new UnsupportedOperationException("Tabuleiro.posicionar ainda não implementado");
    }

    public ResultadoTiro receberTiro(int x, int y) throws JogadaInvalidaException {
        // TODO: validar coordenada e repetição; marcar; calcular AGUA/ACERTO/AFUNDOU e fim de jogo.
        throw new UnsupportedOperationException("Tabuleiro.receberTiro ainda não implementado");
    }

    public boolean frotaPosicionada() {
        return false; // TODO
    }

    public boolean frotaDestruida() {
        return false; // TODO
    }
}
