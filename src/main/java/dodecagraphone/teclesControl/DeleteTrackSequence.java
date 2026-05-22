/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.teclesControl;

import dodecagraphone.MyController;
import dodecagraphone.model.component.MyComponent;
import dodecagraphone.model.component.MyGridSquare;
import dodecagraphone.model.mixer.MyTrack;
import dodecagraphone.ui.Settings;
import java.util.ArrayList;
import java.util.List;

/**
 * [CA] Event undo/redo per a l'eliminació d'una pista completa. Acumula tots els
 * canvis de notes que implica l'eliminació (en forma de llista de {@code Change}) i
 * permet refer-los (redo) o desfer-los (undo) en ordre correcte. L'undo restaura
 * la pista com a no eliminada i reafegeix totes les notes.
 * <p>
 * [EN] Undo/redo event for deleting an entire track. Accumulates all note changes
 * involved in the deletion (as a list of {@code Change} entries) and allows them
 * to be redone or undone in the correct order. Undo marks the track as not deleted
 * and re-adds all notes.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
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

    /**
     * [CA] Crea un nou event d'eliminació de pista associat al controlador indicat.
     * <p>
     * [EN] Creates a new track deletion event associated with the given controller.
     *
     * @param contr [CA] controlador principal / [EN] main controller
     */
    public DeleteTrackSequence(MyController contr){
        super();
        this.controller = contr;
    }

    /**
     * [CA] Afegeix tots els canvis de notes associats a l'eliminació d'una pista.
     * <p>
     * [EN] Adds all note changes associated with deleting a track.
     *
     * @param trackId [CA] identificador de la pista eliminada / [EN] id of the deleted track
     * @param list    [CA] llista de subsquares (notes) que pertanyen a la pista / [EN] list of subsquares (notes) belonging to the track
     */
    public void addAllChanges(int trackId,List<MyGridSquare.SubSquare> list){
        this.trackId = trackId;
        for (MyGridSquare.SubSquare note:list){
            MyGridSquare square = note.getSquare();
            this.addChange(square, false, note.getChannel(),trackId, note.getVelocity(),
                    note.isVisible(), !note.isAudible(), note.isLinked(), square.isSqDotted());
        }
    }

    /**
     * [CA] Afegeix un canvi individual (nota afegida o eliminada) a la llista de canvis.
     * <p>
     * [EN] Adds an individual change (note added or removed) to the change list.
     *
     * @param square  [CA] casella de la graella afectada / [EN] affected grid square
     * @param added   [CA] {@code true} si s'ha afegit, {@code false} si s'ha eliminat / [EN] {@code true} if added, {@code false} if removed
     * @param channel [CA] canal MIDI / [EN] MIDI channel
     * @param trackId [CA] identificador de la pista / [EN] track identifier
     * @param velocity [CA] velocitat MIDI / [EN] MIDI velocity
     * @param visible [CA] si la nota és visible / [EN] whether the note is visible
     * @param mutted  [CA] si la nota és silenciada / [EN] whether the note is muted
     * @param linked  [CA] si la nota és linked / [EN] whether the note is linked
     * @param dotted  [CA] si la nota és puntejada / [EN] whether the note is dotted
     */
    public void addChange(MyGridSquare square, boolean added,int channel, int trackId,
                          int velocity, boolean visible, boolean mutted, boolean linked, boolean dotted) {
        changes.add(new Change(square, channel, trackId, added, velocity, visible, linked, mutted, dotted));
    }

    /**
     * [CA] Redo: reaplicar l'eliminació de la pista (esborra les notes).
     * <p>
     * [EN] Redo: re-applies the track deletion (removes the notes).
     */
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

    /**
     * [CA] Undo: restaura la pista (marcada com a no eliminada) i reafegeix totes
     * les notes en ordre invers.
     * <p>
     * [EN] Undo: restores the track (marked as not deleted) and re-adds all notes
     * in reverse order.
     */
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
