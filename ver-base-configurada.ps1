$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$externalPath = Join-Path $root "database.properties"
$resourcePath = Join-Path $root "src\main\resources\database.properties"

$path = if (Test-Path $externalPath) { $externalPath } else { $resourcePath }
if (-not (Test-Path $path)) {
    throw "No se encontro database.properties."
}

$url = Get-Content $path | Where-Object { $_ -like "db.url=*" } | Select-Object -First 1
$user = Get-Content $path | Where-Object { $_ -like "db.user=*" } | Select-Object -First 1

Write-Host "Configuracion activa:" -ForegroundColor Cyan
Write-Host "Archivo: $path"
Write-Host $url
Write-Host $user

if ($url -match ":3307/") {
    Write-Host "Destino: MySQL Docker" -ForegroundColor Green
} elseif ($url -match ":3306/") {
    Write-Host "Destino: MySQL local habitual" -ForegroundColor Yellow
} else {
    Write-Host "Destino: configuracion personalizada" -ForegroundColor Magenta
}
