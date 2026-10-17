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

    /**
     * [CA] Comprova la forma i el baix que surten d'una cadena d'acord.
     * <p>
     * Va per {@code new Chord(String)} i no per {@code detectAndConvert},
     * perque el format amb punt i coma es del constructor.
     */
    private static void formaBaix(String entrada, String formaVol, int baixVol) {
        dodecagraphone.model.chord.Chord c = new dodecagraphone.model.chord.Chord(entrada);
        String forma = (c.getShape() == null) ? "null" : java.util.Arrays.toString(c.getShape());
        boolean ok = forma.equals(formaVol) && c.getBass() == baixVol;
        if (!ok) {
            fallades++;
        }
        System.out.printf("  %-18s %-22s baix=%-4d %s%n", entrada, forma, c.getBass(),
                ok ? "ok" : "FALLA, s'esperava " + formaVol + " baix=" + baixVol);
    }

    public static void main(String[] args) {
        System.out.println("== disminuit: la triada i la quatriada no s'han de barrejar ==");
        esperat("Cdim",  "do[0,3,6]");
        esperat("Co",    "do[0,3,6]");
        esperat("C°", "do[0,3,6]");
        esperat("Cdim7", "do[0,3,6,9]");
        esperat("Co7",   "do[0,3,6,9]");
        esperat("C°7", "do[0,3,6,9]");

        System.out.println("== semidisminuit, totes les escriptures ==");
        esperat("Cm7b5",  "do[0,3,6,10]");
        esperat("Cmi7b5", "do[0,3,6,10]");
        esperat("Cø", "do[0,3,6,10]");

        System.out.println("== dominant amb quinta augmentada ==");
        esperat("C7#5",  "Do[0,4,8,10]");
        esperat("C7+5",  "Do[0,4,8,10]");
        esperat("Caug7", "Do[0,4,8,10]");

        System.out.println("== la familia mi, sinonim de menor ==");
        esperat("Cmi",     "do[0,3,7]");
        esperat("Cmi7",    "do[0,3,7,10]");
        esperat("Cmi9",    "do[0,3,7,10,14]");
        esperat("Cmiadd9", "do[0,3,7,14]");

        // El perill d'acceptar "mi" com a menor es que "mi" tambe es un nom de
        // nota. No hi ha xoc perque l'arrel es talla abans de mirar el sufix,
        // pero val mes tenir-ho fixat que suposar-ho.
        System.out.println("== les arrels dodecafoniques no s'han de ressentir ==");
        esperat("Mi7",   "Mi[0,4,7,10]");
        esperat("Mim7",  "mi[0,3,7,10]");
        esperat("Domi7", "do[0,3,7,10]");
        // Fa es l'unic nom dodecafonic que comenca amb una lletra A-G seguida
        // d'una altra lletra, o sigui que es l'unic que pot confondre el
        // tallador d'arrels anglosaxones.
        esperat("Faug",  "Fa[0,4,8]");
        esperat("Fadd9", "Fa[0,4,7,14]");

        System.out.println("== el format amb baix i extensions dins dels claudators ==");
        // Tots els nombres son semitons. El baix es col.loca PER SOTA si hi
        // cap, i les extensions sonen una octava amunt.
        formaBaix("Do[6;0,4,7]",     "[0, 4, 7]",          -6);
        formaBaix("Do[5;0,4,7;2,4]", "[0, 4, 7, 14, 16]",  -7);
        // El 0 no te cap octava per sota on anar i es queda a l'arrel.
        formaBaix("Do[0;0,4,7]",     "[0, 4, 7]",           0);
        // El grup d'extensions buit no afegeix res.
        formaBaix("Do[7;0,4,7;]",    "[0, 4, 7]",          -5);

        System.out.println("== la caixa de l'arrel la mana la tercera ==");
        // Entrada: qualsevol caixa val. Sortida: majuscula si la tercera es
        // major, minuscula si es menor, i majuscula si no n'hi ha cap.
        esperat("do[0,4,7]", "Do[0,4,7]");
        esperat("DO[0,3,7]", "do[0,3,7]");
        esperat("Csus4",     "Do[0,5,7]");
        esperat("Csus2",     "Do[0,2,7]");
        esperat("Caug",      "Do[0,4,8]");

        System.out.println("== el nom en paraules de cada acord ==");
        // Surt de Template.nomGeneric, que ja hi era pero el tenien buit nou
        // de les vint-i-dues plantilles. No el fa servir ningu encara: aqui es
        // llista perque es vegi que cap no en queda sense.
        for (String simbol : ChordSymbols.allChordsForRoot("Do", ChordSymbols.FORMAT_SIMBOL)) {
            String ivs = ChordSymbols.detectAndConvert(simbol, null);
            String nom = (ivs == null) ? null
                    : ChordSymbols.toFormat(ivs, ChordSymbols.FORMAT_NOM);
            boolean ok = nom != null && !nom.trim().isEmpty() && !nom.trim().equals("Do");
            if (!ok) {
                fallades++;
            }
            System.out.printf("  %-12s %-28s %s%n", simbol, nom, ok ? "ok" : "FALLA: sense nom");
        }

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
