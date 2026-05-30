/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model;

import dodecagraphone.ui.AppConfig;
import dodecagraphone.ui.Settings;
import java.util.HashMap;

/**
 * [CA] Gestió estàtica del rang de notes i les tonalitats del projecte.
 * Proporciona constants de rang MIDI, noms de notes en notació de do mòbil
 * (do, re, mi...), conversions MIDI ↔ nom, càlcul de signatures de clau,
 * i un mapa de noms de percussió General MIDI.
 * <p>
 * [EN] Static management of the note range and tonalities of the project.
 * Provides MIDI range constants, note names in movable-do notation
 * (do, re, mi...), MIDI ↔ name conversions, key signature calculation,
 * and a General MIDI drum name map.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class ToneRange {
    public static final int DEFAULT_HIGHEST_MIDI = 84;
    public static final int DEFAULT_LOWEST_MIDI = 36;
    public static final int DEFAULT_OCTAVES_UP = 0;
    public static final boolean DEFAULT_IS_METALLOPHONE = true;
    public static final int MAX_NKEYS = 50;
    public static final int MIDDLE_C; // = 60;
    public static final int DEFAULT_KEY;
    private static final char DEFAULT_MODE = 'M'; // Major
    private static int lowestMidi; //= 55;
    private static int highestMidi;// = 79;
    private static int octavesUp; // = 0; = 2;
    private static boolean isMetallophone;
//    private static int lowestMidi = MIDDLE_C - 24; = 36
//    private static int highestMidi = MIDDLE_C + 24; = 84
    private static int lowestPau = 40;
    private static int highestPau = 64;
    private static int lowestSaxo = 49;
    private static int highestSaxo = 81;
    private static int lowestViolin = 55; // = MIDDLE_C - 5;
    private static int highestViolin = 105; // MIDDLE_C + 45;
    private static String instrument = "midi";
//    private static int octava = 0;
//    private static HashMap<Integer, String> filenames = new HashMap<>();
    private static HashMap<Integer, String> noteNames = new HashMap<>();
    private static HashMap<String, Integer> nameNotes = new HashMap<>();
    private static String path;
    private static boolean movileDo = false;

    static {
        if (!instrument.equalsIgnoreCase("midi")) {
            path = System.getProperty("user.dir") + "/" + instrument + "/";
            if (instrument.equals("Xylophone")) {
                lowestMidi = 69;
                highestMidi = 93;
//                octava = 12;
            } else if (instrument.equals("BflatClarinet")) {
                lowestMidi = 50;
                highestMidi = 86;
//                octava = 0;
            }
        }
        movileDo = false;
        lowestMidi = Integer.parseInt(AppConfig.get().get("lowestMidi", ""+DEFAULT_LOWEST_MIDI));
        highestMidi = Integer.parseInt(AppConfig.get().get("highestMidi", ""+DEFAULT_HIGHEST_MIDI));
        octavesUp  = DEFAULT_OCTAVES_UP;
        isMetallophone = Boolean.parseBoolean(AppConfig.get().get("isMetallophone", "" + DEFAULT_IS_METALLOPHONE));
        if (isMetallophone()){
            lowestMidi = 79;
            highestMidi = 103;
//            octavesUp = 2;  // OLD: eliminat - la transposició la gestiona displayOffset (I/O)
        }
//        MIDDLE_C = 60 + 12 * octavesUp;  // OLD
        MIDDLE_C = isMetallophone() ? 84 : (60 + 12 * octavesUp);
        // Metall: C6=84 (5 semitons per sobre de lowestMidi=79). Estàndard: C4=60.
        DEFAULT_KEY = MIDDLE_C;
        lowestPau = MIDDLE_C - 20;
        highestPau = MIDDLE_C + 4;
        lowestSaxo = MIDDLE_C - 11; //49;
        highestSaxo = MIDDLE_C + 21; //81;
        lowestViolin = MIDDLE_C - 5;
        highestViolin = MIDDLE_C + 45;
        setNoteNames();
    }

    /**
     * [CA] Retorna el mode per defecte de l'escala (Major).
     * <p>
     * [EN] Returns the default scale mode (Major).
     *
     * @return [CA] caràcter del mode per defecte ('M' = major) / [EN] default mode character ('M' = major)
     */
    public static char getDefaultMode(){
        return DEFAULT_MODE;
    }

    /**
     * [CA] Retorna el valor MIDI de la tonalitat per defecte (Do central).
     * <p>
     * [EN] Returns the MIDI value of the default key (middle C).
     *
     * @return [CA] valor MIDI de la tonalitat per defecte / [EN] MIDI value of the default key
     */
    public static int getDefaultKey(){
        return DEFAULT_KEY;
    }

    /**
     * [CA] Indica si l'instrument actiu és un metallòfon.
     * <p>
     * [EN] Indicates whether the active instrument is a metallophone.
     *
     * @return [CA] {@code true} si és metallòfon / [EN] {@code true} if it is a metallophone
     */
    public static boolean isMetallophone() {
        return isMetallophone;
    }

    /**
     * [CA] Estableix si l'instrument actiu és un metallòfon.
     * <p>
     * [EN] Sets whether the active instrument is a metallophone.
     *
     * @param v [CA] {@code true} per activar el mode metallòfon / [EN] {@code true} to activate metallophone mode
     */
    public static void setMetallophone(boolean v) {
        ToneRange.isMetallophone = v;
    }

    /**
     * [CA] Retorna el nombre màxim de tecles del grid.
     * <p>
     * [EN] Returns the maximum number of keys in the grid.
     *
     * @return [CA] nombre màxim de tecles / [EN] maximum number of keys
     */
    public static int getMaxNKeys(){
        return ToneRange.MAX_NKEYS;
    }

    /**
     * [CA] Retorna el nom del fitxer de so per a la nota MIDI donada amb l'instrument actiu.
     * <p>
     * [EN] Returns the sound file name for the given MIDI note with the active instrument.
     *
     * @param midi [CA] valor MIDI de la nota / [EN] MIDI note value
     * @return [CA] ruta del fitxer de so, o cadena buida si fora de rang / [EN] sound file path, or empty string if out of range
     */
    public static String getFilename(int midi) {
        return getFilename(midi, instrument);
    }

    /**
     * [CA] Retorna el nom del fitxer de so per a la nota MIDI i instrument donats.
     * <p>
     * [EN] Returns the sound file name for the given MIDI note and instrument.
     *
     * @param midi       [CA] valor MIDI de la nota / [EN] MIDI note value
     * @param instrument [CA] nom de l'instrument / [EN] instrument name
     * @return [CA] ruta del fitxer de so, o cadena buida si fora de rang / [EN] sound file path, or empty string if out of range
     */
    public static String getFilename(int midi, String instrument) {
        if (midi >= lowestMidi && midi <= highestMidi) {
            String fileN = instrument + "_" + getNoteName(midi) + ".wav";
            return path + fileN;
        }
        return "";
    }

    /**
     * [CA] Retorna el valor MIDI per a un nom de nota donat.
     * <p>
     * [EN] Returns the MIDI value for a given note name.
     *
     * @param noteName [CA] nom de la nota (p.ex. "do", "re") / [EN] note name (e.g. "do", "re")
     * @return [CA] valor MIDI corresponent / [EN] corresponding MIDI value
     */
    public static int getMidi(String noteName) {
        int midi = nameNotes.get(noteName);
        return midi;
    }

    /**
     * [CA] Retorna el nom d'acord (majúscula) per a un valor MIDI donat.
     * <p>
     * [EN] Returns the chord name (uppercase) for a given MIDI value.
     *
     * @param midi [CA] valor MIDI / [EN] MIDI value
     * @return [CA] nom d'acord en majúscula / [EN] chord name in uppercase
     */
    public static String getChordName(int midi) {
        String nomNota = getNoteName(midi);
        nomNota = "" + (char) (nomNota.charAt(0) + 'A' - 'a') + nomNota.charAt(1);
        return nomNota;
    }

    /**
     * [CA] Retorna el nom de nota per a un valor MIDI donat (notació de do mòbil).
     * <p>
     * [EN] Returns the note name for a given MIDI value (movable-do notation).
     *
     * @param midi [CA] valor MIDI de la nota / [EN] MIDI note value
     * @return [CA] nom de la nota (p.ex. "do4", "la3") / [EN] note name (e.g. "do4", "la3")
     * @throws NumberFormatException [CA] si el MIDI és fora del rang vàlid / [EN] if MIDI is out of valid range
     */
    public static String getNoteName(int midi) {
        // Desplaça per octaves si cal — safety net (ex. mode dodecàfon)
        midi = clampToRange(midi);
        String nomNota = "" + noteNames.get(midi % 12) + (midi / 12 - 1);
        return nomNota;
    }

    /**
     * [CA] Indica si el nom de nota donat és vàlid.
     * <p>
     * [EN] Indicates whether the given note name is valid.
     *
     * @param name [CA] nom de nota a comprovar / [EN] note name to check
     * @return [CA] {@code true} si és un nom vàlid / [EN] {@code true} if it is a valid name
     */
    public static boolean isValidNoteName(String name) {
        return noteNames.containsValue(name);
    }

    /**
     * [CA] Retorna el nom de nota per a un valor MIDI donat, tenint en compte el do mòbil.
     * <p>
     * [EN] Returns the note name for a given MIDI value, taking movable-do into account.
     *
     * @param midi    [CA] valor MIDI de la nota / [EN] MIDI note value
     * @param midiKey [CA] valor MIDI de la tònica (per al do mòbil) / [EN] MIDI value of the tonic (for movable-do)
     * @return [CA] nom de la nota / [EN] note name
     * @throws NumberFormatException [CA] si el MIDI és fora del rang vàlid / [EN] if MIDI is out of valid range
     */
    public static String getNoteName(int midi, int midiKey) {
        // Desplaça per octaves si cal — safety net per a crides que no passen per placeNote
        midi = clampToRange(midi);
        if (movileDo) {
            String nomNota = "" + noteNames.get((midi - midiKey) % 12) + (midi / 12 - 2);
            return nomNota;
        } else {
            return getNoteName(midi);
        }
    }

    /**
     * [CA] Retorna el nom de la tonalitat (2 caràcters) per a un valor MIDI i mode donats.
     * <p>
     * [EN] Returns the key name (2 characters) for a given MIDI value and mode.
     *
     * @param midiKey [CA] valor MIDI de la tònica / [EN] MIDI value of the tonic
     * @param mode    [CA] mode de l'escala ('M' major, 'm' menor) / [EN] scale mode ('M' major, 'm' minor)
     * @return [CA] nom de la tonalitat en 2 caràcters / [EN] 2-character key name
     */
    public static String getKeyName(int midiKey, char mode) {
        String name = getNoteName(midiKey).substring(0, 2);
        if (mode == 'M') {
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
        };
        return name;
    }

    /**
     * [CA] Retorna el nombre de notes del rang MIDI actiu (highestMidi - lowestMidi + 1).
     * <p>
     * [EN] Returns the number of notes in the active MIDI range (highestMidi - lowestMidi + 1).
     *
     * @return [CA] amplada del rang MIDI / [EN] MIDI range width
     */
    public static int getMidiRange() {
        int mr = getHighestMidi() - getLowestMidi() + 1;
        return mr;
    }

    /**
     * [CA] Retorna el valor MIDI mínim del rang actiu.
     * <p>
     * [EN] Returns the minimum MIDI value of the active range.
     *
     * @return [CA] nota MIDI mínima / [EN] minimum MIDI note
     */
    public static int getLowestMidi() {
        return lowestMidi;
    }

    /**
     * [CA] Retorna el nombre d'octaves que el rang actiu és desplaçat cap amunt respecte al rang estàndard.
     * <p>
     * [EN] Returns the number of octaves the active range is shifted up from the standard range.
     *
     * @return [CA] nombre d'octaves cap amunt / [EN] number of octaves up
     */
    public static int getOctavesUp() {
        return octavesUp;
    }

    /**
     * [CA] Retorna el valor MIDI màxim del rang actiu.
     * <p>
     * [EN] Returns the maximum MIDI value of the active range.
     *
     * @return [CA] nota MIDI màxima / [EN] maximum MIDI note
     */
    public static int getHighestMidi() {
        return highestMidi;
    }

    /**
     * [CA] Retorna la nota MIDI mínima del rang "Pau" (rang personal predefinit).
     * <p>
     * [EN] Returns the minimum MIDI note of the "Pau" range (predefined personal range).
     *
     * @return [CA] nota MIDI mínima del rang Pau / [EN] minimum MIDI note of the Pau range
     */
    public static int getLowestPau() {
        return lowestPau;
    }

    /**
     * [CA] Retorna la nota MIDI màxima del rang "Pau".
     * <p>
     * [EN] Returns the maximum MIDI note of the "Pau" range.
     *
     * @return [CA] nota MIDI màxima del rang Pau / [EN] maximum MIDI note of the Pau range
     */
    public static int getHighestPau() {
        return highestPau;
    }

    /**
     * [CA] Retorna la nota MIDI mínima del rang de violí.
     * <p>
     * [EN] Returns the minimum MIDI note of the violin range.
     *
     * @return [CA] nota MIDI mínima del violí / [EN] minimum MIDI note of the violin
     */
    public static int getLowestViolin() {
        return lowestViolin;
    }

    /**
     * [CA] Retorna la nota MIDI màxima del rang de violí.
     * <p>
     * [EN] Returns the maximum MIDI note of the violin range.
     *
     * @return [CA] nota MIDI màxima del violí / [EN] maximum MIDI note of the violin
     */
    public static int getHighestViolin() {
        return highestViolin;
    }

    /**
     * [CA] Retorna la nota MIDI mínima del rang de saxo.
     * <p>
     * [EN] Returns the minimum MIDI note of the saxophone range.
     *
     * @return [CA] nota MIDI mínima del saxo / [EN] minimum MIDI note of the saxophone
     */
    public static int getLowestSaxo() {
        return lowestSaxo;
    }

    /**
     * [CA] Retorna la nota MIDI màxima del rang de saxo.
     * <p>
     * [EN] Returns the maximum MIDI note of the saxophone range.
     *
     * @return [CA] nota MIDI màxima del saxo / [EN] maximum MIDI note of the saxophone
     */
    public static int getHighestSaxo() {
        return highestSaxo;
    }

    /**
     * [CA] Retorna el nom de l'instrument actiu.
     * <p>
     * [EN] Returns the name of the active instrument.
     *
     * @return [CA] nom de l'instrument (p.ex. "midi", "Xylophone") / [EN] instrument name (e.g. "midi", "Xylophone")
     */
    public static String getInstrument() {
        return instrument;
    }

