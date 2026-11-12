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
import comum.Navio;
import comum.NomeInvalidoException;
import comum.PartidaNaoEncontradaException;
import comum.ResultadoTiro;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

/**
 * Implementação do objeto remoto do servidor (fachada).
 * Cada chamada chega em uma thread do runtime RMI; o estado de cada partida
 * fica protegido pelo lock da própria Partida (Seção 4 do relatório).
 */
public class BatalhaNavalServiceImpl extends UnicastRemoteObject implements BatalhaNavalService {

    private static final long serialVersionUID = 1L;

    private final transient GerenciadorPartidas gerenciador = new GerenciadorPartidas();
    private final transient Notificador notificador = new Notificador();
    private final transient Lobby lobby = new Lobby(gerenciador, notificador);
    private final transient Heartbeat heartbeat = new Heartbeat(gerenciador);

    public BatalhaNavalServiceImpl() throws RemoteException {
        super(Constantes.PORTA_SERVICO);
        heartbeat.iniciar();
    }

    @Override
    public String entrarNoLobby(String nomeJogador, ClienteCallbackInterface callback)
            throws RemoteException, NomeInvalidoException {
        // TODO: validar nome, registrar Jogador e chamar lobby.entrar(...)
        throw new UnsupportedOperationException("entrarNoLobby ainda não implementado");
    }

    @Override
    public void posicionarFrota(String partidaId, String jogadorId, List<Navio> frota)
            throws RemoteException, PartidaNaoEncontradaException, JogadorInvalidoException,
                   FaseInvalidaException, FrotaInvalidaException {
        // TODO: gerenciador.buscarPartida(partidaId).posicionarFrota(jogadorId, frota)
        throw new UnsupportedOperationException("posicionarFrota ainda não implementado");
    }

    @Override
    public ResultadoTiro dispararTiro(String partidaId, String jogadorId, int x, int y)
            throws RemoteException, PartidaNaoEncontradaException, JogadorInvalidoException,
                   FaseInvalidaException, ForaDoTurnoException, JogadaInvalidaException {
        // TODO: gerenciador.buscarPartida(partidaId).processarTiro(jogadorId, x, y)
        throw new UnsupportedOperationException("dispararTiro ainda não implementado");
    }

    @Override
    public EstadoPartida obterEstado(String partidaId, String jogadorId)
            throws RemoteException, PartidaNaoEncontradaException, JogadorInvalidoException {
        // TODO: gerenciador.buscarPartida(partidaId).estadoPara(jogadorId)
        throw new UnsupportedOperationException("obterEstado ainda não implementado");
    }

    @Override
    public void sair(String jogadorId) throws RemoteException {
        // TODO: remover da fila ou encerrar a partida por DESISTENCIA (idempotente)
        throw new UnsupportedOperationException("sair ainda não implementado");
    }
}
