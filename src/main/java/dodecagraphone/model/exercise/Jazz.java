/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
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

/**
 * [CA] Família d'exercicis de jazz per a l'entrenament auditiu.
 * Conté 11 exercicis progressius que cobreixen escales, progressions
 * diatòniques, blues de 12 compassos, progressions II-V-I i peces
 * de jazz estàndard. Implementa {@link MyExerciseFamily} i és invocada
 * pel controlador central quan l'usuari selecciona un exercici de jazz.
 * <p>
 * [EN] Jazz exercise family for ear training. Contains 11 progressive
 * exercises covering scales, diatonic progressions, 12-bar blues,
 * II-V-I progressions and standard jazz tunes. Implements
 * {@link MyExerciseFamily} and is invoked by the central controller
 * when the user selects a jazz exercise.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 * @see MyExerciseFamily
 */
public class Jazz implements MyExerciseFamily {

    private static final List<String> all = Arrays.asList(
        "Ex1", "Ex2", "Ex3", "Ex4", "Ex5", "Ex6", "Ex7", "Ex8", "Ex9", "Ex10", "Ex11"
    );

    /**
     * [CA] Retorna la llista d'etiquetes de tots els exercicis de jazz disponibles
     * ({@code "Ex1"} fins a {@code "Ex11"}).
     * <p>
     * [EN] Returns the list of labels for all available jazz exercises
     * ({@code "Ex1"} through {@code "Ex11"}).
     *
     * @return [CA] llista d'etiquetes d'exercicis / [EN] list of exercise labels
     */
    public static List<String> getExerciseLabelList() {
        return all;
    }

    /**
     * [CA] Configura i llança l'exercici corresponent a l'etiqueta indicada.
     * Actua com a despachador: delega la configuració al mètode privat de cada exercici.
     * Si l'etiqueta és desconeguda, neteja la partitura i llança
     * {@link UnsupportedOperationException}.
     * <p>
     * [EN] Configures and launches the exercise corresponding to the given label.
     * Acts as a dispatcher: delegates setup to the specific private method for each exercise.
     * If the label is unknown, clears the score and throws
     * {@link UnsupportedOperationException}.
     *
     * @param ex    [CA] l'objecte exercici a configurar / [EN] the exercise object to configure
     * @param label [CA] l'etiqueta de l'exercici (p.ex. {@code "Ex1"}) / [EN] the exercise label (e.g. {@code "Ex1"})
     * @throws UnsupportedOperationException [CA] si l'etiqueta no correspon a cap exercici conegut /
     *                                       [EN] if the label does not match any known exercise
     */
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

    /**
     * [CA] Exercici 1: Notes tonals en una octava en el rang del saxofon.
     * Escala major ascendent (graus 0–12), una nota per compàs.
     * <p>
     * [EN] Exercise 1: Tonal notes over one octave in the saxophone range.
     * Ascending major scale (degrees 0–12), one note per bar.
     *
     * @param ex [CA] l'objecte exercici a configurar / [EN] the exercise object to configure
     */
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

    /**
     * [CA] Exercici 2: Blues de 12 compassos amb tríada (arrel, tercera, quinta).
     * Progressió I-IV-V repetida 5 vegades. Usa una frase de tipus corxera-silenci-negra-amb-punt.
     * <p>
     * [EN] Exercise 2: 12-bar blues with a triad (root, third, fifth).
     * I-IV-V progression repeated 5 times. Uses a quarter-rest-dotted-quarter phrase.
     *
     * @param ex [CA] l'objecte exercici a configurar / [EN] the exercise object to configure
     */
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

    /**
     * [CA] Exercici 3: Blues de 12 compassos amb acord de sèptima de dominant
     * (arrel, tercera, quinta, sèptima menor). Frase d'arpegi ascendent i descendent.
     * <p>
     * [EN] Exercise 3: 12-bar blues with dominant seventh chord
     * (root, third, fifth, minor seventh). Ascending and descending arpeggio phrase.
     *
     * @param ex [CA] l'objecte exercici a configurar / [EN] the exercise object to configure
     */
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

