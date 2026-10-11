#!/bin/sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
VERSION=$(sed -n 's/^mavenVersion=//p' "$SCRIPT_DIR/.mvn/wrapper/maven-wrapper.properties")
: "${VERSION:?Could not read Maven version}"

CACHE_ROOT="${MAVEN_USER_HOME:-$HOME/.m2}/wrapper/dists"
MAVEN_HOME="$CACHE_ROOT/apache-maven-$VERSION"
MAVEN_ZIP="$CACHE_ROOT/apache-maven-$VERSION-bin.zip"
URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$VERSION/apache-maven-$VERSION-bin.zip"

if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  mkdir -p "$CACHE_ROOT"
  TMP_DIR=$(mktemp -d)
  trap 'rm -rf "$TMP_DIR"' EXIT INT TERM
  echo "Downloading Maven $VERSION..."
  curl -fsSL "$URL" -o "$TMP_DIR/maven.zip"
  rm -rf "$CACHE_ROOT/apache-maven-$VERSION"
  unzip -q "$TMP_DIR/maven.zip" -d "$CACHE_ROOT"
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
