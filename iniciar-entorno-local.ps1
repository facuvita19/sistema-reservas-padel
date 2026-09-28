$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$webRoot = Join-Path $projectRoot "web-publica"

if (-not (Test-Path (Join-Path $projectRoot "iniciar-api.ps1"))) {
    throw "No se encontro iniciar-api.ps1 en la raiz del proyecto."
}
if (-not (Test-Path (Join-Path $webRoot "iniciar-web.ps1"))) {
    throw "No se encontro web-publica\iniciar-web.ps1."
}

Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-ExecutionPolicy", "Bypass",
    "-File", (Join-Path $projectRoot "iniciar-api.ps1")
)

Start-Sleep -Seconds 4

Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-ExecutionPolicy", "Bypass",
    "-File", (Join-Path $webRoot "iniciar-web.ps1")
)

Write-Host "Se abrieron dos ventanas:" -ForegroundColor Green
Write-Host "1. API publica independiente"
Write-Host "2. Web publica local"
Write-Host "La aplicacion administrativa puede permanecer cerrada."
