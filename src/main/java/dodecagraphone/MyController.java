package dodecagraphone;

import dodecagraphone.model.MyChoice;
import dodecagraphone.model.MyKeyCircles;
import dodecagraphone.model.component.MyExercise;
import dodecagraphone.model.component.MyButtonPanel;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.color.ColorSets;
import dodecagraphone.model.MyTempo;
import dodecagraphone.model.chord.Chord;
import dodecagraphone.model.component.MyCamera;
import dodecagraphone.model.component.MyChordSymbolLine;
import dodecagraphone.model.component.MyLyrics;
import dodecagraphone.model.component.MyAllPurposeScore;
import dodecagraphone.model.component.MyButton;
import dodecagraphone.model.component.MyComponent;
import dodecagraphone.model.component.MyGridSquare;
import dodecagraphone.model.component.MyGridScore;
import dodecagraphone.model.component.MyMidiScore;
import dodecagraphone.model.component.MyScreen;
import dodecagraphone.model.component.MySlide;
import dodecagraphone.model.component.MyStatusLine;
import dodecagraphone.model.component.MyXiloKey;
import dodecagraphone.model.component.MyXiloKeyboard;
import dodecagraphone.model.exercise.exerciseList.MyExerciseList;
import dodecagraphone.model.mixer.MyMixer;
import dodecagraphone.model.mixer.MyTrack;
import dodecagraphone.model.sound.SampleOrMidi;
import dodecagraphone.model.sound.SoundWithMidi;
import dodecagraphone.teclesControl.Event;
import dodecagraphone.teclesControl.MouseSequence;
import dodecagraphone.teclesControl.PilaEvents;
import dodecagraphone.ui.AppConfig;
import dodecagraphone.ui.I18n;
import dodecagraphone.ui.MeterDialog;
import dodecagraphone.ui.MeterDialog.MeterData;
import dodecagraphone.ui.MyDialogs;
import dodecagraphone.ui.MyUserInterface;
import dodecagraphone.ui.SVGandPDF;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import javax.sound.midi.MetaMessage;
import javax.swing.JOptionPane;

/**
 * Class controller implements user commands by managing model objects.
 */
public class MyController {

//    private int aux = 0;
//   private final String exerciseListName = "Blues";
//    private final String exerciseListName = "Test";
//    private final String exerciseListName = "Saxo";
//    private final String exerciseListName = "Miri";
//    private final String exerciseListName = "UYE";
    private String[] exerciceListNames = new String[]{"Saxo", "Blues", "Miri", "EarTraining", "Test"};
    private String exerciseListName;
    private boolean exercisesOn;

    // private Camera cam;
    private final MyUserInterface ui;
    private final MyScreen screen;
    private MyXiloKeyboard keyboard;
    private MyAllPurposeScore allPurposeScore;
    // private MyAllPurposeScore patternScore;
    private final MyChordSymbolLine myChordSymbolLine;
    private final MyLyrics myLyrics;
    private final MyCamera cam;
    private final MyButtonPanel buttons;
    private final MyStatusLine statusLine;
    private static int ntimes = 0;
    // private static boolean doRedraw = true;
    private int previousRow;
    private int previousCol;

    private MyMixer mixer;

    private String currentMidiFile = "";

    //    private MyButtonPanel buttons;
    private int lastXiloKeyPressed;
    private int lastRowPressed;
    private int lastColPressed;
    private int lastButtonPressed;
    private boolean turningOn;
    private boolean linking = false;
    private MyExerciseList exerciseList;
    private Thread replicador;
    private MyGridSquare firstNote = null;
    private MyGridSquare lastNote = null;
    private boolean needsSaving = false;
    private volatile boolean needsDrawing = true;
    private boolean printing = false;

    private final PilaEvents pilaEvents = new PilaEvents();
    private MouseSequence mouseSequence = null;

    /**
     * Canvi de paràmetre pendent de col·locació per part de l'usuari.
     * Quan és no-null, el pròxim clic al grid col·loca el canvi a aquella columna.
     */
    private MyGridScore.ScoreChange pendingChange = null;

    private SampleOrMidi instrument;
//    private int numBeatsMeasure;
//    private int beatFigure;
//    private int numMeasuresPage;
//    private int totalNumMeasures;
//    private int numGridSquaresBeat;
//    private int tempo;

    /**
     * creates a list with XiloKey's.
     *
     * @param ui
     */
    public MyController(MyUserInterface ui) {
        //Settings.initSettings();
        Settings.initSettings();
        MyDialogs.initDialogs();
        this.ui = ui;
        ColorSets.initColors();
        instrument = new SampleOrMidi("Midi");
//        "BflatClarinet";
//        "Xylophone";        
        ToneRange.setInstrument(instrument.getInstrument());
        if (SampleOrMidi.isMidi()) {
            SoundWithMidi.initMidi(this);
        }
        boolean left = true;
        this.screen = new MyScreen(0, 0, (int) Settings.getnColsScreen(), (int) Settings.getnRowsScreen(), null, this);
        this.cam = new MyCamera(Settings.getCamFirstCol(left), Settings.getCamFirstRow(), Settings.getnColsCam(), Settings.getnRowsCam(), 
                this.screen, this);
        this.allPurposeScore = new MyAllPurposeScore(this);
        this.setAllPurposeScore(allPurposeScore);
        this.allPurposeScore.setUseScreenKeyboardRight(!left);
        this.allPurposeScore.setDefaultDelay();
        this.cam.setPlayBar(Settings.getPlayBarCol(left)+this.allPurposeScore.getDelay(left));
        this.cam.setScore(allPurposeScore);
        this.cam.add(allPurposeScore);
        this.myChordSymbolLine = new MyChordSymbolLine(Settings.getChordFirstCol(), Settings.getChordFirstRow(), Settings.getnColsChord(), Settings.getnRowsChord(), this.cam, this, this.allPurposeScore);
        this.cam.setSymbolLine(myChordSymbolLine);
        this.cam.add(myChordSymbolLine);
        this.myLyrics = new MyLyrics(Settings.getLyricsFirstCol(), Settings.getLyricsFirstRow(), Settings.getnColsLyrics(), Settings.getnRowsLyrics(), this.cam, this, this.allPurposeScore);
        this.cam.setLyrics(myLyrics);
        this.cam.add(myLyrics);
        this.cam.reset();
        this.buttons = new MyButtonPanel(Settings.getFirstColControl(), Settings.getControlFirstRow(), Settings.getnColsControl(), Settings.getnRowsControl(), this.screen, this);
        this.statusLine = new MyStatusLine(Settings.getStatusFirstCol(), Settings.getStatusFirstRow(), Settings.getnColsStatus(), Settings.getnRowsStatus(), this.screen, this);
        boolean showChoice = true;
        this.keyboard = new MyXiloKeyboard(Settings.getKeyboardFirstCol(left), Settings.getKeyboardFirstRow(), Settings.getnColsKeyboard(),
                Settings.getnRowsKeyboard(), !left, this.screen, this, showChoice, Settings.getnKeysKeyboard());
        // System.out.println("MyController::MyController: Xilokey.width =" + this.keyboard.getKeyboard().get(10).getWidth());
        this.allPurposeScore.setKeyboard(this.keyboard);
        this.allPurposeScore.getChoice().setNoneChoice();
        this.allPurposeScore.updateStripsNKeyboard();
        this.setDefaultTrack();
//        this.screen.add(cam);
        this.screen.add(keyboard);
        this.screen.add(statusLine);
        this.screen.add(buttons);
        this.screen.add(cam);
        this.allPurposeScore.placeChromaticDescendingScore();
        int tempo = Settings.getDefaultTempo();
        MyTempo.setTempo(tempo);
//        MyTempo.checkTempo();
        this.lastXiloKeyPressed = -1;
        this.lastRowPressed = -1;
        this.lastButtonPressed = -1;
        this.turningOn = false;
        this.allPurposeScore.setShowNoteNames(true);
        this.allPurposeScore.setUseScreenKeyboardRight(!left);
        this.updateTextOfButtons();
        this.ui.getContentPane().setSize((int) screen.getComponentWidth(), (int) screen.getComponentHeight());
        this.ui.pack();
        this.needsSaving = false;

//this.setScreenKeyboardRight(false); 
// WHY? Si no ho poso no inicialitza bé Offscreen
//boolean left = !right;
//this.allPurposeScore.setUseScreenKeyboardRight(right);
//this.screen.setDimensions(0, 0, (int) Settings.getnColsScreen(), (int) Settings.getnRowsScreen());
//this.keyboard.setDimensions(Settings.getKeyboardFirstCol(left), Settings.getKeyboardFirstRow(), Settings.getnColsKeyboard(), Settings.getnRowsKeyboard());
//this.cam.setDimensions(Settings.getCamFirstCol(left), Settings.getCamFirstRow(), Settings.getnColsCam(), Settings.getnRowsCam());
//this.cam.setPlayBar(Settings.getPlayBarCol(left) + allPurposeScore.getDelay());
//this.statusLine.setDimensions(Settings.getStatusFirstCol(), Settings.getStatusFirstRow(), Settings.getnColsStatus(), Settings.getnRowsStatus());
//this.myChordSymbolLine.setDimensions(Settings.getChordFirstCol(), Settings.getChordFirstRow(), Settings.getnColsChord(), Settings.getnRowsChord());
//this.allPurposeScore.setDimensions(Settings.getScoreFirstCol() + allPurposeScore.getDelay(), Settings.getScoreFirstRow(), Settings.getnColsScore(), Settings.getnRowsScore());
//this.allPurposeScore.setCurrentCol(Settings.getInitialCurrentCol(left) + allPurposeScore.getDelay());
//this.cam.reset();
//
        this.allPurposeScore.initOffscreen();
        this.myChordSymbolLine.initOffscreen();
        this.myLyrics.initOffscreen();
        Utilities.printOutWithPriority(false, "MyController::MyController(): offScreenWidth = "+this.allPurposeScore.getOffscreenImage().getWidth());
//        this.setScreenKeyboardRight(!left);
        //this.getUi().getPanel().repinta(true);
//        this.drawFull(true);
//        this.setNeedsDrawing(true);
//        ui.getPanel().repinta(true); // o ui.repaint();

//        System.out.println("MyController::Controller: Xilokey.width =" + this.keyboard.getKeyboard().get(10).getWidth());
        if (Settings.SHOW_DIMENSIONS) {
            System.out.println("Screen(w, h) = " + (int) Settings.getScreenWidth() + ", " + (int) Settings.getScreenHeight());
            System.out.println("Square(w, h) = " + (int) Settings.getSquareWidth() + ", " + (int) Settings.getSquareHeight());
            System.out.println(this.screen.showLayout(""));
        }
    }

    public void undo() {
        pilaEvents.undo();
        this.drawFull(true);
    }

    public void redo() {
        pilaEvents.redo();
        this.drawFull(true);
    }

    public void afegirEvent(Event e) {
        pilaEvents.afegirEvent(e);
    }

    public boolean isNeedsDrawing() {
        return needsDrawing;
    }

    public void setNeedsDrawing(boolean needsDrawing) {
        this.needsDrawing = needsDrawing;
    }

    public boolean isPrinting() {
        return printing;
    }

    public void setPrinting(boolean printing) {
        this.printing = printing;
    }

    
    public void addChordTrackAndInstrumentToMixer(MyTrack track) {
        track.setVelocity(63);
        int canal = 15;
        track.afegirCanal(canal);
        track.setCurrentChannel(canal);
        track.setVisible(false);
        track.setAudible(true);
        int instr = SoundWithMidi.getChordInstrument();
        SoundWithMidi.assignInstToChannel(canal, instr);
        SoundWithMidi.runProgramChange(canal, instr);
        this.mixer.setChordTrack(track);
        this.mixer.setCurrentTrack(track.getId());
    }

