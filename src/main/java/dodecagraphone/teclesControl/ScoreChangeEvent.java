/*
 * PolyForm Noncommercial License 1.0.0
 * Copyright (c) 2024-2026 Pau Bofill. Powered by Claude AI.
 * Full license / Llicència completa: LICENSE (project root / arrel del projecte)
 */
package dodecagraphone.teclesControl;

import dodecagraphone.MyController;
import dodecagraphone.model.component.MyGridScore;

/**
 * [CA] Event undo/redo per a les marques de canvi de paràmetres (tempo,
 * tonalitat, volum, compàs) del {@code changeMap}. Desa l'entrada sencera
 * anterior i la resultant; {@code refer()} aplica la nova i {@code desfer()}
 * restaura l'antiga. Un valor {@code null} indica que no hi havia entrada en
 * aquella columna.
 * <p>
 * Quan la marca és de tonalitat i s'ha transposat la partitura, el mateix event
 * desfà també la transposició, en ordre invers al de la col·locació.
 * <p>
 * [EN] Undo/redo event for parameter change marks (tempo, key, volume, time
 * signature) in the {@code changeMap}. Stores the whole previous entry and the
 * resulting one; {@code refer()} applies the new one and {@code desfer()}
 * restores the old one. A {@code null} value means there was no entry at that
 * column.
 * <p>
 * When the mark is a key change that transposed the score, the same event also
 * reverses the transposition, in the reverse order of the placement.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class ScoreChangeEvent extends Event {

    private final MyController controller;
    private final int col;
    /** Entrada anterior del changeMap (null = no n'hi havia). */
    private final MyGridScore.ScoreChange oldChange;
    /** Entrada resultant del changeMap (null = l'entrada s'ha esborrat). */
    private final MyGridScore.ScoreChange newChange;
    /** Semitons de transposició associats al canvi de to (0 = cap). */
    private final int transposeStep;
    /** Cert si la transposició va afectar les notes; fals si només el choice. */
    private final boolean transposeNotes;

    /**
     * [CA] Crea un event de marca de canvi sense transposició associada.
     * <p>
     * [EN] Creates a change-mark event with no associated transposition.
     *
     * @param controller [CA] controlador principal / [EN] main controller
     * @param col        [CA] columna de la marca / [EN] column of the mark
     * @param oldChange  [CA] entrada anterior, o null / [EN] previous entry, or null
     * @param newChange  [CA] entrada resultant, o null / [EN] resulting entry, or null
     */
    public ScoreChangeEvent(MyController controller, int col,
                            MyGridScore.ScoreChange oldChange,
                            MyGridScore.ScoreChange newChange) {
        this(controller, col, oldChange, newChange, 0, false);
    }

    /**
     * [CA] Crea un event de marca de canvi.
     * <p>
     * [EN] Creates a change-mark event.
     *
     * @param controller     [CA] controlador principal / [EN] main controller
     * @param col            [CA] columna de la marca / [EN] column of the mark
     * @param oldChange      [CA] entrada anterior, o null / [EN] previous entry, or null
     * @param newChange      [CA] entrada resultant, o null / [EN] resulting entry, or null
     * @param transposeStep  [CA] semitons transposats en col·locar la marca (0 = cap) /
     *                       [EN] semitones transposed when placing the mark (0 = none)
     * @param transposeNotes [CA] cert si es van transposar les notes / [EN] true if notes were transposed
     */
    public ScoreChangeEvent(MyController controller, int col,
                            MyGridScore.ScoreChange oldChange,
                            MyGridScore.ScoreChange newChange,
                            int transposeStep, boolean transposeNotes) {
        this.controller     = controller;
        this.col            = col;
        this.oldChange      = (oldChange == null) ? null : oldChange.copy();
        this.newChange      = (newChange == null) ? null : newChange.copy();
        this.transposeStep  = transposeStep;
        this.transposeNotes = transposeNotes;
    }

    /**
     * [CA] Redo: transposa (si escau) i torna a aplicar l'entrada nova.
     * <p>
     * [EN] Redo: transposes (if applicable) and re-applies the new entry.
     */
    @Override
    public void refer() {
        if (transposeStep != 0) {
            controller.applyMarkTranspose(transposeStep, transposeNotes);
        }
        controller.applyScoreChangeAt(col, newChange);
    }

    /**
     * [CA] Undo: restaura l'entrada anterior i després inverteix la
     * transposició (ordre invers al de la col·locació, que transposa abans
     * d'escriure el canvi).
     * <p>
     * [EN] Undo: restores the previous entry and then reverses the
     * transposition (reverse order of the placement, which transposes before
     * writing the change).
     */
    @Override
    public void desfer() {
        controller.applyScoreChangeAt(col, oldChange);
        if (transposeStep != 0) {
            controller.applyMarkTranspose(-transposeStep, transposeNotes);
        }
    }
}
