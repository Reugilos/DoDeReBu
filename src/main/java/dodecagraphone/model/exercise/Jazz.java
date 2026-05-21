package dodecagraphone.model.exercise;

import dodecagraphone.model.ToneRange;
import dodecagraphone.model.chord.Chord;
import dodecagraphone.model.component.MyExercise;
import dodecagraphone.model.component.MyPatternScore;
import dodecagraphone.ui.I18n;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;

import java.util.Arrays;
import java.util.List;

public class Jazz implements MyExerciseFamily {

    private static final List<String> all = Arrays.asList(
        "Ex1", "Ex2", "Ex3", "Ex4", "Ex5", "Ex6", "Ex7", "Ex8", "Ex9", "Ex10", "Ex11"
    );

    public static List<String> getExerciseLabelList() {
        return all;
    }

    @Override
    public void applyExercise(MyExercise ex, String label) {
        ex.setLabel(label);
        switch (label) {
            case "Ex1"  -> exercise_1(ex);
            case "Ex2"  -> exercise_2(ex);
            case "Ex3"  -> exercise_3(ex);
            case "Ex4"  -> exercise_4(ex);
            case "Ex5"  -> exercise_5(ex);
            case "Ex6"  -> exercise_6(ex);
            case "Ex7"  -> exercise_7(ex);
            case "Ex8"  -> exercise_8(ex);
            case "Ex9"  -> exercise_9(ex);
            case "Ex10" -> exercise_10(ex);
            case "Ex11" -> exercise_11(ex);
            default -> {
                ex.getController().clearScore();
                throw new UnsupportedOperationException("No such exercise: " + label);
            }
        }
    }

    // Ex1: tonal notes, one octave (from Saxo)
    private void exercise_1(MyExercise ex) {
        ex.setDescription(I18n.t("jazz.ex1.description"));
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

    // Ex2: 12 bar blues, triad (from Blues tr_1)
    private void exercise_2(MyExercise ex) {
        ex.setDescription(I18n.t("jazz.ex2.description"));
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

    // Ex3: 12 bar blues, 7th chord (from Blues tr_2)
    private void exercise_3(MyExercise ex) {
        ex.setDescription(I18n.t("jazz.ex3.description"));
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

    // Ex4: diatonic major progression (from Saxo diaM)
    private void exercise_4(MyExercise ex) {
        ex.setDescription(I18n.t("jazz.ex4.description"));
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
        ex.placePhrase(midiKey, new String[]{"e0", "e2", "e4", "e5", "e7", "e9", "e11", "e12"}, false);
    }

    // Ex5: diatonic minor progression (from Saxo diam)
    private void exercise_5(MyExercise ex) {
        ex.setDescription(I18n.t("jazz.ex5.description"));
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
        ex.placePhrase(midiKey, new String[]{"e0", "e2", "e3", "e5", "e7", "e8", "e10", "e12"}, false);
    }

    // Ex6: dominant 7th plus 6th (from Blues tr_3)
    private void exercise_6(MyExercise ex) {
        ex.setDescription(I18n.t("jazz.ex6.description"));
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

    // Ex7: ii V I progression (from Saxo ii_V_I)
    private void exercise_7(MyExercise ex) {
        ex.setDescription(I18n.t("jazz.ex7.description"));
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

    // Ex8: ii V i progression (from Saxo ii_V_i)
    private void exercise_8(MyExercise ex) {
        ex.setDescription(I18n.t("jazz.ex8.description"));
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

    // Ex9: pentatonic major + blue notes (from Blues tr_13)
    private void exercise_9(MyExercise ex) {
        ex.setDescription(I18n.t("jazz.ex9.description"));
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

    // Ex10: blues scale (from Blues tr_15)
    private void exercise_10(MyExercise ex) {
        ex.setDescription(I18n.t("jazz.ex10.description"));
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

    // Ex11: Cantaloupe Island (from Blues Cant)
    private void exercise_11(MyExercise ex) {
        ex.setDescription(I18n.t("jazz.ex11.description"));
        ex.setUsePentagramaStrips(true);
        ex.setMidiKey(ToneRange.getDefaultKey());
        int midiKey = ex.getMidiKey();
        int[] roots  = {5, 1, 2, 5};
        int[] basses = {5, 1, 2, 5, 5, 5, 5, 5};
        Chord[] chords = {
            new Chord(roots[0], new int[]{0, 3, 7},     midiKey, ""),
            new Chord(roots[1], new int[]{0, 4, 7, 10}, midiKey, ""),
            new Chord(roots[2], new int[]{0, 3, 7},     midiKey, ""),
            new Chord(roots[3], new int[]{0, 3, 7},     midiKey, "")
        };
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
            Chord ch = chords[j];
            int ncols = ex.phraseLength(phrases[i]);
            ch.setNCols(ncols); ex.placeChordSymbol(ch);
            ex.placePhrase(basses[i] + midiKey, phrases[i], false);
            j = (j + 1) % 4;
        }
    }
}
