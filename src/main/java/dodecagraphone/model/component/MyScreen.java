/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.component;

import dodecagraphone.MyController;

/**
 * [CA] La pantalla es troba a la posició (0,0) del JPanel i es compon de diversos
 * subcomponents. És el {@code MyComponent} arrel de l'arbre i el seu pare és
 * {@code null}. En la versió actual (2.0), la pantalla es compon d'un component
 * de teclat, un de càmera, un de control i un de línia d'estat (cadascun és una
 * extensió de {@code MyComponent}).
 * <p>
 * [EN] The screen is located at position (0,0) of the JPanel window, and it is
 * composed of several subcomponents. The screen is the topmost MyComponent, and
 * its parent is null. In the current version (2.0), the screen is composed of a
 * keyboard component, a camera component, a control component and a status line
 * component (each of them is an extension of {@code MyComponent}).
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MyScreen extends MyComponent {

    /**
     * [CA] Constructor. Estableix les dimensions de la pantalla. El pare ha de ser
     * {@code null} perquè la pantalla és el component arrel.
     * <p>
     * [EN] Constructor. Sets the screen dimensions. The parent should be
     * {@code null} because the screen is the root component.
     *
     * @param firstCol [CA] primera columna (normalment 0) / [EN] first column (normally 0)
     * @param firstRow [CA] primera fila (normalment 0) / [EN] first row (normally 0)
     * @param nCols    [CA] nombre total de columnes / [EN] total number of columns
     * @param nRows    [CA] nombre total de files / [EN] total number of rows
     * @param parent   [CA] hauria de ser null / [EN] should be null
     * @param contr    [CA] referència al controlador / [EN] reference to the controller
     */
    public MyScreen(int firstCol, int firstRow, int nCols, int nRows,
            MyComponent parent, MyController contr) {
        super(firstCol, firstRow, nCols, nRows, parent, contr);
    }

}
