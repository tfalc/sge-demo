# Testa o launcher desktop sem gerar o .exe (requer Java 21 e JARs ja compilados).
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$BackendJar = Join-Path $Root "sge-backend\target\sge-backend-0.0.1-SNAPSHOT.jar"
$LauncherJar = Join-Path $Root "sge-launcher\target\sge-launcher-0.0.1.jar"
$MavenCmd = Join-Path $Root "sge-backend\.tools\apache-maven-3.9.9\bin\mvn.cmd"

if (-not (Test-Path $BackendJar)) {
    Push-Location (Join-Path $Root "sge-backend")
    & $MavenCmd -B -DskipTests -Pstandalone package
    Pop-Location
}

if (-not (Test-Path $LauncherJar)) {
    Push-Location (Join-Path $Root "sge-launcher")
    & $MavenCmd -B -DskipTests package
    Pop-Location
}

Push-Location $Root
java -jar $LauncherJar
Pop-Location
