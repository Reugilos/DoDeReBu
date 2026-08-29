# DoDeReBu v4.1 — Contexte del projecte per a Claude

## Què és
Editor de partitures i app d'entrenament auditiu (ear training). Java 16 + Swing/AWT, Maven. Paquet principal: `dodecagraphone`. Classe principal: `dodecagraphone.ui.MyMain`.

## Arrel del projecte
`G:\La meva unitat\Dodecaphenia\DoDeReBu_v4.1`

El codi font és a `src/main/java/dodecagraphone/`.

## Fitxers clau

| Fitxer | Rol |
|---|---|
| `MyController.java` | Controlador principal; gestiona tota la lògica d'interacció |
| `model/component/MyGridScore.java` | Model de la graella de notes; offscreen rendering; changeMap |
| `model/component/MyAllPurposeScore.java` | Estén MyMidiScore; punts d'entrada de la partitura activa |
| `model/component/MyMidiScore.java` | Lectura/escriptura MIDI; persistència |
| `model/component/MyChordSymbolLine.java` | Franja d'acords + marques de canvi (tempo/to) |
| `model/component/MyLyrics.java` | Franja de lletra |
| `model/MyTempo.java` | Gestió de tempo (estàtica): `scoreTempo` vs `playbackTempo` |
| `model/ToneRange.java` | Notes, tonalitats, `getDefaultKey()`, `getDefaultMode()` |
| `ui/Settings.java` | Paràmetres globals (estàtica); `DEFAULT_TEMPO`, `getColWidth()`, etc. |
| `ui/MyNewPanel.java` | JPanel principal; events de ratolí i teclat |
| `model/component/MyCamera.java` | Viewport (càmera) |
| `ui/AppConfig.java` / `AppPaths.java` | Càrrega/desat de config.properties |
| `ui/I18n.java` | Internacionalització (`I18n.t("clau")`, `I18n.f("clau", arg)`) |
| `resources/i18n/messages_ca.properties` | Textos en català |
| `resources/i18n/messages_en.properties` | Textos en anglès |
| `resources/i18n/messages_es.properties` | Textos en castellà |

## Conceptes arquitectònics essencials

### changeMap
`TreeMap<Integer, ScoreChange>` indexat per columna de partitura. Cada entrada registra canvis de paràmetres globals (tempo, to, compàs, volum…) que entren en vigor a aquella columna.

- `getEffectiveChange(col)` — acumula totes les entrades ≤ col (merge).
- `applyChangesAt(col)` (a MyController) — aplica el ScoreChange efectiu; sempre assigna un valor (el de la marca o el per defecte) per a tempo, midiKey, scaleMode, nBeatsMeasure i beatFigure.
- `freezeBaseTimingParams()` — congela els valors base de timing (col 0); s'ha de cridar quan s'inicialitza o es carrega la partitura.
- `placePendingChangeAt(col)` (a MyController) — col·loca un canvi pendent; crida `drawFullGridinOffscreen` + `drawFullChordLineInOffscreen` + `drawFull`.

### Pending change
Quan l'usuari vol afegir una marca (tempo, to, compàs), `setPendingChange(sc, label, onAtStart)` activa un JDialog no-modal i espera un clic. `onMousePressed` detecta el clic i crida `placePendingChangeAt(col)`.

El diàleg té dos botons: **A l'inici** (`placePendingChangeAtStart()` → columna 0 de la partitura, sense moure la vista) i **Cancel·la** (`cancelPendingChange()`). Tancar la finestra equival a cancel·lar; abans deixava el canvi armat i el següent clic el col·locava sense avisar.

**Enter** fa el mateix que el botó per defecte, o sigui col·loca a la columna 0; **Ctrl+Enter** col·loca al playbar. Abans Enter anava sempre al playbar i, com que el diàleg no és modal, el resultat depenia de quina finestra tingués el focus: la mateixa tecla feia dues coses oposades.

`placePendingChangeAt` descarta els camps que repeteixen el valor ja vigent just abans d'aquella columna (`effectiveBeforeCol` + `dropRedundantFields`): una marca que no canvia res no es col·loca, i si a la columna n'hi havia una d'igual, s'elimina. Tonalitat i compàs es descarten **en bloc**, mai camp a camp.

