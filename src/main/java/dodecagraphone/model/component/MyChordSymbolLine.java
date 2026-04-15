package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.chord.Chord;
import dodecagraphone.ui.MyDialogs;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.Map;

/**
 *
 * The chord symbol line is an horizontal strip that moves along the score, with
 * beat and measures bars, where the application can write chord symbols
 * corresponding to the current score (like in a music fake book).
 *
 * @author Pau
 */
public class MyChordSymbolLine extends MyComponent {

    private MyGridScore score;
    private MyController contr;
    private BufferedImage offscreenImage;
    private Graphics2D offscreenGraphics;

    /**
     *
     * @param firstCol
     * @param firstRow
     * @param nCols
     * @param nRows
     * @param parent
     * @param contr
     * @param score
     */
    public MyChordSymbolLine(int firstCol, int firstRow, int nCols, int nRows, MyComponent parent, MyController contr, MyGridScore score) {
        super(firstCol, firstRow, nCols, nRows, parent, contr);
        this.score = score;
        this.contr = contr;
    }

    public int whichCol(double screenX, double screenY) {
        if (this.contains(screenX, screenY)) {
            int whichCol = this.contr.getAllPurposeScore().getCol(screenX);
            whichCol = whichCol - (whichCol % Settings.getnColsBeat());
            return whichCol;
        }
        return -1;
    }

    public Chord enterChord(Chord oldChord) {
        String defStr = (oldChord != null) ? oldChord.basicString() : "";
        String input = MyDialogs.mostraInputDialog("Enter chord (empty to delete):", "Chord", defStr);
        if (input == null) {
            return oldChord; // cancel·lat → no fer res
        }
        if (input.trim().isEmpty()) {
            return null; // buit → esborrar
        }
        return new Chord(input.trim());
//        String defStr = "";
//        int ncols = 0;
//        if (oldChord != null) {
//            defStr = oldChord.basicString();
//            ncols = oldChord.getNCols();
//        }
//
//        String newChordString = MyDialogs.mostraInputDialog("Enter chord: ", "Chord", defStr);
//        if (newChordString == null || newChordString.isEmpty()) {
//            return null;
//        }
//
//        // 1) Normalitza al format "Intervals" amb l'API de ChordSymbols
//        String intervalsStr;
//        try {
//            String s = newChordString.trim();
//            
//            // RECONSIDERAR
//
//            // Si ja és interval·lic, el normalitzem igualment per assegurar format canònic
//            if (ChordSymbols.isIntervalic(s)) {
//                // Convertim a simbòlic i tornem a intervals per assegurar format consistent
//                String sym = ChordSymbols.fromIntervalNotation(s);
//                intervalsStr = ChordSymbols.toIntervalNotation(sym);
//            } // Symbolic / Sinònim
//            else if (ChordSymbols.isSymbolic(s)) {
//                intervalsStr = ChordSymbols.toIntervalNotation(s);
//            } // DodecaNoms (els teus noms do,de,re,...) 
//            else if (ChordSymbols.isNoteNames(s)) {
//                intervalsStr = ChordSymbols.toIntervalNotation(s);
//            } // Notes amb #/b
//            else if (ChordSymbols.isAlteracions(s)) {
//                intervalsStr = ChordSymbols.toIntervalNotation(s);
//            } // (Opcional) Notació de posició: [1,3,5]...
//            else if (ChordSymbols.isPosicio(s)) {
//                // La teva especificació parla de 5 formats; si vols admetre també posició:
//                intervalsStr = ChordSymbols.fromPosicio(s); // retorna "Do[...]" (format intervals)
//            } else {
//                // Qualsevol altre cas: marquem invàlid
//                throw new IllegalArgumentException("format no reconegut");
//            }
//        } catch (IllegalArgumentException ex) {
//            // Mostra només el missatge estàndard que ja feies servir
//            MyDialogs.mostraError("Acord invalid.", "Error");
//            // Manté el comportament antic: retorna l'acord amb el text d'entrada original
//            return new Chord(newChordString);
//        } catch (Exception ex) {
//            MyDialogs.mostraError("Acord invalid.", "Error");
//            return new Chord(newChordString);
//        }
//
//        // 2) Valida amb el teu model de Chord
//        Chord newchord = new Chord(intervalsStr);
//        if (!newchord.isValidChord()) {
//            MyDialogs.mostraError("Acord invalid.", "Error");
//            return newchord;
//        }
//
//        String nBeatsString = MyDialogs.mostraInputDialog(
//                "Enter num beats: ", "Num Beats", "" + (ncols / Settings.getnColsBeat()));
//        try {
//            if (nBeatsString != null && !nBeatsString.trim().isEmpty()) {
//                newchord.setNCols(Integer.parseInt(nBeatsString.trim()) * Settings.getnColsBeat());
//            }
//        } catch (Exception ignore) {
//            // Si l'usuari posa quelcom no numèric, deixem els nCols tal com estaven
//        }
//        return newchord;
    }

