/*
 * PolyForm Noncommercial License 1.0.0
 * Copyright (c) 2024-2026 Pau Bofill. Powered by Claude AI.
 * Full license / Llicència completa: LICENSE (project root / arrel del projecte)
 */
package dodecagraphone.teclesControl;

import dodecagraphone.MyController;

/**
 * [CA] Event undo/redo per a la transposició de la partitura (botons Trp+/Trp-).
 * Desa el nombre de semitons transposats; {@code refer()} aplica la transposició
 * i {@code desfer()} l'inverteix.
 * <p>
 * [EN] Undo/redo event for score transposition (Trp+/Trp- buttons).
 * Stores the number of transposed semitones; {@code refer()} applies the
 * transposition and {@code desfer()} reverses it.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class TransposeEvent extends Event {

    private final MyController controller;
    private final int step;

    /**
     * [CA] Crea un nou event de transposició.
     * <p>
     * [EN] Creates a new transposition event.
     *
     * @param controller [CA] controlador principal / [EN] main controller
     * @param step       [CA] semitons transposats (positiu = amunt, negatiu = avall) /
     *                   [EN] semitones transposed (positive = up, negative = down)
     */
    public TransposeEvent(MyController controller, int step) {
        this.controller = controller;
        this.step       = step;
    }

    /**
     * [CA] Redo: torna a aplicar la transposició original.
     * <p>
     * [EN] Redo: re-applies the original transposition.
     */
    @Override
    public void refer() {
        controller.transposeWithoutUndo(step);
    }

    /**
     * [CA] Undo: inverteix la transposició.
     * <p>
     * [EN] Undo: reverses the transposition.
     */
    @Override
    public void desfer() {
        controller.transposeWithoutUndo(-step);
    }
}
