/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.proves;

import javax.swing.JFrame;

/**
 * [CA] Punt d'entrada per a la prova de l'espiral de colors. Crea una finestra Swing
 * i hi afegeix un panell {@link ColorSpiral} per visualitzar l'espiral cromàtica.
 * Codi experimental / prototip.
 * <p>
 * [EN] Entry point for the colour spiral test. Creates a Swing window and adds a
 * {@link ColorSpiral} panel to display the chromatic spiral.
 * Experimental / prototype code.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MainSpiral {

    /**
     * [CA] Crea i mostra la finestra amb l'espiral de colors centrada a la pantalla.
     * <p>
     * [EN] Creates and displays the colour spiral window centred on screen.
     *
     * @param args [CA] arguments de la línia de comandes (no s'utilitzen) / [EN] command-line arguments (unused)
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Color Spiral");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ColorSpiral());  // Afegeix el panell de l'espiral de colors
        frame.pack();
        frame.setLocationRelativeTo(null);  // Centra la finestra a la pantalla
        frame.setVisible(true);
    }
}
