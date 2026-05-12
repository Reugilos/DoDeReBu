package dodecagraphone.teclesControl;

import dodecagraphone.MyController;
import dodecagraphone.model.component.MyComponent;
import dodecagraphone.model.component.MyGridSquare;
import dodecagraphone.model.mixer.MyTrack;
import dodecagraphone.ui.Settings;
import java.util.ArrayList;
import java.util.List;

/**
 * Agrupa totes les accions de ratolí entre mousePressed i mouseReleased en un
 sol Event per poder refer undo/redo.
 */
public class DeleteTrackSequence extends Event {


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
    }
    
    private MyController controller;

    private final List<Change> changes = new ArrayList<>();
    
    private int trackId;

    public DeleteTrackSequence(MyController contr){
        super();
        this.controller = contr;
    }
    
    public void addAllChanges(int trackId,List<MyGridSquare.SubSquare> list){
        this.trackId = trackId;
        for (MyGridSquare.SubSquare note:list){
            MyGridSquare square = note.getSquare();
            this.addChange(square, false, note.getChannel(),trackId, note.getVelocity(), 
                    note.isVisible(), !note.isAudible(), note.isLinked(), square.isSqDotted());
        }
    }
    
    public void addChange(MyGridSquare square, boolean added,int channel, int trackId, 
                          int velocity, boolean visible, boolean mutted, boolean linked, boolean dotted) {
        changes.add(new Change(square, channel, trackId, added, velocity, visible, linked, mutted, dotted));
    }

    @Override
    public void refer() {
        boolean firstTime = true;
        MyGridSquare firstSquare = null;
        MyGridSquare lastSquare = null;
        Change firstChange = null;
        Change lastChange = null;
        for (Change c : changes) {
            MyTrack track = this.controller.getMixer().getTrackFromId(c.trackId);
            if (c.added) {
                if (firstTime){
                    firstChange = c;
                    lastChange = c;
                    firstSquare = c.square;
                    firstTime = false;
                }
                lastSquare = c.square;
                lastChange = c;
                track.oneNoteMore();
                c.square = this.controller.getAllPurposeScore().addNoteToSquare(c.square.getScoreRow(),c.square.getScoreCol(),1,Settings.getnRowsSquare(),(MyComponent) this.controller.getAllPurposeScore(),this.controller,this.controller.getAllPurposeScore(),this.controller.getCam(),
                    c.channel, c.trackId, c.velocity, c.visible, c.muted, c.linked, c.dotted);
            } else {
                track.oneNoteLess();
                this.controller.getAllPurposeScore().removeNoteFromSquare(c.square.getScoreRow(),c.square.getScoreCol(),c.channel, c.trackId);
            }
            c.square.updateState();
        }
        if (firstSquare!=null && (firstSquare.getScoreCol()<=lastSquare.getScoreCol())){
            firstSquare.unlinkNote(firstChange.channel,firstChange.trackId,firstChange.velocity,firstChange.visible,firstChange.muted,firstChange.linked,firstChange.dotted);
            firstSquare.updateState();
        } else {
            if (lastSquare!=null){
                lastSquare.unlinkNote(lastChange.channel,lastChange.trackId, lastChange.velocity,lastChange.visible,lastChange.muted,lastChange.linked,lastChange.dotted);
                lastSquare.updateState();
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
        this.controller.getMixer().getTrackFromId(trackId).setDeleted(false);
        // Recorre en ordre invers
        for (int i = changes.size() - 1; i >= 0; i--) {
            Change c = changes.get(i);
            MyTrack track = this.controller.getMixer().getTrackFromId(c.trackId);
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
