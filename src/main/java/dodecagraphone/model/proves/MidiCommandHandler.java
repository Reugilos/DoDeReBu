/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.proves;

import javax.sound.midi.*;
import java.util.HashMap;
import java.util.Map;

/**
 * [CA] Gestor estàtic de comandes MIDI per a proves. Processa missatges {@code ShortMessage}
 * i {@code MetaMessage}, els mostra per consola i opcionalment els envia al sintetitzador
 * del sistema per a la reproducció en temps real. Manté un mapa intern de notes actives
 * per calcular les duracions. Codi experimental / prototip.
 * <p>
 * [EN] Static MIDI command handler for testing. Processes {@code ShortMessage} and
 * {@code MetaMessage} messages, prints them to the console and optionally forwards them
 * to the system synthesiser for real-time playback. Maintains an internal map of active
 * notes to compute durations. Experimental / prototype code.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MidiCommandHandler {

    private static boolean enablePlayback = true;
    private static Map<Integer, NoteInfo> activeNotes = new HashMap<>();

    /**
     * [CA] Activa o desactiva la reproducció en temps real dels missatges MIDI
     * al sintetitzador del sistema.
     * <p>
     * [EN] Enables or disables real-time playback of MIDI messages through the
     * system synthesiser.
     *
     * @param enable [CA] {@code true} per activar la reproducció, {@code false} per desactivar-la /
     *               [EN] {@code true} to enable playback, {@code false} to disable it
     */
    public static void setEnablePlayback(boolean enable) {
        enablePlayback = enable;
    }

    /**
     * [CA] Processa un missatge MIDI (ShortMessage o MetaMessage): el mostra per consola
     * amb el tick, la pista i els detalls del missatge, i si la reproducció està activada
     * l'envia al sintetitzador. Gestiona NOTE_ON, NOTE_OFF, CONTROL_CHANGE, PROGRAM_CHANGE
     * i els meta-missatges de nom de pista, tempo, compàs i tonalitat.
     * <p>
     * [EN] Processes a MIDI message (ShortMessage or MetaMessage): prints it to the console
     * with tick, track and message details, and if playback is enabled forwards it to the
     * synthesiser. Handles NOTE_ON, NOTE_OFF, CONTROL_CHANGE, PROGRAM_CHANGE and meta-messages
     * for track name, tempo, time signature and key signature.
     *
     * @param message     [CA] missatge MIDI a processar / [EN] MIDI message to process
     * @param synthesizer [CA] sintetitzador destinatari de la reproducció / [EN] synthesiser for playback output
     * @param track       [CA] índex de la pista d'origen / [EN] source track index
     * @param tick        [CA] posició temporal en ticks MIDI / [EN] temporal position in MIDI ticks
     */
    public static void handleMidiMessage(MidiMessage message, Synthesizer synthesizer, int track, long tick) {
        try {
            Receiver receiver = synthesizer.getReceiver();

            if (message instanceof ShortMessage) {
                ShortMessage shortMessage = (ShortMessage) message;
                int command = shortMessage.getCommand();
                int channel = shortMessage.getChannel();
                int data1 = shortMessage.getData1();
                int data2 = shortMessage.getData2();

                switch (command) {
                    case ShortMessage.NOTE_ON:
                        if (data2 > 0) {
                            System.out.println("Tick: " + tick + " | Track: " + track + " | NOTE ON: Channel: " + channel + ", Pitch: " + data1 + ", Velocity: " + data2);
                            activeNotes.put(data1, new NoteInfo(channel, data1, tick, data2));
                            if (enablePlayback) {
                                ShortMessage noteOnMessage = new ShortMessage(ShortMessage.NOTE_ON, channel, data1, data2);
                                receiver.send(noteOnMessage, -1);
                            }
                        } else {
                            System.out.println("Tick: " + tick + " | Track: " + track + " | NOTE OFF (as NOTE ON with velocity 0): Channel: " + channel + ", Pitch: " + data1);
                            if (activeNotes.containsKey(data1)) {
                                NoteInfo noteInfo = activeNotes.remove(data1);
                                noteInfo.setEndTick(tick);
                                showNoteInfo(noteInfo);
                            }
                            if (enablePlayback) {
                                ShortMessage noteOffMessage = new ShortMessage(ShortMessage.NOTE_OFF, channel, data1, 0);
                                receiver.send(noteOffMessage, -1);
                            }
                        }
                        break;

                    case ShortMessage.NOTE_OFF:
                        System.out.println("Tick: " + tick + " | Track: " + track + " | NOTE OFF: Channel: " + channel + ", Pitch: " + data1 + ", Velocity: " + data2);
                        if (activeNotes.containsKey(data1)) {
                            NoteInfo noteInfo = activeNotes.remove(data1);
                            noteInfo.setEndTick(tick);
                            showNoteInfo(noteInfo);
                        }
                        if (enablePlayback) {
                            ShortMessage noteOffMessage = new ShortMessage(ShortMessage.NOTE_OFF, channel, data1, data2);
                            receiver.send(noteOffMessage, -1);
                        }
                        break;

                    case ShortMessage.CONTROL_CHANGE:
                        System.out.println("Tick: " + tick + " | Track: " + track + " | CONTROL CHANGE: Channel: " + channel + ", Controller: " + data1 + ", Value: " + data2);
                        printControlChangeExplanation(data1);
                        if (enablePlayback) {
                            ShortMessage controlChangeMessage = new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, data1, data2);
                            receiver.send(controlChangeMessage, -1);
                        }
                        break;

                    case ShortMessage.PROGRAM_CHANGE:
                        System.out.println("Tick: " + tick + " | Track: " + track + " | PROGRAM CHANGE: Channel: " + channel + ", Program: " + data1);
                        if (enablePlayback) {
                            ShortMessage programChangeMessage = new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel, data1);
                            receiver.send(programChangeMessage, -1);
                        }
                        break;

                    default:
                        System.out.println("Tick: " + tick + " | Track: " + track + " | Unhandled MIDI command: " + command);
                        break;
                }

            } else if (message instanceof MetaMessage) {
                MetaMessage metaMessage = (MetaMessage) message;
                int type = metaMessage.getType();
                switch (type) {
                    case 0x03:
                        String trackName = new String(metaMessage.getData());
                        System.out.println("Tick: " + tick + " | Track: " + track + " | Track Name: " + trackName);
                        break;

                    case 0x51:
                        byte[] tempoData = metaMessage.getData();
                        int tempo = ((tempoData[0] & 0xFF) << 16) | ((tempoData[1] & 0xFF) << 8) | (tempoData[2] & 0xFF);
                        double bpm = 60000000.0 / tempo;  // Calcula el tempo en BPM
                        System.out.println("Tick: " + tick + " | Track: " + track + " | Tempo Change: " + tempo + " microseconds per quarter note (" + bpm + " BPM)");
                        break;

                    case 0x58:
                        byte[] timeSignatureData = metaMessage.getData();
                        int numerator = timeSignatureData[0];
                        int denominator = 1 << timeSignatureData[1];
                        System.out.println("Tick: " + tick + " | Track: " + track + " | Time Signature: " + numerator + "/" + denominator);
                        break;

                    case 0x59:
                        byte[] keySignatureData = metaMessage.getData();
                        int key = keySignatureData[0];
                        int scale = keySignatureData[1];
                        System.out.println("Tick: " + tick + " | Track: " + track + " | Key Signature: key=" + key + ", scale=" + (scale == 0 ? "Major" : "Minor"));
                        break;

                    default:
                        System.out.println("Tick: " + tick + " | Track: " + track + " | MetaMessage: Type: " + type);
                        break;
                }
            }

        } catch (MidiUnavailableException e) {
            System.err.println("Error enviant missatge MIDI al sintetitzador: " + e.getMessage());
        } catch (InvalidMidiDataException e) {
            System.err.println("Error: Invalid MIDI data detected: " + e.getMessage());
        }
    }

    private static void showNoteInfo(NoteInfo noteInfo) {
        System.out.println("Nota: Pitch: " + noteInfo.getPitch() + ", Canal: " + noteInfo.getChannel() +
                           ", Tick Inici: " + noteInfo.getStartTick() + ", Durada: " + noteInfo.getDuration() +
                           " ticks, Velocity: " + noteInfo.getVelocity());
    }

    private static void printControlChangeExplanation(int controller) {
        switch (controller) {
            case 7:
                System.out.println(" - Controller 7: Main Volume (controls the overall volume of the channel).");
                break;
            case 10:
                System.out.println(" - Controller 10: Pan (controls left-right positioning of the sound).");
                break;
            case 91:
                System.out.println(" - Controller 91: Reverb Send Level (controls how much reverb is applied to the channel).");
                break;
            case 93:
                System.out.println(" - Controller 93: Chorus Send Level (controls how much chorus is applied to the channel).");
                break;
            default:
                System.out.println(" - No specific explanation for this controller.");
                break;
        }
    }

    // Class to store information about a note
    private static class NoteInfo {
        private int channel;
        private int pitch;
        private long startTick;
        private long endTick;
        private int velocity;

        public NoteInfo(int channel, int pitch, long startTick, int velocity) {
            this.channel = channel;
            this.pitch = pitch;
            this.startTick = startTick;
            this.velocity = velocity;
        }

        public void setEndTick(long endTick) {
            this.endTick = endTick;
        }

        public int getChannel() {
            return channel;
        }

        public int getPitch() {
            return pitch;
        }

        public long getStartTick() {
            return startTick;
        }

        public long getDuration() {
            return endTick - startTick;
        }

        public int getVelocity() {
            return velocity;
        }
    }
}
