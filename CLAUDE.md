# DoDeReBu v4.0 — Contexte del projecte per a Claude

## Què és
Editor de partitures i app d'entrenament auditiu (ear training). Java 16 + Swing/AWT, Maven. Paquet principal: `dodecagraphone`. Classe principal: `dodecagraphone.ui.MyMain`.

## Arrel del projecte
`G:\La meva unitat\Dodecaphenia\DoDeReBu_v4.0`

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

## Conceptes arquitectònics essencials

### changeMap
`TreeMap<Integer, ScoreChange>` indexat per columna de partitura. Cada entrada registra canvis de paràmetres globals (tempo, to, compàs, volum…) que entren en vigor a aquella columna.

- `getEffectiveChange(col)` — acumula totes les entrades ≤ col (merge).
- `applyChangesAt(col)` (a MyController) — aplica el ScoreChange efectiu; sempre assigna un valor (el de la marca o el per defecte) per a tempo, midiKey, scaleMode, nBeatsMeasure i beatFigure.
- `freezeBaseTimingParams()` — congela els valors base de timing (col 0); s'ha de cridar quan s'inicialitza o es carrega la partitura.
- `placePendingChangeAt(col)` (a MyController) — col·loca un canvi pendent; crida `drawFullGridinOffscreen` + `drawFullChordLineInOffscreen` + `drawFull`.

### Pending change
Quan l'usuari vol afegir una marca (tempo, to, compàs), `setPendingChange(sc, label, onAtStart)` activa un JDialog no-modal i espera un clic. `onMousePressed` detecta el clic i crida `placePendingChangeAt(col)`. La tecla **Enter** col·loca el canvi al playbar.

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

### MyTempo
Estàtica. `scoreTempo` (de les marques, mostrat al botó) vs `playbackTempo` (ajustat per Spd+/Spd-). `setTempo()` reseteja ambdós. `DEFAULT_TEMPO = 60`.

## Convencions de codi
- `I18n.t("clau")` per a textos UI; `I18n.f("clau", arg)` per a textos amb paràmetres.
- Totes les coordenades de la graella en columnes de partitura (no píxels); `Settings.getColWidth()` per convertir.
- `nRows` = nombre de files de la franja (chord line = 3 files, lyrics = 2 files aprox.).
- `nKeys` = nombre de tecles (files) de la graella de notes.

## Durada de l'últim acord (`updateStopMarker`)

`MyPatternScore.updateStopMarker()` recalcula la durada de l'últim acord del `chordSymbolLine` perquè arribi fins a `endOfScore` (= final de l'última nota, no del compàs). Distincions clau:

- `endOfScore` = `max(noteEnd, playCol + 1)` — on acaba el contingut musical
- `stopCol` = final del compàs que conté `endOfScore` — fins on avança la reproducció en silenci
- L'últim acord s'estén fins a `endOfScore`, **no** fins a `stopCol`

**Important**: `updateStopMarker` s'ha de cridar tant quan s'afegeix una nota com quan s'esborra. A `MyController`, quan s'afegeix una nota (clic o arrossegament), cal actualitzar `lastColWritten` primer i després cridar `updateStopMarker()`. **No** usar `expandStopIfNeeded` en rutes d'afegir notes si hi ha acords a la partitura, perquè `expandStopIfNeeded` actualitza `stopCol` però no la durada de l'acord.

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

### Autocorrect en drag ADD
`processDragCell` i `onMousePressed` comproven si el track **actual** té una nota a la cel·la (stream sobre `sq.getPoliNotes()`), **no** `isSqVisible()` (que és global i bloquejaria l'autocorrect sobre cel·les amb notes d'altres tracks).

### Tip del porta-retalls
`MyController.showClipboardTip()` mostra el tip via `buttons.showCustomTip(I18n.t("clipboard.full.tip"), ...)`. Respecta `Settings.isTipsVisible()` automàticament.

### Rendiment Ctrl+Z
Ctrl+Z usa `drawCurrentCamInOffscreen()` (ràpid). **No** usar `drawFullGridinOffscreen()` aquí: és molt lent per partitures grans.

## Historial de canvis recents (commits rellevants)
- **2c3d145** Tip porta-retalls (Ctrl+C/X), Ctrl+Z ràpid, botó format d'acord més avall.
- **1aec21e** Desfer selecció en clicar sense Alt.
- **61b6314** Undo/redo per a acords (`ChordEvent`) i millores de paste.
- **9269965** Fix paste undo, autocorrect sobre cel·les polinfòniques, eliminar CSV no usat.
- **7b5da1b** Stop marker, chord formats, dynamic font sizing, fixed layout.
- **4c70217** Fix navegació, restauració de valors, línies divisòries, prefix tempo.
- **dfb2726** Tecla Enter col·loca el canvi pendent al playbar.

## Build
Maven (`pom.xml`). Java 16. Maven no és al PATH; cal obrir-lo des de NetBeans o des del BAT:
```
02_BuildPortableExeAmbJREintegrat_CleanNBuild_runInCmd.bat
```
El JAR resultant va a `target/`. L'executable portable va a `portable/DoDeReBu_v4.0/`.
