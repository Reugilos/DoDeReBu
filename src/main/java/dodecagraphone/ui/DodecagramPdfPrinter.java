/*
 * PolyForm Noncommercial License 1.0.0
 * Copyright (c) 2024-2026 Pau Bofill. Powered by Claude AI.
 * Full license / Llicència completa: LICENSE (project root / arrel del projecte)
 */
package dodecagraphone.ui;

import dodecagraphone.MyController;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.color.ColorSets;
import dodecagraphone.model.component.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * [CA] Impressió de partiture en format PDF. Genera un document A4 multipàgina
 * a partir de les imatges offscreen de la graella de notes, la línia d'acords
 * i la lletra. Afegeix línies vectorials de compàs i beat, doble barra final i
 * teclat estret a l'esquerra de cada fila.
 * <p>
 * [EN] Score printing to PDF format. Generates a multi-page A4 document from
 * the offscreen images of the note grid, chord line and lyrics. Adds vector
 * measure and beat lines, a double final bar and a narrow keyboard strip on
 * the left of each row.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class DodecagramPdfPrinter {

    private final MyController controller;
    private final PdfDrawKit   pdf = new PdfDrawKit(
            new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD),
            new PDType1Font(Standard14Fonts.FontName.HELVETICA));

    private static final float PAGE_W          = PDRectangle.A4.getWidth();
    private static final float PAGE_H          = PDRectangle.A4.getHeight();
    private static final float MARGIN          = 18f;
    private static final float FIRST_HEADER_H  = 44f;  // capçalera base (1 línia de desc.)
    private static final float DESC_LINE_H     = 11f;  // descriptionSize(9) + gap(2)
    private static final float SHORT_HEADER_H  = 18f;
    private static final float ROW_GAP         = 6f;
    private static final float MEASURE_LABEL_H = 10f;
    private static final int   TARGET_ROWS     = 4;
    /**
     * [CA] Files d'un dodecagrama en blanc, quan s'imprimeix una partitura
     * sense notes. Coincideix amb {@link #TARGET_ROWS}: són les files que la
     * distribució de pàgina ja mira d'encabir en una sola plana.
     * <p>
     * [EN] Number of rows of a blank dodecagram, when printing a score with no
     * notes. It matches {@link #TARGET_ROWS}: those are the rows the page
     * layout already aims to fit on a single page.
     */
    public static final int    BLANK_ROWS      = 4;
    /** Max row image height as fraction of usable page height (single-row case). */
    private static final float MAX_ROW_FRAC    = 0.5f;
    /**
     * [CA] Estirament vertical maxim de la franja de lletra al PDF. La fila es
     * dibuixa amb escala no uniforme (amplada a scaleX, alcada a scaleY, menor
     * quan calen mes files per pagina), i el text hi sortia aixafat. La lletra
     * es dibuixa estirada per compensar-ho, fins a aquest limit.
     * <p>
     * [EN] Maximum vertical stretch of the lyrics band in the PDF. The row is
     * drawn with a non-uniform scale (width at scaleX, height at scaleY, which
     * is smaller once more rows per page are needed), which flattened the text.
     * The lyrics are drawn stretched to compensate, up to this limit.
     */
    private static final float MAX_LYRICS_STRETCH = 2.5f;

    /**
     * [CA] Crea un nou DodecagramPdfPrinter per al controlador especificat.
     * <p>
     * [EN] Creates a new DodecagramPdfPrinter for the given controller.
     *
     * @param controller [CA] Controlador principal de l'app / [EN] Main application controller
     */
    public DodecagramPdfPrinter(MyController controller) {
        this.controller = controller;
    }

    /**
     * [CA] Files del dodecagrama en blanc que s'ha de generar; 0 vol dir
     * impressió normal de la partitura.
     * <p>
     * [EN] Rows of the blank dodecagram to be generated; 0 means a normal
     * score printout.
     */
    private int blankRows = 0;

    /**
     * [CA] Genera un dodecagrama en blanc de {@link #BLANK_ROWS} files. És per a
     * la partitura buida, on {@link #print(File)} només trauria la pàgina única
     * que marca {@code stopCol}.
     * <p>
     * Amb {@code dropInitialMarks} fals, la pàgina porta el tempo, la tonalitat,
     * el volum i la transposició vigents (les marques de la columna 0); amb cert
     * no porta res, que és el paper pautat genèric. El PDF copia l'offscreen de
     * la franja d'acords, o sigui que amagar-les allà és el que les treu de la
     * pàgina; el {@code finally} les torna i redibuixa la pantalla.
     * <p>
     * [EN] Generates a blank dodecagram of {@link #BLANK_ROWS} rows. Meant for
     * the empty score, where {@link #print(File)} would only output the single
     * page given by {@code stopCol}.
     * <p>
     * With {@code dropInitialMarks} false the page carries the current tempo,
     * key, volume and transposition (the column 0 marks); with true it carries
     * none, which is the generic staff paper. The PDF copies the chord band
     * offscreen, so hiding them there is what removes them from the page; the
     * {@code finally} brings them back and redraws the screen.
     *
     * @param outputFile       [CA] Fitxer de sortida PDF / [EN] Output PDF file
     * @param dropInitialMarks [CA] cert per treure les marques de la columna 0 /
     *                         [EN] true to drop the column 0 marks
     * @throws IOException [CA] Si falla la creació o l'escriptura del PDF /
     *                     [EN] If creating or writing the PDF fails
     */
    public void printBlank(File outputFile, boolean dropInitialMarks) throws IOException {
        MyChordSymbolLine chordLine = controller.getMyChordSymbolLine();
        blankRows = BLANK_ROWS;
        if (dropInitialMarks) chordLine.setHideInitialMarks(true);
        try {
            print(outputFile);
        } finally {
            blankRows = 0;
            if (dropInitialMarks) {
                chordLine.setHideInitialMarks(false);
                controller.redrawChordLine();
            }
        }
    }

    /**
     * [CA] Genera el fitxer PDF al camí indicat. Divideix la partitura en
     * files de mida fixa, les compon com a imatges rasteritzades i afegeix
     * les línies vectorials de compàs, beat i doble barra.
     * <p>
     * [EN] Generates the PDF file at the given path. Splits the score into
     * fixed-size rows, composes them as rasterized images and adds vector
     * measure, beat and double-bar lines.
     *
     * @param outputFile [CA] Fitxer de sortida PDF / [EN] Output PDF file
     * @throws IOException [CA] Si falla la creació o l'escriptura del PDF /
     *                     [EN] If creating or writing the PDF fails
     */
    public void print(File outputFile) throws IOException {
        MyAllPurposeScore score = controller.getAllPurposeScore();
        MyChordSymbolLine chordLine = controller.getMyChordSymbolLine();
        MyLyrics lyrics = controller.getMyLyrics();

        double colWidthF   = Settings.getColWidth();
        int colWidthPx     = (int) Math.max(1, Math.round(colWidthF));
        int colsPerMeasure = score.getBaseColsPerMeasure();
        if (colsPerMeasure <= 0) colsPerMeasure = 1;

        int fixedCols = score.getFixedColsPerPage();
        if (fixedCols <= 0) fixedCols = colsPerMeasure;
        fixedCols = Math.max(colsPerMeasure, (fixedCols / colsPerMeasure) * colsPerMeasure);

        int fixedSlicePx = (int) Math.round(fixedCols * colWidthF);
        int keyWidthPx   = 4 * colWidthPx;

        // El buffer offscreen d'una partitura nova només cobreix un parell de
        // pàgines, i les files del dodecagrama en blanc se'n retallen: s'ha
        // d'ampliar ABANS de redibuixar-lo.
        if (blankRows > 0) {
            ensureBufferFor(score, chordLine, lyrics, blankRows * fixedCols + 1);
            // El buffer d'acords no es regenera sol; el dodecagrama en blanc
            // necessita un dibuix fresc, que és on es decideix si hi van les
            // marques inicials o no.
            chordLine.setNeedsDrawing(true);
        }

        controller.getCam().drawFullCamInOffscreen();

        BufferedImage gridImg   = score.getOffscreenImage();
        BufferedImage chordImg  = chordLine.getOffscreenImage();
        BufferedImage lyricsImg = lyrics.getOffscreenImage();

        if (gridImg == null || chordImg == null) return;

        // En blanc no hi ha contingut que doni stopCol: són les files
        // demanades, topades pel que el buffer hagi pogut créixer.
        int stopCol = (blankRows > 0)
                ? Math.min(blankRows * fixedCols, score.getNColsBuffer())
                : score.getStopCol();
        if (stopCol <= 0) return;

        // Precompute beat/measure boundaries (lines are now screen-space in draw(), not in offscreen)
        boolean[] isBeat    = new boolean[stopCol + 2];
        boolean[] isMeasure = new boolean[stopCol + 2];
        score.computeBeatMeasureLines(stopCol + 2, isBeat, isMeasure);

        // Amb fit-anacrusis, la primera fila mostra un compàs extra (anacrusis comprimida).
        // Un dodecagrama en blanc no té anacrusa: totes les files són iguals.
        boolean fitFirstRow = blankRows == 0
                && Settings.isFitAnacrusis() && Settings.isHasAnacrusis();
        int firstRowExtraCols = fitFirstRow ? colsPerMeasure : 0;
        int firstRowCols   = fixedCols + firstRowExtraCols;
        int firstSlicePx   = (int) Math.round(firstRowCols * colWidthF);

        // Compute rows first so total count drives the row-height calculation
        List<int[]> rows = new ArrayList<>();
        if (fitFirstRow) {
            rows.add(new int[]{0, firstRowCols});
            for (int c = firstRowCols; c < stopCol; c += fixedCols) {
                rows.add(new int[]{c, c + fixedCols});
            }
        } else {
            for (int c = 0; c < stopCol; c += fixedCols) {
                rows.add(new int[]{c, c + fixedCols});
            }
        }
        if (rows.isEmpty()) return;
        int totalRows = rows.size();

        // PDF layout
        float pdfUsableW = PAGE_W - 2 * MARGIN;
        String desc = nullSafe(score.getDescription());
        int descLineCount = desc.isEmpty() ? 0 : pdf.wrapLines(desc, pdfUsableW).size();
        float firstHeaderH = FIRST_HEADER_H + Math.max(0, descLineCount - 1) * DESC_LINE_H;
        float scaleX     = pdfUsableW / (keyWidthPx + fixedSlicePx);
        float availFirst = PAGE_H - 2 * MARGIN - firstHeaderH;
        int   chordH     = chordImg.getHeight();
        int   gridH      = gridImg.getHeight();
        int   lyricsSrcH = (lyricsImg != null) ? lyricsImg.getHeight() : 0;
        int   scoreRowH0 = chordH + gridH + lyricsSrcH;
        if (scoreRowH0 <= 0) return;

        // Dynamic row height: use fewer "target rows" when content is small so rows grow.
        // Cap at MAX_ROW_FRAC of available height (e.g. half page for a single row).
        int   effectiveTarget = Math.max(1, Math.min(totalRows, TARGET_ROWS));
        float maxRowImgH = (availFirst + ROW_GAP) / effectiveTarget - ROW_GAP - MEASURE_LABEL_H;
        maxRowImgH = Math.min(maxRowImgH, availFirst * MAX_ROW_FRAC - MEASURE_LABEL_H);

        // Quant s'aixafa la fila: scaleY < scaleX vol dir compressio vertical.
        // La franja de lletra es dibuixa estirada per aquest mateix factor, o
        // sigui que despres de la compressio el text torna a la seva proporcio.
        // L'alcada de la fila al paper esta topada per maxRowImgH, o sigui que
        // el que hi guanya la lletra ho perden les altres franges: com que la
        // lletra son 3 files de ~61, el cost per a la graella es d'un 2-3%.
        float scaleY0    = (maxRowImgH > 0) ? Math.min(scaleX, maxRowImgH / scoreRowH0) : scaleX;
        float lyricsStretch = (scaleY0 > 0)
                ? Math.min(MAX_LYRICS_STRETCH, Math.max(1f, scaleX / scaleY0))
                : 1f;
        int   lyricsH    = Math.round(lyricsSrcH * lyricsStretch);
        int   scoreRowH  = chordH + gridH + lyricsH;
        float scaleY     = (maxRowImgH > 0) ? Math.min(scaleX, maxRowImgH / scoreRowH) : scaleX;
        float keyPdfW    = keyWidthPx * scaleX;
        float rowImgPdfH = scoreRowH * scaleY;
        float rowTotalH  = MEASURE_LABEL_H + rowImgPdfH;

        int rowsPerFirst = Math.max(1, (int) ((availFirst + ROW_GAP) / (rowTotalH + ROW_GAP)));
        int rowsPerOther = Math.max(1,
                (int) ((PAGE_H - 2 * MARGIN - SHORT_HEADER_H + ROW_GAP) / (rowTotalH + ROW_GAP)));

        BufferedImage keyImg = renderNarrowKeyboard(colWidthPx, gridH);

        try (PDDocument doc = new PDDocument()) {
            PDPage pdfPage = null;
            PDPageContentStream cs = null;
            int pdfPageNum = 0;
            int rowOnPage  = 0;
            float yPos     = 0;

            for (int i = 0; i < rows.size(); i++) {
                int startCol = rows.get(i)[0];
                int endCol   = rows.get(i)[1];

                boolean needNewPage = (pdfPage == null)
                        || (pdfPageNum == 1 && rowOnPage >= rowsPerFirst)
                        || (pdfPageNum > 1  && rowOnPage >= rowsPerOther);

                if (needNewPage) {
                    if (cs != null) cs.close();
                    pdfPage = new PDPage(PDRectangle.A4);
                    doc.addPage(pdfPage);
                    pdfPageNum++;
                    rowOnPage = 0;
                    yPos = PAGE_H - MARGIN;
                    cs = new PDPageContentStream(doc, pdfPage);
                    pdf.setContentStream(cs);

                    if (pdfPageNum == 1) {
                        pdf.drawTitle(MARGIN, yPos - 16, nullSafe(score.getTitle()));
                        pdf.drawAuthor(MARGIN, yPos - 30, nullSafe(score.getAuthor()));
                        if (!desc.isEmpty())
                            pdf.drawDescription(MARGIN, yPos - 42, desc, pdfUsableW);
                        yPos -= firstHeaderH;
                    } else {
                        pdf.drawPageHeader(PAGE_W - MARGIN, yPos - 12,
                                nullSafe(score.getTitle()) + "  p." + pdfPageNum);
                        yPos -= SHORT_HEADER_H;
                    }
                }

                int rowSlicePx  = (i == 0) ? firstSlicePx : fixedSlicePx;
                float rowScaleX = pdfUsableW / (keyWidthPx + rowSlicePx);
                int startPx = (int) Math.round(startCol * colWidthF);
                int endPx   = Math.min(startPx + rowSlicePx, gridImg.getWidth());
                int sliceW  = Math.max(0, endPx - startPx);

                // Only draw score content up to stopCol; blank the rest.
                int contentCols = Math.min(endCol, stopCol) - startCol;
                int contentPx   = (int) Math.round(contentCols * colWidthF);
                int drawSliceW  = Math.max(0, Math.min(sliceW, contentPx));

                BufferedImage rowImg = composeRow(keyImg, chordImg, gridImg, lyricsImg,
                        keyWidthPx, startPx, drawSliceW, rowSlicePx, scoreRowH, chordH, gridH,
                        lyricsH, lyricsSrcH);

                // Les marques de la columna 0 no s'hi dibuixen aqui: print() crida
                // drawFullCamInOffscreen(), que ja les ha pintades a la imatge
                // offscreen de la banda d'acords. Abans s'hi tornaven a passar per
                // sobre i quedaven dibuixades dues vegades al mateix lloc.

                float yImgBottom = yPos - rowTotalH;
                float yImgTop    = yImgBottom + rowImgPdfH;

                // Measure number label (above the image, below the top border)
                pdf.drawMeasureLabel(MARGIN + keyPdfW, yImgTop + 2,
                        score.getMeasureAndBeatAt(startCol)[0]);

                // Rasterised row image
                PDImageXObject pdImg = PDImageXObject.createFromByteArray(
                        doc, toBytes(rowImg), "row" + i);
                cs.drawImage(pdImg, MARGIN, yImgBottom, pdfUsableW, rowImgPdfH);

                // Thin border around the image
                pdf.drawImageBorder(MARGIN, yImgBottom, pdfUsableW, rowImgPdfH);

                // Horizontal band separators as sharp PDF vector lines (full width, incl. keyboard col).
                // PDF y=0 is at the bottom; image pixel row p maps to PDF y = yImgTop - p*scaleY.
                if (chordH > 0 && gridH > 0) {
                    float sepY = yImgTop - chordH * scaleY;  // chord/grid boundary
                    pdf.drawBandSeparator(MARGIN, MARGIN + pdfUsableW, sepY);
                }
                if (lyricsH > 0) {
                    float sepY = yImgBottom + lyricsH * scaleY;  // grid/lyrics boundary
                    pdf.drawBandSeparator(MARGIN, MARGIN + pdfUsableW, sepY);
                }

                // Beat and measure vertical lines as PDF vectors
                // (they are drawn in screen-space in draw(), so absent from the offscreen bitmap)
                // El bucle comença a startCol (no startCol+1) per incloure la primera measureLine
                // de cada fila, que coincideix amb el límit dret de la columna de teclat.
                {
                    int limitCol = Math.min(endCol, stopCol);
                    for (int col = startCol; col <= limitCol; col++) {
                        if (col >= isBeat.length) break;
                        if (!isBeat[col] && !isMeasure[col]) continue;
                        float lineX = MARGIN + (float) ((keyWidthPx + (col - startCol) * colWidthF) * rowScaleX);
                        if (lineX <= MARGIN || lineX >= MARGIN + pdfUsableW) continue;
                        if (isMeasure[col])
                            pdf.drawMeasureLine(lineX, yImgBottom, rowImgPdfH);
                        else
                            pdf.drawBeatLine(lineX, yImgBottom, rowImgPdfH);
                    }
                }

                // Double bar at stopCol (un dodecagrama en blanc no té final)
                if (blankRows == 0 && startCol < stopCol && stopCol <= endCol) {
                    float stopXPdf = MARGIN + (float) ((keyWidthPx + (stopCol - startCol) * colWidthF) * rowScaleX);
                    pdf.drawDoubleBar(stopXPdf, yImgBottom, rowImgPdfH);
                }

                yPos = yImgBottom - ROW_GAP;
                rowOnPage++;
            }

            if (cs != null) cs.close();
            // Esborra el fitxer existent (l'usuari ja ha confirmat la sobreescriptura)
            // perquè PDFBox no emeti el fals warning "overwriting existing file".
            if (outputFile.exists()) outputFile.delete();
            doc.save(outputFile);
        }
    }

    /**
     * [CA] Amplia els buffers offscreen de les tres franges fins a
     * {@code neededCols} columnes, si encara no hi arriben. És el mateix que fa
     * la navegació en passar pàgina; aquí cal perquè el dodecagrama en blanc
     * demana més columnes de les que té el buffer d'una partitura nova.
     * <p>
     * [EN] Grows the offscreen buffers of the three bands up to
     * {@code neededCols} columns, if they do not reach it yet. It is what
     * paging does when navigating; here it is needed because the blank
     * dodecagram asks for more columns than a new score's buffer holds.
     *
     * @param score      [CA] partitura activa / [EN] active score
     * @param chordLine  [CA] franja d'acords / [EN] chord band
     * @param lyrics     [CA] franja de lletra / [EN] lyrics band
     * @param neededCols [CA] columnes que hi han de cabre / [EN] columns that must fit
     */
    private void ensureBufferFor(MyAllPurposeScore score, MyChordSymbolLine chordLine,
            MyLyrics lyrics, int neededCols) {
        if (score.getNColsBuffer() >= neededCols) return;
        score.resizeOffscreen(neededCols);
        chordLine.resizeOffscreen(neededCols);
        lyrics.resizeOffscreen(neededCols);
    }

    // -----------------------------------------------------------------------
    //  Row image composition
    // -----------------------------------------------------------------------

    private BufferedImage composeRow(BufferedImage keyImg, BufferedImage chordImg,
            BufferedImage gridImg, BufferedImage lyricsImg,
            int keyW, int startPx, int sliceW, int fullSliceW,
            int totalH, int chordH, int gridH, int lyricsH, int lyricsSrcH) {
        int rowImgW = keyW + fullSliceW;
        BufferedImage row = new BufferedImage(rowImgW, totalH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = row.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, rowImgW, totalH);

        // Narrow keyboard (covers only the grid rows)
        if (keyImg != null && keyW > 0) {
            int keyH = Math.min(keyImg.getHeight(), gridH);
            g.drawImage(keyImg, 0, chordH, keyW, chordH + keyH,
                        0, 0, keyImg.getWidth(), keyH, null);
        }

        if (sliceW > 0) {
            // Chord band
            int cSrcW = Math.min(sliceW, Math.max(0, chordImg.getWidth() - startPx));
            int cSrcH = Math.min(chordH, chordImg.getHeight());
            if (cSrcW > 0 && cSrcH > 0)
                g.drawImage(chordImg, keyW, 0, keyW + cSrcW, cSrcH,
                        startPx, 0, startPx + cSrcW, cSrcH, null);

            // Grid band
            int gSrcW = Math.min(sliceW, Math.max(0, gridImg.getWidth() - startPx));
            int gSrcH = Math.min(gridH, gridImg.getHeight());
            if (gSrcW > 0 && gSrcH > 0)
                g.drawImage(gridImg, keyW, chordH, keyW + gSrcW, chordH + gSrcH,
                        startPx, 0, startPx + gSrcW, gSrcH, null);

            // Lyrics band: l'origen es l'alcada natural de la franja i la
            // destinacio la estirada, per compensar la compressio vertical de la
            // fila. Interpolacio bilineal perque el text no quedi dentat.
            if (lyricsImg != null && lyricsH > 0) {
                int lSrcW = Math.min(sliceW, Math.max(0, lyricsImg.getWidth() - startPx));
                int lSrcH = Math.min(lyricsSrcH, lyricsImg.getHeight());
                if (lSrcW > 0 && lSrcH > 0) {
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g.drawImage(lyricsImg, keyW, chordH + gridH,
                            keyW + lSrcW, chordH + gridH + lyricsH,
                            startPx, 0, startPx + lSrcW, lSrcH, null);
                }
            }
        }

        // Band separators are drawn as PDF vector lines in print(); omit from the bitmap.

        g.dispose();
        return row;
    }

    /**
     * [CA] Renderitza la franja de teclat estret (key colours i indicator de slide).
     * Les tecles seleccionades (en l'escala/elecció activa) es desplacen a la dreta;
     * la franja de slide ocupa la part esquerra.
     * <p>
     * [EN] Renders the narrow keyboard strip (key colours and slide indicator).
     * Selected keys (in the current scale/choice) are pushed to the right;
     * the slide strip occupies the left portion.
     *
     * @param colWidthPx [CA] Amplada d'una columna en píxels / [EN] Column width in pixels
     * @param gridH      [CA] Alçada de la graella en píxels / [EN] Grid height in pixels
     * @return [CA] Imatge del teclat estret, o null si les dimensions no són vàlides /
     *         [EN] Narrow keyboard image, or null if dimensions are invalid
     */
    private BufferedImage renderNarrowKeyboard(int colWidthPx, int gridH) {
        MyAllPurposeScore score = controller.getAllPurposeScore();
        MyXiloKeyboard keyboard = controller.getKeyboard();
        int nKeys = score.getnKeys();
        if (nKeys <= 0 || gridH <= 0 || colWidthPx <= 0) return null;

        int w = 4 * colWidthPx;
        BufferedImage img = new BufferedImage(w, gridH, BufferedImage.TYPE_INT_RGB);
        DrawKit dk = new DrawKit(img.createGraphics());
        dk.clearBackground(0, 0, w, gridH);

        double rowH      = (double) gridH / nKeys;
        boolean showChoice = keyboard != null && keyboard.isShowChoice();
        Color slideColor = ColorSets.getEncesColor(ColorSets.LINIA_PENTA);
        double keyFrac   = Settings.KEY_WIDTH_REDUCTION;
        double slideFrac = 1.0 - keyFrac;

        for (int keyId = 0; keyId < nKeys; keyId++) {
            int midi = ToneRange.keyIdToMidi(keyId);
            Color keyColor = ColorSets.getEncesColor(midi % 12);
            if (keyColor == null) keyColor = Color.LIGHT_GRAY;

            int y     = (int) Math.round(keyId * rowH);
            int nextY = (int) Math.round((keyId + 1) * rowH);
            int h     = Math.max(1, nextY - y - 1);

            if (showChoice) {
                boolean selected = keyboard.findIfSelected(midi);
                int slideW = (int) Math.round(slideFrac * w);
                int keyW2  = w - slideW;
                if (selected) {
                    dk.fillCell(0, y, slideW, h, slideColor);
                    dk.fillCell(slideW, y, keyW2, h, keyColor);
                } else {
                    dk.fillCell(0, y, keyW2, h, keyColor);
                    dk.fillCell(keyW2, y, slideW, h, slideColor);
                }
            } else {
                dk.fillCell(0, y, w, h, keyColor);
            }
        }

        // Tonality triangle (same logic as MyXiloKey.draw)
        if (!controller.isDrumsMode()) {
            int midiKey = score.getMidiKey();
            for (int keyId = 0; keyId < nKeys; keyId++) {
                int midi = ToneRange.keyIdToMidi(keyId);
                if (midi % 12 != midiKey % 12) continue;
                int y     = (int) Math.round(keyId * rowH);
                int nextY = (int) Math.round((keyId + 1) * rowH);
                int h     = Math.max(1, nextY - y - 1);
                int triH  = Math.max(5, (int)(h * 0.55));
                int cy    = y + h / 2;
                dk.drawRightArrow(w - 2, cy, triH, triH, ColorSets.getGridSquareFontColor(midi));
            }
        }

        // Right-edge separator between keyboard and score
        dk.drawVerticalLine(w - 1, 0, gridH - 1, 1.0f, Color.BLACK);
        dk.getGraphics().dispose();
        return img;
    }

    private static String nullSafe(String s) {
        return (s != null) ? s : "";
    }

    private static byte[] toBytes(BufferedImage img) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "PNG", baos);
            return baos.toByteArray();
        }
    }
}
