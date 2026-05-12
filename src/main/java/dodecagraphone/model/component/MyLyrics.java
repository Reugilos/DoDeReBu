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
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
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

    // ---- inline edit mode state ----
    private boolean      editMode          = false;
    private int          editCursorCol     = 0;
    private int          editTrack         = 0;
    private StringBuilder editBuffer       = new StringBuilder();
    /** Character position of the cursor within editBuffer (0 = before first char). */
    private int          editCursorCharPos = 0;
    /** Row (0/1/2) currently assigned to the text being typed. */
    private int          previewRow        = 1;
    private volatile boolean needsDrawing  = true;

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
            int col = controller.getAllPurposeScore().getCol(screenX);
            col = Math.max(0, Math.min(col, score.getNumCols() - 1));
            return col;
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
     * overlapping with other segments on the same row: tries 0→1→2;
     * if all are occupied the text goes to row 0 (tough luck).
     */
    public void setLyric(int col, int track, String text) {
        List<LyricSegment> segs =
                lyricsByTrack.computeIfAbsent(track, k -> new ArrayList<>());
        // Remove any existing segment at this col
        segs.removeIf(s -> s.col == col);
        int w   = computeWidthCols(text);
        int row = assignRow(col, w, segs);
        segs.add(new LyricSegment(col, track, text, row, w));
        needsDrawing = true;
    }

    /**
     * Removes the lyric at {@code col} for {@code track} (no-op if absent).
     */
    public void removeLyric(int col, int track) {
        List<LyricSegment> segs = lyricsByTrack.get(track);
        if (segs != null) {
            segs.removeIf(s -> s.col == col);
            needsDrawing = true;
        }
    }

    /**
     * Esborra totes les lletres i surt del mode edició. S'ha de cridar quan
     * s'obre o es crea una partitura nova.
     */
    public void clear() {
        if (editMode) {
            editMode = false;
            editBuffer.setLength(0);
            editCursorCharPos = 0;
        }
        lyricsByTrack.clear();
        displayTrackId = 0;
        if (offscreenGraphics != null) { needsDrawing = true; drawFullLyricsInOffscreen(); }
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
                needsDrawing = true;
                drawFullLyricsInOffscreen();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the first segment whose {@code col} equals {@code col} for
     * {@code track}, or null.
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
     * Returns the character index within {@code text} closest to
     * {@code offsetPx} pixels from the start of the text, measured with the
     * offscreen FontMetrics. Used to position the cursor on a mouse click.
     */
    private int charPositionFromPixel(String text, int offsetPx) {
        if (offscreenGraphics == null || offsetPx <= 0) return 0;
        FontMetrics fm = offscreenGraphics.getFontMetrics();
        for (int i = 0; i < text.length(); i++) {
            int wBefore = fm.stringWidth(text.substring(0, i));
            int wAfter  = fm.stringWidth(text.substring(0, i + 1));
            if (offsetPx <= (wBefore + wAfter) / 2) return i;
        }
        return text.length();
    }

    /**
     * Returns the segment for {@code track} that visually covers {@code col}:
     * exact start-column match first, then any segment whose span includes col.
     * Returns null if none.
     */
    private LyricSegment findSegmentAt(int col, int track) {
        LyricSegment exact = findSegment(col, track);
        if (exact != null) return exact;
        List<LyricSegment> segs = lyricsByTrack.getOrDefault(track, new ArrayList<>());
        for (LyricSegment s : segs) {
            if (col > s.col && col < s.col + s.widthCols) return s;
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
     * Priority: 0 (top) → 1 (middle) → 2 (bottom) → 0 (mala sort).
     */
    private int assignRow(int col, int widthCols, List<LyricSegment> segs) {
        for (int tryRow : new int[]{0, 1, 2}) {
            boolean overlap = false;
            for (LyricSegment s : segs) {
                if (s.row == tryRow && overlaps(col, widthCols, s.col, s.widthCols)) {
                    overlap = true;
                    break;
                }
            }
            if (!overlap) return tryRow;
        }
        return 0; // mala sort: all rows occupied
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
        int x     = (int) Math.round(seg.col * Settings.getColWidth()) + 2;
        int textY = (int) (seg.row * rowH + (rowH + fm.getAscent() - fm.getDescent()) / 2.0);
        g.drawString(seg.text, x, textY);
    }

    // -------------------------------------------------------------------------
    // Inline edit mode
    // -------------------------------------------------------------------------

    /** Returns true while the lyrics strip is in inline edit mode. */
    public boolean isEditMode() { return editMode; }

    public boolean isNeedsDrawing() { return needsDrawing; }
    public void setNeedsDrawing(boolean needsDrawing) { this.needsDrawing = needsDrawing; }

    /**
     * Enters inline edit mode at {@code col} for {@code track}.
     * If a committed segment already exists at that position its text is loaded
     * into the edit buffer and the segment is temporarily removed from the
     * committed store (it will be re-added on commit).
     */
    public void startEdit(int col, int track, double clickScreenX) {
        // Snap to the nearest note start at or before the clicked column
        int noteCol = findNoteAtOrBefore(col, track);
        if (noteCol < 0) {
            MyDialogs.mostraMissatge(
                    I18n.t("myLyrics.noNotesBefore.warning"),
                    I18n.t("myLyrics.label"));
            return;
        }
        editMode       = true;
        editCursorCol  = noteCol;
        editTrack      = track;
        displayTrackId = track;
        editBuffer.setLength(0);
        // If a committed segment exists at the snapped column, load it for editing.
        LyricSegment existing = findSegmentAt(noteCol, displayTrackId);
        if (existing != null) {
            editCursorCol  = existing.col;
            editTrack      = existing.track;
            editBuffer.append(existing.text);
            List<LyricSegment> segs = lyricsByTrack.get(existing.track);
            if (segs != null) segs.remove(existing);
            int textStartX = colToScreenX(existing.col) + 2;
            editCursorCharPos = charPositionFromPixel(
                    existing.text, (int)(clickScreenX - textStartX));
        } else {
            editCursorCharPos = 0;
        }
        updatePreviewRow();
        if (offscreenGraphics != null) { needsDrawing = true; drawFullLyricsInOffscreen(); }
    }

    /**
     * Commits any pending text and exits edit mode.
     * Called on Enter, Escape, or when the user clicks outside the strip.
     */
    public void exitEditMode() {
        commitCurrentWord();
        editMode = false;
        if (offscreenGraphics != null) { needsDrawing = true; drawFullLyricsInOffscreen(); }
    }

    /**
     * Handles key-pressed events (special keys) while in edit mode.
     *
     * @return true if the event was consumed (edit mode was active)
     */
    public boolean handleKeyPressed(KeyEvent e) {
        if (!editMode) return false;
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_ESCAPE) {
            exitEditMode();
        } else if (code == KeyEvent.VK_LEFT) {
            if (editCursorCharPos > 0) {
                editCursorCharPos--;
                needsDrawing = true; drawFullLyricsInOffscreen();
            }
        } else if (code == KeyEvent.VK_RIGHT) {
            if (editCursorCharPos < editBuffer.length()) {
                editCursorCharPos++;
                needsDrawing = true; drawFullLyricsInOffscreen();
            }
        } else if (code == KeyEvent.VK_BACK_SPACE) {
            if (editCursorCharPos > 0) {
                // Esborra el caràcter immediatament a l'esquerra del cursor
                editBuffer.deleteCharAt(editCursorCharPos - 1);
                editCursorCharPos--;
                updatePreviewRow();
                needsDrawing = true; drawFullLyricsInOffscreen();
            } else if (editBuffer.length() == 0) {
                // Buffer buit: torna al segment anterior i carrega'l per editar
                List<LyricSegment> segs = lyricsByTrack.get(editTrack);
                if (segs != null && !segs.isEmpty()) {
                    LyricSegment prev = null;
                    for (LyricSegment s : segs) {
                        if (s.col < editCursorCol) {
                            if (prev == null || s.col > prev.col) prev = s;
                        }
                    }
                    if (prev != null) {
                        editCursorCol     = prev.col;
                        editBuffer.setLength(0);
                        editBuffer.append(prev.text);
                        editCursorCharPos = editBuffer.length(); // cursor al final
                        segs.remove(prev);
                        updatePreviewRow();
                        needsDrawing = true; drawFullLyricsInOffscreen();
                    }
                }
            }
        } else if (code == KeyEvent.VK_SPACE && e.isShiftDown()) {
            // Shift+Espai: commit i salta a la nota ANTERIOR (com findPrevCol)
            commitCurrentWord();
            int prevCol = findPrevCol(editCursorCol);
            editCursorCol = prevCol;
            // Carrega text existent a la nova posició, si n'hi ha
            LyricSegment existing = findSegmentAt(prevCol, editTrack);
            if (existing != null) {
                editBuffer.append(existing.text);
                List<LyricSegment> segs = lyricsByTrack.get(editTrack);
                if (segs != null) segs.remove(existing);
            }
            editCursorCharPos = 0;  // cursor al principi
            updatePreviewRow();
            needsDrawing = true; drawFullLyricsInOffscreen();
        } else if (code == KeyEvent.VK_SPACE) {
            commitCurrentWord();  // reseteja editBuffer i editCursorCharPos
            // Advance to next note column (or next beat if no note found)
            editCursorCol = findNextCol(editCursorCol);
            // Load existing text at new position, if any
            LyricSegment existing = findSegment(editCursorCol, editTrack);
            if (existing != null) {
                editBuffer.append(existing.text);
                List<LyricSegment> segs = lyricsByTrack.get(editTrack);
                if (segs != null) segs.remove(existing);
            }
            editCursorCharPos = 0;  // cursor al principi del segment (nou o existent)
            updatePreviewRow();
            needsDrawing = true; drawFullLyricsInOffscreen();
        }
        return true; // consume all key events while in edit mode
    }

    /**
     * Handles key-typed events (printable characters) while in edit mode.
     *
     * @return true if the event was consumed (edit mode was active)
     */
    public boolean handleKeyTyped(KeyEvent e) {
        if (!editMode) return false;
        char c = e.getKeyChar();
        // Ignore control characters and chars handled by keyPressed
        if (c == KeyEvent.CHAR_UNDEFINED || c < 32 || c == 127
                || c == ' ' || c == '\n' || c == '\r') {
            return true;
        }
        // Hyphen: insert at cursor, commit and advance to next note (like space)
        if (c == '-') {
            editBuffer.insert(editCursorCharPos, '-');
            editCursorCharPos++;
            commitCurrentWord();  // reseteja editBuffer i editCursorCharPos
            editCursorCol = findNextCol(editCursorCol);
            // Load existing text at new position, if any
            LyricSegment existing = findSegment(editCursorCol, editTrack);
            if (existing != null) {
                editBuffer.append(existing.text);
                List<LyricSegment> segs = lyricsByTrack.get(editTrack);
                if (segs != null) segs.remove(existing);
            }
            editCursorCharPos = 0;  // cursor al principi del segment nou
            updatePreviewRow();
            needsDrawing = true; drawFullLyricsInOffscreen();
            return true;
        }
        // Normal character: insert at cursor position and advance
        editBuffer.insert(editCursorCharPos, c);
        editCursorCharPos++;
        updatePreviewRow();
        needsDrawing = true; drawFullLyricsInOffscreen();
        return true;
    }

    // ---- private helpers for edit mode ----

    /** Commits the current edit buffer as a real segment (if non-empty). */
    private void commitCurrentWord() {
        String text = editBuffer.toString().trim();
        if (!text.isEmpty()) {
            setLyric(editCursorCol, editTrack, text);
        }
        editBuffer.setLength(0);
        editCursorCharPos = 0;
    }

    /**
     * Recalculates {@link #previewRow} based on the current edit buffer width
     * and the committed segments of the current track. When the buffer is empty
     * a width of 1 column is used so the cursor already points to the row where
     * the next text will appear.
     */
    private void updatePreviewRow() {
        int w = (editBuffer.length() == 0)
                ? 1
                : computeWidthCols(editBuffer.toString());
        List<LyricSegment> segs = lyricsByTrack.getOrDefault(editTrack, new ArrayList<>());
        previewRow = assignRow(editCursorCol, w, segs);
    }

    /**
     * Returns the first score column after {@code fromCol} that contains at
     * least one note belonging to {@code editTrack}. Falls back to the next
     * beat boundary if no such column is found before the end of the score.
     */
    /**
     * Returns the score column of the nearest note start (visible, not linked)
     * belonging to {@code track} at or before {@code col}, or -1 if none exists.
     */
    private int findNoteAtOrBefore(int col, int track) {
        int nKeys = score.getnKeys();
        for (int c = col; c >= 0; c--) {
            for (int key = 0; key < nKeys; key++) {
                MyGridSquare sq = score.getGridSquare(key, c);
                if (sq != null) {
                    for (MyGridSquare.SubSquare note : sq.getPoliNotes()) {
                        if (note.getTrack() == track
                                && note.isVisible()
                                && !note.isLinked()) {
                            return c;
                        }
                    }
                }
            }
        }
        return -1;
    }

    private int findNextCol(int fromCol) {
        int nKeys = score.getnKeys();
        int nCols = score.getNumCols();
        for (int col = fromCol + 1; col < nCols; col++) {
            for (int key = 0; key < nKeys; key++) {
                MyGridSquare sq = score.getGridSquare(key, col);
                if (sq != null) {
                    for (MyGridSquare.SubSquare note : sq.getPoliNotes()) {
                        // Inici de nota: visible i no linked (no és una prolongació)
                        if (note.getTrack() == editTrack
                                && note.isVisible()
                                && !note.isLinked()) {
                            return col;
                        }
                    }
                }
            }
        }
        // Fallback: advance by one beat
        return fromCol + Settings.getnColsBeat();
    }

    /**
     * Returns the first score column strictly before {@code fromCol} that
     * contains at least one note start (visible and not linked) belonging to
     * {@code editTrack}. Falls back to the previous beat boundary if none found.
     */
    private int findPrevCol(int fromCol) {
        int nKeys = score.getnKeys();
        for (int col = fromCol - 1; col >= 0; col--) {
            for (int key = 0; key < nKeys; key++) {
                MyGridSquare sq = score.getGridSquare(key, col);
                if (sq != null) {
                    for (MyGridSquare.SubSquare note : sq.getPoliNotes()) {
                        if (note.getTrack() == editTrack
                                && note.isVisible()
                                && !note.isLinked()) {
                            return col;
                        }
                    }
                }
            }
        }
        // Fallback: go back one beat (clamp to 0)
        return Math.max(0, fromCol - Settings.getnColsBeat());
    }

    /**
     * Converts a score column index to a screen X coordinate, taking the
     * current camera scroll position into account.
     *
     * @param col  score column
     * @return screen X in panel coordinates
     */
    private int colToScreenX(int col) {
        boolean left = !score.isUseScreenKeyboardRight();
        int ccol = score.getCurrentCol();
        if (left) {
            int firstColToDraw = Math.max(0, ccol - Settings.getnColsCam());
            return (int) Math.round(screenPosX
                    + (col - firstColToDraw) * Settings.getColWidth());
        } else {
            int firstCamColIn  = Math.max(0, Settings.getnColsCam() - ccol);
            int firstColToDraw = Math.max(0, ccol - Settings.getnColsCam());
            return (int) Math.round(screenPosX
                    + firstCamColIn  * Settings.getColWidth()
                    + (col - firstColToDraw) * Settings.getColWidth());
        }
    }

    // -------------------------------------------------------------------------
    // Offscreen buffer
    // -------------------------------------------------------------------------

    /**
     * Allocates the offscreen BufferedImage and draws the full lyrics content
     * into it. Call this whenever the score layout changes (mirrors
     * MyChordSymbolLine.initOffscreen()).
     */
    public java.awt.image.BufferedImage getOffscreenImage() {
        return offscreenImage;
    }

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
        needsDrawing = true;
        drawFullLyricsInOffscreen();
    }

    /**
     * Draws the full lyrics content into the offscreen buffer using absolute
     * pixel coordinates (col * colWidth, 0). Call this whenever lyric data
     * changes.
     */
    public void drawFullLyricsInOffscreen() {
        synchronized (offscreenGraphics) {
            if (!needsDrawing) return;
            // Clear background
            offscreenGraphics.setColor(Color.WHITE);
            offscreenGraphics.fillRect(0, 0,
                    offscreenImage.getWidth(), offscreenImage.getHeight());

            int numCols = score.getNumCols();

            // Committed lyrics for the current display track
            List<LyricSegment> segs = lyricsByTrack.get(displayTrackId);
            if (segs != null && !segs.isEmpty()) {
                offscreenGraphics.setColor(Color.BLACK);
                for (LyricSegment seg : segs) {
                    drawSegment(seg, offscreenGraphics);
                }
            }

            // Preview: text currently being typed (shown in dark-grey)
            if (editMode && editBuffer.length() > 0
                    && editTrack == displayTrackId) {
                offscreenGraphics.setColor(Color.DARK_GRAY);
                LyricSegment preview = new LyricSegment(
                        editCursorCol, editTrack,
                        editBuffer.toString(), previewRow, 0);
                drawSegment(preview, offscreenGraphics);
            }
            needsDrawing = false;
        }
    }

    // -------------------------------------------------------------------------
    // Line drawing helpers (offscreen coords: x = col * colWidth, y = 0..h)
    // -------------------------------------------------------------------------

    private void drawMeasureLine(int col, Graphics2D g) {
        Stroke stroke = g.getStroke();
        g.setStroke(new BasicStroke(1.5f));
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
            if (needsDrawing) drawFullLyricsInOffscreen();
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
                if (Settings.isFitAnacrusis() && Settings.isHasAnacrusis() && firstColToDraw == 0) {
                    int extraCols = Settings.getnBeatsMeasure() * Settings.getnColsBeat();
                    lastColToDraw = Math.min(lastColToDraw + extraCols, score.getNumCols());
                }
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
                if (Settings.isFitAnacrusis() && Settings.isHasAnacrusis() && firstColToDraw == 0) {
                    int extraCols = Settings.getnBeatsMeasure() * Settings.getnColsBeat();
                    lastColToDraw = Math.min(lastColToDraw + extraCols, score.getNumCols());
                }
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

            // Línies de beat i compàs en screen-space per evitar que desapareguin
            // quan l'imatge s'escala amb fit-anacrusis.
            {
                int numCols = score.getNumCols();
                boolean[] isBeat    = new boolean[numCols + 1];
                boolean[] isMeasure = new boolean[numCols + 1];
                score.computeBeatMeasureLines(numCols + 1, isBeat, isMeasure);
                double scaleX = w2 > 0 ? (double) w / w2 : 1.0;
                Shape oldClip = g.getClip();
                g.clipRect(x1, y1, w, h);
                Stroke saved = g.getStroke();
                g.setColor(java.awt.Color.BLACK);
                for (int col = 0; col <= numCols; col++) {
                    if (!isBeat[col] && !isMeasure[col]) continue;
                    int offX = (int) Math.floor(col * Settings.getColWidth());
                    int sx = x1 + (int) Math.round((offX - x2) * scaleX);
                    if (isMeasure[col]) {
                        g.setStroke(new java.awt.BasicStroke(2f));
                        g.drawLine(sx, y1, sx, y1 + h);
                    } else {
                        java.awt.Stroke dashed = new java.awt.BasicStroke(1,
                                java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_BEVEL,
                                0, new float[]{15, 3}, 0);
                        g.setStroke(dashed);
                        g.drawLine(sx, y1, sx, y1 + h);
                    }
                }
                g.setStroke(saved);
                g.setClip(oldClip);
            }

            // Doble barra al stopCol — sempre actualitzada en coordenades de pantalla
            int stopC = controller.getAllPurposeScore().getStopCol();
            if (stopC > 0) {
                int sx = (int) Math.floor(score.getScreenX(stopC)) - 1;
                Shape oldClip = g.getClip();
                g.clipRect(x1, y1, w, h);
                Stroke saved = g.getStroke();
                g.setColor(java.awt.Color.BLACK);
                g.setStroke(new java.awt.BasicStroke(3));
                g.drawLine(sx, y1, sx, y1 + h);
                g.setStroke(new java.awt.BasicStroke(2));
                g.drawLine(sx + 4, y1, sx + 4, y1 + h);
                g.setStroke(saved);
                g.setClip(oldClip);
            }
        }

        // Border drawn after the image blit so the top separator line
        // (between the score and the lyrics) is not overwritten by the
        // white offscreen background.
        g.setColor(Color.BLACK);
        g.drawRect((int) screenPosX, (int) screenPosY, (int) width, (int) height);

        // Cursor: vertical bar after the last typed character (screen coords)
        if (editMode) {
            double rowH = Settings.getRowHeight();
            // Text in drawSegment starts at col*colWidth+2 (offscreen), which maps
            // to colToScreenX(col)+2 on screen.  Add the pixel width of typed text
            // so the cursor sits right after the last character (normal text cursor).
            // Use the offscreen FontMetrics so the measurement matches the rendered text.
            FontMetrics fm = offscreenGraphics != null
                    ? offscreenGraphics.getFontMetrics()
                    : g.getFontMetrics();
            // Only measure the text to the LEFT of the cursor (not the full buffer)
            int safePos = Math.min(editCursorCharPos, editBuffer.length());
            int typedPx = fm.stringWidth(editBuffer.substring(0, safePos));
            int cursorX  = colToScreenX(editCursorCol) + 2 + typedPx;
            int cursorY1 = (int) Math.round(screenPosY + previewRow * rowH) + 2;
            int cursorY2 = (int) Math.round(screenPosY + (previewRow + 1) * rowH) - 2;
            Stroke oldStroke = g.getStroke();
            g.setStroke(new BasicStroke(2f));
            g.setColor(Color.BLUE);
            g.drawLine(cursorX, cursorY1, cursorX, cursorY2);
            g.setStroke(oldStroke);
        }
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
