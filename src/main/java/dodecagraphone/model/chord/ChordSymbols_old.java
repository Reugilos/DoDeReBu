package dodecagraphone.model.chord;

import dodecagraphone.ui.Settings;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.*;

public class ChordSymbols_old {

    // ===== Config =====
    private static final String DEFAULT_CSV_NAME = "ChordSymbols.csv";
    private static final String SYS_PROP_CSV = "dodecagraphone.chords.csv";
    private static final String ENV_VAR_CSV  = "DODECAGRAPHONE_CHORDS_CSV";
    private static final String ALL_SYMBOLS_OUT = "AllSymbols.csv";

    // ===== Taules carregades del CSV =====
    private static final Map<String,int[]> QUALITY_TO_INTERVALS = new LinkedHashMap<>();
    private static final Map<String,String> QUALITY_ALIAS = new HashMap<>();
    private static final Map<String,String> QUALITY_TO_POSICIONS = new HashMap<>();

    // ===== Notes / noms =====
    private static final Map<String,Integer> NOTE_PC = new HashMap<>();
    private static final Map<Integer,String> PC_TO_CUSTOM = new HashMap<>();
    private static final Map<String,Integer> CUSTOM_TO_PC = new HashMap<>();
    private static final String[] PC_TO_LETTER_SHARP = {
        "C","C#","D","D#","E","F","F#","G","G#","A","A#","B"
    };
    private static final String[] PC_TO_LETTER_FLAT = {
        "C","Db","D","Eb","E","F","Gb","G","Ab","A","Bb","B"
    };
    private static final String[] PC_TO_LETTER_FOR_ALTERACIONS = PC_TO_LETTER_SHARP;

    private static final String[] CHARSET_TRY_ORDER = new String[] {
        "UTF-8","UTF-16LE","UTF-16BE","windows-1252","ISO-8859-1"
    };

    // Seccions reconegudes (normalitzades sense accents, minúscules)
    private static final Set<String> SECTION_KEYS = new HashSet<>(Arrays.asList(
        "triades",
        "quatriadesdiatoniques", // admetrem variants d'accentuació
        "quatríadesdiatòniques",  // per seguretat (ja es normalitza igualment)
        "altresquatriades",
        "baix",
        "tensions"
    ));

