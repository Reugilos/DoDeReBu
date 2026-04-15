package dodecagraphone.model.exercise.exerciseList;

import dodecagraphone.MyController;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.component.MyAllPurposeScore;
import dodecagraphone.model.component.MyExercise;
import dodecagraphone.model.exercise.Blues;
import dodecagraphone.model.exercise.Miri;
import dodecagraphone.model.exercise.Saxo;
import dodecagraphone.model.exercise.Test;
import dodecagraphone.model.exercise.EarTraining;
import dodecagraphone.ui.Settings;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pau
 */
public class MyExerciseList {

    private List<String> exerciseLabelList;
    private int nextEx, prevEx;
    private MyAllPurposeScore currentExercise;
    private MyController contr;

    public static List<String> roundTheCircleExerciseLabelList() {
        List<String> labels = new ArrayList<>();
        int midiKey = ToneRange.MIDDLE_C;
        for (int i = 0; i < 12; i++) {
            labels.add(ToneRange.getNoteName(midiKey).substring(0, 2));
            midiKey = midiKey - ToneRange.MIDDLE_C;
            midiKey = ((midiKey - 7) % 12) + ToneRange.MIDDLE_C;
        }
        return labels;
    }

    public MyExerciseList(String whichSet, MyController contr) {
        this.contr = contr;
        exerciseLabelList = new ArrayList<>();
        this.currentExercise = this.contr.getAllPurposeScore();
        //Settings.initSettings();
        currentExercise.resetAllPurposeScore();
        switch (whichSet) {
            case "EarTraining":
//                currentExercise.resetAllPurposeScore();
                EarTraining ear = new EarTraining();
                currentExercise.setFamily(ear);
                exerciseLabelList = EarTraining.getExerciseLabelList();
                break;
            case "Saxo":
//                currentExercise.resetAllPurposeScore();
                Saxo saxo = new Saxo();
                currentExercise.setFamily(saxo);
                exerciseLabelList = Saxo.getExerciseLabelList();
                break;
            case "Miri":
//                currentExercise.resetAllPurposeScore();
                Miri miri = new Miri();
                currentExercise.setFamily(miri);
                exerciseLabelList = Miri.getExerciseLabelList();
                break;
            case "Test":
//                currentExercise.resetAllPurposeScore();
                Test test = new Test();
                currentExercise.setFamily(test);
                exerciseLabelList = Test.getExerciseLabelList();
                break;
            case "Blues":
//                currentExercise.resetAllPurposeScore();
                Blues blues = new Blues();
                currentExercise.setFamily(blues);
                exerciseLabelList = Blues.getExerciseLabelList();
                break;
            default:
                throw new UnsupportedOperationException("No such exercise family " + whichSet);
        }
        nextEx = 0;
        prevEx = exerciseLabelList.size() - 1;
    }

    public boolean isEmpty() {
        if (exerciseLabelList == null) {
            return true;
        }
        return exerciseLabelList.isEmpty();
    }

    public List<String> getExerciseLabelList() {
        return exerciseLabelList;
    }

    public void resetCurrentExercise() {
        currentExercise.setExercise(currentExercise.getLabel());
    }

    public MyAllPurposeScore next() {
        if (!this.isEmpty()) {
            String labelNext = exerciseLabelList.get(nextEx);
            currentExercise.setFirstTime(true);
            currentExercise.setExercise(labelNext);
            prevEx = nextEx - 1;
            if (prevEx < 0) {
                prevEx = exerciseLabelList.size() + prevEx;
            }
            nextEx = (nextEx + 1) % exerciseLabelList.size();
            return currentExercise;
        }
        return null;
    }

    public MyAllPurposeScore previous() {
        if (!this.isEmpty()) {
            String labelPrevious = exerciseLabelList.get(prevEx);
            currentExercise.setFirstTime(true);
            currentExercise.setExercise(labelPrevious);
            nextEx = (prevEx + 1) % exerciseLabelList.size();
            prevEx--;
            if (prevEx < 0) {
                prevEx = exerciseLabelList.size() + prevEx;
            }
            return currentExercise;
        }
        return null;
    }
}
