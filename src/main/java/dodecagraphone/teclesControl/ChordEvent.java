/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.teclesControl;

import dodecagraphone.MyController;
import dodecagraphone.model.chord.Chord;

/**
 * [CA] Event undo/redo per a la col·locació o eliminació d'un símbol d'acord a
 * la franja d'acords. Desa l'acord antic i el nou; {@code refer()} aplica el nou
 * i {@code desfer()} restaura l'antic. Un valor {@code null} en qualsevol dels
 * dos indica que no hi havia cap acord en aquella posició.
 * <p>
 * [EN] Undo/redo event for placing or removing a chord symbol in the chord strip.
 * Stores the old chord and the new chord; {@code refer()} applies the new one and
 * {@code desfer()} restores the old one. A {@code null} value in either field
 * means there was no chord at that position.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class ChordEvent extends Event {

    private final MyController controller;
    private final int col;
    private final Chord oldChord; // null = no hi havia acord
    private final Chord newChord; // null = operació d'esborrat

    /**
     * [CA] Crea un nou event de canvi d'acord.
     * <p>
     * [EN] Creates a new chord change event.
     *
     * @param controller [CA] controlador principal / [EN] main controller
     * @param col        [CA] columna de partitura on es col·loca/esborra l'acord / [EN] score column where the chord is placed or removed
     * @param oldChord   [CA] acord existent prèviament (null si no n'hi havia) / [EN] previously existing chord (null if none)
     * @param newChord   [CA] nou acord a col·locar (null per a esborrat) / [EN] new chord to place (null to delete)
     */
    public ChordEvent(MyController controller, int col, Chord oldChord, Chord newChord) {
        this.controller = controller;
        this.col        = col;
        this.oldChord   = oldChord;
        this.newChord   = newChord;
    }

    /**
     * [CA] Redo: aplica el nou acord a la columna.
     * <p>
     * [EN] Redo: applies the new chord at the column.
     */
    @Override
    public void refer() {
        applyChord(newChord);
    }

    /**
     * [CA] Undo: restaura l'acord anterior a la columna.
     * <p>
     * [EN] Undo: restores the old chord at the column.
     */
    @Override
    public void desfer() {
        applyChord(oldChord);
    }

    private void applyChord(Chord chord) {
        if (chord == null) {
            controller.getAllPurposeScore().removeChordSymbol(col);
        } else {
            controller.getAllPurposeScore().placeChordSymbol(chord, col);
        }
        controller.redrawChordLine();
    }
}
