/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.ui;

import dodecagraphone.MyController;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;

/**
 * [CA] Exportació de la partitura a format SVG (i en el futur PDF). Utilitza
 * la biblioteca Apache Batik per generar un {@link SVGGraphics2D} sobre el
 * qual es dibuixa la partitura amb el mètode estàndard
 * {@link MyController#redraw(java.awt.Graphics2D)}. El resultat es desa en un
 * fitxer SVG codificat en UTF-8.
 * <p>
 * [EN] Score export to SVG (and in the future PDF) format. Uses the Apache
 * Batik library to generate an {@link SVGGraphics2D} on which the score is
 * drawn via the standard {@link MyController#redraw(java.awt.Graphics2D)}
 * method. The result is saved to a UTF-8 encoded SVG file.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class SVGandPDF {
        private MyController controller;

        /**
         * [CA] Crea una nova instància associada al controlador principal.
         * <p>
         * [EN] Creates a new instance associated with the main controller.
         *
         * @param contr [CA] controlador principal / [EN] main controller
         */
        public SVGandPDF(MyController contr){
            controller = contr;
        }

//        public void printSvg(File outSvg, int widthPx, int heightPx) throws IOException {
//		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
//		String svgNS = "http://www.w3.org/2000/svg";
//		Document document = domImpl.createDocument(svgNS, "svg", null);
//
//		SVGGraphics2D svgG = new SVGGraphics2D(document);
//		svgG.setSVGCanvasSize(new Dimension(widthPx, heightPx));
//
//		// Opcional: millora l'aspecte de línies/text
//		svgG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		svgG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//
//		// Si el teu draw assumeix un fons, pinta'l aquí
//		// svgG.setPaint(Color.WHITE);
//		// svgG.fillRect(0, 0, widthPx, heightPx);
//
//		// Dibuixa directament en vector
//                MyScreen screen = controller.getScreen();
//                MyGridScore score = controller.getAllPurposeScore();
//                MyXiloKeyboard keyb = controller.getKeyboard();
//                MyCamera cam = controller.getCam();
//                controller.setPrinting(true);
//		// screen.draw(svgG);
//                score.draw(svgG);
//                keyb.draw(svgG);
//                cam.drawPlayBar(svgG);
//
//		boolean useCSS = true; // true = estils en CSS; false = atributs inline
//		try (Writer out = new OutputStreamWriter(new FileOutputStream(outSvg), StandardCharsets.UTF_8)) {
//			svgG.stream(out, useCSS);
//		}
//	}

        /**
         * [CA] Exporta la vista actual de la partitura a un fitxer SVG.
         * Activa el mode d'impressió del controlador durant el dibuix i
         * el restaura en finalitzar (fins i tot en cas d'error).
         * <p>
         * [EN] Exports the current score view to an SVG file. Activates the
         * controller's printing mode during drawing and restores it when done
         * (even on error).
         *
         * @param outSvg   [CA] fitxer de sortida SVG / [EN] output SVG file
         * @param widthPx  [CA] amplada en píxels del llenç SVG /
         *                 [EN] SVG canvas width in pixels
         * @param heightPx [CA] alçada en píxels del llenç SVG /
         *                 [EN] SVG canvas height in pixels
         * @throws IOException [CA] si hi ha un error d'E/S en escriure el fitxer /
         *                     [EN] if an I/O error occurs while writing the file
         */
	public void printSvg(File outSvg, int widthPx, int heightPx) throws IOException {
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument(svgNS, "svg", null);

		SVGGraphics2D svgG = new SVGGraphics2D(document);
		svgG.setSVGCanvasSize(new Dimension(widthPx, heightPx));

		svgG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		svgG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		boolean useCSS = true;
		Shape oldClip = svgG.getClip();
		controller.setPrinting(true);
		try {
			svgG.setClip(0, 0, widthPx, heightPx);
			controller.setNeedsDrawing(true);
			controller.redraw(svgG);
			try (Writer out = new OutputStreamWriter(new FileOutputStream(outSvg), StandardCharsets.UTF_8)) {
				svgG.stream(out, useCSS);
			}
		} finally {
			svgG.setClip(oldClip);
			controller.setPrinting(false);
		}
	}

//    public void printPdf(File outPdf, int widthPx, int heightPx) throws Exception {
//        // 1) Genera SVG temporal
//        File tmpSvg = File.createTempFile("print_", ".svg");
//        try {
//            printSvg(tmpSvg, widthPx, heightPx);
//
//            // 2) Converteix SVG -> PDF
//            PDFTranscoder t = new PDFTranscoder();
//            try ( InputStream in = new FileInputStream(tmpSvg);  OutputStream out = new FileOutputStream(outPdf)) {
//
//                TranscoderInput input = new TranscoderInput(in);
//                TranscoderOutput output = new TranscoderOutput(out);
//                t.transcode(input, output);
//            }
//        } finally {
//            // neteja
//            if (tmpSvg.exists()) {
//                tmpSvg.delete();
//            }
//        }
//    }
}
