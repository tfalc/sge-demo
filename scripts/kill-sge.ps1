# Encerra processos SGE / backend travados (portas 8080-8089).
$ErrorActionPreference = "SilentlyContinue"

Write-Host "Encerrando SGE.exe..."
Get-Process -Name "SGE" | Stop-Process -Force

Write-Host "Encerrando backends Java do SGE..."
Get-CimInstance Win32_Process -Filter "Name='java.exe'" |
    Where-Object { $_.CommandLine -match 'sge-backend' } |
    ForEach-Object {
        Write-Host "  PID $($_.ProcessId)"
        Stop-Process -Id $_.ProcessId -Force
    }

foreach ($port in 8080..8089) {
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($conn) {
        Write-Host "Liberando porta $port (PID $($conn.OwningProcess))..."
        Stop-Process -Id $conn.OwningProcess -Force
    }
}

Write-Host "Pronto."