//    public static int getOctava() {
//        return octava;
//    }
//
    /**
     * [CA] Estableix el valor MIDI mínim del rang actiu.
     * <p>
     * [EN] Sets the minimum MIDI value of the active range.
     *
     * @param lowestMidi [CA] nova nota MIDI mínima / [EN] new minimum MIDI note
     */
    public static void setLowestMidi(int lowestMidi) {
        ToneRange.lowestMidi = lowestMidi;
    }

    /**
     * [CA] Estableix el valor MIDI màxim del rang actiu.
     * <p>
     * [EN] Sets the maximum MIDI value of the active range.
     *
     * @param highestMidi [CA] nova nota MIDI màxima / [EN] new maximum MIDI note
     */
    public static void setHighestMidi(int highestMidi) {
        ToneRange.highestMidi = highestMidi;
    }

    /**
     * [CA] Estableix el nombre d'octaves de desplaçament cap amunt.
     * <p>
     * [EN] Sets the number of octaves shifted up.
     *
     * @param octavesUp [CA] nombre d'octaves cap amunt / [EN] number of octaves up
     */
    public static void setOctavesUp(int octavesUp) {
        ToneRange.octavesUp = octavesUp;
    }

    /**
     * [CA] Indica si el mode "do mòbil" està actiu (els noms de nota es calculen relativament a la tònica).
     * <p>
     * [EN] Indicates whether the "movable do" mode is active (note names are calculated relative to the tonic).
     *
     * @return [CA] {@code true} si el do mòbil és actiu / [EN] {@code true} if movable do is active
     */
    public static boolean isMovileDo() {
        return movileDo;
    }

    /**
     * [CA] Estableix el mode "do mòbil".
     * <p>
     * [EN] Sets the "movable do" mode.
     *
     * @param movileDo [CA] {@code true} per activar el do mòbil / [EN] {@code true} to activate movable do
     */
    public static void setMovileDo(boolean movileDo) {
        ToneRange.movileDo = movileDo;
    }

    /**
     * [CA] Estableix el nom de l'instrument actiu.
     * <p>
     * [EN] Sets the name of the active instrument.
     *
     * @param instrument [CA] nom de l'instrument / [EN] instrument name
     */
    public static void setInstrument(String instrument) {
        ToneRange.instrument = instrument;
    }

    /**
     * [CA] Retorna l'índex per a la Key Signature MetaMessage MIDI (valors entre -7 i +7)
     * basat en el valor MIDI de la tònica (midiKey) i el mode.
     * <p>
     * [EN] Returns the index for the MIDI Key Signature MetaMessage (values between -7 and +7)
     * based on the MIDI value of the tonic (midiKey) and the mode.
     *
     * @param midiKey [CA] valor MIDI de la tònica / [EN] MIDI value of the tonic
     * @param mode    [CA] 'M' per major, 'm' per menor / [EN] 'M' for major, 'm' for minor
     * @return [CA] enter entre -7 i +7 segons el nombre d'accidentals / [EN] integer between -7 and +7 according to number of accidentals
     */
    public static int getKeySignatureIndex(int midiKey, char mode) {
        // normalitzem al cicle de 12 notes (Do = 0, Do# = 1, ..., Si = 11)
        int pitchClass = midiKey % 12;
        while (pitchClass < 0) {
            pitchClass += 12;
        }
        //System.out.println("Save pitch = "+pitchClass);

        // valors segons la taula de signatures
        int[] majorKeys = {0, 7, 2, 9, 4, 11, 6, 1, 8, 3, 10, 5}; // C, G, D, A, E, B, F#, Db, Ab, Eb, Bb, F
        int[] majorSignatures = {0, 1, 2, 3, 4, 5, 6, -5, -4, -3, -2, -1};
        int[] minorKeys = {9, 4, 11, 6, 1, 8, 3, 10, 5, 0, 7, 2}; // a, e, b, f#, c#, g#, d#, bb, f, c, g, d
        int[] minorSignatures = {0, 1, 2, 3, 4, 5, 6, -5, -4, -3, -2, -1};

        int[] keySet = (mode == 'M') ? majorKeys : minorKeys;
        int[] sigSet = (mode == 'M') ? majorSignatures : minorSignatures;

        for (int i = 0; i < keySet.length; i++) {
            if (keySet[i] == pitchClass) {
                return sigSet[i];
            }
        }

        // si no es troba, retornem 0 per defecte
        return 0;
    }

    private static void setNoteNames() {
        noteNames.put(0, "do");
        noteNames.put(1, "de");
        noteNames.put(2, "re");
        noteNames.put(3, "ri");
        noteNames.put(4, "mi");
        noteNames.put(5, "fa");
        noteNames.put(6, "fo");
        noteNames.put(7, "so");
        noteNames.put(8, "sa");
        noteNames.put(9, "la");
        noteNames.put(10, "li");
        noteNames.put(11, "ti");
        noteNames.put(-12, "do");
        noteNames.put(-11, "de");
        noteNames.put(-10, "re");
        noteNames.put(-9, "ri");
        noteNames.put(-8, "mi");
        noteNames.put(-7, "fa");
        noteNames.put(-6, "fo");
        noteNames.put(-5, "so");
        noteNames.put(-4, "sa");
        noteNames.put(-3, "la");
        noteNames.put(-2, "li");
        noteNames.put(-1, "ti");
        nameNotes.put("do", MIDDLE_C);
        nameNotes.put("de", MIDDLE_C + 1);
        nameNotes.put("re", MIDDLE_C + 2);
        nameNotes.put("ri", MIDDLE_C + 3);
        nameNotes.put("mi", MIDDLE_C + 4);
        nameNotes.put("fa", MIDDLE_C + 5);
        nameNotes.put("fo", MIDDLE_C + 6);
        nameNotes.put("so", MIDDLE_C + 7);
        nameNotes.put("sa", MIDDLE_C + 8);
        nameNotes.put("la", MIDDLE_C + 9);
        nameNotes.put("li", MIDDLE_C + 10);
        nameNotes.put("ti", MIDDLE_C + 11);
        nameNotes.put("Do", MIDDLE_C);
        nameNotes.put("De", MIDDLE_C + 1);
        nameNotes.put("Re", MIDDLE_C + 2);
        nameNotes.put("Ri", MIDDLE_C + 3);
        nameNotes.put("Mi", MIDDLE_C + 4);
        nameNotes.put("Fa", MIDDLE_C + 5);
        nameNotes.put("Fo", MIDDLE_C + 6);
        nameNotes.put("So", MIDDLE_C + 7);
        nameNotes.put("Sa", MIDDLE_C + 8);
        nameNotes.put("La", MIDDLE_C + 9);
        nameNotes.put("Li", MIDDLE_C + 10);
        nameNotes.put("Ti", MIDDLE_C + 11);
//        nameNotes.put("do",MIDDLE_C-12);
//        nameNotes.put("de",MIDDLE_C-11);
//        nameNotes.put("re",MIDDLE_C-10);
//        nameNotes.put("ri",MIDDLE_C-9);
//        nameNotes.put("mi",MIDDLE_C-8);
//        nameNotes.put("fa",MIDDLE_C-7);
//        nameNotes.put("fo",MIDDLE_C-6);
//        nameNotes.put("so",MIDDLE_C-5);
//        nameNotes.put("sa",MIDDLE_C-4);
//        nameNotes.put("la",MIDDLE_C-3);
//        nameNotes.put("li",MIDDLE_C-2);
//        nameNotes.put("ti",MIDDLE_C-1);
//        nameNotes.put("Do",MIDDLE_C-12);
//        nameNotes.put("De",MIDDLE_C-11);
//        nameNotes.put("Re",MIDDLE_C-10);
//        nameNotes.put("Ri",MIDDLE_C-9);
//        nameNotes.put("Mi",MIDDLE_C-8);
//        nameNotes.put("Fa",MIDDLE_C-7);
//        nameNotes.put("Fo",MIDDLE_C-6);
//        nameNotes.put("So",MIDDLE_C-5);
//        nameNotes.put("Sa",MIDDLE_C-4);
//        nameNotes.put("La",MIDDLE_C-3);
//        nameNotes.put("Li",MIDDLE_C-2);
//        nameNotes.put("Ti",MIDDLE_C-1);
    }

    /**
     * [CA] Retorna el valor MIDI per al nom de tonalitat donat, ajustat al rang actiu.
     * Retorna -1 si el nom no és vàlid.
     * <p>
     * [EN] Returns the MIDI value for the given key name, adjusted to the active range.
     * Returns -1 if the name is invalid.
     *
     * @param keyName [CA] nom de la tonalitat (p.ex. "Do", "la") / [EN] key name (e.g. "Do", "la")
     * @return [CA] valor MIDI ajustat al rang, o -1 si invàlid / [EN] MIDI value adjusted to range, or -1 if invalid
     */
    public static int getMidiKey(String keyName){
        if (keyName == null || !nameNotes.containsKey(keyName)) return -1;
        int midiKey = getMidi(keyName);
        while (midiKey > ToneRange.getHighestMidi()) {
            midiKey -= 12;
        }
        while (midiKey < ToneRange.getLowestMidi()) {
            midiKey += 12;
        }
        return midiKey;
    }

    /**
     * [CA] Retorna el mode de l'escala a partir del nom de la tonalitat
     * (majúscula = 'M', minúscula = 'm', invàlid = ' ').
     * <p>
     * [EN] Returns the scale mode from the key name
     * (uppercase = 'M', lowercase = 'm', invalid = ' ').
     *
     * @param keyName [CA] nom de la tonalitat / [EN] key name
     * @return [CA] caràcter del mode ('M', 'm' o ' ') / [EN] mode character ('M', 'm' or ' ')
     */
    public static char getScaleMode(String keyName) {
        if (keyName == null || keyName.isEmpty()) {
            return ' ';
        }
        char c = keyName.charAt(0);
        if (!Character.isLetter(c)) {
            return ' ';
        }
        if (Character.isUpperCase(c)) {
            return 'M';
        } else {
            return 'm';
        }
    }

    /**
     * [CA] Converteix un valor MIDI en l'identificador de tecla del grid (fila des de dalt).
     * Ajusta el MIDI al rang actiu si cal.
     * <p>
     * [EN] Converts a MIDI value to the grid key identifier (row from top).
     * Adjusts the MIDI to the active range if necessary.
     *
     * @param midi [CA] valor MIDI de la nota / [EN] MIDI note value
     * @return [CA] identificador de tecla al grid / [EN] grid key identifier
     */
    public static int midiToKeyId(int midi) {
//        midi = midi+12*ToneRange.getOctavesUp();
        while (midi < getLowestMidi()) {
            midi += 12;
        }
        while (midi > getHighestMidi()) {
            midi -= 12;
        }
        return highestMidi - midi;
    }

    /**
     * [CA] Converteix un identificador de tecla del grid en valor MIDI.
     * <p>
     * [EN] Converts a grid key identifier to a MIDI value.
     *
     * @param keyId [CA] identificador de tecla al grid / [EN] grid key identifier
     * @return [CA] valor MIDI corresponent / [EN] corresponding MIDI value
     */
    public static int keyIdToMidi(int keyId) {
        return ToneRange.getHighestMidi() - keyId;
    }

    /**
     * [CA] Ajusta el MIDI al rang de saxo superior (entre 54 i 65).
     * <p>
     * [EN] Adjusts the MIDI to the upper saxophone range (between 54 and 65).
     *
     * @param midi [CA] valor MIDI d'entrada / [EN] input MIDI value
     * @return [CA] valor MIDI ajustat al rang de saxo superior / [EN] MIDI value adjusted to upper saxophone range
     */
    public static int upperSaxOctave(int midi) {
        if (midi < 54) {
            midi += 12;
        }
        if (midi > 65) {
            midi -= 12;
        }
        return midi;
    }

    /**
     * [CA] Ajusta el MIDI al rang de saxo inferior (entre 51 i 63).
     * <p>
     * [EN] Adjusts the MIDI to the lower saxophone range (between 51 and 63).
     *
     * @param midi [CA] valor MIDI d'entrada / [EN] input MIDI value
     * @return [CA] valor MIDI ajustat al rang de saxo inferior / [EN] MIDI value adjusted to lower saxophone range
     */
    /**
     * [CA] Desplaça el valor MIDI per octaves fins que queda dins del rang actual
     * [{@code lowestMidi}, {@code highestMidi}]. Si el rang és massa estret per a
     * cap octava vàlida, retorna el valor sense modificar.
     * <p>
     * [EN] Shifts the MIDI value by octaves until it falls within the current range
     * [{@code lowestMidi}, {@code highestMidi}]. If the range is too narrow for any
     * valid octave, returns the value unchanged.
     *
     * @param midi [CA] valor MIDI d'entrada / [EN] input MIDI value
     * @return [CA] valor MIDI ajustat al rang actual / [EN] MIDI value adjusted to current range
     */
    public static int clampToRange(int midi) {
        int lo = getLowestMidi();
        int hi = getHighestMidi();
        int limit = 0;
        while (midi > hi && limit++ < 10) midi -= 12;
        while (midi < lo && limit++ < 10) midi += 12;
        return midi;
    }

    public static int lowerSaxOctave(int midi) {
        if (midi < 51) {
            midi += 12;
        }
        if (midi > 63) {
            midi -= 12;
        }
        return midi;
    }

