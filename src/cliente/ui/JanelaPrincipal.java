package cliente.ui;

import cliente.ControladorCliente;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

public class JanelaPrincipal extends JFrame {
    private static final long serialVersionUID = 1L;

    private final transient ControladorCliente controlador;

    public JanelaPrincipal(ControladorCliente controlador, String ipServidor) {
        super("Batalha Naval RMI");
        this.controlador = controlador;

        JLabel status = new JLabel("Conectado ao servidor " + ipServidor, SwingConstants.CENTER);
        status.setFont(status.getFont().deriveFont(Font.BOLD, 18f));
        status.setForeground(new Color(0xE6EDF3));
        status.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        getContentPane().setBackground(new Color(0x0D1B2A));
        getContentPane().add(status, BorderLayout.CENTER);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
    }
}
