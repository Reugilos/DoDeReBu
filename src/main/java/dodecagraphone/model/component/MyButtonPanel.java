package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.color.ColorSets;
import dodecagraphone.ui.I18n;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;

/**
 * The button panel includes the buttons and toggles that control the
 * application. The buttons Map replicates the subComponents List.
 *
 * @author paugpt
 */
public class MyButtonPanel extends MyComponent {

    /**
     * A Map with all the buttons. Each button has an id.
     */
    private final HashMap<Integer, MyButton> buttons;
    private Map<Integer, ButtonInfo> buttonLayout = new HashMap<>();
    private JPopupMenu popUp;

    private int id_PlayButton = 0;
    private int id_TimeSignatureButton = 1;
    private int id_FirstPageButton = 2;
    private int id_NextColButton = 3;
    private int id_PrevColButton = 4;
    private int id_PrevPageButton = 5;
    private int id_NextPageButton = 6;
    private int id_PageNumButton = 7;
    private int id_FasterButton = 8;
    private int id_SlowerButton = 9;
    private int id_TempoButton = 10;
    private int id_TransposeUpButton = 11;
    private int id_TransposeDownButton = 12;
    private int id_LoadButton = 13;
    private int id_SaveButton = 14;
    private int id_NewButton = 15;
    private int id_NextExerciseButton = 16;
    private int id_PrevExerciseButton = 17;
    private int id_RestartExerciseButton = 18;
    private int id_NextKeyButton = 19;
    private int id_PrevKeyButton = 20;
    private int id_ResetKeyButton = 21;
    private int id_DrumsButton = 22;
//    private int id_TrackButton = 22;
//    private int id_ChannelButton = 23;
    private int id_InstrButton = 24;
    private int id_PentaVsChoiceButton = 25;
    private int id_LeftVsRightButton = 26;
    private int id_SelectChoiceButton = 27;
    private int id_NamesVsHideButton = 28;
    private int id_MobileDoVsAbsoluteButton = 29;
    private int id_MixerButton = 30;
    private int id_TitleButton = 31;
    private int id_AuthorButton = 32;
    private int id_LouderButton = 33;
    private int id_QuieterButton = 34;
    private int id_VolumeButton = 35;
    private int id_DescriptionButton = 36;
    private int id_ChordSymbolsButton = 37;
    private int id_FitAnacrusisButton = 39;
    /**
     * True if the button panel has been modified and needs to be repaint.
     */
    private boolean modified;

    /**
     *
     * @param firstCol
     * @param firstRow
     * @param nCols
     * @param nRows
     * @param parent
     * @param contr
     */
    public MyButtonPanel(int firstCol, int firstRow, int nCols, int nRows, MyComponent parent, MyController contr) {
        super(firstCol, firstRow, nCols, nRows, parent, contr);
        this.buttons = new HashMap<>();
        this.loadButtonLayout();
        this.placeButtons();
        this.modified = false;
//        this.setNeedsDrawing(true);
        this.popUp = new JPopupMenu();
    }

