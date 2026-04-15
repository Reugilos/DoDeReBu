package dodecagraphone.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 *
 * @author gptgrogm
 */
public final class I18n {

    private static final String BUNDLE_BASE_NAME = "i18n.messages";

    // Locale de l'aplicació (independent del sistema)
    // Tria aquí el default de l'app (si no hi ha config).
    private static Locale locale = Locale.ENGLISH;

    private static ResourceBundle bundle = loadBundle(locale);

    private I18n() {
    }

    /**
     * Inicialitza l'idioma a partir d'un "language tag" (ex: "ca", "en", "fr",
     * "pt-BR"). Si el tag és buit/null, no fa res.
     */
    public static void initFromLanguageTag(String languageTag) {
        if (languageTag == null) {
            return;
        }
        String tag = languageTag.trim();
        if (tag.isEmpty()) {
            return;
        }
        setLanguageTag(tag);
    }

    /**
     * Canvia l'idioma a partir d'un language tag: "ca", "en", "fr", "pt-BR"...
     */
    public static void setLanguageTag(String languageTag) {
        if (languageTag == null || languageTag.isBlank()) {
            throw new IllegalArgumentException("languageTag buit");
        }
        setLocale(Locale.forLanguageTag(languageTag.trim()));
    }

    public static void setLocale(Locale newLocale) {
        if (newLocale == null) {
            throw new IllegalArgumentException("newLocale null");
        }
        locale = newLocale;
        bundle = loadBundle(locale);
    }

    public static Locale getLocale() {
        return locale;
    }

    public static String t(String key) {
        if (key == null) {
            return "??null??";
        }
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return "??" + key + "??";
        }
    }

    /**
     * Format amb placeholders {0}, {1}... amb el locale de l'app (no el del
     * sistema).
     */
    public static String f(String key, Object... args) {
        String pattern = t(key);
        MessageFormat mf = new MessageFormat(pattern, locale);
        return mf.format(args == null ? new Object[0] : args);
    }

    // -------------------- internals --------------------
    private static ResourceBundle loadBundle(Locale loc) {
        // Forcem lectura en UTF-8 (recomanat per català) i fem fallback a EN si no existeix el bundle del locale
        try {
            return ResourceBundle.getBundle(BUNDLE_BASE_NAME, loc, new UTF8Control());
        } catch (MissingResourceException ex) {
            return ResourceBundle.getBundle(BUNDLE_BASE_NAME, Locale.ENGLISH, new UTF8Control());
        }
    }

    /**
     * ResourceBundle per defecte llegeix .properties en ISO-8859-1. Això força
     * UTF-8.
     */
    private static final class UTF8Control extends ResourceBundle.Control {

        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format,
                ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {

            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");

            InputStream stream = loader.getResourceAsStream(resourceName);
            if (stream == null) {
                return null;
            }

            try ( InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                Properties props = new Properties();
                props.load(reader);

                return new ResourceBundle() {
                    @Override
                    protected Object handleGetObject(String key) {
                        return props.getProperty(key);
                    }

                    @Override
                    public Enumeration<String> getKeys() {
                        return Collections.enumeration(props.stringPropertyNames());
                    }
                };
            }
        }
    }
}
