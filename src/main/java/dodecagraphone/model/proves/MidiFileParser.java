/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.proves;

import javax.sound.midi.*;
import java.io.File;

/**
 * [CA] Parser de fitxers MIDI basat en el {@link Sequencer} de Java Sound que
 * reprodueix el fitxer i encamina cada missatge MIDI cap a un {@link DummyHandler}
 * per al seu processament i visualització per consola. Codi experimental / prototip.
 * <p>
 * [EN] MIDI file parser based on the Java Sound {@link Sequencer} that plays back
 * the file and routes each MIDI message to a {@link DummyHandler} for processing
 * and console output. Experimental / prototype code.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MidiFileParser {

    private DummyHandler dummyHandler;
    private Sequencer sequencer;

    /**
     * [CA] Construeix el parser associant-lo al handler que rebrà els missatges MIDI.
     * <p>
     * [EN] Constructs the parser associating it with the handler that will receive MIDI messages.
     *
     * @param handler [CA] handler destinatari dels missatges MIDI / [EN] handler that will receive MIDI messages
     */
    public MidiFileParser(DummyHandler handler) {
        this.dummyHandler = handler;
    }

    /**
     * [CA] Parseja i reprodueix el fitxer MIDI indicat: configura el {@link Sequencer},
     * afegeix un {@link Receiver} intern que reenvia cada missatge al {@link DummyHandler},
     * inicia la reproducció i espera que finalitzi.
     * <p>
     * [EN] Parses and plays back the given MIDI file: sets up the {@link Sequencer},
     * adds an internal {@link Receiver} that forwards each message to the {@link DummyHandler},
     * starts playback and waits for it to finish.
     *
     * @param filePath [CA] camí al fitxer MIDI a processar / [EN] path to the MIDI file to process
     */
    public void parseAndPlayMidiFile(String filePath) {
        try {
            // Obtenir el fitxer MIDI
            File midiFile = new File(filePath);

            // Configurar el reproductor (Sequencer)
            sequencer = MidiSystem.getSequencer();
            sequencer.setSequence(MidiSystem.getSequence(midiFile));

            // Afegir un Transmitter per enviar missatges al DummyHandler
            Transmitter transmitter = sequencer.getTransmitter();
            transmitter.setReceiver(new MidiReceiver(dummyHandler, sequencer));

            // Obrir i començar a reproduir
            sequencer.open();
            sequencer.start();

            // Esperar fins que la reproducció acabi
            while (sequencer.isRunning()) {
                Thread.sleep(100); // Dormir durant uns mil·lisegons per evitar ocupació de la CPU
            }

            // Tanquem el reproductor quan acabi
            sequencer.stop();
            sequencer.close();

//            // Recorrer tots els missatges sense iniciar la reproducció
//            sequencer.open();
//            while (sequencer.getTickLength() > sequencer.getTickPosition()) {
//                // Fem que el sequencer avanci manualment sense reproduir
//                sequencer.setTickPosition(sequencer.getTickPosition() + 1);  // Incrementem manualment el tick
//                Thread.sleep(1);  // Simulem el temps
//            }
//            sequencer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Classe interna per manejar la recepció dels missatges MIDI
// A la classe MidiFileParser
    private class MidiReceiver implements Receiver {

        private DummyHandler dummyHandler;
        private Sequencer sequencer;

        public MidiReceiver(DummyHandler handler, Sequencer sequencer) {
            this.dummyHandler = handler;
            this.sequencer = sequencer;
        }

        @Override
        public void send(MidiMessage message, long timeStamp) {
            // Obtenim el tick actual des del sequencer
            long currentTick = sequencer.getTickPosition();

            // Processar el missatge MIDI i enviar-lo al DummyHandler amb els ticks
            processMidiMessage(message, currentTick);
        }

        @Override
        public void close() {
        }

        private void processMidiMessage(MidiMessage message, long tick) {
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;
                int command = sm.getCommand();
                int channel = sm.getChannel();
                int data1 = sm.getData1();
                int data2 = sm.getData2();

                switch (command) {
                    case ShortMessage.NOTE_ON:
                        if (data2 > 0) {
                            dummyHandler.handleNoteOn(data1, data2, tick, channel, channel);
                        } else {
                            dummyHandler.handleNoteOff(data1, tick, channel);
                        }
                        break;
                    case ShortMessage.NOTE_OFF:
                        dummyHandler.handleNoteOff(data1, tick, channel);
                        break;
                    case ShortMessage.CONTROL_CHANGE:
                        dummyHandler.handleControlChange(data1, data2, tick, channel, channel);
                        break;
                    case ShortMessage.PROGRAM_CHANGE:
                        dummyHandler.handleProgramChange(data1, tick, channel, channel);
                        break;
                    default:
                        dummyHandler.handleOtherCommand("Command: " + command + " on Channel: " + channel, tick, channel);
                        break;
                }
            } else if (message instanceof SysexMessage) {
                SysexMessage sysex = (SysexMessage) message;
                byte[] data = sysex.getData();
                dummyHandler.handleSysexMessage(data, tick);
            } else {
                dummyHandler.handleOtherCommand("Non-ShortMessage", tick, -1);
            }
        }
    }
}

