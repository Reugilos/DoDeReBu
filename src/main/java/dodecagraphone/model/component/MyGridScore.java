package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.MyChoice;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.color.ColorSets;
import dodecagraphone.model.sound.BackgroundChordPlayer;
import dodecagraphone.model.chord.Chord;
import dodecagraphone.model.component.MyGridSquare.SubSquare;
import dodecagraphone.model.mixer.MyTrack;
import dodecagraphone.model.sound.SampleOrMidi;
import dodecagraphone.model.sound.SoundWithMidi;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.sound.midi.MidiMessage;

/**
 * The MyGridScore is an array of MyGridSquare's: rows correspond to midi values
 * (notes) and columns correspond to time instants. A great square can be on or
 * off (see MyGridSquare), and they can be toggled with the mouse. Each note has
 * its own color.
 *
 * The camera shows the visible part of the score, with currentCol the first
 * column out of sight. When a column of the score hits the camera's play bar,
 * all the grid squares that are on in this column play their midi note. If you
 * set a square to mutted, the note will be drawn but it will not play.
 *
 * In addition to individual notes, the score can play chords on the background.
 * The score includes four maps, indexed by columns: the background chord, the
 * chord symbol (displayed in MyChordSymbolLine), a message to be printed in the
 * status line, and a midi message to be executed by the midi player.
 *
 * The score includes also the number of squares for beat, the number of beats
 * per measure, and the beat figure (4=quarter note).
 *
 * A GridScoreSquare is the minimum time lapse. available.
 *
 * When drawing the score, the strip corresponding to the key note is
 * highlighted in pale red. And other strips highlighted or grey. You can chose
 * to paint in grey the lines corresponding to the pentagram, or highlight the
 * lines that you chose in the field "choice" (see below).
 *
 * @author Pau
 */
public class MyGridScore extends MyComponent {

    /**
     * Player of the background chords.
     */
    protected BackgroundChordPlayer background;
    /**
     * The array of grid squares.
     */
    protected MyGridSquare[][] grid;
    /**
     * If whichRow equals -1, whichCol should also be -1 (see whichCol() and
     * whichRow().
     */
    private int whichRow = -1;
    private int whichCol = -1;
    protected MyCamera cam;
    // protected MyXiloKeyboard keyboard;
    protected int numBeatsMeasure;
    /**
     * 4 = quarter note.
     */
    protected int beatFigure;
    /**
     * Valors de base (col 0) per a computeBeatMeasureLines.
     * No es modifiquen per applyChangesAt; reflecteixen l'estat inicial de la partitura.
     */
    private int baseNColsBeat    = 0;
    private int baseNBeatsMeasure = 0;
    private int baseBeatFigure    = 0; // 0 = no inicialitzat
    /** Nombre de columnes per pàgina, calculat una sola vegada a freezeBaseTimingParams(). */
    private int fixedColsPerPage  = 0;
    protected String label = "";
    protected String title = "Cançó";
    protected String author = "Tradicional";
    protected String description = "Descripció";
    protected int nKeys;

    /**
     * The midi note of the current key.
     */
    protected int midiKey;
    /**
     * The mode of the current key (M-ajor, m-inor, A-tonal).
     */
    protected char scaleMode;
    /**
     * The notes selected in choice are gray.Values relative to key.
     */
    protected volatile MyChoice choice;
    /**
     * When deciding which strips to highlight, it commutes from pentagrama
     * strips to choice strips.
     */
    protected volatile boolean usePentagramaStrips;
    /**
     * When set, the keyboard is displayed to the left of the screen.
     */
    protected boolean useScreenKeyboardRight;
    /**
     * When set, the note names are written in their corresponding squares.
     */
    protected boolean showNoteNames;
    /**
     * Whwen set, note names are written in Mobile Do
     */
    protected boolean useMobileDo;
    /**
     * PlayBar is delayed by this amount (in colums)
     */
    protected int delay;
    
    protected boolean gridColorsHaveChanged;

    /**
     * Chord vs score column, to be displayed in MyChordSymbolLine.
     */
    protected Map<Integer, Chord> chordSymbolLine;
    /**
     * Chord vs score column, to be played with the background player. If the
     * root = -2, stop the current chord.
     */
    protected Map<Integer, Chord> backgroundChordLine;
    /**
     * Text message vs score column, to be displayed in MyStatusBar.
     */
    protected Map<Integer, String> messages;
    /**
     * Midi message vs score column, to be run by the midi player (see the
     * SoundWithMidi class).
     */
    protected Map<Integer, ArrayList<MidiMessage>> midiMessages;
    /**
     * Mapa de canvis de paràmetres indexat per columna de partitura.
     * Cada entrada registra quins paràmetres canvien en arribar a aquella columna.
     */
    private final TreeMap<Integer, ScoreChange> changeMap = new TreeMap<>();

    /**
     * Representa un conjunt de canvis de paràmetres globals en una columna concreta.
     * Els camps null signifiquen "sense canvi" per a aquell paràmetre.
     */
    public static class ScoreChange {
        public Integer tempo;
        public Integer midiKey;
        public Character scaleMode;
        public Integer nBeatsMeasure;
        /** Figura del beat (4 = negra, 8 = corxera, etc.). */
        public Integer beatFigure;
        /** Nombre de columnes per quarter note (paràmetre independent de l'amplada del beat). */
        public Integer nColsQuarter;
        /** Nombre de columnes per beat (calculat a partir de nColsQuarter i beatFigure). */
        public Integer nColsBeat;
        public Integer nMeasuresCam;
        public final Map<Integer, Integer> trackVelocities = new HashMap<>();

        /**
         * Aplica els camps no-null de {@code other} sobre aquest objecte.
         *
         * @param other el ScoreChange a fusionar
         */
        public void mergeFrom(ScoreChange other) {
            if (other.tempo != null)         this.tempo = other.tempo;
            if (other.midiKey != null)       this.midiKey = other.midiKey;
            if (other.scaleMode != null)     this.scaleMode = other.scaleMode;
            if (other.nBeatsMeasure != null) this.nBeatsMeasure = other.nBeatsMeasure;
            if (other.beatFigure != null)    this.beatFigure = other.beatFigure;
            if (other.nColsQuarter != null)  this.nColsQuarter = other.nColsQuarter;
            if (other.nColsBeat != null)     this.nColsBeat = other.nColsBeat;
            if (other.nMeasuresCam != null)  this.nMeasuresCam = other.nMeasuresCam;
            this.trackVelocities.putAll(other.trackVelocities);
        }
    }

    /**
     * The first column of the score to the right of the camera. When 0, the
     * score is just out of sight.
     */
    protected volatile int currentCol;

    protected MyXiloKeyboard keyboard;

    /**
     * Initializes all atributes and creats the grid with grid squares set to
     * off (constructor).
     *
     * @param firstCol
     * @param firstRow
     * @param ncols
     * @param nrows
     * @param parent
     * @param contr
     * @param cam
     * @param keyboard
     */
    public MyGridScore(int firstCol, int firstRow, int ncols, int nrows, MyComponent parent, MyController contr, MyCamera cam, int nKeys) {
        super(firstCol, firstRow, ncols, nrows, parent, contr);
        this.keyboard = null;
        this.nKeys = nKeys;
        this.controller = contr;
        this.cam = cam;
//        this.keyboard = keyboard;
        background = new BackgroundChordPlayer();
        this.grid = new MyGridSquare[this.nKeys][this.nCols];
        for (int row = 0; row < this.nKeys; row++) {
            for (int col = 0; col < this.nCols; col++) {
                int nrsq = Settings.getnRowsSquare();
                grid[row][col] = null; // new MyGridSquare(col, row, 1, nrsq, this, contr, this, cam);
                // this.add(grid[row][col]);
            }
        }
        boolean left = !this.isUseScreenKeyboardRight();
        this.currentCol = Settings.getInitialCurrentCol(left,this);
        setNumBeatsMeasure(Settings.getnBeatsMeasure());
        setBeatFigure(Settings.getBeatFigure());
        freezeBaseTimingParams();
        midiKey = ToneRange.getDefaultKey(); // midi
        scaleMode = ToneRange.getDefaultMode();
        choice = new MyChoice(this.controller);
        showNoteNames = false;
        this.setDefaultDelay();
        usePentagramaStrips = true; // or choice based strips (false)
        chordSymbolLine = new HashMap<>();
        backgroundChordLine = new HashMap<>();
        messages = new HashMap<>();
        midiMessages = new HashMap<>();
        gridColorsHaveChanged = true;
    }

    public final void setDefaultDelay(){
            this.delay = Settings.getDefaultDelay();
    }

    public void setKeyboard(MyXiloKeyboard keyb) {
        this.keyboard = keyb;
    }

    public MyGridSquare addNoteToSquare(
            int firstRow, int firstCol, int nCols, int nRows, MyComponent parent, MyController contr, MyGridScore score, MyCamera cam,
            int channel, int track, int volume, boolean is_visible, boolean is_mutted, boolean is_linked, boolean is_dotted) {
        MyGridSquare sq = this.grid[firstRow][firstCol];
        if (sq == null) {
            sq = new MyGridSquare(firstCol, firstRow, nCols, nRows, parent, contr, score, cam);
        }
        sq.addNote(channel, track, volume, is_visible, is_mutted, is_linked, is_dotted);
        this.grid[firstRow][firstCol] = sq;
        return sq;
    }

    public SubSquare removeNoteFromSquare(int row, int col, int channel, int track) {
        MyGridSquare sq = this.grid[row][col];
        if (sq == null) {
            return null;
        }
        SubSquare sbsq = sq.removeNote(channel, track);
        if (sq.getPoliNotes().isEmpty()) {
            this.grid[row][col] = null;
        }
        return sbsq;
    }

//    public MyGridSquare(int firstCol, int firstRow, int nCols, int nRows, MyComponent parent, MyController contr, MyGridScore score, MyCamera cam) {
//    public void addNote(int channel, int track, int volume, boolean is_visible, boolean is_mutted, boolean is_linked, boolean is_dotted) {
//    public SubSquare removeNote(int channel, int track) {
//
    /**
     * Copies the current grid and adds new columns to it, up to newnCols.
     * (Currently, not used.)
     *
     * @param newnCols
     */
//    public void addCols(int newnCols) {
//        this.subComponents.clear();
//        MyGridSquare[][] oldGrid = this.grid;
//        this.grid = new MyGridSquare[this.nKeys][newnCols];
//        for (int row = 0; row < nKeys; row++) {
//            for (int col = 0; col < nCols; col++) {
//                grid[row][col] = oldGrid[row][col];
//                this.add(grid[row][col]);
//            }
//        }
//        for (int row = 0; row < nKeys; row++) {
//            for (int col = nCols; col < newnCols; col++) {
//                grid[row][col] = new MyGridSquare(col, row, 1, Settings.getnRowsSquare(), this, this.controller, this, cam);
//                this.add(grid[row][col]);
//            }
//        }
//        this.nCols = newnCols;
//    }
//
    public MyChoice getChoice() {
        return choice;
    }

