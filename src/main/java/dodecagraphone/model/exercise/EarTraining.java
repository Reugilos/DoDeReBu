package dodecagraphone.model.exercise;

import dodecagraphone.model.component.MyExercise;
import dodecagraphone.model.MyTempo;
import dodecagraphone.model.ToneRange;
import dodecagraphone.model.chord.Chord;
import dodecagraphone.model.chord.Triad;
import dodecagraphone.ui.I18n;
import dodecagraphone.ui.Settings;
import dodecagraphone.ui.Utilities;

import java.util.Arrays;
import java.util.List;

/**
 * [CA] Família d'exercicis d'entrenament auditiu ({@code EarTraining}).
 * Conté 17 exercicis progressius que treballen el reconeixement de notes
 * i acords dins d'un context tonal. Els exercicis van des de l'entonació
 * de notes individuals de l'escala major fins al reconeixement d'acords
 * tríada en diverses inversions i disposicions.
 *
 * <p>Implementa {@link MyExerciseFamily} i és invocada pel controlador
 * central quan l'usuari selecciona un exercici d'aquesta família.</p>
 *
 * <p>[EN] Ear training exercise family ({@code EarTraining}).
 * Contains 17 progressive exercises covering note and chord recognition
 * within a tonal context. Exercises range from singing individual notes
 * of the major scale to identifying triads in various inversions and
 * voicings.</p>
 *
 * <p>Implements {@link MyExerciseFamily} and is invoked by the central
 * controller when the user selects an exercise from this family.</p>
 *
 * @see MyExerciseFamily
 * @see dodecagraphone.model.component.MyExercise
 */
public class EarTraining implements MyExerciseFamily {

    private static final List<String> exerciseList = Arrays.asList(
            "Ex1", "Ex2", "Ex3", "Ex4", "Ex5", "Ex6", "Ex7",
            "Ex8", "Ex9", "Ex10", "Ex11", "Ex12", "Ex13", "Ex14",
            "Ex15", "Ex16", "Ex17"
    );

    /**
     * [CA] Retorna la llista de etiquetes de tots els exercicis disponibles
     * en aquesta família ({@code "Ex1"} fins a {@code "Ex17"}).
     *
     * <p>[EN] Returns the list of labels for all exercises available
     * in this family ({@code "Ex1"} through {@code "Ex17"}).</p>
     *
     * @return llista immutable d'etiquetes d'exercicis /
     *         immutable list of exercise labels
     */
    public static List<String> getExerciseLabelList() {
        return exerciseList;
    }

    /**
     * [CA] Configura i llança l'exercici corresponent a l'etiqueta indicada.
     * Actua com a despachador: delega la configuració al mètode privat
     * específic de cada exercici. Si l'etiqueta és {@code "Blank"}, neteja
     * la partitura sense llançar cap exercici. Si l'etiqueta és desconeguda,
     * llança {@link UnsupportedOperationException}.
     *
     * <p>[EN] Configures and launches the exercise corresponding to the
     * given label. Acts as a dispatcher: delegates setup to the specific
     * private method for each exercise. If the label is {@code "Blank"},
     * clears the score without launching an exercise. If the label is
     * unknown, throws {@link UnsupportedOperationException}.</p>
     *
     * @param ex    l'objecte exercici a configurar /
     *              the exercise object to configure
     * @param label l'etiqueta de l'exercici (p.ex. {@code "Ex1"}) /
     *              the exercise label (e.g. {@code "Ex1"})
     * @throws UnsupportedOperationException si l'etiqueta no correspon a cap exercici conegut /
     *                                       if the label does not match any known exercise
     */
    @Override
    public void applyExercise(MyExercise ex, String label) {
        ex.setLabel(label);
        switch (label) {
            case "Ex1" ->
                setExercise_1(ex);
            case "Ex2" ->
                setExercise_2(ex);
            case "Ex3" ->
                setExercise_3(ex);
            case "Ex4" ->
                setExercise_4(ex);
            case "Ex5" ->
                setExercise_5(ex);
            case "Ex6" ->
                setExercise_6(ex);
            case "Ex7" ->
                setExercise_7(ex);
            case "Ex8" ->
                setExercise_8(ex);
            case "Ex9" ->
                setExercise_9(ex);
            case "Ex10" ->
                setExercise_10(ex);
            case "Ex11" ->
                setExercise_11(ex);
            case "Ex12" ->
                setExercise_12(ex);
            case "Ex13" ->
                setExercise_13(ex);
            case "Ex14" ->
                setExercise_14(ex);
            case "Ex15" ->
                setExercise_15(ex);
            case "Ex16" ->
                setExercise_16(ex);
            case "Ex17" ->
                setExercise_17(ex);
            case "Blank" ->
                ex.getController().clearScore();
            default -> {
                ex.getController().clearScore();
                throw new UnsupportedOperationException(I18n.f("earTraining.error.noSuchPattern", label));
            }
        }
    }

