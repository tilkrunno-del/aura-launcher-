#!/usr/bin/env sh

DIR="$(cd "$(dirname "$0")" && pwd)"
GRADLE_WRAPPER="$DIR/gradle/wrapper/gradle-wrapper.jar"

exec java -jar "$GRADLE_WRAPPER" "$@"
