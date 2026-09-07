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
| `model/ToneRange.java` | Notes, tonalitats, rang de la graella, `MIDDLE_C`, `midiToKeyId()` |
| `model/InstrumentRange.java` | Tessitures GM del CSV; `calcDisplayOffset()` |
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

### Altura dibuixada vs altura que sona (`displayOffset`)

La graella representa l'altura **escrita**; l'instrument pot sonar en un altre registre. La conversió és una resta:

```
so     = keyIdToMidi(fila) − displayOffset      (MyGridSquare, en desar i sonar)
dibuix = altura_desada      + displayOffset      (MyMidiScore, en carregar)
```

O sigui `displayOffset = dibuix − so`. **Negatiu vol dir que sona per sobre del que es veu.** El glockenspiel val −24. La marca groga de la franja d'acords ho ensenya amb el signe girat (**t+24**), que és el que espera un músic acostumat a l'8va; el gir és només de visualització (`currentTrackTranspose()` i `markTipText`).

**No es desa mai al fitxer.** El calcula `InstrumentRange.calcDisplayOffset(programa, lowestMidi, highestMidi)`, que tria el múltiple de 12 que maximitza la superposició entre la tessitura del CSV i la graella. Conseqüències:

- En **desar**, cada pista no percussiva rep un `PROGRAM_CHANGE` a la seva primera nota (`pcEscrit` a `saveMidiScore`). El bucle va per columnes, no per pistes, o sigui que les pistes hi surten barrejades i cal portar el compte. Sense això, les pistes segona i següents carregarien amb offset 0.
- En **carregar**, `applyInitialProgramsFromSequence` escaneja **totes** les pistes i assigna a cada canal el programa del seu tick més baix. Després, un pre-scan per pista busca el seu primer `PROGRAM_CHANGE` i fixa l'offset **abans** de convertir cap nota; si la pista no en porta cap, pren l'offset del canal on toquen les seves notes.
- Els fitxers antics porten un text meta `displayOffset=`; **ja no es llegeix**.

**Per què cal escanejar totes les pistes.** Molts MIDI externs (MuseScore i companyia) posen tots els `PROGRAM_CHANGE` a la pista de direcció. Aquella pista no té notes, `isMetaTrack` la dóna per capçalera i el bucle de càrrega **se la salta sencera**: l'instrument no s'assignava mai. Les notes es col·locaven amb offset 0 per accident i, pitjor, en desar s'escrivia el `PROGRAM_CHANGE` de l'instrument *per defecte* del canal, o sigui que la càrrega següent calculava un altre offset i desplaçava la partitura avall. **El cicle no tancava: cada desat empitjorava.** És el mateix motiu pel qual ja existia `applyInitialMetaFromSequence` per al compàs i la tonalitat.

### Els dos modes de graella i `MIDDLE_C`

| mode | graella | notes |
|---|---|---|
| `isMetallophone=true` (per defecte) | **55–79** (so3–so5) | teclat de dues octaves, de so a so |
| `isMetallophone=false` | `lowestMidi`..`highestMidi` (36–84) | del `config.properties` |

`MIDDLE_C` val **60 en tots dos** (`octavesUp` és sempre 0; tots els seus usos estan comentats). Abans el mode metal·lòfon el posava a 84 amb graella 79–103 — o sigui l'altura que *sona*. Ara la graella és l'altura *escrita*.

Que `MIDDLE_C` no depengui del mode té una conseqüència important: **la tonalitat d'una partitura es llegeix igual en tots dos modes**, i per tant no s'ha de transposar en carregar. Existia un `transposeChoiceAndKey(newOffset − offsetDesat)` per compensar-ho; s'ha eliminat perquè ja no compensa res (i calculava malament en tots els casos que quedaven).

### Notes fora de rang

`ToneRange.midiToKeyId` **desplaça per octaves** fins encabir la nota: conserva el nom i perd el registre. És el que permet dibuixar un MIDI extern de quatre octaves en un teclat de dues, a costa del perfil melòdic. `MyMidiScore` les compta (`outOfRangeCount`) i `MyController` avisa amb `load.outOfRange.warning`.

**És irreversible en desar**: la graella només guarda la fila ja desplaçada. Carregar un MIDI extern i desar-lo transposa aquelles notes per sempre.

Els dos `while` de `midiToKeyId` no tenen topall (a diferència de `clampToRange`). És segur mentre el rang tingui almenys una octava; `placeNote` comprova el `keyId` abans d'indexar, com ja feia `placeNoteAtRow`.

