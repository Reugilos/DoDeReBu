package dodecagraphone.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;

/**
 * Capa de dibuix semàntica sobre {@link Graphics2D}.
 *
 * <p>Encapsula les crides de dibuix en mètodes de noms significatius
 * (orientats al domini: graella, botons, partitura) i exposa camps públics
 * per poder ajustar colors, gruixos i fonts sense tocar el codi de dibuix.</p>
 *
 * <p>Ús típic:</p>
 * <pre>
 *   DrawKit dk = new DrawKit(g);
 *   dk.enableAntialiasing();
 *   dk.clearBackground(0, 0, w, h);
 *   dk.drawMeasureLine(x, y1, y2);
 *   dk.drawNoteLabel("Do", x, y, w, h, Color.WHITE);
 * </pre>
 */
public class DrawKit {

    private final Graphics2D g;

    // ── Gruixos de línies ────────────────────────────────────────────────────

    /** Gruix de la barra de compàs. */
    public float measureLineWidth   = 1.5f;
    /** Gruix de la línia de temps (beat). */
    public float beatLineWidth      = 1.0f;
    /** Gruix de la línia de subbeat. */
    public float subBeatLineWidth   = 0.5f;
    /** Gruix del separador de files de la graella. */
    public float rowDividerWidth    = 0.2f;
    /** Gruix de la barra de final (stop). */
    public float stopBarWidth       = 3.0f;
    /** Gruix del cursor de playbar. */
    public float playBarWidth       = 2.0f;
    /** Gruix de la segona línia de la doble barra. */
    public float doubleBarThinWidth = 1.5f;
    /** Separació entre les dues línies de la doble barra. */
    public float doubleBarGap       = 3.0f;

    // ── Patrons de guions ────────────────────────────────────────────────────

    /** Guions de la línia de beat. */
    public float[] beatDash    = {15f, 3f};
    /** Guions de la línia de subbeat. */
    public float[] subBeatDash = { 5f, 3f};

    // ── Colors ───────────────────────────────────────────────────────────────

    /** Color de les barres de compàs i dels elements principals de la graella. */
    public Color measureLineColor     = Color.BLACK;
    /** Color de les línies de beat. */
    public Color beatLineColor        = Color.GRAY;
    /** Color de les línies de subbeat i separadors fins. */
    public Color subBeatLineColor     = Color.LIGHT_GRAY;
    /** Color del cursor de playbar. */
    public Color playBarColor         = Color.BLUE;
    /** Color de fons de la zona de selecció (semitransparent). */
    public Color selectionFillColor   = new Color(0, 100, 255);
    /** Opacitat del fons de la zona de selecció (0–1). */
    public float selectionFillAlpha   = 0.3f;
    /** Color de la vora de la zona de selecció (semitransparent). */
    public Color selectionBorderColor = new Color(0, 50, 200);
    /** Opacitat de la vora de la zona de selecció (0–1). */
    public float selectionBorderAlpha = 0.8f;
    /** Color de fons general (per esborrar zones). */
    public Color backgroundColor      = Color.WHITE;
    /** Color per defecte del text i de les formes simples. */
    public Color defaultTextColor     = Color.BLACK;

    // ── Fonts ────────────────────────────────────────────────────────────────

    /** Font per als noms de nota dins de les cel·les de la graella. */
    public Font noteFont  = new Font("SansSerif", Font.PLAIN, 10);
    /** Font per als símbols d'acord. */
    public Font chordFont = new Font("SansSerif", Font.BOLD,  12);
    /** Font per a les marques de tempo, tonalitat i compàs. */
    public Font markFont  = new Font("SansSerif", Font.BOLD,  11);
    /** Font per a les etiquetes de botó. */
    public Font labelFont = new Font("SansSerif", Font.PLAIN, 11);

    // ────────────────────────────────────────────────────────────────────────

    public DrawKit(Graphics2D g) {
        this.g = g;
    }

    // ── Configuració de renderitzat ──────────────────────────────────────────

