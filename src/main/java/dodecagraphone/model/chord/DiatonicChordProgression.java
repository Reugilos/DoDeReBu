/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.chord;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * [CA] Progressió d'acords diatònica sobre una tonalitat i escala donades.
 * Genera automàticament una seqüència d'acords {@link Triad} a partir d'una
 * llista de graus de l'escala (en semitones relatius a la tònica) i una posició
 * d'inversió comuna per a tots els acords.
 * <p>
 * Estén {@link ChordProgression} i és útil per als exercicis d'ear training
 * que treballen progressions harmòniques diatòniques.
 * <p>
 * [EN] Diatonic chord progression over a given key and scale. Automatically
 * generates a sequence of {@link Triad} chords from a list of scale degrees
 * (in semitones relative to the tonic) and a common inversion position for
 * all chords.
 * <p>
 * Extends {@link ChordProgression} and is useful for ear training exercises
 * that work on diatonic harmonic progressions.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 * @see ChordProgression
 * @see Triad
 */
public class DiatonicChordProgression extends ChordProgression{

    /**
     * [CA] Crea una nova progressió d'acords diatònica. Per a cada grau de
     * {@code rootProgression} es construeix un {@link Triad} en la posició
     * indicada ({@link Triad#ROOT_POSITION}, {@link Triad#FIRST_INVERSION},
     * {@link Triad#SECOND_INVERSION} o {@link Triad#RANDOM_INVERSION}).
     * <p>
     * [EN] Creates a new diatonic chord progression. For each degree in
     * {@code rootProgression}, a {@link Triad} is built at the given position
     * ({@link Triad#ROOT_POSITION}, {@link Triad#FIRST_INVERSION},
     * {@link Triad#SECOND_INVERSION} or {@link Triad#RANDOM_INVERSION}).
     *
     * @param key              [CA] nota MIDI de la tònica (per exemple, 60 per a DO4) /
     *                         [EN] MIDI note of the tonic (e.g., 60 for middle C)
     * @param rootProgression  [CA] array de graus de l'escala en semitones relatius a la tònica /
     *                         [EN] array of scale degrees in semitones relative to the tonic
     * @param chordPosition    [CA] posició d'inversió aplicada a tots els acords de la progressió
     *                         (vegeu les constants de {@link Triad}) /
     *                         [EN] inversion position applied to all chords in the progression
     *                         (see {@link Triad} constants)
     */
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
