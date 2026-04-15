package dodecagraphone.ui;

import dodecagraphone.model.ToneRange;
import dodecagraphone.model.component.MyGridScore;
import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * Management of the application settings, including default values. Among other
 * things, settings define the layout of the different graphic components.
 *
 * The layout is the following: The graphic window is a JFrame/JPanel window
 * (see MyUserInterface). The title bar, at the top, includes the title of the
 * application. The screen is the content of the Jpanel. The screen is organized
 * in rows and columns. A square (1 col x 1 row) is the elementary grahic
 * element. In the following, when not stated otherwise, dimensions are given in
 * nubmer of rows and number of columns, with respect to the parent component.
 * In the following we use "bar" for vertical strips, and "line" for horizontal
 * ones
 *
 * The screen is divided into the following areas:
 *
 * The keyboard: the colored strips that produce sound. The keyboard can be
 * placed on the left side of the screen (for reading exercises) or on the right
 * side (for guessing exercises). The control panel: At the right side. It
 * includes the buttons that run the application. The status line: At the bottom
 * of the screen. It shows the messages that the application sends to the user.
 * The camera panel: At the center of the screen we see the contents of the
 * camera. The camera includes the score and the chord symbol line. The score:
 * The score has as many rows as keys in the keyboard, and as many columns as
 * required for the current musical score. Only the visible part of the score is
 * shown in the camera. When playing, the score moves right to left. Beat and
 * Measure lines are shown as well. The currentCol of the score is the first
 * column of the score that is out of sight in the camera. The chord symbol
 * line: Above the score, a horizontal strip shows the symbol of the chord of
 * the current beat or measure (like in fake book scores). The chord symbol line
 * moves along with the score. The play bar: A dim square on the score shows the
 * current play bar. When the play bar hits the keyboard, the active (ON) notes
 * are played. The play bar is placed right next to the keyboard when the
 * keyboard is on the left, and a couple of columns inside the keyboard when the
 * keyboard is on the right. The idea is to guess the current sound before it
 * appears on the camera. (See PLAYBAR_OFFSET).
 *
 * @author pau
 */
public class Settings {

    // Various
    public static final boolean COLORS_BU = true;
    public static final boolean IS_BU = true;
    public static final int REFRESH_PERIOD = 30; // 30ms -> ~33fps
    public static final boolean SHOW_DRAW_HIERARCHY = false;
    public static final boolean SHOW_DIMENSIONS = false;
    public static int PRINT_OUT_PRIORITY = 1; // 1 Max priority, 0 no printOut
    public static final boolean PLAY_AT_BEAT = false;
    public static final int USE_INFO_AS_SIMBOL = -13;
    public static final int IS_STOP_CHORD = -14;
    public static final int REST = -15;
    public static final int END = -16;
    public static final int INVALID_CHORD = -16;
    public static final int NUM_SIBLINGS = 5; // Siblings displayed when showDimensions().
    public static final boolean DEFAULT_AUTO_CORRECT = true;
    public static final boolean DEFAULT_TIPS_VISIBLE = true;
    public static final boolean DEFAULT_SHOW_MUTTED = true;
    public static final int MAX_BPM = 300;

