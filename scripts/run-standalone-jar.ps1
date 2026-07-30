# Teste rapido do JAR standalone (sem jpackage). Requer Java 21 instalado.
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$Frontend = Join-Path $Root "sge-frontend"
$Backend = Join-Path $Root "sge-backend"
$JarPath = Join-Path $Backend "target\sge-backend-0.0.1-SNAPSHOT.jar"

if (-not (Test-Path $JarPath)) {
    Write-Host "JAR nao encontrado. Execute primeiro: .\scripts\build-portable.ps1"
    exit 1
}

Push-Location $Backend
java -Dspring.profiles.active=standalone -jar $JarPath
Pop-Location
