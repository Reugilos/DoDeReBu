/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.exercise.exerciseList;

import dodecagraphone.MyController;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.component.MyAllPurposeScore;
import dodecagraphone.model.exercise.DoDeReBuExercises;
import dodecagraphone.model.exercise.Jazz;
import dodecagraphone.model.exercise.EarTraining;
import java.util.ArrayList;
import java.util.List;

/**
 * [CA] Llista de tots els exercicis disponibles a l'aplicació per a una família donada.
 * Gestiona la seqüència d'exercicis d'una família ({@link EarTraining} o {@link Jazz})
 * i proporciona navegació endavant ({@link #next()}) i endarrere ({@link #previous()})
 * a través de la llista circular d'etiquetes. La partitura activa ({@link MyAllPurposeScore})
 * es reconfigura automàticament cada cop que es canvia d'exercici.
 * <p>
 * [EN] List of all exercises available in the application for a given family.
 * Manages the exercise sequence of a family ({@link EarTraining} or {@link Jazz})
 * and provides forward ({@link #next()}) and backward ({@link #previous()}) navigation
 * through the circular label list. The active score ({@link MyAllPurposeScore})
 * is automatically reconfigured whenever the exercise changes.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 * @see EarTraining
 * @see Jazz
 */
public class MyExerciseList {

    private List<String> exerciseLabelList;
    private int nextEx, prevEx;
    private MyAllPurposeScore currentExercise;
    private MyController contr;

    /**
     * [CA] Retorna la llista d'etiquetes per a l'exercici «Round the Circle»,
     * que recorre les 12 tonalitats majors en ordre de quintes descendents.
     * <p>
     * [EN] Returns the label list for the «Round the Circle» exercise,
     * which traverses the 12 major keys in descending fifths order.
     *
     * @return [CA] llista de 12 etiquetes de tonalitats / [EN] list of 12 key labels
     */
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

    /**
     * [CA] Crea una nova llista d'exercicis per a la família indicada. Inicialitza
     * la partitura activa, assigna la família d'exercicis i carrega la llista
     * d'etiquetes. Les famílies suportades són {@code "EarTraining"} i {@code "Jazz"}.
     * <p>
     * [EN] Creates a new exercise list for the given family. Initializes the active
     * score, assigns the exercise family and loads the label list. Supported families
     * are {@code "EarTraining"} and {@code "Jazz"}.
     *
     * @param whichSet [CA] nom de la família d'exercicis ({@code "EarTraining"} o {@code "Jazz"}) /
     *                 [EN] name of the exercise family ({@code "EarTraining"} or {@code "Jazz"})
     * @param contr    [CA] referència al controlador principal / [EN] reference to the main controller
     * @throws UnsupportedOperationException [CA] si el nom de família no és reconegut /
     *                                       [EN] if the family name is not recognized
     */
    public MyExerciseList(String whichSet, MyController contr) {
        this.contr = contr;
        exerciseLabelList = new ArrayList<>();
        this.currentExercise = this.contr.getAllPurposeScore();
        currentExercise.resetAllPurposeScore();
        switch (whichSet) {
            case "EarTraining":
                EarTraining ear = new EarTraining();
                currentExercise.setFamily(ear);
                exerciseLabelList = EarTraining.getExerciseLabelList();
                break;
            case "Jazz":
                Jazz jazz = new Jazz();
                currentExercise.setFamily(jazz);
                exerciseLabelList = Jazz.getExerciseLabelList();
                break;
            case "DoDeReBuExercises":
                DoDeReBuExercises ddrbEx = new DoDeReBuExercises();
                currentExercise.setFamily(ddrbEx);
                exerciseLabelList = DoDeReBuExercises.getExerciseLabelList();
                break;
            default:
                throw new UnsupportedOperationException("No such exercise family " + whichSet);
        }
        nextEx = 0;
        prevEx = exerciseLabelList.size() - 1;
    }

    /**
     * [CA] Indica si la llista d'exercicis és buida.
     * <p>
     * [EN] Returns whether the exercise list is empty.
     *
     * @return {@code true} si la llista és buida o {@code null} /
     *         {@code true} if the list is empty or {@code null}
     */
    public boolean isEmpty() {
        if (exerciseLabelList == null) {
            return true;
        }
        return exerciseLabelList.isEmpty();
    }

    /**
     * [CA] Retorna la llista d'etiquetes de tots els exercicis de la família.
     * <p>
     * [EN] Returns the list of labels for all exercises in the family.
     *
     * @return [CA] llista d'etiquetes / [EN] list of labels
     */
    public List<String> getExerciseLabelList() {
        return exerciseLabelList;
    }

    /**
     * [CA] Reinicia l'exercici actual (amb la mateixa etiqueta) des del principi.
     * <p>
     * [EN] Restarts the current exercise (with the same label) from the beginning.
     */
    public void resetCurrentExercise() {
        currentExercise.setExercise(currentExercise.getLabel());
    }

    /**
     * [CA] Avança a l'exercici següent de la llista circular i el carrega
     * a la partitura activa. Retorna la partitura activa configurada,
     * o {@code null} si la llista és buida.
     * <p>
     * [EN] Advances to the next exercise in the circular list and loads it
     * into the active score. Returns the configured active score,
     * or {@code null} if the list is empty.
     *
     * @return [CA] la partitura activa configurada amb l'exercici següent, o {@code null} /
     *         [EN] the active score configured with the next exercise, or {@code null}
     */
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

    /**
     * [CA] Retrocedeix a l'exercici anterior de la llista circular i el carrega
     * a la partitura activa. Retorna la partitura activa configurada,
     * o {@code null} si la llista és buida.
     * <p>
     * [EN] Goes back to the previous exercise in the circular list and loads it
     * into the active score. Returns the configured active score,
     * or {@code null} if the list is empty.
     *
     * @return [CA] la partitura activa configurada amb l'exercici anterior, o {@code null} /
     *         [EN] the active score configured with the previous exercise, or {@code null}
     */
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