    private static final double DEFAULT_SCREEN_WIDTH_RATIO = 0.95; // config
    private static final double DEFAULT_SCREEN_HEIGHT_RATIO = 0.8;
    private static final double DEFAULT_KEYBOARD_RATIO = 2.4 / 16;
    private static final double DEFAULT_CONTROL_RATIO = 4.6 / 16;
    private static final double DEFAULT_CHORD_RATIO = 6.0 / 59;
    private static final double DEFAULT_STATUS_RATIO = 4.5 / 59;
    private static final int DEFAULT_NCOLS_SQUARE = 1;
    private static final int DEFAULT_NROWS_BUTTON = 2;
    private static final boolean CHORD_SYMBOL_VERTICAL = true;

//    // Buttons
////    private static final int DEFAULT_CONTROL_W = 60;
//    
//    // Score
//    // Independent
    public static final int DEFAULT_NMEASURES_CAM = 4; // config
    private static final int DEFAULT_NBEATS_MEASURE = 4; // time signature
    private static final int DEFAULT_NCOLS_QUARTER = 12; // config
    private static final int DEFAULT_BEAT_FIGURE = 4; // Time sign. denominator (4 -> quarter notes)    
    private static final int DEFAULT_NCOLS_SCORE = 12000;
//
//    // Keyboard
////    private static final int MAX_NKEYS_KEYBOARD = ToneRange.getMaxNKeys();
////    private static final int DEFAULT_NKEYS_KEYBOARD = ToneRange.getMidiRange();
//    private static final int DEFAULT_KEYBOARD_W = 30;
    public static final double KEY_WIDTH_REDUCTION = 0.75; // Percentage of Xilokey vs Slides.
//    
//    // Status
//    private static final int DEFAULT_STATUS_H = 3;
//
//    // Chordbar
//    private static final int DEFAULT_CHORDBAR_H = 4;
//    
//    // Screen
//    private static final int DEFAULT_NCOLS_SCREEN = DEFAULT_NMEASURES_CAM * DEFAULT_NBEATS_MEASURE * DEFAULT_NCOLS_BEAT + DEFAULT_KEYBOARD_W + DEFAULT_CONTROL_W;
//
//    // Score and GridSquare
//    /** 
//     * A grid Square may have several rows.
//     */
//    private static final int DEFAULT_NCOLS_CAM = DEFAULT_NCOLS_SCREEN - DEFAULT_KEYBOARD_W - DEFAULT_CONTROL_W;
//    private static final int DEFAULT_NROWS_SQUARE = (int) Math.round(MAX_NKEYS_KEYBOARD/DEFAULT_NKEYS_KEYBOARD);
//    private static final int DEFAULT_NROWS_SCORE = DEFAULT_NKEYS_KEYBOARD * DEFAULT_NROWS_SQUARE;
//    private static final int DEFAULT_NCOLS_SCORE = DEFAULT_NCOLS_SCREEN * 12 * 4 + 10;
    public static final int END_COL_MARGIN = 250;
//
//    // Screen
//    private static final int DEFAULT_NROWS_SCREEN = DEFAULT_NROWS_SCORE + DEFAULT_CHORDBAR_H + DEFAULT_STATUS_H;
//
//    // Tempo
    public static final int DEFAULT_TEMPO = 60;
//
    // Diatonic do scale
    public static final Integer[] DEFAULT_CHOICE = {0, 2, 4, 5, 7, 9, 11, 12};
    public static final Integer[] DEFAULT_CHOICE_2OCTAVES = {0, 2, 4, 5, 7, 9, 11, 12, 14, 16, 17, 19, 21, 23, 24};
    public static final Integer[] DEFAULT_MINOR_CHOICE = {0, 2, 3, 5, 7, 8, 10, 12};
    public static final Integer[] DEFAULT_MINOR_CHOICE_2OCTAVES = {0, 2, 3, 5, 7, 8, 10, 12, 14, 15, 17, 19, 20, 22, 24};
    //private static final int DEFAULT_KEY = ToneRange.MIDDLE_C; 