    /**
     * [CA] Exercici 1: Entonació de notes de l'escala major ascendent (Do-Re-Mi-Fa-Sol-La-Si-Do).
     * Es mostra el context tonal i els noms de les notes. Tempo lent (40 bpm).
     * Cada nota sona amb accent (negra amb punt) seguit d'una corxera sense accent.
     *
     * <p>[EN] Exercise 1: Singing notes of the ascending major scale (C-D-E-F-G-A-B-C).
     * Tonal context and note names are shown. Slow tempo (40 bpm).
     * Each note sounds accented (dotted quarter) followed by an unaccented eighth note.</p>
     *
     * @param ex l'objecte exercici a configurar / the exercise object to configure
     */
    private static void setExercise_1(MyExercise ex) {
        ex.setDescription(I18n.t("earTraining.ex1.description"));
        ex.setChoice(new Integer[]{0, 2, 4, 5, 7, 9, 11, 12});
        ex.setUsePentagramaStrips(false); // mobile strips
        ex.setUseScreenKeyboardRight(true);
        ex.setUseMobileDo(true);
        ex.setShowNoteNames(true);
        Settings.setShowMutted(true);
        MyTempo.setTempo(40);
        int midiKey = ex.getMidiKey();
        Chord chord = new Chord(0, new int[]{0}, midiKey, I18n.t("earTraining.backgroundChord"));
        ex.placeBackgroundChord(chord);
        ex.placeTonalContext(midiKey);
        while (!ex.isOver()) {
            int note = Utilities.randFromList(ex.getChoice().getChoiceList()) + midiKey;
            ex.placeNote(note, (int) Math.round(1.5 * ex.ONE_BEAT), true);
            ex.placeAppendMessage(ToneRange.getNoteName(note, midiKey), ex.MESSAGE_DELAY);
            ex.placeNote(note, (int) Math.round(0.5 * ex.ONE_BEAT), false);
        }
    }

    /**
     * [CA] Exercici 2: Entonació de notes de l'escala major en ambdues direccions
     * (ascendent i descendent, dues octaves). Amplía l'Ex1 afegint les notes
     * per sota de la tònica. Context tonal visible i noms de notes.
     *
     * <p>[EN] Exercise 2: Singing notes of the major scale in both directions
     * (ascending and descending, two octaves). Extends Ex1 by adding notes
     * below the tonic. Tonal context and note names visible.</p>
     *
     * @param ex l'objecte exercici a configurar / the exercise object to configure
     */
    private static void setExercise_2(MyExercise ex) {
        ex.setDescription(I18n.t("earTraining.ex2.description"));
        ex.setChoice(new Integer[]{0, 2, 4, 5, 7, 9, 11, 12});
        ex.setUsePentagramaStrips(false); // mobile strips
        ex.setUseScreenKeyboardRight(true);
        ex.setUseMobileDo(true);
        ex.setShowNoteNames(true);
        Settings.setShowMutted(true);
        MyTempo.setTempo(40);
        int midiKey = ex.getMidiKey();
        Chord chord = new Chord(0, new int[]{0}, midiKey, I18n.t("earTraining.backgroundChord"));
        ex.placeBackgroundChord(chord);
        ex.setChoice(new Integer[]{0, 2, 4, 5, 7, 9, 11, 12, -1, -3, -5, -7, -8, -10, -12});
        ex.placeTonalContext(midiKey);
        while (!ex.isOver()) {
            int note = Utilities.randFromList(ex.getChoice().getChoiceList()) + midiKey;
            ex.placeNote(note, (int) Math.round(1.5 * ex.ONE_BEAT), true);
            ex.placeAppendMessage(ToneRange.getNoteName(note, midiKey), ex.MESSAGE_DELAY);
            ex.placeNote(note, (int) Math.round(0.5 * ex.ONE_BEAT), false);
        }
    }

