package dodecagraphone.model.exercise;

import dodecagraphone.model.ToneRange;
import dodecagraphone.model.chord.Chord;
import dodecagraphone.model.component.MyExercise;
import dodecagraphone.model.component.MyPatternScore;
import dodecagraphone.ui.Utilities;

import java.util.Arrays;
import java.util.List;

public class Saxo implements MyExerciseFamily {

    private static final List<String> all = Arrays.asList("Ex1", "ii_V_I", "ii_V_i", "diaM", "diam", "Blank");

//    @Override
//    public String getName() {
//        return "Saxo";
//    }
//
//    @Override
//    public String getDescription() {
//        return "Saxophone practice exercises: tonal lines and harmonic progressions.";
//    }
//
    public static List<String> getExerciseLabelList() {
        return all;
    }

    @Override
    public void applyExercise(MyExercise ex, String label) {
        switch (label) {
            case "Ex1" -> exercise_1(ex);
            case "ii_V_I" -> set_ii_V_I(ex);
            case "ii_V_i" -> set_ii_V_i(ex);
            case "diaM" -> set_diaM(ex);
            case "diam" -> set_diam(ex);
            case "Blank" -> {
                ex.setLabel("Blank");
                ex.setDescription("");
                ex.getController().clearScore();
            }
            default -> {
                ex.getController().clearScore();
                throw new UnsupportedOperationException("No such pattern: " + label);
            }
        }
    }

    // --- Aquí van els mètodes originals convertits a acceptar MyExercise com a paràmetre ---

    private void exercise_1(MyExercise ex) {
        ex.setLabel("Ex1");
        ex.setDescription("Listen, read and repeat, tonal, one octave");
        ex.setUsePentagramaStrips(false);
        ToneRange.setMovileDo(false);
        ex.setShowNoteNames(true);
        ex.setChoice(new Integer[]{0, 2, 4, 5, 7, 9, 11, 12});

        ex.placeTonalContext(ex.getMidiKey(), ToneRange.getLowestSaxo(), ToneRange.getHighestSaxo());

        while (!ex.isOver()) {
            int note = Utilities.randFromList(ex.getChoice().getChoiceList()) + ex.getMidiKey();
            while (note < ToneRange.getLowestSaxo()) note += 12;
            while (note > ToneRange.getHighestSaxo()) note -= 12;
            ex.placeNote(note, MyPatternScore.ONE_BAR, false);
        }
    }

    private void set_ii_V_I(MyExercise ex) {
        ex.setDescription("ii V I progression (+vi)");
        ex.setLabel("ii_V_I");
        ex.setScaleMode('M');
        ex.getChoice().setDefaultChoice();
        int midiKey = ex.getMidiKey();
        int[] roots = {2, 7, 0, 9};
        for (int i = 0; i < 4; i++) {
            roots[i] = ToneRange.upperSaxOctave(roots[i] + midiKey) - midiKey;
        }
        Chord[] chords = {
            new Chord(roots[0], new int[]{0, 3, 7, 10}, midiKey, ""),
            new Chord(roots[1], new int[]{0, 4, 7, 10}, midiKey, ""),
            new Chord(roots[2], new int[]{0, 4, 7, 11}, midiKey, ""),
            new Chord(roots[3], new int[]{0, 3, 7, 10}, midiKey, "")
        };
        String[][] phrases = {
            {"e0", "e2", "e3", "e5", "e7", "e9", "e10", "e12"},
            {"e0", "e2", "e4", "e5", "e7", "e9", "e10", "e12"},
            {"e0", "e2", "e4", "e5", "e7", "e9", "e11", "e12"},
            {"e0", "e2", "e3", "e5", "e7", "e8", "e10", "e12"},
            {"q0", "q3", "q7", "q10"},
            {"q0", "q4", "q7", "q10"},
            {"q0", "q4", "q7", "q11"},
            {"q0", "q3", "q7", "q10"}
        };
        int j = 0;
        for (String[] phrase : phrases) {
            Chord ch = chords[j];
            int ncols = ex.phraseLength(phrase);
            ch.setNCols(ncols); 
            ex.placeChordSymbol(ch);
            ex.placePhrase(roots[j] + midiKey, phrase, false);
            j = (j + 1) % 4;
        }
    }

