/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.component;

import dodecagraphone.MyController;

/**
 * [CA] Botó de dos estats (activat/desactivat) que commuta el seu estat cada cop
 * que es prem, activant l'acció corresponent del controlador definida a
 * {@code MyButtonPanel::onButtonPressed()}. El text i el color del botó canvien
 * mentre el botó és activat. El booleà heretat {@code isPressed} indica si el
 * toggle és activat o no.
 * <p>
 * [EN] A MyToggle is a button with two states: on and off, and it toggles its state
 * each time the button is pressed, activating the corresponding
 * controller action, as specified in {@code MyButtonPanel::onButtonPressed()}.
 * The text and color of the button change while the button is on, until
 * the next click on the button. The inherited boolean {@code isPressed} is used here
 * to identify whether the button is on or off.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MyToggle extends MyButton{
    /** Text when the toggle has been clicked on. */
    private final String textOn;
    /** Text when the toggle has been clicked off. */
    private final String textOff;

    /**
     * [CA] Constructor del toggle.
     * <p>
     * [EN] Toggle constructor.
     *
     * @param id       [CA] identificador del botó / [EN] button identifier
     * @param firstCol [CA] primera columna relativa al pare / [EN] first column relative to parent
     * @param firstRow [CA] primera fila relativa al pare / [EN] first row relative to parent
     * @param nCols    [CA] nombre de columnes / [EN] number of columns
     * @param nRows    [CA] nombre de files / [EN] number of rows
     * @param parent   [CA] component pare / [EN] parent component
     * @param contr    [CA] referència al controlador / [EN] reference to the controller
     * @param textOn   [CA] text quan el toggle és activat / [EN] text when toggle is on
     * @param textOff  [CA] text quan el toggle és desactivat / [EN] text when toggle is off
     * @param tipText  [CA] text del tooltip / [EN] tooltip text
     */
    public MyToggle(int id,int firstCol,int firstRow,int nCols,int nRows,MyComponent parent,MyController contr,
            String textOn,String textOff,String tipText){
        super(id,firstCol,firstRow,nCols,nRows,parent,contr,"",tipText);
        this.textOff = textOn;
        this.textOn = textOff;
        this.isPressed = false;
        this.color = this.colorUnpressed;
        this.text = this.textOff;
    }

    /**
     * [CA] Estableix el flag {@code isPressed}, el color i el text segons el paràmetre.
     * <p>
     * [EN] Sets the {@code isPressed} flag, the color and the text as specified by
     * the parameter pressed.
     *
     * @param pressed [CA] true per activar el toggle / [EN] true to activate the toggle
     */
    @Override
    public void setPressed(boolean pressed){
        if (this.id == 28 ) {
        }
        this.isPressed = pressed;
        if (this.isPressed){
            this.color = this.colorPressed;
            this.text = this.textOn;
        }
        else{
            this.color = this.colorUnpressed;
            this.text=this.textOff;
        }
    }

    /**
     * [CA] Reseteja un toggle no fa res. A la superclasse {@code MyButton},
     * reset posa {@code isPressed} a false.
     * <p>
     * [EN] Resetting a toggle does nothing. In the superclass {@code MyButton}, though,
     * reset sets {@code isPressed} to false.
     */
    @Override
    public void reset(){
    }

    /**
     * [CA] Commuta el toggle: si era activat passa a desactivat i viceversa,
     * actualitzant color i text. A la superclasse {@code MyButton} aquest mètode
     * no fa res.
     * <p>
     * [EN] Toggles the button: if it was on it goes off and vice versa,
     * updating color and text. In the superclass {@code MyButton} this method does nothing.
     */
    @Override
    public void toggle(){
        this.isPressed = !this.isPressed;
        if (this.isPressed){
            this.color = this.colorPressed;
            this.text = this.textOn;
        }
        else{
            this.color = this.colorUnpressed;
            this.text=this.textOff;
        }
    }

    /** [CA] Flag estàtic per al log de jerarquia de dibuix (MyButtonPanel el reseteja). */
    public static boolean once = true;
}