    public void addDrumsTrackAndInstrumentToMixer(MyTrack track) {
        // MyTrack track = new MyTrack(this.mixer.getDrumsTrackId(),this.mixer.getChordTrackName());
        track.setVelocity(63);
        int canal = 9;
        track.afegirCanal(canal);
        track.setCurrentChannel(canal);
        track.setVisible(true);
        track.setAudible(true);
        for (MyTrack tr : this.mixer.getTracks()) {
            tr.setVisible(false);
        }
        this.mixer.setDrumsTrack(track);
        this.mixer.setCurrentTrack(track.getId());
    }

    public void addTrackAndInstrumentToMixer(MyTrack track, int instr) {
        track.setVelocity(63);
        int canal = SoundWithMidi.getNextAvailableChannel();
        track.afegirCanal(canal);
        track.setCurrentChannel(canal);
        SoundWithMidi.assignInstToChannel(canal, instr);
        SoundWithMidi.runProgramChange(canal, instr);
        track.setAudible(true);
        track.setVisible(true);
        this.mixer.addTrack(track);
        this.mixer.setCurrentTrack(track.getId());
    }

    public final void setDefaultTrack() {
        this.mixer = new MyMixer(this);
        MyTrack track = new MyTrack(0, "Track 1");
        track.setIsNew(true);
        SoundWithMidi.resetChannels();
        this.addTrackAndInstrumentToMixer(track, SoundWithMidi.getLeadInstrument());
//        MyTrack bkgr = new MyTrack(1, "ChordTrack");
//        bkgr.setVelocity(31);
//        //this.mixer.addTrack(1, bkgr, false, true, true);
//        bkgr.afegirCanal(14);
//        bkgr.setCurrentChannel(14);
//        SoundWithMidi.assignInstToChannel(14, 19); // ReedOrgan per defecte
//        SoundWithMidi.runProgramChange(14, 19);
//        this.mixer.setCurrentTrack(0);
    }

    public final void setScreenKeyboardRight(boolean right) { // false, left
        boolean left = !right;
        this.allPurposeScore.setUseScreenKeyboardRight(right);
        this.screen.setDimensions(0, 0, (int) Settings.getnColsScreen(), (int) Settings.getnRowsScreen());
        this.keyboard.setDimensions(Settings.getKeyboardFirstCol(left), Settings.getKeyboardFirstRow(), Settings.getnColsKeyboard(), Settings.getnRowsKeyboard());
        this.cam.setDimensions(Settings.getCamFirstCol(left), Settings.getCamFirstRow(), Settings.getnColsCam(), Settings.getnRowsCam());
        this.cam.setPlayBar(Settings.getPlayBarCol(left) + this.allPurposeScore.getDelay(left));
        this.allPurposeScore.setDimensions(Settings.getScoreFirstCol() + allPurposeScore.getDelay(left), Settings.getScoreFirstRow(), Settings.getnColsScore(), Settings.getnRowsScore());
        this.allPurposeScore.setCurrentCol(Settings.getInitialCurrentCol(left,allPurposeScore));
        Utilities.printOutWithPriority(false,"MyController::setScreenKeyboardRight() currentCol = "+this.allPurposeScore.getCurrentCol());
        this.statusLine.setDimensions(Settings.getStatusFirstCol(), Settings.getStatusFirstRow(), Settings.getnColsStatus(), Settings.getnRowsStatus());
        this.myChordSymbolLine.setDimensions(Settings.getChordFirstCol(), Settings.getChordFirstRow(), Settings.getnColsChord(), Settings.getnRowsChord());
        this.cam.reset();
        if (Settings.SHOW_DIMENSIONS) {
            System.out.println("Screen(w,h) = " + Settings.getScreenWidth() + ", " + Settings.getScreenHeight());
            System.out.println("Square(w,h) = " + (int) Settings.getSquareWidth() + ", " + (int) Settings.getSquareHeight());
            System.out.println(this.screen.showLayout(""));
        }
        //this.drawFull(true);
    }

    private void showExerciseList() {
        System.out.println("\nExercise List:");
        for (String label : this.exerciseList.getExerciseLabelList()) {
            if (label.equals(allPurposeScore.getLabel())) {
                System.out.print("> ");
            }
            System.out.println(label + "\t");// + ex.getDescription()
        }
    }

    /**
     * During playing, checks which SquareGrid's collide with the playBar and
     * plays them.
     *
     * @return
     */
    public boolean playScoreColAtPlayBar() {
//        System.out.println("MyController::entering PlayScoreColAtPlayBar");
        int camPBar = cam.getPlayBar();
        int col = allPurposeScore.getScoreCol(camPBar);
        boolean played = allPurposeScore.playScoreCol(col);
        return played;
    }

    public boolean update() {
        boolean modified = this.mixer.isModified();
        this.mixer.setModified(false);

        if (this.buttons.isModified()) {
            modified = true;
            this.updateTextOfButtons();
            this.buttons.setModified(false);
        }
//        if (this.cam.isPlaying()) {
//            if (this.cam.checkTick()) {
////                SwingUtilities.invokeLater(() -> {
//                if (this.playScoreColAtPlayBar()) {
//                    cam.updateCurrentCol();
////                        System.out.println("MyController::update(): currentCol updated");
//                    this.updateTextOfButtons();
////                        System.out.println("MyController::update(): TextOfButtons updated");
//                    modified = true;
//                }
////                });
//                
//            }
//            modified = true;
//        }
        return modified; // Si modified, repinta
    }
    
        /**
         * Updates position of everything that moves (the camera).
         *
         * @return true if there have been changes.
         */
        //    public boolean old_update() {
        //        boolean modified = false;
        //        modified = this.mixer.isModified();
        //        this.mixer.setModified(false);
        //        if (this.cam.isPlaying()) {
        ////            System.out.println("MyController::update(): cam.isPlaying = true");
        //            if (this.cam.checkTick()) { // { // Timing inside
        ////            while (!this.cam.checkTick()); // { // Timing inside
        ////            if (true) { // Timing inside
        //                System.out.println("MyController::update(): playAtBeat. Thread = " + Thread.currentThread());
        //                this.getKeyboard().playAtBeat(20);
//                SwingUtilities.invokeLater(() -> {
//                    if (this.playScoreColAtPlayBar()) {
//                        cam.updateCurrentCol();
////                        System.out.println("MyController::update(): currentCol updated");
////                        System.out.println("MyController::update(): TextOfButtons updated");
//                    } else {
//                        this.stop();
//                        this.buttons.stopPlayButton();
//                    }
//                    this.updateTextOfButtons();
//                });
//            }
//        }
//        // }
//        if (this.buttons.isModified()) {
//            modified = true;
//            this.updateTextOfButtons();
//            this.buttons.setModified(false);
//        }
////        System.out.println("MyController::leaving update() with mofified = "+modified);
//        return modified;
//    }
//
    /**
     * During playing, checks which SquareGrid's collide with the playBar and
     * plays them.
     *
     * @return
     */
//    public boolean playScoreColAtPlayBar() {
////        System.out.println("MyController::entering PlayScoreColAtPlayBar");
//        int camPBar = cam.getPlayBar();
//        int col = allPurposeScore.getScoreCol(camPBar);
//        boolean played = allPurposeScore.playScoreCol(col);
////        System.out.println("MyController::leaving PlayScoreColAtPlayBar, played = "+played);
//        return played;
//    }
    
    private int count3 = 1;

    /**
     * Draws the current image at each ui repaint.
     *
     * @param g
     */
    /**
     * Fills the keyboard-column areas of the chord and lyrics strips with
     * white, and draws a vertical separator line at the camera left edge for
     * each strip. Called at the start of redraw(), before screen.draw().
     */
    private void drawStripsBackground(Graphics2D g) {
        double rowH   = Settings.getRowHeight();
        double keyW   = Settings.getnColsKeyboard() * Settings.getColWidth();

        int chordY  = (int) Math.round(Settings.getChordFirstRow()  * rowH); // = 0
        int chordH  = (int) Math.round(Settings.getnRowsChord()     * rowH);
        int lyricsY = (int) Math.round(Settings.getLyricsFirstRow() * rowH);
        int lyricsH = (int) Math.round(Settings.getnRowsLyrics()    * rowH);
        int kw      = (int) Math.ceil(keyW);
        int sepX    = kw; // x of vertical separator = camera left edge

        // White fill for the keyboard-column part of each strip
        g.setColor(Color.WHITE);
        g.fillRect(0, chordY,  kw, chordH);
        g.fillRect(0, lyricsY, kw, lyricsH);

        // Vertical separator line between keyboard column and camera area
        // (same default stroke as MyCamera.drawRect — BasicStroke(1f))
        g.setColor(Color.BLACK);
        g.drawLine(sepX, chordY,  sepX, chordY  + chordH);
        g.drawLine(sepX, lyricsY, sepX, lyricsY + lyricsH);

        // Labels: vertically centered, left-aligned with a small margin
        java.awt.FontMetrics fm = g.getFontMetrics();
        int textH  = fm.getAscent() - fm.getDescent();
        int margin = (int) Math.max(4, Settings.getColWidth());
        g.setColor(Color.BLACK);
        g.drawString(I18n.t("myChordSymbolLine.label"),
                margin,
                chordY  + (chordH  + textH) / 2);
        g.drawString(I18n.t("myLyrics.label"),
                margin,
                lyricsY + (lyricsH + textH) / 2);
    }

    public void redraw(Graphics2D g) {
//        if (doRedraw) { 
        //g.setColor(Color.WHITE);
        //g.fillRect(0, 0, (int) Settings.getScreenWidth(), (int) Settings.getScreenHeight());
//        Font font = new Font("Courier", Font.BOLD, 14);
        Font font = new Font("Dialog", Font.BOLD, 14);
        g.setFont(font);
        Font f = g.getFont();
	//System.out.println("MyController::redraw() Font real: " + f.getFontName() + " | family=" + f.getFamily());
        //this.setScreenKeyboardRight(this.allPurposeScore.isUseScreenKeyboardRight());
        if(this.isNeedsDrawing()){
            Utilities.printOutWithPriority(5, "MyController::redraw: count = " + count3++);
            drawStripsBackground(g);
            this.screen.draw(g);
//            System.out.println("MyController::redraw(): "+count3++);
        }
        this.setNeedsDrawing(false);
//        aux++;
        //System.out.flush();
//       }
//        doRedraw = false;

//        this.allPurposeScore.draw(g, this.cam);
//        this.xiloKeyboard.draw(g);
//        this.updateButtonsText();
//        this.buttons.draw(g);
//        // Draw camera rect
//        if (false) {
//            g.setColor(Color.magenta);
//            Rectangle camRect = Camera.rect;
//            System.out.println(camRect.x);
//            g.fillRect((int) camRect.x + 8 * Layout.getGridSquareWidth() + Layout.getKeyboardStripWidth(), (int) camRect.y + Layout.getGridSquareHeight(), (int) camRect.w, (int) camRect.h - Layout.getGridSquareHeight());
//        }
    }

    public MouseSequence getMouseSequence() {
        return mouseSequence;
    }

    public MyAllPurposeScore getAllPurposeScore() {
        return allPurposeScore;
    }

    public boolean isTurningOn() {
        return turningOn;
    }

    public MyUserInterface getUi() {
        return ui;
    }

    public MyScreen getScreen() {
        return screen;
    }

    public final void setAllPurposeScore(MyAllPurposeScore allPurposeScore) {
        this.allPurposeScore = allPurposeScore;
    }

    public MyExercise getCurrentExercise() {
        return allPurposeScore;
    }

    public void setMixer(MyMixer mixer) {
        this.mixer = mixer;
    }

    public MyMixer getMixer() {
        return mixer;
    }

    public String getCurrentMidiFile() {
        return currentMidiFile;
    }

    public void setCurrentMidiFile(String currentMidiFile) {
        this.currentMidiFile = currentMidiFile;
    }

    public MyCamera getCam() {
        return cam;
    }

