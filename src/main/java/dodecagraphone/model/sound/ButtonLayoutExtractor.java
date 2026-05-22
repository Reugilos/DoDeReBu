/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.model.sound;

/**
 * [CA] Eina d'extracció de la disposició de botons des del fitxer font {@code MyButtonPanel.java}.
 * Analitza les declaracions de {@code MyToggle} i {@code MyButton} amb una expressió regular
 * i escriu el resultat en un fitxer de text {@code ButtonLayout.txt}.
 * Usada com a utilitat de desenvolupament per generar documentació de la disposició de la UI.
 * <p>
 * [EN] Tool for extracting the button layout from the {@code MyButtonPanel.java} source file.
 * Parses {@code MyToggle} and {@code MyButton} declarations with a regular expression
 * and writes the result to a text file {@code ButtonLayout.txt}.
 * Used as a development utility to generate UI layout documentation.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
import java.io.*;
import java.util.regex.*;

public class ButtonLayoutExtractor {

    /**
     * [CA] Punt d'entrada principal. Llegeix {@code MyButtonPanel.java}, extreu les
     * declaracions de botons i les escriu al fitxer {@code ButtonLayout.txt} amb el format:
     * {@code id;nom;isToggle;fila;columna;textOn;textOff}.
     * <p>
     * [EN] Main entry point. Reads {@code MyButtonPanel.java}, extracts button
     * declarations and writes them to {@code ButtonLayout.txt} with the format:
     * {@code id;name;isToggle;row;column;textOn;textOff}.
     *
     * @param args [CA] arguments de la línia d'ordres (no s'usen) / [EN] command-line arguments (unused)
     */
    public static void main(String[] args) {
        String inputFilePath = "MyButtonPanel.java";
        String outputFilePath = "ButtonLayout.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputFilePath))) {

            String line;
            // Adjusted regex pattern to match both MyToggle and MyButton formats
            Pattern pattern = Pattern.compile("new\\s+(MyToggle|MyButton)\\((\\d+),\\s*(\\w+),\\s*(\\w+),\\s*\\w+,\\s*\\w+,\\s*\\w+,\\s*\\w+,\\s*\"([^\"]+)\"(?:,\\s*\"([^\"]+)\")?\\)");

            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()) {
                    String type = matcher.group(1);
                    String id = matcher.group(2);
                    String col = matcher.group(3);
                    String row = matcher.group(4);
                    String textOn = matcher.group(5);
                    String textOff = matcher.group(6) != null ? matcher.group(6) : "";  // textOff only for MyToggle
                    boolean isToggle = type.equals("MyToggle");

                    // Format output as: id;name;isToggle;row;column;textOn;textOff
                    bw.write(id + ";Button" + id + ";" + isToggle + ";" + row + ";" + col + ";" + textOn + ";" + textOff);
                    bw.newLine();
                }
            }

            System.out.println("Button layout successfully extracted to " + outputFilePath);

        } catch (IOException e) {
            System.err.println("Error reading or writing files: " + e.getMessage());
        }
    }
}