    // Keyboard left (in number of row or columns)
//---------------------------------------------------------------------------
    // Resetable parameters
    private static int nMeasuresCam;
    private static int nBeatsMeasure;
    private static int nColsBeat;
    private static int nColsQuarter;
    private static int beatFigure; // Numerator of time signature
    private static int nColsScore;
    private static boolean chordSymbolVertical;
    private static boolean autoCorrect;
    private static boolean tipsVisible;
    /**
     * When set, draws an X on mutted notes.
     */
    private static boolean showMutted;
    private static double screenWidthRatio;
    private static double screenHeightRatio;

//
//    private static int playBarCol; // column of the play bar 
//    private static int initialCurrentCol; // first score col after camera
//    private static final int squareWidth;
//    private static final int squareHeigth;
//    private static int colWidth;
//    private static final int rowHeight;
//    private static final int screenWidth;
//    private static final int screenHeight;
//    private static final int nRowsScreen;
//    private static final int nRowsScore;
//    private static final int nColsScore;
////    private static final double buttonW;
////    private static final double buttonH;
//    private static int firstColCam;
//    private static int nRowsCam;
//    private static int firstRowCam;
//    private static int nColsControl;
//    private static int firstColControl;
//    private static int nRowsControl;
//    private static int firstRowControl;
//    private static int nColsStatus;
//    private static int firstColStatus;
//    private static int firstRowStatus;
//    private static int nRowsStatus;
//    private static int nColsChord;
//    private static int firstColChord;
//    private static int firstRowChord;
//    private static int nRowsChord;
//    private static int firstColScore;
//    private static int firstRowScore;
//    private static int nColsKeyboard;
//    private static int firstColKeyboard;
//    private static int nRowsKeyboard;
//    private static int firstRowKeyboard;
//    private static final int nKeysKeyboard;
//    private static final int nColsSquare;
//    
//    private static final int nColsButton;
//    private static final int firstColButton;
//    private static final int sepColSButton;
//    private static final int nRowsButton;
//    private static final int nRowsSquare;
//    
    public static void initSettings() {
        // Independent parameters
        autoCorrect = Boolean.parseBoolean(AppConfig.get().get("autoCorrect", "" + DEFAULT_AUTO_CORRECT));
        tipsVisible = Boolean.parseBoolean(AppConfig.get().get("tipsVisible", "" + DEFAULT_TIPS_VISIBLE));
        showMutted = Boolean.parseBoolean(AppConfig.get().get("showMutted", "" + DEFAULT_SHOW_MUTTED));

        nMeasuresCam = Integer.parseInt(AppConfig.get().get("nMeasuresCam", "" + DEFAULT_NMEASURES_CAM));
//        nMeasuresCam = DEFAULT_NMEASURES_CAM;
        nColsQuarter = Integer.parseInt(AppConfig.get().get("nColsQuarter", "" + DEFAULT_NCOLS_QUARTER));
        nColsScore = Integer.parseInt(AppConfig.get().get("nColsScore", "" + DEFAULT_NCOLS_SCORE));
//        nBeatsMeasure = Integer.parseInt(AppConfig.get().get("nBeatsMeasure", "" + DEFAULT_NBEATS_MEASURE));
        nBeatsMeasure = DEFAULT_NBEATS_MEASURE; // time signature
        beatFigure = DEFAULT_BEAT_FIGURE; // time signature
        nColsBeat = nColsQuarter;

        screenWidthRatio = Double.parseDouble(AppConfig.get().get("screenWidthRatio", "" + DEFAULT_SCREEN_WIDTH_RATIO));
        screenHeightRatio = Double.parseDouble(AppConfig.get().get("screenHeightRatio", "" + DEFAULT_SCREEN_HEIGHT_RATIO));

//        int n = getnColsScore();
//        n = getnColsSquare(); // 
        chordSymbolVertical = CHORD_SYMBOL_VERTICAL;
        String language = AppConfig.get().get("ui.language", "en");
        I18n.initFromLanguageTag(language);
//        // Dependent parameters
//        n = getnColsCam();
//        n = getnColsKeyboard(); // DKR
//        n = getnColsControl(); // DCR
//        n = getnColsScreen();
//        double x = getColWidth(); // screen
//        x = getSquareWidth();
//        
//        getnKeysScore(); // tone range
//        getnRowsSquare(); // tone range
//        getnRowsScore(); // 
//        getnRowsChord();
//        getnRowsStatus();
//        getnRowsScreen();
//        getRowHeight(); // screen
//        getSquareHeight();
//        // screenWidth
//        // screenHeight
//        getnColsButton();
//        getFirstColButton();
//        getSepColsButton();
//        getnRowsButton();
//        getnKeysKeyboard();
    }

    public static int getDefaultDelay(){
        return Settings.getnColsQuarter();
    }
    
    public static int getnColsQuarter() {
        return nColsQuarter;
    }

    public static void setnColsQuarter(int nColsQuarter) {
        Settings.nColsQuarter = nColsQuarter;
    }

    public static void setnMeasuresCam(int nm) {
        nMeasuresCam = nm;
    }

    public static int getnMeasuresCam() {
        return nMeasuresCam;
    }

    public static void setnBeatsMeasure(int nBeatsMeasure) {
        Settings.nBeatsMeasure = nBeatsMeasure;
    }

    public static int getnBeatsMeasure() {
        return nBeatsMeasure;
    }

    public static void updateNColsBeat() {
        nColsBeat = nColsQuarter;
        if (beatFigure == 8) {
            nColsBeat = (int) (nColsBeat * 1.5);
        }
    }