    public MyLyrics getMyLyrics() {
        return myLyrics;
    }

    public MyButtonPanel getButtons() {
        return buttons;
    }

    public MyXiloKeyboard getKeyboard() {
        return keyboard;
    }

    public MyStatusLine getStatusLine() {
        return this.statusLine;
    }

    public void toggle(int keyId, int col) { 
        int wasChannel = -1;
        int wasTrack = -1;
        int wasVelocity = 0;
        boolean wasVisible = false;
        boolean wasMutted = false;
        boolean wasLinked = false;
        boolean wasDotted = false;

        MyGridSquare square = this.getAllPurposeScore().getGridSquare(keyId, col);
        if (square!=null && square.isSqVisible()) {
            MyTrack tr = this.getMixer().getCurrentTrack();
            tr.oneNoteLess();
            MyGridSquare.SubSquare note = this.getAllPurposeScore().removeNoteFromSquare(keyId,col,this.getMixer().getCurrentChannelOfCurrentTrack(),
                    this.getMixer().getCurrentTrackId());
            wasChannel = this.getMixer().getCurrentChannelOfCurrentTrack();
            wasTrack = this.getMixer().getCurrentTrackId();
            wasVelocity = note.getVelocity();
            wasVisible = note.isVisible();
            wasMutted = !note.isAudible();
            wasLinked = note.isLinked();
            wasDotted = tr.isDotted();
            this.turningOn = false; // Subsequent dragging will setPressed rows off
            MyGridSquare nextSq = square.next();
            if (nextSq != null && nextSq.isSqVisible()) {
                nextSq.unlinkNote(this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(), SoundWithMidi.getCurrentKeyboardVelocity(),
                        false, false, false, this.mixer.getCurrentTrack().isDotted());
            }
        } else {
            MyTrack tr = this.getMixer().getCurrentTrack();
            if (tr == null) {
                this.setDefaultTrack();
                tr = this.getMixer().getCurrentTrack();
            }
            tr.oneNoteMore();
            square = this.allPurposeScore.addNoteToSquare(keyId,col,1,Settings.getnRowsSquare(),(MyComponent) allPurposeScore,this,allPurposeScore,this.getCam(),
                    this.getMixer().getCurrentChannelOfCurrentTrack(),
                    this.getMixer().getCurrentTrackId(),
                    this.getMixer().getCurrentTrack().getVelocity(), true, false, true,
                    this.getMixer().getCurrentTrack().isDotted());
            //square = this.getAllPurposeScore().getGridSquare(keyId, col);
            this.turningOn = true; // Subsequent dragging will setPressed rows on
        }
        square.updateState();
        if (this.allPurposeScore.isNotNullAndVisible(keyId, col)) {
            firstNote = this.allPurposeScore.getGrid()[keyId][col];
            lastNote = this.allPurposeScore.getGrid()[keyId][col];
//                MyTrack tr = this.mixer.getCurrentTrack();
//                firstNote.addNote(this.mixer.getCurrentChannelOfCurrentTrack(),this.mixer.getCurrentTrackId(), 
//                        this.mixer.getCurrentTrack().getVelocity(), true, false, true, this.mixer.getCurrentTrack().isDotted());
            MyGridSquare sq = this.allPurposeScore.getGrid()[keyId][col];
            mouseSequence.addChange(
                    sq,
                    true,
                    this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(),
                    this.mixer.getCurrentTrack().getVelocity(), sq.isSqVisible(), 
                    !sq.isSq_is_audible(),sq.isSq_is_linked(), this.mixer.getCurrentTrack().isDotted()
            );
        } else {
//                this.allPurposeScore.getGrid()[row][col].removeNote(this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId());
            mouseSequence.addChange(
                    square, // this.allPurposeScore.getGrid()[keyId][col] <- is null
                    false,
                    wasChannel, wasTrack,
                    wasVelocity, wasVisible, wasMutted, wasLinked, wasDotted
            );
        }
    }

    /**
     * On mouse pressed, checks if a XiloKey or a MyGridSquare or a MyButton has
     * been pressed and activates it.
     *
     * @param posX
     * @param posY
     */
    public void onMousePressed(double posX, double posY) {
        /* Exit lyrics edit mode on any click (commits pending text) */
        if (this.myLyrics.isEditMode()) {
            this.myLyrics.exitEditMode();
            this.needsSaving = true;
        }

        /* Col·locar canvi pendent: el clic al grid determina la columna */
        if (pendingChange != null) {
            int row = this.allPurposeScore.whichRow(posX, posY);
            int col = this.allPurposeScore.whichCol();
            if (row != -1 && col != -1) {
                placePendingChangeAt(col);
            }
            return; // el clic queda consumit pel canvi pendent
        }

        /* Check chord symbol */
        int chordCol = this.myChordSymbolLine.whichCol(posX, posY);
        // if (Settings.IS_BU) chordCol = -1;
        if (chordCol != -1) {
            Chord oldchord = this.allPurposeScore.getChordSymbol(chordCol);
            Chord chord = this.myChordSymbolLine.enterChord(oldchord);

            if (chord != null && chord != oldchord) { // chord != oldchord means not cancelled
                if (chord.isValidChord()) {
                    this.allPurposeScore.placeChordSymbol(chord, chordCol); // crea el chordTrack si no existeix
                    this.needsSaving = true;
                    this.myChordSymbolLine.drawFullChordLineInOffscreen();
                    //this.getMixer().refreshMixer();
                }
            } else if (chord == null && oldchord != null) { // empty input = delete
                this.allPurposeScore.removeChordSymbol(chordCol);
                this.needsSaving = true;
                this.myChordSymbolLine.drawFullChordLineInOffscreen();
                //this.getMixer().refreshMixer();
            }
//            this.myChordSymbolLine.thisAndAncestorsNeedDrawing();
            this.drawFull(true);
            return;
        }

        /* Check lyrics strip: enter inline edit mode */
        int lyricsCol = this.myLyrics.whichCol(posX, posY);
        if (lyricsCol != -1) {
            int trackId = this.getMixer().getCurrentTrackId();
            this.myLyrics.startEdit(lyricsCol, trackId, posX);
            this.drawFull(true);
            return;
        }

        /* Check XiloKey. */
        int keyId = this.keyboard.whichKey(posX, posY);
        if (keyId != -1) {
            if (this.getAllPurposeScore().getChoice().isSelecting()){
                this.getAllPurposeScore().getChoice().selectChoice(keyId);
                return;
            }
            this.keyboard.getKey(keyId).doNotHighlight(false);
            this.keyboard.play(keyId);
            this.lastXiloKeyPressed = keyId;
//            this.keyboard.thisAndAncestorsNeedDrawing();
            this.drawFull(true);
            return;
        }

        /* Check MyGridSquare. */
        int row = this.allPurposeScore.whichRow(posX, posY);
        if (row != -1) {
            mouseSequence = new MouseSequence(this);
            int col = this.allPurposeScore.whichCol();
            this.needsSaving = true;
            this.lastRowPressed = row;
            this.lastColPressed = col;
            MyGridSquare sq = this.allPurposeScore.getGridSquare(row, col);
            if (sq!=null && sq.isSqVisible() && !sq.isSq_is_linked()){
                sq.linkNote(this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(), SoundWithMidi.getCurrentKeyboardVelocity(),
                        false, false, false, this.mixer.getCurrentTrack().isDotted());
                this.linking = true;
                return;
            }
            this.toggle(row, col); // Aquí dins es fa l'add i el remove Note i firstNote
            this.keyboard.getKey(row).doNotHighlight(false);
            if (this.isTurningOn()) {
                this.keyboard.play(row);
            }
            if (col + 1 > this.getAllPurposeScore().getLastColWritten()) {
                this.getAllPurposeScore().setLastColWritten(col + 1);
            }
        }

        /* Check SlideKey. */
        keyId = this.keyboard.whichSlideKey(posX, posY);
        if (keyId != -1) {
            if (this.getAllPurposeScore().getChoice().isSelecting()){
                this.getAllPurposeScore().getChoice().selectChoice(keyId);
                return;
            }
            MyXiloKey key = this.keyboard.getKey(keyId);
            MySlide slide = key.getSlide();
            if (slide.isKeyNotSelected()) {
                this.allPurposeScore.getChoice().addKey(key);
                slide.setDimensionsSelected();
                key.setDimensionsSelected();
            } else {
                if (slide.isKeySelected()) {
                    this.allPurposeScore.getChoice().removeKey(key);
                    slide.setDimensionsNotSelected();
                    key.setDimensionsNotSelected();
                }
            }
            this.keyboard.play(keyId);
            this.lastXiloKeyPressed = keyId;
            this.allPurposeScore.updateStripsNKeyboard();
            this.allPurposeScore.setGridColorsHaveChanged(true);
//            this.keyboard.thisAndAncestorsNeedDrawing();
//            this.allPurposeScore.thisAndAncestorsNeedDrawing();
        }

        /* Check MyButton. */
        int button = this.buttons.whichButton(posX, posY);
        if (button != -1) {
            this.buttons.onButtonPressed(button);
            this.lastButtonPressed = button;
            this.buttons.setModified(true);
//            this.buttons.thisAndAncestorsNeedDrawing();
        }
        this.drawFull(true);
    }

