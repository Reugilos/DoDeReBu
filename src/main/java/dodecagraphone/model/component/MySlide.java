package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.color.ColorSets;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author grogm
 */
public class MySlide extends MyComponent {
    private final int keyId;
    private Color color_fill;
    private final int midi;
    private String position;
    private int aux = 0;
    
    public MySlide(int keyId, int firstCol, int firstRow, int nCols, int nRows, MyComponent parent, MyController contr) {
        super(firstCol, firstRow, nCols, nRows, parent, contr);
        this.keyId = keyId;
        this.midi = ToneRange.keyIdToMidi(this.keyId); //Tesitura.getHighestMidi() - row;
        this.screenPosX = parent.screenPosX;
        this.width = parent.width;
       // this.screenPosY = parent.screenPosY + firstRow * Settings.getSquareHeight();
       // this.height = Settings.getRowHeight()*nRows;
        this.position = "notSelected";
    }
    
    public void setColor(){
            if (this.controller.getAllPurposeScore().isShowPentagramaStrips()){
                color_fill = ColorSets.getPentagramaColor(midi);
            } else {
                color_fill = ColorSets.getChoiceColor(midi,this.controller.getAllPurposeScore().getChoice().getChoiceList());
            }
//            color_fill = ColorSets.getEncesColor(ColorSets.LINIA_PENTA);
//            if (position.equals("selcted")){
//                color_fill = ColorSets.getEncesColor(ColorSets.BUIT);
//            }
//            else 
            color_fill = ColorSets.getEncesColor(ColorSets.LINIA_PENTA);
    }
    
    public void setDimensionsSelected(){
        // parent is MyXiloKeyboard
        this.position = "selected";
        if (!this.controller.getAllPurposeScore().isUseScreenKeyboardRight()){
            this.screenPosX = parent.screenPosX;
            this.width = parent.width * (1-Settings.KEY_WIDTH_REDUCTION);            
        } else {
            this.screenPosX = parent.screenPosX + parent.width * (Settings.KEY_WIDTH_REDUCTION) ;
            this.width = parent.width * (1-Settings.KEY_WIDTH_REDUCTION);
        }
        this.setColor();
    }

    public void setDimensionsNotSelected(){
        this.position = "notSelected";
        if (this.controller.getAllPurposeScore().isUseScreenKeyboardRight()){
            this.screenPosX = parent.screenPosX;
            this.width = parent.width * (1-Settings.KEY_WIDTH_REDUCTION);            
        } else {
            this.screenPosX = parent.screenPosX + parent.width * (Settings.KEY_WIDTH_REDUCTION) ;
            this.width = parent.width * (1-Settings.KEY_WIDTH_REDUCTION);
        }
        this.setColor();
    }

    public void setDimensionsOff(){
        this.position = "off";
        this.screenPosX = parent.screenPosX;
        this.width = 0;
        this.setColor();
    }
    
    public int getKeyId() {
        return keyId;
    }

    public boolean isKeyNotSelected(){
        return position.equals("notSelected");
    }
    
    public boolean isKeySelected(){
        return position.equals("selected");
    }

    public static boolean once = true;
    
    @Override
    public void draw(Graphics2D g){
        if (once) {
            if (Settings.SHOW_DRAW_HIERARCHY) {
                Utilities.printOutWithPriority(5, "MySlide::draw: drawing " + this.getClass());
            }
        }
        once = false;
        aux++;
//        if (aux%2 == 0) System.out.println("MySlide::draw: (sx,sy,colX,rowY,w,h) = " + this.keyId + " (" + (int) screenPosX + ","+ (int) screenPosY + "," + (int) parentFirstRow + "," + (int) parentFirstCol + "," + (int) width + "," + (int) height + ") " + aux);   
        if (width>0){
            //g.setColor(Color.getPlatformColor(color_fill));
            g.setColor(color_fill);
            g.fillRect((int) screenPosX,(int) screenPosY, (int)Math.ceil(width), (int) Math.ceil(height));                
        }
    }
}
