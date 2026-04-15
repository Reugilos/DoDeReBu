package dodecagraphone.model.proves;

/**
 *
 * @author grogmgpt
 */
import javax.sound.midi.*;
import java.util.*;

public class TickScheduler {
    private final List<MidiEventWithTrack> events;
    private final Synthesizer synthesizer;
    private final int resolution;  // Resolució (PPQ)
    private long microsecondsPerTick;  // Durada d'un tick en microsegons

    public TickScheduler(List<MidiEventWithTrack> events, Synthesizer synthesizer, int resolution, long initialTempo) {
        this.events = new ArrayList<>(events);
        this.synthesizer = synthesizer;
        this.resolution = resolution;  // Inicialitzem la resolució del MIDI

        // Calcula la durada d'un tick en microsegons utilitzant el tempo inicial
        this.microsecondsPerTick = initialTempo / resolution;

        // Ordenem els esdeveniments per tick i després per track
        this.events.sort(Comparator.comparingLong(MidiEventWithTrack::getTick).thenComparingInt(MidiEventWithTrack::getTrack));
    }

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