    /**
     * On mouse released, checks if a XiloKey or MyGridSquare has been released
     * and stops it.
     *
     * @param posX
     * @param posY
     */
    public void onMouseReleased(double posX, double posY) {
        /* XiloKey. */
        if (this.lastXiloKeyPressed != -1) {
            this.keyboard.stop(this.lastXiloKeyPressed);
            this.keyboard.getKey(this.lastXiloKeyPressed).doNotHighlight(this.allPurposeScore.isUseScreenKeyboardRight());
            this.lastXiloKeyPressed = -1;
//            this.keyboard.thisAndAncestorsNeedDrawing();
            return;
        }
        /* MyGridSquare. */
        if (this.lastRowPressed != -1) {
            if (this.linking){
                this.linking = false;
                return;
            }
            MyGridSquare wasFirstNote = firstNote;
            if (firstNote != null) {
                if (firstNote.getScoreCol() <= lastNote.getScoreCol()) {
                    firstNote.unlinkNote(this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(), SoundWithMidi.getCurrentKeyboardVelocity(),
                            false, false, false, this.mixer.getCurrentTrack().isDotted());
                } else {
                    lastNote.unlinkNote(this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(), SoundWithMidi.getCurrentKeyboardVelocity(),
                            false, false, false, this.mixer.getCurrentTrack().isDotted());
                }
                firstNote = null;
            }
            if (mouseSequence != null) {
                this.afegirEvent(mouseSequence);
                mouseSequence = null;
            }
            this.keyboard.stop(lastRowPressed);
            this.keyboard.getKey(this.lastRowPressed).doNotHighlight(this.allPurposeScore.isUseScreenKeyboardRight());
            this.lastRowPressed = -1;
            this.lastColPressed = -1;

            int curRow = this.allPurposeScore.whichRow(posX, posY);
            int curCol = this.allPurposeScore.whichCol();
            if (Settings.isAutoCorrect()) {
                if (this.turningOn && wasFirstNote != null && curCol != -1) {
                    int fcol = wasFirstNote.getScoreCol();
                    int frow = wasFirstNote.getScoreRow();
                    this.undo();
                    mouseSequence = new MouseSequence(this);
                    int step = 1;
                    if (curCol < fcol) {
                        step = -1;
                    }
                    boolean isFirst = true;
                    for (int col = fcol; col - step != curCol; col += step) {
                        MyTrack tr = this.mixer.getCurrentTrack();
                        tr.oneNoteMore();
                        MyGridSquare sq = this.allPurposeScore.addNoteToSquare(frow,col,1,Settings.getnRowsSquare(),(MyComponent) allPurposeScore,this,allPurposeScore,this.getCam(),
                                this.mixer.getCurrentChannelOfCurrentTrack(),
                                this.mixer.getCurrentTrackId(), this.mixer.getCurrentTrack().getVelocity(),
                                true, false, true, this.mixer.getCurrentTrack().isDotted());
                        lastNote = this.allPurposeScore.getGrid()[frow][col];
                        if (isFirst){
                            firstNote = lastNote;
                            isFirst = false;
                        }
                        mouseSequence.addChange(
                                sq,
                                true,
                                this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(),
                                this.mixer.getCurrentTrack().getVelocity(), sq.isSqVisible(),!sq.isSqAudible(),sq.isSq_is_linked(), this.mixer.getCurrentTrack().isDotted()
                        );

                        if (col + 1 > this.getAllPurposeScore().getLastColWritten()) {
                            this.getAllPurposeScore().setLastColWritten(col + 1);
                        }

                    }
                    MyGridSquare wasLastNote = lastNote;
                    if (firstNote.getScoreCol() <= lastNote.getScoreCol()) {
                        firstNote.unlinkNote(this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(), SoundWithMidi.getCurrentKeyboardVelocity(),
                                false, false, false, this.mixer.getCurrentTrack().isDotted());
                    } else {
                        lastNote.unlinkNote(this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(), SoundWithMidi.getCurrentKeyboardVelocity(),
                                false, false, false, this.mixer.getCurrentTrack().isDotted());
                    }
                    this.afegirEvent(mouseSequence);
                    mouseSequence = null;
                }
            }
            return;
        }
        /* MyButton. */
        if (this.lastButtonPressed != -1) {
            this.buttons.onButtonRelesased(this.lastButtonPressed);
            this.lastButtonPressed = -1;
            this.buttons.setModified(true);
        }
        this.drawFull(true);
    }

//    /**
//     * On mouse dragged, ...
//     *
//     * @param posX
//     * @param posY
//     */
//    public void onMouseDragged_old(double posX, double posY) {
//        /* Check MyGridSquare. */
//        int row = this.allPurposeScore.whichRow(posX, posY);
//        int col = this.allPurposeScore.whichCol();
//        boolean linked = true;
//        if (this.lastRowPressed != -1) {
//            if (this.lastRowPressed != row) {
//                this.keyboard.stop(this.lastRowPressed);
//                this.keyboard.getKey(this.lastRowPressed).doNotHighlight(this.allPurposeScore.isUseScreenKeyboardRight());
//                linked = false;
//            }
//        }
//        if (row != -1) {
//            this.lastRowPressed = row;
//            if (this.turningOn) {
//                this.allPurposeScore.getGrid()[row][col].addNote(this.mixer.getCurrentChannelOfCurrentTrack(), SoundWithMidi.getCurrentTrackId(), SoundWithMidi.getCurrentKeyboardVelocity(), false, linked);
//                this.allPurposeScore.setOn(row, col);
//                this.keyboard.getKey(row).doNotHighlight(false);
//                this.keyboard.play(row);
//            } else {
//                this.allPurposeScore.getGrid()[row][col].removeNote(this.mixer.getCurrentChannelOfCurrentTrack(), SoundWithMidi.getCurrentTrackId());
//                this.allPurposeScore.setOff(row, col);
//            }
//        }
//    }
//
    public void onMouseDragged(double posX, double posY) {
        int wasChannel = -1;
        int wasTrack = -1;
        int wasVelocity = 0;
        boolean wasVisible = false;
        boolean wasMutted = false;
        boolean wasLinked = false;
        boolean wasDotted = false;

        /* Check MyGridSquare. */
        int row = this.allPurposeScore.whichRow(posX, posY);
        int col = this.allPurposeScore.whichCol();

        if (this.linking) return;
        
//        System.out.println("MyController::onMouseDraged() row, col = "+row+", "+col);

        if (row == -1 || col == -1) {
            return; // Ignore out-of-bounds values
        }
        
        if (row == lastRowPressed && col == lastColPressed){
            return; // Ignore repeated squares
        }

        // Interpolate if we have a last pressed row and column to fill in gaps
        if (this.lastRowPressed != -1) {
            int lastRow = this.lastRowPressed;
            int lastCol = this.lastColPressed;

            if (lastRow != row || lastCol != col) {
                // Interpolate cells between the last and current position
                interpolateCells(lastRow, lastCol, row, col);
                // linked = false;
            }
        }

        this.lastRowPressed = row;
        this.lastColPressed = col;

        // Perform the action based on `turningOn` flag
        if (this.turningOn) {
            MyTrack tr = this.mixer.getCurrentTrack();
            tr.oneNoteMore();
            MyGridSquare sq = this.allPurposeScore.addNoteToSquare(row,col,1,Settings.getnRowsSquare(),(MyComponent) allPurposeScore,this,allPurposeScore,this.getCam(),
                    this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(),
                    this.mixer.getCurrentTrack().getVelocity(),
                    true, false, true, this.mixer.getCurrentTrack().isDotted());
            lastNote = this.allPurposeScore.getGrid()[row][col];
            mouseSequence.addChange(
                    sq,
                    true, // added
                    this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(),
                    this.mixer.getCurrentTrack().getVelocity(),
                    sq.isSqVisible(), !sq.isSqAudible(), sq.isSq_is_linked(), this.mixer.getCurrentTrack().isDotted()
            );
            this.keyboard.getKey(row).doNotHighlight(false);
            this.keyboard.play(row);
            if (col + 1 > this.getAllPurposeScore().getLastColWritten()) {
                this.getAllPurposeScore().setLastColWritten(col + 1);
            }
        } else {
            MyGridSquare square = this.allPurposeScore.getGrid()[row][col];
            if (square!=null && square.isSqVisible()) {
                MyGridSquare nextSq = square.next();
                MyGridSquare.SubSquare note = this.getAllPurposeScore().removeNoteFromSquare(row,col,this.getMixer().getCurrentChannelOfCurrentTrack(),
                    this.getMixer().getCurrentTrackId());
                wasChannel = this.getMixer().getCurrentChannelOfCurrentTrack();
                wasTrack = this.getMixer().getCurrentTrackId();
                wasVelocity = note.getVelocity();
                wasVisible = note.isVisible();
                wasMutted = !note.isAudible();
                wasLinked = note.isLinked();
                wasDotted = this.getMixer().getCurrentTrack().isDotted();
                mouseSequence.addChange(
                        square,
                        false,
                        wasChannel, wasTrack,
                        wasVelocity, wasVisible, wasMutted, wasLinked, wasDotted
                );
                if (nextSq != null && nextSq.isSqVisible()) {
                    nextSq.unlinkNote(this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(), SoundWithMidi.getCurrentKeyboardVelocity(),
                            false, false, false, this.mixer.getCurrentTrack().isDotted());
                }
            }
        }
        this.drawFull(true);
//        this.allPurposeScore.thisAndAncestorsNeedDrawing();
//        this.keyboard.thisAndAncestorsNeedDrawing();
    }

    public void onDoubleClick(double posX, double posY){
//        int row = this.allPurposeScore.whichRow(posX, posY);
//        int col = this.allPurposeScore.whichCol();
//        if (row == -1 || col == -1) {
//            return; // Ignore out-of-bounds values
//        }
//        MyGridSquare square = this.allPurposeScore.getGrid()[row][col];
//        if (square!=null && square.isSqVisible()) {
//            if (square.isSq_is_linked()){
//                square.unlinkNote(this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(), SoundWithMidi.getCurrentKeyboardVelocity(),
//                            false, false, false, this.mixer.getCurrentTrack().isDotted());
//            }
//            else{
//                square.linkNote(this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(), SoundWithMidi.getCurrentKeyboardVelocity(),
//                            false, false, false, this.mixer.getCurrentTrack().isDotted());
//            }
//        }
    }
    
    /**
     * On mouse pressed, checks if a XiloKey or a MyGridSquare or a MyButton has
     * been pressed and activates it.
     *
     * @param posX
     * @param posY
     */
    public void onRightMousePressed(double posX, double posY) {
    }

    /**
     * On mouse released, checks if a XiloKey or MyGridSquare has been released
     * and stops it.
     *
     * @param posX
     * @param posY
     */
    public void onRightMouseReleased(double posX, double posY) {
    }

    public void onRightMouseDragged(double posX, double posY) {
    }
    
    private int lastTipButton = -1;

    public void onMouseMoved(double posX, double posY) {
        int button = this.buttons.whichButton(posX, posY);
        if (button != -1) {
            if (button != lastTipButton) {
                this.buttons.hideTip();
                this.buttons.showTip(button, posX, posY);
                lastTipButton = button;
            }
        } else {
            if (lastTipButton != -1) {
                this.buttons.hideTip();
                lastTipButton = -1;
            }
        }
        this.drawFull(true);
    }
    
    private void interpolateCells(int startRow, int startCol, int endRow, int endCol) {
        int wasChannel = -1;
        int wasTrack = -1;
        int wasVelocity = 0;
        boolean wasVisible = false;
        boolean wasMutted = false;
        boolean wasLinked = false;
        boolean wasDotted = false;

        int stepCount = Math.max(Math.abs(endRow - startRow), Math.abs(endCol - startCol));
        int intermediateRow = startRow;
        int intermediateCol = startCol;
        int prevRow;
        int prevCol;
        for (int i = 1; i <= stepCount; i++) {
//            this.allPurposeScore.thisAndAncestorsNeedDrawing();
//            this.keyboard.thisAndAncestorsNeedDrawing();
            prevRow = intermediateRow;
            prevCol = intermediateCol;
            intermediateRow = startRow + i * (endRow - startRow) / stepCount;
            intermediateCol = startCol + i * (endCol - startCol) / stepCount;

            if (intermediateRow != prevRow) {
                this.keyboard.stop(prevRow);
            }

            // Perform the same action as onMouseDragged for interpolated cells
            if (this.turningOn) {
                MyTrack tr = this.mixer.getCurrentTrack();
                tr.oneNoteMore();
                MyGridSquare sq = this.allPurposeScore.addNoteToSquare(intermediateRow,intermediateCol,1,Settings.getnRowsSquare(),(MyComponent) allPurposeScore,this,allPurposeScore,this.getCam(),
                        this.mixer.getCurrentChannelOfCurrentTrack(),
                        this.mixer.getCurrentTrackId(), this.mixer.getCurrentTrack().getVelocity(),
                        true, false, true, this.mixer.getCurrentTrack().isDotted());
                lastNote = this.allPurposeScore.getGrid()[intermediateRow][intermediateCol];
                mouseSequence.addChange(
                        sq,
                        true,
                        this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(),
                        this.mixer.getCurrentTrack().getVelocity(), sq.isSqVisible(), !sq.isSqAudible(), sq.isSq_is_linked(), this.mixer.getCurrentTrack().isDotted()
                );

                // this.allPurposeScore.setOn(intermediateRow, intermediateCol);
                if (intermediateCol + 1 > this.getAllPurposeScore().getLastColWritten()) {
                    this.getAllPurposeScore().setLastColWritten(intermediateCol + 1);
                }
            } else {
                MyGridSquare square = this.allPurposeScore.getGrid()[intermediateRow][intermediateCol];
                if (square!=null && square.isSqVisible()) {
                    MyGridSquare nextSq = square.next();
                    MyGridSquare.SubSquare note = this.getAllPurposeScore().removeNoteFromSquare(intermediateRow,intermediateCol,this.getMixer().getCurrentChannelOfCurrentTrack(),
                        this.getMixer().getCurrentTrackId());
                    wasChannel = this.getMixer().getCurrentChannelOfCurrentTrack();
                    wasTrack = this.getMixer().getCurrentTrackId();
                    wasVelocity = note.getVelocity();
                    wasVisible = note.isVisible();
                    wasMutted = !note.isAudible();
                    wasLinked = note.isLinked();
                    wasDotted = this.getMixer().getCurrentTrack().isDotted();
                    mouseSequence.addChange(
                            square,
                            false,
                            wasChannel, wasTrack,
                            wasVelocity, wasVisible, wasMutted, wasLinked, wasDotted
                    );
                    if (nextSq != null && nextSq.isSqVisible()) {
                        nextSq.unlinkNote(this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(), SoundWithMidi.getCurrentKeyboardVelocity(),
                                false, false, false, this.mixer.getCurrentTrack().isDotted());
                    }
                }
            }
        }
        this.drawFull(true);
    }

