package teste;

import comum.BatalhaNavalService;
import comum.ClienteCallbackInterface;
import comum.MotivoFim;
import comum.NomeInvalidoException;
import comum.ResultadoTiro;
import servidor.FabricaSocketServidor;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ClienteTeste implements ClienteCallbackInterface {
    final String nome;
    final BatalhaNavalService servico;
    final ClienteCallbackInterface stub;

    volatile String jogadorId;
    volatile String partidaId;
    volatile String adversario;
    volatile String jogadorDaVez;
    volatile String vencedor;
    volatile MotivoFim motivo;
    volatile CountDownLatch trava;

    final CountDownLatch pareado = new CountDownLatch(1);
    final CountDownLatch batalha = new CountDownLatch(1);
    final CountDownLatch fim = new CountDownLatch(1);
    final AtomicInteger tirosSofridos = new AtomicInteger();
    final AtomicInteger turnosErrados = new AtomicInteger();

    ClienteTeste(BatalhaNavalService servico, String nome) throws RemoteException {
        this.servico = servico;
        this.nome = nome;
        this.stub = (ClienteCallbackInterface) UnicastRemoteObject.exportObject(
                this, 0, null, new FabricaSocketServidor());
    }

    void entrar() throws RemoteException, NomeInvalidoException {
        jogadorId = servico.entrarNoLobby(nome, stub);
    }

    void sair() {
        try {
            if (jogadorId != null) {
                servico.sair(jogadorId);
            }
        } catch (RemoteException ignorada) {
        }
        derrubar();
    }

    void derrubar() {
        try {
            UnicastRemoteObject.unexportObject(this, true);
        } catch (NoSuchObjectException ignorada) {
        }
    }

    @Override
    public void partidaEncontrada(String partidaId, String nomeAdversario) {
        this.partidaId = partidaId;
        this.adversario = nomeAdversario;
        pareado.countDown();
    }

    @Override
    public void inicioDaBatalha(String jogadorDaVezId) {
        this.jogadorDaVez = jogadorDaVezId;
        batalha.countDown();
    }

    @Override
    public void notificarTiroSofrido(int x, int y, ResultadoTiro resultado, String novoTurno) {
        CountDownLatch t = trava;
        if (t != null) {
            try {
                t.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        tirosSofridos.incrementAndGet();
        if (novoTurno != null && !novoTurno.equals(jogadorId)) {
            turnosErrados.incrementAndGet();
        }
    }

    @Override
    public void fimDePartida(String vencedorId, MotivoFim motivo) {
        this.vencedor = vencedorId;
        this.motivo = motivo;
        fim.countDown();
    }

    @Override
    public void ping() {
    }
}
