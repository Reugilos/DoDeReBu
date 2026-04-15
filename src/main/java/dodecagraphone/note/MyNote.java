package dodecagraphone.note;

import dodecagraphone.model.component.MyGridScore;
import dodecagraphone.ui.Settings;

/**
 *
 * @author pau
 */
public class MyNote {
    double duration; // A whole note (4 beats) has duration 1.
    int ncols; // Number of columns in the current grid.
    int pitch; // relative to midiRoot;
    boolean is_linked; // linked to the previous note
    
    public MyNote(String code, MyGridScore score){
        is_linked = false;
        boolean is_triplet = false;
        if (code.charAt(0)=='T'){ // triplet
            is_triplet = true;
            code = code.substring(1);
        }
        if (code.charAt(0)=='l'){
            is_linked = true;
            code = code.substring(1);
        }
        char d = code.charAt(0);
        switch (d){
            case 'w': // whole
                duration = 1; // one measure (4 beats)
                break;
            case 'h': // half
                duration = 1.0/2;
                break;
            case 'q': // quarter
                duration = 1.0/4;
                break;
            case 'e': // eigth
                duration = 1.0/8;
                break;
            case 's': // sixteenth
                duration = 1.0/16;
                break;
            case 't': // fusa 32th
                duration = 1.0/32;
                break;
            case 'x': // semifusa 64th
                duration = 1.0/64;
                break;
        }
        int i = 1;
        if (code.charAt(1)=='.'){
            i=2;
            duration *= 1.5;
        }
        if (is_triplet){
            duration *= 2.0/3;
        }
        ncols = (int) Math.round(duration * Settings.getnColsBeat() * 4);
        pitch = Integer.parseInt(code.substring(i));
    }

    public double getDuration() {
        return duration;
    }

    public int getNcols() {
        return ncols;
    }

    public int getPitch() {
        return pitch;
    }

    public boolean isLinked() {
        return is_linked;
    }

    public void setLinked(boolean is_linked) {
        this.is_linked = is_linked;
    }
    
    
    
}
