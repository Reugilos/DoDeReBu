package dodecagraphone.model.sound;

import dodecagraphone.MyController;
import dodecagraphone.model.MyTempo;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.component.MyGridScore;
import dodecagraphone.model.component.MyMidiScore;
import dodecagraphone.model.mixer.MyMixer;
import dodecagraphone.ui.AppConfig;
import javax.sound.midi.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;

/**
 * Play and stop a sound whith a midi synth.
 */
public class SoundWithMidi {

    private static final boolean LOCAL_VERBOSE = false;
    public static final int DEFAULT_CHANNEL = 0;
    public static final int DEFAULT_VELOCITY = 127;
    public static final int DEFAULT_LEAD_INSTRUMENT = 9; // Glokenspiel
    public static final int DEFAULT_CHORD_INSTRUMENT = 0; // Piano
    public static final int GLOCKENSPIEL = 9;
    public static final int XYLOPHONE = 13;
    public static final int DEFAULT_TICKS_PER_QUARTER = 480;
    

    public static MyController contr;
    
    // Array per a dades dels canals MIDI
    private static final ChannelData[] channelDataArray = new ChannelData[16];
    private static Receiver receiver;
    private static int[] ntimesChannelIsUsed = new int[16]; 
    private static int currentVelocity;
    private static int leadInstrument;
    private static int chordInstrument;

    // Map per a instruments carregats del fitxer
    private static final Map<Integer, InstrumentData> instruments = new HashMap<>();
    
    static{
        
    }
    
    public static void resetChannels(){
        for (int i = 0; i < ntimesChannelIsUsed.length; i++) {
            if (i == 9) {
                continue; // Ometem canal de drums
            }
            if (i == 15) {
                continue; // Ometem canal d'acords
            }
            ntimesChannelIsUsed[i]=0;
//            if (Settings.IS_BU){
//                SoundWithMidi.runProgramChange(i, 9);
//            }
        }
    }
    
    public static int getNextAvailableChannel() {
        for (int i = 0; i < ntimesChannelIsUsed.length; i++) {
            if (i == 9) {
                continue; // Ometem canal de drums
            }
            if (i == 15) {
                continue; // Ometem canal d'acords
            }
            if (ntimesChannelIsUsed[i]==0) {
                ntimesChannelIsUsed[i]++;
                return i;
            }
        }

        // No queden canals disponibles: mostrar diàleg
        Integer[] opcions = new Integer[15]; // de 0 a 15 excepte 9
        int idx = 0;
        for (int i = 0; i < 16; i++) {
            if (i != 9 && i!=15) {
                opcions[idx++] = i;
            }
        }

        Integer canalSeleccionat = (Integer) JOptionPane.showInputDialog(
                null,
                "No queden canals MIDI lliures.\nSelecciona quin canal vols reutilitzar:",
                "Canals esgotats",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcions,
                0 // canal per defecte
        );

        if (canalSeleccionat == null) {
            // Si l'usuari tanca el diàleg o prem cancel·la
            return -1; // Pots gestionar aquest cas al teu codi si cal
        }

        ntimesChannelIsUsed[canalSeleccionat]++;
        return canalSeleccionat;
    }
    
    public static int getLeadInstrument(){
        return leadInstrument;
    }

    public static int getChordInstrument(){
        return chordInstrument;
    }
    
    
//    
//for (MyTrack t : tracks) {
//    for (int c : t.getCanals()) {
//        if (c >= 0 && c < 16) {
//            canalsUsats[c] = true;
//        }
//    }
//}
//
//int canalDisponible = 0;
//while (canalDisponible < 16 && canalsUsats[canalDisponible]) {
//    canalDisponible++;
//}
//
//if (canalDisponible >= 16) {
//    JOptionPane.showMessageDialog(null, "No hi ha canals disponibles.");
//    return;
//}
//

    static class InstrumentData {
        int instrumentNumber;
        String instrumentMnemonic;
        String instrumentDescription;
        