    public static int getnColsBeat() {
        return nColsBeat;
    }

    public static void setBeatFigure(int bf) {
        beatFigure = bf;
    }

    public static int getBeatFigure() {
        return beatFigure;
    }

    public static void setnColsScore(int ncs) {
        nColsScore = ncs;
    }

    public static int getnColsScore() {
        return nColsScore;
    }

    public static int getnColsSquare() {
        return DEFAULT_NCOLS_SQUARE;
    }

    public static boolean isTipsVisible() {
        return tipsVisible;
    }

    public static void setTipsVisible(boolean tipsVisible) {
        Settings.tipsVisible = tipsVisible;
    }

    public static boolean isAutoCorrect() {
        return autoCorrect;
    }

    public static void setAutoCorrect(boolean autoCorrect) {
        Settings.autoCorrect = autoCorrect;
    }

    public static double getScreenWidthRatio() {
        return screenWidthRatio;
    }

    public static void setScreenWidthRatio(double screenWidthRatio) {
        Settings.screenWidthRatio = screenWidthRatio;
    }

    public static double getScreenHeightRatio() {
        return screenHeightRatio;
    }

    public static void setScreenHeightRatio(double screenHeightRatio) {
        Settings.screenHeightRatio = screenHeightRatio;
    }

    /**
     * getter.
     *
     * @return
     */
    public static boolean isPlayAtBeat() {
        return Settings.PLAY_AT_BEAT;
    }

    public static boolean isShowMutted() {
        return showMutted;
    }

    /**
     * setter.
     *
     * @param showMutted
     */
    public static void setShowMutted(boolean shMutted) {
        showMutted = shMutted;
    }

    // Dependent variables
    public static int getnColsCam() {
        int nmc = getnMeasuresCam();
        int nbm = getnBeatsMeasure();
        int ncb = getnColsBeat();
        int ncc = nmc * nbm * ncb;
        // return getnMeasuresCam()*getnBeatsMeasure()*getnColsBeat();
        return ncc;
    }

    public static int getnColsKeyboard() {
        return (int) (getnColsCam() * DEFAULT_KEYBOARD_RATIO);
    }

    public static int getnColsControl() {
        return (int) (getnColsCam() * DEFAULT_CONTROL_RATIO);
    }

    public static int getnColsScreen() {
        return getnColsCam() + getnColsKeyboard() + getnColsControl();
    }

    public static double getColWidth() {
        return getScreenWidth() / getnColsScreen();
    }

    public static double getSquareWidth() {
        return getColWidth() * getnColsSquare();
    }

    public static int getnKeysScore() {
        return ToneRange.getMidiRange();
    }

    public static int getnRowsSquare() {
        return (int) Math.round(ToneRange.getMaxNKeys() / ToneRange.getMidiRange());
    }

    public static int getnRowsScore() {
        return getnKeysScore() * getnRowsSquare();
    }

    public static int getnRowsChord() {
        return (int) (getnRowsScore() * DEFAULT_CHORD_RATIO);
    }

    public static int getnRowsStatus() {
        return (int) (getnRowsScore() * DEFAULT_STATUS_RATIO);
    }

    public static int getnRowsScreen() {
        return getnRowsScore() + getnRowsChord() + getnRowsStatus();
    }

