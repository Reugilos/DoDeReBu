package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.sound.SoundWithMidi;
import dodecagraphone.ui.Settings;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Set;

/**
 * The XiloKeyboard is a xylophone-like keyboard of XiloKeys. The chilokeys are
 * subcomponents of the xyloKeyboard.
 *
 * @author pau
 */
public class MyXiloKeyboard extends MyComponent {

    /**
     * For clarity, xilokeys is an alias of the subcomponent list.
     */
    private final List<MyXiloKey> xilokeys;
    private boolean showChoice;
    private int nKeys;

    /**
     * The contructor creates the xilokeys, and adds them to the subcomponent
     * list.
     *
     * @param firstCol
     * @param firstRow
     * @param nCols
     * @param nRows
     * @param keyboardRight
     * @param parent
     * @param contr
     */
    public MyXiloKeyboard(int firstCol, int firstRow, int nCols, int nRows, boolean keyboardRight, MyComponent parent, MyController contr,boolean showCh, int nKeys) {
        super(firstCol, firstRow, nCols, nRows, parent, contr);
        /**
         * xilokeys is a shallow copy of subComponents.
         */
//        System.out.println("MyXiloKeyboard::MyXiloKeyboard: "+this.toString());
        this.nKeys = nKeys;
        xilokeys = (List<MyXiloKey>) (List<? extends MyComponent>) subComponents;
        for (int k = 0; k < nKeys; k++) {
            int row = k * Settings.getnRowsSquare();
            MyXiloKey key = new MyXiloKey(k, 0, row, Settings.getnColsKeyboard(), Settings.getnRowsSquare(), this, controller);
            xilokeys.add(key);
            //System.out.println("MyXiloKeyboard::MyXiloKeyboard: key = "+key.toString()+" "+key.getWidth());
        }
        // System.out.println("MyXiloKeyboard::MyXiloKeyboard: Xilokey.width ="+this.getKeyboard().get(10).getWidth());
        this.doNotHighlight(keyboardRight);
        this.setShowChoice(showCh);
        this.setDimensions(firstCol, firstRow, nCols, nRows);
//        this.thisAndAncestorsNeedDrawing();
//        System.out.println("MyXiloKeyboard::MyXiloKeyboard: Xilokey.width ="+this.getKeyboard().get(10).getWidth());
    }

    @Override
    public void draw(Graphics2D g){
        MyXiloKey.once = true;
        super.draw(g);
        g.setColor(Color.BLACK);
        //g.drawLine((int) screenPosX,(int) screenPosY, (int)(screenPosX+width), (int) screenPosY);                
    }
    
//    public MyXiloKeyboard(int firstCol, int firstRow, int nCols, int nRows, boolean keyboardRight, MyComponent parent, MyController contr, boolean showCh) {
//        super(firstCol, firstRow, nCols, nRows, parent, contr);
//        /**
//         * xilokeys is a shallow copy of subComponents.
//         */
//        xilokeys = (List<MyXiloKey>) (List<? extends MyComponent>) subComponents;
//        xilokeys.clear();
//        for (int row = 0; row < nRows; row++) {
//            xilokeys.add(new MyXiloKey(0, row, nCols, 1, this, controller, showCh));
//        }
//        this.doNotHighlight(keyboardRight);
//        this.setShowChoice(showCh);
//    }
    
    public void resetDimensions(){
        for (int keyId = 0; keyId < nKeys; keyId++) {
            MyXiloKey key = this.xilokeys.get(keyId);
            MySlide slide = key.getSlide();
            if (isShowChoice()){
                if (this.findIfSelected(key.getMidi())){
                    key.setDimensionsSelected();
                    slide.setDimensionsSelected();
                } else {
                    key.setDimensionsNotSelected();
                    slide.setDimensionsNotSelected();
                }
            } else {
                key.setDimensionsOff();
                slide.setDimensionsOff();
            }
        }
        /**
         * When the keyboard is on the left, the xilokeys currently being played
         * are highlighted. This method sets the highligh toggle to false when
         * the keyboard is on the right side.
         */
        this.doNotHighlight(this.controller.getAllPurposeScore().isUseScreenKeyboardRight());
        //this.setShowChoice(showChoice);
//        this.thisAndAncestorsNeedDrawing();
    }

