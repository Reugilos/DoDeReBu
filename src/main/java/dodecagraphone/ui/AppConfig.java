package dodecagraphone.ui;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Configuració de l'aplicació.
 *
 * Lectura: Properties estàndard (get/set fàcil, suport UTF-8).
 * Escriptura: segueix l'estructura de /defaults/config.properties, substituint
 * els marcadors #i18n:clau per comentaris en l'idioma actiu (I18n.t).
 * Les claus sense entrada als defaults (lastDir*, instruments, etc.) s'afegeixen
 * al final sense comentari.
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

    public static AppConfig get() {
        return INSTANCE;
    }

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

    public String get(String key, String def) {
        ensureLoaded();
        return props.getProperty(key, def);
    }

    public void set(String key, String value) {
        ensureLoaded();
        if (value == null) {
            props.remove(key);
        } else {
            props.setProperty(key, value);
        }
    }

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

    public boolean getBool(String key, boolean def) {
        ensureLoaded();
        String v = props.getProperty(key);
        if (v == null) {
            return def;
        }
        v = v.trim().toLowerCase();
        return v.equals("true") || v.equals("1") || v.equals("yes") || v.equals("y");
    }

    public synchronized void save() throws IOException {
        ensureLoaded();
        Path p = AppPaths.getUserConfigPath();
        Files.createDirectories(p.getParent());
        writeWithLocalizedComments(p);
    }

    public synchronized void reload() throws IOException {
        ensureLoaded();
        loadFromFile(AppPaths.getUserConfigPath());
    }

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
