/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.proves;

/**
 * [CA] Punt d'entrada per a la prova del parser MIDI. Demana a l'usuari el camí
 * d'un fitxer MIDI per consola, el parseja amb {@link MidiParser}, i el reprodueix
 * en temps real mitjançant un {@link TickScheduler} i el sintetitzador del sistema.
 * Codi experimental / prototip.
 * <p>
 * [EN] Entry point for the MIDI parser test. Asks the user for a MIDI file path
 * via the console, parses it with {@link MidiParser}, and plays it back in real
 * time using a {@link TickScheduler} and the system synthesiser.
 * Experimental / prototype code.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
import javax.sound.midi.*;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class MainParser {

    /**
     * [CA] Llegeix el camí del fitxer MIDI des de l'entrada estàndard, parseja el fitxer
     * i inicia la reproducció. Obre el sintetitzador del sistema i el tanca en finalitzar
     * o en cas d'error.
     * <p>
     * [EN] Reads the MIDI file path from standard input, parses the file and starts
     * playback. Opens the system synthesiser and closes it when done or on error.
     *
     * @param args [CA] arguments de la línia de comandes (no s'utilitzen) / [EN] command-line arguments (unused)
     */
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
