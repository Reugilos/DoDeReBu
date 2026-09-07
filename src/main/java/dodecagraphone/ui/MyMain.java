/*
 * PolyForm Noncommercial License 1.0.0
 * Copyright (c) 2024-2026 Pau Bofill. Powered by Claude AI.
 * Full license / Llicència completa: LICENSE (project root / arrel del projecte)
 */
package dodecagraphone.ui;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.io.IOException;
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
     * [CA] Clau del {@code config.properties} que decideix si a l'arrencada surt
     * el diàleg de benvinguda: la tria d'idioma i, tot seguit, l'explicació d'on
     * és la configuració. S'entrega a {@code true}; un cop mostrat, l'aplicació
     * la posa a {@code false}. L'usuari la pot tornar a posar a {@code true} per
     * veure-ho un altre cop, sense haver d'esborrar el config sencer.
     * <p>
     * [EN] {@code config.properties} key deciding whether the welcome dialog
     * appears at startup: the language choice and then the explanation of where
     * the configuration lives. Shipped as {@code true}; once shown, the
     * application sets it to {@code false}. The user can set it back to
     * {@code true} to see it again, without deleting the whole config.
     */
    private static final String CLAU_BENVINGUDA = "showWelcomeDialog";

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
        // Reescriu el config amb comentaris localitzats (I18n ja inicialitzat).
        // Necessari perquè installUserConfigIfMissing no pot traduir els #i18n:
        // abans que I18n estigui llest.
        try { AppConfig.get().save(); } catch (IOException ignored) {}
        Locale.setDefault(I18n.getLocale());
        JComponent.setDefaultLocale(I18n.getLocale());

        try {
//            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            // Primera arrencada: triar idioma i explicar on és el config, un sol cop.
            // La tria va ABANS de construir la finestra, perquè tota la interfície
            // es munti ja en l'idioma escollit.
            boolean mostrarBenvinguda = AppConfig.get().getBool(CLAU_BENVINGUDA, true);
            if (mostrarBenvinguda) {
                String tria = MyDialogs.triaIdioma(null);
                if (tria != null) {
                    I18n.setLanguageTag(tria);
                    AppConfig.get().set("ui.language", tria);
                    Locale.setDefault(I18n.getLocale());
                    JComponent.setDefaultLocale(I18n.getLocale());
                }
            }

            MyUserInterface iu = new MyUserInterface();
            iu.setVisible(true);
            System.out.println(I18n.f("main.welcome", iu.getVersion(), AppConfig.get().getConfigPathForDebug()));

            if (mostrarBenvinguda) {
                // A la portable ningú no veu la consola: el mateix missatge, ja en
                // l'idioma triat, en una finestra amb un botó OK.
                MyDialogs.mostraBenvinguda(iu, iu.getVersion(), AppConfig.get().getConfigPathForDebug());
                AppConfig.get().set(CLAU_BENVINGUDA, "false");
                // Aquest desat també reescriu els comentaris del config en l'idioma nou.
                try { AppConfig.get().save(); } catch (IOException ignored) {}
            }

            new javax.swing.Timer(Settings.REFRESH_PERIOD, e -> iu.update()).start();
        });
    }
}
