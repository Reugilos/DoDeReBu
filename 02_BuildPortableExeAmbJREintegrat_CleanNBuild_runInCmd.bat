@echo off
setlocal EnableExtensions

REM ========================
REM Debug log
REM ========================
set "LOG=%~dp0build_debug.log"
break > "%LOG%"
call :log "00 START  (bat=%~f0)"
call :log "01 CWD(before)=%CD%"

REM Sempre relatiu al directori del .bat
pushd "%~dp0" >nul
call :log "02 pushd ok. CWD(now)=%CD%"

REM ========================
REM APP_NAME = nom de la carpeta on és el .bat
REM (ex: ...\DoDeReBu_v4.0\03_...bat  -> APP_NAME=DoDeReBu_v4.0)
REM ========================
for %%A in ("%~dp0.") do set "APP_NAME=%%~nxA"

REM ========================
REM Config app
REM ========================
set "INPUT_DIR=target"
set "MAIN_CLASS=dodecagraphone.ui.MyMain"
set "DEST_DIR=portable"
set "RUNTIME_DIR=vendor\jre21"

call :log "03 Vars set: APP_NAME=%APP_NAME%"
call :log "04 Vars set: INPUT_DIR=%INPUT_DIR% MAIN_CLASS=%MAIN_CLASS%"
call :log "05 Vars set: DEST_DIR=%DEST_DIR% RUNTIME_DIR=%RUNTIME_DIR%"

REM ========================
REM Checks
REM ========================
call :log "10 Checking jpackage..."
where jpackage >> "%LOG%" 2>&1
if errorlevel 1 (
  call :log "11 ERROR: jpackage not found"
  goto :fail
)
call :log "12 OK: jpackage found"

call :log "13 Checking INPUT_DIR exists: %CD%\%INPUT_DIR%\"
if not exist "%INPUT_DIR%\" (
  call :log "14 ERROR: INPUT_DIR missing"
  goto :fail
)
call :log "15 OK: INPUT_DIR exists"

call :log "16 Checking runtime java: %CD%\%RUNTIME_DIR%\bin\java.exe"
if not exist "%RUNTIME_DIR%\bin\java.exe" (
  call :log "17 ERROR: runtime java.exe missing"
  goto :fail
)
call :log "18 OK: runtime java.exe exists"

REM ========================
REM Trobar el JAR més recent amb PowerShell (robust)
REM ========================
call :log "20 Finding newest JAR via PowerShell..."

set "MAIN_JAR="
for /f "usebackq delims=" %%J in (`
  powershell -NoProfile -Command ^
    "$j = Get-ChildItem -Path '%INPUT_DIR%' -Filter '*.jar' -File |" ^
    "  Where-Object { $_.Name -notmatch '(-sources\.jar$)|(-javadoc\.jar$)' } |" ^
    "  Sort-Object LastWriteTime -Descending |" ^
    "  Select-Object -First 1;" ^
    "if ($j) { [Console]::Out.WriteLine($j.Name) }"
`) do (
  if not "%%J"=="" (
    set "MAIN_JAR=%%J"
    goto :jar_done
  )
)

:jar_done
call :log "21 MAIN_JAR='%MAIN_JAR%'"

if "%MAIN_JAR%"=="" (
  call :log "22 ERROR: MAIN_JAR empty"
  goto :fail
)

if /i not "%MAIN_JAR:~-4%"==".jar" (
  call :log "22B ERROR: MAIN_JAR not a .jar: %MAIN_JAR%"
  goto :fail
)

if not exist "%INPUT_DIR%\%MAIN_JAR%" (
  call :log "23 ERROR: JAR not found on disk: %INPUT_DIR%\%MAIN_JAR%"
  goto :fail
)

call :log "24 OK: JAR exists: %INPUT_DIR%\%MAIN_JAR%"

REM ========================
REM Build
REM ========================
call :log "30 Removing DEST_DIR if exists: %DEST_DIR%"
if exist "%DEST_DIR%\" (
  rmdir /s /q "%DEST_DIR%" >> "%LOG%" 2>&1
)

call :log "31 Calling jpackage..."
call :log "31A CMD: jpackage --type app-image --name %APP_NAME% --input %INPUT_DIR% --main-jar %MAIN_JAR% --main-class %MAIN_CLASS% --runtime-image %RUNTIME_DIR% --dest %DEST_DIR% --win-console"

jpackage ^
  --type app-image ^
  --name "%APP_NAME%" ^
  --input "%INPUT_DIR%" ^
  --main-jar "%MAIN_JAR%" ^
  --main-class "%MAIN_CLASS%" ^
  --runtime-image "%RUNTIME_DIR%" ^
  --dest "%DEST_DIR%" ^
  --win-console >> "%LOG%" 2>&1

