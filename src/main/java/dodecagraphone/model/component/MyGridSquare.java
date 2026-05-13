package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.color.ColorSets;
import dodecagraphone.model.mixer.MyTrack;
import dodecagraphone.ui.Settings;
import java.awt.Color;
import java.util.LinkedList;

/**
 * Each MyGridSquare corresponds to a midi note (row) and a time instant
 * (column). It is the elementary component of MyGridScore.
 *
 * @author Pau
 */
public class MyGridSquare extends MyComponent {

    public class SubSquare {
        private int channel;
        private int track;
        private MyGridSquare square; // La Square que conté la SubSquare
        private int velocity; // Només s'utilitza en partitures llegides de fitxer. Altrament s'utilitza track.getVelocity() (cfr. MyXilokey::play())
        private boolean is_playing;
        private boolean is_audible;
        private boolean is_linked;
        private boolean is_visible;
        private boolean is_dotted;

        private SubSquare(int channel, int track, MyGridSquare sq, int volume, boolean is_visible, boolean is_mutted, boolean is_linked, boolean is_dotted) {
            this.channel = channel;
            this.track = track;
            this.square = sq;
            this.velocity = volume;
            this.is_visible = is_visible;
            this.is_audible = !is_mutted;
            this.is_linked = is_linked;
            this.is_playing = false;
            this.is_dotted = is_dotted;
        }

        public MyGridSquare getSquare() {
            return square;
        }

        public boolean isLinked() {
            return is_linked;
        }

        public boolean isAudible() {
            return is_audible && controller.getMixer().isTrackAudible(track);
        }

        public boolean isVisible() {
            return is_visible && controller.getMixer().isTrackVisible(track);
        }

//        public boolean isDotted() {
//            return controller.getMixer().isCurrentTrackDotted();
//        }
//
        public boolean trackIsCurrent(){
            return (this.track == controller.getMixer().getCurrentTrackId());
        }

        public int getChannel() {
            return channel;
        }

        public int getTrack() {
            return track;
        }

        public int getVelocity() {
            return velocity;
        }
        
        @Override
        public int hashCode() {
            int hash = 3;
            hash = 73 * hash + this.channel;
            hash = 73 * hash + this.track;
            hash = 73 * hash + this.square.hashCode();
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
            final SubSquare other = (SubSquare) obj;
            if (this.channel != other.channel) {
                return false;
            }
            if (!this.square.equals(other.square)) {
                return false;
            }
            return this.track == other.track;
        }
        
    }

    private MyGridScore gridScore;
    private int midi;
    public int scoreRow, scoreCol;
    private Color color;
    private Color color_on;
    private Color color_off;
    private Color color_playing;
    private boolean sq_is_visible;
    private boolean sq_is_dotted;
    private boolean sq_is_audible;
    private boolean sq_is_linked;
    private boolean sq_is_empty;
    private int are_playing;
    private LinkedList<SubSquare> poliNotes;
    
    public MyGridSquare(int firstCol, int firstRow, int nCols, int nRows, MyComponent parent, MyController contr, MyGridScore score, MyCamera cam) {
        super(firstCol, firstRow, nCols, nRows, parent, contr);
//        System.out.println("MyGridSquare::MyGridSquare: nRows = "+nRows);
        this.scoreCol = firstCol;
        this.scoreRow = firstRow;
        this.gridScore = score;
        this.sq_is_dotted = false;
        this.sq_is_visible = false;
        this.sq_is_audible = false;
        this.sq_is_linked = false;
        this.sq_is_empty = true;
        this.are_playing = 0;
        this.midi = ToneRange.getHighestMidi() - firstRow;
        this.color_on = ColorSets.getEncesColor(this.midi % 12);
        this.color_off = ColorSets.getPentagramaColor(this.midi);
        this.color_playing = ColorSets.getColorFons();
        this.color = color_off;
        this.width = Settings.getColWidth()*nCols;
        this.height = Settings.getRowHeight()*nRows;
        this.poliNotes = new LinkedList<>();
    }
    /**
     * Updates values of the transposed square.
     *
     * @param sq The original square being transposed.
     * @param row The new row after transposition.
     * @param col The new column after transposition.
     */
    public void transposedSquare(MyGridSquare sq, int row, int col) {
        this.gridScore = sq.gridScore;
        this.scoreRow = row;
        this.scoreCol = col;
        this.sq_is_dotted = sq.sq_is_dotted;
        this.sq_is_visible = sq.sq_is_visible;
        this.sq_is_audible = sq.sq_is_audible;
        this.sq_is_linked = sq.sq_is_linked;
        this.sq_is_empty = sq.sq_is_empty;
        this.are_playing = sq.are_playing;
        this.midi = ToneRange.getHighestMidi() - row;
        this.color_on = ColorSets.getEncesColor(this.midi % 12);
        this.color_off = ColorSets.getPentagramaColor(this.midi);
        this.color_playing = ColorSets.getColorFons();
        this.color = this.isSqVisible() ? this.color_on : this.color_off;
        this.width = sq.width;
        this.height = sq.height;
        this.poliNotes = new LinkedList<>(sq.poliNotes);
    }

