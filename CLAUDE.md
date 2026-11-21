# DoDeReBu v5.0 — Contexte del projecte per a Claude

## Què és
Editor de partitures i app d'entrenament auditiu (ear training). Java 16 + Swing/AWT, Maven. Paquet principal: `dodecagraphone`. Classe principal: `dodecagraphone.ui.MyMain`.

## Arrel del projecte
`G:\La meva unitat\Dodecaphenia\DoDeReBu_App`

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
| `ui/DrawKit.java` | La capa de dibuix: totes les crides de pintar; els valors surten del tema |
| `ui/Tema.java` | El tema gràfic: carrega el fitxer triat a `config/temes/`, amb `moni` de reserva |
| `resources/temes/*.properties` | Els temes: `moni` (el per defecte i la reserva), `moni2` i `classic`, tots tres de fàbrica i copiats a la carpeta. El tema `manual` ja no existeix. Vegeu «Els temes gràfics» |
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

### La capa de dibuix (`DrawKit`)

**Tot l'aspecte gràfic viu a `ui/DrawKit`.** Cap altra classe no pinta amb `Graphics2D` ni tria un color, un gruix, una font o on va un text dins de la seva caixa: **canviar el disseny és canviar DrawKit**. Són mètodes estàtics que reben el `Graphics2D` i no creen cap objecte per crida (`drawSquare` en fa milers per redibuix).

