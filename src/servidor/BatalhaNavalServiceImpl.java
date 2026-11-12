package servidor;

import comum.BatalhaNavalService;
import comum.ClienteCallbackInterface;
import comum.Constantes;
import comum.EstadoPartida;
import comum.FaseInvalidaException;
import comum.ForaDoTurnoException;
import comum.FrotaInvalidaException;
import comum.JogadaInvalidaException;
import comum.JogadorInvalidoException;
import comum.MotivoFim;
import comum.Navio;
import comum.NomeInvalidoException;
import comum.PartidaNaoEncontradaException;
import comum.ResultadoTiro;

import java.util.List;
import java.util.UUID;

public class BatalhaNavalServiceImpl implements BatalhaNavalService {
    private final GerenciadorPartidas gerenciador = new GerenciadorPartidas();
    private final Notificador notificador = new Notificador();
    private final Lobby lobby = new Lobby(gerenciador, notificador);
    private final Heartbeat heartbeat;

    public BatalhaNavalServiceImpl() {
        notificador.setAoFalhar(jogador -> desconectar(jogador, MotivoFim.DESCONEXAO));
        heartbeat = new Heartbeat(gerenciador, jogador -> desconectar(jogador, MotivoFim.DESCONEXAO));
    }

    public void iniciar() {
        heartbeat.iniciar();
    }

    public void parar() {
        heartbeat.parar();
        notificador.encerrarTudo();
    }

    @Override
    public String entrarNoLobby(String nomeJogador, ClienteCallbackInterface callback)
            throws NomeInvalidoException {
        String nome = nomeJogador == null ? "" : nomeJogador.strip();
        if (nome.isEmpty()) {
            throw new NomeInvalidoException("Informe um nome.");
        }
        if (nome.length() > Constantes.TAMANHO_MAXIMO_NOME) {
            throw new NomeInvalidoException(
                    "O nome pode ter no máximo " + Constantes.TAMANHO_MAXIMO_NOME + " caracteres.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("Callback não informado.");
        }

        String jogadorId = UUID.randomUUID().toString();
        if (!gerenciador.reservarNome(nome, jogadorId)) {
            throw new NomeInvalidoException("O nome \"" + nome + "\" já está em uso.");
        }
        Jogador jogador = new Jogador(jogadorId, nome, callback);
        gerenciador.registrarJogador(jogador);
        lobby.entrar(jogador);
        return jogadorId;
    }

    @Override
    public void posicionarFrota(String partidaId, String jogadorId, List<Navio> frota)
            throws PartidaNaoEncontradaException, JogadorInvalidoException,
                   FaseInvalidaException, FrotaInvalidaException {
        gerenciador.buscarPartida(partidaId).posicionarFrota(jogadorId, frota);
    }

    @Override
    public ResultadoTiro dispararTiro(String partidaId, String jogadorId, int x, int y)
            throws PartidaNaoEncontradaException, JogadorInvalidoException,
                   FaseInvalidaException, ForaDoTurnoException, JogadaInvalidaException {
        return gerenciador.buscarPartida(partidaId).processarTiro(jogadorId, x, y);
    }

    @Override
    public EstadoPartida obterEstado(String partidaId, String jogadorId)
            throws PartidaNaoEncontradaException, JogadorInvalidoException {
        return gerenciador.buscarPartida(partidaId).estadoPara(jogadorId);
    }

    @Override
    public void sair(String jogadorId) {
        Jogador jogador = gerenciador.buscarJogador(jogadorId);
        if (jogador != null) {
            desconectar(jogador, MotivoFim.DESISTENCIA);
        }
    }

    private void desconectar(Jogador jogador, MotivoFim motivo) {
        if (!jogador.marcarDesconectado()) {
            return;
        }
        Log.info("%s saiu (%s)", jogador.nome(), motivo);
        lobby.remover(jogador);

        Partida partida = gerenciador.partidaOuNull(jogador.partidaId());
        if (partida != null) {
            partida.encerrarPorAbandono(jogador.id(), motivo);
        }
        gerenciador.removerJogador(jogador);
        notificador.encerrarFila(jogador);
    }
}
