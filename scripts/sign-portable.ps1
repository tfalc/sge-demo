# Assina digitalmente o SGE.exe (remove/avalia o aviso do SmartScreen apos reputacao).
# Requer certificado de Code Signing (.pfx) e Windows SDK (signtool).
param(
    [Parameter(Mandatory = $true)]
    [string]$ExePath,
    [string]$PfxPath = $env:SGE_SIGN_PFX,
    [string]$PfxPassword = $env:SGE_SIGN_PFX_PASSWORD,
    [string]$TimestampUrl = "http://timestamp.digicert.com"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $ExePath)) {
    throw "Executavel nao encontrado: $ExePath"
}
if (-not $PfxPath -or -not (Test-Path $PfxPath)) {
    throw "Informe o certificado .pfx em -PfxPath ou na variavel SGE_SIGN_PFX"
}
if (-not $PfxPassword) {
    throw "Informe a senha do .pfx em -PfxPassword ou na variavel SGE_SIGN_PFX_PASSWORD"
}

function Find-SignTool {
    $kitsRoot = Join-Path ${env:ProgramFiles(x86)} "Windows Kits\10\bin"
    if (Test-Path $kitsRoot) {
        $versionDir = Get-ChildItem $kitsRoot -Directory |
            Where-Object { $_.Name -match '^\d' } |
            Sort-Object { [version]$_.Name } -Descending |
            Select-Object -First 1
        if ($versionDir) {
            $candidate = Join-Path $versionDir.FullName "x64\signtool.exe"
            if (Test-Path $candidate) {
                return $candidate
            }
        }
    }

    $cmd = Get-Command signtool -ErrorAction SilentlyContinue
    if ($cmd) {
        return $cmd.Source
    }

    return $null
}

$SignTool = Find-SignTool
if (-not $SignTool) {
    throw "signtool.exe nao encontrado. Instale o Windows SDK (Windows 10/11 SDK)."
}

Write-Host "==> Assinando $ExePath ..."
& $SignTool sign `
    /fd SHA256 `
    /tr $TimestampUrl `
    /td SHA256 `
    /f $PfxPath `
    /p $PfxPassword `
    $ExePath

Write-Host "==> Verificando assinatura ..."
& $SignTool verify /pa /v $ExePath
Write-Host "Assinatura aplicada com sucesso."
