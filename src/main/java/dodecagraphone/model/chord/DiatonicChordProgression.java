package dodecagraphone.model.chord;

import java.util.Arrays;
import java.util.LinkedList;

/**
 *
 * @author Pau
 */
public class DiatonicChordProgression extends ChordProgression{
    
    public DiatonicChordProgression(int key, Integer[] rootProgression, int chordPosition) {
        this.rootProgression = Arrays.asList(rootProgression);
        this.chordProgression = new LinkedList<>();
        for (int i = 0; i < rootProgression.length; i++) {
            int root = rootProgression[i];
            Chord chord = new Triad(root,key,chordPosition);
            this.chordProgression.add(chord);
        }
    }       
}
