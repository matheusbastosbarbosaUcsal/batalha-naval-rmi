package cliente.ui;

import cliente.ControladorCliente;
import comum.Constantes;
import comum.EstadoCelula;
import comum.EstadoPartida;
import comum.MotivoFim;
import comum.Navio;
import comum.Orientacao;
import comum.ResultadoTiro;
import comum.TipoNavio;
import comum.TipoResultado;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class JanelaPrincipal extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final int N = Constantes.TAMANHO_TABULEIRO;

    private static final String LOBBY = "LOBBY";
    private static final String AGUARDANDO = "AGUARDANDO";
    private static final String POSICIONAMENTO = "POSICIONAMENTO";
    private static final String BATALHA = "BATALHA";

    private static final Color COR_FUNDO = new Color(0x0A0F1D);
    private static final Color COR_PAINEL = new Color(0x131C31);
    private static final Color COR_BORDA = new Color(0x283A5E);
    private static final Color COR_TEXTO = new Color(0xECF0F1);
    private static final Color COR_TEXTO_FRACO = new Color(0x94A3B8);
    private static final Color COR_GRADE = new Color(0x1E293B);
    private static final Color COR_NAVIO = new Color(0x0284C7);
    private static final Color COR_AGUA = new Color(0x0C4A6E);
    private static final Color COR_PINGO_AGUA = new Color(0x38BDF8);
    private static final Color COR_ACERTO = new Color(0xEF4444);
    private static final Color COR_MIRA = new Color(0x334155);
    private static final Color COR_PREVIA_OK = new Color(0x10B981);
    private static final Color COR_PREVIA_RUIM = new Color(0xB91C1C);
    private static final Color COR_REMOVER = new Color(0x7DD3FC);
    private static final Color COR_AZUL = new Color(0x0284C7);
    private static final Color COR_VERDE = new Color(0x0D9488);
    private static final Color COR_CINZA = new Color(0x334155);
    private static final Color COR_VERMELHO = new Color(0xBE123C);
    private static final Color COR_VEZ = new Color(0x34D399);
    private static final Color COR_ESPERA = new Color(0xFBBF24);

    private final transient ControladorCliente controlador;
    private final CardLayout cartoes = new CardLayout();
    private final JPanel telas = new JPanel(cartoes);
    private boolean emPartida;

    private JTextField campoNome;
    private JButton botaoEntrar;

    private JLabel textoAguardando;

    private final transient Map<TipoNavio, Navio> frota = new EnumMap<>(TipoNavio.class);
    private final Map<TipoNavio, JButton> botoesNavio = new EnumMap<>(TipoNavio.class);
    private final JButton[][] gradePosicionamento = new JButton[N][N];
    private TipoNavio navioSelecionado = TipoNavio.PORTA_AVIOES;
    private Orientacao orientacao = Orientacao.HORIZONTAL;
    private int mouseX = -1;
    private int mouseY = -1;
    private JPanel telaPosicionamento;
    private JLabel tituloPosicionamento;
    private JLabel avisoPosicionamento;
    private JLabel contadorFrota;
    private JButton botaoGirar;
    private JButton botaoConfirmar;
    private boolean enviandoFrota;

    private final JButton[][] gradeMinha = new JButton[N][N];
    private final JButton[][] gradeRadar = new JButton[N][N];
    private final EstadoCelula[][] radar = new EstadoCelula[N][N];
    private final List<String> afundeiDele = new ArrayList<>();
    private final List<String> perdiMeus = new ArrayList<>();
    private String adversario = "";
    private boolean minhaVez;
    private JLabel textoAdversario;
    private JLabel textoVez;
    private JLabel tituloRadar;
    private JTextArea placar;
    private JTextArea log;

    public JanelaPrincipal(ControladorCliente controlador, String ipServidor) {
        super("Batalha Naval RMI");
        this.controlador = controlador;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controlador.fecharPrograma();
            }
        });
        setSize(1150, 760);
        setMinimumSize(new Dimension(1000, 660));
        setLocationRelativeTo(null);

        telas.add(criarLobby(ipServidor), LOBBY);
        telas.add(criarAguardando(), AGUARDANDO);
        telas.add(criarPosicionamento(), POSICIONAMENTO);
        telas.add(criarBatalha(), BATALHA);
        add(telas);
        mostrarLobby();
    }

    public void mostrarLobby() {
        emPartida = false;
        setEntrando(false);
        cartoes.show(telas, LOBBY);
        SwingUtilities.invokeLater(campoNome::requestFocusInWindow);
    }

    public void setEntrando(boolean entrando) {
        campoNome.setEnabled(!entrando);
        botaoEntrar.setEnabled(!entrando);
        botaoEntrar.setText(entrando ? "Conectando..." : "Entrar no lobby");
    }

    public void mostrarAguardando(String mensagem) {
        textoAguardando.setText(mensagem);
        cartoes.show(telas, AGUARDANDO);
    }

    public void mostrarPosicionamento(String nomeAdversario) {
        emPartida = true;
        adversario = nomeAdversario;
        frota.clear();
        navioSelecionado = TipoNavio.PORTA_AVIOES;
        orientacao = Orientacao.HORIZONTAL;
        mouseX = -1;
        mouseY = -1;
        tituloPosicionamento.setText("Adversário: " + nomeAdversario + ". Posicione sua frota");
        avisoPosicionamento.setText(" ");
        setEnviandoFrota(false);
        atualizarPosicionamento();
        cartoes.show(telas, POSICIONAMENTO);
    }

    public void setEnviandoFrota(boolean enviando) {
        enviandoFrota = enviando;
        liberarConfirmar();
        botaoConfirmar.setText(enviando ? "Enviando..." : "Confirmar frota");
    }

    public void iniciarBatalha(String nomeAdversario, boolean comecoEu) {
        adversario = nomeAdversario;
        afundeiDele.clear();
        perdiMeus.clear();
        log.setText("");

        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                radar[y][x] = EstadoCelula.DESCONHECIDA;
                pintarRadar(x, y);
                pintarMinha(x, y, EstadoCelula.VAZIA);
            }
        }
        for (Navio navio : frota.values()) {
            for (int[] c : celulas(navio)) {
                pintarMinha(c[0], c[1], EstadoCelula.NAVIO);
            }
        }

        textoAdversario.setText("Adversário: " + nomeAdversario);
        tituloRadar.setText("Tabuleiro de " + nomeAdversario + " (clique para atirar)");
        atualizarPlacar();
        setTurno(comecoEu);
        adicionarLog("A batalha começou. " + (comecoEu ? "Você começa." : nomeAdversario + " começa."));
        adicionarLog("Quem acerta atira de novo. A vez só passa quando o tiro cai na água.");
        cartoes.show(telas, BATALHA);
    }

    public void mostrarEstado(EstadoPartida estado, boolean minhaVezAgora) {
        EstadoCelula[][] meu = estado.meuTabuleiro();
        EstadoCelula[][] dele = estado.tabuleiroAdversario();
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                pintarMinha(x, y, meu[y][x]);
                radar[y][x] = dele[y][x];
                pintarRadar(x, y);
            }
        }
        setTurno(minhaVezAgora);
    }

    public void registrarMeuTiro(ResultadoTiro r, String nomeAdversario) {
        radar[r.y()][r.x()] = r.tipo() == TipoResultado.AGUA ? EstadoCelula.AGUA : EstadoCelula.ACERTO;
        pintarRadar(r.x(), r.y());
        String casa = casa(r.x(), r.y());

        switch (r.tipo()) {
            case AGUA -> {
                adicionarLog("Você atirou em " + casa + ": água. Vez de " + nomeAdversario + ".");
                setTurno(false);
            }
            case ACERTO -> {
                adicionarLog("Você acertou " + casa + "! Atire de novo.");
                setTurno(true);
            }
            case AFUNDOU -> {
                afundeiDele.add(nomeDe(r.navioAfundado()));
                atualizarPlacar();
                if (r.fimDeJogo()) {
                    adicionarLog("Você afundou o " + nomeDe(r.navioAfundado()) + " em " + casa + ". Era o último navio!");
                    fimDeJogoNaTela();
                } else {
                    adicionarLog("Você afundou o " + nomeDe(r.navioAfundado()) + " em " + casa + "! Atire de novo.");
                    setTurno(true);
                }
            }
        }
    }

    public void registrarTiroSofrido(int x, int y, ResultadoTiro r, String nomeAdversario) {
        pintarMinha(x, y, r.tipo() == TipoResultado.AGUA ? EstadoCelula.AGUA : EstadoCelula.ACERTO);
        String casa = casa(x, y);

        switch (r.tipo()) {
            case AGUA -> adicionarLog(nomeAdversario + " atirou em " + casa + " e errou. Sua vez.");
            case ACERTO -> adicionarLog(nomeAdversario + " acertou seu navio em " + casa + " e atira de novo.");
            case AFUNDOU -> {
                perdiMeus.add(nomeDe(r.navioAfundado()));
                atualizarPlacar();
                adicionarLog(nomeAdversario + " afundou seu " + nomeDe(r.navioAfundado()) + " em " + casa
                        + (r.fimDeJogo() ? "." : " e atira de novo."));
                if (r.fimDeJogo()) {
                    fimDeJogoNaTela();
                }
            }
        }
    }

    public void setTurno(boolean minha) {
        minhaVez = minha;
        textoVez.setText(minha ? "Sua vez de atirar" : "Vez de " + adversario);
        textoVez.setForeground(minha ? COR_VEZ : COR_ESPERA);
        Cursor cursor = Cursor.getPredefinedCursor(minha ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR);
        for (JButton[] linha : gradeRadar) {
            for (JButton celula : linha) {
                celula.setCursor(cursor);
            }
        }
    }

    public void adicionarLog(String texto) {
        log.append(texto + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    public void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(this, mensagem, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    public void mostrarFimDePartida(boolean venci, MotivoFim motivo, String nomeAdversario) {
        String mensagem = switch (motivo) {
            case FROTA_DESTRUIDA -> venci
                    ? "Você venceu! Afundou toda a frota de " + nomeAdversario + "."
                    : "Você perdeu. " + nomeAdversario + " afundou toda a sua frota.";
            case DESISTENCIA -> venci
                    ? "Você venceu! " + nomeAdversario + " desistiu da partida."
                    : "Partida encerrada.";
            case DESCONEXAO -> venci
                    ? "Você venceu! " + nomeAdversario + " caiu da partida."
                    : "Partida encerrada por falha de conexão.";
        };
        JOptionPane.showMessageDialog(this, mensagem, "Fim de jogo", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel criarLobby(String ipServidor) {
        JPanel tela = fundo(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.insets = new Insets(10, 10, 10, 10);

        tela.add(texto("BATALHA NAVAL", Font.BOLD, 30, COR_TEXTO), g);
        g.gridy++;
        tela.add(texto("Servidor: " + ipServidor, Font.PLAIN, 14, COR_TEXTO_FRACO), g);

        JPanel formulario = cartao(new GridBagLayout());
        formulario.setBorder(new CompoundBorder(new LineBorder(COR_BORDA, 1, true), new EmptyBorder(20, 25, 20, 25)));
        GridBagConstraints f = new GridBagConstraints();
        f.gridx = 0;
        f.gridy = 0;
        f.insets = new Insets(6, 6, 6, 6);
        f.anchor = GridBagConstraints.WEST;

        formulario.add(texto("Seu nome", Font.BOLD, 14, COR_TEXTO), f);
        f.gridy++;
        campoNome = new JTextField(18);
        campoNome.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        campoNome.setBackground(COR_FUNDO);
        campoNome.setForeground(COR_TEXTO);
        campoNome.setCaretColor(COR_TEXTO);
        campoNome.setBorder(new CompoundBorder(new LineBorder(COR_BORDA, 1), new EmptyBorder(6, 8, 6, 8)));
        campoNome.addActionListener(e -> entrar());
        formulario.add(campoNome, f);
        f.gridy++;
        formulario.add(texto("até " + Constantes.TAMANHO_MAXIMO_NOME + " caracteres", Font.PLAIN, 12, COR_TEXTO_FRACO), f);

        f.gridy++;
        f.anchor = GridBagConstraints.CENTER;
        botaoEntrar = botao("Entrar no lobby", COR_AZUL);
        botaoEntrar.addActionListener(e -> entrar());
        formulario.add(botaoEntrar, f);

        g.gridy++;
        tela.add(formulario, g);
        return tela;
    }

    private void entrar() {
        String nome = campoNome.getText().strip();
        if (nome.isEmpty()) {
            mostrarErro("Digite um nome para entrar.");
            return;
        }
        controlador.entrarNoLobby(nome);
    }

    private JPanel criarAguardando() {
        JPanel tela = fundo(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.insets = new Insets(15, 15, 15, 15);

        textoAguardando = texto("", Font.BOLD, 18, COR_TEXTO);
        tela.add(textoAguardando, g);
        g.gridy++;
        JButton cancelar = botao("Cancelar", COR_CINZA);
        cancelar.addActionListener(e -> confirmarDesistencia());
        tela.add(cancelar, g);
        return tela;
    }

    private JPanel criarPosicionamento() {
        telaPosicionamento = fundo(new BorderLayout(15, 15));
        telaPosicionamento.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topo = new JPanel(new GridLayout(3, 1, 0, 4));
        topo.setOpaque(false);
        tituloPosicionamento = texto("", Font.BOLD, 18, COR_TEXTO);
        topo.add(tituloPosicionamento);
        topo.add(texto("Clique no tabuleiro para colocar o navio. Clique num navio já colocado para tirar. "
                + "R ou botão direito gira.", Font.PLAIN, 13, COR_TEXTO_FRACO));
        avisoPosicionamento = texto(" ", Font.BOLD, 13, COR_ESPERA);
        topo.add(avisoPosicionamento);
        telaPosicionamento.add(topo, BorderLayout.NORTH);

        JPanel tabuleiro = cartao(new BorderLayout());
        tabuleiro.add(grade(gradePosicionamento), BorderLayout.CENTER);
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                int cx = x;
                int cy = y;
                JButton celula = gradePosicionamento[y][x];
                celula.addActionListener(e -> clicarPosicionamento(cx, cy));
                celula.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        mouseX = cx;
                        mouseY = cy;
                        atualizarPosicionamento();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        mouseX = -1;
                        mouseY = -1;
                        atualizarPosicionamento();
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            girar();
                        }
                    }
                });
            }
        }
        telaPosicionamento.add(tabuleiro, BorderLayout.CENTER);

        JPanel lateral = cartao(null);
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setPreferredSize(new Dimension(280, 0));

        lateral.add(texto("Navios", Font.BOLD, 14, COR_TEXTO));
        lateral.add(Box.createVerticalStrut(8));
        for (TipoNavio tipo : TipoNavio.values()) {
            JButton botao = botao("", COR_GRADE);
            botao.setToolTipText("Clique para escolher. Se já estiver no tabuleiro, ele sai para você colocar de novo.");
            botao.addActionListener(e -> escolherNavio(tipo));
            botoesNavio.put(tipo, botao);
            lateral.add(larguraCheia(botao));
            lateral.add(Box.createVerticalStrut(6));
        }
        contadorFrota = texto("", Font.PLAIN, 13, COR_TEXTO_FRACO);
        lateral.add(contadorFrota);

        lateral.add(Box.createVerticalStrut(18));
        botaoGirar = botao("", COR_CINZA);
        botaoGirar.addActionListener(e -> girar());
        lateral.add(larguraCheia(botaoGirar));
        lateral.add(Box.createVerticalStrut(8));
        JButton aleatoria = botao("Posição aleatória", COR_VERDE);
        aleatoria.addActionListener(e -> posicaoAleatoria());
        lateral.add(larguraCheia(aleatoria));
        lateral.add(Box.createVerticalStrut(8));
        JButton limpar = botao("Limpar tabuleiro", COR_CINZA);
        limpar.addActionListener(e -> {
            frota.clear();
            navioSelecionado = TipoNavio.PORTA_AVIOES;
            atualizarPosicionamento();
        });
        lateral.add(larguraCheia(limpar));

        lateral.add(Box.createVerticalGlue());
        botaoConfirmar = botao("Confirmar frota", COR_AZUL);
        botaoConfirmar.addActionListener(e -> controlador.enviarFrota(new ArrayList<>(frota.values())));
        lateral.add(larguraCheia(botaoConfirmar));
        lateral.add(Box.createVerticalStrut(8));
        JButton sair = botao("Sair da partida", COR_VERMELHO);
        sair.addActionListener(e -> confirmarDesistencia());
        lateral.add(larguraCheia(sair));
        telaPosicionamento.add(lateral, BorderLayout.EAST);

        // a tecla R funciona com o foco em qualquer lugar da janela, mas só quando essa tela está aberta
        telaPosicionamento.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "girar");
        telaPosicionamento.getActionMap().put("girar", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (telaPosicionamento.isShowing()) {
                    girar();
                }
            }
        });
        return telaPosicionamento;
    }

    private void clicarPosicionamento(int x, int y) {
        avisoPosicionamento.setText(" ");
        TipoNavio jaColocado = navioEm(x, y);
        if (jaColocado != null) {
            frota.remove(jaColocado);
            navioSelecionado = jaColocado;
            atualizarPosicionamento();
            return;
        }
        if (navioSelecionado == null) {
            avisoPosicionamento.setText("Todos os navios já estão no tabuleiro. Confirme a frota ou clique num navio para mudar.");
            return;
        }
        Navio navio = new Navio(navioSelecionado, x, y, orientacao);
        if (!cabe(navio)) {
            avisoPosicionamento.setText("Não cabe aí: o navio sai do tabuleiro ou fica em cima de outro.");
            return;
        }
        frota.put(navioSelecionado, navio);
        navioSelecionado = proximoQueFalta();
        atualizarPosicionamento();
    }

    private void escolherNavio(TipoNavio tipo) {
        avisoPosicionamento.setText(" ");
        frota.remove(tipo);
        navioSelecionado = tipo;
        atualizarPosicionamento();
    }

    private void girar() {
        orientacao = orientacao == Orientacao.HORIZONTAL ? Orientacao.VERTICAL : Orientacao.HORIZONTAL;
        atualizarPosicionamento();
    }

    // sorteia posição e direção de cada navio e tenta de novo até caber.
    // são só 17 casas ocupadas num tabuleiro de 100, então acha rápido
    private void posicaoAleatoria() {
        avisoPosicionamento.setText(" ");
        frota.clear();
        Random sorteio = new Random();
        for (TipoNavio tipo : TipoNavio.values()) {
            Navio navio;
            do {
                Orientacao direcao = sorteio.nextBoolean() ? Orientacao.HORIZONTAL : Orientacao.VERTICAL;
                navio = new Navio(tipo, sorteio.nextInt(N), sorteio.nextInt(N), direcao);
            } while (!cabe(navio));
            frota.put(tipo, navio);
        }
        navioSelecionado = null;
        atualizarPosicionamento();
    }

    private void atualizarPosicionamento() {
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                pintar(gradePosicionamento[y][x], COR_GRADE, "", COR_TEXTO);
            }
        }
        for (Navio navio : frota.values()) {
            for (int[] c : celulas(navio)) {
                pintar(gradePosicionamento[c[1]][c[0]], COR_NAVIO, "", COR_TEXTO);
            }
        }

        // mostra onde o navio vai ficar antes do clique: verde se cabe, vermelho se não.
        // em cima de um navio já colocado, destaca ele, porque o clique ali tira o navio
        if (mouseX >= 0) {
            TipoNavio embaixo = navioEm(mouseX, mouseY);
            if (embaixo != null) {
                for (int[] c : celulas(frota.get(embaixo))) {
                    pintar(gradePosicionamento[c[1]][c[0]], COR_REMOVER, "", COR_FUNDO);
                }
            } else if (navioSelecionado != null) {
                Navio previa = new Navio(navioSelecionado, mouseX, mouseY, orientacao);
                Color cor = cabe(previa) ? COR_PREVIA_OK : COR_PREVIA_RUIM;
                for (int[] c : celulas(previa)) {
                    if (dentro(c[0], c[1])) {
                        pintar(gradePosicionamento[c[1]][c[0]], cor, "", COR_TEXTO);
                    }
                }
            }
        }

        for (TipoNavio tipo : TipoNavio.values()) {
            JButton botao = botoesNavio.get(tipo);
            boolean colocado = frota.containsKey(tipo);
            botao.setText(nomeDe(tipo) + " (" + tipo.tamanho + ")" + (colocado ? "  - colocado" : ""));
            botao.setBackground(tipo == navioSelecionado ? COR_AZUL : COR_GRADE);
            botao.setForeground(colocado ? COR_TEXTO_FRACO : COR_TEXTO);
        }
        botaoGirar.setText("Girar: " + (orientacao == Orientacao.HORIZONTAL ? "horizontal" : "vertical") + " (R)");
        contadorFrota.setText("Navios no tabuleiro: " + frota.size() + " de " + TipoNavio.values().length);
        liberarConfirmar();
    }

    private void liberarConfirmar() {
        boolean pode = !enviandoFrota && frotaCompleta();
        botaoConfirmar.setEnabled(pode);
        botaoConfirmar.setBackground(pode ? COR_AZUL : COR_CINZA);
    }

    private boolean frotaCompleta() {
        return frota.size() == TipoNavio.values().length;
    }

    private TipoNavio proximoQueFalta() {
        for (TipoNavio tipo : TipoNavio.values()) {
            if (!frota.containsKey(tipo)) {
                return tipo;
            }
        }
        return null;
    }

    // mesma regra do servidor: pode encostar, só não pode sobrepor nem sair do tabuleiro.
    // o servidor valida de novo quando a frota chega, isso aqui é só pra avisar o jogador antes
    private boolean cabe(Navio navio) {
        for (int[] c : celulas(navio)) {
            if (!dentro(c[0], c[1])) {
                return false;
            }
            TipoNavio ali = navioEm(c[0], c[1]);
            if (ali != null && ali != navio.tipo()) {
                return false;
            }
        }
        return true;
    }

    private TipoNavio navioEm(int x, int y) {
        for (Navio navio : frota.values()) {
            for (int[] c : celulas(navio)) {
                if (c[0] == x && c[1] == y) {
                    return navio.tipo();
                }
            }
        }
        return null;
    }

    private static List<int[]> celulas(Navio navio) {
        List<int[]> lista = new ArrayList<>();
        for (int i = 0; i < navio.tipo().tamanho; i++) {
            int x = navio.x() + (navio.orientacao() == Orientacao.HORIZONTAL ? i : 0);
            int y = navio.y() + (navio.orientacao() == Orientacao.VERTICAL ? i : 0);
            lista.add(new int[] { x, y });
        }
        return lista;
    }

    private static boolean dentro(int x, int y) {
        return x >= 0 && x < N && y >= 0 && y < N;
    }

    private JPanel criarBatalha() {
        JPanel tela = fundo(new BorderLayout(15, 15));
        tela.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topo = cartao(new BorderLayout(10, 0));
        textoAdversario = texto("", Font.PLAIN, 14, COR_TEXTO_FRACO);
        topo.add(textoAdversario, BorderLayout.WEST);
        textoVez = texto("", Font.BOLD, 20, COR_TEXTO);
        textoVez.setHorizontalAlignment(SwingConstants.CENTER);
        topo.add(textoVez, BorderLayout.CENTER);
        JButton desistir = botao("Desistir", COR_VERMELHO);
        desistir.addActionListener(e -> confirmarDesistencia());
        topo.add(desistir, BorderLayout.EAST);
        tela.add(topo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(1, 2, 25, 0));
        centro.setOpaque(false);

        JPanel minha = cartao(new BorderLayout(5, 5));
        minha.add(texto("Sua frota", Font.BOLD, 14, COR_TEXTO), BorderLayout.NORTH);
        minha.add(grade(gradeMinha), BorderLayout.CENTER);
        centro.add(minha);

        JPanel dele = cartao(new BorderLayout(5, 5));
        tituloRadar = texto("", Font.BOLD, 14, COR_TEXTO);
        dele.add(tituloRadar, BorderLayout.NORTH);
        dele.add(grade(gradeRadar), BorderLayout.CENTER);
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                int cx = x;
                int cy = y;
                JButton celula = gradeRadar[y][x];
                celula.addActionListener(e -> clicarRadar(cx, cy));
                celula.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (minhaVez && radar[cy][cx] == EstadoCelula.DESCONHECIDA) {
                            pintar(celula, COR_MIRA, "", COR_TEXTO);
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        pintarRadar(cx, cy);
                    }
                });
            }
        }
        centro.add(dele);
        tela.add(centro, BorderLayout.CENTER);

        JPanel baixo = new JPanel(new BorderLayout(15, 0));
        baixo.setOpaque(false);
        log = areaDeTexto(6);
        JScrollPane rolagem = new JScrollPane(log);
        rolagem.setBorder(new LineBorder(COR_BORDA, 1));
        JPanel caixaLog = cartao(new BorderLayout(0, 5));
        caixaLog.add(texto("Histórico da partida", Font.BOLD, 12, COR_TEXTO_FRACO), BorderLayout.NORTH);
        caixaLog.add(rolagem, BorderLayout.CENTER);
        baixo.add(caixaLog, BorderLayout.CENTER);

        placar = areaDeTexto(6);
        JPanel caixaPlacar = cartao(new BorderLayout(0, 5));
        caixaPlacar.setPreferredSize(new Dimension(300, 0));
        caixaPlacar.add(texto("Navios afundados", Font.BOLD, 12, COR_TEXTO_FRACO), BorderLayout.NORTH);
        caixaPlacar.add(placar, BorderLayout.CENTER);
        baixo.add(caixaPlacar, BorderLayout.EAST);
        tela.add(baixo, BorderLayout.SOUTH);
        return tela;
    }

    // o servidor confere a vez de novo. a checagem aqui é só pra não mandar tiro que já se sabe que vai voltar recusado
    private void clicarRadar(int x, int y) {
        if (!minhaVez) {
            adicionarLog("Espere a sua vez.");
            return;
        }
        if (radar[y][x] != EstadoCelula.DESCONHECIDA) {
            return;
        }
        controlador.atirar(x, y);
    }

    private void fimDeJogoNaTela() {
        minhaVez = false;
        textoVez.setText("Fim de jogo");
        textoVez.setForeground(COR_TEXTO);
    }

    private void atualizarPlacar() {
        int total = TipoNavio.values().length;
        placar.setText("Do adversário: " + afundeiDele.size() + " de " + total + "\n"
                + (afundeiDele.isEmpty() ? "nenhum ainda" : String.join(", ", afundeiDele)) + "\n\n"
                + "Seus: " + perdiMeus.size() + " de " + total + "\n"
                + (perdiMeus.isEmpty() ? "nenhum ainda" : String.join(", ", perdiMeus)));
    }

    private void confirmarDesistencia() {
        if (emPartida) {
            int resposta = JOptionPane.showConfirmDialog(this,
                    "Sair da partida? O adversário ganha por desistência.", "Desistir", JOptionPane.YES_NO_OPTION);
            if (resposta != JOptionPane.YES_OPTION) {
                return;
            }
        }
        controlador.desistir();
    }

    private void pintarMinha(int x, int y, EstadoCelula estado) {
        JButton celula = gradeMinha[y][x];
        switch (estado) {
            case NAVIO -> pintar(celula, COR_NAVIO, "", COR_TEXTO);
            case AGUA -> pintar(celula, COR_AGUA, "•", COR_PINGO_AGUA);
            case ACERTO -> pintar(celula, COR_ACERTO, "X", Color.WHITE);
            default -> pintar(celula, COR_GRADE, "", COR_TEXTO);
        }
    }

    private void pintarRadar(int x, int y) {
        JButton celula = gradeRadar[y][x];
        switch (radar[y][x]) {
            case AGUA -> pintar(celula, COR_AGUA, "•", COR_PINGO_AGUA);
            case ACERTO -> pintar(celula, COR_ACERTO, "X", Color.WHITE);
            default -> pintar(celula, COR_GRADE, "", COR_TEXTO);
        }
    }

    private JPanel grade(JButton[][] botoes) {
        JPanel grade = new JPanel(new GridLayout(N + 1, N + 1, 3, 3));
        grade.setOpaque(false);
        grade.setBorder(new EmptyBorder(6, 6, 6, 6));
        grade.add(new JLabel());
        for (int x = 0; x < N; x++) {
            grade.add(rotulo(String.valueOf(x + 1)));
        }
        for (int y = 0; y < N; y++) {
            grade.add(rotulo(String.valueOf((char) ('A' + y))));
            for (int x = 0; x < N; x++) {
                JButton celula = new JButton();
                celula.setFocusPainted(false);
                celula.setMargin(new Insets(0, 0, 0, 0));
                celula.setFont(new Font("Segoe UI", Font.BOLD, 16));
                celula.setBorder(new LineBorder(COR_BORDA, 1));
                pintar(celula, COR_GRADE, "", COR_TEXTO);
                botoes[y][x] = celula;
                grade.add(celula);
            }
        }
        return grade;
    }

    private static String casa(int x, int y) {
        return (char) ('A' + y) + String.valueOf(x + 1);
    }

    private static String nomeDe(TipoNavio tipo) {
        return switch (tipo) {
            case PORTA_AVIOES -> "Porta-aviões";
            case ENCOURACADO -> "Encouraçado";
            case CRUZADOR -> "Cruzador";
            case SUBMARINO -> "Submarino";
            case CONTRATORPEDEIRO -> "Contratorpedeiro";
        };
    }

    private static void pintar(JButton botao, Color fundo, String texto, Color corTexto) {
        botao.setBackground(fundo);
        botao.setText(texto);
        botao.setForeground(corTexto);
    }

    private static JPanel fundo(LayoutManager layout) {
        JPanel painel = new JPanel(layout);
        painel.setBackground(COR_FUNDO);
        return painel;
    }

    private static JPanel cartao(LayoutManager layout) {
        JPanel painel = layout == null ? new JPanel() : new JPanel(layout);
        painel.setBackground(COR_PAINEL);
        painel.setBorder(new CompoundBorder(new LineBorder(COR_BORDA, 1), new EmptyBorder(10, 12, 10, 12)));
        return painel;
    }

    private static JLabel texto(String conteudo, int estilo, int tamanho, Color cor) {
        JLabel rotulo = new JLabel(conteudo);
        rotulo.setFont(new Font("Segoe UI", estilo, tamanho));
        rotulo.setForeground(cor);
        return rotulo;
    }

    private static JLabel rotulo(String conteudo) {
        JLabel rotulo = texto(conteudo, Font.BOLD, 14, COR_TEXTO_FRACO);
        rotulo.setHorizontalAlignment(SwingConstants.CENTER);
        return rotulo;
    }

    private static JTextArea areaDeTexto(int linhas) {
        JTextArea area = new JTextArea(linhas, 30);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
        area.setBackground(COR_FUNDO);
        area.setForeground(COR_TEXTO);
        area.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        return area;
    }

    private static JButton botao(String conteudo, Color cor) {
        JButton botao = new JButton(conteudo);
        botao.setFont(new Font("Segoe UI", Font.BOLD, 13));
        botao.setBackground(cor);
        botao.setForeground(Color.WHITE);
        botao.setFocusPainted(false);
        botao.setBorder(new EmptyBorder(9, 16, 9, 16));
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return botao;
    }

    private static JButton larguraCheia(JButton botao) {
        botao.setAlignmentX(Component.LEFT_ALIGNMENT);
        botao.setHorizontalAlignment(SwingConstants.LEFT);
        botao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return botao;
    }
}