        public InstrumentData(int number,String mnemonic,String description){
            this.instrumentNumber = number;
            this.instrumentMnemonic = mnemonic;
            this.instrumentDescription = description;
        }
        
        @Override
        public String toString(){
            return String.format("%3d",this.instrumentNumber)+" "+this.instrumentMnemonic+" "+this.instrumentDescription;
        }
    }
    
    static class ChannelData {
        MidiChannel midiChannel;
        int instrument;
        boolean isActive;
        int id;
        
        public ChannelData(int id, MidiChannel midiChannel, int instrumentNumber, boolean isActive){
            this.midiChannel = midiChannel;
            this.instrument = instrumentNumber;
            this.isActive = isActive;
            this.id = id;
        }
    }

    public static void initMidi(MyController controller) {
        for (int i=0;i<16;i++){
            SoundWithMidi.ntimesChannelIsUsed[i]=0;
        }
        if (ToneRange.isMetallophone()){
            leadInstrument = Integer.parseInt(AppConfig.get().get("leadInstrument", "" + GLOCKENSPIEL));
        } else {
            leadInstrument = Integer.parseInt(AppConfig.get().get("leadInstrument", "" + DEFAULT_LEAD_INSTRUMENT));
        }        
        chordInstrument = Integer.parseInt(AppConfig.get().get("chordInstrument", "" + DEFAULT_CHORD_INSTRUMENT));

        contr = controller;
        try {
            Synthesizer midiSynth = MidiSystem.getSynthesizer();
            midiSynth.open();
            receiver = midiSynth.getReceiver();

            MidiChannel[] midiChannels = midiSynth.getChannels();

            for (int i = 0; i < midiChannels.length; i++) {
                channelDataArray[i] = new ChannelData(i,midiChannels[i], 0,false);
            }

            loadInstrumentData("defaults/GeneralMidiInstruments.csv");
            currentVelocity = DEFAULT_VELOCITY;

        } catch (MidiUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadInstrumentData(String fileName) throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + fileName);
        }
        try ( BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length >= 3) {
                    int instrumentNumber = Integer.parseInt(parts[0].trim());
                    String mnemonic = parts[1].trim();
                    String description = parts[2].trim();
                    instruments.put(instrumentNumber,new InstrumentData(instrumentNumber,mnemonic, description));
                }
            }
        }
    }
    
    public static String getInstrumentMnemonic(int instr){
        if (instr<0 || instr>=instruments.size()) throw new InvalidParameterException("Wrong instrument number: "+instr);
        return instruments.get(instr).instrumentMnemonic;
    }
    
    public static int getInstrumentNumber(String mnemonic){
        int i=0;
        for (InstrumentData data:instruments.values()){
            if (data.instrumentMnemonic.equalsIgnoreCase(mnemonic)) return i;
            i++;
        }
        return -1;
    }

    public static int getCurrentKeyboardVelocity() {
        return currentVelocity;
    }

    public static List<String> getInstrumentsStringList(){
        List<String> infoInstruments = new ArrayList<>();
        for (InstrumentData data:instruments.values()){
            infoInstruments.add(data.toString());
        }
        return infoInstruments;
    }