//    private static void setFilenames() {
//        if (instrument.equals("Xylophone")) { ... }
//    }

    // GM drum map: index = midi note, [0] = short label (≤5 chars), [1] = full name
    private static final String[][] DRUM_NAMES = new String[128][2];
    static {
        DRUM_NAMES[35] = new String[]{"BsDr2", "Bass Drum 2"};
        DRUM_NAMES[36] = new String[]{"BsDr1", "Bass Drum 1"};
        DRUM_NAMES[37] = new String[]{"SdStk", "Side Stick"};
        DRUM_NAMES[38] = new String[]{"AcSnr", "Acoustic Snare"};
        DRUM_NAMES[39] = new String[]{"Clap",  "Hand Clap"};
        DRUM_NAMES[40] = new String[]{"ElSnr", "Electric Snare"};
        DRUM_NAMES[41] = new String[]{"LFTom", "Low Floor Tom"};
        DRUM_NAMES[42] = new String[]{"ClHH",  "Closed Hi-Hat"};
        DRUM_NAMES[43] = new String[]{"HFTom", "High Floor Tom"};
        DRUM_NAMES[44] = new String[]{"PdHH",  "Pedal Hi-Hat"};
        DRUM_NAMES[45] = new String[]{"LoTom", "Low Tom"};
        DRUM_NAMES[46] = new String[]{"OpHH",  "Open Hi-Hat"};
        DRUM_NAMES[47] = new String[]{"LMTom", "Low-Mid Tom"};
        DRUM_NAMES[48] = new String[]{"HMTom", "Hi-Mid Tom"};
        DRUM_NAMES[49] = new String[]{"Crsh1", "Crash Cymbal 1"};
        DRUM_NAMES[50] = new String[]{"HiTom", "High Tom"};
        DRUM_NAMES[51] = new String[]{"Ride1", "Ride Cymbal 1"};
        DRUM_NAMES[52] = new String[]{"China", "Chinese Cymbal"};
        DRUM_NAMES[53] = new String[]{"RdBel", "Ride Bell"};
        DRUM_NAMES[54] = new String[]{"Tamb",  "Tambourine"};
        DRUM_NAMES[55] = new String[]{"Splsh", "Splash Cymbal"};
        DRUM_NAMES[56] = new String[]{"Cowbl", "Cowbell"};
        DRUM_NAMES[57] = new String[]{"Crsh2", "Crash Cymbal 2"};
        DRUM_NAMES[58] = new String[]{"Vibsl", "Vibraslap"};
        DRUM_NAMES[59] = new String[]{"Ride2", "Ride Cymbal 2"};
        DRUM_NAMES[60] = new String[]{"HiBng", "Hi Bongo"};
        DRUM_NAMES[61] = new String[]{"LoBng", "Lo Bongo"};
        DRUM_NAMES[62] = new String[]{"MuCng", "Mute Hi Conga"};
        DRUM_NAMES[63] = new String[]{"OpCng", "Open Hi Conga"};
        DRUM_NAMES[64] = new String[]{"LoCng", "Lo Conga"};
        DRUM_NAMES[65] = new String[]{"HiTmb", "Hi Timbale"};
        DRUM_NAMES[66] = new String[]{"LoTmb", "Lo Timbale"};
        DRUM_NAMES[67] = new String[]{"HiAgo", "Hi Agogo"};
        DRUM_NAMES[68] = new String[]{"LoAgo", "Lo Agogo"};
        DRUM_NAMES[69] = new String[]{"Cabas", "Cabasa"};
        DRUM_NAMES[70] = new String[]{"Marcs", "Maracas"};
        DRUM_NAMES[71] = new String[]{"ShWhi", "Short Whistle"};
        DRUM_NAMES[72] = new String[]{"LgWhi", "Long Whistle"};
        DRUM_NAMES[73] = new String[]{"ShGur", "Short Guiro"};
        DRUM_NAMES[74] = new String[]{"LgGur", "Long Guiro"};
        DRUM_NAMES[75] = new String[]{"Clvs",  "Claves"};
        DRUM_NAMES[76] = new String[]{"HiWd",  "Hi Wood Block"};
        DRUM_NAMES[77] = new String[]{"LoWd",  "Lo Wood Block"};
        DRUM_NAMES[78] = new String[]{"MuCui", "Mute Cuica"};
        DRUM_NAMES[79] = new String[]{"OpCui", "Open Cuica"};
        DRUM_NAMES[80] = new String[]{"MuTri", "Mute Triangle"};
        DRUM_NAMES[81] = new String[]{"OpTri", "Open Triangle"};
    }

    /**
     * [CA] Retorna l'etiqueta curta (màx. 5 caràcters) del nom de percussió GM per a un MIDI donat.
     * <p>
     * [EN] Returns the short label (max. 5 characters) of the GM drum name for a given MIDI.
     *
     * @param midi [CA] valor MIDI del cop de percussió (35-81) / [EN] MIDI value of the drum hit (35-81)
     * @return [CA] etiqueta curta, o "?" si fora de rang / [EN] short label, or "?" if out of range
     */
    public static String getDrumShortName(int midi) {
        if (midi < 35 || midi > 81 || DRUM_NAMES[midi] == null) return "?";
        return DRUM_NAMES[midi][0];
    }

    /**
     * [CA] Retorna el nom complet del cop de percussió GM per a un MIDI donat.
     * <p>
     * [EN] Returns the full name of the GM drum hit for a given MIDI.
     *
     * @param midi [CA] valor MIDI del cop de percussió (35-81) / [EN] MIDI value of the drum hit (35-81)
     * @return [CA] nom complet, o "?" si fora de rang / [EN] full name, or "?" if out of range
     */
    public static String getDrumFullName(int midi) {
        if (midi < 35 || midi > 81 || DRUM_NAMES[midi] == null) return "?";
        return DRUM_NAMES[midi][1];
    }

    /**
     * [CA] Retorna el MIDI de percussió per a un keyId 0..46 (81 → 35); -1 si fora de rang.
     * <p>
     * [EN] Returns the drum MIDI for keyId 0..46 (81 → 35); -1 if out of range.
     *
     * @param keyId [CA] identificador de tecla al grid (0-46) / [EN] grid key identifier (0-46)
     * @return [CA] valor MIDI de percussió, o -1 si fora de rang / [EN] drum MIDI value, or -1 if out of range
     */
    public static int getDrumMidi(int keyId) {
        int midi = 81 - keyId;
        return (midi >= 35) ? midi : -1;
    }
}
