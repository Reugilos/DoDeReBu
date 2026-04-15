package dodecagraphone.model.exercise;

import dodecagraphone.model.MyTempo;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.chord.Chord;
import dodecagraphone.model.chord.Triad;
import dodecagraphone.model.component.MyExercise;
import dodecagraphone.ui.Utilities;

import java.util.Arrays;
import java.util.List;

public class Miri implements MyExerciseFamily {

    private static final List<String> all = Arrays.asList(
        "Ex1a", "Ex1b", "Ex1c", "Ex2a", "Ex2b", "Ex2c", "Ex3a", "Ex3b", "Ex3c"
    );

    public static List<String> getExerciseLabelList() {
        return all;
    }

//    @Override
//    public String getName() {
//        return "Miri";
//    }
//
//    @Override
//    public String getDescription() {
//        return "Ear training and tonal awareness with violin range.";
//    }
//
    @Override
    public void applyExercise(MyExercise ex, String label) {
        switch (label) {
            case "Ex1a" -> setExercise_1_a(ex);
            case "Ex1b" -> setExercise_1_b(ex);
            case "Ex1c" -> setExercise_1_c(ex);
            case "Ex2a" -> setExercise_2_a(ex);
            case "Ex2b" -> setExercise_2_b(ex);
            case "Ex2c" -> setExercise_2_c(ex);
            case "Ex3a" -> setExercise_3_a(ex);
            case "Ex3b" -> setExercise_3_b(ex);
            case "Ex3c" -> setExercise_3_c(ex);
            case "Blank" -> {
                ex.setDescription("");
                ex.setLabel("Blank");
                ex.getController().clearScore();
            }
            default -> {
                ex.getController().clearScore();
                throw new UnsupportedOperationException("No such pattern: " + label);
            }
        }
    }

    private void setExercise_1_a(MyExercise ex) {
        ex.setDescription("Listen, read and repeat, tonal, two octaves, random key");
        int midiKey = Utilities.getRand().nextInt(12) + ToneRange.MIDDLE_C - 6;
        ex.setMidiKey(midiKey);
        ex.setUsePentagramaStrips(false);
        ToneRange.setMovileDo(false);
        ex.setShowNoteNames(true);
        ex.setChoice(new Integer[]{0,2,4,5,7,9,11,12,-12,-10,-8,-7,-5,-3,-1});
        ex.placeTonalContext(midiKey);
        while (!ex.isOver()) {
            int note = Utilities.randFromList(ex.getChoice().getChoiceList()) + midiKey;
            while (note < ToneRange.getLowestViolin()) note += 12;
            while (note > ToneRange.getHighestViolin()) note -= 12;
            ex.placeNote(note, MyExercise.ONE_BAR, false);
            ex.deletePreviousCol();
        }
        MyTempo.slower();
    }

    private void setExercise_1_b(MyExercise ex) {
        ex.setDescription("Listen, read and repeat, tonal, two octaves, La or So key");
        Integer[] keyChoice = {7, 9};
        int midiKey = keyChoice[Utilities.getRand().nextInt(2)] + ToneRange.MIDDLE_C;
        ex.setMidiKey(midiKey);
        ex.setUsePentagramaStrips(false);
        ToneRange.setMovileDo(false);
        ex.setShowNoteNames(true);
        ex.setChoice(new Integer[]{0,2,4,5,7,9,11,12,-12,-10,-8,-7,-5,-3,-1});
        ex.placeTonalContext(midiKey);
        while (!ex.isOver()) {
            int note = Utilities.randFromList(ex.getChoice().getChoiceList()) + midiKey;
            while (note < ToneRange.getLowestViolin()) note += 12;
            while (note > ToneRange.getHighestViolin()) note -= 12;
            ex.placeNote(note, MyExercise.ONE_BAR, false);
            ex.deletePreviousCol();
        }
        MyTempo.slower();
    }

