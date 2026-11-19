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
import dodecagraphone.ui.I18n;
import dodecagraphone.ui.MyDialogs;
import dodecagraphone.ui.DrawKit;
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
 * granate i volum en verd), i les línies de compàs i temps. La de volum és
 * del track actual; les altres dues, globals. (La transposició de
 * l'instrument ja no hi surt com a marca groga: vegeu {@link MarkKind#TRANSPOSE}.) Utilitza un buffer offscreen
 * ({@link BufferedImage}) per minimitzar el redibuix; el mètode
 * {@code drawFullChordLineInOffscreen()} regenera tot el buffer i
 * {@code draw(Graphics2D)} n'extreu la porció visible.
 * <p>
 * [EN] Horizontal chord symbol strip that scrolls with the score. Displays
 * chord symbols from the {@link MyGridScore} {@code chordSymbolLine}, change
 * markers (tempo in blue, key in maroon and volume in green), and bar/beat
 * lines. The volume one belongs to the current track; the other two are
 * global. (Instrument transposition is no longer a yellow mark here: see
 * {@link MarkKind#TRANSPOSE}.) Uses an
 * offscreen buffer ({@link BufferedImage}) to minimise redraws;
 * {@code drawFullChordLineInOffscreen()} regenerates the whole buffer and
 * {@code draw(Graphics2D)} extracts the visible slice.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 5.0
 */
public class MyChordSymbolLine extends MyComponent {

    private MyGridScore score;
    private MyController contr;
    private BufferedImage offscreenImage;
    private Graphics2D offscreenGraphics;

    /** Index into ChordSymbols.DISPLAY_FORMATS for the current display format. */
    private int displayFormatIdx = 0;

    /**
     * [CA] Format amb què es mostren ara els acords.
     * <p>
     * [EN] Format the chords are currently displayed in.
     *
     * @return [CA] una de les constants {@code ChordSymbols.FORMAT_*} /
     *         [EN] one of the {@code ChordSymbols.FORMAT_*} constants
     */
    public String getDisplayFormat() {
        return ChordSymbols.DISPLAY_FORMATS[displayFormatIdx];
    }

    /**
     * [CA] Passa al format següent de {@code ChordSymbols.DISPLAY_FORMATS} i
     * marca la franja per redibuixar. És el que fa el botó de format.
     * <p>
     * [EN] Moves to the next format in {@code ChordSymbols.DISPLAY_FORMATS} and
     * flags the strip for redrawing. This is what the format button does.
     */
    public void cycleDisplayFormat() {
        displayFormatIdx = (displayFormatIdx + 1) % ChordSymbols.DISPLAY_FORMATS.length;
        needsDrawing = true;
    }

    /**
     * [CA] Indica si l'offscreen de la franja s'ha de refer.
     * <p>
     * [EN] Whether the strip's offscreen needs redrawing.
     *
     * @return [CA] cert si cal redibuixar / [EN] true if a redraw is needed
     */
    public boolean isNeedsDrawing() { return needsDrawing; }

    /**
     * [CA] Marca o desmarca la franja per redibuixar. {@code drawFullChordLineInOffscreen}
     * no fa res si el flag és fals, o sigui que cal aixecar-lo sempre que en
     * canviï el contingut.
     * <p>
     * [EN] Flags the strip for redrawing. {@code drawFullChordLineInOffscreen}
     * does nothing when the flag is false.
     *
     * @param needsDrawing [CA] cert per forçar el redibuix / [EN] true to force a redraw
     */
    public void setNeedsDrawing(boolean needsDrawing) { this.needsDrawing = needsDrawing; }

    /** Cached chord font and the row height it was computed for. */
    private Font   cachedChordFont   = null;
    private double cachedChordRowH   = 0;
    private volatile boolean needsDrawing = true;

    /**
     * [CA] Tipus de marca de canvi dibuixada a la franja. El compàs no hi surt:
     * no té caixeta pròpia i per tant no és seleccionable.
     * <p>
     * [EN] Kind of change mark drawn in the strip. Time signature is absent: it
     * has no box of its own and hence is not selectable.
     */
    public enum MarkKind {
        /** Canvi de tempo; blau, global. */
        TEMPO,
        /** Canvi de tonalitat; granate, global. */
        KEY,
        /** Canvi de volum; verd, per pista. */
        VOLUME,
        /**
         * [CA] Transposició de la pista; groc, informativa i no editable.
         * <b>En desús:</b> ja no es dibuixa a la franja. La transposició de
         * l'instrument surt ara al botó de l'instrument, entre parèntesis
         * ({@code Glock (24 st)}, vegeu {@code MyController.updateTextOfButtons}).
         * Es conserva el valor, i el tractament que en fan {@code MyController}
         * i {@link DrawKit.Marca#TRANSPOSICIO}, per si es torna a posar la marca;
         * ara no hi ha cap codi que el crei, de manera que és inabastable.
         * <p>
         * [EN] Track transposition; yellow, informational, not editable.
         * <b>Deprecated in practice:</b> it is no longer drawn in the strip; the
         * instrument's transposition now appears on the instrument button, in
         * brackets ({@code Glock (24 st)}). Kept, with its handling in
         * {@code MyController} and {@link DrawKit.Marca#TRANSPOSICIO}, in case the
         * mark returns; nothing creates it now, so it is unreachable.
         */
        TRANSPOSE;

        /** [CA] Com es dibuixa (el color el tria DrawKit). / [EN] How it is drawn. */
        DrawKit.Marca dibuix() {
            switch (this) {
                case TEMPO:  return DrawKit.Marca.TEMPO;
                case KEY:    return DrawKit.Marca.TONALITAT;
                case VOLUME: return DrawKit.Marca.VOLUM;
                default:     return DrawKit.Marca.TRANSPOSICIO;
            }
        }
    }

    /**
     * [CA] Marca dibuixada, descrita en termes independents de la
     * transformació de dibuix: la columna on comença el grup de marques, el
     * desplaçament horitzontal de la caixeta dins d'aquell grup ({@code xOff},
     * en píxels: les marques d'una mateixa columna van <b>una al costat de
     * l'altra</b> al carril de dalt) i l'amplada {@code w}.
     * <p>
     * [EN] A drawn mark, described in terms independent of the drawing
     * transform: the column where the group of marks starts, the box's
     * horizontal offset inside that group ({@code xOff}, pixels: marks of one
     * column sit <b>side by side</b> in the top lane) and its width {@code w}.
     */
    public static class MarkBox {
        /** Columna de partitura on comença el grup de marques. */
        public final int col;
        /** Tipus de marca que representa. */
        public final MarkKind kind;
        /** Primera columna de partitura que cobreix la caixeta. */
        public final int firstCol;
        /** Última columna de partitura que cobreix la caixeta (inclosa). */
        public final int lastCol;
        /** Desplaçament horitzontal de la caixeta des de la columna {@code col}, en píxels. */
        public final int xOff;
        /** Amplada de la caixeta en píxels. */
        public final int w;
        /** Alçada de la caixeta en píxels. */
        public final int h;

        MarkBox(int col, MarkKind kind, int firstCol, int lastCol, int xOff, int w, int h) {
            this.col      = col;
            this.kind     = kind;
            this.firstCol = firstCol;
            this.lastCol  = lastCol;
            this.xOff     = xOff;
            this.w        = w;
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
    /** [CA] Marge entre la vora de dalt de la franja i el carril de marques, en píxels. */
    private static final int LANE_PAD = 1;
    /** [CA] Separació horitzontal entre dues marques d'una mateixa columna, en píxels. */
    private static final int MARK_GAP = 2;

    /** Mida mínima a què es pot encongir la font d'un acord. */
    private static final int MIN_CHORD_FONT = 6;

    /**
     * [CA] Font comuna de tots els acords de la franja quan la normal no hi
     * cabria. Null = mida normal. La calcula {@link #computeChordFontOverride}
     * a cada redibuix complet de l'offscreen.
     * <p>
     * [EN] Font shared by every chord in the strip when the normal one would not
     * fit. Null = normal size.
     */
    private Font chordFontOverride = null;

    /**
     * [CA] Línia de base del text de l'acord: el peu de la franja, menys l'espai
     * del trianglet d'atac i el descens de la font.
     * <p>
     * [EN] Baseline for the chord text: bottom of the strip, minus the attack
     * triangle space and the font descent.
     */
    private int chordBaselineY(boolean offscreen, int chordH, FontMetrics fm) {
        return (offscreen ? 0 : (int) screenPosY) + chordH - TRIANGLE_SPACE - fm.getDescent() - 2;
    }

    /**
     * [CA] Amplada total de les tres columnes d'un acord amb la mètrica donada.
     * Calca la geometria de {@code drawChordSymbol}: arrel, buit, columna de la
     * quatríada i, si n'hi ha, buit i columna de les extensions.
     * <p>
     * [EN] Total width of a chord's three columns with the given metrics. Mirrors
     * the geometry of {@code drawChordSymbol}.
     */
    /**
     * [CA] Amplada d'una de les dues columnes apilades: la de l'entrada més
     * ampla, però mai menys de <b>dues xifres</b>. Sense aquest mínim, un acord
     * de números d'una sola xifra ({@code Do[0,4,7]}) tenia la columna tan
     * ampla com una xifra i l'alineació a la dreta no es notava: els seus
     * dígits queien on els altres acords hi tenen les desenes. Amb el mínim,
     * totes les unitats de la franja cauen a la mateixa vertical.
     * <p>
     * [EN] Width of one of the two stacked columns: that of its widest entry,
     * but never less than <b>two digits</b>. Without this floor, a chord of
     * single-digit numbers ({@code Do[0,4,7]}) had a one-digit column and the
     * right alignment made no visible difference: its digits fell where other
     * chords have their tens. With the floor, every units digit in the strip
     * lands on the same vertical.
     */
    private static int stackColWidth(FontMetrics fm, List<String> lines, int from, int to) {
        int w = 2 * fm.charWidth('0');
        for (int i = from; i <= to; i++) w = Math.max(w, fm.stringWidth(lines.get(i)));
        return w;
    }

    private static int chordWidth(FontMetrics fm, List<String> lines,
            int splitIdx, int col2Count, int gap) {
        int col2W = stackColWidth(fm, lines, 1, col2Count);
        int w = fm.stringWidth(lines.get(0)) + gap + col2W;
        if (lines.size() > splitIdx) {
            w += gap + stackColWidth(fm, lines, splitIdx, lines.size() - 1);
        }
        return w;
    }

    /**
     * [CA] Espai horitzontal de què disposa l'acord de la columna {@code col}:
     * fins al següent acord de la franja. {@code 0} vol dir que no n'hi ha cap a
     * prop i que, doncs, no cal limitar-lo.
     * <p>
     * Només es miren dos compassos endavant: més enllà l'amplada ja no mana, i
     * el mapa d'acords és un {@code HashMap} sense ordre, o sigui que no se'n pot
     * demanar la clau següent.
     * <p>
     * [EN] Horizontal room available to the chord at {@code col}: up to the next
     * chord in the strip. {@code 0} means there is none nearby, so no limit.
     */
    private int availWidthFor(int col, int xOffset) {
        Map<Integer, Chord> chords = score.getChordSimbolLine();
        if (chords == null) return 0;
        int maxScan = Settings.getnColsBeat() * Math.max(1, score.getNumBeatsMeasure()) * 2;
        for (int k = col + 1; k <= col + maxScan; k++) {
            if (chords.get(k) != null) {
                return Math.max(1, (int) ((k - col) * Settings.getColWidth()) - xOffset - 2);
            }
        }
        return 0;
    }

    /**
     * [CA] Mida de font comuna a tots els acords de la franja: la més petita que
     * necessita cap d'ells per no envair el del costat. S'ha de cridar abans de
     * dibuixar-ne cap, perquè canviar de font també mou la línia de base.
     * <p>
     * Es tria una sola mida per a tota la franja, no una per acord: una filera de
     * símbols amb mides diferents es llegeix malament, encara que cada un
     * aprofités tot l'espai que té.
     * <p>
     * [EN] Font size shared by every chord in the strip: the smallest any of them
     * needs so as not to run into its neighbour. One size for the whole strip
     * rather than one per chord, which would read badly.
     */
    private void computeChordFontOverride(Graphics2D g, Map<Integer, Chord> chords,
            int numCols, Map<Integer, Integer> markerMaxWidths) {
        chordFontOverride = null;              // perquè getChordFont torni la base
        Font base = getChordFont(g);
        Font smallest = base;
        String fmt = getDisplayFormat();
        for (int col = 0; col < numCols; col++) {
            Chord chord = chords.get(col);
            if (chord == null || !chord.isValidChord() || chord.getShape() == null) continue;
            List<String> lines = ChordSymbols.chordToFormatLines(chord, fmt);
            if (lines == null || lines.isEmpty()) continue;
            int availW = availWidthFor(col, markerMaxWidths.getOrDefault(col, 0));
            if (availW <= 0) continue;
            int splitIdx  = ChordSymbols.baseLinesCount(chord);
            int col2Count = Math.min(splitIdx - 1, 4);
            Font f = fitChordFont(g, base, lines, splitIdx, col2Count, 3, availW);
            if (f.getSize() < smallest.getSize()) smallest = f;
        }
        if (smallest.getSize() < base.getSize()) chordFontOverride = smallest;
    }

    /**
     * [CA] Encongeix la font fins que les tres columnes de l'acord caben en
     * {@code availW} píxels. {@link #getBaseChordFont} només mira l'alçada —omple la
     * franja amb quatre línies—, i amb acords de cinc notes o en format Midi això
     * desbordava horitzontalment i es trepitjava l'acord del costat.
     * <p>
     * [EN] Shrinks the font until the chord's three columns fit in {@code availW}
     * pixels. {@link #getBaseChordFont} only looks at height, which overflowed
     * horizontally for five-note chords or the Midi format.
     */
    private Font fitChordFont(Graphics2D g, Font base, List<String> lines,
            int splitIdx, int col2Count, int gap, int availW) {
        Font f = base;
        FontMetrics fm = g.getFontMetrics(f);
        int size = f.getSize();
        while (size > MIN_CHORD_FONT && chordWidth(fm, lines, splitIdx, col2Count, gap) > availW) {
            size--;
            f = DrawKit.fontAcords(size);
            fm = g.getFontMetrics(f);
        }
        return f;
    }

    /**
     * [CA] Font amb què es dibuixa un acord: la base, o la reduïda per
     * {@link #computeChordFontOverride} si a la franja hi ha acords que no hi
     * cabrien d'ample.
     * <p>
     * [EN] Font a chord is drawn with: the base one, or the one reduced by
     * {@link #computeChordFontOverride} when some chord in the strip would not
     * fit widthwise.
     *
     * @param g [CA] no s'usa; es mesura amb {@link #metrics} /
     *          [EN] unused; measuring goes through {@link #metrics}
     * @return [CA] la font a aplicar / [EN] the font to apply
     */
    private Font getChordFont(Graphics2D g) {
        if (chordFontOverride != null) return chordFontOverride;
        return getBaseChordFont();
    }

    /**
     * [CA] Font d'una línia de text de l'acord, sense l'ajust horitzontal de
     * {@link #computeChordFontOverride}. És també la de les marques de canvi,
     * perquè una marca i una línia d'acord han de fer la mateixa mida: és el
     * mateix text, a la mateixa franja, a la mateixa alçada.
     * <p>
     * La base és <b>una fila de la franja per línia</b> ({@code rowH*0.85}).
     * Repartint tota l'alçada entre quatre línies ({@code available/4}), que és
     * el que feia, la lletra sortia enorme d'ençà que la franja va passar a 8
     * files; aquell límit es queda només com a sostre, per si la franja fos
     * baixa.
     * <p>
     * [EN] Font of one line of chord text, without the horizontal fit of
     * {@link #computeChordFontOverride}. It is also the change marks' font,
     * because a mark and a chord line must be the same size: same text, same
     * strip, same height.
     *
     * @return [CA] la font base / [EN] the base font
     */
    private Font getBaseChordFont() {
        double rowH = Settings.getRowHeight();
        if (cachedChordFont != null && rowH == cachedChordRowH) return cachedChordFont;
        cachedChordRowH = rowH;
        int chordH = (int) Math.round(nRows * rowH);
        int arrowH = 7;   // 5px triangle + 2px gap
        int botMargin = 2;
        int available = chordH - arrowH - botMargin;
        int size = Math.max(MIN_CHORD_FONT, (int) Math.min(available / 4.0, rowH * 0.85));
        Font f = DrawKit.fontAcords(size);
        FontMetrics fm = metrics().getFontMetrics(f);
        while (lineStep(fm) * 4 - LINE_GAP > available && size > MIN_CHORD_FONT) {
            size--;
            f = DrawKit.fontAcords(size);
            fm = metrics().getFontMetrics(f);
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

    /**
     * [CA] Alçada forçada de la caixa d'una marca quan la pila no cabria a la
     * franja. 0 = l'alçada d'una línia d'acord.
     * <p>
     * [EN] Box height forced on a mark when the stack would not fit the strip.
     * 0 = the height of one chord line.
     */
    private int markBoxHOverride = 0;

    /**
     * [CA] Alçada de la caixa d'una marca: <b>exactament una línia del símbol
     * d'acord</b> ({@code ascent + descent + LINE_GAP} de la font base). Marca i
     * acord comparteixen franja i mida de lletra, o sigui que la pila de quatre
     * marques inicials fa el mateix alt que les quatre línies d'un acord.
     * <p>
     * No surt de {@code Settings.getRowHeight()}: una línia d'acord en fa poc
     * més d'una, de fila (la font és {@code rowH*0.85} i l'interlineat hi
     * afegeix), i lligar la caixa a la fila deixava les marques més petites que
     * el text del costat.
     * <p>
     * [EN] Height of a mark box: <b>exactly one line of the chord symbol</b>
     * ({@code ascent + descent + LINE_GAP} of the base font). Marks and chords
     * share the strip and the font size, so the stack of four initial marks is
     * as tall as the four lines of a chord.
     * <p>
     * It is not {@code Settings.getRowHeight()}: a chord line is slightly more
     * than one row (the font is {@code rowH*0.85} plus the line gap), and tying
     * the box to the row left the marks smaller than the text beside them.
     *
     * @return [CA] alçada de la caixa en píxels / [EN] box height in pixels
     */
    private int markBoxHeight() {
        if (markBoxHOverride > 0) return markBoxHOverride;
        return lineStep(metrics().getFontMetrics(getMarkFont()));
    }

    /**
     * [CA] Mida de lletra més gran amb {@code ascent + descent + LINE_GAP} dins
     * de {@code boxH} píxels. Només la fa servir {@link #fitMarkStack}, quan la
     * pila no hi cap i cal abaixar caixa i lletra alhora.
     * <p>
     * [EN] Largest font size whose {@code ascent + descent + LINE_GAP} fits in
     * {@code boxH} pixels. Only used by {@link #fitMarkStack}, when the stack
     * does not fit and box and font must come down together.
     */
    private static int markFontSizeFor(int boxH) {
        int size = MIN_CHORD_FONT;
        while (size < 80) {
            FontMetrics fm = metrics().getFontMetrics(DrawKit.fontAcords(size + 1));
            if (lineStep(fm) > boxH) break;
            size++;
        }
        return size;
    }

    /** Context de mesura: només se li demanen mètriques de font. */
    private static Graphics2D metricsGraphics = null;

    private static Graphics2D metrics() {
        if (metricsGraphics == null) {
            metricsGraphics = new java.awt.image.BufferedImage(1, 1,
                    java.awt.image.BufferedImage.TYPE_INT_ARGB).createGraphics();
        }
        return metricsGraphics;
    }

    /**
     * [CA] Font de les marques de canvi: la mateixa que la d'una línia d'acord.
     * <p>
     * [EN] Change-mark font: the same as one line of chord text.
     */
    private Font getMarkFont() {
        if (markFontOverride != null) return markFontOverride;
        return getBaseChordFont();
    }

    /**
     * [CA] <b>En desús</b>: les marques ja no s'apilen sobre els acords sinó que
     * van una al costat de l'altra al carril de dalt ({@link #drawMarkGroup}), de
     * manera que no cal encongir-ne la pila. Es conserva per si torna la pila.
     * <p>
     * [CA] Prepara la franja perquè {@code nMarks} marques apilades hi càpiguen.
     * Les marques s'apilen cap amunt des de la base, o sigui que quan la pila és
     * massa alta la que es perd és la de dalt de tot; amb quatre marques
     * (transposició, tempo, tonalitat i volum) la de volum quedava dibuixada per
     * sobre de la vora i es retallava.
     * <p>
     * Cada marca fa una línia d'acord ({@link #markBoxHeight}), o sigui que la
     * pila de quatre ocupa el mateix que les quatre línies d'un acord; amb la
     * franja a {@code Settings.DEFAULT_NROWS_CHORD} = 6 files i el triangle
     * d'atac a sota, hi caben a qualsevol mida de pantalla. Si mai no hi
     * cabessin, aquí s'abaixa l'alçada de la caixa i la lletra a l'una:
     * encongir només la lletra no serviria de res, perquè l'alçada ja no en
     * depèn.
     * <p>
     * [EN] Prepares the strip so that {@code nMarks} stacked marks fit. Marks
     * stack upward from the base, so an over-tall stack loses the topmost one;
     * with four marks (transposition, tempo, key and volume) the volume one was
     * drawn past the edge and clipped.
     * <p>
     * Each mark is one chord line tall ({@link #markBoxHeight}), so a stack of
     * four takes as much as the four lines of a chord; with the strip at
     * {@code Settings.DEFAULT_NROWS_CHORD} = 6 rows and the attack triangle
     * below, they fit at any screen height. If they ever did not, the box
     * height and the font are lowered together: shrinking only the font would
     * do nothing, since the height no longer depends on it.
     *
     * @param g      [CA] context on es mesurarà / [EN] context used for measuring
     * @param nMarks [CA] marques que s'apilaran / [EN] marks about to be stacked
     */
    private void fitMarkStack(Graphics2D g, int nMarks) {
        markFontOverride = null;
        markBoxHOverride = 0;
        if (nMarks <= 0) return;
        int available = (int) Math.round(nRows * Settings.getRowHeight()) - TRIANGLE_SPACE;
        int boxH = markBoxHeight();
        if (nMarks * boxH <= available) return;      // el cas normal: hi caben
        markBoxHOverride = Math.max(8, available / nMarks);
        markFontOverride = DrawKit.fontAcords(markFontSizeFor(markBoxHOverride));
    }

    /** Torna la pila de marques a la mida normal (una línia d'acord per marca). */
    private void clearMarkStackFit() {
        markFontOverride = null;
        markBoxHOverride = 0;
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
        // El carril de marques de dalt no és de cap acord: no admet entrada.
        if (screenY < screenPosY + Settings.getMarksLaneHeight()) return -1;
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
        // Les marques viuen al carril de dalt de la franja: una sola banda
        // vertical, i la columna decideix quina caixeta és.
        double top = screenPosY;
        int laneH = Settings.getMarksLaneHeight();
        for (int i = markBoxes.size() - 1; i >= 0; i--) {
            MarkBox mb = markBoxes.get(i);
            if (col < mb.firstCol || col > mb.lastCol) continue;
            if (screenY >= top && screenY < top + laneH) return mb;
        }
        return null;
    }

    /**
     * [CA] Obre un diàleg per introduir o editar un símbol d'acord. Si
     * l'usuari confirma un text vàlid, retorna el nou {@link Chord}; si
     * cancel·la retorna l'acord original; si deixa el camp buit retorna null
     * (senyal d'esborrar).
     * <p>
     * Després demana en quina columna del temps ha de sonar (d'1 a
     * {@code nColsBeat} de cara a l'usuari; el model guarda 0..nColsBeat-1).
     * Cancel·lar aquest segon diàleg, o respondre-hi una cosa que no sigui un
     * número dins del rang, equival a cancel·lar tota l'edició: l'acord es
     * queda tal com estava. Deixar el camp buit manté la columna que ja tenia.
     * <p>
     * [EN] Opens a dialog to enter or edit a chord symbol. If the user
     * confirms a valid text, returns the new {@link Chord}; if cancelled
     * returns the original chord; if left empty returns null (delete signal).
     * <p>
     * It then asks which column within the beat it should sound on (1 to
     * {@code nColsBeat} for the user; the model stores 0..nColsBeat-1).
     * Cancelling that second dialog, or answering it with anything that is not
     * a number within range, cancels the whole edit: the chord stays as it
     * was. Leaving the field empty keeps the column it already had.
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
        // Preguntem en quina columna del beat ha de sonar. De cara a l'usuari
        // les columnes van d'1 a nColsBeat, com al tip del cursor de la
        // graella; el model les guarda com a offset 0..nColsBeat-1.
        int nColsBeat = Settings.getnColsBeat();
        int defOffset = (oldChord != null) ? oldChord.getBeatColOffset() : 0;
        // AllowEmpty per distingir Cancel (null) de camp buit (""): amb
        // mostraInputDialog les dues coses arriben com a null.
        String offsetStr = MyDialogs.mostraInputDialogAllowEmpty(
                I18n.f("myChordSymbolLine.enterBeatColOffset.prompt", nColsBeat),
                I18n.t("myChordSymbolLine.enterBeatColOffset.title"),
                "" + (defOffset + 1));
        if (offsetStr == null) {
            return oldChord; // cancel·lat: l'acord es queda tal com estava
        }
        String offsetTxt = offsetStr.trim();
        if (offsetTxt.isEmpty()) {
            chord.setBeatColOffset(defOffset); // sense resposta: manté la columna
        } else {
            int offset;
            try {
                offset = Integer.parseInt(offsetTxt) - 1;
            } catch (NumberFormatException ex) {
                return oldChord; // no és un número: com cancel·lar
            }
            if (offset < 0 || offset >= nColsBeat) {
                return oldChord; // fora de rang: com cancel·lar
            }
            chord.setBeatColOffset(offset);
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
        int camX1 = offscreen
                ? (int) Math.floor(col * Settings.getColWidth())
                : (int) Math.floor(score.getScreenX(col));
        int camY1 = offscreen ? 0 : (int) Math.round(score.getScreenY(-nRows));
        int camY2 = camY1 + (int) (nRows * Settings.getRowHeight());
        DrawKit.barraDeCompas(g, camX1, camY1 + Settings.getMarksLaneHeight() + 1, camY2);
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
        int camX1 = offscreen
                ? (int) Math.floor(col * Settings.getColWidth())
                : (int) Math.floor(score.getScreenX(col));
        int camY1 = offscreen ? 0 : (int) Math.round(score.getScreenY(-nRows));
        int camY2 = camY1 + (int) (nRows * Settings.getRowHeight());
        DrawKit.barraDeTemps(g, camX1, camY1 + Settings.getMarksLaneHeight() + 1, camY2);
    }

    /**
     * [CA] Dibuixa el símbol d'un acord a la columna donada, més el trianglet
     * d'atac. Un acord de text lliure, invàlid o sense intervals només en dóna el
     * text. Els formats que es llegeixen en vertical (tots menys Simbol i Nom
     * genèric) es reparteixen en <b>tres columnes</b>: arrel i baix, la quatríada
     * i les extensions; les dues apilades van alineades a la <b>dreta</b>, perquè
     * els intervals de dues xifres no desquadrin la columna. La mida de la font
     * surt de {@link #getBaseChordFont} —una fila de la franja per línia, la
     * mateixa mida que les marques de canvi— i de
     * {@link #computeChordFontOverride}, que la baixa si els acords no hi
     * cabrien d'ample.
     * <p>
     * [EN] Draws the chord symbol at the given column, plus the attack triangle.
     * A free-text, invalid or interval-less chord draws its text only. The
     * vertical formats (all but Simbol and Nom genèric) are laid out in
     * <b>three columns</b> —root and bass, the four-note body, the tensions—
     * with the two stacked ones <b>right-aligned</b>. Font size comes from
     * {@link #getChordFont} and {@link #computeChordFontOverride}.
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
        Font prevFont = g.getFont();
        DrawKit.comencaAcord(g, getChordFont(g));
        FontMetrics fm = g.getFontMetrics();

        int camX = 3 + xOffset + (offscreen
                ? (int) Math.floor(col * Settings.getColWidth())
                : (int) score.getScreenX(col));

        // Bottom of chord strip — text baseline drawn upward from here.
        // TRIANGLE_SPACE pixels are reserved at the very bottom for the attack triangle.
        int chordH = (int) Math.round(Settings.getnRowsChord() * Settings.getRowHeight());
        int camY = chordBaselineY(offscreen, chordH, fm);
        int gap   = 3;

        // Text lliure, acord invàlid o sense intervals: només se'n pot dibuixar el
        // text. Sense la comprovació d'isValidChord(), un acord que no s'hagués
        // pogut parsejar arribava a basicString() amb shape a null i s'enduia la
        // finestra sencera amb un NullPointerException des del repintat.
        if (chord.getRoot() == Settings.USE_INFO_AS_SIMBOL
                || !chord.isValidChord() || chord.getShape() == null) {
            DrawKit.textAcord(g, chord.getInfo(), camX, camY);
            return;
        }

        String fmt = getDisplayFormat();
        List<String> lines = ChordSymbols.chordToFormatLines(chord, fmt);

        if (lines != null && !lines.isEmpty()) {
            // Split index: base notes end here, tensions start
            int splitIdx = ChordSymbols.baseLinesCount(chord); // tensions start at this index
            int col2Count = Math.min(splitIdx - 1, 4); // base notes in col 2, max 4

            // Column 1: root (lines[0]) at bottom
            String root  = lines.get(0);
            int    col1W = fm.stringWidth(root);
            DrawKit.textAcord(g, root, camX, camY);

            // Column 2: base notes from bottom to top, right-aligned. Els intervals
            // de dues xifres (el 10 d'un [0,4,7,10]) desalineaven la columna; cal
            // mesurar-la sencera abans de dibuixar-ne cap línia. L'amplada té un
            // terra de dues xifres (stackColWidth), perquè els acords que no en
            // porten cap de dos dígits alineïn igualment per la dreta.
            int col2X = camX + col1W + gap;
            int col2W = stackColWidth(fm, lines, 1, col2Count);
            int y = camY;
            for (int i = 1; i <= col2Count; i++) {
                String n = lines.get(i);
                DrawKit.textAcord(g, n, col2X + col2W - fm.stringWidth(n), y);
                y -= lineStep(fm);
            }

            // Column 3: tensions always here, from bottom to top, right-aligned too
            if (lines.size() > splitIdx) {
                int col3X = col2X + col2W + gap;
                int col3W = stackColWidth(fm, lines, splitIdx, lines.size() - 1);
                y = camY;
                for (int i = splitIdx; i < lines.size(); i++) {
                    String t = lines.get(i);
                    DrawKit.textAcord(g, t, col3X + col3W - fm.stringWidth(t), y);
                    y -= lineStep(fm);
                }
            }
        } else {
            // Inline: FORMAT_SIMBOL, FORMAT_NOM
            String displayStr = ChordSymbols.chordToFormat(chord, fmt);
            DrawKit.textAcord(g, displayStr != null ? displayStr : chord.basicString(), camX, camY);
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
            DrawKit.triangletDAtac(g, ax, ayTip);
        }

        DrawKit.restauraFont(g, prevFont);
    }

    // -----------------------------------------------------------------------
    //  Change markers (tempo / key)
    // -----------------------------------------------------------------------

    /**
     * [CA] Figura del beat vigent a la columna {@code col}: la que posa l'últim
     * canvi de compàs del mapa de canvis fins aquella columna, o la de la
     * partitura si no n'hi ha cap. 4 = el beat és la negra (compàs simple);
     * 8 = el beat és el grup de tres corxeres (compàs compost).
     * <p>
     * [EN] Beat figure in force at column {@code col}.
     */
    private int beatFigureAt(int col) {
        int fig = score.getBaseBeatFigure();
        for (Map.Entry<Integer, MyGridScore.ScoreChange> e : score.getChangeMap().headMap(col, true).entrySet()) {
            if (e.getValue().beatFigure != null) fig = e.getValue().beatFigure;
        }
        return fig;
    }

    /**
     * [CA] Text de la marca de tempo <b>sense la figura</b>: {@code " = 123"}. La figura
     * (negra en compàs simple, negra amb punt en compost) la dibuixa
     * {@link DrawKit#simbolDeBeat} com a figura vectorial, perquè la tipografia
     * dels acords no té els glifs ♩ ni ♪.
     * [EN] Tempo mark text without the note figure, which is drawn as a shape.
     */
    private static String tempoText(int bpm) {
        return " = " + bpm;
    }

    /**
     * [CA] Text de la marca de tonalitat: {@code Do} si és major, {@code do} si és menor.
     * [EN] Key mark text: {@code Do} for major, {@code do} for minor.
     */
    private static String keyText(int midiKey, char mode) {
        try {
            return ToneRange.getKeyName(midiKey, mode);
        } catch (Exception ex) {
            return "?";
        }
    }

    /**
     * [CA] Matís (dinàmica) que correspon a una velocitat MIDI: el més proper de
     * {@code pp, p, mp, mf, f, ff} (velocitats de referència 33, 49, 64, 80, 96 i
     * 112). El valor exacte no surt a la franja; es veu i s'edita amb el botó de
     * volum.
     * <p>
     * [EN] The dynamic marking nearest to a MIDI velocity.
     */
    public static String dynamicText(int velocity) {
        if (velocity < 41) return "pp";
        if (velocity < 57) return "p";
        if (velocity < 72) return "mp";
        if (velocity < 88) return "mf";
        if (velocity < 104) return "f";
        return "ff";
    }

    /**
     * [CA] Dibuixa les marques d'una columna al carril de dalt de la franja: una
     * <b>fletxeta mirant avall</b> a la columna on va la marca i, al costat, el text
     * de totes les marques <b>separades per punt i coma</b> (tonalitat, tempo, volum;
     * les {@code null} se salten), cada una del color del seu tipus. En torna
     * l'amplada total. No s'ajusta a la columna següent: si hi ha marques molt
     * juntes poden quedar una sobre de l'altra.
     * <p>
     * [EN] Draws a column's marks in the strip's top lane: a down-pointing arrow
     * at the column and, beside it, the text of all marks <b>separated by
     * semicolons</b>, each in its kind's colour. Returns the total width.
     */
    private int drawMarkGroup(int col, Integer tempo, Integer midiKey, char mode, Integer velocity,
                              Graphics2D g, boolean offscreen) {
        List<String> texts = new java.util.ArrayList<>();
        List<MarkKind> kinds = new java.util.ArrayList<>();
        // Símbol de beat que precedeix el text: 0 = cap, 1 = negra, 2 = negra amb punt.
        List<Integer> symbols = new java.util.ArrayList<>();
        // Ordre: tonalitat, tempo, volum.
        if (midiKey != null) {
            texts.add(keyText(midiKey, mode));
            kinds.add(MarkKind.KEY);
            symbols.add(0);
        }
        if (tempo != null) {
            texts.add(tempoText(tempo));
            kinds.add(MarkKind.TEMPO);
            symbols.add(beatFigureAt(col) == 8 ? 2 : 1);
        }
        if (velocity != null) {
            texts.add(dynamicText(velocity));
            kinds.add(MarkKind.VOLUME);
            symbols.add(0);
        }
        if (texts.isEmpty()) return 0;

        Font prevFont = g.getFont();
        // Mateixa lletra, mida i color (negre) que els acords.
        DrawKit.comencaAcord(g, getMarkFont());
        FontMetrics fm = g.getFontMetrics();
        int boxH = markBoxHeight();
        int laneH = Settings.getMarksLaneHeight();
        int cellX   = offscreen
                ? (int) Math.floor(col * Settings.getColWidth())
                : (int) Math.floor(score.getScreenX(col));
        int cellTop = offscreen ? 0 : (int) Math.round(score.getScreenY(-nRows));
        int boxY    = cellTop + (laneH - boxH) / 2;
        int baseline = boxY + (boxH - (fm.getAscent() + fm.getDescent())) / 2 + fm.getAscent();

        // La fletxeta és el trianglet d'atac dels acords (mateixa mida), amb la punta
        // al centre de la columna, centrada en vertical dins del carril.
        int cw = (int) Settings.getColWidth();
        int arrowX = cellX + cw / 2;
        int arrowTip = cellTop + laneH / 2 + DrawKit.alcadaDeTrianglet() / 2;
        DrawKit.triangletDAtac(g, arrowX, arrowTip);
        int x = cw / 2 + DrawKit.mitjaAmpladaDeTrianglet() + MARK_GAP + 1;

        // Només el buffer de pantalla registra rectangles i pinta la selecció:
        // l'exportació a PDF reutilitza aquest mètode amb un altre Graphics2D.
        boolean toOwnBuffer = offscreen && g == offscreenGraphics;
        double colW = Settings.getColWidth();
        for (int i = 0; i < texts.size(); i++) {
            if (i > 0) {
                DrawKit.textAcord(g, "; ", cellX + x, baseline);
                x += fm.stringWidth("; ");
            }
            String t = texts.get(i);
            MarkKind kind = kinds.get(i);
            int sym = symbols.get(i);
            int segX = x;
            int symW = 0;
            if (sym > 0) {
                symW = DrawKit.simbolDeBeat(g, cellX + x, baseline, fm.getAscent(), sym == 2);
            }
            DrawKit.textAcord(g, t, cellX + x + symW, baseline);
            int w = symW + fm.stringWidth(t);
            if (toOwnBuffer) {
                int firstCol = col + (int) Math.floor(segX / colW);
                int lastCol  = col + (int) Math.floor((segX + Math.max(1, w) - 1) / colW);
                markBoxes.add(new MarkBox(col, kind, firstCol, lastCol, segX, w, boxH));
                // Marca seleccionada: marc groc gruixut PER FORA, amb filet negre.
                if (col == contr.getSelectedMarkCol() && kind == contr.getSelectedMarkKind()) {
                    DrawKit.marcaSeleccionada(g, cellX + segX, boxY, w, boxH);
                }
            }
            x += w;
        }
        DrawKit.restauraFont(g, prevFont);
        return x;
    }

    /**
     * [CA] Buffer offscreen de la franja. És el que copia el PDF: el printer no
     * torna a dibuixar els acords, agafa aquesta imatge.
     * <p>
     * [EN] The strip's offscreen buffer. This is what the PDF copies: the printer
     * does not redraw the chords, it takes this image.
     *
     * @return [CA] imatge offscreen, o {@code null} si encara no s'ha creat /
     *         [EN] offscreen image, or {@code null} if not created yet
     */
    public BufferedImage getOffscreenImage() {
        return offscreenImage;
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
     * [CA] Crea el BufferedImage offscreen de la franja d'acords i hi dibuixa tot
     * el contingut. S'ha de cridar sempre que canviï la disposició de la
     * partitura o les dades dels acords (calca {@code MyGridScore.initOffscreen()}).
     * <p>
     * [EN] Creates the chord strip's offscreen BufferedImage and draws the full
     * content into it. Call it whenever the score layout or the chord data
     * changes (mirrors {@code MyGridScore.initOffscreen()}).
     */
    public void initOffscreen() {
        if (offscreenGraphics != null) {
            offscreenGraphics.dispose();
        }
        int w = (int) (score.getNColsBuffer() * Settings.getColWidth());
        int h = (int) (nRows * Settings.getRowHeight());
        offscreenImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        offscreenGraphics = offscreenImage.createGraphics();
        DrawKit.preparaOffscreen(offscreenGraphics);
        Utilities.printOutWithPriority(false, "MyChordSymbolLine::initOffscreen: w=" + w + ", h=" + h);
        needsDrawing = true;
        drawFullChordLineInOffscreen();
    }

    /**
     * [CA] Amplia l'offscreen a {@code newNCols} columnes conservant el que ja hi
     * havia dibuixat. Si encara no existeix, el crea amb {@link #initOffscreen()}.
     * <p>
     * [EN] Grows the offscreen to {@code newNCols} columns, keeping what is
     * already drawn. Creates it with {@link #initOffscreen()} if absent.
     *
     * @param newNCols [CA] columnes que ha de tenir el buffer / [EN] columns the buffer must hold
     */
    public void resizeOffscreen(int newNCols) {
        if (offscreenImage == null) { initOffscreen(); return; }
        int newW = (int) (newNCols * Settings.getColWidth());
        int oldW = offscreenImage.getWidth();
        if (newW <= oldW) return;
        int h = offscreenImage.getHeight();
        BufferedImage newImg = new BufferedImage(newW, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D newG = newImg.createGraphics();
        DrawKit.ampliaOffscreen(newG, offscreenImage, oldW, newW, h);
        offscreenGraphics.dispose();
        offscreenImage = newImg;
        offscreenGraphics = newG;
    }

    /**
     * [CA] Cert mentre la franja s'ha de dibuixar sense la pila de marques de la
     * columna 0 (transposició, tempo, tonalitat i volum). Només l'aixeca la
     * impressió d'un dodecagrama en blanc, entre el redibuix del buffer i el final
     * del PDF; el PDF copia aquest mateix offscreen, o sigui que treure-les d'aquí
     * les treu de la pàgina. Fora d'aquell tram val sempre fals: les marques
     * inicials són la base de la partitura i a la pantalla no s'amaguen mai.
     * <p>
     * [EN] True while the band must be drawn without the column 0 mark stack
     * (transpose, tempo, key and volume). Only the blank dodecagram printout
     * raises it, between the buffer redraw and the end of the PDF; the PDF copies
     * this very offscreen, so dropping them here drops them from the page. Outside
     * that window it is always false: initial marks are the score's baseline and
     * are never hidden on screen.
     */
    private boolean hideInitialMarks = false;

    /**
     * [CA] Cert mentre s'imprimeix (PDF). <b>Ara no té cap efecte</b>: les marques de
     * la columna 0 es dibuixen sempre al carril de dalt (vegeu {@link #drawMarkGroup}),
     * a la pantalla i al PDF. Abans només es dibuixaven en imprimir; es conserva
     * per si es torna a voler amagar-les a la pantalla. L'aixeca
     * {@code DodecagramPdfPrinter.print}, que la torna a baixar al {@code finally}.
     * <p>
     * [EN] True while printing (PDF). Column 0 marks (initial tempo, key and
     * volume) are <b>drawn only when printing</b>: on screen the buttons above
     * already show them.
     */
    private boolean printingMarks = false;

    /**
     * [CA] Activa o desactiva el mode d'impressió de la franja (vegeu
     * {@link #printingMarks}). Qui l'activa l'ha de desactivar en un {@code finally}
     * i forçar un redibuix, o la pantalla es quedaria amb les marques inicials.
     * <p>
     * [EN] Turns the strip's print mode on or off.
     *
     * @param printing [CA] cert per dibuixar-hi les marques inicials / [EN] true to draw the initial marks
     */
    public void setPrintingMarks(boolean printing) {
        this.printingMarks = printing;
    }

    /**
     * [CA] Amaga o mostra la pila de marques de la columna 0. Qui l'aixequi ha de
     * tornar-la a baixar (en un {@code finally}) i forçar un redibuix, o la franja
     * es quedaria sense marques a la pantalla.
     * <p>
     * [EN] Hides or shows the column 0 mark stack. Whoever raises it must lower it
     * again (in a {@code finally}) and force a redraw, or the band would stay
     * without marks on screen.
     *
     * @param hide [CA] cert per amagar-les / [EN] true to hide them
     */
    public void setHideInitialMarks(boolean hide) {
        this.hideInitialMarks = hide;
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
            DrawKit.fons(offscreenGraphics, 0, 0, offscreenImage.getWidth(), offscreenImage.getHeight());
            // La franja de marques (carril de dalt), amb el seu fons si el tema en té.
            int laneH = Settings.getMarksLaneHeight();
            DrawKit.franjaDeMarques(offscreenGraphics, 0, 0, offscreenImage.getWidth(), laneH);
            // Línia horitzontal de banda a banda entre el carril de marques i els acords.
            DrawKit.liniaDeMarques(offscreenGraphics, 0, laneH, offscreenImage.getWidth());

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

            // Col 0: les marques inicials només es dibuixen en imprimir (a la pantalla
            // ja les mostren els botons de dalt), amb fallback als defaults si el
            // changeMap no té entrada, i no en un dodecagrama en blanc sense marques.
            if (!hideInitialMarks) {
                MyGridScore.ScoreChange sc0 = changeMap.get(0);
                int tempo0   = (sc0 != null && sc0.tempo    != null) ? sc0.tempo    : Settings.DEFAULT_TEMPO;
                int midiKey0 = (sc0 != null && sc0.midiKey  != null) ? sc0.midiKey  : ToneRange.getDefaultKey();
                char mode0   = (sc0 != null && sc0.scaleMode != null) ? sc0.scaleMode : ToneRange.getDefaultMode();
                // Volum: es mostra sempre, com el tempo i la tonalitat. Sense marca
                // explícita el valor és el del track actual, que arrenca a
                // Settings.getDefaultVelocity(); així la marca no menteix mai.
                Integer vel0 = (sc0 != null) ? sc0.trackVelocities.get(currentTrackId) : null;
                int vel0Shown = (vel0 != null) ? vel0 : currentTrackVelocity();
                drawMarkGroup(0, tempo0, midiKey0, mode0, vel0Shown, offscreenGraphics, true);
            }

            // Col > 0: marques del changeMap, al carril de dalt, una al costat de l'altra.
            for (Map.Entry<Integer, MyGridScore.ScoreChange> entry : changeMap.entrySet()) {
                int col = entry.getKey();
                if (col <= 0 || col >= numCols) continue;
                MyGridScore.ScoreChange sc = entry.getValue();
                char mode = (sc.scaleMode != null) ? sc.scaleMode : 'M';
                // Volum: només el del track actual.
                Integer velCur = sc.trackVelocities.get(currentTrackId);
                drawMarkGroup(col, sc.tempo, sc.midiKey, mode, velCur, offscreenGraphics, true);
            }

            // Acords: ja no cal desplaçar-los, les marques tenen carril propi.
            computeChordFontOverride(offscreenGraphics, chords, numCols, markerMaxWidths);
            for (int col = 0; col < numCols; col++) {
                Chord chord = chords.get(col);
                if (chord != null) {
                    drawChordSymbol(chord, col, offscreenGraphics, true, 0);
                }
            }

            needsDrawing = false;
        }
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
        DrawKit.voraDeComponent(g, (int) screenPosX, (int) screenPosY, (int) width, (int) height);

        if (offscreenImage != null) {
            if (needsDrawing) drawFullChordLineInOffscreen();
            // ---- Offscreen path: la finestra la decideix la càmera ----
            // (MyCamera.ViewWindow): els acords llisquen amb la graella i la
            // lletra, a velocitat constant, i amb escala 1 la còpia és 1:1.
            MyCamera.ViewWindow view = contr.getCam().getViewWindow();
            int y1 = (int) Math.round(screenPosY);
            int h  = (int) Math.ceil(height);

            view.blit(g, offscreenImage, y1, h);

            // Línies de beat i compàs en screen-space (no a l'offscreen) per evitar
            // que desapareguin en escalar amb fit-anacrusis.
            {
                int numCols = score.getNumCols();
                boolean[] isBeat    = new boolean[numCols + 1];
                boolean[] isMeasure = new boolean[numCols + 1];
                score.computeBeatMeasureLines(numCols + 1, isBeat, isMeasure);
                // Comencen just sota la línia del carril de marques, sense travessar-lo.
                view.drawBeatAndMeasureLines(g, isBeat, isMeasure,
                        y1 + Settings.getMarksLaneHeight() + 1, y1 + h, y1, h);
            }

            // Doble barra al stopCol — dibuixada sobre la vista (sempre actualitzada)
            int stopC = contr.getAllPurposeScore().getStopCol();
            if (stopC > 0 && contr.getAllPurposeScore().isStopMarkerValid()) {
                int sy1 = (int) Math.round(score.getScreenY(-nRows));
                int sy2 = sy1 + (int) Math.round(nRows * Settings.getRowHeight());
                view.drawStopBar(g, stopC, sy1, sy2, y1, h);
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
