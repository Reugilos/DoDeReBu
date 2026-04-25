package dodecagraphone.teclesControl;

import dodecagraphone.MyController;
import dodecagraphone.model.component.MyAllPurposeScore;
import dodecagraphone.model.component.MyComponent;
import dodecagraphone.model.component.MyGridSquare;
import dodecagraphone.ui.Settings;

/**
 * Undo/redo event for a note move (Ctrl+Shift drag) — supports row and column changes.
 * refer() moves orig→final, desfer() moves final→orig.
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

    /** Redo: remove from (origRow, origHeadCol), place at (finalRow, finalHeadCol). */
    @Override
    public void refer() {
        removeRange(origRow, origHeadCol);
        placeRange(finalRow, finalHeadCol);
        controller.getAllPurposeScore().updateStopMarker();
    }

    /** Undo: remove from (finalRow, finalHeadCol), place at (origRow, origHeadCol). */
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
