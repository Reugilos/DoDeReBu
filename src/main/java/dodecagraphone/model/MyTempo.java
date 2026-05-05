package dodecagraphone.model;

import dodecagraphone.ui.Settings;

/**
 * Manages two tempo values:
 *  - scoreTempo: the tempo that is in the score (from changeMap marks). This is
 *    what the tempo button displays and what gets saved. Changed via setTempo()
 *    (resets both) or setScoreTempo() (only scoreTempo, keeps playbackTempo).
 *  - playbackTempo: the live playback speed. Starts equal to scoreTempo but can
 *    be adjusted with faster()/slower() without placing a mark. Only reset to
 *    scoreTempo by setTempo() (called when loading/creating a score).
 */
public class MyTempo {
    /** The score tempo — from changeMap marks, shown on the button. */
    private static int scoreTempo = Settings.DEFAULT_TEMPO;
    /** The live playback tempo — adjusted by Vel+/Vel-, not shown on button. */
    private static int playbackTempo = Settings.DEFAULT_TEMPO;

    private static final double increment = 5;

    /**
     * Sets the score tempo (called when a mark is applied). Also resets the
     * live playback tempo to the new score tempo.
     */
    public static int setTempo(int tpo) {
        if (tpo > Settings.MAX_BPM)
            scoreTempo = Settings.MAX_BPM;
        else if (tpo < 0)
            scoreTempo = 0;
        else
            scoreTempo = tpo;
        // Reset live speed to score speed
        playbackTempo = scoreTempo;
        return scoreTempo;
    }

    /**
     * Updates only scoreTempo (the value shown on the button and used for
     * saving). Does NOT touch playbackTempo. Called from applyChangesAt so
     * that Spd+/Spd- adjustments survive navigation.
     */
    public static int setScoreTempo(int tpo) {
        if (tpo > Settings.MAX_BPM)
            scoreTempo = Settings.MAX_BPM;
        else if (tpo < 0)
            scoreTempo = 0;
        else
            scoreTempo = tpo;
        return scoreTempo;
    }

    /**
     * Increases the live playback tempo by one increment.
     * Does NOT change scoreTempo or place any mark.
     */
    public static void faster() {
        if (playbackTempo < (Settings.MAX_BPM - increment))
            playbackTempo = (int) Math.round(playbackTempo + increment);
        else
            playbackTempo = Settings.MAX_BPM;
    }

    /**
     * Decreases the live playback tempo by one increment.
     * Does NOT change scoreTempo or place any mark.
     */
    public static void slower() {
        if (playbackTempo - increment > 0)
            playbackTempo = (int) Math.round(playbackTempo - increment);
        else
            playbackTempo = 0;
    }

    /**
     * Returns the score tempo (what's in the marks). Used for display and
     * for serialisation.
     */
    public static int getTempo() {
        return scoreTempo;
    }

    /**
     * Returns the live playback tempo. Used by getNanosPerSquareGrid().
     */
    public static int getPlaybackTempo() {
        return playbackTempo;
    }

    /**
     * Nanoseconds per grid column at the current LIVE playback speed.
     */
    public static double getNanosPerSquareGrid() {
        double mpsq = 60000000000.0 / (double) (Settings.getnColsBeat() * playbackTempo);
        return mpsq;
    }
}
