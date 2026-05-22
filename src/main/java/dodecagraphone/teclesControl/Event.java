/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.teclesControl;

/**
 * [CA] Classe base abstracta per a tots els events del sistema undo/redo.
 * Cada subclasse encapsula una acció reversible i implementa {@code refer()}
 * per reaplicar-la i {@code desfer()} per revertir-la.
 * <p>
 * [EN] Abstract base class for all undo/redo events.
 * Each subclass encapsulates a reversible action and implements {@code refer()}
 * to re-apply it and {@code desfer()} to undo it.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public abstract class Event {

    /**
     * [CA] Reaplicar l'acció (redo). Ha de restaurar l'estat com si l'acció
     * s'hagués executat de nou.
     * <p>
     * [EN] Re-applies the action (redo). Must restore the state as if the
     * action had been executed again.
     */
    public abstract void refer();

    /**
     * [CA] Revertir l'acció (undo). Ha de restaurar l'estat previ a l'acció.
     * <p>
     * [EN] Reverts the action (undo). Must restore the state prior to the action.
     */
    public abstract void desfer();
}