    /**
     * [CA] Exercici 3: Reconeixement d'acords tríada en estat fonamental,
     * escollits aleatòriament entre un subconjunt de graus de l'escala
     * (I, IV, V, VI i els seus equivalents a l'octava inferior).
     * El nom de l'acord apareix un cop sonat.
     *
     * <p>[EN] Exercise 3: Recognition of root-position triads chosen randomly
     * from a subset of scale degrees (I, IV, V, VI and their lower-octave
     * equivalents). The chord name is shown after it sounds.</p>
     *
     * @param ex l'objecte exercici a configurar / the exercise object to configure
     */
    private static void setExercise_3(MyExercise ex) {
        ex.setDescription(I18n.t("earTraining.ex3.description"));
        ex.setUsePentagramaStrips(false); // mobile strips
        ex.setUseScreenKeyboardRight(true);
        ex.setUseMobileDo(true);
        ex.setShowNoteNames(true);
        ex.setDelay(4);
        ex.setChoice(new Integer[]{0, 5, 7, 9, -12, -7, -5, -3});
        int midiKey = ex.getMidiKey();
        while (!ex.isOver()) {
            int root = Utilities.randFromList(ex.getChoice().getChoiceList());
            Chord chord = new Triad(root, midiKey, Triad.ROOT_POSITION);
            ex.placeAppendMessage(chord.toString(), ex.MESSAGE_DELAY);
            ex.placeChord(chord, 2 * ex.ONE_BEAT - 1, false);
            ex.skipCols(1);
        }
    }

    /**
     * [CA] Exercici 4: Com l'Ex3 però amb tots els graus diatònics de l'escala
     * major (I a VII en ambdues octaves). Utilitza bandes de pentagrama mòbils.
     *
     * <p>[EN] Exercise 4: Like Ex3 but using all diatonic degrees of the major
     * scale (I through VII in both octaves). Uses mobile pentagrama strips.</p>
     *
     * @param ex l'objecte exercici a configurar / the exercise object to configure
     */
    private static void setExercise_4(MyExercise ex) {
        ex.setDescription(I18n.t("earTraining.ex4.description"));
        ex.setUsePentagramaStrips(true); // mobile strips
        ex.setUseScreenKeyboardRight(true);
        ex.setUseMobileDo(true);
        ex.setShowNoteNames(true);
        ex.setDelay(4);
        ex.setChoice(new Integer[]{0, 2, 4, 5, 7, 9, -12, -10, -8, -7, -5, -3});
        int midiKey = ex.getMidiKey();
        while (!ex.isOver()) {
            int root = Utilities.randFromList(ex.getChoice().getChoiceList());
            Chord chord = new Triad(root, midiKey, Triad.ROOT_POSITION);
            ex.placeAppendMessage(chord.toString(), ex.MESSAGE_DELAY);
            ex.placeChord(chord, 2 * ex.ONE_BEAT - 1, false);
            ex.skipCols(1);
        }
    }

    /**
     * [CA] Exercici 5: Entonació de notes de l'escala major (ascendent) sense
     * que es mostri la nota a la partitura fins que acaba de sonar. L'usuari
     * ha d'identificar la nota escoltada. Context tonal i fons d'acord visible.
     *
     * <p>[EN] Exercise 5: Singing major scale notes (ascending) without showing
     * the note on the score until after it sounds. The user must identify the
     * note heard. Tonal context and background chord visible.</p>
     *
     * @param ex l'objecte exercici a configurar / the exercise object to configure
     */
    private static void setExercise_5(MyExercise ex) {
        ex.setDescription(I18n.t("earTraining.ex5.description"));
        ex.setUsePentagramaStrips(false);
        ex.setUseScreenKeyboardRight(true);
        ex.setUseMobileDo(true);
        ex.setShowNoteNames(true);
        ex.setDelay(4);
        int midiKey = ex.getMidiKey();
        Chord chord = new Chord(0, new int[]{0}, midiKey, I18n.t("earTraining.backgroundChord"));
        ex.placeBackgroundChord(chord);
        ex.setChoice(new Integer[]{0, 2, 4, 5, 7, 9, 11, 12});
        ex.placeTonalContext(midiKey);
        while (!ex.isOver()) {
            int note = Utilities.randFromList(ex.getChoice().getChoiceList()) + midiKey;
            ex.placeNote(note, ex.ONE_BAR, true);
        }
    }

