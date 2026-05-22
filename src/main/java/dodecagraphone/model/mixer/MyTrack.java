/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.mixer;

import dodecagraphone.model.sound.SoundWithMidi;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [CA] Representa una pista (track) individual del mesclador de l'aplicació.
 * Cada pista té un nom, una llista de canals MIDI associats, un canal actiu,
 * un volum (velocitat), i flags de visibilitat, audibilitat, selecció i
 * marcatge. Les pistes s'usen per organitzar les notes de la partitura en
 * capes independents que es poden silenciar o amagar individualment.
 * <p>
 * [EN] Represents an individual track of the application mixer.
 * Each track has a name, a list of associated MIDI channels, an active channel,
 * a volume (velocity), and flags for visibility, audibility, selection and
 * marking. Tracks are used to organize score notes into independent layers
 * that can be muted or hidden individually.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MyTrack {
    private int id;
    private String nomPista;
    private List<Integer> canals;
    private int currentChannel;
    private boolean dotted;
    private long nNotes;
    private int velocity;
    private boolean keepNoteVelocity;
    private boolean selected; // Nou camp per indicar si és la pista seleccionada
    private boolean deleted;
    private boolean visible;
    private boolean audible;
    private boolean isNew = false;
    private int displayOffset = 0;
    private boolean displayOffsetFromMetadata = false;

    /**
     * [CA] Crea una nova pista amb l'identificador i el nom indicats.
     * La pista s'inicialitza com a visible, audible, no seleccionada,
     * no esborrada, amb velocitat màxima (127) i sense notes.
     * <p>
     * [EN] Creates a new track with the given identifier and name.
     * The track is initialized as visible, audible, not selected,
     * not deleted, with maximum velocity (127) and no notes.
     *
     * @param id       [CA] identificador numèric de la pista / [EN] numeric track identifier
     * @param nomPista [CA] nom de la pista / [EN] track name
     */
    public MyTrack(int id, String nomPista) {
        this.id = id;
        this.nomPista = nomPista;
        this.canals = new ArrayList<>();
        this.currentChannel = -1;
        this.dotted = false;
        this.nNotes = 0;
        this.velocity = 127;
        this.keepNoteVelocity = false;
        this.selected = false;
        this.deleted = false;
        this.visible = true;
        this.audible = true;
        this.isNew = false;
    }

    /**
     * [CA] Estableix l'identificador numèric de la pista.
     * <p>
     * [EN] Sets the numeric identifier of the track.
     *
     * @param id [CA] nou identificador / [EN] new identifier
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * [CA] Indica si la pista és visible a la partitura.
     * <p>
     * [EN] Returns whether the track is visible in the score.
     *
     * @return {@code true} si la pista és visible / {@code true} if the track is visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * [CA] Estableix la visibilitat de la pista a la partitura.
     * <p>
     * [EN] Sets the visibility of the track in the score.
     *
     * @param visible [CA] {@code true} per mostrar la pista / [EN] {@code true} to show the track
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * [CA] Indica si la pista és audible durant la reproducció.
     * <p>
     * [EN] Returns whether the track is audible during playback.
     *
     * @return {@code true} si la pista és audible / {@code true} if the track is audible
     */
    public boolean isAudible() {
        return audible;
    }

    /**
     * [CA] Estableix si la pista és audible durant la reproducció (mute/unmute).
     * <p>
     * [EN] Sets whether the track is audible during playback (mute/unmute).
     *
     * @param audible [CA] {@code true} per activar el so de la pista / [EN] {@code true} to unmute the track
     */
    public void setAudible(boolean audible) {
        this.audible = audible;
    }

    /**
     * [CA] Indica si la pista ha estat marcada com a esborrada.
     * <p>
     * [EN] Returns whether the track has been marked as deleted.
     *
     * @return {@code true} si la pista és esborrada / {@code true} if the track is deleted
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * [CA] Marca la pista com a esborrada i reinicia el comptador de notes a zero.
     * <p>
     * [EN] Marks the track as deleted and resets the note counter to zero.
     *
     * @param deleted [CA] {@code true} per marcar com a esborrada / [EN] {@code true} to mark as deleted
     */
    public void setDeleted(boolean deleted) {
        this.nNotes = 0;
        this.deleted = deleted;
    }

    /**
     * [CA] Indica si la pista és la pista seleccionada actualment al mesclador.
     * <p>
     * [EN] Returns whether the track is the currently selected track in the mixer.
     *
     * @return {@code true} si la pista és seleccionada / {@code true} if the track is selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * [CA] Estableix si la pista és la seleccionada al mesclador.
     * <p>
     * [EN] Sets whether the track is the selected one in the mixer.
     *
     * @param selected [CA] {@code true} per seleccionar la pista / [EN] {@code true} to select the track
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * [CA] Indica si la pista conserva la velocitat original de cada nota
     * en lloc d'aplicar la velocitat global de la pista.
     * <p>
     * [EN] Returns whether the track keeps the original velocity of each note
     * instead of applying the track's global velocity.
     *
     * @return {@code true} si es conserva la velocitat individual de les notes /
     *         {@code true} if individual note velocity is preserved
     */
    public boolean isKeepNoteVelocity() {
        return keepNoteVelocity;
    }

    /**
     * [CA] Estableix si la pista ha de conservar la velocitat original de cada nota.
     * <p>
     * [EN] Sets whether the track should keep the original velocity of each note.
     *
     * @param keepNoteVelocity [CA] {@code true} per conservar la velocitat de cada nota /
     *                         [EN] {@code true} to keep individual note velocity
     */
    public void setKeepNoteVelocity(boolean keepNoteVelocity) {
        this.keepNoteVelocity = keepNoteVelocity;
    }

    /**
     * [CA] Indica si la pista té el marcatge de punt activat (flag visual «dotted»).
     * <p>
     * [EN] Returns whether the track has the dot marking enabled (visual «dotted» flag).
     *
     * @return {@code true} si la pista és marcada amb punt / {@code true} if the track is dotted
     */
    public boolean isDotted() {
        return dotted;
    }

    /**
     * [CA] Estableix el marcatge de punt de la pista.
     * <p>
     * [EN] Sets the dot marking of the track.
     *
     * @param dotted [CA] {@code true} per activar el marcatge / [EN] {@code true} to enable the marking
     */
    public void setDotted(boolean dotted) {
        this.dotted = dotted;
    }

    /**
     * [CA] Indica si la pista és nova (creada però sense notes encara).
     * <p>
     * [EN] Returns whether the track is new (created but without notes yet).
     *
     * @return {@code true} si la pista és nova / {@code true} if the track is new
     */
    public boolean isIsNew() {
        return isNew;
    }

    /**
     * [CA] Estableix si la pista és nova.
     * <p>
     * [EN] Sets whether the track is new.
     *
     * @param isNew [CA] {@code true} per marcar la pista com a nova / [EN] {@code true} to mark the track as new
     */
    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    /**
     * [CA] Retorna la velocitat (volum) global de la pista (0–127).
     * <p>
     * [EN] Returns the global velocity (volume) of the track (0–127).
     *
     * @return [CA] velocitat global de la pista / [EN] global track velocity
     */
    public int getVelocity() {
        return velocity;
    }

    /**
     * [CA] Estableix la velocitat (volum) global de la pista.
     * <p>
     * [EN] Sets the global velocity (volume) of the track.
     *
     * @param velocity [CA] velocitat MIDI (0–127) / [EN] MIDI velocity (0–127)
     */
    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    /**
     * [CA] Retorna el nombre de notes de la pista.
     * <p>
     * [EN] Returns the number of notes in the track.
     *
     * @return [CA] nombre de notes / [EN] number of notes
     */
    public long getnNotes() {
        return nNotes;
    }

    /**
     * [CA] Incrementa el comptador de notes en una unitat i marca la pista
     * com a no-nova (ja té contingut).
     * <p>
     * [EN] Increments the note counter by one and marks the track as
     * non-new (it now has content).
     */
    public void oneNoteMore() {
        this.isNew = false;
        this.nNotes++;
    }

    /**
     * [CA] Decrementa el comptador de notes en una unitat. Si arriba a zero,
     * torna a marcar la pista com a nova.
     * <p>
     * [EN] Decrements the note counter by one. If it reaches zero,
     * marks the track as new again.
     */
    public void oneNoteLess() {
        this.nNotes--;
        if (this.nNotes == 0) isNew = true;
    }

    /**
     * [CA] Indica si la pista no conté cap nota.
     * <p>
     * [EN] Returns whether the track contains no notes.
     *
     * @return {@code true} si la pista és buida / {@code true} if the track is empty
     */
    public boolean isEmpty() {
        return this.nNotes <= 0;
    }

    /**
     * [CA] Estableix directament el nombre de notes de la pista.
     * <p>
     * [EN] Directly sets the number of notes in the track.
     *
     * @param nNotes [CA] nombre de notes / [EN] number of notes
     */
    public void setnNotes(long nNotes) {
        this.nNotes = nNotes;
    }

    /**
     * [CA] Inverteix l'estat del marcatge de punt de la pista i imprimeix
     * un missatge de depuració per consola.
     * <p>
     * [EN] Toggles the dot marking state of the track and prints a
     * debug message to the console.
     */
    public void toggleDotted() {
        this.dotted = !this.dotted;
        System.out.println("MyTrack::toggleDotted(): " + this.getName() + " " + this.dotted);
    }

    /**
     * [CA] Retorna l'identificador numèric de la pista.
     * <p>
     * [EN] Returns the numeric identifier of the track.
     *
     * @return [CA] identificador de la pista / [EN] track identifier
     */
    public int getId() {
        return id;
    }

    /**
     * [CA] Retorna el nom de la pista.
     * <p>
     * [EN] Returns the name of the track.
     *
     * @return [CA] nom de la pista / [EN] track name
     */
    public String getName() {
        return nomPista;
    }

    /**
     * [CA] Estableix el nom de la pista.
     * <p>
     * [EN] Sets the name of the track.
     *
     * @param nomPista [CA] nou nom de la pista / [EN] new track name
     */
    public void setName(String nomPista) {
        this.nomPista = nomPista;
    }

    /**
     * [CA] Retorna la llista de canals MIDI associats a aquesta pista.
     * <p>
     * [EN] Returns the list of MIDI channels associated with this track.
     *
     * @return [CA] llista de canals MIDI / [EN] list of MIDI channels
     */
    public List<Integer> getCanals() {
        return canals;
    }

    /**
     * [CA] Retorna una cadena de text que mostra tots els canals i instruments
     * de la pista, amb el canal actiu marcat amb {@code >>}.
     * <p>
     * [EN] Returns a string showing all channels and instruments of the track,
     * with the active channel marked with {@code >>}.
     *
     * @return [CA] representació textual de canals i instruments / [EN] textual representation of channels and instruments
     */
    public String toStringCanalsInstruments() {
        Map<Integer, String> canalsInstrs = toMapCanalsInstruments();
        StringBuilder cadena = new StringBuilder("{");
        int i = 0;
        for (int canal : canalsInstrs.keySet()) {
            if (canal == this.currentChannel) cadena.append(">>");
            cadena.append(canal).append(":").append(canalsInstrs.get(canal));
            if (i < canalsInstrs.size() - 1) cadena.append(", ");
            i++;
        }
        cadena.append("}");
        return cadena.toString();
    }

    /**
     * [CA] Retorna un mapa que associa cada canal MIDI de la pista amb el mnemònic
     * de l'instrument assignat a aquell canal.
     * <p>
     * [EN] Returns a map associating each MIDI channel of the track with the mnemonic
     * of the instrument assigned to that channel.
     *
     * @return [CA] mapa canal → mnemònic d'instrument / [EN] map channel → instrument mnemonic
     */
    public Map<Integer, String> toMapCanalsInstruments() {
        HashMap<Integer, String> canalsInstrs = new HashMap<>();
        for (int chan : this.canals) {
            canalsInstrs.put(chan, SoundWithMidi.getInstrumentMnemonic(SoundWithMidi.getInstrumentInChannel(chan)));
        }
        return canalsInstrs;
    }

    /**
     * [CA] Afegeix un canal MIDI a la pista si encara no hi és.
     * <p>
     * [EN] Adds a MIDI channel to the track if it is not already present.
     *
     * @param canal [CA] índex del canal MIDI a afegir (0–15) / [EN] MIDI channel index to add (0–15)
     */
    public final void afegirCanal(int canal) {
        if (!canals.contains(canal)) canals.add(canal);
    }

    /**
     * [CA] Retorna el canal MIDI actiu de la pista (el que s'usa per a la reproducció).
     * Retorna {@code -1} si no hi ha cap canal actiu assignat.
     * <p>
     * [EN] Returns the active MIDI channel of the track (the one used for playback).
     * Returns {@code -1} if no active channel is assigned.
     *
     * @return [CA] índex del canal actiu, o {@code -1} / [EN] active channel index, or {@code -1}
     */
    public int getCurrentChannel() {
        return currentChannel;
    }

    /**
     * [CA] Estableix el canal MIDI actiu de la pista.
     * <p>
     * [EN] Sets the active MIDI channel of the track.
     *
     * @param currentChannel [CA] índex del canal MIDI (0–15) / [EN] MIDI channel index (0–15)
     */
    public final void setCurrentChannel(int currentChannel) {
        this.currentChannel = currentChannel;
    }

    /**
     * [CA] Retorna el desplaçament de visualització de la pista (en columnes de partitura).
     * <p>
     * [EN] Returns the display offset of the track (in score columns).
     *
     * @return [CA] desplaçament de visualització en columnes / [EN] display offset in columns
     */
    public int getDisplayOffset() {
        return displayOffset;
    }

    /**
     * [CA] Estableix el desplaçament de visualització de la pista.
     * <p>
     * [EN] Sets the display offset of the track.
     *
     * @param displayOffset [CA] desplaçament en columnes de partitura / [EN] offset in score columns
     */
    public void setDisplayOffset(int displayOffset) {
        this.displayOffset = displayOffset;
    }

    /**
     * [CA] Indica si el desplaçament de visualització prové de metadades MIDI
     * (en lloc d'haver-se establert manualment).
     * <p>
     * [EN] Returns whether the display offset comes from MIDI metadata
     * (instead of having been set manually).
     *
     * @return {@code true} si el desplaçament prové de metadades /
     *         {@code true} if the offset comes from metadata
     */
    public boolean isDisplayOffsetFromMetadata() {
        return displayOffsetFromMetadata;
    }

    /**
     * [CA] Estableix el desplaçament de visualització a partir de metadades MIDI
     * i marca el flag corresponent.
     * <p>
     * [EN] Sets the display offset from MIDI metadata and marks the corresponding flag.
     *
     * @param displayOffset [CA] desplaçament en columnes de partitura / [EN] offset in score columns
     */
    public void setDisplayOffsetFromMetadata(int displayOffset) {
        this.displayOffset = displayOffset;
        this.displayOffsetFromMetadata = true;
    }
}