    private boolean allVisibleTracksAreLinked(){
        boolean allLinked = true;
        for (SubSquare nt:this.poliNotes){
            if (this.controller.getMixer().isTrackVisible(nt.track)){
                if (!nt.is_linked){
                   allLinked = false;
                   break;
                }
            }
        }
        return allLinked;
    }
    
    private boolean currentTrackIsDotted(){
        boolean dotted = false;
        if (this.controller.getMixer().getCurrentTrack() != null){
        dotted = this.controller.getMixer().getCurrentTrack().isDotted();
        int trId = this.controller.getMixer().getCurrentTrackId();
        boolean isTrackVisible = this.controller.getMixer().isTrackVisible(trId);
        boolean trackNoteIsDotted = false;
        for (SubSquare nt:this.poliNotes){
            if (nt.track == trId)
                if (!nt.is_linked && isTrackVisible){
                    trackNoteIsDotted = true;
                    break;
            }
        }
        dotted = dotted && trackNoteIsDotted;
        }
////        dotted = dotted && poliNotes.stream().anyMatch(SubSquare::isDotted);
        return dotted;
    }
    
    public void updateState() {
        this.sq_is_dotted = currentTrackIsDotted();
//        this.sq_is_dotted = poliNotes.stream().anyMatch(SubSquare::isDotted);
//        this.sq_is_dotted = this.sq_is_dotted && poliNotes.stream().anyMatch(SubSquare::trackIsCurrent);
        this.sq_is_visible = poliNotes.stream().anyMatch(SubSquare::isVisible);
        this.sq_is_audible = poliNotes.stream().anyMatch(SubSquare::isAudible);
        this.sq_is_linked = allVisibleTracksAreLinked(); // poliNotes.stream().anyMatch(SubSquare::isLinked);
        this.sq_is_empty = poliNotes.isEmpty();
        if (this.sq_is_visible){
            this.color = color_on;
        }
        else {
            this.color = color_off;
        }
    }

    public boolean isEmpty(){
        return this.sq_is_empty;
    }
        
    public void addNote(int channel, int track, int volume, boolean is_visible, boolean is_mutted, boolean is_linked, boolean is_dotted) {
        SubSquare note = new SubSquare(channel, track, this, volume, is_visible, is_mutted, is_linked, is_dotted);
//        if (!this.poliNotes.contains(note)) 
        this.poliNotes.add(note);
        updateState();
    }

    public SubSquare removeNote(int channel, int track) {
        SubSquare note = new SubSquare(channel, track, this, 0, false, false, false, false);
        int pos = poliNotes.lastIndexOf(note);
        if (pos == -1) {
            return null;
        }
        SubSquare removed = poliNotes.remove(pos);
        updateState();
        return removed;
    }

    public void checkNPlay() {
        MyXiloKey xiloKey = this.controller.getKeyboard().getKey(scoreRow);
        if (this.isSqAudible()) {
            for (SubSquare note : this.poliNotes) {
                int chan = note.channel;
                int velocity;
                int trackId = note.track;
                MyTrack tr = this.controller.getMixer().getTrackFromId(trackId);
                if (tr.isKeepNoteVelocity()) {
                    velocity = note.velocity;
                } else {
                    velocity = tr.getVelocity();
                }
                if (note.isAudible()) {
                    if (!note.isLinked()) {
//                        System.out.println("   MyGridSquare::checkNPlay: stop "+chan+" col "+this.parentFirstCol);
                        xiloKey.stop(chan);
                        this.are_playing--;
                    }
                    if (!xiloKey.isPlaying(chan)) {
//                        System.out.println("   MyGridSquare::checkNPlay: play "+chan+" col "+this.parentFirstCol);
                        int displayOffset = tr.getDisplayOffset();
                        int realMidi = ToneRange.keyIdToMidi(scoreRow) - displayOffset;
                        xiloKey.playWithMidi(realMidi, chan, velocity);
//                        System.out.println("MyGridSquare::checkNPlay():channel,instr "+chan+" "+SoundWithMidi.getInstrumentInChannel(chan));
                        this.are_playing++;
                    }
                    this.color = color_playing;
                }
            }
        } else {
            int count = xiloKey.stopAllChannels();
            this.are_playing -= count;
            if (this.are_playing == 0) {
                this.color = this.color_off;
            }
        }
        if (super.parentFirstCol > 0) {
            MyGridSquare sq = this.gridScore.getGridSquare(super.parentFirstRow, super.parentFirstCol - 1);
            if (sq != null) {
                sq.updateColor(); // At this moment currentCol has already been updated, so we stop col-1.                        }
            }
        }
    }

