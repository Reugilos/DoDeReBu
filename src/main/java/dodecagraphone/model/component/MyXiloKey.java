/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.*;
import dodecagraphone.model.color.ColorSets;
import dodecagraphone.model.sound.SampleOrMidi;
import dodecagraphone.model.sound.Sound;
import dodecagraphone.model.sound.SoundWithMidi;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;

/**
 * [CA] Una tecla del teclat xilofon ({@code MyXiloKeyboard}), identificada pel seu
 * identificador de fila ({@code keyId}). Cada tecla coneix el seu color, el so
 * associat, la nota MIDI i els flags {@code doNotHighlight} i
 * {@code activeChannels}. Conté un {@code MySlide} fill per indicar visualment
 * si la nota pertany a l'escala activa.
 * <p>
 * [EN] A key in the Xylophone keyboard ({@code MyXiloKeyboard}), identified by its
 * row identifier ({@code keyId}). Each key knows its colour, associated sound,
 * MIDI tone, and the flags {@code doNotHighlight} and {@code activeChannels}.
 * It contains a child {@code MySlide} to visually indicate whether the note
 * belongs to the active scale.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MyXiloKey extends MyComponent {
    int aux = 0;
    /**
     * The current color of the key.
     */
    private Color color;
    /**
     * The color that the key takes when it is playing
     */
    private final Color color_playing;
    /**
     * The color that the key takes when it is not playing
     */
    private final Color color_silent;
    /**
     * The sound sample associated with the key (when not using midi).
     */
    private Sound so = null;
    /**
     * When the keyboard is on the left, the key is highlighted when it is
     * playing. Each key stores this flag.
     */
    private boolean doNotHighlight = false;
    /**
     * The row position of the key in the keyboard (first key is 0).
     */
    private final int row;
    /**
     * The numeral of the key (first key is 0).
     */
    private final int keyId;
    /**
     * The midi note associated with the key.
     */
    private final int midi;
    /**
     * The corresponding note name.
     */
    private final String noteName;
    /**
     * The channels that are currently playing.
     */
    private Set<Integer> activeChannels;
    private final java.util.Map<Integer, Integer> channelRealMidi = new java.util.HashMap<>();
    private MySlide slide;

    /**
     * [CA] Constructor. Crea la tecla xilofon amb l'identificador i dimensions donades.
     * Si s'utilitzen mostres de so (no MIDI), carrega el fitxer de so corresponent.
     * <p>
     * [EN] Constructor. Creates the xylophone key with the given identifier and dimensions.
     * If sound samples (not MIDI) are used, loads the corresponding sound file.
     *
     * @param keyId    [CA] identificador de la tecla (primer = 0) / [EN] key identifier (first = 0)
     * @param firstCol [CA] primera columna relativa al pare / [EN] first column relative to parent
     * @param firstRow [CA] primera fila relativa al pare / [EN] first row relative to parent
     * @param nCols    [CA] nombre de columnes / [EN] number of columns
     * @param nRows    [CA] nombre de files / [EN] number of rows
     * @param parent   [CA] component pare (MyXiloKeyboard) / [EN] parent component (MyXiloKeyboard)
     * @param contr    [CA] referència al controlador / [EN] reference to the controller
     */
    public MyXiloKey(int keyId, int firstCol, int firstRow, int nCols, int nRows, MyComponent parent, MyController contr) {
        super(firstCol, firstRow, nCols, nRows, parent, contr);
        this.keyId = keyId;
        this.row = firstRow;
        this.midi = ToneRange.keyIdToMidi(this.keyId);
        this.noteName = ToneRange.getNoteName(midi);
        this.color_playing = ColorSets.getColorFons();
        this.color_silent = ColorSets.getEncesColor(midi % 12);
        if (!SampleOrMidi.isMidi()) {
            this.so = new Sound(ToneRange.getFilename(midi));
        }
        this.color = this.color_silent;
        this.activeChannels = new HashSet<>();

        this.screenPosX = parent.screenPosX;
        this.width = parent.width;
        this.slide = new MySlide(keyId,0,firstRow,nCols,nRows,parent,contr); // parent is MyXiloKeyboard
        this.add(slide);
    }

    /**
     * [CA] Recalcula les dimensions de la tecla en estat "notSelected"
     * quan les dimensions del pare han canviat.
     * <p>
     * [EN] Resets the dimensions of the key in "notSelected" state
     * when parent dimensions have changed.
     */
    public final void setDimensionsNotSelected() {
        if (this.controller.getAllPurposeScore().isUseScreenKeyboardRight()){
            this.screenPosX = parent.screenPosX + (1-Settings.KEY_WIDTH_REDUCTION)*parent.width;
            this.width = parent.width * Settings.KEY_WIDTH_REDUCTION;
        } else{
            this.screenPosX = parent.screenPosX;
            this.width = parent.width * Settings.KEY_WIDTH_REDUCTION;
        }
        this.screenPosY = parent.screenPosY + parentFirstRow * Settings.getRowHeight();
        this.height = nRows * Settings.getRowHeight();
    }

    /**
     * [CA] Recalcula les dimensions de la tecla en estat "selected".
     * <p>
     * [EN] Resets the dimensions of the key in "selected" state.
     */
    public final void setDimensionsSelected() {
        if (!this.controller.getAllPurposeScore().isUseScreenKeyboardRight()){
            this.screenPosX = parent.screenPosX + (1-Settings.KEY_WIDTH_REDUCTION)*parent.width;
            this.width = parent.width * Settings.KEY_WIDTH_REDUCTION;
        } else{
            this.screenPosX = parent.screenPosX;
            this.width = parent.width * Settings.KEY_WIDTH_REDUCTION;
        }
        this.screenPosY = parent.screenPosY + parentFirstRow * Settings.getRowHeight();
        this.height = nRows * Settings.getRowHeight();
    }

    /**
     * [CA] Recalcula les dimensions de la tecla en estat "off" (amplada total del pare).
     * <p>
     * [EN] Resets the dimensions of the key in "off" state (full parent width).
     */
    public final void setDimensionsOff() {
        this.screenPosX = parent.screenPosX;
        this.width = parent.width;
        this.screenPosY = parent.screenPosY + parentFirstRow * Settings.getRowHeight();
        this.height = nRows * Settings.getRowHeight();
    }

    /** [CA] Flag estàtic per al log de jerarquia de dibuix. */
    public static boolean once = true;

    /**
     * [CA] Dibuixa la tecla xilofon amb el color actual, el nom de nota i el slide fill.
     * Si la nota és la tonalitat actual, dibuixa un triangle indicador.
     * <p>
     * [EN] Draw XiloKey with the current colour, the note name and the child slide.
     * If the note is the current key note, draws a pointer triangle.
     *
     * @param g [CA] context gràfic / [EN] graphics context
     */
    @Override
    public void draw(Graphics2D g) {
        if (once){
            if (Settings.SHOW_DRAW_HIERARCHY){
                Utilities.printOutWithPriority(5, "MyXylokey::draw: drawing "+this.getClass());
            }
        }
        once = false;
        aux++;
        int posY = (int) (screenPosY + height * 0.8);
        g.setColor(this.color);
        int h = (int) Math.ceil(height);
        g.fillRect((int) screenPosX,(int) screenPosY, (int) Math.ceil(width), (int) Math.ceil(height));
        String text;
        if (this.controller.isDrumsMode()) {
            int drumMidi = ToneRange.getDrumMidi(this.keyId);
            text = (drumMidi >= 0) ? " " + ToneRange.getDrumShortName(drumMidi) : "";
        } else {
            String nameOnly = this.noteName.replaceAll("[0-9]+$", "");
            text = " " + nameOnly;
            if (!ToneRange.isMetallophone()) {
                if (this.midi == ToneRange.getHighestSaxo()
                        || this.midi == ToneRange.getLowestSaxo()) {
                    text += ">";
                }
            }
        }
        g.setColor(activeChannels.isEmpty() ? ColorSets.getGridSquareFontColor(this.midi) : java.awt.Color.BLACK);
        g.drawString(text, (float) (screenPosX), posY);
        this.slide.draw(g);
        if (!this.controller.isDrumsMode()) {
            int midiKey = this.controller.getAllPurposeScore().getMidiKey();
            if (this.midi % 12 == midiKey % 12) {
                int triH = Math.max(5, (int)(height * 0.55));
                int tip = (int)(screenPosX + width) - 2;
                int cy = (int)(screenPosY + height / 2);
                int[] xp = { tip - triH, tip - triH, tip };
                int[] yp = { cy - triH / 2, cy + triH / 2, cy };
                g.setColor(ColorSets.getGridSquareFontColor(this.midi));
                g.fillPolygon(xp, yp, 3);
            }
        }
    }

    /**
     * [CA] Estableix si la tecla ha de destacar-se quan reprodueix.
     * Quan el teclat és a la dreta (mode endevina), no es ressalta.
     * <p>
     * [EN] Sets whether the key should highlight when playing.
     * When the keyboard is on the right (guessing mode), it is not highlighted.
     *
     * @param keyboardRight [CA] true si el teclat és a la dreta / [EN] true if keyboard is on the right
     */
    public void doNotHighlight(boolean keyboardRight) {
        this.doNotHighlight = keyboardRight;
    }

    /**
     * [CA] Canvia el color a "reproduint" i toca la nota MIDI o el so de mostra.
     * <p>
     * [EN] Changes the colour to "playing" and plays the MIDI note or sample sound.
     *
     * @param channel  [CA] canal MIDI / [EN] MIDI channel
     * @param velocity [CA] velocitat (0-127) / [EN] velocity (0-127)
     */
    public void play(int channel,int velocity) {
        if (!this.doNotHighlight) {
            this.color = this.color_playing;
        }
        if (!this.isPlaying(channel)) {
            if (SampleOrMidi.isMidi()) {
                int midiToPlay = controller.isDrumsMode() ? ToneRange.getDrumMidi(this.keyId) : this.midi;
                if (midiToPlay < 0) return;
                SoundWithMidi.play(midiToPlay, channel, velocity);
            } else {
                /**
                 * Plays sound sample.
                 */
                this.so.play();
            }
            this.activeChannels.add(channel);
        }
    }

    /**
     * [CA] Retorna l'identificador de la tecla.
     * <p>
     * [EN] Returns the key identifier.
     *
     * @return [CA] id de la tecla / [EN] key id
     */
    public int getKeyId() {
        return keyId;
    }

    /**
     * [CA] Retorna el slide associat a la tecla.
     * <p>
     * [EN] Returns the slide associated with the key.
     *
     * @return [CA] slide fill / [EN] child slide
     */
    public MySlide getSlide() {
        return slide;
    }

    /**
     * [CA] Toca la nota amb el MIDI real especificat (usat per transposicions d'instruments).
     * <p>
     * [EN] Plays the note with the specified real MIDI note (used for instrument transpositions).
     *
     * @param realMidi [CA] nota MIDI real a tocar / [EN] real MIDI note to play
     * @param channel  [CA] canal MIDI / [EN] MIDI channel
     * @param velocity [CA] velocitat (0-127) / [EN] velocity (0-127)
     */
    public void playWithMidi(int realMidi, int channel, int velocity) {
        if (!this.doNotHighlight) {
            this.color = this.color_playing;
        }
        if (!this.isPlaying(channel)) {
            if (SampleOrMidi.isMidi()) {
                SoundWithMidi.play(realMidi, channel, velocity);
            } else {
                this.so.play();
            }
            this.channelRealMidi.put(channel, realMidi);
            this.activeChannels.add(channel);
        }
    }

    /**
     * [CA] Canvia el color a "silenci" i atura el so del canal indicat.
     * <p>
     * [EN] Changes the colour to "silent" and stops the sound for the given channel.
     *
     * @param channel [CA] canal MIDI que s'atura / [EN] MIDI channel to stop
     */
    public void stop(int channel) {
        if (this.isPlaying(channel)) {
            if (SampleOrMidi.isMidi()) {
                int midiToStop = channelRealMidi.getOrDefault(channel, this.midi);
                SoundWithMidi.stop(midiToStop, channel);
                channelRealMidi.remove(channel);
            } else {
                this.so.stop();
            }
            this.activeChannels.remove(channel);
            if (this.activeChannels.isEmpty()) {
                this.color = this.color_silent;
            }
        }
    }

    /**
     * [CA] Atura tots els canals actius d'aquesta tecla i retorna el nombre de canals aturats.
     * <p>
     * [EN] Stops all active channels of this key and returns the number of stopped channels.
     *
     * @return [CA] nombre de canals que estaven actius / [EN] number of channels that were active
     */
    public int stopAllChannels() {
        int count = this.activeChannels.size();
        if (count==0) return 0;
        for (int channel:this.activeChannels) {
            if (SampleOrMidi.isMidi()) {
                int midiToStop = channelRealMidi.getOrDefault(channel, this.midi);
                SoundWithMidi.stop(midiToStop, channel);
            } else {
                this.so.stop();
            }
        }
        this.activeChannels.clear();
        this.channelRealMidi.clear();
        this.color = this.color_silent;
        return count;
    }

    /**
     * [CA] Retorna una còpia del conjunt de canals actius.
     * <p>
     * [EN] Returns a copy of the active channels set.
     *
     * @return [CA] conjunt de canals MIDI actius / [EN] set of active MIDI channels
     */
    public Set<Integer> getActiveChannels() {
        return new HashSet<>(activeChannels);
    }

    /**
     * [CA] Retorna el so de mostra associat a la tecla (quan no s'usa MIDI).
     * <p>
     * [EN] Returns the sample sound associated with the key (when not using MIDI).
     *
     * @return [CA] so de mostra / [EN] sample sound
     */
    public Sound getSo() {
        return so;
    }

    /**
     * [CA] Retorna la posició de la fila de la tecla en el teclat.
     * <p>
     * [EN] Returns the row position of the key in the keyboard.
     *
     * @return [CA] fila de la tecla (primera = 0) / [EN] key row (first = 0)
     */
    public int getRow() {
        return row;
    }

    /**
     * [CA] Retorna la nota MIDI associada a la tecla.
     * <p>
     * [EN] Returns the MIDI note associated with the key.
     *
     * @return [CA] nota MIDI / [EN] MIDI note
     */
    public int getMidi() {
        return midi;
    }

    /**
     * [CA] Retorna el nom de la nota associada a la tecla.
     * <p>
     * [EN] Returns the note name associated with the key.
     *
     * @return [CA] nom de la nota / [EN] note name
     */
    public String getNoteName() {
        return noteName;
    }

    /**
     * [CA] Retorna si un canal determinat s'està reproduint en aquesta tecla.
     * <p>
     * [EN] Returns whether a given channel is currently playing on this key.
     *
     * @param channel [CA] canal MIDI a comprovar / [EN] MIDI channel to check
     * @return [CA] true si el canal és actiu / [EN] true if the channel is active
     */
    public boolean isPlaying(int channel) {
        boolean playing = this.activeChannels.contains(channel);
        return playing;
    }

}
