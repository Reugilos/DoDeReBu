package dodecagraphone.model.component;

import dodecagraphone.MyController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * MyCircularButton és similar a MyToggle però, en lloc de dos estats (on/off),
 * disposa d'una llista d'estats (Strings) propis de cada instància.
 * Cada cop que es prem (toggle), avança circularment a l'estat següent.
 *
 * Integració:
 *  - El text del botó és sempre l'estat actual.
 *  - El color "pressed" s'aplica quan l'índex d'estat > 0; si és 0, "unpressed".
 *  - reset() retorna al primer estat (índex 0).
 *  - setPressed(boolean) s'interpreta com: false -> estat 0; true -> estat 1 (si existeix).
 */
public class MyCircularButton extends MyButton {

    /** Llista d'estats (no buida). */
    private final List<String> stateList = new ArrayList<>(Arrays.asList("Simbol", "Sinomim", "Intervals", "Posicions", "Notes"));
    /** Índex de l'estat actual dins de stateList. */
    private int stateIndex = 0;
    /** Text when the toggle has been clicked on. */
    private final String textOn;
    /** Text when the toggle has been clicked off. */
    private final String textOff;


    /**
     * Constructor amb definició de llista d'estats inicial.
     *
     * @param id        identificador
     * @param firstCol  columna inicial
     * @param firstRow  fila inicial
     * @param nCols     amplada en columnes
     * @param nRows     alçada en files
     * @param parent    component pare
     * @param contr     controlador
     * @param states    llista d'estats (mínim 1)
     * @param tipText   tooltip
     */
    public MyCircularButton(int id, int firstCol, int firstRow, int nCols, int nRows,
                            MyComponent parent, MyController contr,String textOn,String textOff,
                            String tipText) {
        super(id, firstCol, firstRow, nCols, nRows, parent, contr, "", tipText);
        // Inicialització visual per defecte
        this.isPressed = false;
        this.color = this.colorUnpressed;
        this.textOn = textOn;
        this.textOff = textOff;

        this.updateVisualsFromState(); // defineix estats i sincronitza visuals
    }

    /**
     * Estableix la llista d'estats d'aquest botó.
     * Manté el stateIndex si és possible; si no, el re-situa dins del rang.
     * Actualitza text/color per reflectir el nou estat actual.
     *
     * @param states array d'estats (mínim 1)
     * @throws IllegalArgumentException si null o buit
     */
    public void setStateList(String[] states) {
        if (states == null || states.length == 0) {
            throw new IllegalArgumentException("MyCircularButton: cal almenys un estat.");
        }
        this.stateList.clear();
        this.stateList.addAll(Arrays.asList(states));

        // Si l'índex actual surt de rang, el reubiquem (normalment 0).
        if (this.stateIndex < 0 || this.stateIndex >= this.stateList.size()) {
            this.stateIndex = 0;
        }
        updateVisualsFromState();
    }

    /**
     * Avança circularment a l'estat següent i actualitza visuals.
     */
    public void nextState() {
        if (stateList.isEmpty()) return;
        this.stateIndex = (this.stateIndex + 1) % stateList.size();
        updateVisualsFromState();
    }

    /**
     * Retorna el nom de l'estat actual.
     */
    public String getCurrentState() {
        if (stateList.isEmpty()) return "";
        return stateList.get(stateIndex);
    }

    /**
     * Opcional: permet fixar un estat concret per índex.
     * @param index índex dins de la llista
     */
    public void setStateIndex(int index) {
        if (stateList.isEmpty()) return;
        if (index < 0) index = 0;
        if (index >= stateList.size()) index = stateList.size() - 1;
        this.stateIndex = index;
        updateVisualsFromState();
    }

    /**
     * Opcional: fixar estat per nom (primer match).
     * @param state nom d'estat
     * @return true si l'ha trobat i establert
     */
    public boolean setStateByName(String state) {
        if (state == null || stateList.isEmpty()) return false;
        int i = stateList.indexOf(state);
        if (i < 0) return false;
        this.stateIndex = i;
        updateVisualsFromState();
        return true;
    }

    /**
     * Alineat amb MyToggle: premre (toggle) canvia d'estat.
     * En MyButton, toggle() no fa res; aquí el sobreescrivim.
     */
    @Override
    public void toggle() {
        nextState();
    }
    
    /**
     * setPressed(boolean) aquí s'interpreta com:
     *  - false -> estat 0 (unpressed)
     *  - true  -> estat 1 (si existeix), sinó 0
     * Es fa servir per mantenir una API semblant a MyToggle.
     */
    @Override
    public void setPressed(boolean pressed) {
        if (stateList.isEmpty()) return;
        if (pressed && stateList.size() >= 2) {
            this.stateIndex = 1;
        } else {
            this.stateIndex = 0;
        }
        updateVisualsFromState();
    }

    /**
     * Reset deixa el botó al primer estat.
     */
    @Override
    public void reset() {
        if (stateList.isEmpty()) return;
        this.stateIndex = 0;
        updateVisualsFromState();
    }

    /* ===== Helpers ===== */

    /**
     * Sincronitza text i color segons l'estat actual.
     * Estat 0: color unpressed; estat >0: color pressed.
     */
    private void updateVisualsFromState() {
        String label = getCurrentState();
        this.text = (label == null ? "" : label);
        boolean pressed = (this.stateIndex > 0); // criteri visual simple
        this.isPressed = pressed;
        this.color = pressed ? this.colorPressed : this.colorUnpressed;
    }
}

