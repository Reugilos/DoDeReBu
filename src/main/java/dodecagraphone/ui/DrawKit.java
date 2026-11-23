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
 * @version 5.0
 */
public final class DrawKit {

    private DrawKit() {}

    // ═════════════════════════════════════════════════════════════════════════
    //  TEMA
    // ═════════════════════════════════════════════════════════════════════════

    // Tots els valors surten del tema triat al config.properties (vegeu
    // {@link Tema}): els temes són fitxers de text a la carpeta «temes», i el
    // tema moni de dins del programa fa de reserva per a qualsevol valor que falti.
    // Es llegeixen una sola vegada, en carregar-se aquesta classe.

    private static Tema T;

    // ── Colors ───────────────────────────────────────────────────────────────

    private static Color FONS;
    private static Color TINTA;
    private static Color SEPARADOR_DE_FILA;
    /** [CA] {@code null} vol dir que el color el decideix ColorSets. */
    private static Color PAUTA_ESPAI;
    private static Color PAUTA_LINIA;
    private static Color PAUTA_DO_CENTRAL;
    /** [CA] Els mateixos valors per al PDF; {@code null} = com a la pantalla. */
    private static Color PDF_PAUTA_ESPAI;
    private static Color PDF_PAUTA_LINIA;
    private static Color PDF_PAUTA_DO_CENTRAL;
    private static Color PDF_SEPARADOR_DE_FILA;
    private static float PDF_SEPARADOR_GRUIX;

    /**
     * [CA] Cert mentre es prepara el buffer de la graella per al PDF: les files
     * buides agafen els colors {@code pdf.pauta.*} del tema i els separadors de fila
     * no es pinten al bitmap (el PDF els dibuixa com a línies vectorials, si el
     * tema en dóna un gruix). No es fa servir a la pantalla.
     */
    private static boolean modePdf = false;

    /** [CA] Activa o desactiva el mode PDF de la graella. / [EN] Toggles PDF grid mode. */
    public static void setModePdf(boolean on) { modePdf = on; }

    /** [CA] Si la graella es dibuixa ara per al PDF. / [EN] Whether the grid is being drawn for the PDF. */
    public static boolean isModePdf() { return modePdf; }

    /** [CA] El PDF dibuixa els separadors de fila com a vectors? / [EN] Vector row separators? */
    public static boolean separadorsPdfVectorials() { return PDF_SEPARADOR_GRUIX > 0f; }

    /** [CA] Gruix (punts PDF) dels separadors de fila al PDF. */
    public static float gruixSeparadorPdf() { return PDF_SEPARADOR_GRUIX; }

