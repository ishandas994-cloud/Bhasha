#!/usr/bin/env bash
# scripts/build-cli.sh
# Build the BanglaLang CLI locally on Linux/macOS.
#
#   ./scripts/build-cli.sh           -> backend/target/bangla-lang-cli.jar
#   ./scripts/build-cli.sh --native  -> + dist/bangla-linux-x64.tar.gz (or macOS)
#
# Requires JDK 17+ (Maven optional; falls back to plain javac).

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND="$ROOT/backend"

build_with_maven() {
    echo "Building with Maven..."
    (cd "$BACKEND" && mvn -B -Pcli clean package -DskipTests)
}

build_with_javac() {
    echo "Maven not found - compiling with javac directly..."
    local classes="$BACKEND/target/cli-classes"
    rm -rf "$classes"
    mkdir -p "$classes"

    # Compile everything except the Spring Boot entry point and REST layer.
    find "$BACKEND/src/main/java" -name '*.java' \
        ! -path '*/com/banglalang/api/*' ! -name 'Main.java' > sources.txt
    javac -encoding UTF-8 -d "$classes" @sources.txt
    rm sources.txt

    jar cfe "$BACKEND/target/bangla-lang-cli.jar" com.banglalang.cli.CliMain -C "$classes" .
}

NATIVE=false
[[ "${1:-}" == "--native" ]] && NATIVE=true

if command -v mvn >/dev/null 2>&1; then
    build_with_maven
else
    build_with_javac
fi

JAR="$BACKEND/target/bangla-lang-cli.jar"
echo ""
echo "Built: $JAR"

if [[ "$NATIVE" == true ]]; then
    echo "Packaging native launcher with jpackage..."
    INPUT="$BACKEND/target/jpackage-input"
    mkdir -p "$INPUT"
    cp "$JAR" "$INPUT/"

    case "$(uname -s)" in
        Darwin) ARTIFACT="bangla-macos-aarch64.tar.gz" ;;
        *)      ARTIFACT="bangla-linux-x64.tar.gz" ;;
    esac

    jpackage --type app-image --name bangla --app-version "1.0.0" --vendor "BanglaLang" \
        --input "$INPUT" --main-jar bangla-lang-cli.jar --dest "$BACKEND/target/dist"

    mkdir -p "$ROOT/dist"
    tar -czf "$ROOT/dist/$ARTIFACT" -C "$BACKEND/target/dist" bangla
    echo "Packaged: $ROOT/dist/$ARTIFACT"
fi

echo ""
echo "Try it:"
echo "  java -jar \"$JAR\" run examples/hello.bangla"
