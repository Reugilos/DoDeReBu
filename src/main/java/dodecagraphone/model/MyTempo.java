/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model;

import dodecagraphone.ui.Settings;

/**
 * [CA] Gestiona dos valors de tempo de forma estàtica:
 * <ul>
 *   <li>{@code scoreTempo}: el tempo de la partitura (de les marques del changeMap).
 *       És el que mostra el botó de tempo i el que es desa. Es canvia amb
 *       {@link #setTempo(int)} (reinicia ambdós) o {@link #setScoreTempo(int)}
 *       (només scoreTempo, conserva playbackTempo).</li>
 *   <li>{@code playbackTempo}: la velocitat de reproducció en viu. Comença igual
 *       que scoreTempo però es pot ajustar amb {@link #faster()}/{@link #slower()}
 *       sense col·locar cap marca. Només es reinicia a scoreTempo amb
 *       {@link #setTempo(int)} (cridat en carregar/crear una partitura).</li>
 * </ul>
 * <p>
 * [EN] Manages two tempo values statically:
 * <ul>
 *   <li>{@code scoreTempo}: the score tempo (from changeMap marks).
 *       This is what the tempo button displays and what gets saved. Changed via
 *       {@link #setTempo(int)} (resets both) or {@link #setScoreTempo(int)}
 *       (only scoreTempo, keeps playbackTempo).</li>
 *   <li>{@code playbackTempo}: the live playback speed. Starts equal to scoreTempo
 *       but can be adjusted with {@link #faster()}/{@link #slower()} without placing
 *       a mark. Only reset to scoreTempo by {@link #setTempo(int)} (called when
 *       loading/creating a score).</li>
 * </ul>
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MyTempo {
    /** The score tempo — from changeMap marks, shown on the button. */
    private static int scoreTempo = Settings.DEFAULT_TEMPO;
    /** The live playback tempo — adjusted by Vel+/Vel-, not shown on button. */
    private static int playbackTempo = Settings.DEFAULT_TEMPO;

    private static final double increment = 5;

    /**
     * [CA] Estableix el tempo de la partitura (cridat quan s'aplica una marca).
     * També reinicia el tempo de reproducció en viu al nou valor.
     * <p>
     * [EN] Sets the score tempo (called when a mark is applied). Also resets the
     * live playback tempo to the new score tempo.
     *
     * @param tpo [CA] nou valor de tempo en BPM / [EN] new tempo value in BPM
     * @return [CA] el valor de tempo efectivament assignat / [EN] the effectively assigned tempo value
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
     * [CA] Actualitza només {@code scoreTempo} (el valor mostrat al botó i usat per desar).
     * No modifica {@code playbackTempo}. Cridat des de {@code applyChangesAt} per
     * preservar els ajustos Spd+/Spd- durant la navegació.
     * <p>
     * [EN] Updates only {@code scoreTempo} (the value shown on the button and used for
     * saving). Does NOT touch {@code playbackTempo}. Called from {@code applyChangesAt} so
     * that Spd+/Spd- adjustments survive navigation.
     *
     * @param tpo [CA] nou valor del tempo de la partitura en BPM / [EN] new score tempo value in BPM
     * @return [CA] el valor de scoreTempo efectivament assignat / [EN] the effectively assigned scoreTempo value
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
     * [CA] Augmenta el tempo de reproducció en viu un increment.
     * No canvia {@code scoreTempo} ni col·loca cap marca.
     * <p>
     * [EN] Increases the live playback tempo by one increment.
     * Does NOT change {@code scoreTempo} or place any mark.
     */
    public static void faster() {
        if (playbackTempo < (Settings.MAX_BPM - increment))
            playbackTempo = (int) Math.round(playbackTempo + increment);
        else
            playbackTempo = Settings.MAX_BPM;
    }

    /**
     * [CA] Redueix el tempo de reproducció en viu un increment.
     * No canvia {@code scoreTempo} ni col·loca cap marca.
     * <p>
     * [EN] Decreases the live playback tempo by one increment.
     * Does NOT change {@code scoreTempo} or place any mark.
     */
    public static void slower() {
        if (playbackTempo - increment > 0)
            playbackTempo = (int) Math.round(playbackTempo - increment);
        else
            playbackTempo = 0;
    }

    /**
     * [CA] Retorna el tempo de la partitura (el que hi ha a les marques).
     * Usat per a la visualització i la serialització.
     * <p>
     * [EN] Returns the score tempo (what's in the marks). Used for display and
     * for serialisation.
     *
     * @return [CA] el tempo de la partitura en BPM / [EN] the score tempo in BPM
     */
    public static int getTempo() {
        return scoreTempo;
    }

    /**
     * [CA] Retorna el tempo de reproducció en viu. Usat per {@code getNanosPerSquareGrid()}.
     * <p>
     * [EN] Returns the live playback tempo. Used by {@code getNanosPerSquareGrid()}.
     *
     * @return [CA] el tempo de reproducció en viu en BPM / [EN] the live playback tempo in BPM
     */
    public static int getPlaybackTempo() {
        return playbackTempo;
    }

    /**
     * [CA] Nanosegons per columna de graella a la velocitat de reproducció en viu actual.
     * <p>
     * [EN] Nanoseconds per grid column at the current LIVE playback speed.
     *
     * @return [CA] nanosegons per columna de graella / [EN] nanoseconds per grid column
     */
    public static double getNanosPerSquareGrid() {
        double mpsq = 60000000000.0 / (double) (Settings.getnColsBeat() * playbackTempo);
        return mpsq;
    }
}
