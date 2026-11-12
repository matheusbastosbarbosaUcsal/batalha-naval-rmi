package servidor;

import comum.MotivoFim;
import comum.ResultadoTiro;

/**
 * Envia os callbacks FORA do lock da partida, por uma fila de uma thread
 * por jogador (preserva a ordem e não deixa um cliente lento travar a partida).
 */
public class Notificador {

    public void partidaEncontrada(Jogador destino, String partidaId, String nomeAdversario) {
        // TODO: enfileirar na fila do jogador
    }

    public void inicioDaBatalha(Jogador destino, String jogadorDaVezId) {
        // TODO
    }

    public void tiroSofrido(Jogador destino, int x, int y, ResultadoTiro resultado, String novoTurno) {
        // TODO
    }

    public void fimDePartida(Jogador destino, String vencedorId, MotivoFim motivo) {
        // TODO
    }

    public void encerrarFila(Jogador jogador) {
        // TODO: desligar o executor do jogador
    }
}