    private void setExercise_1_c(MyExercise ex) {
        ex.setDescription("Listen, read and repeat, tonal, two octaves, La, So, Re, Do or Li key");
        Integer[] keyChoice = {0, 2, 7, 9, 10};
        int midiKey = keyChoice[Utilities.getRand().nextInt(5)] + ToneRange.MIDDLE_C;
        ex.setMidiKey(midiKey);
        ex.setUsePentagramaStrips(false);
        ToneRange.setMovileDo(false);
        ex.setShowNoteNames(true);
        ex.setChoice(new Integer[]{0,2,4,5,7,9,11,12,-12,-10,-8,-7,-5,-3,-1});
        ex.placeTonalContext(midiKey);
        while (!ex.isOver()) {
            int note = Utilities.randFromList(ex.getChoice().getChoiceList()) + midiKey;
            while (note < ToneRange.getLowestViolin()) note += 12;
            while (note > ToneRange.getHighestViolin()) note -= 12;
            ex.placeNote(note, MyExercise.ONE_BAR, false);
            ex.deletePreviousCol();
        }
        MyTempo.slower();
    }

    private void setExercise_2_a(MyExercise ex) {
        ex.setDescription("Read, sing and play, tonal, two octaves, random key");
        int midiKey = Utilities.getRand().nextInt(12) + ToneRange.MIDDLE_C - 6;
        ex.setMidiKey(midiKey);
        ex.setUsePentagramaStrips(true);
        ToneRange.setMovileDo(false);
        ex.setShowNoteNames(true);
        ex.setChoice(new Integer[]{0,2,4,5,7,9,11,12,-12,-10,-8,-7,-5,-3,-1});
        ex.placeTonalContext(midiKey);
        while (!ex.isOver()) {
            int note = Utilities.randFromList(ex.getChoice().getChoiceList()) + midiKey;
            while (note < ToneRange.getLowestViolin()) note += 12;
            while (note > ToneRange.getHighestViolin()) note -= 12;
            ex.placeNote(note, MyExercise.HALF_BAR, true);
            ex.placeNote(note, MyExercise.HALF_BAR, false);
            ex.deletePreviousCol();
        }
        MyTempo.slower();
    }

    private void setExercise_2_b(MyExercise ex) {
        ex.setDescription("Read, sing and play, tonal, two octaves, La or So key");
        Integer[] keyChoice = {7, 9};
        int midiKey = keyChoice[Utilities.getRand().nextInt(2)] + ToneRange.MIDDLE_C;
        ex.setMidiKey(midiKey);
        ex.setUsePentagramaStrips(true);
        ToneRange.setMovileDo(false);
        ex.setShowNoteNames(true);
        ex.setChoice(new Integer[]{0,2,4,5,7,9,11,12,-12,-10,-8,-7,-5,-3,-1});
        ex.placeTonalContext(midiKey);
        while (!ex.isOver()) {
            int note = Utilities.randFromList(ex.getChoice().getChoiceList()) + midiKey;
            while (note < ToneRange.getLowestViolin()) note += 12;
            while (note > ToneRange.getHighestViolin()) note -= 12;
            ex.placeNote(note, MyExercise.HALF_BAR, true);
            ex.placeNote(note, MyExercise.HALF_BAR, false);
            ex.deletePreviousCol();
        }
        MyTempo.slower();
    }

    private void setExercise_2_c(MyExercise ex) {
        ex.setDescription("Read, sing and play, tonal, two octaves, La, So, Re, Do or Li key");
        Integer[] keyChoice = {0, 2, 7, 9, 10};
        int midiKey = keyChoice[Utilities.getRand().nextInt(5)] + ToneRange.MIDDLE_C;
        ex.setMidiKey(midiKey);
        ex.setUsePentagramaStrips(true);
        ToneRange.setMovileDo(false);
        ex.setShowNoteNames(true);
        ex.setChoice(new Integer[]{0,2,4,5,7,9,11,12,-12,-10,-8,-7,-5,-3,-1});
        ex.placeTonalContext(midiKey);
        while (!ex.isOver()) {
            int note = Utilities.randFromList(ex.getChoice().getChoiceList()) + midiKey;
            while (note < ToneRange.getLowestViolin()) note += 12;
            while (note > ToneRange.getHighestViolin()) note -= 12;
            ex.placeNote(note, MyExercise.HALF_BAR, true);
            ex.placeNote(note, MyExercise.HALF_BAR, false);
            ex.deletePreviousCol();
        }
        MyTempo.slower();
    }

