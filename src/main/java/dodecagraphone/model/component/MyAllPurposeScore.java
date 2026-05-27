/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.MyTempo;
import dodecagraphone.model.ToneRange;
import dodecagraphone.ui.I18n;

/**
 * [CA] Estén {@link MyMidiScore} i actua com a punt d'entrada de la partitura
 * activa. Concentra les operacions d'inicialització i restabliment de tots els
 * paràmetres de la partitura (tonalitat, mode, teclat, delay, opcions de
 * visualització). La resta de la lògica de lectura/escriptura rau a
 * {@link MyMidiScore} i les capes superiors.
 * <p>
 * [EN] Extends {@link MyMidiScore} and acts as the entry point of the active
 * score. Concentrates initialisation and reset operations for all score
 * parameters (key, mode, keyboard, delay, display options). The rest of the
 * read/write logic lives in {@link MyMidiScore} and higher layers.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MyAllPurposeScore extends MyMidiScore {

    /**
     * [CA] Crea una nova partitura de propòsit general i estableix la
     * tonalitat per defecte.
     * <p>
     * [EN] Creates a new all-purpose score and sets the default key.
     *
     * @param contr [CA] controlador principal / [EN] main controller
     */
    public MyAllPurposeScore(MyController contr) {
        super(contr);
        this.midiKey = ToneRange.getDefaultKey();
//        this.setNeedsDrawing(true);
    }

    /**
     * [CA] Restableix tots els paràmetres de la partitura als valors per
     * defecte: atura la reproducció, reinicia el patró i l'exercici,
     * estableix la tonalitat i el mode, configura l'opció de pantalla i
     * el delay, i actualitza les franges de color i el teclat.
     * <p>
     * [EN] Resets all score parameters to their default values: stops
     * playback, resets the pattern and exercise, sets the key and mode,
     * configures the screen keyboard option and delay, and updates the
     * colour strips and keyboard.
     */
    public void resetAllPurposeScore() {
        // defaults;
        this.stopAll();
        this.resetPattern();
        //this.resetMidiScore();
        this.resetExercise();

        this.choice.setNoneChoice();
        this.midiKey = ToneRange.getDefaultKey();
        this.usePentagramaStrips = true;
        this.showNoteNames = true;
        this.useMobileDo = false;
        this.useScreenKeyboardRight = false;
        this.setDefaultDelay();
        this.scaleMode = 'M';
        MyTempo.setTempo(60);
        resetMetadata();
        // updates;
        updateStripsNKeyboard(usePentagramaStrips);
        this.controller.setScreenKeyboardRight(useScreenKeyboardRight);
        ToneRange.setMovileDo(useMobileDo);
//        this.setNeedsDrawing(true);
        // showNoteNames;
    }

    public void resetMetadata() {
        this.setTitle(I18n.t("score.default.title"));
        this.setAuthor(I18n.t("score.default.author"));
        this.setDescription(I18n.t("score.default.description"));
    }

}