    static {
        // mapes de notes (lletres amb #/b)
        String[] names = {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B","Db","Eb","Gb","Ab","Bb"};
        int[] pcs      = {  0,   1,   2,   3,   4,   5,   6,   7,   8,   9,   10,  11,   1,   3,   6,   8,  10 };
        for (int i=0;i<names.length;i++) NOTE_PC.put(names[i], pcs[i]);

        // dodeca-noms
        PC_TO_CUSTOM.put(0,"do"); PC_TO_CUSTOM.put(1,"de"); PC_TO_CUSTOM.put(2,"re"); PC_TO_CUSTOM.put(3,"ri");
        PC_TO_CUSTOM.put(4,"mi"); PC_TO_CUSTOM.put(5,"fa"); PC_TO_CUSTOM.put(6,"fo"); PC_TO_CUSTOM.put(7,"so");
        PC_TO_CUSTOM.put(8,"sa"); PC_TO_CUSTOM.put(9,"la"); PC_TO_CUSTOM.put(10,"li"); PC_TO_CUSTOM.put(11,"ti");
        for (Map.Entry<Integer,String> e: PC_TO_CUSTOM.entrySet()) CUSTOM_TO_PC.put(e.getValue(), e.getKey());

        loadCsvRuntime();
    }

    /* ===================== API de validació ===================== */
    public static boolean isSymbolic(String text) {
        String in = norm(text);
        if (looksIntervalic(in) || looksNoteNames(in) || looksPosicio(in) || looksAlteracions(in)) return false;
        try { parseSymbolicToIntervals(in); return true; }
        catch (RuntimeException ex) { throw new IllegalArgumentException("El text " + text + " no es correspon a cap acord vàlid"); }
    }
    public static boolean isIntervalic(String text) {
        String in = norm(text);
        if (looksIntervalic(in)) return true;
        if (looksNoteNames(in) || looksPosicio(in) || looksAlteracions(in) || looksPossiblySymbolic(in)) return false;
        throw new IllegalArgumentException("El text " + text + " no es correspon a cap acord vàlid");
    }
    public static boolean isNoteNames(String text) {
        String in = norm(text);
        if (looksNoteNames(in)) return true;
        if (looksIntervalic(in) || looksPosicio(in) || looksAlteracions(in) || looksPossiblySymbolic(in)) return false;
        throw new IllegalArgumentException("El text " + text + " no es correspon a cap acord vàlid");
    }
    public static boolean isAlteracions(String text) {
        String in = norm(text);
        if (looksAlteracions(in)) return true;
        if (looksIntervalic(in) || looksPosicio(in) || looksNoteNames(in) || looksPossiblySymbolic(in)) return false;
        throw new IllegalArgumentException("El text " + text + " no es correspon a cap acord vàlid");
    }
    public static boolean isPosicio(String text) {
        String in = norm(text);
        in = stripOuterQuotes(in);
        if (looksPosicio(in)) return true;
        if (looksIntervalic(in) || looksNoteNames(in) || looksAlteracions(in) || looksPossiblySymbolic(in)) return false;
        throw new IllegalArgumentException("El text " + text + " no es correspon a cap acord vàlid");
    }
    public static boolean isPoscio(String text) { return isPosicio(text); }

    /* ===================== API principal ===================== */
    public static String toIntervalNotation(String symbol) {
        ParsedSymbol ps = parseSymbolicToIntervals(norm(symbol));
        String rootOut = capitalize(PC_TO_CUSTOM.get(ps.rootPc));
        return rootOut + formatIntervals(ps.intervals) + (ps.relBass!=null? "/"+ps.relBass : "");
    }
    public static String fromIntervalNotation(String intervalStr) {
        String in = norm(intervalStr);
        int slashIdx = in.indexOf('/');
        Integer relBass = null;
        if (slashIdx >= 0) {
            String after = in.substring(slashIdx+1).trim();
            in = in.substring(0, slashIdx).trim();
            try { relBass = Integer.parseInt(after); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Després del slash s'espera un enter: " + after); }
        }
        int lb = in.indexOf('['), rb = in.indexOf(']');
        if (lb<0 || rb<0 || rb<lb) throw new IllegalArgumentException("Format d'intervals invàlid: " + intervalStr);
        String rootWord = in.substring(0, lb).trim();
        String inside = in.substring(lb+1, rb).trim();
        int rootPc = parseRootPcFromCustomOrLetter(rootWord);
        int[] ivals = parseIntList(inside);
        String quality = matchQuality(ivals);
        if (quality == null) throw new IllegalArgumentException("Els intervals no corresponen a cap qualitat permesa");
        String out = PC_TO_LETTER_SHARP[rootPc] + quality;
        if (relBass != null) {
            int bassPc = ((rootPc + relBass) % 12 + 12) % 12;
            out += "/" + PC_TO_LETTER_SHARP[bassPc];
        }
        return out;
    }
    public static String toNoteNames(String text) {
        ParsedAny pa = parseAny(text);
        TreeSet<Integer> uniq = new TreeSet<>();
        for (int iv : pa.intervals) uniq.add(((iv%12)+12)%12);
        ArrayList<String> rest = new ArrayList<>();
        for (int pc : uniq) rest.add(PC_TO_CUSTOM.get(pc));
        if (pa.relBass != null) {
            int bassPc = ((pa.rootPc + pa.relBass) % 12 + 12) % 12;
            return "[" + PC_TO_CUSTOM.get(bassPc) + "; " + String.join(", ", rest) + "]";
        } else {
            return "[" + String.join(", ", rest) + "]";
        }
    }
    public static String toAlteracions(String text) {
        ParsedAny pa = parseAny(text);
        TreeSet<Integer> uniq = new TreeSet<>();
        for (int iv : pa.intervals) uniq.add(((iv%12)+12)%12);
        ArrayList<String> rest = new ArrayList<>();
        for (int pc : uniq) rest.add(PC_TO_LETTER_FOR_ALTERACIONS[pc]);
        if (pa.relBass != null) {
            int bassPc = ((pa.rootPc + pa.relBass) % 12 + 12) % 12;
            return "[" + PC_TO_LETTER_FOR_ALTERACIONS[bassPc] + "; " + String.join(", ", rest) + "]";
        } else {
            return "[" + String.join(", ", rest) + "]";
        }
    }
    public static String fromPosicio(String list) {
        String in = norm(list);
        in = stripOuterQuotes(in);
        if (!looksPosicio(in)) throw new IllegalArgumentException("El text " + list + " no es correspon a cap acord vàlid");
        int[] ivals = parseDegreeList(in);
        return "Do" + formatIntervals(ivals);
    }

    /* =============== Generador AllSymbols.csv (mateix ordre i seccions del CSV) =============== */
    public static void generaChordSymbolTable() {
        if (Settings.IS_BU) return;
        try (BufferedWriter w = Files.newBufferedWriter(Paths.get(ALL_SYMBOLS_OUT), StandardCharsets.UTF_8)) {
            w.write("Simbol;Sinonims;Posicions;Notes;Intervals;DodecaNoms\n");

            List<String> lines = resolveCsvLines();

            int lineNo = 0;
            int idxSimbol = -1, idxPosicions = -1, idxSinonims = -1;

            // Header
            for (; lineNo < lines.size(); lineNo++) {
                String headerRaw = stripBOM(lines.get(lineNo));
                String header = headerRaw.trim();
                if (header.isEmpty() || header.startsWith("#")) continue;
                List<String> cols = splitCsvRespectBrackets(header);
                int[] idx = resolveHeaderIndexes(headerRaw, cols, false);
                idxSimbol   = idx[0];
                idxPosicions= idx[1];
                idxSinonims = idx[2];
                lineNo++;
                break;
            }

            // Dades (exactament el mateix ordre)
            for (; lineNo < lines.size(); lineNo++) {
                String raw = lines.get(lineNo);
                if (raw == null) continue;
                String line = stripBOM(raw).trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                List<String> cols = splitCsvRespectBrackets(line);
                if (cols.isEmpty()) continue;

                String simbol   = take(cols, idxSimbol);
                String posicions= take(cols, idxPosicions);
                String sinonims = take(cols, idxSinonims);

                if (simbol == null || simbol.trim().isEmpty()) continue;

                String sClean = simbol.replaceAll("\\s+","");
                // --- NOVETAT: detecta secció encara que comenci amb A..G (p.ex. "Baix")
                if (isSectionName(simbol)) {
                    w.write(csv(simbol.trim())); w.write(";;;;;\n");
                    continue;
                }

                String root = parseRootToken(sClean);
                if (root == null) {
                    // per si queda alguna línia-cas especial sense arrel
                    w.write(csv(simbol.trim())); w.write(";;;;;\n");
                    continue;
                }

                // Derivats
                String notesAlt, dodeca, intervalsStr;
                try {
                    if (!isSymbolic(sClean)) continue;
                    String intervalic = toIntervalNotation(sClean);
                    int lb = intervalic.indexOf('['), rb = intervalic.indexOf(']');
                    intervalsStr = (lb>=0 && rb>lb) ? intervalic.substring(lb, rb+1) : "";
                    notesAlt = toAlteracions(sClean);
                    dodeca   = toNoteNames(sClean);
                } catch (Exception ex) { continue; }

                String posOut = (posicions == null ? "" : posicions.trim());
                if (posOut.isBlank()) {
                    String qualCanon = normalizeQualityFromCsv(sClean.substring(root.length()));
                    String fromQual = QUALITY_TO_POSICIONS.get(qualCanon);
                    if (fromQual != null) posOut = fromQual;
                }

                w.write(csv(sClean)); w.write(';');
                w.write(csv(sinonims == null ? "" : sinonims)); w.write(';');
                w.write(csv(posOut)); w.write(';');
                w.write(csv(notesAlt)); w.write(';');
                w.write(csv(intervalsStr)); w.write(';');
                w.write(csv(dodeca)); w.write('\n');
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error generant AllSymbols.csv: " + ex.getMessage(), ex);
        }
    }

    private static boolean isSectionName(String text) {
        if (text == null) return false;
        String key = normalizeAscii(text).trim();
        // elimina espais
        key = key.replaceAll("\\s+","");
        // compara amb claus admeses
        if (SECTION_KEYS.contains(key)) return true;
        // també admet variants freqüents (sense accent diacrític o errades d'exportació)
        if (key.startsWith("cuatr") || key.startsWith("quatr") || key.startsWith("quadrid")) {
            // “Cuatriades/Quatriades ... diatoniques”
            if (key.contains("diaton")) return true;
        }
        return false;
    }

    private static String csv(String s) {
        if (s == null) return "";
        boolean needQuotes = s.indexOf(';')>=0 || s.indexOf('"')>=0 || s.indexOf('\n')>=0 || s.indexOf('\r')>=0;
        String t = s.replace("\"","\"\"");
        return needQuotes ? ("\""+t+"\"") : t;
    }

    /* ===================== Parsers/Helpers ===================== */
    private static class ParsedSymbol { int rootPc; int[] intervals; Integer relBass; }
    private static class ParsedAny { int rootPc; int[] intervals; Integer relBass; }

    private static ParsedAny parseAny(String text) {
        String in = norm(text);
        int rootPc; int[] intervals; Integer relBass = null;

        if (looksIntervalic(in)) {
            int lb = in.indexOf('['), rb = in.indexOf(']');
            String rootWord = in.substring(0, lb).trim();
            rootPc = parseRootPcFromCustomOrLetter(rootWord);
            String inside = in.substring(lb+1, rb).trim();
            intervals = parseIntList(inside);
            int slashIdx = in.indexOf('/');
            if (slashIdx >= 0) relBass = Integer.parseInt(in.substring(slashIdx+1).trim());

        } else if (looksNoteNames(in)) {
            rootPc = 0;
            int[] parsed = parseNoteListToPCs(in);
            intervals = parsed;
            Integer bassPc = extractHeadBassFromNoteNames(in);
            if (bassPc != null) relBass = minimizeSemitoneDistance(bassPc - rootPc);

        } else if (looksAlteracions(in)) {
            rootPc = 0;
            int[] parsed = parseAlteracionsListToPCs(in);
            intervals = parsed;
            Integer bassPc = extractHeadBassFromAlteracions(in);
            if (bassPc != null) relBass = minimizeSemitoneDistance(bassPc - rootPc);

        } else if (looksPosicio(in)) {
            intervals = parseDegreeList(in);
            rootPc = 0;

        } else {
            ParsedSymbol ps = parseSymbolicToIntervals(in);
            rootPc = ps.rootPc; intervals = ps.intervals; relBass = ps.relBass;
        }
        ParsedAny pa = new ParsedAny();
        pa.rootPc = rootPc; pa.intervals = intervals; pa.relBass = relBass;
        return pa;
    }

    private static ParsedSymbol parseSymbolicToIntervals(String symbol) {
        String s = symbol.trim();
        String slashPart = null;
        int slashIdx = s.indexOf('/');
        if (slashIdx >= 0) { slashPart = s.substring(slashIdx+1).trim(); s = s.substring(0, slashIdx).trim(); }
        String rootToken = parseRootToken(s);
        if (rootToken == null) throw new IllegalArgumentException("Arrel d'acord no reconeguda: " + symbol);
        int rootPc = NOTE_PC.get(rootToken);
        String qualRawCsv = s.substring(rootToken.length());
        String qual = normalizeQualityFromCsv(qualRawCsv);
        int[] ivals = QUALITY_TO_INTERVALS.get(qual);
        if (ivals == null) throw new IllegalArgumentException("Qualitat no permesa segons el CSV: " + qualRawCsv);
        Integer relBass = null;
        if (slashPart != null && !slashPart.isEmpty()) {
            Integer bassPc = NOTE_PC.get(slashPart);
            if (bassPc == null) throw new IllegalArgumentException("Nota després del slash no reconeguda: " + slashPart);
            relBass = minimizeSemitoneDistance(bassPc - rootPc);
        }
        ParsedSymbol ps = new ParsedSymbol();
        ps.rootPc = rootPc; ps.intervals = uniqSorted(ivals); ps.relBass = relBass;
        return ps;
    }

    private static String normalizeQualityFromCsv(String qualRaw) {
        if (qualRaw == null || qualRaw.isEmpty()) return "";
        String q = qualRaw.replaceAll("\\s+", "");
        // Triangle: 'A' (ASCII) -> Δ variants (mapeig a maj7/maj9/...)
        if (q.startsWith("A")) {
            String tail = q.substring(1);
            if (tail.isEmpty() || "7".equals(tail))          q = "maj7";
            else if ("9".equals(tail))                       q = "maj9";
            else if ("11".equals(tail))                      q = "maj11";
            else if ("13".equals(tail))                      q = "maj13";
            else q = q.replaceFirst("^A", "Δ");
        } else if (q.contains("A")) {
            q = q.replace("A", "Δ");
        }
        if ("o".equals(q) && QUALITY_TO_INTERVALS.containsKey("dim")) return "dim";
        if ("ø".equals(q) && QUALITY_TO_INTERVALS.containsKey("m7b5")) return "m7b5";
        String viaAlias = QUALITY_ALIAS.get(q);
        return (viaAlias != null) ? viaAlias : q;
    }

    private static String parseRootToken(String s) {
        if (s.isEmpty()) return null;
        if (s.length()>=2) {
            String two = s.substring(0,2);
            if (NOTE_PC.containsKey(two)) return two;
        }
        String one = s.substring(0,1);
        if (NOTE_PC.containsKey(one)) return one;
        return null;
    }

    private static String norm(String s) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException("El text " + s + " no es correspon a cap acord vàlid");
        return s.trim();
    }
    private static boolean looksPossiblySymbolic(String s) {
        char c = Character.toUpperCase(s.charAt(0));
        return "ABCDEFG".indexOf(c) >= 0 && !(s.indexOf('[') >= 0 || s.indexOf(']') >= 0);
    }
    private static boolean looksIntervalic(String in) {
        int lb = in.indexOf('['), rb = in.indexOf(']');
        if (lb<0 || rb<0 || rb<lb) return false;
        String rootWord = in.substring(0, lb).trim();
        if (!isKnownRootWord(rootWord)) return false;
        String inside = in.substring(lb+1, rb).trim();
        if (!inside.isEmpty()) return inside.matches(".*\\d.*");
        if (rb+1 < in.length()) {
            String rest = in.substring(rb+1).trim();
            if (!rest.isEmpty()) {
                if (!rest.startsWith("/")) return false;
                try { Integer.parseInt(rest.substring(1).trim()); } catch (NumberFormatException e) { return false; }
            }
        }
        return true;
    }
    private static boolean looksNoteNames(String in) {
        if (!(in.startsWith("[") && in.endsWith("]"))) return false;
        String inside = in.substring(1, in.length()-1).trim();
        if (inside.isEmpty()) return false;
        int sc = inside.indexOf(';');
        if (sc >= 0) {
            String bass = inside.substring(0, sc).trim().toLowerCase();
            if (!CUSTOM_TO_PC.containsKey(bass)) return false;
            String rest = inside.substring(sc+1).trim();
            if (rest.isEmpty()) return false;
            for (String t : rest.split(",")) {
                String nm = t.trim().toLowerCase();
                if (nm.isEmpty() || !CUSTOM_TO_PC.containsKey(nm)) return false;
            }
            return true;
        } else {
            for (String t : inside.split(",")) {
                String nm = t.trim().toLowerCase();
                if (nm.isEmpty() || !CUSTOM_TO_PC.containsKey(nm)) return false;
            }
            return true;
        }
    }
    private static boolean looksAlteracions(String in) {
        if (!(in.startsWith("[") && in.endsWith("]"))) return false;
        String inside = in.substring(1, in.length()-1).trim();
        if (inside.isEmpty()) return false;
        int sc = inside.indexOf(';');
        if (sc >= 0) {
            String bass = inside.substring(0, sc).trim();
            if (!isLetterToken(bass)) return false;
            String rest = inside.substring(sc+1).trim();
            if (rest.isEmpty()) return false;
            for (String t : rest.split(",")) {
                String nm = t.trim();
                if (nm.isEmpty() || !isLetterToken(nm)) return false;
            }
            return true;
        } else {
            for (String t : inside.split(",")) {
                String nm = t.trim();
                if (nm.isEmpty() || !isLetterToken(nm)) return false;
            }
            return true;
        }
    }
    private static boolean isLetterToken(String s) {
        return s.matches("(?i)^[A-G](#|b)?$");
    }
    private static boolean looksPosicio(String in) {
        in = stripOuterQuotes(in);
        if (!(in.startsWith("[") && in.endsWith("]"))) return false;
        String inside = in.substring(1, in.length()-1).trim();
        if (inside.isEmpty()) return false;
        return inside.matches(".*(\\d|b\\d|#\\d).*");
    }
    private static boolean isKnownRootWord(String rootWord) {
        if (rootWord.isEmpty()) return false;
        if (CUSTOM_TO_PC.containsKey(rootWord.toLowerCase())) return true;
        return NOTE_PC.containsKey(rootWord);
    }

    private static Integer extractHeadBassFromNoteNames(String in) {
        String inside = in.substring(1, in.length()-1).trim();
        int scIdx = inside.indexOf(';');
        if (scIdx < 0) return null;
        String head = inside.substring(0, scIdx).trim().toLowerCase();
        return CUSTOM_TO_PC.get(head);
    }
    private static Integer extractHeadBassFromAlteracions(String in) {
        String inside = in.substring(1, in.length()-1).trim();
        int scIdx = inside.indexOf(';');
        if (scIdx < 0) return null;
        String head = inside.substring(0, scIdx).trim().toUpperCase();
        return NOTE_PC.get(head);
    }

    private static String normalizeNoteNames(String in) {
        String inside = in.substring(1, in.length()-1).trim();
        int scIdx = inside.indexOf(';');
        List<String> rest = new ArrayList<>();
        String head = null;
        if (scIdx >= 0) {
            head = inside.substring(0, scIdx).trim().toLowerCase();
            String restPart = inside.substring(scIdx+1).trim();
            for (String t : restPart.split(",")) {
                String nm = t.trim().toLowerCase();
                if (!nm.isEmpty()) rest.add(nm);
            }
        } else {
            for (String t : inside.split(",")) {
                String nm = t.trim().toLowerCase();
                if (!nm.isEmpty()) rest.add(nm);
            }
        }
        if (head != null) return "[" + head + "; " + String.join(", ", rest) + "]";
        return "[" + String.join(", ", rest) + "]";
    }
    private static String normalizeAlteracions(String in) {
        String inside = in.substring(1, in.length()-1).trim();
        int scIdx = inside.indexOf(';');
        List<String> rest = new ArrayList<>();
        String head = null;
        if (scIdx >= 0) {
            head = inside.substring(0, scIdx).trim().toUpperCase();
            String restPart = inside.substring(scIdx+1).trim();
            for (String t : restPart.split(",")) {
                String nm = t.trim().toUpperCase();
                if (!nm.isEmpty()) rest.add(nm);
            }
        } else {
            for (String t : inside.split(",")) {
                String nm = t.trim().toUpperCase();
                if (!nm.isEmpty()) rest.add(nm);
            }
        }
        if (head != null) return "[" + head + "; " + String.join(", ", rest) + "]";
        return "[" + String.join(", ", rest) + "]";
    }

    private static int[] parseNoteListToPCs(String in) {
        String inside = in.substring(1, in.length()-1).trim();
        int scIdx = inside.indexOf(';');
        String payload = (scIdx>=0)? inside.substring(scIdx+1).trim() : inside;
        TreeSet<Integer> set = new TreeSet<>();
        if (!payload.isEmpty()) {
            for (String t : payload.split(",")) {
                String nm = t.trim().toLowerCase();
                Integer pc = CUSTOM_TO_PC.get(nm);
                if (pc == null) throw new IllegalArgumentException("Nom de nota desconegut: " + nm);
                set.add(pc);
            }
        }
        int[] r = new int[set.size()]; int i=0; for (Integer v: set) r[i++]=v; return r;
    }
    private static int[] parseAlteracionsListToPCs(String in) {
        String inside = in.substring(1, in.length()-1).trim();
        int scIdx = inside.indexOf(';');
        String payload = (scIdx>=0)? inside.substring(scIdx+1).trim() : inside;
        TreeSet<Integer> set = new TreeSet<>();
        if (!payload.isEmpty()) {
            for (String t : payload.split(",")) {
                String nm = t.trim().toUpperCase();
                Integer pc = NOTE_PC.get(nm);
                if (pc == null) throw new IllegalArgumentException("Nota (Alteracions) desconeguda: " + nm);
                set.add(pc);
            }
        }
        int[] r = new int[set.size()]; int i=0; for (Integer v: set) r[i++]=v; return r;
    }

    // Helpers afegits per a "Posicions" -> intervals
    private static List<String> parsePositionsList(String txt) {
        String s = txt.trim();
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("“") && s.endsWith("”"))) {
            s = s.substring(1, s.length()-1).trim();
        }
        Matcher m = Pattern.compile("^\\[(.*)]$").matcher(s);
        if (!m.matches()) throw new IllegalArgumentException("posicions sense claudàtors: \"" + txt + "\"");
        String inside = m.group(1).trim();
        if (inside.isEmpty()) return Collections.emptyList();

        String rest = inside;
        int sc = inside.indexOf(';');
        if (sc >= 0) {
            String bass = inside.substring(0, sc).trim();
            if (!bass.isEmpty() && !bass.matches("(b|#)?\\d+")) {
                throw new IllegalArgumentException("Baix de posicions invàlid: " + bass);
            }
            rest = inside.substring(sc + 1).trim();
        }

        if (rest.isEmpty()) return Collections.emptyList();
        String[] parts = rest.split(",");
        ArrayList<String> out = new ArrayList<>();
        for (String p: parts) {
            String t = p.trim();
            if (!t.matches("(b|#)?\\d+")) throw new IllegalArgumentException("Grau invàlid: " + t);
            out.add(t);
        }
        return out;
    }
    private static int[] degreesToSemitones(List<String> degs) {
        TreeSet<Integer> set = new TreeSet<>();
        for (String t : degs) {
            Matcher m = Pattern.compile("(b|#)?(\\d+)").matcher(t);
            if (!m.matches()) throw new IllegalArgumentException("Grau invàlid: " + t);
            String acc = m.group(1); int n = Integer.parseInt(m.group(2));
            Map<Integer,Integer> base = new HashMap<>();
            base.put(1,0); base.put(2,2); base.put(3,4); base.put(4,5); base.put(5,7); base.put(6,9); base.put(7,11);
            base.put(9,14); base.put(11,17); base.put(13,21);
            Integer semis = base.get(n);
            if (semis == null) throw new IllegalArgumentException("Grau no suportat: " + n);
            if ("b".equals(acc)) semis -= 1; else if ("#".equals(acc)) semis += 1;
            set.add(semis);
        }
        int[] r = new int[set.size()]; int i=0; for (Integer v: set) r[i++]=v; return r;
    }