    private void setExercise_3_a(MyExercise ex) {
        ex.setDescription("Recognize key / recognize chord progression (I,IV,V,vi). Key = La,So,Re,Do or Li");
        Integer[] keyChoice = {0, 2, 7, 9, 10};
        int midiKey = keyChoice[Utilities.getRand().nextInt(5)] + ToneRange.MIDDLE_C;
        ex.setMidiKey(midiKey);
        ex.setUsePentagramaStrips(false);
        ToneRange.setMovileDo(false);
        ex.setShowNoteNames(true);
        ex.setChoice(new Integer[]{0,2,4,5,7,9,11,12,-12,-10,-8,-7,-5,-3,-1});
        Integer[] chordChoice = {0,5,7,9,-12,-7,-5,-3};
        while (!ex.isOver()) {
            int root = Utilities.randFromList(Arrays.asList(chordChoice));
            Chord chord = new Triad(root, midiKey, Triad.ROOT_POSITION);
            ex.placeAppendMessage(chord.toString(), MyExercise.MESSAGE_DELAY);
            ex.placeChord(chord, 2 * MyExercise.ONE_BEAT - 1, false);
            ex.skipCols(1);
        }
    }

    private void setExercise_3_b(MyExercise ex) {
        ex.setDescription("Recognize key / recognize chord progression (I,IV,V,vi). Key = Do");
        int midiKey = ToneRange.MIDDLE_C;
        ex.setMidiKey(midiKey);
        ex.setUsePentagramaStrips(false);
        ToneRange.setMovileDo(false);
        ex.setShowNoteNames(true);
        ex.setChoice(new Integer[]{0,2,4,5,7,9,11,12,-12,-10,-8,-7,-5,-3,-1});
        Integer[] chordChoice = {0,5,7,9,-12,-7,-5,-3};
        while (!ex.isOver()) {
            int root = Utilities.randFromList(Arrays.asList(chordChoice));
            Chord chord = new Triad(root, midiKey, Triad.ROOT_POSITION);
            ex.placeAppendMessage(chord.toString(), MyExercise.MESSAGE_DELAY);
            ex.placeChord(chord, 2 * MyExercise.ONE_BEAT - 1, false);
            ex.skipCols(1);
        }
    }

    private void setExercise_3_c(MyExercise ex) {
        ex.setDescription("Recognize key / recognize chord progression (I,IV,V,vi). Key = La or So");
        Integer[] keyChoice = {7, 9};
        int midiKey = keyChoice[Utilities.getRand().nextInt(2)] + ToneRange.MIDDLE_C;
        ex.setMidiKey(midiKey);
        ex.setUsePentagramaStrips(false);
        ToneRange.setMovileDo(false);
        ex.setShowNoteNames(true);
        ex.getController().setScreenKeyboardRight(true);
        ex.setChoice(new Integer[]{0,2,4,5,7,9,11,12,-12,-10,-8,-7,-5,-3,-1});
        Integer[] chordChoice = {0,5,7,9,-12,-7,-5,-3};
        while (!ex.isOver()) {
            int root = Utilities.randFromList(Arrays.asList(chordChoice));
            Chord chord = new Triad(root, midiKey, Triad.ROOT_POSITION);
            ex.placeAppendMessage(chord.toString(), MyExercise.MESSAGE_DELAY);
            ex.placeChord(chord, 2 * MyExercise.ONE_BEAT - 1, false);
            ex.skipCols(1);
        }
    }
}
