/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
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
 * [CA] Capa de dibuix semàntica sobre {@link Graphics2D}.
 * <p>
 * Encapsula les crides de dibuix en mètodes amb noms significatius
 * (orientats al domini: graella, botons, partitura) i exposa camps públics
 * per ajustar colors, gruixos i fonts sense modificar el codi de dibuix.
 * <p>
 * Ús típic:
 * <pre>
 *   DrawKit dk = new DrawKit(g);
 *   dk.enableAntialiasing();
 *   dk.clearBackground(0, 0, w, h);
 *   dk.drawMeasureLine(x, y1, y2);
 *   dk.drawNoteLabel("Do", x, y, w, h, Color.WHITE);
 * </pre>
 * <p>
 * [EN] Semantic drawing layer over {@link Graphics2D}.
 * <p>
 * Wraps drawing calls into domain-oriented methods (grid, buttons, score)
 * and exposes public fields for adjusting colours, widths and fonts without
 * touching the drawing code.
 * <p>
 * Typical usage:
 * <pre>
 *   DrawKit dk = new DrawKit(g);
 *   dk.enableAntialiasing();
 *   dk.clearBackground(0, 0, w, h);
 *   dk.drawMeasureLine(x, y1, y2);
 *   dk.drawNoteLabel("Do", x, y, w, h, Color.WHITE);
 * </pre>
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
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

    /**
     * [CA] Crea un DrawKit associat al context gràfic especificat.
     * <p>
     * [EN] Creates a DrawKit bound to the given graphics context.
     *
     * @param g [CA] Context Graphics2D subjacent / [EN] Underlying Graphics2D context
     */
    public DrawKit(Graphics2D g) {
        this.g = g;
    }

    // ── Configuració de renderitzat ──────────────────────────────────────────

    /**
     * [CA] Activa l'antialiàsing de formes i de text.
     * <p>
     * [EN] Enables antialiasing for shapes and text.
     */
    public void enableAntialiasing() {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // ── Retall (clipping) ────────────────────────────────────────────────────

    /**
     * [CA] Desa i retorna la màscara de retall actual per restaurar-la després.
     * <p>
     * [EN] Saves and returns the current clip mask so it can be restored later.
     *
     * @return [CA] La forma de retall actual / [EN] The current clip shape
     */
    public Shape saveClip() {
        return g.getClip();
    }

    /**
     * [CA] Restaura una màscara de retall desada prèviament amb {@link #saveClip()}.
     * <p>
     * [EN] Restores a clip mask previously saved with {@link #saveClip()}.
     *
     * @param savedClip [CA] La forma de retall a restaurar / [EN] The clip shape to restore
     */
    public void restoreClip(Shape savedClip) {
        g.setClip(savedClip);
    }

    /**
     * [CA] Aplica un rectangle de retall addicional (intersecció amb el retall existent).
     * <p>
     * [EN] Applies an additional clip rectangle (intersection with the existing clip).
     *
     * @param x [CA] Coordenada X del rectangle / [EN] X coordinate of the rectangle
     * @param y [CA] Coordenada Y del rectangle / [EN] Y coordinate of the rectangle
     * @param w [CA] Amplada del rectangle / [EN] Rectangle width
     * @param h [CA] Alçada del rectangle / [EN] Rectangle height
     */
    public void setClipRect(int x, int y, int w, int h) {
        g.clipRect(x, y, w, h);
    }

    // ── Fons ─────────────────────────────────────────────────────────────────

    /**
     * [CA] Esborra una zona pintant-la amb {@link #backgroundColor}.
     * S'utilitza per reinicialitzar un buffer offscreen abans de redibuixar.
     * <p>
     * [EN] Clears a zone by painting it with {@link #backgroundColor}.
     * Used to reset an offscreen buffer before redrawing.
     *
     * @param x [CA] Coordenada X / [EN] X coordinate
     * @param y [CA] Coordenada Y / [EN] Y coordinate
     * @param w [CA] Amplada / [EN] Width
     * @param h [CA] Alçada / [EN] Height
     */
    public void clearBackground(int x, int y, int w, int h) {
        g.setColor(backgroundColor);
        g.fillRect(x, y, w, h);
    }

    // ── Cel·les i rectangles de color ────────────────────────────────────────

    /**
     * [CA] Omple una cel·la de la graella amb el color indicat.
     * <p>
     * [EN] Fills a grid cell with the given colour.
     *
     * @param x     [CA] Coordenada X / [EN] X coordinate
     * @param y     [CA] Coordenada Y / [EN] Y coordinate
     * @param w     [CA] Amplada / [EN] Width
     * @param h     [CA] Alçada / [EN] Height
     * @param color [CA] Color de farciment / [EN] Fill colour
     */
    public void fillCell(int x, int y, int w, int h, Color color) {
        g.setColor(color);
        g.fillRect(x, y, w, h);
    }

    /**
     * [CA] Dibuixa el contorn d'una cel·la de la graella.
     * <p>
     * [EN] Draws the border of a grid cell.
     *
     * @param x     [CA] Coordenada X / [EN] X coordinate
     * @param y     [CA] Coordenada Y / [EN] Y coordinate
     * @param w     [CA] Amplada / [EN] Width
     * @param h     [CA] Alçada / [EN] Height
     * @param color [CA] Color del contorn / [EN] Border colour
     */
    public void drawCellBorder(int x, int y, int w, int h, Color color) {
        g.setColor(color);
        g.drawRect(x, y, w, h);
    }

    /**
     * [CA] Omple el fons rectangular d'un botó.
     * <p>
     * [EN] Fills the rectangular background of a button.
     *
     * @param x     [CA] Coordenada X / [EN] X coordinate
     * @param y     [CA] Coordenada Y / [EN] Y coordinate
     * @param w     [CA] Amplada / [EN] Width
     * @param h     [CA] Alçada / [EN] Height
     * @param color [CA] Color de farciment / [EN] Fill colour
     */
    public void fillButton(int x, int y, int w, int h, Color color) {
        g.setColor(color);
        g.fillRect(x, y, w, h);
    }

    /**
     * [CA] Omple el fons d'un botó amb cantonades arrodonides.
     * <p>
     * [EN] Fills the background of a button with rounded corners.
     *
     * @param x     [CA] Coordenada X / [EN] X coordinate
     * @param y     [CA] Coordenada Y / [EN] Y coordinate
     * @param w     [CA] Amplada / [EN] Width
     * @param h     [CA] Alçada / [EN] Height
     * @param arc   [CA] Radi d'arrodoniment / [EN] Corner arc radius
     * @param color [CA] Color de farciment / [EN] Fill colour
     */
    public void fillRoundedButton(int x, int y, int w, int h, int arc, Color color) {
        g.setColor(color);
        g.fillRoundRect(x, y, w, h, arc, arc);
    }

    // ── Línies de la graella ─────────────────────────────────────────────────

    /**
     * [CA] Dibuixa la barra de compàs vertical (gruixuda, {@link #measureLineWidth}).
     * <p>
     * [EN] Draws the vertical measure bar (thick, {@link #measureLineWidth}).
     *
     * @param x  [CA] Coordenada X de la línia / [EN] X coordinate of the line
     * @param y1 [CA] Y inicial / [EN] Start Y
     * @param y2 [CA] Y final / [EN] End Y
     */
    public void drawMeasureLine(int x, int y1, int y2) {
        Stroke saved = g.getStroke();
        g.setStroke(new BasicStroke(measureLineWidth));
        g.setColor(measureLineColor);
        g.drawLine(x, y1, x, y2);
        g.setStroke(saved);
    }

    /**
     * [CA] Dibuixa la línia vertical de temps (beat) amb el patró de guions
     * {@link #beatDash}.
     * <p>
     * [EN] Draws the vertical beat line with the dashed pattern {@link #beatDash}.
     *
     * @param x  [CA] Coordenada X / [EN] X coordinate
     * @param y1 [CA] Y inicial / [EN] Start Y
     * @param y2 [CA] Y final / [EN] End Y
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
     * [CA] Dibuixa la línia vertical de subbeat (guions fins, {@link #subBeatDash}).
     * <p>
     * [EN] Draws the vertical subbeat line (thin dashes, {@link #subBeatDash}).
     *
     * @param x  [CA] Coordenada X / [EN] X coordinate
     * @param y1 [CA] Y inicial / [EN] Start Y
     * @param y2 [CA] Y final / [EN] End Y
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
     * [CA] Dibuixa un separador horitzontal entre files de la graella
     * (línia molt fina, {@link #rowDividerWidth}).
     * <p>
     * [EN] Draws a horizontal divider between grid rows
     * (very thin line, {@link #rowDividerWidth}).
     *
     * @param x1 [CA] X inicial / [EN] Start X
     * @param x2 [CA] X final / [EN] End X
     * @param y  [CA] Coordenada Y / [EN] Y coordinate
     */
    public void drawRowDivider(int x1, int x2, int y) {
        Stroke saved = g.getStroke();
        g.setStroke(new BasicStroke(rowDividerWidth));
        g.setColor(subBeatLineColor);
        g.drawLine(x1, y, x2, y);
        g.setStroke(saved);
    }

    /**
     * [CA] Dibuixa el cursor de playbar (línia vertical blava, {@link #playBarWidth}).
     * <p>
     * [EN] Draws the playbar cursor (blue vertical line, {@link #playBarWidth}).
     *
     * @param x  [CA] Coordenada X / [EN] X coordinate
     * @param y1 [CA] Y inicial / [EN] Start Y
     * @param y2 [CA] Y final / [EN] End Y
     */
    public void drawPlayBarCursor(int x, int y1, int y2) {
        Stroke saved = g.getStroke();
        g.setStroke(new BasicStroke(playBarWidth));
        g.setColor(playBarColor);
        g.drawLine(x, y1, x, y2);
        g.setStroke(saved);
    }

    /**
     * [CA] Dibuixa la barra de final de partitura (stop bar, gruixuda,
     * {@link #stopBarWidth}).
     * <p>
     * [EN] Draws the end-of-score stop bar (thick, {@link #stopBarWidth}).
     *
     * @param x  [CA] Coordenada X / [EN] X coordinate
     * @param y1 [CA] Y inicial / [EN] Start Y
     * @param y2 [CA] Y final / [EN] End Y
     */
    public void drawStopBar(int x, int y1, int y2) {
        Stroke saved = g.getStroke();
        g.setStroke(new BasicStroke(stopBarWidth));
        g.setColor(measureLineColor);
        g.drawLine(x, y1, x, y2);
        g.setStroke(saved);
    }

    /**
     * [CA] Dibuixa una doble barra vertical: primera línia gruixuda i
     * una segona prima separada {@link #doubleBarGap} píxels.
     * <p>
     * [EN] Draws a double vertical bar: first a thick line and then a thin one
     * separated by {@link #doubleBarGap} pixels.
     *
     * @param x  [CA] Coordenada X de la primera línia / [EN] X coordinate of the first line
     * @param y1 [CA] Y inicial / [EN] Start Y
     * @param y2 [CA] Y final / [EN] End Y
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

    /**
     * [CA] Dibuixa una línia horitzontal amb gruix i color especificats.
     * <p>
     * [EN] Draws a horizontal line with the specified width and colour.
     *
     * @param x1    [CA] X inicial / [EN] Start X
     * @param x2    [CA] X final / [EN] End X
     * @param y     [CA] Coordenada Y / [EN] Y coordinate
     * @param width [CA] Gruix de la línia / [EN] Line width
     * @param color [CA] Color de la línia / [EN] Line colour
     */
    public void drawHorizontalLine(int x1, int x2, int y, float width, Color color) {
        Stroke saved = g.getStroke();
        g.setStroke(new BasicStroke(width));
        g.setColor(color);
        g.drawLine(x1, y, x2, y);
        g.setStroke(saved);
    }

    /**
     * [CA] Dibuixa una línia vertical amb gruix i color especificats.
     * <p>
     * [EN] Draws a vertical line with the specified width and colour.
     *
     * @param x     [CA] Coordenada X / [EN] X coordinate
     * @param y1    [CA] Y inicial / [EN] Start Y
     * @param y2    [CA] Y final / [EN] End Y
     * @param width [CA] Gruix de la línia / [EN] Line width
     * @param color [CA] Color de la línia / [EN] Line colour
     */
    public void drawVerticalLine(int x, int y1, int y2, float width, Color color) {
        Stroke saved = g.getStroke();
        g.setStroke(new BasicStroke(width));
        g.setColor(color);
        g.drawLine(x, y1, x, y2);
        g.setStroke(saved);
    }

    // ── Text ─────────────────────────────────────────────────────────────────

    /**
     * [CA] Dibuixa el nom d'una nota centrat horitzontalment i verticalment
     * dins d'una cel·la de la graella.
     * <p>
     * [EN] Draws a note name centred horizontally and vertically within a grid cell.
     *
     * @param text  [CA] Text a mostrar (nom de la nota) / [EN] Text to display (note name)
     * @param x     [CA] X de la cel·la / [EN] Cell X
     * @param y     [CA] Y de la cel·la / [EN] Cell Y
     * @param w     [CA] Amplada de la cel·la / [EN] Cell width
     * @param h     [CA] Alçada de la cel·la / [EN] Cell height
     * @param color [CA] Color del text / [EN] Text colour
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
     * [CA] Dibuixa un símbol d'acord en la posició indicada, usant {@link #chordFont}.
     * <p>
     * [EN] Draws a chord symbol at the given position using {@link #chordFont}.
     *
     * @param text  [CA] Símbol d'acord / [EN] Chord symbol
     * @param x     [CA] Coordenada X (baseline esquerra) / [EN] X coordinate (left baseline)
     * @param y     [CA] Coordenada Y (baseline) / [EN] Y coordinate (baseline)
     * @param color [CA] Color del text / [EN] Text colour
     */
    public void drawChordSymbol(String text, int x, int y, Color color) {
        g.setFont(chordFont);
        g.setColor(color);
        g.drawString(text, x, y);
    }

    /**
     * [CA] Dibuixa una marca de partitura (tempo, tonalitat, compàs) amb fons de color.
     * El fons es pinta a (bgX, bgY, bgW, bgH); el text a (x, y).
     * <p>
     * [EN] Draws a score mark (tempo, key, time signature) with a coloured background.
     * The background is painted at (bgX, bgY, bgW, bgH); the text at (x, y).
     *
     * @param text      [CA] Text de la marca / [EN] Mark text
     * @param x         [CA] X del text / [EN] Text X
     * @param y         [CA] Y del text / [EN] Text Y
     * @param textColor [CA] Color del text / [EN] Text colour
     * @param bgColor   [CA] Color de fons / [EN] Background colour
     * @param bgX       [CA] X del rectangle de fons / [EN] Background rectangle X
     * @param bgY       [CA] Y del rectangle de fons / [EN] Background rectangle Y
     * @param bgW       [CA] Amplada del fons / [EN] Background width
     * @param bgH       [CA] Alçada del fons / [EN] Background height
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
     * [CA] Dibuixa l'etiqueta d'un botó centrada dins del rectangle del botó,
     * usant {@link #labelFont}.
     * <p>
     * [EN] Draws a button label centred within the button rectangle using
     * {@link #labelFont}.
     *
     * @param text  [CA] Text de l'etiqueta / [EN] Label text
     * @param x     [CA] X del botó / [EN] Button X
     * @param y     [CA] Y del botó / [EN] Button Y
     * @param w     [CA] Amplada del botó / [EN] Button width
     * @param h     [CA] Alçada del botó / [EN] Button height
     * @param color [CA] Color del text / [EN] Text colour
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
     * [CA] Dibuixa un text en la posició absoluta indicada, amb font i color arbitraris.
     * <p>
     * [EN] Draws text at the given absolute position with arbitrary font and colour.
     *
     * @param text  [CA] Text a dibuixar / [EN] Text to draw
     * @param x     [CA] Coordenada X / [EN] X coordinate
     * @param y     [CA] Coordenada Y (baseline) / [EN] Y coordinate (baseline)
     * @param font  [CA] Font a usar / [EN] Font to use
     * @param color [CA] Color del text / [EN] Text colour
     */
    public void drawText(String text, int x, int y, Font font, Color color) {
        g.setFont(font);
        g.setColor(color);
        g.drawString(text, x, y);
    }

    // ── Selecció ─────────────────────────────────────────────────────────────

    /**
     * [CA] Omple la zona de selecció activa amb color semitransparent
     * ({@link #selectionFillColor}, {@link #selectionFillAlpha}).
     * <p>
     * [EN] Fills the active selection area with a semi-transparent colour
     * ({@link #selectionFillColor}, {@link #selectionFillAlpha}).
     *
     * @param x [CA] Coordenada X / [EN] X coordinate
     * @param y [CA] Coordenada Y / [EN] Y coordinate
     * @param w [CA] Amplada / [EN] Width
     * @param h [CA] Alçada / [EN] Height
     */
    public void drawSelectionFill(int x, int y, int w, int h) {
        Composite saved = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, selectionFillAlpha));
        g.setColor(selectionFillColor);
        g.fillRect(x, y, w, h);
        g.setComposite(saved);
    }

    /**
     * [CA] Dibuixa la vora de la zona de selecció amb color semitransparent
     * ({@link #selectionBorderColor}, {@link #selectionBorderAlpha}).
     * <p>
     * [EN] Draws the border of the selection area with a semi-transparent colour
     * ({@link #selectionBorderColor}, {@link #selectionBorderAlpha}).
     *
     * @param x [CA] Coordenada X / [EN] X coordinate
     * @param y [CA] Coordenada Y / [EN] Y coordinate
     * @param w [CA] Amplada / [EN] Width
     * @param h [CA] Alçada / [EN] Height
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
     * [CA] Dibuixa una fletxa cap avall (triangle ple) amb vèrtex superior centrat
     * en (cx, cy) i mides (w × h).
     * <p>
     * [EN] Draws a downward arrow (filled triangle) with the top vertex centred
     * at (cx, cy) and dimensions (w × h).
     *
     * @param cx [CA] Coordenada X del centre superior / [EN] X of the top centre
     * @param cy [CA] Coordenada Y del vèrtex superior / [EN] Y of the top vertex
     * @param w  [CA] Amplada de la base / [EN] Base width
     * @param h  [CA] Alçada del triangle / [EN] Triangle height
     */
    public void drawDownArrow(int cx, int cy, int w, int h) {
        int[] xs = {cx - w / 2, cx + w / 2, cx};
        int[] ys = {cy,          cy,          cy + h};
        Color saved = g.getColor();
        g.setColor(defaultTextColor);
        g.fillPolygon(xs, ys, 3);
        g.setColor(saved);
    }

    /**
     * [CA] Dibuixa una fletxa cap a la dreta (triangle ple) amb punta a (tipX, cy).
     * {@code triW} és la profunditat (base–punta) i {@code triH} l'alçada de la base.
     * <p>
     * [EN] Draws a right-pointing arrow (filled triangle) with tip at (tipX, cy).
     * {@code triW} is the depth (base to tip) and {@code triH} the base height.
     *
     * @param tipX  [CA] Coordenada X de la punta / [EN] X coordinate of the tip
     * @param cy    [CA] Coordenada Y del centre / [EN] Y coordinate of the centre
     * @param triW  [CA] Profunditat del triangle / [EN] Triangle depth
     * @param triH  [CA] Alçada de la base / [EN] Base height
     * @param color [CA] Color del triangle / [EN] Triangle colour
     */
    public void drawRightArrow(int tipX, int cy, int triW, int triH, Color color) {
        int[] xs = {tipX - triW, tipX - triW, tipX};
        int[] ys = {cy - triH / 2, cy + triH / 2, cy};
        Color saved = g.getColor();
        g.setColor(color);
        g.fillPolygon(xs, ys, 3);
        g.setColor(saved);
    }

    // ── Imatge offscreen ─────────────────────────────────────────────────────

    /**
     * [CA] Copia una porció d'una imatge offscreen a la pantalla (blit).
     * Equivalent a {@code g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null)}.
     * <p>
     * [EN] Copies a portion of an offscreen image to the screen (blit).
     * Equivalent to {@code g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null)}.
     *
     * @param img [CA] Imatge font / [EN] Source image
     * @param dx1 [CA] X destinació esquerra / [EN] Destination left X
     * @param dy1 [CA] Y destinació superior / [EN] Destination top Y
     * @param dx2 [CA] X destinació dreta / [EN] Destination right X
     * @param dy2 [CA] Y destinació inferior / [EN] Destination bottom Y
     * @param sx1 [CA] X font esquerra / [EN] Source left X
     * @param sy1 [CA] Y font superior / [EN] Source top Y
     * @param sx2 [CA] X font dreta / [EN] Source right X
     * @param sy2 [CA] Y font inferior / [EN] Source bottom Y
     */
    public void blitOffscreen(Image img,
                               int dx1, int dy1, int dx2, int dy2,
                               int sx1, int sy1, int sx2, int sy2) {
        g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
    }

    // ── Accés directe ────────────────────────────────────────────────────────

    /**
     * [CA] Retorna el {@link Graphics2D} subjacent per a operacions
     * no cobertes per {@code DrawKit}.
     * <p>
     * [EN] Returns the underlying {@link Graphics2D} for operations not
     * covered by {@code DrawKit}.
     *
     * @return [CA] El context gràfic subjacent / [EN] The underlying graphics context
     */
    public Graphics2D getGraphics() {
        return g;
    }
}
