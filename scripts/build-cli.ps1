# scripts/build-cli.ps1
# Build the BanglaLang CLI locally on Windows.
#
#   .\scripts\build-cli.ps1            -> backend\target\bangla-lang-cli.jar
#   .\scripts\build-cli.ps1 -Native    -> + dist\bangla-windows-x64.zip (jpackage .exe launcher)
#
# Requires a JDK 17+ on PATH (Maven optional; falls back to plain javac).

param(
    [switch]$Native
)

$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
$backend = Join-Path $root "backend"

function Build-WithMaven {
    Write-Host "Building with Maven..." -ForegroundColor Cyan
    Push-Location $backend
    mvn -B -Pcli clean package -DskipTests
    $ok = ($LASTEXITCODE -eq 0)
    Pop-Location
    return $ok
}

function Build-WithJavac {
    Write-Host "Maven not found - compiling with javac directly..." -ForegroundColor Yellow
    $classes = Join-Path $backend "target\cli-classes"
    if (Test-Path $classes) { Remove-Item -Recurse -Force $classes }
    New-Item -ItemType Directory -Force $classes | Out-Null

    $files = Get-ChildItem -Recurse -Filter *.java (Join-Path $backend "src\main\java") |
        Where-Object { $_.FullName -notmatch '\\api\\' -and $_.Name -ne 'Main.java' } |
        ForEach-Object { $_.FullName }

    javac -encoding UTF-8 -d $classes @files
    if ($LASTEXITCODE -ne 0) { throw "javac failed" }

    jar cfe (Join-Path $backend "target\bangla-lang-cli.jar") com.banglalang.cli.CliMain -C $classes .
    if ($LASTEXITCODE -ne 0) { throw "jar failed" }
}

if (-not (Build-WithMaven)) { throw "Maven build failed" }
if (-not (Test-Path (Join-Path $backend "target\bangla-lang-cli.jar"))) {
    if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) { Build-WithJavac }
}

$jar = Join-Path $backend "target\bangla-lang-cli.jar"
Write-Host "`nBuilt: $jar" -ForegroundColor Green

if ($Native) {
    Write-Host "Packaging native Windows launcher with jpackage..." -ForegroundColor Cyan
    $input = Join-Path $backend "target\jpackage-input"
    New-Item -ItemType Directory -Force $input | Out-Null
    Copy-Item $jar $input -Force

    jpackage --type app-image --name bangla --app-version "1.0.0" --vendor "BanglaLang" `
        --input $input --main-jar bangla-lang-cli.jar --dest (Join-Path $backend "target\dist")
    if ($LASTEXITCODE -ne 0) { throw "jpackage failed" }

    $dist = Join-Path $root "dist"
    New-Item -ItemType Directory -Force $dist | Out-Null
    Compress-Archive -Path (Join-Path $backend "target\dist\bangla") `
        -DestinationPath (Join-Path $dist "bangla-windows-x64.zip") -Force
    Write-Host "Packaged: $(Join-Path $dist 'bangla-windows-x64.zip')" -ForegroundColor Green
}

Write-Host "`nTry it:" -ForegroundColor White
Write-Host "  java -jar `"$jar`" run examples\hello.bangla"
