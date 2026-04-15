package dodecagraphone.model.sound;

/**
 * Play and stop a sound from an audio file. Based on AudioManager.
 * @author upcnet
 */
public class Sound {
    private byte[] audio;
    public static AudioManager device = AudioManager.getAudioManager();//44100;
    
    /** 
     * Loads the audio from the given file.
     * @param file 
     */
    public Sound(String file){
        this.device.openLineOut();
        this.device.openSourceAudioFile(file);
        this.device.readAudioFile();
        this.audio=this.device.getCurrentAudio();
        this.device.closeSourceAudioFile();
    }
    /**
     * Plays the current audio.
     */
    public void play(){
        this.device.setCurrentAudio(this.audio);
        this.device.playAudio();
    }
    /**
     * Stops audio device.
     */
    public void stop(){
        this.device.stopAudio();
    }
    /**
     * Clean up.
     */
    public void dispose(){
        this.audio=null;
        this.device.closeLineOut();
        this.device.closeAudioManager();
    }
}
