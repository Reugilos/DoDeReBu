/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.chord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * [CA] Progressió d'acords musicals. Manté una llista ordenada d'acords
 * ({@link Chord}) i les seves arrels, i proporciona un iterador per recórrer-los
 * seqüencialment.
 * <p>
 * [EN] Musical chord progression. Maintains an ordered list of chords
 * ({@link Chord}) and their roots, and provides an iterator to traverse them
 * sequentially.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class ChordProgression {
    /** [CA] Llista de les arrels de la progressió. / [EN] List of progression roots. */
    protected List<Integer> rootProgression;
    /** [CA] Llista d'acords de la progressió. / [EN] List of progression chords. */
    protected List<Chord> chordProgression;
    /** [CA] Iterador intern sobre la llista d'acords. / [EN] Internal iterator over the chord list. */
    private Iterator<Chord> it;

    /**
     * [CA] Inicialitza les llistes internes de la progressió.
     * Nota: Aquest mètode té el nom convencional d'un constructor però és un mètode d'instància;
     * les llistes s'han d'inicialitzar cridant-lo explícitament.
     * <p>
     * [EN] Initialises the internal lists of the progression.
     * Note: this method has the conventional name of a constructor but is an instance method;
     * lists must be initialised by calling it explicitly.
     */
    public void ChordProgression(){
        this.rootProgression = new ArrayList<>();
        chordProgression = new ArrayList<>();
    }

    /**
     * [CA] Afegeix un acord a la progressió.
     * <p>
     * [EN] Adds a chord to the progression.
     *
     * @param chord [CA] acord a afegir / [EN] chord to add
     */
    public void add(Chord chord){
        rootProgression.add(chord.getRoot());
        chordProgression.add(chord);
    }

    /**
     * [CA] Reinicia l'iterador intern al principi de la progressió.
     * Cal cridar aquest mètode abans d'usar {@link #nextChord()} o {@link #hasNextChord()}.
     * <p>
     * [EN] Resets the internal iterator to the beginning of the progression.
     * Must be called before using {@link #nextChord()} or {@link #hasNextChord()}.
     */
    public void setIterator(){
            it = chordProgression.iterator();
    }

    /**
     * [CA] Retorna el següent acord de la progressió, o {@code null} si s'ha acabat.
     * <p>
     * [EN] Returns the next chord in the progression, or {@code null} if exhausted.
     *
     * @return [CA] el següent acord, o {@code null} / [EN] the next chord, or {@code null}
     */
    public Chord nextChord(){
        if (it.hasNext()){
            return it.next();
        }
        return null;
    }

    /**
     * [CA] Retorna {@code true} si encara hi ha acords per recórrer amb l'iterador.
     * <p>
     * [EN] Returns {@code true} if there are still chords to iterate over.
     *
     * @return [CA] {@code true} si hi ha un acord següent / [EN] {@code true} if there is a next chord
     */
    public boolean hasNextChord(){
        return it.hasNext();
    }

    /**
     * [CA] Retorna la llista de les arrels de la progressió.
     * <p>
     * [EN] Returns the list of progression roots.
     *
     * @return [CA] llista d'arrels / [EN] list of roots
     */
    public List<Integer> getRootProgression() {
        return rootProgression;
    }

    /**
     * [CA] Retorna la llista d'acords de la progressió.
     * <p>
     * [EN] Returns the list of chords in the progression.
     *
     * @return [CA] llista d'acords / [EN] list of chords
     */
    public List<Chord> getChordProgression() {
        return chordProgression;
    }

    /**
     * [CA] Retorna una representació textual de tots els acords de la progressió,
     * separats per {@code /n}.
     * <p>
     * [EN] Returns a textual representation of all chords in the progression,
     * separated by {@code /n}.
     *
     * @return [CA] cadena de text amb tots els acords / [EN] text string with all chords
     */
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
