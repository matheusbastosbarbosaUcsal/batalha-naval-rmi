package cliente.ui;

import comum.Orientacao;
import comum.TipoNavio;

import javax.swing.JButton;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

// uma casa do tabuleiro. em vez de texto ela mesma desenha o fundo, o pedaço do navio
// que cai nela e a marca do tiro por cima
public class Celula extends JButton {
    private static final long serialVersionUID = 1L;
    private static final Color COR_ONDA = new Color(0x7DD3FC);

    public enum Marca { NENHUMA, AGUA, ACERTO }

    private Color fundo;
    private Color destaque;
    private TipoNavio navio;
    private Orientacao orientacao;
    private int parte;
    private boolean navioApagado;
    private Marca marca = Marca.NENHUMA;

    public Celula(Color fundo) {
        this.fundo = fundo;
        setContentAreaFilled(false);
        setFocusPainted(false);
    }

    public void limpar(Color novoFundo) {
        fundo = novoFundo;
        destaque = null;
        navio = null;
        navioApagado = false;
        marca = Marca.NENHUMA;
        repaint();
    }

    public void setFundo(Color novoFundo) {
        fundo = novoFundo;
        repaint();
    }

    public void setNavio(TipoNavio tipo, Orientacao direcao, int pedaco, boolean apagado) {
        navio = tipo;
        orientacao = direcao;
        parte = pedaco;
        navioApagado = apagado;
        repaint();
    }

    public void setMarca(Marca novaMarca) {
        marca = novaMarca;
        repaint();
    }

    public void setDestaque(Color cor) {
        destaque = cor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();
        g2.setColor(fundo);
        g2.fillRect(0, 0, w, h);

        if (destaque != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
            g2.setColor(destaque);
            g2.fillRect(0, 0, w, h);
            g2.setComposite(AlphaComposite.SrcOver);
        }
        if (navio != null) {
            // na prévia do posicionamento o navio aparece meio transparente
            if (navioApagado) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            }
            DesenhoNavio.desenharPedaco(g2, navio, orientacao, parte, w, h);
            g2.setComposite(AlphaComposite.SrcOver);
        }
        if (marca == Marca.AGUA) {
            DesenhoNavio.desenharAgua(g2, w, h, COR_ONDA);
        } else if (marca == Marca.ACERTO) {
            DesenhoNavio.desenharExplosao(g2, w, h);
        }
        g2.dispose();
    }
}
