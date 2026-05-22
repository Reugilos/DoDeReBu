/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.sound;

import dodecagraphone.model.ToneRange;
import dodecagraphone.model.chord.Chord;
import java.util.HashMap;
import java.util.Map;

/**
 * [CA] Reproductor d'acords en segon pla durant la reproducció de la partitura.
 * Gestiona la reproducció contínua d'un acord harmònic mentre sonen les notes
 * de la melodia principal, usant MIDI o mostres de so segons la configuració.
 * Suporta pausa, represa i transposició de l'acord en temps real.
 * <p>
 * [EN] Background chord player used during score playback.
 * Manages the continuous playback of a harmonic chord while the main melody
 * notes sound, using either MIDI or audio samples depending on configuration.
 * Supports pause, resume and real-time transposition of the chord.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class BackgroundChordPlayer {

    private static final boolean LOCAL_VERBOSE = false;
    private final int CHORD_VOLUME = 30;
//    private boolean useChord;
    private boolean playing;
    private String soundInstrument;
    private int midiInstrument; // Reed organ
    private int chordVolume;
    private int chordChannel;
    private Chord currentChord, previousChord;
    private Map<Integer, Sound> sounds;
//    private int transpositionStep;
    private boolean paused;
    private boolean isOn;

    /**
     * [CA] Crea un nou reproductor d'acords en segon pla amb els valors per defecte.
     * Assigna l'instrument Reed Organ al canal MIDI dedicat als acords i inicialitza
     * l'estat intern com a aturat i actiu.
     * <p>
     * [EN] Creates a new background chord player with default values.
     * Assigns the Reed Organ instrument to the dedicated MIDI chord channel and
     * initializes the internal state as stopped and active.
     */
    public BackgroundChordPlayer() {
        playing = false;
        soundInstrument = "BflatClarinet";
        midiInstrument = 21; // Reed organ
        chordVolume = CHORD_VOLUME;
        chordChannel = 0;
        currentChord = null;
        previousChord = null;
        sounds = new HashMap<>();
        paused = false;
        SoundWithMidi.assignInstToChannel(chordChannel, midiInstrument);
        SoundWithMidi.runProgramChange(chordChannel,midiInstrument);
        isOn = true;
    }

    /**
     * [CA] Indica si el reproductor d'acords en segon pla està activat.
     * <p>
     * [EN] Returns whether the background chord player is enabled.
     *
     * @return {@code true} si el reproductor és actiu / {@code true} if the player is active
     */
    public boolean isBackgroundChordPlayerOn() {
        return isOn;
    }

    /**
     * [CA] Activa o desactiva el reproductor d'acords en segon pla.
     * <p>
     * [EN] Enables or disables the background chord player.
     *
     * @param isOn [CA] {@code true} per activar, {@code false} per desactivar /
     *             [EN] {@code true} to enable, {@code false} to disable
     */
    public void setBackgroundChordPlayer(boolean isOn) {
        this.isOn = isOn;
    }

    /**
     * [CA] Atura i reinicia l'estat del reproductor. Esborra l'acord actual,
     * l'anterior i tots els sons actius.
     * <p>
     * [EN] Stops and resets the player state. Clears the current chord,
     * the previous chord and all active sounds.
     */
    public void clear() {
        playing = false;
        paused = false;
        currentChord = null;
        previousChord = null;
        sounds.clear();
        //       transpositionStep = 0;
    }

    /**
     * [CA] Indica si el reproductor està reproduint un acord en aquest moment.
     * <p>
     * [EN] Returns whether the player is currently playing a chord.
     *
     * @return {@code true} si hi ha reproducció activa / {@code true} if playback is active
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * [CA] Estableix l'estat de reproducció del reproductor.
     * <p>
     * [EN] Sets the playing state of the player.
     *
     * @param playing [CA] {@code true} per marcar com a reproduint /
     *                [EN] {@code true} to mark as playing
     */
    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    /**
     * [CA] Indica si el reproductor està en pausa.
     * <p>
     * [EN] Returns whether the player is paused.
     *
     * @return {@code true} si el reproductor és en pausa / {@code true} if the player is paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * [CA] Posa el reproductor en pausa. Si estava reproduint, atura les notes
     * actuals i marca l'estat com a pausat.
     * <p>
     * [EN] Pauses the player. If it was playing, stops the current notes
     * and marks the state as paused.
     */
    public void pause() {
        if (this.playing) {
            this.stopCurrent();
            this.paused = true;
        }
    }

    /**
     * [CA] Atura completament la reproducció. Si estava reproduint, atura les
     * notes anteriors i marca l'estat com a aturat.
     * <p>
     * [EN] Fully stops playback. If it was playing, stops the previous
     * notes and marks the state as stopped.
     */
    public void stop() {
        if (this.isPlaying()) {
            this.stopPrevious();
        }
        this.playing = false;
    }

    /**
     * [CA] Desa una còpia de l'acord indicat com a acord anterior.
     * S'usa internament per poder aturar les notes de l'acord que acaba de sonar.
     * <p>
     * [EN] Saves a copy of the given chord as the previous chord.
     * Used internally to be able to stop the notes of the chord that just finished.
     *
     * @param chord [CA] l'acord a desar com a anterior / [EN] the chord to save as previous
     */
    public void setPrevious(Chord chord) {
        previousChord = chord.cloneChord();
    }

    /**
     * [CA] Estableix l'acord actual i el reprodueix immediatament.
     * <p>
     * [EN] Sets the current chord and plays it immediately.
     *
     * @param ch [CA] l'acord a reproduir / [EN] the chord to play
     */
    public void setNPlay(Chord ch) {
//        this.stop();
//        this.setPrevious(currentChord);
        this.currentChord = ch.cloneChord();
        this.play();
    }

    /**
     * [CA] Reprodueix l'acord actual. Primer atura les notes de l'acord anterior,
     * desa l'acord actual com a anterior, i inicia la reproducció de totes les
     * notes de l'acord actual via MIDI o mostres de so.
     * <p>
     * [EN] Plays the current chord. First stops the previous chord's notes,
     * saves the current chord as previous, then starts playback of all
     * current chord notes via MIDI or audio samples.
     */
    public void play() {
        this.stopPrevious();
        this.setPrevious(currentChord);
        if (LOCAL_VERBOSE) {
            System.out.print("Play " + currentChord.getMidiShapeAsList());
            System.out.println(", Previous " + previousChord.getMidiShapeAsList());
        }
        for (int midi : currentChord.getMidiNotes()) {
            if (SampleOrMidi.isMidi()) {
                SoundWithMidi.play(midi, chordChannel, chordVolume);
            } else {
                Sound so = new Sound(ToneRange.getFilename(midi, soundInstrument));
                sounds.put(midi, so);
                so.play();
            }
        }
        playing = true;
        paused = false;
    }

    /**
     * [CA] Atura les notes de l'acord anterior. Si no hi ha acord anterior,
     * no fa res.
     * <p>
     * [EN] Stops the notes of the previous chord. If there is no previous
     * chord, does nothing.
     */
    public void stopPrevious() {
        if (previousChord != null) {
            if (LOCAL_VERBOSE) {
                System.out.println("Stop previous " + previousChord.getMidiShapeAsList());
            }
            for (int midi : previousChord.getMidiNotes()) {
                if (SampleOrMidi.isMidi()) {
                    SoundWithMidi.stop(midi, chordChannel);
                } else {
                    Sound so = sounds.get(midi);
                    so.stop();
                }
            }
        }
//        this.setPrevious(null);
    }

    /**
     * [CA] Atura les notes de l'acord actual. Si no hi ha acord actual,
     * no fa res.
     * <p>
     * [EN] Stops the notes of the current chord. If there is no current
     * chord, does nothing.
     */
    public void stopCurrent() {
        if (currentChord != null) {
            if (LOCAL_VERBOSE) {
                System.out.println("Stop current " + currentChord.getMidiShapeAsList());
            }
            for (int midi : currentChord.getMidiNotes()) {
                if (SampleOrMidi.isMidi()) {
                    SoundWithMidi.stop(midi, chordChannel);
                } else {
                    Sound so = sounds.get(midi);
                    so.stop();
                }
            }
        }
    }

    /**
     * [CA] Transposa l'acord actual el nombre de semitones indicat. Si el
     * reproductor estava reproduint (i no en pausa), torna a reproduir
     * l'acord transposat immediatament.
     * <p>
     * [EN] Transposes the current chord by the given number of semitones. If
     * the player was playing (and not paused), immediately plays the
     * transposed chord.
     *
     * @param step [CA] nombre de semitones de la transposició (positiu = amunt, negatiu = avall) /
     *             [EN] number of semitones to transpose (positive = up, negative = down)
     */
    public void transposeCurrentChord(int step) {
        if (currentChord != null) {
            if (LOCAL_VERBOSE) {
                System.out.print("Transpose " + currentChord.getMidiShapeAsList());
                System.out.println(", Previous " + previousChord.getMidiShapeAsList());
            }
            currentChord = currentChord.transpose(step);
            if (this.isPlaying() && !this.isPaused()) { // %% this.cam.isPlaying()
                this.play();
            }
        }
    }
}
