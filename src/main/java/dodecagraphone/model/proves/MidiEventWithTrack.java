/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.proves;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;

/**
 * [CA] Envoltori d'un {@link MidiEvent} que afegeix l'índex de la pista d'origen.
 * Permet ordenar i processar esdeveniments MIDI de múltiples pistes de forma unificada.
 * Codi experimental / prototip.
 * <p>
 * [EN] Wrapper around a {@link MidiEvent} that adds the source track index.
 * Allows sorting and processing MIDI events from multiple tracks in a unified way.
 * Experimental / prototype code.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MidiEventWithTrack {
    private final MidiEvent event;
    private final int track;

    /**
     * [CA] Construeix un nou envoltori associant l'event MIDI amb la seva pista d'origen.
     * <p>
     * [EN] Constructs a new wrapper associating the MIDI event with its source track.
     *
     * @param event [CA] event MIDI original / [EN] original MIDI event
     * @param track [CA] índex de la pista (0-basat) d'on prové l'event / [EN] zero-based index of the track the event comes from
     */
    public MidiEventWithTrack(MidiEvent event, int track) {
        this.event = event;
        this.track = track;
    }

    /**
     * [CA] Retorna l'event MIDI original.
     * <p>
     * [EN] Returns the original MIDI event.
     *
     * @return [CA] l'event MIDI encapsulat / [EN] the encapsulated MIDI event
     */
    public MidiEvent getEvent() {
        return event;
    }

    /**
     * [CA] Retorna l'índex de la pista d'origen de l'event.
     * <p>
     * [EN] Returns the source track index of the event.
     *
     * @return [CA] índex de la pista (0-basat) / [EN] zero-based track index
     */
    public int getTrack() {
        return track;
    }

    /**
     * [CA] Retorna la posició temporal de l'event en ticks MIDI.
     * <p>
     * [EN] Returns the temporal position of the event in MIDI ticks.
     *
     * @return [CA] posició en ticks MIDI / [EN] position in MIDI ticks
     */
    public long getTick() {
        return event.getTick();
    }

    /**
     * [CA] Retorna el missatge MIDI contingut en l'event.
     * <p>
     * [EN] Returns the MIDI message contained in the event.
     *
     * @return [CA] el missatge MIDI de l'event / [EN] the MIDI message of the event
     */
    public MidiMessage getMessage() {
        return event.getMessage();
    }
}
