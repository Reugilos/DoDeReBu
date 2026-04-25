package dodecagraphone.teclesControl;

import dodecagraphone.MyController;
import dodecagraphone.model.component.MyAllPurposeScore;
import dodecagraphone.model.component.MyComponent;
import dodecagraphone.model.component.MyGridSquare;
import dodecagraphone.model.mixer.MyTrack;
import dodecagraphone.ui.Settings;
import java.util.List;

/** Undo/redo event for a paste operation. refer() re-adds notes, desfer() removes them. */
public class PasteEvent extends Event {

    private final MyController controller;
    private final List<ClipNote> notes;
    private final int anchorRow;
    private final int anchorCol;
    private final int targetCh;
    private final int targetTr;

    public PasteEvent(MyController controller, List<ClipNote> notes,
                      int anchorRow, int anchorCol, int targetCh, int targetTr) {
        this.controller = controller;
        this.notes      = notes;
        this.anchorRow  = anchorRow;
        this.anchorCol  = anchorCol;
        this.targetCh   = targetCh;
        this.targetTr   = targetTr;
    }

    @Override
    public void refer() {
        MyAllPurposeScore score = controller.getAllPurposeScore();
        MyTrack track = controller.getMixer().getTrackFromId(targetTr);
        int gridRows = score.getGrid().length;
        int gridCols = score.getGrid()[0].length;
        for (ClipNote n : notes) {
            int row = anchorRow + n.rowOffset;
            int col = anchorCol + n.colOffset;
            if (row < 0 || row >= gridRows || col < 0 || col >= gridCols) continue;
            track.oneNoteMore();
            MyGridSquare sq = score.addNoteToSquare(
                    row, col, 1, Settings.getnRowsSquare(),
                    (MyComponent) score, controller, score, controller.getCam(),
                    targetCh, targetTr, n.velocity, n.visible, n.muted, n.linked, n.dotted);
            sq.updateState();
            if (col + 1 > score.getLastColWritten()) score.setLastColWritten(col + 1);
        }
        score.updateStopMarker();
    }

    @Override
    public void desfer() {
        MyAllPurposeScore score = controller.getAllPurposeScore();
        MyTrack track = controller.getMixer().getTrackFromId(targetTr);
        int gridRows = score.getGrid().length;
        int gridCols = score.getGrid()[0].length;
        for (int i = notes.size() - 1; i >= 0; i--) {
            ClipNote n = notes.get(i);
            int row = anchorRow + n.rowOffset;
            int col = anchorCol + n.colOffset;
            if (row < 0 || row >= gridRows || col < 0 || col >= gridCols) continue;
            score.removeNoteFromSquare(row, col, targetCh, targetTr);
            track.oneNoteLess();
            MyGridSquare sq = score.getGridSquare(row, col);
            if (sq != null) sq.updateState();
        }
        score.updateStopMarker();
    }
}
