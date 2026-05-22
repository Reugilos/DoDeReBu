/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.proves;

/**
 * [CA] Prova d'entrada i sortida de dades mitjançant diàlegs Swing (JOptionPane).
 * Demana un nom i un número a l'usuari i mostra els valors introduïts.
 * Codi experimental / prototip.
 * <p>
 * [EN] Input/output data test using Swing dialogs (JOptionPane).
 * Asks the user for a name and a number and displays the entered values.
 * Experimental / prototype code.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
import javax.swing.JOptionPane;

public class EntradaSortidaDialog {

    /**
     * [CA] Punt d'entrada de la prova: mostra dos diàlegs d'entrada (nom i número)
     * i dos diàlegs de sortida amb els valors introduïts. Si el número no és vàlid
     * mostra un diàleg d'error.
     * <p>
     * [EN] Test entry point: displays two input dialogs (name and number) and
     * two output dialogs with the entered values. If the number is invalid an
     * error dialog is shown.
     *
     * @param args [CA] arguments de la línia de comandes (no s'utilitzen) / [EN] command-line arguments (unused)
     */
    public static void main(String[] args) {
        // Entrada de dades: demanem un nom a l'usuari
        String nom = JOptionPane.showInputDialog(null, "Introdueix el teu nom:");

        // Sortida de dades: mostrem el nom introduït
        JOptionPane.showMessageDialog(null, "Hola, " + nom + "!");

        // Entrada de dades: demanem un número i el convertim a un valor numèric
        String input = JOptionPane.showInputDialog(null, "Introdueix un número:");
        try {
            int numero = Integer.parseInt(input);
            JOptionPane.showMessageDialog(null, "Has introduït el número: " + numero);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Això no és un número vàlid!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
