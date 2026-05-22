/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.component;

import dodecagraphone.MyController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * [CA] Botó circular similar a {@code MyToggle} però, en lloc de dos estats (on/off),
 * disposa d'una llista d'estats (Strings) propis de cada instància. Cada cop que
 * es prem (toggle), avança circularment a l'estat següent. El text del botó és
 * sempre l'estat actual; el color "pressed" s'aplica quan l'índex d'estat és > 0.
 * <p>
 * [EN] MyCircularButton is similar to {@code MyToggle} but, instead of two states
 * (on/off), it has a list of states (Strings) specific to each instance.
 * Each time it is pressed (toggle), it advances circularly to the next state.
 * The button text is always the current state; the "pressed" color applies
 * when the state index is > 0.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
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
     * [CA] Constructor amb definició de llista d'estats inicial.
     * <p>
     * [EN] Constructor with initial state list definition.
     *
     * @param id        [CA] identificador del botó / [EN] button identifier
     * @param firstCol  [CA] columna inicial relativa al pare / [EN] first column relative to parent
     * @param firstRow  [CA] fila inicial relativa al pare / [EN] first row relative to parent
     * @param nCols     [CA] amplada en columnes / [EN] width in columns
     * @param nRows     [CA] alçada en files / [EN] height in rows
     * @param parent    [CA] component pare / [EN] parent component
     * @param contr     [CA] referència al controlador / [EN] reference to the controller
     * @param textOn    [CA] text quan activat / [EN] text when active
     * @param textOff   [CA] text quan desactivat / [EN] text when inactive
     * @param tipText   [CA] text del tooltip / [EN] tooltip text
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
     * [CA] Estableix la llista d'estats d'aquest botó. Manté l'índex d'estat si és
     * possible; si no, el re-situa dins del rang. Actualitza text/color per reflectir
     * el nou estat actual.
     * <p>
     * [EN] Sets the state list of this button. Keeps the state index if possible;
     * otherwise re-positions it within range. Updates text/color to reflect the
     * new current state.
     *
     * @param states [CA] array d'estats (mínim 1) / [EN] states array (minimum 1)
     * @throws IllegalArgumentException [CA] si l'array és null o buit / [EN] if the array is null or empty
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
     * [CA] Avança circularment a l'estat següent i actualitza els visuals.
     * <p>
     * [EN] Advances circularly to the next state and updates visuals.
     */
    public void nextState() {
        if (stateList.isEmpty()) return;
        this.stateIndex = (this.stateIndex + 1) % stateList.size();
        updateVisualsFromState();
    }

    /**
     * [CA] Retorna el nom de l'estat actual.
     * <p>
     * [EN] Returns the name of the current state.
     *
     * @return [CA] nom de l'estat actual, o cadena buida si la llista és buida /
     *         [EN] current state name, or empty string if the list is empty
     */
    public String getCurrentState() {
        if (stateList.isEmpty()) return "";
        return stateList.get(stateIndex);
    }

    /**
     * [CA] Fixa un estat concret per índex.
     * <p>
     * [EN] Sets a specific state by index.
     *
     * @param index [CA] índex dins de la llista / [EN] index within the list
     */
    public void setStateIndex(int index) {
        if (stateList.isEmpty()) return;
        if (index < 0) index = 0;
        if (index >= stateList.size()) index = stateList.size() - 1;
        this.stateIndex = index;
        updateVisualsFromState();
    }

    /**
     * [CA] Fixa l'estat per nom (primer coincidència).
     * <p>
     * [EN] Sets the state by name (first match).
     *
     * @param state [CA] nom d'estat a cercar / [EN] state name to find
     * @return [CA] true si l'ha trobat i establert / [EN] true if found and set
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
     * [CA] Sobreescriu {@code toggle()} de {@code MyButton}: cada pulsació avança
     * circularment a l'estat següent.
     * <p>
     * [EN] Overrides {@code toggle()} from {@code MyButton}: each press advances
     * circularly to the next state.
     */
    @Override
    public void toggle() {
        nextState();
    }

    /**
     * [CA] {@code setPressed(boolean)} s'interpreta com: false → estat 0 (unpressed);
     * true → estat 1 (si existeix), sinó estat 0. Es fa servir per mantenir
     * una API semblant a {@code MyToggle}.
     * <p>
     * [EN] {@code setPressed(boolean)} is interpreted as: false → state 0 (unpressed);
     * true → state 1 (if it exists), otherwise state 0. Used to maintain an API
     * similar to {@code MyToggle}.
     *
     * @param pressed [CA] true per activar / [EN] true to activate
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
     * [CA] Reseteja el botó al primer estat (índex 0).
     * <p>
     * [EN] Resets the button to the first state (index 0).
     */
    @Override
    public void reset() {
        if (stateList.isEmpty()) return;
        this.stateIndex = 0;
        updateVisualsFromState();
    }

    /* ===== Helpers ===== */

    /**
     * [CA] Sincronitza text i color segons l'estat actual.
     * Estat 0: color unpressed; estat > 0: color pressed.
     * <p>
     * [EN] Synchronizes text and color according to the current state.
     * State 0: unpressed color; state > 0: pressed color.
     */
    private void updateVisualsFromState() {
        String label = getCurrentState();
        this.text = (label == null ? "" : label);
        boolean pressed = (this.stateIndex > 0); // criteri visual simple
        this.isPressed = pressed;
        this.color = pressed ? this.colorPressed : this.colorUnpressed;
    }
}
