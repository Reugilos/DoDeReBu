/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.chord;

import dodecagraphone.model.ToneRange;
import dodecagraphone.ui.Utilities;

/**
 * [CA] Tríada musical diatònica (acord de tres notes: fonamental, tercera i quinta).
 * Estén {@link Chord} i genera automàticament la forma de l'acord (major, menor
 * o disminuïda) a partir del grau de l'escala i la inversió indicats.
 * Suporta les inversions estàndard (estat fonamental, primera inversió, segona inversió)
 * i una inversió aleatòria ({@link #RANDOM_INVERSION}).
 * <p>
 * El mètode {@link #toString()} retorna una representació detallada de la tríada
 * que inclou: grau romà, nom de la nota arrel, mode, forma de l'acord i noms
 * de les notes MIDI, i tipus d'inversió.
 * <p>
 * [EN] Diatonic musical triad (three-note chord: root, third and fifth). Extends
 * {@link Chord} and automatically generates the chord shape (major, minor or
 * diminished) from the given scale degree and inversion. Supports standard
 * inversions (root position, first inversion, second inversion) and a random
 * inversion ({@link #RANDOM_INVERSION}).
 * <p>
 * The {@link #toString()} method returns a detailed representation of the triad
 * including: Roman numeral, root note name, mode, chord shape and MIDI note names,
 * and inversion type.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 * @see Chord
 * @see DiatonicChordProgression
 */
public class Triad extends Chord{
    /** [CA] Constant per a estat fonamental (arrel al baix). [EN] Constant for root position (root in the bass). */
    public static final int ROOT_POSITION = 0;
    /** [CA] Constant per a primera inversió (tercera al baix). [EN] Constant for first inversion (third in the bass). */
    public static final int FIRST_INVERSION = 1;
    /** [CA] Constant per a segona inversió (quinta al baix). [EN] Constant for second inversion (fifth in the bass). */
    public static final int SECOND_INVERSION = 2;
    /** [CA] Constant per a inversió aleatòria (escollida a l'atzar entre les tres). [EN] Constant for random inversion (randomly chosen from the three). */
    public static final int RANDOM_INVERSION = 13;

    private int inversion;
    private char mode;

    /**
     * [CA] Crea una tríada buida sense nota arrel ni tonalitat assignades.
     * Ús intern per a la deserialització o còpia.
     * <p>
     * [EN] Creates an empty triad without assigned root note or key.
     * Used internally for deserialization or copying.
     */
    public Triad(){
        super();
        this.inversion = -1;
        this.mode = ' ';
        this.midiKey = -1;
    }

    /**
     * [CA] Crea una tríada diatònica a partir del grau de l'escala, la tonalitat
     * i la inversió indicats. Calcula automàticament la forma de l'acord (major,
     * menor o disminuïda) i el mode corresponents al grau.
     * <p>
     * [EN] Creates a diatonic triad from the given scale degree, key and inversion.
     * Automatically computes the chord shape (major, minor or diminished) and
     * the mode corresponding to the degree.
     *
     * @param root      [CA] grau de l'escala en semitones relatius a la tònica /
     *                  [EN] scale degree in semitones relative to the tonic
     * @param midiKey   [CA] nota MIDI de la tònica / [EN] MIDI note of the tonic
     * @param inversion [CA] inversió de l'acord ({@link #ROOT_POSITION}, {@link #FIRST_INVERSION},
     *                  {@link #SECOND_INVERSION} o {@link #RANDOM_INVERSION}) /
     *                  [EN] chord inversion ({@link #ROOT_POSITION}, {@link #FIRST_INVERSION},
     *                  {@link #SECOND_INVERSION} or {@link #RANDOM_INVERSION})
     */
    public Triad(int root, int midiKey, int inversion){
        super(root,null,midiKey,"");
//        this.midiKey = midiKey;
        diatonicTriadWhichPosition(inversion); // updates this.mode
    }

//    public String toString(boolean movileDo){
//        ToneRange.setMovileDo(movileDo);
//        return this.toString();
//    }
//
    /**
     * [CA] Retorna la inversió actual de la tríada.
     * <p>
     * [EN] Returns the current inversion of the triad.
     *
     * @return [CA] índex d'inversió ({@link #ROOT_POSITION}, {@link #FIRST_INVERSION} o {@link #SECOND_INVERSION}) /
     *         [EN] inversion index ({@link #ROOT_POSITION}, {@link #FIRST_INVERSION} or {@link #SECOND_INVERSION})
     */
    public int getInversion() {
        return inversion;
    }

