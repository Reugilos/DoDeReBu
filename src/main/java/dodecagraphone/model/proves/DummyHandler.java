/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.proves;

import java.util.HashMap;
import java.util.Map;

/**
 * [CA] Handler MIDI buit per a proves que processa i mostra per consola tots els
 * tipus de missatge MIDI: NOTE_ON, NOTE_OFF, Control Change, Program Change i SysEx.
 * Manté un mapa intern de notes actives per calcular la durada de cada nota.
 * Codi experimental / prototip.
 * <p>
 * [EN] Dummy MIDI handler for testing that processes and prints to the console all
 * MIDI message types: NOTE_ON, NOTE_OFF, Control Change, Program Change and SysEx.
 * Maintains an internal map of active notes to compute each note's duration.
 * Experimental / prototype code.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
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

    /**
     * [CA] Gestiona un missatge NOTE_ON: registra la nota com a activa per poder
     * calcular la seva durada quan arribi el NOTE_OFF corresponent.
     * <p>
     * [EN] Handles a NOTE_ON message: registers the note as active so its duration
     * can be computed when the matching NOTE_OFF arrives.
     *
     * @param pitch   [CA] alçada de la nota (0–127) / [EN] note pitch (0–127)
     * @param velocity [CA] velocitat (intensitat) de la nota (0–127) / [EN] note velocity (0–127)
     * @param tick    [CA] posició temporal en ticks MIDI / [EN] temporal position in MIDI ticks
     * @param track   [CA] índex de la pista MIDI / [EN] MIDI track index
     * @param channel [CA] canal MIDI (0–15) / [EN] MIDI channel (0–15)
     */
    public void handleNoteOn(int pitch, int velocity, long tick, int track, int channel) {
        // Guardem la nota activa (s'ha activat NOTE_ON però no ha acabat)
        activeNotes.put(pitch, new NoteInfo(track, pitch, velocity, tick, channel));
    }

    /**
     * [CA] Gestiona un missatge NOTE_OFF: recupera la informació de la nota activa,
     * calcula la seva durada i la mostra per consola.
     * <p>
     * [EN] Handles a NOTE_OFF message: retrieves the active note information,
     * computes its duration and prints it to the console.
     *
     * @param pitch   [CA] alçada de la nota que finalitza (0–127) / [EN] pitch of the ending note (0–127)
     * @param tick    [CA] posició temporal en ticks MIDI del NOTE_OFF / [EN] MIDI tick position of the NOTE_OFF
     * @param channel [CA] canal MIDI (0–15) / [EN] MIDI channel (0–15)
     */
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

    /**
     * [CA] Gestiona un missatge Control Change i el mostra per consola indicant
     * la funció del controlador (Volum, Pan, Sustain, etc.).
     * <p>
     * [EN] Handles a Control Change message and prints it to the console indicating
     * the controller function (Volume, Pan, Sustain, etc.).
     *
     * @param controller [CA] número del controlador MIDI (0–127) / [EN] MIDI controller number (0–127)
     * @param value      [CA] valor del controlador (0–127) / [EN] controller value (0–127)
     * @param tick       [CA] posició temporal en ticks MIDI / [EN] temporal position in MIDI ticks
     * @param track      [CA] índex de la pista MIDI / [EN] MIDI track index
     * @param channel    [CA] canal MIDI (0–15) / [EN] MIDI channel (0–15)
     */
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

    /**
     * [CA] Gestiona un missatge Program Change i el mostra per consola indicant
     * el nom del programa (instrument) si és reconegut.
     * <p>
     * [EN] Handles a Program Change message and prints it to the console indicating
     * the program (instrument) name if recognised.
     *
     * @param program [CA] número de programa General MIDI (0–127) / [EN] General MIDI program number (0–127)
     * @param tick    [CA] posició temporal en ticks MIDI / [EN] temporal position in MIDI ticks
     * @param track   [CA] índex de la pista MIDI / [EN] MIDI track index
     * @param channel [CA] canal MIDI (0–15) / [EN] MIDI channel (0–15)
     */
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

    /**
     * [CA] Gestiona un missatge SysEx (System Exclusive) i mostra per consola
     * el nombre de bytes rebuts i el tick en què s'ha produït.
     * <p>
     * [EN] Handles a SysEx (System Exclusive) message and prints to the console
     * the number of bytes received and the tick at which it occurred.
     *
     * @param data [CA] bytes de dades del missatge SysEx / [EN] SysEx message data bytes
     * @param tick [CA] posició temporal en ticks MIDI / [EN] temporal position in MIDI ticks
     */
    public void handleSysexMessage(byte[] data, long tick) {
        System.out.println("Sysex Message - " + data.length + " bytes, Tick: " + tick);
    }

    /**
     * [CA] Gestiona qualsevol altra comanda MIDI no categoritzada i la mostra per consola.
     * <p>
     * [EN] Handles any other uncategorised MIDI command and prints it to the console.
     *
     * @param command [CA] descripció textual de la comanda / [EN] textual description of the command
     * @param tick    [CA] posició temporal en ticks MIDI / [EN] temporal position in MIDI ticks
     * @param channel [CA] canal MIDI (0–15) / [EN] MIDI channel (0–15)
     */
    public void handleOtherCommand(String command, long tick, int channel) {
        System.out.println(command + " at Tick: " + tick + ", Channel: " + channel);
    }
}
