package cliente;

import cliente.ui.JanelaPrincipal;
import comum.BatalhaNavalService;
import comum.ClienteCallbackInterface;
import comum.Fase;
import comum.ForaDoTurnoException;
import comum.JogadaInvalidaException;
import comum.JogoException;
import comum.MotivoFim;
import comum.Navio;
import comum.ResultadoTiro;

import javax.swing.SwingWorker;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class ControladorCliente {
    @FunctionalInterface
    private interface ChamadaRemota<T> {
        T executar() throws Exception;
    }

    private final BatalhaNavalService servico;
    private ClienteCallbackInterface callback;
    private JanelaPrincipal janela;

    private String jogadorId;
    private String partidaId;
    private String nomeAdversario;
    private Fase fase;
    private long versaoNaTela = -1;
    private boolean atirando;

    public ControladorCliente(BatalhaNavalService servico) {
        this.servico = servico;
    }

    public void setCallback(ClienteCallbackInterface callback) {
        this.callback = callback;
    }

    public void setJanela(JanelaPrincipal janela) {
        this.janela = janela;
    }

    public void entrarNoLobby(String nome) {
        janela.setEntrando(true);
        emSegundoPlano(() -> servico.entrarNoLobby(nome, callback),
                id -> {
                    jogadorId = id;
                    janela.setEntrando(false);
                    // o aviso de partida encontrada pode chegar antes dessa resposta.
                    // se chegou, a tela de posicionamento já está aberta e não pode ser trocada
                    if (partidaId == null) {
                        janela.mostrarAguardando("Procurando um adversário...");
                    }
                },
                erro -> {
                    janela.setEntrando(false);
                    janela.mostrarErro(mensagem(erro));
                });
    }

    public void enviarFrota(List<Navio> frota) {
        String partida = partidaId;
        janela.setEnviandoFrota(true);
        emSegundoPlano(() -> {
                    servico.posicionarFrota(partida, jogadorId, frota);
                    return null;
                },
                ok -> {
                    // mesmo caso do lobby: se eu fui o segundo a mandar, o inicioDaBatalha pode ter chegado antes
                    if (partida.equals(partidaId) && fase == Fase.POSICIONAMENTO) {
                        janela.mostrarAguardando("Frota enviada. Esperando " + nomeAdversario + " posicionar a dele...");
                    }
                },
                erro -> {
                    janela.setEnviandoFrota(false);
                    janela.mostrarErro(mensagem(erro));
                });
    }

    public void atirar(int x, int y) {
        if (fase != Fase.BATALHA || atirando) {
            return;
        }
        atirando = true;
        String partida = partidaId;
        emSegundoPlano(() -> servico.dispararTiro(partida, jogadorId, x, y),
                resultado -> {
                    atirando = false;
                    if (!partida.equals(partidaId)) {
                        return;
                    }
                    janela.registrarMeuTiro(resultado, nomeAdversario);
                    // no último tiro o servidor já apagou a partida, então não tem estado pra buscar.
                    // o fimDePartida chega logo em seguida pelo callback
                    if (!resultado.fimDeJogo()) {
                        atualizarEstado();
                    }
                },
                erro -> {
                    atirando = false;
                    if (erro instanceof ForaDoTurnoException) {
                        janela.adicionarLog("Ainda não é a sua vez.");
                    } else if (erro instanceof JogadaInvalidaException) {
                        janela.adicionarLog(erro.getMessage());
                    } else {
                        janela.mostrarErro(mensagem(erro));
                    }
                });
    }

    public void desistir() {
        String id = jogadorId;
        limpar();
        janela.mostrarLobby();
        if (id != null) {
            emSegundoPlano(() -> {
                servico.sair(id);
                return null;
            }, ok -> { }, erro -> { });
        }
    }

    public void fecharPrograma() {
        String id = jogadorId;
        if (id != null) {
            // avisa o servidor antes de fechar, senão o adversário só fica sabendo pelo heartbeat
            Thread aviso = new Thread(() -> {
                try {
                    servico.sair(id);
                } catch (RemoteException ignorada) {
                }
            });
            aviso.start();
            try {
                aviso.join(2_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.exit(0);
    }

    void aoEncontrarPartida(String partidaId, String nomeAdversario) {
        this.partidaId = partidaId;
        this.nomeAdversario = nomeAdversario;
        fase = Fase.POSICIONAMENTO;
        versaoNaTela = -1;
        janela.mostrarPosicionamento(nomeAdversario);
    }

    void aoIniciarBatalha(String jogadorDaVezId) {
        if (fase != Fase.POSICIONAMENTO) {
            return;
        }
        fase = Fase.BATALHA;
        janela.iniciarBatalha(nomeAdversario, jogadorDaVezId.equals(jogadorId));
        atualizarEstado();
    }

    void aoSofrerTiro(int x, int y, ResultadoTiro resultado, String novoTurno) {
        if (fase != Fase.BATALHA) {
            return;
        }
        janela.registrarTiroSofrido(x, y, resultado, nomeAdversario);
        if (!resultado.fimDeJogo()) {
            janela.setTurno(jogadorId != null && jogadorId.equals(novoTurno));
            atualizarEstado();
        }
    }

    void aoTerminarPartida(String vencedorId, MotivoFim motivo) {
        if (fase == null) {
            return;
        }
        boolean venci = jogadorId != null && jogadorId.equals(vencedorId);
        String adversario = nomeAdversario;
        fase = Fase.ENCERRADA;
        janela.mostrarFimDePartida(venci, motivo, adversario);
        // quando a partida acaba o servidor já tira o jogador da lista, por isso não precisa chamar sair()
        limpar();
        janela.mostrarLobby();
    }

    private void atualizarEstado() {
        String partida = partidaId;
        String jogador = jogadorId;
        emSegundoPlano(() -> servico.obterEstado(partida, jogador),
                estado -> {
                    // as respostas do obterEstado podem chegar fora de ordem quando os tiros são rápidos.
                    // a versão vem do servidor e só aumenta, então uma resposta mais velha é ignorada
                    if (!partida.equals(partidaId) || fase != Fase.BATALHA || estado.versao() < versaoNaTela) {
                        return;
                    }
                    versaoNaTela = estado.versao();
                    janela.mostrarEstado(estado, jogador.equals(estado.jogadorDaVezId()));
                },
                erro -> {
                    if (erro instanceof RemoteException) {
                        janela.mostrarErro(mensagem(erro));
                    }
                });
    }

    private void limpar() {
        jogadorId = null;
        partidaId = null;
        nomeAdversario = null;
        fase = null;
        versaoNaTela = -1;
        atirando = false;
    }

    // toda chamada ao servidor roda fora da thread da tela, senão a janela congela esperando a rede.
    // o done() do SwingWorker já volta pra thread da tela, então ali pode mexer nos componentes
    private <T> void emSegundoPlano(ChamadaRemota<T> chamada, Consumer<T> aoTerminar, Consumer<Exception> aoFalhar) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return chamada.executar();
            }

            @Override
            protected void done() {
                try {
                    aoTerminar.accept(get());
                } catch (ExecutionException e) {
                    aoFalhar.accept(e.getCause() instanceof Exception causa ? causa : e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private static String mensagem(Exception erro) {
        if (erro instanceof JogoException) {
            return erro.getMessage();
        }
        if (erro instanceof RemoteException) {
            return "Sem comunicação com o servidor (" + erro.getClass().getSimpleName() + ").";
        }
        return erro.toString();
    }
}
