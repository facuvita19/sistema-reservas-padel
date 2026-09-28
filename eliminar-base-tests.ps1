$ErrorActionPreference = "Stop"

$project = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $project

$envPath = Join-Path $project ".env"
if (-not (Test-Path $envPath)) {
    throw "Falta .env en la raiz del proyecto: $project"
}

$values = @{}
Get-Content $envPath | Where-Object {
    $_ -match "^[^#].+="
} | ForEach-Object {
    $parts = $_ -split "=", 2
    $values[$parts[0].Trim()] = $parts[1].Trim()
}

if (-not $values.ContainsKey("MYSQL_ROOT_PASSWORD") -or
        [string]::IsNullOrWhiteSpace($values["MYSQL_ROOT_PASSWORD"])) {
    throw "Falta MYSQL_ROOT_PASSWORD en .env."
}

$rootPassword = $values["MYSQL_ROOT_PASSWORD"]
$testDatabase = "padel_reservas_test"

$health = docker inspect `
    -f "{{.State.Health.Status}}" `
    padel-mysql 2>$null

if ($LASTEXITCODE -ne 0 -or $health.Trim() -ne "healthy") {
    throw "padel-mysql debe estar iniciado y healthy."
}

$dropSql = "DROP DATABASE IF EXISTS ``$testDatabase``;"
$dockerArgs = @(
    "exec",
    "-e", "MYSQL_PWD=$rootPassword",
    "padel-mysql",
    "mysql",
    "-uroot",
    "-e", $dropSql
)

& docker @dockerArgs
if ($LASTEXITCODE -ne 0) {
    throw "No se pudo eliminar la base de tests."
}

Write-Host "Base $testDatabase eliminada correctamente." `
    -ForegroundColor Green
