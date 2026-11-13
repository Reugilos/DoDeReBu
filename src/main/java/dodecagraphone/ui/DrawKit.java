/*
 * PolyForm Noncommercial License 1.0.0
 * Copyright (c) 2024-2026 Pau Bofill. Powered by Claude AI.
 * Full license / Llicència completa: LICENSE (project root / arrel del projecte)
 */
package dodecagraphone.ui;

import dodecagraphone.model.color.ColorSets;
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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * [CA] La capa de dibuix de l'aplicació: <b>tot l'aspecte gràfic viu aquí</b>.
 * Cap altra classe no pinta amb {@link Graphics2D} ni tria un color, un gruix,
 * una font o on va un text dins de la seva caixa. Canviar el disseny és canviar
 * aquesta classe.
 * <p>
 * <b>El repartiment.</b> Els components decideixen <i>on</i> va cada cosa (el
 * rectangle d'una cel·la, d'una tecla, d'una marca), perquè la mateixa geometria
 * serveix per saber què hi ha sota el ratolí. DrawKit decideix <i>com es veu</i>:
 * els colors, els traços, les fonts i la posició del text dins de la caixa. Per
 * això els mètodes reben l'<b>estat</b> del que dibuixen (la nota, si sona, si
 * el botó és premut, de quin tipus és la marca) i no un color.
 * <p>
 * <b>Les aparences.</b> Alguns components guarden l'estat visual com un valor
 * calculat d'avançada (una cel·la, una tecla o un botó en saben l'aparença i la
 * canvien quan sona o es prem). Aquests valors només poden sortir de la
 * <b>paleta</b> d'aquesta classe ({@code aparencaDe…}): són opacs per als
 * components, que els guarden i els tornen a passar sense mirar-los.
 * <p>
 * <b>La fontaneria</b> (copiar buffers, retallar, preparar un context) també
 * passa per aquí, perquè cap crida a {@code Graphics2D} quedi fora; no és
 * disseny. Les consultes que no pinten ({@code getFontMetrics}, {@code getFont})
 * es queden als components: formen part del càlcul de la composició.
 * <p>
 * <b>Mentre no es toqui el tema, no canvia res del que es veu</b>: cada mètode
 * reprodueix les crides que substituïa, en el mateix ordre i deixant el context
 * amb el mateix estat (també els que deixaven un color posat).
 * <p>
 * Els mètodes són estàtics i no creen cap objecte per crida.
 * <p>
 * <b>Com es prova un disseny nou.</b> Les proves de disseny es fan a la
 * branca {@code disseny-grafic}, que surt de l'etiqueta
 * {@code drawkit-13-11-26}: aquella etiqueta és l'aspecte de sempre, idèntic
 * píxel a píxel al de {@code master}, i és la referència per comparar. El que
 * es toca per canviar l'aspecte és, per ordre:
 * <ol>
 *   <li>el <b>tema</b> (la secció de dalt): colors, traços, fonts i mides;</li>
 *   <li>la <b>paleta</b> ({@code aparencaDe…}): quin color té cada estat;</li>
 *   <li>el <b>cos</b> d'un mètode, quan el canvi no és de valor sinó de forma
 *       (arrodonir una cel·la, posar el nom en un altre lloc).</li>
 * </ol>
 * Tres coses s'han de respectar perquè el programa segueixi funcionant igual:
 * <ul>
 *   <li><b>la signatura</b> dels mètodes públics (els components no han de saber
 *       que el disseny ha canviat);</li>
 *   <li><b>les caixes</b>: un mètode dibuixa dins del rectangle que rep i no en
 *       surt, o el ratolí i el dibuix deixarien de coincidir;</li>
 *   <li><b>la velocitat</b>: res que es dibuixi a cada fotograma del play
 *       (barres, barra de reproducció, còpia dels buffers) no pot afegir
 *       transparències ni degradats sobre zones grans. Cada canvi de tema s'ha
 *       de tornar a mirar a 300 bpm.</li>
 * </ul>
 * Els criteris estètics que es vagin decidint s'apunten al {@code CLAUDE.md},
 * secció «Criteris de disseny gràfic», perquè no es perdin entre proves.
 * <p>
 * [EN] The application's drawing layer: <b>the whole look lives here</b>. No
 * other class paints with {@code Graphics2D} or picks a colour, a stroke, a font
 * or where a text goes inside its box. Components decide <i>where</i> things go
 * (that geometry also serves hit-testing); DrawKit decides <i>how they look</i>,
 * so methods take the <b>state</b> of what they draw, not a colour. Stored visual
 * states ("appearances") can only come from this class's palette.
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

    // Estil del manual del DoDeRe-Bú (Guia_Mara, nov. 2026): fons blanc, files
    // del pentagrama en gris càlid, el do central en rosa, la plantilla en groc
    // pàl·lid, separadors gris clar d'un píxel, text blanc sobre els colors de
    // les notes, i plafons en blau clar amb una capçalera gris. Els colors de les
    // dotze notes són els de ColorSets, que no es toca.

    private static final Color FONS = Color.WHITE;
    private static final Color TINTA = Color.BLACK;
    /** [CA] Separadors entre files: gris clar d'un píxel, com al manual. */
    private static final Color SEPARADOR_DE_FILA = new Color(217, 217, 217);
    /** [CA] Fila d'espai del pentagrama (entre línies). */
    private static final Color PAUTA_ESPAI = Color.WHITE;
    /** [CA] Fila d'una línia del pentagrama: gris càlid molt suau. */
    private static final Color PAUTA_LINIA = new Color(238, 236, 236);
    /** [CA] Fila del do central: rosa pàl·lid. */
    private static final Color PAUTA_DO_CENTRAL = new Color(255, 214, 214);
    /** [CA] Fila d'una nota de la plantilla: groc pàl·lid. */
    private static final Color PATRO_TRIAT = new Color(245, 255, 171);
    /** [CA] Fila fora de la plantilla. */
    private static final Color PATRO_FORA = Color.WHITE;
    /** [CA] Text i marques damunt dels colors de les notes i de les tecles. */
    private static final Color TEXT_SOBRE_NOTA = Color.WHITE;
    /** [CA] Separació blanca entre dues notes seguides. */
    private static final Color VORA_DE_NOTA = Color.WHITE;
    /** [CA] Blau clar dels plafons del manual (panell de botons, botó de format). */
    private static final Color PLAFO = new Color(219, 231, 249);
    /** [CA] Gris de la capçalera del manual (línia d'estat). */
    private static final Color CAPCALERA = new Color(242, 242, 242);
    /** [CA] Botó en repòs: blanc sobre el plafó blau, com les bafarades del manual. */
    private static final Color BOTO_NORMAL = Color.WHITE;
    /** [CA] Botó premut: el blau de la marca de tempo. */
    private static final Color BOTO_PREMUT = new Color(32, 89, 164);
    /** [CA] Text fosc dels botons i de la capçalera. */
    private static final Color TEXT_FOSC = new Color(35, 35, 45);
    private static final int BOTO_ARC = 12;
    private static final Color FONS_NOTA_PUNTEJADA = Color.BLACK;
    private static final Color TEXT_NOTA_PUNTEJADA = Color.WHITE;
    private static final Color SELECCIO_FARCIMENT = new Color(0, 100, 255);
    private static final float SELECCIO_FARCIMENT_ALFA = 0.3f;
    private static final Color SELECCIO_VORA = new Color(0, 50, 200);
    private static final float SELECCIO_VORA_ALFA = 0.8f;
    private static final Color SELECCIO_LLETRA_FARCIMENT = new Color(0, 90, 190, 60);
    private static final Color SELECCIO_LLETRA_VORA = new Color(0, 90, 190, 160);
    private static final Color MARCA_SELECCIONADA_FILET = Color.BLACK;
    private static final Color MARCA_SELECCIONADA_MARC = Color.YELLOW;
    private static final Color LLETRA = Color.BLACK;
    private static final Color LLETRA_EN_EDICIO = Color.DARK_GRAY;
    private static final Color CURSOR_DE_TEXT = Color.BLUE;
    private static final Color BOTO_DE_FORMAT = PLAFO;
    private static final Color FONS_LINIA_DESTAT = CAPCALERA;
    private static final Color TEXT_LINIA_DESTAT = TEXT_FOSC;
    private static final Color TEXT_TECLA_QUE_SONA = Color.BLACK;
    /** [CA] Tecla del PDF sense color a la paleta. / [EN] PDF key fallback. */
    private static final Color TECLA_SENSE_COLOR = Color.LIGHT_GRAY;

    /**
     * [CA] Els quatre tipus de marca de canvi de la franja d'acords, cadascun
     * amb el seu fons. El text pren el color que hi contrasti.
     * [EN] The four change-mark kinds, each with its background.
     */
    public enum Marca {
        /** [CA] El blau del «60» del manual. */
        TEMPO(new Color(32, 89, 164)),
        /** [CA] El morat del «Do» del manual. */
        TONALITAT(new Color(85, 84, 160)),
        VOLUM(new Color(22, 132, 96)),
        TRANSPOSICIO(new Color(240, 205, 0));

        private final Color fons;

        Marca(Color fons) {
            this.fons = fons;
        }
    }

    // ── Traços ───────────────────────────────────────────────────────────────

    private static final Stroke TRAC_COMPAS = new BasicStroke(1.5f);
    /** [CA] La barra de compàs damunt de la pantalla és més gruixuda que la del buffer. */
    private static final Stroke TRAC_COMPAS_PANTALLA = new BasicStroke(2f);
    /** [CA] Temps: guions curts, com al dodecagrama del manual. */
    private static final Stroke TRAC_TEMPS = new BasicStroke(1, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_BEVEL, 0, new float[]{5, 4}, 0);
    private static final Stroke TRAC_SEPARADOR_DE_FILA = new BasicStroke(1f);
    private static final Stroke TRAC_VORA_DE_NOTA = new BasicStroke(1f);
    private static final Stroke TRAC_DOBLE_BARRA_GRUIXUDA = new BasicStroke(3);
    private static final Stroke TRAC_DOBLE_BARRA_PRIMA = new BasicStroke(2);
    private static final int DOBLE_BARRA_SEPARACIO = 4;
    private static final Stroke TRAC_BARRA_DE_REPRODUCCIO = new BasicStroke(3f);
    private static final Stroke TRAC_MARCA_FILET = new BasicStroke(1f);
    private static final Stroke TRAC_MARCA_MARC = new BasicStroke(3f);
    private static final Stroke TRAC_CURSOR = new BasicStroke(2f);

    // ── Fonts ────────────────────────────────────────────────────────────────

    /**
     * [CA] La lletra del manual és una geomètrica rodona; la més propera que porta
     * Windows és la Century Gothic. Si no hi és, la SansSerif de Java.
     */
    private static final String FAMILIA_GEOMETRICA = familiaDisponible("Century Gothic", "SansSerif");
    private static final String FAMILIA_ACORDS = FAMILIA_GEOMETRICA;
    private static final int ESTIL_ACORDS = Font.BOLD;
    private static final String FAMILIA_LLETRA = FAMILIA_GEOMETRICA;
    private static final int ESTIL_LLETRA = Font.PLAIN;
    private static final String FAMILIA_INTERFICIE = FAMILIA_GEOMETRICA;
    private static final int ESTIL_INTERFICIE = Font.BOLD;

    private static String familiaDisponible(String preferida, String alternativa) {
        try {
            for (String f : java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames()) {
                if (f.equalsIgnoreCase(preferida)) return f;
            }
        } catch (Exception ignored) {
        }
        return alternativa;
    }
    /** [CA] La lletra d'una tecla és una fracció de la seva alçada. */
    private static final double FONT_DE_TECLA_PER_ALCADA = 0.65;
    private static final int FONT_DE_TECLA_MINIMA = 8;

    // ── Mides ────────────────────────────────────────────────────────────────

    private static final int TRIANGLET_MITJA_AMPLADA = 4;
    private static final int TRIANGLET_ALCADA = 6;
    private static final int BOTO_DE_FORMAT_ARC = 4;
    /**
     * [CA] Marge del text dins de la caixa d'una marca. És públic perquè la
     * franja d'acords el necessita per saber quant ocupa una marca.
     * [EN] Text padding inside a mark box (the chord strip needs it for layout).
     */
    public static final int MARGE_DE_MARCA = 2;

    /** [CA] Font dels acords i de les marques, a la mida donada. / [EN] Chord/mark font. */
    public static Font fontAcords(int mida) {
        return new Font(FAMILIA_ACORDS, ESTIL_ACORDS, mida);
    }

    /** [CA] Font de la lletra, a la mida donada. / [EN] Lyrics font. */
    public static Font fontLletra(int mida) {
        return new Font(FAMILIA_LLETRA, ESTIL_LLETRA, mida);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  PALETA: les aparences que guarden els components
    // ═════════════════════════════════════════════════════════════════════════

    /** [CA] Una cel·la amb nota. / [EN] A cell holding a note. */
    public static Color aparencaDeNota(int midi) {
        return ColorSets.getEncesColor(midi % 12);
    }

    /** [CA] Una cel·la amb una nota que sona. / [EN] A cell whose note is sounding. */
    public static Color aparencaDeNotaQueSona() {
        return ColorSets.getColorFons();
    }

    /** [CA] Una fila buida, a la vista de pentagrama. / [EN] An empty row, staff view. */
    public static Color aparencaDePauta(int midi) {
        // ColorSets diu de quin tipus és la fila; el color el posa el tema.
        Color c = ColorSets.getPentagramaColor(midi);
        if (c != null && c.equals(ColorSets.getEncesColor(ColorSets.LINIA_PENTA))) return PAUTA_LINIA;
        if (c != null && c.equals(ColorSets.getEncesColor(ColorSets.DO_PENTA))) return PAUTA_DO_CENTRAL;
        return PAUTA_ESPAI;
    }

    /**
     * [CA] Una fila buida, a la vista de plantilla: s'il·lumina si la nota és
     * del patró.
     * [EN] An empty row, template view.
     */
    public static Color aparencaDelPatro(int midi, List<Integer> patro) {
        // ColorSets diu si la nota és del patró (MARFIL); el color el posa el tema.
        Color c = ColorSets.getChoiceColor(midi, patro);
        return (c != null && c.equals(ColorSets.getEncesColor(ColorSets.MARFIL))) ? PATRO_TRIAT : PATRO_FORA;
    }

    /** [CA] Una tecla en silenci. / [EN] A silent key. */
    public static Color aparencaDeTecla(int midi) {
        return ColorSets.getEncesColor(midi % 12);
    }

    /** [CA] Una tecla que sona. / [EN] A sounding key. */
    public static Color aparencaDeTeclaQueSona() {
        return ColorSets.getColorFons();
    }

    /** [CA] Un botó, premut o no. / [EN] A button, pressed or not. */
    public static Color aparencaDeBoto(boolean premut) {
        return premut ? BOTO_PREMUT : BOTO_NORMAL;
    }

    /** [CA] La tinta del text sobre una nota (i de la seva vora). */
    private static Color tintaDeNota(int midi) {
        // El manual escriu en blanc damunt de totes les notes, també del groc.
        // L'alternativa de sempre (negre sobre re, ri i mi):
        // ColorSets.getGridSquareFontColor(midi).
        return TEXT_SOBRE_NOTA;
    }

    /** [CA] Negre o blanc, el que contrasti amb el fons. */
    private static Color tintaQueContrasta(Color fons) {
        return ColorSets.getSeparatorColor(fons);
    }

    /**
     * [CA] El color dels slides del teclat. Ara és sempre el de la línia del
     * pentagrama. Abans de DrawKit, {@code MySlide} en calculava dues
     * alternatives que després sobreescrivia; es deixen aquí per si es volen
     * recuperar:
     * <ul>
     *   <li>vista de pentagrama: {@code ColorSets.getPentagramaColor(midi)};</li>
     *   <li>vista de plantilla: {@code ColorSets.getChoiceColor(midi, patro)}.</li>
     * </ul>
     * [EN] Keyboard slide colour; former alternatives kept above.
     */
    private static Color colorDelSlide() {
        // Al manual, la columna dels slides és blanca. El gris de sempre:
        // ColorSets.getEncesColor(ColorSets.LINIA_PENTA).
        return FONS;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  GRAELLA
    // ═════════════════════════════════════════════════════════════════════════

    /** [CA] Una cel·la de la graella, amb l'aparença que tingui. / [EN] A grid cell. */
    public static void casella(Graphics2D g, int x, int y, int w, int h, Color aparenca) {
        g.setColor(aparenca);
        g.fillRect(x, y, w, h);
    }

    /** [CA] El nom d'una nota dins de la seva cel·la, en la font actual. / [EN] A note name. */
    public static void nomDeNota(Graphics2D g, String nom, int x, int y, int w, int h, int midi) {
        g.setColor(tintaDeNota(midi));
        g.drawString(nom, (int) (2 + x + w / 3), (int) (y + h * 0.8));
    }

    /**
     * [CA] La vora esquerra d'una nota, que separa notes contigües; del color
     * del nom, perquè es llegeixin com una sola marca. Deixa el color posat.
     * [EN] A note's left edge. Leaves the colour set.
     */
    public static void voraDeNota(Graphics2D g, int x, int y, int h, int midi) {
        // Al manual, dues notes seguides se separen amb un filet blanc.
        Stroke abans = g.getStroke();
        g.setColor(VORA_DE_NOTA);
        g.setStroke(TRAC_VORA_DE_NOTA);
        g.drawLine(x, y, x, y + h - 1);
        g.setStroke(abans);
    }

    /**
     * [CA] La nota amb puntet: un quadrat de costat {@code h} i el nom a sobre,
     * a {@code (textX, y + h)}. Deixa el color del text posat.
     * [EN] The dotted note: a square and the name on top.
     */
    public static void notaPuntejada(Graphics2D g, int x, int y, int h, String nom, int textX) {
        g.setColor(FONS_NOTA_PUNTEJADA);
        g.fillRect(x, y, h, h);
        g.setColor(TEXT_NOTA_PUNTEJADA);
        g.drawString(nom, textX, y + h);
    }

    /** [CA] El separador de dalt d'una cel·la. Deixa el color posat. / [EN] Row divider. */
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

    /** [CA] Barra de compàs, al buffer. Deixa la tinta posada. / [EN] Measure line. */
    public static void barraDeCompas(Graphics2D g, int x, int y1, int y2) {
        linia(g, TRAC_COMPAS, x, y1, y2);
    }

    /**
     * [CA] Barra de compàs dibuixada damunt de la pantalla, sobre la del buffer
     * (que així no es perd quan el fit anacrusis comprimeix la imatge).
     * [EN] On-screen measure line.
     */
    public static void barraDeCompasEnPantalla(Graphics2D g, int x, int y1, int y2) {
        linia(g, TRAC_COMPAS_PANTALLA, x, y1, y2);
    }

    /** [CA] Línia de temps, discontínua. Deixa la tinta posada. / [EN] Beat line. */
    public static void barraDeTemps(Graphics2D g, int x, int y1, int y2) {
        linia(g, TRAC_TEMPS, x, y1, y2);
    }

    /** [CA] Barra de reproducció. Deixa la tinta posada. / [EN] Play bar. */
    public static void barraDeReproduccio(Graphics2D g, int x, int y1, int y2) {
        linia(g, TRAC_BARRA_DE_REPRODUCCIO, x, y1, y2);
    }

    private static void linia(Graphics2D g, Stroke trac, int x, int y1, int y2) {
        Stroke abans = g.getStroke();
        g.setStroke(trac);
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

    // ═════════════════════════════════════════════════════════════════════════
    //  FRANJA D'ACORDS
    // ═════════════════════════════════════════════════════════════════════════

    /** [CA] Comença un símbol d'acord en la font donada. / [EN] Starts a chord symbol. */
    public static void comencaAcord(Graphics2D g, Font font) {
        g.setColor(TINTA);
        g.setFont(font);
    }

    /** [CA] Un tros del símbol d'acord començat. / [EN] A chord text piece. */
    public static void textAcord(Graphics2D g, String text, int x, int y) {
        g.drawString(text, x, y);
    }

    /**
     * [CA] El trianglet d'atac d'un acord, amb la punta a {@code (x, yPunta)}.
     * Torna el color d'abans.
     * [EN] A chord's attack triangle; restores the colour.
     */
    public static void triangletDAtac(Graphics2D g, int x, int yPunta) {
        int[] xs = {x - TRIANGLET_MITJA_AMPLADA, x + TRIANGLET_MITJA_AMPLADA, x};
        int[] ys = {yPunta - TRIANGLET_ALCADA, yPunta - TRIANGLET_ALCADA, yPunta};
        Color abans = g.getColor();
        g.setColor(TINTA);
        g.fillPolygon(xs, ys, 3);
        g.setColor(abans);
    }

    /** [CA] Comença les marques de canvi en la font donada. / [EN] Starts the change marks. */
    public static void comencaMarques(Graphics2D g, Font font) {
        g.setFont(font);
    }

    /**
     * [CA] Una marca de canvi: la caixa amb el fons del seu tipus i el rètol
     * centrat en vertical, del color que hi contrasti. Fa servir la font de
     * {@link #comencaMarques}.
     * [EN] A change mark: the box in its kind's colour and the contrasting label.
     */
    public static void marca(Graphics2D g, int x, int y, int w, int h, Marca tipus, String rotul) {
        FontMetrics fm = g.getFontMetrics();
        g.setColor(tipus.fons);
        g.fillRect(x, y, w, h);
        g.setColor(tintaQueContrasta(tipus.fons));
        int dalt = y + (h - (fm.getAscent() + fm.getDescent())) / 2;
        g.drawString(rotul, x + MARGE_DE_MARCA, dalt + fm.getAscent());
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

    /** [CA] Prepara el buffer de la lletra amb la seva font. / [EN] Sets the lyrics font. */
    public static void fontDeLletra(Graphics2D g, int mida) {
        g.setFont(fontLletra(mida));
    }

    /**
     * [CA] Comença a escriure lletra: la confirmada o la que s'està editant.
     * [EN] Starts writing lyrics: committed or being edited.
     */
    public static void comencaLletra(Graphics2D g, boolean enEdicio) {
        g.setColor(enEdicio ? LLETRA_EN_EDICIO : LLETRA);
    }

    /** [CA] Una síl·laba. / [EN] A syllable. */
    public static void sillaba(Graphics2D g, String text, int x, int y) {
        g.drawString(text, x, y);
    }

    /** [CA] Una síl·laba comprimida en horitzontal (fit anacrusis). / [EN] A scaled syllable. */
    public static void sillabaEscalada(Graphics2D g, String text, int x, int y, double escalaX) {
        AffineTransform oldAt = g.getTransform();
        g.translate(x, y);
        g.scale(escalaX, 1.0);
        g.drawString(text, 0, 0);
        g.setTransform(oldAt);
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
    //  PANTALLA I FRANGES
    // ═════════════════════════════════════════════════════════════════════════

    /** [CA] El fons de la pantalla sencera, amb la font de la interfície. / [EN] Screen background. */
    public static void fonsDePantalla(Graphics2D g, int w, int h, int midaDeLletra) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font(FAMILIA_INTERFICIE, ESTIL_INTERFICIE, midaDeLletra));
        g.setColor(FONS);
        g.fillRect(0, 0, w, h);
    }

    /**
     * [CA] El fons d'un rectangle: un buffer que es torna a dibuixar, la part de
     * les franges que queda al costat del teclat, una imatge de l'exportació.
     * [EN] A background rectangle.
     */
    public static void fons(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(FONS);
        g.fillRect(x, y, w, h);
    }

    /** [CA] La línia que separa el teclat de la partitura. / [EN] Keyboard/score separator. */
    public static void separadorDeTeclat(Graphics2D g, int x, int y1, int y2) {
        g.setColor(TINTA);
        g.drawLine(x, y1, x, y2);
    }

    /** [CA] El rètol d'una franja («Acords», «Lletra»), en la font actual. / [EN] Strip label. */
    public static void rotulDeFranja(Graphics2D g, String text, int x, int y) {
        g.setColor(TINTA);
        g.drawString(text, x, y);
    }

    /** [CA] El botó del format d'acords, amb el seu rètol. / [EN] Chord format button. */
    public static void botoDeFormat(Graphics2D g, String rotul, int x, int y, int w, int h, int textX, int textY) {
        g.setColor(BOTO_DE_FORMAT);
        g.fillRoundRect(x, y, w, h, BOTO_DE_FORMAT_ARC, BOTO_DE_FORMAT_ARC);
        g.setColor(TINTA);
        g.drawRoundRect(x, y, w, h, BOTO_DE_FORMAT_ARC, BOTO_DE_FORMAT_ARC);
        g.drawString(rotul, textX, textY);
    }

    /** [CA] La vora d'un component (càmera, franges, panell de botons). / [EN] Component border. */
    public static void voraDeComponent(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(TINTA);
        g.drawRect(x, y, w, h);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  BOTONS, TECLAT I LÍNIA D'ESTAT
    // ═════════════════════════════════════════════════════════════════════════

    /** [CA] El fons del panell de botons. / [EN] Button panel background. */
    public static void fonsDelPanellDeBotons(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(PLAFO);
        g.fillRect(x, y, w, h);
    }

    /**
     * [CA] Un botó, amb el seu rètol centrat, en la font actual.
     * [EN] A button with its centred label.
     */
    public static void boto(Graphics2D g, double x, double y, double w, double h, Color aparenca, String rotul) {
        FontMetrics fm = g.getFontMetrics();
        float tx = (float) (x + (w - fm.stringWidth(rotul)) / 2.0);
        float ty = (float) (y + (h + fm.getAscent() - fm.getDescent()) / 2.0);
        Object aa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(aparenca);
        g.fillRoundRect((int) x + 2, (int) y + 2, (int) w - 4, (int) h - 4, BOTO_ARC, BOTO_ARC);
        if (aa != null) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
        g.setColor(BOTO_PREMUT.equals(aparenca) ? Color.WHITE : TEXT_FOSC);
        g.drawString(rotul, tx, ty);
    }

    /** [CA] Una tecla, amb l'aparença que tingui. / [EN] A key. */
    public static void tecla(Graphics2D g, int x, int y, int w, int h, Color aparenca) {
        g.setColor(aparenca == null ? TECLA_SENSE_COLOR : aparenca);
        g.fillRect(x, y, w, h);
    }

    /**
     * [CA] El nom d'una tecla, en una lletra proporcional a la seva alçada.
     * Mentre sona, en negre; si no, del color que toca a la nota.
     * [EN] A key name, sized to the key; black while sounding.
     */
    public static void nomDeTecla(Graphics2D g, String nom, double x, double y, double h, int midi, boolean sona) {
        int baseY = (int) (y + h * 0.8);
        Font abans = g.getFont();
        int mida = Math.max(FONT_DE_TECLA_MINIMA, (int) (h * FONT_DE_TECLA_PER_ALCADA));
        if (mida != abans.getSize()) g.setFont(abans.deriveFont((float) mida));
        g.setColor(sona ? TEXT_TECLA_QUE_SONA : tintaDeNota(midi));
        g.drawString(nom, (float) x, (float) baseY);
        g.setFont(abans);
    }

    /** [CA] El slide del costat d'una tecla. / [EN] A key's slide. */
    public static void slide(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(colorDelSlide());
        g.fillRect(x, y, w, h);
    }

    /** [CA] El triangle que marca la tònica al teclat, amb la punta a la dreta. / [EN] Tonic triangle. */
    public static void triangleDeTonica(Graphics2D g, int punta, int cy, int costat, int midi) {
        int[] xp = {punta - costat, punta - costat, punta};
        int[] yp = {cy - costat / 2, cy + costat / 2, cy};
        g.setColor(tintaDeNota(midi));
        g.fillPolygon(xp, yp, 3);
    }

    /**
     * [CA] Acaba el teclat deixant la tinta per defecte posada: el que es dibuixa
     * després pot comptar-hi.
     * [EN] Ends the keyboard leaving the default ink set.
     */
    public static void tancaTeclat(Graphics2D g) {
        g.setColor(TINTA);
    }

    /** [CA] El fons de la línia d'estat. / [EN] Status line background. */
    public static void fonsDeLiniaDEstat(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(FONS_LINIA_DESTAT);
        g.fillRect(x, y, w, h);
        g.setColor(TEXT_LINIA_DESTAT);
    }

    /** [CA] Una línia de text de la línia d'estat, en la font actual. / [EN] A status line text line. */
    public static void textDeLiniaDEstat(Graphics2D g, String text, int x, int y) {
        g.setColor(TEXT_LINIA_DESTAT);
        g.drawString(text, x, y);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  FONTANERIA: buffers, imatges, retalls i context
    // ═════════════════════════════════════════════════════════════════════════

    /** [CA] Prepara el context d'un buffer offscreen. / [EN] Prepares an offscreen buffer. */
    public static void preparaOffscreen(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** [CA] Prepara el context de l'exportació SVG. / [EN] Prepares the SVG export. */
    public static void preparaSvg(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** [CA] Prepara la pantalla per a text comprimit nítid. / [EN] Antialiasing on screen. */
    public static void preparaTextComprimit(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    /** [CA] Torna a posar una font desada abans. / [EN] Restores a saved font. */
    public static void restauraFont(Graphics2D g, Font font) {
        g.setFont(font);
    }

    /** [CA] Torna a posar una tinta desada abans. / [EN] Restores a saved ink. */
    public static void restauraTinta(Graphics2D g, Color tinta) {
        g.setColor(tinta);
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

    /**
     * [CA] Omple un buffer nou més ample amb el contingut del vell i posa fons a
     * la resta.
     * [EN] Fills a wider new buffer with the old one.
     */
    public static void ampliaOffscreen(Graphics2D nou, Image vell, int ampladaVella, int ampladaNova, int alcada) {
        preparaOffscreen(nou);
        nou.drawImage(vell, 0, 0, null);
        nou.setColor(FONS);
        nou.fillRect(ampladaVella, 0, ampladaNova - ampladaVella, alcada);
    }

    /** [CA] Copia una porció d'una imatge. / [EN] Blits an image region. */
    public static void copiaImatge(Graphics2D g, Image img,
            int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2) {
        g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
    }

    /** [CA] Copia una porció d'una imatge estirant-la, bilineal. / [EN] Bilinear stretched blit. */
    public static void copiaImatgeEstirada(Graphics2D g, Image img,
            int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
    }

    /**
     * [CA] Dibuixa una imatge amb una transformació, bilineal, dins d'un retall,
     * i torna el context com estava.
     * [EN] Draws a transformed image, bilinear, inside a clip.
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
}
