package dodecagraphone.model.color;

import dodecagraphone.model.ToneRange;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.awt.Color;

public class ColorSets {
    public static final int DO = 0;
    public static final int DE = 1;
    public static final int RE = 2;
    public static final int RI = 3;
    public static final int MI = 4;
    public static final int FA = 5;
    public static final int FO = 6;
    public static final int SO = 7;
    public static final int SA = 8;
    public static final int LA = 9;
    public static final int LI = 10;
    public static final int TI = 11;
    public static final int BUTO = 12;
    public static final int FONS = 13;
    public static final int MARFIL = 14;
    public static final int IVORI = 15;
    public static final int APAGAT = 16;
    public static final int BUIT = 17;
    public static final int LINIA_PENTA = 18;
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
        
    public static Color getColorFons(){
        return ences.get(FONS);
    }
    
    public static Color getEncesColor(int midi12){
        return ences.get(midi12);
    }

    public static Color getMuttedColor(int midi12){
        return mutted.get(midi12);
    }

    public static Color getIluminatColor(int midi12){
        return iluminat.get(midi12);
    }
        
    public static Color getApagatColor(int midi12){
        return new Color(60f / 256, 60f / 256, 60f / 256, 1.f);
//        return apagat.get(midi12);
    }

    public static Color getDesactivatColor(int midi12){
        return ences.get(FONS);
    }

    public static Color getPentagramaColor(int midi){
        return pentagrama.get(midi);
    }

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

    public static Color getPianoColor(int midi){
        return piano.get(midi);
    }

    public static void initColors(){
        setGamaBasicColors();
        setGamaIluminatColors();
        setGamaPentagramaColors();
        setGamaApagatColors();
        setGamaPianoColors();
        setGamaMuttedColors();
    }

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
    
    private static Color combina(int color, float pesPau, float pesXino){
        setGamaXinos();
        setGamaPau();
        float red = (pau.get(color).getRed()*pesPau+xinos.get(color).getRed()*pesXino)/256;
        float green = (pau.get(color).getGreen()*pesPau+xinos.get(color).getGreen()*pesXino)/256;
        float blue = (pau.get(color).getBlue()*pesPau+xinos.get(color).getBlue()*pesXino)/256;
        return new Color(red,green,blue,1.f);
    }

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
