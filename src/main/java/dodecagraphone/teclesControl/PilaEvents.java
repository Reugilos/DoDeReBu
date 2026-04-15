package dodecagraphone.teclesControl;

/**
 *
 * @author grogmgpt
 */
import java.util.Stack;

public class PilaEvents {
    private final Stack<Event> pilaUndo = new Stack<>();
    private final Stack<Event> pilaRedo = new Stack<>();

    public void afegirEvent(Event e) {
        pilaUndo.push(e);
        pilaRedo.clear(); // esborrem redo si fem una acció nova
    }

    public void undo() {
        if (!pilaUndo.isEmpty()) {
//            System.out.println("Undo");
            Event e = pilaUndo.pop();
            e.desfer();
            pilaRedo.push(e);
        }
    }

    public void redo() {
        if (!pilaRedo.isEmpty()) {
//            System.out.println("Redo");
            Event e = pilaRedo.pop();
            e.refer();
            pilaUndo.push(e);
        }
    }

    public void buidar() {
        pilaUndo.clear();
        pilaRedo.clear();
    }
}
