@echo off
rem Compila o projeto em bin\ (requer JDK 21).
setlocal
cd /d "%~dp0"

set "JAVAC=javac"
if defined JAVA_HOME set "JAVAC=%JAVA_HOME%\bin\javac"

if exist bin rmdir /s /q bin
mkdir bin

"%JAVAC%" --release 21 -encoding UTF-8 -d bin src\comum\*.java src\servidor\*.java src\cliente\*.java src\cliente\ui\*.java
if errorlevel 1 (
    echo.
    echo Falha na compilacao. Confira se o JDK e o 21 com: "%JAVAC%" -version
    exit /b 1
)
echo Compilado em bin\
