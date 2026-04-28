package dodecagraphone.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import javax.swing.*;

/**
 * Classe utilitària per mostrar diàlegs personalitzats a l'usuari. Inclou
 * opcions de selecció, entrada de text, i selecció de fitxers.
 *
 * @author grogmgpt
 */
public class MyDialogs {

    /**
     * Mostra un diàleg de confirmació amb opcions Sí, No i Cancel·la.
     *
     * Shows a confirmation dialog with Yes, No, and Cancel options.
     *
     * @param missatge Missatge a mostrar
     * @param titol Títol de la finestra
     * @return Un valor de JOptionPane: YES_OPTION, NO_OPTION o CANCEL_OPTION
     */
    public static int demanaConfirmacio(String missatge, String titol) {
        return JOptionPane.showConfirmDialog(
                null,
                missatge,
                titol,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
    }

    /**
     * Mostra un diàleg amb una llista d'opcions perquè l'usuari en seleccioni
     * una.
     *
     * @param prompt Missatge principal del diàleg.
     * @param header Títol del diàleg.
     * @param opcions Array d'opcions a mostrar.
     * @param opcioPerDefecte Índex de l'opció que apareixerà seleccionada
     * inicialment.
     * @return L'opció seleccionada per l'usuari, o null si es cancel·la.
     */
    public static String seleccionaOpcio(String prompt, String header, String[] opcions, int opcioPerDefecte) {
        return (String) JOptionPane.showInputDialog(
                null,
                prompt,
                header,
                JOptionPane.PLAIN_MESSAGE,
                null,
                opcions,
                opcions[opcioPerDefecte]
        );
    }

    /**
     * Mostra un diàleg perquè l'usuari introdueixi text.
     *
     * @param prompt Missatge principal del diàleg.
     * @param header Títol del diàleg.
     * @return El text introduït per l'usuari o null si no introdueix res.
     */
    public static String mostraInputDialog(String prompt, String header) {
        String resposta = JOptionPane.showInputDialog(
                null,
                prompt,
                header,
                JOptionPane.PLAIN_MESSAGE
        );
        if (resposta == null || resposta.trim().isEmpty()) {
            return null;
        }
        return resposta.trim();
    }

    /**
     * Mostra un diàleg amb un valor predefinit perquè l'usuari introdueixi
     * text.
     *
     * @param prompt Missatge principal del diàleg.
     * @param header Títol del diàleg.
     * @param defaultValue Valor que apareix com a suggeriment al camp de text.
     * @return El text introduït o null si es cancel·la.
     */
    public static String mostraInputDialog(String prompt, String header, String defaultValue) {
        Object ob = JOptionPane.showInputDialog(
                null,
                prompt,
                header,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                defaultValue
        );
        if (ob == null) {
            return null;
        }
        String resposta = (String) ob;
        if (resposta == null || resposta.trim().isEmpty()) {
            return null;
        }
        return resposta.trim();
    }

    /**
     * Com mostraInputDialog(prompt, header, defaultValue) però distingeix
     * entre camp buit (retorna "") i cancel·lació (retorna null).
     */
    public static String mostraInputDialogAllowEmpty(String prompt, String header, String defaultValue) {
        Object ob = JOptionPane.showInputDialog(
                null, prompt, header,
                JOptionPane.PLAIN_MESSAGE, null, null, defaultValue);
        if (ob == null) return null;          // cancel·lat
        return ((String) ob).trim();          // "" si buit, text si hi ha contingut
    }

    public static void mostraError(String missatge, String titol) {
        JOptionPane.showMessageDialog(null, missatge, titol, JOptionPane.ERROR_MESSAGE);
    }

    public static void mostraMissatge(String missatge, String titol) {
        JOptionPane.showMessageDialog(null, missatge, titol, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Mostra un diàleg personalitzat amb un camp de text i una llista
     * desplegable. L'usuari pot introduir text o triar una opció. Si
     * s'introdueix text, té prioritat.
     *
     * @param parent Component pare (per centrar el diàleg).
     * @param prompt Missatge inicial del diàleg.
     * @param header Títol del diàleg.
     * @param dataList Llista d'opcions per al desplegable.
     * @return El text introduït o seleccionat, o null si es cancel·la.
     */
    public static String showDialog(Component parent, String prompt, String header, List<String> dataList) {
        JDialog dialog = new JDialog((Frame) null, header, true);
        dialog.setLayout(new BorderLayout());

        JTextField textField = new JTextField(20);
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel(prompt));
        inputPanel.add(textField);
        dialog.add(inputPanel, BorderLayout.NORTH);

        JComboBox<String> comboBox = new JComboBox<>(dataList.toArray(new String[0]));
        comboBox.setSelectedIndex(-1);
        JPanel comboPanel = new JPanel();
        comboPanel.add(new JLabel(I18n.t("dialogs.selectFromList")));
        comboPanel.add(comboBox);
        dialog.add(comboPanel, BorderLayout.CENTER);

        JButton okButton = new JButton(I18n.t("btn.ok"));
        JButton cancelButton = new JButton(I18n.t("btn.cancel"));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        final String[] userInput = {null};

        ActionListener acceptAction = (ActionEvent e) -> {
            String textValue = textField.getText().trim();
            String comboValue = (String) comboBox.getSelectedItem();

            if (!textValue.isEmpty()) {
                userInput[0] = textValue;
            } else {
                userInput[0] = comboValue;
            }

            dialog.dispose();
        };

        okButton.addActionListener(acceptAction);
        textField.addActionListener(acceptAction);
        comboBox.addActionListener(acceptAction);

        cancelButton.addActionListener(e -> {
            userInput[0] = null;
            dialog.dispose();
        });

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return userInput[0];
    }

    public static final String CONFIG_KEY_LAST_DIR = "lastDirectory";
    public static final String CONFIG_KEY_LAST_DIR_SVG = "lastDirectorySvg";
    public static final String CONFIG_KEY_LAST_DIR_PDF = "lastDirectoryPdf";
    public static File lastDirectory = null;
    public static File lastDirectorySvg = null;
    public static File lastDirectoryPdf = null;

    public static void initDialogs() {
        lastDirectory = getLastDirectoryFromConfig();
        lastDirectorySvg = getLastDirectorySvgFromConfig();
        lastDirectoryPdf = getLastDirectoryPdfFromConfig();
    }

    private static File getLastDirectoryFromConfig() {
        String path = AppConfig.get().get(CONFIG_KEY_LAST_DIR, null);
        if (path != null) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                return dir;
            }
        }
        return new File(System.getProperty("user.dir"));
    }

