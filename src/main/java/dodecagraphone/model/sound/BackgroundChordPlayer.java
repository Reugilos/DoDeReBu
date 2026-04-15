package dodecagraphone.model.sound;

import dodecagraphone.model.ToneRange;
import dodecagraphone.model.chord.Chord;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Pau
 */
public class BackgroundChordPlayer {

    private static final boolean LOCAL_VERBOSE = false;
    private final int CHORD_VOLUME = 30;
//    private boolean useChord;
    private boolean playing;
    private String soundInstrument;
    private int midiInstrument; // Reed organ
    private int chordVolume;
    private int chordChannel;
    private Chord currentChord, previousChord;
    private Map<Integer, Sound> sounds;
//    private int transpositionStep;
    private boolean paused;
    private boolean isOn;

    public BackgroundChordPlayer() {
        playing = false;
        soundInstrument = "BflatClarinet";
        midiInstrument = 21; // Reed organ
        chordVolume = CHORD_VOLUME;
        chordChannel = 0;
        currentChord = null;
        previousChord = null;
        sounds = new HashMap<>();
        paused = false;
        SoundWithMidi.assignInstToChannel(chordChannel, midiInstrument);
        SoundWithMidi.runProgramChange(chordChannel,midiInstrument);
        isOn = true;
    }

    public boolean isBackgroundChordPlayerOn() {
        return isOn;
    }

    public void setBackgroundChordPlayer(boolean isOn) {
        this.isOn = isOn;
    }

//    public void setDroneTmp(int key) {
//        chordMidis.clear();
//        set = true;
//        playing = false;
//        paused = false;
//        int chordMidi = key + chordInterval;
//        while (chordMidi < ToneRange.getLowestMidi()) {
//            chordMidi += 12;
//        }
//        chordMidis.add(chordMidi);
//        if (useChord) {
//            chordMidis.add(chordMidi + 12);
//            chordMidis.add(chordMidi + 19);
//        }
////        if (previousChord.isEmpty()){
////            for (int note : chordMidis) {
////                previousChord.add(note);
////            }            
////        }
//        SoundWithMidi.setInstrument(chordChannel, midiInstrument);
////        transpositionStep = 0;
//    }
//
////    public void setDrone(int key) { // Requires start background chord to play
////        chordMidis.clear();
////        set = true;
////        playing = false;
////        paused = false;
////        int chordMidi = key + chordInterval;
////        while (chordMidi < ToneRange.getLowestMidi()) {
////            chordMidi += 12;
////        }
////        chordMidis.add(chordMidi);
////        if (useChord) {
////            chordMidis.add(chordMidi + 12);
////            chordMidis.add(chordMidi + 19);
////        }
//////        if (previousChord.isEmpty()){
//////            for (int note : chordMidis) {
//////                previousChord.add(note);
//////            }            
//////        }
////        SoundWithMidi.setInstrument(chordChannel, midiInstrument);
//////        transpositionStep = 0;
////    }
////
//    public void setChord(Chord chord) {
//        currentChord = chord;
//        set = true;
//        playing = false;
//        paused = false;
////        if (previousChord.isEmpty()){
////            for (int note : chordMidis) {
////                previousChord.add(note);
////            }            
////        }
//////        transpositionStep = 0;
//    }
    public void clear() {
        playing = false;
        paused = false;
        currentChord = null;
        previousChord = null;
        sounds.clear();
        //       transpositionStep = 0;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isPaused() {
        return paused;
    }

    public void pause() {
        if (this.playing) {
            this.stopCurrent();
            this.paused = true;
        }
    }

//    public void useDroneChord(boolean useChord) {
//        this.useChord = useChord;
//    }
//
    public void stop() {
        if (this.isPlaying()) {
            this.stopPrevious();
        }
        this.playing = false;
    }

    public void setPrevious(Chord chord) {
        previousChord = chord.cloneChord();
    }

    public void setNPlay(Chord ch) {
//        this.stop();
//        this.setPrevious(currentChord);
        this.currentChord = ch.cloneChord();
        this.play();
    }

    public void play() {
        this.stopPrevious();
        this.setPrevious(currentChord);
        if (LOCAL_VERBOSE) {
            System.out.print("Play " + currentChord.getMidiShapeAsList());
            System.out.println(", Previous " + previousChord.getMidiShapeAsList());
        }
        for (int midi : currentChord.getMidiNotes()) {
            if (SampleOrMidi.isMidi()) {
                SoundWithMidi.play(midi, chordChannel, chordVolume);
            } else {
                Sound so = new Sound(ToneRange.getFilename(midi, soundInstrument));
                sounds.put(midi, so);
                so.play();
            }
        }
        playing = true;
        paused = false;
    }

    public void stopPrevious() {
        if (previousChord != null) {
            if (LOCAL_VERBOSE) {
                System.out.println("Stop previous " + previousChord.getMidiShapeAsList());
            }
            for (int midi : previousChord.getMidiNotes()) {
                if (SampleOrMidi.isMidi()) {
                    SoundWithMidi.stop(midi, chordChannel);
                } else {
                    Sound so = sounds.get(midi);
                    so.stop();
                }
            }
        }
//        this.setPrevious(null);
    }

    public void stopCurrent() {
        if (currentChord != null) {
            if (LOCAL_VERBOSE) {
                System.out.println("Stop current " + currentChord.getMidiShapeAsList());
            }
            for (int midi : currentChord.getMidiNotes()) {
                if (SampleOrMidi.isMidi()) {
                    SoundWithMidi.stop(midi, chordChannel);
                } else {
                    Sound so = sounds.get(midi);
                    so.stop();
                }
            }
        }
    }

    public void transposeCurrentChord(int step) {
        if (currentChord != null) {
            if (LOCAL_VERBOSE) {
                System.out.print("Transpose " + currentChord.getMidiShapeAsList());
                System.out.println(", Previous " + previousChord.getMidiShapeAsList());
            }
            currentChord = currentChord.transpose(step);
            if (this.isPlaying() && !this.isPaused()) { // %% this.cam.isPlaying()
                this.play();
            }
        }
    }
}
