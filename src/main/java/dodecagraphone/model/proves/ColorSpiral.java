/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.proves;

/**
 * [CA] Panell Swing que dibuixa una espiral de colors en forma d'anells concèntrics
 * amb 12 segments (un per semitò) i 4 anells radials. Codi experimental / prototip.
 * <p>
 * [EN] Swing panel that draws a colour spiral as concentric rings with 12 segments
 * (one per semitone) and 4 radial rings. Experimental / prototype code.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

public class ColorSpiral extends JPanel {
    private static final int PANEL_WIDTH = 500;
    private static final int PANEL_HEIGHT = 500;
    private static final int NUM_SEGMENTS = 12;
    private static final int NUM_RINGS = 4;
    private static final Color[][] COLORS = {
            {Color.RED, new Color(255, 100, 100), new Color(255, 150, 150), new Color(255, 200, 200)},
            {Color.ORANGE, new Color(255, 180, 80), new Color(255, 200, 130), new Color(255, 220, 180)},
            {Color.YELLOW, new Color(255, 255, 100), new Color(255, 255, 150), new Color(255, 255, 200)},
            {Color.GREEN, new Color(100, 255, 100), new Color(150, 255, 150), new Color(200, 255, 200)},
            {Color.BLUE, new Color(100, 100, 255), new Color(150, 150, 255), new Color(200, 200, 255)},
            {new Color(128, 0, 128), new Color(150, 80, 150), new Color(180, 130, 180), new Color(210, 180, 210)}
    };

    /**
     * [CA] Constructor. Estableix la mida preferida del panell i el fons negre.
     * <p>
     * [EN] Constructor. Sets the preferred panel size and black background.
     */
    public ColorSpiral() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLACK);
    }

    /**
     * [CA] Dibuixa l'espiral de colors: anells concèntrics dividits en 12 segments
     * cromàtics. Cada anell creix radialment cap a l'exterior amb colors progressivament
     * més clars. A la cantonada superior dreta es dibuixen dos quadrats blancs indicadors.
     * <p>
     * [EN] Draws the colour spiral: concentric rings divided into 12 chromatic segments.
     * Each ring grows radially outward with progressively lighter colours. Two white
     * indicator squares are drawn in the upper-right corner.
     *
     * @param g [CA] context gràfic proporcionat per Swing / [EN] graphics context provided by Swing
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int xCenter = PANEL_WIDTH / 2;
        int yCenter = PANEL_HEIGHT / 2;
        int radiusIncrement = 20;
        int arcWidth = 30;

        for (int ring = 0; ring < NUM_RINGS; ring++) {
            for (int segment = 0; segment < NUM_SEGMENTS; segment++) {
                double angleStart = 360.0 / NUM_SEGMENTS * segment;
                double angleExtent = 360.0 / NUM_SEGMENTS;

                // Determina el color de cada segment
                Color color = COLORS[segment % COLORS.length][ring];
                g2d.setColor(color);

                // Dibuixa cada arc en una posició radial diferent
                int radius = 80 + ring * radiusIncrement;
                int arcX = xCenter - radius;
                int arcY = yCenter - radius;
                g2d.fill(new Arc2D.Double(arcX, arcY, 2 * radius, 2 * radius, angleStart, angleExtent, Arc2D.PIE));
            }
        }

        // Dibuixar els dos quadrats blancs a la cantonada superior dreta
        g2d.setColor(Color.WHITE);
        int squareSize = 20;
        int padding = 10;
        g2d.fillRect(PANEL_WIDTH - 2 * squareSize - 2 * padding, padding, squareSize, squareSize);
        g2d.fillRect(PANEL_WIDTH - squareSize - padding, padding, squareSize, squareSize);
    }

    /**
     * [CA] Punt d'entrada per executar la visualització de l'espiral de forma independent.
     * <p>
     * [EN] Entry point to run the spiral visualisation as a standalone application.
     *
     * @param args [CA] arguments de la línia de comandes (no s'utilitzen) / [EN] command-line arguments (unused)
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Color Spiral");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ColorSpiral());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
