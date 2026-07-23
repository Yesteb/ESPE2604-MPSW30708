@echo off
REM Alternativa a PinkyPuff.exe, por si el antivirus bloquea el .exe.
REM Requiere Java 21 o superior instalado.
cd /d "%~dp0"
javaw -jar PinkyPuff.jar %*