    /**
     * [CA] Exercici 6: Identificació de notes de l'escala major ascendent.
     * El nom de la nota apareix en pantalla just després de sonar (missatge
     * afegit amb retard). Durada de dues negres per nota.
     *
     * <p>[EN] Exercise 6: Identifying notes of the ascending major scale.
     * The note name appears on screen right after it sounds (delayed append
     * message). Duration of two quarter notes per note.</p>
     *
     * @param ex l'objecte exercici a configurar / the exercise object to configure
     */
    private static void setExercise_6(MyExercise ex) {
        ex.setDescription(I18n.t("earTraining.ex6.description"));
        ex.setUsePentagramaStrips(false);
        ex.setUseScreenKeyboardRight(true);
        ex.setUseMobileDo(true);
        ex.setShowNoteNames(true);
        ex.setDelay(4);
        int midiKey = ex.getMidiKey();
        Chord chord = new Chord(0, new int[]{0}, midiKey, I18n.t("earTraining.backgroundChord"));
        ex.placeBackgroundChord(chord);
        ex.setChoice(new Integer[]{0, 2, 4, 5, 7, 9, 11, 12});
        ex.placeTonalContext(midiKey);
        while (!ex.isOver()) {
            int note = Utilities.randFromList(ex.getChoice().getChoiceList()) + midiKey;
            ex.placeAppendMessage(ToneRange.getNoteName(note, midiKey), ex.MESSAGE_DELAY);
            ex.placeNote(note, 2 * ex.ONE_BEAT, false);
        }
    }

    /**
     * [CA] Exercici 7: Com l'Ex6 però amb l'escala major en ambdues direccions
     * (ascendent i descendent, dues octaves). El nom de la nota es mostra
     * després de sonar.
     *
     * <p>[EN] Exercise 7: Like Ex6 but with the major scale in both directions
     * (ascending and descending, two octaves). The note name is shown after
     * it sounds.</p>
     *
     * @param ex l'objecte exercici a configurar / the exercise object to configure
     */
    private static void setExercise_7(MyExercise ex) {
        ex.setDescription(I18n.t("earTraining.ex7.description"));
        ex.setUsePentagramaStrips(false);
        ex.setUseScreenKeyboardRight(true);
        ex.setUseMobileDo(true);
        ex.setShowNoteNames(true);
        ex.setDelay(4);

        int midiKey = ex.getMidiKey();
        Chord chord = new Chord(0, new int[]{0}, midiKey, I18n.t("earTraining.backgroundChord"));
        ex.placeBackgroundChord(chord);
        ex.setChoice(new Integer[]{0, 2, 4, 5, 7, 9, 11, 12, -1, -3, -5, -7, -8, -10, -12});
        ex.placeTonalContext(midiKey);

        while (!ex.isOver()) {
            int note = Utilities.randFromList(ex.getChoice().getChoiceList()) + midiKey;
            ex.placeAppendMessage(ToneRange.getNoteName(note, midiKey), ex.MESSAGE_DELAY);
            ex.placeNote(note, 2 * ex.ONE_BEAT, false);
        }
    }

