/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.sound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

/**
 * [CA] Gestiona les línies d'entrada (micròfon) i sortida (altaveus) d'àudio del sistema,
 * els fitxers d'àudio font i destí, i l'array de bytes d'àudio actual ({@code currentAudio}).
 * Basada en {@code javax.sound.sampled}. La versió original prové del projecte Afinador.
 * Singleton: només es pot obtenir una instància via {@link #getAudioManager()}.
 * <p>
 * [EN] Manages the system audio input (microphone) and output (speakers) lines,
 * source and target audio files, and the current audio byte array ({@code currentAudio}).
 * Based on {@code javax.sound.sampled}. The original version comes from the Afinador project.
 * Singleton: only one instance can be obtained via {@link #getAudioManager()}.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class AudioManager {

    public static final float FS = 22050; // sample rate
    private static final int BUFFER_LENGTH = 16 * 1024; // En samples. Cada frame són frameSize bytes
    private static final int SSIZE = 16; // sample size
    private static final AudioFormat audioFormat = new AudioFormat(FS, SSIZE, 2, true, false); // pcm
    private static final int frameSize = audioFormat.getFrameSize(); // = 4 -> 2 channels x 2 bytes per sample
    private static final AudioFileFormat.Type FILE_TYPE = AudioFileFormat.Type.WAVE;
    private TargetDataLine lineIn = null;
    private SourceDataLine lineOut = null;
    private AudioInputStream audioInputStream = null;
    private long fileLength = 0;
    private File targetFile = null;
    private AudioInputStream ais = null;
    // Producers (capture, read) leave data and consumers (write, play) take it
    private boolean isPlaying = false;
    private byte[] currentAudio;

    private static boolean firstTime = true;

    //------------------ AudioManager -----------
    private AudioManager() {
    }

    /**
     * [CA] Retorna la instància única de {@code AudioManager} (singleton).
     * La primera crida retorna una nova instància; les crides posteriors retornen {@code null}.
     * <p>
     * [EN] Returns the unique instance of {@code AudioManager} (singleton).
     * The first call returns a new instance; subsequent calls return {@code null}.
     *
     * @return [CA] la instància única, o {@code null} si ja s'ha creat / [EN] the unique instance, or {@code null} if already created
     */
    public static AudioManager getAudioManager() {
        if (firstTime) {
            firstTime = false;
            return new AudioManager();
        }
        return null;
    }

    /**
     * [CA] Allibera el buffer d'àudio actual (posa {@code currentAudio} a null).
     * <p>
     * [EN] Releases the current audio buffer (sets {@code currentAudio} to null).
     */
    public void closeAudioManager() {
        currentAudio = null;
    }

    //----------------- LineIn (microphone) ------------------------------------
    /**
     * [CA] Obre la línia d'entrada del micròfon i la configura amb el format d'àudio estàndard.
     * <p>
     * [EN] Opens the microphone input line and configures it with the standard audio format.
     */
    public void openLineIn() {
        lineIn = null;
        try {
            lineIn = findMicrophoneLine(); // Busca el microfon
            lineIn.open(audioFormat);
        } catch (LineUnavailableException ex) {
            System.err.println("AudioManager.openLineIn(): LineUnavailableException, " + ex.getMessage());
        }
    }

    /**
     * [CA] Captura exactament {@code nsamples} mostres des del micròfon i les desa a {@code currentAudio}.
     * <p>
     * [EN] Captures exactly {@code nsamples} samples from the microphone and stores them in {@code currentAudio}.
     *
     * @param nsamples [CA] nombre de mostres a capturar / [EN] number of samples to capture
     * @return [CA] nombre de frames llegits / [EN] number of frames read
     */
    public int captureAudioNSamples(int nsamples) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        lineIn.start();
        int nRead = 0;
        int bufSize = (int) BUFFER_LENGTH * frameSize;
        byte[] buf = new byte[bufSize];
        int pending = nsamples * frameSize;
        int len;
        while (pending > bufSize && ((len = lineIn.read(buf, 0, bufSize)) != -1)) {
            baos.write(buf, 0, len);
            nRead += len;
            pending -= len;
        }
        if (pending > 0 && ((len = lineIn.read(buf, 0, pending)) != -1)) {
            baos.write(buf, 0, len);
            nRead += len;
            pending -= len;
        }
        lineIn.stop();
        currentAudio = baos.toByteArray();
        try {
            baos.close();
            if (pending != 0) {
                throw new IOException("Ncaptured != nsamples");
            }
            if (nRead != currentAudio.length) {
                throw new IOException("Ncaptured != nsamples");
            }
        } catch (IOException ex) {
            System.err.println("IOException " + ex.getMessage());
        }
        return nRead / frameSize;
    }

    /**
     * [CA] Captura àudio des del micròfon durant la durada especificada en mil·lisegons.
     * <p>
     * [EN] Captures audio from the microphone for the specified duration in milliseconds.
     *
     * @param durationInMs [CA] durada de la captura en mil·lisegons / [EN] capture duration in milliseconds
     * @return [CA] nombre de frames llegits / [EN] number of frames read
     */
    public int captureAudioMsec(long durationInMs) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        lineIn.start();
        int nRead = 0;
        byte[] buf = new byte[(int) BUFFER_LENGTH * frameSize];
        long end = System.currentTimeMillis() + durationInMs;
        int len;
        while (System.currentTimeMillis() < end && ((len = lineIn.read(buf, 0, buf.length)) != -1)) {
            baos.write(buf, 0, len);
            nRead += len;
        }
        lineIn.stop();
        currentAudio = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException ex) {
            System.err.println("IOException " + ex.getMessage());
        }
        return nRead / frameSize;
    }

    /**
     * [CA] Mètode no implementat. Llença una excepció si s'invoca.
     * <p>
     * [EN] Unimplemented method. Throws an exception if invoked.
     *
     * @throws Exception [CA] sempre, perquè el mètode no està llest / [EN] always, because the method is not ready
     */
    public void captureNPlay() throws Exception {
        throw new Exception("Method not ready!");
    }

    /**
     * [CA] Tanca la línia d'entrada del micròfon si està oberta.
     * <p>
     * [EN] Closes the microphone input line if it is open.
     */
    public void closeLineIn() {
        if (lineIn != null) {
            lineIn.close();
        }
    }

    //----------------- LineOut (speaker) ------------------------------------
    /**
     * [CA] Obre la línia de sortida dels altaveus i la configura amb el format d'àudio estàndard.
     * <p>
     * [EN] Opens the speaker output line and configures it with the standard audio format.
     */
    public void openLineOut() {
        lineOut = null;
        try {
            lineOut = findSpeakerLine(); // Busca el microfon
            lineOut.open(audioFormat);
        } catch (LineUnavailableException ex) {
            System.err.println("LineUnavailableException " + ex.getMessage());
        }
    }

    /**
     * [CA] Atura la reproducció d'àudio en curs.
     * <p>
     * [EN] Stops the ongoing audio playback.
     */
    public void stopAudio() {
        isPlaying=false;
        lineOut.stop();
        // System.err.println("AudioManager.stopAudio()");
    }

    /**
     * [CA] Inicia la reproducció de {@code currentAudio} en un fil separat (bucle mentre {@code isPlaying}).
     * <p>
     * [EN] Starts playback of {@code currentAudio} in a separate thread (loops while {@code isPlaying}).
     *
     * @return [CA] sempre 0 (valor de retorn reservat) / [EN] always 0 (reserved return value)
     */
    public int playAudio() {
//        lineOut.start();
        Play pl = new Play();
        isPlaying=true;
        pl.start();
        return 0; //nWritten / frameSize;
    }

    /**
     * [CA] Tanca la línia de sortida dels altaveus si està oberta.
     * <p>
     * [EN] Closes the speaker output line if it is open.
     */
    public void closeLineOut() {
        if (lineOut != null) {
            lineOut.close();
        }
    }

    /**
     * [CA] Fil intern de reproducció: reprodueix {@code currentAudio} en bucle mentre {@code isPlaying}.
     * <p>
     * [EN] Internal playback thread: plays {@code currentAudio} in a loop while {@code isPlaying}.
     */
    class Play extends Thread {

        @Override
        public void run() {
            while (isPlaying) {
                lineOut.start();
                ByteArrayInputStream bais = new ByteArrayInputStream(currentAudio);
                byte[] buf = new byte[(int) BUFFER_LENGTH * frameSize];
                int len;
                int nWritten = 0;
                int nRead = 0;
                while (((len = bais.read(buf, 0, buf.length)) != -1)) {
                    nRead += len;
                    nWritten += lineOut.write(buf, 0, len);
                }
                int pending = (int) (currentAudio.length) - nWritten;
                buf = null;
                lineOut.flush();
                //lineOut.stop();
                try {
                    bais.close();
                    if (nWritten != (currentAudio.length) || nRead != (currentAudio.length)) {
                        throw new IOException("AudioManager.playAudio(): Bytes read=" + nRead + " or Bytes written="
                                + nWritten + " != currentAudioLength=" + (currentAudio.length) + " (pending=" + pending + ")");
                    }
                } catch (IOException ex) {
                    // System.err.println("IOException " + ex.getMessage());
                }
            }
        }

    }

    //----------------- SourceAudioFile ------------------------------------
    /**
     * [CA] Obre un fitxer d'àudio font per a lectura posterior.
     * <p>
     * [EN] Opens a source audio file for subsequent reading.
     *
     * @param fileName [CA] ruta del fitxer d'àudio / [EN] audio file path
     */
    public void openSourceAudioFile(String fileName) {
        File sourceFile = new File(fileName);
        try {
            audioInputStream = AudioSystem.getAudioInputStream(sourceFile);
        } catch (UnsupportedAudioFileException | IOException ex) {
            System.err.println("AudioManager.openAudioFile(): " + ex.getMessage());
        }
        this.fileLength = sourceFile.length();
    }

    /**
     * [CA] Retorna la longitud del fitxer font en frames.
     * <p>
     * [EN] Returns the length of the source file in frames.
     *
     * @return [CA] longitud en frames / [EN] length in frames
     */
    public long getFileLength() {
        return this.fileLength / this.frameSize;
    }

    /**
     * [CA] Llegeix tot el fitxer d'àudio font i el desa a {@code currentAudio}.
     * <p>
     * [EN] Reads the entire source audio file and stores it in {@code currentAudio}.
     *
     * @return [CA] nombre de frames llegits / [EN] number of frames read
     */
    public int readAudioFile() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int nBufferSize = BUFFER_LENGTH * frameSize;
        byte[] abBuffer = new byte[nBufferSize];
        int nBytesRead = 0;
        int totalRead = 0;
        while (true) {
            try {
                nBytesRead = audioInputStream.read(abBuffer);
            } catch (IOException ex) {
                System.err.println("AudioManager.readAudioFile(): " + ex.getMessage());
            }

            if (nBytesRead == -1) {
                break;
            }
            baos.write(abBuffer, 0, nBytesRead);
            totalRead += nBytesRead;
        }
        currentAudio = baos.toByteArray();
        int nRead = currentAudio.length / frameSize;
        try {
            baos.close();
            if (totalRead != currentAudio.length && totalRead != this.fileLength) {
                throw new IOException("AudioManager.readAudioFile(): nRead != length current audio");
            }
        } catch (IOException ex) {
            System.err.println("IOException " + ex.getMessage());
        }
        return nRead;
    }

    /**
     * [CA] Llegeix {@code numFrames} frames a partir de l'offset donat i els desa a {@code currentAudio}.
     * Un frame és una mostra de cada canal.
     * <p>
     * [EN] Reads {@code numFrames} frames starting from the given offset and stores them in {@code currentAudio}.
     * A frame is one sample per channel.
     *
     * @param offset    [CA] posició al fitxer del primer frame a llegir / [EN] file position of the first frame to read
     * @param numFrames [CA] nombre de frames a llegir / [EN] number of frames to read
     * @return [CA] nombre de frames llegits / [EN] number of frames read
     */
    public int readAudioFile(int offset, int numFrames) {
        // To check, petava despres de 5 o 6 frames. Anyway, potser millor no oferir aquest metode
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int nBufferSize = BUFFER_LENGTH * frameSize;
        if (BUFFER_LENGTH > numFrames) {
            nBufferSize = numFrames * frameSize;
        }
        byte[] abBuffer = new byte[nBufferSize];
        if (offset > 0) {
            // llegeix i se salta fins offset
            int pendents = offset * frameSize;
            int totalRead = 0;
            while (pendents > 0) {
                int nBytesRead = 0;
                int nbToRead = 0;
                if (pendents > nBufferSize) {
                    nbToRead = nBufferSize;
                } else {
                    nbToRead = pendents;
                }
                try {
                    nBytesRead = audioInputStream.read(abBuffer, 0, nbToRead);
                } catch (IOException ex) {
                    System.err.println("AudioManager.openAudioFile(): " + ex.getMessage());
                }

                if (nBytesRead == -1) {
                    break;
                }
                totalRead += nBytesRead;
                pendents -= nBytesRead;
            }
        }
        int pendents = numFrames * frameSize;
        int totalRead = 0;
        while (pendents > 0) {
            int nBytesRead = 0;
            int nbToRead = 0;
            if (pendents > nBufferSize) {
                nbToRead = nBufferSize;
            } else {
                nbToRead = pendents;
            }
            try {
                nBytesRead = audioInputStream.read(abBuffer, 0, nbToRead);
            } catch (IOException ex) {
                System.err.println("AudioManager.openAudioFile(): " + ex.getMessage());
            }

            if (nBytesRead == -1) {
                break;
            }
            baos.write(abBuffer, 0, nBytesRead);
            totalRead += nBytesRead;
            pendents -= nBytesRead;
        }
        currentAudio = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException ex) {
            System.err.println("IOException " + ex.getMessage());
        }
        return totalRead / frameSize;
    }

    /**
     * [CA] Tanca el flux d'entrada del fitxer d'àudio font.
     * <p>
     * [EN] Closes the input stream of the source audio file.
     */
    public void closeSourceAudioFile() {
        try {
            if (audioInputStream != null) {
                audioInputStream.close();
            }
        } catch (IOException ex) {
            System.err.println("IOException " + ex.getMessage());
        }
    }

    //----------------- TargetAudioFile ------------------------------------
    /**
     * [CA] Prepara un fitxer destí per a l'escriptura de {@code currentAudio} com a WAV.
     * <p>
     * [EN] Prepares a target file for writing {@code currentAudio} as WAV.
     *
     * @param fileName [CA] ruta del fitxer destí / [EN] target file path
     */
    public void openTargetAudioFile(String fileName) {
        if (currentAudio != null) {
            if (currentAudio.length > 0) {
                targetFile = new File(fileName);
                ByteArrayInputStream bais = new ByteArrayInputStream(currentAudio);
                ais = new AudioInputStream(bais, audioFormat,
                        currentAudio.length / frameSize);
                try {
                    bais.close();
                } catch (IOException ex) {
                    System.err.println("IOException " + ex.getMessage());
                }
            }
        }
    }

    /**
     * [CA] Escriu {@code currentAudio} al fitxer destí en format WAV.
     * <p>
     * [EN] Writes {@code currentAudio} to the target file in WAV format.
     *
     * @return [CA] nombre de frames escrits / [EN] number of frames written
     */
    public int writeAudioFile() {
        int nbWritten = 0;
        try {
            nbWritten = AudioSystem.write(ais, AudioManager.FILE_TYPE, targetFile);
        } catch (IOException ex) {
            System.err.println("IOException " + ex.getMessage());
        }
        return nbWritten / frameSize;
    }

    /**
     * [CA] Tanca el flux de sortida del fitxer d'àudio destí.
     * <p>
     * [EN] Closes the output stream of the target audio file.
     */
    public void closeTargetAudioFile() {
        try {
            if (ais != null) {
                ais.close();
            }
        } catch (IOException ex) {
            System.err.println("IOException " + ex.getMessage());
        }
    }

    //---------------- Managing the audio array, currentAudio --------------
    /**
     * [CA] Esborra el buffer d'àudio actual (posa {@code currentAudio} a null).
     * <p>
     * [EN] Clears the current audio buffer (sets {@code currentAudio} to null).
     */
    public void clearAudio() {
        currentAudio = null;
    }

    /**
     * [CA] Retorna la mida de l'àudio actual en frames.
     * <p>
     * [EN] Returns the size of the current audio in frames.
     *
     * @return [CA] nombre de frames, o 0 si no hi ha àudio / [EN] number of frames, or 0 if no audio
     */
    public int audioSize() {
        if (currentAudio == null) {
            return 0;
        }
        return currentAudio.length / frameSize;
    }

    /**
     * [CA] Retorna la longitud en bytes de l'àudio actual.
     * <p>
     * [EN] Returns the length in bytes of the current audio.
     *
     * @return [CA] longitud en bytes / [EN] length in bytes
     */
    public long getCurrentAudioLength() {
        return currentAudio.length;
    }

    /**
     * [CA] Retorna el buffer d'àudio actual com a array de bytes.
     * <p>
     * [EN] Returns the current audio buffer as a byte array.
     *
     * @return [CA] array de bytes d'àudio / [EN] audio byte array
     */
    public byte[] getCurrentAudio() {
        return currentAudio;
    }

    /**
     * [CA] Extreu el canal esquerre de {@code currentAudio} com a array de doubles.
     * <p>
     * [EN] Extracts the left channel from {@code currentAudio} as a double array.
     *
     * @return [CA] mostres del canal esquerre / [EN] left channel samples
     */
    public double[] getLeftChannel() {
        double[] left = new double[currentAudio.length / 4];
        int litEnd, bigEnd;
        for (int i = 0, j = 0; i < currentAudio.length; i += 4, j++) {
            litEnd = currentAudio[i];
            if (litEnd < 0) {
                litEnd += 256;
            }
            bigEnd = currentAudio[i + 1];
            left[j] = bigEnd * 256 + litEnd;
        }
        return left;
    }

    /**
     * [CA] Extreu el canal dret de {@code currentAudio} com a array de doubles.
     * <p>
     * [EN] Extracts the right channel from {@code currentAudio} as a double array.
     *
     * @return [CA] mostres del canal dret / [EN] right channel samples
     */
    public double[] getRightChannel() {
        double[] right = new double[currentAudio.length / 4];
        int litEnd, bigEnd;
        for (int i = 0, j = 0; i < currentAudio.length; i += 4, j++) {
            litEnd = currentAudio[i + 2];
            if (litEnd < 0) {
                litEnd += 256;
            }
            bigEnd = currentAudio[i + 3];
            right[j] = bigEnd * 256 + litEnd;
        }
        return right;
    }

    /**
     * [CA] Estableix ambdós canals de {@code currentAudio} a partir d'arrays de doubles.
     * <p>
     * [EN] Sets both channels of {@code currentAudio} from double arrays.
     *
     * @param left  [CA] mostres del canal esquerre / [EN] left channel samples
     * @param right [CA] mostres del canal dret / [EN] right channel samples
     */
    public void setBothChannels(double[] left, double[] right) {
        currentAudio = new byte[left.length * 4];
        int valInt, litEnd, bigEnd;
        for (int j = 0, i = 0; j < left.length; j++, i += 4) {
            valInt = (int) left[j];
            bigEnd = valInt / 256;
            litEnd = valInt % 256;
            if (valInt < 0 && litEnd != 0) {
                bigEnd--;
            }
            currentAudio[i] = (byte) litEnd;
            currentAudio[i + 1] = (byte) bigEnd;

            valInt = (int) right[j];
            bigEnd = valInt / 256;
            litEnd = valInt % 256;
            if (valInt < 0 && litEnd != 0) {
                bigEnd--;
            }
            currentAudio[i + 2] = (byte) litEnd;
            currentAudio[i + 3] = (byte) bigEnd;
        }
    }

    /**
     * [CA] Estableix el canal esquerre de {@code currentAudio} (el canal dret queda a 0).
     * <p>
     * [EN] Sets the left channel of {@code currentAudio} (right channel set to 0).
     *
     * @param left [CA] mostres del canal esquerre / [EN] left channel samples
     */
    public void setLeft(double[] left) {
        currentAudio = new byte[left.length * 4];
        int valInt, litEnd, bigEnd;
        for (int j = 0, i = 0; j < left.length; j++, i += 4) {
            valInt = (int) left[j];
            bigEnd = valInt / 256;
            litEnd = valInt % 256;
            if (valInt < 0 && litEnd != 0) {
                bigEnd--;
            }
            currentAudio[i] = (byte) litEnd;
            currentAudio[i + 1] = (byte) bigEnd;

            currentAudio[i + 2] = 0;
            currentAudio[i + 3] = 0;
        }
    }

    /**
     * [CA] Estableix el canal dret de {@code currentAudio} (el canal esquerre queda a 0).
     * <p>
     * [EN] Sets the right channel of {@code currentAudio} (left channel set to 0).
     *
     * @param right [CA] mostres del canal dret / [EN] right channel samples
     */
    public void setRight(double[] right) {
        currentAudio = new byte[right.length * 4];
        int valInt, litEnd, bigEnd;
        for (int j = 0, i = 0; j < right.length; j++, i += 4) {
            currentAudio[i] = 0;
            currentAudio[i + 1] = 0;

            valInt = (int) right[j];
            bigEnd = valInt / 256;
            litEnd = valInt % 256;
            if (valInt < 0 && litEnd != 0) {
                bigEnd--;
            }
            currentAudio[i + 2] = (byte) litEnd;
            currentAudio[i + 3] = (byte) bigEnd;
        }
    }

    /**
     * [CA] Estableix el buffer d'àudio actual des d'un array de bytes extern.
     * <p>
     * [EN] Sets the current audio buffer from an external byte array.
     *
     * @param currentAudio [CA] nou buffer d'àudio / [EN] new audio buffer
     */
    public void setCurrentAudio(byte[] currentAudio) {
        this.currentAudio = currentAudio;
    }

    /**
     * [CA] Estableix el buffer d'àudio actual convertint un array de doubles a bytes.
     * <p>
     * [EN] Sets the current audio buffer by converting a double array to bytes.
     *
     * @param audio [CA] array de mostres com a doubles / [EN] sample array as doubles
     */
    public void setCurrentAudio(double[] audio) {
        this.currentAudio = new byte[audio.length];
        int i = 0;
        for (double da : audio) {
            currentAudio[i] = (byte) da;
            i++;
        }
    }

    /**
     * [CA] Compara dos arrays de bytes per comprovar si són iguals (amb marge 0).
     * <p>
     * [EN] Compares two byte arrays to check whether they are equal (with margin 0).
     *
     * @param a [CA] primer array / [EN] first array
     * @param b [CA] segon array / [EN] second array
     * @return [CA] {@code true} si tots els elements coincideixen / [EN] {@code true} if all elements match
     */
    public static boolean byteArrayIguals(byte[] a, byte[] b) {
        int i = 0;
        byte marge = 0;
        for (byte ba : a) {
            if (ba > b[i] + marge || ba < b[i] - marge) {
                System.err.println("i-1=" + (i - 1) + ", a[i-1]=" + a[i - 1] + ", b[i-1]=" + b[i - 1]);
                System.err.println("i=" + i + ", a[i]=" + ba + ", b[i]=" + b[i]);
                break;
            }
            i++;
        }
        return i >= a.length;
    }

    // ---- Managing microphone and speaker lines from the system
    /**
     * [CA] Cerca i retorna la primera línia d'entrada de micròfon disponible al sistema.
     * <p>
     * [EN] Searches and returns the first available microphone input line on the system.
     *
     * @return [CA] la línia de micròfon trobada / [EN] the found microphone line
     * @throws LineUnavailableException [CA] si no es troba cap línia de micròfon / [EN] if no microphone line is found
     */
    public static TargetDataLine findMicrophoneLine() throws LineUnavailableException {
        // find a DataLine that can be read
        TargetDataLine line = null;
        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo1 : mixerInfo) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo1);
            Line.Info[] targetLineInfo = mixer.getTargetLineInfo();
            if (targetLineInfo.length > 0) {
                line = (TargetDataLine) mixer.getLine(targetLineInfo[0]);
                break;
            }
        }
        if (line == null) {
            throw new LineUnavailableException();
        }
        return line;
    }

    /**
     * [CA] Cerca i retorna la primera línia de sortida d'altaveu disponible al sistema.
     * <p>
     * [EN] Searches and returns the first available speaker output line on the system.
     *
     * @return [CA] la línia d'altaveu trobada / [EN] the found speaker line
     * @throws LineUnavailableException [CA] si no es troba cap línia d'altaveu / [EN] if no speaker line is found
     */
    public static SourceDataLine findSpeakerLine() throws LineUnavailableException {
        // find a DataLine that can be written into
        SourceDataLine line = null;

        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo1 : mixerInfo) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo1);
            Line.Info[] sourceLineInfo = mixer.getSourceLineInfo();
            if (sourceLineInfo.length > 0) {
                line = (SourceDataLine) mixer.getLine(sourceLineInfo[0]); // Agafa el primer port speaker
                break;
            }
        }
        if (line == null) {
            throw new LineUnavailableException();
        }
        return line;
    }

    /**
     * [CA] Enumera totes les línies d'àudio (entrada i sortida) disponibles al sistema per consola.
     * <p>
     * [EN] Enumerates all available audio lines (input and output) on the system to the console.
     *
     * @throws LineUnavailableException [CA] si hi ha un problema en accedir a les línies / [EN] if there is a problem accessing the lines
     */
    public static void enumerateLines() throws LineUnavailableException {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info info : mixerInfos) {
            Mixer m = AudioSystem.getMixer(info);
            Line.Info[] lineInfos = m.getSourceLineInfo();
            for (Line.Info lineInfo : lineInfos) {
                System.out.println(info.getName() + "---" + lineInfo);
                Line line = m.getLine(lineInfo);
                System.out.println("\t-----" + line);
            }
            lineInfos = m.getTargetLineInfo();
            for (Line.Info lineInfo : lineInfos) {
                System.out.println(m + "---" + lineInfo);
                Line line = m.getLine(lineInfo);
                System.out.println("\t-----" + line);
            }
        }
    }

}
