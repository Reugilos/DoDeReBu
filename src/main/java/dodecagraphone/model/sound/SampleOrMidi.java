package dodecagraphone.model.sound;

/**
 *
 * @author Pau
 */
public class SampleOrMidi {
    private static boolean midi = true;
    private String instrument;

    public SampleOrMidi(String instrument) {
        midi = instrument.equalsIgnoreCase("midi");
        this.instrument = instrument;
    }

    public static boolean isMidi() {
        return midi;
    }

    public String getInstrument() {
        return instrument;
    }
    
    public String toString(){
        return instrument;
    }
    
}
