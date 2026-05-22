/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.color.ColorSets;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * [CA] Indicador visual de selecció associat a una tecla del teclat xilofon
 * ({@code MyXiloKey}). Un slide es dibuixa com una franja de color a sobre o
 * al costat de la tecla per indicar si la nota pertany a l'escala o mode actiu.
 * Pot estar en tres estats: selected, notSelected i off. Les seves dimensions
 * depenen de la posició del teclat (esquerra o dreta) i de l'estat.
 * <p>
 * [EN] Visual selection indicator associated with a xylophone keyboard key
 * ({@code MyXiloKey}). A slide is drawn as a colour strip on top of or beside
 * the key to indicate whether the note belongs to the active scale or mode.
 * It can be in three states: selected, notSelected and off. Its dimensions
 * depend on the keyboard position (left or right) and the state.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MySlide extends MyComponent {
    private final int keyId;
    private Color color_fill;
    private final int midi;
    private String position;
    private int aux = 0;

    /**
     * [CA] Constructor. Crea el slide associat a la tecla {@code keyId}.
     * <p>
     * [EN] Constructor. Creates the slide associated with key {@code keyId}.
     *
     * @param keyId    [CA] identificador de la tecla (primer = 0) / [EN] key identifier (first = 0)
     * @param firstCol [CA] primera columna relativa al pare / [EN] first column relative to parent
     * @param firstRow [CA] primera fila relativa al pare / [EN] first row relative to parent
     * @param nCols    [CA] nombre de columnes / [EN] number of columns
     * @param nRows    [CA] nombre de files / [EN] number of rows
     * @param parent   [CA] component pare (MyXiloKeyboard) / [EN] parent component (MyXiloKeyboard)
     * @param contr    [CA] referència al controlador / [EN] reference to the controller
     */
    public MySlide(int keyId, int firstCol, int firstRow, int nCols, int nRows, MyComponent parent, MyController contr) {
        super(firstCol, firstRow, nCols, nRows, parent, contr);
        this.keyId = keyId;
        this.midi = ToneRange.keyIdToMidi(this.keyId);
        this.screenPosX = parent.screenPosX;
        this.width = parent.width;
        this.position = "notSelected";
    }

    /**
     * [CA] Actualitza el color de fons del slide segons el mode de visualització actiu
     * (pentagrama o escala/mode).
     * <p>
     * [EN] Updates the fill colour of the slide according to the active display mode
     * (pentagram strips or scale/mode).
     */
    public void setColor(){
            if (this.controller.getAllPurposeScore().isShowPentagramaStrips()){
                color_fill = ColorSets.getPentagramaColor(midi);
            } else {
                color_fill = ColorSets.getChoiceColor(midi,this.controller.getAllPurposeScore().getChoice().getChoiceList());
            }
            color_fill = ColorSets.getEncesColor(ColorSets.LINIA_PENTA);
    }

    /**
     * [CA] Estableix les dimensions del slide en estat "selected" (nota seleccionada).
     * La posició i amplada s'ajusten segons el costat del teclat.
     * <p>
     * [EN] Sets the slide dimensions in the "selected" state (note selected).
     * Position and width are adjusted according to the keyboard side.
     */
    public void setDimensionsSelected(){
        // parent is MyXiloKeyboard
        this.position = "selected";
        if (!this.controller.getAllPurposeScore().isUseScreenKeyboardRight()){
            this.screenPosX = parent.screenPosX;
            this.width = parent.width * (1-Settings.KEY_WIDTH_REDUCTION);
        } else {
            this.screenPosX = parent.screenPosX + parent.width * (Settings.KEY_WIDTH_REDUCTION) ;
            this.width = parent.width * (1-Settings.KEY_WIDTH_REDUCTION);
        }
        this.screenPosY = parent.screenPosY + parentFirstRow * Settings.getRowHeight();
        this.height = nRows * Settings.getRowHeight();
        this.setColor();
    }

    /**
     * [CA] Estableix les dimensions del slide en estat "notSelected" (nota no seleccionada).
     * La posició i amplada s'ajusten segons el costat del teclat.
     * <p>
     * [EN] Sets the slide dimensions in the "notSelected" state (note not selected).
     * Position and width are adjusted according to the keyboard side.
     */
    public void setDimensionsNotSelected(){
        this.position = "notSelected";
        if (this.controller.getAllPurposeScore().isUseScreenKeyboardRight()){
            this.screenPosX = parent.screenPosX;
            this.width = parent.width * (1-Settings.KEY_WIDTH_REDUCTION);
        } else {
            this.screenPosX = parent.screenPosX + parent.width * (Settings.KEY_WIDTH_REDUCTION) ;
            this.width = parent.width * (1-Settings.KEY_WIDTH_REDUCTION);
        }
        this.screenPosY = parent.screenPosY + parentFirstRow * Settings.getRowHeight();
        this.height = nRows * Settings.getRowHeight();
        this.setColor();
    }

    /**
     * [CA] Estableix les dimensions del slide en estat "off" (slide ocult, amplada 0).
     * <p>
     * [EN] Sets the slide dimensions in the "off" state (slide hidden, width 0).
     */
    public void setDimensionsOff(){
        this.position = "off";
        this.screenPosX = parent.screenPosX;
        this.width = 0;
        this.screenPosY = parent.screenPosY + parentFirstRow * Settings.getRowHeight();
        this.height = nRows * Settings.getRowHeight();
        this.setColor();
    }

    /**
     * [CA] Retorna l'identificador de la tecla associada.
     * <p>
     * [EN] Returns the identifier of the associated key.
     *
     * @return [CA] id de la tecla / [EN] key id
     */
    public int getKeyId() {
        return keyId;
    }

    /**
     * [CA] Retorna si la tecla NO està seleccionada.
     * <p>
     * [EN] Returns whether the key is NOT selected.
     *
     * @return [CA] true si la posició és "notSelected" / [EN] true if position is "notSelected"
     */
    public boolean isKeyNotSelected(){
        return position.equals("notSelected");
    }

    /**
     * [CA] Retorna si la tecla està seleccionada.
     * <p>
     * [EN] Returns whether the key is selected.
     *
     * @return [CA] true si la posició és "selected" / [EN] true if position is "selected"
     */
    public boolean isKeySelected(){
        return position.equals("selected");
    }

    /** [CA] Flag estàtic per al log de jerarquia de dibuix. */
    public static boolean once = true;

    /**
     * [CA] Dibuixa el slide com un rectangle omplert amb el color de fons, si l'amplada
     * és positiva.
     * <p>
     * [EN] Draws the slide as a filled rectangle with the fill colour, if the width
     * is positive.
     *
     * @param g [CA] context gràfic / [EN] graphics context
     */
    @Override
    public void draw(Graphics2D g){
        if (once) {
            if (Settings.SHOW_DRAW_HIERARCHY) {
                Utilities.printOutWithPriority(5, "MySlide::draw: drawing " + this.getClass());
            }
        }
        once = false;
        aux++;
        if (width>0){
            g.setColor(color_fill);
            g.fillRect((int) screenPosX,(int) screenPosY, (int)Math.ceil(width), (int) Math.ceil(height));
        }
    }
}
