package dodecagraphone.ui;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Config amb: - lectura via Properties (per poder fer get/set fàcil) -
 * escriptura preservant comentaris, ordre i format - suport de valors
 * multi-línia amb "\" (line continuation) - afegeix claus noves a partir de
 * /defaults/config.properties mantenint comentaris
 *
 * Limitacions (acceptables en config habitual): - No tracta escapes exòtics de
 * unicode (XXXX) a nivell de reescriptura, però Properties.load sí que els
 * llegeix. - Si tens claus duplicades al fitxer usuari, només es modifica la
 * primera aparició.
 */
public final class AppConfig {

    private static final AppConfig INSTANCE = new AppConfig();
    private static final String DEFAULT_CONFIG_RESOURCE = "/defaults/config.properties";

    private final Properties props = new Properties();
    private boolean loaded = false;

    private AppConfig() {
    }

    public static AppConfig get() {
        return INSTANCE;
    }

    // Truca-ho 1 cop al principi (o des d'un static {} de Settings)
    public synchronized void init() {
        if (loaded) {
            return;
        }
        try {
            AppPaths.installUserConfigIfMissing();
            loadFromFile(AppPaths.getUserConfigPath());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            loaded = true;
        }
    }

    // Per si algú fa get() abans d'init()
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

    /**
     * Desa preservant comentaris i el format del fitxer (incloent valors amb
     * "\" al final de línia). També afegeix claus noves que existeixin a
     * defaults (amb els seus comentaris).
     */
    public synchronized void save() throws IOException {
        ensureLoaded();
        Path p = AppPaths.getUserConfigPath();
        Files.createDirectories(p.getParent());
        mergeAndWritePreservingComments(p);
    }

    public synchronized void reload() throws IOException {
        ensureLoaded();
        loadFromFile(AppPaths.getUserConfigPath());
    }

    private void loadFromFile(Path path) throws IOException {
        try ( InputStream in = new FileInputStream(path.toFile());  Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {

            props.clear();
            props.load(r);
        }
    }

    public String getConfigPathForDebug() {
        return AppPaths.getUserConfigPath().toString();
    }

    // =========================================================
    //  Merge preservant comentaris + suport "\" continuations
    // =========================================================
    private void mergeAndWritePreservingComments(Path userFile) throws IOException {

        List<String> userLines = Files.exists(userFile)
                ? Files.readAllLines(userFile, StandardCharsets.UTF_8)
                : new ArrayList<>();

        List<String> defaultLines = readDefaultLinesUtf8();

        // Parse user entries (inclou continuations)
        List<UserEntry> userEntries = parseUserEntries(userLines);

        // Map key -> primera entry
        Map<String, UserEntry> userByKey = new LinkedHashMap<>();
        for (UserEntry e : userEntries) {
            userByKey.putIfAbsent(e.key, e);
        }

        // Defaults: map key -> bloc (comentaris immediats + entry completa (pot ser multi-línia))
        LinkedHashMap<String, List<String>> defaultBlocks = extractDefaultBlocks(defaultLines);

        // 1) Actualitza valors existents al fitxer d'usuari (preservant prefix i estil de wrapping)
        //    Ho fem de baix cap amunt per no desquadrar índexs.
        userEntries.sort((a, b) -> Integer.compare(b.startIdx, a.startIdx));

        for (UserEntry ue : userEntries) {
            if (!props.containsKey(ue.key)) {
                continue;
            }

            String newVal = props.getProperty(ue.key, "");
            List<String> rebuilt = rebuildEntryLines(ue, newVal);

            replaceRange(userLines, ue.startIdx, ue.endIdx, rebuilt);
        }

        // 2) Afegeix claus que falten al fitxer (agafant comentaris + format de defaults quan existeixi)
        Set<String> missingKeys = new LinkedHashSet<>();
        for (String k : props.stringPropertyNames()) {
            if (!userByKey.containsKey(k)) {
                missingKeys.add(k);
            }
        }

        if (!missingKeys.isEmpty()) {
            if (!userLines.isEmpty() && !userLines.get(userLines.size() - 1).isBlank()) {
                userLines.add("");
            }

            for (String k : missingKeys) {
                List<String> block = defaultBlocks.get(k);
                String value = props.getProperty(k, "");

                if (block != null && !block.isEmpty()) {
                    // Reescrivim només la part de l'entry dins del bloc (si és multi-línia també)
                    List<String> out = new ArrayList<>();
                    UserEntry defEntry = null;

                    // separem comentaris vs entry
                    List<String> entryLines = new ArrayList<>();
                    boolean seenEntry = false;

                    for (String line : block) {
                        ParsedEntryStart ps = parseEntryStart(line);
                        if (!seenEntry && ps == null) {
                            out.add(line); // comentari
                        } else {
                            seenEntry = true;
                            entryLines.add(line);
                        }
                    }

                    if (!entryLines.isEmpty()) {
                        defEntry = parseSingleEntryFromLines(entryLines, 0);
                    }

                    if (defEntry != null) {
                        out.addAll(rebuildEntryLines(defEntry, value));
                    } else {
                        // fallback
                        out.add(k + "=" + escapeForProperties(value));
                    }

                    userLines.addAll(out);
                } else {
                    userLines.add(k + "=" + escapeForProperties(value));
                }

                if (!userLines.isEmpty() && !userLines.get(userLines.size() - 1).isBlank()) {
                    userLines.add("");
                }
            }
        }

        Files.write(userFile, userLines, StandardCharsets.UTF_8);
    }

    // ---------- parsing user (amb continuations)
    private static List<UserEntry> parseUserEntries(List<String> lines) {
        List<UserEntry> entries = new ArrayList<>();

        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i);

            ParsedEntryStart start = parseEntryStart(line);
            if (start == null) {
                i++;
                continue;
            }

            int startIdx = i;
            List<String> entryLines = new ArrayList<>();
            entryLines.add(line);

            int endIdx = i;
            while (endsWithUnescapedBackslash(lines.get(endIdx))) {
                // continua a la següent línia
                if (endIdx + 1 >= lines.size()) {
                    break;
                }
                endIdx++;
                entryLines.add(lines.get(endIdx));
            }

            UserEntry ue = parseSingleEntryFromLines(entryLines, startIdx);
            if (ue != null) {
                ue.endIdx = endIdx;
                entries.add(ue);
            }

            i = endIdx + 1;
        }

