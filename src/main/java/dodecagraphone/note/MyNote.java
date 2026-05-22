/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.note;

import dodecagraphone.model.component.MyGridScore;
import dodecagraphone.ui.Settings;

/**
 * [CA] Representació d'una nota musical codificada en format textual. El codi
 * segueix la convenció: prefix opcional {@code T} (tresillo/triplet) i/o {@code l}
 * (linked), lletra de durada ({@code w}=redonda, {@code h}=blanca, {@code q}=negra,
 * {@code e}=corchea, {@code s}=semicorchea, {@code t}=fusa, {@code x}=semifusa),
 * punt opcional de puntillo ({@code .}) i altura relativa a {@code midiRoot}.
 * <p>
 * [EN] Representation of a musical note encoded as a text string. The code follows
 * the convention: optional prefix {@code T} (triplet) and/or {@code l} (linked),
 * duration letter ({@code w}=whole, {@code h}=half, {@code q}=quarter,
 * {@code e}=eighth, {@code s}=sixteenth, {@code t}=thirty-second,
 * {@code x}=sixty-fourth), optional dot ({@code .}) and pitch relative to
 * {@code midiRoot}.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MyNote {
    double duration; // A whole note (4 beats) has duration 1.
    int ncols; // Number of columns in the current grid.
    int pitch; // relative to midiRoot;
    boolean is_linked; // linked to the previous note

    /**
     * [CA] Crea una nota a partir d'un codi textual i la graella de partitura
     * activa. Analitza el codi per extreure la durada, el puntillo, el triplet
     * i el lligat; calcula el nombre de columnes que ocupa la nota.
     * <p>
     * [EN] Creates a note from a text code and the active score grid. Parses the
     * code to extract duration, dot, triplet and tie; calculates the number of
     * grid columns the note occupies.
     *
     * @param code  [CA] codi textual de la nota (ex: "q5", "Tlh3", "e.2") / [EN] text code of the note (e.g. "q5", "Tlh3", "e.2")
     * @param score [CA] graella de partitura activa (per obtenir {@code nColsBeat}) / [EN] active score grid (to get {@code nColsBeat})
     */
    public MyNote(String code, MyGridScore score){
        is_linked = false;
        boolean is_triplet = false;
        if (code.charAt(0)=='T'){ // triplet
            is_triplet = true;
            code = code.substring(1);
        }
        if (code.charAt(0)=='l'){
            is_linked = true;
            code = code.substring(1);
        }
        char d = code.charAt(0);
        switch (d){
            case 'w': // whole
                duration = 1; // one measure (4 beats)
                break;
            case 'h': // half
                duration = 1.0/2;
                break;
            case 'q': // quarter
                duration = 1.0/4;
                break;
            case 'e': // eigth
                duration = 1.0/8;
                break;
            case 's': // sixteenth
                duration = 1.0/16;
                break;
            case 't': // fusa 32th
                duration = 1.0/32;
                break;
            case 'x': // semifusa 64th
                duration = 1.0/64;
                break;
        }
        int i = 1;
        if (code.charAt(1)=='.'){
            i=2;
            duration *= 1.5;
        }
        if (is_triplet){
            duration *= 2.0/3;
        }
        ncols = (int) Math.round(duration * Settings.getnColsBeat() * 4);
        pitch = Integer.parseInt(code.substring(i));
    }

    /**
     * [CA] Retorna la durada de la nota com a fracció d'una redonda (1.0 = redonda).
     * <p>
     * [EN] Returns the note duration as a fraction of a whole note (1.0 = whole note).
     *
     * @return [CA] durada de la nota / [EN] note duration
     */
    public double getDuration() {
        return duration;
    }

    /**
     * [CA] Retorna el nombre de columnes de la graella que ocupa la nota.
     * <p>
     * [EN] Returns the number of grid columns the note occupies.
     *
     * @return [CA] nombre de columnes / [EN] number of columns
     */
    public int getNcols() {
        return ncols;
    }

    /**
     * [CA] Retorna l'altura de la nota relativa a {@code midiRoot}.
     * <p>
     * [EN] Returns the note pitch relative to {@code midiRoot}.
     *
     * @return [CA] altura relativa / [EN] relative pitch
     */
    public int getPitch() {
        return pitch;
    }

    /**
     * [CA] Indica si la nota és lligada a l'anterior.
     * <p>
     * [EN] Indicates whether the note is tied to the previous one.
     *
     * @return [CA] {@code true} si és lligada / [EN] {@code true} if tied
     */
    public boolean isLinked() {
        return is_linked;
    }

    /**
     * [CA] Estableix si la nota és lligada a l'anterior.
     * <p>
     * [EN] Sets whether the note is tied to the previous one.
     *
     * @param is_linked [CA] {@code true} per lligar la nota / [EN] {@code true} to tie the note
     */
    public void setLinked(boolean is_linked) {
        this.is_linked = is_linked;
    }


}
