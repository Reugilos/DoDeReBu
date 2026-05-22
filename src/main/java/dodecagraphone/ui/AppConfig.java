/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.ui;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * [CA] Configuració de l'aplicació. Llegeix i desa {@code config.properties}
 * preservant l'estructura de comentaris i els marcadors {@code #i18n:clau}
 * del fitxer de defaults.
 * <p>
 * [EN] Application configuration. Reads and saves {@code config.properties}
 * while preserving comment structure and {@code #i18n:key} markers
 * from the defaults file.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public final class AppConfig {

    private static final AppConfig INSTANCE = new AppConfig();
    private static final String DEFAULT_CONFIG_RESOURCE = "/defaults/config.properties";

    // Claus d'versions antigues que no s'han de tornar a escriure
    private static final Set<String> STALE_KEYS = Set.of(
            "nBeatsMeasure", "nColsBeat", "fitAnacrusis", "octavesUP", "dodecaphone", "showMutted"
    );

    private final Properties props = new Properties();
    private boolean loaded = false;

    private AppConfig() {
    }

    /**
     * [CA] Retorna la instància singleton d'AppConfig.
     * <p>
     * [EN] Returns the singleton instance of AppConfig.
     *
     * @return [CA] La instància única / [EN] The singleton instance
     */
    public static AppConfig get() {
        return INSTANCE;
    }

    /**
     * [CA] Inicialitza la configuració carregant el fitxer de l'usuari.
     * Si el fitxer no existeix, el crea a partir dels defaults. Elimina les
     * claus obsoletes (STALE_KEYS). Pot cridar-se diverses vegades; la
     * càrrega real es fa una sola vegada.
     * <p>
     * [EN] Initializes the configuration by loading the user config file.
     * Creates it from defaults if it does not exist. Removes stale keys.
     * Safe to call multiple times; the actual load happens only once.
     */
    public synchronized void init() {
        if (loaded) {
            return;
        }
        try {
            AppPaths.installUserConfigIfMissing();
            loadFromFile(AppPaths.getUserConfigPath());
            STALE_KEYS.forEach(props::remove);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            loaded = true;
        }
    }

    private synchronized void ensureLoaded() {
        if (!loaded) {
            init();
        }
    }

    /**
     * [CA] Obté el valor d'una clau com a cadena, o el valor per defecte si no existeix.
     * <p>
     * [EN] Gets the value of a key as a string, or the default if not present.
     *
     * @param key [CA] Clau de configuració / [EN] Configuration key
     * @param def [CA] Valor per defecte / [EN] Default value
     * @return [CA] El valor trobat o el default / [EN] The found value or default
     */
    public String get(String key, String def) {
        ensureLoaded();
        return props.getProperty(key, def);
    }

    /**
     * [CA] Assigna o elimina el valor d'una clau. Si {@code value} és null,
     * la clau s'elimina.
     * <p>
     * [EN] Sets or removes a key's value. If {@code value} is null, the key
     * is removed.
     *
     * @param key   [CA] Clau de configuració / [EN] Configuration key
     * @param value [CA] Valor a assignar, o null per eliminar / [EN] Value to set, or null to remove
     */
    public void set(String key, String value) {
        ensureLoaded();
        if (value == null) {
            props.remove(key);
        } else {
            props.setProperty(key, value);
        }
    }

    /**
     * [CA] Obté el valor d'una clau com a enter.
     * <p>
     * [EN] Gets the value of a key as an integer.
     *
     * @param key [CA] Clau de configuració / [EN] Configuration key
     * @param def [CA] Valor per defecte si la clau no existeix o és invàlida / [EN] Default if key is absent or invalid
     * @return [CA] El valor enter o el default / [EN] The integer value or default
     */
    public int getInt(String key, int def) {
        ensureLoaded();
        String v = props.getProperty(key);
        if (v == null) {
            return def;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (Exception ex) {
            return def;
        }
    }

    /**
     * [CA] Obté el valor d'una clau com a booleà.
     * <p>
     * [EN] Gets the value of a key as a boolean.
     *
     * @param key [CA] Clau de configuració / [EN] Configuration key
     * @param def [CA] Valor per defecte / [EN] Default value
     * @return [CA] true si el valor és "true", "1", "yes" o "y" / [EN] true if value is "true", "1", "yes" or "y"
     */
    public boolean getBool(String key, boolean def) {
        ensureLoaded();
        String v = props.getProperty(key);
        if (v == null) {
            return def;
        }
        v = v.trim().toLowerCase();
        return v.equals("true") || v.equals("1") || v.equals("yes") || v.equals("y");
    }

    /**
     * [CA] Desa la configuració al fitxer de l'usuari, seguint l'estructura
     * dels defaults i substituint els marcadors {@code #i18n:clau} per
     * comentaris localitzats.
     * <p>
     * [EN] Saves the configuration to the user file, following the defaults
     * structure and replacing {@code #i18n:key} markers with localized comments.
     *
     * @throws IOException [CA] Si falla l'escriptura / [EN] If writing fails
     */
    public synchronized void save() throws IOException {
        ensureLoaded();
        Path p = AppPaths.getUserConfigPath();
        Files.createDirectories(p.getParent());
        writeWithLocalizedComments(p);
    }

    /**
     * [CA] Recarrega la configuració des del fitxer de l'usuari.
     * <p>
     * [EN] Reloads the configuration from the user file.
     *
     * @throws IOException [CA] Si falla la lectura / [EN] If reading fails
     */
    public synchronized void reload() throws IOException {
        ensureLoaded();
        loadFromFile(AppPaths.getUserConfigPath());
    }

    /**
     * [CA] Retorna la ruta del fitxer de configuració com a cadena (per a depuració).
     * <p>
     * [EN] Returns the configuration file path as a string (for debugging).
     *
     * @return [CA] Ruta absoluta del fitxer / [EN] Absolute path to the config file
     */
    public String getConfigPathForDebug() {
        return AppPaths.getUserConfigPath().toString();
    }

    // =========================================================
    //  Escriptura seguint l'estructura dels defaults + comentaris I18n
    // =========================================================
    private void writeWithLocalizedComments(Path userFile) throws IOException {
        List<String> defaultLines = readDefaultLinesUtf8();
        List<String> out = new ArrayList<>();
        Set<String> writtenKeys = new LinkedHashSet<>();

        for (String line : defaultLines) {
            String trimmed = line.stripLeading();

            if (trimmed.startsWith("#i18n:")) {
                String i18nKey = trimmed.substring(6).trim();
                String text = I18n.t(i18nKey);
                if (!text.startsWith("??")) {
                    out.add("# " + text);
                }
                continue;
            }

            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!")) {
                out.add(line);
                continue;
            }

            ParsedEntry pe = parseEntry(line);
            if (pe == null) {
                out.add(line);
                continue;
            }

            writtenKeys.add(pe.key);
            String value = props.containsKey(pe.key)
                    ? props.getProperty(pe.key)
                    : line.substring(pe.prefixLen); // valor per defecte
            out.add(pe.key + "=" + escape(value));
        }

        // Claus d'estat en temps d'execució no cobertes pels defaults
        boolean addedExtra = false;
        for (String key : props.stringPropertyNames()) {
            if (!writtenKeys.contains(key)) {
                if (!addedExtra) {
                    out.add("");
                    addedExtra = true;
                }
                out.add(key + "=" + escape(props.getProperty(key)));
            }
        }

        Files.write(userFile, out, StandardCharsets.UTF_8);
    }

    private void loadFromFile(Path path) throws IOException {
        try (InputStream in = new FileInputStream(path.toFile());
             Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            props.clear();
            props.load(r);
        }
    }

    private List<String> readDefaultLinesUtf8() throws IOException {
        try (InputStream in = Objects.requireNonNull(
                AppConfig.class.getResourceAsStream(DEFAULT_CONFIG_RESOURCE),
                "Missing resource: " + DEFAULT_CONFIG_RESOURCE);
             BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        }
    }

    // ---------- helpers ----------
    private static ParsedEntry parseEntry(String line) {
        if (line == null) {
            return null;
        }
        String trimmed = line.stripLeading();
        if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!")) {
            return null;
        }
        int sep = -1;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '=' || c == ':') {
                sep = i;
                break;
            }
        }
        if (sep < 0) {
            return null;
        }
        String key = line.substring(0, sep).trim();
        if (key.isEmpty()) {
            return null;
        }
        String afterSep = line.substring(sep + 1);
        int spaces = 0;
        while (spaces < afterSep.length() && Character.isWhitespace(afterSep.charAt(spaces))) {
            spaces++;
        }
        ParsedEntry pe = new ParsedEntry();
        pe.key = key;
        pe.prefixLen = sep + 1 + spaces;
        return pe;
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '\t' -> sb.append("\\t");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private static final class ParsedEntry {
        String key;
        int prefixLen;
    }
}
