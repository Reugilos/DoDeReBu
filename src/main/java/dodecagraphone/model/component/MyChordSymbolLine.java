/*
 * PolyForm Noncommercial License 1.0.0
 * Copyright (c) 2024-2026 Pau Bofill. Powered by Claude AI.
 * Full license / Llicència completa: LICENSE (project root / arrel del projecte)
 */
package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.chord.Chord;
import dodecagraphone.model.chord.ChordSymbols;
import dodecagraphone.model.color.ColorSets;
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
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * [CA] Franja horitzontal de símbols d'acord que fa scroll juntament amb la
 * partitura. Mostra els símbols d'acord del {@code chordSymbolLine} del
 * {@link MyGridScore}, les marques de canvi (tempo en blau, tonalitat en
 * granate, volum en verd i transposició en groc), i les línies de compàs i
 * temps. Les de volum i transposició són del track actual; les altres dues,
 * globals. Utilitza un buffer offscreen
 * ({@link BufferedImage}) per minimitzar el redibuix; el mètode
 * {@code drawFullChordLineInOffscreen()} regenera tot el buffer i
 * {@code draw(Graphics2D)} n'extreu la porció visible.
 * <p>
 * [EN] Horizontal chord symbol strip that scrolls with the score. Displays
 * chord symbols from the {@link MyGridScore} {@code chordSymbolLine}, change
 * markers (tempo in blue, key in maroon, volume in green and transposition in
 * yellow), and bar/beat lines. The volume and transposition ones belong to the
 * current track; the other two are global. Uses an
 * offscreen buffer ({@link BufferedImage}) to minimise redraws;
 * {@code drawFullChordLineInOffscreen()} regenerates the whole buffer and
 * {@code draw(Graphics2D)} extracts the visible slice.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
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
    private static final Color COLOR_TEMPO_BG  = new Color(0, 90, 190);
    /** Background colour for key change markers (white text on granate). */
    private static final Color COLOR_KEY_BG    = new Color(140, 0, 30);
    /** Background colour for volume change markers (white text on green). */
    private static final Color COLOR_VOLUME_BG = new Color(0, 130, 55);
    /** Fons de la marca de transposició (displayOffset). Groc: el text hi va en negre. */
    private static final Color COLOR_TRANSPOSE_BG = new Color(235, 200, 0);
    /** Cached chord font and the row height it was computed for. */
    private Font   cachedChordFont   = null;
    private double cachedChordRowH   = 0;
    private Font   cachedMarkFont    = null;
    private double cachedMarkRowH    = 0;
    private volatile boolean needsDrawing = true;

    /**
     * [CA] Tipus de marca de canvi dibuixada a la franja. El compàs no hi surt:
     * no té caixeta pròpia i per tant no és seleccionable.
     * <p>
     * [EN] Kind of change mark drawn in the strip. Time signature is absent: it
     * has no box of its own and hence is not selectable.
     */
    public enum MarkKind { TEMPO, KEY, VOLUME, TRANSPOSE }

    /**
     * [CA] Marca dibuixada, descrita en termes independents de la
     * transformació de dibuix: la columna on comença, quantes columnes ocupa la
     * caixeta, i la seva posició dins la pila vertical de marques d'aquella
     * columna ({@code yOff} des de baix i alçada {@code h}).
     * <p>
     * [EN] A drawn mark, described in terms independent of the drawing
     * transform: the starting column, how many columns the box spans, and its
     * place in that column's vertical stack of marks ({@code yOff} from the
     * bottom, height {@code h}).
     */
    public static class MarkBox {
        public final int col;
        public final MarkKind kind;
        /** Columnes de partitura que ocupa la caixeta (mínim 1). */
        public final int spanCols;
        /** Desplaçament de la caixeta des de la base de la pila, en píxels. */
        public final int yOff;
        /** Alçada de la caixeta en píxels. */
        public final int h;

        MarkBox(int col, MarkKind kind, int spanCols, int yOff, int h) {
            this.col      = col;
            this.kind     = kind;
            this.spanCols = spanCols;
            this.yOff     = yOff;
            this.h        = h;
        }
    }

    /**
     * Marques dibuixades a l'offscreen, en ordre de dibuix. Es buida a cada
     * {@code drawFullChordLineInOffscreen()}. Registrar-les en dibuixar-les és
     * més robust que recalcular-ne la geometria: l'amplada depèn de les
     * mètriques de font i les caixes s'apilen verticalment.
     */
    private final List<MarkBox> markBoxes = new java.util.ArrayList<>();

    /** Line gap in pixels between chord text lines (tighter than font leading). */
    private static final int LINE_GAP = 1;
    /** Pixels reserved at the very bottom of the chord strip for the attack triangle. */
    private static final int TRIANGLE_SPACE = 10;

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

    /**
     * [CA] Font forçada per a la pila de marques quan la mida normal no hi
     * cabria. Null = mida normal.
     * <p>
     * [EN] Font forced on the mark stack when the normal size would not fit.
     * Null = normal size.
     */
    private Font markFontOverride = null;

    private Font getMarkFont() {
        if (markFontOverride != null) return markFontOverride;
        double rowH = Settings.getRowHeight();
        if (cachedMarkFont != null && rowH == cachedMarkRowH) return cachedMarkFont;
        cachedMarkRowH = rowH;
        int size = Math.max(6, (int)(rowH * 0.85));
        cachedMarkFont = new Font("SansSerif", Font.BOLD, size);
        return cachedMarkFont;
    }

    /**
     * [CA] Prepara la franja perquè {@code nMarks} marques apilades hi càpiguen.
     * Les marques s'apilen cap amunt des de la base, o sigui que quan la pila és
     * massa alta la que es perd és la de dalt de tot; amb quatre marques
     * (transposició, tempo, tonalitat i volum) la de volum quedava dibuixada per
     * sobre de la vora i es retallava.
     * <p>
     * El marge del triangle d'atac es respecta sempre: l'alçada de la franja
     * (Settings.DEFAULT_NROWS_CHORD) està calculada perquè hi càpiguen les
     * quatre marques i el triangle a partir de 720 px de pantalla, de manera
     * que encongir la lletra només hauria de passar en casos extrems.
     * <p>
     * [EN] Prepares the strip so that {@code nMarks} stacked marks fit. Marks
     * stack upward from the base, so an over-tall stack loses the topmost one;
     * with four marks (transposition, tempo, key and volume) the volume one was
     * drawn past the edge and clipped.
     * <p>
     * The attack-triangle margin is always preserved: the strip height
     * (Settings.DEFAULT_NROWS_CHORD) is sized so that the four marks and the
     * triangle fit from 720 px screen height upwards, so shrinking the font
     * should only happen in extreme cases.
     *
     * @param g      [CA] context on es mesurarà / [EN] context used for measuring
     * @param nMarks [CA] marques que s'apilaran / [EN] marks about to be stacked
     */
    private void fitMarkStack(Graphics2D g, int nMarks) {
        markFontOverride = null;
        if (nMarks <= 0) return;
        int available = (int) Math.round(nRows * Settings.getRowHeight()) - TRIANGLE_SPACE;
        int pad = 2;
        int normalSize = getMarkFont().getSize();
        int size = normalSize;
        while (size > 6) {
            Font f = new Font("SansSerif", Font.BOLD, size);
            FontMetrics fm = g.getFontMetrics(f);
            int stackH = nMarks * (fm.getAscent() + fm.getDescent() + 2 * pad);
            if (stackH <= available) {
                if (size != normalSize) markFontOverride = f;
                return;
            }
            size--;
        }
        markFontOverride = new Font("SansSerif", Font.BOLD, 6);
    }

    /** Torna la pila de marques a la mida normal. */
    private void clearMarkStackFit() {
        markFontOverride = null;
    }

    private static int lineStep(FontMetrics fm) {
        return fm.getAscent() + fm.getDescent() + LINE_GAP;
    }

    /**
     * [CA] Crea la franja de símbols d'acord associada a la partitura indicada.
     * <p>
     * [EN] Creates the chord symbol strip associated with the given score.
     *
     * @param firstCol [CA] primera columna del component / [EN] first column of the component
     * @param firstRow [CA] primera fila del component / [EN] first row of the component
     * @param nCols    [CA] nombre de columnes / [EN] number of columns
     * @param nRows    [CA] nombre de files / [EN] number of rows
     * @param parent   [CA] component pare / [EN] parent component
     * @param contr    [CA] controlador principal / [EN] main controller
     * @param score    [CA] graella de partitura associada / [EN] associated score grid
     */
    public MyChordSymbolLine(int firstCol, int firstRow, int nCols, int nRows, MyComponent parent, MyController contr, MyGridScore score) {
        super(firstCol, firstRow, nCols, nRows, parent, contr);
        this.score = score;
        this.contr = contr;
    }

    /**
     * [CA] Retorna la columna de partitura corresponent a la posició de
     * pantalla, alineada al beat, o -1 si la posició és fora de la franja.
     * <p>
     * [EN] Returns the score column corresponding to the screen position,
     * aligned to the beat, or -1 if the position is outside the strip.
     *
     * @param screenX [CA] coordenada X de pantalla / [EN] screen X coordinate
     * @param screenY [CA] coordenada Y de pantalla / [EN] screen Y coordinate
     * @return [CA] columna alineada al beat, o -1 / [EN] beat-aligned column, or -1
     */
    public int whichCol(double screenX, double screenY) {
        if (this.contains(screenX, screenY)) {
            int whichCol = this.contr.getAllPurposeScore().getCol(screenX);
            whichCol = whichCol - (whichCol % Settings.getnColsBeat());
            return whichCol;
        }
        return -1;
    }

    /**
     * [CA] Retorna la marca de canvi (tempo, to, volum o transposició) dibuixada sota la
     * posició de pantalla indicada, o null si no n'hi ha cap. La columna es
     * resol amb el mateix {@code getCol()} que fa servir {@code whichCol()} per
     * als acords, i la banda vertical es reconstrueix des de la base de la pila
     * de marques; així la detecció no depèn de la transformació de dibuix de
     * l'offscreen. El recorregut és invers perquè guanyi la marca de sobre.
     * <p>
     * [EN] Returns the change mark (tempo, key or volume) drawn under the given
     * screen position, or null if there is none. The column is resolved with the
     * same {@code getCol()} used by {@code whichCol()} for chords, and the
     * vertical band is rebuilt from the bottom of the mark stack, so detection
     * does not depend on the offscreen drawing transform. Iteration is reversed
     * so the mark drawn on top wins.
     *
     * @param screenX [CA] coordenada X de pantalla / [EN] screen X coordinate
     * @param screenY [CA] coordenada Y de pantalla / [EN] screen Y coordinate
     * @return [CA] la marca sota el punt, o null / [EN] the mark under the point, or null
     */
    public MarkBox whichMark(double screenX, double screenY) {
        if (!this.contains(screenX, screenY)) return null;
        // Mateixa inversió que fa servir whichCol() per als acords: getCol() ja
        // compensa la compressió de fit-anacrusis.
        int col = this.contr.getAllPurposeScore().getCol(screenX);
        if (col < 0) return null;
        int stripH    = (int) Math.round(nRows * Settings.getRowHeight());
        double stackY = screenPosY + stripH - TRIANGLE_SPACE;
        for (int i = markBoxes.size() - 1; i >= 0; i--) {
            MarkBox mb = markBoxes.get(i);
            if (col < mb.col || col >= mb.col + mb.spanCols) continue;
            double top = stackY - mb.yOff - mb.h;
            if (screenY >= top && screenY < top + mb.h) return mb;
        }
        return null;
    }

    /**
     * [CA] Obre un diàleg per introduir o editar un símbol d'acord. Si
     * l'usuari confirma un text vàlid, retorna el nou {@link Chord}; si
     * cancel·la retorna l'acord original; si deixa el camp buit retorna null
     * (senyal d'esborrar).
     * <p>
     * [EN] Opens a dialog to enter or edit a chord symbol. If the user
     * confirms a valid text, returns the new {@link Chord}; if cancelled
     * returns the original chord; if left empty returns null (delete signal).
     *
     * @param oldChord [CA] acord existent (pot ser null) / [EN] existing chord (may be null)
     * @return [CA] nou acord, acord original (cancel·lació) o null (esborrar) /
     *         [EN] new chord, original chord (cancel) or null (delete)
     */
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
     * @param score [CA] partitura associada / [EN] associated score
     */
    public void setScore(MyGridScore score) {
        this.score = score;
    }

    /**
     * Draws a measure line. To place the line, it uses score rows (negative)
     * and columns.
     *
     * @param col [CA] columna de partitura / [EN] score column
     * @param g [CA] context gràfic on dibuixar / [EN] graphics context to draw on
     * @param offscreen [CA] cert per dibuixar al buffer offscreen; fals, a pantalla / [EN] true to draw into the offscreen buffer; false, on screen
     */
    private void drawMeasureLine(int col, Graphics2D g, boolean offscreen) {
        //if (Settings.IS_BU) return;
        Stroke stroke = g.getStroke();
        g.setStroke(new BasicStroke(1.5f));
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
     * Draws a beat line. To place the line, it uses score rows (negative) and
     * columns.
     *
     * @param col [CA] columna de partitura / [EN] score column
     * @param g [CA] context gràfic on dibuixar / [EN] graphics context to draw on
     * @param offscreen [CA] cert per dibuixar al buffer offscreen; fals, a pantalla / [EN] true to draw into the offscreen buffer; false, on screen
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
     * @param chord [CA] acord a dibuixar / [EN] chord to draw
     * @param col [CA] columna de partitura / [EN] score column
     * @param g [CA] context gràfic on dibuixar / [EN] graphics context to draw on
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

        // Bottom of chord strip — text baseline drawn upward from here.
        // TRIANGLE_SPACE pixels are reserved at the very bottom for the attack triangle.
        int chordH = (int) Math.round(Settings.getnRowsChord() * Settings.getRowHeight());
        int camY = (offscreen ? 0 : (int) screenPosY) + chordH - TRIANGLE_SPACE - fm.getDescent() - 2;
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

        // Triangle at bottom pointing down, tip aligned with the left edge of the
        // attack column (= exact onset position). ayTip = chordH - 1 to stay inside
        // the buffer (chordH itself is out of bounds).
        {
            int offset = chord.getBeatColOffset();
            int cw = (int) Settings.getColWidth();
            int ax = (offscreen
                    ? (int) Math.floor((col + offset) * Settings.getColWidth())
                    : (int) Math.floor(score.getScreenX(col + offset)))
                    + cw / 2;
            int ayTip = (offscreen ? 0 : (int) screenPosY) + chordH - 1;
            int aw = 4;
            int ah = 6;
            int[] xs = { ax - aw, ax + aw, ax };
            int[] ys = { ayTip - ah, ayTip - ah, ayTip };
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
        return drawChangeMark(col, "" + bpm, COLOR_TEMPO_BG, MarkKind.TEMPO, g, offscreen, existingYOff);
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
        return drawChangeMark(col, text, COLOR_KEY_BG, MarkKind.KEY, g, offscreen, existingYOff);
    }

    /**
     * [CA] Marca de transposició: el {@code displayOffset} de la pista actual en
     * semitons. Ve de l'instrument (no del changeMap), o sigui que és
     * informativa: no es pot editar ni esborrar.
     * <p>
     * [EN] Transposition mark: the current track's {@code displayOffset} in
     * semitones. It comes from the instrument (not from the changeMap), so it
     * is informational: it cannot be edited or deleted.
     */
    private int drawTransposeMark(int col, int semitones, Graphics2D g, boolean offscreen, int existingYOff) {
        String txt = "t" + (semitones > 0 ? "+" : "") + semitones;
        return drawChangeMark(col, txt, COLOR_TRANSPOSE_BG, MarkKind.TRANSPOSE, g, offscreen, existingYOff);
    }

    /** Transposició vigent de la pista actual, en semitons. */
    private int currentTrackTranspose() {
        dodecagraphone.model.mixer.MyTrack t = contr.getMixer().getCurrentTrack();
        return (t != null) ? t.getDisplayOffset() : 0;
    }

    private int drawVolumeMark(int col, int velocity, Graphics2D g, boolean offscreen, int existingYOff) {
        return drawChangeMark(col, "v" + velocity, COLOR_VOLUME_BG, MarkKind.VOLUME, g, offscreen, existingYOff);
    }

    /**
     * Draws a small filled rectangle with contrast-coloured text (white on the
     * dark backgrounds, black on the yellow one) at the bottom-left of the
     * chord strip cell at {@code col}, stacked {@code existingYOff} pixels above
     * the bottom edge (markers stack upward).
     * Returns the height of the box so the caller can stack further markers.
     */
    private int drawChangeMark(int col, String text, Color bgColor, MarkKind kind,
                               Graphics2D g, boolean offscreen, int existingYOff) {
        Font prevFont = g.getFont();
        g.setFont(getMarkFont());
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
        int boxY    = cellTop + stripH - TRIANGLE_SPACE - boxH - existingYOff;

        g.setColor(bgColor);
        g.fillRect(boxX, boxY, boxW, boxH);
        // Color per contrast amb el fons: els fons foscos segueixen amb text
        // blanc, i el groc de la transposició el necessita negre.
        g.setColor(ColorSets.getSeparatorColor(bgColor));
        g.drawString(text, boxX + pad, boxY + pad + fm.getAscent());

        // Només el buffer de pantalla registra rectangles i pinta la selecció:
        // l'exportació a PDF reutilitza aquest mètode amb un altre Graphics2D.
        boolean toOwnBuffer = offscreen && g == offscreenGraphics;
        if (toOwnBuffer) {
            int spanCols = Math.max(1, (int) Math.ceil(boxW / Settings.getColWidth()));
            markBoxes.add(new MarkBox(col, kind, spanCols, existingYOff, boxH));
        }
        // Marca seleccionada: marc groc gruixut PER FORA de la caixa, amb un
        // filet negre exterior. Dibuixat a dins i de 2 px amb prou feines es
        // distingia del fons de la marca.
        if (toOwnBuffer && col == contr.getSelectedMarkCol() && kind == contr.getSelectedMarkKind()) {
            Stroke savedStroke = g.getStroke();
            g.setStroke(new BasicStroke(1f));
            g.setColor(Color.BLACK);
            g.drawRect(boxX - 4, boxY - 4, boxW + 7, boxH + 7);
            g.setStroke(new BasicStroke(3f));
            g.setColor(Color.YELLOW);
            g.drawRect(boxX - 2, boxY - 2, boxW + 3, boxH + 3);
            g.setStroke(savedStroke);
        }

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

    public int drawInitialMarkersAt(Graphics2D g, int tempo, int midiKey, char scaleMode) {
        int yOff = 0;
        if (tempo > 0) yOff += drawTempoMark(0, tempo, g, true, yOff);
        if (midiKey >= 0) yOff += drawKeyMark(0, midiKey, scaleMode, g, true, yOff);
        return yOff;
    }

    /**
     * [CA] Volum vigent del track actual quan no hi ha marca explícita.
     * <p>
     * [EN] Current track volume when there is no explicit mark.
     *
     * @return [CA] velocity del track actual / [EN] current track velocity
     */
    private int currentTrackVelocity() {
        dodecagraphone.model.mixer.MyTrack t = contr.getMixer().getCurrentTrack();
        return (t != null) ? t.getVelocity() : Settings.getDefaultVelocity();
    }

    /**
     * [CA] Marques de la columna 0 (tempo, tonalitat i volum) amb fallback als
     * valors per defecte. És el camí d'<b>impressió/PDF</b>: la pantalla les
     * dibuixa a {@code drawFullChordLineInOffscreen()}, que hi afegeix la de
     * transposició i només mostra el volum del track actual.
     * <p>
     * [EN] Column-0 marks (tempo, key and volume) with a fallback to the
     * defaults. This is the <b>print/PDF</b> path: on screen they are drawn by
     * {@code drawFullChordLineInOffscreen()}, which also adds the transposition
     * one and shows only the current track's volume.
     *
     * @param g [CA] context gràfic on dibuixar / [EN] graphics context to draw on
     */
    public void drawInitialMarkersAt(Graphics2D g) {
        MyGridScore.ScoreChange sc0 = score.getChangeMap().get(0);
        int tempo      = (sc0 != null && sc0.tempo     != null) ? sc0.tempo     : Settings.DEFAULT_TEMPO;
        int midiKey    = (sc0 != null && sc0.midiKey   != null) ? sc0.midiKey   : ToneRange.getDefaultKey();
        char scaleMode = (sc0 != null && sc0.scaleMode != null) ? sc0.scaleMode : ToneRange.getDefaultMode();
        int yOff = drawInitialMarkersAt(g, tempo, midiKey, scaleMode);
        if (sc0 != null) {
            for (Map.Entry<Integer, Integer> e : sc0.trackVelocities.entrySet()) {
                yOff += drawVolumeMark(0, e.getValue(), g, true, yOff);
            }
        }
    }

    public void initOffscreen() {
        if (offscreenGraphics != null) {
            offscreenGraphics.dispose();
        }
        int w = (int) (score.getNColsBuffer() * Settings.getColWidth());
        int h = (int) (nRows * Settings.getRowHeight());
        offscreenImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        offscreenGraphics = offscreenImage.createGraphics();
        offscreenGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Utilities.printOutWithPriority(false, "MyChordSymbolLine::initOffscreen: w=" + w + ", h=" + h);
        needsDrawing = true;
        drawFullChordLineInOffscreen();
    }

    public void resizeOffscreen(int newNCols) {
        if (offscreenImage == null) { initOffscreen(); return; }
        int newW = (int) (newNCols * Settings.getColWidth());
        int oldW = offscreenImage.getWidth();
        if (newW <= oldW) return;
        int h = offscreenImage.getHeight();
        BufferedImage newImg = new BufferedImage(newW, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D newG = newImg.createGraphics();
        newG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        newG.drawImage(offscreenImage, 0, 0, null);
        newG.setColor(Color.WHITE);
        newG.fillRect(oldW, 0, newW - oldW, h);
        offscreenGraphics.dispose();
        offscreenImage = newImg;
        offscreenGraphics = newG;
    }

    /**
     * Draws the complete chord symbol line (all columns) into the offscreen
     * buffer using absolute pixel coordinates (col * colWidth, 0).
     */
    public void drawFullChordLineInOffscreen() {
        synchronized (offscreenGraphics) {
            if (!needsDrawing) return;
            markBoxes.clear();
            // Clear background
            offscreenGraphics.setColor(Color.WHITE);
            offscreenGraphics.fillRect(0, 0, offscreenImage.getWidth(), offscreenImage.getHeight());

            Map<Integer, Chord> chords = score.getChordSimbolLine();
            int numCols = score.getNumCols();

            // Calcula les línies de beat i compàs considerant canvis de compàs mid-score
            boolean[] isBeat    = new boolean[numCols + 1];
            boolean[] isMeasure = new boolean[numCols + 1];
            score.computeBeatMeasureLines(numCols + 1, isBeat, isMeasure);

            // Marques de canvi de tempo i to. Les marques s'apilen verticalment de baix cap amunt.
            // Per a cada columna guardem l'amplada màxima de les marques (per desplaçar l'acord).
            TreeMap<Integer, Integer> markerMaxWidths = new TreeMap<>();
            TreeMap<Integer, MyGridScore.ScoreChange> changeMap = score.getChangeMap();

            // Només mostrem la marca de volum del track actual.
            int currentTrackId = contr.getMixer().getCurrentTrackId();

            // Col 0: sempre es dibuixa (amb fallback als defaults si el changeMap no té entrada).
            {
                MyGridScore.ScoreChange sc0 = changeMap.get(0);
                int tempo0   = (sc0 != null && sc0.tempo    != null) ? sc0.tempo    : Settings.DEFAULT_TEMPO;
                int midiKey0 = (sc0 != null && sc0.midiKey  != null) ? sc0.midiKey  : ToneRange.getDefaultKey();
                char mode0   = (sc0 != null && sc0.scaleMode != null) ? sc0.scaleMode : ToneRange.getDefaultMode();
                // Quatre marques a la columna 0: transposició, tempo, tonalitat i
                // volum. Cal encongir la font si no hi caben totes.
                fitMarkStack(offscreenGraphics, 4);
                int yOff = 0;
                // Primera de la pila = fila inferior (les marques s'apilen cap amunt).
                yOff += drawTransposeMark(0, currentTrackTranspose(), offscreenGraphics, true, yOff);
                yOff += drawTempoMark(0, tempo0, offscreenGraphics, true, yOff);
                yOff += drawKeyMark(0, midiKey0, mode0, offscreenGraphics, true, yOff);
                // Volum: es mostra sempre, com el tempo i la tonalitat. Sense marca
                // explícita el valor és el del track actual, que arrenca a
                // Settings.getDefaultVelocity(); així la marca no menteix mai.
                Integer vel0 = (sc0 != null) ? sc0.trackVelocities.get(currentTrackId) : null;
                int vel0Shown = (vel0 != null) ? vel0 : currentTrackVelocity();
                yOff += drawVolumeMark(0, vel0Shown, offscreenGraphics, true, yOff);
                if (yOff > 0) {
                    offscreenGraphics.setFont(getMarkFont());
                    FontMetrics fm = offscreenGraphics.getFontMetrics();
                    int pad = 2;
                    int maxW = fm.stringWidth("" + tempo0) + 2 * pad;
                    try { maxW = Math.max(maxW, fm.stringWidth(ToneRange.getKeyName(midiKey0, mode0)) + 2 * pad); }
                    catch (Exception ignored) {}
                    maxW = Math.max(maxW, fm.stringWidth("v" + vel0Shown) + 2 * pad);
                    int tr0 = currentTrackTranspose();
                    maxW = Math.max(maxW, fm.stringWidth("t" + (tr0 > 0 ? "+" : "") + tr0) + 2 * pad);
                    markerMaxWidths.put(0, maxW);
                }
                clearMarkStackFit();
            }

            // Col > 0: marques del changeMap.
            for (Map.Entry<Integer, MyGridScore.ScoreChange> entry : changeMap.entrySet()) {
                int col = entry.getKey();
                if (col <= 0 || col >= numCols) continue;
                MyGridScore.ScoreChange sc = entry.getValue();
                int yOff   = 0;
                int maxW   = 0;
                if (sc.tempo != null) {
                    yOff += drawTempoMark(col, sc.tempo, offscreenGraphics, true, yOff);
                }
                if (sc.midiKey != null) {
                    char mode = (sc.scaleMode != null) ? sc.scaleMode : 'M';
                    yOff += drawKeyMark(col, sc.midiKey, mode, offscreenGraphics, true, yOff);
                }
                // Volum: només el del track actual.
                Integer velCur = sc.trackVelocities.get(currentTrackId);
                if (velCur != null) {
                    yOff += drawVolumeMark(col, velCur, offscreenGraphics, true, yOff);
                }
                if (yOff > 0) {
                    offscreenGraphics.setFont(getMarkFont());
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
                    if (velCur != null) {
                        maxW = Math.max(maxW, fm.stringWidth("v" + velCur) + 2 * pad);
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
     * @param g [CA] context gràfic on dibuixar / [EN] graphics context to draw on
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
                // Teclat dret: chord dins la càmera, desplaçament idèntic al grid.
                int firstCamColIn = Math.max(0, Settings.getnColsCam() - ccol);
                int actualWidthInCols = Settings.getnColsCam() - firstCamColIn;
                x1 = (int) Math.round(contr.getCam().getScreenX(firstCamColIn));
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

            // Línies de beat i compàs en screen-space (no a l'offscreen) per evitar
            // que desapareguin en escalar amb fit-anacrusis.
            {
                int numCols = score.getNumCols();
                boolean[] isBeat    = new boolean[numCols + 1];
                boolean[] isMeasure = new boolean[numCols + 1];
                score.computeBeatMeasureLines(numCols + 1, isBeat, isMeasure);
                double scaleX = w2 > 0 ? (double) w / w2 : 1.0;
                int sy1 = y1;
                int sy2 = y1 + h;
                Shape oldClip = g.getClip();
                g.clipRect(x1, y1, w, h);
                Stroke saved = g.getStroke();
                g.setColor(Color.BLACK);
                for (int col = 0; col <= numCols; col++) {
                    if (!isBeat[col] && !isMeasure[col]) continue;
                    int offX = (int) Math.floor(col * Settings.getColWidth());
                    int sx = x1 + (int) Math.round((offX - x2) * scaleX);
                    if (isMeasure[col]) {
                        g.setStroke(new BasicStroke(2f));
                        g.drawLine(sx, sy1, sx, sy2);
                    } else {
                        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT,
                                BasicStroke.JOIN_BEVEL, 0, new float[]{15, 3}, 0);
                        g.setStroke(dashed);
                        g.drawLine(sx, sy1, sx, sy2);
                    }
                }
                g.setStroke(saved);
                g.setClip(oldClip);
            }

            // Doble barra al stopCol — dibuixada sobre la vista (sempre actualitzada)
            int stopC = contr.getAllPurposeScore().getStopCol();
            if (stopC > 0 && contr.getAllPurposeScore().isStopMarkerValid()) {
                int sx = (int) Math.floor(score.getScreenX(stopC)) - 1;
                int sy1 = (int) Math.round(score.getScreenY(-nRows));
                int sy2 = sy1 + (int) Math.round(nRows * Settings.getRowHeight());
                Shape oldClip = g.getClip();
                g.clipRect(x1, y1, w, h);
                Stroke saved = g.getStroke();
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(3));
                g.drawLine(sx, sy1, sx, sy2);
                g.setStroke(new BasicStroke(2));
                g.drawLine(sx + 4, sy1, sx + 4, sy2);
                g.setStroke(saved);
                g.setClip(oldClip);
            }

        } else {
            // ---- Live (fallback) path ----
            // if (!Settings.IS_BU) {  // debug print eliminat (IS_BU sempre true)
            //     System.out.println("MyChordSymbolLine::draw nColsBeat = " + ...);
            // }
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