    /**
     * [CA] Estableix la inversió de la tríada.
     * <p>
     * [EN] Sets the inversion of the triad.
     *
     * @param inversion [CA] índex d'inversió / [EN] inversion index
     */
    public void setInversion(int inversion) {
        this.inversion = inversion;
    }

    /**
     * [CA] Retorna el mode de la tríada: {@code 'M'} (major), {@code 'm'} (menor)
     * o {@code 'd'} (disminuïda).
     * <p>
     * [EN] Returns the mode of the triad: {@code 'M'} (major), {@code 'm'} (minor)
     * or {@code 'd'} (diminished).
     *
     * @return [CA] caràcter del mode / [EN] mode character
     */
    public char getMode() {
        return mode;
    }

    /**
     * [CA] Estableix el mode de la tríada.
     * <p>
     * [EN] Sets the mode of the triad.
     *
     * @param mode [CA] caràcter del mode ({@code 'M'}, {@code 'm'} o {@code 'd'}) /
     *             [EN] mode character ({@code 'M'}, {@code 'm'} or {@code 'd'})
     */
    public void setMode(char mode) {
        this.mode = mode;
    }

    /**
     * [CA] Retorna la nota MIDI de la tònica.
     * <p>
     * [EN] Returns the MIDI note of the tonic.
     *
     * @return [CA] nota MIDI de la tònica / [EN] MIDI note of the tonic
     */
    public int getMidiKey() {
        return midiKey;
    }

    /**
     * [CA] Estableix la nota MIDI de la tònica.
     * <p>
     * [EN] Sets the MIDI note of the tonic.
     *
     * @param midiKey [CA] nota MIDI de la tònica / [EN] MIDI note of the tonic
     */
    public void setMidiKey(int midiKey) {
        this.midiKey = midiKey;
    }

