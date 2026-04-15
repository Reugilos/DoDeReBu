package dodecagraphone.model.proves;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;

/**
 *
 * @author grogmgpt
 */
public class MidiEventWithTrack {
    private final MidiEvent event;
    private final int track;

    public MidiEventWithTrack(MidiEvent event, int track) {
        this.event = event;
        this.track = track;
    }

    public MidiEvent getEvent() {
        return event;
    }

    public int getTrack() {
        return track;
    }

    public long getTick() {
        return event.getTick();
    }

    public MidiMessage getMessage() {
        return event.getMessage();
    }
}
