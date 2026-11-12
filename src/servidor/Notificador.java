package servidor;

import comum.MotivoFim;
import comum.ResultadoTiro;

public class Notificador {
    public void partidaEncontrada(Jogador destino, String partidaId, String nomeAdversario) {
    }

    public void inicioDaBatalha(Jogador destino, String jogadorDaVezId) {
    }

    public void tiroSofrido(Jogador destino, int x, int y, ResultadoTiro resultado, String novoTurno) {
    }

    public void fimDePartida(Jogador destino, String vencedorId, MotivoFim motivo) {
    }

    public void encerrarFila(Jogador jogador) {
    }
}