    /**
     * [CA] Retorna una representació textual detallada de la tríada que inclou:
     * grau romà de l'escala, nom de la nota arrel, mode (M/m/d), forma de l'acord
     * (intervals en semitones), notes MIDI per nom i tipus d'inversió (Root position /
     * First inversion / Second inversion).
     * <p>
     * [EN] Returns a detailed textual representation of the triad including:
     * Roman numeral scale degree, root note name, mode (M/m/d), chord shape
     * (intervals in semitones), MIDI note names and inversion type (Root position /
     * First inversion / Second inversion).
     *
     * @return [CA] cadena de text descriptiva de la tríada / [EN] descriptive text string of the triad
     * @throws UnsupportedOperationException [CA] si el grau de l'arrel no és un grau diatònic vàlid /
     *                                       [EN] if the root degree is not a valid diatonic degree
     */
    @Override
    public String toString(){
        String message = ToneRange.getNoteName(root+midiKey,midiKey);
        message = message.substring(0,2);
        switch ((root)%12){
            // Major
            case 0:
            case 12:
            case -12:
                message = Utilities.capitalize(message);
                message = "I   "+message;
                message = message + mode;
                break;
            case 5:
            case -7:
                message = Utilities.capitalize(message);
                message = "IV  "+message;
                message = message + mode;
                break;
            case 7:
            case -5:
                message = Utilities.capitalize(message);
                message = "V   "+message;
                message = message + mode;
                break;
            // dim
            case 11:
            case -1:
                message = "vii "+message;
                message = message + mode;
                break;
            // minor
            case 2:
            case -10:
                message = "ii  "+message;
                message = message + "m";
                break;
            case 4:
            case -8:
                message = "iii "+message;
                message = message + mode;
                break;
            case 9:
            case -3:
                message = "vi  "+message;
                message = message + mode;
                break;
            default:
                throw new UnsupportedOperationException("No such Chord (root)%12 = "+(root)%12);
        }
        message += " [";
        for (int pos:shape){
            if (pos<0) pos+=12;
            message += pos + ",";
        }
        message = message.substring(0,message.length()-1);
        message += "]";
        if (this.bass!=NULL_BASS){
            message += "/" + (bass);
        }
        message += " ";
        message += "[";
        for (int note:this.getMidiNotes()){
            String noteName = ToneRange.getNoteName(note,midiKey);
            if (note==root+midiKey) noteName = Utilities.capitalize(noteName);
            noteName = noteName.substring(0,noteName.length()-1);
            message += noteName + ",";
        }
        message = message.substring(0,message.length()-1);
        message += "]";
        if (this.bass!=NULL_BASS){
            String noteName = ToneRange.getNoteName(bass+midiKey,midiKey);
            if (bass==root) noteName = Utilities.capitalize(noteName);
            noteName = noteName.substring(0,noteName.length()-1);
            message += "/" + noteName;
        }
        message += " ";
        switch (inversion){
            case (ROOT_POSITION):
                info = "Root position";
                break;
            case (FIRST_INVERSION):
                info = "First inversion";
                break;
            case (SECOND_INVERSION):
                info = "Second inversion";
                break;
        }
        message += info;
        return message;
    }

    /**
     * [CA] Despachador intern que selecciona el mètode d'inicialització de la forma
     * de l'acord segons la inversió indicada. En el cas de {@link #RANDOM_INVERSION},
     * escull aleatòriament entre les tres inversions estàndard.
     * <p>
     * [EN] Internal dispatcher that selects the chord shape initialization method
     * according to the given inversion. For {@link #RANDOM_INVERSION}, randomly
     * selects among the three standard inversions.
     *
     * @param whichInversion [CA] constant d'inversió / [EN] inversion constant
     * @throws NumberFormatException [CA] si la constant d'inversió no és vàlida /
     *                               [EN] if the inversion constant is not valid
     */
    private void diatonicTriadWhichPosition(int whichInversion) {
        switch (whichInversion){
            case RANDOM_INVERSION:
                diatonicTriadRandomPosition();
                break;
            case ROOT_POSITION:
                diatonicTriadRootPosition();
                break;
            case FIRST_INVERSION:
                diatonicTriadFirstInversion();
                break;
            case SECOND_INVERSION:
                diatonicTriadSecondInversion();
                break;
            default:
                throw new NumberFormatException("Wrong inversion number "+whichInversion);
        }
    }

    /**
     * [CA] Selecciona aleatòriament una de les tres inversions estàndard i
     * inicialitza la forma de l'acord en conseqüència.
     * <p>
     * [EN] Randomly selects one of the three standard inversions and
     * initializes the chord shape accordingly.
     */
    private void diatonicTriadRandomPosition() {
        int which = Utilities.getRand().nextInt(3);
        switch (which){
            case ROOT_POSITION:
                diatonicTriadRootPosition();
                break;
            case FIRST_INVERSION:
                diatonicTriadFirstInversion();
                break;
            case SECOND_INVERSION:
                diatonicTriadSecondInversion();
                break;
        }
    }

