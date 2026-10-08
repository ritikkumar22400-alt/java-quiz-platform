@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ============================================
echo   Java-Based Online Quiz Platform
echo ============================================
echo.

if not exist out mkdir out

echo Compiling sources...
set "SRCLIST=%TEMP%\jq_sources.txt"
if exist "%SRCLIST%" del "%SRCLIST%"
for /r src %%f in (*.java) do (
    set "line=%%f"
    echo "!line:\=/!" >> "%SRCLIST%"
)
javac -encoding UTF-8 -d out @"%SRCLIST%"
if errorlevel 1 (
    echo.
    echo Compilation failed. Fix the errors above and try again.
    pause
    exit /b 1
)

echo Starting application...
java -cp out quizplatform.Main
endlocal