        return entries;
    }

    /**
     * Parse d'una entry (pot ser multi-línia) a partir de les seves línies
     * físiques. startIdx és l'índex dins del fitxer original (per poder
     * substituir).
     */
    private static UserEntry parseSingleEntryFromLines(List<String> entryLines, int startIdx) {
        if (entryLines == null || entryLines.isEmpty()) {
            return null;
        }

        ParsedEntryStart ps = parseEntryStart(entryLines.get(0));
        if (ps == null) {
            return null;
        }

        UserEntry ue = new UserEntry();
        ue.key = ps.key;
        ue.startIdx = startIdx;
        ue.endIdx = startIdx;

        ue.prefix = ps.prefix; // inclou "key", separador i espais post-separador
        ue.sepChar = ps.sepChar;

        // indent de continuació (si existeix)
        ue.contIndent = "";
        if (entryLines.size() > 1) {
            String l1 = entryLines.get(1);
            int n = 0;
            while (n < l1.length() && Character.isWhitespace(l1.charAt(n))) {
                n++;
            }
            ue.contIndent = l1.substring(0, n);
        }

        ue.hadContinuations = entryLines.size() > 1;

        // Guardem també el "width" aproximat per rewrapping
        ue.firstLineMax = Math.max(40, 120); // criteri simple

        return ue;
    }

    /**
     * Detecta si una línia és l'inici d'una entry (no comentari/blanc) i
     * retorna: - key - prefix exacte fins on comença el valor (incloent
     * separador i espais després)
     */
    private static ParsedEntryStart parseEntryStart(String line) {
        if (line == null) {
            return null;
        }

        String trimmed = line.stripLeading();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.startsWith("#") || trimmed.startsWith("!")) {
            return null;
        }

        int sep = -1;
        char sepChar = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '=' || c == ':') {
                sep = i;
                sepChar = c;
                break;
            }
        }
        if (sep < 0) {
            return null;
        }

        String keyPart = line.substring(0, sep).trim();
        if (keyPart.isEmpty()) {
            return null;
        }

        // prefix = tot fins a començar el valor (inclou separador i els espais immediats després)
        String afterSep = line.substring(sep + 1);
        int leadingSpaces = 0;
        while (leadingSpaces < afterSep.length() && Character.isWhitespace(afterSep.charAt(leadingSpaces))) {
            leadingSpaces++;
        }

        String prefix = line.substring(0, sep + 1) + afterSep.substring(0, leadingSpaces);

        ParsedEntryStart ps = new ParsedEntryStart();
        ps.key = keyPart;
        ps.prefix = prefix;
        ps.sepChar = sepChar;
        return ps;
    }

    private static boolean endsWithUnescapedBackslash(String line) {
        if (line == null) {
            return false;
        }
        int i = line.length() - 1;
        // ignora espais al final? (en properties, trailing spaces poden comptar; aquí ho deixem literal)
        // Si vols ignorar espais finals, descomenta:
        // while (i >= 0 && Character.isWhitespace(line.charAt(i))) i--;

        int count = 0;
        while (i >= 0 && line.charAt(i) == '\\') {
            count++;
            i--;
        }
        // si hi ha un nombre imparell de backslashes al final, l'últim és continuation
        return count % 2 == 1;
    }

    // ---------- defaults blocks (amb continuations)
    private static LinkedHashMap<String, List<String>> extractDefaultBlocks(List<String> defaultLines) {
        LinkedHashMap<String, List<String>> blocks = new LinkedHashMap<>();

        List<String> pendingComments = new ArrayList<>();
        int i = 0;

        while (i < defaultLines.size()) {
            String line = defaultLines.get(i);

            if (line.isBlank()) {
                pendingComments.clear();
                i++;
                continue;
            }

            String trimmed = line.stripLeading();
            if (trimmed.startsWith("#") || trimmed.startsWith("!")) {
                pendingComments.add(line);
                i++;
                continue;
            }

            ParsedEntryStart ps = parseEntryStart(line);
            if (ps == null) {
                pendingComments.clear();
                i++;
                continue;
            }

            // entry pot ser multi-línia
            List<String> entryLines = new ArrayList<>();
            entryLines.add(line);

            int j = i;
            while (endsWithUnescapedBackslash(defaultLines.get(j))) {
                if (j + 1 >= defaultLines.size()) {
                    break;
                }
                j++;
                entryLines.add(defaultLines.get(j));
            }

            List<String> block = new ArrayList<>();
            block.addAll(pendingComments);
            block.addAll(entryLines);
            blocks.put(ps.key, block);

            pendingComments.clear();
            i = j + 1;
        }

        return blocks;
    }

    private List<String> readDefaultLinesUtf8() throws IOException {
        try ( InputStream in = Objects.requireNonNull(
                AppConfig.class.getResourceAsStream(DEFAULT_CONFIG_RESOURCE),
                "Missing resource: " + DEFAULT_CONFIG_RESOURCE);  Reader r = new InputStreamReader(in, StandardCharsets.UTF_8);  BufferedReader br = new BufferedReader(r)) {

            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        }
    }

    // ---------- rebuild entry (preservant continuations)
    private static List<String> rebuildEntryLines(UserEntry ue, String newValueRaw) {
        String newValue = escapeForProperties(newValueRaw);

        boolean shouldWrap = ue.hadContinuations || (ue.prefix.length() + newValue.length() > ue.firstLineMax);

        if (!shouldWrap) {
            return Collections.singletonList(ue.prefix + newValue);
        }

        // wrap en múltiples línies amb "\"
        String contIndent = (ue.contIndent != null && !ue.contIndent.isEmpty()) ? ue.contIndent : "  ";

        int maxFirst = Math.max(20, ue.firstLineMax - ue.prefix.length() - 2); // espai per " \"
        int maxNext = Math.max(20, ue.firstLineMax - contIndent.length() - 2);

        List<String> chunks = wrapValue(newValue, maxFirst, maxNext);

        List<String> out = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            boolean last = (i == chunks.size() - 1);
            if (i == 0) {
                out.add(ue.prefix + chunks.get(i) + (last ? "" : " \\"));
            } else {
                out.add(contIndent + chunks.get(i) + (last ? "" : " \\"));
            }
        }
        return out;
    }

    /**
     * Wrap simple: intenta tallar per espais; si no, talla dur. maxFirst i
     * maxNext són límits aproximats de caràcters per línia (sense
     * indent/prefix).
     */
    private static List<String> wrapValue(String v, int maxFirst, int maxNext) {
        List<String> parts = new ArrayList<>();
        int idx = 0;
        int max = maxFirst;

        while (idx < v.length()) {
            int remaining = v.length() - idx;
            int take = Math.min(remaining, max);

            int cut = idx + take;
            if (cut < v.length()) {
                // intenta tallar per espai abans del límit
                int space = v.lastIndexOf(' ', cut);
                if (space > idx + 10) {
                    cut = space;
                }
            }

            String piece = v.substring(idx, cut).trim();
            parts.add(piece);

            idx = cut;
            while (idx < v.length() && v.charAt(idx) == ' ') {
                idx++; // menja espais
            }
            max = maxNext;
        }

        if (parts.isEmpty()) {
            parts.add("");
        }
        return parts;
    }

    /**
     * Escapa mínim per properties quan escrivim: - backslash -
     * tab/newline/carriage-return a \t \n \r
     *
     * (Si tens necessitat d'escapar ':' '=' o espais inicials, ho podem
     * afegir.)
     */
    private static String escapeForProperties(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' ->
                    sb.append("\\\\");
                case '\t' ->
                    sb.append("\\t");
                case '\n' ->
                    sb.append("\\n");
                case '\r' ->
                    sb.append("\\r");
                default ->
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private static void replaceRange(List<String> lines, int start, int end, List<String> replacement) {
        // elimina [start..end] i insereix replacement
        for (int i = end; i >= start; i--) {
            lines.remove(i);
        }
        lines.addAll(start, replacement);
    }

    // ---------- helpers / structs
    private static final class ParsedEntryStart {

        String key;
        String prefix;
        char sepChar;
    }

    private static final class UserEntry {

        String key;
        int startIdx;
        int endIdx;

        String prefix;         // "key<spaces><sep><spaces>"
        char sepChar;

        boolean hadContinuations;
        String contIndent;     // indent de les línies continuades
        int firstLineMax;      // ample aproximat per wrap
    }
}