//    public static void setDefaultInstruments() {
//        for (int i = 0; i < channelDataArray.length; i++) {
//            ChannelData defaultData = getDefaultInstrumentForChannel(i);
//            channelDataArray[i] = defaultData;
//            if (defaultData.instrumentNumber != -1) {
//                defaultData.midiChannel.programChange(defaultData.instrumentNumber);
//            }
//        }
//    }
//
//    private static ChannelData getDefaultInstrumentForChannel(int channel) {
//        int instrumentNumber;
//        switch (channel) {
//            case 9: // Canal 10 per a percussió
//                return new ChannelData(channelDataArray[channel].midiChannel, 0, "Percussion", "Percussion Instruments");
//            case 10: case 11: case 12: case 13: case 14: // Canals 11 al 15 lliures
//                return new ChannelData(channelDataArray[channel].midiChannel, 0, "Free", "Free Channel");
//            case 0: instrumentNumber = 0; break; // Acoustic Grand Piano
//            case 1: instrumentNumber = 40; break; // Violin
//            case 2: instrumentNumber = 24; break; // Acoustic Guitar (steel)
//            case 3: instrumentNumber = 32; break; // Acoustic Bass
//            case 4: instrumentNumber = 41; break; // Viola
//            case 5: instrumentNumber = 42; break; // Cello
//            case 6: instrumentNumber = 1; break; // Bright Acoustic Piano
//            case 7: instrumentNumber = 19; break; // Reed Organ
//            case 8: instrumentNumber = 14; break; // Tubular Bells
//            default: instrumentNumber = 0; break; // Default to Piano1
//        }
//
//        String[] instrumentInfo = instrumentData.getOrDefault(instrumentNumber, new String[]{"Unknown", "Unknown"});
//        return new ChannelData(channelDataArray[channel].midiChannel, instrumentNumber, instrumentInfo[0], instrumentInfo[1]);
//    }
//
    public static int getInstOnChannel(int channel){
        return channelDataArray[channel].instrument;
    }
    
    public static int numUsedChannels(){
        return channelDataArray.length;
    }
    
    public static Set<Integer> activeChannels(){
        Set<Integer> achans = new HashSet();
        for (ChannelData channel:channelDataArray){
            if (channel.isActive){
                achans.add(channel.id);
            }
        }
        return achans;
    }

    public static void assignInstToChannel(int channel, int instrumentNumber) {
        if (channel < 0 || channel >= channelDataArray.length) {
            throw new IllegalArgumentException("Invalid channel: " + channel);
        }
        channelDataArray[channel].instrument = instrumentNumber;
        channelDataArray[channel].isActive = true;
        if (LOCAL_VERBOSE) {
            System.out.printf("Assigned instrument %d to channel %d%n",
                    instrumentNumber, channel);
        }
    }

    public static int getInstrumentInChannel(int channel){
        if (channel < 0 || channel >= channelDataArray.length) {
            throw new IllegalArgumentException("Invalid channel: " + channel);
        }
        return channelDataArray[channel].instrument;
    }
    
//    public static void printChannelData() {
//        for (int i = 0; i < channelDataArray.length; i++) {
//            ChannelData data = channelDataArray[i];
//            System.out.printf("Channel %d: Instrument %d (%s - %s)%n", i, data.instrumentNumber, data.instrumentMnemonic, data.instrumentDescription);
//        }
//    }
//
//    public static void play(int midi, int vol) {
//        ChannelData data = channelDataArray[DEFAULT_CHANNEL];
//        data.midiChannel.noteOn(midi, vol);
//    }
//
    public static void play(int midi, int channel, int vol) {
        if ((channel < 0 || channel >= channelDataArray.length) && !channelDataArray[channel].isActive) {
            throw new IllegalArgumentException("Invalid channel: " + channel);
        }
        channelDataArray[channel].midiChannel.noteOn(midi, vol);
    }

//    public static void stop(int midi) {
//        ChannelData data = channelDataArray[DEFAULT_CHANNEL];
//        data.midiChannel.noteOff(midi);
//    }
//
    public static void stop(int midi, int channel) {
        if ((channel < 0 || channel >= channelDataArray.length) && !channelDataArray[channel].isActive) {
            throw new IllegalArgumentException("Invalid channel: " + channel);
        }
        channelDataArray[channel].midiChannel.noteOff(midi);
    }