El nom del fitxer **no es mira mai** en carregar: `.ddcgr` només surt als diàlegs de desar. El que distingeix un MIDI extern és que no porta les metadades 0x7F de l'app.

### Idiomes i diàleg de benvinguda

Els idiomes **es descobreixen sols**: `I18n.getInstalledLanguageTags()` escaneja `i18n/messages_*.properties`, tant des d'un directori de classes (NetBeans) com de dins del JAR (portable). `LANGUAGE_TAGS_IN_ORDER` (`en`, `ca`, `es`) **no és la llista de disponibles**, només diu quins van al davant; la resta s'afegeix al final per ordre alfabètic. Si l'escaneig falla, cau a comprovar aquells tags un per un.

Per afegir un idioma n'hi ha prou amb deixar el seu `messages_XX.properties` a `resources/i18n/`. Si li falta `language.name`, el botó cau al nom que en dóna Java (`Locale.getDisplayLanguage`); si li falta `main.selectLanguage`, no posa línia a la pregunta però conserva el botó. Mai s'ensenya un `??clau??`.

`I18n.tIn(Locale, clau)` i `fIn(Locale, clau, args)` llegeixen una clau en un idioma concret **sense canviar l'idioma actiu**; és el que permet muntar un text multilingüe. Els bundles queden a la memòria cau.

**Arrencada** (`MyMain`), només si `showWelcomeDialog=true`:
1. `MyDialogs.triaIdioma()` pregunta l'idioma en tots els instal·lats alhora, amb un botó per idioma. Va **abans** de construir la finestra principal, perquè tota la interfície es munti ja en l'idioma triat i no calgui reiniciar.
2. `MyDialogs.mostraBenvinguda()` explica on és el `config.properties`, ja només en aquell idioma. A la portable ningú no veu la consola.
3. Es desen `ui.language` i `showWelcomeDialog=false`. Aquest desat també reescriu els comentaris del config en l'idioma nou.

`showWelcomeDialog` s'entrega a `true` als defaults; l'usuari el pot tornar a posar a `true` a mà sense esborrar el config sencer.

### El calaix de sastre de les metadades

En carregar, tota metadada de text 0x7F que no comenci per cap dels prefixos coneguts s'acumula a `this.messages` unida amb `"; "` (`MyMidiScore`), i en desar es torna a escriure. **Si una clau que el desat escriu no és a la llista d'exclusions, la cadena es duplica a cada cicle obrir-desar** i el fitxer creix sense fre.

Va passar amb `choiceExtended=`: la llista tenia `choice=`, i `"choiceExtended=false"` no comença per `"choice="`. Cinc fitxers de `SongsInBooklet` van arribar a acumular fins a 22 KB de brossa. En afegir el prefix, la brossa existent també desapareix sola al següent desat, perquè passa a quedar exclosa.

