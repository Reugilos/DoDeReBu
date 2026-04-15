package dodecagraphone.ui;

import dodecagraphone.MyController;
import dodecagraphone.model.component.MyCamera;
import dodecagraphone.model.component.MyGridScore;
import dodecagraphone.model.component.MyXiloKeyboard;
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
 *
 * @author gptgrogm
 */
public class SVGandPDF {
        private MyController controller;
        
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
	public void printSvg(File outSvg, int widthPx, int heightPx) throws IOException {
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument(svgNS, "svg", null);

		SVGGraphics2D svgG = new SVGGraphics2D(document);
		svgG.setSVGCanvasSize(new Dimension(widthPx, heightPx));

		svgG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		svgG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// Si vols fons blanc:
		// svgG.setPaint(Color.WHITE);
		// svgG.fillRect(0, 0, widthPx, heightPx);

		MyGridScore score = controller.getAllPurposeScore();
		MyXiloKeyboard keyb = controller.getKeyboard();
		MyCamera cam = controller.getCam();

		boolean useCSS = true;

		Shape oldClip = svgG.getClip();
		controller.setPrinting(true);
		try {
			// CLIP A TOT EL CANVAS (això talla qualsevol cosa que surti fora)
			svgG.setClip(0, 0, widthPx, heightPx);

			score.draw(svgG);
			keyb.draw(svgG);
			cam.drawPlayBar(svgG);

			try (Writer out = new OutputStreamWriter(new FileOutputStream(outSvg), StandardCharsets.UTF_8)) {
				svgG.stream(out, useCSS);
			}
		} finally {
			// neteja estat
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
