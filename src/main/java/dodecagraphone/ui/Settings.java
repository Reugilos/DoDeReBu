package dodecagraphone.ui;

import dodecagraphone.model.ToneRange;
import dodecagraphone.model.component.MyGridScore;
import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * Paràmetres globals de l'aplicació. Inclou valors per defecte, estat
 * configurable i mètodes derivats de layout.
 *
 * Layout general: JFrame/JPanel dividit en teclat (esquerra o dreta),
 * càmera (centre), panell de control (dreta) i línia d'estat (baix).
 * La càmera conté: franja d'acords (dalt), partitura (centre) i lletra (baix).
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
    private static boolean fitAnacrusis;
    private static boolean hasAnacrusis;

    // ── Inicialització ────────────────────────────────────────────────────

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

    public static int  getnMeasuresCam()        { return nMeasuresCam; }
    public static void setnMeasuresCam(int nm)  { nMeasuresCam = nm; }

    public static int  getnBeatsMeasure()              { return nBeatsMeasure; }
    public static void setnBeatsMeasure(int nBm)       { nBeatsMeasure = nBm; }

    public static int  getBeatFigure()           { return beatFigure; }
    public static void setBeatFigure(int bf)     { beatFigure = bf; }

    public static int  getnColsQuarter()                 { return nColsQuarter; }
    public static void setnColsQuarter(int nColsQuarter) { Settings.nColsQuarter = nColsQuarter; }

    public static int  getnColsBeat()  { return nColsBeat; }

    public static void updateNColsBeat() {
        nColsBeat = nColsQuarter;
        if (beatFigure == 8) nColsBeat = (int) (nColsBeat * 1.5);
    }

    public static int  getnColsScore()      { return nColsScore; }
    public static void setnColsScore(int n) { nColsScore = n; }

    public static int getDefaultDelay()  { return getnColsQuarter(); }
    public static int getDefaultTempo()  { return DEFAULT_TEMPO; }

    // ── UI: preferències de l'usuari ──────────────────────────────────────

    public static boolean isAutoCorrect()               { return autoCorrect; }
    public static void    setAutoCorrect(boolean v)     { autoCorrect = v; }

    public static boolean isTipsVisible()               { return tipsVisible; }
    public static void    setTipsVisible(boolean v)     { tipsVisible = v; }

    public static boolean isShowMutted()                { return showMutted; }
    public static void    setShowMutted(boolean v)      { showMutted = v; }

    public static boolean isChordSymbolVertical()           { return chordSymbolVertical; }
    public static void    setChordSymbolVertical(boolean v) { chordSymbolVertical = v; }

    // ── Anacrusis ─────────────────────────────────────────────────────────

    public static boolean isFitAnacrusis()          { return fitAnacrusis; }
    public static void    setFitAnacrusis(boolean v){ fitAnacrusis = v; }

    public static boolean isHasAnacrusis()          { return hasAnacrusis; }
    public static void    setHasAnacrusis(boolean v){ hasAnacrusis = v; }

    // ── Pantalla ──────────────────────────────────────────────────────────

    public static double getScreenWidthRatio()              { return screenWidthRatio; }
    public static void   setScreenWidthRatio(double v)      { screenWidthRatio = v; }

    public static double getScreenHeightRatio()             { return screenHeightRatio; }
    public static void   setScreenHeightRatio(double v)     { screenHeightRatio = v; }

    public static double getScreenWidth() {
        return Toolkit.getDefaultToolkit().getScreenSize().getWidth() * screenWidthRatio;
    }

    public static double getScreenHeight() {
        return Toolkit.getDefaultToolkit().getScreenSize().getHeight() * screenHeightRatio;
    }

    // ── Layout derivat: columnes ──────────────────────────────────────────

    public static int    getnColsSquare()  { return DEFAULT_NCOLS_SQUARE; }

    public static int getnColsCam() {
        return getnMeasuresCam() * getnBeatsMeasure() * getnColsBeat();
    }

    public static int getnColsKeyboard() { return (int) (getnColsCam() * DEFAULT_KEYBOARD_RATIO); }
    public static int getnColsControl()  { return (int) (getnColsCam() * DEFAULT_CONTROL_RATIO); }
    public static int getnColsScreen()   { return getnColsCam() + getnColsKeyboard() + getnColsControl(); }
    public static int getnColsChord()    { return getnColsCam(); }
    public static int getnColsStatus()   { return getnColsScreen(); }
    public static int getnColsLyrics()   { return getnColsCam(); }

    public static double getColWidth()    { return getScreenWidth() / getnColsScreen(); }
    public static double getSquareWidth() { return getColWidth() * getnColsSquare(); }

    // ── Layout derivat: files ─────────────────────────────────────────────

    public static int getnKeysScore()    { return ToneRange.getMidiRange(); }
    public static int getnKeysKeyboard() { return getnKeysScore(); }

    public static int getnRowsSquare()  { return (int) Math.round(ToneRange.getMaxNKeys() / ToneRange.getMidiRange()); }
    public static int getnRowsScore()   { return getnKeysScore() * getnRowsSquare(); }
    public static int getnRowsChord()   { return DEFAULT_NROWS_CHORD; }
    public static int getnRowsLyrics()  { return DEFAULT_NROWS_LYRICS; }
    public static int getnRowsStatus()  { return (int) (getnRowsScore() * DEFAULT_STATUS_RATIO); }
    public static int getnRowsScreen()  { return getnRowsScore() + getnRowsChord() + getnRowsLyrics() + getnRowsStatus(); }
    public static int getnRowsCam()     { return getnRowsScore() + getnRowsChord() + getnRowsLyrics(); }
    public static int getnRowsKeyboard(){ return getnRowsScore(); }
    public static int getnRowsControl() { return getnRowsScore() + getnRowsChord() + getnRowsLyrics(); }
    public static int getnRowsButton()  { return DEFAULT_NROWS_BUTTON; }

    public static double getRowHeight()    { return getScreenHeight() / getnRowsScreen(); }
    public static double getSquareHeight() { return getRowHeight() * getnRowsSquare(); }

    // ── Layout derivat: posicions (first col/row) ─────────────────────────

    public static int getKeyboardFirstCol(boolean left) { return left ? 0 : getnColsCam(); }
    public static int getCamFirstCol(boolean left)      { return left ? getnColsKeyboard() : 0; }
    public static int getFirstColControl()              { return getnColsKeyboard() + getnColsCam(); }
    public static int getStatusFirstCol()               { return 0; }
    public static int getChordFirstCol()                { return 0; }
    public static int getScoreFirstCol()                { return 0; }
    public static int getLyricsFirstCol()               { return 0; }

    public static int getCamFirstRow()      { return 0; }
    public static int getChordFirstRow()    { return 0; }
    public static int getScoreFirstRow()    { return getnRowsChord(); }
    public static int getKeyboardFirstRow() { return getnRowsChord(); }
    public static int getLyricsFirstRow()   { return getnRowsChord() + getnRowsScore(); }
    public static int getControlFirstRow()  { return 0; }
    public static int getStatusFirstRow()   { return getnRowsScore() + getnRowsChord() + getnRowsLyrics(); }

    // ── Layout derivat: botons ────────────────────────────────────────────

    public static int getnColsButton()   { return (int) Math.round(getnColsControl() / 4.0); }
    public static int getFirstColButton(){ return (int) Math.round(getnColsButton() / 2.0); }
    public static int getSepColsButton() { return (int) Math.round(getnColsButton() * 1.1); }

    // ── Playbar i posició inicial ─────────────────────────────────────────

    public static int getPlayBarCol(boolean left) { return left ? 0 : getnColsCam(); }

    public static int getInitialCurrentCol(boolean left, MyGridScore score) {
        return left ? getnColsCam() : -score.getDelay(left);
    }

    // ── Reproducció ───────────────────────────────────────────────────────

    public static boolean isPlayAtBeat() { return PLAY_AT_BEAT; }
}
