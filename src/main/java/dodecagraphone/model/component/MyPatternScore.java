package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.chord.Chord;
import dodecagraphone.model.chord.ChordProgression;
import dodecagraphone.model.chord.DiatonicChordProgression;
import dodecagraphone.model.chord.Triad;
import dodecagraphone.model.mixer.MyMixer;
import dodecagraphone.model.mixer.MyTrack;
import dodecagraphone.note.MyNote;
import dodecagraphone.ui.Settings;
import java.util.ArrayList;
import javax.sound.midi.MidiMessage;

/**
 * A MyGridPattern is a subclass of MyGridScore specialized in placing music
 * elements in the score, for further play. It is useful for designing playing
 * and guessing exercises. Music elements are placed on the score at the
 * currentWriteCol, which is updated after each placement.
 *
 * Placed elements can be mutted (for drawing purposes only).
 *
 * @author pau
 */
public class MyPatternScore extends MyGridScore {

    /**
     * The column of the score where the next elements will be placed.
     */
    private int currentWriteCol;
    /**
     * A mark can be set on the desired column and the currentWriteCol can be
     * moved back to the mark.
     */
    private int lastColWritten;
    private int mark;
    /**
     * For guessing exercises, messages are placed with a certain delay, so as
     * to prevent spoilers.
     */
    public static int MESSAGE_DELAY = 4;
    /**
     * Number of columns of a measure.
     */
    public static int ONE_BAR;
    /**
     * Half the previous.
     */
    public static int HALF_BAR;
    /**
     * Number of columns of a beat.
     */
    public static int ONE_BEAT;
    /**
     * Half the previous.
     */
    public static int HALF_BEAT;

    /**
     * Constructor.
     *
     * @param contr
     */
    public MyPatternScore(MyController contr) {
        super(Settings.getScoreFirstCol(), Settings.getScoreFirstRow(), Settings.getnColsScore(), Settings.getnRowsScore(), contr.getCam(), contr, contr.getCam(), Settings.getnKeysKeyboard());
        currentWriteCol = 0;
        lastColWritten = 0;
        mark = -1;
        ONE_BAR = super.getNumBeatsMeasure() * Settings.getnColsBeat();
        HALF_BAR = (int) (ONE_BAR / 2.0);
        ONE_BEAT = Settings.getnColsBeat();
        HALF_BEAT = (int) (ONE_BEAT / 2.0);
    }

    /**
     * Updates the currentWriteCol in ncols, whithout placing anything.
     *
     * @param ncols
     */
    public void skipCols(int ncols) {
        this.currentWriteCol += ncols;
        if (currentWriteCol > lastColWritten) lastColWritten = currentWriteCol;
    }

    /**
     * Sets the mark at the currentWriteCol.
     */
    public void setMark() {
        mark = currentWriteCol;
    }

    /**
     * Moves the currentWriteCol to the position of the mark.
     */
    public void gotoMark() {
        if (this.mark != -1) {
            currentWriteCol = mark;
            if (currentWriteCol > lastColWritten) lastColWritten = currentWriteCol;
        }
    }

    /**
     * Clears the mark.
     */
    public void clearMark() {
        mark = -1;
    }

    /**
     * Clears the score and resets the currentWriteCol.
     */
    public void resetPattern() {
        this.controller.clearScore();
        this.currentWriteCol = 0;
        lastColWritten = currentWriteCol;
    }

    /**
     * getter.
     *
     * @return
     */
    public int getCurrentWriteCol() {
        return currentWriteCol;
    }

    public void setCurrentWriteCol(int currentWriteCol) {
        this.currentWriteCol = currentWriteCol;
        if (currentWriteCol > lastColWritten) lastColWritten = currentWriteCol;
    }

    public int getLastColWritten() {
        return lastColWritten;
    }

    public void setLastColWritten(int lastColWritten) {
        this.lastColWritten = lastColWritten;
    }

