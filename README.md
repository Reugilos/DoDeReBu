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

El projecte es gestiona amb Maven (`pom.xml`). Per generar un executable portable
amb JRE integrat, useu:

```
02_BuildPortableExeAmbJREintegrat_CleanNBuild_runInCmd.bat
```

El `.jar` resultant es genera a `target/`; l'executable portable a
`portable/DoDeReBu_v4.0/`. La classe principal és `dodecagraphone.ui.MyMain`.

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

The project is managed with Maven (`pom.xml`). To generate a portable
executable with an embedded JRE, run:

```
02_BuildPortableExeAmbJREintegrat_CleanNBuild_runInCmd.bat
```

The resulting `.jar` is built to `target/`; the portable executable to
`portable/DoDeReBu_v4.0/`. The main class is `dodecagraphone.ui.MyMain`.

## License

This software is distributed under the **PolyForm Noncommercial License 1.0.0**:
free for any noncommercial use (including modification and redistribution), but
it may not be used to provide paid services or be sold without a separate
commercial license. See [LICENSE.txt](LICENSE.txt) for the full text (English
and an orientative Catalan translation).

## Contact

To report bugs or ask questions: doderebuapp@dodecaphenia.org