    /** [CA] Color dels separadors de fila al PDF; mai {@code null}. */
    public static Color colorSeparadorPdf() {
        return PDF_SEPARADOR_DE_FILA != null ? PDF_SEPARADOR_DE_FILA : SEPARADOR_DE_FILA;
    }
    private static Color PATRO_TRIAT;
    private static Color PATRO_FORA;
    private static Color TEXT_SOBRE_NOTA;
    /** [CA] {@code null}: el MI té el color de ColorSets. */
    private static Color NOTA_MI;
    /** [CA] {@code null}: les notes de la partitura van del color de la seva nota. Si no, totes van d'aquest. */
    private static Color NOTA_GRIS;
    /** [CA] Text de les notes de la graella quan van totes d'un sol color ({@code null} = el de sempre). */
    private static Color NOTA_GRIS_TEXT;
    /** [CA] Si una nota que sona a la graella agafa el seu color (en lloc del fons). */
    private static boolean NOTA_SONA_COLOR;
    /**
     * [CA] Com d'apagades es veuen les tecles <b>no seleccionades</b> del teclat
     * (les que no són a la plantilla triada): 0 = color ple, 1 = el color
     * {@code tecla.apaga.cap} (blanc per defecte; un gris fosc fa un color «apagat» que admet text blanc). Les seleccionades sempre van amb el color ple.
     */
    private static float TECLA_APAGA;
    /** [CA] El color cap al qual s'apaguen les tecles no seleccionades (el blanc, si el tema no en diu cap). */
    private static Color TECLA_APAGA_CAP;
    /** [CA] {@code null}: el text de les tecles en silenci és el de les notes. */
    private static Color TEXT_TECLA_SILENCI;
    /** [CA] {@code null}: la vora de la nota va del color del text de la nota. */
    private static Color VORA_DE_NOTA;
    private static int VORA_DE_NOTA_DESPLACAMENT;
    private static Color FONS_NOTA_PUNTEJADA;
    private static Color TEXT_NOTA_PUNTEJADA;
    private static Color SELECCIO_FARCIMENT;
    private static float SELECCIO_FARCIMENT_ALFA;
    private static Color SELECCIO_VORA;
    private static float SELECCIO_VORA_ALFA;
    private static Color SELECCIO_LLETRA_FARCIMENT;
    private static Color SELECCIO_LLETRA_VORA;
    private static Color MARCA_SELECCIONADA_FILET;
    private static Color MARCA_SELECCIONADA_MARC;
    private static Color LLETRA;
    private static Color LLETRA_EN_EDICIO;
    private static Color CURSOR_DE_TEXT;
    private static Color BOTO_DE_FORMAT;
    private static Color FONS_LINIA_DESTAT;
    private static Color TEXT_LINIA_DESTAT;
    private static Color PANELL_DE_BOTONS;
    private static Color BOTO_NORMAL;
    private static Color BOTO_PREMUT;
    private static Color TEXT_DE_BOTO;
    private static Color TEXT_DE_BOTO_PREMUT;
    private static int BOTO_ARC;
    private static int BOTO_MARGE;
    private static Color SLIDE;
    private static Color TEXT_TECLA_QUE_SONA;
    /** [CA] Si una tecla que sona es ressalta (fons blanc, text negre). Amb {@code no} es veu igual que en silenci. */
    private static boolean TECLA_RESSALTA;
    /** [CA] Tecla del PDF sense color a la paleta. / [EN] PDF key fallback. */
    private static Color TECLA_SENSE_COLOR;
    private static boolean TEXT_SUAVITZAT;

    // Les caixes d'Acords i de Lletra, a l'esquerra, per sobre i per sota del teclat
    private static Color CAIXA_ACORDS;
    private static Color CAIXA_LLETRA;
    private static Color CAIXA_VORA;
    private static Stroke TRAC_CAIXA;
    private static boolean CAIXA_AMB_VORA;

    // Els tips (les finestretes que expliquen un botó). Color null = el del sistema.
    private static Color TIP_FONS;
    private static Color TIP_TEXT;
    private static Color TIP_VORA;
    private static int TIP_VORA_GRUIX;
    private static int TIP_MARGE;
    private static int TIP_FONT_MIDA;
    private static double TIP_FONT_PROPORCIO;

    // Les icones dels botons (play, pausa, inici) i les fletxes de «Tò >>»
    private static double ICONA_PROPORCIO;
    private static double FLETXA_PROPORCIO;

    /**
     * [CA] Els quatre tipus de marca de canvi de la franja d'acords. El color de
     * cadascuna el posa el tema; el text pren el color que hi contrasti.
     * [EN] The four change-mark kinds; their colours come from the theme.
     * <p>
     * [CA] {@link #TRANSPOSICIO} està <b>en desús</b>: la franja ja no dibuixa la
     * marca de transposició (ara surt al botó de l'instrument, p. ex.
     * {@code Glock (24)}). Es conserva, amb la clau {@code marca.transposicio}
     * dels temes, per si la marca torna; ara no la fa servir res.
     * [EN] {@link #TRANSPOSICIO} is unused: the strip no longer draws it.
     */
    public enum Marca {
        TEMPO, TONALITAT, VOLUM, TRANSPOSICIO
    }

    /** [CA] Fons del carril de marques ({@code null} = cap). */
    private static Color MARQUES_FONS;
    private static Color MARCA_TEMPO;
    private static Color MARCA_TONALITAT;
    private static Color MARCA_VOLUM;
    /** [CA] En desús: la marca de transposició ja no es dibuixa (vegeu {@link Marca}). */
    private static Color MARCA_TRANSPOSICIO;

    private static Color fonsDeMarca(Marca m) {
        switch (m) {
            case TEMPO:     return MARCA_TEMPO;
            case TONALITAT: return MARCA_TONALITAT;
            case VOLUM:     return MARCA_VOLUM;
            default:        return MARCA_TRANSPOSICIO;
        }
    }

    // ── Traços ───────────────────────────────────────────────────────────────