    /**
     * Places an ascending scale, starting on midiKey (each note is nCols in
     * length). The currentWriteCol is updated.
     *
     * @param midiKey
     * @param ncols
     * @param mutted
     */
    public void placeScaleUp(int midiKey, int ncols, boolean mutted) {
        int[] relativeScale = new int[]{0, 2, 4, 5, 7, 9, 11, 12};
        for (int i = 0; i < 8; i++) {
            this.placeNote(relativeScale[i] + midiKey, ncols, mutted);
        }
    }

    /**
     * Places a descending scale, starting at midiKey (nCols is the note
     * duration). The currentWriteCol is updated.
     *
     * @param midiKey
     * @param ncols
     * @param mutted
     */
    public void placeScaleDown(int midiKey, int ncols, boolean mutted) {
        int[] relativeScale = new int[]{12, 11, 9, 7, 5, 4, 2, 0};
        for (int i = 0; i < 8; i++) {
            this.placeNote(relativeScale[i] + midiKey, ncols, mutted);
        }
    }

    /**
     * Places a sustained note from the beginning to the end of the grid,
     * without touching the currentWriteCol.
     *
     * @param midi
     * @param mutted
     */
    public void placeSustained(int midi, boolean mutted) {
        int row = ToneRange.midiToKeyId(midi);
        for (int i = 0; i < lastColWritten; i++) {
            MyTrack tr = this.controller.getMixer().getCurrentTrack();
            tr.oneNoteMore();
            this.addNoteToSquare(row,i,1,Settings.getnRowsSquare(),(MyComponent) this,this.controller,this,this.controller.getCam(),
                    this.controller.getMixer().getCurrentChannelOfCurrentTrack(), this.controller.getMixer().getCurrentTrackId(),
                    this.controller.getMixer().getCurrentTrack().getVelocity(), true, mutted, true, false);
//            grid[row][i].setOn();
        }
    }

    /**
     * Checks whether the grid is over (to prevent further placements). A
     * security margin is defined in Settings.END_COL_MARGIN.
     *
     * @return
     */
    public boolean isOver() {
        int margin = Settings.END_COL_MARGIN;
        boolean over = (this.currentWriteCol >= (this.nCols - margin));
        //System.out.println("MyPatternScore::isOver() currWriteCol, over " + this.currentWriteCol + ", " + over);
        return over;
    }

    /**
     * Appends a new message to the current message at the currentWriteCol+delay
     * of the score::messages Map, without updating the currentWriteCol.
     *
     * @param message
     * @param delay
     */
    public void placeAppendMessage(String message, int delay) {
        String mess = this.messages.get(currentWriteCol + delay);
        if (mess != null) {
            mess = mess + "; " + message;
        } else {
            mess = message;
        }
        int col = currentWriteCol + delay;
        this.messages.put(col, mess);
        if (col+1>lastColWritten) lastColWritten = col+1;
    }

    /**
     * Appends a new midi message to the current message at the currentWriteCol
     * of the score::midiMessages Map, without updating the currentWriteCol.
     *
     * @param message
     */
    public void placeAppendMidiMessage(MidiMessage message) {
        ArrayList<MidiMessage> mess = this.midiMessages.get(currentWriteCol);
        if (mess==null){
            mess = new ArrayList<>();
        }
        mess.add(message);
        this.midiMessages.put(currentWriteCol, mess);
        if (getCurrentWriteCol()+1>getLastColWritten()) setLastColWritten(getCurrentWriteCol()+1);
    }

    /**
     * Places the notes of a chord (each note ncols wide). The currentWriteCol
     * is updated.
     *
     * @param chord
     * @param ncols
     * @param mutted
     */
    public void placeChord(Chord chord, int ncols, boolean mutted) {
        int[] midiChord = chord.getMidiNotes();
        int oldCol = currentWriteCol;
        for (int j = 0; j < midiChord.length; j++) {
            this.currentWriteCol = oldCol;
            this.placeNote(midiChord[j], ncols, false);
        }
        currentWriteCol = oldCol + ncols;
        if (currentWriteCol > lastColWritten) lastColWritten = currentWriteCol;
    }

