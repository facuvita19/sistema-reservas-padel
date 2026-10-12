param(
    [switch]$OmitirBuild,
    [switch]$ConservarAlFallar
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$project = "padel-ci-" + ([guid]::NewGuid().ToString("N").Substring(0, 10))
$temp = Join-Path ([IO.Path]::GetTempPath()) $project
$envFile = Join-Path $temp ".env"
$override = Join-Path $temp "compose.override.yml"
$success = $false
New-Item -ItemType Directory -Force -Path $temp | Out-Null

function Invoke-Compose {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Argumentos)
    & docker compose -p $project --env-file $envFile -f (Join-Path $root "docker-compose.yml") -f $override @Argumentos
    if ($LASTEXITCODE -ne 0) { throw "Docker Compose devolvio codigo $LASTEXITCODE." }
}

function Wait-Healthy {
    param([Parameter(Mandatory)][string]$Servicio,[int]$TimeoutSeconds = 180)
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        $id = (& docker compose -p $project --env-file $envFile -f (Join-Path $root "docker-compose.yml") -f $override ps -q $Servicio).Trim()
        if ($id) {
            $status = (& docker inspect -f "{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}" $id 2>$null).Trim()
            if ($status -eq "healthy") { return }
            if ($status -eq "unhealthy" -or $status -eq "exited" -or $status -eq "dead") {
                Invoke-Compose logs --no-color --tail 150 $Servicio
                throw "$Servicio termino con estado $status."
            }
        }
        Start-Sleep -Seconds 3
    } while ((Get-Date) -lt $deadline)
    Invoke-Compose logs --no-color --tail 150 $Servicio
    throw "Tiempo agotado esperando que $Servicio quede healthy."
}

@"
MYSQL_DATABASE=padel_reservas
MYSQL_USER=padel_limpia
MYSQL_PASSWORD=PruebaApp_9x!Limpia
MYSQL_ROOT_PASSWORD=PruebaRoot_9x!Limpia
WEB_ORIGIN=http://127.0.0.1:14321
API_WEB_PUBLICA_URL=http://127.0.0.1:14321/
PUBLIC_SITE_URL=http://127.0.0.1:14321
SMTP_HOST=localhost
SMTP_PORT=2525
SMTP_USER=no-usar
SMTP_PASSWORD=no-usar
SMTP_FROM_EMAIL=no-usar@example.invalid
SMTP_FROM_NAME=Prueba CI
SMTP_STARTTLS=false
SMTP_SSL=false
SMTP_TIMEOUT_MS=1000
"@ | Set-Content -Path $envFile -Encoding utf8NoBOM

@"
services:
  mysql:
    container_name: $project-mysql
    ports: !reset []
    volumes:
      - mysql_limpia:/var/lib/mysql
      - ./database/docker-init:/docker-entrypoint-initdb.d:ro
  api:
    container_name: $project-api
    ports: !reset []
  web:
    container_name: $project-web
    ports: !override
      - "127.0.0.1:14321:80"
volumes:
  mysql_limpia:
    name: $project-mysql-data
"@ | Set-Content -Path $override -Encoding utf8NoBOM

try {
    Write-Host "Validando configuracion Compose aislada..." -ForegroundColor Cyan
    Invoke-Compose config --quiet

    Write-Host "Iniciando instalacion limpia temporal: $project" -ForegroundColor Cyan
    if ($OmitirBuild) {
        Invoke-Compose -Argumentos @("up", "--detach")
    } else {
        Invoke-Compose -Argumentos @("up", "--build", "--detach")
    }

    Wait-Healthy mysql
    Wait-Healthy api
    Wait-Healthy web

    $mysqlId = (& docker compose -p $project --env-file $envFile -f (Join-Path $root "docker-compose.yml") -f $override ps -q mysql).Trim()
    $sql = @"
SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='padel_reservas';
SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='padel_reservas' AND table_name='reservas' AND column_name='cliente_ocasional_nombre';
SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='padel_reservas' AND table_name='torneo_partido_enlaces';
SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='padel_reservas' AND table_name='canchas' AND column_name='orden_visual';
"@
    $result = $sql | docker exec -i -e MYSQL_PWD=PruebaRoot_9x!Limpia $mysqlId mysql -N -B -uroot padel_reservas
    if ($LASTEXITCODE -ne 0) { throw "No se pudo validar el esquema limpio." }
    $values = @($result | Where-Object { $_ -match '^\d+$' })
    if ($values.Count -lt 4 -or [int]$values[0] -lt 20 -or $values[1] -ne '1' -or $values[2] -ne '1' -or $values[3] -ne '1') {
        throw "Esquema incompleto. Resultado: $($values -join ', ')"
    }

    $api = Invoke-RestMethod -Uri "http://127.0.0.1:14321/api/publica/estado" -TimeoutSec 15
    if ($null -eq $api) { throw "La API no devolvio contenido mediante Nginx." }
    $respuestaWeb = Invoke-WebRequest -Uri "http://127.0.0.1:14321/" -UseBasicParsing -TimeoutSec 15
    if ($respuestaWeb.StatusCode -ne 200 -or $respuestaWeb.Content.Length -lt 500) { throw "La web limpia no respondio correctamente." }

    Write-Host "Instalacion Docker limpia validada correctamente." -ForegroundColor Green
    Write-Host "- Proyecto temporal: $project"
    Write-Host "- MySQL, API y web: healthy."
    Write-Host "- Esquema: $($values[0]) tablas."
    Write-Host "- Migraciones criticas 10, 24 y 26: verificadas."
    Write-Host "- API a traves de Nginx: correcta."
    Write-Host "- Web estatica: correcta."
    $success = $true
}
finally {
    if ($success -or -not $ConservarAlFallar) {
        Write-Host "Eliminando contenedores, red y volumen temporales..." -ForegroundColor Cyan
        & docker compose -p $project --env-file $envFile -f (Join-Path $root "docker-compose.yml") -f $override down -v --remove-orphans 2>$null | Out-Null
        Remove-Item $temp -Recurse -Force -ErrorAction SilentlyContinue
    } else {
        Write-Warning "Recursos conservados para diagnostico: $project"
        Write-Warning "Archivos temporales: $temp"
    }
}
