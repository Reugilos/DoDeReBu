package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.*;
import dodecagraphone.model.color.ColorSets;
import dodecagraphone.model.sound.SampleOrMidi;
import dodecagraphone.model.sound.Sound;
import dodecagraphone.model.sound.SoundWithMidi;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;

/**
 * A key in the Xilokeyboard, identified by its row. Each key knows also its
 * color, sound, midi tone, and the flags playing and doNotHighlight. sound.
 */
public class MyXiloKey extends MyComponent {
    int aux = 0;
    /**
     * The current color of the key.
     */
    private Color color;
//    private Color color_fill;
    /**
     * The color that the key takes when it is playing
     */
    private final Color color_playing;
    /**
     * The color that the key takes when it is not playing
     */
    private final Color color_silent;
//    private Color color_slided;
    /**
     * The sound sample associated with the key (when not using midi).
     */
    private Sound so = null;
    /**
     * When the keyboard is on the left, the key is highlighted when it is
     * playing. Each key stores this flag.
     */
    private boolean doNotHighlight = false;
    /**
     * The row position of the key in the keyboard (first key is 0).
     */
    private final int row;
    /**
     * The numeral of the key (first key is 0).
     */
    private final int keyId;
    /**
     * The midi note associated with the key.
     */
    private final int midi;
    /**
     * The corresponding note name.
     */
    private final String noteName;
    /**
     * The channels that are currently playing.
     */
    private Set<Integer> activeChannels;
//    private boolean selected = false;
//    private boolean showChoice = false;
//    private double fullWidth;
//    private double fillWidth;
//    private double fillScreenPosX;
//    private double initScreenPosX;
    private MySlide slide;
//    private MySlide leftSlide;

    /**
     *
     * @param firstCol
     * @param firstRow
     * @param nCols
     * @param nRows
     * @param parent
     * @param contr
     */
    public MyXiloKey(int keyId, int firstCol, int firstRow, int nCols, int nRows, MyComponent parent, MyController contr) {
        super(firstCol, firstRow, nCols, nRows, parent, contr);
        this.keyId = keyId;
        this.row = firstRow;
        this.midi = ToneRange.keyIdToMidi(this.keyId); //Tesitura.getHighestMidi() - row;
        this.noteName = ToneRange.getNoteName(midi);
        this.color_playing = ColorSets.getColorFons(); // getIluminatColor(midi%12);
        this.color_silent = ColorSets.getEncesColor(midi % 12);
        if (!SampleOrMidi.isMidi()) {
            this.so = new Sound(ToneRange.getFilename(midi));
        }
        this.color = this.color_silent;
        this.activeChannels = new HashSet<>();

        this.screenPosX = parent.screenPosX;
        this.width = parent.width;
        //this.screenPosY = parent.screenPosY + row * Settings.getRowHeight();
        //this.height = Settings.getSquareHeight();
//
//        
//        this.fullWidth = this.width;
//        this.fillWidth = this.width;
//        this.initScreenPosX = this.screenPosX;
//        this.fillScreenPosX = this.screenPosX;
//        this.color_fill = color;
        this.slide = new MySlide(keyId,0,firstRow,nCols,nRows,parent,contr); // parent is MyXiloKeyboard
        this.add(slide);
        // this.leftSlide = null;
//        this.color_slided = color;
//        this.setShowChoice(showChoice);
//        this.setSelected(false);
//        System.out.println("MyXiloKey::MyXiloKey: width = "+this.width+", heigth = "+this.height+", nrows = "+this.nRows);
    }

    /**
     * Resets the dimensions of the key, when parent dimensions have changed.
     */
    public final void setDimensionsNotSelected() {
        if (this.controller.getAllPurposeScore().isUseScreenKeyboardRight()){
            this.screenPosX = parent.screenPosX + (1-Settings.KEY_WIDTH_REDUCTION)*parent.width;
            this.width = parent.width * Settings.KEY_WIDTH_REDUCTION;
        } else{
            this.screenPosX = parent.screenPosX;
            this.width = parent.width * Settings.KEY_WIDTH_REDUCTION;
        }
    }

    public final void setDimensionsSelected() {
        if (!this.controller.getAllPurposeScore().isUseScreenKeyboardRight()){
            this.screenPosX = parent.screenPosX + (1-Settings.KEY_WIDTH_REDUCTION)*parent.width;
            this.width = parent.width * Settings.KEY_WIDTH_REDUCTION;
        } else{
            this.screenPosX = parent.screenPosX;
            this.width = parent.width * Settings.KEY_WIDTH_REDUCTION;
        }
    }

    public final void setDimensionsOff() {
        this.screenPosX = parent.screenPosX;
        this.width = parent.width;
    }

//    public void updateKeyFill(){
//        if (isShowChoice()) {
//            if (this.controller.getAllPurposeScore().isShowPentagramaStrips()){
//                color_fill = ColorSets.getPentagramaColor(midi);
//            } else {
//                color_fill = ColorSets.getChoiceColor(midi,this.controller.getAllPurposeScore().getMidiKey(),this.controller.getAllPurposeScore().getChoice().getChoiceList());
//            }
//        }
//    }
    
    public static boolean once = true;
    
