/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
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
 * [CA] Gestió de la internacionalització (i18n) de l'aplicació. Carrega els
 * fitxers de missatges {@code messages_ca.properties} / {@code messages_en.properties}
 * en UTF-8 i ofereix els mètodes {@link #t(String)} i {@link #f(String, Object...)}
 * per obtenir textos traduïts. L'idioma per defecte és l'anglès.
 * <p>
 * [EN] Application internationalisation (i18n) management. Loads the message
 * files {@code messages_ca.properties} / {@code messages_en.properties} in
 * UTF-8 and provides the methods {@link #t(String)} and {@link #f(String, Object...)}
 * for translated strings. The default language is English.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
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
     * [CA] Inicialitza l'idioma a partir d'un language tag (ex: "ca", "en",
     * "fr", "pt-BR"). Si el tag és buit o null, no fa cap canvi.
     * <p>
     * [EN] Initializes the language from a language tag (e.g. "ca", "en",
     * "fr", "pt-BR"). Does nothing if the tag is empty or null.
     *
     * @param languageTag [CA] Tag d'idioma BCP-47 / [EN] BCP-47 language tag
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
     * [CA] Canvia l'idioma actiu a partir d'un language tag.
     * <p>
     * [EN] Changes the active language from a language tag.
     *
     * @param languageTag [CA] Tag d'idioma BCP-47, ex: "ca", "en" /
     *                    [EN] BCP-47 language tag, e.g. "ca", "en"
     * @throws IllegalArgumentException [CA] Si el tag és null o buit /
     *                                  [EN] If the tag is null or blank
     */
    public static void setLanguageTag(String languageTag) {
        if (languageTag == null || languageTag.isBlank()) {
            throw new IllegalArgumentException("languageTag buit");
        }
        setLocale(Locale.forLanguageTag(languageTag.trim()));
    }

    /**
     * [CA] Canvia el {@link Locale} actiu i recarrega el bundle.
     * <p>
     * [EN] Changes the active {@link Locale} and reloads the bundle.
     *
     * @param newLocale [CA] Nou locale / [EN] New locale
     * @throws IllegalArgumentException [CA] Si newLocale és null / [EN] If newLocale is null
     */
    public static void setLocale(Locale newLocale) {
        if (newLocale == null) {
            throw new IllegalArgumentException("newLocale null");
        }
        locale = newLocale;
        bundle = loadBundle(locale);
    }

    /**
     * [CA] Retorna el {@link Locale} actiu.
     * <p>
     * [EN] Returns the active {@link Locale}.
     *
     * @return [CA] Locale actual de l'app / [EN] Current app locale
     */
    public static Locale getLocale() {
        return locale;
    }

    /**
     * [CA] Retorna el text traduït per a la clau indicada. Si la clau no
     * existeix, retorna {@code "??" + key + "??"}.
     * <p>
     * [EN] Returns the translated text for the given key. If the key does
     * not exist, returns {@code "??" + key + "??"}.
     *
     * @param key [CA] Clau del missatge / [EN] Message key
     * @return [CA] Text traduït o marcador d'error / [EN] Translated text or error marker
     */
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
     * [CA] Retorna el text traduït amb substitució de placeholders ({0}, {1}…)
     * usant el locale actiu (no el del sistema).
     * <p>
     * [EN] Returns the translated text with placeholder substitution ({0}, {1}…)
     * using the active locale (not the system locale).
     *
     * @param key  [CA] Clau del missatge / [EN] Message key
     * @param args [CA] Arguments a substituir als placeholders /
     *             [EN] Arguments to substitute in placeholders
     * @return [CA] Text formatat / [EN] Formatted text
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
     * [CA] Control de ResourceBundle que força la lectura dels fitxers
     * {@code .properties} en UTF-8 en lloc d'ISO-8859-1.
     * <p>
     * [EN] ResourceBundle control that forces reading {@code .properties}
     * files in UTF-8 instead of ISO-8859-1.
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
