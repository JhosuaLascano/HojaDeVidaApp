#!/usr/bin/env bash
# Copia la web (fuente única) dentro de los assets del contenedor Flutter.
set -e
cd "$(dirname "$0")"
rm -rf cv_flutter_wrapper/assets/web
cp -r web cv_flutter_wrapper/assets/web
echo "Web copiada a cv_flutter_wrapper/assets/web"