    /** Activa l'antialiàsing de formes i de text. */
    public void enableAntialiasing() {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // ── Retall (clipping) ────────────────────────────────────────────────────

    /** Desa i retorna la màscara de retall actual per restaurar-la després. */
    public Shape saveClip() {
        return g.getClip();
    }

    /** Restaura una màscara de retall desada prèviament amb {@link #saveClip()}. */
    public void restoreClip(Shape savedClip) {
        g.setClip(savedClip);
    }

    /**
     * Aplica un rectangle de retall addicional (intersecció amb el retall existent).
     */
    public void setClipRect(int x, int y, int w, int h) {
        g.clipRect(x, y, w, h);
    }

    // ── Fons ─────────────────────────────────────────────────────────────────

    /**
     * Esborra una zona pintant-la amb {@link #backgroundColor}.
     * S'utilitza per reinicialitzar un buffer offscreen abans de redibuixar.
     */
    public void clearBackground(int x, int y, int w, int h) {
        g.setColor(backgroundColor);
        g.fillRect(x, y, w, h);
    }

    // ── Cel·les i rectangles de color ────────────────────────────────────────

    /** Omple una cel·la de la graella amb el color indicat. */
    public void fillCell(int x, int y, int w, int h, Color color) {
        g.setColor(color);
        g.fillRect(x, y, w, h);
    }

    /** Dibuixa el contorn d'una cel·la de la graella. */
    public void drawCellBorder(int x, int y, int w, int h, Color color) {
        g.setColor(color);
        g.drawRect(x, y, w, h);
    }

    /** Omple el fons rectangular d'un botó. */
    public void fillButton(int x, int y, int w, int h, Color color) {
        g.setColor(color);
        g.fillRect(x, y, w, h);
    }

    /** Omple el fons d'un botó amb cantonades arrodonides. */
    public void fillRoundedButton(int x, int y, int w, int h, int arc, Color color) {
        g.setColor(color);
        g.fillRoundRect(x, y, w, h, arc, arc);
    }

    // ── Línies de la graella ─────────────────────────────────────────────────

    /**
     * Dibuixa la barra de compàs vertical (gruixuda, {@link #measureLineWidth}).
     */
    public void drawMeasureLine(int x, int y1, int y2) {
        Stroke saved = g.getStroke();
        g.setStroke(new BasicStroke(measureLineWidth));
        g.setColor(measureLineColor);
        g.drawLine(x, y1, x, y2);
        g.setStroke(saved);
    }

    /**
     * Dibuixa la línia vertical de temps (beat) amb el patró de guions
     * {@link #beatDash}.
     */
    public void drawBeatLine(int x, int y1, int y2) {
        Stroke saved = g.getStroke();
        g.setStroke(new BasicStroke(beatLineWidth, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL, 0, beatDash, 0));
        g.setColor(beatLineColor);
        g.drawLine(x, y1, x, y2);
        g.setStroke(saved);
    }

    /**
     * Dibuixa la línia vertical de subbeat (guions fins, {@link #subBeatDash}).
     */
    public void drawSubBeatLine(int x, int y1, int y2) {
        Stroke saved = g.getStroke();
        g.setStroke(new BasicStroke(subBeatLineWidth, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL, 0, subBeatDash, 0));
        g.setColor(subBeatLineColor);
        g.drawLine(x, y1, x, y2);
        g.setStroke(saved);
    }

    /**
     * Dibuixa un separador horitzontal entre files de la graella
     * (línia molt fina, {@link #rowDividerWidth}).
     */
    public void drawRowDivider(int x1, int x2, int y) {
        Stroke saved = g.getStroke();
        g.setStroke(new BasicStroke(rowDividerWidth));
        g.setColor(subBeatLineColor);
        g.drawLine(x1, y, x2, y);
        g.setStroke(saved);
    }

    /**
     * Dibuixa el cursor de playbar (línia vertical blava, {@link #playBarWidth}).
     */
    public void drawPlayBarCursor(int x, int y1, int y2) {
        Stroke saved = g.getStroke();
        g.setStroke(new BasicStroke(playBarWidth));
        g.setColor(playBarColor);
        g.drawLine(x, y1, x, y2);
        g.setStroke(saved);
    }

    /**
     * Dibuixa la barra de final de partitura (stop bar, gruixuda,
     * {@link #stopBarWidth}).
     */
    public void drawStopBar(int x, int y1, int y2) {
        Stroke saved = g.getStroke();
        g.setStroke(new BasicStroke(stopBarWidth));
        g.setColor(measureLineColor);
        g.drawLine(x, y1, x, y2);
        g.setStroke(saved);
    }

    /**
     * Dibuixa una doble barra vertical: primera línia gruixuda i
     * una segona prima separada {@link #doubleBarGap} píxels.
     */
    public void drawDoubleBar(int x, int y1, int y2) {
        Stroke saved = g.getStroke();
        g.setColor(measureLineColor);
        g.setStroke(new BasicStroke(stopBarWidth));
        g.drawLine(x, y1, x, y2);
        g.setStroke(new BasicStroke(doubleBarThinWidth));
        g.drawLine(x + (int) doubleBarGap, y1, x + (int) doubleBarGap, y2);
        g.setStroke(saved);
    }

    /** Dibuixa una línia horitzontal amb gruix i color especificats. */
    public void drawHorizontalLine(int x1, int x2, int y, float width, Color color) {
        Stroke saved = g.getStroke();
        g.setStroke(new BasicStroke(width));
        g.setColor(color);
        g.drawLine(x1, y, x2, y);
        g.setStroke(saved);
    }

    /** Dibuixa una línia vertical amb gruix i color especificats. */
    public void drawVerticalLine(int x, int y1, int y2, float width, Color color) {
        Stroke saved = g.getStroke();
        g.setStroke(new BasicStroke(width));
        g.setColor(color);
        g.drawLine(x, y1, x, y2);
        g.setStroke(saved);
    }

    // ── Text ─────────────────────────────────────────────────────────────────

    /**
     * Dibuixa el nom d'una nota centrat horitzontalment i verticalment
     * dins d'una cel·la de la graella.
     */
    public void drawNoteLabel(String text, int x, int y, int w, int h, Color color) {
        g.setFont(noteFont);
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        int tx = x + (w - fm.stringWidth(text)) / 2;
        int ty = y + (h + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(text, tx, ty);
    }

    /**
     * Dibuixa un símbol d'acord en la posició indicada,
     * usant {@link #chordFont}.
     */
    public void drawChordSymbol(String text, int x, int y, Color color) {
        g.setFont(chordFont);
        g.setColor(color);
        g.drawString(text, x, y);
    }

    /**
     * Dibuixa una marca de partitura (tempo, tonalitat, compàs) amb fons de color.
     * El fons es pinta a (bgX, bgY, bgW, bgH); el text a (x, y).
     */
    public void drawScoreMark(String text, int x, int y,
                               Color textColor, Color bgColor,
                               int bgX, int bgY, int bgW, int bgH) {
        g.setColor(bgColor);
        g.fillRect(bgX, bgY, bgW, bgH);
        g.setFont(markFont);
        g.setColor(textColor);
        g.drawString(text, x, y);
    }

    /**
     * Dibuixa l'etiqueta d'un botó centrada dins del rectangle del botó,
     * usant {@link #labelFont}.
     */
    public void drawButtonLabel(String text, int x, int y, int w, int h, Color color) {
        g.setFont(labelFont);
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        float tx = x + (w - fm.stringWidth(text)) / 2.0f;
        float ty = y + (h + fm.getAscent() - fm.getDescent()) / 2.0f;
        g.drawString(text, tx, ty);
    }

    /**
     * Dibuixa un text en la posició absoluta indicada,
     * amb font i color arbitraris.
     */
    public void drawText(String text, int x, int y, Font font, Color color) {
        g.setFont(font);
        g.setColor(color);
        g.drawString(text, x, y);
    }

    // ── Selecció ─────────────────────────────────────────────────────────────

    /**
     * Omple la zona de selecció activa amb color semitransparent
     * ({@link #selectionFillColor}, {@link #selectionFillAlpha}).
     */
    public void drawSelectionFill(int x, int y, int w, int h) {
        Composite saved = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, selectionFillAlpha));
        g.setColor(selectionFillColor);
        g.fillRect(x, y, w, h);
        g.setComposite(saved);
    }

