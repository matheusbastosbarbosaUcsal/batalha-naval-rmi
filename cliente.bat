@echo off
rem Abre um cliente. Uso: cliente.bat [IP_DO_SERVIDOR] [IP_DESTA_MAQUINA]
setlocal
cd /d "%~dp0"

set "JAVA=java"
if defined JAVA_HOME set "JAVA=%JAVA_HOME%\bin\java"

"%JAVA%" -cp bin cliente.ClienteMain %*
