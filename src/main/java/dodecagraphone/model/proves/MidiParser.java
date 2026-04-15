
package dodecagraphone.model.proves;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MidiParser {

    private int resolution;  // Resolució en PPQ (ticks per quarter note)
    private long tempo = 500000;  // Tempo per defecte (500000 microsegons per quarter note, equivalent a 120 bpm)

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

    public int getResolution() {
        return resolution;
    }

    public long getTempo() {
        return tempo;
    }
}
