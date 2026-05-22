/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.ui;

import java.awt.Component;
import java.util.List;

/**
 * [CA] Diàleg de selecció de compàs. Mostra un {@link MultiOptionDialog} per
 * triar el tipus de compàs (simple/compost) i el patró (binari/ternari/quaternari),
 * o introduir directament la fracció de compàs (ex: "6/8"). Proporciona
 * conversions entre {@link MeterData} i cadenes de text de compàs.
 * <p>
 * [EN] Time-signature selection dialog. Shows a {@link MultiOptionDialog} to
 * choose meter type (simple/compound) and pattern (binary/ternary/quaternary),
 * or to enter the time-signature fraction directly (e.g. "6/8"). Provides
 * conversions between {@link MeterData} and time-signature strings.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class MeterDialog {

	/**
	 * [CA] Tipus de compàs: simple (pulsació a la negra o blanca) o compost
	 * (pulsació a la negra amb punt).
	 * <p>
	 * [EN] Meter type: simple (beat on quarter/half note) or compound
	 * (beat on dotted quarter note).
	 */
	public enum MeterType {
		SIMPLE,
		COMPOUND
	}

	/**
	 * [CA] Patró del compàs: binari (2 o 6 temps), ternari (3 o 9 temps),
	 * quaternari (4 o 12 temps) o altre (introducció lliure).
	 * <p>
	 * [EN] Meter pattern: binary (2 or 6 beats), ternary (3 or 9 beats),
	 * quaternary (4 or 12 beats) or other (free input).
	 */
	public enum MeterPattern {
		BINARY,
		TERNARY,
		QUATERNARY,
		OTHER
	}

	/**
	 * [CA] Dades del compàs retornades pel diàleg.
	 * <p>
	 * [EN] Meter data returned by the dialog.
	 */
	public static final class MeterData {
		/** [CA] Tipus de compàs / [EN] Meter type */
		public final MeterType meterType;
		/** [CA] Patró del compàs / [EN] Meter pattern */
		public final MeterPattern meterPattern;
		/** [CA] Text lliure quan meterPattern == OTHER / [EN] Free text when meterPattern == OTHER */
		public final String otherText; // only when meterPattern == OTHER

		/**
		 * [CA] Crea un nou MeterData.
		 * <p>
		 * [EN] Creates a new MeterData.
		 *
		 * @param meterType    [CA] Tipus de compàs / [EN] Meter type
		 * @param meterPattern [CA] Patró del compàs / [EN] Meter pattern
		 * @param otherText    [CA] Text lliure (null si no és OTHER) / [EN] Free text (null if not OTHER)
		 */
		public MeterData(MeterType meterType, MeterPattern meterPattern, String otherText) {
			this.meterType = meterType;
			this.meterPattern = meterPattern;
			this.otherText = otherText;
		}

		/**
		 * [CA] Comprova si el patró és OTHER (entrada lliure).
		 * <p>
		 * [EN] Checks whether the pattern is OTHER (free input).
		 *
		 * @return [CA] true si el patró és OTHER / [EN] true if pattern is OTHER
		 */
		public boolean isOther() {
			return meterPattern == MeterPattern.OTHER;
		}
	}

	/**
	 * [CA] Mostra el diàleg de selecció de compàs i retorna les dades
	 * seleccionades per l'usuari, o null si cancel·la.
	 * <p>
	 * [EN] Shows the meter selection dialog and returns the data chosen
	 * by the user, or null if cancelled.
	 *
	 * @param parent                   [CA] Component pare per centrar el diàleg / [EN] Parent component for centering
	 * @param defaultMeterType         [CA] Tipus de compàs per defecte / [EN] Default meter type
	 * @param defaultMeterPattern      [CA] Patró per defecte / [EN] Default pattern
	 * @param defaultTimeSignatureText [CA] Text de compàs per defecte (ex: "4/4") /
	 *                                 [EN] Default time-signature text (e.g. "4/4")
	 * @return [CA] Dades de compàs seleccionades, o null si es cancel·la /
	 *         [EN] Selected meter data, or null if cancelled
	 */
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

	/**
	 * [CA] Converteix un {@link MeterData} en una cadena de compàs (ex: "4/4",
	 * "6/8"). Retorna null si les dades no es poden convertir.
	 * <p>
	 * [EN] Converts a {@link MeterData} to a time-signature string (e.g. "4/4",
	 * "6/8"). Returns null if the data cannot be converted.
	 *
	 * @param data [CA] Dades de compàs / [EN] Meter data
	 * @return [CA] Cadena de compàs o null / [EN] Time-signature string or null
	 */
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

	/**
	 * [CA] Converteix una cadena de compàs (ex: "4/4", "6/8") en un
	 * {@link MeterData}. Si la cadena no és reconeguda, retorna un
	 * {@link MeterData} de tipus OTHER.
	 * <p>
	 * [EN] Converts a time-signature string (e.g. "4/4", "6/8") to a
	 * {@link MeterData}. If the string is not recognized, returns a
	 * {@link MeterData} of type OTHER.
	 *
	 * @param timeSignature [CA] Cadena de compàs / [EN] Time-signature string
	 * @return [CA] Dades de compàs corresponents, o null si la cadena és null /
	 *         [EN] Corresponding meter data, or null if the string is null
	 */
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
