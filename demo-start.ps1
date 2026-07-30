# Sobe stack completa para demo (Postgres + API + frontend)
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "Subindo demo SGE (profile demo)..." -ForegroundColor Cyan
docker compose --profile demo up -d --build

Write-Host "Aguardando health do backend..." -ForegroundColor Yellow
$deadline = (Get-Date).AddMinutes(3)
while ((Get-Date) -lt $deadline) {
    try {
        $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 3
        if ($health.status -eq "UP") {
            Write-Host ""
            Write-Host "Demo pronta!" -ForegroundColor Green
            Write-Host "  Frontend:  http://localhost:5173"
            Write-Host "  API:       http://localhost:8080"
            Write-Host "  Health:    http://localhost:8080/actuator/health"
            Write-Host "  Roteiro:   DEMO.md"
            Write-Host "  Login pai: pai@sge.com / admin123"
            exit 0
        }
    } catch {
        Start-Sleep -Seconds 3
    }
}

Write-Host "Backend ainda nao respondeu. Verifique: docker compose --profile demo logs backend" -ForegroundColor Red
exit 1
