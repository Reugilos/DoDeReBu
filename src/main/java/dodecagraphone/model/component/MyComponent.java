/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * The screen of the application is a tree of MyCmponent. The screen has some
 * child subcomponents, and each component can have other subcomponents. Each
 * component knows its parent component and the parent has a list of
 * subcomponents. Each component knows its absolute position and size in the
 * screen, (in pixels starting at the top-left), and it also knows its first row
 * and column in the parent (top-left).
 *
 * @author pau
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
     * @param firstCol, relative to parent
     * @param firstRow, relative to parent
     * @param nCols
     * @param nRows
     * @param parent
     * @param contr
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
     *
     * @param firstCol
     * @param firstRow
     * @param nCols
     * @param nRows
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
     * Get the screenX position of a local column.
     *
     * @param col (the first column of the component is 0)
     * @return
     */
    public double getScreenX(int col) {
        double screenX = screenPosX + col * Settings.getColWidth();
        return screenX;
    }

    /**
     * Get the screenY position of a local row.
     *
     * @param row (the first row of the component is 0)
     * @return
     */
    public double getScreenY(int row) {
        double screenY = screenPosY + row * Settings.getRowHeight();
        return screenY;
    }

    /**
     * Given a Y position of the screen, returns the corresponding local row.
     *
     * @param screenY
     * @return (the first row of the component is 0)
     */
//    public int getRow(double screenY) {
//        int row = (int) (screenY - getScreenPosY()) / Settings.getRowHeight();
//        return row;
//    }
//
    /**
     * Given an X position of the screen, returns the corresponing local column.
     *
     * @param screenX
     * @return (the first column of the component is 0)
     */
    public int getCol(double screenX) {
        int col = (int) ((screenX - getScreenPosX()) / Settings.getColWidth());
        return col;
    }

//    public void thisAndAncestorsNeedDrawing(){
//        this.setNeedsDrawing(true);
//        if (this.parent!=null){
//            this.parent.thisAndAncestorsNeedDrawing();
//        }
//    }
    /**
     * Draw the child components.
     *
     * @param g
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
     * Add a child component.
     *
     * @param comp
     */
    public void add(MyComponent comp) {
        this.subComponents.add(comp);
    }

    /**
     * Add a child component.
     *
     * @param comp
     */
    public void remove(MyComponent comp) {
        this.subComponents.remove(comp);
    }

    /**
     * Checks whether an (x,y) position in the screen is inside the component.
     *
     * @param x
     * @param y
     * @return
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
//        if (cont) System.out.println("MyComponent::contains: posX = "+this.screenPosX+", width = "+this.width+", posY = "+this.screenPosY+", height = "+this.height);
        return cont;
    }

    public int getFirstParentCol() {
        return parentFirstCol;
    }

    public int getParentFirstRow() {
        return parentFirstRow;
    }

    public int getnCols() {
        return nCols;
    }

    public int getnRows() {
        return nRows;
    }

    public double getScreenPosX() {
        return screenPosX;
    }

    public double getScreenPosY() {
        return screenPosY;
    }

    public double getComponentWidth() {
        return width;
    }

    public double getComponentHeight() {
        return height;
    }

    public MyController getController() {
        return controller;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    /**
     * Returns the class name of the component.
     *
     * @return
     */
    public String getName() {
        String cl = this.getClass().getName();
        String[] parts = cl.split("\\.");
        String compName = parts[parts.length - 1];
        return compName;
    }

    /**
     * Shows the tree structure of the components, and their respective
     * coordinates.
     *
     * @param indent
     * @return
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

    @Override
    public String toString() {
        return " {" + "posX=" + (int) screenPosX + ", posY=" + (int) screenPosY + 
                ", width=" + (int) width + ", height=" + (int) height + ", firstCol=" + parentFirstCol 
                + ", firstRow=" + parentFirstRow + ", nCols=" + nCols + ", nRows=" + nRows + '}';
    }

}
