package dodecagraphone.model.proves;

import javax.swing.JFrame;

/**
 *
 * @author grogmgpt
 */
public class MainSpiral {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Color Spiral");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ColorSpiral());  // Afegeix el panell de l'espiral de colors
        frame.pack();
        frame.setLocationRelativeTo(null);  // Centra la finestra a la pantalla
        frame.setVisible(true);
    }
}
