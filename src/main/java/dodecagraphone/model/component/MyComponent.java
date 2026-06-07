/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * [CA] Classe base abstracta per a tots els components visuals de l'aplicació.
 * La pantalla de l'aplicació és un arbre de MyComponent: la pantalla té
 * subcomponents fills i cada component pot tenir altres subcomponents.
 * Cada component coneix la seva posició absoluta i mida a la pantalla
 * (en píxels, des de dalt-esquerra), i també coneix la seva primera fila
 * i columna dins del pare (dalt-esquerra).
 * <p>
 * [EN] Abstract base class for all visual components of the application.
 * The application screen is a tree of MyComponent: the screen has child
 * subcomponents, and each component can have other subcomponents. Each
 * component knows its absolute position and size on the screen (in pixels
 * starting at the top-left), and it also knows its first row and column
 * in the parent (top-left).
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public abstract class MyComponent {

    protected List<MyComponent> subComponents;
    /**
     * Absolute position on the screen in pixels (top left = [0,0])
     */
    protected double screenPosX, screenPosY, width, height;
    /**
     * Relative position in the parent, in colums and rows (top left = [0,0])
     */
    protected int parentFirstCol, parentFirstRow, nCols, nRows;
    /**
     * The parent Component (this component is a child of the parent)
     */
    protected MyComponent parent;
    /**
     * Acces to the members of the controller.
     */
    protected MyController controller;

//    protected boolean needsDrawing;

    /**
     * [CA] Constructor que estableix la posició relativa al pare i les dimensions.
     * Si el pare és null (cas MyScreen), la posició de pantalla es calcula des de (0,0).
     * <p>
     * [EN] Constructor that sets the position relative to the parent and dimensions.
     * If parent is null (MyScreen case), screen position is calculated from (0,0).
     *
     * @param firstCol [CA] primera columna relativa al pare / [EN] first column relative to parent
     * @param firstRow [CA] primera fila relativa al pare / [EN] first row relative to parent
     * @param nCols    [CA] nombre de columnes / [EN] number of columns
     * @param nRows    [CA] nombre de files / [EN] number of rows
     * @param parent   [CA] component pare (null per a MyScreen) / [EN] parent component (null for MyScreen)
     * @param contr    [CA] referència al controlador principal / [EN] reference to the main controller
     */
    public MyComponent(int firstCol, int firstRow, int nCols, int nRows, MyComponent parent, MyController contr) {
        this.parentFirstCol = firstCol;
        this.parentFirstRow = firstRow;
        this.nCols = nCols;
        this.nRows = nRows;
        if (parent == null) {
            /**
             * The screen's parent is null.
             */
            this.screenPosX = firstCol * Settings.getColWidth();
            this.screenPosY = firstRow * Settings.getRowHeight();
        } else {
            this.screenPosX = parent.screenPosX + firstCol * Settings.getColWidth();
            this.screenPosY = parent.screenPosY + firstRow * Settings.getRowHeight();
        }
        this.width = nCols * Settings.getColWidth();
        this.height = nRows * Settings.getRowHeight();
        this.parent = parent;
        this.controller = contr;
        this.subComponents = new ArrayList<>();
//        this.needsDrawing = true;
    }

//    public boolean isNeedsDrawing() {
//        return needsDrawing;
//    }
//
//    public void setNeedsDrawing(boolean needsDrawing) {
//        this.needsDrawing = needsDrawing;
//    }
//
    /**
     * [CA] Estableix la posició i les dimensions del component.
     * Recalcula les coordenades de pantalla a partir de la posició del pare.
     * <p>
     * [EN] Sets the position and dimensions of the component.
     * Recalculates screen coordinates from the parent position.
     *
     * @param firstCol [CA] primera columna relativa al pare / [EN] first column relative to parent
     * @param firstRow [CA] primera fila relativa al pare / [EN] first row relative to parent
     * @param nCols    [CA] nombre de columnes / [EN] number of columns
     * @param nRows    [CA] nombre de files / [EN] number of rows
     */
    public void setDimensions(int firstCol, int firstRow, int nCols, int nRows) {
        this.parentFirstCol = firstCol;
        this.parentFirstRow = firstRow;
        this.nCols = nCols;
        this.nRows = nRows;
        if (parent == null) {
            this.screenPosX = firstCol * Settings.getColWidth();
            this.screenPosY = firstRow * Settings.getRowHeight();
        } else {
            this.screenPosX = parent.screenPosX + firstCol * Settings.getColWidth();
            this.screenPosY = parent.screenPosY + firstRow * Settings.getRowHeight();
        }
        this.width = nCols * Settings.getColWidth();
        this.height = nRows * Settings.getRowHeight();
    }

    /**
     * [CA] Retorna la posició X de pantalla d'una columna local.
     * <p>
     * [EN] Get the screen X position of a local column.
     *
     * @param col [CA] la primera columna del component és 0 / [EN] the first column of the component is 0
     * @return [CA] coordenada X absoluta en píxels / [EN] absolute X coordinate in pixels
     */
    public double getScreenX(int col) {
        double screenX = screenPosX + col * Settings.getColWidth();
        return screenX;
    }

    /**
     * [CA] Retorna la posició Y de pantalla d'una fila local.
     * <p>
     * [EN] Get the screen Y position of a local row.
     *
     * @param row [CA] la primera fila del component és 0 / [EN] the first row of the component is 0
     * @return [CA] coordenada Y absoluta en píxels / [EN] absolute Y coordinate in pixels
     */
    public double getScreenY(int row) {
        double screenY = screenPosY + row * Settings.getRowHeight();
        return screenY;
    }

    /**
     * [CA] Donada una posició X de pantalla, retorna la columna local corresponent.
     * <p>
     * [EN] Given an X position of the screen, returns the corresponding local column.
     *
     * @param screenX [CA] posició X absoluta en píxels / [EN] absolute X position in pixels
     * @return [CA] columna local (la primera columna del component és 0) / [EN] local column (first column is 0)
     */
    public int getCol(double screenX) {
        int col = (int) ((screenX - getScreenPosX()) / Settings.getColWidth());
        return col;
    }

    /**
     * [CA] Dibuixa els components fills.
     * <p>
     * [EN] Draw the child components.
     *
     * @param g [CA] context gràfic on dibuixar / [EN] graphics context to draw on
     */
    public void draw(Graphics2D g) {
//        if (this.needsDrawing){
            if (Settings.SHOW_DRAW_HIERARCHY) Utilities.printOutWithPriority(5, "MyComponent::draw: drawing "+this.getClass());
            for (MyComponent comp : subComponents) {
                comp.draw(g);
            }
//            this.setNeedsDrawing(false);
//        }
    }

    /**
     * [CA] Afegeix un component fill.
     * <p>
     * [EN] Add a child component.
     *
     * @param comp [CA] component a afegir / [EN] component to add
     */
    public void add(MyComponent comp) {
        this.subComponents.add(comp);
    }

    /**
     * [CA] Elimina un component fill.
     * <p>
     * [EN] Remove a child component.
     *
     * @param comp [CA] component a eliminar / [EN] component to remove
     */
    public void remove(MyComponent comp) {
        this.subComponents.remove(comp);
    }

    /**
     * [CA] Comprova si una posició (x,y) de pantalla és dins del component.
     * <p>
     * [EN] Checks whether an (x,y) position in the screen is inside the component.
     *
     * @param x [CA] coordenada X en píxels / [EN] X coordinate in pixels
     * @param y [CA] coordenada Y en píxels / [EN] Y coordinate in pixels
     * @return [CA] true si el punt és dins del component / [EN] true if the point is inside the component
     */
    public boolean contains(double x, double y) {
        boolean cont = true;
        if (x < screenPosX) {
            cont = false;
        }
        if (x > screenPosX + width) {
            cont = false;
        }
        if (y < screenPosY) {
            cont = false;
        }
        if (y > screenPosY + height){
            cont = false;
        }
        return cont;
    }

    /**
     * [CA] Retorna la primera columna relativa al pare.
     * <p>
     * [EN] Returns the first column relative to the parent.
     *
     * @return [CA] primera columna relativa al pare / [EN] first column relative to parent
     */
    public int getFirstParentCol() {
        return parentFirstCol;
    }

    /**
     * [CA] Retorna la primera fila relativa al pare.
     * <p>
     * [EN] Returns the first row relative to the parent.
     *
     * @return [CA] primera fila relativa al pare / [EN] first row relative to parent
     */
    public int getParentFirstRow() {
        return parentFirstRow;
    }

    /**
     * [CA] Retorna el nombre de columnes del component.
     * <p>
     * [EN] Returns the number of columns of the component.
     *
     * @return [CA] nombre de columnes / [EN] number of columns
     */
    public int getnCols() {
        return nCols;
    }

    /**
     * [CA] Estableix el nombre de columnes del component.
     * <p>
     * [EN] Sets the number of columns of the component.
     *
     * @param nCols [CA] nombre de columnes / [EN] number of columns
     */
    public void setnCols(int nCols) {
        this.nCols = nCols;
    }

    /**
     * [CA] Retorna el nombre de files del component.
     * <p>
     * [EN] Returns the number of rows of the component.
     *
     * @return [CA] nombre de files / [EN] number of rows
     */
    public int getnRows() {
        return nRows;
    }

    /**
     * [CA] Retorna la posició X absoluta del component en píxels.
     * <p>
     * [EN] Returns the absolute X position of the component in pixels.
     *
     * @return [CA] posició X de pantalla / [EN] screen X position
     */
    public double getScreenPosX() {
        return screenPosX;
    }

    /**
     * [CA] Retorna la posició Y absoluta del component en píxels.
     * <p>
     * [EN] Returns the absolute Y position of the component in pixels.
     *
     * @return [CA] posició Y de pantalla / [EN] screen Y position
     */
    public double getScreenPosY() {
        return screenPosY;
    }

    /**
     * [CA] Retorna l'amplada del component en píxels.
     * <p>
     * [EN] Returns the width of the component in pixels.
     *
     * @return [CA] amplada en píxels / [EN] width in pixels
     */
    public double getComponentWidth() {
        return width;
    }

    /**
     * [CA] Retorna l'alçada del component en píxels.
     * <p>
     * [EN] Returns the height of the component in pixels.
     *
     * @return [CA] alçada en píxels / [EN] height in pixels
     */
    public double getComponentHeight() {
        return height;
    }

    /**
     * [CA] Retorna el controlador principal associat.
     * <p>
     * [EN] Returns the main controller associated with this component.
     *
     * @return [CA] referència al controlador / [EN] reference to the controller
     */
    public MyController getController() {
        return controller;
    }

    /**
     * [CA] Retorna l'amplada del component en píxels.
     * <p>
     * [EN] Returns the width of the component in pixels.
     *
     * @return [CA] amplada en píxels / [EN] width in pixels
     */
    public double getWidth() {
        return width;
    }

    /**
     * [CA] Retorna l'alçada del component en píxels.
     * <p>
     * [EN] Returns the height of the component in pixels.
     *
     * @return [CA] alçada en píxels / [EN] height in pixels
     */
    public double getHeight() {
        return height;
    }

    /**
     * [CA] Retorna el nom de la classe del component (sense paquet).
     * <p>
     * [EN] Returns the class name of the component (without package).
     *
     * @return [CA] nom simple de la classe / [EN] simple class name
     */
    public String getName() {
        String cl = this.getClass().getName();
        String[] parts = cl.split("\\.");
        String compName = parts[parts.length - 1];
        return compName;
    }

    /**
     * [CA] Mostra l'estructura d'arbre dels components i les seves coordenades.
     * <p>
     * [EN] Shows the tree structure of the components, and their respective coordinates.
     *
     * @param indent [CA] cadena d'indentació inicial / [EN] initial indentation string
     * @return [CA] representació en text de l'arbre / [EN] text representation of the tree
     */
    public String showLayout(String indent) {
        StringBuilder result = new StringBuilder();
        String compName = this.getName();
        result.append(indent).append(compName).append(" ").append(this.toString()).append("\n");
        indent += "   ";
        String previous = "";
        int nSiblings = 0;
        for (MyComponent comp : this.subComponents) {
            if (previous.equals(comp.getName())) {
                nSiblings++;
            }
            if (nSiblings == Settings.NUM_SIBLINGS) {
                result.append(indent).append("...").append("\n");
                break;
            }
            result.append(comp.showLayout(indent));
            previous = comp.getName();
        }
        if (nSiblings > 0) {
            result.append(this.subComponents.get(this.subComponents.size() - 1).showLayout(indent));
        }
        return result.toString();
    }

    /**
     * [CA] Retorna una representació en text de les coordenades i dimensions del component.
     * <p>
     * [EN] Returns a text representation of the component coordinates and dimensions.
     *
     * @return [CA] cadena amb posX, posY, amplada, alçada, firstCol, firstRow, nCols, nRows /
     *         [EN] string with posX, posY, width, height, firstCol, firstRow, nCols, nRows
     */
    @Override
    public String toString() {
        return " {" + "posX=" + (int) screenPosX + ", posY=" + (int) screenPosY +
                ", width=" + (int) width + ", height=" + (int) height + ", firstCol=" + parentFirstCol
                + ", firstRow=" + parentFirstRow + ", nCols=" + nCols + ", nRows=" + nRows + '}';
    }

}
