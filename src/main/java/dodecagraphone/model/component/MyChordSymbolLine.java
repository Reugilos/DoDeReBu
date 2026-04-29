package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.chord.Chord;
import dodecagraphone.model.chord.ChordSymbols;
import dodecagraphone.ui.I18n;
import dodecagraphone.ui.MyDialogs;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

    /** Index into ChordSymbols.DISPLAY_FORMATS for the current display format. */
    private int displayFormatIdx = 0;

    public String getDisplayFormat() {
        return ChordSymbols.DISPLAY_FORMATS[displayFormatIdx];
    }

    public void cycleDisplayFormat() {
        displayFormatIdx = (displayFormatIdx + 1) % ChordSymbols.DISPLAY_FORMATS.length;
        needsDrawing = true;
    }

    public boolean isNeedsDrawing() { return needsDrawing; }
    public void setNeedsDrawing(boolean needsDrawing) { this.needsDrawing = needsDrawing; }

    /** Background colour for tempo change markers (white text on blue). */
    private static final Color COLOR_TEMPO_BG = new Color(0, 90, 190);
    /** Background colour for key change markers (white text on granate). */
    private static final Color COLOR_KEY_BG   = new Color(140, 0, 30);
    /** Small bold font used inside change markers. */
    private static final Font  MARK_FONT      = new Font("SansSerif", Font.BOLD, 9);
    /** Cached chord font and the row height it was computed for. */
    private Font   cachedChordFont   = null;
    private double cachedChordRowH   = 0;
    private volatile boolean needsDrawing = true;

    /** Line gap in pixels between chord text lines (tighter than font leading). */
    private static final int LINE_GAP = 1;

    private Font getChordFont(Graphics2D g) {
        double rowH = Settings.getRowHeight();
        if (cachedChordFont != null && rowH == cachedChordRowH) return cachedChordFont;
        cachedChordRowH = rowH;
        int chordH = (int) Math.round(nRows * rowH);
        int arrowH = 7;   // 5px triangle + 2px gap
        int botMargin = 2;
        int available = chordH - arrowH - botMargin;
        // Use tight line step (no font leading) so font can be larger
        int size = Math.max(6, available / 4);
        Font f = new Font("SansSerif", Font.BOLD, size);
        FontMetrics fm = g.getFontMetrics(f);
        while (lineStep(fm) * 4 - LINE_GAP > available && size > 6) {
            size--;
            f = new Font("SansSerif", Font.BOLD, size);
            fm = g.getFontMetrics(f);
        }
        cachedChordFont = f;
        return f;
    }

    private static int lineStep(FontMetrics fm) {
        return fm.getAscent() + fm.getDescent() + LINE_GAP;
    }

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
        // Show default in current display format (or basicString if unknown)
        String defStr = "";
        if (oldChord != null) {
            String converted = ChordSymbols.chordToFormat(oldChord, getDisplayFormat());
            defStr = (converted != null) ? converted : oldChord.basicString();
        }
        String input = MyDialogs.mostraInputDialogAllowEmpty(
                I18n.t("myChordSymbolLine.enterChord.prompt"),
                I18n.t("myChordSymbolLine.enterChord.title"),
                defStr);
        if (input == null) {
            return oldChord; // cancel·lat → no fer res
        }
        if (input.isEmpty()) {
            return null; // buit → esborrar
        }
        // Auto-detect format and convert to user's format (Root[intervals])
        String myFmt = ChordSymbols.detectAndConvert(input.trim(), oldChord);
        if (myFmt == null) {
            MyDialogs.mostraError(
                I18n.t("myChordSymbolLine.invalidFormat"),
                I18n.t("myChordSymbolLine.enterChord.title"));
            return oldChord;
        }
        Chord chord = new Chord(myFmt);
        if (!chord.isValidChord() || chord.getRoot() == Settings.USE_INFO_AS_SIMBOL) {
            return chord; // text lliure o acord no vàlid: no demanem offset
        }
        // Preguntem en quina columna del beat ha de sonar
        int nColsBeat = Settings.getnColsBeat();
        int defOffset = (oldChord != null) ? oldChord.getBeatColOffset() : 0;
        String offsetStr = MyDialogs.mostraInputDialog(
                I18n.f("myChordSymbolLine.enterBeatColOffset.prompt", nColsBeat - 1),
                I18n.t("myChordSymbolLine.enterBeatColOffset.title"),
                "" + defOffset);
        if (offsetStr != null && !offsetStr.trim().isEmpty()) {
            try {
                int offset = Integer.parseInt(offsetStr.trim());
                chord.setBeatColOffset(Math.max(0, Math.min(nColsBeat - 1, offset)));
            } catch (NumberFormatException ignore) { /* deixem 0 */ }
        }
        return chord;
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
        g.setStroke(new BasicStroke(1.5f));
        int camX1 = offscreen
                ? (int) Math.floor(col * Settings.getColWidth()) - 1
                : (int) Math.floor(score.getScreenX(col));
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
                ? (int) Math.floor(col * Settings.getColWidth()) - 1
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
        drawChordSymbol(chord, col, g, false, 0);
    }

    private void drawChordSymbol(Chord chord, int col, Graphics2D g, boolean offscreen) {
        drawChordSymbol(chord, col, g, offscreen, 0);
    }

    /**
     * Draws a chord symbol at the given column, shifted right by {@code xOffset}
     * pixels (used to avoid overlap with change markers at the same column).
     */
    private void drawChordSymbol(Chord chord, int col, Graphics2D g, boolean offscreen, int xOffset) {
        g.setColor(java.awt.Color.BLACK);
        Font prevFont = g.getFont();
        g.setFont(getChordFont(g));
        FontMetrics fm = g.getFontMetrics();

        int camX = 3 + xOffset + (offscreen
                ? (int) Math.floor(col * Settings.getColWidth())
                : (int) score.getScreenX(col));

        // Bottom of chord strip — text baseline drawn upward from here
        int chordH = (int) Math.round(Settings.getnRowsChord() * Settings.getRowHeight());
        int camY = (offscreen ? 0 : (int) screenPosY) + chordH - fm.getDescent() - 2;
        int gap   = 3;

        if (chord.getRoot() == Settings.USE_INFO_AS_SIMBOL) {
            g.drawString(chord.getInfo(), camX, camY);
            return;
        }

        String fmt = getDisplayFormat();
        List<String> lines = ChordSymbols.chordToFormatLines(chord, fmt);

        if (lines != null && !lines.isEmpty()) {
            // Column 1: root (lines[0]) at bottom
            String root  = lines.get(0);
            int    col1W = fm.stringWidth(root);
            g.drawString(root, camX, camY);

            // Split index: base notes end here, tensions start
            int splitIdx = ChordSymbols.baseLinesCount(chord); // tensions start at this index
            int col2Count = Math.min(splitIdx - 1, 4); // base notes in col 2, max 4

            // Column 2: base notes from bottom to top
            int col2X = camX + col1W + gap;
            int col2W = 0;
            int y = camY;
            for (int i = 1; i <= col2Count; i++) {
                String n = lines.get(i);
                col2W = Math.max(col2W, fm.stringWidth(n));
                g.drawString(n, col2X, y);
                y -= lineStep(fm);
            }

            // Column 3: tensions always here, from bottom to top
            if (lines.size() > splitIdx) {
                int col3X = col2X + col2W + gap;
                y = camY;
                for (int i = splitIdx; i < lines.size(); i++) {
                    g.drawString(lines.get(i), col3X, y);
                    y -= lineStep(fm);
                }
            }
        } else {
            // Inline: FORMAT_SIMBOL, FORMAT_NOM
            String displayStr = ChordSymbols.chordToFormat(chord, fmt);
            g.drawString(displayStr != null ? displayStr : chord.basicString(), camX, camY);
        }

        // Arrow at top pointing to the actual attack column (only when offset > 0)
        int offset = chord.getBeatColOffset();
        if (offset > 0) {
            int cw   = (int) Settings.getColWidth();
            int ax   = (offscreen
                    ? (int) Math.floor((col + offset) * Settings.getColWidth())
                    : (int) Math.floor(score.getScreenX(col + offset)))
                    + cw / 2;
            int ay   = offscreen ? 0 : (int) Math.round(score.getScreenY(-nRows));
            int aw   = 5;  // half-width of arrow base
            int ah   = 5;  // arrow height
            int[] xs = { ax - aw, ax + aw, ax };
            int[] ys = { ay,      ay,      ay + ah };
            Color prevColor = g.getColor();
            g.setColor(Color.BLACK);
            g.fillPolygon(xs, ys, 3);
            g.setColor(prevColor);
        }

        g.setFont(prevFont);
    }

    // -----------------------------------------------------------------------
    //  Change markers (tempo / key)
    // -----------------------------------------------------------------------

    /**
     * Draws a tempo-change marker at the bottom-left corner of the chord strip
     * cell at {@code col}, offset {@code existingXOff} pixels to the right to
     * allow stacking multiple markers.  Returns the pixel width of the drawn box.
     */
    /**
     * Draws a tempo-change marker stacked {@code existingYOff} pixels above the
     * bottom of the chord strip.  Returns the height of the drawn box.
     */
    private int drawTempoMark(int col, int bpm, Graphics2D g, boolean offscreen, int existingYOff) {
        return drawChangeMark(col, "" + bpm, COLOR_TEMPO_BG, g, offscreen, existingYOff);
    }

    /**
     * Draws a key-change marker stacked {@code existingYOff} pixels above the
     * bottom of the chord strip.  Returns the height of the drawn box.
     */
    private int drawKeyMark(int col, int midiKey, char mode, Graphics2D g, boolean offscreen, int existingYOff) {
        String text;
        try {
            text = ToneRange.getKeyName(midiKey, mode);
        } catch (Exception ex) {
            text = "?";
        }
        return drawChangeMark(col, text, COLOR_KEY_BG, g, offscreen, existingYOff);
    }

    /**
     * Draws a small filled rectangle with white text at the bottom-left of the
     * chord strip cell at {@code col}, stacked {@code existingYOff} pixels above
     * the bottom edge (markers stack upward).
     * Returns the height of the box so the caller can stack further markers.
     */
    private int drawChangeMark(int col, String text, Color bgColor,
                               Graphics2D g, boolean offscreen, int existingYOff) {
        Font prevFont = g.getFont();
        g.setFont(MARK_FONT);
        FontMetrics fm = g.getFontMetrics();
        int pad  = 2;
        int boxW = fm.stringWidth(text) + 2 * pad;
        int boxH = fm.getAscent() + fm.getDescent() + 2 * pad;

        int cellX   = offscreen
                ? (int) Math.floor(col * Settings.getColWidth())
                : (int) Math.floor(score.getScreenX(col));
        int stripH  = (int) Math.round(nRows * Settings.getRowHeight());
        int cellTop = offscreen ? 0 : (int) Math.round(score.getScreenY(-nRows));
        int boxX    = cellX;
        int boxY    = cellTop + stripH - boxH - existingYOff;

        g.setColor(bgColor);
        g.fillRect(boxX, boxY, boxW, boxH);
        g.setColor(Color.WHITE);
        g.drawString(text, boxX + pad, boxY + pad + fm.getAscent());

        g.setFont(prevFont);
        return boxH;
    }

    /**
     * Initialises the offscreen BufferedImage for the chord symbol line and
     * draws the full content into it.  Call this whenever the score layout or
     * chord data changes (mirrors MyGridScore.initOffscreen()).
     */
    public BufferedImage getOffscreenImage() {
        return offscreenImage;
    }

    public void drawInitialMarkersAt(Graphics2D g, int tempo, int midiKey, char scaleMode) {
        int yOff = 0;
        if (tempo > 0) yOff += drawTempoMark(0, tempo, g, true, yOff);
        if (midiKey >= 0) yOff += drawKeyMark(0, midiKey, scaleMode, g, true, yOff);
    }

    /** Draws the col-0 marks (tempo/key) from the changeMap, with fallback defaults. */
    public void drawInitialMarkersAt(Graphics2D g) {
        MyGridScore.ScoreChange sc0 = score.getChangeMap().get(0);
        int tempo     = (sc0 != null && sc0.tempo     != null) ? sc0.tempo     : Settings.DEFAULT_TEMPO;
        int midiKey   = (sc0 != null && sc0.midiKey   != null) ? sc0.midiKey   : ToneRange.getDefaultKey();
        char scaleMode = (sc0 != null && sc0.scaleMode != null) ? sc0.scaleMode : ToneRange.getDefaultMode();
        drawInitialMarkersAt(g, tempo, midiKey, scaleMode);
    }

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
        needsDrawing = true;
        drawFullChordLineInOffscreen();
    }

    /**
     * Draws the complete chord symbol line (all columns) into the offscreen
     * buffer using absolute pixel coordinates (col * colWidth, 0).
     */
    public void drawFullChordLineInOffscreen() {
        synchronized (offscreenGraphics) {
            if (!needsDrawing) return;
            // Clear background
            offscreenGraphics.setColor(Color.WHITE);
            offscreenGraphics.fillRect(0, 0, offscreenImage.getWidth(), offscreenImage.getHeight());

            Map<Integer, Chord> chords = score.getChordSimbolLine();
            int numCols = score.getNumCols();

            // Calcula les línies de beat i compàs considerant canvis de compàs mid-score
            boolean[] isBeat    = new boolean[numCols + 1];
            boolean[] isMeasure = new boolean[numCols + 1];
            score.computeBeatMeasureLines(numCols + 1, isBeat, isMeasure);

            // Marques de canvi de tempo i to (excloent col 0, que és la marca inicial).
            // Les marques s'apilen verticalment de baix cap amunt.
            // Per a cada columna guardem l'amplada màxima de les marques (per desplaçar l'acord).
            TreeMap<Integer, Integer> markerMaxWidths = new TreeMap<>();
            TreeMap<Integer, MyGridScore.ScoreChange> changeMap = score.getChangeMap();
            for (Map.Entry<Integer, MyGridScore.ScoreChange> entry : changeMap.entrySet()) {
                int col = entry.getKey();
                if (col <= 0 || col >= numCols) continue;
                MyGridScore.ScoreChange sc = entry.getValue();
                int yOff   = 0;  // alçada acumulada (s'apila cap amunt)
                int maxW   = 0;  // amplada màxima de les marques en aquesta columna
                if (sc.tempo != null) {
                    yOff += drawTempoMark(col, sc.tempo, offscreenGraphics, true, yOff);
                }
                if (sc.midiKey != null) {
                    char mode = (sc.scaleMode != null) ? sc.scaleMode : 'M';
                    yOff += drawKeyMark(col, sc.midiKey, mode, offscreenGraphics, true, yOff);
                }
                // Calculem l'amplada màxima rellegint els textos (aproximació: refem FontMetrics)
                if (yOff > 0) {
                    offscreenGraphics.setFont(MARK_FONT);
                    FontMetrics fm = offscreenGraphics.getFontMetrics();
                    int pad = 2;
                    if (sc.tempo != null) {
                        maxW = Math.max(maxW, fm.stringWidth("" + sc.tempo) + 2 * pad);
                    }
                    if (sc.midiKey != null) {
                        char mode = (sc.scaleMode != null) ? sc.scaleMode : 'M';
                        String kn;
                        try { kn = ToneRange.getKeyName(sc.midiKey, mode); }
                        catch (Exception e) { kn = "?"; }
                        maxW = Math.max(maxW, fm.stringWidth(kn) + 2 * pad);
                    }
                    markerMaxWidths.put(col, maxW);
                }
            }

            // Acords (el text comença desplaçat per l'amplada màxima de les marques).
            for (int col = 0; col < numCols; col++) {
                Chord chord = chords.get(col);
                if (chord != null) {
                    int xOff = markerMaxWidths.getOrDefault(col, 0);
                    drawChordSymbol(chord, col, offscreenGraphics, true, xOff);
                }
            }

            // Línies de beat i compàs DESPRÉS dels acords perquè no quedin tapades.
            for (int col = numCols - 1; col >= 0; col--) {
                if (isBeat[col])    drawBeatLine(col, offscreenGraphics, true);
                if (isMeasure[col]) drawMeasureLine(col, offscreenGraphics, true);
            }
            // Línia final (columna numCols)
            if (isMeasure[numCols]) drawMeasureLine(numCols, offscreenGraphics, true);
            needsDrawing = false;
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
            if (needsDrawing) drawFullChordLineInOffscreen();
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
                if (Settings.isFitAnacrusis() && Settings.isHasAnacrusis() && firstColToDraw == 0) {
                    int extraCols = Settings.getnBeatsMeasure() * Settings.getnColsBeat();
                    lastColToDraw = Math.min(lastColToDraw + extraCols, score.getNumCols());
                }
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
                if (Settings.isFitAnacrusis() && Settings.isHasAnacrusis() && firstColToDraw == 0) {
                    int extraCols = Settings.getnBeatsMeasure() * Settings.getnColsBeat();
                    lastColToDraw = Math.min(lastColToDraw + extraCols, score.getNumCols());
                }
                x2 = (int) Math.round(firstColToDraw * Settings.getColWidth());
                w2 = (int) Math.ceil((lastColToDraw - firstColToDraw) * Settings.getColWidth());
            }

            Utilities.printOutWithPriority(false, "MyChordSymbolLine::draw(): left=" + left + ", ccol=" + ccol
                    + ", x1=" + x1 + ", y1=" + y1 + ", w=" + w + ", h=" + h + ", x2=" + x2 + ", w2=" + w2);
            drawImageClamped(g, offscreenImage,
                    x1, y1, x1 + w, y1 + h,
                    x2, 0, x2 + w2, h);

            // Doble barra al stopCol — dibuixada sobre la vista (sempre actualitzada)
            int stopC = contr.getAllPurposeScore().getStopCol();
            if (stopC > 0) {
                int sx = (int) Math.floor(score.getScreenX(stopC)) - 1;
                int sy1 = (int) Math.round(score.getScreenY(-nRows));
                int sy2 = sy1 + (int) Math.round(nRows * Settings.getRowHeight());
                Stroke saved = g.getStroke();
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(3));
                g.drawLine(sx, sy1, sx, sy2);
                g.setStroke(new BasicStroke(2));
                g.drawLine(sx + 4, sy1, sx + 4, sy2);
                g.setStroke(saved);
            }

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
