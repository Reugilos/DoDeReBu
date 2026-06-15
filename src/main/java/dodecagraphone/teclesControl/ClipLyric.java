/*
 * PolyForm Noncommercial License 1.0.0
 * Copyright (c) 2024-2026 Pau Bofill. Powered by Claude AI.
 * Full license / Llicència completa: LICENSE (project root / arrel del projecte)
 */
package dodecagraphone.teclesControl;

/**
 * [CA] Representa una síl·laba de lletra emmagatzemada al porta-retalls.
 * El desplaçament de columna ({@code colOffset}) és relatiu a la columna
 * esquerra de la selecció copiada.
 * <p>
 * [EN] Represents a lyric syllable stored in the clipboard. The column offset
 * ({@code colOffset}) is relative to the left column of the copied selection.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class ClipLyric {
    /** Desplaçament de columna respecte la cantonada esquerra de la selecció. */
    public final int colOffset;
    /** Identificador de la pista a la qual pertany la síl·laba. */
    public final int trackId;
    /** Text de la síl·laba. */
    public final String text;

    public ClipLyric(int colOffset, int trackId, String text) {
        this.colOffset = colOffset;
        this.trackId   = trackId;
        this.text      = text;
    }
}
