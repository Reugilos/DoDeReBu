package dodecagraphone.model.component;

import dodecagraphone.MyController;
import dodecagraphone.model.InstrumentRange;
import dodecagraphone.model.MyKeyCircles;
import dodecagraphone.model.MyTempo;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.chord.Chord;
import dodecagraphone.model.mixer.MyMixer;
import dodecagraphone.model.mixer.MyTrack;
import dodecagraphone.model.sound.SoundWithMidi;
import dodecagraphone.ui.Settings;
import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 * Un MyMidiScore és un MyGridPattern extret d'un arxiu midi.
 *
 * @author paugpt
 */
public class MyMidiScore extends MyExercise {

    private static final boolean LOCAL_VERBOSE = false;
    protected long ticksPerQuarter;
    protected long firstTick = -1;
    // Mantindrem un mapa per seguir les notes actives (NOTE_ON) fins que es trobi el corresponent NOTE_OFF
    private Map<Long, NoteInfo> activeNotes = new HashMap<>();
    private int[] loadChannelDisplayOffset = new int[16];
    //private MyMixer mixer;
//    private Map<Integer, Boolean> activeChannels = new HashMap<>();

    public MyMidiScore(MyController contr) {
        super(contr);
//        this.activeChannels = new HashMap<>();
    }

//    public void resetMidiScore() {
//        this.activeChannels = new HashMap<>();
//    }