### Offscreen rendering
Cada component (graella, chord line, lyrics) té un `BufferedImage` offscreen. `draw(g)` només copia la porció visible de l'offscreen a pantalla.
- `drawFullGridinOffscreen()` — redibuixa tota la graella (esborra el buffer primer amb blanc).
- `drawCurrentCamInOffscreen()` — redibuixa la vista de la càmera actual (més ràpid).
- `drawFullChordLineInOffscreen()` — redibuixa tota la franja d'acords.

### getEditingCol()
Converteix la posició del playbar (càmera) en columna de partitura, afegint el delay:
```java
int camPBar = cam.getPlayBar();
int col = allPurposeScore.getScoreCol(camPBar)
        + allPurposeScore.getDelay(!allPurposeScore.isUseScreenKeyboardRight());
return Math.max(0, col);
```

### MyTempo i els botons Spd± / Vol±
Estàtica. `scoreTempo` vs `playbackTempo`. `DEFAULT_TEMPO = 60`.
- `setTempo()` — reseteja ambdós; cridar en `newScore`, `loadScore`, quan es col·loca una marca de tempo, i des d'`applyChangesAt`.
- `setScoreTempo()` — només actualitza `scoreTempo`.

**Spd± i Vol± editen la marca vigent** (`adjustTempoMark` / `adjustVolumeMark`), no un valor global de reproducció: busquen la marca en vigor a la posició d'edició (la de la columna 0 si no n'hi ha cap de posterior), li sumen el pas i la reescriuen. El valor queda desat a la partitura i el botó mostra sempre el de la marca.

Conseqüències:
- Mantenir el botó premut genera **un sol pas d'undo** per gest: `beginMarkGesture` a la primera premuda, `commitMarkGesture` en deixar-lo anar (des d'`onMouseReleased`).
- `applyChangesAt` aplica les velocitats del `changeMap` **sempre**, no només reproduint: la marca és el volum de la partitura.
- Abans, `applyChangesAt` reaplicava la marca després de cada premuda i esborrava l'ajust; per això els botons de tempo semblaven no respondre.

## Convencions de codi
- `I18n.t("clau")` per a textos UI; `I18n.f("clau", arg)` per a textos amb paràmetres.
- Els tres bundles (`ca`, `en`, `es`) han de tenir **el mateix joc de claus** i el mateix nombre de placeholders per clau. Es llegeixen en UTF-8 (`I18n.UTF8Control`), o sigui que els accents es poden escriure directament; els `\uXXXX` que hi ha són històrics. Als textos que passen per `I18n.f()`, l'apòstrof s'ha de doblar (`''`) perquè `MessageFormat` no se'l mengi.
- Totes les coordenades de la graella en columnes de partitura (no píxels); `Settings.getColWidth()` per convertir.
- `nRows` = nombre de files de la franja (chord line = 3 files, lyrics = 2 files aprox.).
- `nKeys` = nombre de tecles (files) de la graella de notes.

## Durada de l'últim acord (`updateStopMarker`)

`MyPatternScore.updateStopMarker()` recalcula la durada de l'últim acord del `chordSymbolLine` perquè arribi fins a `endOfScore` (= final de l'última nota, no del compàs). Distincions clau:

- `endOfScore` = `max(noteEnd, playCol + 1)` — on acaba el contingut musical
- `stopCol` = final del compàs que conté `endOfScore` — fins on avança la reproducció en silenci
- L'últim acord s'estén fins a `endOfScore`, **no** fins a `stopCol`

**Important**: `updateStopMarker` es crida **només en reproduir i en desar** (`play()` i `saveScore()`), més la inicialització (constructor, `newScore`, `loadScore`), el canvi de compàs base (`refreshAfterChangeMapEdit` a col 0) i `replicateSelection`, que llegeix `stopCol` i el necessita fresc. **No** s'ha de cridar des de rutes d'edició: la doble barra ha de quedar quieta mentre s'edita.

`stopMarkerValid` (a `MyGridScore`) diu si `stopCol` correspon al contingut actual. Es valida a `updateStopMarker`/`setStopCol` i es **caduca a qualsevol mutació de contingut**; la invalidació viu dins del model (`addNoteToSquare`, `removeNoteFromSquare`, `insertColumn`, `deleteColumn`) perquè cap ruta d'edició se n'escapi. Els tres llocs que dibuixen la doble barra (graella, acords, lletra) comproven el flag: si no és vigent, no la dibuixen.

