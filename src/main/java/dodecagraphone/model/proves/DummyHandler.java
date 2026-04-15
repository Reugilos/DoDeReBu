package dodecagraphone.model.proves;
import java.util.HashMap;
import java.util.Map;

public class DummyHandler {
    // Mapa per mantenir la informació de les notes actives (notes que han rebut un NOTE_ON però no un NOTE_OFF)
    private Map<Integer, NoteInfo> activeNotes = new HashMap<>();

    // Classe interna per mantenir la informació d'una nota
    private class NoteInfo {
        int track;
        int pitch;
        int velocity;
        long startTick;
        int channel;

        public NoteInfo(int track, int pitch, int velocity, long startTick, int channel) {
            this.track = track;
            this.pitch = pitch;
            this.velocity = velocity;
            this.startTick = startTick;
            this.channel = channel;
        }
    }

    // Mètode per gestionar NOTE_ON
    public void handleNoteOn(int pitch, int velocity, long tick, int track, int channel) {
        // Guardem la nota activa (s'ha activat NOTE_ON però no ha acabat)
        activeNotes.put(pitch, new NoteInfo(track, pitch, velocity, tick, channel));
    }

    // Mètode per gestionar NOTE_OFF
    public void handleNoteOff(int pitch, long tick, int channel) {
        // Recuperem la informació de la nota quan es va activar el NOTE_ON
        NoteInfo noteInfo = activeNotes.get(pitch);

        if (noteInfo != null) {
            long duration = tick - noteInfo.startTick; // Calculem la durada de la nota
            showNote(noteInfo.track, noteInfo.pitch, noteInfo.velocity, noteInfo.startTick, duration, noteInfo.channel);
            activeNotes.remove(pitch); // Eliminem la nota de la llista de notes actives
        }
    }

    // Mètode per mostrar la informació de la nota
    private void showNote(int track, int pitch, int velocity, long startTick, long duration, int channel) {
        System.out.println("Track: " + track + ", Note: " + pitch + ", Velocity: " + velocity +
                ", Start Tick: " + startTick + ", Duration: " + duration + " ticks, Channel: " + channel);
    }

    // Mètode per gestionar Control Change
    public void handleControlChange(int controller, int value, long tick, int track, int channel) {
        String controllerFunction = "";

        // Explicació de què fa el Control Change segons el controlador
        switch (controller) {
            case 7:
                controllerFunction = "Main Volume";
                break;
            case 10:
                controllerFunction = "Pan (Left/Right Balance)";
                break;
            case 64:
                controllerFunction = "Sustain Pedal";
                break;
            default:
                controllerFunction = "Unknown Controller";
                break;
        }

        // Mostrem la informació del Control Change
        System.out.println("Track: " + track + ", Control Change - Controller: " + controller +
                " (" + controllerFunction + "), Value: " + value + ", Tick: " + tick + ", Channel: " + channel);
    }

    // Mètode per gestionar Program Change
    public void handleProgramChange(int program, long tick, int track, int channel) {
        String programName = "";

        // Explicació de què fa el Program Change segons el programa
        switch (program) {
            case 0:
                programName = "Acoustic Grand Piano";
                break;
            case 5:
                programName = "Electric Piano";
                break;
            case 32:
                programName = "Acoustic Guitar";
                break;
            default:
                programName = "Unknown Program";
                break;
        }

        // Mostrem la informació del Program Change
        System.out.println("Track: " + track + ", Program Change - Program: " + program +
                " (" + programName + "), Tick: " + tick + ", Channel: " + channel);
    }

    // Mètode per gestionar missatges SysEx
    public void handleSysexMessage(byte[] data, long tick) {
        System.out.println("Sysex Message - " + data.length + " bytes, Tick: " + tick);
    }

    // Mètode per gestionar altres missatges MIDI
    public void handleOtherCommand(String command, long tick, int channel) {
        System.out.println(command + " at Tick: " + tick + ", Channel: " + channel);
    }
}