    public void updateStripsNKeyboard() {
        this.updateStripsNKeyboard(this.usePentagramaStrips);
    }

    /**
     * Updates the color of the strips depending on the flag pentagram.
     *
     * @param pentagram. If pentagram is true, the pentagram lines are painted
     * grey. If pentagram is false, the notes chosen in "choice" are
     * highlighted.
     */
    public void updateStripsNKeyboard(boolean pentagram) {
        this.usePentagramaStrips = pentagram;
        for (int row = 0; row < nKeys; row++) {
            int midi = ToneRange.keyIdToMidi(row);
            for (int col = 0; col < nCols; col++) {
                MyGridSquare sq = grid[row][col];
                if (sq != null) {
                    if (pentagram) {
                        grid[row][col].updateStrip(ColorSets.getPentagramaColor(midi));
                    } else {
                        grid[row][col].updateStrip(ColorSets.getChoiceColor(midi, choice.getChoiceList()));
                    }
                }
            }
        }
        this.controller.getKeyboard().resetDimensions();
    }

    /**
     * Computes the number of columns that make up a page, based on the num
     * squares per beat, and the num beats per measure.
     *
     * @return
     */
    public int getNumColsPage() {
        if (cam == null) {
            throw new NullPointerException("The camera should be created before calling MyGridScore::getNumColsPage()");
        }
        // Usa els valors base congelats (baseNColsBeat / baseNBeatsMeasure) per garantir
        // que la mida de pàgina sigui consistent independentment dels canvis mid-score
        // que applyChangesAt hagi aplicat a numBeatsMeasure.
        int colsBeat    = (baseNColsBeat     > 0) ? baseNColsBeat     : Settings.getnColsBeat();
        int beatsMeasure = (baseNBeatsMeasure > 0) ? baseNBeatsMeasure : numBeatsMeasure;
        int colsPerMeasure = colsBeat * beatsMeasure;
        if (colsPerMeasure <= 0) colsPerMeasure = 1;
        int numMeasuresPage = cam.getnCols() / colsPerMeasure;
        int numColsPage = numMeasuresPage * colsPerMeasure;
        return numColsPage;
    }

