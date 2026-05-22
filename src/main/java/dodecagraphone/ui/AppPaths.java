/*
 * MIT License
 * Copyright (c) 2024-2026 Pau Bofill, Claude IA
 * Llicència completa: LICENSE (arrel del projecte)
 */
package dodecagraphone.ui;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * [CA] Gestió de rutes de l'aplicació. Determina el directori base de l'app
 * (mode portable, JAR o IDE) i proporciona les rutes al directori de
 * configuració i al fitxer {@code config.properties} de l'usuari.
 * <p>
 * [EN] Application path management. Determines the app base directory
 * (portable, JAR or IDE mode) and provides paths to the config directory
 * and the user's {@code config.properties} file.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.0
 */
public final class AppPaths {

    private static final String DEFAULT_CONFIG_RESOURCE = "/defaults/config.properties";

    private AppPaths() {
    }

    /**
     * [CA] Retorna el directori base de l'aplicació. La resolució segueix
     * aquest ordre de prioritat:
     * <ol>
     *   <li>Propietat de sistema {@code app.home} (override manual).</li>
     *   <li>Mode portable: directori del {@code .exe} si el procés actual
     *       no és {@code java.exe}/{@code javaw.exe}.</li>
     *   <li>Mode JAR: directori del fitxer {@code .jar}.</li>
     *   <li>Mode IDE: pare del directori {@code target} o {@code build}.</li>
     *   <li>Fallback: {@code user.dir}.</li>
     * </ol>
     * <p>
     * [EN] Returns the application base directory, resolved in this priority order:
     * <ol>
     *   <li>System property {@code app.home} (manual override).</li>
     *   <li>Portable mode: directory of the {@code .exe} if the current process
     *       is not {@code java.exe}/{@code javaw.exe}.</li>
     *   <li>JAR mode: directory containing the {@code .jar} file.</li>
     *   <li>IDE mode: parent of the {@code target} or {@code build} directory.</li>
     *   <li>Fallback: {@code user.dir}.</li>
     * </ol>
     *
     * @return [CA] Ruta absoluta i normalitzada del directori base / [EN] Absolute normalized base directory path
     */
    public static Path getAppBaseDir() {

        // 0) Override manual
        String override = System.getProperty("app.home");
        if (override != null && !override.isBlank()) {
            return Paths.get(override.trim()).toAbsolutePath().normalize();
        }

        // 1) PORTABLE: només si el "command" és un .exe del teu app (no java.exe / javaw.exe)
        try {
            Optional<String> cmd = ProcessHandle.current().info().command();
            if (cmd.isPresent() && !cmd.get().isBlank()) {
                Path exe = Paths.get(cmd.get()).toAbsolutePath().normalize();
                String name = exe.getFileName() != null ? exe.getFileName().toString().toLowerCase() : "";

                boolean looksLikeJava
                        = name.equals("java.exe") || name.equals("javaw.exe") || name.equals("java");

                if (!looksLikeJava && Files.isRegularFile(exe) && name.endsWith(".exe")) {
                    return exe.getParent().toAbsolutePath().normalize(); // <- al costat de l'exe
                }
            }
        } catch (Exception ignore) {
        }

        // 2) IDE / JAR fallback via CodeSource
        try {
            Path location = Paths.get(AppPaths.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .toAbsolutePath().normalize();

            // Si és JAR (execució no-jpackage): base = directori del jar
            if (Files.isRegularFile(location) && location.toString().toLowerCase().endsWith(".jar")) {
                return location.getParent().toAbsolutePath().normalize();
            }

            // IDE: detecta "target" o "build" i retorna el seu pare (arrel projecte)
            Path cur = location;
            while (cur != null) {
                Path name = cur.getFileName();
                if (name != null) {
                    String s = name.toString();
                    if ("target".equalsIgnoreCase(s) || "build".equalsIgnoreCase(s)) {
                        Path projectRoot = cur.getParent();
                        if (projectRoot != null) {
                            return projectRoot.toAbsolutePath().normalize();
                        }
                    }
                }
                cur = cur.getParent();
            }

        } catch (URISyntaxException ex) {
            // ignorem i fem fallback
        } catch (Exception ex) {
            // ignorem i fem fallback
        }

        // 3) últim fallback
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    }

    /**
     * [CA] Retorna el directori de configuració de l'usuari
     * ({@code <appBaseDir>/config}).
     * <p>
     * [EN] Returns the user configuration directory
     * ({@code <appBaseDir>/config}).
     *
     * @return [CA] Ruta del directori de configuració / [EN] Path to the config directory
     */
    public static Path getConfigDir() {
        return getAppBaseDir().resolve("config");
    }

    /**
     * [CA] Retorna la ruta del fitxer de configuració de l'usuari
     * ({@code <configDir>/config.properties}).
     * <p>
     * [EN] Returns the path to the user configuration file
     * ({@code <configDir>/config.properties}).
     *
     * @return [CA] Ruta del fitxer config.properties / [EN] Path to config.properties
     */
    public static Path getUserConfigPath() {
        return getConfigDir().resolve("config.properties");
    }

    /**
     * [CA] Copia el config per defecte si no existeix, filtrant els marcadors
     * {@code #i18n:} (els comentaris localitzats s'escriuen al primer save,
     * quan I18n ja està carregat).
     * <p>
     * [EN] Copies the default config if it does not exist, filtering out
     * {@code #i18n:} markers (localized comments are written on the first save,
     * once I18n is loaded).
     *
     * @return [CA] true si s'ha creat el fitxer, false si ja existia /
     *         [EN] true if the file was created, false if it already existed
     * @throws IOException [CA] Si falla la lectura del recurs o l'escriptura del fitxer /
     *                     [EN] If reading the resource or writing the file fails
     */
    public static boolean installUserConfigIfMissing() throws IOException {
        Path cfgDir = getConfigDir();
        Files.createDirectories(cfgDir);

        Path target = getUserConfigPath();
        if (Files.exists(target)) {
            return false;
        }

        try (InputStream in = Objects.requireNonNull(
                AppPaths.class.getResourceAsStream(DEFAULT_CONFIG_RESOURCE),
                "Missing resource: " + DEFAULT_CONFIG_RESOURCE);
             BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {

            List<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.stripLeading().startsWith("#i18n:")) {
                    lines.add(line);
                }
            }
            Files.write(target, lines, StandardCharsets.UTF_8);
        }
        return true;
    }
}
