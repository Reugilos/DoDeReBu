package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.chord.Chord;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.Map;

/**
 *
 * The chord symbol line is an horizontal strip that moves along the score, with
 * beat and measures bars, where the application can write chord symbols
 * corresponding to the current score (like in a music fake book).
 *
 * @author Pau
 */
public class MyChordSymbolLine extends MyComponent {

    private MyGridScore score;
    private MyController contr;

    /**
     *
     * @param firstCol
     * @param firstRow
     * @param nCols
     * @param nRows
     * @param parent
     * @param contr
     * @param score
     */
    public MyChordSymbolLine(int firstCol, int firstRow, int nCols, int nRows, MyComponent parent, MyController contr, MyGridScore score) {
        super(firstCol, firstRow, nCols, nRows, parent, contr);
        this.score = score;
        this.contr = contr;
    }

    public int whichCol(double screenX, double screenY) {
        if (this.contains(screenX, screenY)) {
            int whichCol = this.contr.getAllPurposeScore().getCol(screenX);
            whichCol = whichCol - (whichCol % Settings.getnColsBeat());
            return whichCol;
        }
        return -1;
    }

    public Chord enterChord(Chord oldChord) {
        return null;
//        String defStr = "";
//        int ncols = 0;
//        if (oldChord != null) {
//            defStr = oldChord.basicString();
//            ncols = oldChord.getNCols();
//        }
//
//        String newChordString = MyDialogs.mostraInputDialog("Enter chord: ", "Chord", defStr);
//        if (newChordString == null || newChordString.isEmpty()) {
//            return null;
//        }
//
//        // 1) Normalitza al format "Intervals" amb l'API de ChordSymbols
//        String intervalsStr;
//        try {
//            String s = newChordString.trim();
//            
//            // RECONSIDERAR
//
//            // Si ja és interval·lic, el normalitzem igualment per assegurar format canònic
//            if (ChordSymbols.isIntervalic(s)) {
//                // Convertim a simbòlic i tornem a intervals per assegurar format consistent
//                String sym = ChordSymbols.fromIntervalNotation(s);
//                intervalsStr = ChordSymbols.toIntervalNotation(sym);
//            } // Symbolic / Sinònim
//            else if (ChordSymbols.isSymbolic(s)) {
//                intervalsStr = ChordSymbols.toIntervalNotation(s);
//            } // DodecaNoms (els teus noms do,de,re,...) 
//            else if (ChordSymbols.isNoteNames(s)) {
//                intervalsStr = ChordSymbols.toIntervalNotation(s);
//            } // Notes amb #/b
//            else if (ChordSymbols.isAlteracions(s)) {
//                intervalsStr = ChordSymbols.toIntervalNotation(s);
//            } // (Opcional) Notació de posició: [1,3,5]...
//            else if (ChordSymbols.isPosicio(s)) {
//                // La teva especificació parla de 5 formats; si vols admetre també posició:
//                intervalsStr = ChordSymbols.fromPosicio(s); // retorna "Do[...]" (format intervals)
//            } else {
//                // Qualsevol altre cas: marquem invàlid
//                throw new IllegalArgumentException("format no reconegut");
//            }
//        } catch (IllegalArgumentException ex) {
//            // Mostra només el missatge estàndard que ja feies servir
//            MyDialogs.mostraError("Acord invalid.", "Error");
//            // Manté el comportament antic: retorna l'acord amb el text d'entrada original
//            return new Chord(newChordString);
//        } catch (Exception ex) {
//            MyDialogs.mostraError("Acord invalid.", "Error");
//            return new Chord(newChordString);
//        }
//
//        // 2) Valida amb el teu model de Chord
//        Chord newchord = new Chord(intervalsStr);
//        if (!newchord.isValidChord()) {
//            MyDialogs.mostraError("Acord invalid.", "Error");
//            return newchord;
//        }
//
//        String nBeatsString = MyDialogs.mostraInputDialog(
//                "Enter num beats: ", "Num Beats", "" + (ncols / Settings.getnColsBeat()));
//        try {
//            if (nBeatsString != null && !nBeatsString.trim().isEmpty()) {
//                newchord.setNCols(Integer.parseInt(nBeatsString.trim()) * Settings.getnColsBeat());
//            }
//        } catch (Exception ignore) {
//            // Si l'usuari posa quelcom no numèric, deixem els nCols tal com estaven
//        }
//        return newchord;
    }

    /**
     * setter.
     *
     * @param score
     */
    public void setScore(MyGridScore score) {
        this.score = score;
    }

    /**
     * Draws a measure line. To place the line, it uses score rows (negative)
     * and columns.
     *
     * @param col
     * @param g
     */
    private void drawMeasureLine(int col, Graphics2D g) {
        if (Settings.IS_BU) return;
        Stroke stroke = g.getStroke();
        g.setStroke(new BasicStroke((float) 3));
        int camX1 = (int) Math.floor(score.getScreenX(col));
        if (!Settings.IS_BU) System.out.println("MyChordSymbolLine::drawMeasureLine camX1 = "+camX1);
        int camY1 = (int) Math.round(score.getScreenY(-nRows));
        int camX2 = camX1;
        int camY2 = camY1 + (int) (nRows * Settings.getRowHeight());
        g.setColor(java.awt.Color.BLACK);
        g.drawLine(camX1, camY1, camX2, camY2);
        g.setStroke(stroke);
    }

