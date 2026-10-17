@echo off
REM FitStore - lanzador para doble clic (abre PowerShell con el script de arranque)
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start.ps1"
pause
