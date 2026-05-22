/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.sound.SoundWithMidi;
import dodecagraphone.ui.Settings;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Set;

/**
 * [CA] Teclat xilofon compost per una columna de {@code MyXiloKey}. Les tecles
 * son subcomponents del teclat. Gestiona la reproducció, l'aturada, el
 * ressaltat i la visualització de l'elecció (notes de l'escala/mode actiu)
 * per a totes les tecles alhora.
 * <p>
 * [EN] The XiloKeyboard is a xylophone-like keyboard of {@code MyXiloKey}s.
 * The xilokeys are subcomponents of the keyboard. It manages playback, stopping,
 * highlighting and choice display (active scale/mode notes) for all keys at once.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MyXiloKeyboard extends MyComponent {

    /**
     * For clarity, xilokeys is an alias of the subcomponent list.
     */
    private final List<MyXiloKey> xilokeys;
    private boolean showChoice;
    private int nKeys;

    /**
     * [CA] Constructor. Crea totes les tecles xilofon i les afegeix com a subcomponents.
     * <p>
     * [EN] Constructor. Creates all xylophone keys and adds them as subcomponents.
     *
     * @param firstCol     [CA] primera columna relativa al pare / [EN] first column relative to parent
     * @param firstRow     [CA] primera fila relativa al pare / [EN] first row relative to parent
     * @param nCols        [CA] nombre de columnes / [EN] number of columns
     * @param nRows        [CA] nombre de files / [EN] number of rows
     * @param keyboardRight [CA] true si el teclat és a la dreta / [EN] true if keyboard is on the right
     * @param parent       [CA] component pare / [EN] parent component
     * @param contr        [CA] referència al controlador / [EN] reference to the controller
     * @param showCh       [CA] true per mostrar l'elecció d'escala / [EN] true to show scale choice
     * @param nKeys        [CA] nombre de tecles / [EN] number of keys
     */
    public MyXiloKeyboard(int firstCol, int firstRow, int nCols, int nRows, boolean keyboardRight, MyComponent parent, MyController contr,boolean showCh, int nKeys) {
        super(firstCol, firstRow, nCols, nRows, parent, contr);
        /**
         * xilokeys is a shallow copy of subComponents.
         */
        this.nKeys = nKeys;
        xilokeys = (List<MyXiloKey>) (List<? extends MyComponent>) subComponents;
        for (int k = 0; k < nKeys; k++) {
            int row = k * Settings.getnRowsSquare();
            MyXiloKey key = new MyXiloKey(k, 0, row, Settings.getnColsKeyboard(), Settings.getnRowsSquare(), this, controller);
            xilokeys.add(key);
        }
        this.doNotHighlight(keyboardRight);
        this.setShowChoice(showCh);
        this.setDimensions(firstCol, firstRow, nCols, nRows);
    }

    /**
     * [CA] Dibuixa totes les tecles i una línia negra al voltant del teclat.
     * <p>
     * [EN] Draws all keys and a black border around the keyboard.
     *
     * @param g [CA] context gràfic / [EN] graphics context
     */
    @Override
    public void draw(Graphics2D g){
        MyXiloKey.once = true;
        super.draw(g);
        g.setColor(Color.BLACK);
    }

    /**
     * [CA] Recalcula les dimensions i posicions de totes les tecles i slides
     * segons l'estat actual (showChoice, costat del teclat).
     * <p>
     * [EN] Recalculates dimensions and positions of all keys and slides
     * according to the current state (showChoice, keyboard side).
     */
    public void resetDimensions(){
        for (int keyId = 0; keyId < nKeys; keyId++) {
            MyXiloKey key = this.xilokeys.get(keyId);
            MySlide slide = key.getSlide();
            if (isShowChoice()){
                if (this.findIfSelected(key.getMidi())){
                    key.setDimensionsSelected();
                    slide.setDimensionsSelected();
                } else {
                    key.setDimensionsNotSelected();
                    slide.setDimensionsNotSelected();
                }
            } else {
                key.setDimensionsOff();
                slide.setDimensionsOff();
            }
        }
        /**
         * When the keyboard is on the left, the xilokeys currently being played
         * are highlighted. This method sets the highlight toggle to false when
         * the keyboard is on the right side.
         */
        this.doNotHighlight(this.controller.getAllPurposeScore().isUseScreenKeyboardRight());
    }

    /**
     * [CA] Reinicia la posició i dimensions del teclat després de canviar el costat o la mida.
     * <p>
     * [EN] Resets the position and dimensions of the keyboard after changing sides or size.
     *
     * @param firstCol [CA] primera columna relativa al pare / [EN] first column relative to parent
     * @param firstRow [CA] primera fila relativa al pare / [EN] first row relative to parent
     * @param nCols    [CA] nombre de columnes / [EN] number of columns
     * @param nRows    [CA] nombre de files / [EN] number of rows
     */
    public final void setDimensions(int firstCol, int firstRow, int nCols, int nRows) {
        super.setDimensions(firstCol, firstRow, nCols, nRows);
        this.resetDimensions();
    }

    /**
     * [CA] Retorna la fila de la tecla xilofon que conté la posició absoluta de pantalla
     * (posX, posY). Retorna -1 si cap tecla conté el punt.
     * <p>
     * [EN] Returns the row of the xilokey that contains the absolute screen position
     * (posX, posY). Returns -1 if no key contains the point.
     *
     * @param posX [CA] coordenada X de pantalla / [EN] screen X coordinate
     * @param posY [CA] coordenada Y de pantalla / [EN] screen Y coordinate
     * @return [CA] id de la tecla o -1 / [EN] key id or -1
     */
    public int whichKey(double posX, double posY) {
        for (MyXiloKey key : xilokeys) {
            if (key.contains(posX, posY)) {
                return key.getKeyId();
            }
        }
        return -1;
    }

    /**
     * [CA] Retorna el keyId del slide que conté la posició de pantalla donada, o -1.
     * <p>
     * [EN] Returns the keyId of the slide containing the given screen position, or -1.
     *
     * @param posX [CA] coordenada X de pantalla / [EN] screen X coordinate
     * @param posY [CA] coordenada Y de pantalla / [EN] screen Y coordinate
     * @return [CA] keyId del slide o -1 / [EN] slide keyId or -1
     */
    public int whichSlideKey(double posX, double posY) {
        for (MyXiloKey key : xilokeys) {
            MySlide slide = key.getSlide();
            if (slide.contains(posX, posY)) return slide.getKeyId();
        }
        return -1;
    }

    /**
     * [CA] Retorna el nombre de tecles del teclat.
     * <p>
     * [EN] Returns the number of keys in the keyboard.
     *
     * @return [CA] nombre de tecles / [EN] number of keys
     */
    public int getnKeys() {
        return nKeys;
    }

    /**
     * [CA] Retorna la tecla xilofon a la fila donada (primera fila = 0).
     * <p>
     * [EN] Gets the Xilokey at a given row (first row = 0).
     *
     * @param row [CA] fila de la tecla / [EN] key row
     * @return [CA] la tecla a la fila donada / [EN] the key at the given row
     */
    public MyXiloKey getKey(int row) {
        return xilokeys.get(row);
    }

    /**
     * [CA] Toca la tecla xilofon a la fila donada amb el canal i velocitat actuals.
     * <p>
     * [EN] Plays the xilokey at a given row with the current channel and velocity.
     *
     * @param key [CA] fila de la tecla a tocar / [EN] key row to play
     */
    public void play(int key) {
        xilokeys.get(key).play(this.controller.getMixer().getCurrentChannelOfCurrentTrack(),SoundWithMidi.getCurrentKeyboardVelocity());
    }

    private static long auxCount =0;

    /**
     * [CA] Toca la tecla en mode "play at beat": només reprodueix a l'inici de cada beat.
     * <p>
     * [EN] Plays the key in "play at beat" mode: only plays at the start of each beat.
     *
     * @param key [CA] fila de la tecla / [EN] key row
     */
    public void playAtBeat(int key) {
        if (Settings.isPlayAtBeat()){
            int ncb = Settings.getnColsBeat();
            if (auxCount % ncb == 0) {
                this.stop(key);
                this.play(key);
            }
            auxCount++;
        }
    }

    /**
     * [CA] Atura la tecla xilofon a la fila donada.
     * <p>
     * [EN] Stops the xilokey at a given row.
     *
     * @param keyID [CA] fila de la tecla a aturar / [EN] key row to stop
     */
    public void stop(int keyID) {
        MyXiloKey key = xilokeys.get(keyID);
        key.stop(this.controller.getMixer().getCurrentChannelOfCurrentTrack());
    }

    /**
     * [CA] Atura totes les tecles xilofon.
     * <p>
     * [EN] Stops all xilokeys.
     */
    public void stopAll() {
        for (int keyId = 0; keyId < nKeys; keyId++) {
            MyXiloKey key = xilokeys.get(keyId);
            Set<Integer> channels = key.getActiveChannels();
            for (int channel : channels) {
                key.stop(channel);
            }
        }
    }

    /**
     * [CA] Estableix el toggle {@code doNotHighlight} de totes les tecles.
     * Quan el teclat és a la dreta (mode endevina), les tecles no es ressalten.
     * <p>
     * [EN] Sets the {@code doNotHighlight} toggle of all keys.
     * When the keyboard is on the right (guessing mode), keys are not highlighted.
     *
     * @param keyboardIsRight [CA] true si el teclat és a la dreta / [EN] true if keyboard is on the right
     */
    public final void doNotHighlight(boolean keyboardIsRight) {
        for (int row = 0; row < nKeys; row++) {
            xilokeys.get(row).doNotHighlight(keyboardIsRight);
        }
    }

    /**
     * [CA] Estableix si s'ha de mostrar la visualització de l'elecció d'escala.
     * <p>
     * [EN] Sets whether the scale choice display should be shown.
     *
     * @param show [CA] true per mostrar l'elecció / [EN] true to show the choice
     */
    public final void setShowChoice(boolean show){
        this.showChoice = show;
    }

    /**
     * [CA] Retorna si es mostra la visualització de l'elecció d'escala.
     * <p>
     * [EN] Returns whether the scale choice display is shown.
     *
     * @return [CA] true si s'està mostrant / [EN] true if showing
     */
    public boolean isShowChoice() {
        return showChoice;
    }

    /**
     * [CA] Comprova si la nota MIDI donada és a la llista de notes seleccionades (elecció).
     * <p>
     * [EN] Checks whether the given MIDI note is in the selected notes list (choice).
     *
     * @param midi [CA] nota MIDI a comprovar / [EN] MIDI note to check
     * @return [CA] true si la nota és seleccionada / [EN] true if the note is selected
     */
    public boolean findIfSelected(int midi){
        List<Integer> selected = this.controller.getAllPurposeScore().getChoice().getChoiceList();
        return (selected.contains(midi));
    }

    /**
     * [CA] Retorna la llista de tecles xilofon.
     * <p>
     * [EN] Returns the list of xilokeys.
     *
     * @return [CA] llista de tecles / [EN] list of keys
     */
    public List<MyXiloKey> getKeyboard() {
        return xilokeys;
    }

}