    public void onDemoButtonPressed() {
        System.out.println("Demo button pressed!");
    }

    public void onDemoButton2Pressed() {
        System.out.println("Demo button 2 pressed!");
    }

    public void play() {
        if (this.cam.isPlaying()) {
            return; // Evita múltiples fils
        }
//        this.cam.play();
//        if (playScoreColAtPlayBar()) {
//            cam.updateCurrentCol();
//            this.updateTextOfButtons();
//        }

        //this.getCam().setPlaying(true);
        cam.setPlaying(true); // opcional: pot activar dibuix
        //cam.getTimer().reset();
        Thread playbackThread = new Thread(() -> {
            long nextStep = System.nanoTime();
            // long nanosPerStep = (long) MyTempo.getNanosPerSquareGrid(); // o calcula-ho
            Utilities.printOutWithPriority(true,"MyController::play():");

            while (this.getCam().isPlaying()) {
                int camPBar = cam.getPlayBar();
//                System.out.println(allPurposeScore.getDelay(!allPurposeScore.isUseScreenKeyboardRight()));
                int col = allPurposeScore.getScoreCol(camPBar)+allPurposeScore.getDelay(!allPurposeScore.isUseScreenKeyboardRight());

                boolean played = allPurposeScore.playScoreCol(col);
                if (!played) {
                    stop(); // també posa isPlaying = false
                    buttons.stopPlayButton();
                    this.keyboard.stopAll();
                    break;
                }

                cam.updateCurrentCol();
                //this.allPurposeScore.setDrawNewCol(true);// potser cal sincronització amb el fil de dibuix
                updateTextOfButtons();
                this.getUi().getPanel().repinta(true);
                // this.redraw(this.getUi().getPanel().getPantalla());
                
                nextStep += (long) MyTempo.getNanosPerSquareGrid();
                long sleepTime = nextStep - System.nanoTime();
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000));
                    } catch (InterruptedException e) {
                        break;
                    }
                }
//                long nanosPerSquare = (long) Math.floor(MyTempo.getNanosPerSquareGrid());
//                long sleepTime = nanosPerSquare - this.cam.getTimer().elapsedNanos();
//                if (sleepTime > 0) {
//                    Utilities.printOutWithPriority(10,"MyController::play(): sleeping " + sleepTime);
//                    try {
//                        Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000));
//                    } catch (InterruptedException e) {
//                        break;
//                    }
//                } else {
//                    Utilities.printOutWithPriority(1,"MyController::play(): NOT sleeping ");
//                }
//                long residualTime = nanosPerSquare - this.cam.getTimer().elapsedNanos();
//                Utilities.printOutWithPriority(1,"MyController::play(): residual time = " + residualTime);
//                cam.getTimer().reset();
//            
//        
////                nextStep += (long) MyTempo.getNanosPerSquareGrid(); 
////                long sleepTime = nextStep - System.nanoTime();
//                
            }
        });
        playbackThread.start();
        System.out.println("MyController::play, playBarCol = "+cam.getPlayBar()+" nColsCam = "+Settings.getnColsCam()+
                " currentCol = "+this.allPurposeScore.getCurrentCol()+        
                " delay = "+this.allPurposeScore.getDelay(!this.allPurposeScore.isUseScreenKeyboardRight()));        
    }

