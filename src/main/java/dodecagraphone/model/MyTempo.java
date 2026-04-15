package dodecagraphone.model;

import dodecagraphone.ui.Settings;

public class MyTempo {
    private static int tempo = Settings.DEFAULT_TEMPO; // 60BPM
    // private static int numSquaresBeat = Settings.DEFAULT_NUM_COLS_BEAT;
    private static final double factor = 1.1;
    private static final double increment = 5;
    public static int setTempo(int tpo){
        if (tpo>Settings.MAX_BPM)
            tempo = Settings.MAX_BPM;
        else if (tpo < 0)
            tempo = 0;
        else
          tempo = tpo;
        return tempo;
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