- **El repartiment**: els components decideixen *on* va cada cosa (el rectangle d'una cel·la, d'una tecla, d'una marca), perquè aquella geometria també serveix per saber què hi ha sota el ratolí. DrawKit decideix *com es veu*. Per això els mètodes reben l'**estat** (la nota `midi`, si sona, el tipus de marca `DrawKit.Marca`) i no un color: `nomDeNota(…, midi)`, `nomDeTecla(…, midi, sona)`, `marca(…, Marca.TEMPO, rotul)`.
- **Les aparences**: la cel·la, la tecla i el botó guarden l'estat visual com un `Color` calculat d'avançada (canvia quan sona o es prem). Aquests valors **només surten de la paleta** de DrawKit (`aparencaDeNota`, `aparencaDePauta`, `aparencaDelPatro`, `aparencaDeTecla`, `aparencaDeBoto`…), i els components els guarden i els tornen a passar sense mirar-los.
- **`ColorSets` només el crida DrawKit** (i `initColors`, el controlador). No s'hi ha esborrat res: els colors que avui no es fan servir s'hi queden per si es volen recuperar.
- **La fontaneria** (copiar buffers, retalls, preparar un context, tornar una font o una tinta desades) també hi passa, perquè cap crida quedi fora. Les consultes que no pinten (`getFontMetrics`, `getFont`) es queden als components: formen part de la composició.
- **Cada mètode reprodueix exactament les crides que substituïa**, també els que deixen el color o el traç posats: el codi de després pot comptar-hi (`tancaTeclat` existeix per això).
- **Dues barres de compàs**: la del buffer fa 1,5 px (`barraDeCompas`) i la que es redibuixa damunt de la pantalla, 2 px (`barraDeCompasEnPantalla`). Ja era així abans de DrawKit.
- **El PDF vectorial té la seva pròpia capa**, `PdfDrawKit` (PDFBox, no `Graphics2D`), amb els seus colors. Les classes de `model/proves/` i les fonts dels quadres de diàleg de Swing queden fora.
- **Branques**: el disseny gràfic nou (tema `moni`, carril de marques, teclat) viu a la branca **`disseny_grafic`** (15–18-11-26; vegeu «Tercera tanda»). Fins al 14-11-26 **tot era a `master`**: `drawkit` (congelada a l'etiqueta `drawkit-13-11-26`) i `disseny-grafic` s'hi van fusionar (fast-forward) i les proves de disseny segueixen des d'allà. `drawkit` era la migració, sense cap canvi visual. El javadoc de la classe explica què es pot tocar (tema, paleta, cos dels mètodes) i què s'ha de respectar (signatures, caixes, velocitat).
- **La migració (13-11-26) es va comprovar píxel a píxel** en dues passades: graella, acords, lletra, pantalla sencera i teclat del PDF, de tres cançons i dues maneres de rotular, idèntics als de `master`, i el cost de pintar durant el play, igual. Qualsevol canvi de tema s'ha de tornar a mirar a 300 bpm.

### Els temes gràfics (`Tema`)

**Els valors de DrawKit no són al codi: són en un tema**, un fitxer de text a la carpeta `temes`, al costat del `config.properties` (`<arrel>/config/temes/` des del NetBeans; al costat de l'exe a la portable). El config en tria un amb `tema=<nom>`; **per defecte, `moni`** (`resources/defaults/config.properties`). Si el config no té la clau `tema`, `Tema.actual()` cau a `moni`.

- **Quins temes hi ha (18-11-26)**: **`moni` (el per defecte), `moni2` i `classic`** són els de fàbrica i els que es copien a la carpeta de l'usuari (`Tema.DE_FABRICA = {"moni", "moni2", "classic"}`). **`moni` (per defecte) té les notes de la graella de colors** (`nota.gris = colorsets` i `nota.gris.text = colorsets`: cadascuna el color de la seva nota de la gamma `colors_manual`, amb el text blanc) i **`moni2` és idèntic però amb les notes totes blaves** (`nota.gris = 95,95,100` amb `nota.gris.text = 255,255,255`: gris fosc i lletra blanca); s'activa amb `tema=moni2`. (19-11-26: els dos noms es van intercanviar; abans `moni` era el blau.) (Hi va haver un `moni2` anterior —tecles no seleccionades en color ple—, eliminat el 18-11-26; aquest és un altre.) `classic.properties` és el disseny original i **ja es copia a la carpeta** com els altres, tot i que el `config.properties` no el menciona. **La reserva és `moni`** (19-11-26; abans el clàssic): un tema triat que no existeix o li falta una clau o la té mal escrita pren el valor de `moni`, llegit del recurs, i `moni` també és el tema si el config no té la clau `tema`. El tema `manual` s'ha eliminat definitivament (fitxer inclòs; `tema=manual` cau a `moni`). Per fer un tema nou: copiar `moni.properties` amb un altre nom, editar-lo i posar-ne el nom a `tema=` (és el que explica el `config.properties`).
- **Sembra**: en arrencar, copia a la carpeta **només els que hi falten**. Un tema editat no es trepitja mai; per recuperar el de fàbrica, s'esborra el fitxer. **Conseqüència que ha mossegat més d'un cop**: si es canvia un tema a `resources/temes/`, la còpia que ja és a `config/temes/` no es refà; cal esborrar-la o copiar-hi els valors. Passa el mateix amb `target/classes` des del NetBeans: si no es fa un *Clean and Build*, els fitxers de textos (`i18n/messages_*.properties`) i els temes poden quedar vells i l'app mostra `??clau??` en lloc del text.
- **Reserva**: `moni`, de dins del programa, respon per a tot. Un tema que no existeix, un fitxer que no es pot llegir, una clau que falta o un valor mal escrit cauen a `moni`, amb un avís a la consola. L'aplicació no es pot quedar sense colors.
- **Format**: colors `r,g,b` o `r,g,b,a`, o `colorsets` (el color el decideix `ColorSets`, com feia l'aplicació abans dels temes); gruixos i proporcions amb punt decimal; guions `llarg,buit`; estils `normal`/`negreta`; interruptors `si`/`no`. `nota.vora.color = text` vol dir del color del text de la nota. Una família de lletra que no estigui instal·lada cau a SansSerif.
- **Es llegeix una sola vegada**, quan es carrega la classe DrawKit (i `ColorSets.initColors` per a la gamma): canviar de tema demana reiniciar. Els valors queden en camps `static final`, o sigui que dibuixar costa el mateix que abans.
- **La gamma de colors de les notes** (`gama`): `ColorSets.setGamaBasicColors` llegeix `Tema.actual().text("gama")`. Valors: `config` (la de sempre: «bu» o «pau» segons `Settings.COLORS_BU`), **`colors_manual`** (la que usa `moni`), `moni` (estimada a ull de la foto del xilòfon) i `moni2` (mesurada sobre la foto; **cap tema l'usa ara**, es conserva al codi). **`colors_manual` són els colors exactes del manual** `DoDeRe-BÚ libro ENG.pdf`: no estan mesurats sobre una imatge sinó llegits del color de farciment dels rectangles vectorials del PDF (PyMuPDF), i cada nota s'ha identificat pel nom escrit a dins; les dotze surten amb un sol color a totes les pàgines: do 218,43,53 · de 234,104,96 · re 237,108,17 · ri 244,153,70 · mi 243,222,0 · fa 14,108,72 · fo 22,146,111 · so 53,143,206 · sa 86,134,197 · la 32,89,164 · li 85,84,160 · ti 113,70,128. (La foto del xilòfon no servia: massa fosca i desaturada, i les gammes `moni`/`moni2` sortien poc fidels.)
- **Claus afegides amb el tema `moni`** (totes són també al clàssic, amb el valor de sempre, perquè `TemesTest` ho exigeix): `gama`; `nota.mi` (color propi del mi, `colorsets` = el de la paleta); `nota.gris` (**totes les notes de la graella d'un sol color**; `colorsets` = cadascuna el seu) i `nota.gris.text` (el text sobre aquell color); `nota.sona.color` (una nota que sona a la graella agafa **el seu color** en lloc del de fons; `DrawKit.aparencaDeNotaQueSona(midi)`); `tecla.apaga` i `tecla.apaga.cap` (les tecles **no seleccionades** es barregen amb `cap`, blanc per defecte, en aquesta proporció: 0 = color ple) i `tecla.text` (el color del nom de les tecles apagades; `colorsets` = el de les notes); `marques.fons` (fons del carril de marques, `colorsets` = cap). `tecla.pal` i `marques.fletxa` van existir una estona i s'han eliminat.
- **Clàssic = `master`**: comprovat píxel a píxel (30 imatges), llevat de la correcció de les cel·les velles de `drawSquare` (vegeu els criteris), que canvia el fons d'aquelles cel·les amb qualsevol tema, i del marc de les caixes del costat del teclat (vegeu els criteris). Les claus noves valen el de sempre al clàssic, o sigui que no n'hi ha cap altre canvi, **excepte la franja d'acords, que ara té 7 files i el carril de marques a dalt amb tots els temes** (vegeu «Marques de canvi»).
- **`TemesTest`** (un `main`, com els altres; `javac src/test/java/model/TemesTest.java` i `java model.TemesTest` des de l'arrel): tota clau que DrawKit demana ha de ser a `moni`, i **`moni2` i `classic` han de tenir exactament les mateixes claus que `moni`**. Afegir un valor nou al tema vol dir afegir-lo a **tots** els fitxers de `resources/temes/`, i també a l'ajuda si cal.
- **Claus només del PDF (19-11-26)**: `pdf.graella.separador.color`, `pdf.graella.separador.gruix` (punts PDF; **0 = el PDF surt com la pantalla**, que és el que fa `classic`) i `pdf.pauta.espai|linia|doCentral` (`colorsets` = com la pantalla). A `moni`/`moni2` fan els separadors de fila de la graella fins (0,25 pt) i foscos (110) i les franges del pentagrama més fosques (205 / 255,185,185). Funcionen amb `DrawKit.setModePdf(true)`, que el PDF aixeca **només** mentre redibuixa l'offscreen de la graella (`drawFullCamInOffscreen`) i que en acabar es baixa i es torna a pintar la graella per a la pantalla; en mode PDF `separadorDeFila` no pinta res al bitmap i `DodecagramPdfPrinter.drawGridRowSeparators` els dibuixa com a línies vectorials. La pantalla no canvia.
- **Línies de temps i compàs i el carril de marques (19-11-26, tots els temes)**: a la franja d'acords **comencen just sota la línia del carril** (`Settings.getMarksLaneHeight() + 1`), a la pantalla i al PDF (vertical i apaisat); ja no el travessen. Al PDF, la línia del carril es redibuixa com a vector de banda a banda (`drawBandSeparator`), perquè arribi fins a l'esquerra de tot, per sobre de la columna del teclat.
- La clau `tema` surt al `config.properties` de fàbrica, amb quatre línies de comentari (`config.tema.comment` … `comment4`; una clau `#i18n:` per línia, perquè cada una dona una sola línia de comentari) que ara diuen **que el tema per defecte és moni (notes de colors), que amb `tema=moni2` les notes de la graella surten totes blaves, i que es pot copiar un tema, canviar-li el nom i editar-lo per crear-ne més** (sense cap menció a `classic` ni `manual`), i a la taula de configuració de l'ajuda (`help.config.tema.desc`), en els tres idiomes.

### Criteris de disseny gràfic

Les proves es van fer a la branca `disseny-grafic` i ara es fan a **`disseny_grafic`** (vegeu la secció de DrawKit). Aquí s'apunten **els criteris que es decideixen**, amb el perquè, a mesura que es van provant: el que es prova i no agrada també s'hi apunta, perquè no es torni a provar. Fins que no s'hi decideixi res, l'aspecte és el de l'etiqueta `drawkit-13-11-26`, idèntic al de `master`.

**Primera prova (13-11-26): l'estil del manual del DoDeRe-Bú**, el tema `manual` (ja no es copia a la carpeta; `moni` el continua) (`Metallochrome/ManualDus/Guia_Mara/Downloaded Nov 26/DoDeRe-BÚ libro ENG.pdf`, fet per un dissenyador). Tot és a DrawKit; `ColorSets` no es toca, i els colors de les dotze notes són els de sempre. Colors mesurats sobre el PDF:

- **Pentagrama**: files blanques; les línies del pentagrama en gris càlid (238,236,236) i el do central en rosa (255,214,214). `aparencaDePauta` pregunta a `ColorSets` de quin tipus és la fila i hi posa el color del tema.
- **Plantilla**: les notes del patró en groc pàl·lid (245,255,171) i la resta en blanc (`aparencaDelPatro`).
- **Graella**: separadors de fila gris clar d'un píxel (217,217,217); temps amb guions curts {5,4}; compàs negre.
- **Notes i tecles**: text blanc a totes, també sobre el groc del mi (el manual ho fa així; l'alternativa de sempre, `ColorSets.getGridSquareFontColor`, queda anotada a `tintaDeNota`). Dues notes seguides se separen amb un filet blanc d'un píxel. El triangle de la tònica, blanc. Els slides del teclat, blancs.
- **Marques**: tonalitat en morat (85,84,160) i tempo en blau (32,89,164), els del «Do / 60» del manual.
- **Interfície**: panell de botons en blau clar (219,231,249) amb botons blancs arrodonits i text fosc; premut, blau (32,89,164) amb text blanc. Línia d'estat en el gris de la capçalera del manual (242,242,242) amb text fosc. Lletra Century Gothic (geomètrica, la més propera a la del manual que porta Windows; si no hi és, SansSerif). Text amb antialiàsing.
- **Correcció que ha fet visible**: les cel·les sense nota visible prenien el fons de la vista que hi havia quan es van crear; amb els colors d'abans no es notava. Ara `drawSquare` les pinta amb el fons de la vista d'ara.
- **Cost**: la zona de la càmera pinta igual (≈3,5–3,9 ms); el panell sencer, uns 0,5 ms més (botons arrodonits i text suavitzat). A 300 bpm el so va igual de just, i l'espera mitjana del fil de Swing puja de ~4,5 a ~8 ms (màxim 26).

Pendent de decidir: si agrada, si el text blanc sobre el groc es llegeix prou, i si la lletra Century Gothic s'ha de mantenir.

**Segona tanda (14-11-26), sobre el tema `manual`**, a tots els temes quan no es diu el contrari:

- **El `>` de les tecles extremes s'ha canviat per un `·`, i només sense metal·lòfon** (`isMetallophone=false`). Marca les dues notes de la tessitura de l'instrument actual (`MyXiloKey.draw`); el `>` ocupava massa. Amb metal·lòfon la graella ja és la tessitura i no hi surt res.
- **Les tecles fan el 67% de l'amplada del teclat** (`Settings.KEY_WIDTH_REDUCTION` = 0,67, abans 0,75); els slides, el 33% restant. El PDF ho segueix.
- **Les caixes d'Acords i de Lletra** (a l'esquerra, per sobre i per sota del teclat) tenen entitat pròpia: `DrawKit.caixaDeFranja(…, Caixa.ACORDS|LLETRA)`, amb el fons de cadascuna al tema (`caixaAcords.fons`, `caixaLletra.fons`) i un marc senzill, **igual que el que separa la franja d'acords de la graella** (negre d'1 px, als dos temes; `caixa.vora.color`, `caixa.vora.gruix`; gruix 0 = sense marc). Un marc gris clar al `manual` es va descartar. **El marc es dibuixa al final** (`DrawKit.margeDeCaixa`, des de `MyController.drawStripsFrames`) i no amb el fons (`caixaDeFranja`): les tecles toquen la caixa pel costat i, pintades després, en tapaven la vora de baix (acords) i de dalt (lletra). El marc fa de línia separadora de la càmera: **el clàssic també l'ha guanyat**, o sigui que ja no és idèntic a `master` en aquest punt.
- **Els tips són del tema** (`DrawKit.estilDeTip`): `tip.fons`, `tip.text`, `tip.vora.color`, `tip.vora.gruix`, `tip.marge`, `tip.font.mida`. Amb `colorsets` i 0 queda el del sistema: el groc de sempre amb el doble marc (finestreta + tip), que **és el que agrada i el que porten els dos temes**. Una prova amb fons blau i un sol marc es va descartar.
- **Icones dibuixades als botons.** Un rètol `{play}`, `{pausa}` o `{inici}` es dibuixa com a figura (triangle ple a la dreta, dues barres, barra + triangle a l'esquerra) amb el color del text del botó; la mida és `icona.proporcio`. Els rètols de to i d'exercici acaben amb `{>>}` o `{<<}` (`Tò {>>}`, `Ex {<<}`): les fletxes es dibuixen centrades en alçada amb les majúscules de la paraula, perquè els `>` de la font queden més amunt o més avall segons la lletra (`fletxa.proporcio`). Els botons de columna i de pàgina també (`{<}`, `{>}`, `{<<}`, `{>>}`, sense paraula), perquè totes les fletxes siguin iguals.
- **Una tecla que sona no canvia d'aspecte** (`tecla.ressalta = no`, als dos temes): abans, en prémer un slide o en reproduir, la tecla es posava blanca amb el text negre un moment. Amb `si` torna el ressaltat de sempre (`aparencaDeTeclaQueSona`, `tecla.textQueSona`).
- **Textos**: el botó de to diu **Tò** en català i **Tono** en castellà (en anglès, Key). L'ajuda ho diu igual.

**Tercera tanda (15–18-11-26): el tema `moni`**, ara el de per defecte (branca `disseny_grafic`). Parteix del manual i hi afegeix el següent:

- **Colors de les notes = els del manual** (`gama = colors_manual`, vegeu «Els temes gràfics»), també per a les tecles.
- **Graella**: totes les notes d'un sol **blau** (`nota.gris`, 188,204,231: una mica més intens que el fons de la botonera, 219,231,249; es va anar fosquint i aclarint a ull fins aquí), amb el nom en gris fosc (`nota.gris.text`, 35,35,45) perquè el blanc no es llegeix sobre blau clar. Quan una nota sona, **agafa el seu color** (`nota.sona.color = si`). *Es va provar i es va deixar:* notes grises (128), que va ser la primera versió.
- **Teclat**: les tecles **seleccionades** (les de la plantilla/choice) van amb el color ple i el nom en blanc; les **no seleccionades** s'apaguen cap al blanc (`tecla.apaga = 0.75`, tons pàl·lids) i el nom és blanc (`tecla.text = colorsets`; es veu poc, i així es va demanar). **Si no hi ha cap tecla seleccionada, totes van enceses**: `MyXiloKeyboard.resetDimensions` calcula `anySelected` i crida `MyXiloKey.setDimmed(anySelected)` (que decideix el color en silenci: `color_dim` o `color_full`). **La tecla que sona s'il·lumina invertint el seu estat** (`MyXiloKey.colorWhilePlaying`, en play i en tocar-la): una **no seleccionada** (apagada) agafa el **color ple** i una **seleccionada** (color ple) agafa l'**apagat**; amb `tecla.ressalta = si` es posa blanca amb el nom negre (`moni` el té a `no`). *Va canviar tres vegades (blanca, sense il·luminar, invertida): la d'ara és la definitiva.* *Es va provar i es va descartar:* apagar cap a un gris fosc (`tecla.apaga.cap = 90,90,90`, 50%) per poder dur el nom blanc (tornat als tons pàl·lids), i il·luminar **tota** la tecla incloent la franja del slide (el slide no es toca: es vol poder modificar a part).
- **Mode bateria i patró de notes**: en seleccionar Bateria, el teclat **amaga el patró de notes seleccionades** (cap tecla seleccionada: totes a l'esquerra i enceses; `MyXiloKeyboard.findIfSelectedVisible`, que és `!isDrumsMode() && findIfSelected`). **És només visual**: `MyChoice` no es toca (el mesclador no hi accedeix i `setDrums` tampoc), els clics als slides no hi afegeixen ni n'esborren res mentre dura (`MyController`, `if (isDrumsMode()) return;`), i en tornar a música el teclat el mostra igual que abans. **El PDF també** fa servir `findIfSelectedVisible` (el teclat del PDF amaga el patró en bateria, com la pantalla); `findIfSelected` és la selecció real. `updateTextOfButtons` refresca el teclat en canviar el mode (`keyboardShownAsDrums`), pel botó o canviant de pista.
- **Caixes del costat del teclat** (marques, acords i lletra): **sempre acaben exactament a la barra inicial**. L'amplada antiga (`ceil(nColsTeclat × colWidth)` + el marc) sobresortia 2–3 px quan l'amplada de columna no és entera (per exemple amb 9/8 i 4 compassos), i es veia una línia a la dreta de la barra. Ara `MyController.keyboardBoxSpan()` agafa el `floor` de la X de la càmera (és on es dibuixa la barra, i el marc hi queda tapat). Els títols (`Acords`, `Lletra`) van a dalt de la seva caixa amb el mateix aire (`topAir + ascent`, `topAir = margin + max(3, 0,25·rowH)`), i el **botó de format és a la fila del títol d'acords, després de la paraula**.
- **Franja d'acords de 7 files** (`DEFAULT_NROWS_CHORD`): el carril de marques en pren 1,6 (`Settings.getMarksLaneHeight()`) i els acords 5,4 (quatre línies i el trianglet). A 8 sobrava una fila per dalt. **La caixa del costat s'ha partit en dues**: a dalt la de les marques (`Tò; Tempo; Vol`) i a sota la d'Acords.
- **Franja de marques**: vegeu «Marques de canvi: la franja de marques».
- **Transposició**: ja no és una marca, surt al botó de l'instrument (`Glock (24 st)`).
- **Botó SVG desactivat (18-11-26)**: `ButtonLayout.csv` té `IsActive = Off` a `ExportButton` (id 37, fila 15 col 2) i `MyButtonPanel.loadButtonLayout` només crea els botons amb `On`: el lloc queda **en blanc i no fa res**. `onExportButtonPressed`, `SVGandPDF.printSvg`, les claus `buttonLayout.ExportButton.*` i l'ajuda (`help.export.svg.*`, que ho diu) es conserven per al futur; per reactivar-lo, `On`.
- **Gammes descartades**: `moni` (a ull) i `moni2` (mesurada de la foto); es conserven al codi però cap tema no les fa servir.


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

### Mode piano temporal (21-11-26)

Amb `isMetallophone=true`, si en carregar un MIDI hi ha notes fora de la graella (`outOfRangeCount > 0`), `loadScore` pregunta (`load.outOfRange.pianoQuestion`) si s'obre amb la configuració de piano. Si es respon que sí, `MyMain.reiniciaInterficie(false, …)` **refà tota la finestra** (la graella i el teclat es dimensionen en construir-los) amb `ToneRange.applyMode(false)` (rang `lowestMidi`/`highestMidi` del config, instrument piano a `initMidi`) i hi torna a obrir el fitxer (`loadScoreKeepingMode`). **No es desa res al config**: `ToneRange` guarda el mode del config (`getConfiguredMetallophone`) i `setConfigProperties` escriu aquest, no l'actual. El proper **Obre** o **Nova** (`ToneRange.isModeOverridden()`) tornen a refer la interfície amb el mode del config. Dues coses que es van trobar pel camí: `SoundWithMidi.initMidi` reutilitza **un sol sintetitzador** (`MidiSystem.getSynthesizer()` en dóna un de nou a cada crida, i el vell quedava obert: el so sortia amb retard), i `MyDialogs.initDialogs` només llegeix les carpetes d'Obre/Desa del config **la primera vegada** (el constructor del controlador la torna a cridar en refer la interfície i les trepitjava).

### Notes i barres: separació i primer pla (21-11-26)

- **Separació entre notes** (`MyGridScore.drawSquare`): ja no hi ha la ratlla vertical davant del nom (`DrawKit.voraDeNota` es conserva, sense ús). Si la cel·la següent de la fila és l'inici d'una altra nota (`isSqVisible && !isSq_is_linked`), aquesta es dibuixa **un píxel més curta** (dos al PDF, perquè la imatge es redueix) i hi queda el fons de la vista.
- **Barres de temps i compàs al fons** (prova, interruptor `MyGridScore.LINIES_AL_FONS`): a la pantalla, després de les barres (`ViewWindow.drawBeatAndMeasureLines`), `repintaNotesSobreLesBarres` torna a copiar de l'offscreen (`ViewWindow.blitPart`) només el tros de les notes que la barra tapava. Al PDF, les barres són vectors damunt de la imatge; `notesOverLinesOverlay` fa una imatge transparent de la mida de la fila amb els píxels de les notes que travessen o comencen a una barra (`MyGridScore.noteStateAt`/`rowTopPx`/`cellHeightPx`) i es dibuixa **després** de les barres. Per tirar-ho enrere, `LINIES_AL_FONS = false`. No es tracten les notes puntejades.

### Notes fora de rang

`ToneRange.midiToKeyId` **desplaça per octaves** fins encabir la nota: conserva el nom i perd el registre. És el que permet dibuixar un MIDI extern de quatre octaves en un teclat de dues, a costa del perfil melòdic. `MyMidiScore` les compta (`outOfRangeCount`) i `MyController` avisa amb `load.outOfRange.warning`.

**És irreversible en desar**: la graella només guarda la fila ja desplaçada. Carregar un MIDI extern i desar-lo transposa aquelles notes per sempre.

Els dos `while` de `midiToKeyId` no tenen topall (a diferència de `clampToRange`). És segur mentre el rang tingui almenys una octava; `placeNote` comprova el `keyId` abans d'indexar, com ja feia `placeNoteAtRow`.

El nom del fitxer **no es mira mai** en carregar: `.ddcgr` només surt als diàlegs de desar. El que distingeix un MIDI extern és que no porta les metadades 0x7F de l'app.

### El repertori d'acords (`ChordSymbols`)

**Cap CSV es llegeix en temps d'execució.** `AllSymbols.csv` i `ChordSymbols.csv` van quedar a `../Complements_Bu/` com a documentació; les dades són al codi. Els CSV que sí que es llegeixen en arrencar són `GeneralMidiInstruments.csv` i `ButtonLayout.csv`, cap més.

El repertori és l'array `TEMPLATES`. Cada `Template` porta **intervals, sufix, sinònims, posicions diatòniques, preferència bemoll/sostingut per nota i nom genèric**, i d'allà surten els tres mapes d'índex (`BY_INTERVALS`, `BY_SUFFIX`, `SIN_TO_SUF`). **Afegir un acord és afegir una línia**: surt alhora a tots els formats, d'entrada i de sortida.

Els 22 que hi ha: 6 tríades (major, m, dim, aug, sus2, sus4), 4 quatríades diatòniques (maj7, m7, 7, m7b5), 6 més (6, 7sus2, 7sus4, 7b5, 7#5, dim7) i 6 amb novena (maj9, m9, 9, m9b5, add9, madd9).

**El menor s'escriu també `mi`** (`Cmi`, `Cmi7`, `Cmi7b5`, `Cmi9`, `Cmi9b5`, `Cmiadd9`), que és la convenció del jazz. No xoca amb el nom de nota `Mi` perquè **l'arrel es talla abans de mirar el sufix**: `Mi7` és un dominant de mi i `Domi7` és un do menor sèptima. Això és d'entrada; la sortida segueix sent `m`.

**Les 22 plantilles tenen totes nom genèric** (`Template.nomGeneric`), i `FORMAT_NOM` el serveix com a «Do augmentat», «Do semidisminuït novena»: l'arrel en la caixa que li toca i **la qualitat sempre en minúscula**, que en català és el normal per a l'adjectiu. Nou el tenien **buit** —el `6`, els dos `7sus`, els quatre amb novena i els dos `add9`—, o sigui que `FORMAT_NOM` els tornava l'arrel i un espai. Encara no el fa servir cap vista; qui el vulgui comprovar, `ChordSymbolsTest` els llista tots.

**Les tensions no es tabulen**: `TENSION_IVLS` (13, 14, 15, 17, 18, 20, 21) i `TENSION_SUFFIX` (`b9 9 #9 11 #11 b13 13`) es componen sobre la plantilla base. `C7b9#11` no és a la taula enlloc.

El model guarda **intervals**, no un identificador de plantilla, o sigui que qualsevol conjunt és vàlid per sonar i dibuixar; la taula només fa falta per **anomenar-lo**. Un acord que no hi és surt bé en Dodeca, Midi, Anglo i Solfeig (només llisten notes) i torna `null` en Simbol i Nom genèric.

**Les Posicions diatòniques ja no demanen plantilla**: es calculen dels intervals (`posicionsDe` i `semToPos`), o sigui que un acord fora de taula també es pot veure en aquell format —`Do[0,5,6]` surt `[1,4,b5]`. Les preferències d'alteració són les que ja feien servir les plantilles (**b2, b3, b5, #5, b7**), i per això el disminuït hi surt `[1,b3,b5]` i l'augmentat `[1,3,#5]`. Per sobre de l'octava mana la taula de tensions —el 13 és `b9` i no `b2`—; el que no hi sigui es tradueix pel seu interval dins de l'octava amb set sumats al grau, i així el 16 és la `10`.

Dos forats silenciosos que hi havia, tots dos de perdre una nota sense dir-ho:

- La forma **en cadena** agafava les posicions de la plantilla de la **base** quan l'exacta no hi era, i per tant **es menjava la tensió**: `Do[0,4,7,14,16]` sortia `[1,3,5]`. Ara només val la plantilla exacta; si no hi és, es calcula.
- La forma **vertical** recorria `TENSION_IVLS` i **es saltava en silenci** tota extensió que no hi fos: la desena (16) desapareixia. Ara passa per `semToPos`, que mira la taula primer i per tant no canvia cap tensió de les de sempre.

Ho vigila `ChordSymbolsTest`, que compara **les dues formes entre elles**: si tornen a divergir, falla. És la comprovació que importa, perquè cap de les dues no petava —totes dues donaven una resposta, només que diferent.

L'entrada és **text lliure**, no una llista per triar: `MyChordSymbolLine.enterChord` passa el que s'escrigui per `ChordSymbols.detectAndConvert`, que endevina el format i ho normalitza a `Root[intervals]`. Una arrel sola en majúscula és major i en minúscula és menor (`G` → `So[0,4,7]`, `g` → `So[0,3,7]`).

Quatre trampes que han costat sang:

- **La plantilla exacta ha de manar sobre la de la base.** `convert()` buscava els intervals de la base (≤11) i només provava amb el joc sencer si aquella fallava — i la base sempre casa. Els sis acords amb novena no consultaven mai la seva plantilla: el sufix es reconstruïa com a base + tensió. `maj9` sortia `Cmaj79`, `add9` sortia `C9` (el símbol del dominant novena) i `madd9` sortia `Cm9` (el del menor novena). També es menjava la novena del format Posicions diatòniques.
- **`convertToLines()` és el cas contrari i no és cap descuit**: allà cada tensió té línia pròpia, o sigui que la plantilla **base** és la bona; amb la sencera la novena sortiria dues vegades.
- **Dues plantilles poden declarar el mateix sinònim, i no ho diu ningú.** `SIN_TO_SUF` és un mapa pla de sinònim a sufix, omplert recorrent `TEMPLATES` amb un `put` que **sobreescriu**: no hi ha cap error ni cap avís, i guanya la que vagi més avall a la taula. `"°"` i `"o"` estaven llistats com a sinònim de la **tríada** disminuïda i també del **setè**, i com que `dim7` va després de `dim`, **`Co` donava la quatríada** `[0,3,6,9]` en lloc de `[0,3,6]`. Ara el cercle sol és la tríada i el setè només porta `°7`, `o7` i `mb5bb7`. Ho vigila `ChordSymbolsTest`.
- **`ROOT_TO_PC` porta també els noms dodecafònics.** `stripAngloRoot` provava dos caràcters contra aquell mapa i `"Fa"` hi és: `Faug` i `Fadd9` es quedaven en `ug` i `dd9` i tornaven `null`. Del segon caràcter només hi pot comptar una alteració (`b` o `#`). És l'únic nom dodecafònic que comença amb una lletra A–G seguida d'una altra lletra, o sigui que només Fa en patia.

**La caixa de l'arrel la mana la tercera.** A l'**entrada** s'accepta qualsevol caixa quan hi ha intervals explícits (`do[0,4,7]` i `DO[0,4,7]` són el mateix); la caixa d'una **arrel sola**, en canvi, segueix volent dir major o menor (`G` → `So[0,4,7]`, `g` → `so[0,3,7]`). A la **sortida**, majúscula si la tercera és major i minúscula si és menor: `Do[0,4,7]` però `do[0,3,7]`, `do[0,3,6,10]`, `mi[0,3,7,10]`. Mana la tercera **més propera a l'arrel**, comptant també les que són a una octava (una desena); un acord sense cap tercera —els `sus`, o un de dues notes— va en majúscula.

L'ajust es fa **un sol cop, a la sortida de `detectAndConvert`** (`ajustaCaixaArrel`), i no a cada lloc que munta la cadena: n'hi ha cinc, cadascun amb els seus `return`, i pegant-los un a un era fàcil deixar-ne algun enrere i que un menor sortís en majúscula només per segons quina escriptura d'entrada. `Chord.getChordRootName()` fa servir la mateixa regla; abans mirava `shape[1] - shape[0]`, o sigui l'interval entre els **dos primers**, que amb un `sus` donava minúscula i amb un acord d'una sola nota petava amb un `AIOOBE`.

**El format amb punt i coma** `Arrel[baix;intervals]` i `Arrel[baix;intervals;extensions]` és d'**entrada**: `Do[6;0,4,7]` i `Do[5;0,4,7;2,4]`. Tots els nombres són **semitons**. El baix es col·loca **per sota si hi cap** —se li resta una octava i només es torna a pujar si baixava de −11—, que és exactament la regla que ja feia servir el baix escrit com a nom de nota. Les **extensions sonen una octava amunt**: el 2 i el 4 són la novena i la desena, 14 i 16; escrites tal qual no serien cap extensió, perquè el 4 ja és la tercera de l'acord.

No cal tocar `basicString()`: el format nou es normalitza cap al model de sempre i es desa com `Do[0,4,7,14,16]/-7`, que el parser ja sap llegir. És una comoditat per escriure, no un format de persistència.

**El grup d'extensions no arribava mai des de la interfície.** Qui l'entenia era el constructor `Chord(String)` —i això és el que comprovava el test—, però el diàleg d'acords passa **abans** per `detectAndConvert`, i allà el punt i coma de més arribava sencer a `parseIntArr`: tota la forma queia a `null` i el diàleg la rebutjava. O sigui que `Do[5;0,4,7;2,4]` només funcionava cridant el constructor a mà. El mateix error era a la còpia de l'Espiral, que és d'on va sortir.

**Els parèntesis tenen dos usos, i només a l'entrada** (`ambClaudators`): darrere d'una **arrel sola** valen pels claudàtors (`Do(0,4,7)`, `Do(5;0,4,7;2,4)`, `Re(1,b3,5)`); darrere d'un **símbol** són les **extensions** (`C7(b9)`, `Cmaj7(#11)`, `C7(b9,#11)`, `Cm(9)`): s'hi enganxen sense comes ni espais (`C7b9#11`) i el parser de sufixos ja en treu les tensions de la cua. El que es mostra i el que es desa segueix anant amb claudàtors i sense parèntesis. La decisió és `esSimbolAmbExtensions`: el que hi ha davant ha d'començar per A–G i no ser una arrel sola.

`Chord(String)` té **un segon parser**, de sufixos sense claudàtors (`Do7`, `Dom7`, `DoMaj7`, `Do7b5`, `Dom7b5`, `Do7#5`), que només entén **arrels dodecafòniques** — `Sol7` no s'hi reconeix. No el toca la interfície (tot el que es desa va en `Root[intervals]` i el diàleg passa abans per `detectAndConvert`), però **ha de dir el mateix** que la taula: hi va estar mapat `7b5` al semidisminuït.

Per comprovar que res no s'ha trencat: round-trip símbol → intervals → símbol de totes les plantilles per les dotze arrels (264 casos), via `allChordsForRoot` + `fromFormat`.

### La franja d'acords: persistència i dibuix

**Els símbols es desen sempre.** Van al MIDI com a metadades de text 0x7F amb prefix `CHORD:`, **a la pista 0**, i `readMidiScore` les llegeix en una passada pròpia sobre `tracks[0]`, independent del bucle de pistes (que es salta la pista de capçalera). El format de cada entrada és `CHORD:<basicString> <info>::<nCols>`.

El flag `saveChordMidiTrack` decideix **només les notes**, i ja no surt d'una pregunta pròpia («vols guardar-los també com a notes MIDI?», que es recordava tota la sessió): surt de la consulta de pistes amagades (vegeu «Desar: pistes visibles i amagades»), on la pista d'acords segueix la regla de qualsevol altra. Fins al 3-10-26 hi penjava també el bucle dels símbols: qui responia que no es trobava el fitxer sense cap acord, i una partitura només d'acords es desava **buida del tot**. Ara la pista d'acords es desa sempre —hi va l'instrument, el canal i el volum de l'acompanyament— i el flag només decideix si entra al `trackMap`; sense entrada, el bucle de la graella se'n salta les notes (`track != null`).

**El text 0x01 de cada acord** (el que veu un altre programa, com el MuseScore) es desa en el **format que es veu ara** i en una sola línia: `ChordSymbols.chordToHorizontalLine`, que dona `arrel[baix;acord;extensions]` (DodecaFormat: `do[-5;0,3,7;14]`; Solfeig: `Do[sol;do,mib,sol;re]`; Símbol i Nom genèric, tal qual). **No es llegeix en carregar**: la persistència segueix sent el `CHORD:` amb `basicString()`.

**Les notes de la pista d'acords són una exportació, no partitura.** L'acompanyament es torna a generar sol en carregar: cada símbol `CHORD:` passa per `placeChordSymbol`, que escriu les notes a la graella. Per això la pista d'acords desada va **marcada amb el seu id especial** (`id=1003`, la metadada de pista que escriu `saveTrackData`) i en carregar se li salten els `NOTE_ON`/`NOTE_OFF`: llegir-les hi afegiria una segona tanda amb les durades velles, que no les esborra ningú (`removeChordMidiNotes` busca el símbol que hi havia abans a aquella columna, i en una càrrega nova no n'hi ha cap). El mateix id evita que la pista entri al mesclador com una de normal: va a `setChordTrack`, no a `addTrack`.

**La porta del bucle de notes en desar és el `trackMap`, no `isTrackVisible`.** La pista d'acords es crea amb `visible=false` perquè les seves notes no es dibuixin a la graella, i el bucle preguntava la visibilitat: dir que sí a la pregunta **no desava ni una nota**. Per a les pistes normals la pregunta era redundant —el `trackMap` només porta les visibles— i a més petava si l'id ja no existia al mesclador.

**`basicString()` és el format de persistència**, o sigui que ha de ser exactament el que sap llegir `Chord(String)`: `Arrel[intervals]/baix`, **el baix després dels claudàtors**. Escrivia `Do/-10[0,4,7,10,13]`, i el constructor talla primer pel `/`: el baix hi arribava com a `-10[0,4,7,10,13]`, petava a `Integer.parseInt` i l'acord queia a `INVALID_CHORD` amb `shape` a `null`. El javadoc ja documentava l'ordre bo; l'escriptor era l'únic que anava a la seva. El parser accepta ara les dues formes, pels fitxers ja desats.

**`Chord(String)` només entén arrels dodecafòniques** (`Do`, `So`…): `Sol7` no s'hi reconeix. Qui entén solfeig, anglosaxó i la resta és `ChordSymbols.detectAndConvert`, per on passa el diàleg d'entrada abans d'arribar al constructor.

**`isValidChord()` no filtra el text lliure**: un acord de text té `root = USE_INFO_AS_SIMBOL` i `shape = null`, i `isValidChord()` només compara amb `INVALID_CHORD`. Qualsevol codi que vagi a `shape` darrere d'un `isValidChord()` peta. Per això `basicString()` torna el text quan no hi ha intervals i `drawChordSymbol` comprova també `getShape() == null`: és codi de dibuix, i una excepció allà s'endú la finestra sencera.

**El dibuix són tres columnes** (`drawChordSymbol`): arrel i baix, la quatríada, les extensions. Les dues apilades van **alineades a la dreta**, altrament els intervals de dues xifres desquadren la columna. Dues coses sobre la mida de la font:

- La base és **una fila de la franja per línia de text** (`min(alçada/4, rowH*0.85)`). Repartint tota l'alçada entre quatre línies, que és el que feia, la lletra sortia enorme d'ençà que la franja va passar de 4 a 8 files. Ara la mida no depèn de l'alçada de la banda, i per això la banda s'ha pogut abaixar (avui té 7 files, 1,6 de les quals són el carril de marques) sense tocar el text.
- Si així i tot els acords es trepitgessin, `computeChordFontOverride` busca **una mida comuna a tota la franja**: la més petita que necessiti cap d'ells. Una mida per acord deixaria una filera amb lletres de mides diferents, que es llegeix malament. Es calcula un cop per redibuix complet i **abans** de dibuixar cap acord, perquè canviar de font mou també la línia de base.

**El PDF no dibuixa res d'això**: copia `chordLine.getOffscreenImage()`. Qualsevol canvi al dibuix de la franja hi surt sol.

### El patró de notes (`MyChoice`) i el Fit to chord
El `choice` és una llista plana de MIDI absoluts que decideix quines files de la graella s'il·luminen (`ColorSets.getChoiceColor`). El botó de patró n'ofereix dues formes d'ajust automàtic:

- **Fit to tonality**: l'escala del to, la mateixa per a tota la partitura.
- **Fit to chord**: la plantilla d'una columna la mana **l'acord vigent allà**, amb les seves notes a totes les octaves. El patró deixa de ser únic i depèn de la posició.

- **Escales avançades i acords ampliats (18-11-26)**: a **Escala**, l'última opció «Advanced» obre, **per aquest ordre, l'escala de tons** (`setWholeToneScaleChoice`, 0,2,4,6,8,10; `choice.scale.wholeTone`) i els modes de la major (`setDorian/Phrygian/Lydian/Mixolydian/LocrianScaleChoice`, intervals des de l'arrel del mode: 0,2,3,5,7,9,10 · 0,1,3,5,7,8,10 · 0,2,4,6,7,9,11 · 0,2,4,5,7,9,10 · 0,1,3,5,6,8,10) i `setPhrygianDominantScaleChoice` (0,1,4,5,7,8,10); l'arrel és la tecla que es clica, com a les altres. A **Acord**, el diàleg (`MyDialogs.seleccionaOpcioIText`: desplegable + finestreta de text) afegeix Major 7M (0,4,7,11), Menor 7 (0,3,7,10), Dominant (0,4,7,10) i Semidisminuït (0,3,6,10), i **un acord escrit en text** (`MyChoice.applyChordTextChoice`, que passa per `ChordSymbols.detectAndConvert`, o sigui tots els formats de la franja d'acords) **mana sobre el desplegable i s'aplica directament** a l'octava més baixa del teclat amb la seva arrel: `showChoiceDialog` torna `true` i **no s'obre el diàleg «Clica una tecla per seleccionar l'arrel»**. Un text no vàlid mostra `choice.error.chord` i no canvia res. Totes les cadenes són a i18n (`choice.scale.advanced`, `choice.advanced.*`, `choice.scale.dorian`…, `choice.chord.major7`…, `choice.chord.text.prompt`, `choice.error.chord`; ca/es/en).

Es podia fer sense remenar res perquè els **dos** llocs que acoloreixen la graella —`updateStripsNKeyboard` i el `draw` d'una casella— ja tenien la columna a mà, i el `chordSymbolLine` ja va per columna: només han hagut de passar de `getChoiceList()` a `getChoiceListAt(col)`.

Allà on encara no hi ha cap acord —abans del primer, o si el que hi ha és text lliure i no té intervals— **no s'encén res**: amb el patró lligat als acords, una franja sense acord no té cap plantilla a ensenyar, i deixar-hi la del to faria creure que allà hi mana alguna cosa. Una llista buida no peta a `getChoiceColor`: `Utilities.max` i `min` tornen 0 amb una llista buida, i cap nota de la graella no cau dins de `[0,0]`, o sigui que totes surten amb el color de no triada. El teclat de pantalla és d'una posició sola i ensenya l'acord d'on s'està editant.

Dues coses que calia mirar i no suposar:

- **`getRoot()` torna l'arrel relativa a la tonalitat**, no una classe de to. Sumar-hi els intervals hauria donat una classe desplaçada tants semitons com valgués la tonalitat; el bo és `getMidiRoot()`.
- **`isValidChord()` no filtra el text lliure** (ja surt a la secció dels acords): la forma s'ha de comprovar a part o peta.

El mapa d'acords és un `HashMap`, no un `TreeMap`, o sigui que no hi ha cap `floorEntry` i cal recórrer les claus per saber quin acord regeix. Són poques; el que costa —muntar la llista de notes— només es fa quan l'acord canvia, i per això hi ha una cau d'una entrada per identitat de l'acord.

### El comptador `nNotes` es recompta, no s'acumula

`MyTrack.nNotes` decideix quines pistes són fantasma (`removeEmptyTracks`), què ensenya el mesclador i què es desa al fitxer. Es manté per increments (`oneNoteMore`/`oneNoteLess`) des d'una vintena de llocs, i **la pista d'acords el desbocava**: cada `placeChordMidiNotes` incrementa i `removeNoteFromSquare` no decrementa, o sigui que col·locar i moure acords el feia créixer sense fre (`nNotes=155713` en una partitura de tres acords).

`MyPatternScore.refreshTrackNoteCounts()` el recompta de la graella —els caps de nota, un apunt per nota llarga— i el crida `saveMidiScore` abans d'escriure les metadades i `readMidiScore` abans de `removeEmptyTracks`. És O(graella) i es fa un sol cop per desat o per càrrega.

### El mesclador sempre ha de tenir una pista

`loadScore` crea un mesclador buit (`new MyMixer(this)`, sense el «Track 1» que posa `setDefaultTrack()`) i, en acabar de llegir, `removeEmptyTracks()` esborra les pistes sense notes i selecciona l'última que quedi viva. **Amb una partitura només d'acords no en queda cap**: `currentTrack` es quedava a `-1` i el primer repintat petava a `getCurrentChannelOfCurrentTrack()` (`MyXiloKey.draw` pregunta `isDrumsMode()`). S'hi arriba també esborrant l'última pista des del mesclador, que no ho impedeix.

`ensureSomeTrackSelected()` —cridada des dels dos llocs— crea una pista nova amb canal i instrument quan no en queda cap. No és només per l'excepció: **sense pista tampoc no es pot escriure cap nota** a la partitura que s'acaba d'obrir.

### Nova torna el mesclador al valor inicial

`newScore` deixa la partitura en blanc i crida `setDefaultTrack()` (mesclador nou amb la pista d'acords, «Track 1» amb l'instrument per defecte i la bateria). **Allò sol no bastava**: el que ja estava dibuixat no s'hi enterava. Per això, tot seguit, `newScore` desmarca el botó de percussió (`MyButtonPanel.setDrumsButton(false)`: si estava premut es quedava així), fa `mixer.refreshMixer()` i `updateTextOfButtons()` (instrument i volum) i `resetDrumsMode()` torna també `lastMelodyTrackId` a 0.

**L'instrument per defecte depèn de `isMetallophone`**: el glockenspiel amb metal·lòfon i el **piano (0) sense**. `SoundWithMidi.initMidi` agafava `DEFAULT_LEAD_INSTRUMENT` (que és el glock, 9) també en el cas sense metal·lòfon, i `resetChannels()` ja feia l'altra cosa. Un `leadInstrument=9` que una execució anterior hagi deixat al `config.properties` (`setConfigProperties` el desa quan `isMetallophone=false`) guanya sobre el per defecte: s'ha d'esborrar a mà.

### Desar: pistes visibles i amagades

**Només es desen les notes de les pistes visibles**, tret que l'usuari triï el contrari. `MyController.saveScore` ho pregunta **abans de demanar el nom del fitxer** —cancel·lar no ha de deixar l'usuari havent triat un nom per res— amb `MyDialogs.demanaPistesAmagades`: **Cancel·lar** (no es desa res), **Continuar** (només les visibles), **Salvar-les totes** o **Tria** (una casella per pista; cancel·lar-ho torna a la pregunta). I18n a `save.hidden.*` (`ca`, `en`, `es`).

Hi surten les pistes normals amagades, no esborrades i amb notes, i **també la d'acords i la de bateria**, que segueixen la mateixa regla: visible → es desa sense preguntar; amagada → es pregunta. La d'acords només hi surt si hi ha símbols (`hasAnyChords`), i la resposta decideix `saveChordMidiTrack`. **Res no es recorda d'un desat a l'altre**: la pregunta es refà a cada desat, que és on es perdrien dades en silenci. Els **símbols** de la franja d'acords es desen sempre, a la pista 0.

**Tots els tracks s'escriuen sempre; només les notes depenen de la visibilitat.** En carregar, l'id d'una pista normal és la seva **posició** (`tr - first - specialTracks`), i saltar-ne una fa que les notes de les següents apuntin a un altre id. Una pista amagada que no s'ha triat s'escriu **buida**, amb les seves metadades (la d'acords i la de bateria, amb `id=1003` i `id=1005`), i en carregar-la `removeEmptyTracks` la neteja. Va passar al revés (només es desaven les visibles, sense preguntar res) i un fitxer va perdre la pista normal sense avís; el fitxer d'*El Bolero de Tania* en va ser l'exemple: tenia acords i bateria i cap pista normal.

**El nom de la pista pertany a la pista que es llegeix.** L'esdeveniment 0x03 s'aplicava a `getCurrentTrack()`: amb una pista especial el nom «DrumsTrack» o el del track d'acords sobreescrivia el de l'última pista normal, i si encara no hi havia pista actual petava amb `null`. Ara va a `mixerTrack`, i en UTF-8 en les dues bandes (en desar, la longitud és la dels bytes i no la dels caràcters).

### La lletra penja del cap de la nota

Una síl·laba es desa per **pista i columna** (`MyLyrics.lyricsByTrack`), sense cap vincle amb la graella, i durant temps això deixava síl·labes **òrfenes** en esborrar o moure una nota: no es podien ni editar ni esborrar, perquè `startEdit` busca la nota anterior de la pista i d'allà la síl·laba, i mai arribava a una columna sense nota. Ara la lletra segueix el **cap** de la nota (`MyLyrics.hasNoteHeadAt`: nota visible de la pista, sense enllaç, a qualsevol altura):

- **Esborrar** (`MyController.removeNoteAtCell`, que cobreix Shift-esborrar i el tall; i `toggle`): si la pista es queda sense cap de nota en aquella columna, la síl·laba s'esborra (`removeOrphanLyric`). Amb polifonia, si en queda un altre cap, la lletra es queda. Es registra al `MouseSequence`, o sigui que Ctrl-Z recupera nota i lletra.
- **Moure en columna** (Ctrl+Maj+arrossegar, `MoveNoteEvent`): la síl·laba se'n va amb la nota (`setLyricMove`), també a l'undo/redo. Si la destinació ja en té una, **s'esborra la que arriba** i no se'n trepitja cap; l'undo la recupera. Moure només d'altura no toca la lletra.
- **Les òrfenes que ja existien** (fitxers antics) es poden editar o esborrar clicant-hi a sobre: `startEdit` hi va directament. **No es netegen en carregar**, perquè no es poden distingir d'una lletra d'una pista amb les notes amagades.

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
- `I18n.t("clau")` per a textos UI; `I18n.f("clau", arg)` per a textos amb paràmetres. També al model quan el valor acaba a la vista: el títol, l'autor i la descripció d'una partitura nova surten de `score.default.*` a `MyGridScore.clearScore()`, no de literals en català. El títol, a més, és la base del nom que proposen Desa i PDF.
- Els bundles (`ca`, `en`, `es`, i els que s'afegeixin) han de tenir **el mateix joc de claus** i el mateix nombre de placeholders per clau. Es llegeixen en UTF-8 (`I18n.UTF8Control`), o sigui que els accents es poden escriure directament; els `\uXXXX` que hi ha són històrics. Als textos que passen per `I18n.f()`, l'apòstrof s'ha de doblar (`''`) perquè `MessageFormat` no se'l mengi.
- Totes les coordenades de la graella en columnes de partitura (no píxels); `Settings.getColWidth()` per convertir.
- `nRows` = nombre de files de la franja (chord line = 3 files, lyrics = 2 files aprox.).
- `nKeys` = nombre de tecles (files) de la graella de notes.
- `config.properties` es llegeix **i** s'escriu en UTF-8 (`AppConfig`). Els fitxers antics, escrits en ISO-8859-1, es detecten i es rellegeixen amb aquell joc de caràcters (`readTextTolerant`); el primer desat els converteix a UTF-8.

## Durada de l'últim acord (`updateStopMarker`)

`MyPatternScore.updateStopMarker()` recalcula `stopCol` i la durada de l'últim acord del `chordSymbolLine`. Distincions clau:

- `endOfScore` = `max(noteEnd, playCol + 1)` — on acaba el contingut musical
- `stopCol` = final del compàs que conté `endOfScore` — fins on avança la reproducció en silenci. **No s'arrodoneix a pàgines senceres**: amb el mínim d'una pàgina (`minStopCol`) la barra d'una partitura curta saltava al final de la pàgina, que no és cap final de res. Aquell mínim només val per al cas buit, on `stopCol` no és un final sinó un límit per a qui el llegeix (el buffer del metrònom, `playScoreCol`)
- `lastColWritten` = `max(noteEnd, final de l'últim acord) − 1`. **Hi han d'entrar les notes de la pista d'acords**, que `computeNoteEndCol` no compta (i no ha de comptar, o l'últim acord s'allargaria a si mateix): en una partitura només d'acords quedava a 0, i el bucle de desat talla a `col == lastColWritten`, o sigui que el fitxer sortia sense cap nota digués el que digués la pregunta de desar-les
- **L'últim acord** s'estén fins al **final del seu compàs**, o fins a `stopCol` si aquesta cau abans, i mai per sota de `noteEnd`. Els altres acaben on comença el següent (`calcNcols`); l'últim no en té cap i arribava només fins a `playCol + 1` —una sola columna— sempre que no hi hagués notes més enllà. El seu càlcul va **després** del de `stopCol`, que n'és el sostre.

**Important**: `updateStopMarker` es crida **només en reproduir i en desar** (`play()` i `saveScore()`), més la inicialització (constructor, `newScore`, `loadScore`), el canvi de compàs base (`refreshAfterChangeMapEdit` a col 0) i `replicateSelection`, que llegeix `stopCol` i el necessita fresc. **No** s'ha de cridar des de rutes d'edició: la doble barra ha de quedar quieta mentre s'edita.

`stopMarkerValid` (a `MyGridScore`) diu si `stopCol` correspon al contingut actual. Es valida a `updateStopMarker`/`setStopCol` i es **caduca a qualsevol mutació de contingut**; la invalidació viu dins del model (`addNoteToSquare`, `removeNoteFromSquare`, `insertColumn`, `deleteColumn`) perquè cap ruta d'edició se n'escapi. Els tres llocs que dibuixen la doble barra (graella, acords, lletra) comproven el flag: si no és vigent, no la dibuixen.

**Partitura buida = cap doble barra.** `updateStopMarker` validava el marcador *sempre*, també quan no hi ha ni notes ni acords (`endOfScore <= 0`), i allà `stopCol` no és cap final: és el mínim d'una pàgina, `Settings.getnColsCam()`. O sigui que una partitura nova arrencava amb barra. Pitjor en canviar el compàs base: `refreshAfterChangeMapEdit` crida `updateStopMarker` **abans** que `applyChangesAt` actualitzi el `Settings`, i per tant `minStopCol` es calculava amb l'amplada de pàgina *vella* mentre la vista ja dibuixava la nova — la barra quedava plantada al mig. Ara el cas buit surt amb `stopMarkerValid = false`, i `stopCol` es manté només per als qui el llegeixen com a límit (el buffer del metrònom, `MyGridScore` línia 2491).

El buffer no depèn de `stopCol`: `expandBufferIfNeeded` es dimensiona amb `lastColWritten`, i **passar pàgina l'amplia** (`onNextPageButtonPressed`), altrament `nextPage()` es negava a avançar més enllà del contingut escrit.

## PDF: Obre o Salva i sistemes per pàgina

- **Obre / Salva (20-11-26)**: `onPrintButtonPressed` pregunta (`MyDialogs.demanaObreOSalva`, claus `print.output.*`) si el PDF s'ha d'**obrir** o **desar**. Obre el genera en un fitxer temporal (`File.createTempFile`, `deleteOnExit`) i l'obre amb `Desktop.open`; Salva és el camí de sempre (selector + sobreescriure). Tancar el diàleg cancel·la. En un dodecagrama en blanc, la pregunta va després de les d'orientació i marques.
- **Sistemes per pàgina**: `pdfMaxRowsPerPage` del `config.properties` (per defecte 2, mínim 1; `DodecagramPdfPrinter.targetRows()`, que el llegeix a cada impressió: no cal reiniciar). Substitueix la constant `TARGET_ROWS`. No afecta `BLANK_ROWS` (4). Documentat a l'ajuda (`help.config.pdfMaxRowsPerPage.desc`) i al config (`config.pdfMaxRowsPerPage.comment`).

## Dodecagrama en blanc (PDF d'una partitura buida)

Si en prémer el botó de PDF la partitura no té **ni notes ni acords** (`MyPatternScore.isBlankScore()`), `onPrintButtonPressed` fa **dues preguntes**:

1. `print.blank.question` — vols un dodecagrama en blanc? Si es respon que no, no s'imprimeix res.
2. `print.blank.dropMarks.question` — en vols treure les marques inicials? Si es respon que sí, la pàgina surt sense les marques inicials de la columna 0 (tonalitat, tempo i volum): paper pautat genèric, bo per a qualsevol tonalitat i qualsevol tempo.

Després, `DodecagramPdfPrinter.printBlank(file, dropInitialMarks)` treu **`BLANK_ROWS` = 4 files buides**. (El PDF d'una partitura normal porta com a màxim **`pdfMaxRowsPerPage` = 2 sistemes per pàgina per defecte (config)**, abans 4, que es veien massa petits; el blanc no hi entra.)

Amb acords i sense notes s'imprimeix com sempre: hi ha contingut a treure.

**Com es treuen les marques.** Des de `42003ca` el PDF no les dibuixa ell: copia l'offscreen de la banda d'acords, que ja les porta. O sigui que treure-les és amagar-les d'aquell offscreen i prou. Ho fa `MyChordSymbolLine.hideInitialMarks`, un senyal que **només** viu entre el redibuix del buffer i el final del PDF: `printBlank` l'aixeca, força `setNeedsDrawing(true)` (sense això `drawFullChordLineInOffscreen` no refà res i el buffer sortiria amb les marques velles) i, en un `finally`, el baixa i crida `redrawChordLine()` perquè la pantalla les recuperi. A la pantalla no s'amaguen mai: són la base de la partitura i es veuen sempre al carril de marques.

Dues coses que el mode en blanc canvia dins de `print()`:

- **`stopCol` no ve del contingut** (no n'hi ha: valdria una sola pàgina), sinó de `blankRows * fixedCols`. Per això el càlcul de `fixedCols` s'ha mogut **abans** de llegir `stopCol`.
- **El buffer offscreen s'ha d'ampliar abans de redibuixar** (`ensureBufferFor`): una partitura nova en té prou amb un parell de pàgines (`computeInitialBufferSize`) i les files de més sortirien retallades. `drawFullCamInOffscreen()` va després.

La doble barra i el fit-anacrusi queden desactivats: un dodecagrama en blanc no té final ni anacrusa.

A l'ajuda hi és a la secció «Fitxers i Exportació» (`help.export.pdfBlank.*`).

## L'ajuda (`MyHelpDialog`)

L'HTML es munta a `buildHtml()` amb claus i18n. Dues coses que no es veuen mirant només el codi de les seccions:

- Els títols de secció i els `*.intro` **no passen per cap crida `I18n.t(...)` literal**: van a l'array `sections` i als arguments d'`appendSection`. Un grep de `I18n.t("help.` els dóna per orfes; no ho són.
- La secció de config no usa `appendSection`: la munta `appendSectionConfig`, que hi afegeix la ruta del `config.properties` **i** la taula de `helpConfig()`. Aquesta taula ha d'anar en paral·lel a `resources/defaults/config.properties`: si s'hi afegeix un paràmetre, hi ha d'entrar també una fila. Fins al 8-9-26 `helpConfig()` no la cridava ningú i les nou files no sortien enlloc.

Els botons documentats han de cobrir `resources/defaults/ButtonLayout.csv`. Hi faltava el metrònom (`help.playback.metronome.*`).

## Diàleg de canvi d'instrument (`MyDialogs.showDialog`)

Camp de text + desplegable amb els 128 instruments GM. Si s'escriu un **número** (o «65 …») es fa servir tal qual; si s'escriu un **nom, encara que sigui parcial**, es busca a la llista (`triaPerNom`): cada paraula ha de ser en alguna part de l'element, sense mirar majúscules ni accents ni l'ordre (`sax`, `alto sax`, `guit`, `violí`). Cap resultat: ho diu i el diàleg es queda obert; un de sol: demana confirmació; diversos: deixa triar-ne un. Els textos són a `dialogs.search.*`.

## Selecció, porta-retalls i undo/redo

### Selecció
- `selectionActive`, `selStartRow/Col`, `selEndRow/Col` a `MyController`.
- **Ctrl + arrossegar** selecciona (`DragMode.SELECT`); un Ctrl+clic sol deixa una selecció d'una cel·la. Clicar sense Ctrl la treu (`selectionActive = false` a `onMousePressed`).
- Ctrl+C i Ctrl+X desactiven la selecció i mostren un tip localitzat (`clipboard.full.tip`).
- **Supr / Del esborra el que hi ha dins de la selecció**, en un sol pas d'undo i **sense tocar el porta-retalls** (`deleteSelection`, que reusa `cutSelection` i en restaura el porta-retalls; `deleteLyricsSelection` per a la franja de lletra, que mana sobre la graella com a Ctrl-C/X/V). Una marca de canvi seleccionada té prioritat (`deleteSelectedMark`). Una selecció **només d'acords o de lletra**, sense cap nota, també s'esborra: `cutSelection` sortia abans si el porta-retalls de notes era buit.

### Enganxar: el punt es marca abans
El camí documentat és **Ctrl-C/Ctrl-X → Ctrl-clic al punt → Ctrl-V**. El punt d'enganxada **és la selecció**: un Ctrl+clic en deixa una d'una cel·la, es veu marcada, i `pasteAtSelection()` hi deixa el porta-retalls amb l'angle superior esquerre a sobre. Val igual per a les notes (graella) i per a la lletra (franja).

El **mecanisme antic segueix viu i no es documenta**: Ctrl-V sense cap punt marcat cau a `pasteWithoutPoint()` → `startPaste()`, que ensenya `paste.clickToPlace.tip` i espera un clic (`DragMode.PASTE`, amb el fantasma de previsualització). Amb lletra al porta-retalls i cap punt, el missatge demana el Ctrl+clic (`paste.lyrics.point.tip`), perquè allà no hi ha cap altre camí.

L'ordre de `MyNewPanel` a Ctrl-V segueix una sola regla —**mana on hi ha la selecció**—: primer la franja de lletra, després la graella, i si no n'hi ha cap, el camí antic.

### Porta-retalls de la lletra
Selecció pròpia, dins de `MyLyrics` (`selActive`, `selTrack`, `selColA/B`): un rang de columnes d'**una** pista, marcat amb **Ctrl + arrossegar** per la franja (`DragMode.LYRICS_SELECT`). És estat d'interfície: es dibuixa a `draw()`, en coordenades de pantalla, i **no** al buffer offscreen, que és el que copien el PDF i l'SVG.

- Ctrl-C / Ctrl-X → `copyLyricsSelection()` / `cutLyricsSelection()`. El retall va a `lyricsClipboard` (`ClipLyric`, desplaçaments relatius a la primera columna).
- Ctrl-V → `pasteLyricsSelection()`, a la columna de l'últim **Ctrl-clic**, conservant les distàncies. La pista de destinació és la de la selecció, o sigui la que s'està veient.
- Les tres tornen **booleà**: comparteixen drecera amb el porta-retalls de les notes i `MyNewPanel` prova primer la lletra; si no hi ha selecció a la franja, cau a la de la graella.
- L'undo no és nou: el retall reusa `MouseSequence.addLyricRemove` i l'enganxada un `PasteEvent` amb les llistes de notes i acords buides. Totes dues ja sabien desfer i refer canvis de lletra, perquè la còpia multipista de la graella ja se les enduia.
- El tip és un altre (`clipboard.lyrics.tip`): a les notes primer es fa Ctrl-V i després es tria el lloc; aquí el lloc es tria abans.

### Sistema undo/redo
`PilaEvents` amb subclasses d'`Event` (`refer()`/`desfer()`):
- `MouseSequence` — seqüència d'accions de ratolí (notes).
- `ChordEvent` (`teclesControl/ChordEvent.java`) — col·locar/esborrar un acord; crida `placeChordSymbol`/`removeChordSymbol` + `redrawChordLine()`.
- `PasteEvent` — enganxar notes; `desfer()` té null guard si la nota ja no existeix.
- `ScoreChangeEvent` — marques de canvi (tempo/to/volum/compàs). Desa l'entrada **sencera** del `changeMap` abans i després (no el delta), perquè el redo no perdi els camps que ja hi havia a la columna. Si la marca de to va transposar, l'event també desfà la transposició, en ordre invers a la col·locació (primer restaura l'entrada, després destransposa). Només es registra si l'entrada realment canvia.
- El diàleg de transposició en canviar de to té **tres opcions** (`MyDialogs.demanaTransposicio`): amunt, avall o no transportar. Amunt i avall porten a la mateixa tonalitat i es diferencien en el registre — el pas es normalitza a `0..11` (amunt) i al mateix menys 12 (avall). L'event desa el pas realment aplicat.

### Marques de canvi: la franja de marques

**Disseny (15–18-11-26).** Les marques de tonalitat, tempo i volum ja no es dibuixen sobre els acords. Viuen en un **carril propi a dalt de la franja d'acords** (`Settings.getMarksLaneHeight()` = 1,6 files), separat dels acords per una **línia horitzontal de banda a banda** (`DrawKit.liniaDeMarques`) i amb un fons opcional del tema (`marques.fons`, `DrawKit.franjaDeMarques`). A l'esquerra, la caixa del costat del teclat té un títol propi: «Tò; Tempo; Vol» / «Tono; Tempo; Vol» / «Key; Tempo; Vol» (`myChordSymbolLine.marksLabel`).

- **Com es dibuixen** (`MyChordSymbolLine.drawMarkGroup`): a cada columna amb marques hi va una **fletxeta mirant avall** (és el trianglet d'atac dels acords, `DrawKit.triangletDAtac`, de la mateixa mida) al centre de la columna i, al costat, **el text de totes les marques separades per `; `**, en **negre i amb la lletra dels acords** (`getMarkFont()` = `getBaseChordFont()`). Ordre fix: **tonalitat, tempo, volum**.
- **Notació**: **tonalitat** `Do` (major) / `do` (menor) (`ToneRange.getKeyName`); **tempo** `<figura> = <bpm>`, amb una **negra** en compàs simple (`beatFigure` 4) i una **negra amb punt** en compost (`beatFigure` 8), triada per `beatFigureAt(col)` (últim canvi de compàs del `changeMap` fins a la columna, o el de la partitura); **volum** el matís més proper (`dynamicText`: pp < 41, p < 57, mp < 72, mf < 88, f < 104, ff). La figura del tempo **es dibuixa com a forma vectorial** (`DrawKit.simbolDeBeat`: cap ple inclinat, plica i punt) perquè la tipografia dels acords no té els glifs ♩/♪ (sortien buits). El valor exacte del volum només es veu al botó de volum.
- **Globals i per pista**: tonalitat i tempo són globals; el **volum és per pista** (`ScoreChange.trackVelocities`, només es dibuixa el de la pista actual: en canviar de pista canvien les marques de volum).
- **Columna 0**: les marques inicials **es dibuixen sempre** (pantalla i PDF), amb fallback als valors per defecte quan no hi ha entrada explícita; només `hideInitialMarks` (dodecagrama en blanc sense marques) les amaga. `printingMarks`/`setPrintingMarks` queden sense efecte (van servir un moment per dibuixar-les només en imprimir).
- **La franja de marques no admet acords, però les marques sí que s'editen clicant-hi** (com abans del carril). `MyController.isMarksLaneArea` (caixa de l'esquerra + part de la càmera, banda vertical del carril) fa que un **clic en un lloc buit s'ignori** (no obre el diàleg d'acords) i que `MyChordSymbolLine.whichCol` torni `-1` dins el carril. Un clic sobre una marca (`whichMark`: banda vertical = tot el carril, columna dins `firstCol..lastCol`) **la selecciona**, un **doble clic l'edita** (`editSelectedMark`) i **Supr l'esborra** (`deleteSelectedMark`; les de la columna 0 s'editen però no s'esborren). El tip és el de la marca (`markTipText`, amb la condició de refresc per columna i tipus) o, fora de cap marca, un de general (`myChordSymbolLine.marks.tip`, `lastTipButton = -9`). `markBoxes` porta `col`, `firstCol`, `lastCol`, `xOff`, `w`, `h`. **Va estar una estona inert (18-11-26)** i es va tornar a obrir: no cal tornar-ho a provar. Els clics per **col·locar** una marca pendent (`pendingChange`) funcionen sobre el carril.
- **El botó de volum** mostra el valor i, entre parèntesis, el matís de la marca: `127 (ff)`, `80 (mf)` (`MyController.volumeButtonText`, que fa servir `MyChordSymbolLine.dynamicText`).
- **`TRANSPOSE` ja no es dibuixa** (`MarkKind.TRANSPOSE`, `DrawKit.Marca.TRANSPOSICIO`, la clau `marca.transposicio` dels temes i el seu tractament al controlador són codi en desús, documentat al javadoc, **no esborrat**). La transposició de l'instrument surt al **botó de l'instrument**: `MyController.updateTextOfButtons` afegeix `(N st)` al nom, amb el signe de músic (`-displayOffset`; sense transposició o a la bateria, només el nom: `Glock (24 st)`, `Piano`). El tip del botó és `Instrument (transposició)` (`buttonLayout.InstrButton.tipText`, tres idiomes).
- **Bug corregit: la barra de compàs fantasma.** Qualsevol `ScoreChange` sense canvi de compàs ni fase (o sigui, una marca de tempo, tonalitat o volum) **reiniciava el comptador de compàs a (0,0)** a `computeBeatMeasureLines`, `getMeasureAndBeatAt`, `getFirstColOfMeasure` i al recompte de fases de `MyController` (el de després d'una inserció de columnes), i a la columna de la marca apareixia una barra de compàs que no tenia a veure amb res. Ara **només toquen el comptador** els canvis de compàs reals (`nColsBeat`, `nBeatsMeasure`, `nColsQuarter`) o una fase guardada (`colInBeatPhase`/`measurePhase`).
- `MyGridScore.putScoreChange` **substitueix** l'entrada sencera (i l'elimina si queda buida), a diferència de `setScoreChange`, que fa merge i el segueixen usant la càrrega MIDI i el paste. La col·locació de marques també usa `putScoreChange`.
- El compàs no té marca dibuixada al carril: guanya undo, però no és seleccionable.
- La mida: la caixa d'una marca (`markBoxHeight()`) fa una línia d'acord (`ascent+descent+LINE_GAP`); el text es centra al carril (`Settings.getMarksLaneHeight()`). `fitMarkStack` i `clearMarkStackFit` ja no es fan servir (la pila vertical va desaparèixer).
- **`DEFAULT_NROWS_CHORD` és 7** (1,6 de carril + 5,4 d'acords: quatre línies amb la font a `rowH*0.85` fan unes 4,7 files i el trianglet se'n menja mitja més). Va passar per 8 (quan la font es repartia tota l'alçada) i 6 (sense carril).
- **No hi ha cap altre lloc que dibuixi marques**: el PDF copia l'offscreen de la franja (`chordLine.getOffscreenImage()`), o sigui que el carril hi surt sol. `DodecagramPdfPrinter.print` es parteix en `print` (aixeca `setPrintingMarks(true)` i redibuixa a la fi) i `printBody`.

Migrats: els 16 `.ddcgr.mid` de `SongsInBooklet/` i els 10 de `../Complements_Bu/OtherSongsBu/`. **Còpies de seguretat**, totes dues fora del control de versions: `../Complements_Bu/SongsInBooklet_bkp_20260907/` i `../Complements_Bu/OtherSongsBu_bkp_20260907/`.

Deixats fora expressament (imports externs, sense metadades de l'app): `prova.mid`, `CucutILaGuimbarda.mid` i tot `../Complements_Bu/OtherSongFullRange/`. Els PDFs de `OtherSongsBu/Dodecagrams/` han quedat desfasats: mostren l'octava antiga.

Aquests fitxers ara **sonen una octava més amunt** que abans de la migració: és el registre real del glockenspiel, que era el que estava malament.

## Historial de canvis recents (commits rellevants)
- **21-11-26** Mode piano temporal en obrir un MIDI amb notes fora del glock (vegeu «Mode piano temporal»); notes consecutives separades per un píxel de menys (sense la ratlla de davant) i barres de temps/compàs al fons, també al PDF (vegeu «Notes i barres: separació i primer pla»); sintetitzador MIDI únic; carpetes d'Obre/Desa que ja es recorden; tema `moni2` amb notes en gris fosc i lletra blanca (`nota.gris = 95,95,100`, `nota.gris.text = 255,255,255`) i línia del do més suau (`pauta.doCentral = 252,222,222`).
- **20-11-26** PDF: opció `pdfMaxRowsPerPage` (defecte 2) al config i diàleg Obre/Salva en generar-lo.
- **19-11-26** Temes `moni`/`moni2` intercanviats (`moni` = notes de colors, per defecte); `classic` es copia a la carpeta i `moni` és la reserva; tema `manual` eliminat; PDF amb 2 sistemes per pàgina (`TARGET_ROWS`), línia del carril fins a l'esquerra, línies de temps/compàs sota el carril (pantalla i PDF), claus `pdf.*` (separadors i franges més fosques, només PDF); acords repetits: cada acord de la franja torna a sonar (`MyPatternScore.placeChordMidiNotes`/`removeChordMidiNotes`, **no verificat a l'oïda**); comandes `documenta`/`comita`/`publica`/`pack`.
- **versió 5.0 (18-11-26)** Número de versió. **La versió viu en un sol lloc: el `<version>` del `pom.xml`** (5.0). D'allà surten el jar (`DoDeReBu_App-5.0.jar`), `app.properties` (filtrat per Maven: `app.name = DoDeReBu_v${project.version}` i `app.version`; `AppConfig.getAppName()` i `getAppVersion()`: títol de la finestra i diàleg de benvinguda), i, **llegida del pom pels scripts de build**, el nom de l'exe, de la carpeta portable i del zip (`DoDeReBu_v5.0.exe`, `portable/DoDeReBu_v5.0/`, `DoDeReBu_v5.0.zip`; jpackage rep també `--app-version`). **Per passar a una versió nova només cal canviar el `<version>` del pom** (i l'`@version` del javadoc, si es vol; no és funcional): **la carpeta del projecte ja no depèn de la versió i es diu `DoDeReBu_App`** (abans `DoDeReBu_v4.1`). `artifactId` i `name` del pom: `DoDeReBu_App`. `README.md`, `LICENSE.txt` i el javadoc diuen 5.0. **Tags de publicació: `publicat_v<versió>_<data>`** (p. ex. `publicat_v5.0_18-11-26`, l'estat publicat de la 5.0; el tag anterior `publicat_18-11-26` s'ha substituït per aquest); tag **`v5.0`** = la versió 5.0 (la branca `disseny_grafic` s'hi va fusionar per fast-forward: tot és a `master`).
- **branca `disseny_grafic` (15–18-11-26)** Tema `moni` (per defecte; l'únic de fàbrica) amb la gamma `colors_manual` (colors exactes del PDF del manual); gammes `moni`/`moni2` conservades i temes `moni2`/classic/manual fora de la carpeta d'usuari; tecles seleccionades/apagades/sense selecció i il·luminació en sonar (desactivada); notes de la graella en blau i amb el seu color en sonar; **carril de marques** (fletxeta, `;`, notació, separador, caixa «Tò; Tempo; Vol») amb la **franja d'acords a 7 files**, sense acords però amb les marques editables clicant-hi; botó de volum amb el matís (`127 (ff)`); **barra de compàs fantasma corregida**; transposició al botó de l'instrument; caixes del costat del teclat sempre a la barra inicial; botó de format a la fila del títol; ajuda, `config.properties` i `TemesTest` al dia; traça de consola de `MyChoice.addKey()` esborrada.
- **d7d872d** Porta-retalls de la lletra (Ctrl + arrossegar a la franja) i punt d'enganxada marcat abans amb Ctrl-clic, també per a les notes. El camí antic de Ctrl-V hi queda, sense documentar. Volum per defecte de la pista d'acords a 127.
- **2d8befe** L'últim acord arriba al final del seu compàs; la doble barra ja no s'arrodoneix a la pàgina; `lastColWritten` compta les notes dels acords; `nNotes` es recompta de la graella; columnes d'intervals amb terra de dues xifres.
- **f62059d** Les notes dels acords es desaven només si la pista era visible (o sigui mai): la porta del bucle de desat passa a ser el `trackMap`. En carregar, a la pista marcada amb `id=1003` se li salten els `NOTE_ON`/`NOTE_OFF`: els símbols ja regeneren l'acompanyament.
- **f7f325b** La franja d'acords, de 8 files a 6: la caixa d'una marca fa exactament una línia d'acord i comparteixen la font (`getBaseChordFont`). Abans l'alçada de la marca sortia de la font i no quadrava amb res.
- **28f5629** Els símbols d'acord es desen sempre (no només amb les notes MIDI); el baix va després dels claudàtors a `basicString()`; `ensureSomeTrackSelected()` perquè una partitura només d'acords no es quedi sense pista; columnes d'intervals a la dreta, font més petita i encabida a l'ample.
- **97dd375** Acords `7b5` i `7#5`; la plantilla exacta mana sobre la de la base, o sigui que els sis acords amb novena recuperen el seu símbol (`Cmaj9`, `Cadd9`, `Cmadd9`); `stripAngloRoot` deixa de menjar-se la a de `Faug`.
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

## Comandes de l'usuari (acordades 19-11-26)

Quatre paraules soles, cada una **inclou les anteriors**. Quan l'usuari en diu una, es fa tot el que segueix sense tornar a preguntar (el permís per fer commit, push i tag és aquest mateix encàrrec). Si alguna cosa falla pel camí (compilació, test, push), s'atura i es diu, sense seguir.

| Comanda | Què vol dir |
|---|---|
| **documenta** | Revisar i, si cal, actualitzar **tota la documentació** que toca el que s'ha canviat: javadoc (CA/EN), ajuda (`MyHelpDialog` i claus `help.*` als tres idiomes), comentaris del `config.properties` (`config.*.comment`) i `helpConfig()`, textos de `README.md`/`LICENSE` si cal, i aquest `CLAUDE.md`. Els bundles han de tenir el mateix joc de claus i placeholders. No es toca el codi funcional. |
| **comita** | **Primer de tot, compilar i passar les proves quan calgui** (`mvn -o -DskipTests compile`; `TemesTest`/`ChordSymbolsTest` si s'han tocat temes o acords): si falla, s'atura abans de res. Després **documenta** + `git commit` (missatge en català, amb l'atribució que toqui) + `git push` de la branca actual. |
| **publica** | **comita** + **tag** de publicació `publicat_v<versió>_<dd-mm-aa>` (la versió és la del `<version>` del `pom.xml`; la data és la d'avui, p. ex. `publicat_v5.0_19-11-26`) i push del tag. Si ja existeix un tag de la mateixa versió i dia, es mou (es diu). |
| **pack** | **publica** + `mvn clean package` **un cop acabat documenta** (el jar du dins els recursos —ajuda i textos— i el `documenta` pot haver-los tocat; el `02_Build…bat` **no compila**: agafa el jar de `target/`, o sigui que sense això el portable sortiria amb la documentació vella) + generar el **portable** i el **zip** (vegeu «Build»: scripts de `../Complements_Bu/` copiats a l'arrel, executats des d'allà i esborrats) + **copiar el zip a `G:\La meva unitat\Dodecaphenia\doderebu_web\media\`** (substituint el de la versió anterior si n'hi havia) + **dir quins arxius s'han de pujar a cdmon** (el zip, i qualsevol altre arxiu del web que s'hagi tocat; si ha canviat la versió, també les referències a `config.php` i `content/baixades.*.php`). |

Notes: cap comanda no puja res a cdmon (ho fa l'usuari); no es fa cap `push --force` ni es salten els hooks; si l'arbre té canvis que no són de la feina d'aquesta sessió, es diu abans de comitar-los.

## Build
Maven (`pom.xml`). Java 16. Maven no és al PATH, però n'hi ha un dins del NetBeans: `C:\Program Files\NetBeans-15\netbeans\java\maven\bin\mvn.cmd`, amb `JAVA_HOME` a `C:\Program Files\Microsoft\jdk-21.0.9.10-hotspot`.

Els tres passos, en ordre:

1. **Compilar**: `mvn clean package` (o Clean & Build al NetBeans). El JAR va a `target/`.
2. **Portable**: `02_BuildPortableExeAmbJREintegrat_CleanNBuild_runInCmd.bat` → `portable/DoDeReBu_v<versió>/` (p. ex. `portable/DoDeReBu_v5.0/DoDeReBu_v5.0.exe`; l'exe porta també la versió interna, `--app-version`).
3. **Zip**: `03_BuildZip.bat` → `DoDeReBu_v<versió>.zip` a l'arrel (p. ex. `DoDeReBu_v5.0.zip`).

**Compte amb el pas 1**: malgrat el nom, el script `02_...CleanNBuild...` **no compila res**. Només agafa el JAR més recent de `target/` i el passa pel `jpackage`. Si no s'ha compilat abans, la portable surt amb codi vell sense avisar de res.

**Què hi ha al paquet**: el zip conté el contingut de `portable/DoDeReBu_v<versió>/` (sense la carpeta arrel, per evitar el doble directori en extreure), més `SongsInBooklet/` i `Dodecagrams/`, que el script hi copia abans de comprimir i en treu després. Si `Dodecagrams/` no hi és, avisa i continua.

**Dependències a `target/libs/`**: el JAR de `target/` és prim (només les classes del projecte). Perquè la portable funcioni, `maven-dependency-plugin` copia jMusic, PDFBox i Batik a `target/libs/` i `maven-jar-plugin` escriu al manifest un `Class-Path` amb prefix `libs/`. El `jpackage` copia tot l'arbre de `target/` dins d'`app/`, o sigui que `app/libs/` hi arriba i el manifest la hi troba. **No** treure cap dels dos plugins del `pom.xml`: sense ells la portable arrenca i peta amb `NoClassDefFoundError`. El script busca l'únic `.jar` de l'arrel de `target/` per decidir el `--main-jar`, per això les dependències han d'anar a `libs/` i no a l'arrel.

**«Obre amb» des del sistema.** `MyMain.main` mira els arguments i, si el primer és un `.mid`/`.midi` existent, l'obre amb `loadScore` un cop la finestra és visible (després del diàleg de benvinguda, si n'hi ha). El llançador del `jpackage` ja hi passa els arguments, o sigui que no cal cap canvi als scripts. Cada «Obre amb» arrenca una instància nova: no carrega el fitxer a una aplicació ja oberta.

**Perquè l'aplicació surti a la llista d'«Obre amb»**, `jpackage --file-associations` no serveix: només escriu al registre als instal·ladors, i aquí es fa un `app-image`. El portable porta dos `.bat` (`Set_OpenMidiFilesWithDoDeReBu.bat` i `Unset_OpenMidiFilesWithDoDeReBu.bat`) que escriuen o esborren les claus de `HKCU\Software\Classes\Applications\<exe>` —sense administrador— i que el `02_Build…bat` copia al portable des de `../Complements_Bu/portable_extras/`. Cal tornar a executar el de registre si es canvia la carpeta de lloc.

**Com es fa el portable i el zip (18-11-26, comprovat)**: (1) `mvn -o -DskipTests package` (el Maven del NetBeans, amb `JAVA_HOME` al JDK 21) genera `target/DoDeReBu_App-5.0.jar`; (2) copiar `02_Build…bat` i `03_BuildZip.bat` d'`../Complements_Bu/` a l'arrel del projecte i executar-los **des de l'arrel** (`& '._Build…bat'` a PowerShell: amb `cmd /c "…"` entre cometes no els troba); (3) esborrar-ne les còpies. Surten `portable/DoDeReBu_v5.0/DoDeReBu_v5.0.exe` (versió interna 5.0, `--app-version`) i `DoDeReBu_v5.0.zip` (≈56 MB), amb `Set_/Unset_OpenMidiFilesWithDoDeReBu.bat` dins. **Els `.bat` han de ser CRLF**: un `sed -i` des del Git Bash els passa a LF i el `cmd` els llegeix a trossos («"M" no se reconoce…»); per canviar-hi text, fer servir Python amb `newline=''` o convertir de nou a CRLF. Els tres `.bat` modificats (`02`, `Set_…`, `Unset_…`) es van haver de recuperar així.

**Ubicació dels scripts de build**: `01_baixa_jre21_FET_A_HP.bat`, `02_BuildPortableExeAmbJREintegrat_CleanNBuild_runInCmd.bat` i `03_BuildZip.bat` ja no són a l'arrel del projecte: ara viuen a `../Complements_Bu/` (germana de `DoDeReBu_App/`), fora del control de versions. **Important**: aquests scripts assumeixen que s'executen des de la carpeta del projecte (`pushd "%~dp0"`, `APP_NAME` = `DoDeReBu_v<versió>` amb la versió llegida del `pom.xml` de l'arrel, rutes relatives com `target/`, `vendor/jre21/`) — si s'executen directament des de `Complements_Bu/` fallaran (`target/`, `vendor/` no hi són). Cal copiar-los (o crear un enllaç/còpia) a l'arrel de `DoDeReBu_App/` abans d'executar-los, o adaptar-los per acceptar la ruta del projecte com a paràmetre.

**Una trampa del `cmd` al `:log` dels scripts**: `echo %~1` analitza la redirecció **abans** d'imprimir, o sigui que qualsevol `>` dins d'un missatge (el `->` dels passos 10, 11, 12 i 44 del `03_BuildZip.bat`) se'l menjava i provava d'escriure en un fitxer inexistent. La rutina desa ara el text en una variable i l'imprimeix amb expansió retardada (`echo(!MSG!`), que ja no es reanalitza. Si es toca cap dels altres scripts, tenir-ho present.

També s'han mogut a `../Complements_Bu/`: `AllSymbols.csv`, `ChordSymbols.csv` (dades de referència, no llegides en temps d'execució — `ChordSymbols.java` té les dades hardcoded), `MeMima.ttf` (no referenciat des de `src/`), `Liam/`, `Prompts/`, `SongsInBooklet - bkp/`, `portable/`, `portable_bkp/` i el zip de distribució.