    public static double getScreenHeight() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return screenSize.getHeight() * screenHeightRatio;
    }

    public static double getScreenWidth() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return screenSize.getWidth() * screenWidthRatio;
    }

    public static double getRowHeight() {
        return getScreenHeight() / getnRowsScreen();
    }

    public static double getSquareHeight() {
        return getRowHeight() * getnRowsSquare();
    }

    public static int getnColsButton() {
        return (int) Math.round(getnColsControl() / 4);
    }

    public static int getFirstColButton() {
        return (int) Math.round(getnColsButton() / 2);
    }

    public static int getSepColsButton() {
        return (int) Math.round(getnColsButton() * 1.1);
    }

    public static int getnRowsButton() {
        return DEFAULT_NROWS_BUTTON;
    }

    public static int getFirstColControl() {
        return getnColsKeyboard() + getnColsCam();
    }

    public static boolean isChordSymbolVertical() {
        return chordSymbolVertical;
    }

    public static void setChordSymbolVertical(boolean chordSymbolVertical) {
        Settings.chordSymbolVertical = chordSymbolVertical;
    }

    public static int getnKeysKeyboard() {
        return getnKeysScore();
    }

    public static int getKeyboardFirstCol(boolean left) {
        if (left) {
            return 0;
        } else {
            return getnColsCam();
        }
    }

    public static int getCamFirstCol(boolean left) {
        if (!left) {
            return 0; //(getnColsCam() + getnColsKeyboard());
        } else {
            return getnColsKeyboard();
        }
    }

    public static int getControlFirstCol() {
        return getnColsKeyboard() + getnColsCam();
    }

    public static int getStatusFirstCol() {
        return 0;
    }

    public static int getnColsStatus() {
        return getnColsScreen();
    }

    public static int getChordFirstCol() {
        return 0; // ? wrt cam? // getCamFirstCol(left); 
    }

    public static int getnColsChord() {
        return getnColsCam();
    }

    public static int getChordFirstRow() {
        return 0;
    }

    public static int getScoreFirstCol() {
        return 0;
    }

    public static int getScoreFirstRow() {
        return getnRowsChord();
    }

    public static int getKeyboardFirstRow() {
        return getnRowsChord();
    }

    public static int getnRowsKeyboard() {
        return getnRowsScore();
    }

    public static int getCamFirstRow() {
        return 0;
    }

    public static int getnRowsCam() {
        return getnRowsScore() + getnRowsChord();
    }

    public static int getControlFirstRow() {
        return 0;
    }

    public static int getnRowsControl() {
        return getnRowsScore() + getnRowsChord();
    }

    public static int getStatusFirstRow() {
        return getnRowsScore() + getnRowsChord();
    }

    public static int getPlayBarCol(boolean left) {
        if (left) {
            return 0;
        } else {
            return getnColsCam();
        }
    }

    public static int getInitialCurrentCol(boolean left, MyGridScore score) {
        if (left) {
            return getnColsCam();
        } else {
            return (- score.getDelay(left));
        }
    }

    public static int getDefaultTempo() {
        return DEFAULT_TEMPO;
    }
//----------------------------------------------------------    