    /**
     * [CA] Exercici 8: Escala major completa (anada i tornada) amb triades
     * en estat fonamental. Per a cada grau es toca primer la nota arrel,
     * després l'acord en arpegi (patró 1-2-3-2-1). El nom de l'acord
     * s'afegeix en pantalla sense retard.
     *
     * <p>[EN] Exercise 8: Full major scale (up and back) with root-position
     * triads. For each degree, the root note sounds first, then the chord
     * is arpeggiated (pattern 1-2-3-2-1). The chord name is shown immediately.</p>
     *
     * @param ex l'objecte exercici a configurar / the exercise object to configure
     */
    private static void setExercise_8(MyExercise ex) {
        ex.setDescription(I18n.t("earTraining.ex8.description"));
        ex.setUsePentagramaStrips(false);
        ex.setUseScreenKeyboardRight(false);
        ex.setUseMobileDo(true);
        ex.setShowNoteNames(true);

        int midiKey = ex.getMidiKey();
        ex.placeTonalContext(midiKey);
        ex.setChoice(new Integer[]{0, 2, 4, 5, 7, 9, 11, 12, 11, 9, 7, 5, 4, 2, 0});
        int[] arpeggiatura = new int[]{1, 2, 3, 2, 1};

        ex.skipCols(-2 * ex.ONE_BEAT);
        for (int root : ex.getChoice().getChoiceList()) {
            int midiRoot = root + midiKey;
            Chord chord = new Triad(root, midiKey, Triad.ROOT_POSITION);
            ex.placeNote(midiRoot, ex.ONE_BEAT, false);
            ex.skipCols(ex.ONE_BEAT);
            ex.placeAppendMessage(chord.toString(), 0);
            ex.placeArpeggio(chord, arpeggiatura, ex.ONE_BEAT, false);
            ex.skipCols(ex.ONE_BEAT);
        }
    }

    /**
     * [CA] Exercici 9: Com l'Ex8 però amb les triades en primera inversió
     * ({@link Triad#FIRST_INVERSION}). Entrena el reconeixement del so
     * de l'acord amb el terç al baix.
     *
     * <p>[EN] Exercise 9: Like Ex8 but with triads in first inversion
     * ({@link Triad#FIRST_INVERSION}). Trains recognition of the chord
     * sound with the third in the bass.</p>
     *
     * @param ex l'objecte exercici a configurar / the exercise object to configure
     */
    private static void setExercise_9(MyExercise ex) {
        ex.setDescription(I18n.t("earTraining.ex9.description"));
        ex.setUsePentagramaStrips(false);
        ex.setUseScreenKeyboardRight(false);
        ex.setUseMobileDo(true);
        ex.setShowNoteNames(true);

        int midiKey = ex.getMidiKey();
        ex.placeTonalContext(midiKey);
        ex.setChoice(new Integer[]{0, 2, 4, 5, 7, 9, 11, 12, 11, 9, 7, 5, 4, 2, 0});
        int[] arpeggiatura = new int[]{1, 2, 3, 2, 1};

        ex.skipCols(-2 * ex.ONE_BEAT);
        for (int root : ex.getChoice().getChoiceList()) {
            int midiRoot = root + midiKey;
            Chord chord = new Triad(root, midiKey, Triad.FIRST_INVERSION);
            ex.placeNote(midiRoot, ex.ONE_BEAT, false);
            ex.skipCols(ex.ONE_BEAT);
            ex.placeAppendMessage(chord.toString(), 0);
            ex.placeArpeggio(chord, arpeggiatura, ex.ONE_BEAT, false);
            ex.skipCols(ex.ONE_BEAT);
        }
    }

    /**
     * [CA] Exercici 10: Com l'Ex8 però amb les triades en segona inversió
     * ({@link Triad#SECOND_INVERSION}). Entrena el reconeixement del so
     * de l'acord amb la quinta al baix.
     *
     * <p>[EN] Exercise 10: Like Ex8 but with triads in second inversion
     * ({@link Triad#SECOND_INVERSION}). Trains recognition of the chord
     * sound with the fifth in the bass.</p>
     *
     * @param ex l'objecte exercici a configurar / the exercise object to configure
     */
    private static void setExercise_10(MyExercise ex) {
        ex.setDescription(I18n.t("earTraining.ex10.description"));
        ex.setUsePentagramaStrips(false);
        ex.setUseScreenKeyboardRight(false);
        ex.setUseMobileDo(true);
        ex.setShowNoteNames(true);

        int midiKey = ex.getMidiKey();
        ex.placeTonalContext(midiKey);
        ex.setChoice(new Integer[]{0, 2, 4, 5, 7, 9, 11, 12, 11, 9, 7, 5, 4, 2, 0});
        int[] arpeggiatura = new int[]{1, 2, 3, 2, 1};

        ex.skipCols(-2 * ex.ONE_BEAT);
        for (int root : ex.getChoice().getChoiceList()) {
            int midiRoot = root + midiKey;
            Chord chord = new Triad(root, midiKey, Triad.SECOND_INVERSION);
            ex.placeNote(midiRoot, ex.ONE_BEAT, false);
            ex.skipCols(ex.ONE_BEAT);
            ex.placeAppendMessage(chord.toString(), 0);
            ex.placeArpeggio(chord, arpeggiatura, ex.ONE_BEAT, false);
            ex.skipCols(ex.ONE_BEAT);
        }
    }

