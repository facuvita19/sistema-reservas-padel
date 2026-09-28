param([Parameter(Mandatory = $true)][string]$Archivo)
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$backup = (Resolve-Path $Archivo).Path
if ((Get-Item $backup).Length -lt 1000) { throw "El backup parece vacio o incompleto." }
if (-not (Test-Path ".env")) { throw "Falta el archivo .env." }

$values = @{}
Get-Content ".env" | Where-Object { $_ -match "^[^#].+=" } | ForEach-Object {
    $parts = $_ -split "=", 2
    $values[$parts[0].Trim()] = $parts[1].Trim()
}
$database = $values["MYSQL_DATABASE"]
$password = $values["MYSQL_ROOT_PASSWORD"]
if ([string]::IsNullOrWhiteSpace($database) -or [string]::IsNullOrWhiteSpace($password)) {
    throw "Faltan MYSQL_DATABASE o MYSQL_ROOT_PASSWORD en .env."
}

Write-Host "ADVERTENCIA: esta operacion reemplazara la base Docker '$database'." -ForegroundColor Yellow
Write-Host "No afecta al MySQL habitual fuera de Docker." -ForegroundColor Yellow
if ((Read-Host "Escribi RESTAURAR para continuar") -cne "RESTAURAR") {
    Write-Host "Operacion cancelada."
    exit 0
}

$containerFile = "/tmp/restauracion.sql"
Write-Host "Deteniendo temporalmente API y web..." -ForegroundColor Cyan
docker compose stop api web | Out-Null

try {
    docker cp $backup "padel-mysql:$containerFile"
    if ($LASTEXITCODE -ne 0) { throw "No se pudo copiar el backup al contenedor." }

    $prepare = "DROP DATABASE IF EXISTS ``$database``; CREATE DATABASE ``$database`` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    docker exec -e "MYSQL_PWD=$password" padel-mysql mysql -uroot -e $prepare
    if ($LASTEXITCODE -ne 0) { throw "No se pudo recrear la base Docker." }

    docker exec -e "MYSQL_PWD=$password" padel-mysql sh -c `
        "mysql -uroot --default-character-set=utf8mb4 '$database' < '$containerFile'"
    if ($LASTEXITCODE -ne 0) { throw "No se pudo importar el backup." }

    Write-Host "Backup restaurado correctamente." -ForegroundColor Green
}
finally {
    docker exec padel-mysql rm -f $containerFile 2>$null | Out-Null
    Write-Host "Iniciando API y web..." -ForegroundColor Cyan
    docker compose start api web | Out-Null
}
