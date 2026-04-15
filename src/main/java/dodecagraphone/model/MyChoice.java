package dodecagraphone.model;

/**
 *
 * @author grogmgpt
 */
import dodecagraphone.MyController;
import dodecagraphone.model.component.MyXiloKey;
import dodecagraphone.ui.MyDialogs;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
        boolean isChromatic = false;
        // Primer pas: selecció general
//        String[] tipusOpcions = {"NoneSelected", "Interval", "Chord", "Scale", "List", "NoChoice"};
        String[] tipusOpcions = {"Scale", "Chord", "Interval", "NoneSelected", "List"};
        String tipusSeleccio = MyDialogs.seleccionaOpcio(
                "Choose a Template:", // Missatge
                "Template", // Títol
                tipusOpcions,
                0 // Per defecte: None
        );
        
        if (tipusSeleccio == null) return;
        if (tipusSeleccio.equalsIgnoreCase("NoChoice")) {
            setNoChoice();
        } else if (tipusSeleccio.equalsIgnoreCase("NoneSelected")) {
            setNoneChoice();
        } else if (tipusSeleccio.equalsIgnoreCase("Interval")) {
            // Segon pas: selecció d'interval (enter)
            String entrada = MyDialogs.mostraInputDialog(
                    "Enter interval number (in semitons):",
                    "Interval"
            );
            if (entrada != null) {
                try {
                    int interval = Integer.parseInt(entrada.trim());
                    setIntervalChoice(interval);
                } catch (Exception e) {
                    MyDialogs.mostraError("Invalid interval.", "Error");                
                }
            } else return;
        } else if (tipusSeleccio.equalsIgnoreCase("List")) {
            // Segon pas: selecció d'interval (enter)
            String entrada = MyDialogs.mostraInputDialog(
                    "Enter a list of intervals. Ex.[0,4,5]:",
                    "List"
            );
            if (entrada != null) {
                try {
                    setListChoice(entrada.trim());
                } catch (Exception e) {
                    MyDialogs.mostraError("Invalid list.", "Error");                
                }
            } else return;
        } else if (tipusSeleccio.equalsIgnoreCase("Chord")) {
            // Segon pas: selecció d'acord
            String[] acords = {"Major", "Minor", "Diminished", "Augmented"};
            String acordSeleccio = MyDialogs.seleccionaOpcio(
                    "Chose a chord pattern: ",
                    "Chord",
                    acords,
                    0
            );
            if (acordSeleccio==null)return;
            if ("Major".equalsIgnoreCase(acordSeleccio)) {
                setMajorChordChoice();
            } else if ("Minor".equalsIgnoreCase(acordSeleccio)) {
                setMinorChordChoice();
            }else if ("Diminished".equalsIgnoreCase(acordSeleccio)) {
                setDiminishedChordChoice();
            }else if ("Augemented".equalsIgnoreCase(acordSeleccio)) {
                setAugmentedChordChoice();
            }
        } else if (tipusSeleccio.equalsIgnoreCase("Scale")) {
            // Segon pas: selecció d'escala
            String[] escales = {"MajorFirstFive","Major","Major full range","Minor","Minor full range","Harmonic minor","MajorPentatonic","Blues","Chromatic"};
            String escalaSeleccio = MyDialogs.seleccionaOpcio(
                    "Chose a scale pattern: ",
                    "Scale",
                    escales,
                    0
            );
            if (escalaSeleccio==null)return;
            if ("MajorFirstFive".equalsIgnoreCase(escalaSeleccio)) {
                setFirstFiveScaleChoice();
            } else if ("Major".equalsIgnoreCase(escalaSeleccio)) {
                setMajorScaleChoice();
            } else if ("Major full range".equalsIgnoreCase(escalaSeleccio)) {
                setFullRangeMajorScaleChoice();
            } else if ("Minor".equalsIgnoreCase(escalaSeleccio)) {
                setMinorScaleChoice();
            } else if ("Minor full range".equalsIgnoreCase(escalaSeleccio)) {
                this.setFullRangeMinorScaleChoice();
            } else if ("Harmonic minor".equalsIgnoreCase(escalaSeleccio)) {
                setHarmonicMinorScaleChoice();
            } else if ("PentatonicMajor".equalsIgnoreCase(escalaSeleccio)) {
                setPentatonicMajorScaleChoice();
            }else if ("Blues".equalsIgnoreCase(escalaSeleccio)) {
                setBluesScaleChoice();
            }else if ("Chromatic".equalsIgnoreCase(escalaSeleccio)) {
                setChromaticScaleChoice();
                isChromatic = true;
            }
        }
        if(isChromatic) addUpRoot(ToneRange.getLowestMidi());
        else addUpRoot(rootMidi);
        
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