    public int getnKeys() {
        return nKeys;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isGridColorsHaveChanged() {
        return gridColorsHaveChanged;
    }

    public void setGridColorsHaveChanged(boolean gridColorsHaveChanged) {
        this.gridColorsHaveChanged = gridColorsHaveChanged;
    }
    
    

    /**
     * getter.
     *
     * @return
     */
    public MyGridSquare[][] getGrid() {
        return grid;
    }

    public BackgroundChordPlayer getBackground() {
        return background;
    }

    public void setBackground(BackgroundChordPlayer background) {
        this.background = background;
    }

    /**
     * getter.
     *
     * @return
     */
    public synchronized int getCurrentCol() {
        return currentCol;
    }

    /**
     * setter.
     *
     * @param currentCol
     */
    public synchronized void setCurrentCol(int currentCol) {
        this.currentCol = currentCol;
    }

    public synchronized void incrementCurrentCol() {
        this.currentCol++;
    }

//    public void updateCurrentCol() {
//        this.currentCol++;
//    }
//
    /**
     * Given the position of a column in the score, it returns its position in
     * the camera.
     *
     * @param scoreCol
     * @return
     */
    public int getCamCol(int scoreCol) {
        int camCol = scoreCol + cam.getnCols() - getCurrentCol() + getFirstParentCol();
        return camCol;
    }

    /**
     * Given the position of a columns in the camera, it returns its position in
     * the score.
     *
     * @param camCol
     * @return
     */
    public int getScoreCol(int camCol) {
        int scoreCol = camCol - getFirstParentCol() - cam.getnCols() + getCurrentCol();
        Utilities.printOutWithPriority(false,"MyGridScore::getScoreCol(): camCol = "+camCol+" currentCol = "+getCurrentCol()+" scoreCol = "+scoreCol);
        return scoreCol;
    }

    /**
     * Given the position of a row in the score, it returns its position in the
     * camera.
     *
     * @param scoreRow
     * @return
     */
    public int getCamRow(int scoreRow) {
        int camRow = scoreRow + getParentFirstRow();
        return camRow;
    }

    /**
     * Given the position of a row in the camera, it returns its position in the
     * score.
     *
     * @param camRow
     * @return
     */
    public int getScoreRow(int camRow) {
        int scoreRow = -getParentFirstRow() + camRow;
        return scoreRow;
    }

    /**
     * Given a column in the score, it returns its absolute X position in the
     * screen (in pixels).
     *
     * @param scoreCol
     * @return
     */
    @Override
    public double getScreenX(int scoreCol) {
        return cam.getScreenX(getCamCol(scoreCol));
    }

    public double getOffScreenScreenX(int scoreCol) {
        return scoreCol * Settings.getColWidth();
//        return (Settings.getnColsCam()+scoreCol-getCurrentCol()) * Settings.getColWidth();
    }

    public double getOffScreenScreenY(int scoreRow) {
        return 0 + scoreRow * Settings.getRowHeight();
//        int camRow = scoreRow;
//        double screenY = cam.getScreenY(camRow);
//        return screenY;
    }

    /**
     * Given a row in the score, it returns its absolute Y position in the
     * screen (in pixels).
     *
     * @param scoreRow
     * @return
     */
    @Override
    public double getScreenY(int scoreRow) {
        int camRow = getCamRow(scoreRow);
        double screenY = cam.getScreenY(camRow);
        return screenY;
    }

    /**
     * getter.
     *
     * @return
     */
    public int getMidiKey() {
        while (midiKey > ToneRange.getHighestMidi()) {
            midiKey -= 12;
        }
        while (midiKey < ToneRange.getLowestMidi()) {
            midiKey += 12;
        }
        return midiKey;
    }

    /**
     * setter.
     *
     * @param midiKey
     */
    public void setMidiKey(int midiKey) {
        this.midiKey = midiKey;
    }

    public char getScaleMode() {
        return scaleMode;
    }

    public void setScaleMode(char scaleMode) {
        this.scaleMode = scaleMode;
    }

    /**
     * setter.
     *
     * @param choice
     */
    public void setChoice(MyChoice choice) {
        this.choice = choice;
    }

    public void setChoice(Integer[] array) {
        this.choice.setChoiceList(Arrays.asList(array));
    }

    public void setUseMobileDo(boolean useMobileDo) {
        this.useMobileDo = useMobileDo;
    }

    /**
     * getter.
     *
     * @return
     */
    public boolean isShowNoteNames() {
        return showNoteNames;
    }

    /**
     * setter.
     *
     * @param showNoteNames
     */
    public void setShowNoteNames(boolean showNoteNames) {
        this.showNoteNames = showNoteNames;
    }

    /**
     * getter.
     *
     * @return
     */
    public boolean isShowPentagramaStrips() {
        return usePentagramaStrips;
    }

    /**
     * setter.
     *
     * @param showPentagramaStrips
     */
    public void setUsePentagramaStrips(boolean showPentagramaStrips) {
        this.usePentagramaStrips = showPentagramaStrips;
    }

    /**
     * getter.
     *
     * @return
     */
    public int getNumRows() {
        return nRows;
    }

    /**
     * getter.
     *
     * @return
     */
    public int getNumCols() {
        return nCols;
    }

    /**
     * getter.
     *
     * @return
     */
    public Map<Integer, Chord> getChordLine() {
        return backgroundChordLine;
    }

    /**
     * getter.
     *
     * @return
     */
    public Map<Integer, Chord> getChordSimbolLine() {
        return chordSymbolLine;
    }

    /**
     * getter.
     *
     * @return
     */
    public Map<Integer, String> getMessages() {
        return messages;
    }

    /**
     * getter.
     *
     * @return
     */
    public Map<Integer, ArrayList<MidiMessage>> getMidiMessages() {
        return midiMessages;
    }

    /**
     * Creates a grid of new grid squares, set to off, clears the background
     * sound, and resets the chord symbol line, the background chord line, the
     * messages and the midi messages.
     *
     */
    public void clearScore() {
        this.subComponents.clear();
        this.grid = new MyGridSquare[this.nKeys][this.nCols];
//        for (int row = 0; row < nKeys; row++) {
//            for (int col = 0; col < nCols; col++) {
//                grid[row][col] = new MyGridSquare(col, row, 1, Settings.getnRowsSquare(), this, this.controller, this, cam);
//                this.add(grid[row][col]);
//            }
//        }
        this.background.clear();
        chordSymbolLine = new HashMap<>();
        backgroundChordLine = new HashMap<>();
        messages = new HashMap<>();
        midiMessages = new HashMap<>();
        title = "Títol";
        author = "Autor";
        description = "Descripció";
        gridColorsHaveChanged = true;
        setNumBeatsMeasure(Settings.getnBeatsMeasure());
        setBeatFigure(Settings.getBeatFigure());
        freezeBaseTimingParams();
        clearChangeMap();
    }

    /**
     * Sets the notes for a random score.
     *
     */
    public void randomScore() {
        for (int row = 0; row < nKeys; row++) {
            for (int col = 0; col < nCols; col++) {
                if (Utilities.tossCoin()) {
                    MyTrack tr = this.controller.getMixer().getCurrentTrack();
                    tr.oneNoteMore();
                    this.addNoteToSquare(row, col, 1, Settings.getnRowsSquare(), (MyComponent) this, this.controller, this, this.controller.getCam(),
                            this.controller.getMixer().getCurrentChannelOfCurrentTrack(), this.controller.getMixer().getCurrentTrackId(),
                            this.controller.getMixer().getCurrentTrack().getVelocity(), true, false, false, false);
                    // grid[row][col].setOn();
                } else {
                    removeNoteFromSquare(row, col, this.controller.getMixer().getCurrentChannelOfCurrentTrack(), this.controller.getMixer().getCurrentTrackId());
                }
            }
        }
        this.controller.getAllPurposeScore().setLastColWritten(nCols);
    }

    /**
     * getter.
     *
     * @return
     */
    public int getBeatFigure() {
        return beatFigure;
    }

    /**
     * setter.
     *
     * @param beatFigure
     */
    public final void setBeatFigure(int beatFigure) {
        this.beatFigure = beatFigure;
        Settings.setBeatFigure(beatFigure);
    }

    public final int getDelay(boolean left) {
        if (left)
            return 0;
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    private boolean drawNewCol = false;

    public boolean isDrawNewCol() {
        return drawNewCol;
    }

    public void setDrawNewCol(boolean drawNewCol) {
        this.drawNewCol = drawNewCol;
    }

    private boolean redrawFullGrid = true;

    public boolean isRedrawFullGrid() {
        return redrawFullGrid;
    }

    public void setRedrawFullGrid(boolean redrawFullGrid) {
        this.redrawFullGrid = redrawFullGrid;
    }

    private BufferedImage offscreenImage;
    private Graphics2D offscreenGraphics;

    public BufferedImage getOffscreenImage() {
        return offscreenImage;
    }

    public Graphics2D getOffscreenGraphics() {
        return offscreenGraphics;
    }

    public void setOffscreenImage(BufferedImage offscreenImage) {
        this.offscreenImage = offscreenImage;
    }
    private static int countInits = 0;

    public void initOffscreen() {
        if (offscreenGraphics != null) {
            offscreenGraphics.dispose();
        }
        int w = (int) (this.getnCols() * Settings.getColWidth());
        int h = (int) (this.getnKeys() * Settings.getRowHeight() * Settings.getnRowsSquare());
        offscreenImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        offscreenGraphics = offscreenImage.createGraphics();
        offscreenGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        offscreenGraphics.setColor(Color.RED);
//        offscreenGraphics.setStroke(new BasicStroke(7));
//        offscreenGraphics.drawRect(0, 0, this.offscreenImage.getWidth() - 1, this.offscreenImage.getHeight() - 1);
        countInits++;
        Utilities.printOutWithPriority(false, "MyGridScore::initOffscreen: countInits = " + countInits);
        //offscreenGraphics.dispose();
        //this.setRedrawFullGrid(true);
        this.drawFullGridinOffscreen();
    }

// provisional
//    public void shiftAndDrawNewColumn() {
//        int colWidth = (int) Settings.getColWidth();
//        int rowHeight = (int) Settings.getRowHeight();
//        int totalWidth = this.offscreenImage.getWidth();
//        int totalHeight = this.offscreenImage.getHeight();
//
//        // Desplaça la imatge cap a l'esquerra
////        this.offscreenGraphics.copyArea(colWidth, 0, totalWidth - colWidth, totalHeight, -colWidth, 0);
//
//        // Pinta nova columna a la dreta
//       int col = this.nCols - 1;
////        for (int row = 0; row < nKeys; row++) {
////            grid[row][getCurrentCol()].draw(this.offscreenGraphics); // o adapta 'draw' per a coordenades absolutes
////        }
////
////        for (int range = -20; range <= col +3;range++){ // getCurrentCol()
////         for (int row = 0; row < nKeys; row++) {
////        int screenX = (int) Math.round(this.getOffScreenScreenX(this.grid[row][range].scoreCol));
////        int screenY = (int) Math.round(this.getOffScreenScreenY(this.grid[row][range].scoreRow*nRows));
////        this.width = Settings.getColWidth()*nCols;
////        this.height = Settings.getRowHeight()*nRows;
////        int wdth = (int) Math.ceil(this.width); // getComponentWidth();
////        int hght = (int) Math.ceil(this.height); // getComponentHeight();
////        //System.out.println("MyGridSquare::Draw: hght = "+(int) hght);
////        //this.updateState();
////        offscreenGraphics.setColor(Color.BLACK);
////        offscreenGraphics.fillRect((int) screenX, (int) screenY, (int) wdth, (int) hght);
////
//////            grid[row][getCurrentCol()].draw(this.offscreenGraphics); // o adapta 'draw' per a coordenades absolutes
////        }
////    }
////        // Línies de mesura o beats
//        if ((col % Settings.getnColsBeat()) == 0) {
//            drawBeatLine(col, this.offscreenGraphics);
//        }
//        if ((col % (Settings.getnColsBeat() * getNumBeatsMeasure())) == 0) {
//            drawMeasureLine(col, this.offscreenGraphics);
//        }
//    }
    private int lastColDrawn = this.getCurrentCol();

    public int getLastColDrawn() {
        return lastColDrawn;
    }

    public void setLastColDrawn(int lastColDrawn) {
        this.lastColDrawn = lastColDrawn;
    }

    private static int countNewCol = 1;

//    public void drawNewColInOffscreen_old() {
//        synchronized (offscreenGraphics) {
//            Utilities.printOutWithPriority(3, "MyGridScore::drawNewColInOffscreen: countNewCol = " + countNewCol++);
//            int ccol = this.getCurrentCol();
//            int lcol = this.getLastColDrawn();
//            int colWidth = (int) Settings.getColWidth();
//            int rowHeight = (int) Settings.getRowHeight();
//            int imageWidth = offscreenImage.getWidth();
//            int imageHeight = offscreenImage.getHeight();
//
//            // 💨 Desplaça tot cap a l'esquerra una columna
//            offscreenGraphics.copyArea(colWidth, 0, // origin 
//                    imageWidth - colWidth, imageHeight, // size
//                    -colWidth, 0); // destination
//
//            Utilities.printOutWithPriority(5, "MyGridScore::drawNewColOffScreen  x, y, w, h = " + (imageWidth - colWidth) + ", " + "0" + ", " + colWidth + ", " + imageHeight);
//
//            int ini = Math.min(lcol +1  - this.delay, this.nCols);
//
////                if ((pos % Settings.getnColsBeat()) == 0) {
////                    drawBeatLine(pos, offscreenGraphics);
////                }
////                if ((pos % (Settings.getnColsBeat() * getNumBeatsMeasure())) == 0) {
////                    drawMeasureLine(pos, offscreenGraphics);
////                }
////
//            for (int col = ini; col < ccol; col++) {
//                offscreenGraphics.setColor(getPlatformColor(dodecagraphone.model.color.Color.BLACK));
//                for (int row = 0; row < nKeys; row++) {
//                    grid[row][col].draw(offscreenGraphics);
//                    grid[row][col-1].draw(offscreenGraphics);
//                    grid[row][col-2].draw(offscreenGraphics);
//                }
//                //                  }
//
//                if ((col % Settings.getnColsBeat()) == 0) {
//                    drawBeatLine(col, offscreenGraphics);
//                }
//                if ((col % (Settings.getnColsBeat() * getNumBeatsMeasure())) == 0) {
//                    drawMeasureLine(col, offscreenGraphics);
//                }
//
//                Utilities.printOutWithPriority(3, "MyGridScore::drawNewColInOffScreen  col = " + col);
//
//                // offscreenGraphics.setColor(Color.YELLOW);
//                // Aquest és el rectangle que entra nou per la dreta
//                //offscreenGraphics.fillRect(imageWidth - colWidth, 0, colWidth, imageHeight);
//            }
//            setLastColDrawn(ccol-1);
//        }
//    }
    //-------------------
    //        // 🔚 Calcula la nova columna (és la que entra per la dreta)
    //        int col = Math.min(this.getCurrentCol() - this.delay, this.nCols - 1);
    //
    //        // 🧱 Dibuixa només la nova columna al final
    //        for (int row = 0; row < nKeys; row++) {
    //            grid[row][col].draw(offscreenGraphics);
    //        }
    //
    //        // 🧭 Línies de beat i compàs
    //        if ((col % Settings.getnColsBeat()) == 0) {
    //            drawBeatLine(col, offscreenGraphics);
    //        }
    //        if ((col % (Settings.getnColsBeat() * getNumBeatsMeasure())) == 0) {
    //            drawMeasureLine(col, offscreenGraphics);
    //        }
    //
    //        // 🖼️ Repaint només de l'àrea dreta
    //        int x = imageWidth - colWidth;
    //        int y = 0;
    //        int w = colWidth;
    //        int h = imageHeight;
    //
    //        this.controller.getUi().repaint(); //repaint(x, y, w, h);
    //    public void slideAndDrawNewCol(int newCol) {
    //        Graphics2D g = (Graphics2D) this.getGraphics(); // O d'un BufferedImage si fas doble buffer
    //
    //        int colWidth = (int) Settings.getColWidth();
    //        int cellHeight = (int) Settings.getRowHeight();
    //        int camCols = Settings.getnColsCam();
    //        int camWidth = camCols * colWidth;
    //        int camHeight = nKeys * cellHeight;
    //
    //        // 🧭 Calcula posicions
    //        int offsetX = 0;
    //        int offsetY = 0;
    //
    //        // 💨 Copia la zona visible cap a l'esquerra
    //        g.copyArea(offsetX + colWidth, offsetY, camWidth - colWidth, camHeight, -colWidth, 0);
    //
    //        // 🧱 Dibuixa la nova columna dreta
    //        for (int row = 0; row < nKeys; row++) {
    //            grid[row][newCol].draw(g);
    //        }
    //
    //        // 🧭 Línies de beat/compàs
    //        if ((newCol % Settings.getnColsBeat()) == 0) {
    //            drawBeatLine(newCol, g);
    //        }
    //        if ((newCol % (Settings.getnColsBeat() * getNumBeatsMeasure())) == 0) {
    //            drawMeasureLine(newCol, g);
    //        }
    //    }
    //    @Override
    //    public void draw(Graphics2D g) {
    //            int colWidth = (int) Settings.getColWidth();
    //            int cellHeight = (int) Settings.getRowHeight();
    //
    //            int camCols = Settings.getnColsCam();
    //            int camWidth = camCols * colWidth;
    //            int camHeight = nKeys * cellHeight;
    //
    //            int offsetX = 0; // posició X d'inici de la càmera
    //            int offsetY = 0; // posició Y si hi ha
    //
    //            // 🔁 Desplaça la zona ja dibuixada cap a l'esquerra
    //            g.copyArea(offsetX + colWidth, offsetY, camWidth - colWidth, camHeight, -colWidth, 0);
    //
    //            // 🧱 Només dibuixa la nova columna dreta
    //            int newCol = getScoreCol(0); // columna nova a afegir
    //            if (newCol < 0) newCol = 0;
    //
    //            for (int row = 0; row < nKeys; row++) {
    //                    grid[row][newCol].draw(g);
    //            }
    //
    //            // 🧭 Redibuixa línies de compàs i pulsació si cal
    //            if ((newCol % Settings.getnColsBeat()) == 0) {
    //                    drawBeatLine(newCol, g);
    //            }
    //            if ((newCol % (Settings.getnColsBeat() * getNumBeatsMeasure())) == 0) {
    //                    drawMeasureLine(newCol, g);
    //            }
    //    }
    //    
    //    @Override
    //    public void draw_original(Graphics2D g) {
    //        int firstDrawCol = getScoreCol(0);
    //        if (firstDrawCol < 0) {
    //            firstDrawCol = 0;
    //        }
    //        //this.nCols = Settings.getnColsCam();
    //        int first = Math.min(this.getCurrentCol() - this.delay, this.nCols);
    //        int col = first;
    //        if ((col % Settings.getnColsBeat()) == 0) {
    //            drawBeatLine(col, g);
    //        }
    //        if ((col % (Settings.getnColsBeat() * getNumBeatsMeasure())) == 0) {
    //            drawMeasureLine(col, g);
    //        }
    //        for (col = first - 1; col >= firstDrawCol; col--) {
    //            for (int row = 0; row < nKeys; row++) {
    //                grid[row][col].draw(g);
    //            }
    //            if ((col % Settings.getnColsBeat()) == 0) {
    //                drawBeatLine(col, g);
    //            }
    //            if ((col % (Settings.getnColsBeat() * getNumBeatsMeasure())) == 0) {
    //                drawMeasureLine(col, g);
    //            }
    //        }
    //        col = nCols;
    //        if ((col % (Settings.getnColsBeat() * getNumBeatsMeasure())) == 0) {
    //            drawMeasureLine(col, g);
    //        }
    //    }
    private int countDrawFull = 1;

    /** Draws the semi-transparent selection rectangle onto g (offscreen coordinates). */
    private void drawSelectionOverlay(Graphics2D g) {
        if (controller == null || !controller.isSelectionActive()) return;
        int r1 = Math.min(controller.getSelStartRow(), controller.getSelEndRow());
        int r2 = Math.max(controller.getSelStartRow(), controller.getSelEndRow());
        int c1 = Math.min(controller.getSelStartCol(), controller.getSelEndCol());
        int c2 = Math.max(controller.getSelStartCol(), controller.getSelEndCol());
        int sx = (int)(c1 * Settings.getColWidth());
        int sy = (int)(r1 * Settings.getRowHeight() * Settings.getnRowsSquare());
        int sw = (int)((c2 - c1 + 1) * Settings.getColWidth());
        int sh = (int)((r2 - r1 + 1) * Settings.getRowHeight() * Settings.getnRowsSquare());
        Composite oldComposite = g.getComposite();
        Color oldColor = g.getColor();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g.setColor(new Color(0, 100, 255));
        g.fillRect(sx, sy, sw, sh);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        g.setColor(new Color(0, 50, 200));
        g.drawRect(sx, sy, sw - 1, sh - 1);
        g.setComposite(oldComposite);
        g.setColor(oldColor);
    }

    /**
     * Draws the score: each visible grid square, the beat lines and the measure
     * lines.
     *
     * @param g
     */
    public void drawFullGridinOffscreen() {
        java.awt.Component panel = (controller != null && controller.getUi() != null)
                ? controller.getUi().getPanel() : null;
        if (panel != null) panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
        synchronized (offscreenGraphics) {
            // Esborrem el buffer sencer per eliminar línies divisòries antigues
            // (les línies de compàs poden deixar píxels residuals si el compàs ha canviat).
            offscreenGraphics.setColor(java.awt.Color.WHITE);
            offscreenGraphics.fillRect(0, 0, offscreenImage.getWidth(), offscreenImage.getHeight());

            int firstDrawCol = 0;
            int lastDrawCol  = this.getnCols();
            int numCols      = lastDrawCol;

            // Calcula línies de beat/compàs tenint en compte canvis mid-score
            boolean[] isBeat    = new boolean[numCols + 1];
            boolean[] isMeasure = new boolean[numCols + 1];
            computeBeatMeasureLines(numCols + 1, isBeat, isMeasure);

            if (isBeat[numCols])    drawBeatLine(numCols, offscreenGraphics);
            if (isMeasure[numCols]) drawMeasureLine(numCols, offscreenGraphics);

            Utilities.printOutWithPriority(false, "MyGridScore::drawFullGridInOffscreen first, last = " + firstDrawCol + ", " + (lastDrawCol - 1) + ", countDrawFull = " + countDrawFull++);
            for (int col = lastDrawCol - 1; col >= firstDrawCol; col--) {
                for (int row = 0; row < nKeys; row++) {
                    this.drawSquare(row, col, offscreenGraphics);
                }
            }
            for (int col = lastDrawCol - 1; col >= firstDrawCol; col--) {
                if (isBeat[col])    drawBeatLine(col, offscreenGraphics);
                if (isMeasure[col]) drawMeasureLine(col, offscreenGraphics);
            }
            if (isMeasure[numCols]) drawMeasureLine(numCols, offscreenGraphics);
            drawSelectionOverlay(offscreenGraphics);
            setGridColorsHaveChanged(false);
        }
        } finally {
            if (panel != null) panel.setCursor(Cursor.getDefaultCursor());
        }
    }

    // Podem ajuntar-lo amb drawFullGridInOffscreen(), canviant paràmetres.
    public void drawCurrentCamInOffscreen() {
        synchronized (offscreenGraphics) {
            int ccol = getCurrentCol();
            int firstDrawCol = Math.max(0, ccol - Settings.getnColsCam());
            int lastDrawCol = ccol;//Math.min(ccol-delay, nCols);

            //------------------------------------
            boolean left = !this.isUseScreenKeyboardRight();
            int firstColToDraw;
            int x1, x2;
            int y1;//  = (int) Math.round(this.cam.getScreenY(Settings.getScoreFirstRow())); //Settings.getCamFirstRow();
            int w, w2;
            int h;
            if (left) {
                int ncolscam = Settings.getnColsCam();
                x1 = (int) (firstDrawCol * Settings.getColWidth()); // (int) Math.round(this.cam.getScreenX(firstDrawCol));//Settings.getCamFirstCol(this.isUseScreenKeyboardRight());
                y1 = 0; //(int) Math.round(this.cam.getScreenY(Settings.getScoreFirstRow())); //Settings.getCamFirstRow();
                w = (int) Math.ceil(this.cam.getWidth());//(int) this.getWidth();
                h = (int) Math.ceil(nKeys * Settings.getRowHeight() * Settings.getnRowsSquare());
                firstColToDraw = firstDrawCol; // (int) Math.max(0,getCurrentCol()-Settings.getnColsCam());
                int lastColToDraw = lastDrawCol; // getCurrentCol();
                x2 = (int) Math.round(firstColToDraw * Settings.getColWidth());
                w2 = (int) Math.ceil((lastColToDraw - firstColToDraw) * Settings.getColWidth());
            } else {
                int ncolscam = Settings.getnColsCam();
                int firstCamColIn = Math.max(0, -Settings.getnColsCam() + getCurrentCol());
                int actualWidthInCols = Settings.getnColsCam() - firstCamColIn;
                x1 = (int) (firstDrawCol * Settings.getColWidth());//Settings.getCamFirstCol(this.isUseScreenKeyboardRight());
                y1 = 0; // (int) Math.round(this.cam.getScreenY(Settings.getScoreFirstRow())); //Settings.getCamFirstRow();
                w = (int) Math.ceil(actualWidthInCols * Settings.getColWidth());//(int) this.getWidth();
                h = (int) Math.ceil(nKeys * Settings.getRowHeight() * Settings.getnRowsSquare());
                firstColToDraw = firstDrawCol; // (int) Math.max(0,getCurrentCol()-Settings.getnColsCam());
                int lastColToDraw = lastDrawCol; // getCurrentCol();
                x2 = (int) (firstColToDraw * Settings.getColWidth());
                w2 = (int) ((lastColToDraw - firstColToDraw) * Settings.getColWidth());
                Utilities.printOutWithPriority(false, "MyGridScore::drawCurrentCamInOffscreen(): left = " + left + ", firstInCamx,colsWidth,first,last = "
                        + firstCamColIn + "," + actualWidthInCols + "," + firstDrawCol + "," + lastDrawCol);
            }

            Utilities.printOutWithPriority(false, "MyGridScore::drawCurrentCamInOffscreen(): left = " + left + ", x1,y1,w,h = "
                    + x1 + "," + y1 + "," + w + "," + h);

            Graphics2D g = offscreenGraphics;
//            Stroke old = g.getStroke();
//            g.setStroke(new BasicStroke((float) 7));
//            g.setColor(Color.RED);
//            //g.drawRect(x1,y1,w,h);
//            g.setStroke(old);
            // if (true) return;

            //------------------------------------------
            // Calcula línies de beat/compàs tenint en compte canvis mid-score
            boolean[] isBeat    = new boolean[nCols + 1];
            boolean[] isMeasure = new boolean[nCols + 1];
            computeBeatMeasureLines(nCols + 1, isBeat, isMeasure);

            for (int col = lastDrawCol - 1; col >= firstDrawCol; col--) {
                for (int row = 0; row < nKeys; row++) {
                    this.drawSquare(row, col, offscreenGraphics);
                }
            }
            // Línies DESPRÉS dels squares (inclou lastDrawCol) perquè no quedin tapades.
            for (int col = lastDrawCol; col >= firstDrawCol; col--) {
                if (isBeat[col])    drawBeatLine(col, offscreenGraphics);
                if (isMeasure[col]) drawMeasureLine(col, offscreenGraphics);
            }
            if (isMeasure[nCols]) drawMeasureLine(nCols, offscreenGraphics);
            drawSelectionOverlay(offscreenGraphics);
            Utilities.printOutWithPriority(false, "MyGridScore::drawCurrentCamInOffscreen first, last = " + firstDrawCol + ", " + (lastDrawCol - 1) + ", countDrawFull = " + countDrawFull++);
        }
    }
    public void drawCurrentCam(Graphics2D g) { // directa i no offScreen
            int ccol = getCurrentCol();
            int firstDrawCol = Math.max(0, ccol - Settings.getnColsCam());
            int lastDrawCol = ccol;//Math.min(ccol-delay, nCols);

            //------------------------------------
            boolean left = !this.isUseScreenKeyboardRight();
            int firstColToDraw;
            int x1, x2;
            int y1;//  = (int) Math.round(this.cam.getScreenY(Settings.getScoreFirstRow())); //Settings.getCamFirstRow();
            int w, w2;
            int h;
            if (left) {
                int ncolscam = Settings.getnColsCam();
                x1 = (int) (firstDrawCol * Settings.getColWidth()); // (int) Math.round(this.cam.getScreenX(firstDrawCol));//Settings.getCamFirstCol(this.isUseScreenKeyboardRight());
                y1 = 0; //(int) Math.round(this.cam.getScreenY(Settings.getScoreFirstRow())); //Settings.getCamFirstRow();
                w = (int) Math.ceil(this.cam.getWidth());//(int) this.getWidth();
                h = (int) Math.ceil(nKeys * Settings.getRowHeight() * Settings.getnRowsSquare());
                firstColToDraw = firstDrawCol; // (int) Math.max(0,getCurrentCol()-Settings.getnColsCam());
                int lastColToDraw = lastDrawCol; // getCurrentCol();
                x2 = (int) Math.round(firstColToDraw * Settings.getColWidth());
                w2 = (int) Math.ceil((lastColToDraw - firstColToDraw) * Settings.getColWidth());
            } else {
                int ncolscam = Settings.getnColsCam();
                int firstCamColIn = Math.max(0, -Settings.getnColsCam() + getCurrentCol());
                int actualWidthInCols = Settings.getnColsCam() - firstCamColIn;
                x1 = (int) (firstDrawCol * Settings.getColWidth());//Settings.getCamFirstCol(this.isUseScreenKeyboardRight());
                y1 = 0; // (int) Math.round(this.cam.getScreenY(Settings.getScoreFirstRow())); //Settings.getCamFirstRow();
                w = (int) Math.ceil(actualWidthInCols * Settings.getColWidth());//(int) this.getWidth();
                h = (int) Math.ceil(nKeys * Settings.getRowHeight() * Settings.getnRowsSquare());
                firstColToDraw = firstDrawCol; // (int) Math.max(0,getCurrentCol()-Settings.getnColsCam());
                int lastColToDraw = lastDrawCol; // getCurrentCol();
                x2 = (int) (firstColToDraw * Settings.getColWidth());
                w2 = (int) ((lastColToDraw - firstColToDraw) * Settings.getColWidth());
                Utilities.printOutWithPriority(false, "MyGridScore::drawCurrentCamInOffscreen(): left = " + left + ", firstInCamx,colsWidth,first,last = "
                        + firstCamColIn + "," + actualWidthInCols + "," + firstDrawCol + "," + lastDrawCol);
            }

            Utilities.printOutWithPriority(false, "MyGridScore::drawCurrentCamInOffscreen(): left = " + left + ", x1,y1,w,h = "
                    + x1 + "," + y1 + "," + w + "," + h);

//            Graphics2D g = offscreenGraphics;
//            Stroke old = g.getStroke();
//            g.setStroke(new BasicStroke((float) 7));
//            g.setColor(Color.RED);
//            //g.drawRect(x1,y1,w,h);
//            g.setStroke(old);
//            // if (true) return;

            //------------------------------------------
            int col = lastDrawCol;
            if ((col % Settings.getnColsBeat()) == 0) {
                //System.out.println("MyGridScore::draw: "+col+" acabo de pintar tots els grids.");
                drawBeatLine(col, g);
                //System.out.println("MyGridScore::draw: "+col+" ara he pintat beat line.");
            }
            if ((col % (Settings.getnColsBeat() * getNumBeatsMeasure())) == 0) {
                drawMeasureLine(col, g);
            }
            for (col = lastDrawCol - 1; col >= firstDrawCol; col--) {
//        for (int col = firstDrawCol; col < this.getCurrentCol() && col < this.nCols; col++) {
                for (int row = 0; row < nKeys; row++) {
                    this.drawSquare(row, col, g);
                }
            }
            for (col = lastDrawCol - 1; col >= firstDrawCol; col--) {
                if ((col % Settings.getnColsBeat()) == 0) {
                    //System.out.println("MyGridScore::draw: "+col+" acabo de pintar tots els grids.");
                    drawBeatLine(col, g);
                    //System.out.println("MyGridScore::draw: "+col+" ara he pintat beat line.");
                }
                if ((col % (Settings.getnColsBeat() * getNumBeatsMeasure())) == 0) {
                    drawMeasureLine(col, g);
                }
            }
            col = nCols;
            if ((col % (Settings.getnColsBeat() * getNumBeatsMeasure())) == 0) {
                drawMeasureLine(col, g);
            }
            //setLastColDrawn(lastDrawCol-1);
            Utilities.printOutWithPriority(false, "MyGridScore::drawCurrentCamInOffscreen first, last = " + firstDrawCol + ", " + (lastDrawCol - 1) + ", countDrawFull = " + countDrawFull++);
    }

    public int count2 = 1;

    public static boolean once = true;

    public void drawSquare(int row, int col, Graphics2D g) {
        if (once) {
            if (Settings.SHOW_DRAW_HIERARCHY) {
                Utilities.printOutWithPriority(false, "MyGridScore::drawSquare()");
            }
            Utilities.printOutWithPriority(false, "MyGridScore::drawSquare(): (row,col) = ("
                    + row + "," + col + ")");
        }
        once = false;
//        if (scoreRow == 0 && scoreCol >= 0 && scoreCol < 4) {
//            System.out.println("MyGridSquare::draw grid[" + scoreRow + "][" + scoreCol + "] = " + this.midi + " (" + isSqVisible() + ") "
//                    + this.controller.getAllPurposeScore().getCurrentCol());
//        }
        int screenX;
        int screenY;
        if (this.controller.isPrinting()){
            screenX = (int) Math.round(this.getScreenX(col));
            screenY = (int) Math.round(this.getScreenY(row * Settings.getnRowsSquare()));
        } else {
            screenX = (int) Math.round(this.getOffScreenScreenX(col));
            screenY = (int) Math.round(this.getOffScreenScreenY(row * Settings.getnRowsSquare()));
        }
        // this.width = Settings.getColWidth() * nCols;
        //double h = this.height;
        int wdth = (int) Math.ceil(Settings.getSquareWidth());
        int hght = (int) Math.ceil(Settings.getSquareHeight());
        //System.out.println("MyGridSquare::Draw: hght = "+(int) hght);

        //int keyId = row/Settings.getnRowsSquare();
        int midi = ToneRange.keyIdToMidi(row);

        MyGridSquare sq = this.grid[row][col];
        Color color;
        if (sq != null) {
            sq.updateState();
            color = sq.getColor();
        } else {
            if (this.usePentagramaStrips) {
                color = ColorSets.getPentagramaColor(midi);
            } else {
                color = ColorSets.getChoiceColor(midi, choice.getChoiceList());
            }
        }

//        g.setColor(color.GREEN);
//        g.fillRect((int) screenX, (int) screenY, (int) wdth, (int) hght);
//        if (true) return;
        g.setColor(color);
        g.fillRect((int) screenX, (int) screenY, (int) (wdth), (int) hght);

        if (sq != null) {
            sq.updateState();
            if (sq.isSqVisible()) {
                if (!sq.isSqAudible() && Settings.isShowMutted()) {
                    g.setColor(ColorSets.getGridSquareFontColor(midi));
                    g.drawString("X", (int) screenX, (int) (screenY + hght));
                }
                if (!sq.isSq_is_linked()) {
                    String name = "✔";

                    if (this.isShowNoteNames()) {
                        name = ToneRange.getNoteName(midi, this.getMidiKey());
                        name = name.substring(0, name.length() - 1);
                    }
                    g.setColor(sq.getColor());
                    g.fillRect((int) screenX, (int) screenY, (int) wdth, (int) hght);

//                    name = "" + col;
                    g.setColor(ColorSets.getGridSquareFontColor(midi));
//                    System.out.println("MyGridSquare::draw() name = " + name);
                    g.drawString(name, (int) (2 + screenX + wdth / 3), (int) (screenY + hght * 0.8));
//                    g.drawLine(screenX + 1, screenY, screenX + 1, screenY + (int) height);
                    g.drawLine(screenX + 1, screenY, screenX + 1, screenY + hght-1);
                    //g.drawString(name, (int) (screenX), (int) (screenY + hght * 0.8));

                }
                if (sq.isSqDotted() && !sq.isSq_is_linked()) {
                    g.setColor(Color.BLACK);
                    g.fillRect((int) screenX, (int) screenY, (int) hght, (int) hght);
                    if (this.isShowNoteNames()) {
                        String name;
                        name = ToneRange.getNoteName(midi, this.getMidiKey());
                        name = name.substring(0, name.length() - 1);
                        g.setColor(Color.WHITE);
                        g.drawString(name, (int) (screenX + width / 3), (int) (screenY + hght));
                    }
                }
            }
        }
        Stroke stroke = g.getStroke();
        Stroke dashed = new BasicStroke((float) 0.5, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 3}, 0);//15,3
        g.setStroke(dashed);
        g.setStroke(new BasicStroke((float) 0.2));
        g.setColor(java.awt.Color.GRAY);
        g.drawLine((int) screenX, (int) screenY, (int) (screenX + wdth), (int) screenY);
        g.setStroke(stroke);
    }

