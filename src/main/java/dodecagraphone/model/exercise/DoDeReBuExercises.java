/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.exercise;

import dodecagraphone.model.MyChoice;
import dodecagraphone.model.MyKeyCircles;
import dodecagraphone.model.MyTempo;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.chord.Chord;
import dodecagraphone.model.component.MyExercise;
import dodecagraphone.ui.I18n;
import dodecagraphone.ui.Utilities;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [CA] Família d'exercicis per al metal·lòfon ({@code DoDeReBuExercises}).
 * Conté 4 exercicis bàsics de reconeixement i entonació de notes individuals
 * en tonalitat major i menor, amb tonalitat aleatòria generada a cada reinici.
 * <p>
 * Notes de disseny:
 * <ul>
 *   <li>La tonalitat es genera aleatòriament a cada crida a applyExercise (restart inclòs).</li>
 *   <li>El pattern de tecles s'estableix com "fit to tonality" (MIDI absoluts, rang complet).</li>
 *   <li>Visibilitat (noms, pentagrama, do mòbil) heretada de l'estat de l'usuari (no sobreescrita).</li>
 *   <li>Ex1/Ex3 (endevina): teclat dret, retard 2 beats.</li>
 *   <li>Ex2/Ex4 (toca): teclat esquerre, sense retard.</li>
 * </ul>
 * <p>
 * [EN] Exercise family for the metallophone ({@code DoDeReBuExercises}).
 * Contains 4 basic exercises for recognising and playing individual notes
 * in major and minor keys, with a random key generated on each restart.
 *
 * @see MyExerciseFamily
 * @see dodecagraphone.model.component.MyExercise
 */
public class DoDeReBuExercises implements MyExerciseFamily {

    private static final List<String> exerciseList = Arrays.asList("Ex1", "Ex2", "Ex3", "Ex4");

    public static List<String> getExerciseLabelList() {
        return exerciseList;
    }

