package dodecagraphone.teclesControl;

import dodecagraphone.MyController;
import dodecagraphone.model.component.MyComponent;
import dodecagraphone.model.component.MyGridSquare;
import dodecagraphone.model.mixer.MyTrack;
import dodecagraphone.ui.Settings;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Agrupa totes les accions de ratolí entre mousePressed i mouseReleased en un
 * sol Event per poder fer undo/redo.
 * Suporta tres tipus de canvi: afegir nota, eliminar nota i canvi de link.
 */
public class MouseSequence extends Event {

    private static class Change {
        MyGridSquare square;
        int channel;
        int trackId;
        boolean isLinkChange; // true = canvi de link, false = add/remove
        boolean added;        // per isLinkChange=false: true=afegit, false=eliminat
        int velocity;
        boolean linked;
        boolean muted;
        boolean dotted;
        boolean visible;
        boolean linkedBefore; // per isLinkChange=true: estat abans
        boolean linkedAfter;  // per isLinkChange=true: estat després

        /** Constructor per add/remove. */
        public Change(MyGridSquare square, int channel, int trackId, boolean added,
                      int velocity, boolean visible, boolean linked, boolean muted, boolean dotted) {
            this.square = square;
            this.channel = channel;
            this.trackId = trackId;
            this.isLinkChange = false;
            this.added = added;
            this.velocity = velocity;
            this.linked = linked;
            this.muted = muted;
            this.dotted = dotted;
            this.visible = visible;
        }

        /** Constructor per link/unlink. */
        public Change(MyGridSquare square, int channel, int trackId,
                      int velocity, boolean visible, boolean muted, boolean dotted,
                      boolean linkedBefore, boolean linkedAfter) {
            this.square = square;
            this.channel = channel;
            this.trackId = trackId;
            this.isLinkChange = true;
            this.velocity = velocity;
            this.visible = visible;
            this.muted = muted;
            this.dotted = dotted;
            this.linkedBefore = linkedBefore;
            this.linkedAfter = linkedAfter;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 17 * hash + Objects.hashCode(this.square);
            hash = 17 * hash + this.channel;
            hash = 17 * hash + this.trackId;
            hash = 17 * hash + (this.added ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            final Change other = (Change) obj;
            if (this.channel != other.channel) return false;
            if (this.trackId != other.trackId) return false;
            if (this.added != other.added) return false;
            return Objects.equals(this.square, other.square);
        }
    }

    private MyController controller;
    private final List<Change> changes = new ArrayList<>();

    public MouseSequence(MyController contr) {
        super();
        this.controller = contr;
    }

    public boolean isEmpty() {
        return changes.isEmpty();
    }

    public void addChange(MyGridSquare square, boolean added, int channel, int trackId,
                          int velocity, boolean visible, boolean mutted, boolean linked, boolean dotted) {
        changes.add(new Change(square, channel, trackId, added, velocity, visible, linked, mutted, dotted));
    }

    public void addLinkChange(MyGridSquare square, int channel, int trackId,
                              int velocity, boolean visible, boolean muted, boolean dotted,
                              boolean linkedBefore, boolean linkedAfter) {
        changes.add(new Change(square, channel, trackId, velocity, visible, muted, dotted, linkedBefore, linkedAfter));
    }

    private void applyLinkState(MyGridSquare square, int channel, int trackId, int velocity,
                                boolean visible, boolean muted, boolean dotted, boolean linked) {
        if (linked) {
            square.linkNote(channel, trackId, velocity, visible, muted, false, dotted);
        } else {
            square.unlinkNote(channel, trackId, velocity, visible, muted, false, dotted);
        }
    }

    @Override
    public void refer() {
        boolean firstTime = true;
        MyGridSquare firstSquare = null;
        MyGridSquare lastSquare = null;
        Change firstChange = null;
        Change lastChange = null;
        boolean hasLinkChanges = changes.stream().anyMatch(c -> c.isLinkChange);

        for (Change c : changes) {
            if (c.isLinkChange) {
                // Busca el quadrat actual: addNoteToSquare pot haver creat un nou objecte
                MyGridSquare actual = controller.getAllPurposeScore()
                        .getGridSquare(c.square.getScoreRow(), c.square.getScoreCol());
                if (actual != null) {
                    applyLinkState(actual, c.channel, c.trackId, c.velocity, c.visible, c.muted, c.dotted, c.linkedAfter);
                    actual.updateState();
                }
                continue;
            }
            MyTrack track = this.controller.getMixer().getTrackFromId(c.trackId);
            if (c.added) {
                track.oneNoteMore();
                c.square = this.controller.getAllPurposeScore().addNoteToSquare(
                        c.square.getScoreRow(), c.square.getScoreCol(),
                        1, Settings.getnRowsSquare(),
                        (MyComponent) this.controller.getAllPurposeScore(), this.controller,
                        this.controller.getAllPurposeScore(), this.controller.getCam(),
                        c.channel, c.trackId, c.velocity, c.visible, c.muted, c.linked, c.dotted);
                if (firstTime) {
                    firstChange = c;
                    firstSquare = c.square;
                    firstTime = false;
                }
                lastSquare = c.square;
                lastChange = c;
            } else {
                track.oneNoteLess();
                this.controller.getAllPurposeScore().removeNoteFromSquare(
                        c.square.getScoreRow(), c.square.getScoreCol(), c.channel, c.trackId);
            }
            c.square.updateState();
        }

        // Unlink implícit per a sequences antigues (sense LINK_CHANGE explícits)
        if (!hasLinkChanges && firstSquare != null && firstChange != null && lastChange != null) {
            if (firstSquare.getScoreCol() <= lastSquare.getScoreCol()) {
                firstSquare.unlinkNote(firstChange.channel, firstChange.trackId, firstChange.velocity,
                        firstChange.visible, firstChange.muted, firstChange.linked, firstChange.dotted);
            } else {
                lastSquare.unlinkNote(lastChange.channel, lastChange.trackId, lastChange.velocity,
                        lastChange.visible, lastChange.muted, lastChange.linked, lastChange.dotted);
            }
        }
    }

    @Override
    public void desfer() {
        for (int i = changes.size() - 1; i >= 0; i--) {
            Change c = changes.get(i);
            if (c.isLinkChange) {
                applyLinkState(c.square, c.channel, c.trackId, c.velocity, c.visible, c.muted, c.dotted, c.linkedBefore);
                c.square.updateState();
                continue;
            }
            MyTrack track = this.controller.getMixer().getTrackFromId(c.trackId);
            if (c.added) {
                track.oneNoteLess();
                this.controller.getAllPurposeScore().removeNoteFromSquare(
                        c.square.getScoreRow(), c.square.getScoreCol(), c.channel, c.trackId);
            } else {
                track.oneNoteMore();
                c.square = this.controller.getAllPurposeScore().addNoteToSquare(
                        c.square.getScoreRow(), c.square.getScoreCol(),
                        1, Settings.getnRowsSquare(),
                        (MyComponent) this.controller.getAllPurposeScore(), this.controller,
                        this.controller.getAllPurposeScore(), this.controller.getCam(),
                        c.channel, c.trackId, c.velocity, c.visible, c.muted, c.linked, c.dotted);
            }
            c.square.updateState();
        }
    }
}
