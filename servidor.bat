@echo off
rem Sobe o servidor. Uso: servidor.bat [IP_DESTA_MAQUINA]
setlocal
cd /d "%~dp0"

set "JAVA=java"
if defined JAVA_HOME set "JAVA=%JAVA_HOME%\bin\java"

"%JAVA%" -cp bin servidor.ServidorMain %*
