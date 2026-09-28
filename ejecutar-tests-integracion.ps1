$ErrorActionPreference = "Stop"

$project = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $project

$envPath = Join-Path $project ".env"
if (-not (Test-Path $envPath)) {
    throw "Falta .env en la raiz del proyecto."
}

$values = @{}
Get-Content $envPath | Where-Object {
    $_ -match "^[^#].+="
} | ForEach-Object {
    $parts = $_ -split "=", 2
    $values[$parts[0].Trim()] = $parts[1].Trim()
}

foreach ($key in @(
    "MYSQL_DATABASE",
    "MYSQL_USER",
    "MYSQL_PASSWORD",
    "MYSQL_ROOT_PASSWORD"
)) {
    if (-not $values.ContainsKey($key) -or
            [string]::IsNullOrWhiteSpace($values[$key])) {
        throw "Falta $key en .env."
    }
}

$sourceDatabase = $values["MYSQL_DATABASE"]
$testDatabase = "padel_reservas_test"
$rootPassword = $values["MYSQL_ROOT_PASSWORD"]
$appUser = $values["MYSQL_USER"]
$appPassword = $values["MYSQL_PASSWORD"]
$dumpFile = "/tmp/padel_test_source.sql"

if ($sourceDatabase -eq $testDatabase) {
    throw "La base principal no puede llamarse padel_reservas_test."
}

$health = docker inspect `
    -f "{{.State.Health.Status}}" `
    padel-mysql 2>$null

if ($LASTEXITCODE -ne 0 -or $health.Trim() -ne "healthy") {
    throw "padel-mysql debe estar iniciado y healthy."
}

Write-Host "Preparando base aislada $testDatabase..." `
    -ForegroundColor Cyan

try {
    $dumpCommand = "mysqldump -uroot " +
        "--single-transaction " +
        "--routines " +
        "--triggers " +
        "--events " +
        "--hex-blob " +
        "--default-character-set=utf8mb4 " +
        "--set-gtid-purged=OFF " +
        "'$sourceDatabase' > '$dumpFile'"

    $dumpArgs = @(
        "exec",
        "-e", "MYSQL_PWD=$rootPassword",
        "padel-mysql",
        "sh", "-c", $dumpCommand
    )

    & docker @dumpArgs
    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo copiar la base principal para testing."
    }

    $prepare = @(
        "DROP DATABASE IF EXISTS ``$testDatabase``;"
        "CREATE DATABASE ``$testDatabase`` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
        "GRANT ALL PRIVILEGES ON ``$testDatabase``.* TO '$appUser'@'%';"
        "FLUSH PRIVILEGES;"
    ) -join " "

    $prepareArgs = @(
        "exec",
        "-e", "MYSQL_PWD=$rootPassword",
        "padel-mysql",
        "mysql",
        "-uroot",
        "-e", $prepare
    )

    & docker @prepareArgs
    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo crear la base ni asignar permisos de prueba."
    }

    $importCommand = "mysql -uroot " +
        "--default-character-set=utf8mb4 " +
        "'$testDatabase' < '$dumpFile'"

    $importArgs = @(
        "exec",
        "-e", "MYSQL_PWD=$rootPassword",
        "padel-mysql",
        "sh", "-c", $importCommand
    )

    & docker @importArgs
    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo importar la base de pruebas."
    }

    $connectionArgs = @(
        "exec",
        "-e", "MYSQL_PWD=$appPassword",
        "padel-mysql",
        "mysql",
        "-u$appUser",
        "-D", $testDatabase,
        "-e", "SELECT DATABASE() AS base_activa;"
    )

    & docker @connectionArgs
    if ($LASTEXITCODE -ne 0) {
        throw "El usuario '$appUser' no puede acceder a la base de pruebas."
    }

    $env:DB_URL = "jdbc:mysql://127.0.0.1:3307/${testDatabase}?serverTimezone=America/Argentina/Buenos_Aires&useSSL=false&allowPublicKeyRetrieval=true"
    $env:DB_USER = $appUser
    $env:DB_PASSWORD = $appPassword

    Write-Host "Ejecutando tests de integracion..." `
        -ForegroundColor Cyan

    $mavenArgs = @(
        "-Dtest=integracion.ReservaDAOMySQLIntegracionTest",
        "test"
    )

    & mvn @mavenArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Los tests de integracion fallaron."
    }

    Write-Host "Tests de integracion completados correctamente." `
        -ForegroundColor Green
}
finally {
    docker exec padel-mysql rm -f $dumpFile 2>$null | Out-Null

    Remove-Item Env:DB_URL -ErrorAction SilentlyContinue
    Remove-Item Env:DB_USER -ErrorAction SilentlyContinue
    Remove-Item Env:DB_PASSWORD -ErrorAction SilentlyContinue
}
