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
 * Undo/redo event for inserting or deleting N columns in the score.
 *
 * For insert: undo = delete the same N columns.
 * For delete: column contents are saved before deletion so undo can restore them.
 */
public class ColumnEvent extends Event {

    /**
     * Snapshot of a single score column, used to restore a deleted column on undo.
     */
    public static class ColSnapshot {
        /** Per grid-row list of [channel, track, velocity, isVisible, isMuted, isLinked, isDotted]. */
        public final Map<Integer, List<int[]>> gridRows = new HashMap<>();
        public Chord chord;
        public Chord bgChord;
        public MyGridScore.ScoreChange change;
        public String message;
        public ArrayList<MidiMessage> midiMsg;
        public final List<MyLyrics.LyricSegment> lyricSegments = new ArrayList<>();
    }

    private final MyController controller;
    private final int col;
    private final int n;
    private final boolean insert;
    private final List<ColSnapshot> snapshots; // non-null only for delete events

    /** Constructor for INSERT event (undo = delete N columns). */
    public ColumnEvent(MyController controller, int col, int n) {
        this.controller = controller;
        this.col        = col;
        this.n          = n;
        this.insert     = true;
        this.snapshots  = null;
    }

    /** Constructor for DELETE event (undo = insert N empty + restore snapshots). */
    public ColumnEvent(MyController controller, int col, int n, List<ColSnapshot> snapshots) {
        this.controller = controller;
        this.col        = col;
        this.n          = n;
        this.insert     = false;
        this.snapshots  = snapshots;
    }

    @Override
    public void refer() {
        if (insert) {
            controller.insertNColumnsAt(col, n);
        } else {
            controller.deleteNColumnsAt(col, n);
        }
    }

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
