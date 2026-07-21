# DoDeReBu_App v4.0

────────────────────────────────────────────────────────────────────────────────
CATALÀ
────────────────────────────────────────────────────────────────────────────────

Editor de partitures i aplicació d'entrenament auditiu (*ear training*), basat en
la solmització mòbil de dotze sons (Do De Re Ri Mi Fa Fo So Sa La Li Ti).

## Funcionalitats

- Editor de graella de notes amb entrada per ratolí (afegir, allargar, esborrar,
  moure, dividir), desfer/refer complet i selecció/còpia/enganxament de patrons.
- Lectura i escriptura de fitxers MIDI, amb marques de tempo, tonalitat, compàs i
  volum integrades a la partitura.
- Línia d'acords i línia de lletra sincronitzades amb la graella.
- Exportació a PDF i SVG ("dodecagrames").
- Més de vuitanta exercicis d'entrenament auditiu (DoDeReBu, entrenament general
  i jazz): reconeixement d'intervals, dictat melòdic, progressions diatòniques,
  arpegis, blues de dotze compassos, etc.
- Interfície en català i anglès (`resources/i18n`).

## Requisits

- Java 16 (JDK)
- Maven

## Compilació i execució

El projecte es gestiona amb Maven (`pom.xml`); no té cap plugin de shade/assembly,
així que la manera més senzilla de compilar i executar-lo és obrint el projecte amb
un IDE compatible amb Maven (p.ex. NetBeans o IntelliJ), que resol les dependències
(`jmusic`, `batik`, `pdfbox`) automàticament, i executant la classe principal
`dodecagraphone.ui.MyMain`.

Per compilar des de la línia d'ordres: `mvn package` genera el `.jar` a `target/`,
però cal afegir les dependències declarades al `pom.xml` al classpath per executar-lo
(p.ex. amb `mvn dependency:build-classpath`). Els scripts per generar un executable
portable amb JRE integrat són eines de build internes, no incloses en aquest
repositori.

## Llicència

Aquest programari es distribueix sota la **PolyForm Noncommercial License 1.0.0**:
lliure per a qualsevol ús no comercial (incloent-hi modificació i redistribució),
però no es pot utilitzar per prestar serveis de pagament ni vendre'l sense una
llicència comercial separada. Vegeu [LICENSE.txt](LICENSE.txt) per al text complet
(anglès i traducció orientativa al català).

## Contacte

Per informar d'errors o consultes: doderebuapp@dodecaphenia.org

────────────────────────────────────────────────────────────────────────────────
ENGLISH
────────────────────────────────────────────────────────────────────────────────

Score editor and ear-training application, based on twelve-tone movable
solmization (Do De Re Ri Mi Fa Fo So Sa La Li Ti).

## Features

- Note-grid editor with mouse input (add, extend, delete, move, split notes),
  full undo/redo, and pattern selection/copy/paste.
- MIDI file reading and writing, with tempo, key, time-signature, and volume
  marks embedded in the score.
- Chord-symbol line and lyrics line synchronized with the grid.
- PDF and SVG export ("dodecagrams").
- Over eighty ear-training exercises (DoDeReBu, general ear training, and jazz):
  interval recognition, melodic dictation, diatonic progressions, arpeggios,
  twelve-bar blues, and more.
- Catalan and English UI (`resources/i18n`).

## Requirements

- Java 16 (JDK)
- Maven

## Build & run

The project is managed with Maven (`pom.xml`); it has no shade/assembly plugin,
so the simplest way to build and run it is to open the project in a
Maven-aware IDE (e.g. NetBeans or IntelliJ), which resolves the dependencies
(`jmusic`, `batik`, `pdfbox`) automatically, and run the main class
`dodecagraphone.ui.MyMain`.

To build from the command line: `mvn package` produces the `.jar` in `target/`,
but you need to add the dependencies declared in `pom.xml` to the classpath to
run it (e.g. with `mvn dependency:build-classpath`). The scripts that generate
a portable executable with an embedded JRE are internal build tools, not
included in this repository.

## License

This software is distributed under the **PolyForm Noncommercial License 1.0.0**:
free for any noncommercial use (including modification and redistribution), but
it may not be used to provide paid services or be sold without a separate
commercial license. See [LICENSE.txt](LICENSE.txt) for the full text (English
and an orientative Catalan translation).

## Contact

To report bugs or ask questions: doderebuapp@dodecaphenia.org