    private static int[] parseDegreeList(String in) {
        in = stripOuterQuotes(in);
        String inside = in.substring(1, in.length()-1).trim();
        if (inside.isEmpty()) return new int[0];

        String rest = inside;
        int sc = inside.indexOf(';');
        if (sc >= 0) {
            String bass = inside.substring(0, sc).trim();
            if (!bass.isEmpty() && !bass.matches("(b|#)?\\d+")) {
                throw new IllegalArgumentException("Baix de posicions invàlid: " + bass);
            }
            rest = inside.substring(sc + 1).trim();
        }

        if (rest.isEmpty()) return new int[0];
        String[] parts = rest.split(",");
        TreeSet<Integer> set = new TreeSet<>();
        for (String p : parts) {
            String t = p.trim();
            Matcher m = Pattern.compile("(b|#)?(\\d+)").matcher(t);
            if (!m.matches()) throw new IllegalArgumentException("Grau invàlid: " + t);
            String acc = m.group(1); int n = Integer.parseInt(m.group(2));
            Map<Integer,Integer> base = new HashMap<>();
            base.put(1,0); base.put(2,2); base.put(3,4); base.put(4,5); base.put(5,7); base.put(6,9); base.put(7,11);
            base.put(9,14); base.put(11,17); base.put(13,21);
            Integer semis = base.get(n);
            if (semis == null) throw new IllegalArgumentException("Grau no suportat: " + n);
            if ("b".equals(acc)) semis -= 1; else if ("#".equals(acc)) semis += 1;
            set.add(semis);
        }
        int[] r = new int[set.size()]; int i=0; for (Integer v: set) r[i++]=v; return r;
    }

