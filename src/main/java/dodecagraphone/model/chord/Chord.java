/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.chord;

import dodecagraphone.model.ToneRange;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * [CA] Representació d'un acord musical en el context dodecafònic de DoDeReBu.
 * Un acord es defineix per la seva nota arrel (relativa a la tonalitat {@code midiKey}),
 * la forma ({@code shape}: intervals en semitones relatius a l'arrel), una informació
 * textual opcional i un baix de tall ({@code bass}).
 * <p>
 * [EN] Representation of a musical chord in DoDeReBu's dodecaphonic context.
 * A chord is defined by its root note (relative to the key {@code midiKey}),
 * its shape ({@code shape}: semitone intervals relative to the root), optional
 * text information, and a slash bass note ({@code bass}).
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class Chord {

    /** [CA] Valor que indica que no hi ha nota de baix de tall. / [EN] Value indicating no slash bass note. */
    public static final int NULL_BASS = -31;
    /** [CA] Nota arrel relativa a {@code midiKey}. / [EN] Root note relative to {@code midiKey}. */
    protected int root; // relative to midiKey
    /** [CA] Tonalitat MIDI de referència. / [EN] Reference MIDI key. */
    protected int midiKey;
    /** [CA] Intervals en semitones relatius a l'arrel. / [EN] Semitone intervals relative to the root. */
    protected int[] shape; // relative to root
    /** [CA] Informació textual de l'acord. / [EN] Textual chord information. */
    protected String info;
    /** [CA] Nota de baix de tall (slash bass), relativa a l'arrel. / [EN] Slash bass note, relative to the root. */
    protected int bass; // the slash bass note of the chord, relative to root
    /** [CA] Nombre de columnes que ocupa l'acord a la partitura. / [EN] Number of score columns occupied by the chord. */
    protected int ncols = 1;
    /** Column offset within the beat at which this chord actually sounds (0 = beat start). */
    protected int beatColOffset = 0;

    /**
     * [CA] Constructor per defecte. Crea un acord buit/invàlid.
     * <p>
     * [EN] Default constructor. Creates an empty/invalid chord.
     */
    public Chord() {
        this.root = Settings.INVALID_CHORD;
        this.midiKey = -1;
        this.shape = null;
        this.info = "";
        this.bass = NULL_BASS;
    }

    /**
     * [CA] Constructor sense baix de tall.
     * <p>
     * [EN] Constructor without slash bass.
     *
     * @param root    [CA] nota arrel relativa a {@code midiKey} / [EN] root note relative to {@code midiKey}
     * @param shape   [CA] intervals en semitones relatius a l'arrel / [EN] semitone intervals relative to root
     * @param midiKey [CA] tonalitat MIDI de referència / [EN] reference MIDI key
     * @param info    [CA] informació textual de l'acord / [EN] textual chord information
     */
    public Chord(int root, int[] shape, int midiKey, String info) {
        this.root = root;
        this.midiKey = midiKey;
        this.shape = shape;
        this.info = info;
        this.bass = NULL_BASS;
    }

    /**
     * [CA] Constructor amb baix de tall.
     * <p>
     * [EN] Constructor with slash bass.
     *
     * @param root    [CA] nota arrel relativa a {@code midiKey} / [EN] root note relative to {@code midiKey}
     * @param shape   [CA] intervals en semitones relatius a l'arrel / [EN] semitone intervals relative to root
     * @param midiKey [CA] tonalitat MIDI de referència / [EN] reference MIDI key
     * @param info    [CA] informació textual de l'acord / [EN] textual chord information
     * @param bass    [CA] nota de baix de tall relativa a l'arrel / [EN] slash bass note relative to root
     */
    public Chord(int root, int[] shape, int midiKey, String info, int bass) {
        this.root = root;
        this.midiKey = midiKey;
        this.shape = shape;
        this.info = info;
        this.bass = bass;
    }

    /**
     * [CA] Constructor que parseja un acord a partir d'una cadena de text en format DoDeReBu.
     * Formats acceptats: {@code Do[0,4,7]}, {@code Do7}, {@code Dom}, {@code Do[0,4,7]/Sol}, etc.
     * Si la cadena no és un nom de nota vàlid, s'usa com a informació textual pura.
     * <p>
     * [EN] Constructor that parses a chord from a text string in DoDeReBu format.
     * Accepted formats: {@code Do[0,4,7]}, {@code Do7}, {@code Dom}, {@code Do[0,4,7]/Sol}, etc.
     * If the string is not a valid note name, it is used as pure text information.
     *
     * @param chordString [CA] cadena de text que representa l'acord / [EN] text string representing the chord
     */
    public Chord(String chordString) {
        boolean VERBOSE = false;
        /*
        Format de chordSting
            Do ---> Do[0,4,7]
            Do7 ---> Do[0,4,7,10]
            DoMaj7 ---> Do[0,4,7,11]
            Dom ---> Do[0,3,7]
            Dom7 ---> Do[0,3,7,10]
            Do7b5 ---> Do[0,3,6,10]
            Do[0,4,7] ---> Nom Nota + [ + intervals,... + ]
            Do[0,4,7,10,13]/So ---> Idem amb baix com nom de nota
            Do[0,4,7,10,13]/-5 ---> Idem amb baix com interval
        L'acord pot ser buit
        Després del format d'un acord hi ha una Info opcional
         */
        this.midiKey = ToneRange.MIDDLE_C;
        this.root = Settings.USE_INFO_AS_SIMBOL;
        this.shape = null;
        this.bass = NULL_BASS;
        this.info = chordString;

        try {
            if (chordString.isEmpty()) {
                this.root = Settings.USE_INFO_AS_SIMBOL;
                this.info = "";
                if (VERBOSE) {
                    System.out.println("Chord::Chord(string) = " + this.toString());
                }
                return;
            }
            chordString = chordString.trim();
            // Separem info addicional (després d'un espai)
            String[] parts = chordString.split("\\s+", 2);
            String chordPart = parts[0].trim();
            String extraInfo = (parts.length > 1) ? parts[1].trim() : "";

            // Separem baix
            String bassPart = null;
            if (chordPart.contains("/")) {
                String[] slashSplit = chordPart.split("/", 2);
                chordPart = slashSplit[0].trim();
                bassPart = slashSplit[1].trim();
            }

            // Separem intervals (si hi són)
            String rootToken = chordPart;
            String[] intervalParts = null;
            if (chordPart.contains("[")) {
                int openIdx = chordPart.indexOf("[");
                int closeIdx = chordPart.indexOf("]");
                rootToken = chordPart.substring(0, openIdx).trim();
                String intervalText = chordPart.substring(openIdx + 1, closeIdx);
                intervalParts = intervalText.split(",");
            }

            // Assignem intervals si hi ha
            if (intervalParts != null) {
                this.shape = new int[intervalParts.length];
                for (int i = 0; i < intervalParts.length; i++) {
                    this.shape[i] = Integer.parseInt(intervalParts[i].trim());
                }
            } else {
                // Si no hi ha intervals, els deduïm del sufix
                if (rootToken.endsWith("Maj7")) {
                    this.shape = new int[]{0, 4, 7, 11};
                } else if (rootToken.endsWith("m7") || rootToken.endsWith("om7")) {
                    this.shape = new int[]{0, 3, 7, 10};
                } else if (rootToken.endsWith("m") || rootToken.endsWith("om")) {
                    this.shape = new int[]{0, 3, 7};
                } else if (rootToken.endsWith("7b5")) {
                    this.shape = new int[]{0, 3, 6, 10};
                } else if (rootToken.endsWith("7")) {
                    this.shape = new int[]{0, 4, 7, 10};
                } else {
                    this.shape = new int[]{0, 4, 7}; // major per defecte
                }

                // El rootToken pot tenir un sufix, l'hem de treure per calcular bé el root
                for (String suffix : new String[]{"Maj7", "m7", "7b5", "m", "7", "om7", "om"}) {
                    if (rootToken.endsWith(suffix)) {
                        rootToken = rootToken.substring(0, rootToken.length() - suffix.length());
                        break;
                    }
                }
            }

            // Deduïm el root
            String aux = rootToken.toLowerCase();
            int rootMidi = ToneRange.getMidi(aux);
            this.root = rootMidi - midiKey;


            // Calculem el baix si hi és
            if (bassPart != null && !bassPart.isEmpty()) {
                if (bassPart.startsWith("-") || Character.isDigit(bassPart.charAt(0))) {
                    this.bass = Integer.parseInt(bassPart);
                } else if (ToneRange.isValidNoteName(bassPart.toLowerCase())) {
                    int bassMidi = ToneRange.getMidi(bassPart.toLowerCase());
                    this.bass = bassMidi - rootMidi -12;
                    if (this.bass<-11) this.bass+=12;
                } // si no és vàlid, es deixa com NULL_BASS
            }

            // Guardem info addicional si hi ha
            if (!extraInfo.isEmpty()) {
                this.info = extraInfo;
            }

            // Si no és un nom de nota vàlid, es considera info
            if (!ToneRange.isValidNoteName(rootToken.toLowerCase())) {
                this.root = Settings.USE_INFO_AS_SIMBOL;
                this.shape = null;
                this.info = chordString;
                if (VERBOSE) {
                    System.out.println("Chord::Chord(string) = " + this.toString());
                }
                return;
            }

        } catch (Exception e) {
            this.root = Settings.INVALID_CHORD;
            this.midiKey = -1;
            this.shape = null;
            this.bass = NULL_BASS;
            this.info = "Invalid chord: " + chordString;
        }
        if (VERBOSE) {
            System.out.println("Chord::Chord(string) = " + this.toString());
        }
    }

    /**
     * [CA] Retorna si l'acord és vàlid (arrel diferent de {@code INVALID_CHORD}).
     * <p>
     * [EN] Returns whether the chord is valid (root different from {@code INVALID_CHORD}).
     *
     * @return [CA] {@code true} si l'acord és vàlid / [EN] {@code true} if the chord is valid
     */
    public boolean isValidChord(){
        return (root!=Settings.INVALID_CHORD);
    }

    /**
     * [CA] Retorna el nombre de columnes que ocupa l'acord a la partitura.
     * <p>
     * [EN] Returns the number of score columns occupied by the chord.
     *
     * @return [CA] nombre de columnes / [EN] number of columns
     */
    public int getNCols() {
        return ncols;
    }

    /**
     * [CA] Estableix el nombre de columnes que ocupa l'acord a la partitura.
     * <p>
     * [EN] Sets the number of score columns occupied by the chord.
     *
     * @param ncols [CA] nombre de columnes / [EN] number of columns
     */
    public void setNCols(int ncols) {
        this.ncols = ncols;
    }

    /**
     * [CA] Retorna el desplaçament de columnes dins del beat en el qual sona l'acord (0 = inici del beat).
     * <p>
     * [EN] Returns the column offset within the beat at which this chord sounds (0 = beat start).
     *
     * @return [CA] desplaçament en columnes / [EN] column offset
     */
    public int getBeatColOffset() {
        return beatColOffset;
    }

    /**
     * [CA] Estableix el desplaçament de columnes dins del beat en el qual sona l'acord.
     * <p>
     * [EN] Sets the column offset within the beat at which this chord sounds.
     *
     * @param beatColOffset [CA] desplaçament en columnes / [EN] column offset
     */
    public void setBeatColOffset(int beatColOffset) {
        this.beatColOffset = beatColOffset;
    }

    /**
     * [CA] Retorna la tonalitat MIDI de referència de l'acord.
     * <p>
     * [EN] Returns the reference MIDI key of the chord.
     *
     * @return [CA] tonalitat MIDI / [EN] MIDI key
     */
    public int getMidiKey() {
        return midiKey;
    }

    /**
     * [CA] Retorna la nota de baix de tall relativa a l'arrel, o {@code NULL_BASS} si no n'hi ha.
     * <p>
     * [EN] Returns the slash bass note relative to the root, or {@code NULL_BASS} if none.
     *
     * @return [CA] baix de tall o {@code NULL_BASS} / [EN] slash bass or {@code NULL_BASS}
     */
    public int getBass() {
        return bass;
    }

    /**
     * [CA] Retorna la nota arrel de l'acord relativa a la tonalitat {@code midiKey}.
     * <p>
     * [EN] Returns the chord root note relative to the key {@code midiKey}.
     *
     * @return [CA] arrel relativa / [EN] relative root
     */
    public int getRoot() {
        return root;
    }

    /**
     * [CA] Retorna el valor MIDI absolut de la nota arrel de l'acord.
     * <p>
     * [EN] Returns the absolute MIDI value of the chord root note.
     *
     * @return [CA] valor MIDI de l'arrel / [EN] MIDI value of the root
     */
    public int getMidiRoot() {
        return root + midiKey;
    }

    /**
     * [CA] Retorna el valor MIDI absolut de la nota de baix de tall.
     * <p>
     * [EN] Returns the absolute MIDI value of the slash bass note.
     *
     * @return [CA] valor MIDI del baix de tall / [EN] MIDI value of the slash bass
     */
    public int getMidiBass() {
        return root + midiKey + bass;
    }

    /**
     * [CA] Retorna una còpia independent (clon) d'aquest acord.
     * Si l'acord no té forma ({@code shape == null}), el clon és un acord buit/invàlid.
     * <p>
     * [EN] Returns an independent copy (clone) of this chord.
     * If the chord has no shape ({@code shape == null}), the clone is an empty/invalid chord.
     *
     * @return [CA] còpia de l'acord / [EN] copy of the chord
     */
    public Chord cloneChord() {
        Chord ch = new Chord();
        if (this.shape != null) {
            ch.root = this.root;
            ch.midiKey = this.midiKey;
            ch.shape = this.shape.clone();
            ch.info = new String(this.info);
            ch.bass = this.bass;
            ch.beatColOffset = this.beatColOffset;
        }
        return ch;
    }

    /**
     * [CA] Retorna un nou acord transposat un nombre de semitones.
     * <p>
     * [EN] Returns a new chord transposed by a number of semitones.
     *
     * @param step [CA] nombre de semitones de transposició (positiu = amunt, negatiu = avall) /
     *             [EN] number of semitones to transpose (positive = up, negative = down)
     * @return [CA] acord transposat / [EN] transposed chord
     */
    public Chord transpose(int step) {
        Chord newch = cloneChord();
        if (newch.midiKey != -1) {
            newch.midiKey += step;
        }
        return newch;
    }

    /**
     * [CA] Estableix la nota de baix de tall.
     * <p>
     * [EN] Sets the slash bass note.
     *
     * @param bass [CA] nota de baix de tall relativa a l'arrel / [EN] slash bass note relative to root
     */
    public void setBass(int bass) {
        this.bass = bass;
    }

    /**
     * [CA] Retorna l'array d'intervals (forma) de l'acord relatius a l'arrel.
     * <p>
     * [EN] Returns the intervals array (shape) of the chord relative to the root.
     *
     * @return [CA] array d'intervals en semitones / [EN] array of semitone intervals
     */
    public int[] getShape() {
        return shape;
    }

    /**
     * [CA] Retorna les notes de l'acord com a llista de valors MIDI absoluts (sense el baix de tall).
     * <p>
     * [EN] Returns the chord notes as a list of absolute MIDI values (without the slash bass).
     *
     * @return [CA] llista de notes MIDI / [EN] list of MIDI notes
     */
    public List<Integer> getMidiShapeAsList() {
        List<Integer> list = new ArrayList<>();
        for (int note : shape) {
            list.add(note + root);
        }
        return list;
    }

    /**
     * [CA] Retorna totes les notes MIDI de l'acord (incloent el baix de tall si n'hi ha),
     * ajustades per quedar dins del rang MIDI vàlid.
     * <p>
     * [EN] Returns all MIDI notes of the chord (including the slash bass if present),
     * adjusted to fall within the valid MIDI range.
     *
     * @return [CA] array de notes MIDI / [EN] array of MIDI notes
     */
    public int[] getMidiNotes() {
        int j;
        int[] midiNotes;
        if (this.bass != NULL_BASS) {
            midiNotes = new int[shape.length + 1];
            int midi = this.getMidiBass();
            while (midi > ToneRange.getHighestMidi()) {
                midi -= 12;
            }
            while (midi < ToneRange.getLowestMidi()) {
                midi += 12;
            }
            midiNotes[0] = midi;
            j = 1;
        } else {
            midiNotes = new int[shape.length];
            j = 0;
        }
        for (int i = 0; i < shape.length; i++) {
            int midi = shape[i] + this.getMidiRoot();
            while (midi > ToneRange.getHighestMidi()) {
                midi -= 12;
            }
            while (midi < ToneRange.getLowestMidi()) {
                midi += 12;
            }
            midiNotes[i + j] = midi;
        }
        return midiNotes;
    }

    /**
     * [CA] Retorna un nou acord en posició fonamental (estat fonamental).
     * Corregeix els intervals negatius rotant l'array fins que l'interval 0 sigui positiu.
     * <p>
     * [EN] Returns a new chord in root position.
     * Corrects negative intervals by rotating the array until interval 0 is positive.
     *
     * @return [CA] acord en posició fonamental / [EN] chord in root position
     */
    public Chord getInRootPosition() {
        String newinfo = info.replace("First inversion", "Root position");
        newinfo = info.replace("Second inversion", "Root position");
        int[] newshape = Arrays.copyOf(shape, shape.length);
        for (int i = 0; i < newshape.length; i++) {
            if (newshape[0] < 0) {
                newshape[0] += 12;
                Utilities.rotateArray(1, newshape);
            }
        }
        Chord ch = new Chord(root, newshape, midiKey, newinfo, bass);
        return ch;
    }

    /**
     * [CA] Retorna la informació textual associada a l'acord.
     * <p>
     * [EN] Returns the textual information associated with the chord.
     *
     * @return [CA] informació textual / [EN] textual information
     */
    public String getInfo() {
        return info;
    }

    /**
     * [CA] Retorna el nom de la nota arrel de l'acord (2 caràcters), amb majúscules
     * per a acords majors i minúscules per a acords menors/disminuïts.
     * <p>
     * [EN] Returns the chord root note name (2 characters), capitalized for major
     * chords and lowercase for minor/diminished chords.
     *
     * @return [CA] nom de l'arrel / [EN] root name
     */
    public String getChordRootName() {
        int midiRoot = root + midiKey;
        String message = ToneRange.getNoteName(midiRoot, midiKey);
        message = message.substring(0, 2);
        if (shape[1] - shape[0] == 4) {
            message = Utilities.capitalize(message);
        }
        return message;
    }

    /**
     * [CA] Retorna el símbol de l'acord (arrel + baix de tall si n'hi ha).
     * <p>
     * [EN] Returns the chord symbol (root + slash bass if present).
     *
     * @return [CA] símbol de l'acord / [EN] chord symbol
     */
    public String getSimbol() {
        String message = getChordRootName();
        if (this.bass != NULL_BASS) {
            String noteName = ToneRange.getNoteName(bass + root + midiKey, midiKey);
            if (bass == root) {
                noteName = Utilities.capitalize(noteName);
            }
            noteName = noteName.substring(0, noteName.length() - 1); // delete octave number
            message += "/" + noteName;
        }
        return message;
    }

    /**
     * [CA] Retorna el símbol de l'acord amb el baix de tall en format numèric (interval).
     * <p>
     * [EN] Returns the chord symbol with the slash bass in numeric (interval) format.
     *
     * @return [CA] símbol amb baix numèric / [EN] symbol with numeric bass
     */
    public String getSimbolWithNumericBass() {
        String message = getChordRootName();
        if (this.bass != NULL_BASS) {
            message += "/" + (bass);
        }
        return message;
    }

    /**
     * [CA] Retorna una representació bàsica de l'acord en format {@code Arrel[intervals]/bass}.
     * <p>
     * [EN] Returns a basic representation of the chord in {@code Root[intervals]/bass} format.
     *
     * @return [CA] cadena bàsica de l'acord / [EN] basic chord string
     */
    public String basicString() {
        String message = getChordRootName();
        if (this.bass != NULL_BASS) {
            message += "/" + (bass);
        }
        message += "[";
        for (int pos : shape) {
            message += pos + ",";
        }
        message = message.substring(0, message.length() - 1); // delete last colon
        message += "]";
        return message;
    }

    /**
     * [CA] Retorna una representació completa de l'acord en format text, incloent
     * el nom de l'arrel, intervals, notes MIDI i noms de notes. Si l'acord és
     * d'informació pura o invàlid, retorna el camp {@code info}.
     * <p>
     * [EN] Returns a complete text representation of the chord, including the root
     * name, intervals, MIDI notes and note names. If the chord is info-only or
     * invalid, returns the {@code info} field.
     *
     * @return [CA] representació textual de l'acord / [EN] textual representation of the chord
     */
    @Override
    public String toString() {
        String out = "";
        if ((this.root == Settings.USE_INFO_AS_SIMBOL)||this.root == Settings.INVALID_CHORD)
            return info;
        out = ToneRange.getChordName(midiKey+root);
        out += "[";
        for (int note : shape) {
            out += note + ",";
        }
        out = out.substring(0, out.length() - 1);
        out += "]";
        if (bass!=NULL_BASS){
            out += "/"+bass;
        }
        out += " ";
        out += "[";
        for (int note : getMidiNotes()) {
            out += note + ",";
        }
        out = out.substring(0, out.length() - 1);
        out += "]";
        out += " ";
        out += "[";
        for (int note : getMidiNotes()) {
            out += ToneRange.getNoteName(note) + ",";
        }
        out = out.substring(0, out.length() - 1);
        out = out + "] ";
        out += info;
        return out;
    }
}