    @Override
    public void applyExercise(MyExercise ex, String label) {
        ex.setLabel(label);
        switch (label) {
            case "Ex1" -> guessNoteMajor(ex);
            case "Ex2" -> playNoteMajor(ex);
            case "Ex3" -> guessNoteMinor(ex);
            case "Ex4" -> playNoteMinor(ex);
            case "Blank" -> ex.getController().clearScore();
            default -> {
                ex.getController().clearScore();
                throw new UnsupportedOperationException(I18n.f("earTraining.error.noSuchPattern", label));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * [CA] Aplica el pattern "fit to tonality" al choice de l'exercici:
     * escala (major o menor), estesa a tot el rang, ancorada a la tònica.
     * [EN] Applies the "fit to tonality" pattern to the exercise choice:
     * scale (major or minor), extended to full range, anchored to the tonic.
     */
    private static void applyFitToTonality(MyExercise ex, boolean minor) {
        MyChoice c = ex.getChoice();
        if (minor) c.setMinorScaleChoice();
        else       c.setMajorScaleChoice();
        // root = primera nota de l'escala al rang visual de l'instrument
        // (usem el rang de l'instrument, no el rang brut del grid, perquè
        //  l'exercici soni en el registre correcte de l'instrument)
        int pc         = ex.getMidiKey() % 12;
        int[] r        = ex.getLeadInstrumentVisualRange();
        int lowestMidi = r[0];
        int root       = lowestMidi + ((pc - lowestMidi % 12 + 12) % 12);
        c.addUpRoot(root);
    }

    /**
     * [CA] Retorna la llista de notes MIDI vàlides dins del rang visual de
     * l'instrument lead, deduplicades, per ser usades com a pool de generació.
     * [EN] Returns the list of valid MIDI notes within the lead instrument's
     * visual range, deduplicated, for use as the random generation pool.
     */
    private static List<Integer> validNotePool(MyExercise ex) {
        int[] r = ex.getLeadInstrumentVisualRange();
        int lo = r[0], hi = r[1];
        return ex.getChoice().getChoiceList().stream()
                .filter(n -> n >= lo && n <= hi)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Exercises
    // -------------------------------------------------------------------------

    /**
     * Ex1: Escolta una nota i endevina quina és (to major).
     * Teclat dret, retard 2 beats → la nota sona primer i apareix 2 beats més tard.
     * Els noms de notes s'amaguen per no revelar la resposta.
     */
    private static void guessNoteMajor(MyExercise ex) {
        ex.setDescription(I18n.t("dodeRebu.ex1.description"));
        int midiKey = ex.useMidiKeyForExercise(ToneRange.getMidi(MyKeyCircles.randM()));
        ex.setUseScreenKeyboardRight(true);

        ex.setDelay(2 * ex.ONE_BEAT);
        MyTempo.setTempo(60);
        applyFitToTonality(ex, false);
        List<Integer> pool = validNotePool(ex);
        Chord chord = new Chord(0, new int[]{0}, midiKey, I18n.t("earTraining.backgroundChord"));
        ex.placeBackgroundChord(chord);
        ex.placeTonalContext(midiKey);
        for (int i = 0; i < 32 && !ex.isOver(); i++) {
            int note = Utilities.randFromList(pool);
            ex.placeNote(note, ex.ONE_BAR, false);
        }
        placeEndOfScore(ex);
    }

    /**
     * Ex2: Veus una nota a la partitura i la toques al teclat (to major).
     * Teclat esquerre, sense retard → la nota apareix immediatament.
     */
    private static void playNoteMajor(MyExercise ex) {
        ex.setDescription(I18n.t("dodeRebu.ex2.description"));
        int midiKey = ex.useMidiKeyForExercise(ToneRange.getMidi(MyKeyCircles.randM()));
        ex.setUseScreenKeyboardRight(false);
        MyTempo.setTempo(60);
        applyFitToTonality(ex, false);
        List<Integer> pool = validNotePool(ex);
        Chord chord = new Chord(0, new int[]{0}, midiKey, I18n.t("earTraining.backgroundChord"));
        ex.placeBackgroundChord(chord);
        ex.placeTonalContext(midiKey);
        for (int i = 0; i < 32 && !ex.isOver(); i++) {
            int note = Utilities.randFromList(pool);
            ex.placeNote(note, ex.ONE_BAR, false);
        }
        placeEndOfScore(ex);
    }

    /**
     * Ex3: Escolta una nota i endevina quina és (to menor).
     * Teclat dret, retard 2 beats.
     * Els noms de notes s'amaguen per no revelar la resposta.
     */
    private static void guessNoteMinor(MyExercise ex) {
        ex.setDescription(I18n.t("dodeRebu.ex3.description"));
        int midiKey = ex.useMidiKeyForExercise(ToneRange.getMidi(MyKeyCircles.randm()));
        ex.setScaleMode('m');
        ex.setUseScreenKeyboardRight(true);

        ex.setDelay(2 * ex.ONE_BEAT);
        MyTempo.setTempo(60);
        applyFitToTonality(ex, true);
        List<Integer> pool = validNotePool(ex);
        Chord chord = new Chord(0, new int[]{0}, midiKey, I18n.t("earTraining.backgroundChord"));
        ex.placeBackgroundChord(chord);
        ex.placeTonalContextMinor(midiKey);
        for (int i = 0; i < 32 && !ex.isOver(); i++) {
            int note = Utilities.randFromList(pool);
            ex.placeNote(note, ex.ONE_BAR, false);
        }
        placeEndOfScore(ex);
    }

    /**
     * Ex4: Veus una nota a la partitura i la toques al teclat (to menor).
     * Teclat esquerre, sense retard.
     */
    private static void playNoteMinor(MyExercise ex) {
        ex.setDescription(I18n.t("dodeRebu.ex4.description"));
        int midiKey = ex.useMidiKeyForExercise(ToneRange.getMidi(MyKeyCircles.randm()));
        ex.setScaleMode('m');
        ex.setUseScreenKeyboardRight(false);
        MyTempo.setTempo(60);
        applyFitToTonality(ex, true);
        List<Integer> pool = validNotePool(ex);
        Chord chord = new Chord(0, new int[]{0}, midiKey, I18n.t("earTraining.backgroundChord"));
        ex.placeBackgroundChord(chord);
        ex.placeTonalContextMinor(midiKey);
        for (int i = 0; i < 32 && !ex.isOver(); i++) {
            int note = Utilities.randFromList(pool);
            ex.placeNote(note, ex.ONE_BAR, false);
        }
        placeEndOfScore(ex);
    }

    /**
     * [CA] Col·loca el marcador de fi de partitura just després de l'última nota.
     * Estableix stopCol al final del compàs que conté lastColWritten perquè
     * la reproducció s'aturi exactament al final de les 8 pàgines.
     * [EN] Places the end-of-score marker just after the last note.
     * Sets stopCol to the end of the measure containing lastColWritten so
     * playback stops exactly at the end of the 8 pages.
     */
    private static void placeEndOfScore(MyExercise ex) {
        int colsPerMeasure = ex.ONE_BAR;
        if (colsPerMeasure <= 0) colsPerMeasure = 1;
        int last = ex.getLastColWritten();
        int stopAt = ((last + colsPerMeasure - 1) / colsPerMeasure) * colsPerMeasure;
        stopAt = Math.min(stopAt, ex.getNumCols());
        ex.setStopCol(stopAt);
    }
}