    /**
     * Retorna el tick més aviat on apareix un NOTE_ON amb velocitat > 0 en
     * qualsevol track de la Sequence. Si no es troba cap NOTE_ON, retorna -1.
     */
    public long firstTick(Sequence sequence) {
        long firstTick = Long.MAX_VALUE;

        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();

                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) {
                        long tick = event.getTick();
                        if (tick < firstTick) {
                            firstTick = tick;
                        }
                    }
                }
            }
        }

        return (firstTick == Long.MAX_VALUE) ? -1 : firstTick;
    }

    public long adjustResolution(long oldValue) {
        int quartersPerMeasure = this.numBeatsMeasure * 4 / this.beatFigure;
        double exactTicksPerQuarter = Math.round(oldValue / quartersPerMeasure) * quartersPerMeasure;
        return (int) Math.round(exactTicksPerQuarter);
    }

    private int readTrackData(MyTrack track, Track midiTr) {
        List<MetaMessage> metaMessages = new ArrayList<>();
        for (int j = 0; j < midiTr.size(); j++) {
            MidiMessage msg = midiTr.get(j).getMessage();
            if (msg instanceof MetaMessage mm && mm.getType() == 0x7F) {
                metaMessages.add((MetaMessage) msg);
            }
        }
        int trackId = -1;
        for (MetaMessage meta : metaMessages) {
            try {
                String text = new String(meta.getData(), "UTF-8").trim();

                if (text.startsWith("id=")) {
                    trackId = Integer.parseInt(text.substring(3));
                } else if (text.startsWith("currentChannel=")) {
                    track.setCurrentChannel(Integer.parseInt(text.substring(15))); // OJO, consistència Channels al multiple tracks
//                    track.setCurrentChannel(SoundWithMidi.getNextAvailableChannel());
                } else if (text.startsWith("nNotes=")) {
                    track.setnNotes(Long.parseLong(text.substring(7)));
                } else if (text.startsWith("keepNoteVelocity=")) {
                    track.setKeepNoteVelocity(Boolean.parseBoolean(text.substring(17)));
                } else if (text.startsWith("selected=")) {
                    track.setSelected(Boolean.parseBoolean(text.substring(9)));
                } else if (text.startsWith("deleted=")) {
                    track.setDeleted(Boolean.parseBoolean(text.substring(8)));
                } else if (text.startsWith("visible=")) {
                    track.setVisible(Boolean.parseBoolean(text.substring(8)));
                } else if (text.startsWith("audible=")) {
                    track.setAudible(Boolean.parseBoolean(text.substring(8)));
                } else if (text.startsWith("dotted=")) {
                    track.setDotted(Boolean.parseBoolean(text.substring(7)));
                } else if (text.startsWith("velocity=")) {
                    track.setVelocity(Integer.parseInt(text.substring(9)));
                } else if (text.startsWith("displayOffset=")) {
                    track.setDisplayOffsetFromMetadata(Integer.parseInt(text.substring(14)));
                } else if (text.startsWith("canals=")) {
                    List<Integer> canals = readCanals(text.substring(7));
                    for (int c : canals) {
                        track.afegirCanal(c);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return trackId;
    }
// Mètode per analitzar un fitxer MIDI i generar la partitura en format MyGridPattern

// Modificat per assignar canals a tracks - falla
//    public void readMidiScore(String fitxer) {
//        // defaults;
//        this.choice.setNoneChoice();
//        this.midiKey = Settings.getDefaultKey();
//        this.usePentagramaStrips = true;
//        this.showNoteNames = true;
//        this.useMobileDo = false;
//        this.useScreenKeyboardRight = false;
//        this.delay = 0;
//        this.scaleMode = 'M';
//        MyTempo.setTempo(60);
//
//        File midiFile = new File(fitxer);
//        Sequence sequence = null;
//        try {
//            sequence = MidiSystem.getSequence(midiFile);
//        } catch (InvalidMidiDataException | IOException e) {
//            JOptionPane.showMessageDialog(null, "No s'ha pogut carregar el fitxer MIDI: " + fitxer);
//            this.controller.updateTextOfButtons();
//            return;
//        }
//        if (!this.analyzeMidiHeader(sequence)) {
//            this.controller.updateTextOfButtons();
//            return;
//        }
//        this.firstTick = 0;
//        this.ticksPerQuarter = adjustResolution(sequence.getResolution());
//        Track[] tracks = sequence.getTracks();
//
//        int first = 0;
//        if (isMetaTrack(tracks[0])) {
//            first = 1;
//        }
//
//        Map<Integer, MyTrack> canalToTrack = new HashMap<>();
//        Map<Integer, Integer> canalToTrackId = new HashMap<>();
//        int nextTrackId = 0;
//        MyMixer mixer = this.controller.getMixer();
//
//        for (int tr = first; tr < tracks.length; tr++) {
//            Track track = tracks[tr];
//
//            setCurrentWriteCol(0);
//            for (int j = 0; j < track.size(); j++) {
//                MidiEvent event = track.get(j);
//                MidiMessage message = event.getMessage();
//
//                if (message instanceof ShortMessage) {
//                    ShortMessage sm = (ShortMessage) message;
//                    int command = sm.getCommand();
//                    int channel = sm.getChannel();
//                    int pitch = sm.getData1();
//                    int velocity = sm.getData2();
//                    long tick = event.getTick();
//
//                    if (!canalToTrack.containsKey(channel)) {
//                        MyTrack nouTrack = new MyTrack(nextTrackId, "Track " + channel);
//                        nouTrack.afegirCanal(channel);
//                        nouTrack.setCurrentChannel(channel);
//                        mixer.addTrack(nouTrack);
//                        canalToTrack.put(channel, nouTrack);
//                        canalToTrackId.put(channel, nextTrackId);
//                        nextTrackId++;
//                    }
//
//                    MyTrack canalTrack = canalToTrack.get(channel);
//                    canalTrack.afegirCanal(channel);
//                    canalTrack.setCurrentChannel(channel);
//
//                    switch (command) {
//                        case ShortMessage.NOTE_ON:
//                            if (velocity > 0) {
//                                activeNotes.put(pitch, new NoteInfo(pitch, tick, velocity, channel, canalToTrackId.get(channel)));
//                            } else {
//                                processNoteOff(pitch, tick);
//                            }
//                            break;
//                        case ShortMessage.NOTE_OFF:
//                            processNoteOff(pitch, tick);
//                            break;
//                        case ShortMessage.CONTROL_CHANGE:
//                            placeAppendMidiMessage(message);
//                            break;
//                        case ShortMessage.PROGRAM_CHANGE:
//                            placeAppendMidiMessage(message);
//                            int instr = sm.getData1();
//                            SoundWithMidi.assignInstToChannel(channel, instr);
//                            SoundWithMidi.runProgramChange(channel, instr);
//                            break;
//                        default:
//                            placeAppendMidiMessage(message);
//                            break;
//                    }
//                } else if (message instanceof MetaMessage) {
//                    MetaMessage metaMessage = (MetaMessage) message;
//                    int type = metaMessage.getType();
//                    long tick = event.getTick();
//
//                    switch (type) {
//                        case 0x51 -> {
//                            byte[] tempoData = metaMessage.getData();
//                            int tempoMicroseconds = ((tempoData[0] & 0xFF) << 16) | ((tempoData[1] & 0xFF) << 8) | (tempoData[2] & 0xFF);
//                            placeAppendMidiMessage(message);
//                        }
//                        case 0x58, 0x59 ->
//                            placeAppendMidiMessage(message);
//                        case 0x03 -> {
//                            byte[] data = metaMessage.getData();
//                            String trackName = new String(data);
//                            // Assignem el nom al primer track disponible
//                            if (!canalToTrack.isEmpty()) {
//                                canalToTrack.values().iterator().next().setName(trackName);
//                            }
//                        }
//                        default ->
//                            placeAppendMidiMessage(message);
//                    }
//                }
//            }
//        }
//
//        // Textos i acords
//        Track metaTrack = sequence.getTracks()[0];
//        int ticksPerCol = getTicksPerCol();
//        for (int i = 0; i < metaTrack.size(); i++) {
//            MidiEvent event = metaTrack.get(i);
//            MidiMessage message = event.getMessage();
//
//            if (message instanceof MetaMessage) {
//                MetaMessage meta = (MetaMessage) message;
//                if (meta.getType() == 0x7F) {
//                    String text = new String(meta.getData(), java.nio.charset.StandardCharsets.UTF_8);
//                    long tick = event.getTick();
//                    int col = (int) (tick / ticksPerCol);
//
//                    if (text.startsWith("CHORD:")) {
//                        String[] parts = text.split("::");
//                        int ncols = Integer.parseInt(parts[1]);
//                        String chordData = parts[0].substring(6).trim();
//                        Chord chord = new Chord(chordData);
//                        chord.setNCols(ncols);
//                        this.placeChordSymbol(chord, col);
//                        if (col + 1 > getLastColWritten()) {
//                            setLastColWritten(col + 1);
//                        }
//                    } else if (!text.startsWith("choice=") && !text.startsWith("midiKey=") && !text.startsWith("scaleMode=")
//                            && !text.startsWith("usePentagramaStrips=") && !text.startsWith("showNoteNames=")
//                            && !text.startsWith("useMobileDo=") && !text.startsWith("useScreenKeyboardRight=")
//                            && !text.startsWith("showMutted=") && !text.startsWith("delay=") && !text.startsWith("chordTrack=")) {
//                        if (this.messages.containsKey(col)) {
//                            String existing = this.messages.get(col);
//                            this.messages.put(col, existing + "; " + text);
//                        } else {
//                            this.messages.put(col, text);
//                        }
//                        if (col + 1 > getLastColWritten()) {
//                            setLastColWritten(col + 1);
//                        }
//                        this.placeAppendMessage(text, 0);
//                    }
//                }
//            }
//        }
//
//        updateStripsNKeyboard(usePentagramaStrips);
//        this.controller.setScreenKeyboardRight(useScreenKeyboardRight);
//        ToneRange.setMovileDo(useMobileDo);
//        this.setCurrentWriteCol(this.getLastColWritten() + 1);
//        this.controller.updateTextOfButtons();
//    }

// Mètode per analitzar un fitxer MIDI i generar la partitura en format MyGridPattern
    public void readMidiScore(String fitxer) {
        // defaults;
        this.choice.setNoneChoice();
        this.midiKey = ToneRange.getDefaultKey();
        this.usePentagramaStrips = true;
        this.showNoteNames = true;
        this.useMobileDo = false;
        this.useScreenKeyboardRight = false;
        this.setDefaultDelay();
        this.scaleMode = 'M';
        MyTempo.setTempo(60);
        SoundWithMidi.resetChannels();

//        this.controller.getMixer().setChordTrack(null);
        File midiFile = new File(fitxer);
        Sequence sequence = null;
        try {
            sequence = MidiSystem.getSequence(midiFile);
        } catch (InvalidMidiDataException e) {
            JOptionPane.showMessageDialog(null, "El fitxer " + fitxer + " no és un fitxer midi!");
            this.controller.updateTextOfButtons();
            return;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "El fitxer " + fitxer + " no existeix!");
            this.controller.updateTextOfButtons();
            return;
        }
        Arrays.fill(loadChannelDisplayOffset, 0);
        boolean ok = this.analyzeMidiHeader(sequence);
        if (!ok) {
            this.controller.updateTextOfButtons();
            return;
        }
        this.firstTick = 0; //firstTick(sequence);

        // Variables per gestionar el processament de notes
        this.ticksPerQuarter = sequence.getResolution();
        this.ticksPerQuarter = adjustResolution(this.ticksPerQuarter);
        Track[] tracks = sequence.getTracks();

        int first = 0;
        Track metatr = tracks[0];
        if (isMetaTrack(metatr)) {
            first = 1;
        }

        for (int tr = first; tr < tracks.length; tr++) {
            Track track = tracks[tr];
            MyTrack mixerTrack = new MyTrack(tr - first, "");
            int trId = readTrackData(mixerTrack, track);
            if (mixerTrack.isDisplayOffsetFromMetadata()) {
                int chan = mixerTrack.getCurrentChannel();
                if (chan >= 0 && chan < 16) {
                    loadChannelDisplayOffset[chan] = mixerTrack.getDisplayOffset();
                }
            }
            MyMixer mixer = this.controller.getMixer();
            int specialTracks = 0;
            if (trId == mixer.getChordTrackId()){
                mixer.setChordTrack(mixerTrack);
                specialTracks++;
            } else if (trId == mixer.getDrumsTrackId()){
                mixer.setDrumsTrack(mixerTrack);
                specialTracks++;
//            } else {
//                trId = tr-first-specialTracks;// assigna el trackId a mixerTrack //???
//                mixer.addTrack(mixerTrack);
//            }
            }
            trId = tr-first-specialTracks;// assigna el trackId a mixerTrack
            mixer.addTrack(mixerTrack);           
            mixerTrack.setId(trId);
            this.controller.getMixer().setCurrentTrack(trId);
            this.controller.getMixer().getCurrentTrack().setnNotes(0);
            if (LOCAL_VERBOSE) {
                System.out.println("Processant pista " + (tr - first));
            }

            setCurrentWriteCol(0);
            for (int j = 0; j < track.size(); j++) {
                MidiEvent event = track.get(j);
                MidiMessage message = event.getMessage();

                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    int command = sm.getCommand();
                    int channel = sm.getChannel();
                    mixerTrack.afegirCanal(channel);
                    mixerTrack.setCurrentChannel(channel);
//                    this.activeChannels.put(channel, false);
                    int pitch = sm.getData1(); // El pitch de la nota o el controlador
                    int velocity = sm.getData2(); // La velocitat de la nota o el valor de control
                    long tick = event.getTick();

                    switch (command) {
                        case ShortMessage.NOTE_ON:
                            if (velocity > 0) {
                                // Processar un NOTE_ON: comença una nova nota
                                if (LOCAL_VERBOSE) {
                                    System.out.println("NOTE_ON al tick " + tick + ": " + pitch + " canal " + channel + " vol " + velocity);
                                }
                                long k = noteKey(pitch, channel, tr - first);
				activeNotes.put(k, new NoteInfo(pitch, tick, velocity, channel, tr - first));
                            } else {
                                // Tractar NOTE_ON amb velocitat 0 com un NOTE_OFF
                                if (LOCAL_VERBOSE) {
                                    System.out.println("NOTE_OFF (vel 0) al tick " + tick + ": " + pitch + " canal " + channel);
                                }
                                long k = noteKey(pitch, channel, tr - first);
                                processNoteOff(k, tick);
                            }
                            break;

                        case ShortMessage.NOTE_OFF:
                            // Processar un NOTE_OFF: finalitza la nota
                            if (LOCAL_VERBOSE) {
                                System.out.println("NOTE_OFF al tick " + tick + ": " + pitch + " canal " + channel);
                            }
                            long k = noteKey(pitch, channel, tr - first);
                            processNoteOff(k, tick);
                            break;

                        case ShortMessage.CONTROL_CHANGE:
                            if (LOCAL_VERBOSE) {
                                System.out.println("CONTROL_CHANGE al tick " + tick + ": Controller " + pitch + " valor " + velocity + " canal " + channel);
                            }
                            placeAppendMidiMessage(message);
                            break;

                        case ShortMessage.PROGRAM_CHANGE:
                            if (LOCAL_VERBOSE) {
                                System.out.println("PROGRAM_CHANGE al tick " + tick + ": Program " + pitch + " canal " + channel);
                            }
                            placeAppendMidiMessage(message);
                            int instr = ((ShortMessage) message).getData1();
                            SoundWithMidi.assignInstToChannel(channel, instr);
                            SoundWithMidi.runProgramChange(channel, instr);
                            if (!mixerTrack.isDisplayOffsetFromMetadata()) {
                                int offset = InstrumentRange.calcDisplayOffset(instr, ToneRange.getLowestMidi(), ToneRange.getHighestMidi());
                                mixerTrack.setDisplayOffset(offset);
                                loadChannelDisplayOffset[channel] = offset;
                            break;
                        default:
                            if (LOCAL_VERBOSE) {
                                System.out.println("ShortMessage MIDI no gestionat: " + command);
                            }
                            // placeAppendMidiMessage(message);
                            break;
                    }

                } else if (message instanceof MetaMessage) {
                    MetaMessage metaMessage = (MetaMessage) message;
                    int type = metaMessage.getType();
                    long tick = event.getTick();
                    switch (type) {
                        case 0x51:
                            byte[] tempoData = metaMessage.getData();
                            int tempoMicroseconds = ((tempoData[0] & 0xFF) << 16) | ((tempoData[1] & 0xFF) << 8) | (tempoData[2] & 0xFF);
                            int tempoBPM = 60000000 / tempoMicroseconds;
                            if (LOCAL_VERBOSE) {
                                System.out.println("Tick(" + tick + ") " + "Canvi de tempo detectat: " + tempoMicroseconds + " microsegons per quarter note (" + tempoBPM + " BPM)");
                            }
                            placeAppendMidiMessage(message);
                            break;

                        case 0x58:
                            byte[] timeSignatureData = metaMessage.getData();
                            int numerator = timeSignatureData[0];
                            int denominator = 1 << timeSignatureData[1];
                            if (LOCAL_VERBOSE) {
                                System.out.println("Tick(" + tick + ") " + "Time Signature: " + numerator + "/" + denominator);
                            }
                            placeAppendMidiMessage(message);
                            break;

                        case 0x59:
                            byte[] keySignatureData = metaMessage.getData();
                            int key = keySignatureData[0];
                            int scale = keySignatureData[1];

                            // Determinar si és major o menor
                            this.scaleMode = (scale == 0) ? 'M' : 'm';

                            // Interpretar la tonalitat (nombre d'accidentals)
                            String keySignature = interpretKeySignature(key, this.scaleMode+"");
                            this.midiKey = ToneRange.getMidiKey(keySignature);
                            if (LOCAL_VERBOSE) {
                                System.out.println("Key Signature: " + keySignature + " (" +this.scaleMode+ ")");
                            }
                            // placeAppendMidiMessage(message);
                            break;

                        case 0x03:
                            // Track name
                            byte[] data = metaMessage.getData();
                            String trackName = new String(data);
                            this.controller.getMixer().getCurrentTrack().setName(trackName);
                            break; // IMPORTANT: evita que caigui al default

                        case 0x7F:
                            // Track-data (id=..., currentChannel=..., etc.)
                            // Ja ho has processat a readTrackData(mixerTrack, track).
                            // NO ho guardis a midiMessages.
                            break;

                        default:
                            if (LOCAL_VERBOSE) {
                                System.out.println("MetaMessage no gestionat: Type " + type);
                            }
                            //placeAppendMidiMessage(message);
                            break;
                    }
                }
            }
            int aux = 0;
        }

        // Recuperar missatges de text (Text Event, MetaMessage 0x01)
        Track metaTrack = sequence.getTracks()[0];
        int ticksPerCol = getTicksPerCol();

        for (int i = 0; i < metaTrack.size(); i++) {
            MidiEvent event = metaTrack.get(i);
            MidiMessage message = event.getMessage();

            if (message instanceof MetaMessage) {
                MetaMessage meta = (MetaMessage) message;
                if (meta.getType() == 0x01) {
                    String text = "";
                    try {
                        text = new String(meta.getData(), "UTF-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

//            metaTrack = sequence.getTracks()[0];
//            ticksPerCol = getTicksPerCol();
        for (int i = 0; i < metaTrack.size(); i++) {
            MidiEvent event = metaTrack.get(i);
            MidiMessage message = event.getMessage();

            if (message instanceof MetaMessage) {
                MetaMessage meta = (MetaMessage) message;
                if (meta.getType() == 0x7F) {
                    String text = "";
                    try {
                        text = new String(meta.getData(), "UTF-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (text.startsWith("CHORD:")) {
                        String[] parts = text.split("::");
                        int ncols = Integer.parseInt(parts[1]);
                        String chordData = parts[0].substring(6).trim(); // Elimina el prefix
                        Chord chord = new Chord(chordData);
                        chord.setNCols(ncols);
                        long tick = event.getTick();
                        int col = (int) (tick / ticksPerCol);

                        this.placeChordSymbol(chord, col);
                        if (col + 1 > getLastColWritten()) {
                            setLastColWritten(col + 1);
                        }

                        if (LOCAL_VERBOSE) {
                            System.out.println("Chord loaded at col " + col + ": " + chord.basicString());
                        }
                    } else {
                        if (!(text.startsWith("choice=")
                                || text.startsWith("midiKey=")
                                || text.startsWith("scaleMode=")
                                || text.startsWith("usePentagramaStrips=")
                                || text.startsWith("showNoteNames=")
                                || text.startsWith("useMobileDo=")
                                || text.startsWith("useScreenKeyboardRight=")
                                || text.startsWith("description=")
                                || text.startsWith("showMutted=")
                                || text.startsWith("delay=")
                                || text.startsWith("chordTrack=")
                                || text.startsWith("nMeasuresCam=")
                                || text.startsWith("nColsQuarter=")
                                || text.startsWith("instruments"))) {

                            // Només entra aquí si el text no comença amb cap d’aquests prefixos
                            long tick = event.getTick();
                            int col = (int) (tick / ticksPerCol);

                            // Guardar al mapa de missatges
                            if (this.messages.containsKey(col)) {
                                String existing = this.messages.get(col);
                                this.messages.put(col, existing + "; " + text);
                            } else {
                                this.messages.put(col, text);
                            }
                            if (col + 1 > getLastColWritten()) {
                                setLastColWritten(col + 1);
                            }

                            // Mostrar el missatge a la partitura
                            this.placeAppendMessage(text, 0);

                            if (LOCAL_VERBOSE) {
                                System.out.println("Text message placed at col " + col + ": " + text);
                            }
                        }
                    }
                }
            }
        }
        //this.placeEndScore();

        // updates;
        updateStripsNKeyboard(usePentagramaStrips);
        this.controller.setScreenKeyboardRight(useScreenKeyboardRight);
        ToneRange.setMovileDo(useMobileDo);
        // showNoteNames;
        this.setCurrentWriteCol(this.getLastColWritten() + 1);
        this.controller.updateTextOfButtons();
        this.initOffscreen();
        if (Settings.IS_BU){
            SoundWithMidi.assignInstToChannel(this.controller.getMixer().getCurrentChannelOfCurrentTrack(),9);
            SoundWithMidi.runProgramChange(this.controller.getMixer().getCurrentChannelOfCurrentTrack(),9);
        }
    }
    
    private long getLastTick(Track track) {
        long last = 0;
        for (int i = 0; i < track.size(); i++) {
            long tick = track.get(i).getTick();
            if (tick > last) {
                last = tick;
            }
        }
        return last;
    }

    private void saveTrackData(MyTrack track, Track midiTrack) {
        try {
            if (track.getName() != null && !track.getName().isEmpty()) {
                MetaMessage nameMsg = new MetaMessage();
                nameMsg.setMessage(0x03, track.getName().getBytes(), track.getName().length());
                midiTrack.add(new MidiEvent(nameMsg, 0));
            }

            addTextMeta(midiTrack, "id=" + track.getId());
            addTextMeta(midiTrack, "currentChannel=" + track.getCurrentChannel());
            addTextMeta(midiTrack, "nNotes=" + track.getnNotes());
            addTextMeta(midiTrack, "keepNoteVelocity=" + track.isKeepNoteVelocity());
            addTextMeta(midiTrack, "selected=" + track.isSelected());
            addTextMeta(midiTrack, "deleted=" + track.isDeleted());
            addTextMeta(midiTrack, "visible=" + track.isVisible());
            addTextMeta(midiTrack, "audible=" + track.isAudible());
            addTextMeta(midiTrack, "dotted=" + track.isDotted());
            addTextMeta(midiTrack, "velocity=" + track.getVelocity());
            addTextMeta(midiTrack, "displayOffset=" + track.getDisplayOffset());
            addTextMeta(midiTrack, "canals=" + track.getCanals().toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

// Mètode per analitzar un fitxer MIDI i generar la partitura en format MyGridPattern
    public void saveMidiScore(String filePath) {
        boolean isFirstNoteOn = true;
        this.ticksPerQuarter = SoundWithMidi.DEFAULT_TICKS_PER_QUARTER;
        // Crear la seqüència amb format 1 i la resolució de la partitura
        Sequence sequence = null;
        try {
            sequence = new Sequence(Sequence.PPQ, (int) ticksPerQuarter);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
            return;
        }

        // Escala columna → tick
        int ticksPerCol = getTicksPerCol();

        // Pista 0: capçalera (tempo, compàs, tonalitat)
        Track metaTrack = sequence.createTrack();
        buildMidiHeader(metaTrack);

        // Crear pistes per a cada pista del mixer
        Map<Integer, Track> trackMap = new HashMap<>();
        for (int tr = 0; tr < this.controller.getMixer().getnTracks(); tr++) {
            Track str=null;
            if (this.controller.getMixer().isTrackVisible(tr)) {
                str = sequence.createTrack();
                trackMap.put(tr, str);
                saveTrackData(this.controller.getMixer().getTrackFromId(tr), str);
                if (tr == 0) { // MuseScore no llegeix el metaData del metaTrack. Aquí replico el key signature i l'instrument, però caldria replicar tot.
                    int keyIndex = ToneRange.getKeySignatureIndex(this.midiKey, this.scaleMode);
                    int scale = (this.scaleMode == 'M') ? 0 : 1;
                    byte[] ksData = new byte[]{(byte) keyIndex, (byte) scale};
                    MetaMessage ksMsg = new MetaMessage();
                    try {
                        ksMsg.setMessage(0x59, ksData, 2);
                    } catch (InvalidMidiDataException e) {
                        e.printStackTrace();
                    }
                    if (str != null) {
                        str.add(new MidiEvent(ksMsg, 0));
                    }
                }
            }
        }

        int tr = this.controller.getMixer().getChordTrackId();
        MyTrack chordTrack = this.controller.getMixer().getTrackFromId(tr);
        if (chordTrack != null) {
            Track str = sequence.createTrack();
            trackMap.put(tr, str);
            saveTrackData(chordTrack, str);
        }

        tr = this.controller.getMixer().getDrumsTrackId();
        MyTrack drumsTrack = this.controller.getMixer().getTrackFromId(tr);
        if (drumsTrack != null) {
            Track str = sequence.createTrack();
            trackMap.put(tr, str);
            saveTrackData(drumsTrack, str);
        }

        // Recorregut per la graella i generació de NOTE_ON/OFF
        for (int keyId = 0; keyId < this.nKeys; keyId++) {
            for (int col = 0; col < this.nCols; col++) {
                if (col == getLastColWritten()) {
                    break;
                }
                MyGridSquare square = this.grid[keyId][col];
                int visualPitch = ToneRange.keyIdToMidi(keyId);

                if (square==null) continue;
                List polinotes = new LinkedList<>(square.getPoliNotes());
                for (Object o : polinotes) {
                    MyGridSquare.SubSquare sub = (MyGridSquare.SubSquare) o;
                    int channel = sub.getChannel();
                    int trackIndex = sub.getTrack();
                    int velocity = sub.getVelocity();
                    if (this.controller.getMixer().isTrackVisible(trackIndex)) {
                        if (!sub.isLinked()) {
                            int lengthCols = 1;
                            for (int k = col + 1; k < this.nCols; k++) {
                                MyGridSquare next = this.grid[keyId][k];
                                boolean linked = false;
                                if (next!= null){
                                    for (Object o2 : next.getPoliNotes()) {
                                        MyGridSquare.SubSquare sub2 = (MyGridSquare.SubSquare) o2;
                                        if (sub2.getChannel() == channel && sub2.getTrack() == trackIndex && sub2.isLinked()) {
                                            linked = true;
                                            lengthCols++;
                                        }
                                    }
                                }
                                if (!linked) {
                                    break;
                                }
                            }

                            long tickOn = (long) col * ticksPerCol;
                            long tickOff = (long) (col + lengthCols) * ticksPerCol;

                            Track track = trackMap.get(trackIndex);
                            if (col > getLastColWritten()) {
                                throw new IllegalArgumentException(
                                        "MyMidiScore::saveMidiScore() col = " + col + " > lastColWritten = " + getLastColWritten());
                            }
                            if (track != null) {
                                if (Settings.IS_BU && isFirstNoteOn) {
                                    isFirstNoteOn = false;
                                    ShortMessage pc = new ShortMessage();
                                    try {
                                        pc.setMessage(ShortMessage.PROGRAM_CHANGE, channel, 9, 0);
                                    } catch (InvalidMidiDataException e) {
                                        e.printStackTrace();
                                    }
                                    track.add(new MidiEvent(pc, tickOn));
                                }
                                MyTrack trackObj = this.controller.getMixer().getTrackFromId(trackIndex);
                                int dispOff = (trackObj != null) ? trackObj.getDisplayOffset() : 0;
                                int realPitch = visualPitch - dispOff;
                                int rectified = realPitch - 12 * ToneRange.getOctavesUp();
                                if (Settings.IS_BU){
                                    rectified = realPitch;
                                }
                                ShortMessage on = new ShortMessage();
                                try {
                                    
                                    on.setMessage(ShortMessage.NOTE_ON, channel, rectified, velocity);
                                } catch (InvalidMidiDataException e) {
                                    e.printStackTrace();
                                }
                                track.add(new MidiEvent(on, tickOn));

                                ShortMessage off = new ShortMessage();
                                try {
                                    off.setMessage(ShortMessage.NOTE_OFF, channel, rectified, 0);
                                } catch (InvalidMidiDataException e) {
                                    e.printStackTrace();
                                }
                                track.add(new MidiEvent(off, tickOff));
                            }
                        }
                    }
                }
            }
        }

        // Missatges MIDI (program change, control, etc.)
        for (int col : this.midiMessages.keySet()) {
            long tick = (long) col * ticksPerCol;
            ArrayList<MidiMessage> messages = this.midiMessages.get(col);
            if (messages != null) {
                for (MidiMessage message : messages) {
                    if (message instanceof ShortMessage) {
                        ShortMessage sm = (ShortMessage) message;                            //t trackIndex = this.controller.getMixer().getTrackOfChannel(sm.getChannel());
                        metaTrack.add(new MidiEvent(sm, tick));
                    } else if (message instanceof MetaMessage) {
                        MetaMessage mm = (MetaMessage) message;                            //t trackIndex = this.controller.getMixer().getTrackOfChannel(sm.getChannel());
                        metaTrack.add(new MidiEvent(mm, tick));
                    }
                }
            }
        }

        // Missatges textuals (Text Event) de this.messages
        for (int col : this.messages.keySet()) {
            long tick = (long) col * ticksPerCol;
            String text = this.messages.get(col);
            if (text != null && !text.isEmpty()) {
                MetaMessage textMsg = new MetaMessage();
                byte[] data = text.getBytes();
                try {
                    textMsg.setMessage(0x7F, data, data.length); // Text Event
                } catch (InvalidMidiDataException e) {
                    e.printStackTrace();
                }
                metaTrack.add(new MidiEvent(textMsg, tick));
            }
        }

        // Desar els acords de chordSymbolLine com a missatges de text (MetaMessage 0x7F)
        for (Map.Entry<Integer, Chord> entry : this.chordSymbolLine.entrySet()) {
            int col = entry.getKey();
            Chord chord = entry.getValue();
            if (chord != null && chord.isValidChord()) {
                long tick = (long) col * ticksPerCol;
                String chordText = "CHORD:" + chord.basicString() + " " + chord.getInfo() + "::" + chord.getNCols(); // prefix distintiu

                MetaMessage textMsg = new MetaMessage();
                byte[] data = null;
                try {
                    data = chordText.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                try {
                    textMsg.setMessage(0x7F, data, data.length);
                } catch (InvalidMidiDataException e) {
                    e.printStackTrace();
                }
                metaTrack.add(new MidiEvent(textMsg, tick));
                // Deso també el simbol de l'acord al text 0x01 per al MuseScore
                textMsg = new MetaMessage();
                try {
                    data = chord.basicString().getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                try {
                    textMsg.setMessage(0x01, data, data.length);
                } catch (InvalidMidiDataException e) {
                    e.printStackTrace();
                }
                metaTrack.add(new MidiEvent(textMsg, tick));
            }
        }

        // Finalitzar totes les pistes amb End of Track (0x2F)
        Track[] tracks = sequence.getTracks();
        for (Track track : tracks) {
            MetaMessage end = new MetaMessage();
            try {
                end.setMessage(0x2F, new byte[0], 0);
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
            long lastTick = getLastTick(track);
            track.add(new MidiEvent(end, lastTick + 1));
        }
        
        MyTrack chrtr = this.controller.getMixer().getChordTrack();

        // Escriure el fitxer MIDI
        File midiFile = new File(filePath);
        try {
            MidiSystem.write(sequence, 1, midiFile); // format 1
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Cannot save " + midiFile);
        }
        if (LOCAL_VERBOSE) {
            System.out.println("Fitxer MIDI desat a: " + filePath);
        }
    }

    public static boolean isMetaTrack(Track track) {
        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            MidiMessage msg = event.getMessage();

            // Si no és un MetaMessage, mirem si és un missatge de canal
            if (!(msg instanceof MetaMessage)) {
                byte[] data = msg.getMessage();
                int status = data[0] & 0xFF;
                // Missatges de canal van de 0x80 a 0xEF
                if (status >= 0x80 && status <= 0xEF) {
//                if (status >= 0x80 && status < 0xA0) { note on and off
                    return false; // Hi ha notes o controladors, no és metatrack
                }
            }
        }
        return true; // Tots els missatges eren MetaMessage o altres no de canal
    }

    /**
     * Assigna els valors de tempo i keys direcament, sense fer un place a la
     * partitura.
     *
     * @param filePath
     */
    public boolean analyzeMidiHeader(Sequence sequence) {
        boolean ok = true;
        // Variables per emmagatzemar la informació de la capçalera
        int tempoBPM = -1;
        String timeSignature = "";
        String keySignature = "";
//        String mode = "";  // Major o Minor
//            boolean tempoDetected = false;
//            boolean timeSignatureDetected = false;
//            boolean keySignatureDetected = false;

        // Recórrer les pistes per trobar els meta missatges d'inici
        Track[] tracks = sequence.getTracks();
        boolean first03 = true;
        for (Track track : tracks) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();

                if (message instanceof MetaMessage) {
                    MetaMessage metaMessage = (MetaMessage) message;
                    int type = metaMessage.getType();
                    String text = new String(metaMessage.getData(), StandardCharsets.UTF_8).trim();

                    if (type == 0x03 || text.startsWith("TITLE:")) {
                        if (first03) {
                            first03 = false;
                            if (text.startsWith("TITLE:")) {
                                text = text.substring(6).trim();
                            }
                            this.setTitle(text);
                        }
                    } else if (type == 0x02 || text.startsWith("AUTHOR:")) {
                        if (text.startsWith("AUTHOR:")) {
                            text = text.substring(7).trim();
                        }
                        this.setAuthor(text);
                    }
                    // Anàlisi del canvi de tempo
//                        if ((type == 0x51) && !tempoDetected) {
//                            tempoDetected = true;
                    if ((type == 0x51)) {
                        byte[] tempoData = metaMessage.getData();
                        int tempoMicroseconds = ((tempoData[0] & 0xFF) << 16) | ((tempoData[1] & 0xFF) << 8) | (tempoData[2] & 0xFF);
                        tempoBPM = 60000000 / tempoMicroseconds;
                        if (LOCAL_VERBOSE) {
                            System.out.println("Tempo: " + tempoBPM + " BPM");
                        }
                        MyTempo.setTempo(tempoBPM);
//                            MyTempo.checkTempo(); 
                    }

                    // Anàlisi de la time signature
//                        if ((type == 0x58) && !timeSignatureDetected) {
//                            timeSignatureDetected = true;
                    if ((type == 0x58)) {
                        byte[] timeSignatureData = metaMessage.getData();
                        int numerator = timeSignatureData[0];
                        int denominator = 1 << timeSignatureData[1];
                        if (denominator == 8) numerator /=3;
                        timeSignature = numerator + "/" + denominator;
                        this.setNumBeatsMeasure(numerator);
                        this.setBeatFigure(denominator);
                        if (LOCAL_VERBOSE) {
                            System.out.println("Time Signature: " + timeSignature);
                        }
                    }

                    // Anàlisi de la pitch signature i mode (Major o Minor)
//                        if ((type == 0x59) && !keySignatureDetected) {
//                            keySignatureDetected = true;
                    if ((type == 0x59)) {
                        byte[] keySignatureData = metaMessage.getData();
                        int key = keySignatureData[0];
                        int scale = keySignatureData[1];

                        // Determinar si és major o menor
                        this.scaleMode = (scale == 0) ? 'M' : 'm';

                        // Interpretar la tonalitat (nombre d'accidentals)
                        keySignature = interpretKeySignature(key, this.scaleMode+"");
                        this.midiKey = ToneRange.getMidiKey(keySignature);
                        if (LOCAL_VERBOSE) {
                            System.out.println("Key Signature: " + keySignature + " (" +this.scaleMode+ ")");
                        }
                    }
                    if (type == 0x7F) { // Text Event
                        try {
                            text = new String(metaMessage.getData(), "UTF-8");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (text.startsWith("description=")) {
                            this.setDescription(text.substring(12));
                        } else if (text.startsWith("choice=")) {
                            String raw = text.substring(7).replaceAll("[\\[\\] ]", "");
                            String[] tokens = raw.split(",");
                            Integer[] ch = Arrays.stream(tokens)
                                    .map(Integer::parseInt) // no cal mapToInt, ja és Integer
                                    .toArray(Integer[]::new); // crea Integer[] 
                            this.choice.setChoiceList(Arrays.asList(ch));
                        } else if (text.startsWith("midiKey=")) {
                            this.midiKey = Integer.parseInt(text.substring(8))+12*ToneRange.getOctavesUp();
                        } else if (text.startsWith("scaleMode=")) {
                            this.scaleMode = text.substring(10).charAt(0);
                        } else if (text.startsWith("usePentagramaStrips=")) {
                            String sub = text.substring(20); // ara si
                            this.usePentagramaStrips = Boolean.parseBoolean(sub);
                        } else if (text.startsWith("showNoteNames=")) {
                            String sub = text.substring(14); // ara
                            this.showNoteNames = Boolean.parseBoolean(sub);
                        } else if (text.startsWith("useMobileDo=")) {
                            this.useMobileDo = Boolean.parseBoolean(text.substring(12));
                        } else if (text.startsWith("useScreenKeyboardRight=")) {
                            this.useScreenKeyboardRight = Boolean.parseBoolean(text.substring(23));
//                        } else if (text.startsWith("delay=")) {
//                            this.delay = Integer.parseInt(text.substring(6));
                        } else if (text.startsWith("nMeasuresCam=")) {
                            Settings.setnMeasuresCam(Integer.parseInt(text.substring(13)));
                        } else if (text.startsWith("nColsQuarter=")) {
                            Settings.setnColsQuarter(Integer.parseInt(text.substring(13)));
                            Settings.updateNColsBeat();
                        } else //                        {
                        //                            for (int tr = 0; tr < tracks.length; tr++) {
                        //                                if (text.startsWith("channels" + tr)) {
                        //                                    List<Integer> canals = readCanals(text.substring(8+String.valueOf(tr).length()));
                        //                                    this.controller.getMixer().setCanals(tr, canals);
                        //                                }
                        //                            }
                        //                        }
                        //                        if (text.startsWith("chordTrack=")) {
                        //                            this.controller.getMixer().setChordTrack(Integer.parseInt(text.substring(11)));
                        //                        } else 
                        if (text.startsWith("instruments")) {
                            Map<Integer, Integer> chanInst = readInstruments(text.substring(9));
                            for (int chan : chanInst.keySet()) {
                                int instr = chanInst.get(chan);
                                SoundWithMidi.assignInstToChannel(chan, instr);
                                SoundWithMidi.runProgramChange(chan, instr);
                            }
                        }
                    }
                }
            }
        }
        return ok;
    }

    /**
     * Construeix un Map<Integer, Integer> a partir del seu format toString()
     * (p. ex. "{0=25, 1=32}"). Builds a Map<Integer, Integer> from its
     * toString() format (e.g., "{0=25, 1=32}").
     *
     * @param text el text amb el format del map // the map-formatted input
     * string
     * @return un mapa canal→instrument // a map from channel to instrument
     */
    public static Map<Integer, Integer> readInstruments(String text) {
        Map<Integer, Integer> map = new HashMap<>();
        text = text.trim();
        if (text.startsWith("{") && text.endsWith("}")) {
            text = text.substring(1, text.length() - 1);  // elimina les claus
            if (!text.isEmpty()) {
                String[] entries = text.split(",");
                for (String entry : entries) {
                    String[] kv = entry.trim().split("=");
                    if (kv.length == 2) {
                        int key = Integer.parseInt(kv[0].trim());
                        int value = Integer.parseInt(kv[1].trim());
                        map.put(key, value);
                    }
                }
            }
        }
        return map;
    }

    public static List<Integer> readCanals(String text) {
        List<Integer> canals = new ArrayList<>();
        // Elimina els claudàtors i espais al voltant
        text = text.trim();
        if (text.startsWith("[") && text.endsWith("]")) {
            text = text.substring(1, text.length() - 1);  // sense els [ ]
            if (!text.isEmpty()) {
                String[] parts = text.split(",");
                for (String part : parts) {
                    canals.add(Integer.parseInt(part.trim()));
                }
            }
        }
        return canals;
    }

    public void buildMidiHeader(Track metaTrack) {
//        try {
        // --- Tempo ---
        int tempoBPM = MyTempo.getTempo();
        int tempoMPQN = 60000000 / tempoBPM;
        byte[] tempoData = new byte[]{
            (byte) ((tempoMPQN >> 16) & 0xFF),
            (byte) ((tempoMPQN >> 8) & 0xFF),
            (byte) (tempoMPQN & 0xFF)
        };
        MetaMessage tempoMsg = new MetaMessage();
        try {
            tempoMsg.setMessage(0x51, tempoData, 3);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        metaTrack.add(new MidiEvent(tempoMsg, 0));

        // --- Time Signature ---
        int numerator = this.getNumBeatsMeasure();
        int denominator = this.getBeatFigure();
        if (denominator == 8) numerator*=3;
        byte[] tsData = new byte[]{
            (byte) numerator,
            (byte) (Math.log(denominator) / Math.log(2)), // expressat com potència de 2
            24, 8
        };
        MetaMessage tsMsg = new MetaMessage();
        try {
            tsMsg.setMessage(0x58, tsData, 4);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        metaTrack.add(new MidiEvent(tsMsg, 0));

        // --- Key Signature ---
//        int keyIndex = ToneRange.getKeySignatureIndex(this.midiKey-12*ToneRange.getOctavesUp(), this.scaleMode);
        int keyIndex = ToneRange.getKeySignatureIndex(this.midiKey, this.scaleMode);
        int scale = (this.scaleMode == 'M') ? 0 : 1;
        byte[] ksData = new byte[]{(byte) keyIndex, (byte) scale};
        MetaMessage ksMsg = new MetaMessage();
        try {
            ksMsg.setMessage(0x59, ksData, 2);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        metaTrack.add(new MidiEvent(ksMsg, 0));

        // --- Title and Author ---
        String titol = this.getTitle();
        if (titol != null && !titol.isEmpty()) {
            MetaMessage titleMsg = new MetaMessage();
	    byte[] data = ("TITLE:" + titol).getBytes(StandardCharsets.UTF_8);
            try {
                titleMsg.setMessage(0x03, data, data.length);
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }

            metaTrack.add(new MidiEvent(titleMsg, 0));
        }

        String descr = this.getDescription();
        if (descr != null && !descr.isEmpty()) {
            try {
                addTextMeta(metaTrack, "description=" + descr);
            } catch (Exception e) { e.printStackTrace(); }
        }

        String autor = this.getAuthor();
        if (autor != null && !autor.isEmpty()) {
            MetaMessage authorMsg = new MetaMessage();
            byte[] data = ("AUTHOR:" + autor).getBytes(StandardCharsets.UTF_8);
            //byte[] data = ("AUTHOR" + autor).getBytes(StandardCharsets.UTF_8);
            try {
                authorMsg.setMessage(0x02, data, data.length);
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
            metaTrack.add(new MidiEvent(authorMsg, 0));
        }

        // --- Text Events amb la configuració interna ---
        try {

            if (this.choice.getChoiceList() != null && !this.choice.getChoiceList().isEmpty()) {
                addTextMeta(metaTrack, "choice=" + Arrays.toString(this.choice.getChoiceList().toArray()));
            }
            addTextMeta(metaTrack, "midiKey=" + this.midiKey);
            addTextMeta(metaTrack, "scaleMode=" + this.scaleMode);
            addTextMeta(metaTrack, "usePentagramaStrips=" + this.usePentagramaStrips);
            addTextMeta(metaTrack, "showNoteNames=" + this.showNoteNames);
            addTextMeta(metaTrack, "useMobileDo=" + this.useMobileDo);
            addTextMeta(metaTrack, "useScreenKeyboardRight=" + this.useScreenKeyboardRight);
//            addTextMeta(metaTrack, "showMutted=" + this.showMutted);
//            addTextMeta(metaTrack, "delay=" + this.delay);
            addTextMeta(metaTrack, "nMeasuresCam=" + Settings.getnMeasuresCam());
            addTextMeta(metaTrack, "nColsQuarter=" + Settings.getnColsQuarter());
//            int ntr = 0;
//            for (int tr = 0; tr < this.controller.getMixer().getnTracks(); tr++) {
//                if (this.controller.getMixer().isTrackVisible(tr)) {
//                    ntr++;
////                    MyTrack pista = this.controller.getMixer().getTracks().get(tr);
////                    List<Integer> canalsUtilitzats = pista.getCanals();
////                    addTextMeta(metaTrack, "channels" + tr + " " + canalsUtilitzats);
//                }
//            }
//            int chtr = this.controller.getMixer().getChordTrack();
//            if (chtr >= ntr) {
//                this.controller.getMixer().setChordTrack(-1);
//            }
//            addTextMeta(metaTrack, "chordTrack=" + this.controller.getMixer().getChordTrack());
	    StringBuilder sb = new StringBuilder();
            sb.append("instruments={");
            boolean firstInst = true;
            for (int channel : SoundWithMidi.activeChannels()) {
                if (!firstInst) {
                    sb.append(", ");
                }
                firstInst = false;
                sb.append(channel).append("=").append(SoundWithMidi.getInstrumentInChannel(channel));
            }
            sb.append("}");
            addTextMeta(metaTrack, sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void addTextMeta(Track track, String text) throws Exception {
        MetaMessage msg = new MetaMessage();
        byte[] data = text.getBytes("UTF-8");
        msg.setMessage(0x7F, data, data.length); // Text Event
        track.add(new MidiEvent(msg, 0));
    }

    public static MetaMessage composeTimeSignatureMessage(String timeSignature) {
        timeSignature = timeSignature.trim();
        String[] parts = timeSignature.split(("/"));
        int numerator = Integer.parseInt(parts[0].trim());
        int denominator = Integer.parseInt(parts[1].trim());

        MetaMessage message = new MetaMessage();

        // Convert the denominator to the required format (as a power of 2)
        int midiDenominator = (int) (Math.log(denominator) / Math.log(2));

        // Set common values for metronome and 32nd notes per quarter note
        int metronome = 24; // 24 MIDI clocks per metronome tick
        int thirtySecondNotes = 8; // Standard for 4/4 time

        // Create the time signature data array
        byte[] data = {(byte) numerator, (byte) midiDenominator, (byte) metronome, (byte) thirtySecondNotes};

        try {
            // Meta message type 0x58 is for time signature
            message.setMessage(0x58, data, data.length);
        } catch (InvalidMidiDataException ex) {
            System.out.println("Invalid time signature data!");
        }
        return message;
    }

    // Mètode auxiliar per interpretar la Key Signature
    public static String interpretKeySignature(int key, String mode) {
        String[] majorKeys = {"Si", "Fo", "De", "Sa", "Ri", "Li", "Fa", "Do", "So", "Re", "La", "Mi", "Si", "Fo", "De"};
        String keyName = majorKeys[key + 7];
        if (mode.equals("m")) {
            keyName = MyKeyCircles.relativeKey(keyName);
        }
        //System.out.println("MyMidiScore::interpretKeySignature keyName = "+keyName+", mode = "+mode+", key= "+key);
        return keyName;
    }
    
    private long noteKey(int pitch, int channel, int track) {
        return ((long) track << 16) | ((long) channel << 8) | (long) (pitch & 0x7F);
    }

    private void processNoteOff(long key, long tick) {
        NoteInfo noteInfo = activeNotes.remove(key);
        if (noteInfo != null) {
            long duradaTicks = tick - noteInfo.getStartTick();
            int ncols = tickLengthToNCols(duradaTicks);
            setCurrentWriteCol(tickToCol(noteInfo.getStartTick()));
            int chan = noteInfo.getChannel();
            int offset = (chan >= 0 && chan < 16) ? loadChannelDisplayOffset[chan] : 0;
            int rectifiedPitch = noteInfo.getPitch() + 12 * ToneRange.getOctavesUp() + offset;
            if (Settings.IS_BU) rectifiedPitch = noteInfo.getPitch();
            placeNote(
                    rectifiedPitch,
                    ncols,
                    false,
                    false,
                    noteInfo.getChannel(),
                    noteInfo.getTrack(),
                    noteInfo.getVelocity()
            );
        }
    }

    // Processar NOTE_OFF o NOTE_ON amb velocitat 0: calcular la durada i col·locar la nota a la graella
    private void processNoteOff_old(int key, long tick) {
        NoteInfo noteInfo = activeNotes.remove(key);
        if (noteInfo != null) {
            long duradaTicks = tick - noteInfo.getStartTick(); // + 1;  // Calcular la durada en ticks
            int ncols = tickLengthToNCols(duradaTicks);
            setCurrentWriteCol(tickToCol(noteInfo.getStartTick()));
//            System.out.println("MyMidiScore::processNoteOff: " + noteInfo.getPitch() + " " +  noteInfo.getChannel()+ " " + noteInfo.getTrack()+ " " + noteInfo.getVelocity());  // Col·locar la nota a la graella
            // if (getCurrentWriteCol()>getLastColWritten())
//            System.out.println("MyMidiScore::processNoteOff, ncols, curWrCol=" + ncols + " " + getCurrentWriteCol());
            int aux = ToneRange.getOctavesUp();
            placeNote(noteInfo.getPitch() + 12 * ToneRange.getOctavesUp(), ncols, false, false, noteInfo.getChannel(), noteInfo.getTrack(), noteInfo.getVelocity());  // Col·locar la nota a la graella
//            this.controller.getMixer().getCurrentTrack().oneNoteMore();
        }
    }

    private int tickToCol(long tick) {
        int col = (int) Math.floor((tick - this.firstTick) / getTicksPerCol());
        return col;
    }

    private int tickLengthToNCols(long length) {
        int ncols;
        int nColsQuarter = Settings.getnColsBeat() * this.beatFigure / 4;
        ncols = (int) (length * nColsQuarter / this.ticksPerQuarter);  // Ajustar segons la conversió a columnes        
        return ncols;
    }

    private int getTicksPerCol() {
        int nColsQuarter = Settings.getnColsBeat() * this.beatFigure / 4;
        int tpc = (int) this.ticksPerQuarter / nColsQuarter;
        return tpc;

    }

    // Classe per emmagatzemar la informació de les notes actives
    private class NoteInfo {

        private int pitch;
        private long startTick;
        private int velocity;
        private int channel;
        private int track;

        public NoteInfo(int pitch, long startTick, int velocity, int channel, int track) {
            this.pitch = pitch;
            this.startTick = startTick;
            this.velocity = velocity;
            this.channel = channel;
            this.track = track;
        }

        public int getPitch() {
            return pitch;
        }

        public long getStartTick() {
            return startTick;
        }

        public int getVelocity() {
            return velocity;
        }

        public int getChannel() {
            return channel;
        }

        public int getTrack() {
            return track;
        }
    }

}
