#!/usr/bin/env bash
# Installe un JDK 17 local pour lancer les tests et les generateurs du depot.
#
#   ./tools/install-jdk.sh              -> dans tools/.jdk (ignore par git)
#   ./tools/install-jdk.sh /tmp/jdk17   -> a l'endroit demande
#
# Le paquet npm utilise contient un JDK complet (javac, java, jmods) : il est
# telechargeable la ou les depots Java habituels (Adoptium, Maven) ne le sont pas.
set -euo pipefail

TARGET="${1:-$(cd "$(dirname "$0")" && pwd)/.jdk}"
VERSION="17.0.8"
PACKAGE="javajre-linux-64"
URL="https://registry.npmjs.org/${PACKAGE}/-/${PACKAGE}-${VERSION}.tgz"

if [ -x "${TARGET}/bin/javac" ]; then
    echo "JDK deja installe : ${TARGET}"
    "${TARGET}/bin/javac" -version
    exit 0
fi

echo "Telechargement de ${URL}"
WORK="$(mktemp -d)"
trap 'rm -rf "${WORK}"' EXIT
curl -sL -o "${WORK}/jdk.tgz" "${URL}"
mkdir -p "${WORK}/unpack"
tar xzf "${WORK}/jdk.tgz" -C "${WORK}/unpack"

# la racine du JDK est le dossier qui contient bin/javac
SOURCE="$(dirname "$(dirname "$(find "${WORK}/unpack" -name javac -path '*/bin/*' | head -1)")")"
if [ ! -x "${SOURCE}/bin/javac" ]; then
    echo "javac introuvable dans l'archive" >&2
    exit 1
fi
mkdir -p "$(dirname "${TARGET}")"
rm -rf "${TARGET}"
mv "${SOURCE}" "${TARGET}"

echo "JDK installe : ${TARGET}"
"${TARGET}/bin/javac" -version
echo
echo "Utilisation :"
echo "  ./tools/run-core-tests.sh ${TARGET}"
echo "  python3 tools/generate-lang.py ${TARGET}"
echo "  python3 tools/preview-skins.py"