set "JPK_RC=%ERRORLEVEL%"
call :log "32 jpackage finished. ERRORLEVEL=%JPK_RC%"
if not "%JPK_RC%"=="0" (
  call :log "33 ERROR: jpackage failed"
  goto :fail
)

REM ========================
REM Generate diagnose.bat next to the EXE
REM Layout esperat:
REM   portable\<APP_NAME>\<APP_NAME>.exe
REM ========================
call :log "40 Creating diagnose.bat..."

set "APP_DIR=%DEST_DIR%\%APP_NAME%"
set "EXE_NAME=%APP_NAME%.exe"
set "EXE_PATH=%APP_DIR%\%EXE_NAME%"
set "DIAG=%APP_DIR%\diagnose.bat"

call :log "41 Paths: APP_DIR=%APP_DIR%"
call :log "42 Paths: EXE_PATH=%EXE_PATH%"
call :log "43 Paths: DIAG=%DIAG%"

if not exist "%EXE_PATH%" (
  call :log "44 WARN: EXE not found, skip diagnose generation"
  goto :ok
)

break > "%DIAG%"
call :log "45 diagnose.bat created/cleared"

>>"%DIAG%" echo @echo off
>>"%DIAG%" echo setlocal EnableExtensions
>>"%DIAG%" echo.
>>"%DIAG%" echo REM Executa l'app i guarda stdout+stderr a logs\console.log. Fa pause si peta.
>>"%DIAG%" echo set "APP_DIR=%%~dp0"
>>"%DIAG%" echo set "EXE=%%APP_DIR%%%EXE_NAME%"
>>"%DIAG%" echo.
>>"%DIAG%" echo if not exist "%%EXE%%" ^(
>>"%DIAG%" echo   echo [ERROR] No trobo l'executable:
>>"%DIAG%" echo   echo %%EXE%%
>>"%DIAG%" echo   pause
>>"%DIAG%" echo   exit /b 1
>>"%DIAG%" echo ^)
>>"%DIAG%" echo.
>>"%DIAG%" echo set "LOGDIR=%%APP_DIR%%logs"
>>"%DIAG%" echo if not exist "%%LOGDIR%%" mkdir "%%LOGDIR%%" ^>nul 2^>^&1
>>"%DIAG%" echo set "LOG=%%LOGDIR%%\console.log"
>>"%DIAG%" echo.
>>"%DIAG%" echo echo ==================================================^>^>"%%LOG%%"
>>"%DIAG%" echo echo [%%date%% %%time%%] Launching: %%EXE%%^>^>"%%LOG%%"
>>"%DIAG%" echo echo WorkingDir: %%CD%%^>^>"%%LOG%%"
>>"%DIAG%" echo echo ==================================================^>^>"%%LOG%%"
>>"%DIAG%" echo.
>>"%DIAG%" echo "%%EXE%%" %%* ^>^> "%%LOG%%" 2^>^&1
>>"%DIAG%" echo set "RC=%%ERRORLEVEL%%"
>>"%DIAG%" echo.
>>"%DIAG%" echo echo.^>^>"%%LOG%%"
>>"%DIAG%" echo echo [%%date%% %%time%%] Finished. ERRORLEVEL=%%RC%%^>^>"%%LOG%%"
>>"%DIAG%" echo.
>>"%DIAG%" echo if not "%%RC%%"=="0" ^(
>>"%DIAG%" echo   echo.
>>"%DIAG%" echo   echo [ERROR] L'aplicacio ha acabat amb codi %%RC%%.
>>"%DIAG%" echo   echo Log guardat a: "%%LOG%%"
>>"%DIAG%" echo   echo.
>>"%DIAG%" echo   type "%%LOG%%"
>>"%DIAG%" echo   echo.
>>"%DIAG%" echo   echo Prem una tecla per tancar...
>>"%DIAG%" echo   pause ^>nul
>>"%DIAG%" echo ^)
>>"%DIAG%" echo endlocal

call :log "46 diagnose.bat written OK"

:ok
call :log "90 DONE OK"
echo.
echo OK. Mira el log: "%LOG%"
popd >nul
endlocal
exit /b 0

:fail
set "FAIL_RC=%ERRORLEVEL%"
call :log "99 FAIL (see log) ERRORLEVEL=%FAIL_RC%"
echo.
echo HA FALLAT. Obre el log: "%LOG%"
popd >nul
endlocal & exit /b 1

:log
REM Escriu al log i també per consola
echo %~1
>> "%LOG%" echo %~1
exit /b 0
