/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.exercise;

import dodecagraphone.model.component.MyExercise;

/**
 * [CA] Interfície que han d'implementar totes les famílies d'exercicis
 * d'entrenament auditiu. Una família d'exercicis agrupa un conjunt d'exercicis
 * relacionats (per exemple, {@link EarTraining} o {@link Jazz}) i és responsable
 * de configurar l'objecte {@link MyExercise} amb el contingut musical corresponent
 * a l'etiqueta de l'exercici indicada.
 * <p>
 * [EN] Interface that all ear training exercise families must implement.
 * An exercise family groups a set of related exercises (for example,
 * {@link EarTraining} or {@link Jazz}) and is responsible for configuring
 * the {@link MyExercise} object with the musical content corresponding
 * to the given exercise label.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 * @see EarTraining
 * @see Jazz
 */
public interface MyExerciseFamily {
//    String getName();
//    String getDescription();
//    String getAuthor();

    /**
     * [CA] Configura l'objecte exercici amb el contingut musical corresponent
     * a l'etiqueta indicada. Cada família defineix la seva pròpia implementació
     * i el conjunt d'etiquetes vàlides (per exemple, {@code "Ex1"}, {@code "Ex2"}, …).
     * <p>
     * [EN] Configures the exercise object with the musical content corresponding
     * to the given label. Each family defines its own implementation and the set
     * of valid labels (for example, {@code "Ex1"}, {@code "Ex2"}, …).
     *
     * @param exercise [CA] objecte exercici a configurar / [EN] exercise object to configure
     * @param label    [CA] etiqueta de l'exercici a aplicar / [EN] label of the exercise to apply
     */
    void applyExercise(MyExercise exercise, String label);
}