//________________________________________________________

//import javax.sound.midi.*;
//import java.io.File;
//
//public class MidiFileParser {
//    private DummyHandler dummyHandler;
//    private Sequencer sequencer;
//
//    public MidiFileParser(DummyHandler handler) {
//        this.dummyHandler = handler;
//    }
//
//    public void parseMidiFile(String filePath) {
//        try {
//            // Obtenir el fitxer MIDI
//            File midiFile = new File(filePath);
//
//            // Configurar el reproductor (Sequencer) per obtenir els esdeveniments sense reproduir-los
//            sequencer = MidiSystem.getSequencer();
//            sequencer.setSequence(MidiSystem.getSequence(midiFile));
//
//            // Afegir un Transmitter per enviar missatges al DummyHandler
//            Transmitter transmitter = sequencer.getTransmitter();
//            transmitter.setReceiver(new MidiReceiver(dummyHandler, sequencer));
//
//            // Obrir el sequencer (sense reproduir) per accedir als missatges
//            sequencer.open();
//
//            // Recorrer tots els missatges sense iniciar la reproducció
//            while (sequencer.getTickLength() > sequencer.getTickPosition()) {
//                // Fem que el sequencer avanci manualment sense reproduir
//                sequencer.setTickPosition(sequencer.getTickPosition() + 1);  // Incrementem manualment el tick
//                Thread.sleep(1);  // Simulem el temps
//            }
//
//            // Tanquem el sequencer quan acabem
//            sequencer.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Classe interna per manejar la recepció dels missatges MIDI
//    private class MidiReceiver implements Receiver {
//        private DummyHandler dummyHandler;
//        private Sequencer sequencer;
//
//        public MidiReceiver(DummyHandler handler, Sequencer sequencer) {
//            this.dummyHandler = handler;
//            this.sequencer = sequencer;
//        }
//
//        @Override
//        public void send(MidiMessage message, long timeStamp) {
//            // Obtenim el tick actual des del sequencer
//            long currentTick = sequencer.getTickPosition();
//
//            // Processar el missatge MIDI i enviar-lo al DummyHandler amb els ticks
//            processMidiMessage(message, currentTick);
//        }
//
//        @Override
//        public void close() {
//        }
//
//        private void processMidiMessage(MidiMessage message, long tick) {
//            if (message instanceof ShortMessage) {
//                ShortMessage sm = (ShortMessage) message;
//                int command = sm.getCommand();
//                int channel = sm.getChannel();
//                int data1 = sm.getData1();
//                int data2 = sm.getData2();
//
//                switch (command) {
//                    case ShortMessage.NOTE_ON:
//                        if (data2 > 0) {
//                            dummyHandler.handleNoteOn(data1, data2, tick, channel, channel);
//                        } else {
//                            dummyHandler.handleNoteOff(data1, tick, channel);
//                        }
//                        break;
//                    case ShortMessage.NOTE_OFF:
//                        dummyHandler.handleNoteOff(data1, tick, channel);
//                        break;
//                    case ShortMessage.CONTROL_CHANGE:
//                        dummyHandler.handleControlChange(data1, data2, tick, channel, channel);
//                        break;
//                    case ShortMessage.PROGRAM_CHANGE:
//                        dummyHandler.handleProgramChange(data1, tick, channel, channel);
//                        break;
//                    default:
//                        dummyHandler.handleOtherCommand("Command: " + command + " on Channel: " + channel, tick, channel);
//                        break;
//                }
//            } else if (message instanceof SysexMessage) {
//                SysexMessage sysex = (SysexMessage) message;
//                byte[] data = sysex.getData();
//                dummyHandler.handleSysexMessage(data, tick);
//            } else {
//                dummyHandler.handleOtherCommand("Non-ShortMessage", tick, -1);
//            }
//        }
//    }
//}
