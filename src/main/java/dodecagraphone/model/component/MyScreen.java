package dodecagraphone.model.component;

import dodecagraphone.MyController;

/**
 * The screen is located at position (0,0) of the JPanel window, and it is
 * composed of several subcomponents. The screen is the topmost MyComponent, and
 * its father is null. In the current version (2.0), the screen is composed of a
 * keyboard component, a camera component, a control component and a status line
 * component (each of them is an extension of MyComponent).
 *
 * @author pau
 */
public class MyScreen extends MyComponent {

    /**
     *
     * @param firstCol
     * @param firstRow
     * @param width
     * @param height
     * @param parent, should be null.
     * @param contr
     */
    public MyScreen(int firstCol, int firstRow, int nCols, int nRows,
            MyComponent parent, MyController contr) {
        super(firstCol, firstRow, nCols, nRows, parent, contr);
        //this.setNeedsDrawing(true);
    }

}
