package dodecagraphone.model;

import dodecagraphone.ui.AppConfig;
import dodecagraphone.ui.Settings;
import java.util.HashMap;

/**
 *
 * @author upcnet
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
            octavesUp = 2;
        }
        MIDDLE_C = 60 + 12 * octavesUp;
        DEFAULT_KEY = MIDDLE_C;
        lowestPau = MIDDLE_C - 20;
        highestPau = MIDDLE_C + 4;
        lowestSaxo = MIDDLE_C - 11; //49;
        highestSaxo = MIDDLE_C + 21; //81;
        lowestViolin = MIDDLE_C - 5;
        highestViolin = MIDDLE_C + 45;
        setNoteNames();
    }

    public static char getDefaultMode(){
        return DEFAULT_MODE;
    }

    public static int getDefaultKey(){
        return DEFAULT_KEY;
    }


    public static boolean isMetallophone() {
        return isMetallophone;
    }

    public static void setMetallophone(boolean v) {
        ToneRange.isMetallophone = v;
    }

    public static int getMaxNKeys(){
        return ToneRange.MAX_NKEYS; 
    }
    
    public static String getFilename(int midi) {
        return getFilename(midi, instrument);
    }

    public static String getFilename(int midi, String instrument) {
        if (midi >= lowestMidi && midi <= highestMidi) {
            String fileN = instrument + "_" + getNoteName(midi) + ".wav";
            return path + fileN;
        }
        return "";
    }

    public static int getMidi(String noteName) {
        int midi = nameNotes.get(noteName);
        return midi;
    }

    public static String getChordName(int midi) {
        String nomNota = getNoteName(midi);
        nomNota = "" + (char) (nomNota.charAt(0) + 'A' - 'a') + nomNota.charAt(1);
        return nomNota;
    }

    public static String getNoteName(int midi) {
        if (midi > getHighestMidi() || midi < getLowestMidi()) {
            throw new NumberFormatException("Midi " + midi + " out of range!");
        }
//        while (midi>highestMidi) midi-=12;
//        while (midi<lowestMidi) midi+=12;
        String nomNota = "" + noteNames.get(midi % 12) + (midi / 12 - 1);
        return nomNota;
    }

    public static boolean isValidNoteName(String name) {
        return noteNames.containsValue(name);
    }

    public static String getNoteName(int midi, int midiKey) {
        if (midi > getHighestMidi() || midi < getLowestMidi()) {
            throw new NumberFormatException("Midi " + midi + " out of range!");
        }
//        while (midi>highestMidi) midi-=12;
//        while (midi<lowestMidi) midi+=12;
        if (movileDo) {
            String nomNota = "" + noteNames.get((midi - midiKey) % 12) + (midi / 12 - 2);
            return nomNota;
        } else {
            return getNoteName(midi);
        }
    }

    public static String getKeyName(int midiKey, char mode) {
        String name = getNoteName(midiKey).substring(0, 2);
        if (mode == 'M') {
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
        };
        return name;
    }

    public static int getMidiRange() {
        int mr = getHighestMidi() - getLowestMidi() + 1;
        return mr;
    }

    public static int getLowestMidi() {
        return lowestMidi;
    }

    public static int getOctavesUp() {
        return octavesUp;
    }

    public static int getHighestMidi() {
        return highestMidi;
    }

    public static int getLowestPau() {
        return lowestPau;
    }

    public static int getHighestPau() {
        return highestPau;
    }

    public static int getLowestViolin() {
        return lowestViolin;
    }

    public static int getHighestViolin() {
        return highestViolin;
    }

    public static int getLowestSaxo() {
        return lowestSaxo;
    }

    public static int getHighestSaxo() {
        return highestSaxo;
    }

    public static String getInstrument() {
        return instrument;
    }

//    public static int getOctava() {
//        return octava;
//    }
//
    public static void setLowestMidi(int lowestMidi) {
        ToneRange.lowestMidi = lowestMidi;
    }

    public static void setHighestMidi(int highestMidi) {
        ToneRange.highestMidi = highestMidi;
    }

    public static void setOctavesUp(int octavesUp) {
        ToneRange.octavesUp = octavesUp;
    }

    public static boolean isMovileDo() {
        return movileDo;
    }

    public static void setMovileDo(boolean movileDo) {
        ToneRange.movileDo = movileDo;
    }

    public static void setInstrument(String instrument) {
        ToneRange.instrument = instrument;
    }

    /**
     * Retorna l'índex per la Key Signature MetaMessage (valors entre -7 i +7)
     * basat en el valor MIDI de la tònica (midiKey) i el mode.
     *
     * @param midiKey valor MIDI de la tònica
     * @param mode 'M' per major, 'm' per menor
     * @return enter entre -7 i +7 segons el nombre d'accidentals
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

    public static int keyIdToMidi(int keyId) {
        return ToneRange.getHighestMidi() - keyId;
    }

    public static int upperSaxOctave(int midi) {
        if (midi < 54) {
            midi += 12;
        }
        if (midi > 65) {
            midi -= 12;
        }
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
//        if (instrument.equals("Xylophone")) {
//            filenames.put(69, "Xylophone_la3.wav");
//            filenames.put(70, "Xylophone_li3.wav");
//            filenames.put(71, "Xylophone_ti3.wav");
//            filenames.put(72, "Xylophone_do4.wav");
//            filenames.put(73, "Xylophone_de4.wav");
//            filenames.put(74, "Xylophone_re4.wav");
//            filenames.put(75, "Xylophone_ri4.wav");
//            filenames.put(76, "Xylophone_mi4.wav");
//            filenames.put(77, "Xylophone_fa4.wav");
//            filenames.put(78, "Xylophone_fo4.wav");
//            filenames.put(79, "Xylophone_so4.wav");
//            filenames.put(80, "Xylophone_sa4.wav");
//            filenames.put(81, "Xylophone_la4.wav");
//            filenames.put(82, "Xylophone_li4.wav");
//            filenames.put(83, "Xylophone_ti4.wav");
//            filenames.put(84, "Xylophone_do5.wav");
//            filenames.put(85, "Xylophone_de5.wav");
//            filenames.put(86, "Xylophone_re5.wav");
//            filenames.put(87, "Xylophone_ri5.wav");
//            filenames.put(88, "Xylophone_mi5.wav");
//            filenames.put(89, "Xylophone_fa5.wav");
//            filenames.put(90, "Xylophone_fo5.wav");
//            filenames.put(91, "Xylophone_so5.wav");
//            filenames.put(92, "Xylophone_sa5.wav");
//            filenames.put(93, "Xylophone_la5.wav");
//        } else if (instrument.equals("BflatClarinet")) {
//            filenames.put(50, "BflatClarinet_re2.wav");
//            filenames.put(51, "BflatClarinet_ri2.wav");
//            filenames.put(52, "BflatClarinet_mi2.wav");
//            filenames.put(53, "BflatClarinet_fa2.wav");
//            filenames.put(54, "BflatClarinet_fo2.wav");
//            filenames.put(55, "BflatClarinet_so2.wav");
//            filenames.put(56, "BflatClarinet_sa2.wav");
//            filenames.put(57, "BflatClarinet_la2.wav");
//            filenames.put(58, "BflatClarinet_li2.wav");
//            filenames.put(59, "BflatClarinet_ti2.wav");
//            filenames.put(60, "BflatClarinet_do3.wav");
//            filenames.put(61, "BflatClarinet_de3.wav");
//            filenames.put(62, "BflatClarinet_re3.wav");
//            filenames.put(63, "BflatClarinet_ri3.wav");
//            filenames.put(64, "BflatClarinet_mi3.wav");
//            filenames.put(65, "BflatClarinet_fa3.wav");
//            filenames.put(66, "BflatClarinet_fo3.wav");
//            filenames.put(67, "BflatClarinet_so3.wav");
//            filenames.put(68, "BflatClarinet_sa3.wav");
//            filenames.put(69, "BflatClarinet_la3.wav");
//            filenames.put(70, "BflatClarinet_li3.wav");
//            filenames.put(71, "BflatClarinet_ti3.wav");
//            filenames.put(72, "BflatClarinet_do4.wav");
//            filenames.put(73, "BflatClarinet_de4.wav");
//            filenames.put(74, "BflatClarinet_re4.wav");
//            filenames.put(75, "BflatClarinet_ri4.wav");
//            filenames.put(76, "BflatClarinet_mi4.wav");
//            filenames.put(77, "BflatClarinet_fa4.wav");
//            filenames.put(78, "BflatClarinet_fo4.wav");
//            filenames.put(79, "BflatClarinet_so4.wav");
//            filenames.put(80, "BflatClarinet_sa4.wav");
//            filenames.put(81, "BflatClarinet_la4.wav");
//            filenames.put(82, "BflatClarinet_li4.wav");
//            filenames.put(83, "BflatClarinet_ti4.wav");
//            filenames.put(84, "BflatClarinet_do5.wav");
//            filenames.put(85, "BflatClarinet_de5.wav");
//            filenames.put(86, "BflatClarinet_re5.wav");
//        }
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

    public static String getDrumShortName(int midi) {
        if (midi < 35 || midi > 81 || DRUM_NAMES[midi] == null) return "?";
        return DRUM_NAMES[midi][0];
    }

    public static String getDrumFullName(int midi) {
        if (midi < 35 || midi > 81 || DRUM_NAMES[midi] == null) return "?";
        return DRUM_NAMES[midi][1];
    }
}
