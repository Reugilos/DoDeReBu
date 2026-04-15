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
 * Generic dialog for selecting multiple option groups (radio buttons),
 * with optional global "Other" that overrides all groups.
 */
public class MultiOptionDialog {

	public static final class Choice<T> {
		public final String label;
		public final T value;

		public Choice(String label, T value) {
			this.label = label;
			this.value = value;
		}
	}

	public static final class GroupSpec<T> {
		public final String id;
		public final String title;
		public final List<Choice<T>> options;
		public final T defaultValue;

		public GroupSpec(String id, String title, List<Choice<T>> options, T defaultValue) {
			this.id = id;
			this.title = title;
			this.options = options;
			this.defaultValue = defaultValue;
		}
	}

	public static final class GlobalOtherSpec {
		public final String id;                // special id for the result
		public final String otherLabel;        // label before text field (e.g., "Other:")
		public final String defaultOtherText;  // default text

		public GlobalOtherSpec(String id, String otherLabel, String defaultOtherText) {
			this.id = id;
			this.otherLabel = otherLabel;
			this.defaultOtherText = defaultOtherText;
		}
	}

	public static final class Selection {
		public final String id;
		public final Object value;
		public final boolean isOther;
		public final String otherText;

		public Selection(String id, Object value, boolean isOther, String otherText) {
			this.id = id;
			this.value = value;
			this.isOther = isOther;
			this.otherText = otherText;
		}
	}

	public static final class MultiResult {
		private final Map<String, Selection> byId;

		public MultiResult(List<Selection> selections) {
			Map<String, Selection> map = new HashMap<>();
			for (Selection sel : selections) {
				map.put(sel.id, sel);
			}
			this.byId = Collections.unmodifiableMap(map);
		}

		public Selection get(String id) {
			return byId.get(id);
		}

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

	public static MultiResult showDialog(Component parent, String title, GroupSpec<?>... groups) {
		return showDialogWithGlobalOther(parent, title, null, groups);
	}

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
