#!/usr/bin/env bash
# Lanzador para Linux/macOS. Coloque este script junto a PinkyPuff.jar.
cd "$(dirname "$0")" || exit 1
exec java -jar PinkyPuff.jar "$@"
