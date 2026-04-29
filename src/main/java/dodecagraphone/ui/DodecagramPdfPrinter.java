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
    private static final float PAGE_W         = PDRectangle.A4.getWidth();
    private static final float PAGE_H         = PDRectangle.A4.getHeight();
    private static final float MARGIN         = 18f;
    private static final float FIRST_HEADER_H = 44f;
    private static final float SHORT_HEADER_H = 18f;
    private static final float ROW_GAP        = 6f;
    private static final float MEASURE_LABEL_H = 10f;
    private static final float BORDER_W       = 0.3f;
    private static final float DOUBLE_BAR_GAP = 1f;
    private static final int   TARGET_ROWS    = 4;

    public DodecagramPdfPrinter(MyController controller) {
        this.controller = controller;
    }

    public void print(File outputFile) throws IOException {
        MyAllPurposeScore score = controller.getAllPurposeScore();
        MyChordSymbolLine chordLine = controller.getMyChordSymbolLine();
        MyLyrics lyrics = controller.getMyLyrics();
        controller.getCam().drawFullCamInOffscreen();

        BufferedImage gridImg   = score.getOffscreenImage();
        BufferedImage chordImg  = chordLine.getOffscreenImage();
        BufferedImage lyricsImg = lyrics.getOffscreenImage();

        if (gridImg == null || chordImg == null) return;

        int stopCol = score.getStopCol();
        if (stopCol <= 0) return;

        // Use floating-point column width to avoid pixel drift across rows
        double colWidthF   = Settings.getColWidth();
        int colWidthPx     = (int) Math.max(1, Math.round(colWidthF));
        int colsPerMeasure = score.getBaseColsPerMeasure();
        if (colsPerMeasure <= 0) colsPerMeasure = 1;

        // fixedCols = exact number of score columns per PDF row (multiple of colsPerMeasure)
        int fixedCols = score.getFixedColsPerPage();
        if (fixedCols <= 0) fixedCols = colsPerMeasure;
        fixedCols = Math.max(colsPerMeasure, (fixedCols / colsPerMeasure) * colsPerMeasure);

        // Pixel width of one row slice (using float to stay aligned with offscreen rendering)
        int fixedSlicePx = (int) Math.round(fixedCols * colWidthF);
        int keyWidthPx   = 4 * colWidthPx;

        // PDF layout
        float pdfUsableW = PAGE_W - 2 * MARGIN;
        float scaleX     = pdfUsableW / (keyWidthPx + fixedSlicePx);
        float availFirst = PAGE_H - 2 * MARGIN - FIRST_HEADER_H;
        int   chordH     = chordImg.getHeight();
        int   gridH      = gridImg.getHeight();
        int   lyricsH    = (lyricsImg != null) ? lyricsImg.getHeight() : 0;
        int   scoreRowH  = chordH + gridH + lyricsH;
        if (scoreRowH <= 0) return;

        float maxRowImgH = (availFirst + ROW_GAP) / TARGET_ROWS - ROW_GAP - MEASURE_LABEL_H;
        float scaleY     = (maxRowImgH > 0) ? Math.min(scaleX, maxRowImgH / scoreRowH) : scaleX;
        float keyPdfW    = keyWidthPx * scaleX;
        float rowImgPdfH = scoreRowH * scaleY;
        float rowTotalH  = MEASURE_LABEL_H + rowImgPdfH;

        int rowsPerFirst = TARGET_ROWS;
        int rowsPerOther = Math.max(TARGET_ROWS,
                (int) ((PAGE_H - 2 * MARGIN - SHORT_HEADER_H + ROW_GAP) / (rowTotalH + ROW_GAP)));

        // Row boundaries: each row covers exactly fixedCols score columns
        List<int[]> rows = new ArrayList<>();
        for (int c = 0; c < stopCol; c += fixedCols) {
            rows.add(new int[]{c, c + fixedCols});
        }
        if (rows.isEmpty()) return;

        BufferedImage keyImg = renderNarrowKeyboard(colWidthPx, gridH);

        PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDFont fontNorm = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

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

                // Pixel slice from offscreen (float-aligned to avoid measure drift)
                int startPx = (int) Math.round(startCol * colWidthF);
                int endPx   = Math.min(startPx + fixedSlicePx, gridImg.getWidth());
                int sliceW  = Math.max(0, endPx - startPx);

                BufferedImage rowImg = composeRow(keyImg, chordImg, gridImg, lyricsImg,
                        keyWidthPx, startPx, sliceW, fixedSlicePx, scoreRowH, chordH, gridH, lyricsH);

                // Overlay col-0 initial markers on the first row
                if (startCol == 0) {
                    Graphics2D g2 = rowImg.createGraphics();
                    g2.translate(keyWidthPx, 0);
                    chordLine.drawInitialMarkersAt(g2);
                    g2.dispose();
                }

                float yImgBottom = yPos - rowTotalH;
                float yImgTop    = yImgBottom + rowImgPdfH;   // = yPos - MEASURE_LABEL_H

                // Measure number label (above the chord band, below the border top)
                drawRowStartLabel(cs, fontNorm, score, startCol,
                        MARGIN + keyPdfW, yImgTop + 2);

                // Rasterised row image
                PDImageXObject pdImg = PDImageXObject.createFromByteArray(
                        doc, toBytes(rowImg), "row" + i);
                cs.drawImage(pdImg, MARGIN, yImgBottom, pdfUsableW, rowImgPdfH);

                // Thin border that wraps only the image content (not the label area)
                cs.setLineWidth(BORDER_W);
                cs.setStrokingColor(Color.BLACK);
                cs.addRect(MARGIN, yImgBottom, pdfUsableW, rowImgPdfH);
                cs.stroke();

                // Double bar at stopCol if it falls within this row
                if (startCol < stopCol && stopCol <= endCol) {
                    float stopXPdf = MARGIN + (float)((keyWidthPx + (stopCol - startCol) * colWidthF) * scaleX);
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
    //  Row image composition
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

        // Narrow keyboard (covers only the grid rows, not chord/lyrics)
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

            // Lyrics band
            if (lyricsImg != null && lyricsH > 0) {
                int lSrcW = Math.min(sliceW, Math.max(0, lyricsImg.getWidth() - startPx));
                int lSrcH = Math.min(lyricsH, lyricsImg.getHeight());
                if (lSrcW > 0 && lSrcH > 0)
                    g.drawImage(lyricsImg, keyW, chordH + gridH,
                            keyW + lSrcW, chordH + gridH + lSrcH,
                            startPx, 0, startPx + lSrcW, lSrcH, null);
            }
        }

        // Thin horizontal separators between bands
        g.setColor(Color.BLACK);
        if (chordH > 0 && gridH > 0)
            g.drawLine(0, chordH, rowImgW - 1, chordH);
        if (lyricsH > 0)
            g.drawLine(0, chordH + gridH, rowImgW - 1, chordH + gridH);

        g.dispose();
        return row;
    }

    // -----------------------------------------------------------------------
    //  PDF drawing helpers
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

    /** Double bar: thin line + 1pt gap + normal measure-line weight. */
    private void drawDoubleBar(PDPageContentStream cs,
            float x, float yBottom, float h) throws IOException {
        cs.setStrokingColor(Color.BLACK);
        cs.setLineWidth(0.75f);
        cs.moveTo(x, yBottom);
        cs.lineTo(x, yBottom + h);
        cs.stroke();
        cs.setLineWidth(1.5f);
        cs.moveTo(x + DOUBLE_BAR_GAP, yBottom);
        cs.lineTo(x + DOUBLE_BAR_GAP, yBottom + h);
        cs.stroke();
    }

    /** Narrow keyboard strip: shows note-row colors using the same palette as the UI. */
    private BufferedImage renderNarrowKeyboard(int colWidthPx, int gridH) {
        MyAllPurposeScore score = controller.getAllPurposeScore();
        int nKeys = score.getnKeys();
        if (nKeys <= 0 || gridH <= 0 || colWidthPx <= 0) return null;
        int w = 4 * colWidthPx;
        BufferedImage img = new BufferedImage(w, gridH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, gridH);
        double rowH = (double) gridH / nKeys;
        for (int keyId = 0; keyId < nKeys; keyId++) {
            int midi = ToneRange.keyIdToMidi(keyId);
            Color c = ColorSets.getEncesColor(midi % 12);
            if (c == null) c = Color.LIGHT_GRAY;
            int y     = (int) Math.round(keyId * rowH);
            int nextY = (int) Math.round((keyId + 1) * rowH);
            int h     = Math.max(1, nextY - y - 1);
            g.setColor(c);
            g.fillRect(0, y, w, h);
        }
        // Right-edge separator
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
