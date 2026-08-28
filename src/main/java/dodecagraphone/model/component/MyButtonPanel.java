/*
 * PolyForm Noncommercial License 1.0.0
 * Copyright (c) 2024-2026 Pau Bofill. Powered by Claude AI.
 * Full license / Llicència completa: LICENSE (project root / arrel del projecte)
 */
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;

/**
 * [CA] Panell de botons i commutadors que controlen l'aplicació. Inclou tots
 * els botons de reproducció, navegació, exportació, mixer, opcions de
 * visualització i exercicis. El layout es carrega des d'un fitxer de
 * configuració intern i s'utilitza reflexió per associar cada botó a un
 * mètode del controlador. El mapa {@code buttons} replica la llista
 * {@code subComponents}.
 * <p>
 * [EN] Button panel with toggles that control the application. Includes all
 * playback, navigation, export, mixer, view options and exercise buttons. The
 * layout is loaded from an internal configuration file and uses reflection to
 * associate each button with a controller method. The {@code buttons} map
 * replicates the {@code subComponents} list.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
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
    private int id_ExportButton = 37;
    private int id_FitAnacrusisButton = 39;
    private int id_TipsButton = 40;
    private int id_PrintButton = 41;
    private int id_TremoloButton = 42;
    private int id_MetronomeButton = 43;
    /**
     * True if the button panel has been modified and needs to be repaint.
     */
    private boolean modified;

    /**
     * [CA] Crea el panell de botons, carrega el layout des del fitxer CSV i
     * instancia tots els botons al seu lloc.
     * <p>
     * [EN] Creates the button panel, loads the layout from the CSV file and
     * instantiates all buttons at their positions.
     *
     * @param firstCol [CA] primera columna del component / [EN] first column of the component
     * @param firstRow [CA] primera fila del component / [EN] first row of the component
     * @param nCols    [CA] nombre de columnes / [EN] number of columns
     * @param nRows    [CA] nombre de files / [EN] number of rows
     * @param parent   [CA] component pare / [EN] parent component
     * @param contr    [CA] controlador principal / [EN] main controller
     */
    public MyButtonPanel(int firstCol, int firstRow, int nCols, int nRows, MyComponent parent, MyController contr) {
        super(firstCol, firstRow, nCols, nRows, parent, contr);
        this.buttons = new HashMap<>();
        this.loadButtonLayout();
        this.placeButtons();
        this.modified = false;
//        this.setNeedsDrawing(true);
        this.popUp = new JPopupMenu();
        this.popUp.setFocusable(false);
    }

    /**
     * [CA] Carrega el layout dels botons des del fitxer de recursos
     * {@code /defaults/ButtonLayout.csv}. Cada línia del CSV descriu un botó
     * amb id, nom, tipus (B/T/C), fila, columna, textos i tooltip.
     * <p>
     * [EN] Loads the button layout from the resource file
     * {@code /defaults/ButtonLayout.csv}. Each CSV line describes a button
     * with id, name, type (B/T/C), row, column, texts and tooltip.
     */
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
                // TextOn: una o més claus I18n separades per "/" (botons circulars 'C':
                // les etiquetes de les diferents opcions, en ordre). Cada clau es tradueix
                // individualment i es torna a unir amb "/".
                String textOn = "";
                if (fields.length > 5 && !fields[5].trim().isEmpty()) {
                    String[] keys = fields[5].trim().split("/");
                    StringBuilder sb = new StringBuilder();
                    for (int k = 0; k < keys.length; k++) {
                        if (k > 0) sb.append("/");
                        sb.append(I18n.t(keys[k].trim()));
                    }
                    textOn = sb.toString();
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
     * [CA] Instancia i col·loca tots els botons i commutadors al panell
     * d'acord amb el layout carregat prèviament.
     * <p>
     * [EN] Instantiates and places all buttons and toggles on the panel
     * according to the previously loaded layout.
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
            int extraGap = nRowsB;
            int cumExtra = 0;
            if (buttonInfo.row > 0) cumExtra += extraGap;
            if (buttonInfo.row > 7) cumExtra += extraGap;
            if (buttonInfo.row > 9) cumExtra += extraGap;
            if (buttonInfo.row > 11) cumExtra += extraGap;
            int yPosition = firstRow + buttonInfo.row * sepRows + cumExtra;

            switch (buttonInfo.type) {
                case 'T':
                    this.buttons.put(buttonInfo.id, but = new MyToggle(buttonInfo.id,
                            xPosition, yPosition, nColsB, nRowsB, this, controller, buttonInfo.textOn, buttonInfo.textOff, buttonInfo.tipText));
                    break;
                case 'B':
                    this.buttons.put(buttonInfo.id, but = new MyButton(buttonInfo.id,
                            xPosition, yPosition, nColsB, nRowsB, this, controller, buttonInfo.textOn, buttonInfo.tipText));
                    break;
                case 'C':
                default:
                    // Botó circular: estats = TextOn (opcions separades per "/") + TextOff (estat per defecte, al final).
                    this.buttons.put(buttonInfo.id, but = new MyCircularButton(buttonInfo.id,
                            xPosition, yPosition, nColsB, nRowsB, this, controller, buttonInfo.textOn, buttonInfo.textOff, buttonInfo.tipText));
                    List<String> states = new ArrayList<>(Arrays.asList(buttonInfo.textOn.split("/")));
                    if (!buttonInfo.textOff.isEmpty()) states.add(buttonInfo.textOff);
                    ((MyCircularButton) but).setStateList(states.toArray(new String[0]));
                    break;
            }
            this.subComponents.add(but);
        }
    }

    /**
     * [CA] Reposiciona tots els botons del panell en les noves coordenades.
     * S'invoca quan canvia la mida de la finestra.
     * <p>
     * [EN] Repositions all buttons on the panel at the new coordinates.
     * Called when the window is resized.
     *
     * @param firstCol [CA] nova primera columna / [EN] new first column
     * @param firstRow [CA] nova primera fila / [EN] new first row
     * @param nCols    [CA] nou nombre de columnes / [EN] new number of columns
     * @param nRows    [CA] nou nombre de files / [EN] new number of rows
     */
    public void resetButtons(int firstCol, int firstRow, int nCols, int nRows) {
        super.setDimensions(firstCol, firstRow, nCols, nRows);
        this.repositionButtons();
    }

    private void repositionButtons() {
        int nColsB  = Settings.getnColsButton();
        int nRowsB  = Settings.getnRowsButton();
        int firstCol = Settings.getFirstColButton();
        int sepCols  = Settings.getSepColsButton();
        int firstRow = 3;
        int sepRows  = 1 + nRowsB;

        for (ButtonInfo buttonInfo : buttonLayout.values()) {
            MyButton but = buttons.get(buttonInfo.id);
            if (but == null) continue;
            int xPosition = firstCol + (buttonInfo.col - 1) * sepCols;
            int extraGap  = nRowsB;
            int cumExtra  = 0;
            if (buttonInfo.row > 0)  cumExtra += extraGap;
            if (buttonInfo.row > 7)  cumExtra += extraGap;
            if (buttonInfo.row > 9)  cumExtra += extraGap;
            if (buttonInfo.row > 11) cumExtra += extraGap;
            int yPosition = firstRow + buttonInfo.row * sepRows + cumExtra;
            but.setDimensions(xPosition, yPosition, nColsB, nRowsB);
        }
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
            popUp.show(this.controller.getUi().getPanel(),
                    (int)(posX + 2 * Settings.getColWidth()),
                    (int)(posY + 2 * Settings.getRowHeight()));
        }
    }

    public void showCustomTip(String text, double posX, double posY) {
        showCustomTip(text, (int) posX - 10, (int) posY - 10);
    }

    public void showCustomTip(String text, int screenX, int screenY) {
        if (Settings.isTipsVisible() && !popUp.isVisible()) {
            this.popUp.removeAll();
            JToolTip toolTip = new JToolTip();
            toolTip.setTipText(text);
            this.popUp.add(toolTip);
            popUp.pack();
            popUp.show(this.controller.getUi().getPanel(), screenX, screenY);
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

    public int getId_RestartExerciseButton() {
        return id_RestartExerciseButton;
    }

    public MyButton getSelectChoiceButton() {
        return buttons.get(id_SelectChoiceButton);
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
     * @return [CA] cert si el panell s'ha modificat des de l'últim reset / [EN] true if the panel changed since the last reset
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Setter.
     *
     * @param modified [CA] nou valor del flag de modificació / [EN] new value of the modified flag
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
            case 'C':
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
     * @param id [CA] identificador del botó / [EN] button id
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
     * @param screenX [CA] coordenada X en píxels / [EN] X coordinate in pixels
     * @param screenY [CA] coordenada Y en píxels / [EN] Y coordinate in pixels
     * @return [CA] id del botó que conté el punt, o -1 / [EN] id of the button containing the point, or -1
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
     * [CA] Dibuixa tots els botons del panell i, si escau, el tip flotant.
     * <p>
     * [EN] Draws every button of the panel and, if applicable, the floating tip.
     *
     * @param g [CA] context gràfic on dibuixar / [EN] graphics context to draw on
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

    public void resetFitAnacrusisButton() {
        setFitAnacrusisButton(false);
    }

    public void setFitAnacrusisButton(boolean value) {
        MyButton b = this.buttons.get(this.id_FitAnacrusisButton);
        if (b instanceof MyToggle) {
            MyToggle tog = (MyToggle) b;
            if (tog.isPressed() != value) tog.toggle();
        }
        Settings.setFitAnacrusis(value);
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
     * @param text [CA] text a mostrar / [EN] text to display
     */
    public void updatePageNumButton(String text) {
        this.setModified(true);
        this.buttons.get(this.id_PageNumButton).setText(text);
    }

    /**
     * A specific update method for the tempo button.
     *
     * @param text [CA] text a mostrar / [EN] text to display
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
        this.setNamesVsHideButton(this.controller.getAllPurposeScore().getNoteDisplayMode());
        this.setMobileDoVsAbsoluteButton(ToneRange.isMovileDo());
        this.setTipsButton(Settings.isTipsVisible());
        this.setTremoloButton(this.controller.isTremoloActive());
//        MyToggle tog = (MyToggle) this.buttons.get(this.id_NamesVsHideButton);
//        System.out.println("MyButtonPanl::setToggleButtonsToProgramValues: isPressed(" + tog.getId() + ") = " + tog.isPressed() + ", showNames = " + this.controller.getScore().isShowNoteNames());
    }

    public int getId_MetronomeButton() { return id_MetronomeButton; }

    public void setTremoloButton(boolean active) {
        this.setModified(true);
        MyToggle tog = (MyToggle) this.buttons.get(this.id_TremoloButton);
        if (tog != null && tog.isPressed() != active) tog.toggle();
    }

    public void setTipsButton(boolean tipsOn) {
        this.setModified(true);
        MyToggle tog = (MyToggle) this.buttons.get(this.id_TipsButton);
        if (tog != null) tog.setPressed(!tipsOn);
    }

    /**
     * [CA] Sincronitza el botó Penta/Plantilla amb l'estat actual del programa.
     * <p>
     * [EN] Synchronizes the Penta/Template button with the program's current state.
     *
     * @param penta [CA] {@code true} si es mostren les franges de pentagrama / [EN] {@code true} if pentagram strips are shown
     */
    public void setPentaVsChoiceButton(boolean penta) {
        this.setModified(true);
        MyToggle tog = (MyToggle) this.buttons.get(this.id_PentaVsChoiceButton);
        if (tog != null) tog.setPressed(!penta);
    }

    /**
     * [CA] Sincronitza el botó Esquerra/Dreta (posició del teclat) amb l'estat
     * actual del programa.
     * <p>
     * [EN] Synchronizes the Left/Right button (keyboard position) with the
     * program's current state.
     *
     * @param right [CA] {@code true} si el teclat és a la dreta / [EN] {@code true} if the keyboard is on the right
     */
    public void setLeftVsRightButton(boolean right) {
        this.setModified(true);
        MyToggle tog = (MyToggle) this.buttons.get(this.id_LeftVsRightButton);
        if (tog!=null) tog.setPressed(right);
    }

    /**
     * [CA] Sincronitza el botó circular Names/Interv/Degree/Anglo/Hide amb l'estat
     * actual del programa, fixant-lo directament a l'índex {@code noteDisplayMode}
     * (un de {@code MyGridScore.NOTE_DISPLAY_*}, que coincideix amb l'ordre dels
     * estats del botó).
     * <p>
     * [EN] Synchronizes the Names/Interv/Degree/Anglo/Hide circular button with the
     * program's current state, setting it directly to index {@code noteDisplayMode}
     * (one of {@code MyGridScore.NOTE_DISPLAY_*}, which matches the button's state
     * order).
     *
     * @param noteDisplayMode [CA] mode de visualització de notes actual / [EN] current note display mode
     */
    public void setNamesVsHideButton(int noteDisplayMode) {
        this.setModified(true);
        MyCircularButton but = (MyCircularButton) this.buttons.get(this.id_NamesVsHideButton);
        if (but != null) but.setStateIndex(noteDisplayMode);
    }
    
    /**
     * [CA] Sincronitza el botó Absolut/Mòbil (do mòbil) amb l'estat actual del
     * programa.
     * <p>
     * [EN] Synchronizes the Absolute/Mobile (movable do) button with the
     * program's current state.
     *
     * @param mobile [CA] {@code true} si el do mòbil és actiu / [EN] {@code true} if movable do is active
     */
    public void setMobileDoVsAbsoluteButton(boolean mobile) {
        this.setModified(true);
        MyToggle tog = (MyToggle) this.buttons.get(this.id_MobileDoVsAbsoluteButton);
        if (tog!=null) tog.setPressed(mobile);
    }

    /**
     * A specific update method for the current exercise button.
     *
     * @param text [CA] text a mostrar / [EN] text to display
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
     * @param text [CA] text a mostrar / [EN] text to display
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