    private static int[] parseIntList(String inside) {
        if (inside == null) return new int[0];
        String norm = inside.trim();
        if ((norm.startsWith("\"") && norm.endsWith("\"")) || (norm.startsWith("“") && norm.endsWith("”"))) {
            norm = norm.substring(1, norm.length()-1).trim();
        }
        norm = norm.replace('\uFF0C', ',').replace('\u00A0', ' ').replace('\u2007', ' ').replace('\u202F', ' ');
        if (norm.indexOf(';') >= 0) throw new IllegalArgumentException("Separador ';' invàlid dins la llista d'intervals: \"" + norm + "\"");
        if (norm.isEmpty()) return new int[0];
        String[] parts = norm.split("\\s*,\\s*");
        for (String tok : parts) if (tok.contains(",")) throw new IllegalArgumentException("Token invàlid \""+tok+"\"");
        int[] res = new int[parts.length];
        for (int i=0;i<parts.length;i++) {
            String tok = parts[i].trim();
            if (!tok.matches("-?\\d+")) {
                if (tok.matches("\\d+,\\d+")) throw new IllegalArgumentException("Decimal \""+tok+"\" no permès");
                throw new IllegalArgumentException("Interval no enter \""+tok+"\"");
            }
            res[i] = Integer.parseInt(tok);
        }
        return res;
    }
    private static int parseRootPcFromCustomOrLetter(String rootWord) {
        String lw = rootWord.toLowerCase();
        Integer pc = CUSTOM_TO_PC.get(lw);
        if (pc != null) return pc;
        Integer pc2 = NOTE_PC.get(rootWord);
        if (pc2 != null) return pc2;
        throw new IllegalArgumentException("Arrel desconeguda en notació d'intervals: " + rootWord);
    }
    private static String formatIntervals(int[] ivals) {
        TreeSet<Integer> uniq = new TreeSet<>();
        for (int x: ivals) uniq.add(x);
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean first = true;
        for (Integer v : uniq) { if (!first) sb.append(','); sb.append(v); first=false; }
        sb.append(']');
        return sb.toString();
    }
    private static int[] uniqSorted(int[] a){ TreeSet<Integer> s=new TreeSet<>(); for(int x:a) s.add(x); int[] r=new int[s.size()]; int i=0; for(Integer v:s) r[i++]=v; return r; }
    private static String capitalize(String s) { if (s==null || s.isEmpty()) return s; return Character.toUpperCase(s.charAt(0)) + s.substring(1); }
    private static int minimizeSemitoneDistance(int d) { d = ((d % 12) + 12) % 12; if (d > 6) d -= 12; return d; }
    private static String matchQuality(int[] rawIvals) {
        TreeSet<Integer> norm = new TreeSet<>();
        for (int x: rawIvals) norm.add(((x%12)+12)%12);
        for (Map.Entry<String,int[]> e : QUALITY_TO_INTERVALS.entrySet()) {
            TreeSet<Integer> cand = new TreeSet<>();
            for (int x: e.getValue()) cand.add(((x%12)+12)%12);
            if (cand.equals(norm)) return e.getKey();
        }
        return null;
    }