    public void updateColor() {
        if (this.isSqVisible()) {
            this.color = color_on;
        } else {
            this.color = color_off;
        }
    }

    /**
     * Stops all notes currently playing in this grid square and updates the
     * color.
     */
    public void stop() {
        if (this.are_playing > 0) {
            MyXiloKey xiloKey = this.controller.getKeyboard().getKey(scoreRow);
            for (SubSquare note : this.poliNotes) {
                if (note.isAudible()) {
                    xiloKey.stop(note.channel);
                }
            }
            this.are_playing = 0;
            this.color = this.isSqVisible() ? this.color_on : this.color_off;
        }
    }

    public LinkedList<SubSquare> getPoliNotes() {
        return poliNotes;
    }

    public int getScoreRow() {
        return scoreRow;
    }

    public int getScoreCol() {
        return scoreCol;
    }

    public Color getColor() {
        return color;
    }


//    @Override
//    public void draw(Graphics2D g) {
//        if (once) {
//            if (Settings.SHOW_DRAW_HIERARCHY) {
//                Utilities.printOutWithPriority(5, "MyGridSquare::draw()");
//            }
//        }
//        once = false;
////        if (scoreRow == 0 && scoreCol >= 0 && scoreCol < 4) {
////            System.out.println("MyGridSquare::draw grid[" + scoreRow + "][" + scoreCol + "] = " + this.midi + " (" + isSqVisible() + ") "
////                    + this.controller.getAllPurposeScore().getCurrentCol());
////        }
//        int screenX = (int) Math.round(gridScore.getOffScreenScreenX(scoreCol));
//        int screenY = (int) Math.round(gridScore.getOffScreenScreenY(scoreRow*Settings.getnRowsSquare()));
//        // this.width = Settings.getColWidth() * nCols;
//        //double h = this.height;
//        int wdth = (int) Math.ceil(Settings.getSquareWidth()); // getComponentWidth();
//        int hght = (int) Math.ceil(Settings.getSquareWidth()); // getComponentHeight();
//        //System.out.println("MyGridSquare::Draw: hght = "+(int) hght);
//        if (scoreRow == 0) {
//            Utilities.printOutWithPriority(30, "   MyGridSquare::draw col, screenX = " + scoreCol + ", " + (int) screenX);
//        }
//        g.setColor(getPlatformColor(this.color));
//        g.fillRect((int) screenX, (int) screenY, (int) wdth, (int) hght);
//        if (this != null) {
//            this.updateState();
//            if (this.isSqVisible()) {
//                if (!this.isSqAudible() && Settings.isShowMutted()) {
//                    g.setColor(getPlatformColor(Color.gridSquareFontColor(this.midi)));
//                    g.drawString("X", (int) screenX, (int) (screenY + hght));
//                }
//                if (gridScore.isShowNoteNames() && !this.sq_is_linked) {
//                    g.setColor(getPlatformColor(this.color));
//                    g.fillRect((int) screenX, (int) screenY, (int) wdth, (int) hght);
//                    String name;
//                    name = ToneRange.getNoteName(midi, gridScore.getMidiKey());
//                    name = name.substring(0, name.length() - 1);
//                    name = "" + scoreCol;
//                    g.setColor(getPlatformColor(Color.gridSquareFontColor(this.midi)));
//                    g.drawString(name, (int) (screenX + width / 3), (int) (screenY + hght * 0.8));
//                    // System.out.println("MyGridSquare::draw() name = " + name);
//                }
//                if (this.isSqDotted() && !this.sq_is_linked) {
//                    g.setColor(getPlatformColor(Color.BLACK));
//                    g.fillRect((int) screenX, (int) screenY, (int) hght, (int) hght);
//                    if (gridScore.isShowNoteNames()) {
//                        String name;
//                        name = ToneRange.getNoteName(midi, gridScore.getMidiKey());
//                        name = name.substring(0, name.length() - 1);
//                        g.setColor(getPlatformColor(Color.WHITE));
//                        g.drawString(name, (int) (screenX + width / 3), (int) (screenY + hght));
//                    }
//                }
//            }
//        }
//        Stroke stroke = g.getStroke();
//        Stroke dashed = new BasicStroke((float) 0.5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 3}, 0);//15,3
//        g.setStroke(dashed);
//        g.setStroke(new BasicStroke((float) 0.2));
//        g.setColor(java.awt.Color.GRAY);
//        g.drawLine((int) screenX, (int) screenY, (int) (screenX + wdth), (int) screenY);
//        g.setStroke(stroke);
//    }
//    
    public void setLinked(boolean is_linked) {
        this.sq_is_linked = is_linked;
    }

