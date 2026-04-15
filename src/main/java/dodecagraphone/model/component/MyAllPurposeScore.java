package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.MyTempo;
import dodecagraphone.model.ToneRange;

/**
 *
 * @author pau
 */
public class MyAllPurposeScore extends MyMidiScore {
    private String titol;
    private String author;
    private String description;

    public MyAllPurposeScore(MyController contr) {
        super(contr);
        this.midiKey = ToneRange.getDefaultKey();
//        this.setNeedsDrawing(true);
    }

    public void resetAllPurposeScore() {
        // defaults;
        this.stopAll();
        this.resetPattern();
        //this.resetMidiScore();
        this.resetExercise();

        this.choice.setNoneChoice();
        this.midiKey = ToneRange.getDefaultKey();
        this.usePentagramaStrips = true;
        this.showNoteNames = true;
        this.useMobileDo = false;
        this.useScreenKeyboardRight = false;
        this.setDefaultDelay();
        this.scaleMode = 'M';
        MyTempo.setTempo(60);
        // updates;
        updateStripsNKeyboard(usePentagramaStrips);
        this.controller.setScreenKeyboardRight(useScreenKeyboardRight);
        ToneRange.setMovileDo(useMobileDo);
//        this.setNeedsDrawing(true);
        // showNoteNames;
    }

    
}
