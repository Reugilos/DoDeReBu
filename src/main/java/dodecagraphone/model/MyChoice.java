package dodecagraphone.model;

/**
 *
 * @author grogmgpt
 */
import dodecagraphone.MyController;
import dodecagraphone.model.component.MyXiloKey;
import dodecagraphone.ui.I18n;
import dodecagraphone.ui.MyDialogs;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Classe que representa una elecció d'escala (None, Diatonic, Pentatonic).
 * L'usuari selecciona una opció mitjançant un diàleg gràfic.
 * ---------------------------------------- Class representing a scale choice
 * (None, Diatonic, Pentatonic). The user selects an option using a graphical
 * dialog.
 */
public class MyChoice {

    private List<Integer> choice;
    private boolean selecting = false;
    private MyController controller;

    public MyChoice(MyController contr) {
        this.controller = contr;
        this.choice = new ArrayList<>();
        this.choice.addAll(Arrays.asList(Settings.DEFAULT_CHOICE));
        this.selecting = false;
    }

    public boolean isSelecting() {
        return selecting;
    }

    public void setSelecting(boolean selecting) {
        this.selecting = selecting;
    }

//    public void clearChoice(){
//        this.choice = new ArrayList<>();
//    }
    /**
     * Selecciona el choice. Selects choice.
     */
    public void selectChoice(int keyId) {
        int midi = this.controller.getKeyboard().getKey(keyId).getMidi();
        //clearChoice();
        readChoice(midi);
        this.controller.getAllPurposeScore().updateStripsNKeyboard();
    }