//    
//    public static int getnColsMeasure(){
//        return getnColsBeat()*getnBeatsMeasure();
//    }
//
//    /**
//     * S'assigna des de la partitura o exercisi.
//     * @param nBeatsMeasure 
//     */
////    public static void setNumColsBeat(){
////        nColsBeat = (DEFAULT_NCOLS_CAM)/(getNMeasuresCam()*getNumBeatsMeasure());
////        setColWidth();
////    }
////    
//    public static int getSquareWitdth(){
//        return squareWidth;
//    }
//    
//    public static void setColWidth(){
//        int nbs = getNMeasuresCam();
//        int ncb = getnColsBeat();
//        int nbm = getNBeatsMeasure();
//        int dcc = DEFAULT_NCOLS_CAM;
//  
//        colWidth = DEFAULT_NCOLS_CAM / (nbm*nbs*ncb);
////        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
////        colWidth = (int) Math.floor((screenSize.getWidth()/DEFAULT_NCOLS_SCREEN));
//    }
//    
//    public static void setDimensionsKeyboardLeft(boolean left){
//        getKeyboardFirstCol(left);
//        getCamFirstCol(left);
//        getControlFirstCol();
//        getStatusFirstCol();
//        getnColsStatus();
//        getChordFirstCol();
//        getnColsChord();
//        getScoreFirstCol();
//        getKeyboardFirstRow();
//        getnRowsKeyboard();
//        getCamFirstRow();
//        getnRowsCam();
//        getControlFirstRow();
//        getnRowsControl();
//        getStatusFirstRow();
//        getnRowsStatus();
//        getChordFirstRow();
//        getnRowsChord();
//        getScoreFirstRow();
//        getPlayBarCol(left);
//        getInitialCurrentCol();
//    }
//    
//    public static int getSepColSButton() {
//        return sepColSButton;
//    }
//
//    public static int getScreenWidth() {
//        return screenWidth;
//    }
//
//    public static int getScreenHeight() {
//        return screenHeight;
//    }
//
//    public static int getDefaultKey(){
//        return DEFAULT_KEY;
//    }
//    
//   public static char getDefaultMode(){
//        return DEFAULT_MODE;
//    }
//    
//    public static int getInitialCurrentCol() {
//        return initialCurrentCol;
//    }
//
//    public static int getFirstRowCam() {
//        return firstRowCam;
//    }
//
//    public static int getFirstRowControl() {
//        return firstRowControl;
//    }
//
//    public static int getFirstRowStatus() {
//        return firstRowStatus;
//    }
//
//    public static int getnRowsStatus() {
//        return nRowsStatus;
//    }
//
//    public static int getFirstRowScore() {
//        return firstRowScore;
//    }
//
//    public static int getnRowsScore() {
//        return nRowsScore;
//    }
//
//    public static int getFirstRowChord() {
//        return firstRowChord;
//    }
//
//    public static int getnRowsChord() {
//        return nRowsChord;
//    }
//
//    public static int getnRowsKeyboard() {
//        return nRowsKeyboard;
//    }
//
//    public static int getFirstRowKeyboard() {
//        return firstRowKeyboard;
//    }
//
//    public static int getnColsStatus() {
//        return nColsStatus;
//    }
//
//    public static int getFirstColStatus() {
//        return firstColStatus;
//    }
//
//    public static int getnColsScore() {
//        return nColsScore;
//    }
//
//    public static int getFirstColScore() {
//        return firstColScore;
//    }
//
//    public static int getnColsChord() {
//        return nColsChord;
//    }
//
//    public static int getFirstColChord() {
//        return firstColChord;
//    }
//
//    public static int getFirstColKeyboard() {
//        return firstColKeyboard;
//    }
//
//    public static int getFirstColCam() {
//        return firstColCam;
//    }
//
//    public static int getPlayBarCol(){
//        return playBarCol;
//    }
//    
//    public static int getDefaultTempo(){
//        return DEFAULT_TEMPO;
//    }
//    
//    public static double getKeyboardW() {
//        return nColsKeyboard * getSquareWitdth();
//    }
//
//    public static double getControlW() {
//        return nColsControl * getSquareWitdth();
//    }
//
////    public static double getCameraW() {
////        return nColsCam * getSquareWitdth();
////    }
////
//    public static double getKeyboardX() {
//        return firstColKeyboard * getSquareWitdth();
//    }
//
//    public static double getCameraX() {
//        return firstColCam * getSquareWitdth();
//    }
//
//    public static double getControlX() {
//        return firstColControl * getSquareWitdth();
//    }
//
//    public static double getStatusX() {
//        return firstColStatus * getSquareWitdth();
//    }
//
//    public static double getStatusW() {
//        return nColsStatus * getSquareWitdth();
//    }
//
//    public static double getChordX() {
//        return firstColChord * getSquareWitdth();
//    }
//
//    public static double getChordW() {
//        return nColsChord * getSquareWitdth();
//    }
//
//    public static double getKeyboardY() {
//        return firstRowKeyboard * getRowHeight();
//    }
//
//    public static double getKeyboardH() {
//        return nRowsKeyboard * getRowHeight();
//    }
//
//    public static double getCameraY() {
//        return firstRowCam * getRowHeight();
//    }
//
//    public static double getCameraH() {
//        return nRowsCam * getRowHeight();
//    }
//
//    public static double getControlY() {
//        return firstRowControl * getRowHeight();
//    }
//
//    public static double getControlH() {
//        return nRowsControl * getRowHeight();
//    }
//
//    public static double getStatusY() {
//        return firstRowStatus * getRowHeight();
//    }
//
//    public static double getStatusH() {
//        return nRowsStatus * getRowHeight();
//    }
//
//    public static double getChordY() {
//        return firstRowChord * getRowHeight();
//    }
//
//    public static double getChordH() {
//        return nRowsChord * getRowHeight();
//    }
//
//
//    public static void main(String[] args){
//        //Settings.initSettings();
//        boolean left = true;
//        if (left) {
//            Settings.setDimensionsKeyboardLeft();
//        } else {
//            Settings.setDimensionsKeyboardRight();
//        }
//        System.out.println("nRowsSquare = "+getnRowsSquare());
//    }
}
