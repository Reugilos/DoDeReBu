/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.ui;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * [CA] Gestor de fitxers de configuració {@code .properties} que conserva
 * comentaris i format original. Permet llegir, actualitzar i desar valors
 * sense perdre cap línia del fitxer.
 * <p>
 * [EN] Configuration file manager for {@code .properties} files that preserves
 * comments and original formatting. Allows reading, updating, and saving values
 * without losing any line in the file.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public class ConfigManager {

    private final File configFile;
    private final List<String> lines = new ArrayList<>();
    private final Map<String, Integer> keyLineMap = new HashMap<>();

    /**
     * [CA] Crea un gestor per al fitxer especificat i en carrega el contingut.
     * <p>
     * [EN] Creates a manager for the given file and loads its content.
     *
     * @param path [CA] Ruta al fitxer de configuració / [EN] Path to the configuration file
     * @throws IOException [CA] Si falla la lectura del fitxer / [EN] If file reading fails
     */
    public ConfigManager(String path) throws IOException {
        this.configFile = new File(path);
        load();
    }

    /**
     * [CA] Carrega el contingut del fitxer i construeix la taula d'índexs de claus.
     * Si el fitxer no existeix, no fa res.
     * <p>
     * [EN] Loads the file content and builds the key index table.
     * If the file does not exist, does nothing.
     *
     * @throws IOException [CA] Si falla la lectura / [EN] If reading fails
     */
    public void load() throws IOException {
        lines.clear();
        keyLineMap.clear();

        if (!configFile.exists()) return;

        List<String> rawLines = Files.readAllLines(configFile.toPath(), StandardCharsets.UTF_8);
        for (int i = 0; i < rawLines.size(); i++) {
            String line = rawLines.get(i);
            lines.add(line);

            String trimmed = line.trim();
            if (!trimmed.startsWith("#") && !trimmed.startsWith("!") && trimmed.contains("=")) {
                String key = trimmed.split("=", 2)[0].trim();
                keyLineMap.put(key, i);
            }
        }
    }

    /**
     * [CA] Obté el valor d'una clau existent, o un valor per defecte si no existeix.
     * <p>
     * [EN] Gets the value of a key, or a default value if the key does not exist.
     *
     * @param key          [CA] Clau / [EN] Key
     * @param defaultValue [CA] Valor per defecte / [EN] Default value
     * @return [CA] Valor trobat o per defecte / [EN] Found value or default
     */
    public String get(String key, String defaultValue) {
        Integer index = keyLineMap.get(key);
        if (index != null) {
            String line = lines.get(index);
            String[] parts = line.split("=", 2);
            if (parts.length == 2) {
                return parts[1].trim();
            }
        }
        return defaultValue;
    }

    /**
     * [CA] Assigna un valor a una clau, modificant la línia existent o afegint-ne una de nova.
     * <p>
     * [EN] Sets a value for a key, updating the existing line or appending a new one.
     *
     * @param key   [CA] Clau / [EN] Key
     * @param value [CA] Valor / [EN] Value
     */
    public void set(String key, String value) {
        String newLine = key + "=" + value;
        Integer index = keyLineMap.get(key);

        if (index != null) {
            lines.set(index, newLine);
        } else {
            lines.add(newLine);
            keyLineMap.put(key, lines.size() - 1);
        }
    }

    /**
     * [CA] Desa totes les línies al fitxer, preservant els comentaris i l'estructura original.
     * <p>
     * [EN] Saves all lines to the file, preserving comments and the original structure.
     *
     * @throws IOException [CA] Si falla l'escriptura / [EN] If writing fails
     */
    public void save() throws IOException {
        Files.write(configFile.toPath(), lines, StandardCharsets.UTF_8);
    }

    /**
     * [CA] Obté el valor d'una clau com a {@code double}, amb un valor per defecte
     * si la clau no existeix o no es pot convertir.
     * <p>
     * [EN] Gets the value of a key as a {@code double}, with a fallback value
     * if the key is absent or cannot be parsed.
     *
     * @param key          [CA] Clau / [EN] Key
     * @param defaultValue [CA] Valor per defecte / [EN] Default value
     * @return [CA] Valor double o el default / [EN] Double value or default
     */
    public double getDouble(String key, double defaultValue) {
        try {
            return Double.parseDouble(get(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * [CA] Obté el valor d'una clau com a {@code boolean}, amb un valor per defecte
     * si la clau no existeix.
     * <p>
     * [EN] Gets the value of a key as a {@code boolean}, with a fallback if absent.
     *
     * @param key          [CA] Clau / [EN] Key
     * @param defaultValue [CA] Valor per defecte / [EN] Default value
     * @return [CA] true si el valor és "true" (insensible a majúscules) / [EN] true if value is "true" (case-insensitive)
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(get(key, String.valueOf(defaultValue)));
    }

    /**
     * [CA] Mètode de prova per demostrar l'ús de ConfigManager.
     * <p>
     * [EN] Test method demonstrating ConfigManager usage.
     *
     * @param args [CA] Arguments de línia de comandes (no s'utilitzen) / [EN] Command-line arguments (unused)
     * @throws IOException [CA] Si falla la lectura o l'escriptura / [EN] If reading or writing fails
     */
    public static void main(String[] args) throws IOException{
        ConfigManager config = new ConfigManager("config.properties");

// Llegir valors
        double volume = config.getDouble("volumeLevel", 0.8);
        boolean autosave = config.getBoolean("autosave", true);

// Actualitzar valors
        config.set("volumeLevel", String.valueOf(0.6));
        config.set("autosave", "false");

// Desa el fitxer, mantenint comentaris
        config.save();
    }
}
