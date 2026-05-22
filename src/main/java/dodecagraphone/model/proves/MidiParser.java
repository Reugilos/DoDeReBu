/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.proves;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * [CA] Parser MIDI experimental que llegeix un fitxer MIDI, n'extreu tots els
 * esdeveniments de totes les pistes i detecta els canvis de tempo. Retorna la
 * llista d'esdeveniments com a {@link MidiEventWithTrack} per al posterior
 * processament pel {@link TickScheduler}. Codi experimental / prototip.
 * <p>
 * [EN] Experimental MIDI parser that reads a MIDI file, extracts all events from
 * all tracks and detects tempo changes. Returns the event list as
 * {@link MidiEventWithTrack} for subsequent processing by the {@link TickScheduler}.
 * Experimental / prototype code.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MidiParser {

    private int resolution;  // Resolució en PPQ (ticks per quarter note)
    private long tempo = 500000;  // Tempo per defecte (500000 microsegons per quarter note, equivalent a 120 bpm)

    /**
     * [CA] Parseja el fitxer MIDI indicat: mostra per consola la informació de
     * la capçalera, detecta canvis de tempo (meta-event 0x51) i recull tots els
     * esdeveniments de totes les pistes en una llista ordenable per tick.
     * <p>
     * [EN] Parses the given MIDI file: prints header information to the console,
     * detects tempo changes (meta-event 0x51) and collects all events from all
     * tracks into a list that can be sorted by tick.
     *
     * @param filePath [CA] camí al fitxer MIDI / [EN] path to the MIDI file
     * @return [CA] llista de tots els esdeveniments MIDI de totes les pistes /
     *         [EN] list of all MIDI events from all tracks
     * @throws InvalidMidiDataException [CA] si el fitxer no és un fitxer MIDI vàlid /
     *                                  [EN] if the file is not a valid MIDI file
     * @throws IOException              [CA] si hi ha un error de lectura del fitxer /
     *                                  [EN] if there is a file read error
     */
    public List<MidiEventWithTrack> parseMidiFile(String filePath) throws InvalidMidiDataException, IOException {
        File midiFile = new File(filePath);
        Sequence sequence = MidiSystem.getSequence(midiFile);

        // Mostrem la informació de la capçalera
        mostrarInfoCapçalera(sequence);

        this.resolution = sequence.getResolution();  // Obtenim la resolució del fitxer MIDI

        List<MidiEventWithTrack> events = new ArrayList<>();

        int trackNumber = 0;  // Ara comença en 0
        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();

                // Verifiquem si el missatge és un meta-esdeveniment de canvi de tempo
                if (message instanceof MetaMessage) {
                    MetaMessage metaMessage = (MetaMessage) message;
                    if (metaMessage.getType() == 0x51) {  // Meta-event de tipus 0x51: canvi de tempo
                        byte[] data = metaMessage.getData();
                        if (data.length == 3) {
                            // El tempo es defineix en tres bytes
                            int tempoValue = ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
                            this.tempo = tempoValue;
                            System.out.println("Canvi de tempo detectat: " + this.tempo + " microsegons per quarter note");
                        }
                    }
                }

                events.add(new MidiEventWithTrack(event, trackNumber));
            }
            trackNumber++;  // Incrementem després d'haver processat el track
        }

        return events;
    }

    private void mostrarInfoCapçalera(Sequence sequence) {
        System.out.println("----- Informació de la capçalera del fitxer MIDI -----");
        System.out.println("Nombre de pistes: " + sequence.getTracks().length);
        System.out.println("Resolució (PPQ - Pulsacions per Quarter Note): " + sequence.getResolution());

        // Mostrem informació sobre el tipus de divisió del temps
        if (sequence.getDivisionType() == Sequence.PPQ) {
            System.out.println("Tipus de divisió del temps: PPQ (Pulsacions per Quarter Note)");
        } else {
            System.out.println("Tipus de divisió del temps: SMPTE");
        }

        System.out.println("-----------------------------------------------------");
    }

    /**
     * [CA] Retorna la resolució (PPQ) del darrer fitxer parsejat.
     * <p>
     * [EN] Returns the resolution (PPQ) of the last parsed file.
     *
     * @return [CA] ticks per quarter note (PPQ) / [EN] ticks per quarter note (PPQ)
     */
    public int getResolution() {
        return resolution;
    }

    /**
     * [CA] Retorna el darrer tempo detectat al fitxer MIDI en microsegons per
     * quarter note. Si no hi ha cap canvi de tempo, retorna 500000 (120 BPM).
     * <p>
     * [EN] Returns the last tempo detected in the MIDI file in microseconds per
     * quarter note. If no tempo change is found, returns 500000 (120 BPM).
     *
     * @return [CA] microsegons per quarter note / [EN] microseconds per quarter note
     */
    public long getTempo() {
        return tempo;
    }
}