    // Mètode per carregar el disseny dels botons des del fitxer CSV
    public final void loadButtonLayout() {
        InputStream in = getClass().getResourceAsStream("/defaults/ButtonLayout.csv");
        if (in == null) {
            try{
                throw new FileNotFoundException("Resource not found: /defaults/ButtonLayout.csv");
            }catch(FileNotFoundException ex){
                System.err.println(ex.getMessage());
            }
        }
        try ( BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(";");
                int id = Integer.parseInt(fields[0].trim());
                String name = fields[1].trim();
                char type = (fields[2].trim()).charAt(0);
                int row = Integer.parseInt(fields[3].trim()) - 1;
                int col = Integer.parseInt(fields[4].trim());
                String textOn = "";
                if (fields.length > 5 && !fields[5].trim().isEmpty()) {
                    textOn = I18n.t(fields[5].trim());
                }
                String textOff = (fields.length > 6 && !fields[6].trim().isEmpty()) ? I18n.t(fields[6].trim()) : "";
                String tipText = "";
                if (fields.length > 7 && !fields[7].trim().isEmpty()) {
                    tipText = I18n.t(fields[7].trim());
                }
                String active = "";
                if (fields.length > 8) {
                    active = fields[8].trim();
                }
                if (active.equals("On")) {
                    buttonLayout.put(id, new ButtonInfo(id, name, type, row, col, textOn, textOff, tipText));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Places the buttons and toggles on the button panel.
     */
// Mètode placeButtons() modificat per utilitzar el mapa buttonLayout
    public final void placeButtons() {
        int nColsB = Settings.getnColsButton();
        int nRowsB = Settings.getnRowsButton();
        int firstCol = Settings.getFirstColButton();
        int sepCols = Settings.getSepColsButton();
        int firstRow = 3;
        int sepRows = 1 + nRowsB;
        MyButton but;

        for (ButtonInfo buttonInfo : buttonLayout.values()) {
            int xPosition = firstCol + (buttonInfo.col - 1) * sepCols;
            int yPosition = firstRow + buttonInfo.row * sepRows;

            switch (buttonInfo.type) {
                case 'T':
                    this.buttons.put(buttonInfo.id, but = new MyToggle(buttonInfo.id,
                            xPosition, yPosition, nColsB, nRowsB, this, controller, buttonInfo.textOn, buttonInfo.textOff, buttonInfo.tipText));
                    break;
                case 'B':
                    this.buttons.put(buttonInfo.id, but = new MyButton(buttonInfo.id,
                            xPosition, yPosition, nColsB, nRowsB, this, controller, buttonInfo.textOn, buttonInfo.tipText));
                    break;
                default:
                    // Circular button
                    this.buttons.put(buttonInfo.id, but = new MyCircularButton(buttonInfo.id,
                            xPosition, yPosition, nColsB, nRowsB, this, controller, buttonInfo.textOn, buttonInfo.textOff, buttonInfo.tipText));
                    ((MyCircularButton) but).setStateList(new String[]{"Simbol", "Sinomim", "Intervals", "Posicions", "Notes"});
                    break;
            }
            this.subComponents.add(but);
        }
    }

    /**
     * Resets the buttons.
     *
     * @param firstCol
     * @param firstRow
     * @param nCols
     * @param nRows
     */
    public void resetButtons(int firstCol, int firstRow, int nCols, int nRows) {
        super.setDimensions(firstCol, firstRow, nCols, nRows);
        this.placeButtons();
    }

    public void showTip(int button, double posX, double posY) {
        String tipText = this.buttons.get(button).getTipText();
        // System.out.println("MyButtonPanel::showTip() text = " + tipText);
        if (Settings.isTipsVisible() && !popUp.isVisible()) {
            this.popUp.removeAll();
            JToolTip toolTip = new JToolTip();
            toolTip.setTipText(tipText);
            this.popUp.add(toolTip); // tipText text pot ser html: "<html><b>Funció del botó:</b><br>Aquest botó executa X acció.</html>"
            popUp.pack();
            popUp.show(this.controller.getUi().getPanel(), (int) posX - 10, (int) posY - 10);
        }
    }

    public void showCustomTip(String text, double posX, double posY) {
        if (Settings.isTipsVisible() && !popUp.isVisible()) {
            this.popUp.removeAll();
            JToolTip toolTip = new JToolTip();
            toolTip.setTipText(text);
            this.popUp.add(toolTip);
            popUp.pack();
            popUp.show(this.controller.getUi().getPanel(), (int) posX - 10, (int) posY - 10);
        }
    }

    public void hideTip() {
        popUp.setVisible(false);
    }

    public HashMap<Integer, MyButton> getButtons() {
        return buttons;
    }

    public int getId_NextExerciseButton() {
        return id_NextExerciseButton;
    }

    public int getId_PrevExerciseButton() {
        return id_PrevExerciseButton;
    }

//    public int getId_TrackButton() {
//        return id_TrackButton;
//    }
//
//    public int getId_ChannelButton() {
//        return id_ChannelButton;
//    }
//
    public int getId_MixerButton() {
        return id_MixerButton;
    }

    public int getId_InstrButton() {
        return id_InstrButton;
    }

    /**
     * Checks whether the button panel has been modified.
     *
     * @return
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Setter.
     *
     * @param modified
     */
    public void setModified(boolean modified) {
        this.modified = modified;
//        this.setNeedsDrawing(modified);
    }

    public void onButtonPressed(int id) {
        ButtonInfo buttonInfo = buttonLayout.get(id);
        if (buttonInfo == null) {
            System.out.println("Botó amb id " + id + " no trobat.");
            return;
        }

        MyButton butt = this.buttons.get(id);
        if (butt == null) {
            System.out.println("Instància de MyButton amb id " + id + " no trobat.");
            return;
        }

        switch (buttonInfo.type) {
            case 'T':
                butt.toggle();
                break;
            case 'B':
                butt.setPressed(true);
                break;
            default:
                butt.toggle();
                break;
        }

        String methodName = "on" + buttonInfo.name + "Pressed";
        try {
            Method method = controller.getClass().getMethod(methodName, MyButton.class);
            // System.out.println("MyButtonPanel::onButtonPressed::method"+method+"("+butt.getId()+")");
//            System.out.println("MyButtonPanel::onButtonPressed(): Invoke " + methodName);
            method.invoke(controller, butt);  // Passem butt com a argument
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            System.err.println("Exception in the invoked method: " + cause);
            cause.printStackTrace(); // Mostra l'error real
        } catch (NoSuchMethodException e) {
            System.out.println("Mètode " + methodName + " no trobat al controlador.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.setModified(true);
        this.controller.updateTextOfButtons();
//        this.thisAndAncestorsNeedDrawing();
    }

//    public void onButtonPressed_old(int id) {
//        MyButton butt = this.buttons.get(id);
//        switch (id) {
//            case 0 -> {
//                butt.toggle();
//                this.controller.onPlayButtonPressed(butt);
//            }
//            case 1 -> {
//                butt.setPressed(true);
//                this.controller.onFirstPageButtonPressed();
//            }
//            case 2 -> {
//                butt.setPressed(true);
//                this.controller.onPrevPageButtonPressed();
//            }
//            case 3 -> {
//                butt.setPressed(true);
//                this.controller.onNextPageButtonPressed();
//            }
//            case 5 -> {
//                butt.setPressed(true);
//                this.controller.onFasterButtonPressed();
//            }
//            case 6 -> {
//                butt.setPressed(true);
//                this.controller.onSlowerButtonPressed();
//            }
//            case 8 -> {
//                butt.setPressed(true);
//                this.controller.onTransposeUpButtonPressed();
//            }
//            case 9 -> {
//                butt.setPressed(true);
//                this.controller.onTransposeDownButtonPressed();
//            }
//            case 10 -> {
//                butt.setPressed(true);
//                this.controller.onLoadButtonPressed(); // Scanner input
//                // inhibits drawing of the button in isPressed position.
//            }
//            case 11 -> {
//                butt.setPressed(true);
//                this.controller.onSaveButtonPressed();
//            }
//            case 12 -> {
//                butt.setPressed(true);
//                this.controller.onNewButtonPressed();
//            }
//            case 13 -> {
//                butt.setPressed(true);
//                this.controller.onNextExerciseButtonPressed(butt);
//            }
//            case 14 -> {
//                butt.setPressed(true);
//                this.controller.onPrevExerciseButtonPressed(butt);
//            }
//            case 15 -> {
//                butt.setPressed(true);
//                this.controller.onResetExerciseButtonPressed(butt);
//            }
//            case 16 -> {
//                butt.setPressed(true);
//                this.controller.onNextKeyButtonPressed(butt);
//            }
//            case 17 -> {
//                butt.setPressed(true);
//                this.controller.onPrevKeyButtonPressed(butt);
//            }
//            case 18 -> {
//                butt.setPressed(true);
//                this.controller.onResetKeyButtonPressed(butt);
//            }
//            case 19 -> {
//                butt.toggle();
//                this.controller.onPentaVsChoiceButtonPressed(butt);
//            }
//            case 20 -> {
//                butt.toggle();
//                this.controller.onLeftVsRightButtonPressed(butt);
//            }
//            case 21 -> {
//                butt.toggle();
//                this.controller.onNamesVsHideButtonPressed(butt);
//            }
//            case 22 -> {
//                butt.toggle();
//                this.controller.onAbsoluteVsMobileDoButtonPressed(butt);
//            }
//        }
//        this.controller.updateTextOfButtons();
//        this.setModified(true);
//    }
    /**
     * Resets the button whose identifier is id.
     *
     * @param id
     */
    public void onButtonRelesased(int id) {
        this.buttons.get(id).reset();
        this.setModified(true);
        this.controller.updateTextOfButtons();
//        this.thisAndAncestorsNeedDrawing();
        // System.out.println("MyButtonPanel::onButtonReleased("+id+")");
    }

    /**
     * Returns the id of the button that contains the coordinates
     * (screenX,screenY). -1 otherwise.
     *
     * @param screenX
     * @param screenY
     * @return
     */
    public int whichButton(double screenX, double screenY) {
        if (this.contains(screenX, screenY)) {
            for (MyButton button : this.buttons.values()) {
                if (button.contains(screenX, screenY)) {
                    return button.getId();
                }
            }
        }
        return -1;
    }

    /**
     *
     * @param g
     */
    @Override
    public void draw(Graphics2D g) {
        if (Settings.SHOW_DRAW_HIERARCHY) {
            Utilities.printOutWithPriority(5, "MyButtonPanel::draw: drawing " + this.getClass());
            MyButton.once = true;
            MyToggle.once = true;
        }
//        System.out.println("MyButtonPanel::draw black: (pX,pY,w,h) = (" +
//                (int) screenPosX + "," + (int) screenPosY + "," + (int) width + "," + (int) height + ")");
        g.setColor(ColorSets.getColorFons());
        g.fillRect((int) screenPosX, (int) screenPosY, (int) width, (int) height);
        for (MyButton button : this.buttons.values()) {
            button.draw(g);
        }
        g.setColor(java.awt.Color.BLACK);
        g.drawRect((int) screenPosX, (int) screenPosY, (int) width, (int) height); // fillRect si vull donar fons de color
    }

    /**
     * A specific method to programtically activate the play button for a stop.
     */
    public void stopPlayButton() {
        MyToggle togg = (MyToggle) this.buttons.get(this.id_PlayButton);
        if (togg.isPressed()) {
            togg.toggle();
        }
        this.controller.onPlayButtonPressed(togg);
    }

    /**
     * A specific method to programtically activate the play button for a start.
     */
    public void startPlayButton() {
        MyToggle togg = (MyToggle) this.buttons.get(this.id_PlayButton);
        togg.setPressed(true);
        this.controller.onPlayButtonPressed(togg);
    }

//    public void updateTrackButton(String text){
//        this.setModified(true);
//        this.buttons.get(this.id_TrackButton).setText(text);
//    }
//
//    public void updateChannelButton(String text){
//        this.setModified(true);
//        this.buttons.get(this.id_ChannelButton).setText(text);
//    }
//
    public void updateInstrButton(String text) {
        this.setModified(true);
        if (this.buttons.get(this.id_InstrButton)!=null) this.buttons.get(this.id_InstrButton).setText(text);
    }

    public void updateMixerButton(String text) {
        this.setModified(true);
        if (this.buttons.get(this.id_MixerButton)!=null) this.buttons.get(this.id_MixerButton).setText(text);
    }

    /**
     * A specif update method for the page num button.
     *
     * @param text
     */
    public void updatePageNumButton(String text) {
        this.setModified(true);
        this.buttons.get(this.id_PageNumButton).setText(text);
    }

    /**
     * A specific update method for the tempo button.
     *
     * @param text
     */
    public void updateTempoButton(String text) {
        this.setModified(true);
        this.buttons.get(this.id_TempoButton).setText(text);
    }

    public void updateVolumeButton(String text) {
        this.setModified(true);
        this.buttons.get(this.id_VolumeButton).setText(text);
    }

    public void setToggleButtonsToProgramValues() {
        this.setPentaVsChoiceButton(this.controller.getAllPurposeScore().isShowPentagramaStrips());
        this.setLeftVsRightButton(this.controller.getAllPurposeScore().isUseScreenKeyboardRight());
        this.setNamesVsHideButton(this.controller.getAllPurposeScore().isShowNoteNames());
        this.setMobileDoVsAbsoluteButton(ToneRange.isMovileDo());
//        MyToggle tog = (MyToggle) this.buttons.get(this.id_NamesVsHideButton);
//        System.out.println("MyButtonPanl::setToggleButtonsToProgramValues: isPressed(" + tog.getId() + ") = " + tog.isPressed() + ", showNames = " + this.controller.getScore().isShowNoteNames());
    }

    /**
     * A specific method to programmatically activate the penta-vs-choice
     * button.
     *
     * @param penta
     */
    public void setPentaVsChoiceButton(boolean penta) {
        this.setModified(true);
        MyToggle tog = (MyToggle) this.buttons.get(this.id_PentaVsChoiceButton);
        tog.setPressed(!penta);
    }

    /**
     * A specific method to programmatically activate the penta-vs-choice
     * button.
     *
     * @param left
     */
    public void setLeftVsRightButton(boolean right) {
        this.setModified(true);
        MyToggle tog = (MyToggle) this.buttons.get(this.id_LeftVsRightButton);
        if (tog!=null) tog.setPressed(right);
    }

    /**
     * A specific method to programmatically activate the penta-vs-choice
     * button.
     *
     * @param penta
     */
    public void setNamesVsHideButton(boolean names) {
        this.setModified(true);
        MyToggle tog = (MyToggle) this.buttons.get(this.id_NamesVsHideButton);
        tog.setPressed(!names);
    }
    
    /**
     * A specific method to programmatically activate the penta-vs-choice
     * button.
     *
     * @param penta
     */
    public void setMobileDoVsAbsoluteButton(boolean mobile) {
        this.setModified(true);
        MyToggle tog = (MyToggle) this.buttons.get(this.id_MobileDoVsAbsoluteButton);
        if (tog!=null) tog.setPressed(mobile);
    }

    /**
     * A specific update method for the current exercise button.
     *
     * @param text
     */
    public void updateCurrentExerciseButton(String text) {
        this.setModified(true);
        MyButton but = this.buttons.get(this.id_RestartExerciseButton);
        if (but!=null)
            but.setText(text);
    }

    public void updateTimeSignatureButton(String text) {
        this.setModified(true);
        this.buttons.get(this.id_TimeSignatureButton).setText(text);
    }

    /**
     * A specific update method for the current key button.
     *
     * @param text
     */
    public void updateCurrentKeyButton(String text) {
        this.setModified(true);
        this.buttons.get(this.id_ResetKeyButton).setText(text);
    }

    private static class ButtonInfo {

        int id;
        String name;
        char type; // 'B', button; 'T', toggle; 'C', circular button;
        int row;
        int col;
        String textOn;
        String textOff;
        String tipText;

        ButtonInfo(int id, String name, char whichButtonType, int row, int col, String textOn, String textOff, String tipText) {
            this.id = id;
            this.name = name;
            this.type = whichButtonType;
            this.row = row;
            this.col = col;
            this.textOn = textOn;
            this.textOff = textOff;
            this.tipText = tipText;
        }
    }
}
