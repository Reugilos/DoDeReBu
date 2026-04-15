package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.MyKeyCircles;
import dodecagraphone.model.MyTempo;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.exercise.MyExerciseFamily;
import dodecagraphone.ui.Settings;

/**
 *
 * @author Pau
 */
public class MyExercise extends MyPatternScore {

    protected boolean firstTime = true;
    protected MyExerciseFamily family;

    public MyExercise(MyController contr) {
        super(contr);
        this.family = null;
    }
    
    public void resetExercise(){
        this.family = null;
    }
    
    public void setExerciseFamily(MyExerciseFamily family){
        this.family = family;
    }

    public boolean isFirstTime() {
        return firstTime;
    }

    public void setFirstTime(boolean firstTime) {
        this.firstTime = firstTime;
    }

    public MyExerciseFamily getFamily() {
        return family;
    }

    public void setFamily(MyExerciseFamily family) {
        this.family = family;
    }

    public void setCurrentExercise(String label) {
        if (family != null) {
            family.applyExercise(this, label);
        } else {
            throw new IllegalStateException("No exercise family defined.");
        }
    }

    public void setExercise(String label) {
        // defaults
        this.stopAll();
        this.resetPattern();
        this.scaleMode = 'M';
        this.choice.setDefaultChoice();
        this.usePentagramaStrips = true;
        this.showNoteNames = true;
        this.useMobileDo = false;
        this.useScreenKeyboardRight = false;
        this.label = label;
        setNumBeatsMeasure(Settings.getnBeatsMeasure());
        setBeatFigure(Settings.getBeatFigure());
        MyTempo.setTempo(60);
        this.setDefaultDelay();
        this.controller.setCurrentMidiFile("");

        // updates
        if (isFirstTime()) {
            this.midiKey = ToneRange.getDefaultKey();
            if (this.scaleMode == 'm') {
                String key = ToneRange.getKeyName(midiKey, 'M');
                key = MyKeyCircles.relativeKey(key);
                this.midiKey = ToneRange.getMidi(key);
            }
            setFirstTime(false);
        }

        setCurrentExercise(label);

        this.author = "Tradicional";
        this.title = this.label;
        updateStripsNKeyboard(usePentagramaStrips);
        this.controller.setScreenKeyboardRight(useScreenKeyboardRight);
        ToneRange.setMovileDo(useMobileDo);
        this.initOffscreen();
        //this.placeEndScore();
    }
}
