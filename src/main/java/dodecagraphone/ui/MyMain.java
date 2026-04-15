package dodecagraphone.ui;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.util.Locale;
import javax.swing.UIManager;

public class MyMain {

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
