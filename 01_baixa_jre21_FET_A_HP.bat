@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM ========================
REM Config
REM ========================
set "JAVA_VERSION=21"
set "ARCH=x64"
set "VENDOR_DIR=vendor"
set "OUT_DIR=%VENDOR_DIR%\jre21"

REM Adoptium API v3 - provarem 2 URLs (1r: eclipse, 2n: adoptium)
set "JRE_URL1=https://api.adoptium.net/v3/binary/latest/%JAVA_VERSION%/ga/windows/%ARCH%/jre/hotspot/normal/eclipse"
set "JRE_URL2=https://api.adoptium.net/v3/binary/latest/%JAVA_VERSION%/ga/windows/%ARCH%/jre/hotspot/normal/adoptium"

REM Work dirs
set "BUILD_DIR=_build_jre"
set "ZIP_FILE=%BUILD_DIR%\temurin-jre.zip"
set "EXTRACT_DIR=%BUILD_DIR%\extract"

REM ========================
REM Checks
REM ========================
where powershell >nul 2>&1
if errorlevel 1 (
  echo [ERROR] No trobo PowerShell ^(powershell.exe^) al sistema.
  echo Solucio: metode offline: baixa manualment un JRE 21 i descomprimeix-lo a "%OUT_DIR%".
  exit /b 1
)

REM ========================
REM Clean + prepare
REM ========================
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
mkdir "%BUILD_DIR%"
mkdir "%EXTRACT_DIR%"

if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
if not exist "%VENDOR_DIR%" mkdir "%VENDOR_DIR%"

REM ========================
REM Download ZIP (intent 1 i intent 2)
REM ========================
echo.
echo Baixant JRE Temurin %JAVA_VERSION% - Windows %ARCH%...
echo Intent 1 URL: %JRE_URL1%

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ProgressPreference='SilentlyContinue';" ^
  "Invoke-WebRequest -Uri '%JRE_URL1%' -OutFile '%ZIP_FILE%' -UseBasicParsing"

if errorlevel 1 (
  echo.
  echo Intent 1 ha fallat. Provant intent 2...
  echo Intent 2 URL: %JRE_URL2%

  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ProgressPreference='SilentlyContinue';" ^
    "Invoke-WebRequest -Uri '%JRE_URL2%' -OutFile '%ZIP_FILE%' -UseBasicParsing"
)

if errorlevel 1 (
  echo.
  echo [ERROR] No s'ha pogut baixar el JRE amb cap de les dues URLs.
  exit /b 1
)

if not exist "%ZIP_FILE%" (
  echo.
  echo [ERROR] No s'ha creat el fitxer ZIP: "%ZIP_FILE%"
  exit /b 1
)

REM ========================
REM Extract ZIP
REM ========================
echo.
echo Extraient ZIP a "%EXTRACT_DIR%"...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "Expand-Archive -Path '%ZIP_FILE%' -DestinationPath '%EXTRACT_DIR%' -Force"

if errorlevel 1 (
  echo [ERROR] No s'ha pogut extreure el ZIP.
  exit /b 1
)

REM ========================
REM Move extracted folder to vendor\jre21
REM ========================
echo.
echo Preparant carpeta final: "%OUT_DIR%"...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$d = Get-ChildItem '%EXTRACT_DIR%' -Directory | Select-Object -First 1;" ^
  "if(-not $d){ throw 'No he trobat cap carpeta dins del ZIP del JRE.' }" ^
  "Move-Item -Path $d.FullName -Destination '%OUT_DIR%' -Force"

if errorlevel 1 (
  echo [ERROR] No s'ha pogut moure el JRE a "%OUT_DIR%".
  exit /b 1
)

if not exist "%OUT_DIR%\bin\java.exe" (
  echo [ERROR] El JRE no ha quedat be: no trobo "%OUT_DIR%\bin\java.exe".
  exit /b 1
)

echo.
echo OK! JRE preparat a: "%OUT_DIR%"
echo Ara executa: 02_build_portable.bat
endlocal
