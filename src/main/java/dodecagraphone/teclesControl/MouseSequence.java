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
 sol Event per poder refer undo/redo.
 */
public class MouseSequence extends Event {


    private static class Change {
        MyGridSquare square;
        int channel;
        int trackId;
        boolean added; // true = s'ha afegit, false = s'ha eliminat
        int velocity;
        boolean linked;
        boolean muted;
        boolean dotted;
        boolean visible;

        public Change(MyGridSquare square, int channel, int trackId, boolean added,
                      int velocity, boolean visible, boolean linked, boolean muted, boolean dotted) {
            this.square = square;
            this.channel = channel;
            this.trackId = trackId;
            this.added = added;
            this.velocity = velocity;
            this.linked = linked;
            this.muted = muted;
            this.dotted = dotted;
            this.visible = visible;
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
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Change other = (Change) obj;
            if (this.channel != other.channel) {
                return false;
            }
            if (this.trackId != other.trackId) {
                return false;
            }
            if (this.added != other.added) {
                return false;
            }
            return Objects.equals(this.square, other.square);
        }
        
        
    }
    
    private MyController controller;

    private final List<Change> changes = new ArrayList<>();

    public MouseSequence(MyController contr){
        super();
        this.controller = contr;
    }
    
    public void addChange(MyGridSquare square, boolean added,int channel, int trackId, 
                          int velocity, boolean visible, boolean mutted, boolean linked, boolean dotted) {
        Change ch = new Change(square, channel, trackId, added, velocity, visible, linked, mutted, dotted);
//        if (!changes.contains(ch)) 
        changes.add(ch);
    }

    @Override
    public void refer() {
        boolean firstTime = true;
        MyGridSquare firstSquare = null;
        MyGridSquare lastSquare = null;
        Change firstChange = null;
        Change lastChange = null;
        for (Change c : changes) {
            MyTrack track = this.controller.getMixer().getTracks().get(c.trackId);
            if (c.added) {
                track.oneNoteMore();
                c.square = this.controller.getAllPurposeScore().addNoteToSquare(c.square.getScoreRow(),c.square.getScoreCol(),
                    1,Settings.getnRowsSquare(),(MyComponent) this.controller.getAllPurposeScore(),this.controller,
                    this.controller.getAllPurposeScore(),this.controller.getCam(),
                    c.channel, c.trackId, c.velocity, c.visible, c.muted, c.linked, c.dotted);
                //System.out.println("MouseSequence::refer(): add Square(row,col) = ("+c.square.getScoreRow()+","+c.square.getScoreCol()+")"+
                //        " nNotes = "+c.square.getnNotes());
                if (firstTime){
                    firstChange = c;
                    //lastChange = c;
                    firstSquare = c.square;
                    //System.out.println("MouseSequence::refer(): firstSquare(row,col) = ("+firstSquare.scoreRow+","+firstSquare.scoreCol+")");
                    firstTime = false;
                }
                lastSquare = c.square;
                lastChange = c;
            } else {
                track.oneNoteLess();
                this.controller.getAllPurposeScore().removeNoteFromSquare(c.square.getScoreRow(),c.square.getScoreCol(),c.channel, c.trackId);
            }
            c.square.updateState();
        }
        if (firstSquare != null) {
            if ((firstSquare.getScoreCol() <= lastSquare.getScoreCol())) {
                firstSquare.unlinkNote(firstChange.channel, firstChange.trackId, firstChange.velocity, firstChange.visible, firstChange.muted, firstChange.linked, firstChange.dotted);
                // System.out.println("MouseSequence::refer(): unlink firstSquare(row,col) = ("+firstSquare.scoreRow+","+firstSquare.scoreCol+")");
                //firstSquare.updateState(); // ja es fa dins d'unlink
            } else {
                lastSquare.unlinkNote(lastChange.channel, lastChange.trackId, lastChange.velocity, lastChange.visible, lastChange.muted, lastChange.linked, lastChange.dotted);
                //lastSquare.updateState();
            }
        }

//        Change first = null;
//        for (Change c : changes) {
//            if (c.added) {
//                if (c.isFirst) first = c;
//                c.square.addNote(c.channel, c.trackId, c.velocity, c.visible, c.muted, c.linked, c.dotted);
//            } else {
//                c.square.removeNote(c.channel, c.trackId);
//            }
//            c.square.updateState();
//        }
//        if (first!=null){
//            first.square.unlinkNote(first.channel,first.trackId,first.velocity,first.visible,first.muted,first.linked,first.dotted);
//            first.square.updateState();
//        }
    }

    @Override
    public void desfer() {
        // Recorre en ordre invers
        for (int i = changes.size() - 1; i >= 0; i--) {
            Change c = changes.get(i);
            MyTrack track = this.controller.getMixer().getTracks().get(c.trackId);
            if (c.added) {
                track.oneNoteLess();
                this.controller.getAllPurposeScore().removeNoteFromSquare(c.square.getScoreRow(),c.square.getScoreCol(),c.channel, c.trackId);
            } else {
                track.oneNoteMore();
                c.square = this.controller.getAllPurposeScore().addNoteToSquare(c.square.getScoreRow(),c.square.getScoreCol(),1,Settings.getnRowsSquare(),(MyComponent) this.controller.getAllPurposeScore(),this.controller,this.controller.getAllPurposeScore(),this.controller.getCam(),
                    c.channel, c.trackId, c.velocity, c.visible, c.muted, c.linked, c.dotted);
            }
            c.square.updateState();
        }
    }
}
