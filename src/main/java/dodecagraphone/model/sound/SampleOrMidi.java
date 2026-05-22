/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.sound;

/**
 * [CA] Abstracció que indica si el motor de so actiu és MIDI o mostres d'àudio (samples).
 * Conté un flag estàtic {@code midi} que s'estableix en crear la instància.
 * <p>
 * [EN] Abstraction indicating whether the active sound engine is MIDI or audio samples.
 * Contains a static {@code midi} flag that is set when the instance is created.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class SampleOrMidi {
    private static boolean midi = true;
    private String instrument;

    /**
     * [CA] Construeix una instància i estableix el mode de so (MIDI o sample) en funció del nom de l'instrument.
     * <p>
     * [EN] Constructs an instance and sets the sound mode (MIDI or sample) based on the instrument name.
     *
     * @param instrument [CA] nom de l'instrument; si és "midi" (ignorant majúscules), activa el mode MIDI / [EN] instrument name; if "midi" (case-insensitive), activates MIDI mode
     */
    public SampleOrMidi(String instrument) {
        midi = instrument.equalsIgnoreCase("midi");
        this.instrument = instrument;
    }

    /**
     * [CA] Indica si el motor de so actiu és MIDI.
     * <p>
     * [EN] Indicates whether the active sound engine is MIDI.
     *
     * @return [CA] {@code true} si és MIDI, {@code false} si és sample / [EN] {@code true} if MIDI, {@code false} if sample
     */
    public static boolean isMidi() {
        return midi;
    }

    /**
     * [CA] Retorna el nom de l'instrument associat a aquesta instància.
     * <p>
     * [EN] Returns the instrument name associated with this instance.
     *
     * @return [CA] nom de l'instrument / [EN] instrument name
     */
    public String getInstrument() {
        return instrument;
    }

    /**
     * [CA] Retorna el nom de l'instrument com a cadena de text.
     * <p>
     * [EN] Returns the instrument name as a string.
     *
     * @return [CA] nom de l'instrument / [EN] instrument name
     */
    public String toString(){
        return instrument;
    }

}