El buffer no depèn de `stopCol`: `expandBufferIfNeeded` es dimensiona amb `lastColWritten`, i **passar pàgina l'amplia** (`onNextPageButtonPressed`), altrament `nextPage()` es negava a avançar més enllà del contingut escrit.

## Selecció, porta-retalls i undo/redo

### Selecció
- `selectionActive`, `selStartRow/Col`, `selEndRow/Col` a `MyController`.
- Clicar sense Alt esborra la selecció (`selectionActive = false` a `onMousePressed`).
- Ctrl+C i Ctrl+X desactiven la selecció i mostren un tip localitzat (`clipboard.full.tip`).

### Sistema undo/redo
`PilaEvents` amb subclasses d'`Event` (`refer()`/`desfer()`):
- `MouseSequence` — seqüència d'accions de ratolí (notes).
- `ChordEvent` (`teclesControl/ChordEvent.java`) — col·locar/esborrar un acord; crida `placeChordSymbol`/`removeChordSymbol` + `redrawChordLine()`.
- `PasteEvent` — enganxar notes; `desfer()` té null guard si la nota ja no existeix.
- `ScoreChangeEvent` — marques de canvi (tempo/to/volum/compàs). Desa l'entrada **sencera** del `changeMap` abans i després (no el delta), perquè el redo no perdi els camps que ja hi havia a la columna. Si la marca de to va transposar, l'event també desfà la transposició, en ordre invers a la col·locació (primer restaura l'entrada, després destransposa). Només es registra si l'entrada realment canvia.
- El diàleg de transposició en canviar de to té **tres opcions** (`MyDialogs.demanaTransposicio`): amunt, avall o no transportar. Amunt i avall porten a la mateixa tonalitat i es diferencien en el registre — el pas es normalitza a `0..11` (amunt) i al mateix menys 12 (avall). L'event desa el pas realment aplicat.

### Marques de canvi: selecció, edició i esborrat
- `MyChordSymbolLine` registra el rectangle real de cada marca dibuixada (`markBoxes`, buidada a cada `drawFullChordLineInOffscreen`) i desa la transformació offscreen→pantalla de l'últim `draw()`; `whichMark(x, y)` la inverteix (compensa l'escala de fit-anacrusis).
- `MyController.onMousePressed` consulta `whichMark` **abans** de `myChordSymbolLine.whichCol`, altrament el clic obriria el diàleg d'acords. Un clic selecciona, doble clic edita (`editSelectedMark`), Supr esborra (`deleteSelectedMark`, a `MyNewPanel.keyPressed`).
- Les marques de la columna 0 són editables però **no** esborrables (són la base de la partitura).
- El compàs no té caixeta dibuixada: guanya undo, però no és seleccionable.
- `MyGridScore.putScoreChange` **substitueix** l'entrada sencera (i l'elimina si queda buida), a diferència de `setScoreChange`, que fa merge i el segueixen usant la càrrega MIDI i el paste. La col·locació de marques també usa `putScoreChange`.
- Editar una marca la **deselecciona** en acabar (`clearMarkSelection()` al final d'`editSelectedMark`).
- Hi ha **quatre** tipus de marca (`MarkKind`): TEMPO (blau), KEY (granate), VOLUME (verd) i TRANSPOSE (groc). La de volum i la de transposició són **per track**; les altres dues, globals.
- **TRANSPOSE** mostra el `displayOffset` de la pista en semitons. Ve de l'instrument, no del `changeMap`: és informativa i `editSelectedMark`/`deleteSelectedMark` la ignoren.
- A la columna 0 es dibuixen sempre les quatre, amb fallback als valors per defecte quan no hi ha entrada explícita. `fitMarkStack()` encongeix la font si la pila no hi cabés; amb `DEFAULT_NROWS_CHORD = 6` no hauria de caldre (amb 5 files no hi cabien per sota de 1200 px d'alçada de pantalla).
- El text de la caixeta es tria per contrast amb el fons (`ColorSets.getSeparatorColor`): blanc sobre els fons foscos, negre sobre el groc.
- El tip d'una marca (`MyController.markTipText`) mostra què és i quant val. La condició de refresc compara **columna i tipus** (`lastTipMarkKind`): com que les quatre marques inicials són a la columna 0, mirant només la columna no es refrescava en passar d'una a l'altra.

### Autocorrect en drag ADD i EXTEND
`processDragCell` i `onMousePressed` comproven si el track **actual** té una nota a la cel·la (stream sobre `sq.getPoliNotes()`), **no** `isSqVisible()` (global). Afecta els modes ADD, EXTEND_PENDING, EXTEND_RIGHT i EXTEND_LEFT. Si el `mouseReleased` és fora del grid (`whichCol == -1`), l'autocorrect usa `lastColPressed` com a posició final.

L'autocorrect **no reescriu** on ja hi ha nota del track actual (`currentTrackHasNoteAt`): abans hi passava per sobre amb `addNoteAtCell` i duplicava el `SubSquare`.

### Enllaç de notes: sempre per pista
**Mai** usar `sq.isSq_is_linked()` per decidir si cal enllaçar: és un AND sobre **tots els tracks visibles** i, a més, és un valor calculat a `updateState()`, o sigui que durant un drag (que processa diverses cel·les sense repintar entremig) pot estar caducat. Usar `MyController.isCurrentTrackNoteLinked(sq)`, que mira `poliNotes` del track actual sense cau.

S'aplica a ADD, EXTEND_PENDING (els dos sentits), EXTEND_RIGHT, EXTEND_LEFT, `removeNoteAtCell`, `unlinkNoteForUndo` i als helpers de MOVE (`findNoteHeadCol` / `findNoteTailCol`).

### Un SubSquare per track i cel·la
La identitat d'un `SubSquare` és `(canal, track, square)`. `MyGridSquare.addNote` **conserva** la nota existent en lloc d'afegir-ne una segona: amb duplicats, `linkNote` (`indexOf`) només n'enllaçava una i `removeNote` (`lastIndexOf`) només n'esborrava una — superposar una nota llarga sobre una de curta deixava un cap de nota, i esborrar amb shift-drag deixava la curta a sota.

### Tip del porta-retalls
`MyController.showClipboardTip()` mostra el tip via `buttons.showCustomTip(I18n.t("clipboard.full.tip"), ...)`. Respecta `Settings.isTipsVisible()` automàticament.

### Rendiment Ctrl+Z
Ctrl+Z usa `drawCurrentCamInOffscreen()` (ràpid). **No** usar `drawFullGridinOffscreen()` aquí: és molt lent per partitures grans.

## Anacrusa (`hasAnacrusis`)

### Detecció
`MyController.detectAnacrusis()` comprova si `lastColWritten == 0` (sense notes → `false`) o si el primer beat (cols 0..beatCols-1) és buit → `hasAnacrusis = true`. S'executa automàticament a:
- `updateTextOfButtons()` — cada cop que es navega o carrega
- `undo()` / `redo()` — via `refreshAnacrusis()`
- `onMouseReleased` — al final del drag (ADD/ERASE/EXTEND/PASTE), via `refreshAnacrusis()`
- `newScore()` — força `hasAnacrusis = false` directament

**No** cridar `refreshAnacrusis()` des de `addNoteAtCell`/`removeNoteAtCell`: canviaria `hasAnacrusis` a mig drag i desajustaria `getCol()` respecte la vista visual.

### Numeració de compassos
`getMeasureAndBeatAt` comença en compàs `0` si `hasAnacrusis`, en `1` si no. Independentment del flag `fitAnacrusis`.

### Fit anacrusis (botó Encabir/Fit)
`Settings.fitAnacrusis` es persisteix al fitxer MIDI (text meta `fitAnacrusis=`). El valor per defecte és `false`. En carregar un MIDI, `loadScore()` aplica el valor via `buttons.setFitAnacrusisButton(allPurposeScore.isFitAnacrusisScore())`. Quan l'usuari prem el botó, `onFitAnacrusisButtonPressed` actualitza `Settings` i `allPurposeScore.fitAnacrusis`.
Quan `fitAnacrusis && hasAnacrusis` a la primera pàgina, `draw()` estén `lastColToDraw` en un compàs extra (`nBeatsMeasure * nColsBeat`) → Java2D escala la imatge més ampla al mateix ample → columnes visuals més estretes.

`MyGridScore.getCol()` compensa aquesta compressió: si `fitAnacrusis && hasAnacrusis && firstColToDraw == 0` (keyboard esquerre), usa:
```java
col = (int)(relX * lastColToDraw / (colWidth * nColsCam));
```
en lloc del càlcul estàndard `relX / colWidth`.

### Paginació fit (`MyCamera`)
`nextPage`/`prevPage` usen `getBaseColsPerMeasure()` per calcular la mida de la primera pàgina ampliada quan `fitAnacrusis && hasAnacrusis`.

## Punts de retorn (tags)
- `punt-de-partida` — abans de la sèrie de correccions de l'agost del 2026.
- `divendres-28-8-26` — amb les sis tandes de correccions fetes.

## Historial de canvis recents (commits rellevants)
- **be0a658** Castellà: `messages_es.properties` amb les 496 claus traduïdes; s'activa amb `ui.language=es`. Els textos de `ca`/`en` sobre l'idioma esmenten els tres.
- **de7c080** Ajuda reescrita (15 seccions amb índex i àncores, inclosa la de `config.properties`) i revisió completa del javadoc.
- **e31d005** Marca de transposició (groga, per track), tips de marca amb valors, franja d'acords a 6 files, transposició amb tres opcions, fixes del blink d'arrencada.
- **c72c1ee** MIDI extern: compàs i tonalitat es llegeixen de totes les pistes abans de convertir notes (`applyInitialMetaFromSequence`). El `case 0x58` desxifrava el compàs i no l'aplicava, i la pista de direcció no es llegia mai.
- **da5c6c1** Lletra: salt de pàgina automàtic (`ensureScoreColVisible`); editar la lletra marca la partitura com a modificada.
- **ad4a6d4** Enllaç de notes per pista; `addNote` no duplica `SubSquare`; MOVE delimita la nota del track actual.
- **bb4df19** Doble barra només en play/desar (`stopMarkerValid`); navegació lliure endavant; paste que abasta una pàgina nova.
- **22b8de7** Marques de canvi: valor redundant, Cancel·la, Enter a l'inici, tip que no tapa, Spd±/Vol± editen la marca.
- **95a7611** Nom de fitxer amb tonalitat; `MyButton.isPressed` volatile; separació de notes contigües.
- **bf25337** fitAnacrusis sempre false a l'inici; getCol compensa compressió fit.
- **0067383** Autocorrect quan mouseReleased és fora del grid.
- **8d07e52** Fix desajust drag: refreshAnacrusis diferit a onMouseReleased.
- **da2d294** refreshAnacrusis després de undo/redo.
- **936067e** Detecció anacrusa en temps real en introduir/esborrar notes.
- **853a706** detectAnacrusis: sense notes → compàs 1; new/load correctes.
- **46aaf08** Autocorrect en drag des de cel·la ocupada (EXTEND mode).
- **5466cc5** Anacrusa automàtica, cursor d'espera i paginació fit.
- **65c8085** Botó Fit anacrusis + fixes alineació chord/lyrics i drawMeasureLine.

## Build
Maven (`pom.xml`). Java 16. Maven no és al PATH; cal obrir-lo des de NetBeans o des del BAT:
```
02_BuildPortableExeAmbJREintegrat_CleanNBuild_runInCmd.bat
```
El JAR resultant va a `target/`. L'executable portable va a `portable/DoDeReBu_v4.1/`.

**Ubicació dels scripts de build**: `01_baixa_jre21_FET_A_HP.bat`, `02_BuildPortableExeAmbJREintegrat_CleanNBuild_runInCmd.bat` i `03_BuildZip.bat` ja no són a l'arrel del projecte: ara viuen a `../Complements_Bu/` (germana de `DoDeReBu_v4.1/`), fora del control de versions. **Important**: aquests scripts assumeixen que s'executen des de la carpeta del projecte (`pushd "%~dp0"`, `APP_NAME` = nom de la carpeta on és el `.bat`, rutes relatives com `target/`, `vendor/jre21/`) — si s'executen directament des de `Complements_Bu/` fallaran (`target/`, `vendor/` no hi són). Cal copiar-los (o crear un enllaç/còpia) a l'arrel de `DoDeReBu_v4.1/` abans d'executar-los, o adaptar-los per acceptar la ruta del projecte com a paràmetre.

També s'han mogut a `../Complements_Bu/`: `AllSymbols.csv`, `ChordSymbols.csv` (dades de referència, no llegides en temps d'execució — `ChordSymbols.java` té les dades hardcoded), `MeMima.ttf` (no referenciat des de `src/`), `Liam/`, `Prompts/`, `SongsInBooklet - bkp/`, `portable/`, `portable_bkp/` i el zip de distribució.
