package dodecagraphone.model.exercise;

import dodecagraphone.model.MyTempo;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.chord.Chord;
import dodecagraphone.model.chord.Triad;
import dodecagraphone.model.component.MyExercise;
import dodecagraphone.model.sound.SoundWithMidi;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import java.util.Arrays;
import java.util.List;

public class Test implements MyExerciseFamily {

    private static final List<String> all = Arrays.asList("Metro", "Ex1", "Ex2");

    public static List<String> getExerciseLabelList() {
        return all;
    }

//    @Override
//    public String getName() {
//        return "Test";
//    }
//
//    @Override
//    public String getDescription() {
//        return "Testing MIDI messages and background chords.";
//    }
//
    @Override
    public void applyExercise(MyExercise ex, String label) {
        switch (label) {
            case "Metro" -> setMetronome(ex);
            case "Ex1" -> setExercise_1(ex);
            case "Ex2" -> setExercise_2(ex);
            case "Blank" -> {
                ex.setDescription("");
                ex.setLabel("Blank");
                ex.getController().clearScore();
            }
            default -> {
                ex.getController().clearScore();
                throw new UnsupportedOperationException("No such pattern: " + label);
            }
        }
    }

    private void setMetronome(MyExercise ex){
        ex.setDescription("Test speed");
        ex.setMidiKey(ToneRange.MIDDLE_C + 3);
        ex.setUsePentagramaStrips(true);
        MyTempo.setTempo(60);
        ex.setNumBeatsMeasure(4);
        ex.setBeatFigure(4);
        ToneRange.setMovileDo(false);
        // ex.setDelay(12);
        ex.setShowNoteNames(true);

        while (!ex.isOver()) {
            ex.placeNote(64, (int) (0.5 * MyExercise.ONE_BEAT), false);
            ex.skipCols( (int) (0.5 * MyExercise.ONE_BEAT));
            ex.placeNote(60, (int) (0.25 * MyExercise.ONE_BEAT), false);
            ex.skipCols( (int) (0.75 * MyExercise.ONE_BEAT));
            ex.placeNote(60, (int) (0.25 * MyExercise.ONE_BEAT), false);
            ex.skipCols( (int) (0.75 * MyExercise.ONE_BEAT));
            ex.placeNote(60, (int) (0.25 * MyExercise.ONE_BEAT), false);
            ex.skipCols( (int) (0.75 * MyExercise.ONE_BEAT));
        }
    }
    
    private void setExercise_1(MyExercise ex) {
        ex.setDescription("Test midi message");
        ex.setMidiKey(ToneRange.MIDDLE_C + 3);
        ex.setUsePentagramaStrips(true);
        MyTempo.setTempo(40);
        ex.setNumBeatsMeasure(3);
        ex.setBeatFigure(4);
        ToneRange.setMovileDo(true);
        // ex.setDelay(12);
        ex.setShowNoteNames(true);

        Chord keyChrd = new Chord(0, new int[]{0, 12, 19}, ex.getMidiKey(), "Place key chord in background");
        ex.placeBackgroundChord(keyChrd);
        ex.setChoice(new Integer[]{0, 2, 4, 5, 7, 9, 11, 12, -1, -3, -5, -7, -8, -10, -12});

        int n = 0;
        int channel = 1;
        while (!ex.isOver()) {
            int note = Utilities.randFromList(ex.getChoice().getChoiceList()) + ex.getMidiKey();
            ex.placeAppendMessage(note + " " + ToneRange.getNoteName(note, ex.getMidiKey()) + "\n", MyExercise.MESSAGE_DELAY);
            ex.placeNote(note, 2 * MyExercise.ONE_BEAT, false, false, channel, 0, 100);
            n++;

            if (n == 6) {
                ShortMessage message = new ShortMessage();
                int chan = 2;
                int inst = 21;
                try {
                    message.setMessage(ShortMessage.PROGRAM_CHANGE, chan, inst, 0);
                } catch (InvalidMidiDataException e) {
                    e.printStackTrace();
                }
                ex.placeAppendMidiMessage(message);
            }

            if (n == 8) {
                MidiMessage mess = SoundWithMidi.createTextMessage("channel::2");
                ex.placeAppendMidiMessage(mess);
            }

            if (n == 2) {
                ex.placeStopBackgroundChord();
            }
        }
    }

    private void setExercise_2(MyExercise ex) {
        ex.setDescription("Test background chord");
        ex.setMidiKey(ToneRange.MIDDLE_C - 6);
        ex.setUsePentagramaStrips(false);
        ToneRange.setMovileDo(false);
        ex.getController().setScreenKeyboardRight(true);
        ex.setShowNoteNames(true);
        int[] arpeggiatura = new int[]{1, 2, 3, 2, 1};
        ex.setChoice(new Integer[]{0, 2, 4, 5, 7, 9, 11, 12, -12, -10, -8, -7, -5, -3, -1});

        Chord keyChrd = new Chord(0, new int[]{0, 12, 19}, ex.getMidiKey(), "Place key chord in background");
        ex.placeBackgroundChord(keyChrd);

        while (!ex.isOver()) {
            int root = Utilities.randFromList(Arrays.asList(new Integer[]{0, 2, 4, 5, 7, 9, 11}));
            Chord chord = new Triad(root, ex.getMidiKey(), Triad.RANDOM_INVERSION);
            chord.setNCols(Settings.getnColsBeat() * Settings.getnBeatsMeasure());
            ex.placeChordSymbol(chord.getInRootPosition());
            ex.placeSimbolInChordLine(chord.getSimbol());
            ex.placeAppendMessage(chord.toString(), MyExercise.MESSAGE_DELAY);
            ex.placeArpeggio(chord, arpeggiatura, MyExercise.HALF_BEAT, false);
            ex.skipCols(MyExercise.HALF_BEAT + MyExercise.ONE_BEAT);
            ex.placeChord(chord, 2 * MyExercise.ONE_BEAT - 1, false);
            ex.skipCols(1);
            ex.placeNote(root, 2 * MyExercise.ONE_BEAT - 1, false);
            ex.skipCols(1 + MyExercise.ONE_BEAT);
        }
        MyTempo.slower();
    }
}
