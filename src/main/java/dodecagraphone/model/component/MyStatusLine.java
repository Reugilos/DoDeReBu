package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * The status line is a horizontal bar at the bottom of the screen where
 * the application can write messages to the user. In its current version
 * the status line has two positions where text can be writen (text, at the
 * left, and rightText at the right).
 *
 * @author Pau
 */
public class MyStatusLine extends MyComponent{
    /** The text in this atribute is writen at the left hand side of the
     * status line. */
    private String text= "Hola";
    /** The text in this atribute is writen at the right hand side. */
    private String rightText = "";
    
    /**
     * 
     * @param firstCol
     * @param firstRow
     * @param nCols
     * @param nRows
     * @param parent
     * @param contr 
     */
    public MyStatusLine(int firstCol, int firstRow, int nCols, int nRows, MyComponent parent,MyController contr){
        super(firstCol,firstRow,nCols,nRows,parent,contr);
    }

    /**
     * Clears both texts. 
     */
    public void clear(){
        this.text="";
        this.rightText="";
    }
    
    /** 
     * getter.
     * @return 
     */
    public String getText() {
        return text;
    }

    /**
     * setter.
     * @param text 
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * getter.
     * @return 
     */
    public String getRightText() {
        return rightText;
    }
    
    /**
     * setter.
     * @param rightText 
     */
    public void setRightText(String rightText) {
        this.rightText = rightText;
    }
    
    /**
     * Returns a concatenation of the text and rightText.
     * @return 
     */
    public String getFullText(){
        return text + "                 " + rightText;
    }

    /**
     * Draws the full text and the status line.
     * @param g 
     */
    @Override
    public void draw(Graphics2D g) {
            if (Settings.SHOW_DRAW_HIERARCHY) {
                Utilities.printOutWithPriority(5, "MyStatusLine::draw: drawing " + this.getClass());
            }

            g.setColor(Color.GRAY);
            g.fillRect((int) screenPosX, (int) screenPosY, (int) width, (int) height);
            g.setColor(java.awt.Color.WHITE);
            g.drawString(getFullText(), (int)(screenPosX+10), (int)(screenPosY+10+Settings.getRowHeight()));

    }
}