    /**
     * [CA] Exercici 11: Com l'Ex8 però amb les triades en inversió aleatòria
     * ({@link Triad#RANDOM_INVERSION}). L'ordre dels graus és l'escala
     * completa (anada i tornada). Combina totes les inversions apreses.
     *
     * <p>[EN] Exercise 11: Like Ex8 but with triads in random inversion
     * ({@link Triad#RANDOM_INVERSION}). The degree order is the full scale
     * (up and back). Combines all previously learned inversions.</p>
     *
     * @param ex l'objecte exercici a configurar / the exercise object to configure
     */
    private static void setExercise_11(MyExercise ex) {
        ex.setDescription(I18n.t("earTraining.ex11.description"));
        ex.setUsePentagramaStrips(false);
        ex.setUseScreenKeyboardRight(false);
        ex.setUseMobileDo(true);
        ex.setShowNoteNames(true);

        int midiKey = ex.getMidiKey();
        ex.placeTonalContext(midiKey);
        ex.setChoice(new Integer[]{0, 2, 4, 5, 7, 9, 11, 12, 11, 9, 7, 5, 4, 2, 0});
        int[] arpeggiatura = new int[]{1, 2, 3, 2, 1};

        ex.skipCols(-2 * ex.ONE_BEAT);
        for (int root : ex.getChoice().getChoiceList()) {
            int midiRoot = root + midiKey;
            Chord chord = new Triad(root, midiKey, Triad.RANDOM_INVERSION);
            ex.placeNote(midiRoot, ex.ONE_BEAT, false);
            ex.skipCols(ex.ONE_BEAT);
            ex.placeAppendMessage(chord.toString(), 0);
            ex.placeArpeggio(chord, arpeggiatura, ex.ONE_BEAT, false);
            ex.skipCols(ex.ONE_BEAT);
        }
    }

    /**
     * [CA] Exercici 12: Com l'Ex11 però amb l'ordre dels graus barrejat
     * aleatòriament ({@link Utilities#shuffle}). Màxima dificultat de la
     * sèrie d'arpegis: inversió aleatòria i seqüència imprevisible.
     *
     * <p>[EN] Exercise 12: Like Ex11 but with the degree order shuffled
     * randomly ({@link Utilities#shuffle}). Maximum difficulty in the
     * arpeggio series: random inversion and unpredictable sequence.</p>
     *
     * @param ex l'objecte exercici a configurar / the exercise object to configure
     */
    private static void setExercise_12(MyExercise ex) {
        ex.setDescription(I18n.t("earTraining.ex12.description"));
        ex.setUsePentagramaStrips(false);
        ex.setUseScreenKeyboardRight(false);
        ex.setUseMobileDo(true);
        ex.setShowNoteNames(true);

        int midiKey = ex.getMidiKey();
        ex.placeTonalContext(midiKey);
        Integer[] choice = new Integer[]{0, 2, 4, 5, 7, 9, 11, 12, 11, 9, 7, 5, 4, 2, 0};
        Utilities.shuffle(choice);
        ex.setChoice(choice);
        int[] arpeggiatura = new int[]{1, 2, 3, 2, 1};

        ex.skipCols(-2 * ex.ONE_BEAT);
        for (int root : ex.getChoice().getChoiceList()) {
            int midiRoot = root + midiKey;
            Chord chord = new Triad(root, midiKey, Triad.RANDOM_INVERSION);
            ex.placeNote(midiRoot, ex.ONE_BEAT, false);
            ex.skipCols(ex.ONE_BEAT);
            ex.placeAppendMessage(chord.toString(), 0);
            ex.placeArpeggio(chord, arpeggiatura, ex.ONE_BEAT, false);
            ex.skipCols(ex.ONE_BEAT);
        }
    }

