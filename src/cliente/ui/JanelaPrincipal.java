package cliente.ui;

import cliente.ControladorCliente;
import comum.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class JanelaPrincipal extends JFrame {
    private static final long serialVersionUID = 1L;

    private final ControladorCliente controlador;
    private final String ipServidor;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel painelPrincipal = new JPanel(cardLayout);

    // Cores do Tema Naval / Militar Moderno
    private static final Color COR_BG_DARK = new Color(0x0A, 0x0F, 0x1D);
    private static final Color COR_PANEL = new Color(0x13, 0x1C, 0x31);
    private static final Color COR_BORDER = new Color(0x28, 0x3A, 0x5E);
    private static final Color COR_TEXTO = new Color(0xEC, 0xF0, 0xF1);
    private static final Color COR_TEXTO_MUTED = new Color(0x94, 0xA3, 0xB8);
    
    // Cores de Células dos Tabuleiros
    private static final Color COR_AGUA_MARINHA = new Color(0x1B, 0x2A, 0x4A);
    private static final Color COR_NAVIO = new Color(0x02, 0x84, 0xC7); // Azul forte
    private static final Color COR_TIRO_AGUA = new Color(0x38, 0xBD, 0xF8); // Azul claro (água atingida)
    private static final Color COR_TIRO_ACERTO = new Color(0xEF, 0x44, 0x44); // Vermelho (acerto)
    private static final Color COR_BOTAO_GRID = new Color(0x1E, 0x29, 0x3B);

    // Lobby
    private JTextField campoNome;

    // Aguardando
    private JLabel lblStatusAguardando;

    // Posicionamento
    private final List<Navio> frotaAtual = new ArrayList<>();
    private TipoNavio navioSelecionadoParaPosicionar = TipoNavio.PORTA_AVIOES;
    private Orientacao orientacaoAtual = Orientacao.HORIZONTAL;
    private final JButton[][] botoesPosicionamento = new JButton[10][10];
    private JLabel lblStatusPosicionamento;
    private JLabel lblStatusFrota;

    // Batalha
    private final JButton[][] botoesMeuTabuleiro = new JButton[10][10];
    private final JButton[][] botoesAdversario = new JButton[10][10];
    private JLabel lblTurnoBatalha;
    private JLabel lblInfoAdversario;
    private JTextArea areaLog;

    public JanelaPrincipal(ControladorCliente controlador, String ipServidor) {
        super("Batalha Naval - Comando Tático RMI");
        this.controlador = controlador;
        this.ipServidor = ipServidor;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 750);
        setLocationRelativeTo(null);

        // Configurar Look and Feel global se possível, ou usar cores customizadas
        inicializarPaineis();
        add(painelPrincipal);
        mostrarLobby();
    }

    private void inicializarPaineis() {
        painelPrincipal.add(criarPainelLobby(), "LOBBY");
        painelPrincipal.add(criarPainelAguardando(), "AGUARDANDO");
        painelPrincipal.add(criarPainelPosicionamento(), "POSICIONAMENTO");
        painelPrincipal.add(criarPainelBatalha(), "BATALHA");
    }

    private JPanel criarPainelLobby() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(COR_BG_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel icone = new JLabel("⚓", SwingConstants.CENTER);
        icone.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        painel.add(icone, gbc);

        gbc.gridy++;
        JLabel titulo = new JLabel("BATALHA NAVAL RMI");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setForeground(COR_TEXTO);
        painel.add(titulo, gbc);

        gbc.gridy++;
        JLabel sub = new JLabel("Servidor Conectado: " + ipServidor);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(COR_TEXTO_MUTED);
        painel.add(sub, gbc);

        gbc.gridy++;
        JPanel cardForm = new JPanel(new GridBagLayout());
        cardForm.setBackground(COR_PANEL);
        cardForm.setBorder(new CompoundBorder(new LineBorder(COR_BORDER, 1, true), new EmptyBorder(20, 25, 20, 25)));
        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.insets = new Insets(8, 8, 8, 8);
        gbcForm.gridx = 0; gbcForm.gridy = 0;
        gbcForm.anchor = GridBagConstraints.WEST;

        JLabel lblNome = new JLabel("Seu Nome de Capitão:");
        lblNome.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNome.setForeground(COR_TEXTO);
        cardForm.add(lblNome, gbcForm);

        gbcForm.gridy++;
        campoNome = new JTextField(18);
        campoNome.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        campoNome.setBackground(COR_BG_DARK);
        campoNome.setForeground(COR_TEXTO);
        campoNome.setCaretColor(COR_TEXTO);
        campoNome.setBorder(new CompoundBorder(new LineBorder(COR_BORDER, 1), new EmptyBorder(6, 8, 6, 8)));
        cardForm.add(campoNome, gbcForm);

        gbcForm.gridy++;
        gbcForm.anchor = GridBagConstraints.CENTER;
        JButton btnEntrar = new JButton("Entrar no Lobby");
        btnEntrar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnEntrar.setBackground(new Color(0x02, 0x84, 0xC7));
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setFocusPainted(false);
        btnEntrar.setBorder(new EmptyBorder(10, 20, 10, 20));
        btnEntrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEntrar.addActionListener(e -> {
            String nome = campoNome.getText().trim();
            if (nome.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor, digite o seu nome de capitão.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            controlador.entrarNoLobby(nome);
        });
        cardForm.add(btnEntrar, gbcForm);

        gbc.gridy++;
        painel.add(cardForm, gbc);
        return painel;
    }

    private JPanel criarPainelAguardando() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(COR_BG_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel spinner = new JLabel("📡", SwingConstants.CENTER);
        spinner.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        painel.add(spinner, gbc);

        gbc.gridy++;
        lblStatusAguardando = new JLabel("Procurando oponente no radar...", SwingConstants.CENTER);
        lblStatusAguardando.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblStatusAguardando.setForeground(COR_TEXTO);
        painel.add(lblStatusAguardando, gbc);

        gbc.gridy++;
        JButton btnCancelar = new JButton("Cancelar / Sair");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancelar.setBackground(new Color(0x33, 0x41, 0x55));
        btnCancelar.setForeground(COR_TEXTO);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> controlador.sair());
        painel.add(btnCancelar, gbc);

        return painel;
    }

    private JPanel criarPainelPosicionamento() {
        JPanel painel = new JPanel(new BorderLayout(15, 15));
        painel.setBackground(COR_BG_DARK);
        painel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Topo: Status / Instrução
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        lblStatusPosicionamento = new JLabel("POSICIONAMENTO DA FROTA - Posicione seus 5 navios no mar", SwingConstants.CENTER);
        lblStatusPosicionamento.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblStatusPosicionamento.setForeground(COR_TEXTO);
        topo.add(lblStatusPosicionamento, BorderLayout.CENTER);
        
        lblStatusFrota = new JLabel("Navios posicionados: 0 / 5", SwingConstants.CENTER);
        lblStatusFrota.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatusFrota.setForeground(COR_TEXTO_MUTED);
        topo.add(lblStatusFrota, BorderLayout.SOUTH);
        painel.add(topo, BorderLayout.NORTH);

        // Centro: Tabuleiro com Letras (A-J) e Números (1-10)
        JPanel tabuleiroWrapper = new JPanel(new BorderLayout());
        tabuleiroWrapper.setBackground(COR_PANEL);
        tabuleiroWrapper.setBorder(new CompoundBorder(new LineBorder(COR_BORDER, 1), new EmptyBorder(10, 10, 10, 10)));

        JPanel gridComCoordenadas = criarGridComCoordenadas(botoesPosicionamento, (x, y) -> tentarPosicionarNavio(x, y));
        tabuleiroWrapper.add(gridComCoordenadas, BorderLayout.CENTER);
        painel.add(tabuleiroWrapper, BorderLayout.CENTER);

        // Painel Direito: Controles de Frota
        JPanel painelControle = new JPanel();
        painelControle.setLayout(new BoxLayout(painelControle, BoxLayout.Y_AXIS));
        painelControle.setBackground(COR_PANEL);
        painelControle.setBorder(new CompoundBorder(new LineBorder(COR_BORDER, 1), new EmptyBorder(15, 15, 15, 15)));
        painelControle.setPreferredSize(new Dimension(280, 0));

        JLabel lblSel = new JLabel("Selecionar Navio:");
        lblSel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSel.setForeground(COR_TEXTO);
        painelControle.add(lblSel);
        painelControle.add(Box.createVerticalStrut(5));

        JComboBox<TipoNavio> comboNavios = new JComboBox<>(TipoNavio.values());
        comboNavios.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboNavios.setBackground(COR_BG_DARK);
        comboNavios.setForeground(COR_TEXTO);
        comboNavios.addActionListener(e -> navioSelecionadoParaPosicionar = (TipoNavio) comboNavios.getSelectedItem());
        painelControle.add(comboNavios);

        painelControle.add(Box.createVerticalStrut(15));
        JButton btnOrientacao = new JButton("📐 Orientação: Horizontal");
        btnOrientacao.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnOrientacao.setBackground(new Color(0x33, 0x41, 0x55));
        btnOrientacao.setForeground(COR_TEXTO);
        btnOrientacao.setFocusPainted(false);
        btnOrientacao.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnOrientacao.addActionListener(e -> {
            orientacaoAtual = (orientacaoAtual == Orientacao.HORIZONTAL) ? Orientacao.VERTICAL : Orientacao.HORIZONTAL;
            btnOrientacao.setText("📐 Orientação: " + (orientacaoAtual == Orientacao.HORIZONTAL ? "Horizontal" : "Vertical"));
        });
        painelControle.add(btnOrientacao);

        painelControle.add(Box.createVerticalStrut(20));
        JButton btnAuto = new JButton("⚡ Auto-Posicionar Frota");
        btnAuto.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAuto.setBackground(new Color(0x0D, 0x94, 0x88));
        btnAuto.setForeground(Color.WHITE);
        btnAuto.setFocusPainted(false);
        btnAuto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAuto.addActionListener(e -> usarFrotaPadrao());
        painelControle.add(btnAuto);

        painelControle.add(Box.createVerticalStrut(10));
        JButton btnLimpar = new JButton("🗑️ Limpar Tabuleiro");
        btnLimpar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnLimpar.setBackground(new Color(0xBE, 0x12, 0x3C));
        btnLimpar.setForeground(Color.WHITE);
        btnLimpar.setFocusPainted(false);
        btnLimpar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLimpar.addActionListener(e -> limparFrota());
        painelControle.add(btnLimpar);

        painelControle.add(Box.createVerticalStrut(30));
        JButton btnEnviar = new JButton("⚓ Confirmar e Enviar Frota");
        btnEnviar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEnviar.setBackground(new Color(0x02, 0x84, 0xC7));
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.setFocusPainted(false);
        btnEnviar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnEnviar.setBorder(new EmptyBorder(10, 15, 10, 15));
        btnEnviar.addActionListener(e -> enviarFrota());
        painelControle.add(btnEnviar);

        painel.add(painelControle, BorderLayout.EAST);
        return painel;
    }

    private JPanel criarPainelBatalha() {
        JPanel painel = new JPanel(new BorderLayout(15, 15));
        painel.setBackground(COR_BG_DARK);
        painel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Topo: Turno e Informação do Oponente
        JPanel topo = new JPanel(new BorderLayout());
        topo.setBackground(COR_PANEL);
        topo.setBorder(new CompoundBorder(new LineBorder(COR_BORDER, 1), new EmptyBorder(10, 15, 10, 15)));

        lblTurnoBatalha = new JLabel("BATALHA EM ANDAMENTO", SwingConstants.CENTER);
        lblTurnoBatalha.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTurnoBatalha.setForeground(COR_TEXTO);
        topo.add(lblTurnoBatalha, BorderLayout.CENTER);

        lblInfoAdversario = new JLabel("Oponente: ...", SwingConstants.LEFT);
        lblInfoAdversario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblInfoAdversario.setForeground(COR_TEXTO_MUTED);
        topo.add(lblInfoAdversario, BorderLayout.WEST);

        painel.add(topo, BorderLayout.NORTH);

        // Centro: Dois Tabuleiros Lado a Lado
        JPanel centro = new JPanel(new GridLayout(1, 2, 25, 0));
        centro.setOpaque(false);

        // Meu Tabuleiro Panel
        JPanel meuContainer = new JPanel(new BorderLayout(5, 5));
        meuContainer.setBackground(COR_PANEL);
        meuContainer.setBorder(new CompoundBorder(new LineBorder(COR_BORDER, 1), new EmptyBorder(10, 10, 10, 10)));
        JLabel lblTituloMeu = new JLabel("🛡️ Seu Tabuleiro (Frota)", SwingConstants.CENTER);
        lblTituloMeu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTituloMeu.setForeground(COR_TEXTO);
        meuContainer.add(lblTituloMeu, BorderLayout.NORTH);
        meuContainer.add(criarGridComCoordenadas(botoesMeuTabuleiro, null, false), BorderLayout.CENTER);
        centro.add(meuContainer);

        // Tabuleiro Adversário Panel
        JPanel advContainer = new JPanel(new BorderLayout(5, 5));
        advContainer.setBackground(COR_PANEL);
        advContainer.setBorder(new CompoundBorder(new LineBorder(COR_BORDER, 1), new EmptyBorder(10, 10, 10, 10)));
        JLabel lblTituloAdv = new JLabel("🎯 Radar do Inimigo (Clique para Atirar)", SwingConstants.CENTER);
        lblTituloAdv.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTituloAdv.setForeground(COR_TEXTO);
        advContainer.add(lblTituloAdv, BorderLayout.NORTH);
        advContainer.add(criarGridComCoordenadas(botoesAdversario, (x, y) -> controlador.dispararTiro(x, y), true), BorderLayout.CENTER);
        centro.add(advContainer);

        painel.add(centro, BorderLayout.CENTER);

        // Sul: Log de Eventos Táticos
        JPanel sul = new JPanel(new BorderLayout());
        sul.setBackground(COR_PANEL);
        sul.setBorder(new CompoundBorder(new LineBorder(COR_BORDER, 1), new EmptyBorder(8, 8, 8, 8)));
        JLabel lblLog = new JLabel("📋 Log de Comunicações e Disparos:");
        lblLog.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLog.setForeground(COR_TEXTO_MUTED);
        sul.add(lblLog, BorderLayout.NORTH);

        areaLog = new JTextArea(4, 40);
        areaLog.setEditable(false);
        areaLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        areaLog.setBackground(COR_BG_DARK);
        areaLog.setForeground(COR_TEXTO);
        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setBorder(new LineBorder(COR_BORDER, 1));
        sul.add(scroll, BorderLayout.CENTER);

        painel.add(sul, BorderLayout.SOUTH);

        return painel;
    }

    private JPanel criarGridComCoordenadas(JButton[][] botoesRef, BiConsumer clickAction) {
        return criarGridComCoordenadas(botoesRef, clickAction, true);
    }

    private interface BiConsumer {
        void accept(int x, int y);
    }

    private JPanel criarGridComCoordenadas(JButton[][] botoesRef, BiConsumer clickAction, boolean interativo) {
        JPanel painelGrid = new JPanel(new GridLayout(11, 11, 3, 3));
        painelGrid.setBackground(COR_PANEL);
        painelGrid.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        String[] rows = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

        // Cabeçalho de colunas (0 a 10, sendo 0 vazio)
        for (int c = 0; c <= 10; c++) {
            JLabel lbl = new JLabel(cols[c], SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lbl.setForeground(COR_TEXTO_MUTED);
            painelGrid.add(lbl);
        }

        // Linhas (A a J) + Botões 10x10
        for (int r = 0; r < 10; r++) {
            JLabel lblRow = new JLabel(rows[r], SwingConstants.CENTER);
            lblRow.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblRow.setForeground(COR_TEXTO_MUTED);
            painelGrid.add(lblRow);

            for (int c = 0; c < 10; c++) {
                JButton btn = new JButton();
                btn.setBackground(COR_BOTAO_GRID);
                btn.setForeground(COR_TEXTO);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
                btn.setFocusPainted(false);
                btn.setBorder(new LineBorder(COR_BORDER, 1));

                final int fx = c;
                final int fy = r;
                if (clickAction != null && interativo) {
                    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    btn.addActionListener(e -> clickAction.accept(fx, fy));
                }

                botoesRef[r][c] = btn;
                painelGrid.add(btn);
            }
        }

        return painelGrid;
    }

    // Métodos públicos de navegação e atualização
    public void mostrarLobby() {
        cardLayout.show(painelPrincipal, "LOBBY");
    }

    public void mostrarAguardando(String mensagem) {
        lblStatusAguardando.setText(mensagem);
        cardLayout.show(painelPrincipal, "AGUARDANDO");
    }

    public void mostrarAguardandoBatalha(String mensagem) {
        lblStatusAguardando.setText(mensagem);
        cardLayout.show(painelPrincipal, "AGUARDANDO");
    }

    public void mostrarTelaPosicionamento(String nomeAdversario) {
        limparFrota();
        lblStatusPosicionamento.setText("Oponente detectado: " + nomeAdversario + ". Posicione sua frota.");
        cardLayout.show(painelPrincipal, "POSICIONAMENTO");
    }

    public void mostrarTelaBatalha(EstadoPartida estado, boolean minhaVez) {
        lblInfoAdversario.setText("⚔️ Oponente: " + estado.nomeAdversario());
        cardLayout.show(painelPrincipal, "BATALHA");
        atualizarVisualizacaoBatalha(estado, minhaVez);
        adicionarLog("Partida iniciada contra " + estado.nomeAdversario() + "!");
    }

    public void atualizarEstadoBatalha(EstadoPartida estado, boolean minhaVez, String mensagemLog) {
        atualizarVisualizacaoBatalha(estado, minhaVez);
        if (mensagemLog != null && !mensagemLog.isEmpty()) {
            adicionarLog(mensagemLog);
        }
    }

    public void setTurno(boolean minhaVez) {
        if (minhaVez) {
            lblTurnoBatalha.setText("🟢 SUA VEZ DE ATIRAR!");
            lblTurnoBatalha.setForeground(new Color(0x34, 0xD3, 0x99)); // Verde vibrante
        } else {
            lblTurnoBatalha.setText("⏳ VEZ DO ADVERSÁRIO...");
            lblTurnoBatalha.setForeground(new Color(0xFB, 0xBF, 0x24)); // Amarelo/Laranja alerta
        }
    }

    private void atualizarVisualizacaoBatalha(EstadoPartida estado, boolean minhaVez) {
        setTurno(minhaVez);

        // Meu Tabuleiro
        EstadoCelula[][] meuTab = estado.meuTabuleiro();
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                JButton b = botoesMeuTabuleiro[y][x];
                switch (meuTab[y][x]) {
                    case VAZIA -> {
                        b.setBackground(COR_BOTAO_GRID);
                        b.setText("");
                    }
                    case NAVIO -> {
                        b.setBackground(COR_NAVIO);
                        b.setText("🚢");
                    }
                    case AGUA -> {
                        b.setBackground(COR_AGUA_MARINHA);
                        b.setText("•");
                        b.setForeground(COR_TEXTO_MUTED);
                    }
                    case ACERTO -> {
                        b.setBackground(COR_TIRO_ACERTO);
                        b.setText("💥");
                    }
                    default -> {}
                }
            }
        }

        // Tabuleiro do Adversário (Radar)
        EstadoCelula[][] advTab = estado.tabuleiroAdversario();
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                JButton b = botoesAdversario[y][x];
                switch (advTab[y][x]) {
                    case DESCONHECIDA -> {
                        b.setBackground(COR_BOTAO_GRID);
                        b.setText("");
                        b.setEnabled(true);
                    }
                    case AGUA -> {
                        b.setBackground(COR_AGUA_MARINHA);
                        b.setText("•");
                        b.setForeground(COR_TEXTO_MUTED);
                        b.setEnabled(false);
                    }
                    case ACERTO -> {
                        b.setBackground(COR_TIRO_ACERTO);
                        b.setText("💥");
                        b.setEnabled(false);
                    }
                    default -> {}
                }
            }
        }
    }

    public void adicionarLog(String texto) {
        areaLog.append(texto + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }

    public void mostrarFimDePartida(boolean venci, MotivoFim motivo, String nomeAdversario) {
        String mensagem;
        if (motivo == MotivoFim.FROTA_DESTRUIDA) {
            mensagem = venci ? "🎉 VITÓRIA! Você destruiu toda a frota de " + nomeAdversario + "!" : "💀 DERROTA! Sua frota foi totalmente destruída.";
        } else if (motivo == MotivoFim.DESCONEXAO) {
            mensagem = venci ? "🏆 VITÓRIA POR W.O.! O oponente desconectou." : "❌ DERROTA! Você se desconectou da partida.";
        } else {
            mensagem = venci ? "🏆 VITÓRIA! O oponente desistiu." : "Partida encerrada.";
        }
        JOptionPane.showMessageDialog(this, mensagem, "Fim de Jogo - Batalha Naval", JOptionPane.INFORMATION_MESSAGE);
        controlador.sair();
    }

    // Lógica de Posicionamento
    private void tentarPosicionarNavio(int x, int y) {
        if (navioSelecionadoParaPosicionar == null) return;
        
        for (Navio n : frotaAtual) {
            if (n.tipo() == navioSelecionadoParaPosicionar) {
                JOptionPane.showMessageDialog(this, "O navio " + obterNomeNavio(navioSelecionadoParaPosicionar) + " já foi posicionado.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        Navio novoNavio = new Navio(navioSelecionadoParaPosicionar, x, y, orientacaoAtual);
        if (!validarNavioNaFrota(novoNavio)) {
            JOptionPane.showMessageDialog(this, "Posição inválida! O navio ultrapassa os limites do tabuleiro ou colide com outro.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        frotaAtual.add(novoNavio);
        redesenharFrotaEmPosicionamento();
        lblStatusFrota.setText("Navios posicionados: " + frotaAtual.size() + " / 5");
    }

    private boolean validarNavioNaFrota(Navio navio) {
        int tam = navio.tipo().tamanho;
        boolean[][] grade = new boolean[10][10];

        for (Navio n : frotaAtual) {
            int t = n.tipo().tamanho;
            for (int i = 0; i < t; i++) {
                int nx = n.x() + (n.orientacao() == Orientacao.HORIZONTAL ? i : 0);
                int ny = n.y() + (n.orientacao() == Orientacao.VERTICAL ? i : 0);
                grade[ny][nx] = true;
            }
        }

        for (int i = 0; i < tam; i++) {
            int nx = navio.x() + (navio.orientacao() == Orientacao.HORIZONTAL ? i : 0);
            int ny = navio.y() + (navio.orientacao() == Orientacao.VERTICAL ? i : 0);
            if (nx < 0 || nx >= 10 || ny < 0 || ny >= 10) return false;
            if (grade[ny][nx]) return false;
        }
        return true;
    }

    private void redesenharFrotaEmPosicionamento() {
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                botoesPosicionamento[y][x].setBackground(COR_BOTAO_GRID);
                botoesPosicionamento[y][x].setText("");
            }
        }

        for (Navio n : frotaAtual) {
            int t = n.tipo().tamanho;
            for (int i = 0; i < t; i++) {
                int nx = n.x() + (n.orientacao() == Orientacao.HORIZONTAL ? i : 0);
                int ny = n.y() + (n.orientacao() == Orientacao.VERTICAL ? i : 0);
                botoesPosicionamento[ny][nx].setBackground(COR_NAVIO);
                botoesPosicionamento[ny][nx].setText("🚢");
            }
        }
    }

    private void limparFrota() {
        frotaAtual.clear();
        redesenharFrotaEmPosicionamento();
        lblStatusFrota.setText("Navios posicionados: 0 / 5");
    }

    private void usarFrotaPadrao() {
        frotaAtual.clear();
        frotaAtual.add(new Navio(TipoNavio.PORTA_AVIOES, 0, 0, Orientacao.HORIZONTAL));
        frotaAtual.add(new Navio(TipoNavio.ENCOURACADO, 0, 2, Orientacao.HORIZONTAL));
        frotaAtual.add(new Navio(TipoNavio.CRUZADOR, 9, 0, Orientacao.VERTICAL));
        frotaAtual.add(new Navio(TipoNavio.SUBMARINO, 5, 5, Orientacao.VERTICAL));
        frotaAtual.add(new Navio(TipoNavio.CONTRATORPEDEIRO, 8, 9, Orientacao.HORIZONTAL));
        redesenharFrotaEmPosicionamento();
        lblStatusFrota.setText("Navios posicionados: 5 / 5");
    }

    private void enviarFrota() {
        if (frotaAtual.size() != TipoNavio.values().length) {
            JOptionPane.showMessageDialog(this, "Você precisa posicionar todos os " + TipoNavio.values().length + " navios antes de confirmar.", "Frota Incompleta", JOptionPane.WARNING_MESSAGE);
            return;
        }
        controlador.posicionarFrota(new ArrayList<>(frotaAtual));
    }

    private String obterNomeNavio(TipoNavio tipo) {
        return switch (tipo) {
            case PORTA_AVIOES -> "Porta-aviões (5)";
            case ENCOURACADO -> "Encouraçado (4)";
            case CRUZADOR -> "Cruzador (3)";
            case SUBMARINO -> "Submarino (3)";
            case CONTRATORPEDEIRO -> "Contratorpedeiro (2)";
        };
    }
}
