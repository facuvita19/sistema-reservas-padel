$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root
if (-not (Test-Path ".env")) {
    throw "Falta .env. Copia .env.example como .env y cambia las contrasenas."
}
& ".\preparar-docker-init.ps1"
docker compose up --build -d
if ($LASTEXITCODE -ne 0) { throw "No se pudo iniciar la simulacion." }
Write-Host "Entorno iniciado:" -ForegroundColor Green
Write-Host "Web: http://localhost:5173"
Write-Host "API: http://localhost:8080/api/publica/estado"
Write-Host "Logs: docker compose logs -f"