    private static Stroke TRAC_COMPAS;
    /** [CA] La barra de compàs damunt de la pantalla, sobre la del buffer. */
    private static Stroke TRAC_COMPAS_PANTALLA;
    private static Stroke TRAC_TEMPS;
    private static Stroke TRAC_SEPARADOR_DE_FILA;
    private static Stroke TRAC_VORA_DE_NOTA;
    private static Stroke TRAC_DOBLE_BARRA_GRUIXUDA;
    private static Stroke TRAC_DOBLE_BARRA_PRIMA;
    private static int DOBLE_BARRA_SEPARACIO;
    private static Stroke TRAC_BARRA_DE_REPRODUCCIO;
    private static Stroke TRAC_MARCA_FILET;
    private static Stroke TRAC_MARCA_MARC;
    private static Stroke TRAC_CURSOR;

    /**
     * [CA] Llegeix tots els valors del tema actual. Es crida en carregar la classe i cada cop que
     * es canvia de tema ({@link #recarrega()}). Els camps no són {@code final} per poder-ho fer.
     * [EN] Reads every theme value; called at class load and on each theme switch.
     */
    private static void carrega() {
        T = Tema.actual();
        FONS = T.color("fons");
        TINTA = T.color("tinta");
        SEPARADOR_DE_FILA = T.color("graella.separador.color");
        PAUTA_ESPAI = T.color("pauta.espai");
        PAUTA_LINIA = T.color("pauta.linia");
        PAUTA_DO_CENTRAL = T.color("pauta.doCentral");
        PDF_PAUTA_ESPAI = T.color("pdf.pauta.espai");
        PDF_PAUTA_LINIA = T.color("pdf.pauta.linia");
        PDF_PAUTA_DO_CENTRAL = T.color("pdf.pauta.doCentral");
        PDF_SEPARADOR_DE_FILA = T.color("pdf.graella.separador.color");
        PDF_SEPARADOR_GRUIX = T.decimal("pdf.graella.separador.gruix");
        PATRO_TRIAT = T.color("patro.triat");
        PATRO_FORA = T.color("patro.fora");
        TEXT_SOBRE_NOTA = T.color("nota.text");
        NOTA_MI = T.color("nota.mi");
        NOTA_GRIS = T.color("nota.gris");
        NOTA_GRIS_TEXT = T.color("nota.gris.text");
        NOTA_SONA_COLOR = T.si("nota.sona.color");
        TECLA_APAGA = Math.max(0f, Math.min(1f, T.decimal("tecla.apaga")));
        TECLA_APAGA_CAP = T.color("tecla.apaga.cap");
        TEXT_TECLA_SILENCI = T.color("tecla.text");
        VORA_DE_NOTA = T.color("nota.vora.color");
        VORA_DE_NOTA_DESPLACAMENT = T.enter("nota.vora.desplacament");
        FONS_NOTA_PUNTEJADA = T.color("notaPuntejada.fons");
        TEXT_NOTA_PUNTEJADA = T.color("notaPuntejada.text");
        SELECCIO_FARCIMENT = T.color("seleccio.farciment");
        SELECCIO_FARCIMENT_ALFA = T.decimal("seleccio.farciment.alfa");
        SELECCIO_VORA = T.color("seleccio.vora");
        SELECCIO_VORA_ALFA = T.decimal("seleccio.vora.alfa");
        SELECCIO_LLETRA_FARCIMENT = T.color("seleccioLletra.farciment");
        SELECCIO_LLETRA_VORA = T.color("seleccioLletra.vora");
        MARCA_SELECCIONADA_FILET = T.color("marcaSeleccionada.filet");
        MARCA_SELECCIONADA_MARC = T.color("marcaSeleccionada.marc");
        LLETRA = T.color("lletra.text");
        LLETRA_EN_EDICIO = T.color("lletra.edicio");
        CURSOR_DE_TEXT = T.color("lletra.cursor");
        BOTO_DE_FORMAT = T.color("botoFormat.fons");
        FONS_LINIA_DESTAT = T.color("liniaEstat.fons");
        TEXT_LINIA_DESTAT = T.color("liniaEstat.text");
        PANELL_DE_BOTONS = T.color("panellBotons.fons");
        BOTO_NORMAL = T.color("boto.normal");
        BOTO_PREMUT = T.color("boto.premut");
        TEXT_DE_BOTO = T.color("boto.text");
        TEXT_DE_BOTO_PREMUT = T.color("boto.textPremut");
        BOTO_ARC = T.enter("boto.arrodoniment");
        BOTO_MARGE = T.enter("boto.marge");
        SLIDE = T.color("slide");
        TEXT_TECLA_QUE_SONA = T.color("tecla.textQueSona");
        TECLA_RESSALTA = T.si("tecla.ressalta");
        TECLA_SENSE_COLOR = T.color("tecla.senseColor");
        TEXT_SUAVITZAT = T.si("text.suavitzat");
        CAIXA_ACORDS = T.color("caixaAcords.fons");
        CAIXA_LLETRA = T.color("caixaLletra.fons");
        CAIXA_VORA = T.color("caixa.vora.color");
        TRAC_CAIXA = new BasicStroke(T.decimal("caixa.vora.gruix"));
        CAIXA_AMB_VORA = T.decimal("caixa.vora.gruix") > 0;
        TIP_FONS = T.color("tip.fons");
        TIP_TEXT = T.color("tip.text");
        TIP_VORA = T.color("tip.vora.color");
        TIP_VORA_GRUIX = T.enter("tip.vora.gruix");
        TIP_MARGE = T.enter("tip.marge");
        TIP_FONT_MIDA = T.enter("tip.font.mida");
        TIP_FONT_PROPORCIO = T.decimal("tip.font.proporcio");
        ICONA_PROPORCIO = T.decimal("icona.proporcio");
        FLETXA_PROPORCIO = T.decimal("fletxa.proporcio");
        MARQUES_FONS = T.color("marques.fons");
        MARCA_TEMPO = T.color("marca.tempo");
        MARCA_TONALITAT = T.color("marca.tonalitat");
        MARCA_VOLUM = T.color("marca.volum");
        MARCA_TRANSPOSICIO = T.color("marca.transposicio");
        TRAC_COMPAS = new BasicStroke(T.decimal("compas.gruix"));
        TRAC_COMPAS_PANTALLA = new BasicStroke(T.decimal("compas.pantalla.gruix"));
        TRAC_TEMPS = new BasicStroke(T.decimal("temps.gruix"), BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_BEVEL, 0, T.decimals("temps.guions"), 0);
        TRAC_SEPARADOR_DE_FILA = new BasicStroke(T.decimal("graella.separador.gruix"));
        TRAC_VORA_DE_NOTA = new BasicStroke(T.decimal("nota.vora.gruix"));
        TRAC_DOBLE_BARRA_GRUIXUDA = new BasicStroke(T.decimal("dobleBarra.gruixuda"));
        TRAC_DOBLE_BARRA_PRIMA = new BasicStroke(T.decimal("dobleBarra.prima"));
        DOBLE_BARRA_SEPARACIO = T.enter("dobleBarra.separacio");
        TRAC_BARRA_DE_REPRODUCCIO = new BasicStroke(T.decimal("reproduccio.gruix"));
        TRAC_MARCA_FILET = new BasicStroke(T.decimal("marcaSeleccionada.filet.gruix"));
        TRAC_MARCA_MARC = new BasicStroke(T.decimal("marcaSeleccionada.marc.gruix"));
        TRAC_CURSOR = new BasicStroke(T.decimal("lletra.cursor.gruix"));
        FAMILIA_ACORDS = familiaDisponible(T.text("font.acords"));
        ESTIL_ACORDS = T.estil("font.acords.estil");
        FAMILIA_LLETRA = familiaDisponible(T.text("font.lletra"));
        ESTIL_LLETRA = T.estil("font.lletra.estil");
        FAMILIA_INTERFICIE = familiaDisponible(T.text("font.interficie"));
        ESTIL_INTERFICIE = T.estil("font.interficie.estil");
        FONT_DE_TECLA_PER_ALCADA = T.decimal("tecla.font.proporcio");
        FONT_INTERFICIE_PROPORCIO = T.decimal("font.interficie.proporcio");
        FONT_ACORDS_PROPORCIO = T.decimal("font.acords.proporcio");
        FONT_MENUS_ESCALA = T.decimal("font.menus.escala");
        FONT_MENUS_MINIMA = T.decimal("font.menus.minima");
        FONT_MARQUES_PROPORCIO = T.decimal("font.marques.proporcio");
        FONT_DE_TECLA_MINIMA = T.enter("tecla.font.minima");
        TRIANGLET_MITJA_AMPLADA = T.enter("trianglet.mitjaAmplada");
        TRIANGLET_ALCADA = T.enter("trianglet.alcada");
        BOTO_DE_FORMAT_ARC = T.enter("botoFormat.arrodoniment");
    }