    /* ===================== CSV runtime ===================== */
    public static synchronized void reloadFromCsv() {
        QUALITY_TO_INTERVALS.clear();
        QUALITY_ALIAS.clear();
        QUALITY_TO_POSICIONS.clear();
        loadCsvRuntime();
    }

    private static void loadCsvRuntime() {
        List<String> lines = resolveCsvLines();
        parseCsvLines(lines);
    }

    private static List<String> resolveCsvLines() {
        String sysPath = System.getProperty(SYS_PROP_CSV);
        if (sysPath != null && !sysPath.isBlank()) {
            Path p = Paths.get(sysPath);
            if (Files.isRegularFile(p)) return readFileWithFallback(p);
        }
        String envPath = System.getenv(ENV_VAR_CSV);
        if (envPath != null && !envPath.isBlank()) {
            Path p = Paths.get(envPath);
            if (Files.isRegularFile(p)) return readFileWithFallback(p);
        }
        Path wd = Paths.get(DEFAULT_CSV_NAME).toAbsolutePath();
        if (Files.isRegularFile(wd)) return readFileWithFallback(wd);
        try (InputStream in = ChordSymbols_old.class.getClassLoader().getResourceAsStream(DEFAULT_CSV_NAME)) {
            if (in != null) return readAllWithFallback(in, DEFAULT_CSV_NAME);
        } catch (IOException ioe) {
            throw new RuntimeException("Error llegint resource CSV del classpath", ioe);
        }
        throw new RuntimeException(
            "No s'ha trobat " + DEFAULT_CSV_NAME +
            ". Posa'l a la working dir: " + System.getProperty("user.dir") +
            " o passa la ruta amb -D"+SYS_PROP_CSV+"=/ruta/ChordSymbols.csv, " +
            "o exporta "+ENV_VAR_CSV+"=/ruta/ChordSymbols.csv, " +
            "o posa'l a src/main/resources."
        );
    }