//        setInstrument(currentChannel, currentInstrument);
//    }
//
//    public static void setDefaultInstruments(){
//        
//    }
//    
//    public static MidiChannel[] getmChannels() {
//        return mChannels;
//    }
//
//    public static int getCurrentTrack() {
//        return currentTrack;
//    }
//
//    public static int getCurrentKeyboardVelocity() {
//        return currentVelocity;
//    }
//
//    public static int getCurrentChannelOfCurrentTrack() {
//        return currentChannel;
//    }
//
//    public static void setCurrentChannel(int currentChannel) {
//        SoundWithMidi.currentChannel = currentChannel;
//    }
//
//    public static void setInstrument(int midiInstrument) {
//        mChannels[currentChannel].programChange(midiInstrument);
//    }
//
//    public static void setInstrument(int channel, int midiInstrument) {
//        mChannels[channel].programChange(midiInstrument);
//    }
//
    public static MetaMessage createTextMessage(String text) {
        MetaMessage message = new MetaMessage();
        try {
            // Tipus 0x01 és per a Text Event; pots canviar-lo a altres com 0x06 (Marker) si ho prefereixes.
            message.setMessage(0x01, text.getBytes(), text.length());
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        return message;
    }

    public static boolean checkNRunTextMessage(MidiMessage message) {
        if (message instanceof MetaMessage) {
            MetaMessage mess = (MetaMessage) message;
            if (mess.getType() == 0x01) { // Comprova si és un Text Event
                byte[] data = mess.getData(); // Obté les dades del missatge
                if (data.length != 0) {
                    String text = new String(data);
                    String[] parts = text.split("::");
                    // throw new IllegalArgumentException("Unprocessed text message: "+parts[0]);
                }
                return true;
            }
        }
        return false;
    }

    public static void runProgramChange(int channel, int newInstr) {
        try {
            ShortMessage programChange = new ShortMessage();
            programChange.setMessage(ShortMessage.PROGRAM_CHANGE, channel, newInstr, 0);

            // Enviem el missatge al receptor
            receiver.send(programChange, -1);
        } catch (InvalidMidiDataException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
	public static String showMidiMessageBytes(javax.sound.midi.MidiMessage msg) {
		if (msg == null) return "MidiMessage: null";

		byte[] b = msg.getMessage();
		if (b == null) return "MidiMessage: (no bytes)";

		StringBuilder hex = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			hex.append(String.format("%02X", b[i] & 0xFF));
			if (i < b.length - 1) hex.append(" ");
		}

		String extra = "";
		if ((b[0] & 0xFF) == 0xFF && msg instanceof javax.sound.midi.MetaMessage) { // META
			javax.sound.midi.MetaMessage mm = (javax.sound.midi.MetaMessage) msg;
			int type = mm.getType();   // p.ex. 0x03, 0x01, 0x7F...
			byte[] data = mm.getData();

			if (type == 0x7F) { // Sequencer Specific
				String ascii = new String(data, java.nio.charset.StandardCharsets.US_ASCII)
						.replace("\u0000", "")
						.replace("\r", "");
				extra = "  | Meta 0x7F ASCII=\"" + ascii + "\"";
			}
		}

		return hex + extra;
	}

    public static void runMidiMessage(MidiMessage message, MyGridScore score) {
        if (message!=null) {
            if (checkNRunTextMessage(message)) {
                return;
            }
            if (message instanceof MetaMessage) {
                MetaMessage metaMessage = (MetaMessage) message;
                int type = metaMessage.getType();

                // Anàlisi del canvi de tempo
                if ((type == 0x51)) {
                    byte[] tempoData = metaMessage.getData();
                    int tempoMicroseconds = ((tempoData[0] & 0xFF) << 16) | ((tempoData[1] & 0xFF) << 8) | (tempoData[2] & 0xFF);
                    int tempoBPM = 60000000 / tempoMicroseconds;
                    if (LOCAL_VERBOSE) {
                        System.out.println("Tempo: " + tempoBPM + " BPM");
                    }
                    MyTempo.setTempo(tempoBPM);
                    return;
                }

                // Anàlisi de la time signature
                if ((type == 0x58)) {
                    byte[] timeSignatureData = metaMessage.getData();
                    int numerator = timeSignatureData[0];
                    int denominator = 1 << timeSignatureData[1];
                    String timeSignature = numerator + "/" + denominator;
                    score.setNumBeatsMeasure(numerator);
                    score.setBeatFigure(denominator);
                    if (LOCAL_VERBOSE) {
                        System.out.println("Time Signature: " + timeSignature);
                    }
                    return;
                }

                // Anàlisi de la pitch signature i mode (Major o Minor)
                if ((type == 0x59)) {
                    byte[] keySignatureData = metaMessage.getData();
                    int key = keySignatureData[0];
                    int scale = keySignatureData[1];

                    // Determinar si és major o menor
                    char scaleMode = (scale == 0) ? 'M' : 'm';

                    // Interpretar la tonalitat (nombre d'accidentals)
                    String keySignature = MyMidiScore.interpretKeySignature(key, ""+scaleMode);
                    int midiKey = ToneRange.getMidi(keySignature);
                    score.setMidiKey(midiKey);
                    score.setScaleMode(scaleMode);
                    if (LOCAL_VERBOSE) {
                        System.out.println("Key Signature: " + keySignature + " (" + scaleMode + ")");
                    }
                    return;
                }
                if (LOCAL_VERBOSE) {
                    System.out.println("MetaMessage no gestionat: Type " + type);
                }
                receiver.send(message, -1);
                return;
            }
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) message;
                int command = sm.getCommand();
                int channel = sm.getChannel();
                int pitch = sm.getData1(); // El pitch de la nota o el controlador
                int velocity = sm.getData2(); // La velocitat de la nota o el valor de control

                switch (command) {
                    case ShortMessage.NOTE_ON:
                    case ShortMessage.NOTE_OFF:
                        return;
                    case ShortMessage.CONTROL_CHANGE:
                        if (LOCAL_VERBOSE) System.out.println("CONTROL_CHANGE a col " + score.getCurrentCol() + ": Controller " + pitch + " valor " + velocity + " canal " + channel);
                        receiver.send(message, -1);
                        return;
                    case ShortMessage.PROGRAM_CHANGE:
                        if (LOCAL_VERBOSE) {
                            System.out.println("PROGRAM_CHANGE a col " + score.getCurrentCol() + ": Program " + pitch + " canal " + channel);
                        }
                        receiver.send(message, -1);
                        int ch = ((ShortMessage) message).getChannel();
                        int instrument = ((ShortMessage) message).getData1();
                        SoundWithMidi.assignInstToChannel(channel, instrument);
                        SoundWithMidi.runProgramChange(channel, instrument);
                        MyMixer mixer = contr.getMixer();
                        mixer.refreshMixer();
                        return;
                    default:
                        if (LOCAL_VERBOSE) System.out.println("ShortMessage MIDI no gestionat: " + command);
                        receiver.send(message, -1);
                        return;
                }
            }
        }
    }
}

