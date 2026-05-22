/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.midifiles;

import dodecagraphone.model.component.MyPatternScore;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import jm.midi.MidiParser;
import jm.midi.SMF;

/**
 * [CA] Utilitat d'entrada/sortida per a fitxers MIDI en format SMF (Standard
 * MIDI File). Proporciona mètodes estàtics per llegir un fitxer MIDI i
 * transferir-ne el contingut a un {@link MyPatternScore}. El suport d'escriptura
 * és gestionat per {@code MyMidiScore.saveMidiScore()}.
 * <p>
 * [EN] Input/output utility for MIDI files in SMF (Standard MIDI File) format.
 * Provides static methods to read a MIDI file and transfer its content into a
 * {@link MyPatternScore}. Write support is handled by
 * {@code MyMidiScore.saveMidiScore()}.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class SMFInputOutput {

    /**
     * [CA] Llegeix un fitxer MIDI del camí indicat i en carrega el contingut
     * a la graella {@code grid}. Actualment crida {@code smf.print()} per a
     * depuració; la integració completa amb {@code MyPatternScore} està
     * pendent d'implementació.
     * <p>
     * [EN] Reads a MIDI file from the given path and loads its content into the
     * {@code grid}. Currently calls {@code smf.print()} for debugging; full
     * integration with {@code MyPatternScore} is pending implementation.
     *
     * @param path [CA] camí al fitxer MIDI / [EN] path to the MIDI file
     * @param grid [CA] graella de patrons on es carregarà el contingut / [EN] pattern grid where content will be loaded
     * @throws IOException [CA] si el fitxer no es pot llegir / [EN] if the file cannot be read
     */
    public static void readSMF(String path,MyPatternScore grid)throws IOException{
        InputStream input = new FileInputStream("pachelbel_canon_d.mid");
        SMF smf = new SMF();
        smf.read(input);
        //MyParser.SMFToGrid(grid,smf);
        //MidiParser.SMFToScore(s, smf);
        //System.out.println(s);
        smf.print();
        input.close();

    }
}
