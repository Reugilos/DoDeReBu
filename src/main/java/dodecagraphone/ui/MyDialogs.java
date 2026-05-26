/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
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
 * [CA] Classe utilitària per mostrar diàlegs personalitzats a l'usuari.
 * Inclou opcions de confirmació, selecció, entrada de text i selecció
 * de fitxers MIDI, SVG i PDF.
 * <p>
 * [EN] Utility class for showing customised dialogs to the user.
 * Includes confirmation, selection, text input and file chooser dialogs
 * for MIDI, SVG and PDF files.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MyDialogs {

    /**
     * [CA] Mostra un diàleg de confirmació amb opcions Sí i No.
     * <p>
     * [EN] Shows a confirmation dialog with Yes and No options.
     *
     * @param missatge [CA] Missatge a mostrar / [EN] Message to display
     * @param titol    [CA] Títol de la finestra / [EN] Window title
     * @return [CA] JOptionPane.YES_OPTION o NO_OPTION / [EN] JOptionPane.YES_OPTION or NO_OPTION
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
     * [CA] Mostra un diàleg amb una llista d'opcions perquè l'usuari en seleccioni una.
     * <p>
     * [EN] Shows a dialog with a list of options for the user to select one.
     *
     * @param prompt          [CA] Missatge principal del diàleg / [EN] Dialog main message
     * @param header          [CA] Títol del diàleg / [EN] Dialog title
     * @param opcions         [CA] Array d'opcions a mostrar / [EN] Array of options to display
     * @param opcioPerDefecte [CA] Índex de l'opció seleccionada inicialment / [EN] Index of initially selected option
     * @return [CA] L'opció seleccionada, o null si es cancel·la / [EN] Selected option, or null if cancelled
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
     * [CA] Mostra un diàleg perquè l'usuari introdueixi text.
     * Retorna null si l'usuari cancel·la o no introdueix res.
     * <p>
     * [EN] Shows a dialog for the user to enter text.
     * Returns null if the user cancels or enters nothing.
     *
     * @param prompt [CA] Missatge principal / [EN] Main message
     * @param header [CA] Títol del diàleg / [EN] Dialog title
     * @return [CA] El text introduït (trimmat), o null / [EN] The entered text (trimmed), or null
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
     * [CA] Mostra un diàleg amb un valor predefinit perquè l'usuari introdueixi text.
     * Retorna null si es cancel·la o el camp és buit.
     * <p>
     * [EN] Shows a dialog with a predefined value for the user to edit.
     * Returns null if cancelled or if the field is empty.
     *
     * @param prompt       [CA] Missatge principal / [EN] Main message
     * @param header       [CA] Títol del diàleg / [EN] Dialog title
     * @param defaultValue [CA] Valor que apareix com a suggeriment / [EN] Value shown as suggestion
     * @return [CA] El text introduït, o null / [EN] The entered text, or null
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
     * [CA] Com {@link #mostraInputDialog(String, String, String)} però distingeix
     * entre camp buit (retorna {@code ""}) i cancel·lació (retorna {@code null}).
     * <p>
     * [EN] Like {@link #mostraInputDialog(String, String, String)} but distinguishes
     * between an empty field (returns {@code ""}) and cancellation (returns {@code null}).
     *
     * @param prompt       [CA] Missatge principal / [EN] Main message
     * @param header       [CA] Títol del diàleg / [EN] Dialog title
     * @param defaultValue [CA] Valor per defecte / [EN] Default value
     * @return [CA] Text introduït (pot ser buit), o null si es cancel·la /
     *         [EN] Entered text (may be empty), or null if cancelled
     */
    public static String mostraInputDialogAllowEmpty(String prompt, String header, String defaultValue) {
        Object ob = JOptionPane.showInputDialog(
                null, prompt, header,
                JOptionPane.PLAIN_MESSAGE, null, null, defaultValue);
        if (ob == null) return null;          // cancel·lat
        return ((String) ob).trim();          // "" si buit, text si hi ha contingut
    }

    /**
     * [CA] Mostra un missatge d'error en un diàleg.
     * <p>
     * [EN] Shows an error message in a dialog.
     *
     * @param missatge [CA] Missatge d'error / [EN] Error message
     * @param titol    [CA] Títol del diàleg / [EN] Dialog title
     */
    public static void mostraError(String missatge, String titol) {
        JOptionPane.showMessageDialog(null, missatge, titol, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * [CA] Mostra un missatge informatiu en un diàleg.
     * <p>
     * [EN] Shows an informational message in a dialog.
     *
     * @param missatge [CA] Missatge a mostrar / [EN] Message to display
     * @param titol    [CA] Títol del diàleg / [EN] Dialog title
     */
    public static void mostraMissatge(String missatge, String titol) {
        JOptionPane.showMessageDialog(null, missatge, titol, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * [CA] Mostra un diàleg personalitzat amb un camp de text i una llista
     * desplegable. L'usuari pot introduir text o triar una opció. Si s'introdueix
     * text, té prioritat sobre la selecció del desplegable.
     * <p>
     * [EN] Shows a custom dialog with a text field and a combo box. The user
     * can enter text or choose an option. If text is entered, it takes priority
     * over the combo selection.
     *
     * @param parent   [CA] Component pare per centrar el diàleg / [EN] Parent component for centering
     * @param prompt   [CA] Missatge inicial del diàleg / [EN] Initial dialog message
     * @param header   [CA] Títol del diàleg / [EN] Dialog title
     * @param dataList [CA] Llista d'opcions per al desplegable / [EN] List of options for the combo box
     * @return [CA] El text introduït o seleccionat, o null si es cancel·la /
     *         [EN] The entered or selected text, or null if cancelled
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

    /** [CA] Clau de config per al darrer directori MIDI / [EN] Config key for the last MIDI directory */
    public static final String CONFIG_KEY_LAST_DIR = "lastDirectory";
    /** [CA] Clau de config per al darrer directori SVG / [EN] Config key for the last SVG directory */
    public static final String CONFIG_KEY_LAST_DIR_SVG = "lastDirectorySvg";
    /** [CA] Clau de config per al darrer directori PDF / [EN] Config key for the last PDF directory */
    public static final String CONFIG_KEY_LAST_DIR_PDF = "lastDirectoryPdf";

    /** [CA] Darrer directori usat per a MIDI / [EN] Last directory used for MIDI */
    public static File lastDirectory = null;
    /** [CA] Darrer directori usat per a SVG / [EN] Last directory used for SVG */
    public static File lastDirectorySvg = null;
    /** [CA] Darrer directori usat per a PDF / [EN] Last directory used for PDF */
    public static File lastDirectoryPdf = null;

    /**
     * [CA] Inicialitza els darrers directoris a partir de la configuració guardada.
     * <p>
     * [EN] Initialises the last directories from the saved configuration.
     */
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
     * [CA] Mostra un diàleg per seleccionar un fitxer MIDI per llegir.
     * <p>
     * [EN] Shows a dialog to select a MIDI file for reading.
     *
     * @param parent      [CA] Component pare per centrar el diàleg / [EN] Parent component for centering
     * @param defaultFile [CA] Ruta suggerida inicialment / [EN] Initially suggested path
     * @return [CA] El camí absolut del fitxer seleccionat, o null si es cancel·la /
     *         [EN] The absolute path of the selected file, or null if cancelled
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
     * [CA] Mostra un diàleg per desar un fitxer (MIDI, SVG o PDF), amb
     * validació d'extensió i confirmació de sobreescriptura.
     * <p>
     * [EN] Shows a dialog for saving a file (MIDI, SVG or PDF), with extension
     * validation and overwrite confirmation.
     *
     * @param parent      [CA] Component pare per centrar el diàleg / [EN] Parent component for centering
     * @param defaultFile [CA] Nom de fitxer suggerit / [EN] Suggested file name
     * @param fileType    [CA] Tipus de fitxer: "svg", "pdf" o qualsevol altre per a MIDI /
     *                    [EN] File type: "svg", "pdf" or anything else for MIDI
     * @return [CA] El camí absolut del fitxer seleccionat per escriure, o null si es cancel·la /
     *         [EN] The absolute path of the file selected for writing, or null if cancelled
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
        final String fullExt   = isSvg ? "svg" : isPdf ? "ddcgr.pdf"  : "ddcgr.mid";
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
        // Si l'extensió és composta (ex. "ddcgr.pdf", "ddcgr.mid") i l'usuari ha posat
        // la part final (ex. ".pdf", ".mid", ".midi"), substituïm en lloc d'afegir.
        int dotIdx = ext.lastIndexOf('.');
        if (dotIdx > 0) {
            String simpleSuffix = "." + ext.substring(dotIdx + 1).toLowerCase();
            if (simpleSuffix.equals(".mid")) {
                // MIDI: .mid i .ddcgr.mid son ambdós vàlids; no forcem .ddcgr.mid
                if (lower.endsWith(".mid")) return f;
                if (lower.endsWith(".midi")) return new File(path.substring(0, path.length() - 5) + ".mid");
            } else if (lower.endsWith(simpleSuffix)) {
                return new File(path.substring(0, path.length() - simpleSuffix.length()) + dotExt);
            }
        } else if ("mid".equals(ext) && lower.endsWith(".midi")) {
            return f;
        }
        return new File(path + dotExt);
    }
}
