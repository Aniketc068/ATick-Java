@echo off
REM Run an ATick example via Maven — uses the io.github.aniketc068:atick dependency, NO source / NO jar build.
REM Usage:  run.bat SignPfx
REM   (SignPfx, PadesLevels, Appearance, MarkColor, HashAlgorithms, Invisible, TickVariations, DeferredEsign)
if "%~1"=="" ( echo Usage: run.bat ^<ScriptName^>  ^(e.g. run.bat SignPfx^) & exit /b 1 )
cd examples && mvn -q compile exec:java -Dexec.mainClass=%1
