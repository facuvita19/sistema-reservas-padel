param([ValidateSet("local","produccion")][string]$Modo="local",[switch]$OmitirPull)
$ErrorActionPreference="Stop";$root=Split-Path -Parent $MyInvocation.MyCommand.Path;Set-Location $root
if((git status --porcelain)){throw "El repositorio tiene cambios sin confirmar. El despliegue fue cancelado."}
& ".\validar-configuracion.ps1" -Archivo ".env" -Modo $Modo
$commit=(git rev-parse HEAD).Trim();$anterior=(git rev-parse HEAD^ 2>$null).Trim()
Write-Host "Creando backup obligatorio previo al despliegue..." -ForegroundColor Cyan
& ".\backup-mysql.ps1";if($LASTEXITCODE-ne0){throw "El backup previo fallo."}
$ultimo=Get-ChildItem ".\backups\padel_reservas_*.sql"|Sort-Object LastWriteTime -Descending|Select-Object -First 1;if($null-eq$ultimo-or-not(Test-Path ($ultimo.FullName+".sha256"))){throw "No se encontro un backup verificado reciente."}
if(-not$OmitirPull){git pull --ff-only;if($LASTEXITCODE-ne0){throw "git pull --ff-only fallo."};$commit=(git rev-parse HEAD).Trim()}
& ".\preparar-docker-init.ps1"
Write-Host "Construyendo y desplegando servicios..." -ForegroundColor Cyan
docker compose up --build --detach --remove-orphans;if($LASTEXITCODE-ne0){throw "Docker Compose no pudo completar el despliegue."}
& ".\validar-despliegue.ps1"
$registro=[ordered]@{fecha=(Get-Date).ToString("o");commit=$commit;commitAnterior=$anterior;backup=$ultimo.Name;modo=$Modo}
$registro|ConvertTo-Json|Set-Content ".ultimo-despliegue.json" -Encoding UTF8
Write-Host "Despliegue seguro completado." -ForegroundColor Green
Write-Host "- Commit desplegado: $($commit.Substring(0,7))"
Write-Host "- Backup previo: registrado."
Write-Host "- Punto de rollback: registrado localmente."
Write-Host "- No se imprimieron secretos."
