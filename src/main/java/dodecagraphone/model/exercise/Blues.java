package dodecagraphone.model.exercise;

import dodecagraphone.model.component.MyExercise;
import dodecagraphone.model.chord.Chord;
import dodecagraphone.model.ToneRange;
import dodecagraphone.ui.Settings;

import java.util.Arrays;
import java.util.List;

public class Blues implements MyExerciseFamily {

    private static final List<String> all = Arrays.asList("test","tr_1", "tr_2", "tr_3", "tr_13", "tr_15", "Cant", "Blank");

//    @Override
//    public String getName() {
//        return "Blues";
//    }
//
//    @Override
//    public String getDescription() {
//        return "Twelve-bar blues with different voicings and melodic ideas.";
//    }
//
    public static List<String> getExerciseLabelList() {
        return all;
    }

    @Override
    public void applyExercise(MyExercise ex, String label) {
        switch (label) {
            case "test" -> setExercise_test(ex);
            case "tr_1" -> setExercise_track_1(ex);
            case "tr_2" -> setExercise_track_2(ex);
            case "tr_3" -> setExercise_track_3(ex);
            case "tr_13" -> setExercise_track_13(ex);
            case "tr_15" -> setExercise_track_15(ex);
            case "Cant" -> setCantaloupe(ex);
            case "Blank" -> {
                ex.setDescription("");
                ex.getController().clearScore();
            }
            default -> {
                ex.getController().clearScore();
                throw new UnsupportedOperationException("No such pattern: " + label);
            }
        }
        // ex.setTitleAuthorDescr();
    }

    private void setExercise_track_1(MyExercise ex) {
        ex.setDescription("12 bar blues, triad");
        ex.setLabel("tr_1");
        ex.setUsePentagramaStrips(true);
        int midiKey = ex.getMidiKey();
        int[] progression = {0, 0, 0, 0, 5, 5, 0, 0, 7, 5, 0, 7};

        for (int i = 0; i < 5; i++) {
            for (int root : progression) {
                Chord ch = new Chord(root, new int[]{0, 4, 7, 10}, midiKey, "", -5);
                int ncols = ex.phraseLength(new String[]{"q0", "e" + Settings.REST, "q.4", "q7"});
                ch.setNCols(ncols); ex.placeChordSymbol(ch);
                ex.placePhrase(root + midiKey, new String[]{"q0", "e" + Settings.REST, "q.4", "q7"}, false);
            }
        }
    }

    private void setExercise_test(MyExercise ex) {
        ex.setDescription("test, triad");
        ex.setLabel("test");
        ex.setUsePentagramaStrips(true);
        int midiKey = ex.getMidiKey();
        int[] progression = {0, 5, 7, 0};

        for (int i = 0; i < 1; i++) {
            for (int root : progression) {
                Chord ch = new Chord(root, new int[]{0, 4, 7, 10}, midiKey, "");
                int ncols = ex.phraseLength(new String[]{"q0", "e" + Settings.REST, "q.4", "q7"});
                ch.setNCols(ncols); ex.placeChordSymbol(ch);
                ex.placePhrase(root + midiKey, new String[]{"q0", "e" + Settings.REST, "q.4", "q7"}, false);
            }
        }
    }

    private void setExercise_track_2(MyExercise ex) {
        ex.setDescription("12 bar blues, {0 4 7 10}");
        ex.setLabel("tr_2");
        ex.setUsePentagramaStrips(true);
        int midiKey = ex.getMidiKey();
        int[] progression = {0, 0, 0, 0, 5, 5, 0, 0, 7, 5, 0, 7};

        for (int i = 0; i < 5; i++) {
            for (int root : progression) {
                Chord ch = new Chord(root, new int[]{0, 4, 7, 10}, midiKey, "");
                String[] phrase = {"Tq0", "Te0", "Tq4", "Te4", "Tq7", "Te7", "Tq10", "Te7"};
                int ncols = ex.phraseLength(phrase);
                ch.setNCols(ncols); ex.placeChordSymbol(ch);
                ex.placePhrase(root + midiKey, phrase, false);
            }
        }
    }

