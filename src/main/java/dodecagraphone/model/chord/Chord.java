package dodecagraphone.model.chord;

import dodecagraphone.model.ToneRange;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author grogm
 */
public class Chord {

    public static final int NULL_BASS = -31;
    protected int root; // relative to midiKey
    protected int midiKey;
    protected int[] shape; // relative to root
    protected String info;
    protected int bass; // the slash bass note of the chord, relative to root
    protected int ncols = 1;

    public Chord() {
        this.root = Settings.INVALID_CHORD;
        this.midiKey = -1;
        this.shape = null;
        this.info = "";
        this.bass = NULL_BASS;
    }

    public Chord(int root, int[] shape, int midiKey, String info) {
        this.root = root;
        this.midiKey = midiKey;
        this.shape = shape;
        this.info = info;
        this.bass = NULL_BASS;
    }

    public Chord(int root, int[] shape, int midiKey, String info, int bass) {
        this.root = root;
        this.midiKey = midiKey;
        this.shape = shape;
        this.info = info;
        this.bass = bass;
    }

    public Chord(String chordString) {
        boolean VERBOSE = true;
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
            // Separem info addicional (després d’un espai)
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

                // El rootToken pot tenir un sufix, l’hem de treure per calcular bé el root
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

    public boolean isValidChord(){
        return (root!=Settings.INVALID_CHORD);
    }

    public int getNCols() {
        return ncols;
    }

    public void setNCols(int ncols) {
        this.ncols = ncols;
    }
    
    public int getMidiKey() {
        return midiKey;
    }

    public int getBass() {
        return bass;
    }

    public int getRoot() {
        return root;
    }

    public int getMidiRoot() {
        return root + midiKey;
    }

    public int getMidiBass() {
        return root + midiKey + bass;
    }

    public Chord cloneChord() {
        Chord ch = new Chord();
        if (this.shape != null) {
            ch.root = this.root;
            ch.midiKey = this.midiKey;
            ch.shape = this.shape.clone();
            ch.info = new String(this.info);
            ch.bass = this.bass;
        }
        return ch;
    }

    public Chord transpose(int step) {
        Chord newch = cloneChord();
        if (newch.midiKey != -1) {
            newch.midiKey += step;
        }
        return newch;
    }

    public void setBass(int bass) {
        this.bass = bass;
    }

    public int[] getShape() {
        return shape;
    }

    public List<Integer> getMidiShapeAsList() {
        List<Integer> list = new ArrayList<>();
        for (int note : shape) {
            list.add(note + root);
        }
        return list;
    }

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

    public String getInfo() {
        return info;
    }

    public String getChordRootName() {
        int midiRoot = root + midiKey;
        String message = ToneRange.getNoteName(midiRoot, midiKey);
        message = message.substring(0, 2);
        if (shape[1] - shape[0] == 4) {
            message = Utilities.capitalize(message);
        }
//        switch (root){
//            // Major
//            case 0:
//            case 12:
//            case -12:
//            case 5:
//            case -7:
//            case 7:
//            case -5:
//                message = Utilities.capitalize(message);
//                break;
//            // dim
//            case 11:
//            case -1:
//            // minor
//            case 2:
//            case -10:
//            case 4:
//            case -8:
//            case 9:
//            case -3: 
//                break;
//            default:
//                throw new UnsupportedOperationException("No such Chord (root)%12 = "+(root)%12);
//        }
        return message;
    }

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

    public String getSimbolWithNumericBass() {
        String message = getChordRootName();
        if (this.bass != NULL_BASS) {
            message += "/" + (bass);
        }
        return message;
    }

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
