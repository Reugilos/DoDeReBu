package dodecagraphone.model.proves;

/**
 *
 * @author grogm
 */
//public class MainParser {
//    public static void main(String[] args) {
//        // Ruta al fitxer MIDI (ajusta-ho segons el teu fitxer)
//        // String midiFilePath = "pachelbel_canon_d.mid";
//        String midiFilePath = "AmorQueTensMaVida.mid";
//
//        // Crear el gestor DummyHandler
//        DummyHandler handler = new DummyHandler();
//
//        // Crear el parser MIDI
//        MidiFileParser parser = new MidiFileParser(handler);
//
//        // Parseja el fitxer MIDI
//        parser.parseAndPlayMidiFile(midiFilePath);
//    }
//}

import javax.sound.midi.*;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class MainParser {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Demana el nom del fitxer a l'usuari
        System.out.print("Introduïu el camí del fitxer MIDI: ");
        String filePath = scanner.nextLine();

        MidiParser parser = new MidiParser();
        Synthesizer synthesizer = null;

        try {
            // Obre el sintetitzador
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();

            // Parseja el fitxer MIDI
            List<MidiEventWithTrack> events = parser.parseMidiFile(filePath);

            // Obtenim la resolució i el tempo inicial del fitxer
            int resolution = parser.getResolution();
            long initialTempo = parser.getTempo();

            // Crea el TickScheduler per gestionar l'execució dels esdeveniments MIDI
            TickScheduler scheduler = new TickScheduler(events, synthesizer, resolution, initialTempo);
            scheduler.start();

        } catch (InvalidMidiDataException | IOException e) {
            System.out.println("Error llegint el fitxer MIDI: " + e.getMessage());
        } catch (MidiUnavailableException e) {
            System.out.println("Error iniciant el sintetitzador MIDI: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Execució interrompuda: " + e.getMessage());
        } finally {
            if (synthesizer != null && synthesizer.isOpen()) {
                synthesizer.close();
            }
        }
    }
}