    /**
     * [CA] Exercici 13: Reconeixement d'acords tríada en estat fonamental
     * escollits aleatòriament entre un subconjunt de graus (I, II, IV, V, VI
     * i equivalents a l'octava inferior). Usa bandes de pentagrama mòbils.
     * El nom de l'acord apareix amb retard.
     *
     * <p>[EN] Exercise 13: Recognition of root-position triads chosen randomly
     * from a subset of degrees (I, II, IV, V, VI and lower-octave equivalents).
     * Uses mobile pentagrama strips. The chord name appears with a delay.</p>
     *
     * @param ex l'objecte exercici a configurar / the exercise object to configure
     */
    private static void setExercise_13(MyExercise ex) {
        ex.setDescription(I18n.t("earTraining.ex13.description"));
        ex.setUsePentagramaStrips(true);
        ex.setUseScreenKeyboardRight(true);
        ex.setUseMobileDo(true);
        ex.setShowNoteNames(true);
        ex.setDelay(4);

        int midiKey = ex.getMidiKey();
        ex.setChoice(new Integer[]{0, 2, 5, 7, 9, -12, -10, -7, -5, -3});
        while (!ex.isOver()) {
            int root = Utilities.randFromList(ex.getChoice().getChoiceList());
            Chord chord = new Triad(root, midiKey, Triad.ROOT_POSITION);
            ex.placeAppendMessage(chord.toString(), ex.MESSAGE_DELAY);
            ex.placeChord(chord, 2 * ex.ONE_BEAT - 1, false);
            ex.skipCols(1);
        }
    }

    /**
     * [CA] Exercici 14: Com l'Ex13 però amb tots els graus diatònics
     * de l'escala major (I a VII en ambdues octaves). Triades en estat
     * fonamental escollides aleatòriament.
     *
     * <p>[EN] Exercise 14: Like Ex13 but with all diatonic degrees of the
     * major scale (I through VII in both octaves). Root-position triads
     * chosen randomly.</p>
     *
     * @param ex l'objecte exercici a configurar / the exercise object to configure
     */
    private static void setExercise_14(MyExercise ex) {
        ex.setDescription(I18n.t("earTraining.ex14.description"));
        ex.setUsePentagramaStrips(true);
        ex.setUseScreenKeyboardRight(true);
        ex.setUseMobileDo(true);
        ex.setShowNoteNames(true);
        ex.setDelay(4);

        int midiKey = ex.getMidiKey();
        ex.setChoice(new Integer[]{0, 2, 4, 5, 7, 9, 11, -12, -10, -8, -7, -5, -3, -1});
        while (!ex.isOver()) {
            int root = Utilities.randFromList(ex.getChoice().getChoiceList());
            Chord chord = new Triad(root, midiKey, Triad.ROOT_POSITION);
            ex.placeAppendMessage(chord.toString(), ex.MESSAGE_DELAY);
            ex.placeChord(chord, 2 * ex.ONE_BEAT - 1, false);
            ex.skipCols(1);
        }
    }

    /**
     * [CA] Exercici 15: Triades en inversió aleatòria escollides d'un
     * subconjunt de graus. Tempo lent (40 bpm) per facilitar la identificació.
     * Usa bandes de pentagrama mòbils. El nom de l'acord apareix amb retard.
     *
     * <p>[EN] Exercise 15: Triads in random inversion chosen from a subset
     * of degrees. Slow tempo (40 bpm) to facilitate identification. Uses
     * mobile pentagrama strips. The chord name appears with a delay.</p>
     *
     * @param ex l'objecte exercici a configurar / the exercise object to configure
     */
    private static void setExercise_15(MyExercise ex) {
        ex.setDescription(I18n.t("earTraining.ex15.description"));
        ex.setUsePentagramaStrips(true);
        ex.setUseScreenKeyboardRight(true);
        ex.setUseMobileDo(true);
        ex.setShowNoteNames(true);
        ex.setDelay(4);
        MyTempo.setTempo(40);

        ex.setChoice(new Integer[]{0, 2, 4, 5, 7, 9, -12, -10, -8, -7, -5, -3});
        int midiKey = ex.getMidiKey();
        while (!ex.isOver()) {
            int root = Utilities.randFromList(ex.getChoice().getChoiceList());
            Chord chord = new Triad(root, midiKey, Triad.RANDOM_INVERSION);
            ex.placeAppendMessage(chord.toString(), ex.MESSAGE_DELAY);
            ex.placeChord(chord, 2 * ex.ONE_BEAT - 1, false);
            ex.skipCols(1);
        }
    }

