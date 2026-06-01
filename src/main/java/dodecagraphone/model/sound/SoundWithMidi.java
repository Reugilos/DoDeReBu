/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
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
 * [CA] Capa d'accés al sintetitzador MIDI del sistema. Gestiona la inicialització
 * dels canals MIDI, l'assignació d'instruments a canals, la reproducció i aturada
 * de notes, i la interpretació de missatges MIDI durant la reproducció de la partitura.
 * Totes les operacions són estàtiques; la classe no s'instancia.
 * <p>
 * [EN] Access layer for the system MIDI synthesizer. Manages MIDI channel initialization,
 * instrument-to-channel assignment, note playback and stopping, and interpretation of
 * MIDI messages during score playback. All operations are static; the class is not
 * instantiated.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class SoundWithMidi {

    private static final boolean LOCAL_VERBOSE = false;
    /** [CA] Canal MIDI per defecte. [EN] Default MIDI channel. */
    public static final int DEFAULT_CHANNEL = 0;
    /**
     * [CA] Canal MIDI de percussió GM (canal 10, índex 9). Les notes activen sons
     * de percussió directament sense necessitat de programChange.
     * [EN] GM percussion MIDI channel (channel 10, index 9). Notes trigger drum
     * sounds directly without programChange.
     */
    public static final int METRONOME_CHANNEL = 9;
    /** [CA] Nota de percussió GM per al temps fort (inici de compàs): Claves. [EN] GM drum note for strong beat (measure start): Claves. */
    public static final int METRONOME_NOTE_STRONG = 75; // Claves
    /** [CA] Nota de percussió GM per al temps feble (beat interior): Low Wood Block. [EN] GM drum note for weak beat (inner beat): Low Wood Block. */
    public static final int METRONOME_NOTE_WEAK   = 77; // Low Wood Block
    /** [CA] Velocitat MIDI per defecte (fortíssim). [EN] Default MIDI velocity (fortissimo). */
    public static final int DEFAULT_VELOCITY = 127;
    /** [CA] Instrument lead per defecte (Glockenspiel). [EN] Default lead instrument (Glockenspiel). */
    public static final int DEFAULT_LEAD_INSTRUMENT = 9; // Glokenspiel
    /** [CA] Instrument d'acords per defecte (Piano). [EN] Default chord instrument (Piano). */
    public static final int DEFAULT_CHORD_INSTRUMENT = 0; // Piano
    /** [CA] Número de programa MIDI del Glockenspiel. [EN] MIDI program number for Glockenspiel. */
    public static final int GLOCKENSPIEL = 9;
    /** [CA] Número de programa MIDI del Xilòfon. [EN] MIDI program number for Xylophone. */
    public static final int XYLOPHONE = 13;
    /** [CA] Nombre de ticks per quart (resolució MIDI estàndard). [EN] Ticks per quarter note (standard MIDI resolution). */
    public static final int DEFAULT_TICKS_PER_QUARTER = 480;


    /** [CA] Referència al controlador principal. [EN] Reference to the main controller. */
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

    /**
     * [CA] Reinicia tots els canals MIDI als seus valors per defecte. Els canals
     * de percussió (9) i de reserva (15) no es modifiquen. L'instrument per defecte
     * és el Glockenspiel si el dispositiu és un metal·lòfon, o el piano (0) en cas contrari.
     * <p>
     * [EN] Resets all MIDI channels to their default values. Percussion (9) and
     * reserved (15) channels are not modified. The default instrument is Glockenspiel
     * for metallophone devices, or piano (0) otherwise.
     */
    public static void resetChannels(){
        int defaultInstr = ToneRange.isMetallophone() ? GLOCKENSPIEL : 0;
        for (int i = 0; i < ntimesChannelIsUsed.length; i++) {
            if (i == 9 || i == 15) continue;
            ntimesChannelIsUsed[i] = 0;
            if (channelDataArray[i] != null) {
                channelDataArray[i].instrument = defaultInstr;
                channelDataArray[i].isActive = false;
                runProgramChange(i, defaultInstr);
            }
        }
    }

    /**
     * [CA] Retorna el primer canal MIDI lliure disponible (excloent els canals 9 i 15).
     * Si no queden canals lliures, mostra un diàleg a l'usuari per seleccionar quin
     * canal reutilitzar. Retorna {@code -1} si l'usuari cancel·la el diàleg.
     * <p>
     * [EN] Returns the first available free MIDI channel (excluding channels 9 and 15).
     * If no free channels remain, shows a dialog for the user to select which channel
     * to reuse. Returns {@code -1} if the user cancels the dialog.
     *
     * @return [CA] índex del canal disponible, o {@code -1} si l'usuari cancel·la /
     *         [EN] index of the available channel, or {@code -1} if the user cancels
     */
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

    /**
     * [CA] Retorna el número de programa MIDI de l'instrument lead actual.
     * <p>
     * [EN] Returns the MIDI program number of the current lead instrument.
     *
     * @return [CA] número de programa de l'instrument lead / [EN] lead instrument program number
     */
    public static int getLeadInstrument(){
        return leadInstrument;
    }

    /**
     * [CA] Retorna el número de programa MIDI de l'instrument d'acords actual.
     * <p>
     * [EN] Returns the MIDI program number of the current chord instrument.
     *
     * @return [CA] número de programa de l'instrument d'acords / [EN] chord instrument program number
     */
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

    /**
     * [CA] Dades internes d'un instrument MIDI (número, mnemònic i descripció).
     * Usada per al mapa d'instruments carregats del fitxer CSV.
     * <p>
     * [EN] Internal data for a MIDI instrument (number, mnemonic and description).
     * Used for the instrument map loaded from the CSV file.
     */
    static class InstrumentData {
        int instrumentNumber;
        String instrumentMnemonic;
        String instrumentDescription;

        /**
         * [CA] Crea un nou registre de dades d'instrument.
         * <p>
         * [EN] Creates a new instrument data record.
         *
         * @param number      [CA] número de programa MIDI / [EN] MIDI program number
         * @param mnemonic    [CA] mnemònic breu de l'instrument / [EN] short instrument mnemonic
         * @param description [CA] descripció llegible de l'instrument / [EN] human-readable instrument description
         */
        public InstrumentData(int number,String mnemonic,String description){
            this.instrumentNumber = number;
            this.instrumentMnemonic = mnemonic;
            this.instrumentDescription = description;
        }

        /**
         * [CA] Retorna una cadena amb el número, mnemònic i descripció de l'instrument.
         * <p>
         * [EN] Returns a string with the instrument number, mnemonic and description.
         *
         * @return [CA] representació textual de l'instrument / [EN] textual representation of the instrument
         */
        @Override
        public String toString(){
            return String.format("%3d",this.instrumentNumber)+" "+this.instrumentMnemonic+" "+this.instrumentDescription;
        }
    }

    /**
     * [CA] Dades internes d'un canal MIDI (canal, instrument assignat, estat actiu i identificador).
     * <p>
     * [EN] Internal data for a MIDI channel (channel, assigned instrument, active state and identifier).
     */
    static class ChannelData {
        MidiChannel midiChannel;
        int instrument;
        boolean isActive;
        int id;

        /**
         * [CA] Crea un nou registre de dades de canal MIDI.
         * <p>
         * [EN] Creates a new MIDI channel data record.
         *
         * @param id             [CA] identificador del canal (0–15) / [EN] channel identifier (0–15)
         * @param midiChannel    [CA] objecte canal MIDI del sintetitzador / [EN] MIDI channel object from synthesizer
         * @param instrumentNumber [CA] número de programa MIDI assignat / [EN] assigned MIDI program number
         * @param isActive       [CA] indica si el canal és actiu / [EN] indicates whether the channel is active
         */
        public ChannelData(int id, MidiChannel midiChannel, int instrumentNumber, boolean isActive){
            this.midiChannel = midiChannel;
            this.instrument = instrumentNumber;
            this.isActive = isActive;
            this.id = id;
        }
    }

    /**
     * [CA] Inicialitza el sintetitzador MIDI del sistema: obre el sintetitzador,
     * configura els canals, carrega la taula d'instruments des del CSV i llegeix
     * els instruments lead i chord de la configuració de l'aplicació.
     * Cal cridar aquest mètode una sola vegada en arrencar l'aplicació.
     * <p>
     * [EN] Initializes the system MIDI synthesizer: opens the synthesizer,
     * configures channels, loads the instrument table from CSV and reads
     * the lead and chord instruments from application configuration.
     * This method must be called once at application startup.
     *
     * @param controller [CA] referència al controlador principal / [EN] reference to the main controller
     */
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

    /**
     * [CA] Toca un tic de metrònom en el canal reservat. Envia noteOn i programa
     * un noteOff 50 ms després en un fil dimoni per no bloquejar el fil de reproducció.
     * <p>
     * [EN] Plays a metronome tick on the reserved channel. Sends noteOn and schedules
     * a noteOff 50 ms later on a daemon thread to avoid blocking the playback thread.
     *
     * @param midi [CA] nota MIDI a tocar / [EN] MIDI note to play
     * @param vel  [CA] velocitat MIDI (0–127) / [EN] MIDI velocity (0–127)
     */
    public static void playMetronomeTick(int midi, int vel) {
        if (channelDataArray[METRONOME_CHANNEL] == null) return;
        channelDataArray[METRONOME_CHANNEL].midiChannel.noteOn(midi, vel);
        Thread t = new Thread(() -> {
            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            channelDataArray[METRONOME_CHANNEL].midiChannel.noteOff(midi);
        });
        t.setDaemon(true);
        t.start();
    }

    /**
     * [CA] Carrega la taula d'instruments MIDI des d'un fitxer CSV de recursos.
     * Cada línia del fitxer té el format: {@code número;mnemònic;descripció}.
     * <p>
     * [EN] Loads the MIDI instrument table from a resource CSV file.
     * Each line has the format: {@code number;mnemonic;description}.
     *
     * @param fileName [CA] ruta relativa al fitxer de recursos / [EN] relative path to the resource file
     * @throws IOException           [CA] si el fitxer no es pot llegir / [EN] if the file cannot be read
     * @throws FileNotFoundException [CA] si el recurs no existeix al classpath / [EN] if the resource does not exist on the classpath
     */
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

    /**
     * [CA] Retorna el mnemònic de l'instrument corresponent al número de programa MIDI indicat.
     * <p>
     * [EN] Returns the mnemonic of the instrument corresponding to the given MIDI program number.
     *
     * @param instr [CA] número de programa MIDI (0–127) / [EN] MIDI program number (0–127)
     * @return [CA] cadena mnemònica de l'instrument / [EN] instrument mnemonic string
     * @throws InvalidParameterException [CA] si el número d'instrument és fora de rang /
     *                                   [EN] if the instrument number is out of range
     */
    public static String getInstrumentMnemonic(int instr){
        if (instr<0 || instr>=instruments.size()) throw new InvalidParameterException("Wrong instrument number: "+instr);
        return instruments.get(instr).instrumentMnemonic;
    }

    /**
     * [CA] Retorna el número de programa MIDI a partir del mnemònic de l'instrument.
     * La cerca no distingeix entre majúscules i minúscules. Retorna {@code -1} si
     * no es troba cap instrument amb aquest mnemònic.
     * <p>
     * [EN] Returns the MIDI program number for the given instrument mnemonic.
     * The search is case-insensitive. Returns {@code -1} if no instrument
     * with that mnemonic is found.
     *
     * @param mnemonic [CA] mnemònic de l'instrument / [EN] instrument mnemonic
     * @return [CA] número de programa MIDI, o {@code -1} si no es troba /
     *         [EN] MIDI program number, or {@code -1} if not found
     */
    public static int getInstrumentNumber(String mnemonic){
        int i=0;
        for (InstrumentData data:instruments.values()){
            if (data.instrumentMnemonic.equalsIgnoreCase(mnemonic)) return i;
            i++;
        }
        return -1;
    }

    /**
     * [CA] Retorna la velocitat MIDI actual del teclat de pantalla.
     * <p>
     * [EN] Returns the current MIDI velocity of the on-screen keyboard.
     *
     * @return [CA] velocitat MIDI actual (0–127) / [EN] current MIDI velocity (0–127)
     */
    public static int getCurrentKeyboardVelocity() {
        return currentVelocity;
    }

    /**
     * [CA] Retorna la llista de tots els instruments MIDI disponibles com a cadenes
     * de text amb el format {@code "NNN mnemònic descripció"}.
     * <p>
     * [EN] Returns the list of all available MIDI instruments as strings
     * in the format {@code "NNN mnemonic description"}.
     *
     * @return [CA] llista de cadenes descriptives d'instruments / [EN] list of instrument description strings
     */
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
    /**
     * [CA] Retorna el número de programa MIDI de l'instrument assignat al canal indicat.
     * <p>
     * [EN] Returns the MIDI program number of the instrument assigned to the given channel.
     *
     * @param channel [CA] índex del canal MIDI (0–15) / [EN] MIDI channel index (0–15)
     * @return [CA] número de programa de l'instrument assignat / [EN] assigned instrument program number
     */
    public static int getInstOnChannel(int channel){
        return channelDataArray[channel].instrument;
    }

    /**
     * [CA] Retorna el nombre total de canals MIDI disponibles (sempre 16).
     * <p>
     * [EN] Returns the total number of available MIDI channels (always 16).
     *
     * @return [CA] nombre de canals MIDI / [EN] number of MIDI channels
     */
    public static int numUsedChannels(){
        return channelDataArray.length;
    }

    /**
     * [CA] Retorna el conjunt d'identificadors dels canals MIDI que estan
     * marcats com a actius.
     * <p>
     * [EN] Returns the set of MIDI channel identifiers that are marked as active.
     *
     * @return [CA] conjunt d'índexs de canals actius / [EN] set of active channel indices
     */
    public static Set<Integer> activeChannels(){
        Set<Integer> achans = new HashSet();
        for (ChannelData channel:channelDataArray){
            if (channel.isActive){
                achans.add(channel.id);
            }
        }
        return achans;
    }

    /**
     * [CA] Assigna un instrument MIDI a un canal i el marca com a actiu.
     * No envia el canvi de programa al sintetitzador (cal cridar {@link #runProgramChange}
     * separadament per enviar el missatge MIDI).
     * <p>
     * [EN] Assigns a MIDI instrument to a channel and marks it as active.
     * Does not send the program change to the synthesizer (call {@link #runProgramChange}
     * separately to send the MIDI message).
     *
     * @param channel          [CA] índex del canal MIDI (0–15) / [EN] MIDI channel index (0–15)
     * @param instrumentNumber [CA] número de programa MIDI de l'instrument / [EN] MIDI program number of the instrument
     * @throws IllegalArgumentException [CA] si el canal és fora de rang / [EN] if the channel is out of range
     */
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

    /**
     * [CA] Retorna el número de programa MIDI de l'instrument assignat al canal indicat,
     * amb validació del rang del canal.
     * <p>
     * [EN] Returns the MIDI program number of the instrument assigned to the given channel,
     * with channel range validation.
     *
     * @param channel [CA] índex del canal MIDI (0–15) / [EN] MIDI channel index (0–15)
     * @return [CA] número de programa de l'instrument / [EN] instrument program number
     * @throws IllegalArgumentException [CA] si el canal és fora de rang / [EN] if the channel is out of range
     */
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
    /**
     * [CA] Inicia la reproducció d'una nota MIDI en el canal i volum indicats.
     * <p>
     * [EN] Starts playback of a MIDI note on the given channel and volume.
     *
     * @param midi    [CA] nota MIDI a reproduir (21–108) / [EN] MIDI note to play (21–108)
     * @param channel [CA] índex del canal MIDI (0–15) / [EN] MIDI channel index (0–15)
     * @param vol     [CA] velocitat/volum MIDI (0–127) / [EN] MIDI velocity/volume (0–127)
     * @throws IllegalArgumentException [CA] si el canal és invàlid o inactiu / [EN] if the channel is invalid or inactive
     */
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
    /**
     * [CA] Atura la reproducció d'una nota MIDI en el canal indicat.
     * <p>
     * [EN] Stops playback of a MIDI note on the given channel.
     *
     * @param midi    [CA] nota MIDI a aturar / [EN] MIDI note to stop
     * @param channel [CA] índex del canal MIDI (0–15) / [EN] MIDI channel index (0–15)
     * @throws IllegalArgumentException [CA] si el canal és invàlid o inactiu / [EN] if the channel is invalid or inactive
     */
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
    /**
     * [CA] Crea un missatge MIDI de text (MetaMessage tipus 0x01) amb el contingut indicat.
     * S'usa per incrustar missatges de text en la seqüència MIDI de la partitura.
     * <p>
     * [EN] Creates a MIDI text message (MetaMessage type 0x01) with the given content.
     * Used to embed text messages in the score's MIDI sequence.
     *
     * @param text [CA] text a incrustar al missatge MIDI / [EN] text to embed in the MIDI message
     * @return [CA] el MetaMessage creat / [EN] the created MetaMessage
     */
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

    /**
     * [CA] Comprova si el missatge MIDI és un event de text (MetaMessage tipus 0x01)
     * i, si és així, l'interpreta i executa l'acció associada. Retorna {@code true}
     * si el missatge era un event de text (processat o no), {@code false} en cas contrari.
     * <p>
     * [EN] Checks whether the MIDI message is a text event (MetaMessage type 0x01)
     * and, if so, interprets and executes the associated action. Returns {@code true}
     * if the message was a text event (processed or not), {@code false} otherwise.
     *
     * @param message [CA] missatge MIDI a comprovar / [EN] MIDI message to check
     * @return [CA] {@code true} si era un event de text / [EN] {@code true} if it was a text event
     */
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

    /**
     * [CA] Envia un missatge de canvi de programa (Program Change) MIDI al sintetitzador
     * per al canal i instrument indicats. Usa el receptor del sintetitzador obert
     * durant la inicialització.
     * <p>
     * [EN] Sends a MIDI Program Change message to the synthesizer for the given
     * channel and instrument. Uses the receiver of the synthesizer opened
     * during initialization.
     *
     * @param channel  [CA] índex del canal MIDI (0–15) / [EN] MIDI channel index (0–15)
     * @param newInstr [CA] número de programa MIDI del nou instrument (0–127) /
     *                 [EN] MIDI program number of the new instrument (0–127)
     * @throws IllegalArgumentException [CA] si les dades MIDI no són vàlides /
     *                                  [EN] if the MIDI data is invalid
     */
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

    /**
     * [CA] Retorna una representació hexadecimal dels bytes d'un missatge MIDI,
     * incloent informació addicional per als MetaMessages Sequencer Specific (0x7F).
     * Útil per a depuració.
     * <p>
     * [EN] Returns a hexadecimal representation of the bytes of a MIDI message,
     * including additional information for Sequencer Specific MetaMessages (0x7F).
     * Useful for debugging.
     *
     * @param msg [CA] missatge MIDI a mostrar, o {@code null} /
     *            [EN] MIDI message to display, or {@code null}
     * @return [CA] cadena hexadecimal amb els bytes del missatge /
     *         [EN] hexadecimal string of the message bytes
     */
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
						.replace(" ", "")
						.replace("\r", "");
				extra = "  | Meta 0x7F ASCII=\"" + ascii + "\"";
			}
		}

		return hex + extra;
	}

    /**
     * [CA] Interpreta i executa un missatge MIDI durant la reproducció de la partitura.
     * Gestiona: events de text, MetaMessages de tempo (0x51), signatura de temps (0x58),
     * signatura de to (0x59), i ShortMessages de control (CONTROL_CHANGE, PROGRAM_CHANGE).
     * Els events NOTE_ON i NOTE_OFF s'ignoren (ja gestionats per la seqüència MIDI).
     * <p>
     * [EN] Interprets and executes a MIDI message during score playback.
     * Handles: text events, tempo MetaMessages (0x51), time signature (0x58),
     * key signature (0x59), and control ShortMessages (CONTROL_CHANGE, PROGRAM_CHANGE).
     * NOTE_ON and NOTE_OFF events are ignored (already handled by the MIDI sequence).
     *
     * @param message [CA] missatge MIDI a processar, o {@code null} (no fa res) /
     *                [EN] MIDI message to process, or {@code null} (does nothing)
     * @param score   [CA] la graella de partitura activa (per actualitzar compàs i to) /
     *                [EN] the active score grid (to update time and key signature)
     */
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
