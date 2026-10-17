package model;

import dodecagraphone.model.chord.ChordSymbols;

import java.util.List;

/**
 * Comprova que el repertori d'acords llegeix cada escriptura com toca.
 * <p>
 * Es un {@code main()} com {@link MyKeyCirclesTest}: el projecte no porta JUnit.
 * Imprimeix una linia per cas i acaba dient quantes n'han fallat, o sigui que
 * amb una ullada se sap si alguna cosa s'ha mogut.
 * <p>
 * El que vigila de debo son <b>les ambiguitats entre plantilles</b>.
 * {@code SIN_TO_SUF} es un mapa pla de sinonim a sufix, i hi posa les entrades
 * recorrent {@code TEMPLATES} amb un {@code put} que <b>sobreescriu</b>: dues
 * plantilles que declarin el mateix sinonim no donen cap error ni cap avis, i
 * guanya la que vagi mes avall a la taula. Va passar amb {@code "°"} i
 * {@code "o"}, que eren sinonim de la triada disminuida i tambe del setè: com
 * que {@code dim7} va despres de {@code dim}, «Co» donava la quatriada.
 * <p>
 * El round-trip final es el que ja descrivia el CLAUDE.md: els simbols de totes
 * les plantilles per les dotze arrels, tornats a llegir.
 */
public class ChordSymbolsTest {

    private static int fallades = 0;

    private static void esperat(String entrada, String vol) {
        String te;
        try {
            te = ChordSymbols.detectAndConvert(entrada, null);
        } catch (Throwable t) {
            te = "EXCEPCIO: " + t.getClass().getSimpleName();
        }
        boolean ok = vol.equals(String.valueOf(te));
        if (!ok) {
            fallades++;
        }
        System.out.printf("  %-10s -> %-16s %s%n",
                entrada, te, ok ? "ok" : "FALLA, s'esperava " + vol);
    }

    public static void main(String[] args) {
        System.out.println("== disminuit: la triada i la quatriada no s'han de barrejar ==");
        esperat("Cdim",  "Do[0,3,6]");
        esperat("Co",    "Do[0,3,6]");
        esperat("C°", "Do[0,3,6]");
        esperat("Cdim7", "Do[0,3,6,9]");
        esperat("Co7",   "Do[0,3,6,9]");
        esperat("C°7", "Do[0,3,6,9]");

        System.out.println("== semidisminuit, totes les escriptures ==");
        esperat("Cm7b5",  "Do[0,3,6,10]");
        esperat("Cmi7b5", "Do[0,3,6,10]");
        esperat("Cø", "Do[0,3,6,10]");

        System.out.println("== dominant amb quinta augmentada ==");
        esperat("C7#5",  "Do[0,4,8,10]");
        esperat("C7+5",  "Do[0,4,8,10]");
        esperat("Caug7", "Do[0,4,8,10]");

        System.out.println("== la familia mi, sinonim de menor ==");
        esperat("Cmi",     "Do[0,3,7]");
        esperat("Cmi7",    "Do[0,3,7,10]");
        esperat("Cmi9",    "Do[0,3,7,10,14]");
        esperat("Cmiadd9", "Do[0,3,7,14]");

        // El perill d'acceptar "mi" com a menor es que "mi" tambe es un nom de
        // nota. No hi ha xoc perque l'arrel es talla abans de mirar el sufix,
        // pero val mes tenir-ho fixat que suposar-ho.
        System.out.println("== les arrels dodecafoniques no s'han de ressentir ==");
        esperat("Mi7",   "Mi[0,4,7,10]");
        esperat("Mim7",  "Mi[0,3,7,10]");
        esperat("Domi7", "Do[0,3,7,10]");
        // Fa es l'unic nom dodecafonic que comenca amb una lletra A-G seguida
        // d'una altra lletra, o sigui que es l'unic que pot confondre el
        // tallador d'arrels anglosaxones.
        esperat("Faug",  "Fa[0,4,8]");
        esperat("Fadd9", "Fa[0,4,7,14]");

        System.out.println("== round-trip de totes les plantilles, dotze arrels ==");
        String[] arrels = {"Do","De","Re","Ri","Mi","Fa","Fo","So","Sa","La","Li","Ti"};
        int casos = 0;
        for (String arrel : arrels) {
            List<String> simbols = ChordSymbols.allChordsForRoot(arrel, ChordSymbols.FORMAT_SIMBOL);
            for (String simbol : simbols) {
                if (simbol == null) {
                    fallades++;
                    System.out.println("  simbol nul per a l'arrel " + arrel);
                    continue;
                }
                casos++;
                if (ChordSymbols.detectAndConvert(simbol, null) == null) {
                    fallades++;
                    System.out.println("  no es torna a llegir: " + simbol);
                }
            }
        }
        System.out.printf("  %d casos%n", casos);

        System.out.println(fallades == 0 ? "TOT CORRECTE" : fallades + " FALLADES");
    }
}