    /**
     * Places a chord at the currentWriteCol in the background chord Map,
     * without updating the currentWriteCol.
     *
     * @param chord
     */
    public void placeBackgroundChord(Chord chord) {
        this.backgroundChordLine.put(currentWriteCol, chord);
        if (getCurrentWriteCol()+1>getLastColWritten()) setLastColWritten(getCurrentWriteCol()+1);
    }

    public void removeChordSymbol(int col){
        this.chordSymbolLine.remove(col);
    }
    /**
     * Places a chord at the currentWriteCol of the chordSymbol line Map,
     * without updating the currentWriteCol.
     *
     * @param chord
     */
    public void placeChordSymbol(Chord chord,int col) {
        MyMixer mixer = this.controller.getMixer();
        int oldCurrentTrack = mixer.getCurrentTrackId();
        int ncols = chord.getNCols();
        // The chord symbol is stored at the beat-start column (for display and lookup).
        this.chordSymbolLine.put(col, chord);
        int[] midiChord = chord.getMidiNotes();
        int oldCol = currentWriteCol;
        MyTrack track = mixer.getChordTrack();
        if (track==null) {
            track = new MyTrack(mixer.getChordTrackId(), mixer.getChordTrackName());
            this.controller.addChordTrackAndInstrumentToMixer(track);
            track = mixer.getChordTrack();
        }
        int channel = this.controller.getMixer().getCurrentChannelOfTrack(track.getId());
        int velocity = track.getVelocity();
        // The notes are placed at col + beatColOffset so playback fires at the right sub-beat.
        int playCol = col + chord.getBeatColOffset();
        for (int j = 0; j < midiChord.length; j++) {
            this.currentWriteCol = playCol;
            this.placeNote(midiChord[j], ncols, false, false, channel, track.getId(), velocity);
        }
        currentWriteCol = oldCol;
        mixer.setCurrentTrack(oldCurrentTrack);
    }

    public Chord getChordSymbol(int col) {
        Chord chord = this.chordSymbolLine.get(col);
        return chord;
    }

    public void placeChordSymbol(Chord chord) {
        placeChordSymbol(chord,currentWriteCol);
    }

    /**
     * Places a text sympbol at the currentWriteCol of the chordSympbol line
     * Map, without updating the currentWriteCo. To place a symbol a dummy chord
     * is created with midiRoot set to -1 and the symbol text copied on the info
     * field of the chord.
     *
     * @param simbol
     */
    public void placeSimbolInChordLine(String simbol) {
        Chord ch = new Chord(Settings.USE_INFO_AS_SIMBOL, null, -1, simbol); // if root = -13, use info as simbol
        this.chordSymbolLine.put(currentWriteCol, ch);
        if (getCurrentWriteCol()+1>getLastColWritten()) setLastColWritten(getCurrentWriteCol()+1);
    }

    /**
     * Places a stop for the background chord at the currentWriteCol of the
     * backgroundChordLine Map, without updating the currentWriteCo. To place a
     * stop, a dummy chord is created with midiRoot = Settings.IS_STOP_CHORD.
     */
    public void placeStopBackgroundChord() {
        Chord stopChord = new Chord(Settings.IS_STOP_CHORD, null, -1, "Stop current background chord");
        this.backgroundChordLine.put(currentWriteCol, stopChord);
        if (getCurrentWriteCol()+1>getLastColWritten()) setLastColWritten(getCurrentWriteCol()+1);
    }
    
    /**
     * Places and end to the score.
     */
    public void placeEndScore(){
//        Chord endChord = new Chord(Settings.END,null,-1,"End score");
//        this.backgroundChordLine.put(currentWriteCol, endChord);
        if (getCurrentWriteCol()+1>getLastColWritten()) setLastColWritten(getCurrentWriteCol()+1);
    }

