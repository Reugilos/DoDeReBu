/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.proves;

/**
 * [CA] Panell Swing de prova per verificar l'ordre de dibuix entre {@code fillRect}
 * i {@code drawLine} en Java2D. Dibuixa primer un rectangle verd i després una línia
 * discontínua negra que el travessa, comprovant que la línia queda per sobre del rectangle.
 * Codi experimental / prototip.
 * <p>
 * [EN] Swing test panel to verify the drawing order between {@code fillRect} and
 * {@code drawLine} in Java2D. Draws a green rectangle first and then a black dashed
 * line crossing it, verifying that the line appears on top of the rectangle.
 * Experimental / prototype code.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
import javax.swing.*;
import java.awt.*;

public class ProvaFillRect extends JPanel {

    /**
     * [CA] Dibuixa un rectangle verd i una línia discontínua negra que el travessa
     * per verificar l'ordre Z de les operacions de dibuix de Java2D.
     * <p>
     * [EN] Draws a green rectangle and a black dashed line crossing it to verify
     * the Z-order of Java2D drawing operations.
     *
     * @param g [CA] context gràfic proporcionat per Swing / [EN] graphics context provided by Swing
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Dibuixem un rectangle verd
        g2.setColor(Color.GREEN);
        g2.fillRect(50, 50, 200, 100);

        // Dibuixem una línia vermella que travessa el rectangle
        Stroke stroke = g2.getStroke();
        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{15, 3}, 0);
        g2.setStroke(dashed);
        g2.setColor(java.awt.Color.BLACK);
        // g2.setColor(Color.RED);
//        g2.setStroke(new BasicStroke(3));
        g2.drawLine(30, 30, 300, 200);
        g2.setStroke(stroke);
    }

    /**
     * [CA] Punt d'entrada per executar la prova de dibuix de forma independent.
     * <p>
     * [EN] Entry point to run the drawing test as a standalone application.
     *
     * @param args [CA] arguments de la línia de comandes (no s'utilitzen) / [EN] command-line arguments (unused)
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Prova fillRect + drawLine");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.add(new ProvaFillRect());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
