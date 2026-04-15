package dodecagraphone.model;

import dodecagraphone.ui.Settings;

public class MyTempo {
    private static int tempo = Settings.DEFAULT_TEMPO; // 60BPM
    // private static int numSquaresBeat = Settings.DEFAULT_NUM_COLS_BEAT;
    private static final double factor = 1.1;
    private static final double increment = 5;
    public static void setTempo(int tpo){
        tempo = tpo;
        // int numSquaresBeat = Settings.getnColsBeat();
        // if (numSquaresBeat!=Settings.getnColsBeat()) System.out.println("MyTempo::setTempo: param = "+numSquaresBeat+", settings = "+Settings.getnColsBeat());
    }
    public static double getNanosPerSquareGrid(){
        double mpsq = 60000000000.0/(double)(Settings.getnColsBeat()*tempo);
        return mpsq; 
    }
    public static void faster(){
//        tempo = (int) Math.round(tempo*factor);
        if (tempo<(Settings.MAX_BPM-increment))
            tempo = (int) Math.round(tempo+increment);
        else tempo = Settings.MAX_BPM;
    }
    public static void slower(){
//        tempo = (int) Math.round(tempo/factor);
        if (tempo-increment>0)
            tempo = (int) Math.round(tempo-increment);
        else tempo = 0;
    }

    public static int getTempo() {
        return tempo;
    }
        
//    public static void checkTempo(){
//        System.out.println("MyTempo::checkTempo: tempo = "+tempo+", nSquaresBeat = "+Settings.getnColsBeat()+", milisPerSquareGrid = "+getNanosPerSquareGrid());
//    }
}
