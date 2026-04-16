package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.ui.I18n;
import dodecagraphone.ui.MyDialogs;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An horizontal strip below the score that scrolls in sync with the grid,
 * reserved for song lyrics. Mirrors the BufferedImage offscreen mechanism of
 * MyChordSymbolLine: the full content is pre-rendered into an offscreen
 * BufferedImage and the visible slice is extracted each frame with
 * drawImageClamped().
 *
 * [CA] Franja horitzontal a sota de la partitura que fa scroll juntament amb
 * el grid, reservada per a la lletra de la cançó.
 *
 * @author Pau
 */
public class MyLyrics extends MyComponent {

    private final MyGridScore score;
    private BufferedImage offscreenImage;
    private Graphics2D offscreenGraphics;

    /**
     * All lyrics segments, grouped by track ID.
     * Key = track ID, Value = list of segments for that track.
     */
    private final Map<Integer, List<LyricSegment>> lyricsByTrack = new HashMap<>();

    /** Track whose lyrics are currently displayed in the offscreen buffer. */
    private int displayTrackId = 0;

    /**
     * @param firstCol  col within parent (camera)
     * @param firstRow  row within parent (camera)
     * @param nCols     number of columns
     * @param nRows     number of rows (= Settings.getnRowsLyrics())
     * @param parent    parent component (camera)
     * @param contr     controller
     * @param score     the grid score (used for currentCol, beat/measure info)
     */
    public MyLyrics(int firstCol, int firstRow, int nCols, int nRows,
                    MyComponent parent, MyController contr, MyGridScore score) {
        super(firstCol, firstRow, nCols, nRows, parent, contr);
        this.score = score;
    }

    // -------------------------------------------------------------------------
    // Hit-testing and editing
    // -------------------------------------------------------------------------

    /**
     * Returns the exact score column corresponding to the screen position
     * (screenX, screenY), or -1 if the position is outside the lyrics strip.
     *
     * @param screenX  screen X in panel coordinates
     * @param screenY  screen Y in panel coordinates
     * @return column index or -1
     */
    public int whichCol(double screenX, double screenY) {
        if (this.contains(screenX, screenY)) {
            return controller.getAllPurposeScore().getCol(screenX);
        }
        return -1;
    }

    /**
     * Opens an input dialog to enter or edit the lyric at a column.
     * Returns null if the user cancels, an empty string to signal deletion,
     * or the new text otherwise.
     *
     * @param oldText  current lyric (may be null)
     * @return new text, "" (delete), or null (cancel)
     */
    public String enterLyrics(String oldText) {
        String defStr = (oldText != null) ? oldText : "";
        String input = MyDialogs.mostraInputDialog(
                I18n.t("myLyrics.enterLyrics.prompt"),
                I18n.t("myLyrics.enterLyrics.title"),
                defStr);
        if (input == null) {
            return null; // cancel·lat
        }
        return input.trim(); // "" = esborrar, text = nou valor
    }

    // -------------------------------------------------------------------------
    // Public lyrics API
    // -------------------------------------------------------------------------

    /**
     * Returns the lyric text at {@code col} for {@code track}, or null if none.
     */
    public String getLyric(int col, int track) {
        LyricSegment seg = findSegment(col, track);
        return seg != null ? seg.text : null;
    }

    /**
     * Adds or replaces the lyric at {@code col} for {@code track}.
     * The row (0=top, 1=mid, 2=bottom) is assigned automatically to avoid
     * overlapping with other segments on the same row: tries 1→2→0;
     * if all are occupied the text goes to row 1 (tough luck).
     */
    public void setLyric(int col, int track, String text) {
        List<LyricSegment> segs =
                lyricsByTrack.computeIfAbsent(track, k -> new ArrayList<>());
        // Remove any existing segment at this col
        segs.removeIf(s -> s.col == col);
        int w   = computeWidthCols(text);
        int row = assignRow(col, w, segs);
        segs.add(new LyricSegment(col, track, text, row, w));
    }

    /**
     * Removes the lyric at {@code col} for {@code track} (no-op if absent).
     */
    public void removeLyric(int col, int track) {
        List<LyricSegment> segs = lyricsByTrack.get(track);
        if (segs != null) {
            segs.removeIf(s -> s.col == col);
        }
    }