    /**
     * Mostra un diàleg per triar l'escala i aplica la configuració
     * corresponent. Shows a dialog to select the scale and applies the
     * corresponding configuration.
     */
    public void readChoice(int rootMidi) {
        boolean isChromatic    = false;
        boolean canExtend      = false;
        int     effectiveRoot  = rootMidi;

        String kScale    = I18n.t("choice.type.scale");
        String kChord    = I18n.t("choice.type.chord");
        String kInterval = I18n.t("choice.type.interval");
        String kNone     = I18n.t("choice.type.none");
        String kList     = I18n.t("choice.type.list");

        String[] tipusOpcions = { kScale, kChord, kInterval, kNone, kList };
        String tipusSeleccio = MyDialogs.seleccionaOpcio(
                I18n.t("choice.dialog.prompt"),
                I18n.t("choice.dialog.title"),
                tipusOpcions, 0);
        if (tipusSeleccio == null) return;

        if (tipusSeleccio.equals(kNone)) {
            setNoneChoice();
        } else if (tipusSeleccio.equals(kInterval)) {
            String entrada = MyDialogs.mostraInputDialog(
                    I18n.t("choice.interval.prompt"),
                    I18n.t("choice.interval.title"));
            if (entrada == null) return;
            try {
                setIntervalChoice(Integer.parseInt(entrada.trim()));
                canExtend = true;
            } catch (Exception e) {
                MyDialogs.mostraError(I18n.t("choice.error.interval"), I18n.t("choice.error.title"));
                return;
            }
        } else if (tipusSeleccio.equals(kList)) {
            String entrada = MyDialogs.mostraInputDialog(
                    I18n.t("choice.list.prompt"),
                    I18n.t("choice.list.title"));
            if (entrada == null) return;
            try {
                setListChoice(entrada.trim());
                canExtend = true;
            } catch (Exception e) {
                MyDialogs.mostraError(I18n.t("choice.error.list"), I18n.t("choice.error.title"));
                return;
            }
        } else if (tipusSeleccio.equals(kChord)) {
            String kMaj  = I18n.t("choice.chord.major");
            String kMin  = I18n.t("choice.chord.minor");
            String kDim  = I18n.t("choice.chord.diminished");
            String kAug  = I18n.t("choice.chord.augmented");
            String[] acords = { kMaj, kMin, kDim, kAug };
            String sel = MyDialogs.seleccionaOpcio(
                    I18n.t("choice.chord.prompt"),
                    I18n.t("choice.chord.title"),
                    acords, 0);
            if (sel == null) return;
            if      (sel.equals(kMaj)) setMajorChordChoice();
            else if (sel.equals(kMin)) setMinorChordChoice();
            else if (sel.equals(kDim)) setDiminishedChordChoice();
            else if (sel.equals(kAug)) setAugmentedChordChoice();
            canExtend = true;
        } else if (tipusSeleccio.equals(kScale)) {
            String kMajF5 = I18n.t("choice.scale.majorFirst5");
            String kMaj   = I18n.t("choice.scale.major");
            String kMin   = I18n.t("choice.scale.minor");
            String kHarm  = I18n.t("choice.scale.harmonicMinor");
            String kPenta = I18n.t("choice.scale.pentatonicMajor");
            String kBlues = I18n.t("choice.scale.blues");
            String kChrom = I18n.t("choice.scale.chromatic");
            String kFit   = I18n.t("choice.scale.fitToTonality");
            String[] escales = { kMajF5, kMaj, kMin, kHarm, kPenta, kBlues, kChrom, kFit };
            String sel = MyDialogs.seleccionaOpcio(
                    I18n.t("choice.scale.prompt"),
                    I18n.t("choice.scale.title"),
                    escales, 0);
            if (sel == null) return;
            if      (sel.equals(kMajF5)) { setFirstFiveScaleChoice();       canExtend = true; }
            else if (sel.equals(kMaj))   { setMajorScaleChoice();           canExtend = true; }
            else if (sel.equals(kMin))   { setMinorScaleChoice();           canExtend = true; }
            else if (sel.equals(kHarm))  { setHarmonicMinorScaleChoice();   canExtend = true; }
            else if (sel.equals(kPenta)) { setPentatonicMajorScaleChoice(); canExtend = true; }
            else if (sel.equals(kBlues)) { setBluesScaleChoice();           canExtend = true; }
            else if (sel.equals(kChrom)) { setChromaticScaleChoice();       isChromatic = true; }
            else if (sel.equals(kFit))   {
                char mode = this.controller.getAllPurposeScore().getScaleMode();
                if (mode == 'm') setMinorScaleChoice();
                else             setMajorScaleChoice();
                // center the scale around middle C (C4 = MIDI 60)
                int pc = this.controller.getAllPurposeScore().getMidiKey() % 12;
                effectiveRoot = pc + 48;
                if (effectiveRoot < 49) effectiveRoot += 12;  // push C (48) up to 60
                canExtend = true;
            }
        }

        // Botó One octave / Extend (per tots els casos que ho suporten)
        if (canExtend) {
            String kOne  = I18n.t("choice.extend.oneOctave");
            String kFull = I18n.t("choice.extend.full");
            int result = JOptionPane.showOptionDialog(
                    null,
                    I18n.t("choice.extend.prompt"),
                    I18n.t("choice.extend.title"),
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new Object[]{ kOne, kFull },
                    kOne);
            if (result == 1) {
                extendChoiceToFullRange();
            }
        }

        if (isChromatic) addUpRoot(ToneRange.getLowestMidi());
        else             addUpRoot(effectiveRoot);
    }
    
    public void transposeChoice(int step){
        if (choice!=null && !choice.isEmpty()){
            for (int i=0;i<this.choice.size();i++){
                choice.set(i,choice.get(i)+ step);
            }
        }
    }

    public void addUpRoot(int rootMidi){
        if (choice!=null && !choice.isEmpty()){
            for (int i=0; i<choice.size(); i++){
                choice.set(i,choice.get(i)+rootMidi);
            }
        }
    }
    public void addKey(MyXiloKey key) {
        int midi = key.getMidi();
        System.out.println("MyChoice:addKey() choice = "+this.choice);
        this.choice.add(midi);
        Collections.sort(this.choice);
//        System.out.println("MyChoice::addKey:"+this.choice);
    }

    public void removeKey(MyXiloKey key) {
        int midi = key.getMidi();
        this.choice.remove(Integer.valueOf(midi));
//        System.out.println("MyChoice::removeKey:"+this.choice);
        // Collections.sort(this.choice);
    }

