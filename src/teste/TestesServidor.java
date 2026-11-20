package teste;

import comum.BatalhaNavalService;
import comum.Constantes;
import comum.EstadoCelula;
import comum.EstadoPartida;
import comum.FaseInvalidaException;
import comum.ForaDoTurnoException;
import comum.FrotaInvalidaException;
import comum.JogadaInvalidaException;
import comum.JogadorInvalidoException;
import comum.MotivoFim;
import comum.Navio;
import comum.NomeInvalidoException;
import comum.Orientacao;
import comum.PartidaNaoEncontradaException;
import comum.Rede;
import comum.ResultadoTiro;
import comum.TipoNavio;
import comum.TipoResultado;
import servidor.Log;
import servidor.Servidor;
import servidor.Tabuleiro;

import java.io.InvalidClassException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TestesServidor {
    private static final String IP = "127.0.0.1";
    private static final int[][] CELULAS_DA_FROTA = {
            {0, 0}, {1, 0}, {2, 0}, {3, 0}, {4, 0},
            {0, 2}, {1, 2}, {2, 2}, {3, 2},
            {9, 0}, {9, 1}, {9, 2},
            {5, 5}, {5, 6}, {5, 7},
            {8, 9}, {9, 9}};

    private static BatalhaNavalService servico;
    private static int ok;
    private static int falhas;

    @FunctionalInterface
    private interface Acao {
        void executar() throws Exception;
    }

    public static void main(String[] args) throws Exception {
        boolean log = false;
        String ipServidor = null;
        String meuIp = null;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--log" -> log = true;
                case "--servidor" -> ipServidor = valor(args, ++i);
                case "--meu-ip" -> meuIp = valor(args, ++i);
                default -> uso("opção desconhecida: " + args[i]);
            }
        }

        boolean remoto = ipServidor != null;
        String alvo = remoto ? ipServidor : IP;
        Log.setAtivo(log);
        Servidor servidor = null;
        if (remoto) {
            if (meuIp == null) {
                meuIp = Rede.ipLocalPara(alvo);
            }
            System.setProperty("java.rmi.server.hostname", meuIp);
            ClienteTeste.porta = Constantes.PORTA_CALLBACK;
            System.out.println("Servidor remoto: " + alvo + " | este computador: " + meuIp
                    + " (callbacks na porta " + Constantes.PORTA_CALLBACK + ")");
        } else {
            servidor = Servidor.iniciar(IP);
            System.out.println("Servidor local: " + IP);
        }
        servico = (BatalhaNavalService) Naming.lookup(Constantes.urlServico(alvo));

        try {
            testeTabuleiro();
            testeRegras();
            testeFiltro();
            testeEstresseDeTurno();
            testeEmparelhamento();
            testeDesconexao();
            testeClienteTravado();
        } catch (Exception e) {
            falhas++;
            System.out.println("  [FALHOU] teste interrompido: " + e);
        } finally {
            if (servidor != null) {
                servidor.parar();
            }
        }

        System.out.println();
        System.out.println(ok + " verificações ok, " + falhas + " falhas");
        System.exit(falhas == 0 ? 0 : 1);
    }

    private static String valor(String[] args, int i) {
        if (i >= args.length) {
            uso("faltou o valor de " + args[i - 1]);
        }
        return args[i];
    }

    private static void uso(String erro) {
        System.out.println(erro);
        System.out.println("uso: testar [--log] [--servidor IP] [--meu-ip IP]");
        System.exit(2);
    }

    private static void testeTabuleiro() throws Exception {
        titulo("1. Tabuleiro");
        checar("frota válida é aceita", !lanca(Exception.class, () -> new Tabuleiro().posicionar(frota())));
        checar("frota com 4 navios é recusada",
                lanca(FrotaInvalidaException.class, () -> new Tabuleiro().posicionar(frota().subList(0, 4))));

        List<Navio> repetida = new ArrayList<>(frota());
        repetida.set(4, new Navio(TipoNavio.SUBMARINO, 0, 8, Orientacao.HORIZONTAL));
        checar("navio repetido é recusado",
                lanca(FrotaInvalidaException.class, () -> new Tabuleiro().posicionar(repetida)));

        List<Navio> fora = new ArrayList<>(frota());
        fora.set(0, new Navio(TipoNavio.PORTA_AVIOES, 6, 0, Orientacao.HORIZONTAL));
        checar("navio fora do tabuleiro é recusado",
                lanca(FrotaInvalidaException.class, () -> new Tabuleiro().posicionar(fora)));

        List<Navio> sobreposta = new ArrayList<>(frota());
        sobreposta.set(3, new Navio(TipoNavio.SUBMARINO, 2, 0, Orientacao.VERTICAL));
        Tabuleiro t = new Tabuleiro();
        checar("navios sobrepostos são recusados",
                lanca(FrotaInvalidaException.class, () -> t.posicionar(sobreposta)));
        checar("frota recusada não deixa nada gravado", !t.frotaPosicionada());

        Tabuleiro alvo = new Tabuleiro();
        alvo.posicionar(frota());
        checar("tiro na água", alvo.receberTiro(5, 0).tipo() == TipoResultado.AGUA);
        checar("tiro no navio", alvo.receberTiro(8, 9).tipo() == TipoResultado.ACERTO);
        ResultadoTiro afundou = alvo.receberTiro(9, 9);
        checar("navio afundado", afundou.tipo() == TipoResultado.AFUNDOU
                && afundou.navioAfundado() == TipoNavio.CONTRATORPEDEIRO);
        checar("tiro repetido é recusado", lanca(JogadaInvalidaException.class, () -> alvo.receberTiro(9, 9)));
        checar("tiro fora do tabuleiro é recusado", lanca(JogadaInvalidaException.class, () -> alvo.receberTiro(10, 0)));
        checar("adversário não enxerga os navios",
                alvo.visaoDoAdversario()[0][0] == EstadoCelula.DESCONHECIDA
                        && alvo.visaoDoDono()[0][0] == EstadoCelula.NAVIO);
    }

    private static void testeRegras() throws Exception {
        titulo("2. Regras do jogo pela rede (RMI)");
        ClienteTeste ana = new ClienteTeste(servico, "Ana");
        ClienteTeste bruno = new ClienteTeste(servico, "Bruno");
        ClienteTeste intruso = new ClienteTeste(servico, "ana");

        checar("nome vazio é recusado",
                lanca(NomeInvalidoException.class, () -> servico.entrarNoLobby("   ", ana.stub)));
        checar("nome com mais de 20 letras é recusado",
                lanca(NomeInvalidoException.class, () -> servico.entrarNoLobby("x".repeat(21), ana.stub)));
        ana.entrar();
        checar("nome repetido é recusado (sem diferenciar maiúsculas)",
                lanca(NomeInvalidoException.class, intruso::entrar));
        bruno.entrar();
        aguardar(ana.pareado);
        aguardar(bruno.pareado);
        checar("os dois recebem partidaEncontrada com a mesma partida",
                ana.partidaId.equals(bruno.partidaId) && "Bruno".equals(ana.adversario) && "Ana".equals(bruno.adversario));

        String partida = ana.partidaId;
        checar("atirar antes da batalha é recusado",
                lanca(FaseInvalidaException.class, () -> servico.dispararTiro(partida, ana.jogadorId, 0, 0)));
        checar("frota inválida é recusada pela rede",
                lanca(FrotaInvalidaException.class,
                        () -> servico.posicionarFrota(partida, ana.jogadorId, new ArrayList<>(frota().subList(0, 3)))));
        checar("partida inexistente é recusada",
                lanca(PartidaNaoEncontradaException.class, () -> servico.posicionarFrota("inexistente", ana.jogadorId, frota())));
        checar("jogador de fora da partida é recusado",
                lanca(JogadorInvalidoException.class, () -> servico.posicionarFrota(partida, UUID.randomUUID().toString(), frota())));

        servico.posicionarFrota(partida, ana.jogadorId, frota());
        checar("enviar a frota duas vezes é recusado",
                lanca(FaseInvalidaException.class, () -> servico.posicionarFrota(partida, ana.jogadorId, frota())));
        servico.posicionarFrota(partida, bruno.jogadorId, frota());
        aguardar(ana.batalha);
        aguardar(bruno.batalha);
        checar("os dois recebem inicioDaBatalha com o mesmo jogador da vez",
                ana.jogadorDaVez.equals(bruno.jogadorDaVez));

        ClienteTeste primeiro = ana.jogadorId.equals(ana.jogadorDaVez) ? ana : bruno;
        ClienteTeste segundo = primeiro == ana ? bruno : ana;

        checar("tiro fora da vez é recusado",
                lanca(ForaDoTurnoException.class, () -> servico.dispararTiro(partida, segundo.jogadorId, 0, 0)));
        checar("tiro fora do tabuleiro é recusado",
                lanca(JogadaInvalidaException.class, () -> servico.dispararTiro(partida, primeiro.jogadorId, 10, 0)));

        EstadoPartida estado = servico.obterEstado(partida, primeiro.jogadorId);
        checar("obterEstado mostra meus navios e esconde os do adversário",
                estado.meuTabuleiro()[0][0] == EstadoCelula.NAVIO
                        && estado.tabuleiroAdversario()[0][0] == EstadoCelula.DESCONHECIDA);

        servico.dispararTiro(partida, primeiro.jogadorId, 5, 0);
        checar("tiro na água passa a vez para o adversário",
                lanca(ForaDoTurnoException.class, () -> servico.dispararTiro(partida, primeiro.jogadorId, 6, 0)));
        servico.dispararTiro(partida, segundo.jogadorId, 5, 0);

        int[] primeiraCelula = CELULAS_DA_FROTA[0];
        servico.dispararTiro(partida, primeiro.jogadorId, primeiraCelula[0], primeiraCelula[1]);
        checar("acerto mantém a vez com quem atirou",
                lanca(ForaDoTurnoException.class, () -> servico.dispararTiro(partida, segundo.jogadorId, 6, 0)));
        checar("tiro repetido é recusado e a vez continua com o jogador",
                lanca(JogadaInvalidaException.class,
                        () -> servico.dispararTiro(partida, primeiro.jogadorId, primeiraCelula[0], primeiraCelula[1])));

        // daqui pra frente só tem acerto, então o primeiro tem que conseguir atirar até o fim sem perder a vez
        ResultadoTiro ultimo = null;
        boolean sequenciaAceita = true;
        try {
            for (int k = 1; k < CELULAS_DA_FROTA.length; k++) {
                int[] c = CELULAS_DA_FROTA[k];
                ultimo = servico.dispararTiro(partida, primeiro.jogadorId, c[0], c[1]);
            }
        } catch (ForaDoTurnoException e) {
            sequenciaAceita = false;
        }
        checar("quem acerta atira de novo até afundar a frota inteira", sequenciaAceita);
        checar("último tiro afunda a frota e encerra o jogo",
                ultimo != null && ultimo.fimDeJogo() && ultimo.tipo() == TipoResultado.AFUNDOU);

        aguardar(primeiro.fim);
        aguardar(segundo.fim);
        checar("os dois recebem fimDePartida com o mesmo vencedor (FROTA_DESTRUIDA)",
                primeiro.jogadorId.equals(primeiro.vencedor) && primeiro.jogadorId.equals(segundo.vencedor)
                        && segundo.motivo == MotivoFim.FROTA_DESTRUIDA);
        checar("todas as notificações de tiro chegaram (18 e 1)",
                segundo.tirosSofridos.get() == 18 && primeiro.tirosSofridos.get() == 1);
        checar("cada notificação trouxe o jogador da vez certo",
                primeiro.turnosErrados.get() == 0 && segundo.turnosErrados.get() == 0);
        checar("partida encerrada é removida do servidor",
                lanca(PartidaNaoEncontradaException.class, () -> servico.obterEstado(partida, primeiro.jogadorId)));

        ClienteTeste revanche = new ClienteTeste(servico, "Ana");
        checar("o nome fica livre depois da partida", !lanca(Exception.class, revanche::entrar));

        revanche.sair();
        ana.derrubar();
        bruno.derrubar();
        intruso.derrubar();
    }

    private static void testeFiltro() throws Exception {
        titulo("3. Filtro de desserialização");
        List<Object> bruta = new ArrayList<>();
        bruta.add(new java.text.SimpleDateFormat());
        @SuppressWarnings("unchecked")
        List<Navio> maliciosa = (List<Navio>) (List<?>) bruta;

        Throwable erro = null;
        try {
            servico.posicionarFrota("x", "y", maliciosa);
        } catch (Throwable t) {
            erro = t;
        }
        // sem o filtro o objeto chegaria no método e a resposta seria PartidaNaoEncontradaException.
        // com o filtro a chamada falha antes, no RMI. em rede de verdade às vezes o servidor fecha a
        // conexão antes da resposta chegar, por isso aceita a InvalidClassException ou a conexão encerrada
        if (erro != null && !temCausa(erro, InvalidClassException.class)) {
            System.out.println("    recusada com: " + erro.getClass().getSimpleName()
                    + (erro.getCause() != null ? " <- " + erro.getCause().getClass().getSimpleName() : ""));
        }
        checar("classe fora do jogo é recusada antes de chegar no método", erro instanceof RemoteException);
        checar("o servidor continua respondendo depois da recusa",
                lanca(PartidaNaoEncontradaException.class, () -> servico.obterEstado("x", "y")));
    }

    private static void testeEstresseDeTurno() throws Exception {
        titulo("4. Estresse de turno: 50 threads por jogador atirando ao mesmo tempo");
        // 100 threads atirando ao mesmo tempo na mesma partida. se o lock da Partida não funcionasse
        // ia aparecer tiro aceito fora da vez, célula aceita duas vezes ou notificação faltando
        ClienteTeste[] par = parEmBatalha("Estresse");
        int threadsPorJogador = 50;

        AtomicInteger[] aceitos = { new AtomicInteger(), new AtomicInteger() };
        AtomicInteger[] naAgua = { new AtomicInteger(), new AtomicInteger() };
        List<Set<Integer>> alvos = List.of(ConcurrentHashMap.newKeySet(), ConcurrentHashMap.newKeySet());
        AtomicInteger aceitosRepetidos = new AtomicInteger();
        AtomicInteger foraDoTurno = new AtomicInteger();
        AtomicInteger celulaRepetida = new AtomicInteger();
        AtomicInteger inesperados = new AtomicInteger();
        AtomicBoolean acabou = new AtomicBoolean();
        CountDownLatch largada = new CountDownLatch(1);

        ExecutorService pool = Executors.newFixedThreadPool(threadsPorJogador * 2);
        for (int k = 0; k < 2; k++) {
            int jogador = k;
            ClienteTeste cliente = par[k];
            for (int t = 0; t < threadsPorJogador; t++) {
                pool.execute(() -> {
                    try {
                        largada.await();
                    } catch (InterruptedException e) {
                        return;
                    }
                    ThreadLocalRandom aleatorio = ThreadLocalRandom.current();
                    while (!acabou.get()) {
                        int x = aleatorio.nextInt(10);
                        int y = aleatorio.nextInt(10);
                        try {
                            ResultadoTiro r = servico.dispararTiro(cliente.partidaId, cliente.jogadorId, x, y);
                            aceitos[jogador].incrementAndGet();
                            if (r.tipo() == TipoResultado.AGUA) {
                                naAgua[jogador].incrementAndGet();
                            }
                            if (!alvos.get(jogador).add(y * 10 + x)) {
                                aceitosRepetidos.incrementAndGet();
                            }
                            if (r.fimDeJogo()) {
                                acabou.set(true);
                            }
                        } catch (ForaDoTurnoException e) {
                            foraDoTurno.incrementAndGet();
                        } catch (JogadaInvalidaException e) {
                            celulaRepetida.incrementAndGet();
                        } catch (FaseInvalidaException | PartidaNaoEncontradaException e) {
                            acabou.set(true);
                        } catch (Exception e) {
                            if (inesperados.incrementAndGet() == 1) {
                                System.out.println("    erro inesperado: " + e);
                            }
                            acabou.set(true);
                        }
                    }
                });
            }
        }
        long inicio = System.nanoTime();
        largada.countDown();
        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.MINUTES);
        long ms = (System.nanoTime() - inicio) / 1_000_000;

        aguardar(par[0].fim);
        aguardar(par[1].fim);
        int a = aceitos[0].get();
        int b = aceitos[1].get();
        int diferencaAgua = naAgua[0].get() - naAgua[1].get();
        System.out.printf("    tiros aceitos: %d e %d (na água: %d e %d) | recusados fora da vez: %d | célula repetida: %d | %d ms%n",
                a, b, naAgua[0].get(), naAgua[1].get(), foraDoTurno.get(), celulaRepetida.get(), ms);

        // cada vez de jogar termina com um tiro na água, menos a última, que termina no tiro que ganha.
        // então quem começou tem o mesmo número de tiros na água se ganhou, ou um a mais se perdeu
        boolean comecouGanhou = par[0].jogadorId.equals(par[0].vencedor);
        checar("houve disputa de verdade (tiros recusados por estar fora da vez)", foraDoTurno.get() > 0);
        checar("a vez só trocou nos tiros na água",
                (comecouGanhou && diferencaAgua == 0) || (!comecouGanhou && diferencaAgua == 1));
        checar("nenhuma célula foi aceita duas vezes", aceitosRepetidos.get() == 0);
        checar("cada tiro aceito gerou exatamente uma notificação no adversário",
                par[1].tirosSofridos.get() == a && par[0].tirosSofridos.get() == b);
        checar("toda notificação trouxe o jogador da vez certo",
                par[0].turnosErrados.get() == 0 && par[1].turnosErrados.get() == 0);
        checar("um único vencedor, igual para os dois",
                par[0].vencedor != null && par[0].vencedor.equals(par[1].vencedor)
                        && par[0].motivo == MotivoFim.FROTA_DESTRUIDA);
        checar("nenhum erro inesperado", inesperados.get() == 0);

        par[0].derrubar();
        par[1].derrubar();
    }

    private static void testeEmparelhamento() throws Exception {
        titulo("5. Emparelhamento: 100 clientes entrando no lobby ao mesmo tempo");
        int total = 100;
        List<ClienteTeste> clientes = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            clientes.add(new ClienteTeste(servico, "Jogador" + i));
        }

        CountDownLatch largada = new CountDownLatch(1);
        AtomicInteger erros = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(total);
        for (ClienteTeste cliente : clientes) {
            pool.execute(() -> {
                try {
                    largada.await();
                    cliente.entrar();
                } catch (Exception e) {
                    erros.incrementAndGet();
                }
            });
        }
        largada.countDown();
        pool.shutdown();
        pool.awaitTermination(1, TimeUnit.MINUTES);

        boolean todosPareados = true;
        for (ClienteTeste cliente : clientes) {
            todosPareados &= cliente.pareado.await(10, TimeUnit.SECONDS);
        }
        Map<String, List<ClienteTeste>> porPartida = new HashMap<>();
        for (ClienteTeste cliente : clientes) {
            if (cliente.partidaId != null) {
                porPartida.computeIfAbsent(cliente.partidaId, k -> new ArrayList<>()).add(cliente);
            }
        }
        boolean paresCorretos = porPartida.values().stream().allMatch(dupla -> dupla.size() == 2
                && dupla.get(0).nome.equals(dupla.get(1).adversario)
                && dupla.get(1).nome.equals(dupla.get(0).adversario));

        System.out.printf("    partidas criadas: %d%n", porPartida.size());
        checar("todos os 100 entraram sem erro", erros.get() == 0);
        checar("todos foram pareados", todosPareados);
        checar("exatamente 50 partidas", porPartida.size() == 50);
        checar("cada partida tem 2 jogadores diferentes e cada um conhece o outro", paresCorretos);

        clientes.forEach(ClienteTeste::sair);
    }

    private static void testeDesconexao() throws Exception {
        titulo("6. Desconexão no meio da partida");
        ClienteTeste[] par = parEmBatalha("Queda");
        long inicio = System.nanoTime();
        par[1].derrubar();

        boolean recebeu = par[0].fim.await(15, TimeUnit.SECONDS);
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        System.out.printf("    o adversário foi avisado em %d ms%n", ms);
        checar("quem ficou recebe fimDePartida", recebeu);
        checar("vence por W.O. (DESCONEXAO)",
                par[0].jogadorId.equals(par[0].vencedor) && par[0].motivo == MotivoFim.DESCONEXAO);
        checar("aviso em até 10 segundos", ms <= 10_000);
        par[0].derrubar();
    }

    private static void testeClienteTravado() throws Exception {
        titulo("7. Cliente travado não trava o servidor");
        // o callback do segundo jogador nunca responde. o tiro do primeiro tem que voltar na hora
        // (o aviso vai por outra thread) e o servidor derruba o travado pelo responseTimeout de 5 s
        ClienteTeste[] par = parEmBatalha("Travado");
        CountDownLatch trava = new CountDownLatch(1);
        par[1].trava = trava;

        long inicio = System.nanoTime();
        servico.dispararTiro(par[0].partidaId, par[0].jogadorId, 5, 0);
        long msTiro = (System.nanoTime() - inicio) / 1_000_000;
        System.out.printf("    resposta do tiro com o adversário travado: %d ms%n", msTiro);
        checar("quem atirou recebe a resposta na hora (menos de 1 segundo)", msTiro < 1_000);

        boolean recebeu = par[0].fim.await(20, TimeUnit.SECONDS);
        long msFim = (System.nanoTime() - inicio) / 1_000_000;
        System.out.printf("    partida encerrada por desconexão em %d ms%n", msFim);
        checar("o travado é desconectado pelo tempo-limite e o outro vence",
                recebeu && par[0].motivo == MotivoFim.DESCONEXAO && par[0].jogadorId.equals(par[0].vencedor));

        trava.countDown();
        par[0].derrubar();
        par[1].derrubar();
    }

    private static ClienteTeste[] parEmBatalha(String prefixo) throws Exception {
        ClienteTeste a = new ClienteTeste(servico, prefixo + "A");
        ClienteTeste b = new ClienteTeste(servico, prefixo + "B");
        a.entrar();
        b.entrar();
        aguardar(a.pareado);
        aguardar(b.pareado);
        servico.posicionarFrota(a.partidaId, a.jogadorId, frota());
        servico.posicionarFrota(b.partidaId, b.jogadorId, frota());
        aguardar(a.batalha);
        aguardar(b.batalha);
        return a.jogadorId.equals(a.jogadorDaVez) ? new ClienteTeste[] { a, b } : new ClienteTeste[] { b, a };
    }

    private static List<Navio> frota() {
        return List.of(
                new Navio(TipoNavio.PORTA_AVIOES, 0, 0, Orientacao.HORIZONTAL),
                new Navio(TipoNavio.ENCOURACADO, 0, 2, Orientacao.HORIZONTAL),
                new Navio(TipoNavio.CRUZADOR, 9, 0, Orientacao.VERTICAL),
                new Navio(TipoNavio.SUBMARINO, 5, 5, Orientacao.VERTICAL),
                new Navio(TipoNavio.CONTRATORPEDEIRO, 8, 9, Orientacao.HORIZONTAL));
    }

    private static void aguardar(CountDownLatch latch) throws InterruptedException {
        if (!latch.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("tempo esgotado esperando notificação do servidor");
        }
    }

    private static boolean lanca(Class<? extends Throwable> tipo, Acao acao) {
        try {
            acao.executar();
            return false;
        } catch (Throwable t) {
            return tipo.isInstance(t);
        }
    }

    private static boolean temCausa(Throwable erro, Class<? extends Throwable> tipo) {
        for (Throwable t = erro; t != null; t = t.getCause()) {
            if (tipo.isInstance(t)) {
                return true;
            }
        }
        return false;
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println(texto);
    }

    private static void checar(String descricao, boolean condicao) {
        if (condicao) {
            ok++;
            System.out.println("  [OK] " + descricao);
        } else {
            falhas++;
            System.out.println("  [FALHOU] " + descricao);
        }
    }
}
