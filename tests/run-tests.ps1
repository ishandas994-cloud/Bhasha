# tests/run-tests.ps1
# BanglaLang test runner - executes every .bangla script through the CLI
# and compares stdout against the matching .expected file.
#
#   .\tests\run-tests.ps1              # auto-detects the CLI
#   .\tests\run-tests.ps1 <path>       # use a specific jar / exe / classes dir
#
# Exit code: 0 if all tests pass, 1 otherwise (CI-friendly).

param(
    [string]$CliPath = ""
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$root = Split-Path $PSScriptRoot -Parent

# ---- Locate the CLI binary (jpackage exe > jar > compiled classes) ----
function Resolve-Cli {
    if ($CliPath) {
        # Resolve relative paths to absolute - PowerShell's & operator
        # misparses bare relative paths as module names.
        $resolved = (Resolve-Path $CliPath).Path
        # Normalize whatever the user passed into the same { kind, path } shape
        if ($resolved -like "*.jar") { return @{ kind = "jar"; path = $resolved } }
        if ($resolved -like "*cli-classes*") { return @{ kind = "classes"; path = $resolved } }
        return @{ kind = "exe"; path = $resolved }
    }

    # Windows jpackage app-images put the launcher at the folder root,
    # not under bin\ (that is the Linux/macOS layout).
    $exe = Join-Path $root "backend\target\bangla\bangla.exe"
    if (Test-Path $exe) { return @{ kind = "exe"; path = $exe } }

    $jar = Join-Path $root "backend\target\bangla-lang-cli.jar"
    if (Test-Path $jar) { return @{ kind = "jar"; path = $jar } }

    $classes = Join-Path $root "backend\target\cli-classes"
    if (Test-Path $classes) { return @{ kind = "classes"; path = $classes } }

    throw "No BanglaLang CLI found. Build one first: .\scripts\build-cli.ps1"
}

$cli = Resolve-Cli

function Invoke-Bangla {
    param([string]$Script)
    switch ($cli.kind) {
        "exe"     { return ([string] (& $cli.path run $script 2>&1 | Out-String)) }
        "jar"     { return ([string] (& java -jar $cli.path run $script 2>&1 | Out-String)) }
        "classes" { return ([string] (& java -cp $cli.path com.banglalang.cli.CliMain run $script 2>&1 | Out-String)) }
        default   { throw "Unknown CLI kind: $($cli.kind)" }
    }
}

# ---- Run every test ----
$tests = Get-ChildItem -Path $PSScriptRoot -Filter "*.bangla" | Sort-Object Name
$passed = 0
$failed = 0
$results = @()

Write-Host ""
Write-Host "BanglaLang test suite ($($tests.Count) tests, CLI: $($cli.kind))" -ForegroundColor Cyan
Write-Host ("-" * 60)

foreach ($test in $tests) {
    $expectedFile = [System.IO.Path]::ChangeExtension($test.FullName, ".expected")

    if (-not (Test-Path $expectedFile)) {
        $results += [pscustomobject]@{ Name = $test.BaseName; Status = "SKIP"; Note = "no .expected file" }
        continue
    }

    # Normalize line endings (\r\n -> \n) so Windows console output
    # compares equal to the Unix-style .expected files.
    $actual = ((Invoke-Bangla $test.FullName) -replace "`r", "").Trim()
    $expected = ((Get-Content $expectedFile -Raw -Encoding UTF8) -replace "`r", "").Trim()

    if ($actual -eq $expected) {
        $script:passed++
        $results += [pscustomobject]@{ Name = $test.BaseName; Status = "PASS"; Note = "" }
        Write-Host "  PASS  $($test.Name)" -ForegroundColor Green
    } else {
        $script:failed++
        $results += [pscustomobject]@{ Name = $test.BaseName; Status = "FAIL"; Note = "output mismatch" }
        Write-Host "  FAIL  $($test.Name)" -ForegroundColor Red
        Write-Host "    --- expected ---"      -ForegroundColor DarkGray
        Write-Host ($expected -split "`n" | ForEach-Object { "    | $_" })
        Write-Host "    --- actual ---"        -ForegroundColor DarkGray
        Write-Host ($actual -split "`n" | ForEach-Object { "    | $_" })
    }
}

Write-Host ("-" * 60)
Write-Host "Results: $passed passed, $failed failed" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Red" })

exit $(if ($failed -eq 0) { 0 } else { 1 })