    public void setNoChoice(){
        this.controller.getKeyboard().setShowChoice(false);        
    }
    
    public void setNoneChoice() {
        this.choice = new ArrayList<>();
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void setDiatonicScaleChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 2, 4, 5, 7, 9, 11, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void setFirstFiveScaleChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 2, 4, 5, 7}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void setMajorScaleChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 2, 4, 5, 7, 9, 11, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void extendChoiceToFullRange(){
        List<Integer> currentChoice = choice;
        choice.addAll(Utilities.listPlusConst(currentChoice, 12));
        choice.addAll(Utilities.listPlusConst(currentChoice, 24));
        choice.addAll(Utilities.listPlusConst(currentChoice, -12));
        choice.addAll(Utilities.listPlusConst(currentChoice, -24));
    }
    
    public void setFullRangeMajorScaleChoice(){
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 2, 4, 5, 7, 9, 11, 12}));
        extendChoiceToFullRange();
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void setFullRangeMinorScaleChoice(){
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 2, 3, 5, 7, 8, 10, 12}));
        extendChoiceToFullRange();
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void setChromaticScaleChoice(){
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
            17, 18, 19, 20, 21, 22, 23, 24, 25}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void setMinorScaleChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 2, 3, 5, 7, 8, 10, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void setHarmonicMinorScaleChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 2, 3, 5, 7, 8, 11, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void setPentatonicScaleChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 2, 4, 7, 9, 10}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void setPentatonicMajorScaleChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 2, 4, 7, 9, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void setBluesScaleChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 3, 5, 6, 7, 10, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void setMajorChordChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 4, 7, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void setMinorChordChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 3, 7, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void setDiminishedChordChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 3, 6, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void setAugmentedChordChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 4, 8, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * Afegeix una llista d'enters al paràmetre `choice` a partir d'una cadena
     * amb format [int,int,...]. Llença una excepció si el format és incorrecte.
     *
     * Adds a list of integers to the `choice` field from a string in the format
     * [int,int,...]. Throws an exception if the format is invalid.
     *
     * @param entrada Cadena amb la llista d'enters
     * @throws IllegalArgumentException si el format és incorrecte
     */
    public void setListChoice(String entrada) throws IllegalArgumentException {
        if (entrada == null || !entrada.matches("\\[\\s*-?\\d+(\\s*,\\s*-?\\d+)*\\s*\\]")) {
            throw new IllegalArgumentException("Format incorrecte. Cal una llista d'enters entre claudàtors, com [0, 4, 5]");
        }

        // Elimina els claudàtors i separa pels comes
        String senseClaudators = entrada.substring(1, entrada.length() - 1);
        String[] parts = senseClaudators.split(",");

        List<Integer> parsedList = new ArrayList<>();
        for (String part : parts) {
            parsedList.add(Integer.parseInt(part.trim()));
        }

        this.choice = parsedList;
        Collections.sort(this.choice);
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void setIntervalChoice(int interval) {
        while (interval>12) interval-=12;
        while (interval<-12) interval+=12;
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, interval}));
        Collections.sort(choice);
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void setDefaultChoice() {
        this.choice = new ArrayList<>(Arrays.asList(Settings.DEFAULT_CHOICE));
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void setDefaultChoice2Octaves() {
        this.choice = new ArrayList<>(Arrays.asList(Settings.DEFAULT_CHOICE_2OCTAVES));
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void setDefaultMinorChoice() {
        this.choice = new ArrayList<>(Arrays.asList(Settings.DEFAULT_MINOR_CHOICE));
        this.controller.getKeyboard().setShowChoice(true);
    }

    public void setDefaultMinorChoice2Octaves() {
        this.choice = new ArrayList<>(Arrays.asList(Settings.DEFAULT_MINOR_CHOICE_2OCTAVES));
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * Retorna l'escala seleccionada com a llista d'enters. Returns the selected
     * scale as a list of integers.
     */
    public List<Integer> getChoiceList() {
        return choice;
    }

    public void setChoiceList(List<Integer> choice) {
        this.choice = new ArrayList<>(choice);
    }
}
