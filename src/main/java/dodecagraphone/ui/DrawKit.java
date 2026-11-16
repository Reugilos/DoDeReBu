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
 *   <li>el <b>tema</b>: colors, traços, fonts i mides. No és al codi sinó en un
 *       fitxer de la carpeta {@code temes} (vegeu {@link Tema}), o sigui que
 *       canviar un valor o fer un tema nou no demana compilar;</li>
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

    // Tots els valors surten del tema triat al config.properties (vegeu
    // {@link Tema}): els temes són fitxers de text a la carpeta «temes», i el
    // clàssic de dins del programa fa de reserva per a qualsevol valor que falti.
    // Es llegeixen una sola vegada, en carregar-se aquesta classe.

    private static final Tema T = Tema.actual();

    // ── Colors ───────────────────────────────────────────────────────────────

    private static final Color FONS = T.color("fons");
    private static final Color TINTA = T.color("tinta");
    private static final Color SEPARADOR_DE_FILA = T.color("graella.separador.color");
    /** [CA] {@code null} vol dir que el color el decideix ColorSets. */
    private static final Color PAUTA_ESPAI = T.color("pauta.espai");
    private static final Color PAUTA_LINIA = T.color("pauta.linia");
    private static final Color PAUTA_DO_CENTRAL = T.color("pauta.doCentral");
    private static final Color PATRO_TRIAT = T.color("patro.triat");
    private static final Color PATRO_FORA = T.color("patro.fora");
    private static final Color TEXT_SOBRE_NOTA = T.color("nota.text");
    /** [CA] {@code null}: el MI té el color de ColorSets. */
    private static final Color NOTA_MI = T.color("nota.mi");
    /** [CA] {@code null}: les notes de la partitura van del color de la seva nota. Si no, totes van d'aquest. */
    private static final Color NOTA_GRIS = T.color("nota.gris");
    /**
     * [CA] Com d'apagades es veuen les tecles <b>no seleccionades</b> del teclat
     * (les que no són a la plantilla triada): 0 = color ple, 1 = negre. Les
     * seleccionades sempre van amb el color ple.
     */
    private static final float TECLA_APAGA = Math.max(0f, Math.min(1f, T.decimal("tecla.apaga")));
    /** [CA] {@code null}: el text de les tecles en silenci és el de les notes. */
    private static final Color TEXT_TECLA_SILENCI = T.color("tecla.text");
    /** [CA] {@code null}: la vora de la nota va del color del text de la nota. */
    private static final Color VORA_DE_NOTA = T.color("nota.vora.color");
    private static final int VORA_DE_NOTA_DESPLACAMENT = T.enter("nota.vora.desplacament");
    private static final Color FONS_NOTA_PUNTEJADA = T.color("notaPuntejada.fons");
    private static final Color TEXT_NOTA_PUNTEJADA = T.color("notaPuntejada.text");
    private static final Color SELECCIO_FARCIMENT = T.color("seleccio.farciment");
    private static final float SELECCIO_FARCIMENT_ALFA = T.decimal("seleccio.farciment.alfa");
    private static final Color SELECCIO_VORA = T.color("seleccio.vora");
    private static final float SELECCIO_VORA_ALFA = T.decimal("seleccio.vora.alfa");
    private static final Color SELECCIO_LLETRA_FARCIMENT = T.color("seleccioLletra.farciment");
    private static final Color SELECCIO_LLETRA_VORA = T.color("seleccioLletra.vora");
    private static final Color MARCA_SELECCIONADA_FILET = T.color("marcaSeleccionada.filet");
    private static final Color MARCA_SELECCIONADA_MARC = T.color("marcaSeleccionada.marc");
    private static final Color LLETRA = T.color("lletra.text");
    private static final Color LLETRA_EN_EDICIO = T.color("lletra.edicio");
    private static final Color CURSOR_DE_TEXT = T.color("lletra.cursor");
    private static final Color BOTO_DE_FORMAT = T.color("botoFormat.fons");
    private static final Color FONS_LINIA_DESTAT = T.color("liniaEstat.fons");
    private static final Color TEXT_LINIA_DESTAT = T.color("liniaEstat.text");
    private static final Color PANELL_DE_BOTONS = T.color("panellBotons.fons");
    private static final Color BOTO_NORMAL = T.color("boto.normal");
    private static final Color BOTO_PREMUT = T.color("boto.premut");
    private static final Color TEXT_DE_BOTO = T.color("boto.text");
    private static final Color TEXT_DE_BOTO_PREMUT = T.color("boto.textPremut");
    private static final int BOTO_ARC = T.enter("boto.arrodoniment");
    private static final int BOTO_MARGE = T.enter("boto.marge");
    private static final Color SLIDE = T.color("slide");
    private static final Color TEXT_TECLA_QUE_SONA = T.color("tecla.textQueSona");
    /** [CA] Si una tecla que sona es ressalta (fons blanc, text negre). Amb {@code no} es veu igual que en silenci. */
    private static final boolean TECLA_RESSALTA = T.si("tecla.ressalta");
    /** [CA] Tecla del PDF sense color a la paleta. / [EN] PDF key fallback. */
    private static final Color TECLA_SENSE_COLOR = T.color("tecla.senseColor");
    private static final boolean TEXT_SUAVITZAT = T.si("text.suavitzat");

    // Les caixes d'Acords i de Lletra, a l'esquerra, per sobre i per sota del teclat
    private static final Color CAIXA_ACORDS = T.color("caixaAcords.fons");
    private static final Color CAIXA_LLETRA = T.color("caixaLletra.fons");
    private static final Color CAIXA_VORA = T.color("caixa.vora.color");
    private static final Stroke TRAC_CAIXA = new BasicStroke(T.decimal("caixa.vora.gruix"));
    private static final boolean CAIXA_AMB_VORA = T.decimal("caixa.vora.gruix") > 0;

    // Els tips (les finestretes que expliquen un botó). Color null = el del sistema.
    private static final Color TIP_FONS = T.color("tip.fons");
    private static final Color TIP_TEXT = T.color("tip.text");
    private static final Color TIP_VORA = T.color("tip.vora.color");
    private static final int TIP_VORA_GRUIX = T.enter("tip.vora.gruix");
    private static final int TIP_MARGE = T.enter("tip.marge");
    private static final int TIP_FONT_MIDA = T.enter("tip.font.mida");

    // Les icones dels botons (play, pausa, inici) i les fletxes de «Tò >>»
    private static final double ICONA_PROPORCIO = T.decimal("icona.proporcio");
    private static final double FLETXA_PROPORCIO = T.decimal("fletxa.proporcio");

    /**
     * [CA] Els quatre tipus de marca de canvi de la franja d'acords. El color de
     * cadascuna el posa el tema; el text pren el color que hi contrasti.
     * [EN] The four change-mark kinds; their colours come from the theme.
     * <p>
     * [CA] {@link #TRANSPOSICIO} està <b>en desús</b>: la franja ja no dibuixa la
     * marca de transposició (ara surt al botó de l'instrument, p. ex.
     * {@code Glock (24 st)}). Es conserva, amb la clau {@code marca.transposicio}
     * dels temes, per si la marca torna; ara no la fa servir res.
     * [EN] {@link #TRANSPOSICIO} is unused: the strip no longer draws it.
     */
    public enum Marca {
        TEMPO, TONALITAT, VOLUM, TRANSPOSICIO
    }

    private static final Color MARCA_TEMPO = T.color("marca.tempo");
    private static final Color MARCA_TONALITAT = T.color("marca.tonalitat");
    private static final Color MARCA_VOLUM = T.color("marca.volum");
    /** [CA] En desús: la marca de transposició ja no es dibuixa (vegeu {@link Marca}). */
    private static final Color MARCA_TRANSPOSICIO = T.color("marca.transposicio");

    private static Color fonsDeMarca(Marca m) {
        switch (m) {
            case TEMPO:     return MARCA_TEMPO;
            case TONALITAT: return MARCA_TONALITAT;
            case VOLUM:     return MARCA_VOLUM;
            default:        return MARCA_TRANSPOSICIO;
        }
    }

    // ── Traços ───────────────────────────────────────────────────────────────

    private static final Stroke TRAC_COMPAS = new BasicStroke(T.decimal("compas.gruix"));
    /** [CA] La barra de compàs damunt de la pantalla, sobre la del buffer. */
    private static final Stroke TRAC_COMPAS_PANTALLA = new BasicStroke(T.decimal("compas.pantalla.gruix"));
    private static final Stroke TRAC_TEMPS = new BasicStroke(T.decimal("temps.gruix"), BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_BEVEL, 0, T.decimals("temps.guions"), 0);
    private static final Stroke TRAC_SEPARADOR_DE_FILA = new BasicStroke(T.decimal("graella.separador.gruix"));
    private static final Stroke TRAC_VORA_DE_NOTA = new BasicStroke(T.decimal("nota.vora.gruix"));
    private static final Stroke TRAC_DOBLE_BARRA_GRUIXUDA = new BasicStroke(T.decimal("dobleBarra.gruixuda"));
    private static final Stroke TRAC_DOBLE_BARRA_PRIMA = new BasicStroke(T.decimal("dobleBarra.prima"));
    private static final int DOBLE_BARRA_SEPARACIO = T.enter("dobleBarra.separacio");
    private static final Stroke TRAC_BARRA_DE_REPRODUCCIO = new BasicStroke(T.decimal("reproduccio.gruix"));
    private static final Stroke TRAC_MARCA_FILET = new BasicStroke(T.decimal("marcaSeleccionada.filet.gruix"));
    private static final Stroke TRAC_MARCA_MARC = new BasicStroke(T.decimal("marcaSeleccionada.marc.gruix"));
    private static final Stroke TRAC_CURSOR = new BasicStroke(T.decimal("lletra.cursor.gruix"));

    // ── Fonts ────────────────────────────────────────────────────────────────

    private static final String FAMILIA_ACORDS = familiaDisponible(T.text("font.acords"));
    private static final int ESTIL_ACORDS = T.estil("font.acords.estil");
    private static final String FAMILIA_LLETRA = familiaDisponible(T.text("font.lletra"));
    private static final int ESTIL_LLETRA = T.estil("font.lletra.estil");
    private static final String FAMILIA_INTERFICIE = familiaDisponible(T.text("font.interficie"));
    private static final int ESTIL_INTERFICIE = T.estil("font.interficie.estil");
    /** [CA] La lletra d'una tecla és una fracció de la seva alçada. */
    private static final double FONT_DE_TECLA_PER_ALCADA = T.decimal("tecla.font.proporcio");
    private static final int FONT_DE_TECLA_MINIMA = T.enter("tecla.font.minima");

    /**
     * [CA] La família demanada si és instal·lada o és una de les lògiques de Java
     * (Dialog, SansSerif…); si no, la SansSerif.
     */
    private static String familiaDisponible(String preferida) {
        for (String logica : new String[]{"Dialog", "DialogInput", "SansSerif", "Serif", "Monospaced"}) {
            if (logica.equalsIgnoreCase(preferida)) return logica;
        }
        try {
            for (String f : java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getAvailableFontFamilyNames()) {
                if (f.equalsIgnoreCase(preferida)) return f;
            }
        } catch (Exception ignored) {
        }
        System.err.println("Tema: la lletra «" + preferida + "» no és instal·lada; es fa servir SansSerif");
        return "SansSerif";
    }

    // ── Mides ────────────────────────────────────────────────────────────────

    private static final int TRIANGLET_MITJA_AMPLADA = T.enter("trianglet.mitjaAmplada");
    private static final int TRIANGLET_ALCADA = T.enter("trianglet.alcada");
    private static final int BOTO_DE_FORMAT_ARC = T.enter("botoFormat.arrodoniment");
    /**
     * [CA] Marge del text dins de la caixa d'una marca. És públic perquè la
     * franja d'acords el necessita per saber quant ocupa una marca; no és del
     * tema, perquè és composició.
     * [EN] Text padding inside a mark box (layout, not theme).
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
        return NOTA_GRIS != null ? NOTA_GRIS : colorDeNota(midi);
    }

    /** [CA] El color de la nota (0–11): el del tema per al MI si en té, o ColorSets. */
    private static Color colorDeNota(int midi) {
        int n = midi % 12;
        if (n == ColorSets.MI && NOTA_MI != null) return NOTA_MI;
        return ColorSets.getEncesColor(n);
    }

    /** [CA] Una cel·la amb una nota que sona. / [EN] A cell whose note is sounding. */
    public static Color aparencaDeNotaQueSona() {
        return ColorSets.getColorFons();
    }

    /**
     * [CA] Una fila buida, a la vista de pentagrama. ColorSets diu de quin tipus
     * és la fila (espai, línia o do central); el color el posa el tema, o
     * ColorSets mateix si el tema hi diu {@code colorsets}.
     * [EN] An empty row, staff view.
     */
    public static Color aparencaDePauta(int midi) {
        Color c = ColorSets.getPentagramaColor(midi);
        Color delTema;
        if (c != null && c.equals(ColorSets.getEncesColor(ColorSets.LINIA_PENTA))) delTema = PAUTA_LINIA;
        else if (c != null && c.equals(ColorSets.getEncesColor(ColorSets.DO_PENTA))) delTema = PAUTA_DO_CENTRAL;
        else delTema = PAUTA_ESPAI;
        return delTema != null ? delTema : c;
    }

    /**
     * [CA] Una fila buida, a la vista de plantilla. ColorSets diu si la nota és
     * del patró; el color el posa el tema, o ColorSets si hi diu {@code colorsets}.
     * [EN] An empty row, template view.
     */
    public static Color aparencaDelPatro(int midi, List<Integer> patro) {
        Color c = ColorSets.getChoiceColor(midi, patro);
        boolean triat = c != null && c.equals(ColorSets.getEncesColor(ColorSets.MARFIL));
        Color delTema = triat ? PATRO_TRIAT : PATRO_FORA;
        return delTema != null ? delTema : c;
    }

    /** [CA] Una tecla en silenci. / [EN] A silent key. */
    public static Color aparencaDeTecla(int midi) {
        return colorDeNota(midi);
    }

    /**
     * [CA] Una tecla en silenci que <b>no és de la plantilla triada</b>: el color
     * de la nota apagat (barrejat amb negre segons {@code tecla.apaga}). Amb
     * {@code tecla.apaga = 0} (el clàssic) és igual que {@link #aparencaDeTecla}.
     * [EN] A silent key outside the chosen template: the note colour dimmed.
     */
    public static Color aparencaDeTeclaApagada(int midi) {
        Color c = colorDeNota(midi);
        if (c == null || TECLA_APAGA <= 0f) return c;
        float f = 1f - TECLA_APAGA;
        return new Color(Math.round(c.getRed() * f), Math.round(c.getGreen() * f), Math.round(c.getBlue() * f));
    }

    /** [CA] Una tecla que sona: el color de ressaltat, o el color ple de la nota si el tema no ressalta. / [EN] A sounding key. */
    public static Color aparencaDeTeclaQueSona(int midi) {
        return TECLA_RESSALTA ? ColorSets.getColorFons() : colorDeNota(midi);
    }

    /** [CA] Un botó, premut o no. / [EN] A button, pressed or not. */
    public static Color aparencaDeBoto(boolean premut) {
        if (premut) {
            return BOTO_PREMUT != null ? BOTO_PREMUT : ColorSets.getIluminatColor(ColorSets.BUTO);
        }
        return BOTO_NORMAL != null ? BOTO_NORMAL : ColorSets.getEncesColor(ColorSets.BUTO);
    }

    /** [CA] La tinta del text sobre una nota o una tecla. */
    private static Color tintaDeNota(int midi) {
        return TEXT_SOBRE_NOTA != null ? TEXT_SOBRE_NOTA : ColorSets.getGridSquareFontColor(midi);
    }

    /** [CA] Negre o blanc, el que contrasti amb el fons. */
    private static Color tintaQueContrasta(Color fons) {
        return ColorSets.getSeparatorColor(fons);
    }

    /**
     * [CA] El color dels slides del teclat. Amb {@code colorsets}, el de la línia
     * del pentagrama. Abans de DrawKit, {@code MySlide} en calculava dues
     * alternatives que després sobreescrivia; es deixen aquí per si es volen
     * recuperar:
     * <ul>
     *   <li>vista de pentagrama: {@code ColorSets.getPentagramaColor(midi)};</li>
     *   <li>vista de plantilla: {@code ColorSets.getChoiceColor(midi, patro)}.</li>
     * </ul>
     * [EN] Keyboard slide colour; former alternatives kept above.
     */
    private static Color colorDelSlide() {
        return SLIDE != null ? SLIDE : ColorSets.getEncesColor(ColorSets.LINIA_PENTA);
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
        Stroke abans = g.getStroke();
        g.setColor(VORA_DE_NOTA != null ? VORA_DE_NOTA : tintaDeNota(midi));
        g.setStroke(TRAC_VORA_DE_NOTA);
        int xv = x + VORA_DE_NOTA_DESPLACAMENT;
        g.drawLine(xv, y, xv, y + h - 1);
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
        Color fons = fonsDeMarca(tipus);
        g.setColor(fons);
        g.fillRect(x, y, w, h);
        g.setColor(tintaQueContrasta(fons));
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
        if (TEXT_SUAVITZAT) {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
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

    /** [CA] Les dues caixes que hi ha al costat del teclat. / [EN] The two boxes beside the keyboard. */
    public enum Caixa {
        ACORDS, LLETRA
    }

    /**
     * [CA] El fons de la caixa d'Acords o de Lletra, a la columna del teclat (el
     * de cadascuna és del tema). El marc va a part, {@link #margeDeCaixa}, perquè
     * s'ha de dibuixar <b>després</b> del teclat: les tecles toquen la caixa i, si
     * el marc anés amb el fons, en taparien la vora.
     * [EN] Background of one of the two boxes beside the keyboard.
     */
    public static void caixaDeFranja(Graphics2D g, int x, int y, int w, int h, Caixa quina) {
        Color fons = quina == Caixa.ACORDS ? CAIXA_ACORDS : CAIXA_LLETRA;
        g.setColor(fons != null ? fons : FONS);
        g.fillRect(x, y, w, h);
    }

    /**
     * [CA] El marc d'una caixa (color i gruix del tema; gruix 0 = sense marc).
     * Es dibuixa sobre el teclat i la càmera, com una vora senzilla d'un píxel.
     * [EN] The frame of a box; drawn on top of everything else.
     */
    public static void margeDeCaixa(Graphics2D g, int x, int y, int w, int h) {
        if (!CAIXA_AMB_VORA) return;
        Stroke abans = g.getStroke();
        g.setStroke(TRAC_CAIXA);
        g.setColor(CAIXA_VORA != null ? CAIXA_VORA : TINTA);
        g.drawRect(x, y, w, h);
        g.setStroke(abans);
    }

    /**
     * [CA] L'aspecte d'un tip: el color de fons, el del text, el marc, el marge i
     * la mida de la lletra els posa el tema. Un color {@code colorsets} deixa el
     * del sistema (el groc de sempre, amb el doble marc de la finestreta i del tip).
     * [EN] Styles a tip popup from the theme.
     */
    public static void estilDeTip(javax.swing.JPopupMenu popup, javax.swing.JToolTip tip) {
        if (TIP_FONS != null) {
            popup.setOpaque(true);
            popup.setBackground(TIP_FONS);
            tip.setOpaque(true);
            tip.setBackground(TIP_FONS);
        }
        if (TIP_TEXT != null) tip.setForeground(TIP_TEXT);
        if (TIP_FONT_MIDA > 0) tip.setFont(new Font(FAMILIA_INTERFICIE, Font.PLAIN, TIP_FONT_MIDA));
        javax.swing.border.Border marge = javax.swing.BorderFactory.createEmptyBorder(
                TIP_MARGE, TIP_MARGE, TIP_MARGE, TIP_MARGE);
        if (TIP_VORA != null && TIP_VORA_GRUIX > 0) {
            tip.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(TIP_VORA, TIP_VORA_GRUIX), marge));
        } else if (TIP_MARGE > 0) {
            tip.setBorder(javax.swing.BorderFactory.createCompoundBorder(tip.getBorder(), marge));
        }
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
        g.setColor(PANELL_DE_BOTONS != null ? PANELL_DE_BOTONS : ColorSets.getColorFons());
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
        g.setColor(aparenca);
        if (BOTO_ARC <= 0) {
            g.fillRect((int) x + BOTO_MARGE, (int) y + BOTO_MARGE,
                    (int) w - 2 * BOTO_MARGE, (int) h - 2 * BOTO_MARGE);
        } else {
            Object aa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.fillRoundRect((int) x + BOTO_MARGE, (int) y + BOTO_MARGE,
                    (int) w - 2 * BOTO_MARGE, (int) h - 2 * BOTO_MARGE, BOTO_ARC, BOTO_ARC);
            if (aa != null) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
        }
        g.setColor(aparenca.equals(aparencaDeBoto(true)) ? TEXT_DE_BOTO_PREMUT : TEXT_DE_BOTO);
        if (rotul.startsWith("{") && rotul.endsWith("}") && dibuixaIcona(g, x, y, w, h, rotul)) return;
        int k = rotul.lastIndexOf('{');
        // k == 0: només fletxes ({<<}), com els botons de columna i de pàgina
        if (k >= 0 && rotul.endsWith("}") && dibuixaRotulAmbFletxes(g, x, y, w, ty, rotul, k)) return;
        g.drawString(rotul, tx, ty);
    }

    // ── Icones i fletxes dels botons ─────────────────────────────────────────
    //
    // Els rètols d'un botó poden portar una icona dibuixada, en lloc de caràcters
    // que cada lletra dibuixa a la seva manera: {play}, {pausa} i {inici}. I els
    // de Tò/Ex acaben amb una o dues fletxes («Tò {>>}», «Ex {<<}»), que es
    // dibuixen centrades en alçada amb la paraula. Tot va amb el color del text
    // del botó (que ja és del tema) i la mida surt de l'alçada del botó.

    private static boolean dibuixaIcona(Graphics2D g, double x, double y, double w, double h, String marca) {
        double s = Math.min(w, h) * ICONA_PROPORCIO;
        double cx = x + w / 2.0, cy = y + h / 2.0;
        Object aa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean fet = true;
        switch (marca) {
            case "{play}": {
                // Triangle ple que apunta a la dreta, centrat per l'ull (el centre de
                // gravetat d'un triangle és a un terç de la base, no al mig)
                double tw = s * 0.9, x0 = cx - tw * 0.4;
                g.fill(triangle(x0, cy - s / 2, x0, cy + s / 2, x0 + tw, cy));
                break;
            }
            case "{pausa}": {
                double bw = s * 0.28, gap = s * 0.2, x0 = cx - (2 * bw + gap) / 2.0;
                g.fill(new java.awt.geom.Rectangle2D.Double(x0, cy - s / 2, bw, s));
                g.fill(new java.awt.geom.Rectangle2D.Double(x0 + bw + gap, cy - s / 2, bw, s));
                break;
            }
            case "{inici}": {
                // Una barra i un triangle que apunta a l'esquerra: «torna al principi»
                double bw = s * 0.2, tw = s * 0.72, x0 = cx - s / 2;
                g.fill(new java.awt.geom.Rectangle2D.Double(x0, cy - s / 2, bw, s));
                g.fill(triangle(x0 + s, cy - s / 2, x0 + s, cy + s / 2, x0 + s - tw, cy));
                break;
            }
            default:
                fet = false;
        }
        if (aa != null) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
        return fet;
    }

    private static Shape triangle(double x1, double y1, double x2, double y2, double x3, double y3) {
        java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
        p.moveTo(x1, y1);
        p.lineTo(x2, y2);
        p.lineTo(x3, y3);
        p.closePath();
        return p;
    }

    /**
     * [CA] «Paraula {>>}»: la paraula i les fletxes, tot centrat al botó, amb les
     * fletxes centrades en alçada amb les majúscules de la paraula (les lletres
     * «>» i «<» queden més amunt o més avall segons la font).
     * [EN] A word followed by one or two drawn chevrons, vertically centred on it.
     */
    private static boolean dibuixaRotulAmbFletxes(Graphics2D g, double x, double y, double w, float ty,
            String rotul, int k) {
        String fletxes = rotul.substring(k + 1, rotul.length() - 1);
        boolean dreta = fletxes.matches(">+");
        if (!dreta && !fletxes.matches("<+")) return false;
        String paraula = rotul.substring(0, k).trim();
        FontMetrics fm = g.getFontMetrics();
        double alcadaMajuscula = g.getFont().createGlyphVector(g.getFontRenderContext(), "H")
                .getVisualBounds().getHeight();
        double mida = g.getFont().getSize2D();
        double fw = alcadaMajuscula * 0.34 * FLETXA_PROPORCIO;   // amplada d'una fletxa
        double fh = alcadaMajuscula * 0.42 * FLETXA_PROPORCIO;   // mitja alçada
        double sep = fw * 0.9;                                    // entre dues fletxes
        double gruix = Math.max(1.2, mida / 9.0);
        double buit = fm.stringWidth(" ") * 0.8;
        int n = fletxes.length();
        if (paraula.isEmpty()) buit = 0;
        double total = fm.stringWidth(paraula) + buit + n * fw + (n - 1) * sep;
        double x0 = x + (w - total) / 2.0;
        if (!paraula.isEmpty()) g.drawString(paraula, (float) x0, ty);
        double cy = ty - alcadaMajuscula / 2.0;
        Object aa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        Stroke abans = g.getStroke();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke((float) gruix, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double fx = x0 + fm.stringWidth(paraula) + buit;
        for (int i = 0; i < n; i++) {
            double a = fx + i * (fw + sep);
            java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
            if (dreta) {
                p.moveTo(a, cy - fh);
                p.lineTo(a + fw, cy);
                p.lineTo(a, cy + fh);
            } else {
                p.moveTo(a + fw, cy - fh);
                p.lineTo(a, cy);
                p.lineTo(a + fw, cy + fh);
            }
            g.draw(p);
        }
        g.setStroke(abans);
        if (aa != null) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
        return true;
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
        Color tinta;
        if (sona && TECLA_RESSALTA) tinta = TEXT_TECLA_QUE_SONA;
        else if (!sona && TEXT_TECLA_SILENCI != null) tinta = TEXT_TECLA_SILENCI;
        else tinta = tintaDeNota(midi);
        g.setColor(tinta);
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
        if (TEXT_SUAVITZAT) {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
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