    /**
     * Draw XiloKey with the current color.
     *
     * @param g
     */
    @Override
    public void draw(Graphics2D g) {
        if (once){
            if (Settings.SHOW_DRAW_HIERARCHY){
                Utilities.printOutWithPriority(5, "MyXylokey::draw: drawing "+this.getClass());                
            }
        }
        once = false;
//        this.controller.getKeyboard().setShowChoice(showChoice);
        aux++;
//        this.width = 100;
//        if (aux%2 ==0)System.out.println("MyXiloKey::draw: (sx,sy,colX,rowY,w,h) = " + this.keyId + " (" + (int) screenPosX + ","+ (int) screenPosY + "," + (int) parentFirstRow + "," + (int) parentFirstCol + "," + (int) width + "," + (int) height + ") " + aux);   
        int posY = (int) (screenPosY + height * 0.8);
        g.setColor(this.color);
        int h = (int) Math.ceil(height);
        g.fillRect((int) screenPosX,(int) screenPosY, (int) Math.ceil(width), (int) Math.ceil(height));
        String nameOnly = this.noteName.replaceAll("[0-9]+$", "");
        String text = " " + nameOnly;
        if (!ToneRange.isDodecaphone()) {
            if (this.midi == ToneRange.getHighestSaxo()
                    || this.midi == ToneRange.getLowestSaxo()) {
                text += ">";
            }
        }
        g.setColor(activeChannels.isEmpty() ? ColorSets.getGridSquareFontColor(this.midi) : java.awt.Color.BLACK);
        g.drawString(text, (float) (screenPosX), posY);
        this.slide.draw(g);
    }

    /**
     * Setter.
     *
     * @param keyboardRight
     */
    public void doNotHighlight(boolean keyboardRight) {
        this.doNotHighlight = keyboardRight;
    }

    /**
     * Change color and play.
     *
     * @param channel
     */
    public void play(int channel,int velocity) {
        if (!this.doNotHighlight) {
            this.color = this.color_playing;
        }
        if (!this.isPlaying(channel)) {
            if (SampleOrMidi.isMidi()) {
//                System.out.println(""+this.midi);
//                SoundWithMidi.play(this.midi+12*ToneRange.getOctavesUp(), channel, velocity);
                SoundWithMidi.play(this.midi, channel, velocity);
//                System.out.println("      MyChiloKey::play: channel = "+channel);
//                System.out.flush();
            } else {
                /**
                 * Plays sound sample.
                 */
                this.so.play();
            }
            this.activeChannels.add(channel);
        }
    }

//    public boolean isSelected() {
//        return selected;
//    }
//
//    public void setSelected(boolean selected) {
//        this.selected = selected;
//        if (isShowChoice()) {
//            if (isSelected()) {
//                this.screenPosX = this.initScreenPosX+this.fullWidth - this.width;
//                this.fillScreenPosX = this.initScreenPosX;
//                //if (this.slide!=null) this.remove(slide);
//                this.slide = null;
//                this.leftSlide = new MySlide(this.parentFirstCol,this.parentFirstRow,(int)(this.fillWidth/Settings.getColWidth()),this.nRows,this.parent,this.controller);
//                //this.add(leftSlide);
//            } else {
//                this.screenPosX = this.initScreenPosX;
//                this.fillScreenPosX = this.initScreenPosX + this.width;
//                //if (this.leftSlide!=null) this.remove(leftSlide);
//                this.leftSlide =  null;
//                this.slide = new MySlide(this.parentFirstCol+(int)(this.width/Settings.getColWidth()),this.parentFirstRow,(int)(this.fillWidth/Settings.getColWidth()),this.nRows,this.parent,this.controller);
//                //this.add(this.slide);
//            }
//        }
//    }
//
    public int getKeyId() {
        return keyId;
    }

    public MySlide getSlide() {
        return slide;
    }

//    public boolean isShowChoice() {
//        return showChoice;
//    }
//
//    public void setShowChoice(boolean showChoice) {
//        this.showChoice = showChoice;
//        if (isShowChoice()) {
//            this.width = Settings.KEY_WIDTH_REDUCTION * fullWidth;
//            this.fillWidth = fullWidth * (1-Settings.KEY_WIDTH_REDUCTION);
//        } else {
//            this.slide = null;
//            this.leftSlide = null;
//            this.width = fullWidth;
//            this.fillWidth = 0;
//            this.screenPosX = this.initScreenPosX;
//        }
//    }
//
    /**
     * Change color and stop sound.
     *
     * @param channel. The midi channel used for playing this key.
     */
    public void stop(int channel) {
        if (this.isPlaying(channel)) {
            if (SampleOrMidi.isMidi()) {
//                SoundWithMidi.stop(this.midi+12*ToneRange.getOctavesUp(), channel);
                SoundWithMidi.stop(this.midi, channel);
//                System.out.println("      MyChiloKey::stop: channel = "+channel);
//                System.out.flush();
            } else {
                this.so.stop();
            }
            this.activeChannels.remove(channel);
//            System.out.println("MyXiloKey::stop(): nActiveChannels = "+this.activeChannels.size());
            if (this.activeChannels.isEmpty()) {
                this.color = this.color_silent;
            }
        }
    }

    /**
     * Change color and stop sound.
     *
     * @param channel. The midi channel used for playing this key.
     */
    public int stopAllChannels() {
        int count = this.activeChannels.size();
        if (count==0) return 0;
        for (int channel:this.activeChannels) {
            if (SampleOrMidi.isMidi()) {
                SoundWithMidi.stop(this.midi, channel);
//                System.out.println("        MyChiloKey::stopAllChannels: channel = "+channel);
//                System.out.flush();
            } else {
                this.so.stop();
            }
        }
        this.activeChannels.clear();
        this.color = this.color_silent;
        return count;
    }

    public Set<Integer> getActiveChannels() {
        return new HashSet<>(activeChannels);
    }

    /**
     *
     * @return
     */
    public Sound getSo() {
        return so;
    }

    /**
     *
     * @return
     */
    public int getRow() {
        return row;
    }

    /**
     *
     * @return
     */
    public int getMidi() {
        return midi;
    }

    public String getNoteName() {
        return noteName;
    }

    /**
     *
     * @return
     */
    public boolean isPlaying(int channel) {
        boolean playing = this.activeChannels.contains(channel);
        return playing;
    }

}
