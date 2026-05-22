/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.color;

import dodecagraphone.model.ToneRange;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.awt.Color;

/**
 * [CA] Conjunts de colors predefinits per a la UI i els gràfics de la partitura.
 * Defineix les constants cromàtiques (DO–TI i colors de fons/auxiliars) i les
 * gammes de colors per a notes enceses, atenuades, il·luminades, apagades,
 * de pentagrama, del piano i de selecció. Tots els mètodes i camps són estàtics;
 * la classe no s'instancia. Cal cridar {@link #initColors()} en arrencar l'aplicació.
 * <p>
 * [EN] Predefined color sets for the UI and score graphics. Defines the chromatic
 * constants (DO–TI and background/auxiliary colors) and the color palettes for
 * active notes, muted notes, illuminated notes, dimmed notes, pentagrama lines,
 * piano keys and selections. All methods and fields are static; the class is not
 * instantiated. {@link #initColors()} must be called at application startup.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class ColorSets {
    /** [CA] Índex de color per a DO. [EN] Color index for C. */
    public static final int DO = 0;
    /** [CA] Índex de color per a DO# / RE♭. [EN] Color index for C# / D♭. */
    public static final int DE = 1;
    /** [CA] Índex de color per a RE. [EN] Color index for D. */
    public static final int RE = 2;
    /** [CA] Índex de color per a RE# / MI♭. [EN] Color index for D# / E♭. */
    public static final int RI = 3;
    /** [CA] Índex de color per a MI. [EN] Color index for E. */
    public static final int MI = 4;
    /** [CA] Índex de color per a FA. [EN] Color index for F. */
    public static final int FA = 5;
    /** [CA] Índex de color per a FA# / SOL♭. [EN] Color index for F# / G♭. */
    public static final int FO = 6;
    /** [CA] Índex de color per a SOL. [EN] Color index for G. */
    public static final int SO = 7;
    /** [CA] Índex de color per a SOL# / LA♭. [EN] Color index for G# / A♭. */
    public static final int SA = 8;
    /** [CA] Índex de color per a LA. [EN] Color index for A. */
    public static final int LA = 9;
    /** [CA] Índex de color per a LA# / SI♭. [EN] Color index for A# / B♭. */
    public static final int LI = 10;
    /** [CA] Índex de color per a SI. [EN] Color index for B. */
    public static final int TI = 11;
    /** [CA] Índex de color per a botons. [EN] Color index for buttons. */
    public static final int BUTO = 12;
    /** [CA] Índex de color per al fons de la partitura. [EN] Color index for the score background. */
    public static final int FONS = 13;
    /** [CA] Índex de color marfil (color de fons alternatiu). [EN] Ivory color index (alternative background). */
    public static final int MARFIL = 14;
    /** [CA] Índex de color ivori (tecles negres del piano). [EN] Ivory color index (piano black keys). */
    public static final int IVORI = 15;
    /** [CA] Índex de color apagat (notes inactives). [EN] Dimmed color index (inactive notes). */
    public static final int APAGAT = 16;
    /** [CA] Índex de color per a cel·les buides. [EN] Color index for empty cells. */
    public static final int BUIT = 17;
    /** [CA] Índex de color per a les línies del pentagrama. [EN] Color index for pentagrama lines. */
    public static final int LINIA_PENTA = 18;
    /** [CA] Índex de color per a la línia de DO al pentagrama. [EN] Color index for the C line in the pentagrama. */
    public static final int DO_PENTA = 19;

    private static final TreeMap<Integer,Color> xinos=new TreeMap<Integer,Color>();
    private static final TreeMap<Integer,Color> pau=new TreeMap<Integer,Color>();
    private static final TreeMap<Integer,Color> bu=new TreeMap<Integer,Color>();
    private static final TreeMap<Integer,Color> ences=new TreeMap<Integer,Color>();
    private static final TreeMap<Integer,Color> mutted=new TreeMap<Integer,Color>();
    private static final TreeMap<Integer,Color> iluminat=new TreeMap<Integer,Color>();
    private static final TreeMap<Integer,Color> apagat=new TreeMap<Integer,Color>();
    private static final TreeMap<Integer,Color> pentagrama=new TreeMap<Integer,Color>();
    private static final TreeMap<Integer,Color> piano=new TreeMap<Integer,Color>();

    /**
     * [CA] Retorna el color de fons de la partitura.
     * <p>
     * [EN] Returns the background color of the score.
     *
     * @return [CA] color de fons / [EN] background color
     */
    public static Color getColorFons(){
        return ences.get(FONS);
    }

    /**
     * [CA] Retorna el color «encès» (actiu) per a la nota indicada (0–11).
     * <p>
     * [EN] Returns the «active» color for the given note (0–11).
     *
     * @param midi12 [CA] nota MIDI mod 12 (0 = DO, 11 = SI) / [EN] MIDI note mod 12 (0 = C, 11 = B)
     * @return [CA] color actiu de la nota / [EN] active color of the note
     */
    public static Color getEncesColor(int midi12){
        return ences.get(midi12);
    }

    /**
     * [CA] Retorna el color atenuat (mutted) per a la nota indicada (0–11).
     * <p>
     * [EN] Returns the muted color for the given note (0–11).
     *
     * @param midi12 [CA] nota MIDI mod 12 / [EN] MIDI note mod 12
     * @return [CA] color atenuat de la nota / [EN] muted color of the note
     */
    public static Color getMuttedColor(int midi12){
        return mutted.get(midi12);
    }

    /**
     * [CA] Retorna el color il·luminat per a la nota indicada (0–11).
     * <p>
     * [EN] Returns the illuminated color for the given note (0–11).
     *
     * @param midi12 [CA] nota MIDI mod 12 / [EN] MIDI note mod 12
     * @return [CA] color il·luminat de la nota / [EN] illuminated color of the note
     */
    public static Color getIluminatColor(int midi12){
        return iluminat.get(midi12);
    }

    /**
     * [CA] Retorna el color apagat (gris fosc) per a qualsevol nota.
     * El paràmetre {@code midi12} no s'utilitza (el color és uniforme).
     * <p>
     * [EN] Returns the dimmed (dark grey) color for any note.
     * The {@code midi12} parameter is not used (the color is uniform).
     *
     * @param midi12 [CA] nota MIDI mod 12 (no s'usa) / [EN] MIDI note mod 12 (unused)
     * @return [CA] color gris fosc / [EN] dark grey color
     */
    public static Color getApagatColor(int midi12){
        return new Color(60f / 256, 60f / 256, 60f / 256, 1.f);
//        return apagat.get(midi12);
    }

    /**
     * [CA] Retorna el color de cel·la desactivada (igual que el color de fons).
     * <p>
     * [EN] Returns the deactivated cell color (same as the background color).
     *
     * @param midi12 [CA] nota MIDI mod 12 (no s'usa) / [EN] MIDI note mod 12 (unused)
     * @return [CA] color de fons / [EN] background color
     */
    public static Color getDesactivatColor(int midi12){
        return ences.get(FONS);
    }

    /**
     * [CA] Retorna el color del pentagrama per a la nota MIDI absoluta indicada.
     * <p>
     * [EN] Returns the pentagrama color for the given absolute MIDI note.
     *
     * @param midi [CA] nota MIDI absoluta / [EN] absolute MIDI note
     * @return [CA] color de la cel·la al pentagrama / [EN] pentagrama cell color
     */
    public static Color getPentagramaColor(int midi){
        return pentagrama.get(midi);
    }

    /**
     * [CA] Retorna el color de selecció per a una nota, tenint en compte si la nota
     * és dins de la llista de seleccionades i si és entre el mínim i el màxim seleccionats.
     * <p>
     * [EN] Returns the selection color for a note, taking into account whether the note
     * is in the selected list and whether it is between the minimum and maximum selected.
     *
     * @param midi     [CA] nota MIDI absoluta / [EN] absolute MIDI note
     * @param selected [CA] llista de notes seleccionades / [EN] list of selected notes
     * @return [CA] color de selecció per a la nota / [EN] selection color for the note
     */
    public static Color getChoiceColor(int midi, List<Integer>selected) {
        int max = Utilities.max(selected);
        int min = Utilities.min(selected);
//        int relative = midi-key;
//        while (relative > 12) relative -=12;
//        while (relative < 0) relative +=12;
        if (selected.contains(midi)) {
            //if ((midi) % 12 == 0) {
               // return ences.get(DE).brighter();
            //} else {
                return ences.get(MARFIL);
//                return ences.get(APAGAT).brighter().brighter();
            //}
        } else {
            if (midi>=min && midi<=max){
//                return ences.get(MARFIL);
                return ences.get(LINIA_PENTA);
            }
            else{
                return ences.get(LINIA_PENTA);
//                return ences.get(MARFIL); // The whole score sheet is MARFIL
            }
        }
    }

    /**
     * [CA] Retorna el color de font (text) adequat per a una cel·la de graella
     * de la nota MIDI indicada, per garantir una bona llegibilitat.
     * <p>
     * [EN] Returns the appropriate font (text) color for a score grid cell
     * of the given MIDI note, to ensure good readability.
     *
     * @param midi [CA] nota MIDI absoluta / [EN] absolute MIDI note
     * @return [CA] color de font per al text de la cel·la / [EN] font color for the cell text
     */
    public static Color getGridSquareFontColor(int midi){
        switch (midi%12){
            case 2:
            case 3:
            case 4:
//            case 5:
//            case 6:
//            case 7:
                return Color.BLACK;
            default:
                return Color.WHITE;
        }
    }

    /**
     * [CA] Retorna el color del piano per a la nota MIDI absoluta indicada
     * (blanc per a tecles blanques, ivori per a tecles negres).
     * <p>
     * [EN] Returns the piano color for the given absolute MIDI note
     * (white for white keys, ivory for black keys).
     *
     * @param midi [CA] nota MIDI absoluta / [EN] absolute MIDI note
     * @return [CA] color de la tecla de piano / [EN] piano key color
     */
    public static Color getPianoColor(int midi){
        return piano.get(midi);
    }

    /**
     * [CA] Inicialitza totes les gammes de colors. Cal cridar aquest mètode
     * una sola vegada en arrencar l'aplicació, abans d'usar qualsevol altre mètode.
     * <p>
     * [EN] Initializes all color palettes. This method must be called once
     * at application startup, before using any other method.
     */
    public static void initColors(){
        setGamaBasicColors();
        setGamaIluminatColors();
        setGamaPentagramaColors();
        setGamaApagatColors();
        setGamaPianoColors();
        setGamaMuttedColors();
    }

    /**
     * [CA] Inicialitza la gamme de colors atenuats (mutted) per a les 12 notes
     * i els colors auxiliars (BUTO, FONS, IVORI, MARFIL, APAGAT).
     * <p>
     * [EN] Initializes the muted color palette for the 12 notes
     * and auxiliary colors (BUTO, FONS, IVORI, MARFIL, APAGAT).
     */
    private static void setGamaMuttedColors(){
        mutted.put(DO, new Color(192f / 256, 0f / 256, 0f / 256, 1.f));
        mutted.put(DE, new Color(248f / 256, 93f / 256, 75f / 256, 1.f));
        mutted.put(RE, new Color(254f / 256, 158f / 256, 71f / 256, 1.f));
        mutted.put(RI, new Color(252f / 256, 221f / 256, 68f / 256, 1.f));
        mutted.put(MI, new Color(255f / 256, 255f / 256, 2f / 256, 1.f));
        mutted.put(FA, new Color(128f / 256, 251f / 256, 56f / 256, 1.f));
        mutted.put(FO, new Color(28f / 256, 128f / 256, 132f / 256, 1.f));
        mutted.put(SO, new Color(60f / 256, 177f / 256, 255f / 256, 1.f));
        mutted.put(SA, new Color(58f / 256, 119f / 256, 202f / 256, 1.f));
        mutted.put(LA, new Color(74f / 256, 76f / 256, 163f / 256, 1.f));
        mutted.put(LI, new Color(95f / 256, 58f / 256, 127f / 256, 1.f));
        mutted.put(TI, new Color(103f / 256, 48f / 256, 67f / 256, 1.f));
        mutted.put(BUTO, new Color(125f / 256, 125f / 256, 125f / 256, 1.f));
        mutted.put(FONS, new Color(255f / 256, 255f / 256, 255f / 256, 1.f));
        mutted.put(IVORI,new Color(106f / 256, 80f / 256, 53f / 256, 1.f));
        mutted.put(MARFIL,new Color(255f / 256, 255f / 256, 220f / 256, 1.f));
        mutted.put(APAGAT, new Color(40f / 256, 40f / 256, 40f / 256, 1.f));
    }

    /**
     * [CA] Inicialitza la gamme de colors «xinos» (paleta alternativa amb tons
     * més vius i saturats). Ús intern per a la combinació de gammes.
     * <p>
     * [EN] Initializes the «xinos» color palette (alternative palette with
     * more vivid and saturated tones). Used internally for palette blending.
     */
    private static void setGamaXinos() {
        xinos.put(DO, new Color(211f / 256, 80f / 256, 97f / 256, 1.f));
        xinos.put(DE, new Color(240f / 256, 81f / 256, 54f / 256, 1.f));
        xinos.put(RE, new Color(255f / 256, 108f / 256, 47f / 256, 1.f));
        xinos.put(RI, new Color(255f / 256, 147f / 256, 81f / 256, 1.f));
        xinos.put(MI, new Color(255f / 256, 241f / 256, 102f / 256, 1.f));
        xinos.put(FA, new Color(0f / 256, 141f / 256, 108f / 256, 1.f));
        xinos.put(FO, new Color(0f / 256, 172f / 256, 140f / 256, 1.f));
        xinos.put(SO, new Color(18f / 256, 149f / 256, 216f / 256, 1.f));
        xinos.put(SA, new Color(100f / 256, 144f / 256, 232f / 256, 1.f));
        xinos.put(LA, new Color(27f / 256, 95f / 256, 170f / 256, 1.f));
        xinos.put(LI, new Color(118f / 256, 87f / 256, 157f / 256, 1.f));
        xinos.put(TI, new Color(125f / 256, 87f / 256, 135f / 256, 1.f));
    }

    /**
     * [CA] Inicialitza la gamme de colors «pau» (versió antiga). Ús intern.
     * <p>
     * [EN] Initializes the «pau» color palette (old version). Internal use.
     */
    private static void setGamaPauOld() {
        pau.put(DO, new Color(192f / 256, 0f / 256, 0f / 256, 1.f));
        pau.put(DE, new Color(248f / 256, 93f / 256, 75f / 256, 1.f));
        pau.put(RE, new Color(254f / 256, 158f / 256, 71f / 256, 1.f));
        pau.put(RI, new Color(252f / 256, 221f / 256, 68f / 256, 1.f));
        pau.put(MI, new Color(255f / 256, 255f / 256, 2f / 256, 1.f));
        pau.put(FA, new Color(128f / 256, 251f / 256, 56f / 256, 1.f));
        pau.put(FO, new Color(28f / 256, 128f / 256, 132f / 256, 1.f));
        pau.put(SO, new Color(60f / 256, 177f / 256, 255f / 256, 1.f));
        pau.put(SA, new Color(58f / 256, 119f / 256, 202f / 256, 1.f));
        pau.put(LA, new Color(74f / 256, 76f / 256, 163f / 256, 1.f));
        pau.put(LI, new Color(95f / 256, 58f / 256, 127f / 256, 1.f));
        pau.put(TI, new Color(103f / 256, 48f / 256, 67f / 256, 1.f));
    }

    /**
     * [CA] Inicialitza la gamme de colors «pau» (versió actual). Ús intern.
     * <p>
     * [EN] Initializes the «pau» color palette (current version). Internal use.
     */
    private static void setGamaPau() {
        pau.put(DO, new Color(192f / 256, 0f / 256, 0f / 256, 1.f));
        pau.put(DE, new Color(248f / 256, 93f / 256, 75f / 256, 1.f));
        pau.put(RE, new Color(254f / 256, 158f / 256, 71f / 256, 1.f));
        pau.put(RI, new Color(252f / 256, 221f / 256, 68f / 256, 1.f));
        pau.put(MI, new Color(255f / 256, 255f / 256, 2f / 256, 1.f));
        pau.put(FA, new Color(103f / 256, 201f / 256, 45f / 256, 1.f));
        pau.put(FO, new Color(33f / 256, 152f / 256, 156f / 256, 1.f));
        pau.put(SO, new Color(51f / 256, 150f / 256, 217f / 256, 1.f));
        pau.put(SA, new Color(58f / 256, 119f / 256, 202f / 256, 1.f));
        pau.put(LA, new Color(27f / 256, 95f / 256, 169f / 256, 1.f));
        pau.put(LI, new Color(95f / 256, 58f / 256, 127f / 256, 1.f));
        pau.put(TI, new Color(103f / 256, 48f / 256, 67f / 256, 1.f));
    }

    /**
     * [CA] Combina les gammes «pau» i «xinos» amb els pesos indicats per obtenir
     * un color intermedi. Ús intern per a la gamme «bu».
     * <p>
     * [EN] Blends the «pau» and «xinos» palettes with the given weights to obtain
     * an intermediate color. Used internally for the «bu» palette.
     *
     * @param color   [CA] índex de nota (DO–TI) / [EN] note index (DO–TI)
     * @param pesPau  [CA] pes de la gamme «pau» (0.0–1.0) / [EN] weight of the «pau» palette (0.0–1.0)
     * @param pesXino [CA] pes de la gamme «xinos» (0.0–1.0) / [EN] weight of the «xinos» palette (0.0–1.0)
     * @return [CA] color combinat / [EN] blended color
     */
    private static Color combina(int color, float pesPau, float pesXino){
        setGamaXinos();
        setGamaPau();
        float red = (pau.get(color).getRed()*pesPau+xinos.get(color).getRed()*pesXino)/256;
        float green = (pau.get(color).getGreen()*pesPau+xinos.get(color).getGreen()*pesXino)/256;
        float blue = (pau.get(color).getBlue()*pesPau+xinos.get(color).getBlue()*pesXino)/256;
        return new Color(red,green,blue,1.f);
    }

    /**
     * [CA] Inicialitza la gamme de colors «bu», que és una combinació ponderada
     * de les gammes «pau» i «xinos». Ús intern.
     * <p>
     * [EN] Initializes the «bu» color palette, which is a weighted blend
     * of the «pau» and «xinos» palettes. Internal use.
     */
    private static void setGamaBu() {
        bu.put(DO, combina(DO,0.4f,0.6f));
        bu.put(DE, combina(DE,0,1));
        bu.put(RE, combina(RE,0,1));
        bu.put(RI, combina(RI,0,1));
        bu.put(MI, combina(MI,0,1));
        bu.put(FA, combina(FA,0.3f,0.7f));
        bu.put(FO, combina(FO,0,1));
        bu.put(SO, combina(SO,0,1));
        bu.put(SA, combina(SA,0,1));
        bu.put(LA, combina(LA,0,1));
        bu.put(LI, combina(LI,0,1));
        bu.put(TI, combina(TI,0.3f,0.7f));
    }

    /**
     * [CA] Inicialitza la gamme de colors bàsics (encesos), que és la gamme «pau»
     * o «bu» segons la configuració {@link Settings#COLORS_BU}, i afegeix els colors
     * auxiliars (BUTO, FONS, IVORI, MARFIL, APAGAT, BUIT, LINIA_PENTA, DO_PENTA).
     * <p>
     * [EN] Initializes the basic (active) color palette, which is the «pau»
     * or «bu» palette depending on the {@link Settings#COLORS_BU} setting, and adds
     * auxiliary colors (BUTO, FONS, IVORI, MARFIL, APAGAT, BUIT, LINIA_PENTA, DO_PENTA).
     */
    private static void setGamaBasicColors(){
        if (Settings.COLORS_BU){
            setGamaBu();
            for (int nota = DO; nota <= TI; nota++){
                ences.put(nota,bu.get(nota));
            }
        } else {
            setGamaPau();
            for (int nota = DO; nota <= TI; nota++){
                ences.put(nota,pau.get(nota));
            }
        }
        ences.put(BUTO, new Color(125f / 256, 125f / 256, 125f / 256, 1.f));
        ences.put(FONS, new Color(255f / 256, 255f / 256, 255f / 256, 1.f));
        ences.put(IVORI,new Color(106f / 256, 80f / 256, 53f / 256, 1.f));
        ences.put(MARFIL,new Color(255f / 256, 255f / 256, 220f / 256, 1.f));
        ences.put(APAGAT, new Color(40f / 256, 40f / 256, 40f / 256, 1.f));
        ences.put(BUIT, new Color(240f / 256, 240f / 256, 240f / 256, 1.f));
        ences.put(LINIA_PENTA, new Color(180f / 256, 180f / 256, 180f / 256, 1.f));
        ences.put(DO_PENTA, new Color(255f / 256, 172f / 256, 168f / 256, 1.f));
    }

    /**
     * [CA] Inicialitza la gamme de colors il·luminats (versió aclarida de la gamme encesa)
     * per a les 12 notes i els colors auxiliars BUTO i FONS.
     * <p>
     * [EN] Initializes the illuminated color palette (brightened version of the active palette)
     * for the 12 notes and the BUTO and FONS auxiliary colors.
     */
    private static void setGamaIluminatColors(){
        iluminat.put(DO, ences.get(DO).brighter());
        iluminat.put(DE, ences.get(DE).brighter());
        iluminat.put(RE, ences.get(RE).brighter());
        iluminat.put(RI, ences.get(RI).brighter());
        iluminat.put(MI, ences.get(MI).brighter());
        iluminat.put(FA, ences.get(FA).brighter());
        iluminat.put(FO, ences.get(FO).brighter());
        iluminat.put(SO, ences.get(SO).brighter());
        iluminat.put(SA, ences.get(SA).brighter());
        iluminat.put(LA, ences.get(LA).brighter());
        iluminat.put(LI, ences.get(LI).brighter());
        iluminat.put(TI, ences.get(TI).brighter());
        iluminat.put(BUTO, new Color(200f / 256, 200f / 256, 200f / 256, 1.f));
        iluminat.put(FONS, ences.get(FONS));
    }

    /**
     * [CA] Inicialitza la gamme de colors apagats (versió enfosquida de la gamme encesa)
     * per a les 12 notes i els colors auxiliars BUTO i FONS.
     * <p>
     * [EN] Initializes the dimmed color palette (darkened version of the active palette)
     * for the 12 notes and the BUTO and FONS auxiliary colors.
     */
    private static void setGamaApagatColors(){
//        // OJO, subsumed a getApagatColor()
        apagat.put(DO, ences.get(DO).darker());
        apagat.put(DE, ences.get(DE).darker());
        apagat.put(RE, ences.get(RE).darker());
        apagat.put(RI, ences.get(RI).darker());
        apagat.put(MI, ences.get(MI).darker());
        apagat.put(FA, ences.get(FA).darker());
        apagat.put(FO, ences.get(FO).darker());
        apagat.put(SO, ences.get(SO).darker());
        apagat.put(SA, ences.get(SA).darker());
        apagat.put(LA, ences.get(LA).darker());
        apagat.put(LI, ences.get(LI).darker());
        apagat.put(TI, ences.get(TI).darker());
        apagat.put(BUTO, ences.get(BUTO));
        apagat.put(FONS, ences.get(FONS));
    }

    /**
     * [CA] Inicialitza la gamme de colors del pentagrama per a totes les notes
     * del rang MIDI. Les notes de les línies del pentagrama reben el color LINIA_PENTA,
     * el DO central rep DO_PENTA, i la resta rep MARFIL.
     * <p>
     * [EN] Initializes the pentagrama color palette for all notes in the MIDI range.
     * Notes on pentagrama lines receive the LINIA_PENTA color, middle C receives DO_PENTA,
     * and the rest receive MARFIL.
     */
    private static void setGamaPentagramaColors(){
        TreeSet<Integer> linies=new TreeSet<Integer>();
        int octavaAlta=12*ToneRange.getOctavesUp();
        linies.add(64+octavaAlta);
        linies.add(67+octavaAlta);
        linies.add(71+octavaAlta);
        linies.add(74+octavaAlta);
        linies.add(77+octavaAlta);
        if (!ToneRange.isMetallophone()){
            linies.add(57+octavaAlta);
            linies.add(53+octavaAlta);
            linies.add(50+octavaAlta);
            linies.add(47+octavaAlta);
            linies.add(43+octavaAlta);
        }
        for (int midi=ToneRange.getLowestMidi();midi<=ToneRange.getHighestMidi();midi++){
            if (linies.contains(midi)){
                pentagrama.put(midi,ences.get(LINIA_PENTA));
            }
            else if (midi == 60+octavaAlta){
                pentagrama.put(midi,ences.get(DO_PENTA));
            }
            else{
                pentagrama.put(midi,ences.get(MARFIL));
            }
        }
    }

    /**
     * [CA] Inicialitza la gamme de colors del piano per a totes les notes del rang MIDI.
     * Les notes corresponents a tecles negres del piano (1, 3, 6, 8, 10 mod 12)
     * reben el color IVORI; la resta reben MARFIL.
     * <p>
     * [EN] Initializes the piano color palette for all notes in the MIDI range.
     * Notes corresponding to piano black keys (1, 3, 6, 8, 10 mod 12) receive
     * the IVORI color; the rest receive MARFIL.
     */
    private static void setGamaPianoColors(){
        TreeSet<Integer> linies=new TreeSet<>();
        linies.add(1);
        linies.add(3);
        linies.add(6);
        linies.add(8);
        linies.add(10);
        for (int midi=ToneRange.getLowestMidi();midi<=ToneRange.getHighestMidi();midi++){
            if (linies.contains(midi%12)){
                piano.put(midi,ences.get(IVORI));
            }
            else{
                piano.put(midi,ences.get(MARFIL));
            }
        }
    }

}
