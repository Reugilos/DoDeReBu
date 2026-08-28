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
     * <b>Compte</b>: el rang surt de {@code defaults/GeneralMidiInstruments.csv}
     * i no sempre correspon a l'instrument que modela l'app. El glockenspiel hi
     * consta com a 67–96 (so4–do7), una octava per sota del metal·lòfon que
     * representa la graella (79–103, so5–so7), i per això li surt un offset de
     * +12 que fa sonar i exportar una octava per sota del que es veu. Corregir-ho
     * vol dir ajustar el CSV i fer que la càrrega respecti el {@code displayOffset}
     * desat als fitxers antics, per no desafinar-los.
     * <p>
     * <b>Note</b>: the range comes from {@code defaults/GeneralMidiInstruments.csv}
     * and does not always match the instrument the app models. The glockenspiel
     * is listed as 67–96 (G4–C7), one octave below the metallophone the grid
     * represents (79–103, G5–G7), which is why it gets a +12 offset that makes it
     * sound and export one octave below what is shown. Fixing it means adjusting
     * the CSV and making loading honour the {@code displayOffset} stored in older
     * files, so they are not detuned.
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
