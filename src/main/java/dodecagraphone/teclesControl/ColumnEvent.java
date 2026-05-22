/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.teclesControl;

import dodecagraphone.MyController;
import dodecagraphone.model.chord.Chord;
import dodecagraphone.model.component.MyGridScore;
import dodecagraphone.model.component.MyLyrics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sound.midi.MidiMessage;

/**
 * [CA] Event undo/redo per a la inserció o eliminació de N columnes consecutives
 * a la partitura. En mode inserció, l'undo elimina les mateixes N columnes. En mode
 * eliminació, el contingut de les columnes es desa en {@link ColSnapshot} abans
 * d'esborrar per poder-lo restaurar en l'undo.
 * <p>
 * [EN] Undo/redo event for inserting or deleting N consecutive columns in the score.
 * For insert: undo deletes the same N columns. For delete: column contents are saved
 * in {@link ColSnapshot} before deletion so undo can restore them.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class ColumnEvent extends Event {

    /**
     * [CA] Instantània del contingut d'una columna de partitura, usada per
     * restaurar una columna eliminada en l'operació undo.
     * <p>
     * [EN] Snapshot of a single score column, used to restore a deleted column on undo.
     */
    public static class ColSnapshot {
        /**
         * [CA] Notes per fila: mapa de fila → llista d'arrays [canal, track, velocitat,
         * isVisible, isMuted, isLinked, isDotted].
         * <p>
         * [EN] Notes per row: map of row → list of arrays [channel, track, velocity,
         * isVisible, isMuted, isLinked, isDotted].
         */
        public final Map<Integer, List<int[]>> gridRows = new HashMap<>();
        /** [CA] Acord del chord symbol line. / [EN] Chord in the chord symbol line. */
        public Chord chord;
        /** [CA] Acord del background chord line. / [EN] Chord in the background chord line. */
        public Chord bgChord;
        /** [CA] Canvi de paràmetres (ScoreChange) registrat en aquesta columna. / [EN] Parameter change (ScoreChange) recorded at this column. */
        public MyGridScore.ScoreChange change;
        /** [CA] Missatge de text associat a la columna. / [EN] Text message associated with the column. */
        public String message;
        /** [CA] Missatges MIDI associats a la columna. / [EN] MIDI messages associated with the column. */
        public ArrayList<MidiMessage> midiMsg;
        /** [CA] Segments de lletra associats a la columna. / [EN] Lyric segments associated with the column. */
        public final List<MyLyrics.LyricSegment> lyricSegments = new ArrayList<>();
    }

    private final MyController controller;
    private final int col;
    private final int n;
    private final boolean insert;
    private final List<ColSnapshot> snapshots; // non-null only for delete events

    /**
     * [CA] Constructor per a un event d'inserció de columnes.
     * L'undo eliminarà les N columnes inserides.
     * <p>
     * [EN] Constructor for a column insert event.
     * Undo will delete the N inserted columns.
     *
     * @param controller [CA] controlador principal / [EN] main controller
     * @param col        [CA] columna on s'insereix / [EN] column where insertion starts
     * @param n          [CA] nombre de columnes a inserir / [EN] number of columns to insert
     */
    public ColumnEvent(MyController controller, int col, int n) {
        this.controller = controller;
        this.col        = col;
        this.n          = n;
        this.insert     = true;
        this.snapshots  = null;
    }

    /**
     * [CA] Constructor per a un event d'eliminació de columnes.
     * L'undo reinserirà N columnes buides i en restaurarà el contingut des dels snapshots.
     * <p>
     * [EN] Constructor for a column delete event.
     * Undo will reinsert N empty columns and restore their content from the snapshots.
     *
     * @param controller [CA] controlador principal / [EN] main controller
     * @param col        [CA] columna on comença l'eliminació / [EN] column where deletion starts
     * @param n          [CA] nombre de columnes eliminades / [EN] number of columns deleted
     * @param snapshots  [CA] instantànies de les columnes eliminades / [EN] snapshots of the deleted columns
     */
    public ColumnEvent(MyController controller, int col, int n, List<ColSnapshot> snapshots) {
        this.controller = controller;
        this.col        = col;
        this.n          = n;
        this.insert     = false;
        this.snapshots  = snapshots;
    }

    /**
     * [CA] Redo: reinsereix les N columnes (si era inserció) o les elimina de nou
     * (si era eliminació).
     * <p>
     * [EN] Redo: reinserts the N columns (if it was an insert) or deletes them again
     * (if it was a delete).
     */
    @Override
    public void refer() {
        if (insert) {
            controller.insertNColumnsAt(col, n);
        } else {
            controller.deleteNColumnsAt(col, n);
        }
    }

    /**
     * [CA] Undo: elimina les N columnes (si era inserció) o reinsereix N columnes
     * buides i en restaura el contingut (si era eliminació).
     * <p>
     * [EN] Undo: deletes the N columns (if it was an insert) or reinserts N empty
     * columns and restores their content (if it was a delete).
     */
    @Override
    public void desfer() {
        if (insert) {
            controller.deleteNColumnsAt(col, n);
        } else {
            controller.insertNEmptyColumnsAt(col, n);
            for (int i = 0; i < n; i++) {
                controller.restoreColumnAt(col + i, snapshots.get(i));
            }
        }
    }
}
