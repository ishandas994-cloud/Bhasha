#!/usr/bin/env bash
# tests/run-tests.sh
# BanglaLang test runner (Linux/macOS) - same contract as run-tests.ps1.

set -uo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

CLI="${1:-}"
if [[ -z "$CLI" ]]; then
    if   [[ -x "$ROOT/backend/target/bangla/bin/bangla" ]]; then
        CLI="$ROOT/backend/target/bangla/bin/bangla"
    elif [[ -f "$ROOT/backend/target/bangla-lang-cli.jar" ]]; then
        CLI="jar:$ROOT/backend/target/bangla-lang-cli.jar"
    else
        echo "No BanglaLang CLI found. Build one first: ./scripts/build-cli.sh" >&2
        exit 1
    fi
fi

run_bangla() {
    local script="$1"
    case "$CLI" in
        jar:*) java -jar "${CLI#jar:}" run "$script" ;;
        *)     "$CLI" run "$script" ;;
    esac
}

passed=0
failed=0

echo ""
echo "BanglaLang test suite"
echo "------------------------------------------------------------"

for test in "$ROOT"/tests/*.bangla; do
    name="$(basename "$test")"
    expected_file="${test%.bangla}.expected"

    [[ -f "$expected_file" ]] || { echo "  SKIP  $name (no .expected)"; continue; }

    actual="$(run_bangla "$test" 2>&1)"
    expected="$(cat "$expected_file")"

    if [[ "$(echo "$actual" | xargs)" == "$(echo "$expected" | xargs)" ]]; then
        passed=$((passed + 1))
        echo "  PASS  $name"
    else
        failed=$((failed + 1))
        echo "  FAIL  $name"
        echo "    --- expected ---"; echo "$expected" | sed 's/^/    | /'
        echo "    --- actual ---";   echo "$actual"   | sed 's/^/    | /'
    fi
done

echo "------------------------------------------------------------"
echo "Results: $passed passed, $failed failed"

[[ $failed -eq 0 ]]
