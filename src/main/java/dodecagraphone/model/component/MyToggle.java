package dodecagraphone.model.component;

import dodecagraphone.MyController;

/**
 * A MyToggle is a button with two states: on an off, and it toggles its state
 * each time the button is pressed, activating the corresponding
 * controller action, as specified in MyButtonPanel::onButtonPressed().
 * The text and color of the button change while the button is on, until
 * next click on the button. The inherited boolean isPressed is used here
 * to identify whether the button is on or off.
 * @author pau
 */
public class MyToggle extends MyButton{
    /** Text when the toggle has been clicked on. */
    private final String textOn;
    /** Text when the toggle has been clicked off. */
    private final String textOff;
    
    
    /**
     * 
     * @param id
     * @param firstCol
     * @param firstRow
     * @param nCols
     * @param nRows
     * @param parent
     * @param contr
     * @param textOn
     * @param textOff 
     */
    public MyToggle(int id,int firstCol,int firstRow,int nCols,int nRows,MyComponent parent,MyController contr,
            String textOn,String textOff,String tipText){
        super(id,firstCol,firstRow,nCols,nRows,parent,contr,"",tipText);
        this.textOff = textOn;
        this.textOn = textOff;
        this.isPressed = false;
        this.color = this.colorUnpressed;
        this.text = this.textOff;
    }

    /**
     * Sets the isPressed flag, the color and the text, as specified by
     * the parameter pressed.
     * 
     * @param pressed 
     */
    @Override
    public void setPressed(boolean pressed){
        if (this.id == 28 ) {
 //           System.out.println("MyToggle::setPressed: " + this.id + " " + pressed);
//            try {
//                throw new InvalidParameterException("toggle.setPressed()");
//            }
//            catch (InvalidParameterException ex){
//                ex.printStackTrace();
//            }
        }
        this.isPressed = pressed;
        if (this.isPressed){
            this.color = this.colorPressed;
            this.text = this.textOn;
        }
        else{
            this.color = this.colorUnpressed;
            this.text=this.textOff;
        }
    }

    /**
     * Resetting a toggle does nothing. In the superclass MyButton, though,
     * reset sets isPressed to false.
     */
    @Override
    public void reset(){        
    }
    
    /** Toggling a button does nothing, but toggling a toggle does toggle it.
     * In the superclass MyButton this method does nothing.
     */
    @Override
    public void toggle(){
        this.isPressed = !this.isPressed;
        // if (this.id == 28) System.out.println("MyToggle::togle: after toggling isPressed = " + isPressed);
        if (this.isPressed){
            this.color = this.colorPressed;
            this.text = this.textOn;
        }
        else{
            this.color = this.colorUnpressed;
            this.text=this.textOff;
        }
    }
    
//    public static boolean once = true;
//    
//    @Override
//    public void draw(Graphics2D g){
//        once = false;
//        super.draw(g);
//    }
}
