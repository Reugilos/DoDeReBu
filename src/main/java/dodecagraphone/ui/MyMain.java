/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.ui;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.util.Locale;
import javax.swing.UIManager;

/**
 * [CA] Punt d'entrada de l'aplicació DoDeReBu. Inicialitza la configuració,
 * l'idioma i la interfície gràfica, i arrenca el bucle de refresc de la UI.
 * <p>
 * [EN] Application entry point for DoDeReBu. Initialises configuration,
 * localisation and the graphical interface, then starts the UI refresh loop.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MyMain {

    /**
     * [CA] Mètode principal. Carrega la configuració, configura l'idioma,
     * aplica el look-and-feel del sistema i crea la finestra principal.
     * Un {@link javax.swing.Timer} crida {@code update()} cada
     * {@link Settings#REFRESH_PERIOD} ms per refrescar la UI.
     * <p>
     * [EN] Main method. Loads configuration, sets the locale, applies the
     * system look-and-feel and creates the main window. A
     * {@link javax.swing.Timer} calls {@code update()} every
     * {@link Settings#REFRESH_PERIOD} ms to refresh the UI.
     *
     * @param args [CA] arguments de línia de comandes (no s'utilitzen) /
     *             [EN] command-line arguments (unused)
     */
    public static void main(String[] args) {
        AppConfig.get().init();
        String langTag = AppConfig.get().get("ui.language", "ca");
        I18n.initFromLanguageTag(langTag);
        Locale.setDefault(I18n.getLocale());
        JComponent.setDefaultLocale(I18n.getLocale());

        try {
//            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            MyUserInterface iu = new MyUserInterface();
            iu.setVisible(true);
            System.out.println(I18n.f("main.welcome", iu.getTitle(), AppConfig.get().getConfigPathForDebug()));
            new javax.swing.Timer(Settings.REFRESH_PERIOD, e -> iu.update()).start();
        });
    }
}