    private static List<String> readFileWithFallback(Path p) {
        byte[] bytes;
        try { bytes = Files.readAllBytes(p); }
        catch (IOException ioe) { throw new RuntimeException("No s'ha pogut llegir el CSV: " + p.toAbsolutePath(), ioe); }
        if (bytes.length>=2 && bytes[0]==0x50 && bytes[1]==0x4B) {
            throw new RuntimeException("Sembla un .xlsx. Exporta com a CSV text pla.");
        }
        for (String csName : CHARSET_TRY_ORDER) {
            try {
                String text = new String(bytes, java.nio.charset.Charset.forName(csName));
                String norm = text.replace("\r\n","\n").replace("\r","\n");
                List<String> lines = Arrays.asList(norm.split("\n", -1));
                String first = lines.stream().map(s->s==null? "": s.trim()).filter(s->!s.isEmpty() && !s.startsWith("#")).findFirst().orElse("");
                long q = first.chars().filter(ch -> ch=='?').count();
                boolean manyQ = q > Math.max(3, first.length()*0.6);
                if (!manyQ) { return lines; }
            } catch (Exception ignore) {}
        }
        String fallback = new String(bytes, StandardCharsets.UTF_8).replace("\r\n","\n").replace("\r","\n");
        return Arrays.asList(fallback.split("\n", -1));
    }
    private static List<String> readAllWithFallback(InputStream in, String resourceName) throws IOException {
        byte[] bytes = in.readAllBytes();
        if (bytes.length>=2 && bytes[0]==0x50 && bytes[1]==0x4B) {
            throw new RuntimeException("El resource sembla un .xlsx. Cal CSV de text pla.");
        }
        for (String csName : CHARSET_TRY_ORDER) {
            try {
                String text = new String(bytes, java.nio.charset.Charset.forName(csName));
                String norm = text.replace("\r\n","\n").replace("\r","\n");
                List<String> lines = Arrays.asList(norm.split("\n", -1));
                String first = lines.stream().map(s->s==null? "": s.trim()).filter(s->!s.isEmpty() && !s.startsWith("#")).findFirst().orElse("");
                long q = first.chars().filter(ch -> ch=='?').count();
                boolean manyQ = q > Math.max(3, first.length()*0.6);
                if (!manyQ) { return lines; }
            } catch (Exception ignore) {}
        }
        String fallback = new String(bytes, StandardCharsets.UTF_8).replace("\r\n","\n").replace("\r","\n");
        return Arrays.asList(fallback.split("\n", -1));
    }

