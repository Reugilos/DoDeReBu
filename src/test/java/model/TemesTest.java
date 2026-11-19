package model;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Comprova els temes gràfics de fàbrica ({@code resources/temes}).
 * <p>
 * Es un {@code main()} com {@link ChordSymbolsTest}: el projecte no porta JUnit.
 * S'executa des de l'arrel del projecte.
 * <ul>
 *   <li>Tota clau que {@code DrawKit} demana al tema ({@code T.color("…")},
 *       {@code T.decimal("…")}…) ha de ser al tema moni, que fa de reserva:
 *       si no hi fos, {@code Tema} petaria en arrencar.</li>
 *   <li>Els temes ({@code moni2} i {@code classic}) han de tenir les mateixes
 *       claus que moni, perquè qui en copiï un per fer-ne un de propi hi trobi tot
 *       el que es pot tocar.</li>
 * </ul>
 */
public class TemesTest {

    private static int fallades = 0;

    public static void main(String[] args) throws Exception {
        Properties classic = carrega("moni");

        String drawKit = Files.readString(Path.of("src/main/java/dodecagraphone/ui/DrawKit.java"),
                StandardCharsets.UTF_8);
        Matcher m = Pattern.compile("T\\.(?:color|decimal|decimals|enter|text|si|estil|es|esColorSets)\\(\"([^\"]+)\"")
                .matcher(drawKit);
        Set<String> demanades = new TreeSet<>();
        while (m.find()) demanades.add(m.group(1));
        for (String k : demanades) {
            if (!classic.containsKey(k)) falla("DrawKit demana «" + k + "» i el tema moni no la té");
        }
        System.out.println("DrawKit demana " + demanades.size() + " claus al tema");

        // Els altres temes de fàbrica no poden tenir més ni menys claus que moni.
        for (String nom : new String[] {"moni2", "classic"}) {
            Properties tema = carrega(nom);
            for (Object k : classic.keySet()) {
                if (!tema.containsKey(k)) falla("al tema " + nom + " li falta «" + k + "»");
            }
            for (Object k : tema.keySet()) {
                if (!classic.containsKey(k)) falla("el tema " + nom + " té «" + k + "», que moni no té");
            }
        }
        System.out.println(fallades == 0 ? "TOT BÉ" : fallades + " FALLADES");
        if (fallades > 0) System.exit(1);
    }

    private static Properties carrega(String nom) throws Exception {
        try (InputStream in = Files.newInputStream(Path.of("src/main/resources/temes/" + nom + ".properties"))) {
            Properties p = new Properties();
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            return p;
        }
    }

    private static void falla(String que) {
        fallades++;
        System.out.println("FALLA: " + que);
    }
}
