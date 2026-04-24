package dodecagraphone.model.chord;

import dodecagraphone.ui.I18n;
import dodecagraphone.ui.Settings;
import java.util.*;

/**
 * Chord symbol conversion between all supported formats.
 *
 * Formats:
 *   DodecaFormat        – user's format:  So[0,4,7] / So[0,4,7]/Re
 *   Simbol              – Anglo symbol:   G  / Gm7b5 / G7b9
 *   Posicions diatòn.   – degrees:        [1,3,5]
 *   Notes anglosaxo     – note names:     [G, B, D]
 *   Solfeig             – solfège:        [sol, si, re]
 *   Intervals           – raw intervals:  [0,4,7]
 *   DodecaNotes         – dodeca notes:   [so, ti, re]
 *   Nom genèric         – generic name:   Major
 *
 * All data is hardcoded from AllSymbols.csv — no runtime file reading needed.
 *
 * Root is always a dodecaphonic name (do/De/re/Ri … case-insensitive) or
 * an Anglo note name (C/Db/D/Eb/E/F/F#/G/Ab/A/Bb/B).
 */
public class ChordSymbols {

    // ── Format name constants ────────────────────────────────────────────────
    public static final String FORMAT_MY        = "DodecaFormat";
    public static final String FORMAT_SIMBOL    = "Simbol";
    public static final String FORMAT_SINONIMS  = "Sinònims";
    public static final String FORMAT_POSICIONS = "Posicions diatòniques";
    public static final String FORMAT_NOTES     = "Notes anglosaxo";
    public static final String FORMAT_SOLFEIG   = "Solfeig";
    public static final String FORMAT_INTERVALS = "Intervals";
    public static final String FORMAT_DODECA    = "DodecaNotes";
    public static final String FORMAT_NOM       = "Nom genèric";
    public static final String FORMAT_MIDI      = "Midi";

    /** All display formats in cycling order (Sinònims and Nom genèric excluded). */
    public static final String[] DISPLAY_FORMATS = {
        FORMAT_MY, FORMAT_DODECA, FORMAT_MIDI, FORMAT_SIMBOL, FORMAT_POSICIONS, FORMAT_SOLFEIG, FORMAT_NOTES
    };

    public static String formatLabel(String fmt) {
        switch (fmt) {
            case FORMAT_MY:        return I18n.t("chordFormat.dodeca");
            case FORMAT_DODECA:    return I18n.t("chordFormat.dodecaNoms");
            case FORMAT_SIMBOL:    return I18n.t("chordFormat.simbol");
            case FORMAT_POSICIONS: return I18n.t("chordFormat.intervals");
            case FORMAT_SOLFEIG:   return I18n.t("chordFormat.solfeig");
            case FORMAT_NOTES:     return I18n.t("chordFormat.anglo");
            case FORMAT_MIDI:      return I18n.t("chordFormat.midi");
            default:               return fmt;
        }
    }

    // ── Note tables ──────────────────────────────────────────────────────────
    /** Dodecaphonic note names, index = semitone 0-11. */
    static final String[] DODECA =
        {"do","de","re","ri","mi","fa","fo","so","sa","la","li","ti"};

    /** Anglo root names for chord symbols (preferred spelling per root). */
    static final String[] ANGLO_ROOT =
        {"C","Db","D","Eb","E","F","F#","G","Ab","A","Bb","B"};

    /** Anglo note names – flat preference. */
    static final String[] ANGLO_FLAT  =
        {"C","Db","D","Eb","E","F","Gb","G","Ab","A","Bb","B"};
    /** Anglo note names – sharp preference. */
    static final String[] ANGLO_SHARP =
        {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};

    /** Solfège – flat preference. */
    static final String[] SOLFEGE_FLAT  =
        {"do","reb","re","mib","mi","fa","solb","sol","lab","la","sib","si"};
    /** Solfège – sharp preference. */
    static final String[] SOLFEGE_SHARP =
        {"do","do#","re","re#","mi","fa","fa#","sol","sol#","la","la#","si"};

    // ── Root → pitch-class map (dodeca + Anglo, upper & lower) ──────────────
    static final Map<String,Integer> ROOT_TO_PC = new HashMap<>();
    static {
        for (int i = 0; i < 12; i++) {
            ROOT_TO_PC.put(DODECA[i], i);
            ROOT_TO_PC.put(cap(DODECA[i]), i);
        }
        String[] an = {"C","C#","Db","D","D#","Eb","E","F","F#","Gb","G","G#","Ab","A","A#","Bb","B"};
        int[]    ap = { 0,   1,   1,   2,   3,   3,  4,  5,   6,   6,  7,   8,   8,  9,  10,  10, 11};
        for (int i = 0; i < an.length; i++) ROOT_TO_PC.put(an[i], ap[i]);
    }

    // ── Tension intervals ────────────────────────────────────────────────────
    /** Semitone values for compound tension intervals (> 11). */
    static final int[]     TENSION_IVLS    = {13, 14, 15, 17, 18, 20, 21};
    /** Mod-12 values of tension intervals (used to promote small values on input). */
    static final Set<Integer> TENSION_MOD12;
    static {
        TENSION_MOD12 = new HashSet<>();
        for (int iv : TENSION_IVLS) TENSION_MOD12.add(iv % 12);
    }
    /** Suffix appended to chord symbol for each tension. */
    static final String[]  TENSION_SUFFIX  = {"b9","9","#9","11","#11","b13","13"};
    /** Whether the tension note prefers flat spelling. */
    static final boolean[] TENSION_FLAT    = {true,false,false,false,false,true,false};

    // ── Chord templates ──────────────────────────────────────────────────────
    static class Template {
        final int[]     intervals;
        final String    suffix;    // chord-type suffix for Simbol (after root letter)
        final String[]  sinonims;
        final String    posicions;
        /** Per-interval flat preference (parallel to intervals[]). */
        final boolean[] useFlat;
        final String    nomGeneric;

        Template(int[] iv, String sx, String[] sn, String pos, boolean[] uf, String nom) {
            intervals = iv; suffix = sx; sinonims = sn;
            posicions = pos; useFlat = uf; nomGeneric = nom;
        }
    }

