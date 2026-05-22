/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.teclesControl;

import dodecagraphone.MyController;
import dodecagraphone.model.component.MyAllPurposeScore;
import dodecagraphone.model.component.MyComponent;
import dodecagraphone.model.component.MyGridSquare;
import dodecagraphone.ui.Settings;

/**
 * [CA] Event undo/redo per a un moviment de nota (arrossegament Ctrl+Shift).
 * Desa la posició original i la posició final de la nota; {@code refer()} mou
 * la nota de l'origen a la destinació i {@code desfer()} fa el moviment invers.
 * Suporta canvis tant de fila (alçada) com de columna.
 * <p>
 * [EN] Undo/redo event for a note move (Ctrl+Shift drag). Stores the original
 * and final position of the note; {@code refer()} moves the note from origin to
 * destination and {@code desfer()} reverses the move. Supports both row (pitch)
 * and column changes.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MoveNoteEvent extends Event {

    private final MyController controller;
    private final int origRow;
    private final int finalRow;
    private final int origHeadCol;
    private final int finalHeadCol;
    private final int length;
    private final int ch;
    private final int tr;
    private final int[] velocities;
    private final boolean[] visibles;
    private final boolean[] muteds;
    private final boolean[] linkeds;
    private final boolean dotted;

    /**
     * [CA] Crea un nou event de moviment de nota.
     * <p>
     * [EN] Creates a new note move event.
     *
     * @param controller    [CA] controlador principal / [EN] main controller
     * @param origRow       [CA] fila original de la nota / [EN] original row of the note
     * @param finalRow      [CA] fila de destinació / [EN] destination row
     * @param origHeadCol   [CA] columna del cap original de la nota / [EN] original head column of the note
     * @param finalHeadCol  [CA] columna del cap de destinació / [EN] destination head column
     * @param length        [CA] longitud de la nota en columnes / [EN] note length in columns
     * @param ch            [CA] canal MIDI / [EN] MIDI channel
     * @param tr            [CA] id de la pista / [EN] track id
     * @param velocities    [CA] velocitats de cada cel·la de la nota / [EN] velocities for each cell of the note
     * @param visibles      [CA] visibilitat de cada cel·la / [EN] visibility of each cell
     * @param muteds        [CA] muted de cada cel·la / [EN] muted state of each cell
     * @param linkeds       [CA] linked de cada cel·la / [EN] linked state of each cell
     * @param dotted        [CA] si la nota és puntejada / [EN] whether the note is dotted
     */
    public MoveNoteEvent(MyController controller,
                         int origRow, int finalRow,
                         int origHeadCol, int finalHeadCol, int length,
                         int ch, int tr,
                         int[] velocities, boolean[] visibles,
                         boolean[] muteds, boolean[] linkeds, boolean dotted) {
        this.controller = controller;
        this.origRow = origRow;
        this.finalRow = finalRow;
        this.origHeadCol = origHeadCol;
        this.finalHeadCol = finalHeadCol;
        this.length = length;
        this.ch = ch;
        this.tr = tr;
        this.velocities = velocities.clone();
        this.visibles = visibles.clone();
        this.muteds = muteds.clone();
        this.linkeds = linkeds.clone();
        this.dotted = dotted;
    }

    /**
     * [CA] Redo: elimina la nota de la posició original i la col·loca a la
     * posició final.
     * <p>
     * [EN] Redo: removes the note from the original position and places it at
     * the final position.
     */
    @Override
    public void refer() {
        removeRange(origRow, origHeadCol);
        placeRange(finalRow, finalHeadCol);
        controller.getAllPurposeScore().updateStopMarker();
    }

    /**
     * [CA] Undo: elimina la nota de la posició final i la col·loca a la posició
     * original.
     * <p>
     * [EN] Undo: removes the note from the final position and places it back at
     * the original position.
     */
    @Override
    public void desfer() {
        removeRange(finalRow, finalHeadCol);
        placeRange(origRow, origHeadCol);
        controller.getAllPurposeScore().updateStopMarker();
    }

    private void removeRange(int row, int headCol) {
        MyAllPurposeScore score = controller.getAllPurposeScore();
        for (int i = length - 1; i >= 0; i--) {
            score.removeNoteFromSquare(row, headCol + i, ch, tr);
            MyGridSquare sq = score.getGridSquare(row, headCol + i);
            if (sq != null) sq.updateState();
        }
    }

    private void placeRange(int row, int headCol) {
        MyAllPurposeScore score = controller.getAllPurposeScore();
        for (int i = 0; i < length; i++) {
            MyGridSquare sq = score.addNoteToSquare(
                    row, headCol + i, 1, Settings.getnRowsSquare(),
                    (MyComponent) score, controller, score, controller.getCam(),
                    ch, tr, velocities[i], visibles[i], muteds[i], linkeds[i], dotted);
            sq.updateState();
        }
    }
}