    private static void parseCsvLines(List<String> lines) {
        if (lines.isEmpty()) throw new RuntimeException("CSV buit");
        int idxSimbol = -1, idxPosicions = -1, idxSinonims = -1;
        int lineNo = 0;

        // Header
        for (; lineNo < lines.size(); lineNo++) {
            String headerRaw = stripBOM(lines.get(lineNo));
            String header = headerRaw.trim();
            if (header.isEmpty() || header.startsWith("#")) continue;
            List<String> cols = splitCsvRespectBrackets(header);
            int[] idx = resolveHeaderIndexes(headerRaw, cols, false);
            idxSimbol   = idx[0];
            idxPosicions= idx[1];
            idxSinonims = idx[2];
            lineNo++;
            break;
        }

        // Dades
        for (; lineNo < lines.size(); lineNo++) {
            String rawLine = lines.get(lineNo);
            if (rawLine == null) continue;
            String line = stripBOM(rawLine).trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            List<String> cols = splitCsvRespectBrackets(line);
            if (cols.isEmpty()) continue;

            String simbol   = take(cols, idxSimbol);
            String posicions= take(cols, idxPosicions);
            String sinonims = take(cols, idxSinonims);

            if (simbol == null || simbol.isEmpty()) continue;

            // --- NOVETAT: ignora explícitament les files que són secció
            if (isSectionName(simbol)) continue;

            simbol = simbol.replaceAll("\\s+", "");
            String root = parseRootToken(simbol);
            if (root == null) continue; // secció (fallback)

            String qualRaw = simbol.substring(root.length());
            int si = qualRaw.indexOf('/');
            if (si >= 0) qualRaw = qualRaw.substring(0, si);
            String qualCanon = normalizeQualityFromCsv(qualRaw);

            // intervals des de posicions
            int[] intervals = null;
            if (posicions != null && !posicions.trim().isEmpty()) {
                try {
                    List<String> degs = parsePositionsList(posicions.trim());
                    intervals = degreesToSemitones(degs);
                } catch (Exception ex) {
                    continue;
                }
            }
            if (intervals == null) continue;

            QUALITY_TO_INTERVALS.putIfAbsent(qualCanon, intervals);
            if (posicions != null && !posicions.trim().isEmpty()) {
                QUALITY_TO_POSICIONS.putIfAbsent(qualCanon, posicions.trim());
            }

            // Àlies a partir de "Sinonims"
            if (sinonims != null && !sinonims.trim().isEmpty()) {
                String cleaned = sinonims.trim().replaceAll("\\s+", "");
                String[] parts = cleaned.split("[,/_]+");
                for (String p2: parts) {
                    if (p2.isEmpty()) continue;
                    if ("ABCDEFG".indexOf(Character.toUpperCase(p2.charAt(0)))<0) continue;
                    String r2 = parseRootToken(p2);
                    if (r2 == null) continue;
                    String q2 = p2.substring(r2.length());
                    String qn = normalizeQualityFromCsv(q2);
                    if (!qn.equals(qualCanon) && !QUALITY_TO_INTERVALS.containsKey(qn)) {
                        QUALITY_ALIAS.putIfAbsent(qn, qualCanon);
                    }
                }
            }
        }
    }