    private void set_ii_V_i(MyExercise ex) {
        ex.setDescription("ii V i progression (+III)");
        ex.setLabel("ii_V_i");
        ex.setScaleMode('m');
        ex.getChoice().setDefaultMinorChoice();
        ex.setShowNoteNames(true);
        int midiKey = ex.getMidiKey();
        int[] roots = {2, 7, 0, 3};
        for (int i = 0; i < 4; i++) {
            roots[i] = ToneRange.upperSaxOctave(roots[i] + midiKey) - midiKey;
        }
        Chord[] chords = {
            new Chord(roots[0], new int[]{0, 3, 6, 10}, midiKey, ""),
            new Chord(roots[1], new int[]{0, 4, 7, 10}, midiKey, ""),
            new Chord(roots[2], new int[]{0, 3, 7, 10}, midiKey, ""),
            new Chord(roots[3], new int[]{0, 4, 7, 11}, midiKey, "")
        };
        String[][] phrases = {
            {"e0", "e1", "e3", "e5", "e6", "e8", "e10", "e12"},
            {"e0", "e1", "e4", "e5", "e7", "e8", "e10", "e12"},
            {"e0", "e2", "e3", "e5", "e7", "e8", "e10", "e12"},
            {"e0", "e2", "e4", "e5", "e7", "e9", "e11", "e12"},
            {"q0", "q3", "q6", "q10"},
            {"q0", "q4", "q7", "q10"},
            {"q0", "q3", "q7", "q10"},
            {"q0", "q4", "q7", "q11"}
        };
        int j = 0;
        for (String[] phrase : phrases) {
            Chord ch = chords[j];
            int ncols = ex.phraseLength(phrase);
            ch.setNCols(ncols); ex.placeChordSymbol(ch);
            ex.placePhrase(roots[j] + midiKey, phrase, false);
            j = (j + 1) % 4;
        }
    }

    private void set_diaM(MyExercise ex) {
        ex.setDescription("diatonic Major progression");
        ex.setLabel("diaM");
        ex.setScaleMode('M');
        ex.getChoice().setDefaultChoice();
        int midiKey = ex.getMidiKey();
        int[] roots = {0, 2, 4, 5, 7, 9, 11, 0};
        Chord[] chords = {
            new Chord(roots[0], new int[]{0, 4, 7, 11}, midiKey, ""),
            new Chord(roots[1], new int[]{0, 3, 7, 10}, midiKey, ""),
            new Chord(roots[2], new int[]{0, 3, 7, 10}, midiKey, ""),
            new Chord(roots[3], new int[]{0, 4, 7, 11}, midiKey, ""),
            new Chord(roots[4], new int[]{0, 4, 7, 10}, midiKey, ""),
            new Chord(roots[5], new int[]{0, 3, 7, 10}, midiKey, ""),
            new Chord(roots[6], new int[]{0, 3, 6, 10}, midiKey, ""),
            new Chord(roots[7], new int[]{0, 4, 7, 11}, midiKey, "")
        };
        String[][] phrases = {
            {"e0", "e4", "e7", "e11"},
            {"e0", "e3", "e7", "e10"},
            {"e0", "e3", "e7", "e10"},
            {"e0", "e4", "e7", "e11"},
            {"e0", "e4", "e7", "e10"},
            {"e0", "e3", "e7", "e10"},
            {"e0", "e3", "e6", "e10"},
            {"e0", "e4", "e7", "e11"}
        };
        for (int i = 0; i < phrases.length; i++) {
            int ncols = ex.phraseLength(phrases[i]);
            chords[i].setNCols(ncols);
            ex.placeChordSymbol(chords[i]);
            ex.placePhrase(roots[i] + midiKey, phrases[i], false);
        }
        String[] scale = {"e0", "e2", "e4", "e5", "e7", "e9", "e11", "e12"};
        ex.placePhrase(midiKey, scale, false);
    }

    private void set_diam(MyExercise ex) {
        ex.setDescription("diatonic minor progression");
        ex.setLabel("diam");
        ex.setScaleMode('m');
        ex.getChoice().setDefaultChoice();
        int midiKey = ex.getMidiKey();
        int[] roots = {0, 2, 3, 5, 7, 8, 10, 0};
        Chord[] chords = {
            new Chord(roots[0], new int[]{0, 3, 7, 10}, midiKey, ""),
            new Chord(roots[1], new int[]{0, 3, 6, 10}, midiKey, ""),
            new Chord(roots[2], new int[]{0, 4, 7, 11}, midiKey, ""),
            new Chord(roots[3], new int[]{0, 3, 7, 10}, midiKey, ""),
            new Chord(roots[4], new int[]{0, 3, 7, 10}, midiKey, ""),
            new Chord(roots[5], new int[]{0, 4, 7, 11}, midiKey, ""),
            new Chord(roots[6], new int[]{0, 3, 7, 10}, midiKey, ""),
            new Chord(roots[7], new int[]{0, 3, 7, 10}, midiKey, "")
        };
        String[][] phrases = {
            {"e0", "e3", "e7", "e10"},
            {"e0", "e3", "e6", "e10"},
            {"e0", "e4", "e7", "e11"},
            {"e0", "e3", "e7", "e10"},
            {"e0", "e3", "e7", "e10"},
            {"e0", "e4", "e7", "e11"},
            {"e0", "e4", "e7", "e10"},
            {"e0", "e3", "e7", "e10"}
        };
        for (int i = 0; i < phrases.length; i++) {
            int ncols = ex.phraseLength(phrases[i]);
            chords[i].setNCols(ncols);
            ex.placeChordSymbol(chords[i]);
            ex.placePhrase(roots[i] + midiKey, phrases[i], false);
        }
        String[] scale = {"e0", "e2", "e3", "e5", "e7", "e8", "e10", "e12"};
        ex.placePhrase(midiKey, scale, false);
    }
}
