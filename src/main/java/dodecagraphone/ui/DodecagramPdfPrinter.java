package dodecagraphone.ui;

import dodecagraphone.MyController;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.color.ColorSets;
import dodecagraphone.model.component.*;
import java.awt.Color;
import java.awt.Graphics2D;
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
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class DodecagramPdfPrinter {

    private final MyController controller;
    private static final float PAGE_W  = PDRectangle.A4.getWidth();
    private static final float PAGE_H  = PDRectangle.A4.getHeight();
    private static final float MARGIN         = 18f;
    private static final float FIRST_HEADER_H = 44f;
    private static final float SHORT_HEADER_H = 18f;
    private static final float ROW_GAP        = 6f;
    private static final float MEASURE_LABEL_H = 10f;
    private static final float BORDER_W       = 2f;
    private static final float DOUBLE_BAR_GAP = 3f;
    private static final int   TARGET_ROWS    = 4;

    public DodecagramPdfPrinter(MyController controller) {
        this.controller = controller;
    }

    public void print(File outputFile) throws IOException {
        MyAllPurposeScore score = controller.getAllPurposeScore();
        MyChordSymbolLine chordLine = controller.getMyChordSymbolLine();
        MyLyrics lyrics = controller.getMyLyrics();
        controller.getCam().drawFullCamInOffscreen();

        BufferedImage gridImg  = score.getOffscreenImage();
        BufferedImage chordImg = chordLine.getOffscreenImage();
        BufferedImage lyricsImg = lyrics.getOffscreenImage();

        if (gridImg == null || chordImg == null) return;

        int stopCol = score.getStopCol();
        if (stopCol <= 0) return;

        int colWidthPx    = (int) Math.max(1, Math.ceil(Settings.getColWidth()));
        int colsPerMeasure = score.getBaseColsPerMeasure();
        if (colsPerMeasure <= 0) colsPerMeasure = 1;

        // fixedCols = columnes per fila, arrodonit a múltiple de colsPerMeasure
        int fixedCols = score.getFixedColsPerPage();
        if (fixedCols <= 0) fixedCols = colsPerMeasure;
        fixedCols = Math.max(colsPerMeasure, (fixedCols / colsPerMeasure) * colsPerMeasure);

        // Cada fila comença en un múltiple de fixedCols i té exactament fixedCols columnes.
        // La última fila pot tenir espai buit si stopCol no és múltiple de fixedCols.
        List<int[]> rows = new ArrayList<>();
        for (int c = 0; c < stopCol; c += fixedCols) {
            rows.add(new int[]{c, c + fixedCols});
        }
        if (rows.isEmpty()) return;

        int chordH  = chordImg.getHeight();
        int gridH   = gridImg.getHeight();
        int lyricsH = (lyricsImg != null) ? lyricsImg.getHeight() : 0;
        int scoreRowH = chordH + gridH + lyricsH;
        if (scoreRowH <= 0) return;

        BufferedImage keyImg  = renderNarrowKeyboard(colWidthPx, gridH);
        int keyWidthPx        = 4 * colWidthPx;
        int fullSlicePx       = fixedCols * colWidthPx;
        float pdfUsableW      = PAGE_W - 2 * MARGIN;
        // scaleX omple l'ample de pàgina; scaleY comprimeix l'alçada per a TARGET_ROWS per pàgina
        float scaleX          = pdfUsableW / (keyWidthPx + fullSlicePx);
        float availFirst      = PAGE_H - 2 * MARGIN - FIRST_HEADER_H;
        float maxRowImgH      = (availFirst + ROW_GAP) / TARGET_ROWS - ROW_GAP - MEASURE_LABEL_H;
        float scaleY          = (scoreRowH > 0 && maxRowImgH > 0)
                                ? Math.min(scaleX, maxRowImgH / scoreRowH) : scaleX;
        float keyPdfW         = keyWidthPx * scaleX;
        float rowImgPdfH      = scoreRowH * scaleY;
        float rowTotalH       = MEASURE_LABEL_H + rowImgPdfH;

        int rowsPerFirst = TARGET_ROWS;
        int rowsPerOther = Math.max(TARGET_ROWS,
                (int) ((PAGE_H - 2 * MARGIN - SHORT_HEADER_H + ROW_GAP) / (rowTotalH + ROW_GAP)));

        PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDFont fontNorm = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        try (PDDocument doc = new PDDocument()) {
            PDPage pdfPage = null;
            PDPageContentStream cs = null;
            int pdfPageNum = 0;
            int rowOnPage  = 0;
            float yPos = 0;

            for (int i = 0; i < rows.size(); i++) {
                int startCol = rows.get(i)[0];
                int endCol   = rows.get(i)[1];

                boolean needNewPage = (pdfPage == null)
                        || (pdfPageNum == 1 && rowOnPage >= rowsPerFirst)
                        || (pdfPageNum > 1 && rowOnPage >= rowsPerOther);

                if (needNewPage) {
                    if (cs != null) cs.close();
                    pdfPage = new PDPage(PDRectangle.A4);
                    doc.addPage(pdfPage);
                    pdfPageNum++;
                    rowOnPage = 0;
                    yPos = PAGE_H - MARGIN;
                    cs = new PDPageContentStream(doc, pdfPage);

                    if (pdfPageNum == 1) {
                        drawText(cs, fontBold, 16, MARGIN, yPos - 16, nullSafe(score.getTitle()));
                        drawText(cs, fontNorm, 10, MARGIN, yPos - 30, nullSafe(score.getAuthor()));
                        String desc = nullSafe(score.getDescription());
                        if (!desc.isEmpty())
                            drawText(cs, fontNorm, 9, MARGIN, yPos - 42, desc);
                        yPos -= FIRST_HEADER_H;
                    } else {
                        drawTextRight(cs, fontBold, 10, PAGE_W - MARGIN, yPos - 12,
                                nullSafe(score.getTitle()) + "  p." + pdfPageNum);
                        yPos -= SHORT_HEADER_H;
                    }
                }

                // Píxels de la porció de partitura disponibles al buffer
                int startPx  = startCol * colWidthPx;
                int endPx    = Math.min(endCol * colWidthPx, gridImg.getWidth());
                int sliceW   = Math.max(0, endPx - startPx);

                // Imatge de la fila (sempre fullSlicePx d'amplada a la part de partitura)
                BufferedImage rowImg = composeRow(keyImg, chordImg, gridImg, lyricsImg,
                        keyWidthPx, startPx, sliceW, fullSlicePx, scoreRowH, chordH, gridH, lyricsH);

                // Marques inicials (col 0) a la primera fila
                if (startCol == 0) {
                    Graphics2D g2 = rowImg.createGraphics();
                    g2.translate(keyWidthPx, 0);
                    chordLine.drawInitialMarkersAt(g2);
                    g2.dispose();
                }

                float yImgBottom = yPos - rowTotalH;

                // Número de compàs al principi de la fila (sobre la chord band)
                drawRowStartLabel(cs, fontNorm, score, startCol,
                        MARGIN + keyPdfW, yPos - MEASURE_LABEL_H + 2);

                // Imatge rasteritzada de la fila
                PDImageXObject pdImg = PDImageXObject.createFromByteArray(
                        doc, toBytes(rowImg), "row" + i);
                cs.drawImage(pdImg, MARGIN, yImgBottom, pdfUsableW, rowImgPdfH);

                // Requadre de la fila (inclou l'àrea de l'etiqueta de compàs)
                cs.setLineWidth(BORDER_W);
                cs.setStrokingColor(Color.BLACK);
                cs.addRect(MARGIN, yImgBottom, pdfUsableW, rowTotalH);
                cs.stroke();

                // Doble barra al stopCol si cau dins d'aquesta fila
                if (startCol < stopCol && stopCol <= endCol) {
                    float stopXPdf = MARGIN + keyPdfW + (stopCol - startCol) * colWidthPx * scale;
                    drawDoubleBar(cs, stopXPdf, yImgBottom, rowImgPdfH);
                }

                yPos = yImgBottom - ROW_GAP;
                rowOnPage++;
            }

            if (cs != null) cs.close();
            doc.save(outputFile);
        }
    }

    // -----------------------------------------------------------------------
    //  Composició de la imatge de fila
    // -----------------------------------------------------------------------

    private BufferedImage composeRow(BufferedImage keyImg, BufferedImage chordImg,
            BufferedImage gridImg, BufferedImage lyricsImg,
            int keyW, int startPx, int sliceW, int fullSliceW,
            int totalH, int chordH, int gridH, int lyricsH) {
        int rowImgW = keyW + fullSliceW;
        BufferedImage row = new BufferedImage(rowImgW, totalH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = row.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, rowImgW, totalH);

        if (keyImg != null && keyW > 0) {
            int keyH = Math.min(keyImg.getHeight(), gridH);
            g.drawImage(keyImg, 0, chordH, keyW, chordH + keyH,
                        0, 0, keyImg.getWidth(), keyH, null);
        }

        if (sliceW > 0) {
            int chordSliceH = Math.min(chordH, chordImg.getHeight());
            int chordSliceW = Math.min(sliceW, chordImg.getWidth() - startPx);
            if (chordSliceW > 0 && chordSliceH > 0)
                g.drawImage(chordImg, keyW, 0, keyW + chordSliceW, chordSliceH,
                        startPx, 0, startPx + chordSliceW, chordSliceH, null);

            int gridSliceH = Math.min(gridH, gridImg.getHeight());
            int gridSliceW = Math.min(sliceW, gridImg.getWidth() - startPx);
            if (gridSliceW > 0 && gridSliceH > 0)
                g.drawImage(gridImg, keyW, chordH, keyW + gridSliceW, chordH + gridSliceH,
                        startPx, 0, startPx + gridSliceW, gridSliceH, null);

            if (lyricsImg != null && lyricsH > 0) {
                int lyricSliceH = Math.min(lyricsH, lyricsImg.getHeight());
                int lyricSliceW = Math.min(sliceW, lyricsImg.getWidth() - startPx);
                if (lyricSliceW > 0 && lyricSliceH > 0)
                    g.drawImage(lyricsImg, keyW, chordH + gridH,
                            keyW + lyricSliceW, chordH + gridH + lyricSliceH,
                            startPx, 0, startPx + lyricSliceW, lyricSliceH, null);
            }
        }

        g.dispose();
        return row;
    }

    // -----------------------------------------------------------------------
    //  Helpers de dibuix PDF
    // -----------------------------------------------------------------------

    private void drawRowStartLabel(PDPageContentStream cs, PDFont font,
            MyAllPurposeScore score, int startCol, float x, float y) throws IOException {
        int measure = score.getMeasureAndBeatAt(startCol)[0];
        cs.setFont(font, 7);
        cs.setNonStrokingColor(Color.DARK_GRAY);
        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(String.valueOf(measure));
        cs.endText();
        cs.setNonStrokingColor(Color.BLACK);
    }

    private void drawDoubleBar(PDPageContentStream cs,
            float x, float yBottom, float h) throws IOException {
        cs.setStrokingColor(Color.BLACK);
        cs.setLineWidth(2.5f);
        cs.moveTo(x, yBottom);
        cs.lineTo(x, yBottom + h);
        cs.stroke();
        cs.setLineWidth(1f);
        cs.moveTo(x + DOUBLE_BAR_GAP, yBottom);
        cs.lineTo(x + DOUBLE_BAR_GAP, yBottom + h);
        cs.stroke();
    }

    private BufferedImage renderNarrowKeyboard(int colWidthPx, int gridH) {
        MyAllPurposeScore score = controller.getAllPurposeScore();
        int nKeys = score.getnKeys();
        if (nKeys <= 0 || gridH <= 0 || colWidthPx <= 0) return null;
        int w = 4 * colWidthPx;
        BufferedImage img = new BufferedImage(w, gridH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, gridH);
        List<Integer> choiceList = score.getChoice().getChoiceList();
        boolean isPenta = score.isShowPentagramaStrips();
        double rowH = (double) gridH / nKeys;
        double barW    = w * 0.5;
        double indentX = w * 0.5;
        for (int keyId = 0; keyId < nKeys; keyId++) {
            int midi = ToneRange.keyIdToMidi(keyId);
            boolean inChoice = choiceList.contains(midi);
            Color c = isPenta ? ColorSets.getPentagramaColor(midi)
                              : ColorSets.getChoiceColor(midi, choiceList);
            if (c == null) c = Color.LIGHT_GRAY;
            int y = (int)(keyId * rowH);
            int h = Math.max(1, (int) Math.ceil(rowH) - 1);
            int x = inChoice ? (int) indentX : 0;
            g.setColor(c);
            g.fillRect(x, y, (int) Math.ceil(barW), h);
            g.setColor(Color.GRAY);
            g.drawLine(0, y + h, w - 1, y + h);
        }
        g.setColor(Color.BLACK);
        g.drawLine(w - 1, 0, w - 1, gridH - 1);
        g.dispose();
        return img;
    }

    private void drawText(PDPageContentStream cs, PDFont font, float size,
            float x, float y, String text) throws IOException {
        if (text == null || text.isEmpty()) return;
        cs.setFont(font, size);
        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(sanitize(text));
        cs.endText();
    }

    private void drawTextRight(PDPageContentStream cs, PDFont font, float size,
            float rightX, float y, String text) throws IOException {
        if (text == null || text.isEmpty()) return;
        float approxW = text.length() * size * 0.5f;
        drawText(cs, font, size, rightX - approxW, y, text);
    }

    private static String nullSafe(String s) {
        return (s != null) ? s : "";
    }

    private static String sanitize(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            if (c < 256) sb.append(c);
            else sb.append('?');
        }
        return sb.toString();
    }

    private static byte[] toBytes(BufferedImage img) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "PNG", baos);
            return baos.toByteArray();
        }
    }
}
