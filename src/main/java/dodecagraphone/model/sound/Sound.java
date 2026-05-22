/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.sound;

/**
 * [CA] Representa un so carregat des d'un fitxer d'àudio i el reprodueix via {@link AudioManager}.
 * Proporciona mètodes per reproduir, aturar i alliberar els recursos del so.
 * <p>
 * [EN] Represents a sound loaded from an audio file and plays it via {@link AudioManager}.
 * Provides methods to play, stop and release sound resources.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class Sound {
    private byte[] audio;
    public static AudioManager device = AudioManager.getAudioManager();//44100;

    /**
     * [CA] Carrega l'àudio des del fitxer donat.
     * <p>
     * [EN] Loads the audio from the given file.
     *
     * @param file [CA] ruta del fitxer d'àudio a carregar / [EN] path of the audio file to load
     */
    public Sound(String file){
        this.device.openLineOut();
        this.device.openSourceAudioFile(file);
        this.device.readAudioFile();
        this.audio=this.device.getCurrentAudio();
        this.device.closeSourceAudioFile();
    }

    /**
     * [CA] Reprodueix l'àudio actual.
     * <p>
     * [EN] Plays the current audio.
     */
    public void play(){
        this.device.setCurrentAudio(this.audio);
        this.device.playAudio();
    }

    /**
     * [CA] Atura la reproducció del dispositiu d'àudio.
     * <p>
     * [EN] Stops the audio device playback.
     */
    public void stop(){
        this.device.stopAudio();
    }

    /**
     * [CA] Allibera els recursos d'àudio (buffer i dispositiu).
     * <p>
     * [EN] Releases audio resources (buffer and device).
     */
    public void dispose(){
        this.audio=null;
        this.device.closeLineOut();
        this.device.closeAudioManager();
    }
}
