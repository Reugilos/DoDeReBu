package dodecagraphone.model.proves;

/**
 *
 * @author grogmgpt
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

    public ColorSpiral() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLACK);
    }

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

    public static void main(String[] args) {
        JFrame frame = new JFrame("Color Spiral");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ColorSpiral());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
