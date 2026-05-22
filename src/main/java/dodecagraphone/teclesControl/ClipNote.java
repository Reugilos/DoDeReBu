/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.teclesControl;

/**
 * [CA] Representa una nota emmagatzemada al porta-retalls (clipboard). Els
 * desplaçaments de fila i columna ({@code rowOffset}, {@code colOffset}) són
 * relatius a la cantonada superior esquerra de la selecció copiada. S'usa amb
 * {@link PasteEvent} per enganxar notes en una nova posició.
 * <p>
 * [EN] Represents a note stored in the clipboard. Row and column offsets
 * ({@code rowOffset}, {@code colOffset}) are relative to the top-left corner
 * of the copied selection. Used with {@link PasteEvent} to paste notes at a
 * new position.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class ClipNote {
    /** [CA] Desplaçament de fila respecte la cantonada superior de la selecció. / [EN] Row offset from the top of the selection. */
    public final int rowOffset;
    /** [CA] Desplaçament de columna respecte la cantonada esquerra de la selecció. / [EN] Column offset from the left of the selection. */
    public final int colOffset;
    /** [CA] Velocitat MIDI de la nota (0-127). / [EN] MIDI velocity of the note (0-127). */
    public final int velocity;
    /** [CA] Indica si la nota és visible a la graella. / [EN] Whether the note is visible in the grid. */
    public final boolean visible;
    /** [CA] Indica si la nota és silenciada (muted). / [EN] Whether the note is muted. */
    public final boolean muted;
    /** [CA] Indica si la nota està lligada a l'anterior (linked). / [EN] Whether the note is linked to the previous one. */
    public final boolean linked;
    /** [CA] Indica si la nota és puntejada (dotted). / [EN] Whether the note is dotted. */
    public final boolean dotted;
    /** Track and channel originaris. Usats en mode multi-track (clipboardMultiTrack). */
    /** [CA] Identificador de la pista d'origen. / [EN] Source track identifier. */
    public final int trackId;
    /** [CA] Canal MIDI d'origen. / [EN] Source MIDI channel. */
    public final int channel;

    /**
     * [CA] Crea una nova nota de porta-retalls amb tots els seus atributs.
     * <p>
     * [EN] Creates a new clipboard note with all its attributes.
     *
     * @param rowOffset [CA] desplaçament de fila / [EN] row offset
     * @param colOffset [CA] desplaçament de columna / [EN] column offset
     * @param velocity  [CA] velocitat MIDI (0-127) / [EN] MIDI velocity (0-127)
     * @param visible   [CA] si és visible / [EN] whether visible
     * @param muted     [CA] si és muted / [EN] whether muted
     * @param linked    [CA] si és linked / [EN] whether linked
     * @param dotted    [CA] si és puntejat / [EN] whether dotted
     * @param trackId   [CA] id de la pista d'origen / [EN] source track id
     * @param channel   [CA] canal MIDI d'origen / [EN] source MIDI channel
     */
    public ClipNote(int rowOffset, int colOffset, int velocity,
                    boolean visible, boolean muted, boolean linked, boolean dotted,
                    int trackId, int channel) {
        this.rowOffset = rowOffset;
        this.colOffset = colOffset;
        this.velocity  = velocity;
        this.visible   = visible;
        this.muted     = muted;
        this.linked    = linked;
        this.dotted    = dotted;
        this.trackId   = trackId;
        this.channel   = channel;
    }
}