    /**
     * [CA] Exercici 4: Progressió diatònica major (I a VII i retorn al I)
     * amb acords de sèptima major o menor. Cada grau té la seva frase d'arpegi
     * pròpia, seguida d'una escala major ascendent.
     * <p>
     * [EN] Exercise 4: Diatonic major progression (I through VII and back to I)
     * with major or minor seventh chords. Each degree has its own arpeggio phrase,
     * followed by an ascending major scale.
     *
     * @param ex [CA] l'objecte exercici a configurar / [EN] the exercise object to configure
     */
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

    /**
     * [CA] Exercici 5: Progressió diatònica menor (i a VII♭ i retorn al i)
     * amb acords de sèptima. Cada grau té la seva frase d'arpegi pròpia,
     * seguida d'una escala menor natural ascendent.
     * <p>
     * [EN] Exercise 5: Diatonic minor progression (i through VII♭ and back to i)
     * with seventh chords. Each degree has its own arpeggio phrase, followed
     * by an ascending natural minor scale.
     *
     * @param ex [CA] l'objecte exercici a configurar / [EN] the exercise object to configure
     */
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

    /**
     * [CA] Exercici 6: Sèptima de dominant més sisena. Progressió I-IV-V-I
     * en el rang del saxofon. Dues frases per compàs (escala + ornament cromàtic).
     * <p>
     * [EN] Exercise 6: Dominant seventh plus sixth. I-IV-V-I progression
     * in the saxophone range. Two phrases per bar (scale + chromatic ornament).
     *
     * @param ex [CA] l'objecte exercici a configurar / [EN] the exercise object to configure
     */
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

    /**
     * [CA] Exercici 7: Progressió II-V-I en mode major en el rang del saxofon.
     * Dos voltes: la primera amb escales, la segona amb acords bloquejats.
     * <p>
     * [EN] Exercise 7: II-V-I progression in major mode in the saxophone range.
     * Two rounds: the first with scales, the second with blocked chords.
     *
     * @param ex [CA] l'objecte exercici a configurar / [EN] the exercise object to configure
     */
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

    /**
     * [CA] Exercici 8: Progressió II-V-i en mode menor en el rang del saxofon.
     * Dos voltes: la primera amb escales, la segona amb acords bloquejats.
     * <p>
     * [EN] Exercise 8: II-V-i progression in minor mode in the saxophone range.
     * Two rounds: the first with scales, the second with blocked chords.
     *
     * @param ex [CA] l'objecte exercici a configurar / [EN] the exercise object to configure
     */
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

    /**
     * [CA] Exercici 9: Escala pentatònica major amb notes blues. Progressió I-IV-V-I
     * amb dues frases per compàs (pentatònica + ornament cromàtic amb sextes).
     * <p>
     * [EN] Exercise 9: Major pentatonic scale with blue notes. I-IV-V-I progression
     * with two phrases per bar (pentatonic + chromatic ornament with sixths).
     *
     * @param ex [CA] l'objecte exercici a configurar / [EN] the exercise object to configure
     */
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

    /**
     * [CA] Exercici 10: Escala de blues (pentatònica menor + quinta disminuïda).
     * Progressió I-IV-V-I amb una frase per compàs.
     * <p>
     * [EN] Exercise 10: Blues scale (minor pentatonic + diminished fifth).
     * I-IV-V-I progression with one phrase per bar.
     *
     * @param ex [CA] l'objecte exercici a configurar / [EN] the exercise object to configure
     */
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

    /**
     * [CA] Exercici 11: «Cantaloupe Island» (Herbie Hancock). Progressió de 4 acords
     * (FA menor, RE♭7, MI♭ menor, FA menor) amb frases melòdiques i rítmiques.
     * La tonalitat s'estableix al valor per defecte de l'aplicació.
     * <p>
     * [EN] Exercise 11: «Cantaloupe Island» (Herbie Hancock). 4-chord progression
     * (F minor, D♭7, E♭ minor, F minor) with melodic and rhythmic phrases.
     * The key is set to the application default value.
     *
     * @param ex [CA] l'objecte exercici a configurar / [EN] the exercise object to configure
     */
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