    static { carrega(); }

    /**
     * [CA] Torna a llegir el tema actual ({@link Tema#actual()}) i en recalcula tots els valors.
     * Qui el canvia ha de repintar-ho tot i tornar a calcular els colors guardats pels components.
     * [EN] Re-reads the current theme.
     */
    public static void recarrega() {
        carrega();
    }

    // ── Fonts ────────────────────────────────────────────────────────────────

    private static String FAMILIA_ACORDS;
    private static int ESTIL_ACORDS;
    private static String FAMILIA_LLETRA;
    private static int ESTIL_LLETRA;
    private static String FAMILIA_INTERFICIE;
    private static int ESTIL_INTERFICIE;
    /** [CA] La lletra d'una tecla és una fracció de la seva alçada. */
    private static double FONT_DE_TECLA_PER_ALCADA;
    private static double FONT_INTERFICIE_PROPORCIO;
    private static double FONT_ACORDS_PROPORCIO;

    /** [CA] Mida de la lletra de la interfície (botons, estat, títols) per a una fila d'alçada {@code rowH}. */
    public static int midaDeLletraDeInterficie(double rowH) {
        return Math.max(8, (int) (rowH * FONT_INTERFICIE_PROPORCIO));
    }

    private static double FONT_MENUS_ESCALA;
    private static double FONT_MENUS_MINIMA;

