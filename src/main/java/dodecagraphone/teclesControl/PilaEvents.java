/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.teclesControl;

/**
 * [CA] Pila d'events undo/redo del sistema. Manté dues piles internes: una per
 * a undo i una per a redo. Quan s'afegeix un nou event, la pila de redo es
 * buida per evitar arbres de canvis divergents.
 * <p>
 * [EN] Undo/redo event stack for the system. Maintains two internal stacks: one
 * for undo and one for redo. When a new event is added the redo stack is cleared
 * to avoid diverging change trees.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
import java.util.Stack;

public class PilaEvents {
    private final Stack<Event> pilaUndo = new Stack<>();
    private final Stack<Event> pilaRedo = new Stack<>();

    /**
     * [CA] Afegeix un nou event a la pila de undo i buida la pila de redo.
     * <p>
     * [EN] Adds a new event to the undo stack and clears the redo stack.
     *
     * @param e [CA] l'event a afegir / [EN] the event to add
     */
    public void afegirEvent(Event e) {
        pilaUndo.push(e);
        pilaRedo.clear(); // esborrem redo si fem una acció nova
    }

    /**
     * [CA] Desfà l'últim event: extreu de la pila undo, crida {@code desfer()}
     * i empeny el resultat a la pila redo.
     * <p>
     * [EN] Undoes the last event: pops from the undo stack, calls {@code desfer()}
     * and pushes the event onto the redo stack.
     */
    public void undo() {
        if (!pilaUndo.isEmpty()) {
//            System.out.println("Undo");
            Event e = pilaUndo.pop();
            e.desfer();
            pilaRedo.push(e);
        }
    }

    /**
     * [CA] Refà l'últim event desfet: extreu de la pila redo, crida {@code refer()}
     * i empeny el resultat a la pila undo.
     * <p>
     * [EN] Redoes the last undone event: pops from the redo stack, calls {@code refer()}
     * and pushes the event back onto the undo stack.
     */
    public void redo() {
        if (!pilaRedo.isEmpty()) {
//            System.out.println("Redo");
            Event e = pilaRedo.pop();
            e.refer();
            pilaUndo.push(e);
        }
    }

    /**
     * [CA] Comprova si la pila de undo és buida.
     * <p>
     * [EN] Checks whether the undo stack is empty.
     *
     * @return [CA] {@code true} si no hi ha res a desfer / [EN] {@code true} if there is nothing to undo
     */
    public boolean isUndoEmpty() {
        return pilaUndo.isEmpty();
    }

    /**
     * [CA] Buida completament les dues piles (undo i redo).
     * <p>
     * [EN] Clears both stacks completely (undo and redo).
     */
    public void buidar() {
        pilaUndo.clear();
        pilaRedo.clear();
    }
}