    /**
     * setter.
     *
     * @param score
     */
    public void setScore(MyGridScore score) {
        this.score = score;
    }

    /**
     * Draws a measure line. To place the line, it uses score rows (negative)
     * and columns.
     *
     * @param col
     * @param g
     */
    private void drawMeasureLine(int col, Graphics2D g, boolean offscreen) {
        //if (Settings.IS_BU) return;
        Stroke stroke = g.getStroke();
        g.setStroke(new BasicStroke((float) 3));
        int camX1 = offscreen
                ? (int) Math.floor(col * Settings.getColWidth())
                : (int) Math.floor(score.getScreenX(col));
        if (!offscreen && !Settings.IS_BU) System.out.println("MyChordSymbolLine::drawMeasureLine camX1 = "+camX1);
        int camY1 = offscreen ? 0 : (int) Math.round(score.getScreenY(-nRows));
        int camX2 = camX1;
        int camY2 = camY1 + (int) (nRows * Settings.getRowHeight());
        g.setColor(java.awt.Color.BLACK);
        g.drawLine(camX1, camY1, camX2, camY2);
        g.setStroke(stroke);
    }

    /**
     * Draws a beat line. To place the line, it uses score rows (negative) and
     * columns.
     *
     * @param col
     * @param g
     */
    private void drawBeatLine(int col, Graphics2D g, boolean offscreen) {
        //if (Settings.IS_BU) return;
        Stroke stroke = g.getStroke();
        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{15, 3}, 0);
        g.setStroke(dashed);
        int camX1 = offscreen
                ? (int) Math.floor(col * Settings.getColWidth())
                : (int) Math.floor(score.getScreenX(col));
        int camY1 = offscreen ? 0 : (int) Math.round(score.getScreenY(-nRows));
        int camX2 = camX1;
        int camY2 = camY1 + (int) (nRows * Settings.getRowHeight());
        g.setColor(java.awt.Color.BLACK);
        g.drawLine(camX1, camY1, camX2, camY2);
        g.setStroke(stroke);
    }

    /**
     * Given a chord, it draws the chord symbol: if the chord root is -1, it
     * draws the field info of the chord (a text); if the Settings flag
     * chordSymbolVertical is set, it draws the chord notes in vertical,
     * otherwise it draws a basic chord symbol (chord.basicString()).
     *
     * @param chord
     * @param col
     * @param g
     */
    public void drawChordSymbol(Chord chord, int col, Graphics2D g) {
        drawChordSymbol(chord, col, g, false);
    }

    private void drawChordSymbol(Chord chord, int col, Graphics2D g, boolean offscreen) {
        g.setColor(java.awt.Color.BLACK);
        int camX = 3 + (offscreen
                ? (int) Math.floor(col * Settings.getColWidth())
                : (int) score.getScreenX(col));
        int camY = 8 + (int) Settings.getRowHeight() + (offscreen
                ? (int) (2 * Settings.getRowHeight())
                : (int) getScreenY(2));
        if (chord.getRoot() == Settings.USE_INFO_AS_SIMBOL) { // draws info
            g.drawString(chord.getInfo(), (int) (camX), (int) (camY));
        } else {
            if (Settings.isChordSymbolVertical()) {
                int camYFirst = camY;
                g.drawString(chord.getSimbolWithNumericBass(), camX, camY);
                int[] shape = chord.getShape();
                camX += 10;
                if (chord.getBass() != Chord.NULL_BASS) {
                    camX += 30;
                }
                camY = camYFirst;
                for (int i = 0; i < 4; i++) {
                    if (i < shape.length) {
                        g.drawString(String.format("%2d", shape[i]) + "", camX, camY);
                        camY -= 10;
                    }
                }
                camX += 20;
                camY = camYFirst;
                for (int i = 4; i < shape.length; i++) {
                    if (i < shape.length) {
                        g.drawString(String.format("%2d", shape[i]) + "", camX, camY);
                        camY -= 10;
                    }
                }
            } else {
                g.drawString(chord.basicString(), camX, camY);
            }
        }
    }

    /**
     * Initialises the offscreen BufferedImage for the chord symbol line and
     * draws the full content into it.  Call this whenever the score layout or
     * chord data changes (mirrors MyGridScore.initOffscreen()).
     */
    public void initOffscreen() {
        if (offscreenGraphics != null) {
            offscreenGraphics.dispose();
        }
        int w = (int) (score.getNumCols() * Settings.getColWidth());
        int h = (int) (nRows * Settings.getRowHeight());
        offscreenImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        offscreenGraphics = offscreenImage.createGraphics();
        offscreenGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Utilities.printOutWithPriority(false, "MyChordSymbolLine::initOffscreen: w=" + w + ", h=" + h);
        drawFullChordLineInOffscreen();
    }

    /**
     * Draws the complete chord symbol line (all columns) into the offscreen
     * buffer using absolute pixel coordinates (col * colWidth, 0).
     */
    public void drawFullChordLineInOffscreen() {
        synchronized (offscreenGraphics) {
            // Clear background
            offscreenGraphics.setColor(Color.WHITE);
            offscreenGraphics.fillRect(0, 0, offscreenImage.getWidth(), offscreenImage.getHeight());

            Map<Integer, Chord> chords = score.getChordSimbolLine();
            int numCols = score.getNumCols();

            // Last column measure line
            int col = numCols;
            if ((col % (Settings.getnColsBeat() * score.getNumBeatsMeasure())) == 0) {
                drawMeasureLine(col, offscreenGraphics, true);
            }
            // All columns backwards
            for (col = numCols - 1; col >= 0; col--) {
                Chord chord = chords.get(col);
                if (chord != null) {
                    drawChordSymbol(chord, col, offscreenGraphics, true);
                }
                if ((col % Settings.getnColsBeat()) == 0) {
                    drawBeatLine(col, offscreenGraphics, true);
                }
                if ((col % (Settings.getnColsBeat() * score.getNumBeatsMeasure())) == 0) {
                    drawMeasureLine(col, offscreenGraphics, true);
                }
            }
        }
    }

    /**
     * Copies a region of a BufferedImage to screen, clamping source
     * coordinates to the image bounds (mirrors MyGridScore.drawImageClamped).
     */
    private static void drawImageClamped(Graphics2D g, BufferedImage img,
            int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2) {
        int iw = img.getWidth();
        int ih = img.getHeight();
        if (iw <= 0 || ih <= 0) return;
        if (sx2 <= 0 || sy2 <= 0 || sx1 >= iw || sy1 >= ih) return;

        int osx1 = sx1, osy1 = sy1, osx2 = sx2, osy2 = sy2;
        sx1 = Math.max(0, Math.min(sx1, iw));
        sx2 = Math.max(0, Math.min(sx2, iw));
        sy1 = Math.max(0, Math.min(sy1, ih));
        sy2 = Math.max(0, Math.min(sy2, ih));
        if (sx2 <= sx1 || sy2 <= sy1) return;

        double sw = (osx2 - osx1), sh = (osy2 - osy1);
        if (sw == 0 || sh == 0) return;
        double dw = (dx2 - dx1), dh = (dy2 - dy1);

        double leftCut   = (sx1 - osx1) / sw;
        double rightCut  = (osx2 - sx2) / sw;
        double topCut    = (sy1 - osy1) / sh;
        double bottomCut = (osy2 - sy2) / sh;

        int ndx1 = (int) Math.round(dx1 + dw * leftCut);
        int ndx2 = (int) Math.round(dx2 - dw * rightCut);
        int ndy1 = (int) Math.round(dy1 + dh * topCut);
        int ndy2 = (int) Math.round(dy2 - dh * bottomCut);
        if (ndx2 <= ndx1 || ndy2 <= ndy1) return;

        g.drawImage(img, ndx1, ndy1, ndx2, ndy2, sx1, sy1, sx2, sy2, null);
    }

    /**
     * For each column, draws the chord symbol (if not null) and the beat and
     * measure lines (when required).  When an offscreen buffer is available,
     * extracts the visible portion from the buffer instead of redrawing from
     * scratch (mirrors MyGridScore.draw()).
     *
     * @param g
     */
    @Override
    public void draw(Graphics2D g) {
        if (Settings.SHOW_DRAW_HIERARCHY) {
            Utilities.printOutWithPriority(5, "MyChordSymbolLine::draw: drawing " + this.getClass());
        }
        // Draw border
        g.setColor(Color.BLACK);
        g.drawRect((int) screenPosX, (int) screenPosY, (int) width, (int) height);

        if (offscreenImage != null) {
            // ---- Offscreen path: extract visible slice ----
            boolean left = !score.isUseScreenKeyboardRight();
            int ccol = score.getCurrentCol();
            int firstColToDraw, x1, y1, w, h, x2, w2;

            if (left) {
                x1 = (int) Math.round(screenPosX);
                y1 = (int) Math.round(screenPosY);
                w  = (int) Math.ceil(width);
                h  = (int) Math.ceil(height);
                firstColToDraw = Math.max(0, ccol - Settings.getnColsCam());
                int lastColToDraw = ccol;
                x2 = (int) Math.round(firstColToDraw * Settings.getColWidth());
                w2 = (int) Math.ceil((lastColToDraw - firstColToDraw) * Settings.getColWidth());
            } else {
                int firstCamColIn = Math.max(0, Settings.getnColsCam() - ccol);
                int actualWidthInCols = Settings.getnColsCam() - firstCamColIn;
                x1 = (int) Math.round(screenPosX + firstCamColIn * Settings.getColWidth());
                y1 = (int) Math.round(screenPosY);
                w  = (int) Math.ceil(actualWidthInCols * Settings.getColWidth());
                h  = (int) Math.ceil(height);
                firstColToDraw = Math.max(0, ccol - Settings.getnColsCam());
                int lastColToDraw = ccol;
                x2 = (int) Math.round(firstColToDraw * Settings.getColWidth());
                w2 = (int) Math.ceil((lastColToDraw - firstColToDraw) * Settings.getColWidth());
            }

            Utilities.printOutWithPriority(false, "MyChordSymbolLine::draw(): left=" + left + ", ccol=" + ccol
                    + ", x1=" + x1 + ", y1=" + y1 + ", w=" + w + ", h=" + h + ", x2=" + x2 + ", w2=" + w2);
            drawImageClamped(g, offscreenImage,
                    x1, y1, x1 + w, y1 + h,
                    x2, 0, x2 + w2, h);

        } else {
            // ---- Live (fallback) path ----
            if (!Settings.IS_BU) {
                System.out.println("MyChordSymbolLine::draw nColsBeat = " + Settings.getnColsBeat()
                        + " nBM = " + score.getNumBeatsMeasure()
                        + " colWidth=" + Settings.getColWidth()
                        + " beatPx=" + (Settings.getnColsBeat() * Settings.getColWidth()));
            }
            int firstDrawCol = score.getScoreCol(0);
            if (firstDrawCol < 0) firstDrawCol = 0;
            Map<Integer, Chord> chordSimbolLine = score.getChordSimbolLine();
            int first = Math.min(score.getCurrentCol() - score.getDelay(!score.isUseScreenKeyboardRight()), score.getNumCols());
            int col = first;
            if ((col % Settings.getnColsBeat()) == 0) {
                drawBeatLine(col, g, false);
            }
            if ((col % (Settings.getnColsBeat() * score.getNumBeatsMeasure())) == 0) {
                drawMeasureLine(col, g, false);
            }
            for (col = first - 1; col >= firstDrawCol; col--) {
                Chord chord = chordSimbolLine.get(col);
                if (chord != null) {
                    drawChordSymbol(chord, col, g);
                }
                if ((col % Settings.getnColsBeat()) == 0) {
                    drawBeatLine(col, g, false);
                }
                if ((col % (Settings.getnColsBeat() * score.getNumBeatsMeasure())) == 0) {
                    drawMeasureLine(col, g, false);
                }
            }
            col = score.getNumCols();
            if ((col % (Settings.getnColsBeat() * score.getNumBeatsMeasure())) == 0) {
                drawMeasureLine(col, g, false);
            }
        }
    }
}
