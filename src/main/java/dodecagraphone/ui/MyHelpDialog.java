package dodecagraphone.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;

public class MyHelpDialog {

    private static JDialog dialog;
    private static JEditorPane pane;

    public static void show(Frame owner, String anchor) {
        if (dialog != null && dialog.isVisible()) {
            scrollTo(anchor);
            dialog.toFront();
            return;
        }

        dialog = new JDialog(owner, I18n.t("help.dialog.title"), false);
        dialog.setLayout(new BorderLayout());

        pane = new JEditorPane("text/html", buildHtml());
        pane.setEditable(false);
        pane.setCaretPosition(0);

        pane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String desc = e.getDescription();
                if (desc != null) {
                    int idx = desc.indexOf('#');
                    if (idx >= 0) pane.scrollToReference(desc.substring(idx + 1));
                }
            }
        });

        JScrollPane scroll = new JScrollPane(pane);
        dialog.add(scroll, BorderLayout.CENTER);

        JButton closeBtn = new JButton(I18n.t("btn.ok"));
        closeBtn.addActionListener(e -> { dialog.dispose(); pane = null; });
        javax.swing.JPanel south = new javax.swing.JPanel();
        south.add(closeBtn);
        dialog.add(south, BorderLayout.SOUTH);

        dialog.setPreferredSize(new Dimension(720, 580));
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);

        SwingUtilities.invokeLater(() -> pane.scrollToReference(anchor));
    }

    private static void scrollTo(String anchor) {
        if (pane != null) {
            SwingUtilities.invokeLater(() -> pane.scrollToReference(anchor));
        }
    }

    private static String buildHtml() {
        String bg     = "#f8f8f8";
        String hdrBg  = "#2a6099";
        String hdrFg  = "#ffffff";
        String secBg  = "#dce8f5";
        String secFg  = "#1a3a5c";
        String codeBg = "#e8e8e8";
        String border = "#b0c8e8";
        String idxBg  = "#eef4fb";

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style>")
          .append("body{font-family:SansSerif;font-size:12px;background:").append(bg).append(";margin:10px;}")
          .append("h1{background:").append(hdrBg).append(";color:").append(hdrFg)
              .append(";padding:6px 10px;margin:0 0 10px 0;font-size:15px;}")
          .append("h2{background:").append(secBg).append(";color:").append(secFg)
              .append(";padding:4px 8px;margin:12px 0 4px 0;font-size:13px;border-left:4px solid ").append(border).append(";}")
          .append("table{border-collapse:collapse;width:100%;margin-bottom:4px;}")
          .append("td{padding:3px 8px;vertical-align:top;}")
          .append("td.key{width:36%;font-family:monospace;background:").append(codeBg)
              .append(";border-radius:3px;white-space:nowrap;}")
          .append("tr:nth-child(even) td{background:#eeeeee;}")
          .append("tr:nth-child(even) td.key{background:#d8d8d8;}")
          .append("div.idx{background:").append(idxBg).append(";border:1px solid ").append(border)
              .append(";padding:6px 12px;margin-bottom:12px;line-height:2.0;}")
          .append("a{color:").append(hdrBg).append(";text-decoration:none;}")
          .append("a:hover{text-decoration:underline;}")
          .append("p.note{margin:2px 0 4px 0;font-size:11px;color:#555;}")
          .append("</style></head><body>");

        sb.append("<h1>").append(I18n.t("help.dialog.title")).append("</h1>");

        // ── Index ──────────────────────────────────────────────────────────────
        String[][] sections = {
            {"grid",         "help.section.grid.title"},
            {"selection",    "help.section.selection.title"},
            {"columns",      "help.section.columns.title"},
            {"chordslyrics", "help.section.chordslyrics.title"},
            {"markers",      "help.section.markers.title"},
            {"xylokeyboard", "help.section.xylokeyboard.title"},
            {"pattern",      "help.section.pattern.title"},
            {"score",        "help.section.score.title"},
            {"key",          "help.section.key.title"},
            {"playback",     "help.section.playback.title"},
            {"view",         "help.section.view.title"},
            {"exercises",    "help.section.exercises.title"},
            {"mixer",        "help.section.mixer.title"},
            {"export",       "help.section.export.title"},
            {"config",       "help.section.config.title"},
        };
        sb.append("<a name=\"top\"></a><div class=\"idx\">");
        for (int i = 0; i < sections.length; i++) {
            if (i > 0) sb.append(" &nbsp;&middot;&nbsp; ");
            sb.append("<a href=\"#").append(sections[i][0]).append("\">")
              .append(I18n.t(sections[i][1])).append("</a>");
        }
        sb.append("</div>");

        // ── Sections ───────────────────────────────────────────────────────────
        appendSection(sb, "grid",         "help.section.grid.title",         "help.grid.intro",         helpGrid());
        appendSection(sb, "selection",    "help.section.selection.title",    "help.selection.intro",    helpSelection());
        appendSection(sb, "columns",      "help.section.columns.title",      "help.columns.intro",      helpColumns());
        appendSection(sb, "chordslyrics", "help.section.chordslyrics.title", "help.chordslyrics.intro", helpChordsLyrics());
        appendSection(sb, "markers",      "help.section.markers.title",      "help.markers.intro",      helpMarkers());
        appendSection(sb, "xylokeyboard", "help.section.xylokeyboard.title", "help.xylokeyboard.intro", helpXylokeyboard());
        appendSection(sb, "pattern",      "help.section.pattern.title",      "help.pattern.intro",      helpPattern());
        appendSection(sb, "score",        "help.section.score.title",        "help.score.intro",        helpScore());
        appendSection(sb, "key",          "help.section.key.title",          "help.key.intro",          helpKey());
        appendSection(sb, "playback",     "help.section.playback.title",     "help.playback.intro",     helpPlayback());
        appendSection(sb, "view",         "help.section.view.title",         "help.view.intro",         helpView());
        appendSection(sb, "exercises",    "help.section.exercises.title",    "help.exercises.intro",    helpExercises());
        appendSection(sb, "mixer",        "help.section.mixer.title",        "help.mixer.intro",        helpMixer());
        appendSection(sb, "export",       "help.section.export.title",       "help.export.intro",       helpExport());
        appendSectionConfig(sb);

        sb.append("</body></html>");
        return sb.toString();
    }

    private static void appendSection(StringBuilder sb, String anchor, String titleKey, String descKey, String[][] rows) {
        sb.append("<a name=\"").append(anchor).append("\"></a>");
        sb.append("<h2>").append(I18n.t(titleKey)).append("</h2>");
        if (descKey != null) sb.append("<p class=\"note\">").append(I18n.t(descKey)).append("</p>");
        sb.append("<table>");
        for (String[] row : rows) {
            sb.append("<tr><td class=\"key\">").append(row[0])
              .append("</td><td>").append(row[1]).append("</td></tr>");
        }
        sb.append("</table>");
        appendBackToIndex(sb);
    }

    private static void appendSectionConfig(StringBuilder sb) {
        sb.append("<a name=\"config\"></a>");
        sb.append("<h2>").append(I18n.t("help.section.config.title")).append("</h2>");
        sb.append("<p class=\"note\">").append(I18n.t("help.config.intro")).append("</p>");
        sb.append("<table>");
        for (String[] row : helpConfig()) {
            sb.append("<tr><td class=\"key\">").append(row[0])
              .append("</td><td>").append(row[1]).append("</td></tr>");
        }
        sb.append("</table>");
        appendBackToIndex(sb);
    }

    private static void appendBackToIndex(StringBuilder sb) {
        sb.append("<div style=\"text-align:right;font-size:10px;margin:2px 0 10px 0;\">")
          .append("<a href=\"#top\">").append(I18n.t("help.back.to.index")).append("</a>")
          .append("</div>");
    }

    private static String[][] helpGrid() {
        return new String[][] {
            { I18n.t("help.grid.enter.key"),  I18n.t("help.grid.enter.desc") },
            { I18n.t("help.grid.delete.key"), I18n.t("help.grid.delete.desc") },
            { I18n.t("help.grid.split.key"),  I18n.t("help.grid.split.desc") },
            { I18n.t("help.grid.move.key"),   I18n.t("help.grid.move.desc") },
            { I18n.t("help.grid.undo.key"),   I18n.t("help.grid.undo.desc") },
            { I18n.t("help.grid.redo.key"),   I18n.t("help.grid.redo.desc") },
        };
    }

    private static String[][] helpSelection() {
        return new String[][] {
            { I18n.t("help.selection.select.key"),       I18n.t("help.selection.select.desc") },
            { I18n.t("help.selection.copy.key"),         I18n.t("help.selection.copy.desc") },
            { I18n.t("help.selection.cut.key"),          I18n.t("help.selection.cut.desc") },
            { I18n.t("help.selection.paste.key"),        I18n.t("help.selection.paste.desc") },
            { I18n.t("help.selection.replicate.key"),    I18n.t("help.selection.replicate.desc") },
            { I18n.t("help.selection.replicateEnd.key"), I18n.t("help.selection.replicateEnd.desc") },
        };
    }

    private static String[][] helpColumns() {
        return new String[][] {
            { I18n.t("help.columns.insert.key"), I18n.t("help.columns.insert.desc") },
            { I18n.t("help.columns.delete.key"), I18n.t("help.columns.delete.desc") },
        };
    }

    private static String[][] helpChordsLyrics() {
        return new String[][] {
            { I18n.t("help.chords.add.key"),     I18n.t("help.chords.add.desc") },
            { I18n.t("help.chords.format.key"),  I18n.t("help.chords.format.desc") },
            { I18n.t("help.lyrics.edit.key"),    I18n.t("help.lyrics.edit.desc") },
            { I18n.t("help.lyrics.type.key"),    I18n.t("help.lyrics.type.desc") },
            { I18n.t("help.lyrics.advance.key"), I18n.t("help.lyrics.advance.desc") },
            { I18n.t("help.lyrics.exit.key"),    I18n.t("help.lyrics.exit.desc") },
        };
    }

    private static String[][] helpMarkers() {
        return new String[][] {
            { I18n.t("help.markers.timesig.key"), I18n.t("help.markers.timesig.desc") },
            { I18n.t("help.markers.key.key"),     I18n.t("help.markers.key.desc") },
            { I18n.t("help.markers.tempo.key"),   I18n.t("help.markers.tempo.desc") },
            { I18n.t("help.markers.volume.key"),  I18n.t("help.markers.volume.desc") },
            { I18n.t("help.markers.place.key"),   I18n.t("help.markers.place.desc") },
        };
    }

    private static String[][] helpXylokeyboard() {
        return new String[][] {
            { I18n.t("help.xylokeyboard.play.key"),   I18n.t("help.xylokeyboard.play.desc") },
            { I18n.t("help.xylokeyboard.slide.key"),  I18n.t("help.xylokeyboard.slide.desc") },
            { I18n.t("help.xylokeyboard.insert.key"), I18n.t("help.xylokeyboard.insert.desc") },
        };
    }

    private static String[][] helpPattern() {
        return new String[][] {
            { I18n.t("help.pattern.activate.key"), I18n.t("help.pattern.activate.desc") },
            { I18n.t("help.pattern.types.key"),    I18n.t("help.pattern.types.desc") },
            { I18n.t("help.pattern.extend.key"),   I18n.t("help.pattern.extend.desc") },
            { I18n.t("help.pattern.tonality.key"), I18n.t("help.pattern.tonality.desc") },
            { I18n.t("help.pattern.manual.key"),   I18n.t("help.pattern.manual.desc") },
        };
    }

    private static String[][] helpScore() {
        return new String[][] {
            { I18n.t("help.score.title.key"),       I18n.t("help.score.title.desc") },
            { I18n.t("help.score.author.key"),      I18n.t("help.score.author.desc") },
            { I18n.t("help.score.description.key"), I18n.t("help.score.description.desc") },
        };
    }

    private static String[][] helpKey() {
        return new String[][] {
            { I18n.t("help.key.nextprev.key"), I18n.t("help.key.nextprev.desc") },
            { I18n.t("help.key.reset.key"),    I18n.t("help.key.reset.desc") },
        };
    }

    private static String[][] helpPlayback() {
        return new String[][] {
            { I18n.t("help.playback.first.key"),        I18n.t("help.playback.first.desc") },
            { I18n.t("help.playback.prevnextpage.key"), I18n.t("help.playback.prevnextpage.desc") },
            { I18n.t("help.playback.prevnextcol.key"),  I18n.t("help.playback.prevnextcol.desc") },
            { I18n.t("help.playback.pagenum.key"),      I18n.t("help.playback.pagenum.desc") },
            { I18n.t("help.playback.play.key"),         I18n.t("help.playback.play.desc") },
            { I18n.t("help.playback.tempo.key"),        I18n.t("help.playback.tempo.desc") },
            { I18n.t("help.playback.volume.key"),       I18n.t("help.playback.volume.desc") },
            { I18n.t("help.playback.speed.key"),        I18n.t("help.playback.speed.desc") },
            { I18n.t("help.playback.transpose.key"),    I18n.t("help.playback.transpose.desc") },
            { I18n.t("help.playback.tremolo.key"),      I18n.t("help.playback.tremolo.desc") },
        };
    }

    private static String[][] helpView() {
        return new String[][] {
            { I18n.t("help.view.penta.key"),     I18n.t("help.view.penta.desc") },
            { I18n.t("help.view.keyboard.key"),  I18n.t("help.view.keyboard.desc") },
            { I18n.t("help.view.names.key"),     I18n.t("help.view.names.desc") },
            { I18n.t("help.view.mobiledo.key"),  I18n.t("help.view.mobiledo.desc") },
            { I18n.t("help.view.tips.key"),      I18n.t("help.view.tips.desc") },
            { I18n.t("help.view.anacrusis.key"), I18n.t("help.view.anacrusis.desc") },
        };
    }

    private static String[][] helpExercises() {
        return new String[][] {
            { I18n.t("help.exercises.nextprev.key"), I18n.t("help.exercises.nextprev.desc") },
            { I18n.t("help.exercises.restart.key"),  I18n.t("help.exercises.restart.desc") },
        };
    }

    private static String[][] helpMixer() {
        return new String[][] {
            { I18n.t("help.mixer.open.key"),  I18n.t("help.mixer.open.desc") },
            { I18n.t("help.mixer.track.key"), I18n.t("help.mixer.track.desc") },
            { I18n.t("help.mixer.instr.key"), I18n.t("help.mixer.instr.desc") },
            { I18n.t("help.mixer.drums.key"), I18n.t("help.mixer.drums.desc") },
        };
    }

    private static String[][] helpExport() {
        return new String[][] {
            { I18n.t("help.export.new.key"),  I18n.t("help.export.new.desc") },
            { I18n.t("help.export.load.key"), I18n.t("help.export.load.desc") },
            { I18n.t("help.export.midi.key"), I18n.t("help.export.midi.desc") },
            { I18n.t("help.export.svg.key"),  I18n.t("help.export.svg.desc") },
            { I18n.t("help.export.pdf.key"),  I18n.t("help.export.pdf.desc") },
        };
    }

    private static String[][] helpConfig() {
        return new String[][] {
            { "ui.language",                          I18n.t("help.config.language.desc") },
            { "isMetallophone",                       I18n.t("help.config.metallophone.desc") },
            { "lowestMidi / highestMidi",             I18n.t("help.config.midirange.desc") },
            { "tipsVisible",                          I18n.t("help.config.tips.desc") },
            { "screenWidthRatio / screenHeightRatio", I18n.t("help.config.window.desc") },
            { "nMeasuresCam",                         I18n.t("help.config.camera.desc") },
            { "nColsQuarter",                         I18n.t("help.config.resolution.desc") },
            { "nColsScore",                           I18n.t("help.config.capacity.desc") },
            { "autoCorrect",                          I18n.t("help.config.autocorrect.desc") },
        };
    }
}
