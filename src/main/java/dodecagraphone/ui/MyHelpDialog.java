package dodecagraphone.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class MyHelpDialog {

    private static JDialog dialog;

    public static void show(Frame owner, String anchor) {
        if (dialog != null && dialog.isVisible()) {
            scrollTo(anchor);
            dialog.toFront();
            return;
        }

        dialog = new JDialog(owner, I18n.t("help.dialog.title"), false);
        dialog.setLayout(new BorderLayout());

        JEditorPane pane = new JEditorPane("text/html", buildHtml());
        pane.setEditable(false);
        pane.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(pane);
        dialog.add(scroll, BorderLayout.CENTER);

        JButton closeBtn = new JButton(I18n.t("btn.ok"));
        closeBtn.addActionListener(e -> dialog.dispose());
        javax.swing.JPanel south = new javax.swing.JPanel();
        south.add(closeBtn);
        dialog.add(south, BorderLayout.SOUTH);

        dialog.setPreferredSize(new Dimension(660, 520));
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);

        SwingUtilities.invokeLater(() -> {
            pane.scrollToReference(anchor);
        });
    }

    private static void scrollTo(String anchor) {
        if (dialog == null) return;
        JScrollPane scroll = (JScrollPane) ((java.awt.BorderLayout) dialog.getContentPane().getLayout())
                .getLayoutComponent(BorderLayout.CENTER);
        if (scroll == null) return;
        JEditorPane pane = (JEditorPane) scroll.getViewport().getView();
        if (pane != null) {
            SwingUtilities.invokeLater(() -> pane.scrollToReference(anchor));
        }
    }

    private static String buildHtml() {
        String bg      = "#f8f8f8";
        String hdrBg   = "#2a6099";
        String hdrFg   = "#ffffff";
        String secBg   = "#dce8f5";
        String secFg   = "#1a3a5c";
        String codeBg  = "#e8e8e8";
        String border  = "#b0c8e8";

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style>")
          .append("body{font-family:SansSerif;font-size:12px;background:").append(bg).append(";margin:10px;}")
          .append("h1{background:").append(hdrBg).append(";color:").append(hdrFg).append(";padding:6px 10px;margin:0 0 12px 0;font-size:15px;}")
          .append("h2{background:").append(secBg).append(";color:").append(secFg).append(";padding:4px 8px;margin:14px 0 6px 0;font-size:13px;border-left:4px solid ").append(border).append(";}")
          .append("table{border-collapse:collapse;width:100%;margin-bottom:8px;}")
          .append("td{padding:3px 8px;vertical-align:top;}")
          .append("td.key{width:38%;font-family:monospace;background:").append(codeBg).append(";border-radius:3px;white-space:nowrap;}")
          .append("tr:nth-child(even) td{background:#eeeeee;}")
          .append("tr:nth-child(even) td.key{background:#d8d8d8;}")
          .append("</style></head><body>");

        sb.append("<h1>").append(I18n.t("help.dialog.title")).append("</h1>");

        appendSection(sb, "grid",      "help.section.grid.title",      helpGrid());
        appendSection(sb, "selection", "help.section.selection.title",  helpSelection());
        appendSection(sb, "columns",   "help.section.columns.title",    helpColumns());
        appendSection(sb, "pattern",   "help.section.pattern.title",    helpPattern());
        appendSection(sb, "playback",  "help.section.playback.title",   helpPlayback());
        appendSection(sb, "mixer",     "help.section.mixer.title",      helpMixer());
        appendSection(sb, "export",    "help.section.export.title",     helpExport());

        sb.append("</body></html>");
        return sb.toString();
    }

    private static void appendSection(StringBuilder sb, String anchor, String titleKey, String[][] rows) {
        sb.append("<a name=\"").append(anchor).append("\"></a>");
        sb.append("<h2>").append(I18n.t(titleKey)).append("</h2>");
        sb.append("<table>");
        for (String[] row : rows) {
            sb.append("<tr><td class=\"key\">").append(row[0])
              .append("</td><td>").append(row[1]).append("</td></tr>");
        }
        sb.append("</table>");
    }

    private static String[][] helpGrid() {
        return new String[][] {
            { I18n.t("help.grid.enter.key"),       I18n.t("help.grid.enter.desc") },
            { I18n.t("help.grid.delete.key"),      I18n.t("help.grid.delete.desc") },
            { I18n.t("help.grid.split.key"),       I18n.t("help.grid.split.desc") },
            { I18n.t("help.grid.move.key"),        I18n.t("help.grid.move.desc") },
            { I18n.t("help.grid.undo.key"),        I18n.t("help.grid.undo.desc") },
            { I18n.t("help.grid.redo.key"),        I18n.t("help.grid.redo.desc") },
        };
    }

    private static String[][] helpSelection() {
        return new String[][] {
            { I18n.t("help.selection.select.key"),    I18n.t("help.selection.select.desc") },
            { I18n.t("help.selection.copy.key"),      I18n.t("help.selection.copy.desc") },
            { I18n.t("help.selection.cut.key"),       I18n.t("help.selection.cut.desc") },
            { I18n.t("help.selection.paste.key"),     I18n.t("help.selection.paste.desc") },
            { I18n.t("help.selection.replicate.key"), I18n.t("help.selection.replicate.desc") },
            { I18n.t("help.selection.replicateEnd.key"), I18n.t("help.selection.replicateEnd.desc") },
        };
    }

    private static String[][] helpColumns() {
        return new String[][] {
            { I18n.t("help.columns.insert.key"),  I18n.t("help.columns.insert.desc") },
            { I18n.t("help.columns.delete.key"),  I18n.t("help.columns.delete.desc") },
        };
    }

    private static String[][] helpPattern() {
        return new String[][] {
            { I18n.t("help.pattern.activate.key"),  I18n.t("help.pattern.activate.desc") },
            { I18n.t("help.pattern.types.key"),     I18n.t("help.pattern.types.desc") },
            { I18n.t("help.pattern.extend.key"),    I18n.t("help.pattern.extend.desc") },
            { I18n.t("help.pattern.tonality.key"),  I18n.t("help.pattern.tonality.desc") },
            { I18n.t("help.pattern.manual.key"),    I18n.t("help.pattern.manual.desc") },
        };
    }

    private static String[][] helpPlayback() {
        return new String[][] {
            { I18n.t("help.playback.play.key"),      I18n.t("help.playback.play.desc") },
            { I18n.t("help.playback.tempo.key"),     I18n.t("help.playback.tempo.desc") },
            { I18n.t("help.playback.volume.key"),    I18n.t("help.playback.volume.desc") },
            { I18n.t("help.playback.speed.key"),     I18n.t("help.playback.speed.desc") },
            { I18n.t("help.playback.transpose.key"), I18n.t("help.playback.transpose.desc") },
            { I18n.t("help.playback.tremolo.key"),   I18n.t("help.playback.tremolo.desc") },
        };
    }

    private static String[][] helpMixer() {
        return new String[][] {
            { I18n.t("help.mixer.open.key"),   I18n.t("help.mixer.open.desc") },
            { I18n.t("help.mixer.track.key"),  I18n.t("help.mixer.track.desc") },
            { I18n.t("help.mixer.instr.key"),  I18n.t("help.mixer.instr.desc") },
            { I18n.t("help.mixer.drums.key"),  I18n.t("help.mixer.drums.desc") },
        };
    }

    private static String[][] helpExport() {
        return new String[][] {
            { I18n.t("help.export.svg.key"),  I18n.t("help.export.svg.desc") },
            { I18n.t("help.export.pdf.key"),  I18n.t("help.export.pdf.desc") },
            { I18n.t("help.export.midi.key"), I18n.t("help.export.midi.desc") },
        };
    }
}
