# Gera pasta portatil com SGE.exe (Java embutido + interface grafica de controle).
# Requisitos nesta maquina: JDK 21, Node.js 20+, Maven, npm.
param(
    [string]$OutputDir = "dist-portable",
    [string]$AppVersion = "0.0.1",
    [string]$SignPfxPath = $env:SGE_SIGN_PFX,
    [string]$SignPfxPassword = $env:SGE_SIGN_PFX_PASSWORD
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$Frontend = Join-Path $Root "sge-frontend"
$Backend = Join-Path $Root "sge-backend"
$Launcher = Join-Path $Root "sge-launcher"
$BackendJarName = "sge-backend-0.0.1-SNAPSHOT.jar"
$LauncherJarName = "sge-launcher-0.0.1.jar"
$BackendJarPath = Join-Path $Backend "target\$BackendJarName"
$LauncherJarPath = Join-Path $Launcher "target\$LauncherJarName"

function Require-Command($name) {
    if (-not (Get-Command $name -ErrorAction SilentlyContinue)) {
        throw "Comando obrigatorio nao encontrado: $name"
    }
}

$MavenCmd = "mvn"
$BundledMaven = Join-Path $Backend ".tools\apache-maven-3.9.9\bin\mvn.cmd"
if (-not (Get-Command mvn -ErrorAction SilentlyContinue) -and (Test-Path $BundledMaven)) {
    $MavenCmd = $BundledMaven
}

Write-Host "==> Verificando ferramentas de build..."
Require-Command node
Require-Command npm
Require-Command jpackage
if (-not (Get-Command $MavenCmd -ErrorAction SilentlyContinue) -and -not (Test-Path $MavenCmd)) {
    throw "Maven nao encontrado. Instale Maven ou use o projeto com .tools/apache-maven."
}

if (-not $env:JAVA_HOME) {
    throw "Defina JAVA_HOME apontando para o JDK 21 (jpackage precisa do JDK completo)."
}

Write-Host "==> Build do frontend (API na mesma origem)..."
Push-Location $Frontend
if (-not (Test-Path "node_modules")) {
    npm install
}
$env:VITE_API_BASE_URL = ""
npm run build
Pop-Location

if (-not (Test-Path (Join-Path $Frontend "dist\index.html"))) {
    throw "Frontend nao gerou dist/index.html"
}

Write-Host "==> Build do backend (perfil standalone, UI embutida)..."
Push-Location $Backend
& $MavenCmd -B -DskipTests -Pstandalone clean package
Pop-Location

Write-Host "==> Build do launcher desktop..."
Push-Location $Launcher
& $MavenCmd -B -DskipTests package
Pop-Location

if (-not (Test-Path $BackendJarPath)) {
    throw "JAR do backend nao encontrado: $BackendJarPath"
}
if (-not (Test-Path $LauncherJarPath)) {
    throw "JAR do launcher nao encontrado: $LauncherJarPath"
}

$JpackageInput = Join-Path $Root "build\jpackage-input"
if (Test-Path $JpackageInput) {
    Remove-Item $JpackageInput -Recurse -Force
}
New-Item -ItemType Directory -Path $JpackageInput | Out-Null
Copy-Item $BackendJarPath (Join-Path $JpackageInput $BackendJarName)
Copy-Item $LauncherJarPath (Join-Path $JpackageInput $LauncherJarName)

$OutRoot = Join-Path $Root $OutputDir
if (Test-Path $OutRoot) {
    Remove-Item $OutRoot -Recurse -Force
}
New-Item -ItemType Directory -Path $OutRoot | Out-Null

Write-Host "==> Empacotando com jpackage (interface grafica + Java embutido)..."
$AppImageDir = Join-Path $OutRoot "SGE"
jpackage `
    --input $JpackageInput `
    --dest $OutRoot `
    --name SGE `
    --app-version $AppVersion `
    --main-jar $LauncherJarName `
    --main-class br.com.sge.launcher.SgeDesktopLauncher `
    --type app-image `
    --java-options "-Djava.awt.headless=false" `
    --java-options "-Dsun.java2d.d3d=false"

$ExePath = Join-Path $AppImageDir "SGE.exe"
$RuntimeBin = Join-Path $AppImageDir "runtime\bin"
$JavaExe = Join-Path $RuntimeBin "java.exe"
$JavawExe = Join-Path $RuntimeBin "javaw.exe"
if (-not (Test-Path $ExePath)) {
    throw "Executavel nao gerado: $ExePath"
}

Write-Host "==> Copiando java.exe e javaw.exe para o runtime (necessario para o launcher)..."
$JdkBin = Join-Path $env:JAVA_HOME "bin"
if (-not (Test-Path (Join-Path $JdkBin "java.exe"))) {
    throw "java.exe nao encontrado em JAVA_HOME: $env:JAVA_HOME"
}
Copy-Item (Join-Path $JdkBin "java.exe") $RuntimeBin -Force
Copy-Item (Join-Path $JdkBin "javaw.exe") $RuntimeBin -Force
if (-not (Test-Path $JavaExe)) {
    throw "Falha ao copiar java.exe para o runtime."
}

Write-Host "==> Criando atalhos alternativos (mais estaveis que SGE.exe no Windows)..."
$BatPath = Join-Path $AppImageDir "Iniciar SGE.bat"
$batLines = @(
    '@echo off',
    'cd /d "%~dp0"',
    "start `"`" `"%~dp0runtime\bin\javaw.exe`" -jar `"%~dp0app\$LauncherJarName`""
)
Set-Content -Path $BatPath -Value $batLines -Encoding ASCII

$VbsPath = Join-Path $AppImageDir "Iniciar SGE.vbs"
$vbsLines = @(
    'Set shell = CreateObject("WScript.Shell")',
    'base = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName)',
    'shell.CurrentDirectory = base',
    "shell.Run `"`"`" & base & `"\runtime\bin\javaw.exe`"`" -jar `"`"`" & base & `"\app\$LauncherJarName`"`"`", 0, False"
)
Set-Content -Path $VbsPath -Value $vbsLines -Encoding ASCII

