package dodecagraphone.ui;

import java.awt.Color;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * Capa semàntica sobre {@link PDPageContentStream} per a l'impressió PDF.
 * Agrupa tots els paràmetres d'estil (gruixos, fonts, mides) en camps públics
 * i ofereix mètodes amb noms significatius.
 *
 * Ús: crear una instància per document; cridar {@link #setContentStream(PDPageContentStream)}
 * cada cop que s'obre un nou {@code PDPageContentStream}.
 */
public class PdfDrawKit {

    // ── Content stream ────────────────────────────────────────────────────────

    private PDPageContentStream cs;

    // ── Fonts i mides de text ─────────────────────────────────────────────────

    public PDFont titleFont;
    public PDFont authorFont;
    public PDFont descriptionFont;
    public PDFont labelFont;
    public PDFont headerFont;

    public float titleSize       = 16f;
    public float authorSize      = 10f;
    public float descriptionSize = 9f;
    public float labelSize       = 7f;
    public float headerSize      = 10f;

    // ── Colors de text ────────────────────────────────────────────────────────

    public Color titleColor       = Color.BLACK;
    public Color authorColor      = Color.BLACK;
    public Color descriptionColor = Color.BLACK;
    public Color labelColor       = Color.DARK_GRAY;
    public Color headerColor      = Color.BLACK;

    // ── Gruixos de línia ──────────────────────────────────────────────────────

    public float measureLineWidth    = 0.75f;
    public float beatLineWidth       = 0.3f;
    public float bandSeparatorWidth  = 0.5f;
    public float imageBorderWidth    = 0.3f;
    public float doubleBarThinWidth  = 0.75f;
    public float doubleBarThickWidth = 1.5f;

    // ── Geometria ─────────────────────────────────────────────────────────────

    public float doubleBarGap = 1f;

    // ── Patrons de traç ───────────────────────────────────────────────────────

    public float[] beatDashPattern = {15f, 3f};

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @param titleFont  font per al títol (obligatoria)
     * @param bodyFont   font per a autor, descripció, etiquetes i capçalera
     */
    public PdfDrawKit(PDFont titleFont, PDFont bodyFont) {
        this.titleFont       = titleFont;
        this.authorFont      = bodyFont;
        this.descriptionFont = bodyFont;
        this.labelFont       = bodyFont;
        this.headerFont      = titleFont;
    }

    /** Actualitza el content stream actiu (cridar per cada pàgina nova). */
    public void setContentStream(PDPageContentStream cs) {
        this.cs = cs;
    }

    public PDPageContentStream getContentStream() {
        return cs;
    }

    // ── Text estructurat ──────────────────────────────────────────────────────

    /** Títol de la partitura (pàgina 1). */
    public void drawTitle(float x, float y, String text) throws IOException {
        drawText(titleFont, titleSize, titleColor, x, y, text);
    }

    /** Nom de l'autor (pàgina 1). */
    public void drawAuthor(float x, float y, String text) throws IOException {
        drawText(authorFont, authorSize, authorColor, x, y, text);
    }

    /** Descripció de la partitura (pàgina 1). */
    public void drawDescription(float x, float y, String text) throws IOException {
        drawText(descriptionFont, descriptionSize, descriptionColor, x, y, text);
    }

    /**
     * Capçalera de pàgina alineada a la dreta.
     * @param rightX coordenada X del marge dret
     */
    public void drawPageHeader(float rightX, float y, String text) throws IOException {
        if (text == null || text.isEmpty()) return;
        float approxW = text.length() * headerSize * 0.5f;
        drawText(headerFont, headerSize, headerColor, rightX - approxW, y, text);
    }

    /**
     * Número de compàs damunt d'una fila de partitura.
     * @param x coordenada X (just a la dreta de la tecla)
     * @param y coordenada Y (PDF, des de baix)
     * @param measure número de compàs a mostrar
     */
    public void drawMeasureLabel(float x, float y, int measure) throws IOException {
        drawText(labelFont, labelSize, labelColor, x, y, String.valueOf(measure));
    }

    // ── Línies verticals ──────────────────────────────────────────────────────

    /**
     * Línia de compàs (sòlida, gruixuda).
     */
    public void drawMeasureLine(float x, float yBottom, float h) throws IOException {
        cs.setLineWidth(measureLineWidth);
        cs.setLineDashPattern(new float[]{}, 0);
        cs.setStrokingColor(Color.BLACK);
        cs.moveTo(x, yBottom);
        cs.lineTo(x, yBottom + h);
        cs.stroke();
    }

    /**
     * Línia de temps (discontínua, prima).
     */
    public void drawBeatLine(float x, float yBottom, float h) throws IOException {
        cs.setLineWidth(beatLineWidth);
        cs.setLineDashPattern(beatDashPattern, 0);
        cs.setStrokingColor(Color.BLACK);
        cs.moveTo(x, yBottom);
        cs.lineTo(x, yBottom + h);
        cs.stroke();
        cs.setLineDashPattern(new float[]{}, 0);
    }

    // ── Línies horitzontals ───────────────────────────────────────────────────

    /**
     * Separador horitzontal entre franges (acords/notes/lletra).
     */
    public void drawBandSeparator(float x1, float x2, float y) throws IOException {
        cs.setLineWidth(bandSeparatorWidth);
        cs.setStrokingColor(Color.BLACK);
        cs.moveTo(x1, y);
        cs.lineTo(x2, y);
        cs.stroke();
    }

    // ── Rectangles ────────────────────────────────────────────────────────────

    /**
     * Vora fina al voltant d'una imatge de fila.
     */
    public void drawImageBorder(float x, float y, float w, float h) throws IOException {
        cs.setLineWidth(imageBorderWidth);
        cs.setStrokingColor(Color.BLACK);
        cs.addRect(x, y, w, h);
        cs.stroke();
    }

    // ── Formes compostes ──────────────────────────────────────────────────────

    /**
     * Doble barra final: línia prima seguida de línia gruixuda separades per {@link #doubleBarGap}.
     */
    public void drawDoubleBar(float x, float yBottom, float h) throws IOException {
        cs.setStrokingColor(Color.BLACK);
        cs.setLineDashPattern(new float[]{}, 0);
        cs.setLineWidth(doubleBarThinWidth);
        cs.moveTo(x, yBottom);
        cs.lineTo(x, yBottom + h);
        cs.stroke();
        cs.setLineWidth(doubleBarThickWidth);
        cs.moveTo(x + doubleBarGap, yBottom);
        cs.lineTo(x + doubleBarGap, yBottom + h);
        cs.stroke();
    }

    // ── Text de baix nivell ───────────────────────────────────────────────────

    /**
     * Dibuixa text en una posició absoluta (PDF coords).
     */
    public void drawText(PDFont font, float size, Color color,
            float x, float y, String text) throws IOException {
        if (text == null || text.isEmpty()) return;
        cs.setFont(font, size);
        cs.setNonStrokingColor(color);
        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(sanitize(text));
        cs.endText();
        cs.setNonStrokingColor(Color.BLACK);
    }

    // ── Utilitats ─────────────────────────────────────────────────────────────

    /**
     * Elimina caràcters fora de Latin-1 (que PDType1Font no pot codificar).
     */
    public static String sanitize(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            if (c < 256) sb.append(c);
            else sb.append('?');
        }
        return sb.toString();
    }
}
