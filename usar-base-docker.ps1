$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$envPath = Join-Path $root ".env"
$externalPath = Join-Path $root "database.properties"
$backupPath = Join-Path $root "database.local.properties"

if (-not (Test-Path $envPath)) {
    throw "No se encontro .env en la raiz del proyecto."
}

if (-not (Test-Path $backupPath)) {
    $candidates = @()

    if (Test-Path $externalPath) {
        $candidates += Get-Item $externalPath
    }

    $candidates += Get-ChildItem $root -Recurse -Force -File `
        -Filter "database.properties" -ErrorAction SilentlyContinue |
        Where-Object {
            $_.FullName -notlike "*\target\*" -and
            $_.FullName -ne $externalPath
        }

    $localConfig = $candidates |
        Where-Object {
            $content = Get-Content $_.FullName -Raw
            $content -match 'db\.url=.*:3306/'
        } |
        Select-Object -First 1

    if ($null -eq $localConfig) {
        throw "No se encontro una configuracion local con puerto 3306. Ejecuta en PowerShell: Get-ChildItem . -Recurse -Force -Filter database.properties | Select-Object FullName"
    }

    Copy-Item $localConfig.FullName $backupPath -Force
    Write-Host "Configuracion local respaldada desde:" -ForegroundColor Cyan
    Write-Host $localConfig.FullName
    Write-Host "Copia segura:" -ForegroundColor Cyan
    Write-Host $backupPath
}

$values = @{}
Get-Content $envPath | Where-Object { $_ -match "^[^#].+=" } | ForEach-Object {
    $parts = $_ -split "=", 2
    $values[$parts[0].Trim()] = $parts[1].Trim()
}

foreach ($key in @("MYSQL_DATABASE", "MYSQL_USER", "MYSQL_PASSWORD")) {
    if (-not $values.ContainsKey($key) -or
            [string]::IsNullOrWhiteSpace($values[$key])) {
        throw "Falta $key en .env."
    }
}

$content = @"
db.url=jdbc:mysql://127.0.0.1:3307/$($values['MYSQL_DATABASE'])?serverTimezone=America/Argentina/Buenos_Aires&useSSL=false&allowPublicKeyRetrieval=true
db.user=$($values['MYSQL_USER'])
db.password=$($values['MYSQL_PASSWORD'])
"@

$utf8 = New-Object System.Text.UTF8Encoding($false)
[IO.File]::WriteAllText($externalPath, $content, $utf8)

Write-Host "Aplicacion administrativa configurada para MySQL Docker." -ForegroundColor Green
Write-Host "Archivo activo: $externalPath"
Write-Host "Puerto: 3307"
Write-Host "Cerra y volve a iniciar la aplicacion administrativa."