    private static void drawImageClamped(Graphics2D g, BufferedImage img,
            int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2) {

        int iw = img.getWidth();
        int ih = img.getHeight();

        if (iw <= 0 || ih <= 0) {
            return;
        }

        // Si el source està completament fora, no dibuixis res
        if (sx2 <= 0 || sy2 <= 0 || sx1 >= iw || sy1 >= ih) {
            return;
        }

        int osx1 = sx1, osy1 = sy1, osx2 = sx2, osy2 = sy2;

        // Clamp source
        sx1 = Math.max(0, Math.min(sx1, iw));
        sx2 = Math.max(0, Math.min(sx2, iw));
        sy1 = Math.max(0, Math.min(sy1, ih));
        sy2 = Math.max(0, Math.min(sy2, ih));

        if (sx2 <= sx1 || sy2 <= sy1) {
            return;
        }

        // Ajusta el destí proporcionalment al clamp del source
        double sw = (osx2 - osx1);
        double sh = (osy2 - osy1);
        if (sw == 0 || sh == 0) {
            return;
        }

        double dw = (dx2 - dx1);
        double dh = (dy2 - dy1);

        double leftCut = (sx1 - osx1) / sw;
        double rightCut = (osx2 - sx2) / sw;
        double topCut = (sy1 - osy1) / sh;
        double bottomCut = (osy2 - sy2) / sh;

        int ndx1 = (int) Math.round(dx1 + dw * leftCut);
        int ndx2 = (int) Math.round(dx2 - dw * rightCut);
        int ndy1 = (int) Math.round(dy1 + dh * topCut);
        int ndy2 = (int) Math.round(dy2 - dh * bottomCut);

        if (ndx2 <= ndx1 || ndy2 <= ndy1) {
            return;
        }

        g.drawImage(img, ndx1, ndy1, ndx2, ndy2, sx1, sy1, sx2, sy2, null);
    }
    
