# ============================================================
#  FitStore - Arranque completo para la demo
#  Verifica Docker, Redis, RabbitMQ, SQL Server y arranca el backend.
#  Uso:  doble clic en start.bat  o  powershell -ExecutionPolicy Bypass -File start.ps1
# ============================================================

$ErrorActionPreference = "Continue"
$root    = $PSScriptRoot
$backend = Join-Path $root "backend"

function Info($m) { Write-Host "  $m"        -ForegroundColor Cyan }
function Ok($m)   { Write-Host "  [OK] $m"   -ForegroundColor Green }
function Warn($m) { Write-Host "  [!]  $m"   -ForegroundColor Yellow }
function Fail($m) { Write-Host "  [X]  $m"   -ForegroundColor Red }

function Test-Port($port, $h = "localhost") {
    try {
        $c = New-Object System.Net.Sockets.TcpClient
        $c.Connect($h, $port); $c.Close(); return $true
    } catch { return $false }
}

Write-Host ""
Write-Host "==============================================" -ForegroundColor Magenta
Write-Host "  FitStore - Arranque para la exposicion"       -ForegroundColor Magenta
Write-Host "==============================================" -ForegroundColor Magenta
Write-Host ""

# -- 1) JDK 25 -----------------------------------------------
function Get-JavaMajor($javaExe) {
    try {
        $out = & $javaExe -version 2>&1 | Select-Object -First 1
        if ($out -match '"(\d+)') { return [int]$Matches[1] }
    } catch {}
    return 0
}

Info "1/5 Buscando JDK 25 (el backend esta compilado para Java 25)..."
$jdk = $null
if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin\java.exe"))) {
    if ((Get-JavaMajor (Join-Path $env:JAVA_HOME "bin\java.exe")) -eq 25) { $jdk = $env:JAVA_HOME }
}
if (-not $jdk) {
    $cands = @()
    $cands += Get-ChildItem "C:\Program Files\Java"             -Directory -Filter "jdk-25*" -ErrorAction SilentlyContinue
    $cands += Get-ChildItem "$env:USERPROFILE\.jdk"             -Directory -Filter "jdk-25*" -ErrorAction SilentlyContinue
    $cands += Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory -Filter "jdk-25*" -ErrorAction SilentlyContinue
    foreach ($c in $cands) {
        if (Test-Path (Join-Path $c.FullName "bin\java.exe")) { $jdk = $c.FullName; break }
    }
}
if ($jdk) {
    $env:JAVA_HOME = $jdk
    $env:PATH = (Join-Path $jdk "bin") + ";" + $env:PATH
    Ok "JDK 25: $jdk"
} else {
    Fail "No se encontro JDK 25. Instala JDK 25 o define JAVA_HOME apuntando a un JDK 25."
    exit 1
}

# -- 2) Docker Desktop ---------------------------------------
Info "2/5 Verificando Docker Desktop..."
$dockerOk = $false
& docker info *> $null
if ($LASTEXITCODE -eq 0) { $dockerOk = $true }

if (-not $dockerOk) {
    $dd = "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    if (Test-Path $dd) {
        Warn "Docker no esta corriendo. Iniciando Docker Desktop (puede tardar ~1 min)..."
        Start-Process $dd | Out-Null
        for ($i = 0; $i -lt 40; $i++) {
            Start-Sleep -Seconds 3
            & docker info *> $null
            if ($LASTEXITCODE -eq 0) { $dockerOk = $true; break }
        }
    } else {
        Fail "Docker Desktop no encontrado en $dd"
    }
}
if ($dockerOk) { Ok "Docker listo" } else { Warn "Docker no disponible: Redis/RabbitMQ no arrancaran" }

# -- 2) Redis + RabbitMQ -------------------------------------
if ($dockerOk) {
    Info "3/5 Levantando Redis y RabbitMQ (docker compose up -d)..."
    Push-Location $root
    & docker compose up -d *> $null
    if ($LASTEXITCODE -ne 0) { & docker-compose up -d *> $null }
    Pop-Location

    foreach ($p in @(@(6380, "Redis"), @(5672, "RabbitMQ"))) {
        $ok = $false
        for ($i = 0; $i -lt 20; $i++) {
            if (Test-Port $p[0]) { $ok = $true; break }
            Start-Sleep -Milliseconds 500
        }
        if ($ok) { Ok "$($p[1]) escuchando en $($p[0])" } else { Warn "$($p[1]) no responde en $($p[0])" }
    }
} else {
    Info "3/5 Omitido (Docker no disponible)"
}

# -- 3) SQL Server -------------------------------------------
Info "4/5 Verificando SQL Server (puerto 1433)..."
if (Test-Port 1433) {
    Ok "SQL Server escuchando en 1433"
} else {
    Fail "SQL Server NO responde en 1433."
    Warn "Inicia el servicio:  net start MSSQLSERVER   (o desde servicios de Windows)"
}

# -- 4) Backend ----------------------------------------------
Info "5/5 Arrancando backend Spring Boot (http://localhost:8080)..."
Warn "Deja esta ventana abierta mientras dura la demo. Ctrl+C para detener."
Write-Host ""

$mvnw = Join-Path $backend "mvnw.cmd"
if (-not (Test-Path $mvnw)) { $mvnw = Join-Path $backend "mvnw" }
& $mvnw -f (Join-Path $backend "pom.xml") spring-boot:run