    /**
     * Resets the position and dimensions of the keyboard after changing sides.
     *
     * @param firstCol
     * @param firstRow
     * @param nCols
     * @param nRows
     * @param ifKeyboardIsRight (true if the keyboard is at the right).
     */
    public final void setDimensions(int firstCol, int firstRow, int nCols, int nRows) { // or left
        super.setDimensions(firstCol, firstRow, nCols, nRows);
        this.resetDimensions();
//        for (int keyId = 0; keyId < nKeys; keyId++) {
//            MyXiloKey key = this.xilokeys.get(keyId);
//            MySlide slide = key.getSlide();
//            if (isShowChoice()){
//                if (this.findIfSelected(key.getMidi())){
//                    key.setDimensionsNotSelected();
//                    slide.setDimensionsNotSelected();
//                } else {
//                    key.setDimensionsNotSelected();
//                    slide.setDimensionsNotSelected();
//                }
//            } else {
//                key.setDimensionsOff();
//                slide.setDimensionsOff();
//            }
//        }
//        /**
//         * When the keyboard is on the left, the xilokeys currently being played
//         * are highlighted. This method sets the highligh toggle to false when
//         * the keyboard is on the right side.
//         */
//        this.doNotHighlight(ifKeyboardIsRight);
//        //this.setShowChoice(showChoice);
    }
    
//    public void updateKeyShow(){
//        boolean show = this.isShowChoice();
//        this.setShowChoice(show);
////        for (MyXiloKey key:xilokeys){
////            key.updateKeyFill();
////        }
//    }    
//
    /**
     * Returns the row of the xilokey that contains the absolute screen position
     * (posX,posY), -1 if none.
     *
     * @param posX
     * @param posY
     * @return (first row is 0)
     */
    public int whichKey(double posX, double posY) {
        for (MyXiloKey key : xilokeys) {
            if (key.contains(posX, posY)) {
                return key.getKeyId();
            }
        }
        return -1;
    }
    
    public int whichSlideKey(double posX, double posY) {
        for (MyXiloKey key : xilokeys) {
            MySlide slide = key.getSlide();
            if (slide.contains(posX, posY)) return slide.getKeyId();
        }
        return -1;
    }
    
    public int getnKeys() {
        return nKeys;
    }
    
    /**
     * Gets the Xilokey at a given row (first row is 0).
     *
     * @param row
     * @return
     */
    public MyXiloKey getKey(int row) {
        return xilokeys.get(row);
    }

    /**
     * Plays the xilokey at a given row.
     *
     * @param key
     */
    public void play(int key) {
        xilokeys.get(key).play(this.controller.getMixer().getCurrentChannelOfCurrentTrack(),SoundWithMidi.getCurrentKeyboardVelocity());
    }

    private static long auxCount =0;

    public void playAtBeat(int key) {
        if (Settings.isPlayAtBeat()){
            int ncb = Settings.getnColsBeat();
            if (auxCount % ncb == 0) {
                this.stop(key);
                this.play(key);
            }
            auxCount++;
        }
    }

    /**
     * Stops the xilokey at a given row.
     *
     * @param keyID
     */
    public void stop(int keyID) {
        MyXiloKey key = xilokeys.get(keyID);
        key.stop(this.controller.getMixer().getCurrentChannelOfCurrentTrack());
    }

    /**
     * Stops all the xilokeys.
     */
    public void stopAll() {
        for (int keyId = 0; keyId < nKeys; keyId++) {
            MyXiloKey key = xilokeys.get(keyId);
            Set<Integer> channels = key.getActiveChannels(); 
            for (int channel : channels) {
                key.stop(channel);
            }
        }
    }

    /**
     * Sets the doNotHiglight toggle of all the xilokeys.
     *
     * @param keyboardIsRight
     */
    public final void doNotHighlight(boolean keyboardIsRight) {
        for (int row = 0; row < nKeys; row++) {
            xilokeys.get(row).doNotHighlight(keyboardIsRight);
        }
    }

    public final void setShowChoice(boolean show){
        this.showChoice = show;
    }

    public boolean isShowChoice() {
        return showChoice;
    }
    
    public boolean findIfSelected(int midi){
        List<Integer> selected = this.controller.getAllPurposeScore().getChoice().getChoiceList();
        return (selected.contains(midi));
    }


    /**
     * Getter of the xilokey list.
     *
     * @return
     */
    public List<MyXiloKey> getKeyboard() {
        return xilokeys;
    }
    
//    @Override
//    public void draw(Graphics2D g) {
//        this.setShowChoice(showChoice);
//        for (int row = 0; row < nKeys; row++) {
//            xilokeys.get(row).draw(g);
//        }
//    }

}
