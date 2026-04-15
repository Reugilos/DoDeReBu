package dodecagraphone.model.proves;

/**
 *
 * @author pau
 */
import javax.swing.*;
import java.awt.*;

/**
 * Classe de prova per verificar l'ordre de dibuix entre fillRect i drawLine.
 * Es dibuixa primer un rectangle i després una línia que l'atravessa.
 */
public class ProvaFillRect extends JPanel {

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
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Prova fillRect + drawLine");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.add(new ProvaFillRect());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
