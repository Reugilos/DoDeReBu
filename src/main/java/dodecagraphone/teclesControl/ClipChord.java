/*
 * PolyForm Noncommercial License 1.0.0
 * Copyright (c) 2024-2026 Pau Bofill. Powered by Claude AI.
 * Full license / Llicència completa: LICENSE (project root / arrel del projecte)
 */
package dodecagraphone.teclesControl;

import dodecagraphone.model.chord.Chord;

/**
 * [CA] Representa un acord emmagatzemat al porta-retalls. El desplaçament de
 * columna ({@code colOffset}) és relatiu a la columna esquerra de la selecció.
 * <p>
 * [EN] Represents a chord stored in the clipboard. The column offset
 * ({@code colOffset}) is relative to the left column of the selection.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class ClipChord {
    /** Desplaçament de columna respecte la cantonada esquerra de la selecció. */
    public final int colOffset;
    /** L'acord copiat. */
    public final Chord chord;

    public ClipChord(int colOffset, Chord chord) {
        this.colOffset = colOffset;
        this.chord     = chord;
    }
}