    /**
     * [CA] Exercici 16: Triades en inversió aleatòria de tots els graus
     * diatònics. Després de cada acord bloquejat (3 negres) es toca la
     * nota arrel sola (1 negra), reforçant la connexió entre l'acord
     * i el seu fonamental.
     *
     * <p>[EN] Exercise 16: Random-inversion triads on all diatonic degrees.
     * After each blocked chord (3 quarter notes) the root note sounds alone
     * (1 quarter note), reinforcing the link between the chord and its root.</p>
     *
     * @param ex l'objecte exercici a configurar / the exercise object to configure
     */
    private static void setExercise_16(MyExercise ex) {
        ex.setDescription(I18n.t("earTraining.ex16.description"));
        ex.setUsePentagramaStrips(false);
        ex.setUseScreenKeyboardRight(true);
        ex.setUseMobileDo(true);
        ex.setShowNoteNames(true);
        ex.setDelay(4);

        int[] arpeggiatura = new int[]{1, 2, 3, 2, 1};
        ex.setChoice(new Integer[]{0, 2, 4, 5, 7, 9, 11, -12, -10, -8, -7, -5, -3, -1});
        int midiKey = ex.getMidiKey();
        while (!ex.isOver()) {
            int root = Utilities.randFromList(ex.getChoice().getChoiceList());
            Chord chord = new Triad(root, midiKey, Triad.RANDOM_INVERSION);
            ex.placeAppendMessage(chord.toString(), ex.MESSAGE_DELAY);
            ex.placeChord(chord, 3 * ex.ONE_BEAT - 1, false);
            ex.skipCols(1);
            ex.placeNote(root + midiKey, ex.ONE_BEAT - 1, false);
            ex.skipCols(1);
        }
    }

    /**
     * [CA] Exercici 17: L'exercici més complet de la família. Per a cada acord
     * aleatori en inversió aleatòria es combinen quatre elements: acord bloquejat
     * (1 compàs), arpegi (patró 1-2-3-2-1 a mig temps), acord bloquejat breu
     * (2 negres) i nota arrel sola (2 negres). Entrena simultàniament el
     * reconeixement i l'entonació del fonamental.
     *
     * <p>[EN] Exercise 17: The most complete exercise in the family. For each
     * random chord in random inversion, four elements are combined: blocked chord
     * (1 bar), arpeggio (pattern 1-2-3-2-1 at half speed), short blocked chord
     * (2 quarter notes) and root note alone (2 quarter notes). Simultaneously
     * trains chord recognition and root-note singing.</p>
     *
     * @param ex l'objecte exercici a configurar / the exercise object to configure
     */
    private static void setExercise_17(MyExercise ex) {
        ex.setDescription(I18n.t("earTraining.ex17.description"));
        ex.setUsePentagramaStrips(false);
        ex.setUseScreenKeyboardRight(true);
        ex.setUseMobileDo(true);
        ex.setShowNoteNames(true);
        ex.setDelay(4);

        int[] arpeggiatura = new int[]{1, 2, 3, 2, 1};
        ex.setChoice(new Integer[]{0, 2, 4, 5, 7, 9, 11, -12, -10, -8, -7, -5, -3, -1});
        int midiKey = ex.getMidiKey();
        while (!ex.isOver()) {
            int root = Utilities.randFromList(ex.getChoice().getChoiceList());
            Chord chord = new Triad(root, midiKey, Triad.RANDOM_INVERSION);
            ex.placeAppendMessage(chord.toString(), ex.MESSAGE_DELAY);
            ex.placeChord(chord, ex.ONE_BAR - 1, false);
            ex.skipCols(1);
            ex.placeArpeggio(chord, arpeggiatura, ex.HALF_BEAT, false);
            ex.skipCols(ex.HALF_BEAT + ex.ONE_BEAT);
            ex.placeChord(chord, 2 * ex.ONE_BEAT - 1, false);
            ex.skipCols(1);
            ex.placeNote(root + midiKey, 2 * ex.ONE_BEAT - 1, false);
            ex.skipCols(1);
        }
    }
}