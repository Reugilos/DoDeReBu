/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
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
 * [CA] Classe que representa una elecció de notes (escala, acord, interval o llista personalitzada)
 * per destacar-les al teclat visual de l'aplicació. L'usuari selecciona el tipus d'elecció
 * mitjançant un diàleg gràfic i la llista de valors MIDI resultant s'usa per colorar el teclat.
 * <p>
 * [EN] Class representing a note choice (scale, chord, interval or custom list)
 * to highlight on the application's visual keyboard. The user selects the choice type
 * through a graphical dialog and the resulting MIDI value list is used to color the keyboard.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MyChoice {

    private List<Integer> choice;
    private boolean selecting = false;
    private MyController controller;

    /**
     * [CA] Construeix una {@code MyChoice} amb l'elecció per defecte de {@code Settings}.
     * <p>
     * [EN] Constructs a {@code MyChoice} with the default choice from {@code Settings}.
     *
     * @param contr [CA] controlador principal de l'aplicació / [EN] main application controller
     */
    public MyChoice(MyController contr) {
        this.controller = contr;
        this.choice = new ArrayList<>();
        this.choice.addAll(Arrays.asList(Settings.DEFAULT_CHOICE));
        this.selecting = false;
    }

    /**
     * [CA] Indica si l'usuari està en mode de selecció de notes.
     * <p>
     * [EN] Indicates whether the user is in note selection mode.
     *
     * @return [CA] {@code true} si s'està seleccionant / [EN] {@code true} if selecting
     */
    public boolean isSelecting() {
        return selecting;
    }

    /**
     * [CA] Estableix el mode de selecció.
     * <p>
     * [EN] Sets the selection mode.
     *
     * @param selecting [CA] {@code true} per activar el mode de selecció / [EN] {@code true} to activate selection mode
     */
    public void setSelecting(boolean selecting) {
        this.selecting = selecting;
    }

//    public void clearChoice(){
//        this.choice = new ArrayList<>();
//    }
    /**
     * [CA] Selecciona l'elecció a partir de l'identificador d'una tecla del teclat,
     * llegeix el MIDI corresponent i actualitza les franges i el teclat.
     * <p>
     * [EN] Selects the choice from a keyboard key identifier,
     * reads the corresponding MIDI and updates the strips and keyboard.
     *
     * @param keyId [CA] identificador de la tecla del teclat visual / [EN] visual keyboard key identifier
     */
    public void selectChoice(int keyId) {
        int midi = this.controller.getKeyboard().getKey(keyId).getMidi();
        applyChoiceRoot(midi);
    }

    /**
     * [CA] Mostra el diàleg de selecció de tipus de patró (primer l'opció d'ajust a la
     * tonalitat, després escales, acords, etc.), aplica la configuració dels intervals i
     * l'extensió, i retorna si el patró ja s'ha aplicat completament o si cal que
     * l'usuari cliqui la nota arrel al teclat.
     * <p>
     * [EN] Shows the pattern type selection dialog (fit to tonality first, then scales,
     * chords, etc.), applies the interval configuration and extension, and returns whether
     * the pattern has been fully applied or the user still needs to click the root note.
     *
     * @return [CA] {@code true} si el patró s'ha aplicat completament (cal retornar al mode normal);
     *         {@code false} si l'usuari ha de clicar la nota arrel al teclat
     *         [EN] {@code true} if the pattern was fully applied; {@code false} if the user
     *         must click the root note on the keyboard
     */
    public boolean showChoiceDialog() {
        String kFit      = I18n.t("choice.scale.fitToTonality");
        String kScale    = I18n.t("choice.type.scale");
        String kChord    = I18n.t("choice.type.chord");
        String kInterval = I18n.t("choice.type.interval");
        String kNone     = I18n.t("choice.type.none");
        String kList     = I18n.t("choice.type.list");

        String[] tipusOpcions = { kFit, kScale, kChord, kInterval, kNone, kList };
        String tipusSeleccio = MyDialogs.seleccionaOpcio(
                I18n.t("choice.dialog.prompt"),
                I18n.t("choice.dialog.title"),
                tipusOpcions, 0);
        if (tipusSeleccio == null) return true;

        if (tipusSeleccio.equals(kFit)) {
            char mode = this.controller.getAllPurposeScore().getScaleMode();
            if (mode == 'm') setMinorScaleChoice();
            else             setMajorScaleChoice();
            askAndExtend();
            int pc = this.controller.getAllPurposeScore().getMidiKey() % 12;
            int lowestMidi = ToneRange.getLowestMidi();
            int root = lowestMidi + ((pc - lowestMidi % 12 + 12) % 12);
            addUpRoot(root);
            this.controller.getAllPurposeScore().updateStripsNKeyboard();
            return true;
        } else if (tipusSeleccio.equals(kNone)) {
            setNoneChoice();
            this.controller.getAllPurposeScore().updateStripsNKeyboard();
            return true;
        } else if (tipusSeleccio.equals(kInterval)) {
            String entrada = MyDialogs.mostraInputDialog(
                    I18n.t("choice.interval.prompt"),
                    I18n.t("choice.interval.title"));
            if (entrada == null) return true;
            try {
                setIntervalChoice(Integer.parseInt(entrada.trim()));
            } catch (Exception e) {
                MyDialogs.mostraError(I18n.t("choice.error.interval"), I18n.t("choice.error.title"));
                return true;
            }
        } else if (tipusSeleccio.equals(kList)) {
            String entrada = MyDialogs.mostraInputDialog(
                    I18n.t("choice.list.prompt"),
                    I18n.t("choice.list.title"));
            if (entrada == null) return true;
            try {
                setListChoice(entrada.trim());
            } catch (Exception e) {
                MyDialogs.mostraError(I18n.t("choice.error.list"), I18n.t("choice.error.title"));
                return true;
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
            if (sel == null) return true;
            if      (sel.equals(kMaj)) setMajorChordChoice();
            else if (sel.equals(kMin)) setMinorChordChoice();
            else if (sel.equals(kDim)) setDiminishedChordChoice();
            else if (sel.equals(kAug)) setAugmentedChordChoice();
        } else if (tipusSeleccio.equals(kScale)) {
            String kMajF5 = I18n.t("choice.scale.majorFirst5");
            String kMaj   = I18n.t("choice.scale.major");
            String kMin   = I18n.t("choice.scale.minor");
            String kHarm  = I18n.t("choice.scale.harmonicMinor");
            String kPenta = I18n.t("choice.scale.pentatonicMajor");
            String kBlues = I18n.t("choice.scale.blues");
            String kChrom = I18n.t("choice.scale.chromatic");
            String[] escales = { kMajF5, kMaj, kMin, kHarm, kPenta, kBlues, kChrom };
            String sel = MyDialogs.seleccionaOpcio(
                    I18n.t("choice.scale.prompt"),
                    I18n.t("choice.scale.title"),
                    escales, 0);
            if (sel == null) return true;
            if      (sel.equals(kMajF5)) setFirstFiveScaleChoice();
            else if (sel.equals(kMaj))   setMajorScaleChoice();
            else if (sel.equals(kMin))   setMinorScaleChoice();
            else if (sel.equals(kHarm))  setHarmonicMinorScaleChoice();
            else if (sel.equals(kPenta)) setPentatonicMajorScaleChoice();
            else if (sel.equals(kBlues)) setBluesScaleChoice();
            else if (sel.equals(kChrom)) setChromaticScaleChoice();
        }
        askAndExtend();
        return false; // cal que l'usuari cliqui la nota arrel
    }

    /**
     * [CA] Mostra el diàleg d'una octava / tot el teclat i amplia l'elecció si cal.
     * [EN] Shows the one-octave / full keyboard dialog and extends the choice if needed.
     */
    private void askAndExtend() {
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

    /**
     * [CA] Aplica la nota arrel a l'elecció actual i actualitza el teclat i les franges.
     * S'ha de cridar quan l'usuari clica la nota arrel al teclat (mode de selecció).
     * <p>
     * [EN] Applies the root note to the current choice and updates the keyboard and strips.
     * Called when the user clicks the root note on the keyboard (selection mode).
     *
     * @param midi [CA] valor MIDI de la nota arrel / [EN] MIDI value of the root note
     */
    public void applyChoiceRoot(int midi) {
        addUpRoot(midi);
        this.controller.getAllPurposeScore().updateStripsNKeyboard();
    }

    /**
     * [CA] Transposa tota l'elecció un nombre de semitons.
     * <p>
     * [EN] Transposes the entire choice by a number of semitones.
     *
     * @param step [CA] nombre de semitons a transposar (positiu cap amunt, negatiu cap avall) / [EN] semitones to transpose (positive up, negative down)
     */
    public void transposeChoice(int step){
        if (choice!=null && !choice.isEmpty()){
            for (int i=0;i<this.choice.size();i++){
                choice.set(i,choice.get(i)+ step);
            }
        }
    }

    /**
     * [CA] Afegeix el valor MIDI arrel a tots els elements de l'elecció (desplaça la llista).
     * <p>
     * [EN] Adds the root MIDI value to all elements of the choice (shifts the list).
     *
     * @param rootMidi [CA] valor MIDI arrel a afegir / [EN] root MIDI value to add
     */
    public void addUpRoot(int rootMidi){
        if (choice!=null && !choice.isEmpty()){
            for (int i=0; i<choice.size(); i++){
                choice.set(i,choice.get(i)+rootMidi);
            }
        }
    }

    /**
     * [CA] Afegeix una tecla a l'elecció actual i ordena la llista resultant.
     * <p>
     * [EN] Adds a key to the current choice and sorts the resulting list.
     *
     * @param key [CA] tecla del xilòfon a afegir / [EN] xylophone key to add
     */
    public void addKey(MyXiloKey key) {
        int midi = key.getMidi();
        System.out.println("MyChoice:addKey() choice = "+this.choice);
        this.choice.add(midi);
        Collections.sort(this.choice);
//        System.out.println("MyChoice::addKey:"+this.choice);
    }

    /**
     * [CA] Elimina una tecla de l'elecció actual.
     * <p>
     * [EN] Removes a key from the current choice.
     *
     * @param key [CA] tecla del xilòfon a eliminar / [EN] xylophone key to remove
     */
    public void removeKey(MyXiloKey key) {
        int midi = key.getMidi();
        this.choice.remove(Integer.valueOf(midi));
//        System.out.println("MyChoice::removeKey:"+this.choice);
        // Collections.sort(this.choice);
    }

    /**
     * [CA] Desactiva la visualització de l'elecció al teclat (oculta el ressaltat).
     * <p>
     * [EN] Deactivates the display of the choice on the keyboard (hides the highlight).
     */
    public void setNoChoice(){
        this.controller.getKeyboard().setShowChoice(false);
    }

    /**
     * [CA] Estableix una elecció buida (cap nota seleccionada) i activa la visualització.
     * <p>
     * [EN] Sets an empty choice (no note selected) and activates the display.
     */
    public void setNoneChoice() {
        this.choice = new ArrayList<>();
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Estableix l'elecció com l'escala diatònica major (intervals: 0,2,4,5,7,9,11,12).
     * <p>
     * [EN] Sets the choice as the diatonic major scale (intervals: 0,2,4,5,7,9,11,12).
     */
    public void setDiatonicScaleChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 2, 4, 5, 7, 9, 11, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Estableix l'elecció com les primeres 5 notes de l'escala major (intervals: 0,2,4,5,7).
     * <p>
     * [EN] Sets the choice as the first 5 notes of the major scale (intervals: 0,2,4,5,7).
     */
    public void setFirstFiveScaleChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 2, 4, 5, 7}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Estableix l'elecció com l'escala major completa (intervals: 0,2,4,5,7,9,11,12).
     * <p>
     * [EN] Sets the choice as the full major scale (intervals: 0,2,4,5,7,9,11,12).
     */
    public void setMajorScaleChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 2, 4, 5, 7, 9, 11, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Estén l'elecció actual a tot el rang disponible replicant-la en octaves superiors i inferiors.
     * <p>
     * [EN] Extends the current choice to the full available range by replicating it in upper and lower octaves.
     */
    public void extendChoiceToFullRange(){
        List<Integer> currentChoice = choice;
        choice.addAll(Utilities.listPlusConst(currentChoice, 12));
        choice.addAll(Utilities.listPlusConst(currentChoice, 24));
        choice.addAll(Utilities.listPlusConst(currentChoice, -12));
        choice.addAll(Utilities.listPlusConst(currentChoice, -24));
    }

    /**
     * [CA] Estableix l'elecció com l'escala major estesa a tot el rang.
     * <p>
     * [EN] Sets the choice as the major scale extended to the full range.
     */
    public void setFullRangeMajorScaleChoice(){
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 2, 4, 5, 7, 9, 11, 12}));
        extendChoiceToFullRange();
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Estableix l'elecció com l'escala menor estesa a tot el rang.
     * <p>
     * [EN] Sets the choice as the minor scale extended to the full range.
     */
    public void setFullRangeMinorScaleChoice(){
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 2, 3, 5, 7, 8, 10, 12}));
        extendChoiceToFullRange();
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Estableix l'elecció com l'escala cromàtica completa.
     * <p>
     * [EN] Sets the choice as the full chromatic scale.
     */
    public void setChromaticScaleChoice(){
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
            17, 18, 19, 20, 21, 22, 23, 24, 25}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Estableix l'elecció com l'escala menor natural (intervals: 0,2,3,5,7,8,10,12).
     * <p>
     * [EN] Sets the choice as the natural minor scale (intervals: 0,2,3,5,7,8,10,12).
     */
    public void setMinorScaleChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 2, 3, 5, 7, 8, 10, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Estableix l'elecció com l'escala menor harmònica (intervals: 0,2,3,5,7,8,11,12).
     * <p>
     * [EN] Sets the choice as the harmonic minor scale (intervals: 0,2,3,5,7,8,11,12).
     */
    public void setHarmonicMinorScaleChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 2, 3, 5, 7, 8, 11, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Estableix l'elecció com l'escala pentatònica (intervals: 0,2,4,7,9,10).
     * <p>
     * [EN] Sets the choice as the pentatonic scale (intervals: 0,2,4,7,9,10).
     */
    public void setPentatonicScaleChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 2, 4, 7, 9, 10}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Estableix l'elecció com l'escala pentatònica major (intervals: 0,2,4,7,9,12).
     * <p>
     * [EN] Sets the choice as the major pentatonic scale (intervals: 0,2,4,7,9,12).
     */
    public void setPentatonicMajorScaleChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 2, 4, 7, 9, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Estableix l'elecció com l'escala de blues (intervals: 0,3,5,6,7,10,12).
     * <p>
     * [EN] Sets the choice as the blues scale (intervals: 0,3,5,6,7,10,12).
     */
    public void setBluesScaleChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 3, 5, 6, 7, 10, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Estableix l'elecció com l'acord major (intervals: 0,4,7,12).
     * <p>
     * [EN] Sets the choice as the major chord (intervals: 0,4,7,12).
     */
    public void setMajorChordChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 4, 7, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Estableix l'elecció com l'acord menor (intervals: 0,3,7,12).
     * <p>
     * [EN] Sets the choice as the minor chord (intervals: 0,3,7,12).
     */
    public void setMinorChordChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 3, 7, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Estableix l'elecció com l'acord disminuït (intervals: 0,3,6,12).
     * <p>
     * [EN] Sets the choice as the diminished chord (intervals: 0,3,6,12).
     */
    public void setDiminishedChordChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 3, 6, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Estableix l'elecció com l'acord augmentat (intervals: 0,4,8,12).
     * <p>
     * [EN] Sets the choice as the augmented chord (intervals: 0,4,8,12).
     */
    public void setAugmentedChordChoice() {
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, 4, 8, 12}));
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Afegeix una llista d'enters al camp {@code choice} a partir d'una cadena
     * amb format {@code [int,int,...]}. Llença una excepció si el format és incorrecte.
     * <p>
     * [EN] Sets the choice from a string in the format {@code [int,int,...]}.
     * Throws an exception if the format is invalid.
     *
     * @param entrada [CA] cadena amb la llista d'enters entre claudàtors / [EN] string with integer list in brackets
     * @throws IllegalArgumentException [CA] si el format és incorrecte / [EN] if the format is invalid
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

    /**
     * [CA] Estableix l'elecció com un interval de dos sons (root i root+interval),
     * normalitzat entre -12 i +12.
     * <p>
     * [EN] Sets the choice as an interval of two pitches (root and root+interval),
     * normalized between -12 and +12.
     *
     * @param interval [CA] interval en semitons / [EN] interval in semitones
     */
    public void setIntervalChoice(int interval) {
        while (interval>12) interval-=12;
        while (interval<-12) interval+=12;
        choice = new ArrayList<>(Arrays.asList(new Integer[]{0, interval}));
        Collections.sort(choice);
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Estableix l'elecció per defecte definida a {@code Settings.DEFAULT_CHOICE}.
     * <p>
     * [EN] Sets the default choice defined in {@code Settings.DEFAULT_CHOICE}.
     */
    public void setDefaultChoice() {
        this.choice = new ArrayList<>(Arrays.asList(Settings.DEFAULT_CHOICE));
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Estableix l'elecció per defecte de dues octaves definida a {@code Settings.DEFAULT_CHOICE_2OCTAVES}.
     * <p>
     * [EN] Sets the two-octave default choice defined in {@code Settings.DEFAULT_CHOICE_2OCTAVES}.
     */
    public void setDefaultChoice2Octaves() {
        this.choice = new ArrayList<>(Arrays.asList(Settings.DEFAULT_CHOICE_2OCTAVES));
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Estableix l'elecció menor per defecte definida a {@code Settings.DEFAULT_MINOR_CHOICE}.
     * <p>
     * [EN] Sets the default minor choice defined in {@code Settings.DEFAULT_MINOR_CHOICE}.
     */
    public void setDefaultMinorChoice() {
        this.choice = new ArrayList<>(Arrays.asList(Settings.DEFAULT_MINOR_CHOICE));
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Estableix l'elecció menor per defecte de dues octaves definida a {@code Settings.DEFAULT_MINOR_CHOICE_2OCTAVES}.
     * <p>
     * [EN] Sets the two-octave default minor choice defined in {@code Settings.DEFAULT_MINOR_CHOICE_2OCTAVES}.
     */
    public void setDefaultMinorChoice2Octaves() {
        this.choice = new ArrayList<>(Arrays.asList(Settings.DEFAULT_MINOR_CHOICE_2OCTAVES));
        this.controller.getKeyboard().setShowChoice(true);
    }

    /**
     * [CA] Retorna l'elecció actual com a llista de valors MIDI enters.
     * <p>
     * [EN] Returns the current choice as a list of integer MIDI values.
     *
     * @return [CA] llista de valors MIDI de l'elecció actual / [EN] list of MIDI values of the current choice
     */
    public List<Integer> getChoiceList() {
        return choice;
    }

    /**
     * [CA] Estableix la llista de l'elecció directament.
     * <p>
     * [EN] Sets the choice list directly.
     *
     * @param choice [CA] nova llista de valors MIDI / [EN] new list of MIDI values
     */
    public void setChoiceList(List<Integer> choice) {
        this.choice = new ArrayList<>(choice);
    }
}