    /**
     * Draws the score: each visible grid square, the beat lines and the measure
     * lines.
     *
     * @param g
     */
    @Override
    public void draw(Graphics2D g) {
        if (this.controller.isPrinting()){
            drawCurrentCam(g);
            this.controller.setPrinting(false);
            return;
        }
        if (Settings.SHOW_DRAW_HIERARCHY) {
            Utilities.printOutWithPriority(false, "MyGridScore::draw: drawing " + this.getClass());
        }
        Utilities.printOutWithPriority(false, "MyGridScore::draw: drawing " + this.getClass());
        this.once = true;
        MySlide.once = true;

        if (this.getOffscreenImage() != null) {
            boolean left = !this.isUseScreenKeyboardRight();
            int firstColToDraw, lastColToDraw;
            int x1, x2;
            int y1;//  = (int) Math.round(this.cam.getScreenY(Settings.getScoreFirstRow())); //Settings.getCamFirstRow();
            int w, w2;
            int h;
            int cc = getCurrentCol();
            int ccol = cc;
            if (left) {
                x1 = (int) Math.round(this.cam.getScreenX(0));//Settings.getCamFirstCol(this.isUseScreenKeyboardRight());
                y1 = (int) Math.round(this.cam.getScreenY(Settings.getScoreFirstRow())); //Settings.getCamFirstRow();
                w = (int) Math.ceil(this.cam.getWidth());//(int) this.getWidth();
                h = (int) Math.ceil(nKeys * Settings.getRowHeight() * Settings.getnRowsSquare());
                firstColToDraw = (int) Math.max(0, ccol - Settings.getnColsCam());
                lastColToDraw = ccol;
                if (Settings.isFitAnacrusis() && Settings.isHasAnacrusis() && firstColToDraw == 0) {
                    int extraCols = Settings.getnBeatsMeasure() * Settings.getnColsBeat();
                    lastColToDraw = Math.min(lastColToDraw + extraCols, nCols);
                }
                x2 = (int) Math.round(firstColToDraw * Settings.getColWidth());
                w2 = (int) Math.ceil((lastColToDraw - firstColToDraw) * Settings.getColWidth());
            } else {
                int firstCamColIn = Math.max(0, Settings.getnColsCam() - ccol);
                int actualWidthInCols = Settings.getnColsCam() - firstCamColIn;
                x1 = (int) Math.round(this.cam.getScreenX(firstCamColIn));//Settings.getCamFirstCol(this.isUseScreenKeyboardRight());
                y1 = (int) Math.round(this.cam.getScreenY(Settings.getScoreFirstRow())); //Settings.getCamFirstRow();
                w = (int) Math.ceil(actualWidthInCols * Settings.getColWidth());//(int) this.getWidth();
                h = (int) Math.ceil(nKeys * Settings.getRowHeight() * Settings.getnRowsSquare());
                firstColToDraw = (int) Math.max(0, ccol - Settings.getnColsCam());
                lastColToDraw = ccol;
                x2 = (int) Math.round(firstColToDraw * Settings.getColWidth());
                w2 = (int) Math.ceil((lastColToDraw - firstColToDraw) * Settings.getColWidth());
            }

            Utilities.printOutWithPriority(false, "MyGridScore::draw(): left = " + left + ", CurrentCol = " + ccol);
            Utilities.printOutWithPriority(false, "MyGridScore::draw(): left = " + left + ", firstColToDraw, lastColToDraw = "
                    + firstColToDraw + "," + lastColToDraw);
            Utilities.printOutWithPriority(false, "MyGridScore::draw(): left = " + left + ", x1,y1,w,h,x2,w2 = "
                    + x1 + "," + y1 + "," + w + "," + h + "," + x2 + "," + w2);

//            g.drawImage(this.getOffscreenImage(),
//                    x1, y1, x1 + w, y1 + h, // destí (pantalla) 
//                    x2, 0, x2 + w2, h, // origen (imatge)
//                    null);
            
            drawImageClamped(g, this.getOffscreenImage(),
                    x1, y1, x1 + w, y1 + h, // destí (pantalla) 
                    x2, 0, x2 + w2, h);

        }
    }

