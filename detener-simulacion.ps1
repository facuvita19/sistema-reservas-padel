$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root
docker compose down
Write-Host "Entorno detenido. Los datos de MySQL se conservaron." -ForegroundColor Green
