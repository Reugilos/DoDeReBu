package dodecagraphone.teclesControl;

import dodecagraphone.MyController;
import dodecagraphone.model.chord.Chord;

/** Undo/redo event for placing or removing a chord symbol. */
public class ChordEvent extends Event {

    private final MyController controller;
    private final int col;
    private final Chord oldChord; // null = no hi havia acord
    private final Chord newChord; // null = operació d'esborrat

    public ChordEvent(MyController controller, int col, Chord oldChord, Chord newChord) {
        this.controller = controller;
        this.col        = col;
        this.oldChord   = oldChord;
        this.newChord   = newChord;
    }

    @Override
    public void refer() {
        applyChord(newChord);
    }

    @Override
    public void desfer() {
        applyChord(oldChord);
    }

    private void applyChord(Chord chord) {
        if (chord == null) {
            controller.getAllPurposeScore().removeChordSymbol(col);
        } else {
            controller.getAllPurposeScore().placeChordSymbol(chord, col);
        }
        controller.redrawChordLine();
    }
}
