/*
 * PolyForm Noncommercial License 1.0.0
 * Copyright (c) 2024-2026 Pau Bofill. Powered by Claude AI.
 * Full license / Llicència completa: LICENSE (project root / arrel del projecte)
 */
package dodecagraphone.teclesControl;

import dodecagraphone.MyController;
import dodecagraphone.model.chord.Chord;
import dodecagraphone.model.component.MyAllPurposeScore;
import dodecagraphone.model.component.MyComponent;
import dodecagraphone.model.component.MyGridSquare;
import dodecagraphone.model.mixer.MyTrack;
import dodecagraphone.ui.Settings;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * [CA] Event undo/redo per a l'operació d'enganxar notes (paste). {@code refer()}
 * reafegeix totes les notes del porta-retalls a la posició d'ancoratge i
 * {@code desfer()} les elimina en ordre invers. Suporta mode multi-pista
 * (cada nota va al seu track original) i mode mono-pista (totes van al
 * track de destinació indicat).
 * <p>
 * [EN] Undo/redo event for the paste operation. {@code refer()} re-adds all
 * clipboard notes at the anchor position and {@code desfer()} removes them in
 * reverse order. Supports multi-track mode (each note goes to its original track)
 * and single-track mode (all go to the specified target track).
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class PasteEvent extends Event {

    private final MyController controller;
    private final List<ClipNote> notes;
    private final int anchorRow;
    private final int anchorCol;
    private final int targetCh;
    private final int targetTr;
    private final boolean multiTrack;
    /** Accords a enganxar: col absoluta → nou acord. Buit si no hi ha accords. */
    private final Map<Integer, Chord> newChords;
    /** Accords que hi havia abans del paste: col absoluta → acord antic (null=no n'hi havia). */
    private final Map<Integer, Chord> oldChords;
    /** Lletres noves enganxades (col relativa a l'ancoratge). Buit si no n'hi ha. */
    private final List<ClipLyric> newLyrics;
    /** Lletres que hi havia abans del paste (per a desfer). Buit si no n'hi havia. */
    private final List<ClipLyric> oldLyrics;

    /**
     * [CA] Crea un nou event de paste.
     * <p>
     * [EN] Creates a new paste event.
     *
     * @param controller [CA] controlador principal / [EN] main controller
     * @param notes      [CA] llista de notes del porta-retalls a enganxar / [EN] list of clipboard notes to paste
     * @param anchorRow  [CA] fila d'ancoratge (fila superior de la selecció) / [EN] anchor row (top row of the selection)
     * @param anchorCol  [CA] columna d'ancoratge (columna esquerra de la selecció) / [EN] anchor column (left column of the selection)
     * @param targetCh   [CA] canal MIDI de destinació (mode mono-pista) / [EN] target MIDI channel (single-track mode)
     * @param targetTr   [CA] id de la pista de destinació (mode mono-pista) / [EN] target track id (single-track mode)
     * @param multiTrack [CA] si és cert, cada nota va al seu track original / [EN] if true, each note goes to its original track
     */
    public PasteEvent(MyController controller, List<ClipNote> notes,
                      int anchorRow, int anchorCol, int targetCh, int targetTr,
                      boolean multiTrack) {
        this(controller, notes, anchorRow, anchorCol, targetCh, targetTr, multiTrack,
             Collections.emptyMap(), Collections.emptyMap(),
             Collections.emptyList(), Collections.emptyList());
    }

    public PasteEvent(MyController controller, List<ClipNote> notes,
                      int anchorRow, int anchorCol, int targetCh, int targetTr,
                      boolean multiTrack,
                      Map<Integer, Chord> newChords, Map<Integer, Chord> oldChords) {
        this(controller, notes, anchorRow, anchorCol, targetCh, targetTr, multiTrack,
             newChords, oldChords, Collections.emptyList(), Collections.emptyList());
    }

    public PasteEvent(MyController controller, List<ClipNote> notes,
                      int anchorRow, int anchorCol, int targetCh, int targetTr,
                      boolean multiTrack,
                      Map<Integer, Chord> newChords, Map<Integer, Chord> oldChords,
                      List<ClipLyric> newLyrics, List<ClipLyric> oldLyrics) {
        this.controller = controller;
        this.notes      = notes;
        this.anchorRow  = anchorRow;
        this.anchorCol  = anchorCol;
        this.targetCh   = targetCh;
        this.targetTr   = targetTr;
        this.multiTrack = multiTrack;
        this.newChords  = newChords;
        this.oldChords  = oldChords;
        this.newLyrics  = newLyrics;
        this.oldLyrics  = oldLyrics;
    }

    /**
     * [CA] Redo: reafegeix totes les notes del porta-retalls a la posició
     * d'ancoratge.
     * <p>
     * [EN] Redo: re-adds all clipboard notes at the anchor position.
     */
    @Override
    public void refer() {
        MyAllPurposeScore score = controller.getAllPurposeScore();
        int nKeys = score.getnKeys();
        int nCols = score.getNumCols();
        for (ClipNote n : notes) {
            int row = anchorRow + n.rowOffset;
            int col = anchorCol + n.colOffset;
            if (row < 0 || row >= nKeys || col < 0 || col >= nCols) continue;
            int ch = multiTrack ? n.channel : targetCh;
            int tr = multiTrack ? n.trackId : targetTr;
            MyTrack track = controller.getMixer().getTrackFromId(tr);
            if (track == null) continue;
            track.oneNoteMore();
            MyGridSquare sq = score.addNoteToSquare(
                    row, col, 1, Settings.getnRowsSquare(),
                    (MyComponent) score, controller, score, controller.getCam(),
                    ch, tr, n.velocity, n.visible, n.muted, n.linked, n.dotted);
            sq.updateState();
            if (col + 1 > score.getLastColWritten()) score.setLastColWritten(col + 1);
        }
        score.updateStopMarker();
        if (!newChords.isEmpty()) {
            for (Map.Entry<Integer, Chord> e : newChords.entrySet()) {
                score.placeChordSymbol(e.getValue(), e.getKey());
            }
            controller.redrawChordLine();
        }
        if (!newLyrics.isEmpty()) {
            for (ClipLyric cl : newLyrics) {
                controller.getMyLyrics().setLyric(anchorCol + cl.colOffset, cl.trackId, cl.text);
            }
            controller.getMyLyrics().drawFullLyricsInOffscreen();
        }
    }

    /**
     * [CA] Undo: elimina totes les notes enganxades en ordre invers. Si una
     * nota ja no existeix (null guard), s'ignora.
     * <p>
     * [EN] Undo: removes all pasted notes in reverse order. If a note no longer
     * exists (null guard) it is skipped.
     */
    @Override
    public void desfer() {
        MyAllPurposeScore score = controller.getAllPurposeScore();
        int nKeys = score.getnKeys();
        int nCols = score.getNumCols();
        for (int i = notes.size() - 1; i >= 0; i--) {
            ClipNote n = notes.get(i);
            int row = anchorRow + n.rowOffset;
            int col = anchorCol + n.colOffset;
            if (row < 0 || row >= nKeys || col < 0 || col >= nCols) continue;
            int ch = multiTrack ? n.channel : targetCh;
            int tr = multiTrack ? n.trackId : targetTr;
            MyTrack track = controller.getMixer().getTrackFromId(tr);
            MyGridSquare.SubSquare removed = score.removeNoteFromSquare(row, col, ch, tr);
            if (removed == null) continue;
            if (track != null) track.oneNoteLess();
            MyGridSquare sq = score.getGridSquare(row, col);
            if (sq != null) sq.updateState();
        }
        score.updateStopMarker();
        if (!oldChords.isEmpty()) {
            for (Map.Entry<Integer, Chord> e : oldChords.entrySet()) {
                if (e.getValue() != null) {
                    score.placeChordSymbol(e.getValue(), e.getKey());
                } else {
                    score.removeChordSymbol(e.getKey());
                }
            }
            controller.redrawChordLine();
        }
        if (!newLyrics.isEmpty() || !oldLyrics.isEmpty()) {
            // Primer elimina les lletres que vam enganxar
            for (ClipLyric cl : newLyrics) {
                controller.getMyLyrics().removeLyric(anchorCol + cl.colOffset, cl.trackId);
            }
            // Després restaura les que hi havia abans
            for (ClipLyric cl : oldLyrics) {
                controller.getMyLyrics().setLyric(anchorCol + cl.colOffset, cl.trackId, cl.text);
            }
            controller.getMyLyrics().drawFullLyricsInOffscreen();
        }
    }
}