    static final Template[] TEMPLATES = {
        // ── Tríades ──────────────────────────────────────────────────────────
        new Template(new int[]{0,4,7},      "",      new String[]{},
            "[1,3,5]",    new boolean[]{false,false,false},        "Major"),
        new Template(new int[]{0,3,7},      "m",     new String[]{"-"},
            "[1,b3,5]",   new boolean[]{false,true,false},          "Menor"),
        new Template(new int[]{0,3,6},      "dim",   new String[]{"mb5","°","o"},
            "[1,b3,b5]",  new boolean[]{false,true,true},            "Disminuït"),
        new Template(new int[]{0,4,8},      "aug",   new String[]{"+"},
            "[1,3,#5]",   new boolean[]{false,false,false},          "Augmentat"),
        new Template(new int[]{0,2,7},      "sus2",  new String[]{"2"},
            "[1,2,5]",    new boolean[]{false,false,false},          "Suspès 2"),
        new Template(new int[]{0,5,7},      "sus4",  new String[]{"4"},
            "[1,4,5]",    new boolean[]{false,false,false},          "Suspès 4"),
        // ── Quatrèades diatòniques ────────────────────────────────────────────
        new Template(new int[]{0,4,7,11},   "maj7",  new String[]{"△","Δ"},
            "[1,3,5,7]",   new boolean[]{false,false,false,false},   "Major sèptima"),
        new Template(new int[]{0,3,7,10},   "m7",    new String[]{"-7"},
            "[1,b3,5,b7]", new boolean[]{false,true,false,true},     "Menor sèptima"),
        new Template(new int[]{0,4,7,10},   "7",     new String[]{},
            "[1,3,5,b7]",  new boolean[]{false,false,false,true},    "Dominant"),
        new Template(new int[]{0,3,6,10},   "m7b5",  new String[]{"ø"},
            "[1,b3,b5,b7]",new boolean[]{false,true,true,true},      "Semidisminuït"),
        // ── Altres quatrèades ─────────────────────────────────────────────────
        new Template(new int[]{0,4,7,9},    "6",     new String[]{},
            "[1,3,5,6]",   new boolean[]{false,false,false,false},   ""),
        new Template(new int[]{0,2,7,10},   "7sus2", new String[]{},
            "[1,2,5,b7]",  new boolean[]{false,false,false,true},    ""),
        new Template(new int[]{0,5,7,10},   "7sus4", new String[]{},
            "[1,4,5,b7]",  new boolean[]{false,false,false,true},    ""),
        new Template(new int[]{0,3,6,9},    "dim7",
            new String[]{"°","o","°7","o7","mb5bb7"},
            "[1,b3,b5,bb7]",new boolean[]{false,true,true,true},     "Completament disminuït"),
        // ── Acord + tensió ────────────────────────────────────────────────────
        new Template(new int[]{0,4,7,11,14},"maj9",  new String[]{},
            "[1,3,5,7,9]",  new boolean[]{false,false,false,false,false}, ""),
        new Template(new int[]{0,3,7,10,14},"m9",    new String[]{},
            "[1,b3,5,b7,9]",new boolean[]{false,true,false,true,false},   ""),
        new Template(new int[]{0,4,7,10,14},"9",     new String[]{"79"},
            "[1,3,5,b7,9]", new boolean[]{false,false,false,true,false},  ""),
        new Template(new int[]{0,3,6,10,14},"m9b5",  new String[]{},
            "[1,b3,b5,b7,9]",new boolean[]{false,true,true,true,false},   ""),
        new Template(new int[]{0,4,7,14},   "add9",  new String[]{},
            "[1,3,5,9]",    new boolean[]{false,false,false,false},        ""),
        new Template(new int[]{0,3,7,14},   "madd9",
            new String[]{"m add9","madd9"},
            "[1,b3,5,9]",   new boolean[]{false,true,false,false},         ""),
    };

    // ── Lookup maps ──────────────────────────────────────────────────────────
    /** intervals-array key → Template */
    static final Map<String,Template> BY_INTERVALS = new LinkedHashMap<>();
    /** chord-type suffix → Template */
    static final Map<String,Template> BY_SUFFIX    = new LinkedHashMap<>();
    /** synonym suffix → canonical suffix */
    static final Map<String,String>   SIN_TO_SUF   = new HashMap<>();

