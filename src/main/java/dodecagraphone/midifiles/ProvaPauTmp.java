/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dodecagraphone.midifiles;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import jm.midi.MidiParser;
import jm.midi.SMF;
import jm.music.data.Score;

/**
 *
 * @author Pau
 */
public class ProvaPauTmp {

    /**
     * @param args the command line arguments
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
