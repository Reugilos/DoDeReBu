package dodecagraphone.model.proves;

/**
 *
 * @author grogmgpt
 */
import javax.swing.JOptionPane;

public class EntradaSortidaDialog {

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
