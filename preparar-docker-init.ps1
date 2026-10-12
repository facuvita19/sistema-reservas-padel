$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$source = Join-Path $root "database"
$target = Join-Path $source "docker-init"

if (-not (Test-Path $source)) { throw "No se encontro la carpeta database." }
$schema = Join-Path $source "schema.sql"
if (-not (Test-Path $schema)) { throw "No se encontro database\schema.sql." }

$migraciones = Get-ChildItem $source -Filter "*.sql" -File |
    Where-Object { $_.Name -ne "schema.sql" } |
    Sort-Object @{ Expression = {
        if ($_.BaseName -match '^(\d+)_') { [int]$Matches[1] } else { [int]::MaxValue }
    } }, Name
if (-not $migraciones) { throw "No se encontraron migraciones SQL." }

New-Item -ItemType Directory -Force -Path $target | Out-Null
Get-ChildItem $target -Filter "*.sql" -File -ErrorAction SilentlyContinue | Remove-Item -Force
Copy-Item $schema (Join-Path $target "000_esquema_principal.sql")

$indice = 1
foreach ($file in $migraciones) {
    $nombre = "{0:D3}_{1}" -f $indice, $file.Name
    Copy-Item $file.FullName (Join-Path $target $nombre)
    $indice++
}

$generados = Get-ChildItem $target -Filter "*.sql" -File | Sort-Object Name
$esperados = $migraciones.Count + 1
if ($generados.Count -ne $esperados) {
    throw "Docker init incompleto: se esperaban $esperados archivos y se generaron $($generados.Count)."
}
foreach ($file in $migraciones) {
    if (-not ($generados.Name | Where-Object { $_ -like "*_$($file.Name)" })) {
        throw "Falta copiar la migracion $($file.Name)."
    }
}

Write-Host "Docker init regenerado correctamente." -ForegroundColor Green
Write-Host "Esquema principal: schema.sql" -ForegroundColor Cyan
Write-Host "Migraciones incluidas: $($migraciones.Count)" -ForegroundColor Cyan
$generados | ForEach-Object { Write-Host "  $($_.Name)" }