    /* ===================== Header & split utils ===================== */
    private static String stripBOM(String s) {
        if (s != null && !s.isEmpty() && s.charAt(0) == '\uFEFF') return s.substring(1);
        return s;
    }
    private static String normalizeAscii(String raw) {
        if (raw == null) return "";
        String s = stripBOM(raw).trim();
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+","");
        return s.toLowerCase(Locale.ROOT);
    }
    private static String normalizeHeader(String raw) {
        if (raw == null) return "";
        String s = stripBOM(raw).trim().toLowerCase(Locale.ROOT);
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+","");
        s = s.replaceAll("[^a-z]", "");
        return s;
    }
    private static boolean isHeaderSimbol(String key) {
        return key.equals("simbol") || key.equals("simbols") || key.equals("symbol") || key.equals("symbols")
                || key.equals("simbolo") || key.equals("simbolic") || key.equals("symbolic");
    }
    private static boolean isHeaderPosicions(String key) {
        return key.equals("posicions") || key.equals("posicio") || key.equals("positions")
                || key.equals("position") || key.equals("graus") || key.equals("degrees") || key.equals("degree");
    }
    private static boolean isHeaderSinonims(String key) {
        return key.equals("sinonims") || key.equals("sinonim") || key.equals("synonyms") || key.equals("synonym")
                || key.equals("aliases") || key.equals("alias") || key.equals("sinonimsynonyms");
    }
    private static int[] resolveHeaderIndexes(String rawHeader, List<String> cols, boolean forTest) {
        int idxSimbol = -1, idxPosicions = -1, idxSinonims = -1;
        for (int i=0; i<cols.size(); i++) {
            String raw = stripBOM(cols.get(i));
            String key = normalizeHeader(raw);
            if (isHeaderSimbol(key))   idxSimbol = i;
            if (isHeaderPosicions(key)) idxPosicions = i;
            if (isHeaderSinonims(key))  idxSinonims = i;
        }
        if (idxSimbol < 0) {
            if (cols.size() >= 1) idxSimbol = 0;
            if (cols.size() >= 3) {
                if (idxSinonims < 0)  idxSinonims  = 1;
                if (idxPosicions < 0) idxPosicions = 2;
            }
        }
        if (idxSimbol < 0) throw new RuntimeException("Cap columna 'Simbol' al CSV.");
        return new int[]{idxSimbol, idxPosicions, idxSinonims};
    }

    private static List<String> splitCsvRespectBrackets(String line) {
        ArrayList<String> cols = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int depth = 0;
        for (int i=0;i<line.length();i++) {
            char c = line.charAt(i);
            if (c == '[') { depth++; cur.append(c); }
            else if (c == ']') { depth = Math.max(0, depth-1); cur.append(c); }
            else if ((c == ';' || c == '\t') && depth==0) { cols.add(cur.toString()); cur.setLength(0); }
            else { cur.append(c); }
        }
        cols.add(cur.toString());
        return cols;
    }
    private static String take(List<String> cols, int idx) { if (idx<0 || idx>=cols.size()) return null; return cols.get(idx); }
    private static String stripOuterQuotes(String s) {
        if (s == null || s.length()<2) return s;
        char a = s.charAt(0), b = s.charAt(s.length()-1);
        if ((a=='"' && b=='"') || (a=='“' && b=='”') || (a=='\'' && b=='\'')) {
            return s.substring(1, s.length()-1).trim();
        }
        return s;
    }

    /* ===================== MAIN ===================== */
    public static void main(String[] args) {
        // Genera la taula amb el mateix ordre i seccions del CSV original
        generaChordSymbolTable();

        // Smoke test curt
        System.out.println("Exemple Cmaj7:");
        System.out.println("  Intervals: " + toIntervalNotation("Cmaj7"));
        System.out.println("  Notes (#/b): " + toAlteracions("Cmaj7"));
        System.out.println("  DodecaNoms:  " + toNoteNames("Cmaj7"));
    }

    /* ===================== Utils ===================== */
}
