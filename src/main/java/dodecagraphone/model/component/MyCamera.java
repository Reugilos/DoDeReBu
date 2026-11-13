/*
 * PolyForm Noncommercial License 1.0.0
 * Copyright (c) 2024-2026 Pau Bofill. Powered by Claude AI.
 * Full license / Llicència completa: LICENSE (project root / arrel del projecte)
 */
package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.*;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

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
     * [CA] L'últim pas del fil de reproducció: quina columna ha deixat com a
     * {@code currentCol}, en quin moment i quant dura. Va en un sol objecte
     * immutable perquè el fil de dibuix no en llegeixi mai una meitat d'un pas
     * i l'altra meitat del següent.
     * <p>
     * [EN] The playback thread's last step: which column it left as
     * {@code currentCol}, when, and how long the step lasts. Kept in a single
     * immutable object so the drawing thread never reads half of one step and
     * half of the next.
     */
    private static final class Step {
        final int col;
        final long startNanos;
        final double durNanos;
        Step(int col, long startNanos, double durNanos) {
            this.col = col;
            this.startNanos = startNanos;
            this.durNanos = durNanos;
        }
    }
    private volatile Step lastStep = null;

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
     * [CA] L'anota el fil de reproducció just després d'avançar la columna:
     * a partir d'ara i durant {@code durNanos}, la vista llisca de la columna
     * anterior a la nova. El so no en depèn gens.
     * <p>
     * [EN] Called by the playback thread right after advancing the column:
     * from now and for {@code durNanos}, the view slides from the previous
     * column to the new one. Sound does not depend on it at all.
     *
     * @param startNanos [CA] instant en què ha sonat la columna (el {@code nanoTime}
     *                   previst pel fil, no el d'ara) / [EN] instant the column
     *                   sounded (the thread's scheduled {@code nanoTime})
     * @param durNanos   [CA] durada del pas / [EN] step duration
     */
    public void markStep(long startNanos, double durNanos) {
        lastStep = new Step(score.getCurrentCol(), startNanos, durNanos);
    }

    /**
     * [CA] La posició de la vista en columnes de partitura, amb decimals: és el
     * {@code currentCol} que es dibuixa. Fora del play és {@code currentCol}
     * tal qual. Durant el play va de {@code currentCol - 1} a
     * {@code currentCol} al llarg del pas, o sigui que la columna que sona
     * arriba a la barra en el mateix instant que sona, i la partitura llisca
     * a velocitat constant en lloc d'anar a salts d'una columna.
     * <p>
     * [EN] The view position in score columns, with decimals: the
     * {@code currentCol} that gets drawn. Outside playback it is
     * {@code currentCol}. During playback it goes from {@code currentCol - 1}
     * to {@code currentCol} over the step, so the column being played reaches
     * the bar exactly when it sounds and the score slides at constant speed.
     *
     * @return [CA] posició fraccionària / [EN] fractional position
     */
    public double getViewPos() {
        int cc = score.getCurrentCol();
        Step s = lastStep;
        if (!playing || s == null) {
            return cc;
        }
        double frac = s.durNanos <= 0 ? 1.0
                : (System.nanoTime() - s.startNanos) / s.durNanos;
        if (frac < 0) frac = 0;
        if (frac > 1) frac = 1;
        // Entre que el fil avança la columna i en marca el pas, l'últim pas
        // encara és l'anterior (s.col == cc - 1, ja acabat): la vista es queda
        // a cc - 1 en lloc de saltar a cc i tornar enrere. L'acotació també
        // impedeix que un pas caducat l'allunyi més d'una columna.
        double v = s.col - 1 + frac;
        if (v < cc - 1) v = cc - 1;
        if (v > cc) v = cc;
        return v;
    }

    /**
     * [CA] Què es veu a la càmera: la columna de partitura (amb decimals) que cau
     * a la vora esquerra i l'escala horitzontal. Totes les franges (graella,
     * acords, lletra) i les seves barres de temps i compàs es col·loquen amb
     * aquest mateix objecte, o sigui que no es poden desajustar entre elles.
     * <p>
     * Amb escala 1, el desplaçament s'arrodoneix a píxels sencers i la còpia de
     * l'offscreen es fa 1:1: el text no es reescala mai i es manté nítid. Amb
     * <i>fit anacrusis</i> l'escala és menor que 1 i es redueix de mica en mica
     * mentre la vista recorre el primer compàs, en lloc de saltar de cop a
     * l'escala normal al primer pas de la reproducció.
     * <p>
     * [EN] What the camera shows: the score column (with decimals) at its left
     * edge and the horizontal scale. Every strip (grid, chords, lyrics) and
     * their beat and measure lines are placed with this same object, so they
     * cannot drift apart. With scale 1 the shift is rounded to whole pixels and
     * the offscreen is copied 1:1, so text is never resampled. With fit
     * anacrusis the scale is below 1 and relaxes gradually over the first
     * measure instead of jumping on the first playback step.
     */
    public static final class ViewWindow {
        /** [CA] Vora esquerra de la càmera a la pantalla. / [EN] Camera left edge on screen. */
        public final int camX;
        /** [CA] Amplada de la càmera a la pantalla. / [EN] Camera width on screen. */
        public final int camW;
        /** [CA] Columna de partitura a la vora esquerra. / [EN] Score column at the left edge. */
        public final double startCol;
        /** [CA] Columnes de partitura que hi caben. / [EN] Score columns that fit. */
        public final double spanCols;
        /** [CA] Píxels de pantalla per píxel d'offscreen. / [EN] Screen pixels per offscreen pixel. */
        public final double scale;
        /** [CA] {@code startCol * colWidth} arrodonit (escala 1). / [EN] Rounded shift (scale 1). */
        public final int shiftPx;
        private final double colW;

        ViewWindow(int camX, int camW, double startCol, double spanCols, double colW) {
            this.camX = camX;
            this.camW = camW;
            this.startCol = startCol;
            this.spanCols = spanCols;
            this.colW = colW;
            double nColsCam = Settings.getnColsCam();
            double s = spanCols > 0 ? nColsCam / spanCols : 1.0;
            this.scale = Math.abs(s - 1.0) < 1e-9 ? 1.0 : s;
            this.shiftPx = (int) Math.round(startCol * colW);
        }

        /** [CA] Hi ha compressió (fit anacrusis). / [EN] Compressed (fit anacrusis). */
        public boolean isScaled() {
            return scale != 1.0;
        }

        /**
         * [CA] On cau a la pantalla una x de l'offscreen.
         * [EN] Screen x of an offscreen x.
         */
        public double screenX(double offX) {
            if (scale == 1.0) return camX + offX - shiftPx;
            return camX + (offX - startCol * colW) * scale;
        }

        /** [CA] On cau a la pantalla una columna de partitura. / [EN] Screen x of a score column. */
        public double colToScreenX(double col) {
            return screenX(col * colW);
        }

        /** [CA] Quina columna de partitura hi ha sota una x de pantalla. / [EN] Score column under a screen x. */
        public double screenXToCol(double x) {
            return startCol + (x - camX) / (colW * scale);
        }

        /**
         * [CA] Copia a la pantalla la porció visible d'un offscreen, a l'alçada
         * {@code y}..{@code y+h}. Amb escala 1, 1:1 i sense reescalar; amb
         * compressió, amb interpolació bilineal.
         * <p>
         * [EN] Blits the visible part of an offscreen to the screen at
         * {@code y}..{@code y+h}. 1:1 with scale 1; bilinear when compressed.
         */
        public void blit(Graphics2D g, BufferedImage img, int y, int h) {
            if (img == null) return;
            int iw = img.getWidth(), ih = img.getHeight();
            int sh = Math.min(h, ih);
            if (iw <= 0 || sh <= 0) return;
            if (scale == 1.0) {
                int sx1 = Math.max(0, shiftPx);
                int sx2 = Math.min(iw, shiftPx + camW);
                if (sx2 <= sx1) return;
                int dx1 = camX + (sx1 - shiftPx);
                g.drawImage(img, dx1, y, dx1 + (sx2 - sx1), y + sh,
                        sx1, 0, sx2, sh, null);
            } else {
                double offL = startCol * colW;
                int sx1 = Math.max(0, (int) Math.floor(offL));
                int sx2 = Math.min(iw, (int) Math.ceil(offL + spanCols * colW) + 1);
                if (sx2 <= sx1) return;
                BufferedImage sub = img.getSubimage(sx1, 0, sx2 - sx1, sh);
                Shape oldClip = g.getClip();
                Object oldInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
                g.clipRect(camX, y, camW, h);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                AffineTransform at = new AffineTransform(scale, 0, 0, 1,
                        screenX(sx1), y);
                g.drawImage(sub, at, null);
                if (oldInterp != null) {
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterp);
                }
                g.setClip(oldClip);
            }
        }

        /**
         * [CA] Dibuixa les barres de temps (discontínues) i de compàs (gruixudes)
         * d'una franja, a l'alçada {@code y1}..{@code y2}, només les visibles.
         * <p>
         * [EN] Draws the visible beat (dashed) and measure (thick) lines of a
         * strip between {@code y1} and {@code y2}.
         */
        public void drawBeatAndMeasureLines(Graphics2D g, boolean[] isBeat, boolean[] isMeasure,
                int y1, int y2, int clipY, int clipH) {
            int from = Math.max(0, (int) Math.floor(startCol) - 1);
            int to = Math.min(isBeat.length - 1, (int) Math.ceil(startCol + spanCols) + 1);
            Shape oldClip = g.getClip();
            g.clipRect(camX, clipY, camW, clipH);
            Stroke saved = g.getStroke();
            g.setColor(java.awt.Color.BLACK);
            Stroke thick = new BasicStroke(2f);
            Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL, 0, new float[]{15, 3}, 0);
            for (int col = from; col <= to; col++) {
                if (!isBeat[col] && !isMeasure[col]) continue;
                int offX = (int) Math.floor(col * colW);
                int sx = (int) Math.round(screenX(offX));
                g.setStroke(isMeasure[col] ? thick : dashed);
                g.drawLine(sx, y1, sx, y2);
            }
            g.setStroke(saved);
            g.setClip(oldClip);
        }

        /**
         * [CA] La doble barra final, a la columna {@code stopCol}.
         * [EN] The final double bar at {@code stopCol}.
         */
        public void drawStopBar(Graphics2D g, int stopCol, int y1, int y2, int clipY, int clipH) {
            int sx = (int) Math.floor(colToScreenX(stopCol)) - 1;
            Shape oldClip = g.getClip();
            g.clipRect(camX, clipY, camW, clipH);
            Stroke saved = g.getStroke();
            g.setColor(java.awt.Color.BLACK);
            g.setStroke(new BasicStroke(3));
            g.drawLine(sx, y1, sx, y2);
            g.setStroke(new BasicStroke(2));
            g.drawLine(sx + 4, y1, sx + 4, y2);
            g.setStroke(saved);
            g.setClip(oldClip);
        }
    }

    /**
     * [CA] La finestra visible ara mateix (vegeu {@link ViewWindow}).
     * <p>
     * Sense <i>fit</i>, la vora esquerra és {@code getViewPos() - nColsCam}
     * (negativa amb el teclat a la dreta a l'inici, i aleshores la partitura
     * comença més endins). Amb <i>fit anacrusis</i> i el teclat a l'esquerra,
     * mentre la vora esquerra {@code L} no ha recorregut el compàs extra
     * {@code E}, la vora dreta es queda a {@code nColsCam + E} i l'escala és
     * {@code nColsCam / (nColsCam + E - L)}: a {@code L = 0} és la pàgina
     * encabida de sempre i a {@code L = E} ja és l'escala normal, sense cap salt.
     * <p>
     * [EN] The current visible window. Without fit the left edge is
     * {@code getViewPos() - nColsCam}. With fit anacrusis (left keyboard),
     * while the left edge {@code L} has not travelled the extra measure
     * {@code E}, the right edge stays at {@code nColsCam + E} and the scale is
     * {@code nColsCam / (nColsCam + E - L)}, which is continuous.
     *
     * @return [CA] la finestra / [EN] the window
     */
    public ViewWindow getViewWindow() {
        double colW = Settings.getColWidth();
        int nColsCam = Settings.getnColsCam();
        int camX = (int) Math.round(this.screenPosX);
        int camW = (int) Math.ceil(nColsCam * colW);
        double L = getViewPos() - nColsCam;
        double span = nColsCam;
        boolean left = !this.controller.getAllPurposeScore().isUseScreenKeyboardRight();
        if (left && Settings.isFitAnacrusis() && Settings.isHasAnacrusis()) {
            if (L < 0) L = 0;
            int extra = Settings.getnBeatsMeasure() * Settings.getnColsBeat();
            double right = Math.max(L + nColsCam,
                    Math.min(nColsCam + extra, score.getNumCols()));
            span = right - L;
        }
        return new ViewWindow(camX, camW, L, span, colW);
    }

    /**
     * [CA] El rectangle de pantalla que canvia mentre la vista llisca: la
     * càmera, la franja d'acords i la de la lletra, amb un petit marge per a
     * la barra de reproducció i la doble barra. És el que repinta el
     * temporitzador de fotogrames del play, i res més.
     * <p>
     * [EN] The screen rectangle that changes while the view slides: camera,
     * chord strip and lyrics strip, plus a small margin for the play bar and
     * the double bar. It is what the playback frame timer repaints.
     *
     * @return [CA] rectangle en píxels / [EN] rectangle in pixels
     */
    public java.awt.Rectangle getDrawBounds() {
        java.awt.Rectangle r = new java.awt.Rectangle(
                (int) Math.floor(screenPosX), (int) Math.floor(screenPosY),
                (int) Math.ceil(width) + 1, (int) Math.ceil(height) + 1);
        for (MyComponent c : new MyComponent[]{chordSymbolLine, lyrics}) {
            if (c == null) continue;
            r = r.union(new java.awt.Rectangle(
                    (int) Math.floor(c.getScreenPosX()), (int) Math.floor(c.getScreenPosY()),
                    (int) Math.ceil(c.getWidth()) + 1, (int) Math.ceil(c.getHeight()) + 1));
        }
        r.grow(4, 2);
        return r;
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
        if (playing) {
            // Pas inicial ja acabat a la columna d'ara: fins al primer pas de
            // debò la vista no es mou, i just després d'avançar no salta.
            lastStep = new Step(score.getCurrentCol(), System.nanoTime(), 0);
        }
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