    private void setExercise_track_3(MyExercise ex) {
        ex.setDescription("12 bar blues, dominant plus 6 {0_4_7_9_10}");
        ex.setLabel("tr_3");
        ex.setUsePentagramaStrips(true);
        int midiKey = ex.getMidiKey();
        int[] progression = {0, 5, 7, 0};

        for (int i = 0; i < 4; i++) {
            progression[i] = ToneRange.upperSaxOctave(progression[i] + midiKey) - midiKey;
        }

        for (int root : progression) {
            Chord ch = new Chord(root, new int[]{0, 4, 7, 10}, midiKey, "");
            String[] phrase = {"q0", "q4", "q7", "e9", "e10"};
            int ncols = ex.phraseLength(phrase);
            ch.setNCols(ncols); ex.placeChordSymbol(ch);
            ex.placePhrase(root + midiKey, phrase, false);
        }

        for (int root : progression) {
            Chord ch = new Chord(root, new int[]{0, 4, 7, 10}, midiKey, "");
            String[] phrase = {"q0", "e2", "e3", "q4", "e7", "e9"};
            int ncols = ex.phraseLength(phrase);
            ch.setNCols(ncols); ex.placeChordSymbol(ch);
            ex.placePhrase(progression[0] + midiKey, phrase, false);
        }
    }

    private void setExercise_track_13(MyExercise ex) {
        ex.setDescription("12 bar blues, penta major plus blue notes {0_2_3_4_7_9_10}");
        ex.setLabel("tr_13");
        ex.setUsePentagramaStrips(true);
        int midiKey = ex.getMidiKey();
        int[] progression = {0, 5, 7, 0};

        for (int root : progression) {
            Chord ch = new Chord(root, new int[]{0, 4, 7, 10}, midiKey, "");
            String[] phrase = {"e0", "e2", "e3", "e4", "e7", "e9", "q10"};
            int ncols = ex.phraseLength(phrase);
            ch.setNCols(ncols); ex.placeChordSymbol(ch);
            ex.placePhrase(root + midiKey, phrase, false);
        }

        for (int root : progression) {
            Chord ch = new Chord(root, new int[]{0, 4, 7, 10}, midiKey, "");
            String[] phrase = {"e0", "e2", "e3", "Ts4", "Ts5", "Ts6", "s7", "s8", "e9", "e10", "e11"};
            int ncols = ex.phraseLength(phrase);
            ch.setNCols(ncols); ex.placeChordSymbol(ch);
            ex.placePhrase(root + midiKey, phrase, false);
        }
    }

    private void setExercise_track_15(MyExercise ex) {
        ex.setDescription("12 bar blues, blues scale {0_3_5_6_7_10}");
        ex.setLabel("tr_15");
        ex.setUsePentagramaStrips(true);
        int midiKey = ex.getMidiKey();
        int[] progression = {0, 5, 7, 0};

        for (int root : progression) {
            Chord ch = new Chord(root, new int[]{0, 4, 7, 10}, midiKey, "");
            String[] phrase = {"e0", "e3", "e5", "e6", "e7", "e10", "q12"};
            int ncols = ex.phraseLength(phrase);
            ch.setNCols(ncols); ex.placeChordSymbol(ch);
            ex.placePhrase(midiKey, phrase, false);
        }
    }

    private void setCantaloupe(MyExercise ex) {
        ex.setDescription("Cantaloupe Island");
        ex.setLabel("Cant");
        ex.setUsePentagramaStrips(true);
        ex.setMidiKey(ToneRange.getDefaultKey());
        int midiKey = ex.getMidiKey();
        int[] roots = {5, 1, 2, 5};
        int[] basses = {5, 1, 2, 5, 5, 5, 5, 5};
        Chord[] chords = new Chord[4];
        chords[0] = new Chord(roots[0], new int[]{0, 3, 7}, midiKey, "");
        chords[1] = new Chord(roots[1], new int[]{0, 4, 7, 10}, midiKey, "");
        chords[2] = new Chord(roots[2], new int[]{0, 3, 7}, midiKey, "");
        chords[3] = new Chord(roots[3], new int[]{0, 3, 7}, midiKey, "");

        String[][] phrases = {
            {"e0", "e2", "e3", "e5", "e7", "e9", "e10", "e12"},
            {"e0", "e2", "e4", "e5", "e7", "e9", "e10", "e12"},
            {"e0", "e2", "e3", "e5", "e7", "e9", "e10", "e12"},
            {"Tq0", "Tq3", "Tq5", "Tq6", "Tq7", "Tq10"},
            {"Tq0", "Tq3", "Tq5", "Tq6", "Tq7", "Tq10"},
            {"Tq0", "Tq3", "Tq5", "Tq6", "Tq7", "Tq10"},
            {"Tq0", "Tq3", "Tq5", "Tq6", "Tq7", "Tq10"},
            {"Tq0", "Tq3", "Tq5", "Tq6", "Tq7", "Tq10"}
        };

        int j = 0;
        for (int i = 0; i < phrases.length; i++) {
            int bass = basses[i];
            Chord ch = chords[j];
            String[] phrase = phrases[i];
            int ncols = ex.phraseLength(phrase);
            ch.setNCols(ncols); ex.placeChordSymbol(ch);
            ex.placePhrase(bass + midiKey, phrase, false);
            j = (j + 1) % 4;
        }
    }
} 