    /**
     * Given an absolute Y position in the screen, it returns its corresponding
     * row in the score.
     *
     * @param screenY
     * @return
     */
    public int getRow(double screenY) {
        double cspy = cam.getScreenPosY();
        double rowH = Settings.getRowHeight();
        int row = (int) ((screenY - cspy) / rowH);
        int sr = getScoreRow(row) / Settings.getnRowsSquare();
        return sr;
    }

    /**
     * Given an absolute X position in the screen, it return its corresponding
     * column in the score.
     *
     * @param screenX
     * @return
     */
    @Override
    public int getCol(double screenX) {
        int col = (int) ((screenX - cam.getScreenPosX()) / Settings.getColWidth());
        return getScoreCol(col);
    }

    /**
     * Given an absolute position in the screen (screenX,screenY), if this
     * position hits a grid square, it sets which row and which column has been
     * hit, and returns the row (else -1). This method should be invoked before
     * whichCol().
     *
     * @param screenX
     * @param screenY
     * @return
     */
    public int whichRow(double screenX, double screenY) {
        if (cam.contains(screenX, screenY)) {
            int c = getCol(screenX);
            int r = getRow(screenY);
            // Guard against values that fall outside the grid array bounds
            if (c < 0 || c >= getNumCols() || r < 0 || r >= getnKeys()) {
                this.whichRow = -1;
                this.whichCol = -1;
                return -1;
            }
            this.whichCol = c;
            this.whichRow = r;
            return this.whichRow;
        }
        this.whichRow = -1;
        this.whichCol = -1;
        return -1;
    }

    /**
     * Returns the column selected in the previous call to whichRow() (-1,
     * none).
     *
     * @return
     */
    public int whichCol() {
        if (this.whichRow != -1) {
            return whichCol;
        }
        return -1;
    }

//    /**
//     * Toggles the grid square at the given score row and column. Deprecated.
//     *
//     * @param row
//     * @param col
//     */
//    public boolean toggle(int row, int col) {
//        boolean added = this.grid[row][col].toggle();
//        if (col + 1 > this.controller.getAllPurposeScore().getLastColWritten()) {
//            this.controller.getAllPurposeScore().setLastColWritten(col + 1);
//        }
//        return added;
//    }
//
    /**
     * Check wether the corresponding square is on.
     *
     * @param row
     * @param col
     * @return
     */
    public boolean isNotNullAndVisible(int row, int col) {
        if (this.grid[row][col] == null) {
            return false;
        }
        return this.grid[row][col].isSqVisible();
    }

    public void setUseScreenKeyboardRight(boolean useScreenKeyboardRight) {
        this.useScreenKeyboardRight = useScreenKeyboardRight;
    }

    public final boolean isUseScreenKeyboardRight() {
        return useScreenKeyboardRight;
    }

    /**
     * It returns the corresponding grid square.
     *
     * @param row
     * @param col
     * @return
     */
    public MyGridSquare getGridSquare(int key, int col) {
        return this.grid[key][col];
    }

    /**
     * getter.
     *
     * @return
     */
//    public MyXiloKeyboard getKeyboard() {
//        return keyboard;
//    }
//    /**
//     * It "plays" the correspondig grid square (see MyGridSquare).
//     * 
//     * @param row
//     * @param col 
//     */
//    public void play(int row, int col) {
//        this.grid[row][col].play();
//    }
//
    /**
     * It "stops" the correspondig grid square (see MyGridSquare).
     *
     * @param row
     * @param col
     */
    public void stop(int row, int col) {
        this.grid[row][col].stop();
    }

    /**
     * Stops all the grid squares of the grid.
     */
    public void stopAll() {
        for (int row = 0; row < nKeys; row++) {
            for (int col = 0; col < nCols; col++) {
                if (this.grid[row][col] != null) {
                    this.grid[row][col].stop();
                }
            }
        }
    }

    /**
     * Draws a measure line at the indicated column.
     *
     * @param col
     * @param g
     */
    private void drawMeasureLine(int col, Graphics2D g) {
        Stroke stroke = g.getStroke();
        g.setStroke(new BasicStroke((float) 3));
        int camX1;
        int camY1;
        if (this.controller.isPrinting()){
            camX1 = (int) Math.floor(getScreenX(col));
            camY1 = (int) Math.round(getScreenY(0));
        } else {
            camX1 = (int) Math.floor(getOffScreenScreenX(col));
            camY1 = (int) Math.round(getOffScreenScreenY(0));
        }
        int camX2 = camX1;
        int camY2 = camY1 + (int) (nRows * Settings.getRowHeight());
        g.setColor(java.awt.Color.BLACK);
        g.drawLine(camX1, camY1+1, camX2, camY2-2);
        g.setStroke(stroke);
    }

