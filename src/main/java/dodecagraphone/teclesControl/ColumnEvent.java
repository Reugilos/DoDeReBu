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
 * Undo/redo event for inserting or deleting a column in the score.
 *
 * For insert: undo = delete the same column.
 * For delete: the column contents are saved before deletion so undo can restore them.
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
    private final boolean insert;
    private final ColSnapshot snapshot; // non-null only for delete events

    /** Constructor for INSERT event (undo = delete). */
    public ColumnEvent(MyController controller, int col) {
        this.controller = controller;
        this.col        = col;
        this.insert     = true;
        this.snapshot   = null;
    }

    /** Constructor for DELETE event (undo = insert + restore snapshot). */
    public ColumnEvent(MyController controller, int col, ColSnapshot snapshot) {
        this.controller = controller;
        this.col        = col;
        this.insert     = false;
        this.snapshot   = snapshot;
    }

    @Override
    public void refer() {
        if (insert) {
            controller.insertColumnAt(col);
        } else {
            controller.deleteColumnAt(col);
        }
    }

    @Override
    public void desfer() {
        if (insert) {
            controller.deleteColumnAt(col);
        } else {
            controller.insertEmptyColumnAt(col);
            controller.restoreColumnAt(col, snapshot);
        }
    }
}
