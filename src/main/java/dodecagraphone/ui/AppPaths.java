package dodecagraphone.ui;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Objects;
import java.util.Optional;

public final class AppPaths {

    private static final String DEFAULT_CONFIG_RESOURCE = "/defaults/config.properties";

    private AppPaths() {
    }

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

    public static Path getConfigDir() {
        return getAppBaseDir().resolve("config");
    }

    public static Path getUserConfigPath() {
        return getConfigDir().resolve("config.properties");
    }

    /**
     * Copia el config per defecte si no existeix, filtrant els marcadors #i18n:
     * (els comentaris localitzats s'escriuen al primer save, quan I18n ja està carregat).
     *
     * @return true si s'ha creat el fitxer, false si ja existia
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
