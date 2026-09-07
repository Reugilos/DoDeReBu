/*
 * PolyForm Noncommercial License 1.0.0
 * Copyright (c) 2024-2026 Pau Bofill. Powered by Claude AI.
 * Full license / Llicència completa: LICENSE (project root / arrel del projecte)
 */
package dodecagraphone.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * [CA] Gestió de la internacionalització (i18n) de l'aplicació. Carrega els
 * fitxers de missatges {@code messages_ca.properties} / {@code messages_en.properties}
 * / {@code messages_es.properties} en UTF-8 i ofereix els mètodes
 * {@link #t(String)} i {@link #f(String, Object...)} per obtenir textos
 * traduïts. L'idioma per defecte és l'anglès.
 * <p>
 * [EN] Application internationalisation (i18n) management. Loads the message
 * files {@code messages_ca.properties} / {@code messages_en.properties} /
 * {@code messages_es.properties} in UTF-8 and provides the methods
 * {@link #t(String)} and {@link #f(String, Object...)} for translated strings.
 * The default language is English.
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

    /**
     * [CA] Ordre de preferència per presentar els idiomes: primer l'anglès,
     * després el català i el castellà. <b>No és la llista d'idiomes
     * disponibles</b>: es descobreixen sols escanejant {@code i18n/}. Aquesta
     * llista només diu quins van al davant; els que es trobin i no hi siguin
     * s'afegeixen al final, per ordre alfabètic.
     * <p>
     * [EN] Preference order for presenting languages: English first, then
     * Catalan and Spanish. <b>This is not the list of available languages</b>:
     * they are discovered by scanning {@code i18n/}. This list only says which
     * come first; any found and not listed here are appended at the end,
     * alphabetically.
     */
    private static final String[] LANGUAGE_TAGS_IN_ORDER = {"en", "ca", "es"};

    /** Bundles per locale, per no rellegir-los cada cop que es demana un text d'un altre idioma. */
    private static final Map<String, ResourceBundle> bundleCache = new HashMap<>();

    private I18n() {
    }

    /**
     * [CA] Retorna els tags dels idiomes realment instal·lats, en l'ordre de
     * presentació ({@code en}, {@code ca}, {@code es}, i els que s'hi afegeixin).
     * Un tag de {@link #LANGUAGE_TAGS_IN_ORDER} sense fitxer de missatges no hi
     * surt, o sigui que la llista mai promet un idioma que no existeix.
     * <p>
     * [EN] Returns the tags of the languages actually installed, in display
     * order ({@code en}, {@code ca}, {@code es}, plus any added). A tag from
     * {@link #LANGUAGE_TAGS_IN_ORDER} with no message file is left out, so the
     * list never promises a language that is not there.
     *
     * @return [CA] Llista de tags instal·lats / [EN] List of installed tags
     */
    public static List<String> getInstalledLanguageTags() {
        Set<String> trobats = scanLanguageTags();

        List<String> ordenats = new ArrayList<>();
        for (String tag : LANGUAGE_TAGS_IN_ORDER) {   // els preferits, en el seu ordre
            if (trobats.remove(tag)) {
                ordenats.add(tag);
            }
        }
        ordenats.addAll(trobats);                     // la resta, alfabèticament (TreeSet)

        if (ordenats.isEmpty()) {
            ordenats.add("en"); // no hauria de passar mai; millor un idioma que cap
        }
        return ordenats;
    }

    /**
     * [CA] Busca els {@code i18n/messages_XX.properties} que hi ha realment,
     * tant si l'app corre des d'un directori de classes (IDE) com des d'un JAR
     * (portable). Si l'escaneig falla per qualsevol motiu, cau a comprovar un
     * per un els tags de {@link #LANGUAGE_TAGS_IN_ORDER}, o sigui que mai es
     * queda sense idiomes.
     * <p>
     * [EN] Finds the {@code i18n/messages_XX.properties} files actually present,
     * whether the app runs from a class directory (IDE) or from a JAR
     * (portable). If scanning fails for any reason it falls back to probing the
     * tags in {@link #LANGUAGE_TAGS_IN_ORDER} one by one, so it is never left
     * with no languages.
     *
     * @return [CA] Tags trobats, alfabèticament / [EN] Tags found, alphabetically
     */
    private static Set<String> scanLanguageTags() {
        Set<String> tags = new TreeSet<>();
        ClassLoader cl = I18n.class.getClassLoader();
        try {
            Enumeration<URL> urls = cl.getResources("i18n");
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if ("file".equals(url.getProtocol())) {
                    File dir = new File(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8));
                    String[] noms = dir.list();
                    if (noms != null) {
                        for (String nom : noms) {
                            afegirTag(nom, tags);
                        }
                    }
                } else if ("jar".equals(url.getProtocol())) {
                    String path = url.getPath();               // file:/....jar!/i18n
                    int sep = path.indexOf('!');
                    if (sep > 0 && path.startsWith("file:")) {
                        String jarPath = URLDecoder.decode(
                                path.substring("file:".length(), sep), StandardCharsets.UTF_8);
                        try (JarFile jar = new JarFile(jarPath)) {
                            Enumeration<JarEntry> entrades = jar.entries();
                            while (entrades.hasMoreElements()) {
                                String nom = entrades.nextElement().getName();
                                if (nom.startsWith("i18n/")) {
                                    afegirTag(nom.substring("i18n/".length()), tags);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // cau al fallback de sota
        }
        if (tags.isEmpty()) {
            for (String tag : LANGUAGE_TAGS_IN_ORDER) {
                if (cl.getResource("i18n/messages_" + tag + ".properties") != null) {
                    tags.add(tag);
                }
            }
        }
        return tags;
    }

    /** Afegeix el tag d'un nom de fitxer si té la forma messages_XX.properties. */
    private static void afegirTag(String nomFitxer, Set<String> tags) {
        if (nomFitxer.startsWith("messages_") && nomFitxer.endsWith(".properties")) {
            String tag = nomFitxer.substring("messages_".length(),
                    nomFitxer.length() - ".properties".length());
            if (!tag.isEmpty()) {
                tags.add(tag);
            }
        }
    }

    /**
     * [CA] Nom que un idioma es dóna a si mateix, per retolar-hi un botó. Surt
     * de la clau {@code language.name} del seu propi bundle; si el fitxer no la
     * porta —cas típic d'una traducció acabada d'afegir— es fa servir el nom
     * que en dóna Java, per no ensenyar mai un {@code ??language.name??}.
     * <p>
     * [EN] The name a language gives itself, for labelling a button. It comes
     * from its own bundle's {@code language.name} key; if the file lacks it —
     * typical of a freshly added translation — Java's own display name is used,
     * so a {@code ??language.name??} is never shown.
     *
     * @param tag [CA] Tag de l'idioma / [EN] Language tag
     * @return [CA] Nom de l'idioma en ell mateix / [EN] The language's name in itself
     */
    public static String getLanguageDisplayName(String tag) {
        Locale loc = Locale.forLanguageTag(tag);
        String nom = tIn(loc, "language.name");
        if (nom.startsWith("??")) {
            nom = loc.getDisplayLanguage(loc);
            if (nom == null || nom.isBlank()) {
                return tag;
            }
            if (nom.length() > 1) {
                nom = nom.substring(0, 1).toUpperCase(loc) + nom.substring(1);
            }
        }
        return nom;
    }

    /**
     * [CA] Indica si un idioma porta la clau indicada. Serveix per no incloure
     * en un text multilingüe la línia d'un idioma que encara no l'ha traduïda.
     * <p>
     * [EN] Tells whether a language carries the given key. Used to leave out of
     * a multilingual text the line of a language that has not translated it yet.
     *
     * @param tag [CA] Tag de l'idioma / [EN] Language tag
     * @param key [CA] Clau a comprovar / [EN] Key to check
     * @return {@code true} [CA] si la clau hi és / [EN] if the key is there
     */
    public static boolean hasKey(String tag, String key) {
        return !tIn(Locale.forLanguageTag(tag), key).startsWith("??");
    }

    /**
     * [CA] Retorna el text traduït d'una clau en un idioma concret, <b>sense</b>
     * canviar l'idioma actiu de l'aplicació. Serveix per mostrar un missatge en
     * diversos idiomes alhora.
     * <p>
     * [EN] Returns a key's translation in a given language, <b>without</b>
     * changing the application's active language. Used to show one message in
     * several languages at once.
     *
     * @param loc [CA] Locale desitjat / [EN] Desired locale
     * @param key [CA] Clau del missatge / [EN] Message key
     * @return [CA] Text traduït o marcador d'error / [EN] Translated text or error marker
     */
    public static String tIn(Locale loc, String key) {
        if (key == null) {
            return "??null??";
        }
        if (loc == null) {
            return t(key);
        }
        try {
            return bundleFor(loc).getString(key);
        } catch (MissingResourceException e) {
            return "??" + key + "??";
        }
    }

    /**
     * [CA] Com {@link #tIn(Locale, String)} però substituint els placeholders
     * ({0}, {1}…) amb el format del locale demanat.
     * <p>
     * [EN] Like {@link #tIn(Locale, String)} but substituting placeholders
     * ({0}, {1}…) using the requested locale's format.
     *
     * @param loc  [CA] Locale desitjat / [EN] Desired locale
     * @param key  [CA] Clau del missatge / [EN] Message key
     * @param args [CA] Arguments dels placeholders / [EN] Placeholder arguments
     * @return [CA] Text formatat / [EN] Formatted text
     */
    public static String fIn(Locale loc, String key, Object... args) {
        String pattern = tIn(loc, key);
        MessageFormat mf = new MessageFormat(pattern, loc == null ? locale : loc);
        return mf.format(args == null ? new Object[0] : args);
    }

    private static ResourceBundle bundleFor(Locale loc) {
        return bundleCache.computeIfAbsent(loc.toLanguageTag(), k -> loadBundle(loc));
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
