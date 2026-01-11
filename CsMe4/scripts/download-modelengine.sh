#!/usr/bin/env bash
set -euo pipefail

VERSION="R4.0.9-2210"
DEST_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../libs" && pwd)"
DEST_FILE="${DEST_DIR}/ModelEngine-${VERSION}.jar"
URL="https://repo.ticxo.net/repository/maven-public/com/ticxo/modelengine/ModelEngine/${VERSION}/ModelEngine-${VERSION}.jar"

mkdir -p "${DEST_DIR}"

curl -fL -o "${DEST_FILE}" "${URL}"

echo "Downloaded ${DEST_FILE}"