    /**
     * Draws a beat line at the indicated column.
     *
     * @param col
     * @param g
     */
    private void drawBeatLine(int col, Graphics2D g) {
        Stroke stroke = g.getStroke();
        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{15, 3}, 0);
        g.setStroke(dashed);
        // g.setStroke(new BasicStroke(3));
        int camX1;
        int camY1;
        if (this.controller.isPrinting()){
            camX1 = (int) Math.floor(getScreenX(col));
            camY1 = (int) Math.round(getScreenY(0));
        } else {
            camX1 = (int) Math.floor(getOffScreenScreenX(col));
            camY1 = (int) Math.round(getOffScreenScreenY(0));
        }
        int camX2 = camX1;
        int camY2 = camY1 + (int) (nRows * Settings.getRowHeight());
        g.setColor(java.awt.Color.BLACK);
        g.drawLine(camX1, camY1+1, camX2, camY2-2);
        g.setStroke(stroke);
    }

    /**
     * Transposes the score by step halftones.
     *
     * @param step
     */
    public void transpose(int step) {
        boolean playing = this.cam.isPlaying();
        if (playing) {
            this.cam.stop();
            this.controller.getKeyboard().stopAll();
            this.stopAll();
        }
        this.subComponents.clear();
        MyGridSquare[][] newgrid = new MyGridSquare[nKeys][nCols];
        for (int row = 0; row < nKeys; row++) {
            for (int col = 0; col < nCols; col++) {
                newgrid[row][col] = null;
            }
        }
        for (int row = 0; row < nKeys; row++) {
            int rowDest = row - step;
            if ((rowDest < nKeys) && rowDest >= 0) {
                for (int col = 0; col < nCols; col++) {
                    MyGridSquare sq = grid[row][col];
                    if (sq != null) {
                        newgrid[rowDest][col] = new MyGridSquare(
                                col, rowDest, sq.nCols, sq.nRows, sq.parent, controller, this, this.cam);
                        newgrid[rowDest][col].transposedSquare(sq, rowDest, col);
                    }
                    this.add(newgrid[rowDest][col]);
                }
            }
        }
        this.midiKey = this.midiKey + step;
        while (midiKey > ToneRange.getHighestMidi()) {
            midiKey -= 12;
        }
        while (midiKey < ToneRange.getLowestMidi()) {
            midiKey += 12;
        }
        // Transposa les marques de tonalitat registrades al changeMap
        for (ScoreChange sc : changeMap.values()) {
            if (sc.midiKey != null) {
                sc.midiKey = sc.midiKey + step;
                while (sc.midiKey > ToneRange.getHighestMidi()) sc.midiKey -= 12;
                while (sc.midiKey < ToneRange.getLowestMidi()) sc.midiKey += 12;
            }
        }
        this.grid = newgrid;
        this.transposeChordSymbolLine(step);
        this.transposeBackgroundChordLine(step);
        this.background.transposeCurrentChord(step);
        this.choice.transposeChoice(step);
        this.updateStripsNKeyboard(usePentagramaStrips);
        if (playing) {
            this.controller.play();
        }
    }

    /**
     * Transpose the chord symbol line by step.
     *
     * @param step
     */
    public void transposeChordSymbolLine(int step) {
        for (int i : chordSymbolLine.keySet()) {
            Chord ch = chordSymbolLine.get(i);
            ch = ch.transpose(step);
            chordSymbolLine.put(i, ch);
        }
    }

    /**
     * Transpose the background chord line by step.
     *
     * @param step
     */
    public void transposeBackgroundChordLine(int step) {
        for (int i : backgroundChordLine.keySet()) {
            Chord ch = backgroundChordLine.get(i);
            ch = ch.transpose(step);
            backgroundChordLine.put(i, ch);
        }
    }

    /**
     * getter.
     *
     * @return
     */
    public BackgroundChordPlayer getBackgroundChordPlayer() {
        return background;
    }

    /**
     * getter.
     *
     * @return
     */
    public int getNumBeatsMeasure() {
        return numBeatsMeasure;
    }

    public String params2TimeSignature(){
        int denominator = this.beatFigure;
        int numerator = this.numBeatsMeasure;
        if (denominator == 8){
            numerator *= 3;
        }
        return numerator+"/"+denominator;
    }
    
    public void timeSignature2Params(String timeSignature){
        timeSignature = timeSignature.trim();
        String[] parts = timeSignature.split(("/"));
        int numerator = Integer.parseInt(parts[0].trim());
        int denominator = Integer.parseInt(parts[1].trim());
        if (denominator == 8) numerator /=3;
        this.setNumBeatsMeasure(numerator);
        this.setBeatFigure(denominator);
        Settings.updateNColsBeat();
    }


    /**
     * setter.
     *
     * @param numBM
     */
    public final void setNumBeatsMeasure(int numBM) {
        numBeatsMeasure = numBM;
        //Settings.setnBeatsMeasure(numBM);
    }

    /**
     * Desa els valors de timing de base (col 0) usats per computeBeatMeasureLines.
     * S'ha de cridar quan s'inicialitza la partitura o quan es fa clearScore,
     * però NO des d'applyChangesAt (que modifica Settings per a la reproducció).
     */
    public void freezeBaseTimingParams() {
        // Comença amb els valors actuals de Settings/score
        int nColsBeat     = Settings.getnColsBeat();
        int nBeatsMeasure = this.numBeatsMeasure;
        int beatFig       = this.beatFigure;
        // Si hi ha un canvi a la col 0, aplica'l sobre la base
        ScoreChange sc0 = changeMap.get(0);
        if (sc0 != null) {
            if (sc0.nColsBeat     != null) nColsBeat     = sc0.nColsBeat;
            if (sc0.nBeatsMeasure != null) nBeatsMeasure = sc0.nBeatsMeasure;
            if (sc0.beatFigure    != null) beatFig       = sc0.beatFigure;
        }
        this.baseNColsBeat     = nColsBeat;
        this.baseNBeatsMeasure = nBeatsMeasure;
        this.baseBeatFigure    = beatFig;
        // Calcula i congela el nombre de columnes per pàgina (valor fix per a la navegació)
        if (cam != null) {
            this.fixedColsPerPage = getNumColsPage();
        }
    }

    public int getBaseNBeatsMeasure() { return baseNBeatsMeasure; }
    public int getBaseBeatFigure()    { return (baseBeatFigure > 0) ? baseBeatFigure : beatFigure; }

    public int getBaseColsPerMeasure() {
        int colsBeat     = (baseNColsBeat     > 0) ? baseNColsBeat     : Settings.getnColsBeat();
        int beatsMeasure = (baseNBeatsMeasure > 0) ? baseNBeatsMeasure : numBeatsMeasure;
        int cols = colsBeat * beatsMeasure;
        return cols > 0 ? cols : 1;
    }

    /**
     * Retorna el nombre de columnes per pàgina congelat en la inicialització.
     * Si encara no s'ha calculat, usa getNumColsPage() com a fallback.
     */
    public int getFixedColsPerPage() {
        return (fixedColsPerPage > 0) ? fixedColsPerPage : getNumColsPage();
    }

