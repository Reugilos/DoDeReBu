/*
 * PolyForm Noncommercial License 1.0.0
 * Copyright (c) 2024-2026 Pau Bofill. Powered by Claude AI.
 * Full license / Llicència completa: LICENSE (project root / arrel del projecte)
 */
package dodecagraphone;

import dodecagraphone.model.MyChoice;
import dodecagraphone.model.MyKeyCircles;
import dodecagraphone.model.component.MyExercise;
import dodecagraphone.model.component.MyButtonPanel;
import dodecagraphone.model.InstrumentRange;
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
import dodecagraphone.teclesControl.ChordEvent;
import dodecagraphone.teclesControl.ClipChord;
import dodecagraphone.teclesControl.ClipLyric;
import dodecagraphone.teclesControl.ClipNote;
import dodecagraphone.teclesControl.ColumnEvent;
import dodecagraphone.teclesControl.Event;
import dodecagraphone.teclesControl.MouseSequence;
import dodecagraphone.teclesControl.MoveNoteEvent;
import dodecagraphone.teclesControl.PasteEvent;
import dodecagraphone.teclesControl.PilaEvents;
import dodecagraphone.ui.AppConfig;
import dodecagraphone.ui.I18n;
import dodecagraphone.ui.MeterDialog;
import dodecagraphone.ui.MeterDialog.MeterData;
import dodecagraphone.ui.MyDialogs;
import dodecagraphone.ui.MyNewPanel;
import dodecagraphone.ui.MyUserInterface;
import dodecagraphone.ui.DodecagramPdfPrinter;
import dodecagraphone.ui.SVGandPDF;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * [CA] Controlador principal de l'aplicació DoDeReBu. Gestiona tota la lògica
 * d'interacció: events de ratolí (press/drag/release), edició de notes (ADD, ERASE,
 * EXTEND, MOVE, SELECT, PASTE), sistema undo/redo, reproducció MIDI, canvis de
 * paràmetres (tempo, to, compàs, volum), gestió de fitxers (new/load/save),
 * exercicis d'ear training i inserció/eliminació de columnes.
 * <p>
 * La classe connecta el model ({@link dodecagraphone.model.component.MyAllPurposeScore},
 * {@link dodecagraphone.model.component.MyChordSymbolLine},
 * {@link dodecagraphone.model.component.MyLyrics},
 * {@link dodecagraphone.model.component.MyCamera}, etc.) amb la vista
 * ({@link dodecagraphone.ui.MyUserInterface}, {@link dodecagraphone.model.component.MyButtonPanel}).
 * <p>
 * [EN] Main application controller for DoDeReBu. Manages all interaction logic:
 * mouse events (press/drag/release), note editing (ADD, ERASE, EXTEND, MOVE,
 * SELECT, PASTE), undo/redo system, MIDI playback, parameter changes (tempo, key,
 * time signature, volume), file management (new/load/save), ear-training exercises
 * and column insert/delete.
 * <p>
 * The class connects the model ({@link dodecagraphone.model.component.MyAllPurposeScore},
 * {@link dodecagraphone.model.component.MyChordSymbolLine},
 * {@link dodecagraphone.model.component.MyLyrics},
 * {@link dodecagraphone.model.component.MyCamera}, etc.) with the view
 * ({@link dodecagraphone.ui.MyUserInterface}, {@link dodecagraphone.model.component.MyButtonPanel}).
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MyController {

//    private int aux = 0;
//   private final String exerciseListName = "Blues";
//    private final String exerciseListName = "Test";
//    private final String exerciseListName = "Saxo";
//    private final String exerciseListName = "Miri";
//    private final String exerciseListName = "UYE";
    private String[] getExerciseListNames() {
        return ToneRange.isMetallophone()
                ? new String[]{"DoDeReBuExercises"}
                : new String[]{"DoDeReBuExercises", "EarTraining", "Jazz"};
    }
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
    private enum DragMode { NONE, ADD, ERASE, EXTEND_PENDING, EXTEND_RIGHT, EXTEND_LEFT, MOVE, SELECT, PASTE }
    private DragMode dragMode = DragMode.NONE;
    private int extendStartRow = -1;
    private int extendStartCol = -1;
    // MOVE mode state
    private int moveNoteOrigRow = -1;
    private int moveCurrentRow = -1;
    private int moveNoteOrigHeadCol = -1;
    private int moveNoteLength = 0;
    private int moveCurrentHeadCol = -1;
    private int moveClickOffset = 0;
    private int moveCh = -1;
    private int moveTr = -1;
    private int[] moveVelocities;
    private boolean[] moveVisibles;
    private boolean[] moveMuteds;
    private boolean[] moveLinkeds;
    private boolean moveDotted;
    // SELECT mode state
    private int selStartRow = -1;
    private int selStartCol = -1;
    private int selEndRow   = -1;
    private int selEndCol   = -1;
    private boolean selectionActive = false;
    // Auto-scroll durant SELECT drag
    private Timer    selAutoScrollTimer = null;
    private int      selAutoScrollDir   = 0;   // +1 = dreta, -1 = esquerra
    private double   lastSelectDragY    = 0;   // última Y del ratolí en SELECT drag
    // Clipboard
    private List<ClipNote> clipboard = null;
    private List<ClipChord> clipboardChords = new ArrayList<>();
    private List<ClipLyric> clipboardLyrics = new ArrayList<>();
    private boolean clipboardMultiTrack = false;
    private boolean clipboardTipVisible = false;
    /** Instant (nanoTime) en què va acabar l'última reproducció. S'usa per detectar idle llarg. */
    private volatile long lastPlayEndNanos = 0;
    // PASTE mode state
    private boolean pendingPaste = false;
    private int pasteCurrentRow = -1;
    private int pasteCurrentCol = -1;
    private int pasteCh = -1;
    private int pasteTr = -1;
    private boolean pasteDotted = false;
    private MyExerciseList exerciseList;
    private Thread replicador;
    private MyGridSquare firstNote = null;
    private MyGridSquare lastNote = null;
    /** True if firstNote was already linked before the EXTEND_LEFT drag touched it. */
    private boolean firstNoteWasLinkedBeforeDrag = false;
    private boolean needsSaving = false;
    private Boolean saveChordMidiChoice = null; // null = no preguntat; true/false = resposta de la sessió
    private boolean drumsMode = false;
    private boolean tremoloActive = false;
    private int lastMelodyTrackId = 0;
    private volatile boolean needsDrawing = true;
    private boolean printing = false;

    private final PilaEvents pilaEvents = new PilaEvents();
    private MouseSequence mouseSequence = null;

    /**
     * Canvi de paràmetre pendent de col·locació per part de l'usuari.
     * Quan és no-null, el pròxim clic al grid col·loca el canvi a aquella columna.
     */
    private MyGridScore.ScoreChange pendingChange = null;
    private int pendingTransposeStep = 0;

    private String pendingColumnOp = null;
    private int pendingColumnN = 1;
    private javax.swing.JDialog pendingColumnDialog = null;

    private SampleOrMidi instrument;
//    private int numBeatsMeasure;
//    private int beatFigure;
//    private int numMeasuresPage;
//    private int totalNumMeasures;
//    private int numGridSquaresBeat;
//    private int tempo;

    /**
     * [CA] Construeix el controlador i inicialitza tots els components de la UI
     * (graella, teclat, càmera, botons, franja d'acords, línia de lletra). Es crida
     * una sola vegada en arrencar l'aplicació.
     * <p>
     * [EN] Builds the controller and initialises all UI components (grid, keyboard,
     * camera, buttons, chord strip, lyrics strip). Called once at application startup.
     *
     * @param ui [CA] finestra principal de l'aplicació / [EN] main application window
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
        this.allPurposeScore.updateStopMarker();
        int tempo = Settings.getDefaultTempo();
        MyTempo.setTempo(tempo);
//        MyTempo.checkTempo();
        this.lastXiloKeyPressed = -1;
        this.lastRowPressed = -1;
        this.lastButtonPressed = -1;
        this.turningOn = false;
        this.allPurposeScore.setShowNoteNames(true);
        this.allPurposeScore.setUseScreenKeyboardRight(!left);
        this.allPurposeScore.resetMetadata();
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

    /**
     * [CA] Desfà l'últim event registrat a la pila undo. Usa
     * {@code drawCurrentCamInOffscreen()} (ràpid) per actualitzar la vista.
     * <p>
     * [EN] Undoes the last event recorded in the undo stack. Uses
     * {@code drawCurrentCamInOffscreen()} (fast) to update the view.
     */
    public void undo() {
        pilaEvents.undo();
        if (pilaEvents.isUndoEmpty()) needsSaving = false;
        refreshAnacrusis();
        this.drawFull(true);
    }

    /**
     * [CA] Refà l'últim event desfet. Actualitza l'anacrusis i redibuixa la vista.
     * <p>
     * [EN] Redoes the last undone event. Updates anacrusis detection and redraws the view.
     */
    public void redo() {
        pilaEvents.redo();
        refreshAnacrusis();
        this.drawFull(true);
    }

    /**
     * [CA] Afegeix un event a la pila undo/redo.
     * <p>
     * [EN] Adds an event to the undo/redo stack.
     *
     * @param e [CA] l'event a afegir / [EN] the event to add
     */
    public void afegirEvent(Event e) {
        pilaEvents.afegirEvent(e);
    }

    /**
     * [CA] Redibuixa la franja d'acords al buffer offscreen i actualitza la vista.
     * <p>
     * [EN] Redraws the chord strip to the offscreen buffer and updates the view.
     */
    public void redrawChordLine() {
        myChordSymbolLine.setNeedsDrawing(true);
        myChordSymbolLine.drawFullChordLineInOffscreen();
        drawFull(true);
    }

    /**
     * [CA] Mostra un missatge flotant informatiu indicant que el porta-retalls
     * conté dades. Respecta {@code Settings.isTipsVisible()} automàticament.
     * <p>
     * [EN] Shows a floating informational tooltip indicating the clipboard contains
     * data. Respects {@code Settings.isTipsVisible()} automatically.
     */
    public void showClipboardTip() {
        double tipX = Settings.getScreenWidth() / 2.0;
        double tipY = Settings.getChordFirstRow() * Settings.getRowHeight() + 30;
        this.buttons.hideTip();
        this.buttons.showCustomTip(I18n.t("clipboard.full.tip"), tipX, tipY);
        this.clipboardTipVisible = true;
        this.lastTipButton = -1;
    }

    public void clearClipboardTip() {
        if (clipboardTipVisible) {
            clipboardTipVisible = false;
            this.buttons.hideTip();
        }
    }

    /**
     * [CA] Indica si cal redibuixar la pantalla al proper tick de la UI.
     * <p>
     * [EN] Indicates whether the screen needs to be redrawn at the next UI tick.
     *
     * @return [CA] {@code true} si cal redibuixar / [EN] {@code true} if a redraw is needed
     */
    public boolean isNeedsDrawing() {
        return needsDrawing;
    }

    /**
     * [CA] Estableix si cal redibuixar la pantalla al proper tick de la UI.
     * <p>
     * [EN] Sets whether the screen needs to be redrawn at the next UI tick.
     *
     * @param needsDrawing [CA] {@code true} per forçar un redibuix / [EN] {@code true} to force a redraw
     */
    public void setNeedsDrawing(boolean needsDrawing) {
        this.needsDrawing = needsDrawing;
    }

    /**
     * [CA] Indica si l'aplicació es troba en mode d'impressió/exportació.
     * <p>
     * [EN] Indicates whether the application is in print/export mode.
     *
     * @return [CA] {@code true} si s'està imprimint / [EN] {@code true} if printing is active
     */
    public boolean isPrinting() {
        return printing;
    }

    /**
     * [CA] Activa o desactiva el mode d'impressió/exportació.
     * <p>
     * [EN] Activates or deactivates print/export mode.
     *
     * @param printing [CA] {@code true} per activar el mode impressió / [EN] {@code true} to activate print mode
     */
    public void setPrinting(boolean printing) {
        this.printing = printing;
    }

    
    /**
     * [CA] Configura una pista com a pista d'acords i l'afegeix al mixer. Assigna
     * el canal 15 i l'instrument d'acords per defecte.
     * <p>
     * [EN] Configures a track as the chord track and adds it to the mixer. Assigns
     * channel 15 and the default chord instrument.
     *
     * @param track [CA] pista a configurar / [EN] track to configure
     */
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

    /**
     * [CA] Configura una pista com a pista de percussió i l'afegeix al mixer.
     * Assigna el canal 9 (canal MIDI de percussió general).
     * <p>
     * [EN] Configures a track as the drums track and adds it to the mixer. Assigns
     * channel 9 (General MIDI drums channel).
     *
     * @param track [CA] pista a configurar / [EN] track to configure
     */
    public void addDrumsTrackAndInstrumentToMixer(MyTrack track) {
        track.setVelocity(63);
        int canal = 9;
        track.afegirCanal(canal);
        track.setCurrentChannel(canal);
        track.setVisible(true);
        track.setAudible(true);
        this.mixer.setDrumsTrack(track);
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
        int offset = InstrumentRange.calcDisplayOffset(instr, ToneRange.getLowestMidi(), ToneRange.getHighestMidi());
        track.setDisplayOffset(offset);
        this.mixer.addTrack(track);
        this.mixer.setCurrentTrack(track.getId());
    }

    public final void setDefaultTrack() {
        this.mixer = new MyMixer(this);
        SoundWithMidi.resetChannels();
        MyTrack chordTr = new MyTrack(this.mixer.getChordTrackId(), this.mixer.getChordTrackName());
        this.addChordTrackAndInstrumentToMixer(chordTr);
        MyTrack track = new MyTrack(0, "Track 1");
        track.setIsNew(true);
        this.addTrackAndInstrumentToMixer(track, SoundWithMidi.getLeadInstrument());
        MyTrack drumsTr = new MyTrack(this.mixer.getDrumsTrackId(), this.mixer.getDrumsTrackName());
        this.addDrumsTrackAndInstrumentToMixer(drumsTr);
        this.mixer.setCurrentTrack(0);
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
        // Chord i lyrics sempre dins la càmera (firstCol=0 relatiu a cam), nColsCam columnes.
        this.myChordSymbolLine.setDimensions(Settings.getChordFirstCol(), Settings.getChordFirstRow(), Settings.getnColsChord(), Settings.getnRowsChord());
        this.myLyrics.setDimensions(Settings.getLyricsFirstCol(), Settings.getLyricsFirstRow(), Settings.getnColsLyrics(), Settings.getnRowsLyrics());
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
        boolean modified = this.mixer.isModified() || this.needsDrawing;
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
        double camW   = Settings.getnColsCam()      * Settings.getColWidth();
        boolean right = allPurposeScore != null && allPurposeScore.isUseScreenKeyboardRight();

        int chordY  = (int) Math.round(Settings.getChordFirstRow()  * rowH);
        int chordH  = (int) Math.round(Settings.getnRowsChord()     * rowH);
        int lyricsY = (int) Math.round(Settings.getLyricsFirstRow() * rowH);
        int lyricsH = (int) Math.round(Settings.getnRowsLyrics()    * rowH);
        int kw      = (int) Math.ceil(keyW);
        int kx      = right ? (int) Math.ceil(camW) : 0;  // x on comença la zona del keyboard
        int sepX    = right ? (int) Math.ceil(camW) : kw; // línia separadora càmera/keyboard

        // White fill for the keyboard-column part of each strip
        g.setColor(Color.WHITE);
        g.fillRect(kx, chordY,  kw, chordH);
        g.fillRect(kx, lyricsY, kw, lyricsH);

        // Vertical separator line between keyboard column and camera area
        g.setColor(Color.BLACK);
        g.drawLine(sepX, chordY,  sepX, chordY  + chordH);
        g.drawLine(sepX, lyricsY, sepX, lyricsY + lyricsH);

        // Labels: vertically centered, aligned with keyboard zone
        java.awt.FontMetrics fm = g.getFontMetrics();
        int lineH  = fm.getAscent() + fm.getDescent();
        int margin = (int) Math.max(4, Settings.getColWidth());
        int labelX = kx + margin;
        g.setColor(Color.BLACK);

        // "Acords" label at top of chord strip
        int acordsY = chordY + margin + fm.getAscent();
        g.drawString(I18n.t("myChordSymbolLine.label"), labelX, acordsY);

        // Format button 2 rows below the "Acords" label
        String fmtLabel = dodecagraphone.model.chord.ChordSymbols.formatLabel(
                myChordSymbolLine.getDisplayFormat());
        int btnPad = 2;
        int btnW = fm.stringWidth(fmtLabel) + 2 * btnPad;
        int btnH = lineH + 2 * btnPad;
        int btnX = labelX;
        int btnY = (int) Math.round(chordY + 2 * rowH);
        g.setColor(new Color(220, 230, 255));
        g.fillRoundRect(btnX, btnY, btnW, btnH, 4, 4);
        g.setColor(Color.BLACK);
        g.drawRoundRect(btnX, btnY, btnW, btnH, 4, 4);
        g.drawString(fmtLabel, btnX + btnPad, btnY + btnPad + fm.getAscent());

        // Lyrics label
        g.drawString(I18n.t("myLyrics.label"),
                labelX,
                lyricsY + (lyricsH + lineH) / 2);
    }

    private boolean isChordFormatButtonClick(double posX, double posY) {
        double rowH   = Settings.getRowHeight();
        double keyW   = Settings.getnColsKeyboard() * Settings.getColWidth();
        boolean right = allPurposeScore != null && allPurposeScore.isUseScreenKeyboardRight();
        double kx     = right ? Settings.getnColsCam() * Settings.getColWidth() : 0;
        double chordY = Settings.getChordFirstRow()  * rowH;
        double chordH = Settings.getnRowsChord()     * rowH;
        return posX >= kx && posX < kx + keyW && posY >= chordY && posY < chordY + chordH;
    }

    public void redraw(Graphics2D g) {
        // Auto-correct if panel size diverges from Settings (e.g. maximize without componentResized)
        int pw = getUi().getPanel().getWidth();
        int ph = getUi().getPanel().getHeight();
        if (pw > 0 && ph > 0
                && (Math.abs(pw - (int) Settings.getScreenWidth())  > 1
                 || Math.abs(ph - (int) Settings.getScreenHeight()) > 1)) {
            Settings.setScreenPixelDimensions(pw, ph);
            onScreenResizedQuick();
        }

        // rowHeight (no squareHeight) perquè botons i franges no s'adapten a nRowsSquare
        int fontSize = Math.max(8, (int)(Settings.getRowHeight() * 0.85));
        Font font = new Font("Dialog", Font.BOLD, fontSize);
        g.setFont(font);
        // Sempre blit·ejem el buffer offscreen (ja actualitzat) a la pantalla.
        // needsDrawing controlava si es dibuixava, però això deixava la pantalla
        // en blanc en restaurar la finestra o obrir diàlegs (OS repaint sense
        // que needsDrawing fos true). El buffer offscreen és barat de copiar.
        Utilities.printOutWithPriority(5, "MyController::redraw: count = " + count3++);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, pw, ph);
        drawStripsBackground(g);
        this.screen.draw(g);
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

    public MyChordSymbolLine getMyChordSymbolLine() {
        return myChordSymbolLine;
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
            this.allPurposeScore.updateStopMarker();
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
            if (col + 1 > this.allPurposeScore.getLastColWritten())
                this.allPurposeScore.setLastColWritten(col + 1);
            this.allPurposeScore.updateStopMarker();
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

    // ── MOVE helpers ────────────────────────────────────────────────────────────

    private int findNoteHeadCol(int row, int col) {
        int h = col;
        while (h > 0) {
            MyGridSquare sq = this.allPurposeScore.getGridSquare(row, h);
            if (sq == null || !sq.isSqVisible() || !sq.isSq_is_linked()) break;
            h--;
        }
        return h;
    }

    private int findNoteTailCol(int row, int col) {
        int t = col;
        while (true) {
            MyGridSquare next = this.allPurposeScore.getGridSquare(row, t + 1);
            if (next == null || !next.isSqVisible() || !next.isSq_is_linked()) break;
            t++;
        }
        return t;
    }

    private void captureNoteData(int row, int headCol, int length, int ch, int tr) {
        moveVelocities = new int[length];
        moveVisibles   = new boolean[length];
        moveMuteds     = new boolean[length];
        moveLinkeds    = new boolean[length];
        moveDotted = this.mixer.getCurrentTrack().isDotted();
        for (int i = 0; i < length; i++) {
            MyGridSquare sq = this.allPurposeScore.getGridSquare(row, headCol + i);
            if (sq != null) {
                for (MyGridSquare.SubSquare sub : sq.getPoliNotes()) {
                    if (sub.getChannel() == ch && sub.getTrack() == tr) {
                        moveVelocities[i] = sub.getVelocity();
                        moveVisibles[i]   = sub.isVisible();
                        moveMuteds[i]     = !sub.isAudible();
                        moveLinkeds[i]    = sub.isLinked();
                        break;
                    }
                }
            }
        }
    }

    /** Remove the live (temp) note from the grid without recording in mouseSequence. */
    private void removeTempNote(int row, int headCol) {
        for (int i = moveNoteLength - 1; i >= 0; i--) {
            this.allPurposeScore.removeNoteFromSquare(row, headCol + i, moveCh, moveTr);
            MyGridSquare sq = this.allPurposeScore.getGridSquare(row, headCol + i);
            if (sq != null) sq.updateState();
        }
    }

    /** Place the live (temp) note at the grid without recording in mouseSequence. */
    private void placeTempNote(int row, int headCol) {
        int gridWidth = this.allPurposeScore.getGrid()[row].length;
        for (int i = 0; i < moveNoteLength; i++) {
            int c = headCol + i;
            if (c < 0 || c >= gridWidth) continue;
            MyGridSquare sq = this.allPurposeScore.addNoteToSquare(
                    row, c, 1, Settings.getnRowsSquare(),
                    (MyComponent) allPurposeScore, this, allPurposeScore, this.getCam(),
                    moveCh, moveTr, moveVelocities[i], moveVisibles[i], moveMuteds[i], moveLinkeds[i], moveDotted);
            sq.updateState();
        }
        int lastC = headCol + moveNoteLength - 1;
        if (lastC + 1 > this.allPurposeScore.getLastColWritten())
            this.allPurposeScore.setLastColWritten(lastC + 1);
        this.allPurposeScore.updateStopMarker();
    }

    // ── END MOVE helpers ─────────────────────────────────────────────────────────

    // ── Selection overlay getters (read by MyGridScore) ──────────────────────────

    public boolean isSelectionActive() { return selectionActive; }
    public void clearSelection() { selectionActive = false; }
    public int getSelStartRow()        { return selStartRow; }
    public int getSelStartCol()        { return selStartCol; }
    public int getSelEndRow()          { return selEndRow; }
    public int getSelEndCol()          { return selEndCol; }

    // ── Copy / Cut / Paste ───────────────────────────────────────────────────────

    /** Copies notes in the current selection to the clipboard. Asks if all tracks or current only. */
    public void copySelection() {
        if (!selectionActive) return;
        int r1 = Math.min(selStartRow, selEndRow);
        int r2 = Math.max(selStartRow, selEndRow);
        int c1 = Math.min(selStartCol, selEndCol);
        int c2 = Math.max(selStartCol, selEndCol);
        long activeTracks = mixer.getTracks().stream().filter(t -> !t.isDeleted()).count();
        if (activeTracks > 1) {
            int res = MyDialogs.demanaConfirmacio(
                    I18n.t("copy.allTracks.confirm"), I18n.t("copy.allTracks.title"));
            clipboardMultiTrack = (res == javax.swing.JOptionPane.YES_OPTION);
        } else {
            clipboardMultiTrack = false;
        }
        // Copia accords i lletres si el track actual és el chord track o si és multi-track
        clipboardChords = new ArrayList<>();
        clipboardLyrics = new ArrayList<>();
        boolean includeChords = clipboardMultiTrack
                || this.mixer.getCurrentTrackId() == this.mixer.getChordTrackId();
        if (includeChords) {
            for (Map.Entry<Integer, Chord> entry : allPurposeScore.getChordSimbolLine().entrySet()) {
                int col = entry.getKey();
                if (col >= c1 && col <= c2) {
                    clipboardChords.add(new ClipChord(col - c1, entry.getValue()));
                }
            }
            // Copia lletres de tots els tracks de la zona seleccionada
            for (Map.Entry<Integer, List<MyLyrics.LyricSegment>> entry
                    : myLyrics.getLyricsByTrack().entrySet()) {
                int trackId = entry.getKey();
                for (MyLyrics.LyricSegment seg : entry.getValue()) {
                    if (seg.col >= c1 && seg.col <= c2) {
                        clipboardLyrics.add(new ClipLyric(seg.col - c1, trackId, seg.text));
                    }
                }
            }
        }
        clipboard = new ArrayList<>();
        if (clipboardMultiTrack) {
            for (int row = r1; row <= r2; row++) {
                for (int col = c1; col <= c2; col++) {
                    MyGridSquare sq = this.allPurposeScore.getGridSquare(row, col);
                    if (sq == null || !sq.isSqVisible()) continue;
                    for (MyGridSquare.SubSquare sub : sq.getPoliNotes()) {
                        MyTrack t = this.mixer.getTrackFromId(sub.getTrack());
                        clipboard.add(new ClipNote(row - r1, col - c1, sub.getVelocity(),
                                sub.isVisible(), !sub.isAudible(), sub.isLinked(),
                                t != null && t.isDotted(), sub.getTrack(), sub.getChannel()));
                    }
                }
            }
        } else {
            int ch = this.mixer.getCurrentChannelOfCurrentTrack();
            int tr = this.mixer.getCurrentTrackId();
            for (int row = r1; row <= r2; row++) {
                for (int col = c1; col <= c2; col++) {
                    MyGridSquare sq = this.allPurposeScore.getGridSquare(row, col);
                    if (sq == null || !sq.isSqVisible()) continue;
                    for (MyGridSquare.SubSquare sub : sq.getPoliNotes()) {
                        if (sub.getChannel() == ch && sub.getTrack() == tr) {
                            clipboard.add(new ClipNote(row - r1, col - c1, sub.getVelocity(),
                                    sub.isVisible(), !sub.isAudible(), sub.isLinked(),
                                    this.mixer.getCurrentTrack().isDotted(), tr, ch));
                            break;
                        }
                    }
                }
            }
        }
        selectionActive = false;
    }

    /** Copies selection to clipboard, then erases it (undoable). */
    public void cutSelection() {
        if (!selectionActive) return;
        int r1 = Math.min(selStartRow, selEndRow);
        int r2 = Math.max(selStartRow, selEndRow);
        int c1 = Math.min(selStartCol, selEndCol);
        int c2 = Math.max(selStartCol, selEndCol);
        copySelection(); // sets clipboardMultiTrack and fills clipboard; clears selectionActive
        if (clipboard == null || clipboard.isEmpty()) return;
        mouseSequence = new MouseSequence(this);
        if (clipboardMultiTrack) {
            // Esborra notes de tots els tracks a la zona seleccionada
            for (int row = r1; row <= r2; row++) {
                for (int col = c1; col <= c2; col++) {
                    MyGridSquare sq = this.allPurposeScore.getGridSquare(row, col);
                    if (sq == null) continue;
                    for (MyGridSquare.SubSquare sub : new ArrayList<>(sq.getPoliNotes())) {
                        int savedTr = this.mixer.getCurrentTrackId();
                        int savedCh = this.mixer.getCurrentChannelOfCurrentTrack();
                        this.mixer.setCurrentTrack(sub.getTrack());
                        removeNoteAtCell(row, col);
                        this.mixer.setCurrentTrack(savedTr);
                    }
                }
            }
        } else {
            for (int row = r1; row <= r2; row++) {
                for (int col = c1; col <= c2; col++) {
                    removeNoteAtCell(row, col);
                }
            }
        }
        // Elimina accords de la selecció si n'hi ha al clipboard
        if (!clipboardChords.isEmpty()) {
            for (ClipChord cc : clipboardChords) {
                int col = c1 + cc.colOffset;
                Chord existing = allPurposeScore.getChordSimbolLine().get(col);
                if (existing != null) {
                    mouseSequence.addChordRemove(col, existing);
                    allPurposeScore.removeChordSymbol(col);
                }
            }
            redrawChordLine();
        }
        // Elimina lletres de la selecció si n'hi ha al clipboard
        if (!clipboardLyrics.isEmpty()) {
            for (ClipLyric cl : clipboardLyrics) {
                int col = c1 + cl.colOffset;
                String existing = myLyrics.getLyric(col, cl.trackId);
                if (existing != null) {
                    mouseSequence.addLyricRemove(col, cl.trackId, existing);
                    myLyrics.removeLyric(col, cl.trackId);
                }
            }
            myLyrics.drawFullLyricsInOffscreen();
        }
        if (!mouseSequence.isEmpty()) afegirEvent(mouseSequence);
        mouseSequence = null;
        this.needsSaving = true;
    }

    /** Replicate selection to the right. toEnd=false: one copy; toEnd=true: ask target measure. */
    public void replicateSelection(boolean toEnd) {
        if (!selectionActive) return;
        int r1 = Math.min(selStartRow, selEndRow);
        int r2 = Math.max(selStartRow, selEndRow);
        int c1 = Math.min(selStartCol, selEndCol);
        int c2 = Math.max(selStartCol, selEndCol);
        int selWidth = c2 - c1 + 1;
        long activeTracksR = mixer.getTracks().stream().filter(t -> !t.isDeleted()).count();
        boolean allTracks;
        if (activeTracksR > 1) {
            int res = MyDialogs.demanaConfirmacio(
                    I18n.t("copy.allTracks.confirm"), I18n.t("copy.allTracks.title"));
            allTracks = (res == javax.swing.JOptionPane.YES_OPTION);
        } else {
            allTracks = false;
        }
        mouseSequence = new MouseSequence(this);
        if (!toEnd) {
            pasteSelectionCopy(r1, r2, c1, c2, c2 + 1, c2 + selWidth, allTracks);
            selStartCol = c2 + 1;
            selEndCol   = c2 + selWidth;
        } else {
            // Calcula el darrer compàs actual
            int stopCol = allPurposeScore.getStopCol();
            int colsPerMeasure = Settings.getnColsBeat() * allPurposeScore.getNumBeatsMeasure();
            if (colsPerMeasure <= 0) colsPerMeasure = 1;
            int[] stopMB = allPurposeScore.getMeasureAndBeatAt(Math.max(0, stopCol - 1));
            int lastMeasure = stopMB[0];

            String input = MyDialogs.mostraInputDialog(
                    I18n.t("replicate.toMeasure.prompt"),
                    I18n.t("replicate.toMeasure.title"),
                    String.valueOf(lastMeasure));
            if (input == null) { mouseSequence = null; return; }
            int targetMeasure;
            try { targetMeasure = Integer.parseInt(input); }
            catch (NumberFormatException ex) { mouseSequence = null; return; }

            // Col final del compàs objectiu; intenta posició exacta via changeMap
            int targetEndCol;
            int fc = allPurposeScore.getFirstColOfMeasure(targetMeasure);
            if (fc >= 0) {
                targetEndCol = fc + colsPerMeasure - 1;
            } else {
                // Compàs més enllà del contingut actual — extrapola des de stopCol
                targetEndCol = stopCol + (targetMeasure - lastMeasure) * colsPerMeasure - 1;
            }
            if (targetEndCol < c2) { mouseSequence = null; return; }

            // Amplia el buffer i els offscreens si cal
            if (targetEndCol >= allPurposeScore.getNColsBuffer()) {
                int colsPerPage = allPurposeScore.getFixedColsPerPage();
                int newNCols = targetEndCol + 2 * colsPerPage;
                allPurposeScore.resizeOffscreen(newNCols);
                myChordSymbolLine.resizeOffscreen(newNCols);
                myLyrics.resizeOffscreen(newNCols);
            }

            int destStart = c2 + 1;
            while (destStart <= targetEndCol) {
                int destEnd = Math.min(destStart + selWidth - 1, targetEndCol);
                pasteSelectionCopy(r1, r2, c1, c2, destStart, destEnd, allTracks);
                destStart += selWidth;
            }
            selectionActive = false;
        }
        if (!mouseSequence.isEmpty()) afegirEvent(mouseSequence);
        mouseSequence = null;
        this.needsSaving = true;
        this.allPurposeScore.updateStopMarker();
        refreshAnacrusis();
        if (selectionActive) {
            int newSelStart = Math.min(selStartCol, selEndCol);
            if (newSelStart >= allPurposeScore.getCurrentCol()) {
                navigateToScoreCol(newSelStart);
            }
        }
        updateTextOfButtons();
    }

    /** Navega perquè targetScoreCol quedi al playBar. */
    private void navigateToScoreCol(int targetScoreCol) {
        int camPBar      = cam.getPlayBar();
        int firstParent  = allPurposeScore.getFirstParentCol();
        int nColsCam     = cam.getnCols();
        int delay        = allPurposeScore.getDelay(!allPurposeScore.isUseScreenKeyboardRight());
        int newCurrentCol = targetScoreCol - camPBar + firstParent + nColsCam - delay;
        newCurrentCol = Math.max(0, newCurrentCol);
        allPurposeScore.setCurrentCol(newCurrentCol);
    }

    private void pasteSelectionCopy(int r1, int r2, int srcC1, int srcC2, int destStart, int destEnd, boolean allTracks) {
        for (int row = r1; row <= r2; row++) {
            for (int col = srcC1; col <= srcC2; col++) {
                int destCol = destStart + (col - srcC1);
                if (destCol > destEnd) break;
                MyGridSquare sq = this.allPurposeScore.getGridSquare(row, col);
                if (sq == null) continue;
                if (allTracks) {
                    for (MyGridSquare.SubSquare sub : sq.getPoliNotes()) {
                        if (!sub.isVisible()) continue;
                        int savedTr = this.mixer.getCurrentTrackId();
                        this.mixer.setCurrentTrack(sub.getTrack());
                        addNoteAtCell(row, destCol);
                        if (sub.isLinked()) {
                            MyGridSquare dest = this.allPurposeScore.getGridSquare(row, destCol);
                            if (dest != null) linkNoteAtCell(dest);
                        }
                        this.mixer.setCurrentTrack(savedTr);
                    }
                } else {
                    int ch = this.mixer.getCurrentChannelOfCurrentTrack();
                    int tr = this.mixer.getCurrentTrackId();
                    MyGridSquare.SubSquare matchSub = sq.getPoliNotes().stream()
                            .filter(n -> n.getChannel() == ch && n.getTrack() == tr && n.isVisible())
                            .findFirst().orElse(null);
                    if (matchSub == null) continue;
                    addNoteAtCell(row, destCol);
                    if (matchSub.isLinked()) {
                        MyGridSquare dest = this.allPurposeScore.getGridSquare(row, destCol);
                        if (dest != null) linkNoteAtCell(dest);
                    }
                }
            }
        }
    }

    /** Shows track picker then activates PASTE drag mode on next mouse press. */
    public void startPaste() {
        if (clipboard == null || clipboard.isEmpty()) return;
        selectionActive = false;
        pendingPaste = true;
        if (clipboardMultiTrack) {
            // Multi-track: cada nota va al seu track original; no cal triar destí
            this.pasteTr     = -1;
            this.pasteCh     = -1;
            this.pasteDotted = false;
        } else {
            int targetTr = showTrackPickerDialog();
            if (targetTr == -1) {
                pendingPaste = false;
                return;
            }
            this.pasteTr     = targetTr;
            this.pasteCh     = this.mixer.getCurrentChannelOfTrack(targetTr);
            this.pasteDotted = this.mixer.getTrackFromId(targetTr).isDotted();
        }
    }

    public boolean isPendingPaste() { return pendingPaste; }

    public void cancelPaste() {
        if (pasteCurrentRow >= 0) removePasteGhost(pasteCurrentRow, pasteCurrentCol);
        pendingPaste = false;
        dragMode = DragMode.NONE;
        pasteCurrentRow = -1;
        pasteCurrentCol = -1;
    }

    /** Simple track picker using JOptionPane. Returns the chosen track ID, or -1 if cancelled. */
    private int showTrackPickerDialog() {
        int curId = this.mixer.getCurrentTrackId();
        // Skip dialog if current track is drums
        if (curId == this.mixer.getDrumsTrackId()) return curId;

        List<String> nameList = new ArrayList<>();
        List<Integer> idList  = new ArrayList<>();
        List<MyTrack> tracks = this.mixer.getTracks();
        for (int i = 0; i < tracks.size(); i++) {
            nameList.add(tracks.get(i).getName());
            idList.add(i);
        }
        MyTrack chordTrack = this.mixer.getChordTrack();
        if (chordTrack != null && this.allPurposeScore.hasAnyChords()) {
            nameList.add(chordTrack.getName());
            idList.add(this.mixer.getChordTrackId());
        }
        MyTrack drumsTrack = this.mixer.getDrumsTrack();
        if (drumsTrack != null && drumsTrack.getnNotes() > 0) {
            nameList.add(drumsTrack.getName());
            idList.add(this.mixer.getDrumsTrackId());
        }
        // Skip dialog if only one track
        if (nameList.size() <= 1) return curId;

        String[] names = nameList.toArray(new String[0]);
        int defaultIdx = idList.indexOf(curId);
        if (defaultIdx < 0) defaultIdx = 0;
        Object picked = JOptionPane.showInputDialog(
                this.ui,
                I18n.t("paste.trackPicker.message"),
                I18n.t("paste.trackPicker.title"),
                JOptionPane.PLAIN_MESSAGE,
                null,
                names,
                names[defaultIdx]);
        if (picked == null) return -1;
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(picked)) return idList.get(i);
        }
        return curId;
    }

    /** Place the paste ghost (temp) into the grid without track-count or undo recording. */
    private void placePasteGhost(int anchorRow, int anchorCol) {
        if (clipboard == null) return;
        int nKeys = this.allPurposeScore.getnKeys();
        int nCols = this.allPurposeScore.getNumCols();
        for (ClipNote n : clipboard) {
            int row = anchorRow + n.rowOffset;
            int col = anchorCol + n.colOffset;
            if (row < 0 || row >= nKeys || col < 0 || col >= nCols) continue;
            int ch = clipboardMultiTrack ? n.channel : pasteCh;
            int tr = clipboardMultiTrack ? n.trackId : pasteTr;
            MyGridSquare sq = this.allPurposeScore.addNoteToSquare(
                    row, col, 1, Settings.getnRowsSquare(),
                    (MyComponent) allPurposeScore, this, allPurposeScore, this.getCam(),
                    ch, tr, n.velocity, n.visible, n.muted, n.linked, n.dotted);
            sq.updateState();
        }
    }

    /** Remove the paste ghost from the grid. */
    private void removePasteGhost(int anchorRow, int anchorCol) {
        if (clipboard == null) return;
        int nKeys = this.allPurposeScore.getnKeys();
        int nCols = this.allPurposeScore.getNumCols();
        for (int i = clipboard.size() - 1; i >= 0; i--) {
            ClipNote n = clipboard.get(i);
            int row = anchorRow + n.rowOffset;
            int col = anchorCol + n.colOffset;
            if (row < 0 || row >= nKeys || col < 0 || col >= nCols) continue;
            int ch = clipboardMultiTrack ? n.channel : pasteCh;
            int tr = clipboardMultiTrack ? n.trackId : pasteTr;
            this.allPurposeScore.removeNoteFromSquare(row, col, ch, tr);
            MyGridSquare sq = this.allPurposeScore.getGridSquare(row, col);
            if (sq != null) sq.updateState();
        }
    }

    /** Actually place notes at final anchor, track counts included, and create PasteEvent. */
    private void finalizeHardPaste(int anchorRow, int anchorCol) {
        if (clipboard == null || clipboard.isEmpty()) return;
        int nKeys = this.allPurposeScore.getnKeys();
        int nCols = this.allPurposeScore.getNumCols();
        List<ClipNote> placed = new ArrayList<>();
        for (ClipNote n : clipboard) {
            int row = anchorRow + n.rowOffset;
            int col = anchorCol + n.colOffset;
            if (row < 0 || row >= nKeys || col < 0 || col >= nCols) continue;
            int ch = clipboardMultiTrack ? n.channel : pasteCh;
            int tr = clipboardMultiTrack ? n.trackId : pasteTr;
            MyTrack track = this.mixer.getTrackFromId(tr);
            if (track == null) continue;
            track.oneNoteMore();
            MyGridSquare sq = this.allPurposeScore.addNoteToSquare(
                    row, col, 1, Settings.getnRowsSquare(),
                    (MyComponent) allPurposeScore, this, allPurposeScore, this.getCam(),
                    ch, tr, n.velocity, n.visible, n.muted, n.linked, n.dotted);
            sq.updateState();
            if (col + 1 > this.allPurposeScore.getLastColWritten())
                this.allPurposeScore.setLastColWritten(col + 1);
            placed.add(n);
        }
        this.allPurposeScore.updateStopMarker();
        // Enganxa accords (si n'hi ha al clipboard) i registra-ho per a undo
        Map<Integer, Chord> newChordsMap = new java.util.LinkedHashMap<>();
        Map<Integer, Chord> oldChordsMap = new java.util.LinkedHashMap<>();
        if (!clipboardChords.isEmpty()) {
            for (ClipChord cc : clipboardChords) {
                int col = anchorCol + cc.colOffset;
                if (col < 0 || col >= this.allPurposeScore.getNumCols()) continue;
                oldChordsMap.put(col, allPurposeScore.getChordSimbolLine().get(col));
                allPurposeScore.placeChordSymbol(cc.chord, col);
                newChordsMap.put(col, cc.chord);
            }
            redrawChordLine();
        }
        // Enganxa lletres (si n'hi ha al clipboard) i registra-ho per a undo
        List<ClipLyric> newLyricsList = new ArrayList<>();
        List<ClipLyric> oldLyricsList = new ArrayList<>();
        boolean pasteLyrics = !clipboardLyrics.isEmpty()
                && MyDialogs.demanaConfirmacio(
                        I18n.t("paste.lyrics.confirm"),
                        I18n.t("paste.lyrics.title")) == javax.swing.JOptionPane.YES_OPTION;
        if (pasteLyrics) {
            for (ClipLyric cl : clipboardLyrics) {
                int col = anchorCol + cl.colOffset;
                if (col < 0 || col >= this.allPurposeScore.getNumCols()) continue;
                String oldText = myLyrics.getLyric(col, cl.trackId);
                if (oldText != null) {
                    oldLyricsList.add(new ClipLyric(col - anchorCol, cl.trackId, oldText));
                }
                myLyrics.setLyric(col, cl.trackId, cl.text);
                newLyricsList.add(new ClipLyric(col - anchorCol, cl.trackId, cl.text));
            }
            myLyrics.drawFullLyricsInOffscreen();
        }
        if (!placed.isEmpty() || !newChordsMap.isEmpty() || !newLyricsList.isEmpty()) {
            afegirEvent(new PasteEvent(this, placed, anchorRow, anchorCol,
                    pasteCh, pasteTr, clipboardMultiTrack, newChordsMap, oldChordsMap,
                    newLyricsList, oldLyricsList));
        }
        this.needsSaving = true;
    }

    // ── END Copy / Cut / Paste ───────────────────────────────────────────────────

    /** Afegeix una nota al cell (row,col) del track actual i la registra al mouseSequence. */
    private void addNoteAtCell(int row, int col) {
        expandBufferIfNeeded(col);
        MyTrack tr = this.mixer.getCurrentTrack();
        tr.oneNoteMore();
        MyGridSquare sq = this.allPurposeScore.addNoteToSquare(row, col, 1, Settings.getnRowsSquare(),
                (MyComponent) allPurposeScore, this, allPurposeScore, this.getCam(),
                this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(),
                this.mixer.getCurrentTrack().getVelocity(), true, false, false,
                this.mixer.getCurrentTrack().isDotted());
        lastNote = this.allPurposeScore.getGridSquare(row, col);
        mouseSequence.addChange(sq, true,
                this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(),
                this.mixer.getCurrentTrack().getVelocity(),
                sq.isSqVisible(), !sq.isSqAudible(), sq.isSq_is_linked(),
                this.mixer.getCurrentTrack().isDotted());
        if (col + 1 > this.allPurposeScore.getLastColWritten())
            this.allPurposeScore.setLastColWritten(col + 1);
        this.allPurposeScore.updateStopMarker();
    }

    /** Elimina la nota al cell (row,col) del track actual i la registra al mouseSequence. */
    private void removeNoteAtCell(int row, int col) {
        MyGridSquare square = this.allPurposeScore.getGridSquare(row, col);
        if (square == null || !square.isSqVisible()) return;
        MyGridSquare nextSq = square.next();
        MyGridSquare.SubSquare note = this.allPurposeScore.removeNoteFromSquare(row, col,
                this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId());
        if (note == null) return;
        this.allPurposeScore.updateStopMarker();
        mouseSequence.addChange(square, false,
                this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(),
                note.getVelocity(), note.isVisible(), !note.isAudible(), note.isLinked(),
                this.mixer.getCurrentTrack().isDotted());
        if (nextSq != null && nextSq.isSqVisible() && nextSq.isSq_is_linked()) {
            nextSq.unlinkNote(this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(),
                    SoundWithMidi.getCurrentKeyboardVelocity(), false, false, false,
                    this.mixer.getCurrentTrack().isDotted());
            mouseSequence.addLinkChange(nextSq,
                    this.mixer.getCurrentChannelOfCurrentTrack(), this.mixer.getCurrentTrackId(),
                    SoundWithMidi.getCurrentKeyboardVelocity(), true, false,
                    this.mixer.getCurrentTrack().isDotted(), true, false);
        }
    }

    /** Linka la nota d'un square i registra el canvi al mouseSequence per undo. */
    private void linkNoteAtCell(MyGridSquare sq) {
        int ch = this.mixer.getCurrentChannelOfCurrentTrack();
        int tr = this.mixer.getCurrentTrackId();
        if (tremoloActive) {
            // Durant el tremolo no linkem físicament (les notes es mostren com a separades),
            // però marquem la square com a continuació perquè es re-linki en desactivar.
            final int fCh = ch, fTr = tr;
            sq.getPoliNotes().stream()
                .filter(n -> n.getChannel() == fCh && n.getTrack() == fTr)
                .findFirst()
                .ifPresent(n -> n.setFirstSquareOfNote(false));
            return;
        }
        int vel = SoundWithMidi.getCurrentKeyboardVelocity();
        boolean wasMuted = !sq.isSqAudible();
        boolean wasDotted = this.mixer.getCurrentTrack().isDotted();
        sq.linkNote(ch, tr, vel, true, wasMuted, false, wasDotted);
        mouseSequence.addLinkChange(sq, ch, tr, vel, true, wasMuted, wasDotted, false, true);
    }

    /** Deslinka la nota d'un square i registra el canvi al mouseSequence per undo. */
    private void unlinkNoteForUndo(MyGridSquare sq) {
        int ch = this.mixer.getCurrentChannelOfCurrentTrack();
        int tr = this.mixer.getCurrentTrackId();
        int vel = SoundWithMidi.getCurrentKeyboardVelocity();
        boolean wasMuted = !sq.isSqAudible();
        boolean wasDotted = this.mixer.getCurrentTrack().isDotted();
        boolean wasLinked = sq.isSq_is_linked();
        sq.unlinkNote(ch, tr, vel, true, wasMuted, false, wasDotted);
        if (wasLinked) {
            mouseSequence.addLinkChange(sq, ch, tr, vel, true, wasMuted, wasDotted, true, false);
        }
    }

    /** Processa un cell individual durant el drag, segons el dragMode actiu. */
    private void processDragCell(int row, int col) {
        switch (dragMode) {
            case ADD: {
                MyGridSquare sq = this.allPurposeScore.getGridSquare(row, col);
                int curCh = this.mixer.getCurrentChannelOfCurrentTrack();
                int curTr = this.mixer.getCurrentTrackId();
                boolean currentTrackHasNote = sq != null && sq.getPoliNotes().stream()
                    .anyMatch(n -> n.getChannel() == curCh && n.getTrack() == curTr && n.isVisible());
                if (currentTrackHasNote) {
                    if (!sq.isSq_is_linked()) linkNoteAtCell(sq);
                } else {
                    addNoteAtCell(row, col);
                    MyGridSquare added = this.allPurposeScore.getGridSquare(row, col);
                    if (added != null) linkNoteAtCell(added);
                    this.keyboard.play(row);
                }
                break;
            }
            case ERASE:
                removeNoteAtCell(row, col);
                break;
            case EXTEND_PENDING: {
                int curCh2 = this.mixer.getCurrentChannelOfCurrentTrack();
                int curTr2 = this.mixer.getCurrentTrackId();
                if (col > extendStartCol) {
                    dragMode = DragMode.EXTEND_RIGHT;
                    // Link start note to its left neighbour if one exists (join notes).
                    if (extendStartCol > 0) {
                        MyGridSquare startSq = this.allPurposeScore.getGridSquare(extendStartRow, extendStartCol);
                        MyGridSquare leftSq = this.allPurposeScore.getGridSquare(extendStartRow, extendStartCol - 1);
                        if (startSq != null && startSq.isSqVisible() && !startSq.isSq_is_linked()
                                && leftSq != null && leftSq.isSqVisible()) {
                            linkNoteAtCell(startSq);
                        }
                    }
                    MyGridSquare nextSq = this.allPurposeScore.getGridSquare(row, col);
                    boolean nextHasNote = nextSq != null && nextSq.getPoliNotes().stream()
                        .anyMatch(n -> n.getChannel() == curCh2 && n.getTrack() == curTr2 && n.isVisible());
                    if (nextHasNote) {
                        if (!nextSq.isSq_is_linked()) linkNoteAtCell(nextSq);
                        lastNote = nextSq;
                    } else {
                        addNoteAtCell(row, col);
                        MyGridSquare added = this.allPurposeScore.getGridSquare(row, col);
                        if (added != null) linkNoteAtCell(added);
                    }
                } else if (col < extendStartCol) {
                    dragMode = DragMode.EXTEND_LEFT;
                    MyGridSquare startSq = this.allPurposeScore.getGridSquare(extendStartRow, extendStartCol);
                    if (startSq != null && startSq.isSqVisible() && !startSq.isSq_is_linked())
                        linkNoteAtCell(startSq);
                    MyGridSquare nextSq = this.allPurposeScore.getGridSquare(row, col);
                    boolean nextHasNote = nextSq != null && nextSq.getPoliNotes().stream()
                        .anyMatch(n -> n.getChannel() == curCh2 && n.getTrack() == curTr2 && n.isVisible());
                    if (nextHasNote) {
                        boolean wasLinked = nextSq.getPoliNotes().stream()
                            .anyMatch(n -> n.getChannel() == curCh2 && n.getTrack() == curTr2 && n.isVisible() && n.isLinked());
                        if (!wasLinked) linkNoteAtCell(nextSq);  // linka durant el drag; el cap es determina al release
                        firstNote = nextSq;
                        firstNoteWasLinkedBeforeDrag = wasLinked;
                    } else {
                        addNoteAtCell(row, col);  // crea unlinked ✓
                        firstNote = this.allPurposeScore.getGridSquare(row, col);
                        firstNoteWasLinkedBeforeDrag = false;
                    }
                }
                break;
            }
            case EXTEND_RIGHT: {
                int curCh2 = this.mixer.getCurrentChannelOfCurrentTrack();
                int curTr2 = this.mixer.getCurrentTrackId();
                MyGridSquare sq = this.allPurposeScore.getGridSquare(row, col);
                boolean currentTrackHasNote = sq != null && sq.getPoliNotes().stream()
                    .anyMatch(n -> n.getChannel() == curCh2 && n.getTrack() == curTr2 && n.isVisible());
                if (currentTrackHasNote) {
                    if (!sq.isSq_is_linked()) linkNoteAtCell(sq);
                    lastNote = sq;
                } else {
                    addNoteAtCell(row, col);
                    MyGridSquare added = this.allPurposeScore.getGridSquare(row, col);
                    if (added != null) linkNoteAtCell(added);
                }
                break;
            }
            case EXTEND_LEFT: {
                int curCh2 = this.mixer.getCurrentChannelOfCurrentTrack();
                int curTr2 = this.mixer.getCurrentTrackId();
                MyGridSquare sq = this.allPurposeScore.getGridSquare(row, col);
                boolean currentTrackHasNote = sq != null && sq.getPoliNotes().stream()
                    .anyMatch(n -> n.getChannel() == curCh2 && n.getTrack() == curTr2 && n.isVisible());
                boolean isNewLeftmost = (firstNote == null || col < firstNote.getScoreCol());
                if (isNewLeftmost) {
                    // L'anterior firstNote passa a ser continuació (linked)
                    if (firstNote != null && !firstNote.isSq_is_linked()) linkNoteAtCell(firstNote);
                    if (currentTrackHasNote) {
                        boolean wasLinked = sq.getPoliNotes().stream()
                            .anyMatch(n -> n.getChannel() == curCh2 && n.getTrack() == curTr2 && n.isVisible() && n.isLinked());
                        if (!wasLinked) linkNoteAtCell(sq);  // linka durant el drag; el cap es determina al release
                        firstNote = sq;
                        firstNoteWasLinkedBeforeDrag = wasLinked;
                    } else {
                        addNoteAtCell(row, col);  // crea unlinked ✓
                        firstNote = this.allPurposeScore.getGridSquare(row, col);
                        firstNoteWasLinkedBeforeDrag = false;
                    }
                } else {
                    // No és el nou leftmost → linked
                    if (currentTrackHasNote) {
                        boolean isLinked = sq.getPoliNotes().stream()
                            .anyMatch(n -> n.getChannel() == curCh2 && n.getTrack() == curTr2 && n.isVisible() && n.isLinked());
                        if (!isLinked) linkNoteAtCell(sq);
                    } else {
                        addNoteAtCell(row, col);
                        MyGridSquare added = this.allPurposeScore.getGridSquare(row, col);
                        if (added != null) linkNoteAtCell(added);
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    /**
     * On mouse pressed, checks if a XiloKey or a MyGridSquare or a MyButton has
     * been pressed and activates it.
     *
     * @param posX
     * @param posY
     * @param shiftDown true if the Shift key is held (erase mode)
     */
    public void onMousePressed(double posX, double posY, boolean shiftDown, boolean ctrlDown, boolean altDown) {
        clipboardTipVisible = false;
        this.buttons.hideTip();
        lastTipButton = -1;
        lastTipKeyRow = -1;
        /* Exit lyrics edit mode on any click (commits pending text) */
        if (this.myLyrics.isEditMode()) {
            this.myLyrics.exitEditMode();
            this.needsSaving = true;
        }

        /* Check MyButton PRIMER — els botons han de funcionar sempre, fins i tot
           quan hi ha un canvi pendent. A més, els clics sobre botons no han
           d'esborrar la selecció activa (l'usuari pot navegar sense perdre-la). */
        int button = this.buttons.whichButton(posX, posY);
        if (button != -1) {
            this.buttons.onButtonPressed(button);
            this.lastButtonPressed = button;
            this.buttons.setModified(true);
            return;
        }

        /* Qualsevol clic sense Ctrl FORA DELS BOTONS deselecciona la selecció activa.
           Guardem si érem en selecció per consumir el clic a la graella
           sense afegir nota. */
        boolean wasDeselecting = !ctrlDown && selectionActive;
        if (wasDeselecting) {
            selectionActive = false;
            // Redibui immediatament perquè el sombrejat desaparegui abans
            // que qualsevol diàleg (acords, lyrics) s'obri per sobre
            this.allPurposeScore.drawCurrentCamInOffscreen();
            MyNewPanel panel = this.ui.getPanel();
            panel.paintImmediately(0, 0, panel.getWidth(), panel.getHeight());
        }

        /* Col·locar canvi pendent: qualsevol clic a la zona de la càmera col·loca
           el canvi. La columna es calcula des de posX (sense dependre de whichRow),
           de manera que clics al chord symbol line o al score funcionen igual. */
        if (pendingChange != null) {
            int col = this.allPurposeScore.getCol(posX);
            if (col >= 0) {
                placePendingChangeAt(col);
            }
            return; // el clic queda consumit pel canvi pendent
        }

        /* Operació de columna pendent (insert/delete): el clic tria la columna. */
        if (pendingColumnOp != null) {
            int col = this.allPurposeScore.getCol(posX);
            if (col >= 0) executePendingColumnOpAt(col);
            return;
        }

        /* Check chord format button (left keyboard-column area of the chord strip) */
        if (isChordFormatButtonClick(posX, posY)) {
            myChordSymbolLine.cycleDisplayFormat();
            myChordSymbolLine.drawFullChordLineInOffscreen();
            drawFull(true);
            return;
        }

        /* Check chord symbol */
        int chordCol = this.myChordSymbolLine.whichCol(posX, posY);
        // if (Settings.IS_BU) chordCol = -1;
        if (chordCol != -1) {
            Chord oldchord = this.allPurposeScore.getChordSymbol(chordCol);
            Chord chord = this.myChordSymbolLine.enterChord(oldchord);

            if (chord != null && chord != oldchord) { // chord != oldchord means not cancelled
                if (chord.isValidChord()) {
                    this.allPurposeScore.placeChordSymbol(chord, chordCol);
                    afegirEvent(new ChordEvent(this, chordCol, oldchord, chord));
                    this.needsSaving = true;
                    this.myChordSymbolLine.setNeedsDrawing(true);
                    this.myChordSymbolLine.drawFullChordLineInOffscreen();
                }
            } else if (chord == null && oldchord != null) { // empty input = delete
                this.allPurposeScore.removeChordSymbol(chordCol);
                afegirEvent(new ChordEvent(this, chordCol, oldchord, null));
                this.needsSaving = true;
                this.myChordSymbolLine.setNeedsDrawing(true);
                this.myChordSymbolLine.drawFullChordLineInOffscreen();
            }
            this.drawFull(true);
            return;
        }

        /* Check lyrics strip: enter inline edit mode */
        int lyricsCol = this.myLyrics.whichCol(posX, posY);
        if (lyricsCol != -1) {
            if (this.allPurposeScore.getLastColWritten() == 0) {
                MyDialogs.mostraMissatge(I18n.t("myLyrics.noNotes.warning"), I18n.t("myLyrics.label"));
                return;
            }
            int trackId = this.getMixer().getCurrentTrackId();
            this.myLyrics.startEdit(lyricsCol, trackId, posX);
            this.drawFull(true);
            return;
        }

        /* Check XiloKey. */
        int keyId = this.keyboard.whichKey(posX, posY);
        if (keyId != -1) {
            if (this.getAllPurposeScore().getChoice().isSelecting()){
                this.buttons.hideTip();
                this.getAllPurposeScore().getChoice().selectChoice(keyId);
                deactivateSelectingMode();
                this.allPurposeScore.drawCurrentCamInOffscreen();
                this.getUi().getPanel().repinta(true);
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
            if (wasDeselecting) return;  // clic consumit per deseleccionar; no afegir nota

            int col = this.allPurposeScore.whichCol();
            this.needsSaving = true;
            this.lastRowPressed = row;
            this.lastColPressed = col;

            if (pendingPaste) {
                dragMode = DragMode.PASTE;
                pasteCurrentRow = row;
                pasteCurrentCol = col;
                placePasteGhost(pasteCurrentRow, pasteCurrentCol);
                this.keyboard.getKey(row).doNotHighlight(false);
                return;
            }

            if (ctrlDown && !shiftDown) {
                dragMode = DragMode.SELECT;
                selStartRow = row;
                selStartCol = col;
                selEndRow   = row;
                selEndCol   = col;
                selectionActive = true;
                this.turningOn = false;
                mouseSequence = null;
                this.keyboard.getKey(row).doNotHighlight(false);
                return;
            }

            showBeatColTip(col, posX, posY);
            MyGridSquare sq = this.allPurposeScore.getGridSquare(row, col);
            if (ctrlDown && shiftDown && sq != null && sq.isSqVisible()) {
                // MOVE mode: capture the whole note and prepare for live drag
                int headCol = findNoteHeadCol(row, col);
                int tailCol = findNoteTailCol(row, col);
                moveNoteOrigRow = row;
                moveCurrentRow = row;
                moveNoteOrigHeadCol = headCol;
                moveNoteLength = tailCol - headCol + 1;
                moveCurrentHeadCol = headCol;
                moveClickOffset = col - headCol;
                moveCh = this.mixer.getCurrentChannelOfCurrentTrack();
                moveTr = this.mixer.getCurrentTrackId();
                captureNoteData(row, headCol, moveNoteLength, moveCh, moveTr);
                dragMode = DragMode.MOVE;
                mouseSequence = null;
                this.turningOn = false;
            } else {
                mouseSequence = new MouseSequence(this);
                int curCh = this.mixer.getCurrentChannelOfCurrentTrack();
                int curTr = this.mixer.getCurrentTrackId();
                boolean currentTrackHasNote = sq != null && sq.getPoliNotes().stream()
                    .anyMatch(n -> n.getChannel() == curCh && n.getTrack() == curTr && n.isVisible());
                if (shiftDown) {
                    dragMode = DragMode.ERASE;
                    this.turningOn = false;
                    removeNoteAtCell(row, col);
                } else if (currentTrackHasNote) {
                    dragMode = DragMode.EXTEND_PENDING;
                    this.turningOn = false;
                    extendStartRow = row;
                    extendStartCol = col;
                } else {
                    dragMode = DragMode.ADD;
                    this.turningOn = true;
                    addNoteAtCell(row, col);
                    firstNote = this.allPurposeScore.getGridSquare(row, col);
                    lastNote = firstNote;
                    this.keyboard.play(row);
                }
            }
            this.keyboard.getKey(row).doNotHighlight(false);
        }

        /* Check SlideKey. */
        keyId = this.keyboard.whichSlideKey(posX, posY);
        if (keyId != -1) {
            if (this.getAllPurposeScore().getChoice().isSelecting()){
                this.buttons.hideTip();
                this.getAllPurposeScore().getChoice().selectChoice(keyId);
                deactivateSelectingMode();
                this.allPurposeScore.drawCurrentCamInOffscreen();
                this.getUi().getPanel().repinta(true);
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

        /* Check MyButton — ja gestionat al principi del mètode. */
        this.drawFull(true);
    }

    /**
     * On mouse released, checks if a XiloKey or MyGridSquare has been released
     * and stops it.
     *
     * @param posX
     * @param posY
     */
    private void startSelectAutoScroll(int dir) {
        if (selAutoScrollTimer != null && selAutoScrollTimer.isRunning()
                && selAutoScrollDir == dir) return;
        stopSelectAutoScroll();
        selAutoScrollDir = dir;
        selAutoScrollTimer = new Timer(60, e -> doSelectAutoScroll());
        selAutoScrollTimer.start();
    }

    private void stopSelectAutoScroll() {
        if (selAutoScrollTimer != null) {
            selAutoScrollTimer.stop();
            selAutoScrollTimer = null;
        }
    }

    private void doSelectAutoScroll() {
        if (dragMode != DragMode.SELECT) { stopSelectAutoScroll(); return; }
        boolean left = !allPurposeScore.isUseScreenKeyboardRight();
        int initCol = Settings.getInitialCurrentCol(left, allPurposeScore);
        if (selAutoScrollDir > 0) {
            // Scroll dreta: augmentar currentCol mostra columnes posteriors
            // (mateix comportament que onPrevColButtonPressed)
            if (allPurposeScore.getCurrentCol() < allPurposeScore.getNColsBuffer() - 1) {
                allPurposeScore.setCurrentCol(allPurposeScore.getCurrentCol() + 1);
                selEndCol = Math.min(selEndCol + 1, allPurposeScore.getNColsBuffer() - 1);
            }
        } else {
            // Scroll esquerra: disminuir currentCol mostra columnes anteriors
            // (mateix comportament que onNextColButtonPressed)
            if (allPurposeScore.getCurrentCol() > initCol) {
                allPurposeScore.setCurrentCol(allPurposeScore.getCurrentCol() - 1);
                selEndCol = Math.max(selEndCol - 1, 0);
            }
        }
        // Actualitza fila si el ratolí segueix dins del grid verticalment
        double gridLeft  = cam.getScreenX(0);
        double gridRight = gridLeft + cam.getWidth();
        double clampedX  = selAutoScrollDir > 0 ? gridRight - 1 : gridLeft + 1;
        int row = allPurposeScore.whichRow(clampedX, lastSelectDragY);
        if (row != -1) selEndRow = row;
        updateTextOfButtons();
        allPurposeScore.drawCurrentCamInOffscreen();
        getUi().getPanel().repinta(true);
    }

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
            if (dragMode == DragMode.SELECT) {
                stopSelectAutoScroll();
                dragMode = DragMode.NONE;
                this.lastRowPressed = -1;
                this.lastColPressed = -1;
                double tipX = Settings.getScreenWidth() / 2.0;
                double tipY = Settings.getChordFirstRow() * Settings.getRowHeight() + 30;
                this.buttons.showCustomTip(I18n.t("selection.made.tip"), tipX, tipY);
                return;
            }
            if (dragMode == DragMode.PASTE) {
                removePasteGhost(pasteCurrentRow, pasteCurrentCol);
                finalizeHardPaste(pasteCurrentRow, pasteCurrentCol);
                dragMode = DragMode.NONE;
                pendingPaste = false;
                pasteCurrentRow = -1;
                pasteCurrentCol = -1;
                this.lastRowPressed = -1;
                this.lastColPressed = -1;
                refreshAnacrusis();
                return;
            }
            if (dragMode == DragMode.MOVE) {
                int finalRow = moveCurrentRow;
                int finalHeadCol = moveCurrentHeadCol;
                if (finalRow != moveNoteOrigRow || finalHeadCol != moveNoteOrigHeadCol) {
                    MoveNoteEvent event = new MoveNoteEvent(
                            this, moveNoteOrigRow, finalRow, moveNoteOrigHeadCol, finalHeadCol, moveNoteLength,
                            moveCh, moveTr, moveVelocities, moveVisibles, moveMuteds, moveLinkeds, moveDotted);
                    afegirEvent(event);
                    this.needsSaving = true;
                }
                dragMode = DragMode.NONE;
                moveNoteOrigRow = -1;
                moveCurrentRow = -1;
                moveNoteOrigHeadCol = -1;
                moveNoteLength = 0;
                moveCurrentHeadCol = -1;
                moveCh = -1;
                moveTr = -1;
                moveVelocities = null;
                moveVisibles = null;
                moveMuteds = null;
                moveLinkeds = null;
                this.lastRowPressed = -1;
                this.lastColPressed = -1;
                return;
            }
            if (dragMode == DragMode.ADD && firstNote != null) {
                // Unlink the leftmost note (head); when dragging left, lastNote is leftmost.
                MyGridSquare head = (lastNote != null && lastNote.getScoreCol() < firstNote.getScoreCol())
                        ? lastNote : firstNote;
                if (head != firstNote) linkNoteAtCell(firstNote);  // firstNote no és el head → linked
                unlinkNoteForUndo(head);
            } else if (dragMode == DragMode.EXTEND_LEFT && firstNote != null) {
                // Unlink firstNote only if it wasn't already linked before the drag started.
                // If it was already linked (part of a pre-existing chain), keep it linked.
                if (!firstNoteWasLinkedBeforeDrag) {
                    unlinkNoteForUndo(firstNote);
                }
            }

            MyGridSquare wasFirstNote = firstNote;
            DragMode wasDragMode = dragMode;
            firstNote = null;
            lastNote = null;
            firstNoteWasLinkedBeforeDrag = false;

            if (mouseSequence != null && !mouseSequence.isEmpty()) {
                this.afegirEvent(mouseSequence);
                this.needsSaving = true;
            }
            mouseSequence = null;

            // Autocorrect: straighten diagonal ADD drag into a horizontal line.
            if (Settings.isAutoCorrect() && wasDragMode == DragMode.ADD && wasFirstNote != null) {
                int acRow = this.allPurposeScore.whichRow(posX, posY);
                int acCol = this.allPurposeScore.whichCol();
                if (acCol == -1 && this.lastColPressed != -1) {
                    acCol = this.lastColPressed;
                    acRow = this.lastRowPressed;
                }
                if (acCol != -1) {
                    int fcol = wasFirstNote.getScoreCol();
                    int frow = wasFirstNote.getScoreRow();
                    this.undo();
                    mouseSequence = new MouseSequence(this);
                    int step = (acCol >= fcol) ? 1 : -1;
                    MyGridSquare acFirst = null;
                    MyGridSquare acLast = null;
                    for (int c = fcol; c != acCol + step; c += step) {
                        addNoteAtCell(frow, c);
                        acLast = this.allPurposeScore.getGridSquare(frow, c);
                        if (acFirst == null) {
                            acFirst = acLast;
                        } else {
                            linkNoteAtCell(acLast);
                        }
                    }
                    MyGridSquare acHead = (step == 1) ? acFirst : acLast;
                    if (step == -1 && acFirst != null && acFirst != acHead) linkNoteAtCell(acFirst);
                    if (acHead != null) unlinkNoteForUndo(acHead);
                    if (mouseSequence != null && !mouseSequence.isEmpty()) {
                        this.afegirEvent(mouseSequence);
                        this.needsSaving = true;
                    }
                    mouseSequence = null;
                    firstNote = null;
                    lastNote = null;
                }
            }

            dragMode = DragMode.NONE;
            extendStartRow = -1;
            extendStartCol = -1;
            refreshAnacrusis();

            this.keyboard.stop(lastRowPressed);
            this.keyboard.getKey(this.lastRowPressed).doNotHighlight(this.allPurposeScore.isUseScreenKeyboardRight());
            this.lastRowPressed = -1;
            this.lastColPressed = -1;
            this.buttons.hideTip();
            lastTipGridBeatCol = -1;
            lastTipButton = -1;
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
        if (dragMode == DragMode.NONE) return;

        if (dragMode == DragMode.SELECT) {
            lastSelectDragY = posY;
            double gridLeft  = cam.getScreenX(0);
            double gridRight = gridLeft + cam.getWidth();
            boolean outsideLeft  = posX < gridLeft;
            boolean outsideRight = posX > gridRight;

            if (outsideLeft || outsideRight) {
                // Actualitza la fila si el ratolí segueix dins del grid verticalment
                int newRow = this.allPurposeScore.whichRow(
                        outsideLeft ? gridLeft + 1 : gridRight - 1, posY);
                if (newRow != -1) selEndRow = newRow;
                startSelectAutoScroll(outsideRight ? +1 : -1);
            } else {
                stopSelectAutoScroll();
                int newRow = this.allPurposeScore.whichRow(posX, posY);
                int newCol = this.allPurposeScore.whichCol();
                if (newRow == -1 || newCol == -1) return;
                showBeatColTip(newCol, posX, posY);
                selEndRow = newRow;
                selEndCol = newCol;
                this.drawFull(true);
            }
            return;
        }

        if (dragMode == DragMode.PASTE) {
            int newRow = this.allPurposeScore.whichRow(posX, posY);
            if (newRow == -1) newRow = pasteCurrentRow;
            int newCol = this.allPurposeScore.whichCol();
            if (newCol == -1) return;
            showBeatColTip(newCol, posX, posY);
            if (newRow == pasteCurrentRow && newCol == pasteCurrentCol) return;
            removePasteGhost(pasteCurrentRow, pasteCurrentCol);
            pasteCurrentRow = newRow;
            pasteCurrentCol = newCol;
            placePasteGhost(pasteCurrentRow, pasteCurrentCol);
            this.drawFull(true);
            return;
        }

        if (dragMode == DragMode.MOVE) {
            int newRow = this.allPurposeScore.whichRow(posX, posY);
            if (newRow == -1) newRow = moveCurrentRow; // clamp to current if outside grid rows
            int col = this.allPurposeScore.whichCol();
            if (col == -1) return;
            showBeatColTip(col, posX, posY);
            int newHeadCol = col - moveClickOffset;
            newHeadCol = Math.max(0, newHeadCol);
            int gridWidth = this.allPurposeScore.getGrid()[newRow].length;
            newHeadCol = Math.min(newHeadCol, gridWidth - moveNoteLength);
            if (newRow == moveCurrentRow && newHeadCol == moveCurrentHeadCol) return;
            removeTempNote(moveCurrentRow, moveCurrentHeadCol);
            placeTempNote(newRow, newHeadCol);
            moveCurrentRow = newRow;
            moveCurrentHeadCol = newHeadCol;
            this.drawFull(true);
            return;
        }

        /* Check MyGridSquare. */
        int row = this.allPurposeScore.whichRow(posX, posY);
        int col = this.allPurposeScore.whichCol();

        if (row == -1 || col == -1) return;
        showBeatColTip(col, posX, posY);
        if (row == lastRowPressed && col == lastColPressed) return;

        // Interpolate gaps between last and current position
        int lastIntermediateRow = this.lastRowPressed != -1 ? this.lastRowPressed : row;
        if (this.lastRowPressed != -1) {
            if (lastRowPressed != row || lastColPressed != col) {
                lastIntermediateRow = interpolateCells(lastRowPressed, lastColPressed, row, col);
            }
        }

        boolean right = this.allPurposeScore.isUseScreenKeyboardRight();
        if (lastIntermediateRow != row) {
            this.keyboard.stop(lastIntermediateRow);
            this.keyboard.getKey(lastIntermediateRow).doNotHighlight(right);
        }

        this.lastRowPressed = row;
        this.lastColPressed = col;

        processDragCell(row, col);

        this.drawFull(true);
    }

    public void onDoubleClick(double posX, double posY){
        int row = this.allPurposeScore.whichRow(posX, posY);
        int col = this.allPurposeScore.whichCol();
        if (row == -1 || col == -1) return;
        MyGridSquare sq = this.allPurposeScore.getGridSquare(row, col);
        if (sq == null) return;
        int curCh = this.mixer.getCurrentChannelOfCurrentTrack();
        int curTr = this.mixer.getCurrentTrackId();
        boolean hasLinkedNote = sq.getPoliNotes().stream()
            .anyMatch(n -> n.getChannel() == curCh && n.getTrack() == curTr && n.isVisible() && n.isLinked());
        if (!hasLinkedNote) return;
        int vel = SoundWithMidi.getCurrentKeyboardVelocity();
        boolean wasMuted = !sq.isSqAudible();
        boolean wasDotted = this.mixer.getCurrentTrack().isDotted();
        mouseSequence = new MouseSequence(this);
        sq.unlinkNote(curCh, curTr, vel, true, wasMuted, false, wasDotted);
        mouseSequence.addLinkChange(sq, curCh, curTr, vel, true, wasMuted, wasDotted, true, false);
        afegirEvent(mouseSequence);
        mouseSequence = null;
        this.needsSaving = true;
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
    private int lastTipKeyRow = -1;
    private int lastTipGridBeatCol = -1;

    public void onMouseMoved(double posX, double posY) {
        if (clipboardTipVisible) return;
        int button = this.buttons.whichButton(posX, posY);
        if (button != -1) {
            if (button != lastTipButton) {
                this.buttons.hideTip();
                this.buttons.showTip(button, posX, posY);
                lastTipButton = button;
                lastTipKeyRow = -1;
            }
        } else if (isChordFormatButtonClick(posX, posY)) {
            if (lastTipButton != -2) {
                this.buttons.hideTip();
                this.buttons.showCustomTip(I18n.t("myChordSymbolLine.formatBtn.tip"),
                        (int) posX + 5,
                        (int)(posY + Settings.getRowHeight()));
                lastTipButton = -2;
                lastTipKeyRow = -1;
            }
        } else if (myChordSymbolLine.whichCol(posX, posY) != -1) {
            if (lastTipButton != -3) {
                this.buttons.hideTip();
                this.buttons.showCustomTip(I18n.t("myChordSymbolLine.chord.tip"), posX, posY);
                lastTipButton = -3;
                lastTipKeyRow = -1;
            }
        } else {
            int keyRow = this.keyboard.whichKey(posX, posY);
            if (keyRow != -1) {
                if (lastTipButton != -4 || lastTipKeyRow != keyRow) {
                    MyXiloKey key = this.keyboard.getKey(keyRow);
                    this.buttons.hideTip();
                    String keyTip;
                    if (isDrumsMode()) {
                        int drumMidi = ToneRange.getDrumMidi(keyRow);
                        keyTip = (drumMidi >= 0) ? ToneRange.getDrumFullName(drumMidi) : "";
                    } else {
                        MyTrack currentTrack = this.mixer.getCurrentTrack();
                        int dispOff = (currentTrack != null) ? currentTrack.getDisplayOffset() : 0;
                        int realMidi = key.getMidi() - dispOff;
                        String visualName = key.getNoteName();
                        String nameOnly = visualName.replaceAll("-?[0-9]+$", "");
                        int visualOctave = Integer.parseInt(visualName.substring(nameOnly.length()));
                        String realNoteName = nameOnly + (visualOctave - dispOff / 12);
                        keyTip = realNoteName + " (" + realMidi + ")";
                    }
                    this.buttons.showCustomTip(keyTip,
                            (int)(posX + 2 * Settings.getColWidth()), (int) posY);
                    lastTipButton = -4;
                    lastTipKeyRow = keyRow;
                }
            } else if (this.keyboard.whichSlideKey(posX, posY) != -1) {
                if (lastTipButton != -5) {
                    this.buttons.hideTip();
                    this.buttons.showCustomTip(I18n.t("myXiloKey.slide.tip"),
                            (int)(posX + 2 * Settings.getColWidth()), (int) posY);
                    lastTipButton = -5;
                    lastTipKeyRow = -1;
                }
            } else if (this.myLyrics != null && this.myLyrics.contains(posX, posY)) {
                if (lastTipButton != -6) {
                    this.buttons.hideTip();
                    int lyricsBottom = (int)(this.myLyrics.getScreenPosY() + this.myLyrics.getHeight()) + 3;
                    this.buttons.showCustomTip(I18n.t("myLyrics.tip"), (int) posX, lyricsBottom);
                    lastTipButton = -6;
                    lastTipKeyRow = -1;
                }
            } else if (this.allPurposeScore.whichRow(posX, posY) != -1) {
                if (lastTipButton != -7) {
                    this.buttons.hideTip();
                    int gridBottom = (int)(this.allPurposeScore.getScreenPosY() + this.allPurposeScore.getHeight()) + 3;
                    this.buttons.showCustomTip(I18n.t("myGridScore.tip"), (int) posX, gridBottom);
                    lastTipButton = -7;
                    lastTipKeyRow = -1;
                    lastTipGridBeatCol = -1;
                }
            } else {
                if (lastTipButton != -1) {
                    this.buttons.hideTip();
                    lastTipButton = -1;
                    lastTipKeyRow = -1;
                    lastTipGridBeatCol = -1;
                }
            }
        }
        this.drawFull(true);
    }

    /** Returns the last intermediate row processed (or startRow if none). */
    private int interpolateCells(int startRow, int startCol, int endRow, int endCol) {
        boolean right = this.allPurposeScore.isUseScreenKeyboardRight();
        int stepCount = Math.max(Math.abs(endRow - startRow), Math.abs(endCol - startCol));
        int intermediateRow = startRow;
        int intermediateCol = startCol;
        int prevRow;
        for (int i = 1; i < stepCount; i++) {
            prevRow = intermediateRow;
            intermediateRow = startRow + i * (endRow - startRow) / stepCount;
            intermediateCol = startCol + i * (endCol - startCol) / stepCount;
            if (intermediateRow != prevRow) {
                this.keyboard.stop(prevRow);
                this.keyboard.getKey(prevRow).doNotHighlight(right);
            }
            processDragCell(intermediateRow, intermediateCol);
        }
        return intermediateRow;
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
        int metroBufSize = allPurposeScore.getStopCol() + Settings.getnColsBeat() * allPurposeScore.getNumBeatsMeasure() + 2;
        boolean[] metroIsBeat    = new boolean[metroBufSize];
        boolean[] metroIsMeasure = new boolean[metroBufSize];
        allPurposeScore.computeBeatMeasureLines(metroBufSize, metroIsBeat, metroIsMeasure);
        Thread playbackThread = new Thread(() -> {
            // Warmup de l'àudio: si fa més de 30 s que no s'ha reproduït res,
            // el driver d'àudio pot estar en repòs i introduir ~2 compassos de
            // latència inicial. Tocar una nota inaudible (vel=1, canal de percussió)
            // premia el pipeline i el sleep dona temps al driver a despertar-se.
            long idleNanos = System.nanoTime() - lastPlayEndNanos;
            if (lastPlayEndNanos == 0 || idleNanos > 30_000_000_000L) { // > 30 s
                SoundWithMidi.playMetronomeTick(SoundWithMidi.METRONOME_NOTE_WEAK, 1);
                try { Thread.sleep(250); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
            }
            long nextStep = System.nanoTime();
            // long nanosPerStep = (long) MyTempo.getNanosPerSquareGrid(); // o calcula-ho
//            Utilities.printOutWithPriority(true,"MyController::play():");

            while (this.getCam().isPlaying()) {
                int camPBar = cam.getPlayBar();
//                System.out.println(allPurposeScore.getDelay(!allPurposeScore.isUseScreenKeyboardRight()));
                int col = allPurposeScore.getScoreCol(camPBar)+allPurposeScore.getDelay(!allPurposeScore.isUseScreenKeyboardRight());

                boolean played = allPurposeScore.playScoreCol(col);
                if (!played) {
                    stop();
                    buttons.stopPlayButton();
                    this.keyboard.stopAll();
                    SwingUtilities.invokeLater(() -> {
                        updateTextOfButtons();
                        this.getUi().getPanel().repinta(true);
                    });
                    break;
                }

                cam.updateCurrentCol();
                //this.allPurposeScore.setDrawNewCol(true);// potser cal sincronització amb el fil de dibuix
                // applyChangesAt síncron: cal que s'executi abans de getNanosPerSquareGrid()
                // perquè les marques de tempo del changeMap actualitzin playbackTempo a temps.
                applyChangesAt(getEditingCol());
                if (Settings.isMetronome()) {
                    int scoreColAtPlayBar = allPurposeScore.getScoreCol(camPBar);
                    if (scoreColAtPlayBar >= 0 && scoreColAtPlayBar < metroIsBeat.length && metroIsBeat[scoreColAtPlayBar]) {
                        if (metroIsMeasure[scoreColAtPlayBar]) {
                            SoundWithMidi.playMetronomeTick(SoundWithMidi.METRONOME_NOTE_STRONG, 100); // Claves — inici de compàs
                        } else {
                            SoundWithMidi.playMetronomeTick(SoundWithMidi.METRONOME_NOTE_WEAK,   70);  // Low Wood Block — beat
                        }
                    }
                }
                // Resta de la UI asíncrona: no bloquejar el fil de timing.
                SwingUtilities.invokeLater(() -> {
                    updateTextOfButtons();      // crida applyChangesAt de nou (idempotent)
                    getUi().getPanel().repinta(true);
                });

                nextStep += (long) MyTempo.getNanosPerSquareGrid();
                // Sleep fins a ~1.5 ms abans del deadline, després spin-wait precís.
                // Thread.sleep() a Windows arrodoneix sempre cap amunt (biaix +0.5–1 ms/col);
                // el spin-wait final elimina aquest biaix sistemàtic sense cost significatiu.
                long waitNanos = nextStep - System.nanoTime();
                if (waitNanos > 1_500_000L) {
                    try {
                        Thread.sleep((waitNanos - 1_500_000L) / 1_000_000L);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                while (System.nanoTime() < nextStep) {
                    Thread.onSpinWait();
                }
            }
            lastPlayEndNanos = System.nanoTime();
        });
        playbackThread.start();
    }
//  ── old_play (codi comentat) ──────────────────────────────────────────────────

//    public void old_play_desglossat_2() {
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
        this.needsSaving = true;
        this.updateTextOfButtons();
        this.myChordSymbolLine.setNeedsDrawing(true);
        this.myChordSymbolLine.drawFullChordLineInOffscreen();
        this.allPurposeScore.drawFullGridinOffscreen();
        this.getUi().getPanel().repinta(true);
    }

    public boolean isDrumsMode() {
        return drumsMode || mixer.getCurrentChannelOfCurrentTrack() == 9;
    }

    public void setDrums(boolean drumsOn) {
        this.drumsMode = drumsOn;
        if (drumsOn) {
            lastMelodyTrackId = mixer.getCurrentTrackId();
            mixer.setCurrentTrack(mixer.getDrumsTrackId());
        } else {
            mixer.setCurrentTrack(lastMelodyTrackId);
        }
        mixer.refreshMixer();
        updateTextOfButtons();
        this.allPurposeScore.drawCurrentCamInOffscreen();
        redrawChordLine();
    }

    private void showBeatColTip(int scoreCol, double posX, double posY) {
        int[] mbc = this.allPurposeScore.getMeasureAndBeatAt(scoreCol);
        int beatCol = mbc[2] + 1;
        if (lastTipGridBeatCol != beatCol) {
            this.buttons.hideTip();
            this.buttons.showCustomTip(I18n.f("grid.cursor.beatCol", beatCol, Settings.getnColsBeat()),
                    (int)(posX + 15), (int)(posY - 10));
            lastTipGridBeatCol = beatCol;
        }
    }

    private void resetDrumsMode() {
        drumsMode = false;
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
        java.awt.Frame owner = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this.getUi().getPanel());
        dodecagraphone.ui.MyHelpDialog.show(owner, resolveHelpAnchor());
        if (togg != null) togg.setPressed(false);
    }

    private String resolveHelpAnchor() {
        if (this.mixer.isMixerVisible())                          return "mixer";
        if (this.selectionActive)                                 return "selection";
        if (this.allPurposeScore.getChoice().isSelecting())       return "pattern";
        if (this.isDrumsMode())                                   return "mixer";
        return "top";
    }

    public void onTipsButtonPressed(MyButton togg) {
        Settings.setTipsVisible(!togg.isPressed());
        if (togg.isPressed()) {
            this.buttons.hideTip();
        }
    }
    
    public void onExportButtonPressed(MyButton togg) {
        System.out.println("MyController::onExportButtonPressed()");
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

    public void onPrintButtonPressed(MyButton togg) {
        String defaultName = allPurposeScore.getTitle();
        if (defaultName == null || defaultName.isBlank()) defaultName = "partitura";
        defaultName = defaultName.trim().replaceAll("[^\\p{L}\\p{N}\\-._ ]", "_") + ".ddcgr.pdf";
        String fitxer = MyDialogs.seleccionaFitxerEscriptura(null, defaultName, "pdf");
        if (fitxer == null || fitxer.isBlank()) {
            if (togg != null) togg.setPressed(false);
            return;
        }
        File file = new File(fitxer);
        if (file.exists()) {
            int resposta = JOptionPane.showConfirmDialog(
                    null,
                    I18n.t("file.overwrite"),
                    I18n.t("MyController.dialog.confirm.title"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (resposta == JOptionPane.NO_OPTION) {
                if (togg != null) togg.setPressed(false);
                return;
            }
        }
        try {
            new DodecagramPdfPrinter(this).print(file);
        } catch (Exception ex) {
            MyDialogs.mostraError(
                    I18n.f("print.error", ex.getMessage()),
                    I18n.t("MyController.dialog.error.title"));
            System.err.println("PDF error: " + ex.getMessage());
        }
        if (togg != null) togg.setPressed(false);
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
        if (cam.isPlaying()) { togg.setPressed(false); return; }
        int old_key = this.allPurposeScore.getMidiKey();
        int new_key = this.getNextKey();
        this.transpose(new_key - old_key);
        if (this.exercisesOn) {
            this.resetExercise();
        }
//        System.out.println("MyController::onNextKeyButtonPressed: (id) " + togg.getId() + " " + this.allPurposeScore.getMidiKey());
    }

    public void onPrevKeyButtonPressed(MyButton togg) {
        if (cam.isPlaying()) { togg.setPressed(false); return; }
//        System.out.println("MyController::onPrevKeyButtonPressed: (id) " + togg.getId());
        int old_key = this.allPurposeScore.getMidiKey();
        int new_key = this.getPrevKey();
        this.transpose(new_key - old_key);
        if (this.exercisesOn) {
            this.resetExercise();
        }
    }

    public void onResetKeyButtonPressed(MyButton togg) {
        if (cam.isPlaying()) { togg.setPressed(false); return; }
        // Botó de tonalitat: demana la nova to i activa el workflow de canvi pendent.
        togg.setPressed(false);
        String to = MyDialogs.mostraInputDialog(I18n.t("keyButton.prompt"), I18n.t("keyButton.title"));
        if (to == null || to.isBlank()) return;
        char mode = ToneRange.getScaleMode(to);
        int newMidiKey = ToneRange.getMidiKey(to);
        if (mode == ' ' || newMidiKey < 0) {
            MyDialogs.mostraError(
                I18n.f("keyButton.invalidInput", to),
                I18n.t("MyController.dialog.error.title"));
            return;
        }
        int oldMidiKey = allPurposeScore.getMidiKey();
        int step = newMidiKey - oldMidiKey;
        MyGridScore.ScoreChange sc = new MyGridScore.ScoreChange();
        sc.midiKey   = newMidiKey;
        sc.scaleMode = mode;
        setPendingChange(sc, I18n.t("scoreChange.key"));
        pendingTransposeStep = step;
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
        togg.setPressed(false);
        String input = (String) javax.swing.JOptionPane.showInputDialog(
                this.getUi(),
                I18n.t("goto.measure.prompt"),
                I18n.t("goto.measure.title"),
                javax.swing.JOptionPane.QUESTION_MESSAGE,
                null, null, null);
        if (input == null) return;
        try {
            int targetMeasure = Integer.parseInt(input.trim());
            int col = allPurposeScore.getFirstColOfMeasure(targetMeasure);
            if (col >= 0) {
                navigateToScoreCol(col);
                allPurposeScore.drawCurrentCamInOffscreen();
                getUi().getPanel().repinta(true);
            }
        } catch (NumberFormatException ex) {
            // entrada invàlida, ignorem
        }
    }

    public void onVolumeButtonPressed(MyButton togg) {
        if (cam.isPlaying()) return;
        this.buttons.hideTip();
        togg.setPressed(false);
        int currentVol = this.getMixer().getCurrentTrack().getVelocity();
        String input = MyDialogs.mostraInputDialog(
                I18n.t("MyController.onVolumeButtonPressed.prompt"),
                I18n.t("MyController.onVolumeButtonPressed.title"),
                String.valueOf(currentVol)
        );
        if (input == null) return;
        try {
            int requested = Integer.parseInt(input.trim());
            int clamped = Math.max(0, Math.min(127, requested));
            int trackId = this.getMixer().getCurrentTrackId();
            this.getMixer().getCurrentTrack().setVelocity(clamped);
            this.buttons.updateVolumeButton("" + clamped);
            MyGridScore.ScoreChange sc = new MyGridScore.ScoreChange();
            sc.trackVelocities.put(trackId, clamped);
            setPendingChange(sc, I18n.t("scoreChange.volume"));
        } catch (NumberFormatException e) {
            MyDialogs.mostraError(
                    I18n.f("MyController.onVolumeButtonPressed.invalidInput", input),
                    I18n.t("MyController.dialog.error.title")
            );
        }
    }

    public void onFirstPageButtonPressed(MyButton togg) {
        this.stop();
        this.cam.reset();
        this.buttons.stopPlayButton();
//        this.drawFull(true);
    }

    public void onScreenResizedQuick() {
        if (allPurposeScore == null) return;
        repositionAllComponents();
        setNeedsDrawing(true);
    }

    private void repositionAllComponents() {
        if (allPurposeScore == null) return;
        boolean left = !allPurposeScore.isUseScreenKeyboardRight();
        screen.setDimensions(0, 0, (int) Settings.getnColsScreen(), (int) Settings.getnRowsScreen());
        keyboard.setDimensions(Settings.getKeyboardFirstCol(left), Settings.getKeyboardFirstRow(),
                               Settings.getnColsKeyboard(), Settings.getnRowsKeyboard());
        cam.setDimensions(Settings.getCamFirstCol(left), Settings.getCamFirstRow(),
                          Settings.getnColsCam(), Settings.getnRowsCam());
        allPurposeScore.setDimensions(Settings.getScoreFirstCol() + allPurposeScore.getDelay(left),
                                      Settings.getScoreFirstRow(), Settings.getnColsScore(), Settings.getnRowsScore());
        statusLine.setDimensions(Settings.getStatusFirstCol(), Settings.getStatusFirstRow(),
                                 Settings.getnColsStatus(), Settings.getnRowsStatus());
        myChordSymbolLine.setDimensions(Settings.getChordFirstCol(), Settings.getChordFirstRow(),
                                        Settings.getnColsChord(), Settings.getnRowsChord());
        myLyrics.setDimensions(Settings.getLyricsFirstCol(), Settings.getLyricsFirstRow(),
                               Settings.getnColsLyrics(), Settings.getnRowsLyrics());
        buttons.resetButtons(Settings.getFirstColControl(), Settings.getControlFirstRow(),
                             Settings.getnColsControl(), Settings.getnRowsControl());
    }

    public void onScreenResized() {
        if (allPurposeScore == null) {
            setNeedsDrawing(true);
            return;
        }
        repositionAllComponents();
        allPurposeScore.initOffscreen();
        myChordSymbolLine.initOffscreen();
        myLyrics.initOffscreen();
        allPurposeScore.drawFullGridinOffscreen();
        myChordSymbolLine.drawFullChordLineInOffscreen();
        myLyrics.drawFullLyricsInOffscreen();
        setNeedsDrawing(true);
        if (getUi() != null && getUi().getPanel() != null) {
            getUi().getPanel().repinta(true);
        }
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
        this.updateTextOfButtons();
        this.drawFull(true);
    }

    public void onPrevPageButtonPressed(MyButton togg) {
        this.stop();
        this.cam.prevPage();
        this.buttons.stopPlayButton();
        this.updateTextOfButtons();
        this.drawFull(true);
    }

    public void onPrevColButtonPressed(MyButton togg) {
        if (allPurposeScore.getCurrentCol() < allPurposeScore.getNColsBuffer()) {
            allPurposeScore.setCurrentCol(allPurposeScore.getCurrentCol() + 1);
            updateTextOfButtons();
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
                    if (allPurposeScore.getCurrentCol() < allPurposeScore.getNColsBuffer()) {
                        allPurposeScore.setCurrentCol(allPurposeScore.getCurrentCol() + 1);
                        updateTextOfButtons();
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
//        System.out.println("MyController:onNextCol() currentCol = "+this.allPurposeScore.getCurrentCol());
        boolean left = !this.getAllPurposeScore().isUseScreenKeyboardRight();
        if (allPurposeScore.getCurrentCol() > (Settings.getInitialCurrentCol(left,allPurposeScore))) {
            allPurposeScore.setCurrentCol(allPurposeScore.getCurrentCol() - 1);
            updateTextOfButtons();
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
                        updateTextOfButtons();
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
        if (cam.isPlaying()) return;
        this.buttons.hideTip();
        togg.setPressed(false);
        String input = MyDialogs.mostraInputDialog(
                I18n.t("MyController.onTempoButtonPressed.prompt"),
                I18n.t("MyController.onTempoButtonPressed.title"),
                String.valueOf(MyTempo.getPlaybackTempo())
        );
        if (input == null) {
            return;
        }
        try {
            int requested = Integer.parseInt(input);
            // Clamp the value without touching MyTempo state (preserves playbackTempo).
            int clamped = Math.max(0, Math.min(Settings.MAX_BPM, requested));
            MyGridScore.ScoreChange sc = new MyGridScore.ScoreChange();
            sc.tempo = clamped;
            setPendingChange(sc, I18n.t("scoreChange.tempo"));
        } catch (NumberFormatException e) {
            MyDialogs.mostraError(
                    I18n.f("MyController.onTempoButtonPressed.invalidInput", input),
                    I18n.t("MyController.dialog.error.title")
            );
        }
    }
    
    public void onFasterButtonPressed(MyButton togg) {
        // Vel+: modifica el playbackTempo (velocitat de reproducció en viu).
        // El botó mostra el playbackTempo en tot moment.
        MyTempo.faster();
        this.buttons.updateTempoButton("" + MyTempo.getPlaybackTempo());
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
                    this.buttons.updateTempoButton("" + MyTempo.getPlaybackTempo());
                    Thread.sleep(Settings.BUTTON_REPEAT_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        replicador.start();
    }

    public void onLouderButtonPressed(MyButton togg) {
        int vol = this.getMixer().louder();
        this.buttons.updateVolumeButton("" + vol);
        this.mixer.refreshMixer();
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
                    int v = this.getMixer().louder();
                    this.buttons.updateVolumeButton("" + v);
                    this.mixer.refreshMixer();
                    Thread.sleep(Settings.BUTTON_REPEAT_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        replicador.start();
    }

    public void onQuieterButtonPressed(MyButton togg) {
        int vol = this.getMixer().quieter();
        this.buttons.updateVolumeButton("" + vol);
        this.mixer.refreshMixer();
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
                    int v = this.getMixer().quieter();
                    this.buttons.updateVolumeButton("" + v);
                    this.mixer.refreshMixer();
                    Thread.sleep(Settings.BUTTON_REPEAT_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        replicador.start();
    }

    public void onSlowerButtonPressed(MyButton togg) {
        // Vel-: modifica el playbackTempo (velocitat de reproducció en viu).
        // El botó mostra el playbackTempo en tot moment.
        MyTempo.slower();
        this.buttons.updateTempoButton("" + MyTempo.getPlaybackTempo());
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
                    this.buttons.updateTempoButton("" + MyTempo.getPlaybackTempo());
                    Thread.sleep(Settings.BUTTON_REPEAT_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
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
        updateWindowTitle();
        this.statusLine.setText(scoreStatusText());
    }

    public void onAuthorButtonPressed(MyButton togg) {
        String author = this.allPurposeScore.getAuthor();
        author = MyDialogs.mostraInputDialog(I18n.t("MyController.score.author.prompt"), I18n.t("MyController.score.author.label"), author);
        if (author != null) {
            this.allPurposeScore.setAuthor(author);
        }
        togg.setPressed(false);
        updateWindowTitle();
        this.statusLine.setText(scoreStatusText());
    }

    public void onDescriptionButtonPressed(MyButton togg) {
        String descr = this.allPurposeScore.getDescription();
        descr = MyDialogs.mostraInputDialog(I18n.t("MyController.score.description.prompt"), I18n.t("MyController.score.description.label"), descr);
        if (descr != null) {
            this.allPurposeScore.setDescription(descr);
        }
        togg.setPressed(false);
        this.statusLine.setText(scoreStatusText());
    }

    public void onTransposeDownButtonPressed(MyButton togg) {
        if (cam.isPlaying()) { togg.setPressed(false); return; }
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
        if (cam.isPlaying()) { togg.setPressed(false); return; }
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
        this.updateTextOfButtons();
        MyNewPanel panel = getUi() != null ? getUi().getPanel() : null;
        if (panel != null) {
            MyNewPanel.setRepintaWasCalled(true);
            panel.paintImmediately(0, 0, panel.getWidth(), panel.getHeight());
        }
        this.loadScore("");
        togg.setPressed(false);
    }

    /** Mostra un diàleg no modal que demana a l'usuari que cliqui la nota arrel al teclat. */
    private void showSelectingDialog() {
        if (selectingDialog != null) selectingDialog.dispose();
        javax.swing.JDialog dlg = new javax.swing.JDialog(
                this.getUi(), I18n.t("choice.selectingDialog.title"), false);
        dlg.setDefaultCloseOperation(javax.swing.JDialog.DO_NOTHING_ON_CLOSE);
        javax.swing.JLabel label = new javax.swing.JLabel(
                I18n.t("buttonLayout.SelectChoiceButton.selectingTip"),
                javax.swing.SwingConstants.CENTER);
        label.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 24, 8, 24));
        javax.swing.JButton btnCancel = new javax.swing.JButton(I18n.t("choice.selectingDialog.cancel"));
        btnCancel.addActionListener(ev -> {
            deactivateSelectingMode();
            if (selectingDialog != null) { selectingDialog.dispose(); selectingDialog = null; }
            allPurposeScore.drawCurrentCamInOffscreen();
            getUi().getPanel().repinta(true);
        });
        javax.swing.JPanel contentPanel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 8));
        contentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 24, 16, 24));
        contentPanel.add(label, java.awt.BorderLayout.CENTER);
        contentPanel.add(btnCancel, java.awt.BorderLayout.SOUTH);
        dlg.add(contentPanel);
        dlg.pack();
        dlg.setLocationRelativeTo(this.getUi());
        dlg.setVisible(true);
        selectingDialog = dlg;
    }

    /** Tanca el diàleg de selecció d'arrel si estava obert. */
    private void closeSelectingDialog() {
        if (selectingDialog != null) {
            selectingDialog.dispose();
            selectingDialog = null;
        }
    }

    public void deactivateSelectingMode() {
        MyChoice choice = this.allPurposeScore.getChoice();
        choice.setSelecting(false);
        MyButton btn = this.buttons.getSelectChoiceButton();
        if (btn != null) btn.setPressed(false);
        this.allPurposeScore.setGridColorsHaveChanged(true);
        closeSelectingDialog();
    }

    public void onSelectChoiceButtonPressed(MyButton togg) {
        MyChoice choice = this.allPurposeScore.getChoice();
        if (choice.isSelecting()) {
            deactivateSelectingMode();
        } else {
            boolean done = choice.showChoiceDialog();
            if (!done) {
                choice.setSelecting(true);
                togg.setPressed(true);
                showSelectingDialog();
            } else {
                togg.setPressed(false);
                this.allPurposeScore.drawCurrentCamInOffscreen();
                this.getUi().getPanel().repinta(true);
            }
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

    public void onFitAnacrusisButtonPressed(MyButton togg) {
        if (togg.isPressed()) {
            detectAnacrusis();
        }
        Settings.setFitAnacrusis(togg.isPressed());
        this.allPurposeScore.setFitAnacrusisScore(togg.isPressed());
        this.allPurposeScore.drawFullGridinOffscreen();
        this.myChordSymbolLine.drawFullChordLineInOffscreen();
        this.myLyrics.setNeedsDrawing(true);
        this.myLyrics.drawFullLyricsInOffscreen();
        this.drawFull(true);
    }

    public void onMetronomeButtonPressed(MyButton togg) {
        Settings.setMetronome(togg.isPressed());
    }

    public void onTremoloButtonPressed(MyButton togg) {
        if (togg.isPressed()) {
            activateTremolo();
        } else {
            deactivateTremolo();
        }
        this.allPurposeScore.drawFullGridinOffscreen();
        this.drawFull(true);
    }

    private void activateTremolo() {
        tremoloActive = true;
        int ch = this.mixer.getCurrentChannelOfCurrentTrack();
        int tr = this.mixer.getCurrentTrackId();
        int nKeys = this.allPurposeScore.getnKeys();
        int nCols = this.allPurposeScore.getLastColWritten();
        for (int col = 0; col < nCols; col++) {
            for (int row = 0; row < nKeys; row++) {
                MyGridSquare sq = this.allPurposeScore.getGridSquare(row, col);
                if (sq == null) continue;
                final int fCh = ch, fTr = tr;
                java.util.Optional<MyGridSquare.SubSquare> noteOpt = sq.getPoliNotes().stream()
                    .filter(n -> n.getChannel() == fCh && n.getTrack() == fTr)
                    .findFirst();
                if (!noteOpt.isPresent()) continue;
                MyGridSquare.SubSquare note = noteOpt.get();
                // Assignem isFirstSquareOfNote EXPLÍCITAMENT segons l'estat real is_linked:
                // addNoteAtCell sempre crea amb is_linked=false; linkNoteAtCell linka després
                // sense tocar isFirstSquareOfNote, de manera que el constructor no és suficient.
                if (note.isLinked()) {
                    note.setFirstSquareOfNote(false); // continuació: marcar com a no-primera
                    sq.unlinkNote(ch, tr, 0, true, false, false, false);
                } else {
                    note.setFirstSquareOfNote(true); // inici de nota o nota aïllada
                }
            }
        }
    }

    private void deactivateTremolo() {
        tremoloActive = false;
        int ch = this.mixer.getCurrentChannelOfCurrentTrack();
        int tr = this.mixer.getCurrentTrackId();
        int nKeys = this.allPurposeScore.getnKeys();
        int nCols = this.allPurposeScore.getLastColWritten();
        for (int col = 0; col < nCols; col++) {
            for (int row = 0; row < nKeys; row++) {
                MyGridSquare sq = this.allPurposeScore.getGridSquare(row, col);
                if (sq == null) continue;
                final int fCh = ch, fTr = tr;
                java.util.Optional<MyGridSquare.SubSquare> noteOpt = sq.getPoliNotes().stream()
                    .filter(n -> n.getChannel() == fCh && n.getTrack() == fTr)
                    .findFirst();
                if (!noteOpt.isPresent()) continue;
                MyGridSquare.SubSquare note = noteOpt.get();
                // Re-linka si és continuació (isFirstSquareOfNote=false, fixat al constructor)
                // Cobreix tant notes originals com notes noves afegides per drag durant el tremolo
                if (!note.isFirstSquareOfNote() && col > 0) {
                    MyGridSquare prev = this.allPurposeScore.getGridSquare(row, col - 1);
                    if (prev != null && prev.getPoliNotes().stream()
                            .anyMatch(n -> n.getChannel() == fCh && n.getTrack() == fTr)) {
                        sq.linkNote(ch, tr, 0, true, false, false, false);
                    }
                }
            }
        }
    }

    public boolean isTremoloActive() {
        return tremoloActive;
    }

    private void resetTremolo() {
        tremoloActive = false;
    }

    /** Detecta anacrusa i actualitza el botó de compàs si el resultat ha canviat. */
    private void refreshAnacrusis() {
        boolean before = Settings.isHasAnacrusis();
        detectAnacrusis();
        if (Settings.isHasAnacrusis() != before) {
            int[] mb = allPurposeScore.getMeasureAndBeatAt(getEditingCol());
            this.buttons.updatePageNumButton(mb[0] + " (" + mb[1] + ")");
        }
    }

    public void detectAnacrusis() {
        if (this.allPurposeScore.getLastColWritten() == 0) {
            Settings.setHasAnacrusis(false);
            return;
        }
        int beatCols = Settings.getnColsBeat();
        int nKeys = this.allPurposeScore.getnKeys();
        java.util.List<dodecagraphone.model.component.MyGridSquare.SubSquare> firstBeat =
            this.allPurposeScore.getNotesOfRegion(0, 0, nKeys - 1, beatCols - 1);
        Settings.setHasAnacrusis(firstBeat.isEmpty());
    }

    private MeterData inputMeterDialog(MeterData defaultValue) {
        Component parentComponent = this.getUi();
        int currentNMeasuresCam = Settings.getnMeasuresCam();
        return MeterDialog.show(
                parentComponent,
                defaultValue.meterType,
                defaultValue.meterPattern,
                "",
                currentNMeasuresCam
        );
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
        int newBeatFigure    = allPurposeScore.getBeatFigure();
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
        sc.beatFigure    = newBeatFigure;
        sc.nColsQuarter  = newNColsQuarter;
        sc.nColsBeat     = newNColsBeat;
        sc.nMeasuresCam  = selection.nMeasuresCam;
        // NO canviem Settings.nMeasuresCam aquí: ho farà applyChangesAt quan el canvi
        // es col·loqui. Canviar-lo ara desincronitzaria getColWidth() de cam.nCols.
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
            MyTrack currentTrack = this.mixer.getCurrentTrack();
            if (currentTrack != null) {
                int offset = InstrumentRange.calcDisplayOffset(newInstr, ToneRange.getLowestMidi(), ToneRange.getHighestMidi());
                currentTrack.setDisplayOffset(offset);
            }
        }
        this.mixer.refreshMixer();
        this.updateTextOfButtons();
    }

    public void changeCurrentChannelOfCurrentTrack() {
        // this.mixer.changeCurrentChannelOfCurrentTrack();
        throw new IllegalStateException("changeCurrentChannelOfCurrentTrack not implemented");
    }

    private void updateWindowTitle() {
        String base = this.getUi().getVersion();
        String title  = this.allPurposeScore != null ? this.allPurposeScore.getTitle()  : "";
        String author = this.allPurposeScore != null ? this.allPurposeScore.getAuthor() : "";
        boolean hasTitle  = title  != null && !title.trim().isEmpty();
        boolean hasAuthor = author != null && !author.trim().isEmpty();
        String suffix = "";
        if (hasTitle && hasAuthor) suffix = " — " + title + " (" + author + ")";
        else if (hasTitle)         suffix = " — " + title;
        else if (hasAuthor)        suffix = " — (" + author + ")";
        this.getUi().setTitle(base + suffix);
    }

    private String scoreStatusText() {
        String title  = allPurposeScore.getTitle();
        String author = allPurposeScore.getAuthor();
        String descr  = allPurposeScore.getDescription();
        boolean hasTitle  = title  != null && !title.trim().isEmpty();
        boolean hasAuthor = author != null && !author.trim().isEmpty();
        boolean hasDescr  = descr  != null && !descr.trim().isEmpty();
        StringBuilder sb = new StringBuilder();
        if (hasTitle)  sb.append(title);
        if (hasAuthor) sb.append(" (").append(author).append(")");
        if (hasDescr) {
            if (sb.length() > 0) sb.append(": ");
            sb.append(descr);
        }
        return sb.toString();
    }

    public final void updateTextOfButtons() {
        updateWindowTitle();

        // Aplica els canvis de paràmetres registrats a la columna actual
        applyChangesAt(getEditingCol());

        // Detecta anacrusa automàticament (primer beat buit → compàs 0)
        if (allPurposeScore != null) detectAnacrusis();

        // Mostra el número de compàs i el beat actual, tenint en
        // compte els canvis de compàs registrats al changeMap.
        int editCol = getEditingCol();
        int[] mb = allPurposeScore.getMeasureAndBeatAt(editCol);
        String pageNo = mb[0] + " (" + mb[1] + ")";
        this.buttons.updatePageNumButton(pageNo);
        String tempo = "" + MyTempo.getPlaybackTempo();
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
            instr = (channel == 9)
                    ? I18n.t("mixer.drumsTrack")
                    : SoundWithMidi.getInstrumentMnemonic(SoundWithMidi.getInstrumentInChannel(channel));
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
    /** Finestra emergent no modal que es mostra mentre hi ha un canvi pendent. */
    private javax.swing.JDialog pendingChangeDialog = null;
    /** Finestra emergent no modal que es mostra mentre s'espera que l'usuari cliqui l'arrel del patró. */
    private javax.swing.JDialog selectingDialog = null;

    private void setPendingChange(MyGridScore.ScoreChange change, String description) {
        setPendingChange(change, description, null);
    }

    /**
     * @param onPlaceAtStart callback opcional executat quan l'usuari prem "A l'inici"
     *        en lloc de l'acció per defecte. Si és null s'usa placePendingChangeAt(0).
     */
    private void setPendingChange(MyGridScore.ScoreChange change, String description, Runnable onPlaceAtStart) {
        this.pendingChange = change;
        this.pendingTransposeStep = 0;
        // Tanca qualsevol diàleg pendent anterior
        if (pendingChangeDialog != null) {
            pendingChangeDialog.dispose();
        }
        // Diàleg no modal: no bloqueja l'usuari, desapareix quan es col·loca el canvi
        javax.swing.JDialog dlg = new javax.swing.JDialog(
                this.getUi(), I18n.t("scoreChange.clickToPlace.title"), false);
        dlg.setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);
        javax.swing.JLabel label = new javax.swing.JLabel(
                I18n.f("scoreChange.clickToPlace", description),
                javax.swing.SwingConstants.CENTER);
        label.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 24, 8, 24));
        // Botó "Inici": col·loca el canvi a la columna 0. És el botó per defecte,
        // de manera que prémer Enter el dispara.
        javax.swing.JButton btnInici = new javax.swing.JButton(I18n.t("scoreChange.placeAtStart"));
        btnInici.addActionListener(ev -> {
            if (onPlaceAtStart != null) {
                onPlaceAtStart.run();
            } else {
                placePendingChangeAt(0);
            }
        });
        javax.swing.JPanel contentPanel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 8));
        contentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 24, 16, 24));
        contentPanel.add(label, java.awt.BorderLayout.CENTER);
        contentPanel.add(btnInici, java.awt.BorderLayout.SOUTH);
        dlg.add(contentPanel);
        dlg.getRootPane().setDefaultButton(btnInici);
        dlg.pack();
        dlg.setLocationRelativeTo(this.getUi());
        dlg.setVisible(true);
        pendingChangeDialog = dlg;
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
        // Snap a l'inici de compàs per a canvis de tempo, tonalitat i compàs.
        // Per a canvis de volum purs (trackVelocities), s'aplica a la columna exacta.
        boolean isVolumeOnly = pendingChange.tempo == null && pendingChange.midiKey == null
                && pendingChange.scaleMode == null && pendingChange.nBeatsMeasure == null
                && pendingChange.beatFigure == null && pendingChange.nColsQuarter == null
                && pendingChange.nColsBeat == null
                && !pendingChange.trackVelocities.isEmpty();
        if (!isVolumeOnly) {
            col = allPurposeScore.getFirstColOfCurrentMeasure(col);
        }
        // Si hi ha un canvi de to pendent, preguntar si cal transposar les notes.
        if (pendingTransposeStep != 0) {
            int step = pendingTransposeStep;
            pendingTransposeStep = 0;
            int res = MyDialogs.demanaConfirmacio(
                    I18n.t("keyButton.transposeConfirm"),
                    I18n.t("keyButton.transposeConfirm.title"));
            if (res == javax.swing.JOptionPane.YES_OPTION) {
                allPurposeScore.transpose(step);
            } else {
                // Les notes no es transposen, però el choice sí segueix la nova tonalitat
                allPurposeScore.getChoice().transposeChoice(step);
                allPurposeScore.updateStripsNKeyboard();
            }
        }
        allPurposeScore.setScoreChange(col, pendingChange);
        // Reseteja playbackTempo només si la marca és a la posició actual o abans;
        // si és posterior al playbar el tempo en viu no canvia fins que s'hi arriba.
        if (pendingChange.tempo != null && col <= getEditingCol()) {
            MyTempo.setTempo(pendingChange.tempo);
        }
        pendingChange = null;
        // Si el canvi és a la columna 0 actualitzem també la base de timing i la doble barra
        if (col == 0) {
            allPurposeScore.freezeBaseTimingParams();
            // Sincronitza numBeatsMeasure amb el nou compàs base abans de calcular stopCol
            // (freezeBaseTimingParams no actualitza numBeatsMeasure, ho fa applyChangesAt,
            // però applyChangesAt s'executa més tard, dins updateTextOfButtons).
            // Patró idèntic al de loadScore (línies 3903-3913).
            MyGridScore.ScoreChange sc0bm = allPurposeScore.getEffectiveChange(0);
            if (sc0bm != null) {
                if (sc0bm.nBeatsMeasure != null) allPurposeScore.setNumBeatsMeasure(sc0bm.nBeatsMeasure);
                if (sc0bm.beatFigure    != null) allPurposeScore.setBeatFigure(sc0bm.beatFigure);
            }
            allPurposeScore.updateStopMarker();
        }
        // Tanca el diàleg informatiu
        if (pendingChangeDialog != null) {
            pendingChangeDialog.dispose();
            pendingChangeDialog = null;
        }
        needsSaving = true;
        // Redibuixa la franja d'acords (nova marca) i la graella (línies de beat/compàs)
        this.myChordSymbolLine.setNeedsDrawing(true);
        this.myChordSymbolLine.drawFullChordLineInOffscreen();
        this.cam.drawFullCamInOffscreen();
        this.statusLine.setText(scoreStatusText());
        this.updateTextOfButtons();
        // applyChangesAt (cridat des d'updateTextOfButtons) pot haver canviat nMeasuresCam.
        // Si getnColsCam() difereix de cam.getnCols(), redimensionem la càmera.
        // Nota: freezeBaseTimingParams (per col==0) fa el resize des de Settings.nBeatsMeasure
        // base; aquí resolem el cas general (col>0 i/o nMeasuresCam canviat).
        int newNCols = Settings.getnColsCam();
        if (cam != null && cam.getnCols() != newNCols) {
            int firstColToDraw = Math.max(0, allPurposeScore.getCurrentCol() - cam.getnCols());
            cam.setnCols(newNCols);
            allPurposeScore.setCurrentCol(firstColToDraw + newNCols);
            allPurposeScore.refreshFixedColsPerPage();
            // Redibuixa els offscreens amb el nou layout
            allPurposeScore.drawFullGridinOffscreen();
            redrawChordLine();
        }
        this.drawFull(true);
        return true;
    }

    /**
     * Col·loca el canvi pendent a la columna del playBar (editingCol).
     * Es crida quan l'usuari prem Enter mentre hi ha un canvi pendent.
     *
     * @return true si hi havia un canvi pendent i s'ha col·locat
     */
    public boolean placePendingChangeAtPlayBar() {
        if (pendingChange == null) return false;
        return placePendingChangeAt(getEditingCol());
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
        // Apliquem sempre un valor (el del canvi o el per defecte), igual que fem amb
        // el compàs, perquè tornar enrere restauri els valors anteriors a la marca.
        // Fora de reproducció: setTempo sincronitza scoreTempo i playbackTempo amb la
        // marca efectiva, de manera que el botó sempre mostra el tempo de la partitura.
        // Durant la reproducció amb marca explícita: setTempo (la marca substitueix Spd+).
        // Durant la reproducció sense marca explícita: setScoreTempo preserva Spd+/Spd-.
        int resolvedTempo = sc.tempo != null ? sc.tempo : Settings.DEFAULT_TEMPO;
        if (!cam.isPlaying() || sc.tempo != null) {
            MyTempo.setTempo(resolvedTempo);
        } else {
            MyTempo.setScoreTempo(resolvedTempo);
        }
        allPurposeScore.setMidiKey(sc.midiKey != null
                ? sc.midiKey
                : ToneRange.getDefaultKey());
        allPurposeScore.setScaleMode(sc.scaleMode != null
                ? sc.scaleMode
                : ToneRange.getDefaultMode());
        // Compàs: sempre apliquem un valor (el del canvi o el base), per garantir
        // que tornar enrere restauri el compàs original i no deixi el canvi encallat.
        // NO actualitzem Settings.nBeatsMeasure ni Settings.nColsBeat: el layout
        // (colWidth, nColsCam) es basa sempre en el compàs base (col 0), gestionat
        // per freezeBaseTimingParams(). Això garanteix colWidth constant en partitures
        // amb canvis de compàs mid-score.
        allPurposeScore.setNumBeatsMeasure(sc.nBeatsMeasure != null
                ? sc.nBeatsMeasure
                : allPurposeScore.getBaseNBeatsMeasure());
        allPurposeScore.setBeatFigure(sc.beatFigure != null
                ? sc.beatFigure
                : allPurposeScore.getBaseBeatFigure());
        // nColsQuarter i nColsBeat: tampoc s'apliquen a Settings per no alterar getColWidth().
        // computeBeatMeasureLines() llegeix sc.nColsBeat directament del changeMap.
        if (sc.nMeasuresCam != null) {
            Settings.setnMeasuresCam(sc.nMeasuresCam);
        }
        // Les velocitats del changeMap s'apliquen al track i al botó només durant la
        // reproducció (quan el playbar passa per una marca de volum). En mode d'edició,
        // la velocitat del track reflecteix l'ajust manual de l'usuari (Louder/Quieter/Volum).
        if (cam.isPlaying()) {
            for (java.util.Map.Entry<Integer, Integer> e : sc.trackVelocities.entrySet()) {
                MyTrack t = getMixer().getTrackFromId(e.getKey());
                if (t != null) {
                    t.setVelocity(e.getValue());
                    if (e.getKey() == getMixer().getCurrentTrackId()) {
                        this.buttons.updateVolumeButton("" + e.getValue());
                    }
                }
            }
        }
    }

    public void saveScore(String fitxer) {
        if ("".equals(fitxer)) {
            String defMidi = allPurposeScore.getTitle();
            if (defMidi == null || defMidi.isBlank()) defMidi = "partitura";
            defMidi = defMidi.trim().replaceAll("[^\\p{L}\\p{N}\\-._ ]", "_") + ".ddcgr.mid";
            fitxer = MyDialogs.seleccionaFitxerEscriptura(null, defMidi, "mid");
        }
        if (fitxer == null || "".equals(fitxer)) {
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
                return;
            }
        }
        String extensio = obtenirExtensio(fitxer);
        this.currentMidiFile = fitxer;
        if (extensio.equals("mid")) {
            this.stop();
            this.buttons.stopPlayButton();
            if (this.allPurposeScore.hasAnyChords() && saveChordMidiChoice == null) {
                int resp = JOptionPane.showConfirmDialog(
                        this.getUi(),
                        I18n.t("save.chordMidi.confirm"),
                        I18n.t("save.chordMidi.title"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                saveChordMidiChoice = (resp == JOptionPane.YES_OPTION);
            }
            boolean saveChordMidi = Boolean.TRUE.equals(saveChordMidiChoice);
            this.allPurposeScore.saveMidiScore(fitxer, saveChordMidi);
        }
        this.needsSaving = false;
    }

    public static String obtenirExtensio(String nomFitxer) {
        // Comprovar si hi ha un punt al nom del fitxer
        int lastIndexOfDot = nomFitxer.lastIndexOf('.');

        // Si hi ha punt i no és al principi del nom
        if (lastIndexOfDot > 0 && lastIndexOfDot < nomFitxer.length() - 1) {
            return nomFitxer.substring(lastIndexOfDot + 1).toLowerCase();  // Retornar l'extensió
        } else {
            return "";  // Retornar buit si no té extensió
        }
    }

    /** Retorna la mida inicial del buffer: max(initialCurrentCol, endOfContent) + 2 pàgines. */
    private int computeInitialBufferSize(int endOfContent) {
        boolean left = !allPurposeScore.isUseScreenKeyboardRight();
        int initCol = Math.max(0, Settings.getInitialCurrentCol(left, allPurposeScore));
        int colsPerPage = allPurposeScore.getFixedColsPerPage();
        return Math.max(initCol, endOfContent) + 2 * colsPerPage;
    }

    /** Estima l'última columna de la partitura llegint només la capçalera MIDI.
     *  Retorna -1 si el fitxer no es pot llegir. */
    private int estimateLastColFromMidi(String fitxer) {
        try {
            Sequence seq = MidiSystem.getSequence(new File(fitxer));
            long lastTick = 0;
            for (Track t : seq.getTracks()) {
                if (t.size() > 0) lastTick = Math.max(lastTick, t.get(t.size() - 1).getTick());
            }
            long tpq = seq.getResolution(); // ticks per quarter
            int nColsBeat = Settings.getnColsBeat();
            // beatFigure desconegut abans de llegir el MIDI; assumim 4 (negra)
            long ticksPerCol = Math.max(1, tpq / nColsBeat);
            return (int) (lastTick / ticksPerCol);
        } catch (Exception e) {
            return -1;
        }
    }

    /** Expandeix el buffer si col és a l'última pàgina (buffer = endOfScore + 2 pàgines). */
    private void expandBufferIfNeeded(int col) {
        int colsPerPage = allPurposeScore.getFixedColsPerPage();
        if (col + colsPerPage < allPurposeScore.getNColsBuffer()) return;
        int newNCols = Math.max(col, allPurposeScore.getStopCol()) + 2 * colsPerPage;
        allPurposeScore.resizeOffscreen(newNCols);
        myChordSymbolLine.resizeOffscreen(newNCols);
        myLyrics.resizeOffscreen(newNCols);
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
            saveChordMidiChoice = null;
            this.stop();
            this.buttons.stopPlayButton();
            this.buttons.resetFitAnacrusisButton();
            this.exercisesOn = false;
            this.buttons.updateCurrentExerciseButton("");
            this.updateTextOfButtons();
            allPurposeScore.resetAllPurposeScore();
            this.myLyrics.clear();
            this.mixer = new MyMixer(this);
            // Reset compàs i compassos per pantalla per defecte: si el MIDI no té
            // l'entrada al changeMap, s'usa el valor correcte en lloc del de la
            // partitura anterior (p. ex. ¾ persistiria en carregar un 4/4 sense marca).
            this.allPurposeScore.timeSignature2Params("4/4");
            Settings.setnMeasuresCam(4);
            int estimatedCols = estimateLastColFromMidi(fitxer);
            allPurposeScore.setNColsBuffer(computeInitialBufferSize(Math.max(0, estimatedCols)));
            allPurposeScore.readMidiScore(fitxer);
            int outOfRange = allPurposeScore.getOutOfRangeCount();
            if (outOfRange > 0) {
                String msg = I18n.f("load.outOfRange.warning", outOfRange)
                        + (ToneRange.isMetallophone() ? "\n" + I18n.t("load.outOfRange.metallofonHint") : "");
                JOptionPane.showMessageDialog(null, msg,
                        I18n.t("load.outOfRange.title"),
                        JOptionPane.WARNING_MESSAGE);
            }
            resetDrumsMode();
            resetTremolo();
            for (MyTrack t : this.mixer.getTracks()) {
                if (!t.isDeleted()) t.setAudible(true);
            }
            // Aplica nBeatsMeasure del changeMap abans de calcular stopCol:
            // readMidiScore omple el changeMap però no crida applyChangesAt, de manera
            // que numBeatsMeasure encara té el valor del reset (binari per defecte).
            // Sense aquesta correcció, updateStopMarker calcula stopCol amb el compàs
            // incorrecte i la doble barra de final apareix desplaçada en ritme ternari.
            // Sincronitza numBeatsMeasure amb el compàs del fitxer perquè tant
            // freezeBaseTimingParams com updateStopMarker usin el valor correcte.
            // IMPORTANT: en fitxers MIDI estàndard amb anacrusa, el 0x58 pot caure a
            // tsCol > 0 (no al col 0). En tal cas getEffectiveChange(0) retorna nBeatsMeasure=null.
            // Fallback: busca el primer SC del changeMap amb dades de compàs.
            MyGridScore.ScoreChange sc0bm = allPurposeScore.getEffectiveChange(0);
            if (sc0bm == null || (sc0bm.nBeatsMeasure == null && sc0bm.beatFigure == null)) {
                for (MyGridScore.ScoreChange sc : allPurposeScore.getChangeMap().values()) {
                    if (sc.nBeatsMeasure != null || sc.beatFigure != null) { sc0bm = sc; break; }
                }
            }
            if (sc0bm != null) {
                if (sc0bm.nBeatsMeasure != null) allPurposeScore.setNumBeatsMeasure(sc0bm.nBeatsMeasure);
                if (sc0bm.beatFigure    != null) allPurposeScore.setBeatFigure(sc0bm.beatFigure);
            }
            // Llegeix nMeasuresCam del CHANGEMAP SC col 0 (format nou, guardat per newScore/save).
            // El text event legacy "nMeasuresCam=N" (format antic) s'ignora perquè podia
            // contenir valors incorrectes de sessions anteriors.
            // Si el fitxer no té nMeasuresCam al CHANGEMAP, s'usa el defecte (4).
            {
                int nmc = Settings.DEFAULT_NMEASURES_CAM;
                MyGridScore.ScoreChange sc0nmc = allPurposeScore.getChangeMap().get(0);
                if (sc0nmc != null && sc0nmc.nMeasuresCam != null) {
                    nmc = sc0nmc.nMeasuresCam;
                }
                Settings.setnMeasuresCam(nmc);
            }
            // ORDRE IMPORTANT: freezeBaseTimingParams primer perquè actualitza
            // Settings.nBeatsMeasure i getnColsCam(); updateStopMarker ho necessita
            // per calcular minStopCol correctament (evita usar 4/4 en scores ternaris).
            allPurposeScore.freezeBaseTimingParams();
            allPurposeScore.updateStopMarker();
            // Assegura que el changeMap té el tempo inicial del fitxer al col 0.
            // analyzeMidiHeader (dins readMidiScore) ja ha fixat MyTempo al tempo MIDI.
            // Si no hi ha cap entrada de tempo al col 0, n'afegim una implícita perquè
            // applyChangesAt (cridat des d'updateTextOfButtons) no caigui al DEFAULT_TEMPO.
            int midiTempo = MyTempo.getTempo();
            MyGridScore.ScoreChange sc0fix = allPurposeScore.getChangeMap().get(0);
            if (sc0fix == null) {
                sc0fix = new MyGridScore.ScoreChange();
                allPurposeScore.setScoreChange(0, sc0fix);
            }
            if (sc0fix.tempo == null) {
                sc0fix.tempo = midiTempo;
            }
            MyTempo.setTempo(sc0fix.tempo);
            // Aplica fitAnacrusis desat al MIDI (sincronitza Settings i el botó).
            this.buttons.setFitAnacrusisButton(this.allPurposeScore.isFitAnacrusisScore());
            this.allPurposeScore.initOffscreen();
            this.myChordSymbolLine.initOffscreen();
            // Sincronitza el track de visualització de lletres al primer que en té.
            // clear() ha resetejat displayTrackId=0; sense aquesta crida les lletres
            // carregades sota un altre trackId no es dibuixarien.
            this.myLyrics.syncDisplayTrackId();
            this.myLyrics.initOffscreen();
            this.cam.reset();
            this.statusLine.setText(scoreStatusText());
            Settings.setTipsVisible(true);
            this.updateTextOfButtons();
            this.buttons.hideTip();
            MyNewPanel panel = getUi() != null ? getUi().getPanel() : null;
            if (panel != null) {
                MyNewPanel.setRepintaWasCalled(true);
                this.setNeedsDrawing(true);
                panel.paintImmediately(0, 0, panel.getWidth(), panel.getHeight());
            }
        } else {
            this.updateTextOfButtons();
        }
    }

    private void newScore() {
        saveChordMidiChoice = null;
        allPurposeScore.resetAllPurposeScore(); //new MyAllPurposeScore(this);
        this.allPurposeScore.clearScore();  // buida notes, acords i missatges
        Settings.setHasAnacrusis(false);
        this.buttons.resetFitAnacrusisButton();
        this.myLyrics.clear();
        this.stop();
        this.buttons.stopPlayButton();
        this.exercisesOn = false;
        this.buttons.updateCurrentExerciseButton("");
        Settings.setnMeasuresCam(4);
        // Restaura el compàs per defecte (4/4) i recalcula nColsBeat
        this.allPurposeScore.timeSignature2Params("4/4");
        // Marques per defecte a la columna 0 (tempo, tonalitat i compàs)
        MyGridScore.ScoreChange defaultMarks = new MyGridScore.ScoreChange();
        defaultMarks.tempo        = Settings.getDefaultTempo();
        defaultMarks.midiKey      = ToneRange.getDefaultKey();
        defaultMarks.scaleMode    = 'M'; // per defecte Major
        defaultMarks.nBeatsMeasure = allPurposeScore.getNumBeatsMeasure();
        defaultMarks.beatFigure    = allPurposeScore.getBeatFigure();
        defaultMarks.nMeasuresCam  = Settings.getnMeasuresCam();
        this.allPurposeScore.setScoreChange(0, defaultMarks);
        MyTempo.setTempo(Settings.getDefaultTempo());
        allPurposeScore.freezeBaseTimingParams();
        allPurposeScore.setNColsBuffer(computeInitialBufferSize(0));
        // allPurposeScore.resetAllPurposeScore();
        //this.myChordSymbolLine.setScore(allPurposeScore);
        //this.cam.setScore(allPurposeScore);
        //this.cam.setSymbolLine(myChordSymbolLine);
        this.cam.reset();
        allPurposeScore.updateStopMarker();
        this.statusLine.setText(scoreStatusText());
        Settings.setTipsVisible(true);
        this.buttons.setToggleButtonsToProgramValues();
        this.currentMidiFile = "";
        resetDrumsMode();
        resetTremolo();
        this.setDefaultTrack();
        this.allPurposeScore.initOffscreen();
        this.myChordSymbolLine.initOffscreen();
        this.myLyrics.initOffscreen();

        this.allPurposeScore.getChoice().setNoneChoice();
        deactivateSelectingMode();
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
        // NOTE: Settings that have no UI control (tipsVisible, screenWidthRatio,
        // screenHeightRatio, ui.language, nColsQuarter) are intentionally NOT saved here.
        // The user sets them by editing config.properties while the app is closed.
        // Saving them here would overwrite manual edits made between runs.
        AppConfig.get().set("autoCorrect", "" + Settings.isAutoCorrect());
        AppConfig.get().set("nColsQuarter", "" + Settings.getnColsQuarter());
        AppConfig.get().set("nColsScore", "" + Settings.getnColsScore());
        AppConfig.get().set("isMetallophone", "" + ToneRange.isMetallophone());
        if (ToneRange.isMetallophone()) {
            AppConfig.get().set("lowestMidi", "" + ToneRange.DEFAULT_LOWEST_MIDI);
            AppConfig.get().set("highestMidi", "" + ToneRange.DEFAULT_HIGHEST_MIDI);
        } else {
            AppConfig.get().set("lowestMidi", "" + ToneRange.getLowestMidi());
            AppConfig.get().set("highestMidi", "" + ToneRange.getHighestMidi());
            AppConfig.get().set("leadInstrument", "" + SoundWithMidi.getLeadInstrument());
            AppConfig.get().set("chordInstrument", "" + SoundWithMidi.getChordInstrument());
        }
        AppConfig.get().set(MyDialogs.CONFIG_KEY_LAST_DIR, MyDialogs.lastDirectory.getAbsolutePath());
        AppConfig.get().set(MyDialogs.CONFIG_KEY_LAST_DIR_SVG, MyDialogs.lastDirectorySvg.getAbsolutePath());
        AppConfig.get().set(MyDialogs.CONFIG_KEY_LAST_DIR_PDF, MyDialogs.lastDirectoryPdf.getAbsolutePath());
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

    /** Inicialitza el track per a la família d'exercicis donada.
     *  Per a EarTraining i Jazz força l'instrument a piano (0);
     *  per a DoDeReBuExercises usa l'instrument de la configuració (glock). */
    private void initExerciseFamilyTrack(String familyName) {
        this.setDefaultTrack();
        if ("EarTraining".equals(familyName) || "Jazz".equals(familyName)) {
            dodecagraphone.model.mixer.MyTrack leadTrack = this.mixer.getCurrentTrack();
            if (leadTrack != null) {
                int chan = leadTrack.getCurrentChannel();
                SoundWithMidi.assignInstToChannel(chan, 0);
                SoundWithMidi.runProgramChange(chan, 0);
                int offset = InstrumentRange.calcDisplayOffset(0, ToneRange.getLowestMidi(), ToneRange.getHighestMidi());
                leadTrack.setDisplayOffset(offset);
            }
        }
    }

    private void nextExercise() {
        if (!this.exercisesOn) {
            this.exerciseListName = MyDialogs.seleccionaOpcio(I18n.t("MyController.exercise.selectFamily.prompt"), I18n.t("MyController.exercise.selectFamily.title"), getExerciseListNames(), 0);
            if (exerciseListName == null || exerciseListName.isEmpty()) {
                this.buttons.onButtonRelesased(this.getButtons().getId_NextExerciseButton());
                return;
            }
            this.exerciseList = new MyExerciseList(exerciseListName, this);
            this.exercisesOn = true;
            this.initExerciseFamilyTrack(exerciseListName);
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
        this.updateTextOfButtons();
        this.buttons.onButtonRelesased(this.getButtons().getId_NextExerciseButton());
        this.statusLine.setText(allPurposeScore.getLabel() + ": " + allPurposeScore.getDescription());
    }

    private void prevExercise() {
        if (!this.exercisesOn) {
            this.exerciseListName = MyDialogs.seleccionaOpcio(I18n.t("MyController.exercise.selectPackage.prompt"), I18n.t("MyController.exercise.selectPackage.title"), getExerciseListNames(), 0);
            if (exerciseListName == null || exerciseListName.isEmpty()) {
                this.buttons.onButtonRelesased(this.getButtons().getId_PrevExerciseButton());
                return;
            }
            this.exerciseList = new MyExerciseList(exerciseListName, this);
            this.exercisesOn = true;
            this.initExerciseFamilyTrack(exerciseListName);
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
        this.updateTextOfButtons();
        this.buttons.onButtonRelesased(this.getButtons().getId_PrevExerciseButton());
        this.statusLine.setText(allPurposeScore.getLabel() + ": " + allPurposeScore.getDescription());
    }

    private void resetExercise() {
        this.stop();
        this.buttons.stopPlayButton();
        this.exerciseList.resetCurrentExercise();
        this.myChordSymbolLine.initOffscreen();
        this.myLyrics.initOffscreen();
        this.cam.reset();
        this.updateTextOfButtons();
        this.buttons.onButtonRelesased(this.getButtons().getId_RestartExerciseButton());
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

    // =========================================================================
    // Column insert / delete (Ctrl+I / Ctrl+D)
    // =========================================================================

    /** Inserts an empty column at col, extending notes that span the point. */
    public void insertColumnAt(int col) {
        allPurposeScore.insertColumn(col, true);
        getMyLyrics().shiftSegmentsFrom(col, +1);
        allPurposeScore.updateStopMarker();
    }

    /** Inserts a truly empty column at col without extending spanning notes (used in undo-of-delete). */
    public void insertEmptyColumnAt(int col) {
        allPurposeScore.insertColumn(col, false);
        getMyLyrics().shiftSegmentsFrom(col, +1);
        allPurposeScore.updateStopMarker();
    }

    /** Deletes the column at col, shifting all content left. */
    public void deleteColumnAt(int col) {
        allPurposeScore.deleteColumn(col);
        getMyLyrics().shiftSegmentsFrom(col, -1);
        allPurposeScore.updateStopMarker();
    }

    /** Invoked by Ctrl+I: asks how many, then waits for a column click. */
    public void handleInsertColumn() {
        int n = showColumnCountDialog("insert");
        if (n <= 0) return;
        startPendingColumnOp("insert", n);
    }

    /** Invoked by Ctrl+D: asks how many, then waits for a column click. */
    public void handleDeleteColumn() {
        int n = showColumnCountDialog("delete");
        if (n <= 0) return;
        startPendingColumnOp("delete", n);
    }

    private int showColumnCountDialog(String op) {
        String title   = I18n.t("column." + op + ".dialog.title");
        String message = I18n.t("column." + op + ".dialog.message");
        javax.swing.JSpinner spinner = new javax.swing.JSpinner(
            new javax.swing.SpinnerNumberModel(1, 1, 999, 1));
        Object[] content = {message, spinner};
        int result = JOptionPane.showConfirmDialog(this.getUi(), content, title,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return 0;
        return (Integer) spinner.getValue();
    }

    public boolean isPendingColumnOp() { return pendingColumnOp != null; }

    public void cancelPendingColumnOp() {
        pendingColumnOp = null;
        if (pendingColumnDialog != null) {
            pendingColumnDialog.dispose();
            pendingColumnDialog = null;
        }
    }

    public int getEditingColPublic() { return getEditingCol(); }

    public void executePendingColumnOpAt(int col) {
        if (pendingColumnOp == null) return;
        String op = pendingColumnOp;
        int n = pendingColumnN;
        cancelPendingColumnOp();
        if ("insert".equals(op)) {
            insertNColumnsAt(col, n);
            afegirEvent(new ColumnEvent(this, col, n));
        } else {
            List<ColumnEvent.ColSnapshot> snaps = buildColumnSnapshots(col, n);
            deleteNColumnsAt(col, n);
            afegirEvent(new ColumnEvent(this, col, n, snaps));
        }
        allPurposeScore.drawFullGridinOffscreen();
        redrawChordLine();
        myLyrics.setNeedsDrawing(true);
        myLyrics.drawFullLyricsInOffscreen();
        if (this.getUi() != null && this.getUi().getPanel() != null) {
            this.getUi().getPanel().repinta(true);
        }
    }

    private void startPendingColumnOp(String op, int n) {
        if (pendingChange != null) return;
        cancelPendingColumnOp();
        pendingColumnOp = op;
        pendingColumnN  = n;
        String title   = I18n.t("column." + op + ".dialog.title");
        String message = I18n.t("column." + op + ".dialog.click");
        javax.swing.JDialog dlg = new javax.swing.JDialog(this.getUi(), title, false);
        dlg.setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);
        dlg.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) {
                pendingColumnOp = null;
                pendingColumnDialog = null;
            }
        });
        javax.swing.JLabel label = new javax.swing.JLabel(message, javax.swing.SwingConstants.CENTER);
        label.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 24, 8, 24));
        javax.swing.JButton btnPlaybar = new javax.swing.JButton(I18n.t("column.op.atPlaybar"));
        btnPlaybar.addActionListener(ev -> executePendingColumnOpAt(getEditingCol()));
        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 8));
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 24, 16, 24));
        panel.add(label, java.awt.BorderLayout.CENTER);
        panel.add(btnPlaybar, java.awt.BorderLayout.SOUTH);
        dlg.add(panel);
        dlg.getRootPane().setDefaultButton(btnPlaybar);
        dlg.pack();
        dlg.setLocationRelativeTo(this.getUi());
        dlg.setVisible(true);
        pendingColumnDialog = dlg;
        drawFull(true);
    }

    /** Inserts n columns at col, extending notes that span the insertion point. */
    public void insertNColumnsAt(int col, int n) {
        for (int i = 0; i < n; i++) insertColumnAt(col);
        fixBarlinePhasesAfterInsert(col, n);
    }

    /**
     * Després d'inserir N columnes a col, afegeix un ScoreChange a col+n que
     * reinicia el comptador de barres al punt de fase que tenia col originalment.
     * Això evita que les barres de compàs es desplacin cap a posicions incorrectes.
     */
    private void fixBarlinePhasesAfterInsert(int col, int n) {
        // Computa la fase (beatInMeasure, colInBeat) que tenia 'col' just ABANS
        // de la inserció. Entrées < col no s'han desplaçat; les >= col estan a >= col+n.
        int curNColsBeat     = (allPurposeScore.getBaseNColsBeat()    > 0)
                ? allPurposeScore.getBaseNColsBeat()    : Settings.getnColsBeat();
        int curNBeatsMeasure = (allPurposeScore.getBaseNBeatsMeasure() > 0)
                ? allPurposeScore.getBaseNBeatsMeasure() : allPurposeScore.getNumBeatsMeasure();
        int colInBeat = 0;
        int beatInMeasure = 0;
        for (int c = 0; c <= col; c++) {
            MyGridScore.ScoreChange sc = allPurposeScore.getChangeMap().get(c);
            if (sc != null) {
                boolean hasMeterChange = (sc.nColsBeat != null || sc.nBeatsMeasure != null
                                          || sc.nColsQuarter != null);
                if (sc.nColsBeat     != null) curNColsBeat     = sc.nColsBeat;
                if (sc.nBeatsMeasure != null) curNBeatsMeasure = sc.nBeatsMeasure;
                if (hasMeterChange) {
                    // Canvi explícit de compàs: el comptador sempre comença a (0,0)
                    colInBeat     = 0;
                    beatInMeasure = 0;
                } else {
                    // SC de fase pura: usa la fase guardada
                    colInBeat     = (sc.colInBeatPhase != null) ? sc.colInBeatPhase : 0;
                    beatInMeasure = (sc.measurePhase   != null) ? sc.measurePhase   : 0;
                }
            }
            if (c < col) {
                colInBeat++;
                if (colInBeat >= curNColsBeat) {
                    colInBeat = 0;
                    beatInMeasure++;
                    if (beatInMeasure >= curNBeatsMeasure) beatInMeasure = 0;
                }
            }
        }
        if (curNColsBeat <= 0 || curNBeatsMeasure <= 0) return;

        // Col=0 especial: la chord line sempre dibuixa una marca de fallback a col=0
        // usant el SC que hi hagi (o els defaults si no n'hi ha). La inserció ha
        // desplaçat el SC original de col=0 fins a col+n → duplicat visual. Ho resolem:
        // restaurem el SC original a col=0 i netegem col+n.
        // A més, per a col=0 NO afegim SC de fase a col+n: el comptador natural
        // col·loca les barres des de col=0 (cada colsPerMeasure), que és exactament
        // el que vol l'usuari quan insereix a l'inici de la partitura.
        if (col == 0) {
            MyGridScore.ScoreChange sc0 = allPurposeScore.getChangeMap().get(n);
            if (sc0 != null) {
                allPurposeScore.setScoreChange(0, sc0);
                allPurposeScore.getChangeMap().remove(n);
            }
            return; // barres naturals des de col=0: no cal cap SC addicional
        }

        // Col > 0: l'avanç natural ja posa col+n a la fase (colInBeat+1, beatInMeasure),
        // que és la correcta per al contingut desplaçat. No cal afegir cap SC de fase:
        // qualsevol SC d'aquest tipus (colInBeatPhase) causaria que un beat aparegués
        // amb una columna de més (si col era inici de beat) o que l'origen del compàs
        // es desplacés (si calia suprimir el beat spurious). L'únic artefacte és que
        // l'última sub-columna del beat anterior a col+n passa a ser la primera del beat
        // següent, però és 1/nColsBeat de beat (en general 1/8 de beat) i poc perceptible.
        //
        // NOTA: si a col+n ja hi ha un SC de canvi de compàs (nColsBeat/nBeatsMeasure),
        // aquest SC ha quedat desplaçat des de col per la inserció i és correcte tal qual;
        // el seu camp colInBeatPhase (si en tenia d'una correcció anterior) queda obsolet
        // però és inert perquè el canvi de compàs reinicia la fase a (0,0) per davant.
    }

    /** Inserts n truly empty columns at col (used in undo-of-delete). */
    public void insertNEmptyColumnsAt(int col, int n) {
        for (int i = 0; i < n; i++) insertEmptyColumnAt(col);
    }

    /** Deletes n columns starting at col. */
    public void deleteNColumnsAt(int col, int n) {
        for (int i = 0; i < n; i++) deleteColumnAt(col);
    }

    /** Captures snapshots of n consecutive columns starting at col. */
    public List<ColumnEvent.ColSnapshot> buildColumnSnapshots(int col, int n) {
        List<ColumnEvent.ColSnapshot> snaps = new ArrayList<>();
        for (int i = 0; i < n; i++) snaps.add(buildColumnSnapshot(col + i));
        return snaps;
    }

    /** Captures a snapshot of column col before it is deleted. */
    public ColumnEvent.ColSnapshot buildColumnSnapshot(int col) {
        ColumnEvent.ColSnapshot snap = new ColumnEvent.ColSnapshot();
        int nKeys = allPurposeScore.getnKeys();
        for (int row = 0; row < nKeys; row++) {
            MyGridSquare sq = allPurposeScore.getGridSquare(row, col);
            if (sq != null && !sq.getPoliNotes().isEmpty()) {
                List<int[]> notes = new ArrayList<>();
                for (MyGridSquare.SubSquare ss : sq.getPoliNotes()) {
                    notes.add(new int[]{
                        ss.getChannel(), ss.getTrack(), ss.getVelocity(),
                        ss.isStoredVisible() ? 1 : 0,
                        ss.isStoredMuted()   ? 1 : 0,
                        ss.isLinked()        ? 1 : 0,
                        ss.isDotted()        ? 1 : 0
                    });
                }
                snap.gridRows.put(row, notes);
            }
        }
        snap.chord   = allPurposeScore.getChordSimbolLine().get(col);
        snap.bgChord = allPurposeScore.getChordLine().get(col);
        snap.change  = allPurposeScore.getChangeMap().get(col);
        snap.message = allPurposeScore.getMessages().get(col);
        snap.midiMsg = allPurposeScore.getMidiMessages().get(col);
        for (List<MyLyrics.LyricSegment> segs : getMyLyrics().getLyricsByTrack().values()) {
            for (MyLyrics.LyricSegment seg : segs) {
                if (seg.col == col) snap.lyricSegments.add(seg);
            }
        }
        return snap;
    }

    /** Restores column data from a snapshot (called after insertEmptyColumnAt in undo-of-delete). */
    public void restoreColumnAt(int col, ColumnEvent.ColSnapshot snapshot) {
        for (Map.Entry<Integer, List<int[]>> entry : snapshot.gridRows.entrySet()) {
            int scoreRow = entry.getKey();
            for (int[] info : entry.getValue()) {
                allPurposeScore.addNoteToSquare(scoreRow, col, 1, Settings.getnRowsSquare(),
                    (MyComponent) allPurposeScore, this, allPurposeScore, getCam(),
                    info[0], info[1], info[2], info[3]==1, info[4]==1, info[5]==1, info[6]==1);
            }
        }
        if (snapshot.chord   != null) allPurposeScore.placeChordSymbol(snapshot.chord, col);
        if (snapshot.bgChord != null) allPurposeScore.getChordLine().put(col, snapshot.bgChord);
        if (snapshot.change  != null) allPurposeScore.setScoreChange(col, snapshot.change);
        if (snapshot.message != null) allPurposeScore.getMessages().put(col, snapshot.message);
        if (snapshot.midiMsg != null) allPurposeScore.getMidiMessages().put(col, snapshot.midiMsg);
        Map<Integer, List<MyLyrics.LyricSegment>> byTrack = getMyLyrics().getLyricsByTrack();
        for (MyLyrics.LyricSegment seg : snapshot.lyricSegments) {
            byTrack.computeIfAbsent(seg.track, k -> new ArrayList<>()).add(seg);
        }
        allPurposeScore.updateStopMarker();
    }
}
