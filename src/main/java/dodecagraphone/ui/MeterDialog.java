package dodecagraphone.ui;

import java.awt.Component;
import java.util.List;

public class MeterDialog {

	public enum MeterType {
		SIMPLE,
		COMPOUND
	}

	public enum MeterPattern {
		BINARY,
		TERNARY,
		QUATERNARY,
		OTHER
	}

	public static final class MeterData {
		public final MeterType meterType;
		public final MeterPattern meterPattern;
		public final String otherText; // only when meterPattern == OTHER

		public MeterData(MeterType meterType, MeterPattern meterPattern, String otherText) {
			this.meterType = meterType;
			this.meterPattern = meterPattern;
			this.otherText = otherText;
		}

		public boolean isOther() {
			return meterPattern == MeterPattern.OTHER;
		}
	}

	public static MeterData show(
			Component parent,
			MeterType defaultMeterType,
			MeterPattern defaultMeterPattern,
			String defaultTimeSignatureText
	) {
		MultiOptionDialog.GroupSpec<MeterType> typeGroup =
				new MultiOptionDialog.GroupSpec<>(
						"type",
						I18n.t("dialog.meter.group.type"),
						List.of(
								new MultiOptionDialog.Choice<>(I18n.t("dialog.meter.type.simple"), MeterType.SIMPLE),
								new MultiOptionDialog.Choice<>(I18n.t("dialog.meter.type.compound"), MeterType.COMPOUND)
						),
						(defaultMeterType != null ? defaultMeterType : MeterType.SIMPLE)
				);

		MultiOptionDialog.GroupSpec<MeterPattern> patternGroup =
				new MultiOptionDialog.GroupSpec<>(
						"pattern",
						I18n.t("dialog.meter.group.pattern"),
						List.of(
								new MultiOptionDialog.Choice<>(I18n.t("dialog.meter.pattern.binary"), MeterPattern.BINARY),
								new MultiOptionDialog.Choice<>(I18n.t("dialog.meter.pattern.ternary"), MeterPattern.TERNARY),
								new MultiOptionDialog.Choice<>(I18n.t("dialog.meter.pattern.quaternary"), MeterPattern.QUATERNARY)
						),
						(defaultMeterPattern != null ? defaultMeterPattern : MeterPattern.BINARY)
				);

		MultiOptionDialog.GlobalOtherSpec globalOther =
				new MultiOptionDialog.GlobalOtherSpec(
						"timeSignature",
						I18n.t("dialog.multi.other.label"),
						(defaultTimeSignatureText != null ? defaultTimeSignatureText : "")
				);

		MultiOptionDialog.MultiResult result =
				MultiOptionDialog.showDialogWithGlobalOther(
						parent,
						I18n.t("dialog.meter.title"),
						globalOther,
						typeGroup,
						patternGroup
				);

		if (result == null) return null;

		MultiOptionDialog.Selection tsSel = result.get("timeSignature");
		if (tsSel != null && tsSel.isOther) {
			MeterData parsed = timeSignature2MeterData(tsSel.otherText);
			if (parsed != null) return parsed;
			return new MeterData(MeterType.SIMPLE, MeterPattern.OTHER, tsSel.otherText);
		}

		MeterType meterType = result.getValue("type", MeterType.class);
		MeterPattern meterPattern = result.getValue("pattern", MeterPattern.class);

		return new MeterData(meterType, meterPattern, null);
	}

	public static String meterData2TimeSignature(MeterData data) {
		if (data == null) return null;

		int denominator = 4; // default
		if (data.meterPattern == MeterPattern.OTHER) {
			if (data.otherText == null) return null;
			String ts = data.otherText.trim();
			return ts.isEmpty() ? null : ts;
		}

		int numerator;
		if (data.meterType == MeterType.COMPOUND) {
        		denominator = 8; // default
			switch (data.meterPattern) {
				case BINARY: numerator = 6; break;
				case TERNARY: numerator = 9; break;
				case QUATERNARY: numerator = 12; break;
				default: return null;
			}
		} else {
			switch (data.meterPattern) {
				case BINARY: numerator = 2; break;
				case TERNARY: numerator = 3; break;
				case QUATERNARY: numerator = 4; break;
				default: return null;
			}
		}

		return numerator + "/" + denominator;
	}

	public static MeterData timeSignature2MeterData(String timeSignature) {
		if (timeSignature == null) return null;

		String ts = timeSignature.trim();
		if (ts.isEmpty()) return null;

		String[] parts = ts.split("/");
		if (parts.length != 2) {
			return new MeterData(MeterType.SIMPLE, MeterPattern.OTHER, ts);
		}

		int numerator;
		int denominator;
		try {
			numerator = Integer.parseInt(parts[0].trim());
			denominator = Integer.parseInt(parts[1].trim());
		} catch (NumberFormatException ex) {
			return new MeterData(MeterType.SIMPLE, MeterPattern.OTHER, ts);
		}

		MeterType meterType;
		if (numerator == 6 || numerator == 9 || numerator == 12) {
			meterType = MeterType.COMPOUND;
		} else if (numerator == 2 || numerator == 3 || numerator == 4) {
			meterType = MeterType.SIMPLE;
		} else {
			return new MeterData(MeterType.SIMPLE, MeterPattern.OTHER, numerator + "/" + denominator);
		}

		MeterPattern meterPattern;
		if (numerator == 2 || numerator == 6) {
			meterPattern = MeterPattern.BINARY;
		} else if (numerator == 3 || numerator == 9) {
			meterPattern = MeterPattern.TERNARY;
		} else if (numerator == 4 || numerator == 12) {
			meterPattern = MeterPattern.QUATERNARY;
		} else {
			meterPattern = MeterPattern.OTHER;
		}

		String otherText = (meterPattern == MeterPattern.OTHER) ? (numerator + "/" + denominator) : null;
		return new MeterData(meterType, meterPattern, otherText);
	}
}
