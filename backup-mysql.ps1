$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

if (-not (Test-Path ".env")) { throw "Falta el archivo .env." }
$values = @{}
Get-Content ".env" | Where-Object { $_ -match "^[^#].+=" } | ForEach-Object {
    $parts = $_ -split "=", 2
    $values[$parts[0].Trim()] = $parts[1].Trim()
}
foreach ($key in @("MYSQL_DATABASE", "MYSQL_ROOT_PASSWORD")) {
    if (-not $values.ContainsKey($key) -or [string]::IsNullOrWhiteSpace($values[$key])) {
        throw "Falta $key en .env."
    }
}

$health = docker inspect -f "{{.State.Health.Status}}" padel-mysql 2>$null
if ($LASTEXITCODE -ne 0 -or $health.Trim() -ne "healthy") {
    throw "El contenedor padel-mysql no esta disponible o no esta healthy."
}

$backupDir = Join-Path $root "backups"
New-Item -ItemType Directory -Force -Path $backupDir | Out-Null
$stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$file = Join-Path $backupDir "padel_reservas_$stamp.sql"
$containerFile = "/tmp/padel_reservas_$stamp.sql"
$password = $values["MYSQL_ROOT_PASSWORD"]
$database = $values["MYSQL_DATABASE"]

try {
    docker exec -e "MYSQL_PWD=$password" padel-mysql sh -c `
        "mysqldump -uroot --single-transaction --routines --triggers --events --hex-blob --default-character-set=utf8mb4 --set-gtid-purged=OFF '$database' > '$containerFile'"
    if ($LASTEXITCODE -ne 0) { throw "No se pudo generar el backup dentro del contenedor." }

    docker cp "padel-mysql:$containerFile" $file
    if ($LASTEXITCODE -ne 0) { throw "No se pudo copiar el backup a Windows." }
}
finally {
    docker exec padel-mysql rm -f $containerFile 2>$null | Out-Null
}

if (-not (Test-Path $file) -or (Get-Item $file).Length -lt 1000) {
    Remove-Item $file -ErrorAction SilentlyContinue
    throw "No se pudo crear un backup valido."
}

Write-Host "Backup creado correctamente:" -ForegroundColor Green
Write-Host $file
Write-Host "Tamano: $([math]::Round((Get-Item $file).Length / 1KB, 2)) KB"
