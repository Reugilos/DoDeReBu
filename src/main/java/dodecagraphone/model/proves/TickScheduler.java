/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.proves;

/**
 * [CA] Planificador de ticks MIDI per a proves. Itera sobre una llista d'esdeveniments
 * {@link MidiEventWithTrack} ordenats per tick i els envia al sintetitzador del sistema
 * en el moment correcte, calculant la durada de cada tick a partir de la resolució PPQ
 * i el tempo inicial. Codi experimental / prototip.
 * <p>
 * [EN] MIDI tick scheduler for testing. Iterates over a list of tick-sorted
 * {@link MidiEventWithTrack} events and dispatches them to the system synthesiser at
 * the correct time, computing each tick duration from the PPQ resolution and initial
 * tempo. Experimental / prototype code.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
import javax.sound.midi.*;
import java.util.*;

public class TickScheduler {
    private final List<MidiEventWithTrack> events;
    private final Synthesizer synthesizer;
    private final int resolution;  // Resolució (PPQ)
    private long microsecondsPerTick;  // Durada d'un tick en microsegons

    /**
     * [CA] Construeix el planificador: copia la llista d'esdeveniments, calcula la
     * durada d'un tick a partir del tempo i la resolució, i ordena els esdeveniments
     * per tick (i per pista en cas d'empat).
     * <p>
     * [EN] Constructs the scheduler: copies the event list, computes the tick duration
     * from the tempo and resolution, and sorts the events by tick (and by track on tie).
     *
     * @param events        [CA] llista d'esdeveniments MIDI a processar / [EN] list of MIDI events to process
     * @param synthesizer   [CA] sintetitzador destinatari de la reproducció / [EN] synthesiser for playback output
     * @param resolution    [CA] resolució en PPQ (ticks per quarter note) / [EN] PPQ resolution (ticks per quarter note)
     * @param initialTempo  [CA] tempo inicial en microsegons per quarter note / [EN] initial tempo in microseconds per quarter note
     */
    public TickScheduler(List<MidiEventWithTrack> events, Synthesizer synthesizer, int resolution, long initialTempo) {
        this.events = new ArrayList<>(events);
        this.synthesizer = synthesizer;
        this.resolution = resolution;  // Inicialitzem la resolució del MIDI

        // Calcula la durada d'un tick en microsegons utilitzant el tempo inicial
        this.microsecondsPerTick = initialTempo / resolution;

        // Ordenem els esdeveniments per tick i després per track
        this.events.sort(Comparator.comparingLong(MidiEventWithTrack::getTick).thenComparingInt(MidiEventWithTrack::getTrack));
    }

    /**
     * [CA] Inicia la reproducció seqüencial dels esdeveniments MIDI. Per a cada tick,
     * espera la durada calculada i llavors envia tots els esdeveniments d'aquell tick
     * al sintetitzador via {@link MidiCommandHandler}.
     * <p>
     * [EN] Starts the sequential playback of MIDI events. For each tick, waits the
     * computed duration and then dispatches all events for that tick to the synthesiser
     * via {@link MidiCommandHandler}.
     *
     * @throws InterruptedException [CA] si el fil de reproducció és interromput /
     *                              [EN] if the playback thread is interrupted
     */
    public void start() throws InterruptedException {
        long currentTick = 0;
        Iterator<MidiEventWithTrack> eventIterator = events.iterator();

        MidiEventWithTrack nextEvent = eventIterator.hasNext() ? eventIterator.next() : null;

        long startTime = System.currentTimeMillis();  // Guarda el temps d'inici

        while (nextEvent != null) {
            // Pausa per la durada d'un tick
            long targetTimeForTick = (long) (microsecondsPerTick / 1000);  // En mil·lisegons
            Thread.sleep(targetTimeForTick);

            // Processa tots els esdeveniments que tenen el mateix tick
            while (nextEvent != null && currentTick == nextEvent.getTick()) {
                MidiMessage message = nextEvent.getMessage();
                int track = nextEvent.getTrack();
                long tick = nextEvent.getTick();
                MidiCommandHandler.handleMidiMessage(message, synthesizer, track, tick);
                nextEvent = eventIterator.hasNext() ? eventIterator.next() : null;
            }

            currentTick++;
        }
    }
}