    /** Fonts que ja hem ampliat nosaltres (per no ampliar-les dues vegades). */
    private static final java.util.Set<Font> FONTS_AMPLIADES =
            java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());

    /**
     * [CA] Amplia les fonts de Swing (diàlegs, menús, selector de fitxers, desplegables…) per
     * l'escala {@code font.menus.escala} del tema. S'ha de cridar <b>després</b> de cada
     * {@code UIManager.setLookAndFeel}, que torna les fonts al seu valor; és idempotent: una font
     * que ja és una d'ampliades per aquí no es torna a ampliar. Amb escala 1 no fa res.
     * <p>
     * [EN] Enlarges Swing's UI fonts by the theme's {@code font.menus.escala}. Call it after each
     * {@code setLookAndFeel}; idempotent.
     */
    public static void ampliaFontsDeSwing() {
        if ((FONT_MENUS_ESCALA <= 0 || Math.abs(FONT_MENUS_ESCALA - 1.0) < 1e-9) && FONT_MENUS_MINIMA <= 0) return;
        javax.swing.UIDefaults defs = javax.swing.UIManager.getDefaults();
        // Mida de referència: la de l'etiqueta («Label.font», la dels diàlegs com «Canvia l'instrument»),
        // ampliada. Totes les fonts de la interfície es posen a aquesta mateixa mida: el sistema en
        // dona de diferents (7, 12, 15…) i uns diàlegs es veien més grossos que altres.
        Font etiqueta = defs.getFont("Label.font");
        float base = (etiqueta != null) ? etiqueta.getSize2D() : 12f;
        float ref = (float) Math.max(base * (FONT_MENUS_ESCALA > 0 ? FONT_MENUS_ESCALA : 1.0), FONT_MENUS_MINIMA);
        if (etiqueta != null && FONTS_AMPLIADES.contains(etiqueta)) ref = etiqueta.getSize2D();
        java.util.List<Object> claus = java.util.Collections.list(defs.keys());
        for (Object k : claus) {
            if (!(k instanceof String) || !((String) k).toLowerCase().endsWith("font")) continue;
            // El llistat de fitxers del selector i els tips ja tenien una mida bona: no es toquen.
            if ("FileChooser.listFont".equals(k) || "ToolTip.font".equals(k)) continue;
            Object v = defs.get(k);
            if (!(v instanceof Font)) continue;
            Font f = (Font) v;
            if (FONTS_AMPLIADES.contains(f)) continue;
            Font nova = new javax.swing.plaf.FontUIResource(f.deriveFont(ref));
            FONTS_AMPLIADES.add(nova);
            javax.swing.UIManager.put(k, nova);
        }
    }

    private static double FONT_MARQUES_PROPORCIO;

    /** [CA] Proporció de l'alçada d'una fila que fa la lletra de les marques de la franja superior. */
    public static double proporcioDeLletraDeMarques() {
        return FONT_MARQUES_PROPORCIO;
    }

    /** [CA] Proporció de l'alçada d'una fila que fa la lletra dels acords. */
    public static double proporcioDeLletraDAcords() {
        return FONT_ACORDS_PROPORCIO;
    }
    private static int FONT_DE_TECLA_MINIMA;

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

    private static int TRIANGLET_MITJA_AMPLADA;
    private static int TRIANGLET_ALCADA;
    private static int BOTO_DE_FORMAT_ARC;
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
     * [CA] Una cel·la amb la nota {@code midi} sonant. Amb {@code nota.sona.color = si}
     * (tema) pren el <b>color propi de la nota</b>, encara que la resta de notes de la
     * graella siguin d'un sol color ({@code nota.gris}); si no, el de fons de sempre.
     * [EN] A cell whose note is sounding: the note's own colour if the theme says so.
     */
    public static Color aparencaDeNotaQueSona(int midi) {
        return NOTA_SONA_COLOR ? colorDeNota(midi) : aparencaDeNotaQueSona();
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
        if (c != null && c.equals(ColorSets.getEncesColor(ColorSets.LINIA_PENTA)))
            delTema = (modePdf && PDF_PAUTA_LINIA != null) ? PDF_PAUTA_LINIA : PAUTA_LINIA;
        else if (c != null && c.equals(ColorSets.getEncesColor(ColorSets.DO_PENTA)))
            delTema = (modePdf && PDF_PAUTA_DO_CENTRAL != null) ? PDF_PAUTA_DO_CENTRAL : PAUTA_DO_CENTRAL;
        else
            delTema = (modePdf && PDF_PAUTA_ESPAI != null) ? PDF_PAUTA_ESPAI : PAUTA_ESPAI;
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
     * de la nota apagat (barrejat amb blanc segons {@code tecla.apaga}). Amb
     * {@code tecla.apaga = 0} (el classic) és igual que {@link #aparencaDeTecla}.
     * [EN] A silent key outside the chosen template: the note colour dimmed.
     */
    public static Color aparencaDeTeclaApagada(int midi) {
        Color c = colorDeNota(midi);
        if (c == null || TECLA_APAGA <= 0f) return c;
        Color cap = TECLA_APAGA_CAP != null ? TECLA_APAGA_CAP : Color.WHITE;
        return new Color(
                Math.round(c.getRed() + (cap.getRed() - c.getRed()) * TECLA_APAGA),
                Math.round(c.getGreen() + (cap.getGreen() - c.getGreen()) * TECLA_APAGA),
                Math.round(c.getBlue() + (cap.getBlue() - c.getBlue()) * TECLA_APAGA));
    }

    /** [CA] Cert si el tema ressalta (posa en blanc) les tecles que sonen. / [EN] True if sounding keys are highlighted white. */
    public static boolean teclaRessalta() {
        return TECLA_RESSALTA;
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
        // Amb un sol color per a totes les notes, el text és el del tema. Si cada nota té el seu
        // color i {@code nota.gris.text = colorsets}, el decideix ColorSets (negre sobre les notes
        // pàl·lides —re, ri, mi— i blanc sobre les altres); el nom de les tecles no hi passa.
        Color tinta;
        if (NOTA_GRIS != null && NOTA_GRIS_TEXT != null) tinta = NOTA_GRIS_TEXT;
        else if (NOTA_GRIS == null && NOTA_GRIS_TEXT == null) tinta = ColorSets.getGridSquareFontColor(midi);
        else tinta = tintaDeNota(midi);
        g.setColor(tinta);
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
        if (modePdf && PDF_SEPARADOR_GRUIX > 0f) return;
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
    public static int alcadaDeTrianglet() { return TRIANGLET_ALCADA; }

    /** [CA] Mitja amplada del trianglet d'atac (també la fletxeta de les marques). */
    public static int mitjaAmpladaDeTrianglet() { return TRIANGLET_MITJA_AMPLADA; }

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

    /**
     * [CA] El fons de la franja de marques (el carril de dalt de la franja
     * d'acords), si el tema en té ({@code marques.fons}); amb {@code colorsets}
     * no es pinta res.
     * [EN] Background of the marks lane, if the theme has one.
     */
    public static void franjaDeMarques(Graphics2D g, int x, int y, int w, int h) {
        if (MARQUES_FONS == null) return;
        g.setColor(MARQUES_FONS);
        g.fillRect(x, y, w, h);
    }

    /**
     * [CA] Dibuixa la figura del beat d'una marca de tempo com a forma vectorial:
     * una <b>negra</b> (cap ple lleugerament inclinat i plica) o, si
     * {@code puntejada}, una <b>negra amb punt</b>. S'usa perquè la tipografia dels
     * acords no té els glifs ♩/♪. Es pinta del color actual i en torna l'amplada.
     * {@code ascent} és l'alçada de la lletra, que en mana la mida.
     * [EN] Draws a quarter note (dotted if asked) as a shape; returns its width.
     */
    public static int simbolDeBeat(Graphics2D g, int x, int baseline, int ascent, boolean puntejada) {
        int hw = Math.max(5, Math.round(ascent * 0.62f));
        int hh = Math.max(4, Math.round(ascent * 0.44f));
        int cy = baseline - hh / 2 - 1;
        java.awt.geom.AffineTransform abansT = g.getTransform();
        g.rotate(Math.toRadians(-20), x + hw / 2.0, cy);
        g.fillOval(x, cy - hh / 2, hw, hh);
        g.setTransform(abansT);
        Stroke abans = g.getStroke();
        g.setStroke(new java.awt.BasicStroke(Math.max(1f, ascent / 9f)));
        int plicaX = x + hw - 1;
        g.drawLine(plicaX, cy, plicaX, baseline - Math.round(ascent * 0.95f));
        g.setStroke(abans);
        int w = hw + 2;
        if (puntejada) {
            int dd = Math.max(2, ascent / 6);
            g.fillOval(x + hw + 2, cy - dd / 2, dd, dd);
            w += 2 + dd;
        }
        return w;
    }

    /**
     * [CA] La línia horitzontal que separa el carril de marques dels acords, de
     * banda a banda de la franja, en la tinta del tema.
     * [EN] Horizontal line between the marks lane and the chords.
     */
    public static void liniaDeMarques(Graphics2D g, int x, int y, int w) {
        Color abans = g.getColor();
        g.setColor(TINTA);
        g.drawLine(x, y, x + w, y);
        g.setColor(abans);
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
        if (TIP_FONT_MIDA > 0) {
            tip.setFont(new Font(FAMILIA_INTERFICIE, Font.PLAIN, TIP_FONT_MIDA));
        } else if (TIP_FONT_PROPORCIO > 0) {
            // [CA] Creix amb la pantalla com la resta de textos (relativa a l'alçada d'una fila),
            // però mai per sota de la mida del sistema, que és la que ja va bé a les pantalles petites.
            Font base = tip.getFont();
            if (base != null) {
                int mida = (int) Math.round(Settings.getRowHeight() * TIP_FONT_PROPORCIO);
                if (mida > base.getSize()) tip.setFont(base.deriveFont((float) mida));
            }
        }
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
        nomDeTecla(g, nom, x, y, h, midi, sona, false);
    }

    /**
     * [CA] Com {@link #nomDeTecla(Graphics2D, String, double, double, double, int, boolean)},
     * però sabent si la tecla és <b>apagada</b> (no seleccionada, en silenci): llavors el
     * nom va del color {@code tecla.text} del tema, si en té.
     * [EN] Same, knowing whether the key is dimmed (unselected and silent).
     */
    public static void nomDeTecla(Graphics2D g, String nom, double x, double y, double h, int midi,
                                  boolean sona, boolean apagada) {
        int baseY = (int) (y + h * 0.8);
        Font abans = g.getFont();
        int mida = Math.max(FONT_DE_TECLA_MINIMA, (int) (h * FONT_DE_TECLA_PER_ALCADA));
        if (mida != abans.getSize()) g.setFont(abans.deriveFont((float) mida));
        Color tinta;
        if (sona && TECLA_RESSALTA) tinta = TEXT_TECLA_QUE_SONA;
        else if (!sona && apagada && TEXT_TECLA_SILENCI != null) tinta = TEXT_TECLA_SILENCI;
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
