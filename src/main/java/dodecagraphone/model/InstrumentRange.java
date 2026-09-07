/*
 * PolyForm Noncommercial License 1.0.0
 * Copyright (c) 2024-2026 Pau Bofill. Powered by Claude AI.
 * Full license / Llicència completa: LICENSE (project root / arrel del projecte)
 */
package dodecagraphone.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * [CA] Tesitures dels instruments General MIDI (programa 0-127).
 * Llegides de {@code defaults/GeneralMidiInstruments.csv} (camps 3 i 4: lowestMidi i highestMidi).
 * Usada per calcular el {@code displayOffset} de cada pista en funció de l'instrument.
 * <p>
 * [EN] Pitch ranges for General MIDI instruments (program 0-127).
 * Read from {@code defaults/GeneralMidiInstruments.csv} (fields 3 and 4: lowestMidi and highestMidi).
 * Used to calculate the {@code displayOffset} of each track based on the instrument.
 *
 * <p>
 * <b>Convenció de signe</b>: {@code displayOffset = dibuix − so}. Negatiu vol
 * dir que l'instrument sona <i>per sobre</i> del que es dibuixa. El glockenspiel,
 * que el CSV declara 79–103 (so5–so7, la seva tessitura real), contra una graella
 * de metal·lòfon de 55–79 (so3–so5, l'altura escrita) dóna {@code -24}: es veu do4
 * i sona do6. La marca groga de la franja d'acords ho ensenya amb el signe
 * contrari, t+24, que és el que espera un músic acostumat a l'8va.
 * <p>
 * El valor <b>no es desa mai</b> al fitxer MIDI: es recalcula en carregar, a
 * partir del {@code PROGRAM_CHANGE} de cada pista. Per això el desat n'escriu
 * un per pista, a la primera nota.
 * <p>
 * <b>Compte</b>: el rang del CSV no sempre correspon a l'instrument que modela
 * l'app. El glockenspiel hi constava com a 67–96, una octava per sota del que
 * sona de debò, i això li donava un offset equivocat. Si es torna a tocar cap
 * fila del CSV, els fitxers ja desats es dibuixaran on digui el nou offset:
 * les altures MIDI desades no canvien (i per tant el que sona tampoc), però
 * les que quedin fora de [{@code lowestMidi}, {@code highestMidi}] les octava
 * {@link ToneRange#midiToKeyId(int)} en carregar.
 * <p>
 * <b>Sign convention</b>: {@code displayOffset = drawn − sounding}. Negative
 * means the instrument sounds <i>above</i> what is drawn. The glockenspiel,
 * declared 79–103 in the CSV (G5–G7, its real sounding range), against a
 * metallophone grid of 55–79 (G3–G5, the written pitch) yields {@code -24}:
 * C4 is shown and C6 sounds. The yellow mark on the chord strip displays the
 * opposite sign, t+24, which is what a musician used to 8va expects.
 * <p>
 * The value is <b>never stored</b> in the MIDI file: it is recomputed on load
 * from each track's {@code PROGRAM_CHANGE}. That is why saving writes one per
 * track, at its first note.
 * <p>
 * <b>Note</b>: the CSV range does not always match the instrument the app
 * models. The glockenspiel used to be listed as 67–96, one octave below what
 * it really sounds, which gave it the wrong offset. If any CSV row is changed
 * again, already-saved files will be drawn wherever the new offset says: the
 * stored MIDI pitches do not change (nor, therefore, what sounds), but any
 * falling outside [{@code lowestMidi}, {@code highestMidi}] get octave-shifted
 * by {@link ToneRange#midiToKeyId(int)} on load.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public final class InstrumentRange {

    private static final int[][] RANGES = new int[128][2];

    static {
        for (int i = 0; i < 128; i++) {
            RANGES[i][0] = 36;
            RANGES[i][1] = 84;
        }
        try {
            InputStream in = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("defaults/GeneralMidiInstruments.csv");
            if (in != null) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty()) continue;
                        String[] parts = line.split(";");
                        if (parts.length >= 5) {
                            int prog = Integer.parseInt(parts[0].trim());
                            int lo   = Integer.parseInt(parts[3].trim());
                            int hi   = Integer.parseInt(parts[4].trim());
                            if (prog >= 0 && prog < 128) {
                                RANGES[prog][0] = lo;
                                RANGES[prog][1] = hi;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // fallback: default ranges already set above
        }
    }

    private InstrumentRange() {}

    /**
     * [CA] Retorna la nota MIDI més baixa del rang de l'instrument especificat.
     * <p>
     * [EN] Returns the lowest MIDI note of the specified instrument's range.
     *
     * @param program [CA] número de programa MIDI (0-127) / [EN] MIDI program number (0-127)
     * @return [CA] nota MIDI mínima del rang / [EN] minimum MIDI note of the range
     */
    public static int getLowest(int program) {
        return RANGES[clamp(program)][0];
    }

    /**
     * [CA] Retorna la nota MIDI més alta del rang de l'instrument especificat.
     * <p>
     * [EN] Returns the highest MIDI note of the specified instrument's range.
     *
     * @param program [CA] número de programa MIDI (0-127) / [EN] MIDI program number (0-127)
     * @return [CA] nota MIDI màxima del rang / [EN] maximum MIDI note of the range
     */
    public static int getHighest(int program) {
        return RANGES[clamp(program)][1];
    }

    /**
     * [CA] Calcula el {@code displayOffset} (múltiple de 12) que maximitza la superposició
     * entre el rang de l'instrument i el rang del grid. En cas d'empat,
     * prefereix el menor offset absolut.
     * <p>
     * [EN] Calculates the {@code displayOffset} (multiple of 12) that maximises the overlap
     * between the instrument range and the grid range. In case of a tie,
     * prefers the smaller absolute offset.
     *
     * @param program     [CA] número de programa MIDI / [EN] MIDI program number
     * @param gridLowest  [CA] nota MIDI mínima del grid / [EN] lowest MIDI note of the grid
     * @param gridHighest [CA] nota MIDI màxima del grid / [EN] highest MIDI note of the grid
     * @return [CA] offset en semitons (múltiple de 12) / [EN] offset in semitones (multiple of 12)
     */
    public static int calcDisplayOffset(int program, int gridLowest, int gridHighest) {
        int instrLo = getLowest(program);
        int instrHi = getHighest(program);

        int bestOffset = 0;
        int bestOverlap = overlap(instrLo, instrHi, gridLowest, gridHighest);

        for (int oct = -4; oct <= 4; oct++) {
            if (oct == 0) continue;
            int offset = oct * 12;
            int ov = overlap(instrLo + offset, instrHi + offset, gridLowest, gridHighest);
            if (ov > bestOverlap || (ov == bestOverlap && Math.abs(offset) < Math.abs(bestOffset))) {
                bestOverlap = ov;
                bestOffset = offset;
            }
        }
        return bestOffset;
    }

    private static int overlap(int lo1, int hi1, int lo2, int hi2) {
        return Math.max(0, Math.min(hi1, hi2) - Math.max(lo1, lo2));
    }

    private static int clamp(int program) {
        return Math.max(0, Math.min(127, program));
    }
}
