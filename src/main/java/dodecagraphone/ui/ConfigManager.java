package dodecagraphone.ui;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Gestor de fitxers de configuració `.properties` que conserva comentaris i format original.
 * Permet llegir, actualitzar i desar valors sense perdre cap línia del fitxer.
 *
 * ---
 *
 * Configuration file manager for `.properties` that preserves comments and original formatting.
 * Allows reading, updating, and saving values without losing any line in the file.
 *
 * @author Equip Dodecaphenium
 */
public class ConfigManager {

    private final File configFile;
    private final List<String> lines = new ArrayList<>();
    private final Map<String, Integer> keyLineMap = new HashMap<>();

    /**
     * Crea un gestor per al fitxer especificat i en carrega el contingut.
     * 
     * ---
     * 
     * Creates a manager for the given file and loads its content.
     * 
     * @param path Ruta al fitxer de configuració / Path to the configuration file
     * @throws IOException Si falla la lectura del fitxer / If file reading fails
     */
    public ConfigManager(String path) throws IOException {
        this.configFile = new File(path);
        load();
    }

    /**
     * Carrega el contingut del fitxer i construeix la taula d'índexs de claus.
     * 
     * ---
     * 
     * Loads the file content and builds the key index table.
     * 
     * @throws IOException Si falla la lectura / If reading fails
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
     * Obté el valor d'una clau existent, o un valor per defecte si no existeix.
     * 
     * ---
     * 
     * Gets the value of a key, or a default if it doesn't exist.
     * 
     * @param key Clau / Key
     * @param defaultValue Valor per defecte / Default value
     * @return Valor trobat o per defecte / Found or default value
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
     * Assigna un valor a una clau, modificant o afegint la línia corresponent.
     * 
     * ---
     * 
     * Sets a value for a key, updating or appending the relevant line.
     * 
     * @param key Clau / Key
     * @param value Valor / Value
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
     * Desa totes les línies al fitxer, preservant els comentaris i l'estructura original.
     * 
     * ---
     * 
     * Saves all lines to the file, preserving comments and structure.
     * 
     * @throws IOException Si falla l'escriptura / If writing fails
     */
    public void save() throws IOException {
        Files.write(configFile.toPath(), lines, StandardCharsets.UTF_8);
    }

    /**
     * Obté el valor com a double, amb un valor per defecte si no es pot convertir.
     * 
     * ---
     * 
     * Gets the value as double, with fallback if parsing fails.
     */
    public double getDouble(String key, double defaultValue) {
        try {
            return Double.parseDouble(get(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Obté el valor com a boolean.
     * 
     * ---
     * 
     * Gets the value as boolean.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(get(key, String.valueOf(defaultValue)));
    }
    
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