if ($SignPfxPath -and (Test-Path $SignPfxPath)) {
    Write-Host "==> Assinando SGE.exe com certificado de code signing..."
    $SignScript = Join-Path $PSScriptRoot "sign-portable.ps1"
    & $SignScript -ExePath $ExePath -PfxPath $SignPfxPath -PfxPassword $SignPfxPassword
} elseif ($SignPfxPath) {
    Write-Host "AVISO: certificado nao encontrado em $SignPfxPath - SGE.exe ficara sem assinatura."
}

Write-Host ""
Write-Host "Pronto! Distribua a pasta:"
Write-Host "  $AppImageDir"
Write-Host ""
Write-Host "Na outra maquina (recomendado: Iniciar SGE.vbs ou Iniciar SGE.bat):"
Write-Host "  1. Execute Iniciar SGE.vbs (ou SGE.exe)"
Write-Host "  2. Clique em 'Iniciar app' na janela de controle"
Write-Host "  3. Use 'Finalizar app' para encerrar o servidor"
Write-Host ""
Write-Host "Dados persistem em .\data\ dentro da pasta do app."
Write-Host "Se veio de uma versao antiga (CEM), apague a pasta .\data\ antes de iniciar ou reinicie o app uma vez (normalizacao automatica)."
Write-Host ""
Write-Host "Contas de teste (senha admin123 para todas):"
Write-Host "  admin@sge.com  pai@sge.com  prof@sge.com  prof.carla@sge.com  prof.paulo@sge.com"
Write-Host "  nutri@sge.com  psico@sge.com  coord@sge.com  diretor@sge.com  aluno@sge.com"
