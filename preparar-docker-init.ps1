$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$source = Join-Path $root "database"
$target = Join-Path $source "docker-init"

if (-not (Test-Path $source)) {
    throw "No se encontro la carpeta database."
}

New-Item -ItemType Directory -Force -Path $target | Out-Null
Get-ChildItem $target -Filter "*.sql" -File -ErrorAction SilentlyContinue |
    Remove-Item -Force

$files = Get-ChildItem $source -Filter "*.sql" -File
if (-not $files) {
    throw "No se encontraron archivos SQL en la carpeta database."
}

$schemaPrincipal = $null
foreach ($file in $files) {
    $contenido = Get-Content $file.FullName -Raw
    if ($contenido -match '(?is)CREATE\s+TABLE\s+(IF\s+NOT\s+EXISTS\s+)?clientes' -and
        $contenido -match '(?is)CREATE\s+TABLE\s+(IF\s+NOT\s+EXISTS\s+)?reservas' -and
        $contenido -match '(?is)CREATE\s+TABLE\s+(IF\s+NOT\s+EXISTS\s+)?canchas') {
        $schemaPrincipal = $file
        break
    }
}

if ($null -eq $schemaPrincipal) {
    throw "No se encontro el esquema principal que crea clientes, canchas y reservas."
}

Copy-Item $schemaPrincipal.FullName `
    (Join-Path $target "000_esquema_principal.sql")

$migraciones = $files |
    Where-Object { $_.FullName -ne $schemaPrincipal.FullName } |
    Sort-Object Name

$indice = 1
foreach ($file in $migraciones) {
    $nombre = "{0:D3}_{1}" -f $indice, $file.Name
    Copy-Item $file.FullName (Join-Path $target $nombre)
    $indice++
}

Write-Host "Esquema principal: $($schemaPrincipal.Name)" -ForegroundColor Cyan
Write-Host "Orden de inicializacion Docker:" -ForegroundColor Green
Get-ChildItem $target -Filter "*.sql" -File |
    Sort-Object Name |
    ForEach-Object { Write-Host "  $($_.Name)" }
