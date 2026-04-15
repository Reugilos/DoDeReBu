package dodecagraphone.model.chord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Pau
 */
public class ChordProgression {
    protected List<Integer> rootProgression;
    protected List<Chord> chordProgression;
    private Iterator<Chord> it;
    
    public void ChordProgression(){
        this.rootProgression = new ArrayList<>();
        chordProgression = new ArrayList<>();
    }
    
    public void add(Chord chord){
        rootProgression.add(chord.getRoot());
        chordProgression.add(chord);
    }
    
    public void setIterator(){
            it = chordProgression.iterator();
    }
    
    public Chord nextChord(){
        if (it.hasNext()){
            return it.next();
        }
        return null;
    }

    public boolean hasNextChord(){
        return it.hasNext();
    }

    public List<Integer> getRootProgression() {
        return rootProgression;
    }

    public List<Chord> getChordProgression() {
        return chordProgression;
    }

    @Override
    public String toString(){
        String output="";
        this.setIterator();
        while (hasNextChord()){
            output += nextChord().toString()+"/n";
        }
        return output;
    }
}
