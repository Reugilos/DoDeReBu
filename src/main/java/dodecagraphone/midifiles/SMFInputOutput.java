package dodecagraphone.midifiles;

import dodecagraphone.model.component.MyPatternScore;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import jm.midi.MidiParser;
import jm.midi.SMF;

/**
 *
 * @author pau
 */
public class SMFInputOutput {
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
