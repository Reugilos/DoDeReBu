package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.color.ColorSets;
import static dodecagraphone.model.color.ColorSets.BUTO;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * There are two kinds of button: MyButton (superclsass) that reacts when 
 * it is pressed and resets when the mouse is released, and MyToggle (subclass) 
 * that toggles its state every time it is pressed. 
 * When a MyButton is pressed, it activates the corresponding controller 
 * command, as specified in MyButtonPanel::onButtonPressed(). A button has
 * a text, and it has a different color while it is being pressed and when
 * it is released.
 * 
 * @author Pau
 */
public class MyButton extends MyComponent {
    /** Position of the text in the button. */
    protected static double xOffsetText;
    protected static double yOffsetText;
    
    /** Button id. */
    protected int id;
    /* Text of the button. */
    protected String text;
    /** Current color of the button. */
    protected Color color;
    /** Color while the button is being pressed. */
    protected Color colorPressed = ColorSets.getIluminatColor(BUTO);
    /** Color of the button when it is not pressed. */
    protected Color colorUnpressed = ColorSets.getEncesColor(BUTO);
    /** True if the button is being pressed. */
    protected boolean isPressed;
    protected String tipText;
//    protected boolean once = true;
    
    /**
     * 
     * @param id
     * @param firstCol
     * @param firstRow
     * @param nCols
     * @param nRows
     * @param parent
     * @param contr
     * @param text
     * @param tipText
     */
    public MyButton(int id,int firstCol,int firstRow,int nCols,int nRows,MyComponent parent,MyController contr,String text, String tipText){
        super(firstCol,firstRow,nCols,nRows,parent,contr);
        this.id = id;
        this.text = text;
        xOffsetText = this.width /5;
        yOffsetText = 2* this.height /3;
        this.isPressed = false;
        this.color = this.colorUnpressed;
        this.tipText = "<html> Soc el botó " + id + " i mostro un missatge molt llarg <br>en dos línies </html>";
        if (!"".equals(tipText)) this.tipText = tipText;
    }

    public static boolean once = true;
    /**
     * Draws the button with the current text and color.
     * @param g 
     */
    @Override
    public void draw(Graphics2D g) {
        if (once) {
            if (Settings.SHOW_DRAW_HIERARCHY) {
                Utilities.printOutWithPriority(5, "MyButton::draw: drawing " + this.getClass());
            }
        }
        once = false;
//        if (this.id == 28 && once) {
//            once = false;
//            System.out.println("MyButton::draw: isPressed = " + this.id + " " + this.isPressed + " text = " + this.text);
//        }
        g.setColor(this.color);
        g.fillRect((int) screenPosX, (int) screenPosY, (int) width, (int) height);
        g.setColor(java.awt.Color.WHITE);
        g.drawString(text, (float) (screenPosX + xOffsetText), (float) (screenPosY + yOffsetText));
    }

    public String getTipText() {
        return tipText;
    }

    public void setTipText(String tipText) {
        this.tipText = tipText;
    }

    public void setButtonText(String text){
        this.text = text;
    }

    public String getButtonText(){
        return text;
    }
    /**
     * 
     * @return 
     */
    public int getId() {
        return id;
    }

    /**
     * 
     * @param isPressed 
     */
    public void setPressed(boolean isPressed){
        this.isPressed = isPressed;
        if (isPressed){
            this.color = this.colorPressed;
        }
        else {
            this.color = this.colorUnpressed;           
        }
    }
    
    /** 
     * Reseting a button is the same as setting it to false, but reseting
     * a toggle should do nothing. 
     * This method is overriden in the subclass MyToggle.
     */
    public void reset(){
        this.setPressed(false);
    }

    /**
     * Toggling a button does nothing, but toggling a toggle does toggle it.
     * This method is overriden in the subclass MyToggle.
     */
    public void toggle(){        
    }

    /**
     * 
     * @return 
     */
    public boolean isPressed() {
        return isPressed;
    }

    /**
     * 
     * @param text 
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * 
     * @return 
     */
    public String getText() {
        return text;
    }
    
}