    /**
     * Dibuixa la vora de la zona de selecció amb color semitransparent
     * ({@link #selectionBorderColor}, {@link #selectionBorderAlpha}).
     */
    public void drawSelectionBorder(int x, int y, int w, int h) {
        Composite saved = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, selectionBorderAlpha));
        g.setColor(selectionBorderColor);
        g.drawRect(x, y, w, h);
        g.setComposite(saved);
    }

    // ── Formes ───────────────────────────────────────────────────────────────

    /**
     * Dibuixa una fletxa cap avall (triangle ple) amb vèrtex superior centrat
     * en (cx, cy) i mides (w × h).
     */
    public void drawDownArrow(int cx, int cy, int w, int h) {
        int[] xs = {cx - w / 2, cx + w / 2, cx};
        int[] ys = {cy,          cy,          cy + h};
        Color saved = g.getColor();
        g.setColor(defaultTextColor);
        g.fillPolygon(xs, ys, 3);
        g.setColor(saved);
    }

    // ── Imatge offscreen ─────────────────────────────────────────────────────

    /**
     * Copia una porció d'una imatge offscreen a la pantalla (blit).
     * Equivalent a {@code g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null)}.
     */
    public void blitOffscreen(Image img,
                               int dx1, int dy1, int dx2, int dy2,
                               int sx1, int sy1, int sx2, int sy2) {
        g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
    }

    // ── Accés directe ────────────────────────────────────────────────────────

    /**
     * Retorna el {@link Graphics2D} subjacent per a operacions
     * no cobertes per {@code DrawKit}.
     */
    public Graphics2D getGraphics() {
        return g;
    }
}
