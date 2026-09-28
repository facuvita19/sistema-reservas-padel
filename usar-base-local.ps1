$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$externalPath = Join-Path $root "database.properties"
$backupPath = Join-Path $root "database.local.properties"

if (-not (Test-Path $backupPath)) {
    throw "No se encontro database.local.properties. No se puede restaurar la configuracion local de forma segura."
}

Copy-Item $backupPath $externalPath -Force

Write-Host "Aplicacion administrativa configurada para MySQL local." -ForegroundColor Green
Write-Host "Archivo activo: $externalPath"
Write-Host "Puerto: 3306"
Write-Host "Cerra y volve a iniciar la aplicacion administrativa."