    public boolean isSqDotted(){
        updateState();
        return sq_is_dotted;
    }
    
    public boolean isSqVisible() {
        updateState();
        return sq_is_visible;
    }

    public void setSqVisible(boolean visible){
        sq_is_visible = visible;
    }
    
    public boolean isSq_is_visible() {
        return sq_is_visible;
    }

    public boolean isSq_is_dotted() {
        return sq_is_dotted;
    }

    public boolean isSq_is_audible() {
        return sq_is_audible;
    }

    public boolean isSq_is_linked() {
        return sq_is_linked;
    }

    public boolean isSq_is_empty() {
        return sq_is_empty;
    }
    
    

    public boolean isSqAudible() {
        updateState();
        return sq_is_audible;
    }

    public int getMidi() {
        return midi;
    }

    /**
     * Updates the strip color of the square based on the provided color. If the
     * square is visible, its off color is updated.
     *
     * @param color The new strip color.
     */
    public void updateStrip(Color color) {
        this.color_off = color;
        if (!this.isSqVisible()) {
            this.color = this.color_off;
        } else {
            this.color = this.color_on;
        }
    }

    /**
     * Unlinks a specific note (SubSquare) in this grid square.
     *
     * @param channel The MIDI channel of the note.
     * @param track The track associated with the note.
     * @param volume The velocity of the note.
     * @param is_mutted Whether the note is muted.
     * @param is_linked Whether the note is linked.
     */
    public void unlinkNote(int channel, int track, int volume, boolean is_visible, boolean is_mutted, boolean is_linked, boolean is_dotted) {
        SubSquare note = new SubSquare(channel, track, this, volume, is_visible, is_mutted, is_linked, is_dotted);
        int pos = this.poliNotes.indexOf(note);
        if (pos >= 0) {
            this.poliNotes.get(pos).is_linked = false;
        }
        updateState();
    }

    /**
     * Links a specific note (SubSquare) in this grid square.
     *
     * @param channel The MIDI channel of the note.
     * @param track The track associated with the note.
     * @param volume The velocity of the note.
     * @param is_mutted Whether the note is muted.
     * @param is_linked Whether the note is linked.
     */
    public void linkNote(int channel, int track, int volume, boolean is_visible, boolean is_mutted, boolean is_linked, boolean is_dotted) {
        SubSquare note = new SubSquare(channel, track, this, volume, is_visible, is_mutted, is_linked, is_dotted);
        int pos = this.poliNotes.indexOf(note);
        if (pos >= 0) {
            this.poliNotes.get(pos).is_linked = true;
        }
        updateState();
    }

    public MyGridSquare next(){
        return this.controller.getAllPurposeScore().getGridSquare(this.getScoreRow(),this.getScoreCol()+1);
    }
    
    /** Deprecated */
//    public boolean toggle() {        
//        boolean added;
//        if (this.isSqVisible()) {
//            MyTrack tr = this.controller.getMixer().getCurrentTrack();
//            tr.oneNoteLess();
//            this.removeNote(this.controller.getMixer().getCurrentChannelOfCurrentTrack(), this.controller.getMixer().getCurrentTrackId());
//            added = false;
//        } else {
//            MyTrack tr = this.controller.getMixer().getCurrentTrack();
//            tr.oneNoteMore();
//            if (this == null)
//            this.addNote(this.controller.getMixer().getCurrentChannelOfCurrentTrack(), this.controller.getMixer().getCurrentTrackId(),
//                    this.controller.getMixer().getCurrentTrack().getVelocity(), true, false, true,
//                    this.controller.getMixer().getCurrentTrack().isDotted());
//            added = true;
//        }
//        updateState();
//        return added;
//    }
    public int getnNotes(){
        return this.poliNotes.size();
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
        final MyGridSquare other = (MyGridSquare) obj;
        if (this.scoreCol != other.scoreCol) {
            return false;
        }
        if (this.scoreRow != other.scoreRow) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.scoreRow;
        hash = 47 * hash + this.scoreCol;
        return hash;
    }
        
}