/*
 * PolyForm Noncommercial License 1.0.0
 * Copyright (c) 2024-2026 Pau Bofill. Powered by Claude AI.
 * Full license / Llicència completa: LICENSE (project root / arrel del projecte)
 */
package dodecagraphone.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * [CA] La capa de dibuix de l'aplicació: <b>totes</b> les crides que pinten
 * (a la pantalla, als buffers offscreen i a les imatges del PDF i l'SVG) passen
 * per aquí, i cap altra classe no toca directament els mètodes de pintar de
 * {@link Graphics2D}. Canviar l'aspecte gràfic és canviar aquesta classe: el
 * tema (colors, gruixos, guions i fonts) és a dalt de tot, i cada mètode diu
 * <i>què</i> es dibuixa (una cel·la, una barra de compàs, la caixa d'una marca),
 * no amb quina primitiva.
 * <p>
 * Els mètodes són estàtics i reben el {@code Graphics2D}: no es crea cap objecte
 * per crida, i el {@code drawSquare} de la graella en fa milers per redibuix.
 * Els traços i els colors del tema es creen una sola vegada.
 * <p>
 * <b>La regla d'aquesta capa és no canviar res del que es veu</b> mentre no es
 * toqui el tema: cada mètode reprodueix exactament les crides que hi havia al
 * lloc d'on ve, en el mateix ordre i deixant el context amb el mateix estat. Els
 * que deixaven un color o un traç posats sense restaurar-los segueixen fent el
 * mateix, perquè el codi de després pot comptar-hi.
 * <p>
 * Les consultes que no pinten (la font actual, les {@code FontMetrics}) es
 * queden on són: formen part del càlcul de la composició i no canvien l'aspecte.
 * <p>
 * [EN] The application's drawing layer: <b>every</b> painting call (screen,
 * offscreen buffers, PDF and SVG images) goes through here. Changing the look
 * means changing this class: the theme (colours, widths, dashes, fonts) is at
 * the top, and each method says <i>what</i> is drawn, not with which primitive.
 * Methods are static and take the {@code Graphics2D}; nothing is allocated per
 * call. Until the theme is changed, every method reproduces exactly the calls it
 * replaced, in the same order and leaving the same context state.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.1
 */
public final class DrawKit {

    private DrawKit() {}

    // ═════════════════════════════════════════════════════════════════════════
    //  TEMA
    // ═════════════════════════════════════════════════════════════════════════

    // ── Colors ───────────────────────────────────────────────────────────────

    /** [CA] Fons dels buffers i de les franges. / [EN] Buffer and strip background. */
    public static final Color FONS = Color.WHITE;
    /** [CA] Tinta per defecte: línies, vores, text. / [EN] Default ink. */
    public static final Color TINTA = Color.BLACK;
    /** [CA] Separador entre files de la graella. / [EN] Grid row divider. */
    public static final Color SEPARADOR_DE_FILA = Color.GRAY;
    /** [CA] Fons de la nota amb puntet. / [EN] Dotted note background. */
    public static final Color FONS_NOTA_PUNTEJADA = Color.BLACK;
    /** [CA] Text de la nota amb puntet. / [EN] Dotted note text. */
    public static final Color TEXT_NOTA_PUNTEJADA = Color.WHITE;
    /** [CA] Selecció de la graella: farciment i vora. / [EN] Grid selection fill and border. */
    public static final Color SELECCIO_FARCIMENT = new Color(0, 100, 255);
    public static final float SELECCIO_FARCIMENT_ALFA = 0.3f;
    public static final Color SELECCIO_VORA = new Color(0, 50, 200);
    public static final float SELECCIO_VORA_ALFA = 0.8f;
    /** [CA] Selecció de la lletra (amb alfa al color). / [EN] Lyrics selection (alpha in the colour). */
    public static final Color SELECCIO_LLETRA_FARCIMENT = new Color(0, 90, 190, 60);
    public static final Color SELECCIO_LLETRA_VORA = new Color(0, 90, 190, 160);
    /** [CA] Marca seleccionada: filet exterior i marc. / [EN] Selected mark: outer rule and frame. */
    public static final Color MARCA_SELECCIONADA_FILET = Color.BLACK;
    public static final Color MARCA_SELECCIONADA_MARC = Color.YELLOW;
    /** [CA] Lletra confirmada i lletra en edició. / [EN] Committed and in-edit lyrics. */
    public static final Color LLETRA = Color.BLACK;
    public static final Color LLETRA_EN_EDICIO = Color.DARK_GRAY;
    /** [CA] Cursor d'edició de la lletra. / [EN] Lyrics edit cursor. */
    public static final Color CURSOR_DE_TEXT = Color.BLUE;
    /** [CA] Botó del format d'acords. / [EN] Chord format button. */
    public static final Color BOTO_DE_FORMAT = new Color(220, 230, 255);
    /** [CA] Línia d'estat: fons i text. / [EN] Status line background and text. */
    public static final Color FONS_LINIA_DESTAT = Color.GRAY;
    public static final Color TEXT_LINIA_DESTAT = Color.WHITE;
    /** [CA] Text dels botons. / [EN] Button text. */
    public static final Color TEXT_DE_BOTO = Color.WHITE;
    /** [CA] Text d'una tecla mentre sona. / [EN] Text of a sounding key. */
    public static final Color TEXT_TECLA_ENCESA = Color.BLACK;

    // ── Traços ───────────────────────────────────────────────────────────────

    private static final float[] GUIONS_DE_TEMPS = {15, 3};
    /** [CA] Barra de compàs, al buffer. / [EN] Measure line, in the buffer. */
    public static final Stroke TRAC_COMPAS = new BasicStroke(1.5f);
    /**
     * [CA] Barra de compàs dibuixada damunt de la pantalla, sobre la del buffer
     * (que així no es perd quan el fit anacrusis comprimeix la imatge).
     * [EN] Measure line drawn on screen, over the buffer's one.
     */
    public static final Stroke TRAC_COMPAS_PANTALLA = new BasicStroke(2f);
    /** [CA] Línia de temps, discontínua. / [EN] Dashed beat line. */
    public static final Stroke TRAC_TEMPS = new BasicStroke(1, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_BEVEL, 0, GUIONS_DE_TEMPS, 0);
    /** [CA] Separador de files. / [EN] Row divider. */
    public static final Stroke TRAC_SEPARADOR_DE_FILA = new BasicStroke(0.2f);
    /** [CA] Vora esquerra d'una nota. / [EN] Note left edge. */
    public static final Stroke TRAC_VORA_DE_NOTA = new BasicStroke(2f);
    /** [CA] Doble barra final: la gruixuda i la prima. / [EN] Final double bar. */
    public static final Stroke TRAC_DOBLE_BARRA_GRUIXUDA = new BasicStroke(3);
    public static final Stroke TRAC_DOBLE_BARRA_PRIMA = new BasicStroke(2);
    public static final int DOBLE_BARRA_SEPARACIO = 4;
    /** [CA] Barra de reproducció. / [EN] Play bar. */
    public static final Stroke TRAC_BARRA_DE_REPRODUCCIO = new BasicStroke(3f);
    /** [CA] Marca seleccionada: filet i marc. / [EN] Selected mark rule and frame. */
    public static final Stroke TRAC_MARCA_FILET = new BasicStroke(1f);
    public static final Stroke TRAC_MARCA_MARC = new BasicStroke(3f);
    /** [CA] Cursor de la lletra. / [EN] Lyrics cursor. */
    public static final Stroke TRAC_CURSOR = new BasicStroke(2f);

    // ── Fonts ────────────────────────────────────────────────────────────────

    /** [CA] Família dels acords i de les marques. / [EN] Chord and mark family. */
    public static final String FAMILIA_ACORDS = "SansSerif";
    public static final int ESTIL_ACORDS = Font.BOLD;
    /** [CA] Família de la lletra. / [EN] Lyrics family. */
    public static final String FAMILIA_LLETRA = "SansSerif";
    public static final int ESTIL_LLETRA = Font.PLAIN;
    /** [CA] Família de la interfície (botons, teclat, rètols). / [EN] UI family. */
    public static final String FAMILIA_INTERFICIE = "Dialog";
    public static final int ESTIL_INTERFICIE = Font.BOLD;

    // ── Mides ────────────────────────────────────────────────────────────────

    /** [CA] Trianglet d'atac d'un acord: mitja amplada i alçada. / [EN] Chord attack triangle. */
    public static final int TRIANGLET_MITJA_AMPLADA = 4;
    public static final int TRIANGLET_ALCADA = 6;
    /** [CA] Arrodoniment del botó de format. / [EN] Format button corner arc. */
    public static final int BOTO_DE_FORMAT_ARC = 4;

    /** [CA] Font dels acords i de les marques, a la mida donada. / [EN] Chord/mark font. */
    public static Font fontAcords(int mida) {
        return new Font(FAMILIA_ACORDS, ESTIL_ACORDS, mida);
    }

    /** [CA] Font de la lletra, a la mida donada. / [EN] Lyrics font. */
    public static Font fontLletra(int mida) {
        return new Font(FAMILIA_LLETRA, ESTIL_LLETRA, mida);
    }

    /** [CA] Font de la interfície, a la mida donada. / [EN] UI font. */
    public static Font fontInterficie(int mida) {
        return new Font(FAMILIA_INTERFICIE, ESTIL_INTERFICIE, mida);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  CONTEXT
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * [CA] Prepara el {@code Graphics2D} d'un buffer offscreen (graella, acords,
     * lletra): antialiàsing de formes.
     * [EN] Prepares an offscreen buffer's graphics: shape antialiasing.
     */
    public static void preparaOffscreen(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    /**
     * [CA] Prepara el {@code Graphics2D} de l'exportació SVG: antialiàsing de
     * formes i de text.
     * [EN] Prepares the SVG export graphics.
     */
    public static void preparaSvg(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** [CA] Posa la font. / [EN] Sets the font. */
    public static void font(Graphics2D g, Font f) {
        g.setFont(f);
    }

    /** [CA] Posa el color de la tinta. / [EN] Sets the ink colour. */
    public static void tinta(Graphics2D g, Color c) {
        g.setColor(c);
    }

    /** [CA] Retalla a un rectangle i torna el retall d'abans. / [EN] Clips; returns the old clip. */
    public static Shape retalla(Graphics2D g, int x, int y, int w, int h) {
        Shape abans = g.getClip();
        g.clipRect(x, y, w, h);
        return abans;
    }

    /** [CA] Torna el retall desat. / [EN] Restores a saved clip. */
    public static void restauraRetall(Graphics2D g, Shape retall) {
        g.setClip(retall);
    }

    /** [CA] Fixa el retall a un rectangle (no l'intersecta). / [EN] Sets the clip. */
    public static void posaRetall(Graphics2D g, int x, int y, int w, int h) {
        g.setClip(x, y, w, h);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  FONS I IMATGES
    // ═════════════════════════════════════════════════════════════════════════

    /** [CA] Esborra un rectangle amb el color de fons. / [EN] Clears a rectangle. */
    public static void esborra(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(FONS);
        g.fillRect(x, y, w, h);
    }

    /**
     * [CA] Omple un buffer nou més ample amb el contingut del vell i esborra la
     * resta. És el que fan els tres buffers en créixer.
     * [EN] Fills a wider new buffer with the old one and clears the rest.
     */
    public static void ampliaOffscreen(Graphics2D nou, Image vell, int ampladaVella, int ampladaNova, int alcada) {
        preparaOffscreen(nou);
        nou.drawImage(vell, 0, 0, null);
        nou.setColor(FONS);
        nou.fillRect(ampladaVella, 0, ampladaNova - ampladaVella, alcada);
    }

    /** [CA] Copia una porció d'una imatge (origen → destí). / [EN] Blits an image region. */
    public static void copiaImatge(Graphics2D g, Image img,
            int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2) {
        g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
    }

    /**
     * [CA] Copia una porció d'una imatge estirant-la amb interpolació bilineal
     * (la franja de lletra del PDF, que es compensa en vertical).
     * [EN] Blits an image region with bilinear stretching.
     */
    public static void copiaImatgeEstirada(Graphics2D g, Image img,
            int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
    }

    /**
     * [CA] Dibuixa una imatge amb una transformació (escala horitzontal del fit
     * anacrusis), amb interpolació bilineal, dins d'un retall, i torna el
     * context com estava.
     * [EN] Draws an image with a transform, bilinear, inside a clip.
     */
    public static void imatgeTransformada(Graphics2D g, BufferedImage img, AffineTransform at,
            int clipX, int clipY, int clipW, int clipH) {
        Shape oldClip = g.getClip();
        Object oldInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.clipRect(clipX, clipY, clipW, clipH);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, at, null);
        if (oldInterp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterp);
        }
        g.setClip(oldClip);
    }

    /** [CA] Omple un rectangle d'un color. / [EN] Fills a rectangle. */
    public static void rectangle(Graphics2D g, int x, int y, int w, int h, Color c) {
        g.setColor(c);
        g.fillRect(x, y, w, h);
    }

    /** [CA] Vora d'un component (càmera, franges, panell de botons). / [EN] Component border. */
    public static void voraDeComponent(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(TINTA);
        g.drawRect(x, y, w, h);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  GRAELLA
    // ═════════════════════════════════════════════════════════════════════════

    /** [CA] El fons d'una cel·la (pauta, patró o nota). / [EN] A cell's fill. */
    public static void cella(Graphics2D g, int x, int y, int w, int h, Color c) {
        g.setColor(c);
        g.fillRect(x, y, w, h);
    }

    /**
     * [CA] El nom d'una nota dins de la seva cel·la, en la font actual.
     * [EN] A note name inside its cell, in the current font.
     */
    public static void nomDeNota(Graphics2D g, String nom, int x, int y, int w, int h, Color c) {
        g.setColor(c);
        g.drawString(nom, (int) (2 + x + w / 3), (int) (y + h * 0.8));
    }

    /**
     * [CA] La vora esquerra d'una nota, que separa notes contigües. Deixa el
     * color posat.
     * [EN] A note's left edge. Leaves the colour set.
     */
    public static void voraDeNota(Graphics2D g, int x, int y, int h, Color c) {
        Stroke abans = g.getStroke();
        g.setColor(c);
        g.setStroke(TRAC_VORA_DE_NOTA);
        g.drawLine(x + 1, y, x + 1, y + h - 1);
        g.setStroke(abans);
    }

    /**
     * [CA] La nota amb puntet: un quadrat negre de costat {@code h} i el nom a
     * sobre. Deixa el color del text posat.
     * [EN] The dotted note: a black square and the name on top.
     */
    public static void notaPuntejada(Graphics2D g, int x, int y, int h, String nom, int textX, int textY) {
        g.setColor(FONS_NOTA_PUNTEJADA);
        g.fillRect(x, y, h, h);
        g.setColor(TEXT_NOTA_PUNTEJADA);
        g.drawString(nom, textX, textY);
    }

    /** [CA] El separador horitzontal de dalt d'una cel·la. Deixa el color posat. / [EN] Row divider. */
    public static void separadorDeFila(Graphics2D g, int x1, int x2, int y) {
        Stroke abans = g.getStroke();
        g.setStroke(TRAC_SEPARADOR_DE_FILA);
        g.setColor(SEPARADOR_DE_FILA);
        g.drawLine(x1, y, x2, y);
        g.setStroke(abans);
    }

    /** [CA] El rectangle de la selecció de la graella. / [EN] Grid selection rectangle. */
    public static void seleccio(Graphics2D g, int x, int y, int w, int h) {
        Composite oldComposite = g.getComposite();
        Color oldColor = g.getColor();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, SELECCIO_FARCIMENT_ALFA));
        g.setColor(SELECCIO_FARCIMENT);
        g.fillRect(x, y, w, h);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, SELECCIO_VORA_ALFA));
        g.setColor(SELECCIO_VORA);
        g.drawRect(x, y, w - 1, h - 1);
        g.setComposite(oldComposite);
        g.setColor(oldColor);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LÍNIES DE TEMPS, COMPÀS I FINAL
    // ═════════════════════════════════════════════════════════════════════════

    /** [CA] Barra de compàs vertical. Deixa la tinta posada. / [EN] Measure line. */
    public static void barraDeCompas(Graphics2D g, int x, int y1, int y2) {
        Stroke abans = g.getStroke();
        g.setStroke(TRAC_COMPAS);
        g.setColor(TINTA);
        g.drawLine(x, y1, x, y2);
        g.setStroke(abans);
    }

    /** [CA] Barra de compàs damunt de la pantalla. Deixa la tinta posada. / [EN] On-screen measure line. */
    public static void barraDeCompasEnPantalla(Graphics2D g, int x, int y1, int y2) {
        Stroke abans = g.getStroke();
        g.setStroke(TRAC_COMPAS_PANTALLA);
        g.setColor(TINTA);
        g.drawLine(x, y1, x, y2);
        g.setStroke(abans);
    }

    /** [CA] Línia de temps vertical, discontínua. Deixa la tinta posada. / [EN] Beat line. */
    public static void barraDeTemps(Graphics2D g, int x, int y1, int y2) {
        Stroke abans = g.getStroke();
        g.setStroke(TRAC_TEMPS);
        g.setColor(TINTA);
        g.drawLine(x, y1, x, y2);
        g.setStroke(abans);
    }

    /** [CA] Doble barra final. / [EN] Final double bar. */
    public static void dobleBarra(Graphics2D g, int x, int y1, int y2) {
        Stroke abans = g.getStroke();
        g.setColor(TINTA);
        g.setStroke(TRAC_DOBLE_BARRA_GRUIXUDA);
        g.drawLine(x, y1, x, y2);
        g.setStroke(TRAC_DOBLE_BARRA_PRIMA);
        g.drawLine(x + DOBLE_BARRA_SEPARACIO, y1, x + DOBLE_BARRA_SEPARACIO, y2);
        g.setStroke(abans);
    }

    /** [CA] Barra de reproducció. Deixa la tinta posada. / [EN] Play bar. */
    public static void barraDeReproduccio(Graphics2D g, int x, int y1, int y2) {
        Stroke abans = g.getStroke();
        g.setStroke(TRAC_BARRA_DE_REPRODUCCIO);
        g.setColor(TINTA);
        g.drawLine(x, y1, x, y2);
        g.setStroke(abans);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  FRANJA D'ACORDS
    // ═════════════════════════════════════════════════════════════════════════

    /** [CA] Comença un símbol d'acord: tinta i font. / [EN] Starts a chord symbol. */
    public static void comencaAcord(Graphics2D g, Font f) {
        g.setColor(TINTA);
        g.setFont(f);
    }

    /** [CA] Un tros de text d'un acord, en la tinta i la font actuals. / [EN] A chord text piece. */
    public static void textAcord(Graphics2D g, String text, int x, int y) {
        g.drawString(text, x, y);
    }

    /**
     * [CA] El trianglet d'atac d'un acord, amb la punta a {@code (x, yPunta)}.
     * Torna el color d'abans.
     * [EN] A chord's attack triangle; restores the previous colour.
     */
    public static void triangletDAtac(Graphics2D g, int x, int yPunta) {
        int[] xs = {x - TRIANGLET_MITJA_AMPLADA, x + TRIANGLET_MITJA_AMPLADA, x};
        int[] ys = {yPunta - TRIANGLET_ALCADA, yPunta - TRIANGLET_ALCADA, yPunta};
        Color abans = g.getColor();
        g.setColor(TINTA);
        g.fillPolygon(xs, ys, 3);
        g.setColor(abans);
    }

    /**
     * [CA] La caixa d'una marca de canvi i el seu text, del color que contrasti
     * amb el fons.
     * [EN] A change mark's box and its contrasting text.
     */
    public static void marca(Graphics2D g, int x, int y, int w, int h, Color fons, Color text,
            String rotul, int textX, int textY) {
        g.setColor(fons);
        g.fillRect(x, y, w, h);
        g.setColor(text);
        g.drawString(rotul, textX, textY);
    }

    /** [CA] El marc d'una marca seleccionada, per fora de la caixa. / [EN] Selected mark frame. */
    public static void marcaSeleccionada(Graphics2D g, int x, int y, int w, int h) {
        Stroke abans = g.getStroke();
        g.setStroke(TRAC_MARCA_FILET);
        g.setColor(MARCA_SELECCIONADA_FILET);
        g.drawRect(x - 4, y - 4, w + 7, h + 7);
        g.setStroke(TRAC_MARCA_MARC);
        g.setColor(MARCA_SELECCIONADA_MARC);
        g.drawRect(x - 2, y - 2, w + 3, h + 3);
        g.setStroke(abans);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LLETRA
    // ═════════════════════════════════════════════════════════════════════════

    /** [CA] Una síl·laba, en la tinta i la font actuals. / [EN] A syllable. */
    public static void sillaba(Graphics2D g, String text, int x, int y) {
        g.drawString(text, x, y);
    }

    /**
     * [CA] Una síl·laba comprimida en horitzontal (fit anacrusis), en la tinta i
     * la font actuals.
     * [EN] A horizontally scaled syllable.
     */
    public static void sillabaEscalada(Graphics2D g, String text, int x, int y, double escalaX) {
        AffineTransform oldAt = g.getTransform();
        g.translate(x, y);
        g.scale(escalaX, 1.0);
        g.drawString(text, 0, 0);
        g.setTransform(oldAt);
    }

    /** [CA] Activa l'antialiàsing de formes a la pantalla. / [EN] Shape antialiasing on screen. */
    public static void antialiasing(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    /** [CA] La franja translúcida de la selecció de la lletra. / [EN] Lyrics selection band. */
    public static void seleccioLletra(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(SELECCIO_LLETRA_FARCIMENT);
        g.fillRect(x, y, w, h);
        g.setColor(SELECCIO_LLETRA_VORA);
        g.drawRect(x, y, w - 1, h - 1);
    }

    /** [CA] El cursor d'edició de la lletra. / [EN] Lyrics edit cursor. */
    public static void cursorDeText(Graphics2D g, int x, int y1, int y2) {
        Stroke abans = g.getStroke();
        g.setStroke(TRAC_CURSOR);
        g.setColor(CURSOR_DE_TEXT);
        g.drawLine(x, y1, x, y2);
        g.setStroke(abans);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  INTERFÍCIE: rètols, botons, teclat i línia d'estat
    // ═════════════════════════════════════════════════════════════════════════

    /** [CA] Un text en la tinta indicada i la font actual. / [EN] Text in the given ink. */
    public static void text(Graphics2D g, String text, int x, int y, Color c) {
        g.setColor(c);
        g.drawString(text, x, y);
    }

    /** [CA] Un text en la tinta i la font actuals. / [EN] Text in the current ink. */
    public static void text(Graphics2D g, String text, int x, int y) {
        g.drawString(text, x, y);
    }

    /** [CA] Un text en coordenades decimals, en la tinta i la font actuals. / [EN] Float text. */
    public static void text(Graphics2D g, String text, float x, float y) {
        g.drawString(text, x, y);
    }

    /** [CA] Línia vertical d'un píxel en la tinta. / [EN] One-pixel vertical rule. */
    public static void filetVertical(Graphics2D g, int x, int y1, int y2) {
        g.setColor(TINTA);
        g.drawLine(x, y1, x, y2);
    }

    /** [CA] El botó del format d'acords, amb el seu rètol. / [EN] Chord format button. */
    public static void botoDeFormat(Graphics2D g, String rotul, int x, int y, int w, int h, int textX, int textY) {
        g.setColor(BOTO_DE_FORMAT);
        g.fillRoundRect(x, y, w, h, BOTO_DE_FORMAT_ARC, BOTO_DE_FORMAT_ARC);
        g.setColor(TINTA);
        g.drawRoundRect(x, y, w, h, BOTO_DE_FORMAT_ARC, BOTO_DE_FORMAT_ARC);
        g.drawString(rotul, textX, textY);
    }

    /** [CA] Un botó: el fons del seu color i el text centrat. / [EN] A button. */
    public static void boto(Graphics2D g, int x, int y, int w, int h, Color c, String text, float textX, float textY) {
        g.setColor(c);
        g.fillRect(x, y, w, h);
        g.setColor(TEXT_DE_BOTO);
        g.drawString(text, textX, textY);
    }

    /** [CA] El triangle de la tònica al teclat, amb la punta a la dreta. / [EN] Tonic triangle. */
    public static void triangleDeTonica(Graphics2D g, int punta, int cy, int costat, Color c) {
        int[] xp = {punta - costat, punta - costat, punta};
        int[] yp = {cy - costat / 2, cy + costat / 2, cy};
        g.setColor(c);
        g.fillPolygon(xp, yp, 3);
    }

    /** [CA] El fons de la línia d'estat. Deixa la tinta del text posada. / [EN] Status line. */
    public static void fonsDeLiniaDEstat(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(FONS_LINIA_DESTAT);
        g.fillRect(x, y, w, h);
        g.setColor(TEXT_LINIA_DESTAT);
    }
}