    /**
     * Updates the track whose lyrics are drawn. Triggers a redraw of the
     * offscreen buffer so the new track's lyrics appear immediately.
     *
     * @param trackId  new display track ID
     */
    public void setDisplayTrackId(int trackId) {
        if (this.displayTrackId != trackId) {
            this.displayTrackId = trackId;
            if (offscreenGraphics != null) {
                drawFullLyricsInOffscreen();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the first segment at {@code col} for {@code track}, or null.
     */
    private LyricSegment findSegment(int col, int track) {
        List<LyricSegment> segs = lyricsByTrack.get(track);
        if (segs == null) return null;
        for (LyricSegment s : segs) {
            if (s.col == col) return s;
        }
        return null;
    }

    /**
     * Computes the width of {@code text} in score columns (ceiling), adding a
     * small padding. Falls back to 4 columns if the offscreen context is not
     * yet initialised.
     */
    private int computeWidthCols(String text) {
        if (offscreenGraphics == null) return 4;
        FontMetrics fm = offscreenGraphics.getFontMetrics();
        int px = fm.stringWidth(text) + 4; // 4 px right-padding
        return Math.max(1, (int) Math.ceil(px / Settings.getColWidth()));
    }

    /**
     * Assigns a row to a new segment at {@code col} with width {@code widthCols}
     * given the existing segments {@code segs} for the same track.
     * Priority: 1 (middle) → 2 (bottom) → 0 (top) → 1 (mala sort).
     */
    private int assignRow(int col, int widthCols, List<LyricSegment> segs) {
        for (int tryRow : new int[]{1, 2, 0}) {
            boolean overlap = false;
            for (LyricSegment s : segs) {
                if (s.row == tryRow && overlaps(col, widthCols, s.col, s.widthCols)) {
                    overlap = true;
                    break;
                }
            }
            if (!overlap) return tryRow;
        }
        return 1; // mala sort: all rows occupied
    }

    /** True if column range [colA, colA+wA) overlaps [colB, colB+wB). */
    private static boolean overlaps(int colA, int wA, int colB, int wB) {
        return colA < colB + wB && colB < colA + wA;
    }

    /**
     * Draws a single lyric segment into {@code g} using offscreen coordinates.
     * Each of the 3 lyrics rows occupies {@code rowHeight} pixels;
     * the text is vertically centred within its row.
     */
    private void drawSegment(LyricSegment seg, Graphics2D g) {
        FontMetrics fm = g.getFontMetrics();
        double rowH = Settings.getRowHeight();
        int x     = (int) Math.floor(seg.col * Settings.getColWidth()) + 2;
        int textY = (int) (seg.row * rowH + (rowH + fm.getAscent() - fm.getDescent()) / 2.0);
        g.drawString(seg.text, x, textY);
    }

    // -------------------------------------------------------------------------
    // Offscreen buffer
    // -------------------------------------------------------------------------

    /**
     * Allocates the offscreen BufferedImage and draws the full lyrics content
     * into it. Call this whenever the score layout changes (mirrors
     * MyChordSymbolLine.initOffscreen()).
     */
    public void initOffscreen() {
        if (offscreenGraphics != null) {
            offscreenGraphics.dispose();
        }
        int w = (int) (score.getNumCols() * Settings.getColWidth());
        int h = (int) (nRows * Settings.getRowHeight());
        offscreenImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        offscreenGraphics = offscreenImage.createGraphics();
        offscreenGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        Utilities.printOutWithPriority(false,
                "MyLyrics::initOffscreen: w=" + w + ", h=" + h);
        drawFullLyricsInOffscreen();
    }

    /**
     * Draws the full lyrics content into the offscreen buffer using absolute
     * pixel coordinates (col * colWidth, 0). Call this whenever lyric data
     * changes.
     */
    public void drawFullLyricsInOffscreen() {
        synchronized (offscreenGraphics) {
            // Clear background
            offscreenGraphics.setColor(Color.WHITE);
            offscreenGraphics.fillRect(0, 0,
                    offscreenImage.getWidth(), offscreenImage.getHeight());

            int numCols = score.getNumCols();

            // Last-column measure line
            int col = numCols;
            if ((col % (Settings.getnColsBeat() * score.getNumBeatsMeasure())) == 0) {
                drawMeasureLine(col, offscreenGraphics);
            }
            // All columns: beat and measure separator lines
            for (col = numCols - 1; col >= 0; col--) {
                if ((col % Settings.getnColsBeat()) == 0) {
                    drawBeatLine(col, offscreenGraphics);
                }
                if ((col % (Settings.getnColsBeat() * score.getNumBeatsMeasure())) == 0) {
                    drawMeasureLine(col, offscreenGraphics);
                }
            }

            // Lyrics text for the current display track
            List<LyricSegment> segs = lyricsByTrack.get(displayTrackId);
            if (segs != null && !segs.isEmpty()) {
                offscreenGraphics.setColor(Color.BLACK);
                for (LyricSegment seg : segs) {
                    drawSegment(seg, offscreenGraphics);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Line drawing helpers (offscreen coords: x = col * colWidth, y = 0..h)
    // -------------------------------------------------------------------------

    private void drawMeasureLine(int col, Graphics2D g) {
        Stroke stroke = g.getStroke();
        g.setStroke(new BasicStroke(3f));
        int x = (int) Math.floor(col * Settings.getColWidth());
        int h = (int) (nRows * Settings.getRowHeight());
        g.setColor(Color.BLACK);
        g.drawLine(x, 0, x, h);
        g.setStroke(stroke);
    }

    private void drawBeatLine(int col, Graphics2D g) {
        Stroke stroke = g.getStroke();
        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL, 0, new float[]{15, 3}, 0);
        g.setStroke(dashed);
        int x = (int) Math.floor(col * Settings.getColWidth());
        int h = (int) (nRows * Settings.getRowHeight());
        g.setColor(Color.BLACK);
        g.drawLine(x, 0, x, h);
        g.setStroke(stroke);
    }

    // -------------------------------------------------------------------------
    // drawImageClamped (mirrors MyGridScore / MyChordSymbolLine)
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // draw
    // -------------------------------------------------------------------------

    /**
     * Extracts the visible slice from the offscreen buffer and blits it to
     * the screen (mirrors MyChordSymbolLine.draw()).
     *
     * @param g
     */
    @Override
    public void draw(Graphics2D g) {
        if (Settings.SHOW_DRAW_HIERARCHY) {
            Utilities.printOutWithPriority(5,
                    "MyLyrics::draw: drawing " + this.getClass());
        }
        if (offscreenImage != null) {
            boolean left = !score.isUseScreenKeyboardRight();
            int ccol = score.getCurrentCol();

            int x1, y1, w, h, x2, w2;
            if (left) {
                x1 = (int) Math.round(screenPosX);
                y1 = (int) Math.round(screenPosY);
                w  = (int) Math.ceil(width);
                h  = (int) Math.ceil(height);
                int firstColToDraw = Math.max(0, ccol - Settings.getnColsCam());
                int lastColToDraw  = ccol;
                x2 = (int) Math.round(firstColToDraw * Settings.getColWidth());
                w2 = (int) Math.ceil((lastColToDraw - firstColToDraw) * Settings.getColWidth());
            } else {
                int firstCamColIn    = Math.max(0, Settings.getnColsCam() - ccol);
                int actualWidthInCols = Settings.getnColsCam() - firstCamColIn;
                x1 = (int) Math.round(screenPosX + firstCamColIn * Settings.getColWidth());
                y1 = (int) Math.round(screenPosY);
                w  = (int) Math.ceil(actualWidthInCols * Settings.getColWidth());
                h  = (int) Math.ceil(height);
                int firstColToDraw = Math.max(0, ccol - Settings.getnColsCam());
                int lastColToDraw  = ccol;
                x2 = (int) Math.round(firstColToDraw * Settings.getColWidth());
                w2 = (int) Math.ceil((lastColToDraw - firstColToDraw) * Settings.getColWidth());
            }

            Utilities.printOutWithPriority(false,
                    "MyLyrics::draw(): left=" + left + ", ccol=" + ccol
                    + ", x1=" + x1 + ", y1=" + y1 + ", w=" + w
                    + ", h=" + h + ", x2=" + x2 + ", w2=" + w2);
            drawImageClamped(g, offscreenImage,
                    x1, y1, x1 + w, y1 + h,
                    x2, 0, x2 + w2, h);
        }

        // Border drawn after the image blit so the top separator line
        // (between the score and the lyrics) is not overwritten by the
        // white offscreen background.
        g.setColor(Color.BLACK);
        g.drawRect((int) screenPosX, (int) screenPosY, (int) width, (int) height);
    }

    // =========================================================================
    // Inner class: LyricSegment
    // =========================================================================

    /**
     * A single lyric text fragment attached to a specific score column and
     * MIDI track. The {@code row} field (0=top, 1=middle, 2=bottom) is
     * assigned automatically to avoid visual overlap with neighbouring
     * segments on the same row.
     */
    public static class LyricSegment {

        /** Score column where the segment starts (exact column where the user clicked). */
        public final int col;
        /** MIDI track index this segment belongs to. */
        public final int track;
        /** The lyric text. */
        public String text;
        /**
         * Row within the lyrics strip: 0 = top, 1 = middle, 2 = bottom.
         * Assigned automatically by {@link MyLyrics#assignRow}.
         */
        public int row;
        /**
         * Width of this segment expressed in score columns (ceiling of
         * pixelWidth / colWidth). Used for overlap detection.
         */
        public int widthCols;

        public LyricSegment(int col, int track, String text, int row, int widthCols) {
            this.col       = col;
            this.track     = track;
            this.text      = text;
            this.row       = row;
            this.widthCols = widthCols;
        }
    }
}
