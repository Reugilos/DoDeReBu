/*
 * PolyForm Noncommercial License 1.0.0
 * Copyright (c) 2024-2026 Pau Bofill. Powered by Claude AI.
 * Full license / Llicència completa: LICENSE (project root / arrel del projecte)
 */
package dodecagraphone.ui;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * [CA] El tema gràfic: el joc de valors (colors, gruixos, fonts, mides) que fa
 * servir {@link DrawKit}. Cada tema és un fitxer de text a la carpeta
 * {@code temes}, al costat del {@code config.properties}, i el config en tria un
 * amb la clau {@code tema} (per defecte, {@code tema=moni}).
 * <p>
 * <b>D'on surten els temes.</b> El programa porta dins el tema de fàbrica
 * ({@code resources/temes/moni.properties}, l'únic de {@link #DE_FABRICA}). En
 * arrencar, el copia a la carpeta si hi falta, i <b>només</b> si hi falta: un tema
 * que l'usuari hagi editat no es trepitja mai. Per tornar a la versió de fàbrica,
 * n'hi ha prou d'esborrar-ne el fitxer. Per fer-ne de nous, es copia
 * {@code moni.properties} amb un altre nom i s'edita. El {@code classic} i el
 * {@code manual} són recursos interns que no es copien: el clàssic és la reserva
 * (vegeu més avall) i {@code tema=manual} encara es pot triar, perquè si el fitxer
 * no és a la carpeta es llegeix del recurs.
 * <p>
 * <b>La xarxa de seguretat.</b> El tema clàssic de dins del programa fa de
 * reserva per a tot: si el tema triat no existeix, si el fitxer no es pot llegir
 * o si li falta un valor (o el té mal escrit), aquell valor es pren del clàssic.
 * L'aplicació no es pot quedar mai sense colors. Els avisos van a la consola.
 * <p>
 * <b>Format dels valors.</b> Colors: {@code r,g,b} o {@code r,g,b,a}, o bé
 * {@code colorsets} quan el color l'ha de decidir {@code ColorSets} (és com
 * ho feia l'aplicació abans dels temes). Gruixos i proporcions: un número amb
 * punt decimal. Guions: {@code llarg,buit}. Estils de lletra: {@code normal}
 * o {@code negreta}. Interruptors: {@code si} o {@code no}.
 * <p>
 * El tema es llegeix una sola vegada, en arrencar: canviar-lo demana reiniciar.
 * <p>
 * [EN] The graphic theme used by {@link DrawKit}. Each theme is a text file in
 * the {@code temes} folder next to {@code config.properties}; the config picks
 * one with {@code tema=…}. Factory themes are seeded into the folder only when
 * missing (user edits are never overwritten). The built-in classic theme is the
 * fallback for any missing or malformed value.
 *
 * @author Pau Bofill
 * @author Claude IA
 * @version 4.1
 */
public final class Tema {

    /** [CA] Clau del config que tria el tema. / [EN] Config key choosing the theme. */
    public static final String CLAU_CONFIG = "tema";
    /** [CA] El tema de reserva, i el de partida si el config no en diu cap. */
    public static final String CLASSIC = "classic";
    /** [CA] Els temes de fàbrica, que es copien a la carpeta si hi falten. */
    private static final String[] DE_FABRICA = {"moni"};
    private static final String RECURS = "/temes/";
    private static final String EXTENSIO = ".properties";

    private static Tema actual;

    private final String nom;
    private final Properties valors;
    private final Properties reserva;

    private Tema(String nom, Properties valors, Properties reserva) {
        this.nom = nom;
        this.valors = valors;
        this.reserva = reserva;
    }

    /**
     * [CA] El tema que fa servir l'aplicació. El primer cop sembra la carpeta,
     * llegeix el config i carrega el tema triat.
     * [EN] The current theme; loaded once.
     */
    public static synchronized Tema actual() {
        if (actual == null) {
            actual = carrega(AppConfig.get().get(CLAU_CONFIG, CLASSIC).trim());
        }
        return actual;
    }

    /** [CA] La carpeta dels temes. / [EN] The themes folder. */
    public static Path carpeta() {
        return AppPaths.getConfigDir().resolve("temes");
    }

    /** [CA] El nom del tema carregat. / [EN] Loaded theme name. */
    public String nom() {
        return nom;
    }

    private static Tema carrega(String nom) {
        Properties reserva = llegeixRecurs(CLASSIC);
        if (reserva == null) {
            reserva = new Properties();
            System.err.println("Tema: falta el tema clàssic de dins del programa");
        }
        sembra();
        Properties valors = null;
        Path fitxer = carpeta().resolve(nom + EXTENSIO);
        if (Files.isRegularFile(fitxer)) {
            try {
                valors = llegeix(new StringReader(Files.readString(fitxer, StandardCharsets.UTF_8)));
            } catch (Exception ex) {
                System.err.println("Tema: no es pot llegir " + fitxer + " (" + ex.getMessage() + ")");
            }
        }
        if (valors == null) {
            valors = llegeixRecurs(nom);
        }
        if (valors == null) {
            System.err.println("Tema: no hi ha cap tema que es digui «" + nom + "»; es fa servir el clàssic");
            return new Tema(CLASSIC, reserva, reserva);
        }
        return new Tema(nom, valors, reserva);
    }

    /** [CA] Copia a la carpeta els temes de fàbrica que hi faltin. */
    private static void sembra() {
        try {
            Files.createDirectories(carpeta());
            for (String t : DE_FABRICA) {
                Path desti = carpeta().resolve(t + EXTENSIO);
                if (Files.exists(desti)) continue;
                try (InputStream in = Tema.class.getResourceAsStream(RECURS + t + EXTENSIO)) {
                    if (in != null) Files.copy(in, desti);
                }
            }
        } catch (IOException ex) {
            System.err.println("Tema: no es poden copiar els temes a " + carpeta() + " (" + ex.getMessage() + ")");
        }
    }

    private static Properties llegeixRecurs(String nom) {
        try (InputStream in = Tema.class.getResourceAsStream(RECURS + nom + EXTENSIO)) {
            if (in == null) return null;
            return llegeix(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            return null;
        }
    }

    private static Properties llegeix(Reader r) throws IOException {
        Properties p = new Properties();
        p.load(r);
        return p;
    }

    // ── Lectura dels valors ──────────────────────────────────────────────────

    private String cru(String clau) {
        String v = valors.getProperty(clau);
        return v != null ? v.trim() : null;
    }

    private String deReserva(String clau) {
        String v = reserva.getProperty(clau);
        if (v == null) throw new IllegalStateException("El tema clàssic no té la clau «" + clau + "»");
        return v.trim();
    }

    private void avisa(String clau, String valor) {
        System.err.println("Tema «" + nom + "»: valor no vàlid per a «" + clau + "» (" + valor
                + "); es fa servir el del clàssic");
    }

    /** [CA] Cert si el valor és {@code colorsets}: el color el decideix ColorSets. */
    public boolean esColorSets(String clau) {
        return es(clau, "colorsets");
    }

    /**
     * [CA] Cert si el valor és la paraula donada (per exemple {@code text}). Un
     * valor que no sigui ni un color ni una paraula clau es pren del clàssic.
     */
    public boolean es(String clau, String paraula) {
        String v = cru(clau);
        if (v == null || (!esParaulaClau(v) && colorDe(v) == null)) v = deReserva(clau);
        return v.equalsIgnoreCase(paraula);
    }

    /**
     * [CA] Un color, o {@code null} si el tema diu {@code colorsets} (o una altra
     * paraula clau).
     * [EN] A colour, or null for {@code colorsets}.
     */
    public Color color(String clau) {
        String v = cru(clau);
        if (v != null) {
            if (esParaulaClau(v)) return null;
            Color c = colorDe(v);
            if (c != null) return c;
            avisa(clau, v);
        }
        String r = deReserva(clau);
        return esParaulaClau(r) ? null : colorDe(r);
    }

    /** [CA] {@code colorsets} o {@code text}: el color no és del tema. */
    private static boolean esParaulaClau(String v) {
        return v.equalsIgnoreCase("colorsets") || v.equalsIgnoreCase("text");
    }

    private static Color colorDe(String v) {
        String[] p = v.split(",");
        if (p.length != 3 && p.length != 4) return null;
        try {
            int r = Integer.parseInt(p[0].trim()), g = Integer.parseInt(p[1].trim()), b = Integer.parseInt(p[2].trim());
            if (p.length == 4) return new Color(r, g, b, Integer.parseInt(p[3].trim()));
            return new Color(r, g, b);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /** [CA] Un número decimal (gruix, proporció, alfa). / [EN] A decimal number. */
    public float decimal(String clau) {
        String v = cru(clau);
        if (v != null) {
            try {
                return Float.parseFloat(v);
            } catch (NumberFormatException ex) {
                avisa(clau, v);
            }
        }
        return Float.parseFloat(deReserva(clau));
    }

    /** [CA] Un enter (mides en píxels). / [EN] An integer. */
    public int enter(String clau) {
        String v = cru(clau);
        if (v != null) {
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException ex) {
                avisa(clau, v);
            }
        }
        return Integer.parseInt(deReserva(clau));
    }

    /** [CA] Una llista de números ({@code 15,3}): els guions d'una línia. */
    public float[] decimals(String clau) {
        String v = cru(clau);
        if (v != null) {
            float[] f = decimalsDe(v);
            if (f != null) return f;
            avisa(clau, v);
        }
        return decimalsDe(deReserva(clau));
    }

    private static float[] decimalsDe(String v) {
        String[] p = v.split(",");
        float[] f = new float[p.length];
        try {
            for (int i = 0; i < p.length; i++) f[i] = Float.parseFloat(p[i].trim());
        } catch (NumberFormatException ex) {
            return null;
        }
        return f;
    }

    /** [CA] Un text (el nom d'una família de lletra). / [EN] A string. */
    public String text(String clau) {
        String v = cru(clau);
        return (v != null && !v.isEmpty()) ? v : deReserva(clau);
    }

    /** [CA] Un interruptor: {@code si} o {@code no}. / [EN] A switch. */
    public boolean si(String clau) {
        String v = cru(clau);
        if (v == null || !(v.equalsIgnoreCase("si") || v.equalsIgnoreCase("sí") || v.equalsIgnoreCase("no"))) {
            if (v != null) avisa(clau, v);
            v = deReserva(clau);
        }
        return !v.equalsIgnoreCase("no");
    }

    /** [CA] Un estil de lletra: {@code negreta} o {@code normal}. / [EN] A font style. */
    public int estil(String clau) {
        String v = cru(clau);
        if (v == null || !(v.equalsIgnoreCase("negreta") || v.equalsIgnoreCase("normal"))) {
            if (v != null) avisa(clau, v);
            v = deReserva(clau);
        }
        return v.equalsIgnoreCase("negreta") ? java.awt.Font.BOLD : java.awt.Font.PLAIN;
    }
}
