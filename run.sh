#!/bin/sh
# Run an ATick example via Maven — uses the io.github.aniketc068:atick dependency, NO source / NO jar build.
# Usage:  ./run.sh SignPfx
#   (SignPfx, PadesLevels, Appearance, MarkColor, HashAlgorithms, Invisible, TickVariations, DeferredEsign)
[ -z "$1" ] && { echo "Usage: ./run.sh <ScriptName>   e.g. ./run.sh SignPfx"; exit 1; }
cd examples && mvn -q compile exec:java -Dexec.mainClass="$1"
