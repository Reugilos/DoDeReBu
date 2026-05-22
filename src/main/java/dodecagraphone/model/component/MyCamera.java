/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.*;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 * [CA] La càmera conté la partitura, la franja d'acords i la barra de reproducció.
 * Mostra la part visible de la partitura i de la franja d'acords. Quan la càmera
 * reprodueix, la partitura i la franja d'acords es mouen cap a l'esquerra. La barra
 * de reproducció té dues posicions possibles, depenent de la posició del teclat.
 * La partitura s'organitza en pàgines amb mètodes per canviar de pàgina.
 * La columna de partitura visible depèn de {@code MyGridScore::currentCol},
 * que és la primera columna oculta a la dreta de la càmera.
 * <p>
 * [EN] The Camera includes the score, the chord symbol line and the play bar.
 * The camera shows the visible part of the score and the chord symbol line.
 * When the camera is playing the score and the chord symbol line move to
 * the left. The play bar has two positions depending on the keyboard position.
 * The score within the camera is organized in pages, and there are methods
 * to change the page. The visible part of the score depends on
 * {@code MyGridScore::currentCol}, which is the first score column hidden
 * to the right of the camera.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MyCamera extends MyComponent {

    /** The score. The camera shows only the visible part of the score. */
    public MyGridScore score;
    /** The chord symbol line. An horizontal strip where chords can be
     * displayed at their corresponding beat and measure (as in a fake book).*/
    public MyChordSymbolLine chordSymbolLine;
    /** The lyrics strip below the score. */
    public MyLyrics lyrics;
    /** The camera column of the play bar. The score column that hits the
     * playbar is the column that is being played.
     * When the keyboard is on the left,
     * the playBar is at column 0. When the keyboard is on the right, the
     * play bar is at the right side, hidden under the keyboard. In this way
     * the user does not see the column that is being played until after
     * a couple of columns (useful for guessing exercises). See
     * Settings.R_PLAYBAR_OFFSET
     */
    public int playBarCol;
    /** A flag that is on when the camera is playing. */
    private volatile boolean playing;
    /** The page of the score that is currently on display.*/
    private int currentPage;

    /**
     * [CA] Constructor. Estableix les coordenades de pantalla de la càmera i inicialitza l'estat.
     * <p>
     * [EN] Constructor. Sets camera screen coordinates and initializes state.
     *
     * @param firstCol [CA] primera columna relativa al pare / [EN] first column relative to parent
     * @param firstRow [CA] primera fila relativa al pare / [EN] first row relative to parent
     * @param nCols    [CA] nombre de columnes / [EN] number of columns
     * @param nRows    [CA] nombre de files / [EN] number of rows
     * @param parent   [CA] component pare / [EN] parent component
     * @param contr    [CA] referència al controlador / [EN] reference to the controller
     */
    public MyCamera(int firstCol, int firstRow, int nCols, int nRows, MyComponent parent,MyController contr) {
        super(firstCol, firstRow, nCols, nRows, parent, contr);
        this.playing = false;
        this.currentPage = 1;
        this.score = null;
    }

    /**
     * [CA] Estableix la partitura associada a la càmera.
     * Després de canviar la partitura, cal fer un reset de la càmera.
     * <p>
     * [EN] Sets the score associated with the camera.
     * After setting the score, the camera should be reset.
     *
     * @param score [CA] la graella de notes / [EN] the grid score
     */
    public void setScore(MyGridScore score) {
        this.score = score;
   }

    /**
     * [CA] Estableix la franja d'acords.
     * <p>
     * [EN] Sets the chord symbol line.
     *
     * @param chords [CA] la franja d'acords / [EN] the chord symbol line
     */
    public void setSymbolLine(MyChordSymbolLine chords) {
        this.chordSymbolLine = chords;
    }

    /**
     * [CA] Estableix la franja de lletra.
     * <p>
     * [EN] Sets the lyrics strip.
     *
     * @param lyrics [CA] la franja de lletra / [EN] the lyrics strip
     */
    public void setLyrics(MyLyrics lyrics) {
        this.lyrics = lyrics;
    }

    /**
     * [CA] Retorna la pàgina actual.
     * <p>
     * [EN] Returns the current page number.
     *
     * @return [CA] número de pàgina actual / [EN] current page number
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * [CA] Estableix la posició de la barra de reproducció en columnes de càmera.
     * <p>
     * [EN] Sets the play bar position in camera columns.
     *
     * @param col [CA] columna de càmera de la barra de reproducció / [EN] camera column of the play bar
     */
    public void setPlayBar(int col) {
        playBarCol = col;
    }

    /**
     * [CA] Retorna la posició de la barra de reproducció en columnes de càmera.
     * <p>
     * [EN] Returns the play bar position in camera columns.
     *
     * @return [CA] columna de la barra de reproducció / [EN] play bar column
     */
    public int getPlayBar() {
        return playBarCol;
    }

    /**
     * [CA] Reinicia el temporitzador, el flag de reproducció, la columna actual de la
     * partitura i la pàgina actual.
     * <p>
     * [EN] Resets the timer, the playing flag, the current column of the score and
     * the current page.
     */
    public void reset() {
        this.playing = false;
        boolean left = !this.controller.getAllPurposeScore().isUseScreenKeyboardRight();
        this.score.setCurrentCol(Settings.getInitialCurrentCol(left,score));
        this.currentPage = 1;
    }

    /**
     * [CA] Mou la partitura a la primera columna d'una pàgina determinada.
     * <p>
     * [EN] Sets the score currentCol to the first column of the given page.
     *
     * @param page [CA] número de pàgina (comença en 1) / [EN] page number (starts at 1)
     */
    public void goToFirstColOfPage(int page) {
        int nColsPage = score.getNumColsPage();
        boolean left = !this.controller.getAllPurposeScore().isUseScreenKeyboardRight();
        score.setCurrentCol((Settings.getInitialCurrentCol(left,score)) + (page - 1) * nColsPage);
    }

    /**
     * [CA] Actualitza el número de pàgina actual basant-se en la columna actual de la partitura.
     * <p>
     * [EN] Updates the current page number based on the current score column.
     */
    public void updateCurrentPage(){
        boolean left = !this.controller.getAllPurposeScore().isUseScreenKeyboardRight();
        this.currentPage = 1+(score.getCurrentCol() - (Settings.getInitialCurrentCol(left,score)))/score.getNumColsPage();
    }

    /**
     * [CA] Avança la partitura exactament una pàgina endavant.
     * Amb l'anacrusis encabida activa, la primera pàgina abasta un compàs extra.
     * <p>
     * [EN] Moves the score forward by exactly one page.
     * With fit anacrusis active, the first page spans one extra measure.
     */
    public void nextPage() {
        boolean left = !this.controller.getAllPurposeScore().isUseScreenKeyboardRight();
        int minCol     = Settings.getInitialCurrentCol(left, score);
        int normalCols = score.getFixedColsPerPage();
        int currentCol = score.getCurrentCol();

        int jump;
        if (Settings.isFitAnacrusis() && Settings.isHasAnacrusis()) {
            int fitPageCols = normalCols + score.getBaseColsPerMeasure();
            jump = (currentCol < minCol + fitPageCols) ? fitPageCols : normalCols;
        } else {
            jump = normalCols;
        }
        int newCol = currentCol + jump;
        if (newCol <= score.getNColsBuffer()) {
            score.setCurrentCol(newCol);
        }
        updateCurrentPage();
        this.playing = false;
    }

    /**
     * [CA] Retrocedeix la partitura exactament una pàgina.
     * Amb l'anacrusis encabida activa, tornar enrere des de la segona pàgina
     * retorna a l'inici.
     * <p>
     * [EN] Moves the score backward by exactly one page.
     * With fit anacrusis active, going back from the second page returns to the start.
     */
    public void prevPage() {
        boolean left = !this.controller.getAllPurposeScore().isUseScreenKeyboardRight();
        int normalCols = score.getFixedColsPerPage();
        int minCol     = Settings.getInitialCurrentCol(left, score);
        int currentCol = score.getCurrentCol();

        int newCol;
        if (Settings.isFitAnacrusis() && Settings.isHasAnacrusis()) {
            int fitPageCols = normalCols + score.getBaseColsPerMeasure();
            if (currentCol <= minCol + fitPageCols) {
                newCol = minCol;
            } else {
                newCol = Math.max(minCol + fitPageCols, currentCol - normalCols);
            }
        } else {
            newCol = Math.max(minCol, currentCol - normalCols);
        }
        score.setCurrentCol(newCol);
        updateCurrentPage();
        this.playing = false;
    }

    /**
     * [CA] Mou la partitura cap a l'esquerra incrementant la columna actual del score.
     * Si s'ha superat el nombre màxim de columnes, atura la reproducció.
     * <p>
     * [EN] Moves the score to the left by incrementing the score's currentCol.
     * If the maximum number of columns has been exceeded, stops playback.
     */
    public void updateCurrentCol() {
        if (score.getCurrentCol() < Settings.getnColsScore()) {
                score.incrementCurrentCol();
                updateCurrentPage();
        } else {
            this.playing = false;
        }
    }

    /**
     * [CA] Atura la reproducció posant el flag {@code playing} a false.
     * <p>
     * [EN] Sets playing to false, stopping playback.
     */
    public void stop() {
        this.playing = false;
    }

    /**
     * [CA] Estableix el flag de reproducció.
     * <p>
     * [EN] Sets the playing flag.
     *
     * @param playing [CA] true si la càmera ha de reproduir / [EN] true if the camera should play
     */
    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    /**
     * [CA] Retorna si la càmera està en mode reproducció.
     * <p>
     * [EN] Returns whether the camera is in playback mode.
     *
     * @return [CA] true si s'està reproduint / [EN] true if playing
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * [CA] Redibuixa els tres buffers offscreen complets: graella, franja d'acords i lletra.
     * <p>
     * [EN] Redraws all three full offscreen buffers (grid, chord line, lyrics).
     */
    public void drawFullCamInOffscreen() {
        score.drawFullGridinOffscreen();
        chordSymbolLine.drawFullChordLineInOffscreen();
        lyrics.drawFullLyricsInOffscreen();
    }

    /**
     * [CA] Dibuixa la barra de reproducció com una línia vertical fina i negra.
     * <p>
     * [EN] Draws the playbar as a thin vertical black line.
     *
     * @param g [CA] context gràfic / [EN] graphics context
     */
    public void drawPlayBar(Graphics2D g){
        double rh = Settings.getRowHeight();
        Stroke stroke = g.getStroke();
        g.setStroke(new BasicStroke((float) 3));
        g.setColor(java.awt.Color.BLACK);
        int x = (int) getScreenX(playBarCol);
        int y = (int) getScreenY(0) + 1;
        int h = (int) (Settings.getnRowsCam() * rh) - 2;
        g.drawLine(x, y, x, y + h);
        g.setStroke(stroke);
    }

    /**
     * [CA] Dibuixa la partitura, la franja d'acords, la lletra i la barra de reproducció.
     * <p>
     * [EN] Draws the score, the chord symbol line, the lyrics and the play bar.
     *
     * @param g [CA] context gràfic / [EN] graphics context
     */
    @Override
    public void draw(Graphics2D g) {
        if (Settings.SHOW_DRAW_HIERARCHY) {
            Utilities.printOutWithPriority(5, "MyCamera::draw: drawing " + this.getClass());
        }
            this.score.draw(g);
            this.chordSymbolLine.draw(g);
        if (this.lyrics != null) {
            this.lyrics.draw(g);
        }
        this.drawPlayBar(g);
        g.setColor(java.awt.Color.BLACK);
        g.drawRect((int) screenPosX, (int) screenPosY, (int) width, (int) height);
    }
}
