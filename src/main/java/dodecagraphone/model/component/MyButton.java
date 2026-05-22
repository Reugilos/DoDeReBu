/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.color.ColorSets;
import static dodecagraphone.model.color.ColorSets.BUTO;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * [CA] Botó visual de l'aplicació. Hi ha dos tipus: {@code MyButton} (superclasse)
 * que es reseteja en alliberar el ratolí, i {@code MyToggle} (subclasse) que
 * commuta el seu estat cada cop que es prem. Quan es prem un {@code MyButton},
 * s'activa l'acció corresponent del controlador definida a
 * {@code MyButtonPanel::onButtonPressed()}. El botó té un text i canvia de color
 * mentre és premut.
 * <p>
 * [EN] There are two kinds of button: {@code MyButton} (superclass) that reacts when
 * it is pressed and resets when the mouse is released, and {@code MyToggle} (subclass)
 * that toggles its state every time it is pressed.
 * When a MyButton is pressed, it activates the corresponding controller
 * command, as specified in {@code MyButtonPanel::onButtonPressed()}. A button has
 * a text, and it has a different color while it is being pressed and when
 * it is released.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MyButton extends MyComponent {
    /** Button id. */
    protected int id;
    /* Text of the button. */
    protected String text;
    /** Current color of the button. */
    protected Color color;
    /** Color while the button is being pressed. */
    protected Color colorPressed = ColorSets.getIluminatColor(BUTO);
    /** Color of the button when it is not pressed. */
    protected Color colorUnpressed = ColorSets.getEncesColor(BUTO);
    /** True if the button is being pressed. */
    protected boolean isPressed;
    protected String tipText;

    /**
     * [CA] Constructor del botó.
     * <p>
     * [EN] Button constructor.
     *
     * @param id       [CA] identificador únic del botó / [EN] unique button identifier
     * @param firstCol [CA] primera columna relativa al pare / [EN] first column relative to parent
     * @param firstRow [CA] primera fila relativa al pare / [EN] first row relative to parent
     * @param nCols    [CA] nombre de columnes / [EN] number of columns
     * @param nRows    [CA] nombre de files / [EN] number of rows
     * @param parent   [CA] component pare / [EN] parent component
     * @param contr    [CA] referència al controlador / [EN] reference to the controller
     * @param text     [CA] text visible al botó / [EN] text displayed on the button
     * @param tipText  [CA] text del tooltip / [EN] tooltip text
     */
    public MyButton(int id,int firstCol,int firstRow,int nCols,int nRows,MyComponent parent,MyController contr,String text, String tipText){
        super(firstCol,firstRow,nCols,nRows,parent,contr);
        this.id = id;
        this.text = text;
        this.isPressed = false;
        this.color = this.colorUnpressed;
        this.tipText = "<html> Soc el botó " + id + " i mostro un missatge molt llarg <br>en dos línies </html>";
        if (!"".equals(tipText)) this.tipText = tipText;
    }

    public static boolean once = true;

    /**
     * [CA] Dibuixa el botó amb el text i color actuals, centrant el text.
     * <p>
     * [EN] Draws the button with the current text and color, centering the text.
     *
     * @param g [CA] context gràfic / [EN] graphics context
     */
    @Override
    public void draw(Graphics2D g) {
        if (once) {
            if (Settings.SHOW_DRAW_HIERARCHY) {
                Utilities.printOutWithPriority(5, "MyButton::draw: drawing " + this.getClass());
            }
        }
        once = false;
        g.setColor(this.color);
        g.fillRect((int) screenPosX, (int) screenPosY, (int) width, (int) height);
        g.setColor(java.awt.Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        float tx = (float)(screenPosX + (width  - fm.stringWidth(text)) / 2.0);
        float ty = (float)(screenPosY + (height + fm.getAscent() - fm.getDescent()) / 2.0);
        g.drawString(text, tx, ty);
    }

    /**
     * [CA] Retorna el text del tooltip del botó.
     * <p>
     * [EN] Returns the tooltip text of the button.
     *
     * @return [CA] text del tooltip / [EN] tooltip text
     */
    public String getTipText() {
        return tipText;
    }

    /**
     * [CA] Estableix el text del tooltip del botó.
     * <p>
     * [EN] Sets the tooltip text of the button.
     *
     * @param tipText [CA] nou text del tooltip / [EN] new tooltip text
     */
    public void setTipText(String tipText) {
        this.tipText = tipText;
    }

    /**
     * [CA] Estableix el text visible del botó.
     * <p>
     * [EN] Sets the visible text of the button.
     *
     * @param text [CA] nou text del botó / [EN] new button text
     */
    public void setButtonText(String text){
        this.text = text;
    }

    /**
     * [CA] Retorna el text visible del botó.
     * <p>
     * [EN] Returns the visible text of the button.
     *
     * @return [CA] text del botó / [EN] button text
     */
    public String getButtonText(){
        return text;
    }

    /**
     * [CA] Retorna l'identificador del botó.
     * <p>
     * [EN] Returns the button identifier.
     *
     * @return [CA] id del botó / [EN] button id
     */
    public int getId() {
        return id;
    }

    /**
     * [CA] Estableix si el botó és premut i actualitza el color corresponent.
     * <p>
     * [EN] Sets whether the button is pressed and updates the corresponding color.
     *
     * @param isPressed [CA] true si el botó és premut / [EN] true if the button is pressed
     */
    public void setPressed(boolean isPressed){
        this.isPressed = isPressed;
        if (isPressed){
            this.color = this.colorPressed;
        }
        else {
            this.color = this.colorUnpressed;
        }
    }

    /**
     * [CA] Reseteja el botó posant-lo a no premut. En la subclasse {@code MyToggle},
     * aquest mètode no fa res.
     * <p>
     * [EN] Resets a button to not-pressed. In the subclass {@code MyToggle},
     * this method does nothing.
     */
    public void reset(){
        this.setPressed(false);
    }

    /**
     * [CA] Commuta l'estat del botó. En {@code MyButton} no fa res; en
     * {@code MyToggle} sí que commuta.
     * <p>
     * [EN] Toggles the button state. In {@code MyButton} this does nothing;
     * in {@code MyToggle} it toggles.
     */
    public void toggle(){
    }

    /**
     * [CA] Retorna si el botó és premut.
     * <p>
     * [EN] Returns whether the button is pressed.
     *
     * @return [CA] true si el botó és premut / [EN] true if the button is pressed
     */
    public boolean isPressed() {
        return isPressed;
    }

    /**
     * [CA] Estableix el text del botó.
     * <p>
     * [EN] Sets the button text.
     *
     * @param text [CA] nou text / [EN] new text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * [CA] Retorna el text del botó.
     * <p>
     * [EN] Returns the button text.
     *
     * @return [CA] text del botó / [EN] button text
     */
    public String getText() {
        return text;
    }

}
