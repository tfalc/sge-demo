# Para stack demo e Postgres
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "Parando demo SGE..." -ForegroundColor Cyan
docker compose --profile demo down
Write-Host "Concluido." -ForegroundColor Green