//    public void old_play_desglossat() {
//        this.cam.play();
////        if (playScoreColAtPlayBar()) {
//
//        int camPBar = cam.getPlayBar();
//        int col = allPurposeScore.getScoreCol(camPBar);
//        boolean played = allPurposeScore.playScoreCol(col);
////        return played;
//
//        if (played) {
//            cam.updateCurrentCol();
//        } else {
//            this.stop();
//            this.buttons.stopPlayButton();
//        }
//        this.updateTextOfButtons();
//    }
//    public void old_play() {
////        System.out.println("MyController::play: tempo = "+MyTempo.getTempo()+", m/Sq = "+MyTempo.getNanosPerSquareGrid()+" = "+(60000/(MyTempo.getTempo()*Settings.getnColsBeat())));
////        System.out.println("MyController::play: tempo = "+MyTempo.getTempo()+", m/Sq = "+MyTempo.getNanosPerSquareGrid()+", nCB = "+Settings.getnColsBeat());
////        MyTempo.checkTempo();
//        this.cam.play();
//        if (playScoreColAtPlayBar()) {
//            cam.updateCurrentCol();
//        } else {
//            this.stop();
//            this.buttons.stopPlayButton();
//        }
//        this.updateTextOfButtons();
//    }
//
    public void pause() {
//        BackgroundChordPlayer bkgr = this.allPurposeScore.getBackgroundChordPlayer();
//        if (bkgr.isPlaying()) {
//            bkgr.pause();
//        }

        this.cam.stop();
        this.keyboard.stopAll();
        this.allPurposeScore.stopAll();
    }

    public void stop() {
        this.getCam().setPlaying(false);
//        cam.stop();
//        buttons.stopPlayButton();
//        BackgroundChordPlayer bkgr = this.allPurposeScore.getBackgroundChordPlayer();
//        if (bkgr.isPlaying()) {
//            bkgr.stop();
//        }
        this.cam.stop();
        this.keyboard.stopAll();
        this.allPurposeScore.stopAll();
    }

    public void transpose(int step) {
        this.allPurposeScore.transpose(step);
    }

    public void setDrums(boolean drumsOn) {
        System.out.println("Controller::setDrums: " + drumsOn);
    }

    public void setStrips(boolean penta) {
        this.allPurposeScore.setUsePentagramaStrips(penta);
        this.allPurposeScore.updateStripsNKeyboard(penta);
    }

    public void onDrumsButtonPressed(MyButton togg) {
        if (togg.isPressed()) {
            this.setDrums(true);
        } else {
            this.setDrums(false);
        }
    }

    public void onPentaVsChoiceButtonPressed(MyButton togg) {
        if (togg.isPressed()) {
            this.setStrips(false); // False = setPressed strips on selected notes
//            this.keyboard.setShowChoice(false);
        } else {
            this.setStrips(true); // True = setPressed pentagram strips
//            this.keyboard.setShowChoice(true);
        }
        this.allPurposeScore.setGridColorsHaveChanged(true);

    }

    public void onLeftVsRightButtonPressed(MyButton togg) {
        if (togg.isPressed()) {
            this.setScreenKeyboardRight(true);
        } else {
            this.setScreenKeyboardRight(false);
        }
        boolean show = this.keyboard.isShowChoice();
//        this.screen.remove(keyboard);
//        this.keyboard = new MyXiloKeyboard(Settings.getFirstColKeyboard(), Settings.getFirstRowKeyboard(), Settings.getnColsKeyboard(), 
//                Settings.getnRowsKeyboard(), this.allPurposeScore.isUseScreenKeyboardRight(), this.screen, this, show);
//        this.screen.add(keyboard);
        this.keyboard.setShowChoice(show);
        this.buttons.stopPlayButton();
    }

    public void onNamesVsHideButtonPressed(MyButton togg) {
        // System.out.println("onNamesVsHideButtonPressed: "+togg.isPressed());
        if (togg.isPressed()) {
            this.getAllPurposeScore().setShowNoteNames(false);
        } else {
            this.getAllPurposeScore().setShowNoteNames(true);
        }
    }

    public void onAbsoluteVsMobileDoButtonPressed(MyButton togg) {
//        System.out.println("onAbsoluteVsMobileDoButtonPressed: " + togg.isPressed());
        if (togg.isPressed()) {
            ToneRange.setMovileDo(true);
        } else {
            ToneRange.setMovileDo(false);
        }
    }

    public void onHelpButtonPressed(MyButton togg){
        MyDialogs.mostraMissatge(I18n.t("MyController.help.text"), I18n.t("MyController.btn.help.title"));
        if (togg!=null) togg.setPressed(false);
    }
    
    public void onChordSymbolsButtonPressed(MyButton togg) {
        System.out.println("MyController::onChordSymbolsButtonPressed()");
        SVGandPDF svg = new SVGandPDF(this);
        try {
            String fitxer = MyDialogs.seleccionaFitxerEscriptura(null, "export.svg", "svg");
            if (fitxer == null || "".equals(fitxer)) {
                return;
            }
            File file = new File(fitxer);
            if (file.exists()) {
                int resposta = JOptionPane.showConfirmDialog(
                        null,
                        I18n.t("file.overwrite"),
                        I18n.t("MyController.dialog.confirm.title"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (resposta == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            svg.printSvg(file, (int) this.screen.getWidth(), (int) screen.getHeight());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        if (togg!=null) togg.setPressed(false);
    }
    
    public void onPlayButtonPressed(MyButton togg) {
//        System.out.println("MyController::onPlayButtonPressed()");
        if (togg.isPressed()) {
            if (this.allPurposeScore.isGridColorsHaveChanged()){
                this.allPurposeScore.initOffscreen();
                this.myChordSymbolLine.initOffscreen();
                this.myLyrics.initOffscreen();
            }
            this.play();
        } else {
            this.pause();
//            this.cam.updateCurrentCol();
        }
    }

    public int getNextKey() {
        int oldMidi = this.allPurposeScore.getMidiKey();
        String name = ToneRange.getKeyName(oldMidi, this.allPurposeScore.getScaleMode());
        name = MyKeyCircles.next(name);
        int midi = ToneRange.getMidi(name);
        if (MyKeyCircles.isMajor(name)){
            this.allPurposeScore.setScaleMode('M');
        }
        else {
            this.allPurposeScore.setScaleMode('m');
        }
        return midi;
    }

    public int getPrevKey() {
        int oldMidi = this.allPurposeScore.getMidiKey();
        String name = ToneRange.getKeyName(oldMidi, this.allPurposeScore.getScaleMode());
        name = MyKeyCircles.prev(name);
        int midi = ToneRange.getMidi(name);
        if (MyKeyCircles.isMajor(name)){
            this.allPurposeScore.setScaleMode('M');
        }
        else {
            this.allPurposeScore.setScaleMode('m');
        }
        return midi;
    }

    public void onNextKeyButtonPressed(MyButton togg) {
        int old_key = this.allPurposeScore.getMidiKey();
        int new_key = this.getNextKey();
        this.transpose(new_key - old_key);
        if (this.exercisesOn) {
            this.resetExercise();
        }
//        System.out.println("MyController::onNextKeyButtonPressed: (id) " + togg.getId() + " " + this.allPurposeScore.getMidiKey());
    }

    public void onPrevKeyButtonPressed(MyButton togg) {
//        System.out.println("MyController::onPrevKeyButtonPressed: (id) " + togg.getId());
        int old_key = this.allPurposeScore.getMidiKey();
        int new_key = this.getPrevKey();
        this.transpose(new_key - old_key);
        if (this.exercisesOn) {
            this.resetExercise();
        }
    }

    public void onResetKeyButtonPressed(MyButton togg) {
        int old_key = this.allPurposeScore.getMidiKey();
        if (Settings.IS_BU){
            if (cam.isPlaying()) { togg.setPressed(false); return; }
            String to = MyDialogs.mostraInputDialog(I18n.t("keyButton.prompt"),I18n.t("keyButton.title"));
            char mode = ToneRange.getScaleMode(to);
            if (mode ==' ') return;
            int midiKey = ToneRange.getMidiKey(to);
            this.allPurposeScore.setMidiKey(midiKey);
            this.allPurposeScore.setScaleMode(mode);
            MyGridScore.ScoreChange sc = new MyGridScore.ScoreChange();
            sc.midiKey   = allPurposeScore.getMidiKey();
            sc.scaleMode = allPurposeScore.getScaleMode();
            setPendingChange(sc, I18n.t("scoreChange.key"));
            togg.setPressed(false);
        } else {
            int new_key = this.getRandKey();
            this.transpose(new_key - old_key);
            if (this.exercisesOn) {
                this.resetExercise();
            }
        }
    }

    public void onNextExerciseButtonPressed(MyButton togg) {
        this.nextExercise();
    }

    public void onPrevExerciseButtonPressed(MyButton togg) {
        this.prevExercise();
    }

    public void onRestartExerciseButtonPressed(MyButton togg) {
        if (!"".equals(togg.getText())) {
            this.resetExercise();
        }
    }

    public void onPageNumButtonPressed(MyButton togg) {
    }

    public void onFirstPageButtonPressed(MyButton togg) {
        this.stop();
        this.cam.reset();
        this.buttons.stopPlayButton();
//        this.drawFull(true);
    }

    public void drawFull(boolean full){
        this.setNeedsDrawing(true);
        //this.allPurposeScore.setDrawNewCol(!full);
        //this.allPurposeScore.setRedrawFullGrid(full);
        //if (this.getUi().getPanel()!=null) this.getUi().getPanel().repinta(true);
    }
    
    public void onNextPageButtonPressed(MyButton togg) {
        this.stop();
        this.cam.nextPage();
        this.buttons.stopPlayButton();
//        this.drawFull(true);
    }

    public void onPrevPageButtonPressed(MyButton togg) {
        this.stop();
        this.cam.prevPage();
        this.buttons.stopPlayButton();
  //      this.drawFull(true);
    }

    public void onPrevColButtonPressed(MyButton togg) {
        System.out.println("MyController:onPrevCol() currentCol = "+this.allPurposeScore.getCurrentCol());
        if (allPurposeScore.getCurrentCol() < allPurposeScore.getNumCols()) {
            allPurposeScore.setCurrentCol(allPurposeScore.getCurrentCol() + 1);
        }
        int ccol = allPurposeScore.getCurrentCol();
        if (replicador != null && replicador.isAlive()) {
            return;
        }

        // Crear un fil per avançar passos mentre el botó està premut
        replicador = new Thread(() -> {
            if (togg.isPressed()) {
                try {
                    // Pausa entre cada pas per controlar la velocitat
                    Thread.sleep(Settings.BUTTON_REPEAT_INITIAL_DELAY_MS); // Ajusta el temps segons sigui necessari
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            while (togg.isPressed()) {
                try {
                    System.out.println("MyController:onPrevCol() currentCol = "+this.allPurposeScore.getCurrentCol());
                    if (allPurposeScore.getCurrentCol() < allPurposeScore.getNumCols()) {
                        allPurposeScore.setCurrentCol(allPurposeScore.getCurrentCol() + 1);
                        allPurposeScore.drawCurrentCamInOffscreen();
                        this.getUi().getPanel().repinta(true);
                    }
                    // Pausa entre cada pas per controlar la velocitat
                    Thread.sleep(25); // Ajusta el temps segons sigui necessari
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        replicador.start();
    }

    public void onNextColButtonPressed(MyButton togg) {
        System.out.println("MyController:onNextCol() currentCol = "+this.allPurposeScore.getCurrentCol());
        boolean left = !this.getAllPurposeScore().isUseScreenKeyboardRight();
        if (allPurposeScore.getCurrentCol() > (Settings.getInitialCurrentCol(left,allPurposeScore))) {
            allPurposeScore.setCurrentCol(allPurposeScore.getCurrentCol() - 1);
        }
        if (replicador != null && replicador.isAlive()) {
            return;
        }

        // Crear un fil per avançar passos mentre el botó està premut
        replicador = new Thread(() -> {
            if (togg.isPressed()) {
                try {
                    // Pausa entre cada pas per controlar la velocitat
                    Thread.sleep(Settings.BUTTON_REPEAT_INITIAL_DELAY_MS); // Ajusta el temps segons sigui necessari
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            while (togg.isPressed()) {
                try {
                    if (allPurposeScore.getCurrentCol() >(Settings.getInitialCurrentCol(left,allPurposeScore))) {
                        allPurposeScore.setCurrentCol(allPurposeScore.getCurrentCol() - 1);
                        allPurposeScore.drawCurrentCamInOffscreen();
                        this.getUi().getPanel().repinta(true);
                    }
                    // Pausa entre cada pas per controlar la velocitat
                    Thread.sleep(25); // Ajusta el temps segons sigui necessari
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        replicador.start();
    }

    public void onTempoButtonPressed(MyButton togg) {
        this.buttons.hideTip();
        togg.setPressed(false);
        String input = MyDialogs.mostraInputDialog(
                I18n.t("MyController.onTempoButtonPressed.prompt"),
                I18n.t("MyController.onTempoButtonPressed.title"),
                String.valueOf(MyTempo.getTempo())
        );
        if (input == null) {
            return;
        }
        try {
            int requested = Integer.parseInt(input);
            int actual = MyTempo.setTempo(requested);
            this.buttons.updateTempoButton(String.valueOf(actual));
        } catch (NumberFormatException e) {
            MyDialogs.mostraError(
                    I18n.f("MyController.onTempoButtonPressed.invalidInput", input),
                    I18n.t("MyController.dialog.error.title")
            );
        }
    }
    
    public void onFasterButtonPressed(MyButton togg) {
        if (cam.isPlaying()) return; // no es permet canviar durant la reproducció
        MyTempo.faster();
        this.buttons.updateTempoButton("" + MyTempo.getTempo());
        if (replicador != null && replicador.isAlive()) {
            return;
        }
        replicador = new Thread(() -> {
            if (togg.isPressed()) {
                try {
                    Thread.sleep(Settings.BUTTON_REPEAT_INITIAL_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            while (togg.isPressed()) {
                try {
                    MyTempo.faster();
                    this.buttons.updateTempoButton("" + MyTempo.getTempo());
                    Thread.sleep(Settings.BUTTON_REPEAT_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            // Quan es deixa anar el botó, registra el canvi com a pendent
            MyGridScore.ScoreChange sc = new MyGridScore.ScoreChange();
            sc.tempo = MyTempo.getTempo();
            setPendingChange(sc, I18n.t("scoreChange.tempo"));
        });
        replicador.start();
        // Si no hi ha repetició (un sol clic), registra igualment com a pendent
        // però ho fa quan es deixa anar (el thread acaba ràpid si !togg.isPressed())
    }

    public void onLouderButtonPressed(MyButton togg) {
        if (cam.isPlaying()) return;
        int vol = this.getMixer().louder();
        this.buttons.updateVolumeButton("" + vol);
        this.mixer.refreshMixer();
        if (replicador != null && replicador.isAlive()) {
            return;
        }
        int trackId = this.getMixer().getCurrentTrackId();
        replicador = new Thread(() -> {
            if (togg.isPressed()) {
                try {
                    Thread.sleep(Settings.BUTTON_REPEAT_INITIAL_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            while (togg.isPressed()) {
                try {
                    int v = this.getMixer().louder();
                    this.buttons.updateVolumeButton("" + v);
                    this.mixer.refreshMixer();
                    Thread.sleep(Settings.BUTTON_REPEAT_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            MyTrack tr = getMixer().getTrackFromId(trackId);
            if (tr != null) {
                MyGridScore.ScoreChange sc = new MyGridScore.ScoreChange();
                sc.trackVelocities.put(trackId, tr.getVelocity());
                setPendingChange(sc, I18n.t("scoreChange.volume"));
            }
        });
        replicador.start();
    }

    public void onQuieterButtonPressed(MyButton togg) {
        if (cam.isPlaying()) return;
        int vol = this.getMixer().quieter();
        this.buttons.updateVolumeButton("" + vol);
        this.mixer.refreshMixer();
        if (replicador != null && replicador.isAlive()) {
            return;
        }
        int trackId = this.getMixer().getCurrentTrackId();
        replicador = new Thread(() -> {
            if (togg.isPressed()) {
                try {
                    Thread.sleep(Settings.BUTTON_REPEAT_INITIAL_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            while (togg.isPressed()) {
                try {
                    int v = this.getMixer().quieter();
                    this.buttons.updateVolumeButton("" + v);
                    this.mixer.refreshMixer();
                    Thread.sleep(Settings.BUTTON_REPEAT_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            MyTrack tr = getMixer().getTrackFromId(trackId);
            if (tr != null) {
                MyGridScore.ScoreChange sc = new MyGridScore.ScoreChange();
                sc.trackVelocities.put(trackId, tr.getVelocity());
                setPendingChange(sc, I18n.t("scoreChange.volume"));
            }
        });
        replicador.start();
    }

    public void onSlowerButtonPressed(MyButton togg) {
        if (cam.isPlaying()) return;
        MyTempo.slower();
        this.buttons.updateTempoButton("" + MyTempo.getTempo());
        if (replicador != null && replicador.isAlive()) {
            return;
        }
        replicador = new Thread(() -> {
            if (togg.isPressed()) {
                try {
                    Thread.sleep(Settings.BUTTON_REPEAT_INITIAL_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            while (togg.isPressed()) {
                try {
                    MyTempo.slower();
                    this.buttons.updateTempoButton("" + MyTempo.getTempo());
                    Thread.sleep(Settings.BUTTON_REPEAT_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            MyGridScore.ScoreChange sc = new MyGridScore.ScoreChange();
            sc.tempo = MyTempo.getTempo();
            setPendingChange(sc, I18n.t("scoreChange.tempo"));
        });
        replicador.start();
    }

    public void onTitleButtonPressed(MyButton togg) {
        String title = this.allPurposeScore.getTitle();
        title = MyDialogs.mostraInputDialog(I18n.t("MyController.score.title.prompt"), I18n.t("MyController.score.title.label"), title);
        if (title != null) {
            this.allPurposeScore.setTitle(title);
        }
        togg.setPressed(false);
        this.statusLine.setText(allPurposeScore.getTitle() + ": " + allPurposeScore.getDescription());
    }

    public void onAuthorButtonPressed(MyButton togg) {
        String author = this.allPurposeScore.getAuthor();
        author = MyDialogs.mostraInputDialog(I18n.t("MyController.score.author.prompt"), I18n.t("MyController.score.author.label"), author);
        if (author != null) {
            this.allPurposeScore.setAuthor(author);
        }
        togg.setPressed(false);
        this.statusLine.setText(allPurposeScore.getTitle() + ": " + allPurposeScore.getAuthor());
    }

    public void onDescriptionButtonPressed(MyButton togg) {
        String descr = this.allPurposeScore.getDescription();
        descr = MyDialogs.mostraInputDialog(I18n.t("MyController.score.description.prompt"), I18n.t("MyController.score.description.label"), descr);
        if (descr != null) {
            this.allPurposeScore.setDescription(descr);
        }
        togg.setPressed(false);
        this.statusLine.setText(allPurposeScore.getTitle() + ": " + allPurposeScore.getDescription());
    }

    public static final String[] SymbolOptions =
    new String[] { "Interval", "Docecanotes", "Symbol", "Position", "Notes" };

    public static int which = 0;
    
    public void onChordSymbolsButtonPressed_future(MyButton togg) { // Should change MyButton for CircularButton.
        togg.setText(SymbolOptions[which]);
        which = (which + 1) % SymbolOptions.length; 
    }

    public void onTransposeDownButtonPressed(MyButton togg) {
        this.transpose(-1);
        if (replicador != null && replicador.isAlive()) {
            return;
        }
        replicador = new Thread(() -> {
            if (togg.isPressed()) {
                try {
                    Thread.sleep(Settings.BUTTON_REPEAT_INITIAL_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            while (togg.isPressed()) {
                try {
                    this.transpose(-1);
                    allPurposeScore.drawCurrentCamInOffscreen();
                    this.getUi().getPanel().repinta(true);
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        replicador.start();
    }

    public void onTransposeUpButtonPressed(MyButton togg) {
        this.transpose(1);
        if (replicador != null && replicador.isAlive()) {
            return;
        }
        replicador = new Thread(() -> {
            if (togg.isPressed()) {
                try {
                    Thread.sleep(Settings.BUTTON_REPEAT_INITIAL_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            while (togg.isPressed()) {
                try {
                    this.transpose(1);
                    allPurposeScore.drawCurrentCamInOffscreen();
                    this.getUi().getPanel().repinta(true);
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        replicador.start();
    }

    public void onSaveButtonPressed(MyButton togg) {
        this.buttons.hideTip();
        togg.setPressed(false);
        this.saveScore("");
    }

    public void onLoadButtonPressed(MyButton togg) {
        this.buttons.hideTip();
        togg.setPressed(false);
        this.loadScore("");
        togg.setPressed(false);
    }

    public void onSelectChoiceButtonPressed(MyButton togg) {
        MyChoice choice = this.allPurposeScore.getChoice();
        if (choice.isSelecting()){
            choice.setSelecting(false);
            togg.setPressed(false);
            this.allPurposeScore.setGridColorsHaveChanged(true);
        }
        else {
            choice.setSelecting(true);
            togg.setPressed(true);
        }
    }

    public void onNewButtonPressed(MyButton togg) {
        this.newScore();
    }

//    public void onTrackButtonPressed(MyButton togg) {
//        this.changeCurrentTrack();
//    }
//
//    public void onChannelButtonPressed(MyButton togg) {
//        this.changeCurrentChannelOfCurrentTrack();
//    }
//
    public void onInstrButtonPressed(MyButton togg) {
        this.changeInstrOfCurrentTrack();
    }

    public void onMixerButtonPressed(MyButton togg) {
        if (togg.isPressed()) {
            this.mixer.showMixer();
        } else {
            this.mixer.closeMixerWindow();
        }
    }

    private MeterData inputMeterDialog(MeterData defaultValue) {
        Component parentComponent = this.getUi(); // o un JFrame/JPanel real

        MeterData selection
                = MeterDialog.show(
                        parentComponent,
                        defaultValue.meterType,
                        defaultValue.meterPattern,
                        ""
                );

        return selection;
    }

    public void onTimeSignatureButtonPressed(MyButton togg) {
        if (cam.isPlaying()) { togg.setPressed(false); return; }
        String currentTS = this.allPurposeScore.params2TimeSignature();
        MeterData current = MeterDialog.timeSignature2MeterData(currentTS);
        MeterData selection = inputMeterDialog(current);
        if (selection == null) {
            togg.setPressed(false);
            return;
        }
        String compas = MeterDialog.meterData2TimeSignature(selection);
        // Analitza el nou compàs SEN aplicar-lo globalment:
        // guardem els valors actuals, parsem els nous, i restaurem els antics.
        int oldNBeatsMeasure = allPurposeScore.getNumBeatsMeasure();
        int oldBeatFigure    = allPurposeScore.getBeatFigure();
        int oldNColsQuarter  = Settings.getnColsQuarter();
        this.allPurposeScore.timeSignature2Params(compas); // aplica temporalment
        int newNBeatsMeasure = allPurposeScore.getNumBeatsMeasure();
        int newNColsQuarter  = Settings.getnColsQuarter();
        int newNColsBeat     = Settings.getnColsBeat();
        // Restaura els valors antics (el canvi s'aplicarà a la columna triada)
        allPurposeScore.setNumBeatsMeasure(oldNBeatsMeasure);
        allPurposeScore.setBeatFigure(oldBeatFigure);
        Settings.setnColsQuarter(oldNColsQuarter);
        Settings.updateNColsBeat();
        MetaMessage message = MyMidiScore.composeTimeSignatureMessage(compas);
        this.allPurposeScore.placeAppendMidiMessage(message);
        // Crea el canvi pendent (s'aplicarà a la columna on l'usuari faci clic)
        MyGridScore.ScoreChange sc = new MyGridScore.ScoreChange();
        sc.nBeatsMeasure = newNBeatsMeasure;
        sc.nColsQuarter  = newNColsQuarter;
        sc.nColsBeat     = newNColsBeat;
        setPendingChange(sc, I18n.t("scoreChange.timeSignature"));
        togg.setPressed(false);
    }

    public void changeCurrentTrack() {
        // this.mixer.changeCurrentTrack();
        throw new IllegalStateException("changeCurrentTrack not implemented");
    }

    public void changeInstrOfCurrentTrack() {
        this.getButtons().onButtonRelesased(this.getButtons().getId_InstrButton());
        int chan = this.mixer.getCurrentChannelOfCurrentTrack();
        String opcio = MyDialogs.showDialog(this.getUi(), I18n.f("MyController.instr.change.prompt", chan), I18n.t("MyController.instr.change.title"), SoundWithMidi.getInstrumentsStringList());
        if (opcio != null && !opcio.isEmpty()) {
            opcio = opcio.trim();
            String[] parts = opcio.split(" ");
            int newInstr = Integer.parseInt(parts[0]);
            SoundWithMidi.assignInstToChannel(chan, newInstr);
            SoundWithMidi.runProgramChange(chan, newInstr);
        }
        this.mixer.refreshMixer();
    }

    public void changeCurrentChannelOfCurrentTrack() {
        // this.mixer.changeCurrentChannelOfCurrentTrack();
        throw new IllegalStateException("changeCurrentChannelOfCurrentTrack not implemented");
    }

    public final void updateTextOfButtons() {
//        String titolGeneral = this.getUi().getVersion();
//        titolGeneral += "          " + this.allPurposeScore.getTitle() + ", " + this.allPurposeScore.getAuthor(); // +", "+this.allPurposeScore.getDescription()
//        this.getUi().setTitle(titolGeneral);

        // Aplica els canvis de paràmetres registrats a la columna actual
        applyChangesAt(getEditingCol());

        // Mostra el número de pàgina i el temps dins el compàs (1-based)
        int editCol  = getEditingCol();
        int colsBeat = Settings.getnColsBeat();
        int colsMeas = colsBeat * allPurposeScore.getNumBeatsMeasure();
        int beat     = (colsMeas > 0) ? (editCol % colsMeas) / colsBeat + 1 : 1;
        String pageNo = cam.getCurrentPage() + " (" + beat + ")";
        this.buttons.updatePageNumButton(pageNo);
        String tempo = "" + MyTempo.getTempo();
        this.buttons.updateTempoButton(tempo);
        MyTrack track = this.getMixer().getCurrentTrack();
        int volume = 63;
        if (track != null) {
            volume = track.getVelocity();
        }
        this.buttons.updateVolumeButton("" + volume);
        String timeSignature = allPurposeScore.params2TimeSignature();
        this.buttons.updateTimeSignatureButton(timeSignature);
        if (this.allPurposeScore != null) {
            String label = this.allPurposeScore.getLabel();
            this.buttons.updateCurrentExerciseButton(label);
        }
        this.buttons.updateCurrentKeyButton(ToneRange.getKeyName(this.allPurposeScore.getMidiKey(), this.allPurposeScore.getScaleMode()));
        this.buttons.setToggleButtonsToProgramValues();
        String name = "";
        int channel = -1;
        String instr = "";
        if (track != null) {
            name = track.getName();
            channel = track.getCurrentChannel();
            instr = SoundWithMidi.getInstrumentMnemonic(SoundWithMidi.getInstrumentInChannel(channel));
        }
//        this.buttons.updateTrackButton(name);
//        this.buttons.updateChannelButton("" + channel);
        this.buttons.updateInstrButton(instr);
//        this.mixer.refreshMixer();
//        this.buttons.setNeedsDrawing(false);
    }

    // ---------------------------------------------------------------------------
    // Mètodes del mapa de canvis per columna (ScoreChange)
    // ---------------------------------------------------------------------------

    /**
     * Desa un canvi pendent i mostra el missatge a la barra d'estat demanant
     * a l'usuari que faci clic a la columna on vol aplicar el canvi.
     *
     * @param change      el canvi a col·locar
     * @param description clau I18n (sense prefix) del tipus de canvi per al missatge
     */
    private void setPendingChange(MyGridScore.ScoreChange change, String description) {
        this.pendingChange = change;
        // Mostra un missatge emergent (no bloquejant visualment: és informatiu)
        // description ja és el text traduit (p.ex. "tempo", "compàs"...)
        javax.swing.JOptionPane.showMessageDialog(
                this.getUi(),
                I18n.f("scoreChange.clickToPlace", description),
                I18n.t("scoreChange.clickToPlace.title"),
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
        this.drawFull(true);
    }

    /**
     * Si hi ha un canvi pendent, el col·loca a la columna indicada, neteja
     * l'estat pendent i restaura el text de la barra d'estat.
     *
     * @param col columna de partitura on col·locar el canvi
     * @return true si s'ha col·locat un canvi pendent
     */
    private boolean placePendingChangeAt(int col) {
        if (pendingChange == null) return false;
        allPurposeScore.setScoreChange(col, pendingChange);
        pendingChange = null;
        needsSaving = true;
        // Redibuixa les franges que tenen línies de beat/compàs depenents del changeMap
        this.myChordSymbolLine.drawFullChordLineInOffscreen();
        this.myLyrics.drawFullLyricsInOffscreen();
        this.statusLine.setText(allPurposeScore.getTitle() + ": " + allPurposeScore.getDescription());
        this.updateTextOfButtons();
        this.drawFull(true);
        return true;
    }

    /**
     * Retorna la columna de partitura que correspon a la posició actual de la playbar.
     *
     * @return columna de partitura actual (mínim 0)
     */
    private int getEditingCol() {
        int camPBar = cam.getPlayBar();
        int col = allPurposeScore.getScoreCol(camPBar)
                  + allPurposeScore.getDelay(!allPurposeScore.isUseScreenKeyboardRight());
        return Math.max(0, col);
    }

    /**
     * Aplica els canvis de paràmetres globals efectius a la columna indicada.
     * No fa res si el changeMap és buit (tots els camps null).
     *
     * @param col columna de partitura
     */
    public void applyChangesAt(int col) {
        MyGridScore.ScoreChange sc = allPurposeScore.getEffectiveChange(col);
        if (sc.tempo != null)        MyTempo.setTempo(sc.tempo);
        if (sc.midiKey != null)      allPurposeScore.setMidiKey(sc.midiKey);
        if (sc.scaleMode != null)    allPurposeScore.setScaleMode(sc.scaleMode);
        if (sc.nBeatsMeasure != null) {
            allPurposeScore.setNumBeatsMeasure(sc.nBeatsMeasure);
            Settings.setnBeatsMeasure(sc.nBeatsMeasure);
        }
        if (sc.nColsQuarter != null) {
            Settings.setnColsQuarter(sc.nColsQuarter);
            Settings.updateNColsBeat(); // recalcula nColsBeat a partir de nColsQuarter i beatFigure
        }
        // nColsBeat pot estar present sense nColsQuarter (p.ex. si s'ha calculat per avançat)
        // En aquest cas s'usa directament si no hi ha nColsQuarter
        if (sc.nColsBeat != null && sc.nColsQuarter == null) {
            // No hi ha manera de recalcular beatFigure des de nColsBeat sol;
            // apliquem directament (cas inusual, conservatiu)
            Settings.setnColsQuarter(sc.nColsBeat); // aproximació: quarter = beat
            Settings.updateNColsBeat();
        }
        if (sc.nMeasuresCam != null) {
            Settings.setnMeasuresCam(sc.nMeasuresCam);
        }
        for (java.util.Map.Entry<Integer, Integer> e : sc.trackVelocities.entrySet()) {
            MyTrack t = getMixer().getTrackFromId(e.getKey());
            if (t != null) t.setVelocity(e.getValue());
        }
    }

    public void saveScore(String fitxer) {
        if ("".equals(fitxer)) {
            fitxer = MyDialogs.seleccionaFitxerEscriptura(null, "provaSave.mid", "mid");
        }
        if (fitxer == null || "".equals(fitxer)) {
            this.updateTextOfButtons();
            return;
        }
        File file = new File(fitxer);
        if (file.exists()) {
            int resposta = JOptionPane.showConfirmDialog(
                    null,
                    I18n.t("file.overwrite"),
//                    "El fitxer ja existeix.\nVols sobreescriure?",
                    I18n.t("MyController.dialog.confirm.title"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (resposta == JOptionPane.NO_OPTION) {
                this.updateTextOfButtons();
                return;
            }
        }
        String extensio = obtenirExtensio(fitxer);
        this.currentMidiFile = fitxer;
        if (extensio.equals("mid")) {
            this.stop();
            this.buttons.stopPlayButton();
            MyMidiScore midScore = (MyMidiScore) (this.allPurposeScore);
            this.allPurposeScore.saveMidiScore(fitxer);
            this.statusLine.setText(I18n.f("MyController.score.saving", midScore.getName(), fitxer));
        }
        this.needsSaving = false;
        this.updateTextOfButtons();
    }

    public static String obtenirExtensio(String nomFitxer) {
        // Comprovar si hi ha un punt al nom del fitxer
        int lastIndexOfDot = nomFitxer.lastIndexOf('.');

        // Si hi ha punt i no és al principi del nom
        if (lastIndexOfDot > 0 && lastIndexOfDot < nomFitxer.length() - 1) {
            return nomFitxer.substring(lastIndexOfDot + 1);  // Retornar l'extensió
        } else {
            return "";  // Retornar buit si no té extensió
        }
    }

    public void loadScore(String fitxer) {
//        this.newScore();
//        if ("".equals(fitxer)) fitxer = JOptionPane.showInputDialog("Fitxer?", "prova.mid");
        if ("".equals(fitxer)) {
            fitxer = MyDialogs.seleccionaFitxerLectura(null, "prova.mid");
        }
        if (fitxer == null || "".equals(fitxer)) {
            this.updateTextOfButtons();
            return;
        }
        File file = new File(fitxer);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(null, I18n.f("MyController.file.notfound", fitxer));
            this.updateTextOfButtons();
            return;
        }
        String extensio = obtenirExtensio(fitxer);
        this.currentMidiFile = fitxer;
        if (extensio.equals("mid")||extensio.equals("midi")) {
            this.stop();
            this.buttons.stopPlayButton();
            // this.allPurposeScore midScore = new MyMidiScore(this);
            this.exercisesOn = false;
            allPurposeScore.resetAllPurposeScore();
            this.mixer = new MyMixer(this);
            allPurposeScore.readMidiScore(fitxer);
            this.myChordSymbolLine.initOffscreen();
            this.myLyrics.initOffscreen();
            //this.myChordSymbolLine.setScore(allPurposeScore);
            //this.cam.setScore(allPurposeScore);
            //this.cam.setSymbolLine(myChordSymbolLine);
            this.cam.reset();
            this.statusLine.setText(allPurposeScore.getTitle() + ": " + allPurposeScore.getDescription());
        }
        this.updateTextOfButtons();
    }

    private void newScore() {
        allPurposeScore.resetAllPurposeScore(); //new MyAllPurposeScore(this);
        this.allPurposeScore.clearScore();  // buida notes, acords i missatges
        this.myLyrics.clear();
        this.stop();
        this.buttons.stopPlayButton();
        this.exercisesOn = false;
        Settings.setnMeasuresCam(4);
        // allPurposeScore.resetAllPurposeScore();
        //this.myChordSymbolLine.setScore(allPurposeScore);
        //this.cam.setScore(allPurposeScore);
        //this.cam.setSymbolLine(myChordSymbolLine);
        this.cam.reset();
        this.statusLine.setText(allPurposeScore.getTitle() + ": " + allPurposeScore.getDescription());
        this.buttons.setToggleButtonsToProgramValues();
        this.currentMidiFile = "";
        this.setDefaultTrack();
        this.allPurposeScore.initOffscreen();
        this.myChordSymbolLine.initOffscreen();
        this.myLyrics.initOffscreen();

//        this.allPurposeScore.getChoice().setNoneChoice();
    }

    public void clearScore() {
        this.allPurposeScore.clearScore();   // també crida clearChangeMap() internament
        this.allPurposeScore.clearChangeMap(); // crida explícita addicional per garantia
        this.myLyrics.clear();
        this.getStatusLine().clear();
        this.buttons.resetButtons(Settings.getFirstColControl(), Settings.getControlFirstRow(), Settings.getnColsControl(), Settings.getnRowsControl());
        this.allPurposeScore.setUseScreenKeyboardRight(false);
        this.setScreenKeyboardRight(this.allPurposeScore.isUseScreenKeyboardRight());
        ToneRange.setMovileDo(false);
        this.allPurposeScore.setShowNoteNames(true);
        this.setStrips(true); // penta
        this.allPurposeScore.getChoice().setNoneChoice();
//        this.allPurposeScore.setMidiKey(Settings.getDefaultKey());
//        this.updateTextOfButtons();
        this.needsSaving = false;
//        Settings.setnMeasuresCam(Settings.DEFAULT_NMEASURES_CAM);
//        String timeSignature = "4/4";
//        this.allPurposeScore.timeSignature2Params(timeSignature);
    }

    public void setConfigProperties() {
        AppConfig.get().set("autoCorrect", "" + Settings.isAutoCorrect());
        AppConfig.get().set("tipsVisible", "" + Settings.isTipsVisible());
        AppConfig.get().set("showMutted", "" + Settings.isShowMutted());
        AppConfig.get().set("nMeasuresCam", "" + Settings.getnMeasuresCam());
        AppConfig.get().set("nBeatsMeasure", "" + Settings.getnBeatsMeasure());
        AppConfig.get().set("nColsBeat", "" + Settings.getnColsBeat());
        AppConfig.get().set("nColsScore", "" + Settings.getnColsScore());
        AppConfig.get().set("dodecaphone", "" + ToneRange.isDodecaphone());
        if (!ToneRange.isDodecaphone()) {
            AppConfig.get().set("lowestMidi", "" + ToneRange.getLowestMidi());
            AppConfig.get().set("highestMidi", "" + ToneRange.getHighestMidi());
            AppConfig.get().set("octavesUP", "" + ToneRange.getOctavesUp());
            AppConfig.get().set("leadInstrument", "" + SoundWithMidi.getLeadInstrument());
            AppConfig.get().set("chordInstrument", "" + SoundWithMidi.getChordInstrument());
        }
        AppConfig.get().set("screenWidthRatio", "" + Settings.getScreenWidthRatio());
        AppConfig.get().set("screenHeightRatio", "" + Settings.getScreenHeightRatio());
        AppConfig.get().set(MyDialogs.CONFIG_KEY_LAST_DIR, MyDialogs.lastDirectory.getAbsolutePath());
        AppConfig.get().set(MyDialogs.CONFIG_KEY_LAST_DIR_SVG, MyDialogs.lastDirectorySvg.getAbsolutePath());
    }

    public boolean onExitCheckNSave() {
        if (this.needsSaving) {
            int resposta = JOptionPane.showConfirmDialog(
                    null,
                    I18n.t("MyController.exit.unsaved.message"),
                    I18n.t("MyController.exit.unsaved.title"),
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (resposta == JOptionPane.CANCEL_OPTION || resposta == JOptionPane.CLOSED_OPTION) {
                return false; // L’usuari ha cancel·lat
            } else if (resposta == JOptionPane.YES_OPTION) {
                this.saveScore("");
            }
        }

        this.setConfigProperties();

        try {
            AppConfig.get().save();
        } catch (IOException ex) {
            MyDialogs.mostraError(I18n.t("MyController.config.save.error"), I18n.t("MyController.dialog.error.title"));
        }

        return true;
    }

    private void nextExercise() {
        if (!this.exercisesOn) {
            this.exerciseListName = MyDialogs.seleccionaOpcio(I18n.t("MyController.exercise.selectFamily.prompt"), I18n.t("MyController.exercise.selectFamily.title"), exerciceListNames, 0);
            if (exerciseListName == null || exerciseListName.isEmpty()) {
                this.buttons.onButtonRelesased(this.getButtons().getId_NextExerciseButton());
                return;
            }
            this.exerciseList = new MyExerciseList(exerciseListName, this);
            this.exercisesOn = true;
            this.setDefaultTrack();
//            this.mixer = new MyMixer(this);
//            MyTrack track = new MyTrack(0, "Track 1");
//            track.afegirCanal(0);
//            track.setCurrentChannel(0);
//            SoundWithMidi.assignInstToChannel(0, 0); // GranPiano per defecte
//            this.mixer.addTrack(0, track, true, true);
//            this.mixer.addTrack(1, new MyTrack(1, "ChordBackground"), false, true);
//            this.mixer.setCurrentTrack(0);
        }
        this.stop();
        this.buttons.stopPlayButton();
        this.allPurposeScore = this.exerciseList.next();
//        this.myChordSymbolLine.setScore(allPurposeScore);
//        this.cam.setScore(allPurposeScore);
//        this.cam.setSymbolLine(myChordSymbolLine);
        this.cam.reset();
        this.showExerciseList();
        this.buttons.setToggleButtonsToProgramValues();
        this.statusLine.setText(allPurposeScore.getLabel() + ": " + allPurposeScore.getDescription());
    }

    private void prevExercise() {
        if (!this.exercisesOn) {
            this.exerciseListName = MyDialogs.seleccionaOpcio(I18n.t("MyController.exercise.selectPackage.prompt"), I18n.t("MyController.exercise.selectPackage.title"), exerciceListNames, 0);
            if (exerciseListName == null || exerciseListName.isEmpty()) {
                this.buttons.onButtonRelesased(this.getButtons().getId_PrevExerciseButton());
                return;
            }
            this.exerciseList = new MyExerciseList(exerciseListName, this);
            this.exercisesOn = true;
            this.setDefaultTrack();
//            this.mixer = new MyMixer(this);
//            MyTrack track = new MyTrack(0, "Track 1");
//            track.afegirCanal(0);
//            track.setCurrentChannel(0);
//            SoundWithMidi.assignInstToChannel(0, 0); // GranPiano per defecte
//            this.mixer.addTrack(0, track, true, true);
//            this.mixer.addTrack(1, new MyTrack(1, "ChordBackground"), false, true);
//            this.mixer.setCurrentTrack(0);
        }
        this.stop();
        this.buttons.stopPlayButton();
        this.allPurposeScore = this.exerciseList.previous();
//        this.myChordSymbolLine.setScore(allPurposeScore);
//        this.cam.setScore(allPurposeScore);
//        this.cam.setSymbolLine(myChordSymbolLine);
        this.cam.reset();
        this.showExerciseList();
        this.buttons.setToggleButtonsToProgramValues();
        this.statusLine.setText(allPurposeScore.getLabel() + ": " + allPurposeScore.getDescription());
    }

    private void resetExercise() {
        this.stop();
        this.buttons.stopPlayButton();
        this.exerciseList.resetCurrentExercise();
        this.myChordSymbolLine.initOffscreen();
        this.myLyrics.initOffscreen();
        this.buttons.setToggleButtonsToProgramValues();
        this.statusLine.setText(allPurposeScore.getLabel() + ": " + allPurposeScore.getDescription());
    }

    private int getRandKey() {
        String key;
        if (this.allPurposeScore.getScaleMode() == 'M') {
            key = MyKeyCircles.randM();
        } else if (this.allPurposeScore.getScaleMode() == 'm') {
            key = MyKeyCircles.randm();
        } else {
            key = "Do";
        }
        int midi = ToneRange.getMidi(key);
        // this.allPurposeScore.setMidiKey(midi);
        return midi;
    }
}
