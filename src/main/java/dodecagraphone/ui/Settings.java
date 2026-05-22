/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.ui;

import dodecagraphone.model.ToneRange;
import dodecagraphone.model.component.MyGridScore;
import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * [CA] Paràmetres globals de l'aplicació. Inclou valors per defecte, estat
 * configurable i mètodes derivats de layout. Tota la classe és estàtica: no
 * s'instancia mai.
 * <p>
 * Layout general: JFrame/JPanel dividit en teclat (esquerra o dreta),
 * càmera (centre), panell de control (dreta) i línia d'estat (baix).
 * La càmera conté: franja d'acords (dalt), partitura (centre) i lletra (baix).
 * <p>
 * [EN] Global application parameters. Contains default values, configurable
 * state and derived layout methods. The class is entirely static and is never
 * instantiated.
 * <p>
 * General layout: JFrame/JPanel divided into keyboard (left or right),
 * camera (centre), control panel (right) and status line (bottom). The camera
 * contains: chord strip (top), score (centre) and lyrics (bottom).
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class Settings {

    // ── Comportament de l'app ─────────────────────────────────────────────
    public static final boolean COLORS_BU  = true;
    public static final boolean IS_BU      = true;
    public static final int  REFRESH_PERIOD               = 30;  // ms → ~33 fps
    public static final int  BUTTON_REPEAT_INITIAL_DELAY_MS = 600;
    public static final int  BUTTON_REPEAT_INTERVAL_MS      = 100;

    // ── Flags de depuració ────────────────────────────────────────────────
    public static final boolean SHOW_DRAW_HIERARCHY = false;
    public static final boolean SHOW_DIMENSIONS     = false;
    public static       int     PRINT_OUT_PRIORITY  = 1; // 1 = màxim, 0 = silenci
    public static final int     NUM_SIBLINGS        = 5; // companys mostrats a showDimensions()

    // ── Valors sentinel de domini ─────────────────────────────────────────
    public static final int USE_INFO_AS_SIMBOL = -13;
    public static final int IS_STOP_CHORD      = -14;
    public static final int REST               = -15;
    public static final int END                = -16;
    public static final int INVALID_CHORD      = -16;

    // ── Constants de partitura i reproducció ─────────────────────────────
    public static final boolean PLAY_AT_BEAT  = false;
    public static final int     DEFAULT_TEMPO = 60;
    public static final int     MAX_BPM       = 300;
    public static final int     END_COL_MARGIN = 250;

    // ── Valors per defecte de layout ──────────────────────────────────────
    private static final double DEFAULT_SCREEN_WIDTH_RATIO  = 0.95;
    private static final double DEFAULT_SCREEN_HEIGHT_RATIO = 0.8;
    private static final double DEFAULT_KEYBOARD_RATIO      = 2.4 / 16;
    private static final double DEFAULT_CONTROL_RATIO       = 4.6 / 16;
    private static final double DEFAULT_STATUS_RATIO        = 4.5 / 59;
    private static final int    DEFAULT_NROWS_CHORD         = 5;
    private static final int    DEFAULT_NROWS_LYRICS        = 3;
    private static final int    DEFAULT_NCOLS_SQUARE        = 1;
    private static final int    DEFAULT_NROWS_BUTTON        = 2;
    private static final boolean CHORD_SYMBOL_VERTICAL      = true;

    // ── Valors per defecte de partitura ───────────────────────────────────
    public static final int DEFAULT_NMEASURES_CAM  = 4;
    public static final double KEY_WIDTH_REDUCTION = 0.75; // ràtio XiloKey vs Slides
    private static final int DEFAULT_NBEATS_MEASURE = 4;
    private static final int DEFAULT_NCOLS_QUARTER  = 12;
    private static final int DEFAULT_BEAT_FIGURE    = 4;   // denominador compàs (4 = negra)
    private static final int DEFAULT_NCOLS_SCORE    = 12000;

    // ── Escales per defecte ───────────────────────────────────────────────
    public static final Integer[] DEFAULT_CHOICE =
            {0, 2, 4, 5, 7, 9, 11, 12};
    public static final Integer[] DEFAULT_CHOICE_2OCTAVES =
            {0, 2, 4, 5, 7, 9, 11, 12, 14, 16, 17, 19, 21, 23, 24};
    public static final Integer[] DEFAULT_MINOR_CHOICE =
            {0, 2, 3, 5, 7, 8, 10, 12};
    public static final Integer[] DEFAULT_MINOR_CHOICE_2OCTAVES =
            {0, 2, 3, 5, 7, 8, 10, 12, 14, 15, 17, 19, 20, 22, 24};

    // ── UI per defecte ────────────────────────────────────────────────────
    public static final boolean DEFAULT_AUTO_CORRECT  = true;
    public static final boolean DEFAULT_TIPS_VISIBLE  = true;
    public static final boolean DEFAULT_SHOW_MUTTED   = true;

    // ── Estat configurable ────────────────────────────────────────────────
    private static int     nMeasuresCam;
    private static int     nBeatsMeasure;
    private static int     nColsBeat;
    private static int     nColsQuarter;
    private static int     beatFigure;
    private static int     nColsScore;
    private static boolean chordSymbolVertical;
    private static boolean autoCorrect;
    private static boolean tipsVisible;
    private static boolean showMutted;
    private static double  screenWidthRatio;
    private static double  screenHeightRatio;
    private static double  screenPixelWidth  = 0;
    private static double  screenPixelHeight = 0;
    private static boolean fitAnacrusis;
    private static boolean hasAnacrusis;

    // ── Inicialització ────────────────────────────────────────────────────

    /**
     * [CA] Inicialitza tots els paràmetres globals llegint les preferències
     * de {@link AppConfig}. S'ha de cridar una sola vegada en arrancar
     * l'aplicació. {@code fitAnacrusis} sempre s'inicialitza a {@code false}
     * (no es persisteix).
     * <p>
     * [EN] Initialises all global parameters by reading preferences from
     * {@link AppConfig}. Must be called once at application startup.
     * {@code fitAnacrusis} is always initialised to {@code false} (not
     * persisted).
     */
    public static void initSettings() {
        autoCorrect  = Boolean.parseBoolean(AppConfig.get().get("autoCorrect",  "" + DEFAULT_AUTO_CORRECT));
        tipsVisible  = Boolean.parseBoolean(AppConfig.get().get("tipsVisible",  "" + DEFAULT_TIPS_VISIBLE));
        showMutted   = DEFAULT_SHOW_MUTTED;

        nMeasuresCam = Integer.parseInt(AppConfig.get().get("nMeasuresCam", "" + DEFAULT_NMEASURES_CAM));
        nColsQuarter = Integer.parseInt(AppConfig.get().get("nColsQuarter", "" + DEFAULT_NCOLS_QUARTER));
        nColsScore   = Integer.parseInt(AppConfig.get().get("nColsScore",   "" + DEFAULT_NCOLS_SCORE));
        nBeatsMeasure = DEFAULT_NBEATS_MEASURE;
        beatFigure   = DEFAULT_BEAT_FIGURE;
        nColsBeat    = nColsQuarter;

        screenWidthRatio  = Double.parseDouble(AppConfig.get().get("screenWidthRatio",  "" + DEFAULT_SCREEN_WIDTH_RATIO));
        screenHeightRatio = Double.parseDouble(AppConfig.get().get("screenHeightRatio", "" + DEFAULT_SCREEN_HEIGHT_RATIO));

        fitAnacrusis       = false; // no es persisteix; sempre comença desactivat
        hasAnacrusis       = false;
        chordSymbolVertical = CHORD_SYMBOL_VERTICAL;

        String language = AppConfig.get().get("ui.language", "en");
        I18n.initFromLanguageTag(language);
    }

    // ── Partitura: compassos, temps i columnes ────────────────────────────

    /**
     * [CA] Retorna el nombre de compassos visibles a la càmera.
     * <p>
     * [EN] Returns the number of measures visible in the camera.
     *
     * @return [CA] nombre de compassos / [EN] number of measures
     */
    public static int  getnMeasuresCam()        { return nMeasuresCam; }

    /**
     * [CA] Estableix el nombre de compassos visibles a la càmera.
     * <p>
     * [EN] Sets the number of measures visible in the camera.
     *
     * @param nm [CA] nombre de compassos / [EN] number of measures
     */
    public static void setnMeasuresCam(int nm)  { nMeasuresCam = nm; }

    /**
     * [CA] Retorna el nombre de temps per compàs.
     * <p>
     * [EN] Returns the number of beats per measure.
     *
     * @return [CA] temps per compàs / [EN] beats per measure
     */
    public static int  getnBeatsMeasure()              { return nBeatsMeasure; }

    /**
     * [CA] Estableix el nombre de temps per compàs.
     * <p>
     * [EN] Sets the number of beats per measure.
     *
     * @param nBm [CA] temps per compàs / [EN] beats per measure
     */
    public static void setnBeatsMeasure(int nBm)       { nBeatsMeasure = nBm; }

    /**
     * [CA] Retorna la figura del temps (4 = negra, 8 = corxera).
     * <p>
     * [EN] Returns the beat figure (4 = quarter note, 8 = eighth note).
     *
     * @return [CA] figura del temps / [EN] beat figure
     */
    public static int  getBeatFigure()           { return beatFigure; }

    /**
     * [CA] Estableix la figura del temps.
     * <p>
     * [EN] Sets the beat figure.
     *
     * @param bf [CA] figura del temps / [EN] beat figure
     */
    public static void setBeatFigure(int bf)     { beatFigure = bf; }

    /**
     * [CA] Retorna el nombre de columnes per quarter note (resolució temporal).
     * <p>
     * [EN] Returns the number of columns per quarter note (temporal resolution).
     *
     * @return [CA] columnes per quarter / [EN] columns per quarter note
     */
    public static int  getnColsQuarter()                 { return nColsQuarter; }

    /**
     * [CA] Estableix el nombre de columnes per quarter note.
     * <p>
     * [EN] Sets the number of columns per quarter note.
     *
     * @param nColsQuarter [CA] columnes per quarter / [EN] columns per quarter note
     */
    public static void setnColsQuarter(int nColsQuarter) { Settings.nColsQuarter = nColsQuarter; }

    /**
     * [CA] Retorna el nombre de columnes per temps (beat).
     * <p>
     * [EN] Returns the number of columns per beat.
     *
     * @return [CA] columnes per beat / [EN] columns per beat
     */
    public static int  getnColsBeat()  { return nColsBeat; }

    /**
     * [CA] Recalcula {@code nColsBeat} a partir de {@code nColsQuarter} i
     * {@code beatFigure}. Per a compàs de corxeres (8), el beat és 1,5 vegades
     * la quarter.
     * <p>
     * [EN] Recomputes {@code nColsBeat} from {@code nColsQuarter} and
     * {@code beatFigure}. For eighth-note time signatures (8), the beat is
     * 1.5 times the quarter.
     */
    public static void updateNColsBeat() {
        nColsBeat = nColsQuarter;
        if (beatFigure == 8) nColsBeat = (int) (nColsBeat * 1.5);
    }

    /**
     * [CA] Retorna la capacitat total de la partitura en columnes.
     * <p>
     * [EN] Returns the total score capacity in columns.
     *
     * @return [CA] nombre total de columnes / [EN] total number of columns
     */
    public static int  getnColsScore()      { return nColsScore; }

    /**
     * [CA] Estableix la capacitat total de la partitura en columnes.
     * <p>
     * [EN] Sets the total score capacity in columns.
     *
     * @param n [CA] nombre de columnes / [EN] number of columns
     */
    public static void setnColsScore(int n) { nColsScore = n; }

    /**
     * [CA] Retorna el retard per defecte (en columnes), equivalent a una
     * quarter note.
     * <p>
     * [EN] Returns the default delay (in columns), equivalent to one quarter
     * note.
     *
     * @return [CA] retard per defecte / [EN] default delay
     */
    public static int getDefaultDelay()  { return getnColsQuarter(); }

    /**
     * [CA] Retorna el tempo per defecte en BPM.
     * <p>
     * [EN] Returns the default tempo in BPM.
     *
     * @return [CA] tempo per defecte / [EN] default tempo
     */
    public static int getDefaultTempo()  { return DEFAULT_TEMPO; }

    // ── UI: preferències de l'usuari ──────────────────────────────────────

    /**
     * [CA] Indica si l'autocorrecció de notes és activa.
     * <p>
     * [EN] Indicates whether note autocorrection is active.
     *
     * @return [CA] true si activa / [EN] true if active
     */
    public static boolean isAutoCorrect()               { return autoCorrect; }

    /**
     * [CA] Activa o desactiva l'autocorrecció de notes.
     * <p>
     * [EN] Activates or deactivates note autocorrection.
     *
     * @param v [CA] nou valor / [EN] new value
     */
    public static void    setAutoCorrect(boolean v)     { autoCorrect = v; }

    /**
     * [CA] Indica si els consells (tips) de la UI són visibles.
     * <p>
     * [EN] Indicates whether UI tips are visible.
     *
     * @return [CA] true si visibles / [EN] true if visible
     */
    public static boolean isTipsVisible()               { return tipsVisible; }

    /**
     * [CA] Activa o desactiva la visibilitat dels consells de la UI.
     * <p>
     * [EN] Activates or deactivates UI tip visibility.
     *
     * @param v [CA] nou valor / [EN] new value
     */
    public static void    setTipsVisible(boolean v)     { tipsVisible = v; }

    /**
     * [CA] Indica si les notes silenciades es mostren a la partitura.
     * <p>
     * [EN] Indicates whether muted notes are shown in the score.
     *
     * @return [CA] true si es mostren / [EN] true if shown
     */
    public static boolean isShowMutted()                { return showMutted; }

    /**
     * [CA] Activa o desactiva la visualització de notes silenciades.
     * <p>
     * [EN] Activates or deactivates the display of muted notes.
     *
     * @param v [CA] nou valor / [EN] new value
     */
    public static void    setShowMutted(boolean v)      { showMutted = v; }

    /**
     * [CA] Indica si els símbols d'acord es mostren en vertical.
     * <p>
     * [EN] Indicates whether chord symbols are displayed vertically.
     *
     * @return [CA] true si vertical / [EN] true if vertical
     */
    public static boolean isChordSymbolVertical()           { return chordSymbolVertical; }

    /**
     * [CA] Activa o desactiva la disposició vertical dels símbols d'acord.
     * <p>
     * [EN] Activates or deactivates vertical chord symbol layout.
     *
     * @param v [CA] nou valor / [EN] new value
     */
    public static void    setChordSymbolVertical(boolean v) { chordSymbolVertical = v; }

    // ── Anacrusis ─────────────────────────────────────────────────────────

    /**
     * [CA] Indica si el mode "Encabir anacrusis" és actiu. Quan és actiu i
     * la partitura té anacrusis, la primera pàgina es comprimeix visualment.
     * No es persisteix: sempre arrenca com a {@code false}.
     * <p>
     * [EN] Indicates whether "Fit anacrusis" mode is active. When active and
     * the score has an anacrusis, the first page is visually compressed. Not
     * persisted: always starts as {@code false}.
     *
     * @return [CA] true si actiu / [EN] true if active
     */
    public static boolean isFitAnacrusis()          { return fitAnacrusis; }

    /**
     * [CA] Activa o desactiva el mode "Encabir anacrusis".
     * <p>
     * [EN] Activates or deactivates "Fit anacrusis" mode.
     *
     * @param v [CA] nou valor / [EN] new value
     */
    public static void    setFitAnacrusis(boolean v){ fitAnacrusis = v; }

    /**
     * [CA] Indica si la partitura actual té anacrusis (primer compàs incomplet).
     * <p>
     * [EN] Indicates whether the current score has an anacrusis (incomplete
     * first measure).
     *
     * @return [CA] true si té anacrusis / [EN] true if anacrusis present
     */
    public static boolean isHasAnacrusis()          { return hasAnacrusis; }

    /**
     * [CA] Estableix si la partitura actual té anacrusis.
     * <p>
     * [EN] Sets whether the current score has an anacrusis.
     *
     * @param v [CA] nou valor / [EN] new value
     */
    public static void    setHasAnacrusis(boolean v){ hasAnacrusis = v; }

    // ── Pantalla ──────────────────────────────────────────────────────────

    /**
     * [CA] Retorna la fracció de l'amplada del monitor que ocupa la finestra.
     * <p>
     * [EN] Returns the fraction of the monitor width occupied by the window.
     *
     * @return [CA] ràtio d'amplada / [EN] width ratio
     */
    public static double getScreenWidthRatio()              { return screenWidthRatio; }

    /**
     * [CA] Estableix la fracció de l'amplada del monitor que ocupa la finestra.
     * <p>
     * [EN] Sets the fraction of the monitor width occupied by the window.
     *
     * @param v [CA] ràtio d'amplada / [EN] width ratio
     */
    public static void   setScreenWidthRatio(double v)      { screenWidthRatio = v; }

    /**
     * [CA] Retorna la fracció de l'alçada del monitor que ocupa la finestra.
     * <p>
     * [EN] Returns the fraction of the monitor height occupied by the window.
     *
     * @return [CA] ràtio d'alçada / [EN] height ratio
     */
    public static double getScreenHeightRatio()             { return screenHeightRatio; }

    /**
     * [CA] Estableix la fracció de l'alçada del monitor que ocupa la finestra.
     * <p>
     * [EN] Sets the fraction of the monitor height occupied by the window.
     *
     * @param v [CA] ràtio d'alçada / [EN] height ratio
     */
    public static void   setScreenHeightRatio(double v)     { screenHeightRatio = v; }

    /**
     * [CA] Actualitza les dimensions reals del panell en píxels i recalcula
     * els ràtios de pantalla corresponents.
     * <p>
     * [EN] Updates the actual panel pixel dimensions and recomputes the
     * corresponding screen ratios.
     *
     * @param w [CA] amplada en píxels / [EN] width in pixels
     * @param h [CA] alçada en píxels / [EN] height in pixels
     */
    public static void setScreenPixelDimensions(int w, int h) {
        screenPixelWidth  = w;
        screenPixelHeight = h;
        Dimension monitor = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidthRatio  = w / monitor.getWidth();
        screenHeightRatio = h / monitor.getHeight();
    }

    /**
     * [CA] Retorna l'amplada de pantalla disponible en píxels. Si no s'han
     * establert dimensions reals, calcula el valor a partir del ràtio i la
     * resolució del monitor.
     * <p>
     * [EN] Returns the available screen width in pixels. If no real dimensions
     * have been set, computes the value from the ratio and monitor resolution.
     *
     * @return [CA] amplada en píxels / [EN] width in pixels
     */
    public static double getScreenWidth() {
        return screenPixelWidth > 0
                ? screenPixelWidth
                : Toolkit.getDefaultToolkit().getScreenSize().getWidth() * screenWidthRatio;
    }

    /**
     * [CA] Retorna l'alçada de pantalla disponible en píxels. Si no s'han
     * establert dimensions reals, calcula el valor a partir del ràtio i la
     * resolució del monitor.
     * <p>
     * [EN] Returns the available screen height in pixels. If no real dimensions
     * have been set, computes the value from the ratio and monitor resolution.
     *
     * @return [CA] alçada en píxels / [EN] height in pixels
     */
    public static double getScreenHeight() {
        return screenPixelHeight > 0
                ? screenPixelHeight
                : Toolkit.getDefaultToolkit().getScreenSize().getHeight() * screenHeightRatio;
    }

    // ── Layout derivat: columnes ──────────────────────────────────────────

    /**
     * [CA] Retorna el nombre de columnes per quadrat (resolució mínima).
     * <p>
     * [EN] Returns the number of columns per grid square (minimum resolution).
     *
     * @return [CA] columnes per quadrat / [EN] columns per square
     */
    public static int    getnColsSquare()  { return DEFAULT_NCOLS_SQUARE; }

    /**
     * [CA] Retorna el nombre de columnes visibles a la càmera.
     * <p>
     * [EN] Returns the number of columns visible in the camera viewport.
     *
     * @return [CA] columnes de la càmera / [EN] camera columns
     */
    public static int getnColsCam() {
        return getnMeasuresCam() * getnBeatsMeasure() * getnColsBeat();
    }

    /**
     * [CA] Retorna el nombre de columnes que ocupa el teclat.
     * <p>
     * [EN] Returns the number of columns occupied by the keyboard.
     *
     * @return [CA] columnes del teclat / [EN] keyboard columns
     */
    public static int getnColsKeyboard() { return (int) (getnColsCam() * DEFAULT_KEYBOARD_RATIO); }

    /**
     * [CA] Retorna el nombre de columnes que ocupa el panell de control.
     * <p>
     * [EN] Returns the number of columns occupied by the control panel.
     *
     * @return [CA] columnes del control / [EN] control panel columns
     */
    public static int getnColsControl()  { return (int) (getnColsCam() * DEFAULT_CONTROL_RATIO); }

    /**
     * [CA] Retorna el nombre total de columnes de la pantalla (teclat + càmera + control).
     * <p>
     * [EN] Returns the total number of screen columns (keyboard + camera + control).
     *
     * @return [CA] columnes totals / [EN] total columns
     */
    public static int getnColsScreen()   { return getnColsCam() + getnColsKeyboard() + getnColsControl(); }

    /**
     * [CA] Retorna el nombre de columnes de la franja d'acords (igual que la càmera).
     * <p>
     * [EN] Returns the number of columns in the chord strip (same as camera).
     *
     * @return [CA] columnes de la franja d'acords / [EN] chord strip columns
     */
    public static int getnColsChord()    { return getnColsCam(); }

    /**
     * [CA] Retorna el nombre de columnes de la línia d'estat.
     * <p>
     * [EN] Returns the number of columns in the status line.
     *
     * @return [CA] columnes de l'estat / [EN] status line columns
     */
    public static int getnColsStatus()   { return getnColsScreen(); }

    /**
     * [CA] Retorna el nombre de columnes de la franja de lletra.
     * <p>
     * [EN] Returns the number of columns in the lyrics strip.
     *
     * @return [CA] columnes de la lletra / [EN] lyrics strip columns
     */
    public static int getnColsLyrics()   { return getnColsCam(); }

    /**
     * [CA] Retorna l'amplada d'una columna en píxels.
     * <p>
     * [EN] Returns the width of one column in pixels.
     *
     * @return [CA] amplada de columna en píxels / [EN] column width in pixels
     */
    public static double getColWidth()    { return getScreenWidth() / getnColsScreen(); }

    /**
     * [CA] Retorna l'amplada d'un quadrat (cel·la) en píxels.
     * <p>
     * [EN] Returns the width of one grid square (cell) in pixels.
     *
     * @return [CA] amplada del quadrat en píxels / [EN] square width in pixels
     */
    public static double getSquareWidth() { return getColWidth() * getnColsSquare(); }

    // ── Layout derivat: files ─────────────────────────────────────────────

    /**
     * [CA] Retorna el nombre de files de la partitura (rang MIDI visible).
     * <p>
     * [EN] Returns the number of score rows (visible MIDI range).
     *
     * @return [CA] files de la partitura / [EN] score rows
     */
    public static int getnKeysScore()    { return ToneRange.getMidiRange(); }

    /**
     * [CA] Retorna el nombre de files del teclat de pantalla.
     * <p>
     * [EN] Returns the number of rows in the on-screen keyboard.
     *
     * @return [CA] files del teclat / [EN] keyboard rows
     */
    public static int getnKeysKeyboard() { return getnKeysScore(); }

    /**
     * [CA] Retorna el nombre de files per quadrat (cel·la).
     * <p>
     * [EN] Returns the number of rows per grid square (cell).
     *
     * @return [CA] files per quadrat / [EN] rows per square
     */
    public static int getnRowsSquare()  { return (int) Math.round(ToneRange.getMaxNKeys() / ToneRange.getMidiRange()); }

    /**
     * [CA] Retorna el nombre total de files de la partitura.
     * <p>
     * [EN] Returns the total number of score rows.
     *
     * @return [CA] files totals de la partitura / [EN] total score rows
     */
    public static int getnRowsScore()   { return getnKeysScore() * getnRowsSquare(); }

    /**
     * [CA] Retorna el nombre de files de la franja d'acords.
     * <p>
     * [EN] Returns the number of rows in the chord strip.
     *
     * @return [CA] files de la franja d'acords / [EN] chord strip rows
     */
    public static int getnRowsChord()   { return DEFAULT_NROWS_CHORD; }

    /**
     * [CA] Retorna el nombre de files de la franja de lletra.
     * <p>
     * [EN] Returns the number of rows in the lyrics strip.
     *
     * @return [CA] files de la lletra / [EN] lyrics strip rows
     */
    public static int getnRowsLyrics()  { return DEFAULT_NROWS_LYRICS; }

    /**
     * [CA] Retorna el nombre de files de la línia d'estat.
     * <p>
     * [EN] Returns the number of rows in the status line.
     *
     * @return [CA] files de l'estat / [EN] status line rows
     */
    public static int getnRowsStatus()  { return (int) (getnRowsScore() * DEFAULT_STATUS_RATIO); }

    /**
     * [CA] Retorna el nombre total de files de la pantalla.
     * <p>
     * [EN] Returns the total number of screen rows.
     *
     * @return [CA] files totals / [EN] total rows
     */
    public static int getnRowsScreen()  { return getnRowsScore() + getnRowsChord() + getnRowsLyrics() + getnRowsStatus(); }

    /**
     * [CA] Retorna el nombre de files de la càmera (acords + partitura + lletra).
     * <p>
     * [EN] Returns the number of rows in the camera viewport (chords + score + lyrics).
     *
     * @return [CA] files de la càmera / [EN] camera rows
     */
    public static int getnRowsCam()     { return getnRowsScore() + getnRowsChord() + getnRowsLyrics(); }

    /**
     * [CA] Retorna el nombre de files del teclat de pantalla.
     * <p>
     * [EN] Returns the number of rows in the on-screen keyboard.
     *
     * @return [CA] files del teclat / [EN] keyboard rows
     */
    public static int getnRowsKeyboard(){ return getnRowsScore(); }

    /**
     * [CA] Retorna el nombre de files del panell de control.
     * <p>
     * [EN] Returns the number of rows in the control panel.
     *
     * @return [CA] files del control / [EN] control panel rows
     */
    public static int getnRowsControl() { return getnRowsScore() + getnRowsChord() + getnRowsLyrics(); }

    /**
     * [CA] Retorna el nombre de files d'un botó al panell de control.
     * <p>
     * [EN] Returns the number of rows per button in the control panel.
     *
     * @return [CA] files per botó / [EN] rows per button
     */
    public static int getnRowsButton()  { return DEFAULT_NROWS_BUTTON; }

    /**
     * [CA] Retorna l'alçada d'una fila en píxels.
     * <p>
     * [EN] Returns the height of one row in pixels.
     *
     * @return [CA] alçada de fila en píxels / [EN] row height in pixels
     */
    public static double getRowHeight()    { return getScreenHeight() / getnRowsScreen(); }

    /**
     * [CA] Retorna l'alçada d'un quadrat (cel·la) en píxels.
     * <p>
     * [EN] Returns the height of one grid square (cell) in pixels.
     *
     * @return [CA] alçada del quadrat en píxels / [EN] square height in pixels
     */
    public static double getSquareHeight() { return getRowHeight() * getnRowsSquare(); }

    // ── Layout derivat: posicions (first col/row) ─────────────────────────

    /**
     * [CA] Retorna la primera columna del teclat (0 si és esquerre, o darrere
     * de la càmera si és dret).
     * <p>
     * [EN] Returns the first column of the keyboard (0 if left, or after the
     * camera if right).
     *
     * @param left [CA] true si el teclat és a l'esquerra / [EN] true if keyboard is on the left
     * @return [CA] primera columna del teclat / [EN] first keyboard column
     */
    public static int getKeyboardFirstCol(boolean left) { return left ? 0 : getnColsCam(); }

    /**
     * [CA] Retorna la primera columna de la càmera (0 si el teclat és a la
     * dreta, o darrere del teclat si és a l'esquerra).
     * <p>
     * [EN] Returns the first column of the camera (0 if keyboard is on the
     * right, or after the keyboard if on the left).
     *
     * @param left [CA] true si el teclat és a l'esquerra / [EN] true if keyboard is on the left
     * @return [CA] primera columna de la càmera / [EN] first camera column
     */
    public static int getCamFirstCol(boolean left)      { return left ? getnColsKeyboard() : 0; }

    /**
     * [CA] Retorna la primera columna del panell de control.
     * <p>
     * [EN] Returns the first column of the control panel.
     *
     * @return [CA] primera columna del control / [EN] first control panel column
     */
    public static int getFirstColControl()              { return getnColsKeyboard() + getnColsCam(); }

    /**
     * [CA] Retorna la primera columna de la línia d'estat.
     * <p>
     * [EN] Returns the first column of the status line.
     *
     * @return [CA] primera columna de l'estat / [EN] first status line column
     */
    public static int getStatusFirstCol()               { return 0; }

    /**
     * [CA] Retorna la primera columna de la franja d'acords.
     * <p>
     * [EN] Returns the first column of the chord strip.
     *
     * @return [CA] primera columna dels acords / [EN] first chord strip column
     */
    public static int getChordFirstCol()                { return 0; }

    /**
     * [CA] Retorna la primera columna de la partitura.
     * <p>
     * [EN] Returns the first column of the score.
     *
     * @return [CA] primera columna de la partitura / [EN] first score column
     */
    public static int getScoreFirstCol()                { return 0; }

    /**
     * [CA] Retorna la primera columna de la franja de lletra.
     * <p>
     * [EN] Returns the first column of the lyrics strip.
     *
     * @return [CA] primera columna de la lletra / [EN] first lyrics column
     */
    public static int getLyricsFirstCol()               { return 0; }

    /**
     * [CA] Retorna la primera fila de la càmera.
     * <p>
     * [EN] Returns the first row of the camera viewport.
     *
     * @return [CA] primera fila de la càmera / [EN] first camera row
     */
    public static int getCamFirstRow()      { return 0; }

    /**
     * [CA] Retorna la primera fila de la franja d'acords.
     * <p>
     * [EN] Returns the first row of the chord strip.
     *
     * @return [CA] primera fila dels acords / [EN] first chord strip row
     */
    public static int getChordFirstRow()    { return 0; }

    /**
     * [CA] Retorna la primera fila de la partitura (just sota els acords).
     * <p>
     * [EN] Returns the first row of the score (just below the chords).
     *
     * @return [CA] primera fila de la partitura / [EN] first score row
     */
    public static int getScoreFirstRow()    { return getnRowsChord(); }

    /**
     * [CA] Retorna la primera fila del teclat de pantalla.
     * <p>
     * [EN] Returns the first row of the on-screen keyboard.
     *
     * @return [CA] primera fila del teclat / [EN] first keyboard row
     */
    public static int getKeyboardFirstRow() { return getnRowsChord(); }

    /**
     * [CA] Retorna la primera fila de la franja de lletra.
     * <p>
     * [EN] Returns the first row of the lyrics strip.
     *
     * @return [CA] primera fila de la lletra / [EN] first lyrics row
     */
    public static int getLyricsFirstRow()   { return getnRowsChord() + getnRowsScore(); }

    /**
     * [CA] Retorna la primera fila del panell de control.
     * <p>
     * [EN] Returns the first row of the control panel.
     *
     * @return [CA] primera fila del control / [EN] first control panel row
     */
    public static int getControlFirstRow()  { return 0; }

    /**
     * [CA] Retorna la primera fila de la línia d'estat.
     * <p>
     * [EN] Returns the first row of the status line.
     *
     * @return [CA] primera fila de l'estat / [EN] first status line row
     */
    public static int getStatusFirstRow()   { return getnRowsScore() + getnRowsChord() + getnRowsLyrics(); }

    // ── Layout derivat: botons ────────────────────────────────────────────

    /**
     * [CA] Retorna el nombre de columnes d'un botó al panell de control.
     * <p>
     * [EN] Returns the number of columns per button in the control panel.
     *
     * @return [CA] columnes per botó / [EN] columns per button
     */
    public static int getnColsButton()   { return (int) Math.round(getnColsControl() / 4.0); }

    /**
     * [CA] Retorna la columna del primer botó al panell de control.
     * <p>
     * [EN] Returns the column of the first button in the control panel.
     *
     * @return [CA] columna del primer botó / [EN] first button column
     */
    public static int getFirstColButton(){ return (int) Math.round(getnColsButton() / 2.0); }

    /**
     * [CA] Retorna la separació entre centres de botons consecutius.
     * <p>
     * [EN] Returns the separation between centres of consecutive buttons.
     *
     * @return [CA] separació entre botons / [EN] button separation
     */
    public static int getSepColsButton() { return (int) Math.round(getnColsButton() * 1.1); }

    // ── Playbar i posició inicial ─────────────────────────────────────────

    /**
     * [CA] Retorna la columna de pantalla del playbar (barra de reproducció).
     * <p>
     * [EN] Returns the screen column of the playbar (playback bar).
     *
     * @param left [CA] true si el teclat és a l'esquerra / [EN] true if keyboard is on the left
     * @return [CA] columna del playbar / [EN] playbar column
     */
    public static int getPlayBarCol(boolean left) { return left ? 0 : getnColsCam(); }

    /**
     * [CA] Retorna la columna inicial de la partitura (currentCol) per a la
     * posició de la càmera en arrencar.
     * <p>
     * [EN] Returns the initial score column (currentCol) for the camera
     * position at startup.
     *
     * @param left  [CA] true si el teclat és a l'esquerra / [EN] true if keyboard is on the left
     * @param score [CA] graella de partitura (per obtenir el delay) /
     *              [EN] grid score (to obtain the delay)
     * @return [CA] columna inicial / [EN] initial column
     */
    public static int getInitialCurrentCol(boolean left, MyGridScore score) {
        return left ? getnColsCam() : -score.getDelay(left);
    }

    // ── Reproducció ───────────────────────────────────────────────────────

    /**
     * [CA] Indica si la reproducció s'alinea als beats (mode PLAY_AT_BEAT).
     * <p>
     * [EN] Indicates whether playback aligns to beats (PLAY_AT_BEAT mode).
     *
     * @return [CA] true si s'alinea als beats / [EN] true if aligned to beats
     */
    public static boolean isPlayAtBeat() { return PLAY_AT_BEAT; }
}
