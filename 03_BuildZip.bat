@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM ==================================================
REM 04_CreaZipPortable.bat
REM - Fa zip del CONTINGUT de portable\<APP_NAME>\ (no inclou la carpeta arrel)
REM   -> evita doble carpeta quan l'extractor crea carpeta automàtica pel nom del zip
REM - Abans copia SongsInBooklet (des de l'arrel del projecte) dins la carpeta de l'app
REM - Elimina la subcarpeta logs (si existeix) ABANS de zipejar
REM ==================================================

REM --- APP_NAME = nom de la carpeta on és aquest .bat (arrel projecte)
for %%A in ("%~dp0.") do set "APP_NAME=%%~nxA"

set "PROJECT_DIR=%~dp0"
set "DEST_DIR=%PROJECT_DIR%portable"
set "APP_DIR=%DEST_DIR%\%APP_NAME%"
set "EXE_PATH=%APP_DIR%\%APP_NAME%.exe"
set "SONGS_SRC=%PROJECT_DIR%SongsInBooklet"
set "SONGS_DST=%APP_DIR%\SongsInBooklet"

REM Zip al directori arrel del projecte (al costat del bat)
set "ZIP_PATH=%PROJECT_DIR%%APP_NAME%.zip"

REM Log simple
set "LOG=%PROJECT_DIR%zip_debug.log"
break > "%LOG%"

call :log "00 START zip"
call :log "01 PROJECT_DIR=%PROJECT_DIR%"
call :log "02 APP_NAME=%APP_NAME%"
call :log "03 APP_DIR=%APP_DIR%"
call :log "04 ZIP_PATH=%ZIP_PATH%"

REM --- Checks bàsics
if not exist "%APP_DIR%\" (
  call :log "10 ERROR: No existeix APP_DIR. Has fet el jpackage abans? -> %APP_DIR%"
  goto :fail
)

if not exist "%EXE_PATH%" (
  call :log "11 ERROR: No trobo l'EXE esperat -> %EXE_PATH%"
  goto :fail
)

if not exist "%SONGS_SRC%\" (
  call :log "12 ERROR: No existeix SongsInBooklet a l'arrel -> %SONGS_SRC%"
  goto :fail
)

REM --- 1) Eliminar logs si existeix
set "LOGS_DIR=%APP_DIR%\logs"
if exist "%LOGS_DIR%\" (
  call :log "20 Eliminant logs abans del zip: %LOGS_DIR%"
  rmdir /s /q "%LOGS_DIR%" >> "%LOG%" 2>&1
  if exist "%LOGS_DIR%\" (
    call :log "21 ERROR: No he pogut eliminar logs (fitxers en ús?)."
    goto :fail
  )
  call :log "22 OK: logs eliminat."
) else (
  call :log "23 OK: No hi ha logs."
)

REM --- 2) Copiar SongsInBooklet dins la carpeta de l'app (temporal)
call :log "30 Copiant SongsInBooklet a l'app..."
if exist "%SONGS_DST%\" (
  call :log "31 Esborrant SongsInBooklet previ dins app..."
  rmdir /s /q "%SONGS_DST%" >> "%LOG%" 2>&1
)

robocopy "%SONGS_SRC%" "%SONGS_DST%" /E /NFL /NDL /NJH /NJS /NC /NS >> "%LOG%" 2>&1
set "RBC=%ERRORLEVEL%"
REM Robocopy retorna 0..7 com a “OK”
if %RBC% GEQ 8 (
  call :log "32 ERROR: robocopy ha fallat. ERRORLEVEL=%RBC%"
  goto :cleanup_fail
)
call :log "33 OK: SongsInBooklet copiat (robocopy rc=%RBC%)"

REM --- 3) Crear ZIP del CONTINGUT (no incloure la carpeta APP_NAME)
call :log "40 Creant ZIP (sense carpeta arrel)..."

if exist "%ZIP_PATH%" (
  call :log "41 Esborrant ZIP anterior: %ZIP_PATH%"
  del /q "%ZIP_PATH%" >> "%LOG%" 2>&1
)

pushd "%APP_DIR%" >nul

REM Important:
REM -Path '*' zipeja el contingut directament (exe, runtime, config, etc.)
REM Això evita el doble folder quan l'extractor crea carpeta automàtica.
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "Compress-Archive -Path '*' -DestinationPath '%ZIP_PATH%' -Force" >> "%LOG%" 2>&1

set "ZIP_RC=%ERRORLEVEL%"
popd >nul

if not "%ZIP_RC%"=="0" (
  call :log "42 ERROR: Compress-Archive ha fallat. ERRORLEVEL=%ZIP_RC%"
  goto :cleanup_fail
)

if not exist "%ZIP_PATH%" (
  call :log "43 ERROR: No s'ha creat el ZIP (no existeix)."
  goto :cleanup_fail
)

call :log "44 OK: ZIP creat -> %ZIP_PATH%"

REM --- 4) Treure la còpia temporal SongsInBooklet de la carpeta portable
call :log "50 Netejant SongsInBooklet temporal dins app..."
if exist "%SONGS_DST%\" (
  rmdir /s /q "%SONGS_DST%" >> "%LOG%" 2>&1
)
call :log "51 OK: neteja feta."

call :log "90 DONE OK"
echo.
echo OK. ZIP creat: "%ZIP_PATH%"
echo En extreure'l, no hauria d'aparèixer la carpeta duplicada.
echo Log: "%LOG%"
exit /b 0


:cleanup_fail
REM Neteja en cas d'error
if exist "%SONGS_DST%\" (
  rmdir /s /q "%SONGS_DST%" >> "%LOG%" 2>&1
)
:fail
call :log "99 FAIL"
echo.
echo HA FALLAT. Mira el log: "%LOG%"
exit /b 1


:log
echo %~1
>> "%LOG%" echo %~1
exit /b 0
