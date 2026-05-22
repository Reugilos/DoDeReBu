/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.midifiles;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import jm.midi.MidiParser;
import jm.midi.SMF;
import jm.music.data.Score;

/**
 * [CA] Classe de proves temporal per a la lectura de fitxers MIDI via la
 * biblioteca JMusic. Llegeix un SMF (Standard MIDI File) i en mostra la
 * representació textual per consola. Aquesta classe és temporal i no forma
 * part del flux principal de l'aplicació.
 * <p>
 * [EN] Temporary test class for reading MIDI files via the JMusic library.
 * Reads an SMF (Standard MIDI File) and prints a textual representation to
 * the console. This class is temporary and does not belong to the main
 * application flow.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class ProvaPauTmp {

    /**
     * [CA] Punt d'entrada principal. Llegeix el fitxer MIDI
     * {@code pachelbel_canon_d.mid} del directori de treball, el converteix
     * a un objecte {@link Score} de JMusic i en mostra el contingut.
     * <p>
     * [EN] Main entry point. Reads the MIDI file {@code pachelbel_canon_d.mid}
     * from the working directory, converts it to a JMusic {@link Score} object
     * and prints its content.
     *
     * @param args [CA] arguments de línia de comandes (no s'usen) / [EN] command-line arguments (unused)
     * @throws IOException [CA] si el fitxer no es pot llegir / [EN] if the file cannot be read
     */
    public static void main(String[] args) throws IOException {
        InputStream input = new FileInputStream("pachelbel_canon_d.mid");
        SMF smf = new SMF();
        smf.read(input);
        Score s = new Score();
        MidiParser.SMFToScore(s, smf);
        System.out.println(s);
        smf.print();
        input.close();
    }

}
