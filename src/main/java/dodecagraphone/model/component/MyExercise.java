/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.MyKeyCircles;
import dodecagraphone.model.MyTempo;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.exercise.MyExerciseFamily;
import dodecagraphone.ui.Settings;

/**
 * [CA] Representa un exercici d'entrenament auditiu (ear training). Estén
 * {@code MyPatternScore} amb gestió de família d'exercicis i estat de primera
 * execució. El mètode {@code setExercise(label)} inicialitza tots els paràmetres
 * per defecte i aplica la configuració específica de l'exercici via
 * {@code MyExerciseFamily}.
 * <p>
 * [EN] Represents an ear training exercise. Extends {@code MyPatternScore} with
 * exercise family management and first-run state. The method
 * {@code setExercise(label)} initialises all default parameters and applies the
 * exercise-specific configuration via {@code MyExerciseFamily}.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MyExercise extends MyPatternScore {

    /** [CA] Flag que indica si és la primera vegada que s'executa l'exercici. */
    protected boolean firstTime = true;
    /** [CA] Família d'exercicis activa (pot ser null si no n'hi ha cap). */
    protected MyExerciseFamily family;

    /**
     * [CA] Constructor. Inicialitza amb la família a null.
     * <p>
     * [EN] Constructor. Initialises with the family set to null.
     *
     * @param contr [CA] referència al controlador principal / [EN] reference to the main controller
     */
    public MyExercise(MyController contr) {
        super(contr);
        this.family = null;
    }

    /**
     * [CA] Reseteja l'exercici posant la família a null.
     * <p>
     * [EN] Resets the exercise by setting the family to null.
     */
    public void resetExercise(){
        this.family = null;
    }

    /**
     * [CA] Estableix la família d'exercicis.
     * <p>
     * [EN] Sets the exercise family.
     *
     * @param family [CA] nova família d'exercicis / [EN] new exercise family
     */
    public void setExerciseFamily(MyExerciseFamily family){
        this.family = family;
    }

    /**
     * [CA] Retorna si és la primera vegada que s'executa l'exercici.
     * <p>
     * [EN] Returns whether it is the first time the exercise runs.
     *
     * @return [CA] true si és la primera vegada / [EN] true if it is the first run
     */
    public boolean isFirstTime() {
        return firstTime;
    }

    /**
     * [CA] Estableix el flag de primera execució.
     * <p>
     * [EN] Sets the first-run flag.
     *
     * @param firstTime [CA] true per indicar primera execució / [EN] true to indicate first run
     */
    public void setFirstTime(boolean firstTime) {
        this.firstTime = firstTime;
    }

    /**
     * [CA] Retorna la família d'exercicis activa.
     * <p>
     * [EN] Returns the active exercise family.
     *
     * @return [CA] família d'exercicis o null / [EN] exercise family or null
     */
    public MyExerciseFamily getFamily() {
        return family;
    }

    /**
     * [CA] Estableix la família d'exercicis.
     * <p>
     * [EN] Sets the exercise family.
     *
     * @param family [CA] nova família d'exercicis / [EN] new exercise family
     */
    public void setFamily(MyExerciseFamily family) {
        this.family = family;
    }

    /**
     * [CA] Aplica l'exercici identificat per {@code label} dins de la família activa.
     * <p>
     * [EN] Applies the exercise identified by {@code label} within the active family.
     *
     * @param label [CA] etiqueta de l'exercici / [EN] exercise label
     * @throws IllegalStateException [CA] si no hi ha família definida / [EN] if no family is defined
     */
    public void setCurrentExercise(String label) {
        if (family != null) {
            family.applyExercise(this, label);
        } else {
            throw new IllegalStateException("No exercise family defined.");
        }
    }

    /**
     * [CA] Inicialitza tots els paràmetres per defecte de l'exercici (escala, tempo,
     * teclat, nota base) i aplica la configuració específica via
     * {@code setCurrentExercise(label)}.
     * <p>
     * [EN] Initialises all default exercise parameters (scale, tempo, keyboard, root note)
     * and applies the specific configuration via {@code setCurrentExercise(label)}.
     *
     * @param label [CA] etiqueta de l'exercici a carregar / [EN] label of the exercise to load
     */
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
        // Evita que midiKey + 12 superi highestMidi i provoqui que l'octava
        // superior aparegui una octava per sota (wrapping de midiToKeyId).
        // Prevents midiKey + 12 from exceeding highestMidi, which would cause
        // the upper-octave note to wrap one octave downward via midiToKeyId.
        {
            int hi = ToneRange.getHighestMidi() - 12;
            int lo = ToneRange.getLowestMidi();
            while (this.midiKey > hi) this.midiKey -= 12;
            while (this.midiKey < lo) this.midiKey += 12;
        }

        setCurrentExercise(label);

        this.author = "Tradicional";
        this.title = this.label;
        updateStripsNKeyboard(usePentagramaStrips);
        this.controller.setScreenKeyboardRight(useScreenKeyboardRight);
        ToneRange.setMovileDo(useMobileDo);
        this.initOffscreen();
    }
}