    /**
     * Draws a beat line. To place the line, it uses score rows (negative) and
     * columns.
     *
     * @param col
     * @param g
     */
    private void drawBeatLine(int col, Graphics2D g) {
        if (Settings.IS_BU) return;
        Stroke stroke = g.getStroke();
        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{15, 3}, 0);
        g.setStroke(dashed);
        int camX1 = (int) Math.floor(score.getScreenX(col));
        int camY1 = (int) Math.round(score.getScreenY(-nRows));
        int camX2 = camX1;
        int camY2 = camY1 + (int) (nRows * Settings.getRowHeight());
        g.setColor(java.awt.Color.BLACK);
        g.drawLine(camX1, camY1, camX2, camY2);
        g.setStroke(stroke);
    }

    /**
     * Given a chord, it draws the chord symbol: if the chord root is -1, it
     * draws the field info of the chord (a text); if the Settings flag
     * chordSymbolVertical is set, it draws the chord notes in vertical,
     * otherwise it draws a basic chord symbol (chord.basicString()).
     *
     * @param chord
     * @param col
     * @param g
     */
    public void drawChordSymbol(Chord chord, int col, Graphics2D g) {
        g.setColor(java.awt.Color.BLACK);
        int camX = 3 + (int) score.getScreenX(col);
        int camY = 8 + (int) Settings.getRowHeight() + (int) getScreenY(2);
        if (chord.getRoot() == Settings.USE_INFO_AS_SIMBOL) { // draws info
            g.drawString(chord.getInfo(), (int) (camX), (int) (camY));
        } else {
            if (Settings.isChordSymbolVertical()) {
                int camYFirst = camY;
                g.drawString(chord.getSimbolWithNumericBass(), camX, camY);
                int[] shape = chord.getShape();
                camX += 10;
                if (chord.getBass() != Chord.NULL_BASS) {
                    camX += 30;
                }
                camY = camYFirst;
                for (int i = 0; i < 4; i++) {
                    if (i < shape.length) {
                        g.drawString(String.format("%2d", shape[i]) + "", camX, camY);
                        camY -= 10;
                    }
                }
                camX += 20;
                camY = camYFirst;
                for (int i = 4; i < shape.length; i++) {
                    if (i < shape.length) {
                        g.drawString(String.format("%2d", shape[i]) + "", camX, camY);
                        camY -= 10;
                    }
                }
            } else {
                g.drawString(chord.basicString(), camX, camY);
            }
        }
    }

    /**
     * For each column, draws the chord symbol (if not null) and the beat and
     * measure lines (when required).
     *
     * @param g
     */
    @Override
    public void draw(Graphics2D g) {
        if (Settings.SHOW_DRAW_HIERARCHY) {
            Utilities.printOutWithPriority(5, "MyChordSymbolLine::draw: drawing " + this.getClass());
        }
        if (!Settings.IS_BU){
            System.out.println("MyChordSymbolLine::draw nColsBeat = "+Settings.getnColsBeat()+" nBM = "+score.getNumBeatsMeasure()+
            " colWidth=" + Settings.getColWidth() +
            " beatPx=" + (Settings.getnColsBeat() * Settings.getColWidth()));
        }
        //g.setColor(Color.MAGENTA);
        //g.drawRect((int) screenPosX, (int) screenPosY, (int) width, (int) height);
        g.setColor(Color.BLACK);
        g.drawRect((int) screenPosX, (int) screenPosY, (int) width, (int) height);
        int firstDrawCol = score.getScoreCol(0);
        if (firstDrawCol < 0) {
            firstDrawCol = 0;
        }
        Map<Integer, Chord> chordSimbolLine = score.getChordSimbolLine();
        int first = Math.min(this.score.getCurrentCol() - this.score.getDelay(!this.score.isUseScreenKeyboardRight()), this.score.getNumCols());
        int col = first;
        if ((col % Settings.getnColsBeat()) == 0) {
            //System.out.println("MyGridScore::draw: "+col+" acabo de pintar tots els grids.");
            drawBeatLine(col, g);
            //System.out.println("MyGridScore::draw: "+col+" ara he pintat beat line.");
        }
        if ((col % (Settings.getnColsBeat() * score.getNumBeatsMeasure())) == 0) {
            drawMeasureLine(col, g);
        }
        for (col = first - 1; col >= firstDrawCol; col--) {
            Chord chord = chordSimbolLine.get(col);
            if (chord != null) {
                // System.out.println("MyChordSymbolLine::draw: "+chord.toString());
                drawChordSymbol(chord, col, g);
            }
            if ((col % Settings.getnColsBeat()) == 0) {
                drawBeatLine(col, g);
            }
            if ((col % (Settings.getnColsBeat() * score.getNumBeatsMeasure())) == 0) {
                drawMeasureLine(col, g);
            }
        }
        col = score.getNumCols();
        if ((col % (Settings.getnColsBeat() * score.getNumBeatsMeasure())) == 0) {
            drawMeasureLine(col, g);
        }
    }
}
