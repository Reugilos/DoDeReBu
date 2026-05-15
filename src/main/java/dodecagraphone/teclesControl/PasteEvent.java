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
    private final boolean multiTrack;

    public PasteEvent(MyController controller, List<ClipNote> notes,
                      int anchorRow, int anchorCol, int targetCh, int targetTr,
                      boolean multiTrack) {
        this.controller = controller;
        this.notes      = notes;
        this.anchorRow  = anchorRow;
        this.anchorCol  = anchorCol;
        this.targetCh   = targetCh;
        this.targetTr   = targetTr;
        this.multiTrack = multiTrack;
    }

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
    }

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
    }
}
