/*
 * PolyForm Noncommercial License 1.0.0
 * Copyright (c) 2024-2026 Pau Bofill. Powered by Claude AI.
 * Full license / Llicència completa: LICENSE (project root / arrel del projecte)
 */
package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * [CA] Línia d'estat horitzontal a la part inferior de la pantalla on l'aplicació
 * pot escriure missatges a l'usuari. En la versió actual disposa de dues zones
 * de text: {@code text} (a l'esquerra) i {@code rightText} (a la dreta).
 * <p>
 * [EN] The status line is a horizontal bar at the bottom of the screen where
 * the application can write messages to the user. In its current version
 * the status line has two positions where text can be written ({@code text}, at the
 * left, and {@code rightText} at the right).
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MyStatusLine extends MyComponent{
    /** The text in this attribute is written at the left hand side of the
     * status line. */
    private String text= "Hola";
    /** The text in this attribute is written at the right hand side. */
    private String rightText = "";

    /**
     * [CA] Constructor. Estableix la posició i mida de la línia d'estat.
     * <p>
     * [EN] Constructor. Sets the position and size of the status line.
     *
     * @param firstCol [CA] primera columna relativa al pare / [EN] first column relative to parent
     * @param firstRow [CA] primera fila relativa al pare / [EN] first row relative to parent
     * @param nCols    [CA] nombre de columnes / [EN] number of columns
     * @param nRows    [CA] nombre de files / [EN] number of rows
     * @param parent   [CA] component pare / [EN] parent component
     * @param contr    [CA] referència al controlador / [EN] reference to the controller
     */
    public MyStatusLine(int firstCol, int firstRow, int nCols, int nRows, MyComponent parent,MyController contr){
        super(firstCol,firstRow,nCols,nRows,parent,contr);
    }

    /**
     * [CA] Esborra els dos textos de la línia d'estat.
     * <p>
     * [EN] Clears both texts of the status line.
     */
    public void clear(){
        this.text="";
        this.rightText="";
    }

    /**
     * [CA] Retorna el text de l'esquerra.
     * <p>
     * [EN] Returns the left-side text.
     *
     * @return [CA] text esquerre / [EN] left text
     */
    public String getText() {
        return text;
    }

    /**
     * [CA] Estableix el text de l'esquerra.
     * <p>
     * [EN] Sets the left-side text.
     *
     * @param text [CA] nou text esquerre / [EN] new left text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * [CA] Retorna el text de la dreta.
     * <p>
     * [EN] Returns the right-side text.
     *
     * @return [CA] text dret / [EN] right text
     */
    public String getRightText() {
        return rightText;
    }

    /**
     * [CA] Estableix el text de la dreta.
     * <p>
     * [EN] Sets the right-side text.
     *
     * @param rightText [CA] nou text dret / [EN] new right text
     */
    public void setRightText(String rightText) {
        this.rightText = rightText;
    }

    /**
     * [CA] Retorna la concatenació del text esquerre i el dret, separats per espais.
     * <p>
     * [EN] Returns a concatenation of the text and rightText, separated by spaces.
     *
     * @return [CA] text complet / [EN] full text
     */
    public String getFullText(){
        return text + "                 " + rightText;
    }

    /**
     * [CA] Dibuixa la línia d'estat amb el text i el fons gris.
     * <p>
     * [EN] Draws the full text and the status line background.
     *
     * @param g [CA] context gràfic / [EN] graphics context
     */
    @Override
    public void draw(Graphics2D g) {
            if (Settings.SHOW_DRAW_HIERARCHY) {
                Utilities.printOutWithPriority(5, "MyStatusLine::draw: drawing " + this.getClass());
            }

            g.setColor(Color.GRAY);
            g.fillRect((int) screenPosX, (int) screenPosY, (int) width, (int) height);
            g.setColor(java.awt.Color.WHITE);

            String full = getFullText();
            int maxWidth = (int) width - 20;
            java.awt.FontMetrics fm = g.getFontMetrics();
            int lineH = fm.getHeight();
            int baseY = (int)(screenPosY + 4 + lineH);

            if (fm.stringWidth(full) <= maxWidth) {
                g.drawString(full, (int)(screenPosX + 10), baseY);
            } else {
                // Cerca el tall de paraula més proper al centre
                String[] words = full.split(" ");
                StringBuilder line1 = new StringBuilder();
                int splitIdx = 0;
                for (int i = 0; i < words.length; i++) {
                    String candidate = (line1.length() == 0 ? "" : line1 + " ") + words[i];
                    if (fm.stringWidth(candidate) > maxWidth && i > 0) break;
                    line1 = new StringBuilder(candidate);
                    splitIdx = i + 1;
                }
                StringBuilder line2 = new StringBuilder();
                for (int i = splitIdx; i < words.length; i++) {
                    if (line2.length() > 0) line2.append(" ");
                    line2.append(words[i]);
                }
                g.drawString(line1.toString(), (int)(screenPosX + 10), baseY);
                g.drawString(line2.toString(), (int)(screenPosX + 10), baseY + lineH);
            }
    }
}