**Regla**: qualsevol clau nova que s'escrigui amb `addTextMeta(metaTrack, ...)` s'ha d'afegir alhora a la llista d'exclusions. Les de pista (`addTextMeta(midiTrack, ...)`) van per `readTrackData` i no toquen aquest calaix.

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
- Els bundles (`ca`, `en`, `es`, i els que s'afegeixin) han de tenir **el mateix joc de claus** i el mateix nombre de placeholders per clau. Es llegeixen en UTF-8 (`I18n.UTF8Control`), o sigui que els accents es poden escriure directament; els `\uXXXX` que hi ha són històrics. Als textos que passen per `I18n.f()`, l'apòstrof s'ha de doblar (`''`) perquè `MessageFormat` no se'l mengi.
- Totes les coordenades de la graella en columnes de partitura (no píxels); `Settings.getColWidth()` per convertir.
- `nRows` = nombre de files de la franja (chord line = 3 files, lyrics = 2 files aprox.).
- `nKeys` = nombre de tecles (files) de la graella de notes.
- `config.properties` es llegeix **i** s'escriu en UTF-8 (`AppConfig`). Els fitxers antics, escrits en ISO-8859-1, es detecten i es rellegeixen amb aquell joc de caràcters (`readTextTolerant`); el primer desat els converteix a UTF-8.

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
- **TRANSPOSE** mostra quants semitons amunt sona la pista respecte del que es dibuixa, o sigui el `displayOffset` **canviat de signe** (glock: camp −24 → marca `t+24`). Ve de l'instrument, no del `changeMap`: és informativa i `editSelectedMark`/`deleteSelectedMark` la ignoren.
- A la columna 0 es dibuixen sempre les quatre, amb fallback als valors per defecte quan no hi ha entrada explícita. `fitMarkStack()` encongeix la font si la pila no hi cabés, reservant sempre `TRIANGLE_SPACE` per al trianglet d'atac. `DEFAULT_NROWS_CHORD` va passar de 6 a **8** pel PDF: el printer dibuixa la fila amb escala no uniforme (amplada a `scaleX`, alçada a `scaleY`, menor quan calen més de quatre files per pàgina), i amb 6 files la pila de quatre marques més el trianglet omplien la banda del PDF exactament, sense marge, obligant `fitMarkStack` a encongir fins al mínim.
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
- `29-8-26` — ajuda reescrita i javadoc revisat, encara sense castellà.
- `castella-29-8-26` — tot l'anterior, més la interfície en castellà (`ca`/`en`/`es`), el `config.properties` en UTF-8 i `ConfigManager` esborrat.
- `5-9-26` — tot l'anterior, més la columna de l'acord numerada d'1 a 12, el tip de Ctrl-V i el nom del PDF igual que el del MIDI.
- `7-9-26` — do central = do4, graella de metal·lòfon 55–79, `displayOffset` calculat i no desat, i la biblioteca migrada. Inclou també el diàleg de benvinguda amb tria d'idioma, el fix del `choiceExtended`, el del `PROGRAM_CHANGE` de la pista de direcció, els de la banda d'acords i la lletra al PDF, i els botons de Swing traduïts. Inclou també la nomenclatura nova de les cançons i els dodecagrames al repositori.

Tots els tags són a `origin`. Per veure com era el codi en un punt sense tocar res: `git switch --detach <tag>`; per recuperar-ne un sol fitxer: `git checkout <tag> -- <ruta>`.

## Migració de la biblioteca (7-9-26)

En baixar la graella de 79–103 a 55–79, els fitxers ja desats s'havien de reescriure: guardaven l'altura amb el `displayOffset` antic i la tonalitat en coordenades de la graella vella. La regla aplicada, **per pista**:

```
nou_desat = desat_antic + displayOffset_antic     (conservar el dibuix)
midiKey / choice / CHANGEMAP:midiKey  −24         (la finestra ha baixat 24)
displayOffset=  →  -24
```

El `+ displayOffset_antic` no és sempre +12: `Vem kan segla forutan vind_mi` el tenia a 0 i les seves notes no s'havien de moure gens. La percussió (canal 9, `displayOffset=0`) no es toca.

Migrats: els 16 `.ddcgr.mid` de `SongsInBooklet/` i els 10 de `../Complements_Bu/OtherSongsBu/`. **Còpies de seguretat**, totes dues fora del control de versions: `../Complements_Bu/SongsInBooklet_bkp_20260907/` i `../Complements_Bu/OtherSongsBu_bkp_20260907/`.

Deixats fora expressament (imports externs, sense metadades de l'app): `prova.mid`, `CucutILaGuimbarda.mid` i tot `../Complements_Bu/OtherSongFullRange/`. Els PDFs de `OtherSongsBu/Dodecagrams/` han quedat desfasats: mostren l'octava antiga.

Aquests fitxers ara **sonen una octava més amunt** que abans de la migració: és el registre real del glockenspiel, que era el que estava malament.

## Historial de canvis recents (commits rellevants)
- **f027e33** Cançons amb la nomenclatura nova `Titol_To`; `Dodecagrams/` surt del `.gitignore` i els 16 PDFs entren al repositori. En tornar-les a desar, la brossa del `choiceExtended` ha desaparegut de totes.
- **72eeb59** La lletra del PDF es dibuixa estirada verticalment per compensar la compressió de la fila; el text passa del 52% al 96% de la seva proporció.
- **06d17f9** `I18n.applySwingDefaults()`: els botons que Swing es dibuixa sol (Yes/No/OK/Cancel del JOptionPane i tot el JFileChooser). S'ha de cridar **després** de `setLookAndFeel`, que esborra els valors del UIManager.
- **42003ca** Banda d'acords a 8 files pel PDF i fora la duplicació de marques (l'offscreen ja les portava).
- **5e283c3** El PDF dibuixava una pila de marques diferent de la de pantalla: sense `fitMarkStack`, sense la de transposició i amb un volum per cada pista. Ara `drawInitialMarkersAt` calca `drawFullChordLineInOffscreen`.
- **5c8dc50** Multipista: `applyInitialProgramsFromSequence` llegeix el `PROGRAM_CHANGE` de la pista de direcció. Sense això, un MIDI extern es desplaçava avall a cada desat.
- **1cd1e04** Fix del `choiceExtended`, que inflava els fitxers a cada desat; javadoc de `main()` recol·locat.
- **47d062b** Diàleg de benvinguda amb tria d'idioma, flag `showWelcomeDialog` i detecció automàtica d'idiomes.
- **tag `7-9-26`** Do central = do4 en tots dos modes de graella. CSV: glock 67–96 → 79–103. `ToneRange`: metal·lòfon 55–79 i `MIDDLE_C` sempre 60. El `displayOffset` deixa de desar-se i es calcula sempre de l'instrument; un `PROGRAM_CHANGE` per pista en desar; `transposeChoiceAndKey`, el lector de `displayOffset=` i `displayOffsetFromMetadata` eliminats. Marca de transposició amb signe de músic (t+24). Guarda de `keyId` a `placeNote`.
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
Maven (`pom.xml`). Java 16. Maven no és al PATH, però n'hi ha un dins del NetBeans: `C:\Program Files\NetBeans-15\netbeans\java\maven\bin\mvn.cmd`, amb `JAVA_HOME` a `C:\Program Files\Microsoft\jdk-21.0.9.10-hotspot`.

Els tres passos, en ordre:

1. **Compilar**: `mvn clean package` (o Clean & Build al NetBeans). El JAR va a `target/`.
2. **Portable**: `02_BuildPortableExeAmbJREintegrat_CleanNBuild_runInCmd.bat` → `portable/DoDeReBu_v4.1/`.
3. **Zip**: `03_BuildZip.bat` → `DoDeReBu_v4.1.zip` a l'arrel.

**Compte amb el pas 1**: malgrat el nom, el script `02_...CleanNBuild...` **no compila res**. Només agafa el JAR més recent de `target/` i el passa pel `jpackage`. Si no s'ha compilat abans, la portable surt amb codi vell sense avisar de res.

**Què hi ha al paquet**: el zip conté el contingut de `portable/DoDeReBu_v4.1/` (sense la carpeta arrel, per evitar el doble directori en extreure), més `SongsInBooklet/` i `Dodecagrams/`, que el script hi copia abans de comprimir i en treu després. Si `Dodecagrams/` no hi és, avisa i continua.

**Dependències a `target/libs/`**: el JAR de `target/` és prim (només les classes del projecte). Perquè la portable funcioni, `maven-dependency-plugin` copia jMusic, PDFBox i Batik a `target/libs/` i `maven-jar-plugin` escriu al manifest un `Class-Path` amb prefix `libs/`. El `jpackage` copia tot l'arbre de `target/` dins d'`app/`, o sigui que `app/libs/` hi arriba i el manifest la hi troba. **No** treure cap dels dos plugins del `pom.xml`: sense ells la portable arrenca i peta amb `NoClassDefFoundError`. El script busca l'únic `.jar` de l'arrel de `target/` per decidir el `--main-jar`, per això les dependències han d'anar a `libs/` i no a l'arrel.

**Ubicació dels scripts de build**: `01_baixa_jre21_FET_A_HP.bat`, `02_BuildPortableExeAmbJREintegrat_CleanNBuild_runInCmd.bat` i `03_BuildZip.bat` ja no són a l'arrel del projecte: ara viuen a `../Complements_Bu/` (germana de `DoDeReBu_v4.1/`), fora del control de versions. **Important**: aquests scripts assumeixen que s'executen des de la carpeta del projecte (`pushd "%~dp0"`, `APP_NAME` = nom de la carpeta on és el `.bat`, rutes relatives com `target/`, `vendor/jre21/`) — si s'executen directament des de `Complements_Bu/` fallaran (`target/`, `vendor/` no hi són). Cal copiar-los (o crear un enllaç/còpia) a l'arrel de `DoDeReBu_v4.1/` abans d'executar-los, o adaptar-los per acceptar la ruta del projecte com a paràmetre.

**Una trampa del `cmd` al `:log` dels scripts**: `echo %~1` analitza la redirecció **abans** d'imprimir, o sigui que qualsevol `>` dins d'un missatge (el `->` dels passos 10, 11, 12 i 44 del `03_BuildZip.bat`) se'l menjava i provava d'escriure en un fitxer inexistent. La rutina desa ara el text en una variable i l'imprimeix amb expansió retardada (`echo(!MSG!`), que ja no es reanalitza. Si es toca cap dels altres scripts, tenir-ho present.

També s'han mogut a `../Complements_Bu/`: `AllSymbols.csv`, `ChordSymbols.csv` (dades de referència, no llegides en temps d'execució — `ChordSymbols.java` té les dades hardcoded), `MeMima.ttf` (no referenciat des de `src/`), `Liam/`, `Prompts/`, `SongsInBooklet - bkp/`, `portable/`, `portable_bkp/` i el zip de distribució.