    /**
     * Given a chord and an arpeggio structure, it places the arpeggio at the
     * currentWriteCol (each note is ncols wide). The currentWriteCol is
     * updated.
     *
     * @param chord
     * @param arpeggiatura, an array of chord positions, stating the order in
     * which each chord note must be placed (first chord note = 1)
     * @param ncols
     * @param mutted
     */
    public void placeArpeggio(Chord chord, int[] arpeggiatura, int ncols, boolean mutted) {
        int[] midiChord = chord.getMidiNotes();
        int[] arpeggioStructure = new int[arpeggiatura.length];
        for (int i = 0; i < arpeggioStructure.length; i++) {
            arpeggioStructure[i] = midiChord[arpeggiatura[i] - 1];
        }
        for (int j = 0; j < arpeggioStructure.length; j++) {
            int midi = arpeggioStructure[j];
            this.placeNote(midi, ncols, mutted);
        }
    }

    public void placePhrase(int midiBass, String[] notes, boolean mutted) {
        int totalCols = 0;
        for (String note : notes) {
            MyNote n = new MyNote(note,this);
            int ncols = n.getNcols();
            totalCols+=ncols;
            int pitch = n.getPitch();
            boolean linked = n.isLinked();
            if (pitch == Settings.REST) {
                this.skipCols(ncols);
            } else {
//                System.out.println("MyPatternScore.placePhrase: "+(pitch+root+midiKey));
                placeNote(pitch + midiBass, ncols, mutted, linked);
            }
        }
//        return totalCols;
    }

    public int phraseLength(String[] notes) {
        int totalCols = 0;
        for (String note : notes) {
            MyNote n = new MyNote(note,this);
            int ncols = n.getNcols();
            totalCols+=ncols;
        }
        return totalCols;
    }

    /**
     * Places a chord progression (each note is ncols wide). The currentWriteCol
     * is updated.
     *
     * @param progression
     * @param ncols
     * @param mutted
     */
    public void placeProgression(ChordProgression progression, int ncols, boolean mutted) {
        progression.setIterator();
        while (progression.hasNextChord()) {
            placeChord(progression.nextChord(), ncols, mutted);
        }
    }

    /**
     * Deletes the previous column of the grid without moving the
     * currentWriteCol. It is used to separate two elements that, otherwise,
     * would overlap.
     *
     */
    public void deletePreviousCol() {
        throw new UnsupportedOperationException("deletePreviousCol");
//        int col = currentWriteCol - 1;
//        for (int row = 0; row < nRows; row++) {
//            if (grid[row][col].isSqVisible()) {
//                if (grid[row][col].isMutted()) {
//                    grid[row][col].setMuttedOff();
//                }
//                grid[row][col].setOff();
//            }
//        }
    }

    /**
     * Places a midi note ncols wide at the currentWriteCol, and updates the
     * currentWriteCol.For drawing purposess if the argument "mutted" is true
     * the corresponding grid square is mutted.
     *
     * @param midi
     * @param ncols
     * @param mutted
     */
    public void placeNote(int midi, int ncols, boolean mutted, boolean linked) {
        int channel = this.controller.getMixer().getCurrentChannelOfCurrentTrack();
        int track = this.controller.getMixer().getCurrentTrackId();
        int velocity = this.controller.getMixer().getCurrentTrack().getVelocity();
        placeNote(midi, ncols, mutted, linked, channel, track, velocity);
    }

    public void placeNote(int midi, int ncols, boolean mutted) {
        boolean linked = false;
        placeNote(midi,ncols,mutted,linked);
    }
    
