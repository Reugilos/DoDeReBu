package dodecagraphone.model.chord;

import dodecagraphone.model.ToneRange;
import dodecagraphone.ui.Utilities;

/**
 *
 * @author Pau
 */
public class Triad extends Chord{
    public static final int ROOT_POSITION = 0;
    public static final int FIRST_INVERSION = 1;
    public static final int SECOND_INVERSION = 2;
    public static final int RANDOM_INVERSION = 13;

    private int inversion;
    private char mode;
        
    public Triad(){
        super();
        this.inversion = -1;
        this.mode = ' ';
        this.midiKey = -1;
    }
    
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
    public int getInversion() {
        return inversion;
    }

    public void setInversion(int inversion) {
        this.inversion = inversion;
    }

    public char getMode() {
        return mode;
    }

    public void setMode(char mode) {
        this.mode = mode;
    }

    public int getMidiKey() {
        return midiKey;
    }

    public void setMidiKey(int midiKey) {
        this.midiKey = midiKey;
    }
    
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
