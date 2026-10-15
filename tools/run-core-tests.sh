#!/usr/bin/env bash
# Compile et lance les tests du moteur de jeu (aucune dependance a Minecraft ou Gradle).
#
# Usage : tools/run-core-tests.sh [chemin-vers-un-JDK-17]
#
# Un JDK 17 est necessaire (javac + java). Il peut etre fourni par :
#   - la variable d'environnement JAVA_HOME
#   - le premier argument du script
#   - le dossier tools/.jdk
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="${ROOT}/build/core-tests"

find_jdk() {
  if [ "${1:-}" != "" ] && [ -x "$1/bin/javac" ]; then echo "$1"; return; fi
  if [ "${JAVA_HOME:-}" != "" ] && [ -x "${JAVA_HOME}/bin/javac" ]; then echo "${JAVA_HOME}"; return; fi
  if [ -x "${ROOT}/tools/.jdk/bin/javac" ]; then echo "${ROOT}/tools/.jdk"; return; fi
  if command -v javac >/dev/null 2>&1; then echo "$(dirname "$(command -v javac)")/.."; return; fi
  echo ""
}

JDK="$(find_jdk "${1:-}")"
if [ -z "$JDK" ]; then
  echo "Aucun JDK trouve. Installez Java 17 ou passez le chemin : tools/run-core-tests.sh /chemin/vers/jdk-17" >&2
  exit 1
fi

echo "JDK : $JDK"
rm -rf "$OUT"
mkdir -p "$OUT"

echo "Compilation du moteur..."
"$JDK/bin/javac" -encoding UTF-8 -d "$OUT" \
  $(find "$ROOT/src/main/java/com/arcanumrealms/core" -name '*.java')

echo "Compilation des tests..."
"$JDK/bin/javac" -encoding UTF-8 -cp "$OUT" -d "$OUT" \
  $(find "$ROOT/src/test/java" -name '*.java')

echo "Execution des tests..."
echo
"$JDK/bin/java" -cp "$OUT" com.arcanumrealms.core.test.CoreTests