//    /**
//     * Saves the current grid in a simple format (to be replaced).
//     * 
//     * @param output 
//     */
//    @Deprecated
//    public void save(PrintWriter output) {
//        output.println(nKeys);
//        output.println(nCols);
//        for (int row = 0; row < nKeys; row++) {
//            for (int col = 0; col < nCols; col++) {
//                if (this.grid[row][col].isSqVisible()) {
//                    if (this.grid[row][col].isSqAudible()) {
//                        output.print("2 ");
//                    } else {
//                        output.print("1 ");
//                    }
//                } else {
//                    output.print("0 ");
//                }
//            }
//            output.println();
//        }
//    }
//
//    /**
//     * Loads the grid score from a file (to be replaced).
//     * @param input
//     * @throws IOException 
//     */
//    @Deprecated
//    public void load(BufferedReader input) throws IOException {
//        String line = input.readLine();
//        int nrows = Integer.parseInt(line.trim());
//
//        line = input.readLine();
//        int ncols = Integer.parseInt(line.trim());
//
//        this.grid = new MyGridSquare[nrows][ncols];
//        for (int row = 0; row < nrows; row++) {
//            line = input.readLine();
//            String[] cols = line.split(" ");
//            for (int col = 0; col < ncols; col++) {
//                switch (cols[col].trim()) {
//                    case "1":
//                        this.grid[row][col] = new MyGridSquare(col, row, 1, 1, this, this.controller, this, cam);
//                        this.add(grid[row][col]);
//                        break;
//                    case "0":
//                        this.grid[row][col] = new MyGridSquare(col, row, 1, 1, this, this.controller, this, cam);
//                        this.add(grid[row][col]);
//                        break;
//                    case "2":
//                        this.grid[row][col] = new MyGridSquare(col, row, 1, 1, this, this.controller, this, cam);
//                        this.add(grid[row][col]);
//                        break;
//                    default:
//                        break;
//                }
//            }
//        }
//    }
//    
    //private static boolean once = true;
    /**
     * Plays the score at the given column.
     *
     * @param col
     * @return
     */
    public boolean playScoreCol(int col) {
        Utilities.printOutWithPriority(false,"MyGridScore::playScoreCol(): col = "+col+" playBar = "+this.cam.getPlayBar());
        if (col >= 0 && col < this.getNumCols() && col < this.controller.getAllPurposeScore().getStopCol()) {
            if (Settings.isPlayAtBeat() && once) {
                once = false;
                System.out.println("MyGridScore::playScoreCol: playing at Beat is on");
            }
            this.keyboard.playAtBeat(20);
            this.checkNRunMidiMessage(col);
            this.checkNShowMessage(col);
//            if (this.controller.getAllPurposeScore().getBackground().isBackgroundChordPlayerOn()) {
//                this.checkNPlayBackgroundChord(col);
//            }
            MyGridSquare gridSq;
            int nks = this.nKeys;
            for (int keyId = 0; keyId < nks; keyId++) {
                gridSq = this.getGridSquare(keyId, col);
//                if (!gridSq.isEmpty()) System.out.println("MyGridScore::playScoreCol: col = "+col);
                if (gridSq != null) {
                    gridSq.checkNPlay();
                } else {
                    this.keyboard.getKey(keyId).stopAllChannels();
                }
            }
            return true;
        }
        return false;
    }

    public void updateState(int row, int col) {
        this.grid[row][col].updateState();
    }

    /**
     * Checks if there is a midi message at the current col, and runs it.
     *
     * @param col
     */
    public void checkNRunMidiMessage(int col) {
        if (SampleOrMidi.isMidi()) {
            ArrayList<MidiMessage> midis = this.midiMessages.get(col);
            if (midis != null) {
                for (MidiMessage mess : midis) {
                    SoundWithMidi.runMidiMessage(mess, this);
//                    System.out.println("Col="+col+" "+SoundWithMidi.showMidiMessageBytes(mess));
                }
            }
        }
    }

    /**
     * Checks if there is a message at the given column, and it shows it on the
     * status line.
     *
     * @param col
     */
    public void checkNShowMessage(int col) {
        MyStatusLine statusLine = this.controller.getStatusLine();
        String mess = messages.get(col);
        if (mess != null) {
            // System.err.print(mess+"\n");
            statusLine.setRightText(mess);
        }
    }

    /**
     * Checks if there is a background chord in the given column, and it plays
     * it (or it stops the current background chord if the chord root is -2).
     *
     * @param col
     */
    public void checkNPlayBackgroundChord(int col) {
        Chord chord = backgroundChordLine.get(col);
        if (chord != null) {
            if (chord.getRoot() == Settings.IS_STOP_CHORD) {
                this.background.stop();
            } else if (chord.getRoot() == Settings.END) {
                this.controller.stop();
                this.controller.getButtons().stopPlayButton();
            } else if (chord.getRoot() >= -11) {
                this.background.setNPlay(chord);
            }
        } else if (this.background.isPaused()) {
            this.background.play();
        }
    }

    /**
     * Retorna totes les SubSquare que pertanyen a la pista indicada.
     */
    public List<MyGridSquare.SubSquare> getNotesOfTrack(int trackId) {
        List<MyGridSquare.SubSquare> result = new ArrayList<>();
        for (int row = 0; row < this.nKeys; row++) {
            for (int col = 0; col < this.getnCols(); col++) {
                MyGridSquare sq = this.getGridSquare(row, col);
                if (sq != null) {
                    for (MyGridSquare.SubSquare sub : sq.getPoliNotes()) {
                        if (sub.getTrack() == trackId) {
                            result.add(sub);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Elimina totes les notes de la pista especificada del gridScore.
     */
//    public void removeAllNotesOfTrack(int trackId) {
//        removeNotesOfList(getNotesOfTrack(trackId));
////        for (int row = 0; row < this.nKeys; row++) {
////            for (int col = 0; col < this.getnCols(); col++) {
////                MyGridSquare sq = this.getGridSquare(row, col);
////                LinkedList<MyGridSquare.SubSquare> toRemove = new LinkedList<>();
////                for (MyGridSquare.SubSquare sub : sq.getPoliNotes()) {
////                    if (sub.getTrack() == trackId) {
////                        toRemove.add(sub);
////                    }
////                }
////                if (!toRemove.isEmpty()) {
////                    sq.getPoliNotes().removeAll(toRemove);
////                    sq.updateState(); // Important per actualitzar l’aspecte
////                }
////            }
////        }
//    }
//
    public List<SubSquare> getNotesOfRegion(int row1, int col1, int row2, int col2) {
        List<SubSquare> notes = new ArrayList<>();
        int rMin = Math.min(row1, row2);
        int rMax = Math.max(row1, row2);
        int cMin = Math.min(col1, col2);
        int cMax = Math.max(col1, col2);

        for (int row = rMin; row <= rMax; row++) {
            for (int col = cMin; col <= cMax; col++) {
                if (grid[row][col] != null) {
                    notes.addAll(grid[row][col].getPoliNotes());
                }
            }
        }
        return notes;
    }

    public void removeNotesOfList(List<MyGridSquare.SubSquare> notesToRemove) {
        for (int row = 0; row < nKeys; row++) {
            for (int col = 0; col < getnCols(); col++) {
                MyGridSquare sq = getGridSquare(row, col);
                LinkedList<MyGridSquare.SubSquare> poli = sq.getPoliNotes();
                poli.removeIf(notesToRemove::contains);
                sq.updateState();
            }
        }
    }

    // Faalta provar, no conserva els mutted etc.
    public void pasteNotesFromList(List<MyGridSquare.SubSquare> notes, int baseRow, int baseCol) {
        for (MyGridSquare.SubSquare note : notes) {
            int relRow = note.getSquare().getScoreRow();  // assume that SubSquare knows onquin MyGridSquare vivia
            int relCol = note.getSquare().getScoreCol();
            int newRow = baseRow + relRow;
            int newCol = baseCol + relCol;
            if (isValidCell(newRow, newCol)) {
                MyGridSquare target = getGridSquare(newRow, newCol);
                this.addNoteToSquare(newRow, newCol, 1, Settings.getnRowsSquare(), (MyComponent) this, this.controller, this, this.controller.getCam(),
                        note.getChannel(),
                        note.getTrack(),
                        note.getVelocity(),
                        true, // assumim visible
                        false, // no muttada
                        false, // no lligada
                        false // no puntejada
                );
            }
        }
    }

    public boolean isValidCell(int row, int col) {
        if (row < 0) {
            return false;
        }
        if (row >= nKeys) {
            return false;
        }
        if (col < 0) {
            return false;
        }
        if (col >= nRows) {
            return false;
        }
        return true;
    }

    // ---------------------------------------------------------------------------
    // Mètodes del changeMap (mapa de canvis per columna)
    // ---------------------------------------------------------------------------

    /**
     * Registra (o fusiona) un ScoreChange a la columna indicada.
     * Si ja hi ha una entrada, es fusionen els camps no-null.
     *
     * @param col    columna de partitura
     * @param change canvis a registrar
     */
    public void setScoreChange(int col, ScoreChange change) {
        ScoreChange existing = changeMap.get(col);
        if (existing == null) {
            existing = new ScoreChange();
            changeMap.put(col, existing);
        }
        existing.mergeFrom(change);
    }

    /**
     * Retorna el ScoreChange efectiu a la columna indicada, acumulant
     * tots els canvis de les columnes anteriors o iguals a {@code col}.
     * Els camps null indiquen que aquell paràmetre no ha estat mai modificat.
     *
     * @param col columna de partitura
     * @return ScoreChange acumulat (pot tenir tots els camps null si changeMap és buit)
     */
    public ScoreChange getEffectiveChange(int col) {
        ScoreChange result = new ScoreChange();
        // headMap(col, true) = claus <= col: el canvi a la columna X
        // entra en vigor exactament a X, no un pas abans.
        for (ScoreChange sc : changeMap.headMap(col, true).values()) {
            result.mergeFrom(sc);
        }
        return result;
    }

    /**
     * Buida el mapa de canvis per columna.
     */
    public void clearChangeMap() {
        changeMap.clear();
    }

    /**
     * Retorna el mapa de canvis per columna (lectura). Útil per a components
     * com MyChordSymbolLine que necessiten consultar les marques de canvi.
     */
    public java.util.TreeMap<Integer, ScoreChange> getChangeMap() {
        return changeMap;
    }

    /**
     * Calcula les posicions de les línies de beat i de compàs per a totes les
     * columnes de la partitura, tenint en compte els canvis de compàs registrats
     * al {@code changeMap}. Útil per a {@code MyChordSymbolLine} i
     * {@code MyLyrics} que necessiten dibuixar les mateixes línies.
     *
     * @param numCols   nombre de columnes (longitud dels arrays de sortida)
     * @param isBeat    array de sortida: {@code true} si la columna és inici de beat
     * @param isMeasure array de sortida: {@code true} si la columna és inici de compàs
     */
    /**
     * Retorna la primera columna del compàs que conté {@code col}.
     * Si {@code col} ja és l'inici d'un compàs, el retorna directament.
     * Gestiona canvis de compàs mid-score via computeBeatMeasureLines.
     *
     * @param col columna clicada (0-based)
     * @return primera columna del compàs corresponent
     */
    public int getFirstColOfCurrentMeasure(int col) {
        if (col <= 0) return 0;
        // Calcula fins a col+1 (només necessitem isMeasure[0..col])
        boolean[] isMeasure = new boolean[col + 1];
        computeBeatMeasureLines(col + 1, null, isMeasure);
        for (int c = col; c >= 0; c--) {
            if (isMeasure[c]) return c;
        }
        return 0;
    }

    /**
     * Retorna el número de compàs (1-based) i el beat dins del compàs (1-based)
     * a la columna {@code col}, tenint en compte els canvis de compàs del changeMap.
     *
     * @param col columna (0-based)
     * @return array de dos elements: [measureNumber, beatNumber], ambdós 1-based
     */
    public int[] getMeasureAndBeatAt(int col) {
        if (col < 0) return new int[]{1, 1};

        int curNColsBeat     = (baseNColsBeat     > 0) ? baseNColsBeat     : Settings.getnColsBeat();
        int curNBeatsMeasure = (baseNBeatsMeasure  > 0) ? baseNBeatsMeasure  : getNumBeatsMeasure();
        int colInBeat        = 0;
        int beatInMeasure    = 0;
        int measure          = Settings.isHasAnacrusis() ? 0 : 1;
        int beat             = 1;  // 1-based

        for (int c = 0; c <= col; c++) {
            // Aplica canvis de compàs registrats exactament en aquesta columna
            ScoreChange sc = changeMap.get(c);
            if (sc != null) {
                if (sc.nColsBeat     != null) curNColsBeat     = sc.nColsBeat;
                if (sc.nBeatsMeasure != null) curNBeatsMeasure = sc.nBeatsMeasure;
                colInBeat     = 0;
                beatInMeasure = 0;
            }

            // Inici de beat
            if (colInBeat == 0) {
                // Inici de compàs
                if (beatInMeasure == 0) {
                    if (c > 0) measure++;  // col 0 ja és el compàs 1
                    beat = 1;
                } else {
                    beat = beatInMeasure + 1;
                }
            }

            colInBeat++;
            if (colInBeat >= curNColsBeat) {
                colInBeat = 0;
                beatInMeasure++;
                if (beatInMeasure >= curNBeatsMeasure) beatInMeasure = 0;
            }
        }

        return new int[]{measure, beat};
    }

    public void computeBeatMeasureLines(int numCols, boolean[] isBeat, boolean[] isMeasure) {
        // Usa els valors de base (col 0), no els valors globals de Settings que
        // applyChangesAt pot haver modificat per a la posició del playbar.
        int curNColsBeat     = (baseNColsBeat    > 0) ? baseNColsBeat    : Settings.getnColsBeat();
        int curNBeatsMeasure = (baseNBeatsMeasure > 0) ? baseNBeatsMeasure : getNumBeatsMeasure();
        int colInBeat       = 0;
        int beatInMeasure   = 0;

        for (int col = 0; col < numCols; col++) {
            // Aplica canvis de compàs registrats exactament en aquesta columna
            ScoreChange sc = changeMap.get(col);
            if (sc != null) {
                if (sc.nColsBeat     != null) curNColsBeat     = sc.nColsBeat;
                if (sc.nBeatsMeasure != null) curNBeatsMeasure = sc.nBeatsMeasure;
                // Reinicia els comptadors al punt de canvi
                colInBeat     = 0;
                beatInMeasure = 0;
            }

            if (colInBeat == 0) {
                if (isBeat    != null) isBeat[col]    = true;
                if (beatInMeasure == 0 && isMeasure != null) isMeasure[col] = true;
            }

            colInBeat++;
            if (colInBeat >= curNColsBeat) {
                colInBeat = 0;
                beatInMeasure++;
                if (beatInMeasure >= curNBeatsMeasure) beatInMeasure = 0;
            }
        }
    }

    private Thread playThread;
    //private volatile boolean isPlaying = false;
    private long nanosPerStep = 125_000_000; // per exemple, 125 ms = 125_000_000 ns
    private int playingCol;

//    public void startPlayback() {
//        MyCamera cam = this.controller.getCam(); 
//        if (cam.isPlaying()) return;
//        cam.setPlaying(true);
//        this.playingCol = this.getCurrentCol(); // <–– usa l'atribut, no variable local
//
//        playThread = new Thread(() -> {
//            long nextStep = System.nanoTime();
//
//            while (cam.isPlaying() && playingCol < nCols) {
//                long now = System.nanoTime();
//
//                if (now >= nextStep) {
//                    playScoreCol(playingCol++);
//                    nextStep += nanosPerStep;
//                } else {
//                    long sleepTime = (nextStep - now) / 1_000_000;
//                    if (sleepTime > 1) {
//                        try {
//                            Thread.sleep(sleepTime);
//                        } catch (InterruptedException e) {
//                            break;
//                        }
//                    } else {
//                        Thread.yield();
//                    }
//                }
//            }
//
//            cam.setPlaying(false);
//        });
//        playThread.setDaemon(true);
//        playThread.start();
//    }
//    public void stopPlayback() {
//        this.controller.getCam().setPlaying(false);
//        if (playThread != null && playThread.isAlive()) {
//            playThread.interrupt();
//        }
//    }
//    public boolean isIsPlaying() {
//        return isPlaying;
//    }
//
//    public void setIsPlaying(boolean isPlaying) {
//        this.isPlaying = isPlaying;
//    }
}
