/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * [CA] Diàleg genèric per a la selecció de múltiples grups d'opcions
 * (botons de ràdio), amb un camp "Altre" global opcional que substitueix
 * tots els grups.
 * <p>
 * [EN] Generic dialog for selecting multiple option groups (radio buttons),
 * with an optional global "Other" field that overrides all groups.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MultiOptionDialog {

	/**
	 * [CA] Opció individual dins d'un grup de ràdio.
	 * <p>
	 * [EN] Individual choice within a radio-button group.
	 *
	 * @param <T> [CA] Tipus del valor de l'opció / [EN] Type of the option value
	 */
	public static final class Choice<T> {
		/** [CA] Text a mostrar / [EN] Display label */
		public final String label;
		/** [CA] Valor associat / [EN] Associated value */
		public final T value;

		/**
		 * [CA] Crea una opció amb etiqueta i valor.
		 * <p>
		 * [EN] Creates a choice with a label and value.
		 *
		 * @param label [CA] Text a mostrar / [EN] Display text
		 * @param value [CA] Valor associat / [EN] Associated value
		 */
		public Choice(String label, T value) {
			this.label = label;
			this.value = value;
		}
	}

	/**
	 * [CA] Especificació d'un grup de botons de ràdio.
	 * <p>
	 * [EN] Specification for a radio-button group.
	 *
	 * @param <T> [CA] Tipus del valor de les opcions / [EN] Type of option values
	 */
	public static final class GroupSpec<T> {
		/** [CA] Identificador del grup / [EN] Group identifier */
		public final String id;
		/** [CA] Títol del grup (vora del panell) / [EN] Group title (panel border) */
		public final String title;
		/** [CA] Llista d'opcions / [EN] List of choices */
		public final List<Choice<T>> options;
		/** [CA] Valor per defecte / [EN] Default value */
		public final T defaultValue;

		/**
		 * [CA] Crea una especificació de grup.
		 * <p>
		 * [EN] Creates a group specification.
		 *
		 * @param id           [CA] Identificador del grup / [EN] Group identifier
		 * @param title        [CA] Títol del grup / [EN] Group title
		 * @param options      [CA] Llista d'opcions / [EN] List of options
		 * @param defaultValue [CA] Valor per defecte / [EN] Default value
		 */
		public GroupSpec(String id, String title, List<Choice<T>> options, T defaultValue) {
			this.id = id;
			this.title = title;
			this.options = options;
			this.defaultValue = defaultValue;
		}
	}

	/**
	 * [CA] Especificació del camp "Altre" global que substitueix tots els grups.
	 * <p>
	 * [EN] Specification for the global "Other" field that overrides all groups.
	 */
	public static final class GlobalOtherSpec {
		/** [CA] Identificador especial per al resultat / [EN] Special id for the result */
		public final String id;
		/** [CA] Etiqueta davant del camp de text / [EN] Label before the text field */
		public final String otherLabel;
		/** [CA] Text per defecte al camp / [EN] Default text in the field */
		public final String defaultOtherText;

		/**
		 * [CA] Crea una especificació "Altre" global.
		 * <p>
		 * [EN] Creates a global "Other" specification.
		 *
		 * @param id               [CA] Identificador / [EN] Identifier
		 * @param otherLabel       [CA] Etiqueta del camp / [EN] Field label
		 * @param defaultOtherText [CA] Text per defecte / [EN] Default text
		 */
		public GlobalOtherSpec(String id, String otherLabel, String defaultOtherText) {
			this.id = id;
			this.otherLabel = otherLabel;
			this.defaultOtherText = defaultOtherText;
		}
	}

	/**
	 * [CA] Selecció realitzada per l'usuari per a un grup o camp "Altre".
	 * <p>
	 * [EN] User selection for a group or "Other" field.
	 */
	public static final class Selection {
		/** [CA] Identificador del grup / [EN] Group identifier */
		public final String id;
		/** [CA] Valor seleccionat (null si isOther) / [EN] Selected value (null if isOther) */
		public final Object value;
		/** [CA] true si l'usuari ha triat l'opció "Altre" / [EN] true if the user chose "Other" */
		public final boolean isOther;
		/** [CA] Text introduït al camp "Altre" / [EN] Text entered in the "Other" field */
		public final String otherText;

		/**
		 * [CA] Crea una selecció.
		 * <p>
		 * [EN] Creates a selection.
		 *
		 * @param id        [CA] Identificador del grup / [EN] Group identifier
		 * @param value     [CA] Valor seleccionat / [EN] Selected value
		 * @param isOther   [CA] Indica si és l'opció "Altre" / [EN] Whether this is the "Other" option
		 * @param otherText [CA] Text lliure de l'opció "Altre" / [EN] Free text from "Other"
		 */
		public Selection(String id, Object value, boolean isOther, String otherText) {
			this.id = id;
			this.value = value;
			this.isOther = isOther;
			this.otherText = otherText;
		}
	}

	/**
	 * [CA] Resultat complet d'un {@link MultiOptionDialog}, indexat per
	 * identificador de grup.
	 * <p>
	 * [EN] Full result of a {@link MultiOptionDialog}, indexed by group id.
	 */
	public static final class MultiResult {
		private final Map<String, Selection> byId;

		/**
		 * [CA] Construeix un resultat a partir d'una llista de seleccions.
		 * <p>
		 * [EN] Builds a result from a list of selections.
		 *
		 * @param selections [CA] Llista de seleccions / [EN] List of selections
		 */
		public MultiResult(List<Selection> selections) {
			Map<String, Selection> map = new HashMap<>();
			for (Selection sel : selections) {
				map.put(sel.id, sel);
			}
			this.byId = Collections.unmodifiableMap(map);
		}

		/**
		 * [CA] Retorna la selecció per a l'identificador donat.
		 * <p>
		 * [EN] Returns the selection for the given identifier.
		 *
		 * @param id [CA] Identificador del grup / [EN] Group identifier
		 * @return [CA] La selecció, o null si no existeix / [EN] The selection, or null if absent
		 */
		public Selection get(String id) {
			return byId.get(id);
		}

		/**
		 * [CA] Retorna el valor tipat de la selecció per a l'identificador donat.
		 * <p>
		 * [EN] Returns the typed value of the selection for the given identifier.
		 *
		 * @param <T> [CA] Tipus esperat / [EN] Expected type
		 * @param id  [CA] Identificador del grup / [EN] Group identifier
		 * @param cls [CA] Classe esperada (per a la conversió) / [EN] Expected class (for casting)
		 * @return [CA] Valor tipat, o null si no existeix / [EN] Typed value, or null if absent
		 */
		@SuppressWarnings("unchecked")
		public <T> T getValue(String id, Class<T> cls) {
			Selection sel = byId.get(id);
			if (sel == null) return null;
			return (T) sel.value;
		}
	}

	private static final class GroupState {
		public final String id;
		public final Object[] valueRef;

		public GroupState(String id, Object[] valueRef) {
			this.id = id;
			this.valueRef = valueRef;
		}
	}

	/**
	 * [CA] Mostra un diàleg sense camp "Altre" global.
	 * <p>
	 * [EN] Shows a dialog without a global "Other" field.
	 *
	 * @param parent [CA] Component pare / [EN] Parent component
	 * @param title  [CA] Títol del diàleg / [EN] Dialog title
	 * @param groups [CA] Grups d'opcions / [EN] Option groups
	 * @return [CA] Resultat de la selecció, o null si es cancel·la /
	 *         [EN] Selection result, or null if cancelled
	 */
	public static MultiResult showDialog(Component parent, String title, GroupSpec<?>... groups) {
		return showDialogWithGlobalOther(parent, title, null, groups);
	}

	/**
	 * [CA] Mostra un diàleg amb camp "Altre" global opcional. Si l'usuari
	 * activa l'opció "Altre" i introdueix text, aquest substitueix tots els
	 * grups de ràdio.
	 * <p>
	 * [EN] Shows a dialog with an optional global "Other" field. If the user
	 * activates "Other" and enters text, it overrides all radio groups.
	 *
	 * @param parent      [CA] Component pare / [EN] Parent component
	 * @param title       [CA] Títol del diàleg / [EN] Dialog title
	 * @param globalOther [CA] Especificació del camp "Altre" (null per ometre) /
	 *                    [EN] "Other" field specification (null to omit)
	 * @param groups      [CA] Grups d'opcions / [EN] Option groups
	 * @return [CA] Resultat de la selecció, o null si es cancel·la /
	 *         [EN] Selection result, or null if cancelled
	 */
	public static MultiResult showDialogWithGlobalOther(
			Component parent,
			String title,
			GlobalOtherSpec globalOther,
			GroupSpec<?>... groups
	) {
		JDialog dialog = new JDialog((Frame) null, title, true);
		dialog.setLayout(new BorderLayout(10, 10));

		JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		dialog.add(contentPanel, BorderLayout.CENTER);

		// ----- Global mode selector: Options vs Other
		final boolean[] isGlobalOther = new boolean[]{false};
		final JTextField globalOtherField;

		final List<JPanel> groupPanels = new ArrayList<>();

		JPanel modePanel = null;
		if (globalOther != null) {
			modePanel = new JPanel();
			modePanel.setLayout(new BoxLayout(modePanel, BoxLayout.X_AXIS));

			JRadioButton optionsMode = new JRadioButton(I18n.t("dialog.multi.mode.options"));
			JRadioButton otherMode = new JRadioButton(I18n.t("dialog.multi.mode.other"));
			ButtonGroup modeGroup = new ButtonGroup();
			modeGroup.add(optionsMode);
			modeGroup.add(otherMode);
			optionsMode.setSelected(true);

			JLabel otherLabel = new JLabel(globalOther.otherLabel != null ? globalOther.otherLabel : I18n.t("dialog.multi.other.label"));

			// Small field ~ 3 cm (approx): 10 columns
			globalOtherField = new JTextField(globalOther.defaultOtherText != null ? globalOther.defaultOtherText : "", 10);
			globalOtherField.setMaximumSize(new Dimension(globalOtherField.getPreferredSize().width, globalOtherField.getPreferredSize().height));
			globalOtherField.setEnabled(false);
			otherLabel.setEnabled(false);

			optionsMode.addActionListener(e -> {
				isGlobalOther[0] = false;
				globalOtherField.setEnabled(false);
				otherLabel.setEnabled(false);
				for (JPanel gp : groupPanels) gp.setEnabled(true);
				setEnabledRecursively(groupPanels, true);
			});

			otherMode.addActionListener(e -> {
				isGlobalOther[0] = true;
				globalOtherField.setEnabled(true);
				otherLabel.setEnabled(true);
				setEnabledRecursively(groupPanels, false);
			});

			modePanel.add(optionsMode);
			modePanel.add(Box.createHorizontalStrut(10));
			modePanel.add(otherMode);
			modePanel.add(Box.createHorizontalStrut(10));
			modePanel.add(otherLabel);
			modePanel.add(Box.createHorizontalStrut(5));
			modePanel.add(globalOtherField);

			contentPanel.add(modePanel, BorderLayout.NORTH);
		} else {
			globalOtherField = null;
		}

		// ----- Groups
		JPanel groupsPanel = new JPanel();
		groupsPanel.setLayout(new BoxLayout(groupsPanel, BoxLayout.Y_AXIS));

		final List<GroupState> groupStates = new ArrayList<>();

		for (GroupSpec<?> groupSpecRaw : groups) {
			final GroupSpec<?> groupSpec = groupSpecRaw;

			JPanel groupPanel = new JPanel();
			groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
			groupPanel.setBorder(BorderFactory.createTitledBorder(groupSpec.title));

			ButtonGroup buttonGroup = new ButtonGroup();
			final Object[] selectedValueRef = new Object[]{groupSpec.defaultValue};

			boolean anySelected = false;

			for (Choice<?> choiceRaw : groupSpec.options) {
				final Choice<?> choice = choiceRaw;
				final Object optionValue = choice.value;

				JRadioButton radio = new JRadioButton(choice.label);
				buttonGroup.add(radio);
				groupPanel.add(radio);

				boolean isDefault = (groupSpec.defaultValue != null && groupSpec.defaultValue.equals(optionValue));
				if (!anySelected && isDefault) {
					radio.setSelected(true);
					anySelected = true;
					selectedValueRef[0] = optionValue;
				}

				radio.addActionListener(e -> selectedValueRef[0] = optionValue);
			}

			if (!anySelected && !groupSpec.options.isEmpty()) {
				Choice<?> first = groupSpec.options.get(0);
				selectedValueRef[0] = first.value;

				for (Component c : groupPanel.getComponents()) {
					if (c instanceof JRadioButton rb && rb.getText().equals(first.label)) {
						rb.setSelected(true);
						break;
					}
				}
			}

			groupsPanel.add(groupPanel);
			groupsPanel.add(Box.createVerticalStrut(10));

			groupPanels.add(groupPanel);
			groupStates.add(new GroupState(groupSpec.id, selectedValueRef));
		}

		JScrollPane scroll = new JScrollPane(groupsPanel);
		scroll.setBorder(null);
		contentPanel.add(scroll, BorderLayout.CENTER);

		// ----- Buttons
		JButton okButton = new JButton(I18n.t("btn.ok"));
		JButton cancelButton = new JButton(I18n.t("btn.cancel"));
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		dialog.add(buttonPanel, BorderLayout.SOUTH);

		final MultiResult[] resultRef = new MultiResult[]{null};

		okButton.addActionListener(e -> {
			// Global OTHER overrides all
			if (globalOther != null && isGlobalOther[0]) {
				String otherText = (globalOtherField != null && globalOtherField.getText() != null)
						? globalOtherField.getText().trim()
						: "";
				if (otherText.isEmpty()) {
					JOptionPane.showMessageDialog(
							dialog,
							I18n.f("dialog.multi.other_required", globalOther.id),
							title,
							JOptionPane.WARNING_MESSAGE
					);
					return;
				}

				List<Selection> out = new ArrayList<>();
				out.add(new Selection(globalOther.id, null, true, otherText));
				resultRef[0] = new MultiResult(out);
				dialog.dispose();
				return;
			}

			// Normal options mode
			List<Selection> selections = new ArrayList<>();
			for (GroupState st : groupStates) {
				selections.add(new Selection(st.id, st.valueRef[0], false, null));
			}

			resultRef[0] = new MultiResult(selections);
			dialog.dispose();
		});

		cancelButton.addActionListener(e -> {
			resultRef[0] = null;
			dialog.dispose();
		});

		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.getRootPane().setDefaultButton(okButton);
		dialog.setVisible(true);

		return resultRef[0];
	}

	private static void setEnabledRecursively(List<JPanel> panels, boolean enabled) {
		for (JPanel p : panels) {
			p.setEnabled(enabled);
			for (Component c : p.getComponents()) {
				c.setEnabled(enabled);
			}
		}
	}
}