    /**
     * Places a note and sets its square's midi channel.
     *
     * @param midi
     * @param ncols
     * @param mutted
     * @param channel
     */
    public void placeNote(int midi, int ncols, boolean mutted, boolean linked, int channel, int trackId, int velocity) {
        int col = currentWriteCol;
        int keyId = ToneRange.midiToKeyId(midi);
        for (int i = 0; i < ncols; i++) {
            boolean lnkd = true;
            if (i==0) lnkd = linked;
            //MyGridSquare sq = grid[keyId][col];
            MyMixer mixer = this.controller.getMixer();
            MyTrack tr = mixer.getTrackFromId(trackId); 
            tr.oneNoteMore();
            this.addNoteToSquare(keyId,col,1,Settings.getnRowsSquare(),(MyComponent) this,this.controller,this,this.controller.getCam(),
                channel, trackId, velocity, true, mutted, lnkd, tr.isDotted());
            col++;
//            grid[row][col].setLinked(true);
        }
        this.currentWriteCol = col;
        if (currentWriteCol > lastColWritten) 
            lastColWritten = currentWriteCol;
//        grid[row][col].setLinked(linked);
    }

    /**
     *
     */
    /**
     * Sets the notes for a chromatic descent.
     * 
     */
    public void placeChromaticDescendingScore() {
        this.setCurrentWriteCol(0);
        do {
            for (int iDkey = 0; iDkey < nKeys; iDkey++) {
                int midi = ToneRange.keyIdToMidi(iDkey);
                this.placeNote(midi, 2, false);
//                    MyTrack tr = this.controller.getMixer().getCurrentTrack();
////                    tr.oneNoteMore();
////                    grid[row][col+1].addNote(this.controller.getMixer().getCurrentChannelOfCurrentTrack(),this.controller.getMixer().getCurrentTrackId(),
////                            this.controller.getMixer().getCurrentTrack().getVelocity(),true,false,true,false);
//                    tr.oneNoteMore();
//                    grid[row][col].addNote(this.controller.getMixer().getCurrentChannelOfCurrentTrack(),this.controller.getMixer().getCurrentTrackId(),
//                            this.controller.getMixer().getCurrentTrack().getVelocity(),true,false,false,false);
//                    // grid[row][col].setOn();
//
//                else {
//                    MyTrack tr = this.controller.getMixer().getCurrentTrack();
//                    tr.oneNoteLess();
//                    grid[row][col].removeNote(this.controller.getMixer().getCurrentChannelOfCurrentTrack(), this.controller.getMixer().getCurrentTrackId());
//                }
            }
        } while (this.currentWriteCol < nCols - nKeys * 2);
//        int col = this.currentWriteCol - 2;
//        for (int row = 0; row < nKeys; row++) {
//            this.placeNote(ToneRange.keyIdToMidi(row),2,false);
////            MyTrack tr = this.controller.getMixer().getCurrentTrack();
////            tr.oneNoteMore();
////            grid[row][col].addNote(this.controller.getMixer().getCurrentChannelOfCurrentTrack(), this.controller.getMixer().getCurrentTrackId(),
////                    this.controller.getMixer().getCurrentTrack().getVelocity(),true,false,false,false);
////            // grid[row][col].setOn();
//        }        
        this.controller.getAllPurposeScore().setLastColWritten(this.currentWriteCol + 1);
    }

    public void placeTonalContext(int key) {
        placeTonalContext(key,ToneRange.getLowestMidi(),ToneRange.getHighestMidi());
    }
    
    /**
     * Places the required elements to set up a tonal context, updating the
     * currentWriteCol.
     *
     * @param midiKey
     */
    public void placeTonalContext(int midiKey,int lowest,int highest) {
        Integer[] rootProgression = new Integer[]{0, 5, 7, 0};
        if (midiKey<lowest) midiKey+=12;
        if (midiKey+14>highest) midiKey-=12;
        DiatonicChordProgression progression
                = new DiatonicChordProgression(midiKey, rootProgression, Triad.ROOT_POSITION);
        placeProgression(progression, ONE_BEAT, false);
        Chord tonalityChord = new Chord(0, new int[]{-12, 0, 7}, midiKey, "");
        placeChord(tonalityChord, ONE_BAR, false);
        placeScaleUp(midiKey, HALF_BEAT, false);
        placeScaleDown(midiKey, HALF_BEAT, false);
//        skipCols(3 * ONE_BEAT);
        placeSustained(midiKey - 24, true);
    }
}
