package cliente.ui;

import comum.Orientacao;
import comum.TipoNavio;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public final class DesenhoNavio {
    private static final Color CONTORNO = new Color(0x1E293B);
    private static final Color CANHAO = new Color(0x334155);

    private DesenhoNavio() {
    }

    // cada casa do tabuleiro desenha só o seu pedaço do navio. o navio inteiro é desenhado
    // deslocado e a casa corta o que não é dela, assim os pedaços se encaixam formando um barco só
    public static void desenharPedaco(Graphics2D g, TipoNavio tipo, Orientacao orientacao, int parte, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        suavizar(g2);
        g2.clipRect(0, 0, w, h);
        if (orientacao == Orientacao.HORIZONTAL) {
            g2.translate(-parte * w, 0);
            desenharInteiro(g2, tipo, tipo.tamanho * w, h);
        } else {
            // na vertical é o mesmo desenho girado 90 graus, com a proa pra baixo
            g2.translate(w, -parte * h);
            g2.rotate(Math.PI / 2);
            desenharInteiro(g2, tipo, tipo.tamanho * h, w);
        }
        g2.dispose();
    }

    // desenha o navio deitado, com a proa (frente) virada pra direita
    public static void desenharInteiro(Graphics2D g, TipoNavio tipo, double comprimento, double largura) {
        double casa = comprimento / tipo.tamanho;
        switch (tipo) {
            case PORTA_AVIOES -> portaAvioes(g, comprimento, largura, casa);
            case ENCOURACADO -> encouracado(g, comprimento, largura, casa);
            case CRUZADOR -> cruzador(g, comprimento, largura, casa);
            case SUBMARINO -> submarino(g, comprimento, largura, casa);
            case CONTRATORPEDEIRO -> contratorpedeiro(g, comprimento, largura, casa);
        }
    }

    public static void desenharAgua(Graphics2D g, int w, int h, Color cor) {
        Graphics2D g2 = (Graphics2D) g.create();
        suavizar(g2);
        double cx = w / 2.0;
        double cy = h / 2.0;
        double r = Math.min(w, h) * 0.3;
        double onda = Math.min(w, h) * 0.09;
        g2.setColor(cor);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // três ondinhas, a do meio deslocada pra não ficar certinho
        for (int i = -1; i <= 1; i++) {
            double y = cy + i * onda * 2.2;
            double x0 = cx - r + (i == 0 ? onda : 0);
            double passo = r / 2;
            Path2D linha = new Path2D.Double();
            linha.moveTo(x0, y);
            linha.quadTo(x0 + passo / 2, y - onda, x0 + passo, y);
            linha.quadTo(x0 + passo * 1.5, y + onda, x0 + passo * 2, y);
            linha.quadTo(x0 + passo * 2.5, y - onda, x0 + passo * 3, y);
            g2.draw(linha);
        }
        g2.dispose();
    }

    public static void desenharExplosao(Graphics2D g, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        suavizar(g2);
        double cx = w / 2.0;
        double cy = h / 2.0;
        double fora = Math.min(w, h) * 0.38;
        g2.setColor(new Color(0xEF4444));
        g2.fill(estrela(cx, cy, fora, fora * 0.5, 9));
        g2.setColor(new Color(0xF97316));
        g2.fill(estrela(cx, cy, fora * 0.72, fora * 0.36, 9));
        g2.setColor(new Color(0xFDE047));
        double r = fora * 0.28;
        g2.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        g2.dispose();
    }

    public static Icon icone(TipoNavio tipo, int casa) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                suavizar(g2);
                g2.translate(x, y);
                desenharInteiro(g2, tipo, getIconWidth(), getIconHeight());
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return casa * tipo.tamanho;
            }

            @Override
            public int getIconHeight() {
                return casa;
            }
        };
    }

    // casco com a proa em ponta e a popa reta, usado pelos navios de superfície
    private static Shape casco(double c, double l, double casa, double folga) {
        double ponta = casa * 0.08;
        double topo = l * folga;
        double base = l - topo;
        Path2D p = new Path2D.Double();
        p.moveTo(casa * 0.12, topo + l * 0.06);
        p.quadTo(casa * 0.1, topo, casa * 0.3, topo);
        p.lineTo(c - casa * 0.9, topo);
        p.quadTo(c - casa * 0.25, topo, c - ponta, l / 2);
        p.quadTo(c - casa * 0.25, base, c - casa * 0.9, base);
        p.lineTo(casa * 0.3, base);
        p.quadTo(casa * 0.1, base, casa * 0.12, base - l * 0.06);
        p.closePath();
        return p;
    }

    private static void pintarCasco(Graphics2D g, Shape casco, Color claro, Color escuro, double l) {
        g.setPaint(new GradientPaint(0, 0, claro, 0, (float) l, escuro));
        g.fill(casco);
        g.setColor(CONTORNO);
        g.setStroke(new BasicStroke(1.4f));
        g.draw(casco);
    }

    // torre de canhão: um círculo com dois canos apontando pra ponta mais perto
    private static void torre(Graphics2D g, double x, double y, double r, boolean praFrente) {
        double cano = r * 1.9 * (praFrente ? 1 : -1);
        g.setColor(new Color(0x1F2937));
        g.setStroke(new BasicStroke((float) Math.max(1.6, r * 0.28), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(x, y - r * 0.32, x + cano, y - r * 0.32));
        g.draw(new Line2D.Double(x, y + r * 0.32, x + cano, y + r * 0.32));
        g.setColor(CANHAO);
        g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
        g.setColor(new Color(0x94A3B8));
        g.setStroke(new BasicStroke(1f));
        g.draw(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
    }

    private static void ponte(Graphics2D g, double x, double y, double comprimento, double altura) {
        g.setColor(new Color(0xCBD5E1));
        g.fill(new RoundRectangle2D.Double(x, y - altura / 2, comprimento, altura, altura * 0.4, altura * 0.4));
        g.setColor(CONTORNO);
        g.setStroke(new BasicStroke(1f));
        g.draw(new RoundRectangle2D.Double(x, y - altura / 2, comprimento, altura, altura * 0.4, altura * 0.4));
    }

    private static void chamine(Graphics2D g, double x, double y, double r) {
        g.setColor(new Color(0x0F172A));
        g.fill(new Ellipse2D.Double(x - r, y - r * 0.8, r * 2, r * 1.6));
    }

    private static void portaAvioes(Graphics2D g, double c, double l, double casa) {
        double topo = l * 0.14;
        double alto = l - topo * 2;
        Path2D deck = new Path2D.Double();
        deck.moveTo(casa * 0.1, topo + alto * 0.12);
        deck.lineTo(casa * 0.1, l - topo);
        deck.lineTo(c - casa * 0.55, l - topo);
        deck.lineTo(c - casa * 0.08, l / 2 + alto * 0.18);
        deck.lineTo(c - casa * 0.08, topo + alto * 0.05);
        deck.lineTo(casa * 0.9, topo);
        deck.closePath();
        pintarCasco(g, deck, new Color(0x6B7280), new Color(0x4B5563), l);

        // pista principal no meio e a pista de pouso inclinada na popa
        g.setColor(new Color(0xE5E7EB));
        g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10,
                new float[] { (float) (casa * 0.16), (float) (casa * 0.12) }, 0));
        g.draw(new Line2D.Double(casa * 0.35, l * 0.56, c - casa * 0.6, l * 0.56));
        g.draw(new Line2D.Double(casa * 0.35, l - topo - alto * 0.12, casa * 2.2, topo + alto * 0.18));

        double ilhaX = c * 0.58;
        g.setColor(new Color(0x9CA3AF));
        g.fill(new RoundRectangle2D.Double(ilhaX, topo - l * 0.02, casa * 0.75, l * 0.17, 4, 4));
        g.setColor(CONTORNO);
        g.setStroke(new BasicStroke(1f));
        g.draw(new RoundRectangle2D.Double(ilhaX, topo - l * 0.02, casa * 0.75, l * 0.17, 4, 4));
        chamine(g, ilhaX + casa * 0.5, topo + l * 0.065, l * 0.045);

        aviao(g, c - casa * 1.25, l * 0.56, l * 0.2);
        aviao(g, c - casa * 2.1, l * 0.56, l * 0.2);
        aviao(g, casa * 1.1, l * 0.68, l * 0.17);
    }

    private static void aviao(Graphics2D g, double x, double y, double t) {
        g.setColor(new Color(0xF8FAFC));
        g.setStroke(new BasicStroke((float) Math.max(1.4, t * 0.18), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Line2D.Double(x - t * 0.6, y, x + t * 0.6, y));
        g.draw(new Line2D.Double(x + t * 0.1, y - t * 0.55, x + t * 0.1, y + t * 0.55));
        g.draw(new Line2D.Double(x - t * 0.5, y - t * 0.25, x - t * 0.5, y + t * 0.25));
    }

    private static void encouracado(Graphics2D g, double c, double l, double casa) {
        pintarCasco(g, casco(c, l, casa, 0.11), new Color(0x8395A7), new Color(0x4B5D70), l);
        double meio = l / 2;
        double r = l * 0.16;
        torre(g, c - casa * 1.0, meio, r, true);
        torre(g, c - casa * 1.6, meio, r, true);
        ponte(g, casa * 1.55, meio, casa * 0.95, l * 0.3);
        chamine(g, casa * 1.35, meio, l * 0.07);
        torre(g, casa * 0.75, meio, r, false);
    }

    private static void cruzador(Graphics2D g, double c, double l, double casa) {
        pintarCasco(g, casco(c, l, casa, 0.14), new Color(0x9AAAB8), new Color(0x5B6B7A), l);
        double meio = l / 2;
        double r = l * 0.13;
        torre(g, c - casa * 0.95, meio, r, true);
        ponte(g, casa * 1.2, meio, casa * 0.7, l * 0.26);
        chamine(g, casa * 1.05, meio, l * 0.06);
        torre(g, casa * 0.6, meio, r, false);
    }

    private static void submarino(Graphics2D g, double c, double l, double casa) {
        double topo = l * 0.2;
        double alto = l - topo * 2;
        Shape corpo = new RoundRectangle2D.Double(casa * 0.12, topo, c - casa * 0.24, alto, alto, alto);
        g.setPaint(new GradientPaint(0, (float) topo, new Color(0x374151), 0, (float) (topo + alto), new Color(0x111827)));
        g.fill(corpo);
        g.setColor(new Color(0x0B1220));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(corpo);

        // leme cruzado na popa
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(0x1F2937));
        g.draw(new Line2D.Double(casa * 0.2, l / 2 - alto * 0.85, casa * 0.2, l / 2 + alto * 0.85));

        double torreX = c * 0.42;
        g.setColor(new Color(0x111827));
        g.fill(new RoundRectangle2D.Double(torreX, l / 2 - alto * 0.3, casa * 0.55, alto * 0.6, alto * 0.5, alto * 0.5));
        g.setColor(new Color(0x64748B));
        g.setStroke(new BasicStroke(1f));
        g.draw(new RoundRectangle2D.Double(torreX, l / 2 - alto * 0.3, casa * 0.55, alto * 0.6, alto * 0.5, alto * 0.5));
        g.setColor(new Color(0xFBBF24));
        g.fill(new Ellipse2D.Double(torreX + casa * 0.36, l / 2 - 2, 4, 4));

        g.setColor(new Color(0x4B5563));
        for (int i = 1; i <= 3; i++) {
            double x = c - casa * 0.35 - i * casa * 0.22;
            g.fill(new Rectangle2D.Double(x, l / 2 - 1, casa * 0.1, 2));
        }
    }

    private static void contratorpedeiro(Graphics2D g, double c, double l, double casa) {
        pintarCasco(g, casco(c, l, casa, 0.18), new Color(0xB8C4D0), new Color(0x6E7F90), l);
        double meio = l / 2;
        torre(g, c - casa * 0.75, meio, l * 0.11, true);
        ponte(g, casa * 0.75, meio, casa * 0.45, l * 0.22);
        chamine(g, casa * 0.55, meio, l * 0.05);
        g.setColor(new Color(0xEF4444));
        g.fill(new Rectangle2D.Double(casa * 0.2, meio - 1.5, casa * 0.18, 3));
    }

    private static Shape estrela(double cx, double cy, double fora, double dentro, int pontas) {
        Path2D p = new Path2D.Double();
        for (int i = 0; i < pontas * 2; i++) {
            double r = i % 2 == 0 ? fora : dentro;
            double a = Math.PI * i / pontas - Math.PI / 2;
            double x = cx + Math.cos(a) * r;
            double y = cy + Math.sin(a) * r;
            if (i == 0) {
                p.moveTo(x, y);
            } else {
                p.lineTo(x, y);
            }
        }
        p.closePath();
        return p;
    }

    private static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }
}
