package dodecagraphone.model.sound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

/**
 * La versió original d'aquesta classe està al projecte Afinador.
 * Aquesta classe gestiona una line in (micro), line out (speakers),
 * source audio file, target audio file, i gestio de l'array de so 
 * (currentAudio). Basada en Javax.sound.sampled.
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

    public static AudioManager getAudioManager() {
        if (firstTime) {
            firstTime = false;
            return new AudioManager();
        }
        return null;
    }

    public void closeAudioManager() {
        currentAudio = null;
    }

    //----------------- LineIn (microphone) ------------------------------------
    public void openLineIn() {
        lineIn = null;
        try {
            lineIn = findMicrophoneLine(); // Busca el microfon
            lineIn.open(audioFormat);
        } catch (LineUnavailableException ex) {
            System.err.println("AudioManager.openLineIn(): LineUnavailableException, " + ex.getMessage());
        }
    }

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

    public void captureNPlay() throws Exception {
        throw new Exception("Method not ready!");
    }

    public void closeLineIn() {
        if (lineIn != null) {
            lineIn.close();
        }
    }

    //----------------- LineOut (speaker) ------------------------------------
    public void openLineOut() {
        lineOut = null;
        try {
            lineOut = findSpeakerLine(); // Busca el microfon
            lineOut.open(audioFormat);
        } catch (LineUnavailableException ex) {
            System.err.println("LineUnavailableException " + ex.getMessage());
        }
    }

    public void stopAudio() {
        isPlaying=false;
        lineOut.stop();
        // System.err.println("AudioManager.stopAudio()");
    }

    public int playAudio() {
//        lineOut.start();
        Play pl = new Play();
        isPlaying=true;
        pl.start();
//        lineOut.stop();
//        lineOut.start();
//        ByteArrayInputStream bais = new ByteArrayInputStream(currentAudio);
//        byte[] buf = new byte[(int) BUFFER_LENGTH * frameSize];
//        int len;
//        int nWritten = 0;
//        int nRead=0;
//        while (((len = bais.read(buf, 0, buf.length)) != -1)) {
//            nRead+=len;
//            nWritten += lineOut.write(buf, 0, len);
//        }
//        int pending=(int)(currentAudio.length)-nWritten;
//        buf = null;
//        lineOut.stop();
//        try {
//            bais.close();
//            if (nWritten!=(currentAudio.length)||nRead!=(currentAudio.length)){
//                throw new IOException("AudioManager.playAudio(): Bytes read="+nRead+" or Bytes written="+
//                        nWritten+" != currentAudioLength="+(currentAudio.length)+" (pending="+pending+")");
//            }
//        } catch (IOException ex) {
//            System.err.println("IOException " + ex.getMessage());
//        }
        return 0; //nWritten / frameSize;
    }

    public void closeLineOut() {
        if (lineOut != null) {
            lineOut.close();
        }
    }

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
    public void openSourceAudioFile(String fileName) {
        File sourceFile = new File(fileName);
        try {
            audioInputStream = AudioSystem.getAudioInputStream(sourceFile);
        } catch (UnsupportedAudioFileException | IOException ex) {
            System.err.println("AudioManager.openAudioFile(): " + ex.getMessage());
        }
        this.fileLength = sourceFile.length();
    }

    public long getFileLength() {
        return this.fileLength / this.frameSize;
    }

    /**
     * Llegeix tot el fitxer
     *
     * @return
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
     * Llegeix numFrames frames a partir d'offset. Retorna els no de frames
     * llegits. Un frame es un sample de cada canal
     *
     * @param offset posicio al fitxer del primer frame a llegir
     * @param numFrames nombre de frames a llegir
     * @return nombre de frames llegits
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

    public int writeAudioFile() {
        int nbWritten = 0;
        try {
            nbWritten = AudioSystem.write(ais, AudioManager.FILE_TYPE, targetFile);
        } catch (IOException ex) {
            System.err.println("IOException " + ex.getMessage());
        }
        return nbWritten / frameSize;
    }

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
    public void clearAudio() {
        currentAudio = null;
    }

    public int audioSize() {
        if (currentAudio == null) {
            return 0;
        }
        return currentAudio.length / frameSize;
    }

    public long getCurrentAudioLength() {
        return currentAudio.length;
    }

    public byte[] getCurrentAudio() {
        return currentAudio;
    }

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

    public void setCurrentAudio(byte[] currentAudio) {
        this.currentAudio = currentAudio;
    }

    public void setCurrentAudio(double[] audio) {
        this.currentAudio = new byte[audio.length];
        int i = 0;
        for (double da : audio) {
            currentAudio[i] = (byte) da;
            i++;
        }
    }

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