    static {
        for (Template t : TEMPLATES) {
            BY_INTERVALS.put(Arrays.toString(t.intervals), t);
            BY_SUFFIX.put(t.suffix, t);
            for (String s : t.sinonims) SIN_TO_SUF.put(s, t.suffix);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  PUBLIC API
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Convert FROM user's format TO any named format.
     *
     * @param myChord    e.g. "So[0,4,7]" or "So[0,4,7]/Re"
     * @param targetFmt  one of the FORMAT_* constants (not FORMAT_SINONIMS)
     * @return converted string, or null if not convertible
     */
    public static String toFormat(String myChord, String targetFmt) {
        if (myChord == null || myChord.isBlank()) return null;
        String s = myChord.trim();

        // Extract bass note (after '/')
        String bassDodeca = null;
        int sl = s.indexOf('/');
        if (sl > 0) { bassDodeca = s.substring(sl + 1).trim(); s = s.substring(0, sl).trim(); }

        // Extract root and intervals
        int lb = s.indexOf('['), rb = s.lastIndexOf(']');
        if (lb < 0 || rb < 0) return null;
        String rootStr   = s.substring(0, lb).trim();
        String ivsStr    = s.substring(lb + 1, rb).trim();

        Integer rootPc = resolvePc(rootStr);
        if (rootPc == null) return null;

        int[] ivs;
        try { ivs = parseIntArr(ivsStr); } catch (Exception e) { return null; }

        Integer bassPc = (bassDodeca != null) ? resolvePc(bassDodeca) : null;

        return convert(rootPc, ivs, bassPc, targetFmt);
    }

    /**
     * Convert FROM any named format TO user's format.
     *
     * @param chordStr  chord string in the source format
     * @param root      root note in dodecaphonic or Anglo notation (e.g. "so", "G")
     * @param srcFmt    one of the FORMAT_* constants
     * @return chord in user's format (e.g. "So[0,4,7]"), or null
     */
    public static String fromFormat(String chordStr, String root, String srcFmt) {
        if (chordStr == null || root == null) return null;
        if (FORMAT_MY.equals(srcFmt)) return chordStr;

        Integer rootPc = resolvePc(root);
        if (rootPc == null) return null;
        String rootCap = cap(DODECA[rootPc]);

        switch (srcFmt) {

            case FORMAT_SIMBOL:
            case FORMAT_SINONIMS: {
                String s = chordStr.trim();
                String bassResult = null;
                int sl = s.indexOf('/');
                if (sl > 0) {
                    String bassToken = s.substring(sl + 1).trim();
                    String bName = bassTokenToDodeca(bassToken, rootPc);
                    if (bName != null) bassResult = bName;
                    s = s.substring(0, sl).trim();
                }
                // Strip Anglo root
                String sx = stripAngloRoot(s);
                if (sx == null) return null;
                // Resolve suffix (may include tension modifiers)
                int[] ivs = resolveSuffixToIntervals(sx);
                if (ivs == null) return null;
                String r = rootCap + "[" + joinInts(ivs) + "]";
                if (bassResult != null) r += "/" + bassResult;
                return r;
            }

            case FORMAT_INTERVALS: {
                String s = chordStr.trim();
                if (!s.startsWith("[") || !s.endsWith("]")) return null;
                try {
                    int[] ivs = parseIntArr(s.substring(1, s.length() - 1).trim());
                    return rootCap + "[" + joinInts(canonicalizeIntervals(ivs)) + "]";
                } catch (NumberFormatException e) {
                    return rootCap + s;
                }
            }

            case FORMAT_POSICIONS: {
                String s = chordStr.trim();
                for (Template t : TEMPLATES) {
                    if (t.posicions.equalsIgnoreCase(s))
                        return rootCap + "[" + joinInts(t.intervals) + "]";
                }
                return null;
            }

            case FORMAT_NOM: {
                String s = chordStr.trim();
                for (Template t : TEMPLATES) {
                    if (t.nomGeneric.equalsIgnoreCase(s))
                        return rootCap + "[" + joinInts(t.intervals) + "]";
                }
                return null;
            }

            case FORMAT_NOTES:
            case FORMAT_SOLFEIG:
            case FORMAT_DODECA: {
                String s = chordStr.trim();
                if (!s.startsWith("[") || !s.endsWith("]")) return null;
                String inside = s.substring(1, s.length() - 1).trim();

                String bassNote = null;
                int sc = inside.indexOf(';');
                if (sc >= 0) { bassNote = inside.substring(0, sc).trim(); inside = inside.substring(sc + 1).trim(); }

                String[] parts = inside.split(",");
                int[] pcs = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    Integer pc = noteNameToPc(parts[i].trim(), srcFmt);
                    if (pc == null) return null;
                    pcs[i] = pc;
                }

                // Compute mod-12 intervals relative to rootPc
                int[] modIvs = new int[pcs.length];
                for (int i = 0; i < pcs.length; i++)
                    modIvs[i] = ((pcs[i] - rootPc) + 12) % 12;

                // Recover canonical (possibly extended) intervals
                int[] ivs = canonicalFromMod12(modIvs);

                String r = rootCap + "[" + joinInts(ivs) + "]";
                if (bassNote != null) {
                    Integer bpc = noteNameToPc(bassNote, srcFmt);
                    if (bpc != null) r += "/" + cap(DODECA[bpc]);
                }
                return r;
            }

            default: return null;
        }
    }

    /**
     * Return all possible chords for a given root in a given format.
     * Includes all 20 templates. Root in dodecaphonic notation.
     */
    public static List<String> allChordsForRoot(String root, String fmt) {
        Integer rootPc = resolvePc(root);
        if (rootPc == null) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (Template t : TEMPLATES) {
            String chord = convert(rootPc, t.intervals, null, fmt);
            if (chord != null && !chord.isBlank()) result.add(chord);
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CORE CONVERSION
    // ════════════════════════════════════════════════════════════════════════

    private static String convert(int rootPc, int[] ivs, Integer bassPc, String fmt) {
        // Split into base (≤11) and extension (>11) intervals
        int[] baseIvs    = filterBase(ivs);
        int[] extIvs     = filterExt(ivs);

        Template t = BY_INTERVALS.get(Arrays.toString(baseIvs));
        // If no exact match, try with all intervals (might be a named combined chord)
        if (t == null) t = BY_INTERVALS.get(Arrays.toString(ivs));

        String rootCap   = cap(DODECA[rootPc]);
        String rootAnglo = ANGLO_ROOT[rootPc];

        switch (fmt) {

            case FORMAT_MY: {
                String r = rootCap + "[" + joinInts(ivs) + "]";
                if (bassPc != null) r += "/" + cap(DODECA[bassPc]);
                return r;
            }

            case FORMAT_INTERVALS: {
                if (bassPc == null) return "[" + joinInts(ivs) + "]";
                return "[" + bassIv(rootPc, bassPc) + ";" + joinInts(ivs) + "]";
            }

            case FORMAT_SIMBOL: {
                Template base = (t != null) ? t : findBaseTemplate(baseIvs);
                if (base == null) return null;
                String suffix = base.suffix;
                for (int i = 0; i < TENSION_IVLS.length; i++) {
                    for (int ev : extIvs) if (ev == TENSION_IVLS[i]) { suffix += TENSION_SUFFIX[i]; break; }
                }
                String r = rootAnglo + suffix;
                if (bassPc != null) r += "/" + ANGLO_ROOT[bassPc];
                return r;
            }

            case FORMAT_POSICIONS: {
                if (t == null) return null;
                if (bassPc == null) return t.posicions;
                int bassDeg = semToDeg(((bassPc - rootPc) + 12) % 12);
                return "[" + bassDeg + ";" + t.posicions.substring(1);
            }

            case FORMAT_NOTES: {
                List<String> notes = buildNoteList(rootPc, ivs, t, ANGLO_FLAT, ANGLO_SHARP);
                String r = "[" + String.join(", ", notes) + "]";
                if (bassPc != null) r = "[" + ANGLO_ROOT[bassPc] + "; " + r.substring(1);
                return r;
            }

            case FORMAT_SOLFEIG: {
                List<String> notes = buildNoteList(rootPc, ivs, t, SOLFEGE_FLAT, SOLFEGE_SHARP);
                String r = "[" + String.join(", ", notes) + "]";
                if (bassPc != null) r = "[" + SOLFEGE_FLAT[bassPc] + "; " + r.substring(1);
                return r;
            }

            case FORMAT_DODECA: {
                List<String> notes = new ArrayList<>();
                for (int iv : ivs) notes.add(DODECA[((rootPc + iv) % 12 + 12) % 12]);
                String r = "[" + String.join(", ", notes) + "]";
                if (bassPc != null) r = "[" + DODECA[bassPc] + "; " + r.substring(1);
                return r;
            }

            case FORMAT_NOM: {
                if (t == null || t.nomGeneric == null || t.nomGeneric.isEmpty()) return null;
                return cap(DODECA[rootPc]) + " " + t.nomGeneric;
            }

            default: return null;
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private static List<String> buildNoteList(int rootPc, int[] ivs, Template t,
                                               String[] flatArr, String[] sharpArr) {
        List<String> notes = new ArrayList<>();
        for (int i = 0; i < ivs.length; i++) {
            int iv = ivs[i];
            int pc = ((rootPc + iv) % 12 + 12) % 12;
            boolean flat;
            if (t != null && i < t.useFlat.length) {
                flat = t.useFlat[i];
            } else {
                flat = tensionFlat(iv);
            }
            notes.add(flat ? flatArr[pc] : sharpArr[pc]);
        }
        return notes;
    }

    private static boolean tensionFlat(int iv) {
        for (int i = 0; i < TENSION_IVLS.length; i++)
            if (TENSION_IVLS[i] == iv) return TENSION_FLAT[i];
        return false;
    }

    /**
     * If s begins with a dodeca or solfège root name, returns {dodecaRootName, suffix}.
     * Tries longest roots first to avoid partial matches (e.g. "sol" before "so").
     * Returns null if no match or if the entire string is consumed (root-only handled elsewhere).
     */
    private static String[] stripDodecaSolfeigRoot(String s) {
        if (s == null || s.length() < 2) return null;
        String sLow = s.toLowerCase();
        // (root_lowercase, dodeca_name) pairs sorted by length desc
        String[][] roots = {
            // 4-char solfège
            {"sol#", DODECA[8]}, {"solb", DODECA[6]},
            // 3-char solfège + dodeca-sharp
            {"reb", DODECA[1]}, {"mib", DODECA[3]}, {"lab", DODECA[8]},
            {"sib", DODECA[10]}, {"sol", DODECA[7]},
            {"do#", DODECA[1]}, {"re#", DODECA[3]}, {"fa#", DODECA[6]}, {"la#", DODECA[10]},
            // 2-char dodeca + solfège
            {"do", DODECA[0]}, {"de", DODECA[1]}, {"re", DODECA[2]}, {"ri", DODECA[3]},
            {"mi", DODECA[4]}, {"fa", DODECA[5]}, {"fo", DODECA[6]}, {"so", DODECA[7]},
            {"sa", DODECA[8]}, {"la", DODECA[9]}, {"li", DODECA[10]}, {"ti", DODECA[11]},
            {"si", DODECA[11]}
        };
        for (String[] pair : roots) {
            String rootLow = pair[0];
            if (sLow.startsWith(rootLow) && s.length() > rootLow.length()) {
                return new String[]{pair[1], s.substring(rootLow.length())};
            }
        }
        return null;
    }

    /**
     * If all intervals are ≤ 11 and the array has a recognizable base-chord prefix,
     * promotes any remaining values whose mod-12 is a tension value by adding 12.
     * E.g. [0,4,7,2] → prefix [0,4,7]=major, 2∈tensionMod12 → [0,4,7,14].
     */
    static int[] canonicalizeIntervals(int[] ivs) {
        boolean hasExt = false;
        for (int iv : ivs) if (iv > 11) { hasExt = true; break; }
        if (hasExt) return ivs; // already has explicit extended intervals
        if (BY_INTERVALS.containsKey(Arrays.toString(ivs))) return ivs; // exact template match
        // Find longest prefix (2–4 notes) matching a base template
        for (int len = Math.min(ivs.length - 1, 4); len >= 2; len--) {
            int[] prefix = Arrays.copyOf(ivs, len);
            if (BY_INTERVALS.containsKey(Arrays.toString(prefix))) {
                int[] result = Arrays.copyOf(ivs, ivs.length);
                for (int i = len; i < ivs.length; i++) {
                    if (TENSION_MOD12.contains(result[i])) result[i] += 12;
                }
                return result;
            }
        }
        return ivs;
    }

    /** Strip the Anglo root letter(s) from s; return the suffix or null. */
    private static String stripAngloRoot(String s) {
        if (s.length() >= 2) {
            String r2 = Character.toUpperCase(s.charAt(0)) + s.substring(1, 2);
            if (ROOT_TO_PC.containsKey(r2)) return s.substring(2);
        }
        if (s.length() >= 1) {
            String r1 = String.valueOf(Character.toUpperCase(s.charAt(0)));
            if (ROOT_TO_PC.containsKey(r1)) return s.substring(1);
        }
        return null;
    }

    /**
     * Given a chord-type suffix (possibly with tension suffixes appended),
     * resolve to the full interval array.
     */
    /** Normalise case-insensitive keywords in a chord suffix. */
    private static String normalizeSuffix(String suffix) {
        if (suffix == null) return null;
        return suffix
            .replaceAll("(?i)dim", "dim")
            .replaceAll("(?i)aug", "aug")
            .replaceAll("(?i)maj", "maj")
            .replaceAll("(?i)sus", "sus");
    }

    private static int[] resolveSuffixToIntervals(String suffix) {
        suffix = normalizeSuffix(suffix);
        if (suffix != null) suffix = suffix.trim().replaceAll("\\s+", "");
        // Try direct match first
        Template t = BY_SUFFIX.get(suffix);
        if (t != null) return t.intervals;
        String canon = SIN_TO_SUF.get(suffix);
        if (canon != null && BY_SUFFIX.containsKey(canon)) return BY_SUFFIX.get(canon).intervals;

        // Extract tension suffixes from the end (try longest tension first)
        String baseSuffix = suffix;
        List<Integer> tensions = new ArrayList<>();
        // Ordered so longer matches win
        String[] tsorted = {"#11","b13","#9","b9","11","13","9"};
        boolean changed = true;
        while (changed) {
            changed = false;
            for (String ts : tsorted) {
                if (baseSuffix.endsWith(ts)) {
                    for (int i = 0; i < TENSION_SUFFIX.length; i++) {
                        if (TENSION_SUFFIX[i].equals(ts)) { tensions.add(TENSION_IVLS[i]); break; }
                    }
                    baseSuffix = baseSuffix.substring(0, baseSuffix.length() - ts.length());
                    changed = true;
                    break;
                }
            }
        }
        // Resolve base
        Template base = BY_SUFFIX.get(baseSuffix);
        if (base == null) {
            String c2 = SIN_TO_SUF.get(baseSuffix);
            if (c2 != null) base = BY_SUFFIX.get(c2);
        }
        if (base == null) return null;

        // Combine
        int[] combined = Arrays.copyOf(base.intervals, base.intervals.length + tensions.size());
        for (int i = 0; i < tensions.size(); i++) combined[base.intervals.length + i] = tensions.get(i);
        Arrays.sort(combined);
        return combined;
    }

    private static Template findBaseTemplate(int[] baseIvs) {
        return BY_INTERVALS.get(Arrays.toString(baseIvs));
    }

    /**
     * Given mod-12 intervals (relative to root), try to find the canonical
     * extended interval array by matching templates mod-12.
     */
    private static int[] canonicalFromMod12(int[] modIvs) {
        for (Template t : TEMPLATES) {
            if (t.intervals.length != modIvs.length) continue;
            boolean ok = true;
            for (int i = 0; i < modIvs.length; i++) {
                if (((t.intervals[i] % 12) + 12) % 12 != ((modIvs[i] % 12) + 12) % 12) { ok = false; break; }
            }
            if (ok) return t.intervals;
        }
        return modIvs; // no match: return as-is
    }

    private static int[] filterBase(int[] ivs) {
        return Arrays.stream(ivs).filter(x -> x <= 11).toArray();
    }
    private static int[] filterExt(int[] ivs) {
        return Arrays.stream(ivs).filter(x -> x > 11).toArray();
    }

    private static Integer noteNameToPc(String name, String fmt) {
        switch (fmt) {
            case FORMAT_NOTES: return ROOT_TO_PC.get(name);
            case FORMAT_SOLFEIG: {
                String[][] sf = {
                    {"do","reb","re","mib","mi","fa","solb","sol","lab","la","sib","si"},
                    {"do#","re#","fa#","sol#","la#"}
                };
                int[] sfPc2 = {1,3,6,8,10};
                for (int i = 0; i < 12; i++) if (sf[0][i].equalsIgnoreCase(name)) return i;
                for (int i = 0; i < sfPc2.length; i++) if (sf[1][i].equalsIgnoreCase(name)) return sfPc2[i];
                return null;
            }
            case FORMAT_DODECA: {
                for (int i = 0; i < 12; i++) if (DODECA[i].equalsIgnoreCase(name)) return i;
                return null;
            }
            default: return null;
        }
    }

    private static Integer resolvePc(String name) {
        if (name == null) return null;
        Integer pc = ROOT_TO_PC.get(name);
        if (pc != null) return pc;
        pc = ROOT_TO_PC.get(name.toLowerCase());
        if (pc != null) return pc;
        // Handle lowercase Anglo roots (e.g. "g" → "G")
        if (name.length() >= 1) {
            String capped = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            pc = ROOT_TO_PC.get(capped);
            if (pc != null) return pc;
        }
        // Handle solfège names (e.g. "Sol" → "so" → pc 7)
        String sd = solfeigToDodeca(name.toLowerCase());
        if (sd != null) pc = ROOT_TO_PC.get(sd);
        return pc;
    }

    /**
     * Converts a bass token to a dodeca note name, given the root pitch class.
     * Accepts: dodeca/solfège/Anglo note name, negative interval (e.g. "-5"),
     * or positive integer (treated as pitch-class offset from root, e.g. "2" → Re).
     * Returns null if unrecognisable.
     */
    private static String bassTokenToDodeca(String bassToken, int rootPc) {
        if (bassToken == null || bassToken.isBlank()) return null;
        // Try as note name first (dodeca, Anglo, solfège)
        Integer bpc = resolvePc(bassToken);
        if (bpc != null) return cap(DODECA[bpc]);
        // Try as integer interval (positive or negative)
        try {
            int v = Integer.parseInt(bassToken.trim());
            bpc = ((rootPc + v) % 12 + 12) % 12;
            return cap(DODECA[bpc]);
        } catch (NumberFormatException ignored) {}
        return null;
    }

    private static int bassIv(int rootPc, int bassPc) {
        int b = bassPc - rootPc - 12;
        if (b < -11) b += 12;
        return b;
    }

    private static int semToDeg(int s) {
        // Approximate semitone → diatonic degree
        int[] map = {1,1,2,3,3,4,5,5,6,6,7,7};
        return (s >= 0 && s < 12) ? map[s] : s;
    }

    private static int[] parseIntArr(String s) {
        String[] parts = s.split(",");
        int[] r = new int[parts.length];
        for (int i = 0; i < parts.length; i++) r[i] = Integer.parseInt(parts[i].trim());
        return r;
    }

    private static String joinInts(int[] a) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++) { if (i > 0) sb.append(','); sb.append(a[i]); }
        return sb.toString();
    }

    private static String cap(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CHORD OBJECT CONVERSION
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Convert a Chord object to any display format string.
     * Returns null for info-only or invalid chords.
     */
    public static String chordToFormat(Chord chord, String fmt) {
        if (chord == null || !chord.isValidChord()) return null;
        if (chord.getRoot() == Settings.USE_INFO_AS_SIMBOL) return null;
        int rootPc   = ((chord.getMidiRoot()) % 12 + 12) % 12;
        int[] ivs    = chord.getShape();
        if (ivs == null) return null;
        Integer bassPc = (chord.getBass() == Chord.NULL_BASS) ? null
                         : ((chord.getMidiBass() % 12 + 12) % 12);
        return convert(rootPc, ivs, bassPc, fmt);
    }

    /**
     * Returns the number of lines that correspond to base-chord notes (root line included),
     * i.e. the index at which tensions start in the list returned by chordToFormatLines.
     * Tensions are intervals > 11.
     */
    public static int baseLinesCount(Chord chord) {
        if (chord == null || !chord.isValidChord()
                || chord.getRoot() == Settings.USE_INFO_AS_SIMBOL) return 0;
        int[] ivs = chord.getShape();
        if (ivs == null) return 0;
        int base = 0;
        for (int iv : ivs) if (iv <= 11) base++;
        return 1 + base; // +1 for root line
    }

    /**
     * Returns the chord as a list of display lines for vertical rendering.
     * First element is the root name; remaining elements are notes/intervals.
     * Returns null for formats that don't produce a list (FORMAT_SIMBOL, FORMAT_NOM).
     */
    public static List<String> chordToFormatLines(Chord chord, String fmt) {
        if (chord == null || !chord.isValidChord()) return null;
        if (chord.getRoot() == Settings.USE_INFO_AS_SIMBOL) return null;
        int rootPc = ((chord.getMidiRoot()) % 12 + 12) % 12;
        int[] ivs  = chord.getShape();
        if (ivs == null) return null;
        Integer bassPc = (chord.getBass() == Chord.NULL_BASS) ? null
                         : ((chord.getMidiBass() % 12 + 12) % 12);
        return convertToLines(rootPc, ivs, bassPc, fmt);
    }

    private static List<String> convertToLines(int rootPc, int[] ivs, Integer bassPc, String fmt) {
        int[] baseIvs = filterBase(ivs);
        int[] extIvs  = filterExt(ivs);
        Template t = BY_INTERVALS.get(Arrays.toString(baseIvs));
        if (t == null) t = BY_INTERVALS.get(Arrays.toString(ivs));

        String rootDodeca = cap(DODECA[rootPc]);
        List<String> lines = new ArrayList<>();

        switch (fmt) {
            case FORMAT_MY: {
                int bdisp = (bassPc != null) ? bassIv(rootPc, bassPc) : 0;
                if (bdisp < 0) bdisp += 12;
                lines.add(rootDodeca + (bassPc != null ? "/" + bdisp : ""));
                for (int iv : ivs) lines.add(String.valueOf(iv > 11 ? iv - 12 : iv));
                return lines;
            }
            case FORMAT_DODECA: {
                lines.add(rootDodeca + (bassPc != null ? "/" + DODECA[bassPc] : ""));
                for (int iv : ivs) lines.add(DODECA[((rootPc + iv) % 12 + 12) % 12]);
                return lines;
            }
            case FORMAT_POSICIONS: {
                if (t == null) return null;
                String bassStr = (bassPc != null)
                        ? "/" + semToDeg(((bassPc - rootPc) + 12) % 12) : "";
                lines.add(cap(SOLFEGE_FLAT[rootPc]) + bassStr);
                String inner = t.posicions.substring(1, t.posicions.length() - 1);
                for (String p : inner.split(",")) lines.add(p.trim());
                for (int ev : extIvs)
                    for (int i = 0; i < TENSION_IVLS.length; i++)
                        if (TENSION_IVLS[i] == ev) { lines.add(TENSION_SUFFIX[i]); break; }
                return lines;
            }
            case FORMAT_SOLFEIG: {
                lines.add(cap(SOLFEGE_FLAT[rootPc]) + (bassPc != null ? "/" + SOLFEGE_FLAT[bassPc] : ""));
                lines.addAll(buildNoteList(rootPc, ivs, t, SOLFEGE_FLAT, SOLFEGE_SHARP));
                return lines;
            }
            case FORMAT_NOTES: {
                lines.add(ANGLO_ROOT[rootPc] + (bassPc != null ? "/" + ANGLO_ROOT[bassPc] : ""));
                lines.addAll(buildNoteList(rootPc, ivs, t, ANGLO_FLAT, ANGLO_SHARP));
                return lines;
            }
            case FORMAT_MIDI: {
                int midiRoot = rootPc + 60;
                lines.add(cap(DODECA[rootPc]) + (bassPc != null ? "/" + (bassPc + 60 - 12) : ""));
                for (int iv : ivs) lines.add(String.valueOf(midiRoot + iv));
                return lines;
            }
            default:
                return null;
        }
    }

    /**
     * Auto-detect format and convert any chord string to user's format.
     * Falls back to oldChord root (or "do") when format has no root.
     *
     * @param input    user-typed chord string
     * @param oldChord previous chord (may be null), used to infer root for rootless formats
     * @return chord in user's format (e.g. "So[0,4,7]"), or null if unrecognised
     */
    public static String detectAndConvert(String input, Chord oldChord) {
        if (input == null || input.isBlank()) return null;
        String s = input.trim();

        // ── 0. Root-only: bare root name → major if uppercase, minor if lowercase ─
        if (!s.contains("[") && !s.contains(" ") && !s.contains("(")) {
            String sLow = s.toLowerCase();
            Integer pc = null;
            // Try dodeca root name (2-3 chars: "so", "ti", "de", ...)
            for (int i = 0; i < DODECA.length; i++) {
                if (DODECA[i].equals(sLow)) { pc = i; break; }
            }
            // Try solfège root name (e.g. "sol", "si", "reb", "mib")
            if (pc == null) {
                String sd = solfeigToDodeca(sLow);
                if (sd != null) pc = ROOT_TO_PC.get(sd);
            }
            // Try Anglo root name exactly: 1-2 chars matching [A-G][b#]?
            if (pc == null && s.matches("(?i)[A-G][b#]?")) {
                String sNorm = Character.toUpperCase(s.charAt(0)) + s.substring(1);
                String rootTok = (sNorm.length() >= 2 && ROOT_TO_PC.containsKey(sNorm.substring(0, 2)))
                        ? sNorm.substring(0, 2) : sNorm.substring(0, 1);
                pc = ROOT_TO_PC.get(rootTok);
            }
            if (pc != null) {
                boolean isMajor = Character.isUpperCase(s.charAt(0));
                return cap(DODECA[pc]) + (isMajor ? "[0,4,7]" : "[0,3,7]");
            }
        }

        // ── 1. Root[...] – my format or Root+note-list ──────────────────────
        int lb = s.indexOf('[');
        if (lb > 0) {
            String rootTok = s.substring(0, lb).trim();
            int sl2 = rootTok.indexOf('/');
            String candidate = (sl2 > 0) ? rootTok.substring(0, sl2).trim() : rootTok;
            Integer cPc = ROOT_TO_PC.get(candidate);
            if (cPc == null) cPc = ROOT_TO_PC.get(candidate.toLowerCase());
            if (cPc == null) cPc = ROOT_TO_PC.get(candidate.toUpperCase());
            if (cPc == null) { // solfège root (e.g. "Sol", "Mib")
                String sd = solfeigToDodeca(candidate.toLowerCase());
                if (sd != null) cPc = ROOT_TO_PC.get(sd);
            }
            if (cPc != null) {
                int rb = s.lastIndexOf(']');
                if (rb > lb) {
                    String inside = s.substring(lb + 1, rb);
                    if (inside.matches("[-0-9, ;]+")) {
                        int sc0 = inside.indexOf(';');
                        // Normalize root to canonical dodeca form (e.g. "c" → "Do")
                        String rootNorm = cap(DODECA[cPc]);
                        if (sc0 < 0) {
                            try {
                                int[] ivs1 = parseIntArr(inside);
                                String res1 = rootNorm + "[" + joinInts(canonicalizeIntervals(ivs1)) + "]";
                                // Check for /bass after ']'
                                if (rb < s.length() - 1) {
                                    String after = s.substring(rb + 1).trim();
                                    if (after.startsWith("/")) {
                                        String bt = after.substring(1).trim();
                                        String bName = bassTokenToDodeca(bt, cPc);
                                        if (bName != null) res1 += "/" + bName;
                                    }
                                }
                                return res1;
                            } catch (NumberFormatException e) {
                                return rootNorm + s.substring(lb);
                            }
                        }
                        // Normalize Do[-5;0,4,7] or Do[2;0,4,7] → Do[0,4,7]/Bass
                        try {
                            String bassStr0 = inside.substring(0, sc0).trim();
                            String ivsOnly  = inside.substring(sc0 + 1).trim();
                            int[] ivs0 = parseIntArr(ivsOnly);
                            String bName0 = bassTokenToDodeca(bassStr0, cPc);
                            if (bName0 == null) return null;
                            return rootNorm + "[" + joinInts(canonicalizeIntervals(ivs0)) + "]/" + bName0;
                        } catch (NumberFormatException ignored) { return null; }
                    }
                    // Root[note_list] with note names (e.g. C[D;C,E,G] or So[re;do,mi,so])
                    String rootDodecaC = DODECA[cPc];
                    String bracketStr = "[" + inside + "]";
                    int sc3 = inside.indexOf(';');
                    String payload3 = (sc3 >= 0) ? inside.substring(sc3 + 1).trim() : inside;
                    if (!payload3.isEmpty()) {
                        String firstTok3 = payload3.split(",")[0].trim();
                        String payLow3 = payload3.toLowerCase();
                        String convR;
                        if (firstTok3.matches("(?i)[A-G][b#]?")) {
                            convR = fromFormat(bracketStr, rootDodecaC, FORMAT_NOTES);
                        } else if (payLow3.matches(".*\\b(so|sa|li|ti|fo|de|ri)\\b.*")) {
                            convR = fromFormat(bracketStr, rootDodecaC, FORMAT_DODECA);
                        } else if (payLow3.matches(".*\\b(sol|sib|mib|reb|lab|solb|si)\\b.*")) {
                            convR = fromFormat(bracketStr, rootDodecaC, FORMAT_SOLFEIG);
                        } else {
                            convR = fromFormat(bracketStr, rootDodecaC, FORMAT_DODECA);
                        }
                        if (convR != null) return convR;
                    }
                }
            }
        }

        // ── 2. List format: starts with [ ───────────────────────────────────
        if (s.startsWith("[")) {
            int rb = s.lastIndexOf(']');
            if (rb < 0) return null;
            String inside = s.substring(1, rb).trim();
            // Strip optional bass (before ';')
            int sc = inside.indexOf(';');
            String payload = (sc >= 0) ? inside.substring(sc + 1).trim() : inside;
            if (payload.isEmpty()) return null;

            String firstTok = payload.split(",")[0].trim();

            // 2a. Intervals or diatonic positions: all tokens are integers
            if (inside.matches("[-0-9, ;]+")) {
                String root = inferRoot(oldChord);
                // If all chord-tone values are 1-7 (no 0, no negatives), try diatonic positions
                String[] toks2a = payload.split(",");
                boolean allDiatonic = true;
                for (String tok : toks2a) {
                    String t2 = tok.trim();
                    if (t2.isEmpty()) { allDiatonic = false; break; }
                    try {
                        int v = Integer.parseInt(t2);
                        if (v < 1 || v > 7) { allDiatonic = false; break; }
                    } catch (NumberFormatException e) { allDiatonic = false; break; }
                }
                if (allDiatonic) {
                    String res = fromFormat(s, root, FORMAT_POSICIONS);
                    if (res != null) return res;
                }
                return fromFormat(s, root, FORMAT_INTERVALS);
            }
            // 2b. Posicions: tokens match b?\\d+ pattern
            if (payload.matches("([b#]?\\d+,?\\s*)+")) {
                String root = inferRoot(oldChord);
                return fromFormat(s, root, FORMAT_POSICIONS);
            }
            // 2c. Notes anglosaxo: first token is Anglo (single uppercase letter ± b/#)
            if (firstTok.matches("(?i)[A-G][b#]?")) {
                String root = ROOT_TO_PC.containsKey(firstTok) ? firstTok : firstTok;
                String rootDodeca = toDodecaRoot(firstTok);
                return fromFormat(s, rootDodeca, FORMAT_NOTES);
            }
            // 2d. DodecaNotes vs Solfeig: check unique tokens
            String payloadLow = payload.toLowerCase();
            if (payloadLow.matches(".*\\b(so|sa|li|ti|fo|de|ri)\\b.*")) {
                // Unique dodeca tokens
                String firstLow = firstTok.toLowerCase();
                String rootDodeca = ROOT_TO_PC.containsKey(firstLow) ? firstLow : inferRoot(oldChord);
                return fromFormat(s, rootDodeca, FORMAT_DODECA);
            }
            if (payloadLow.matches(".*\\b(sol|sib|mib|reb|lab|solb|si)\\b.*")) {
                String rootDodeca = solfeigToDodeca(firstTok);
                if (rootDodeca == null) rootDodeca = inferRoot(oldChord);
                return fromFormat(s, rootDodeca, FORMAT_SOLFEIG);
            }
            // Ambiguous (only shared tokens like do/re/mi/fa/la): try DodecaNotes
            String firstLow = firstTok.toLowerCase();
            String rootDodeca = ROOT_TO_PC.containsKey(firstLow) ? firstLow : inferRoot(oldChord);
            return fromFormat(s, rootDodeca, FORMAT_DODECA);
        }

        // ── 2.5. Dodeca/Solfège symbol: Root(dodeca/solfège) + chord suffix ────
        {
            String[] ds = stripDodecaSolfeigRoot(s);
            if (ds != null) {
                String rootDodeca = ds[0];
                String suffix     = ds[1];
                // Split bass ("/note") from suffix if present
                String bassToken = null;
                int slashPos = suffix.indexOf('/');
                if (slashPos >= 0) {
                    bassToken = suffix.substring(slashPos + 1).trim();
                    suffix    = suffix.substring(0, slashPos).trim();
                }
                int[] ivs = resolveSuffixToIntervals(suffix);
                if (ivs != null) {
                    Integer pc = ROOT_TO_PC.get(rootDodeca);
                    if (pc != null) {
                        String result = cap(DODECA[pc]) + "[" + joinInts(ivs) + "]";
                        if (bassToken != null) {
                            String bName = bassTokenToDodeca(bassToken, pc);
                            if (bName != null) result += "/" + bName;
                        }
                        return result;
                    }
                }
            }
        }

        // ── 3. Simbol / Sinònim: starts with Anglo root A-G (case-insensitive) ─
        if (s.matches("(?i)[A-G][b#]?.*")) {
            // Normalize: capitalize root letter so ROOT_TO_PC and stripAngloRoot work
            String sNorm = Character.toUpperCase(s.charAt(0)) + s.substring(1);
            String rootAnglo = (sNorm.length() >= 2 && ROOT_TO_PC.containsKey(sNorm.substring(0, 2)))
                    ? sNorm.substring(0, 2) : sNorm.substring(0, 1);
            String rootDodeca = toDodecaRoot(rootAnglo);
            return fromFormat(sNorm, rootDodeca, FORMAT_SIMBOL);
        }

        return null; // unrecognised
    }

    private static String inferRoot(Chord oldChord) {
        if (oldChord != null && oldChord.isValidChord()
                && oldChord.getRoot() != Settings.USE_INFO_AS_SIMBOL) {
            int pc = ((oldChord.getMidiRoot()) % 12 + 12) % 12;
            return DODECA[pc];
        }
        return "do";
    }

    private static String toDodecaRoot(String angloRoot) {
        Integer pc = ROOT_TO_PC.get(angloRoot);
        if (pc == null) pc = ROOT_TO_PC.get(angloRoot.toUpperCase());
        if (pc == null) return "do";
        return DODECA[pc];
    }

    private static String solfeigToDodeca(String sf) {
        String sfl = sf.toLowerCase();
        String[] sfFlat  = {"do","reb","re","mib","mi","fa","solb","sol","lab","la","sib","si"};
        String[] sfSharp = {"do#","re#","fa#","sol#","la#"};
        int[]    sfSpc   = {1, 3, 6, 8, 10};
        for (int i = 0; i < 12; i++) if (sfFlat[i].equals(sfl)) return DODECA[i];
        for (int i = 0; i < sfSharp.length; i++) if (sfSharp[i].equals(sfl)) return DODECA[sfSpc[i]];
        return null;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  TEST / VALIDATION
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Print all chords for a given root in all display formats.
     * Used for validation.
     */
    public static String generateRootReport(String root) {
        Integer rootPc = resolvePc(root);
        if (rootPc == null) return "Root not found: " + root;
        StringBuilder sb = new StringBuilder();
        sb.append("=== Acords de ").append(cap(root)).append(" ===\n\n");
        sb.append(String.format("%-22s %-10s %-16s %-24s %-24s %-24s %-14s%n",
            FORMAT_MY, FORMAT_SIMBOL, FORMAT_POSICIONS,
            FORMAT_NOTES, FORMAT_SOLFEIG, FORMAT_DODECA, FORMAT_NOM));
        sb.append("-".repeat(140)).append("\n");
        for (Template t : TEMPLATES) {
            String my   = convert(rootPc, t.intervals, null, FORMAT_MY);
            String sym  = convert(rootPc, t.intervals, null, FORMAT_SIMBOL);
            String pos  = convert(rootPc, t.intervals, null, FORMAT_POSICIONS);
            String notes= convert(rootPc, t.intervals, null, FORMAT_NOTES);
            String solf = convert(rootPc, t.intervals, null, FORMAT_SOLFEIG);
            String ddca = convert(rootPc, t.intervals, null, FORMAT_DODECA);
            String nom  = convert(rootPc, t.intervals, null, FORMAT_NOM);
            sb.append(String.format("%-22s %-10s %-16s %-24s %-24s %-24s %-14s%n",
                my, sym, pos, notes, solf, ddca, nom));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(generateRootReport("so"));
        // Quick round-trip test
        System.out.println("\n=== Round-trip tests (So) ===");
        String[] tests = {"So[0,4,7]","So[0,3,7]","So[0,4,7,10]","So[0,4,7,11]","So[0,3,6,10]","So[0,4,7,10,14]"};
        for (String chord : tests) {
            String sym = toFormat(chord, FORMAT_SIMBOL);
            String back = fromFormat(sym, "so", FORMAT_SIMBOL);
            System.out.printf("%-22s → %-10s → %-22s  %s%n", chord, sym, back, chord.equals(back) ? "✓" : "✗ (expected "+chord+")");
        }
    }
}