    private static File getLastDirectorySvgFromConfig() {
        String path = AppConfig.get().get(CONFIG_KEY_LAST_DIR_SVG, null);
        if (path != null) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                return dir;
            }
        }
        return new File(System.getProperty("user.dir"));
    }

    private static File getLastDirectoryPdfFromConfig() {
        String path = AppConfig.get().get(CONFIG_KEY_LAST_DIR_PDF, null);
        if (path != null) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                return dir;
            }
        }
        return new File(System.getProperty("user.dir"));
    }

    /**
     * Mostra un diàleg per seleccionar un fitxer MIDI per llegir.
     *
     * @param parent Component pare per centrar el diàleg.
     * @param defaultFile Ruta suggerida inicialment.
     * @return El camí del fitxer seleccionat, o null si es cancel·la.
     */
    public static String seleccionaFitxerLectura(Component parent, String defaultFile) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        File cwd = new File(System.getProperty("user.dir"));
        File startDir = (lastDirectory != null && lastDirectory.exists()) ? lastDirectory : cwd;

        JFileChooser chooser = new JFileChooser(startDir);
        chooser.setDialogTitle(I18n.t("dialogs.selectFile"));
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(I18n.t("dialogs.fileFilter.midi"), "mid", "midi"));

        if (defaultFile != null && !defaultFile.trim().isEmpty()) {
            String df = defaultFile.trim();
            File suggested = new File(df);
            if (!suggested.isAbsolute()) {
                suggested = new File(startDir, df);
            }
            chooser.setSelectedFile(suggested);
        }

        int result = chooser.showOpenDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File selected = chooser.getSelectedFile();
        if (selected == null) {
            return null;
        }

        File parentDir = selected.getParentFile();
        if (parentDir != null && parentDir.exists()) {
            lastDirectory = parentDir;
        }

        return selected.getAbsolutePath();
    }

    /**
     * Mostra un diàleg per desar un fitxer MIDI, amb validació d’extensió i
     * confirmació de sobreescriptura.
     *
     * @param parent Component pare per centrar el diàleg.
     * @param defaultFile Nom de fitxer suggerit.
     * @return El camí del fitxer seleccionat per escriure, o null si es
     * cancel·la.
     */
    public static String seleccionaFitxerEscriptura(Component parent, String defaultFile, String fileType) {
        boolean isSvg = "svg".equalsIgnoreCase(fileType);
        boolean isPdf = "pdf".equalsIgnoreCase(fileType);

        File lastDir = lastDirectory;
        if (isSvg) lastDir = lastDirectorySvg;
        else if (isPdf) lastDir = lastDirectoryPdf;

        File cwd = new File(System.getProperty("user.dir"));
        File startDir = (lastDir != null && lastDir.exists()) ? lastDir : cwd;

        final String ext       = isSvg ? "svg" : isPdf ? "pdf"        : "mid";
        final String fullExt   = isSvg ? "svg" : isPdf ? "ddcgr.pdf"  : "mid";
        JFileChooser chooser = new JFileChooser(startDir) {
            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                if (f == null) { super.approveSelection(); return; }
                File fixed = ensureExtensionOnce(f, fullExt);
                if (!fixed.equals(f)) setSelectedFile(fixed);
                super.approveSelection();
            }
        };

        chooser.setDialogTitle(I18n.t("dialogs.saveFile"));
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);

        if (isSvg) {
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(I18n.t("dialogs.fileFilter.svg"), "svg"));
        } else if (isPdf) {
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(I18n.t("print.pdfFilter"), "pdf"));
        } else {
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(I18n.t("dialogs.fileFilter.midi"), "mid", "midi"));
        }

        if (defaultFile != null && !defaultFile.trim().isEmpty()) {
            String df = defaultFile.trim();
            File suggested = new File(df);
            if (!suggested.isAbsolute()) suggested = new File(startDir, df);
            chooser.setSelectedFile(suggested);
        }

        int result = chooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) return null;

        File selected = chooser.getSelectedFile();
        if (selected == null) return null;

        File parentDir = selected.getParentFile();
        if (isSvg) lastDirectorySvg = parentDir;
        else if (isPdf) lastDirectoryPdf = parentDir;
        else lastDirectory = parentDir;

        return selected.getAbsolutePath();
    }

    private static File ensureExtensionOnce(File f, String ext) {
        String path = f.getAbsolutePath();
        String lower = path.toLowerCase();
        String dotExt = "." + ext.toLowerCase();
        if (lower.endsWith(dotExt)) return f;
        if ("mid".equals(ext) && lower.endsWith(".midi")) return f;
        // Si l'extensió és composta (ex. "ddcgr.pdf") i l'usuari ha posat
        // la part final (ex. ".pdf"), substituïm en lloc d'afegir.
        int dotIdx = ext.lastIndexOf('.');
        if (dotIdx > 0) {
            String simpleSuffix = "." + ext.substring(dotIdx + 1).toLowerCase();
            if (lower.endsWith(simpleSuffix)) {
                return new File(path.substring(0, path.length() - simpleSuffix.length()) + dotExt);
            }
        }
        return new File(path + dotExt);
    }
}