//    /**
//     * Plays the current audio.
//     */
//    public static void play(int midi, int vol) {
//        if (LOCAL_VERBOSE) {
//            System.err.println("playmidi " + midi + " " + currentChannel);
//        }
//        mChannels[currentChannel].noteOn(midi, vol);//On channel 0, play note number 60 with velocity 100 
//    }
//
//    public static void play(int midi, int channel, int vol) {
//        if (channel == -1) {
//            play(midi, vol);
//        } else {
//            if (LOCAL_VERBOSE) {
//                System.err.println("playmidi " + midi + " " + channel);
//            }
//            mChannels[channel].noteOn(midi, vol);//On channel 0, play note number 60 with velocity 100 
//        }
//    }
//
//    /**
//     * Stops audio device.
//     */
//    public static void stop(int midi) {
//        if (LOCAL_VERBOSE) {
//            System.err.println("stopmidi " + midi + " " + currentChannel);
//        }
//        mChannels[currentChannel].noteOff(midi);//turn of the note
//    }
//
//    public static void stop(int midi, int channel) {
//        if (channel == -1) {
//            stop(midi);
//        } else {
//            if (LOCAL_VERBOSE) {
//                System.err.println("stopmidi " + midi + " " + channel);
//            }
//            mChannels[channel].noteOff(midi);//turn of the note
//        }
//    }
//}