    /**
     * [CA] Inicialitza la tríada en estat fonamental (arrel al baix). La forma
     * de l'acord (major, menor o disminuïda) es determina automàticament a partir
     * del grau de l'arrel en l'escala diatònica major.
     * <p>
     * [EN] Initializes the triad in root position (root in the bass). The chord
     * shape (major, minor or diminished) is automatically determined from the
     * root degree in the diatonic major scale.
     *
     * @throws NumberFormatException [CA] si el grau de l'arrel no és un grau diatònic vàlid /
     *                               [EN] if the root degree is not a valid diatonic degree
     */
    public void diatonicTriadRootPosition() {
        switch ((root) % 12) {
            case 0: // do
            case 5: // fa
            case 7: // sol
            case -7: // fa
            case -5: // sol
                shape = new int[]{0, 4, 7};
                mode ='M';
                break;
            case 2: // re
            case 4: // mi
            case 9: // la
            case -10: // re
            case -8: // mi
            case -3: // la
                shape = new int[]{0, 3, 7};
                mode = 'm';
                break;
            case 11: // si
            case -1: // si
                shape = new int[]{0, 3, 6};
                mode = 'd';
                break;
            default:
                throw new NumberFormatException("Wrong root or key");
        }
        inversion = ROOT_POSITION;
    }

    /**
     * [CA] Inicialitza la tríada en primera inversió (tercera al baix). La forma
     * de l'acord s'adapta automàticament al grau de l'escala diatònica major.
     * <p>
     * [EN] Initializes the triad in first inversion (third in the bass). The chord
     * shape is automatically adapted to the diatonic major scale degree.
     */
    private void diatonicTriadFirstInversion() {
        switch ((root) % 12) {
            case 0: // do
            case 5: // fa
            case 7: // sol
            case -7: // fa
            case -5: // sol
                shape = new int[]{-8, -5, 0};
//              shape = new int[]{4, 7, 12};
                mode = 'M';
                break;
            case 2: // re
            case 4: // mi
            case 9: // la
            case -10: // re
            case -8: // mi
            case -3: // la
//            shape = new int[]{3, 7, 12};
              shape = new int[]{-9, -5, 0};
              mode = 'm';
              break;
            case 11: // si
            case -1: // si
//            shape = new int[]{3, 6, 12};
              shape = new int[]{-9, -6, 0};
              mode = 'd';
              break;
        }
        inversion = FIRST_INVERSION;
    }

    /**
     * [CA] Inicialitza la tríada en segona inversió (quinta al baix). La forma
     * de l'acord s'adapta automàticament al grau de l'escala diatònica major.
     * <p>
     * [EN] Initializes the triad in second inversion (fifth in the bass). The chord
     * shape is automatically adapted to the diatonic major scale degree.
     */
    private void diatonicTriadSecondInversion() {
        switch ((root) % 12) {
            case 0: // do
            case 5: // fa
            case 7: // sol
            case -7: // fa
            case -5: // sol
                shape = new int[]{-5, 0, 4};
                mode = 'M';
                break;
            case 2: // re
            case 4: // mi
            case 9: // la
            case -10: // re
            case -8: // mi
            case -3: // la
                shape = new int[]{-5, 0, 3};
                mode = 'm';
                break;
            case 11: // si
            case -1: // si
                shape = new int[]{-6, 0, 3};
                mode = 'd';
                break;
        }
        inversion = SECOND_INVERSION;
    }

    /**
     * [CA] Mètode de prova per a la classe {@code Triad}. Imprimeix per consola
     * diverses tríades per a verificar la generació de formes i representació textual.
     * <p>
     * [EN] Test method for the {@code Triad} class. Prints various triads to the
     * console to verify shape generation and textual representation.
     *
     * @param args [CA] arguments de la línia de comandes (no s'usen) /
     *             [EN] command-line arguments (unused)
     */
    public static void main(String[] args){
        ToneRange.setMovileDo(true);
        int key = 54;
        int root = 53-key;
        System.err.println("Root = "+(root+key)+"("+Utilities.capitalize(ToneRange.getNoteName(root+key,key))+"), key = "+key+", (r)%12 ="+(root)%12);
        Triad chord = new Triad(root,key,RANDOM_INVERSION);
        System.out.println(chord.toString());
        Chord chord2 = new Chord(53-key,new int[]{0,4,9},key,"Fourth");
        System.out.println(chord2.toString());
    }
}
