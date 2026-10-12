param([switch]$RestaurarBase)
$ErrorActionPreference="Stop";$root=Split-Path -Parent $MyInvocation.MyCommand.Path;Set-Location $root
if(-not(Test-Path ".ultimo-despliegue.json")){throw "No existe un registro local de despliegue."}
$r=Get-Content ".ultimo-despliegue.json" -Raw -Encoding UTF8|ConvertFrom-Json
if([string]::IsNullOrWhiteSpace($r.commitAnterior)){throw "No existe commit anterior registrado."}
Write-Host "Rollback preparado:" -ForegroundColor Yellow
Write-Host "- Se restaurara el codigo al commit $($r.commitAnterior.Substring(0,7))."
Write-Host "- La base solo se restaurara si se usa -RestaurarBase."
if((Read-Host "Escribi ROLLBACK para continuar")-cne"ROLLBACK"){Write-Host "Operacion cancelada.";exit 0}
git checkout $r.commitAnterior;if($LASTEXITCODE-ne0){throw "No se pudo cambiar al commit anterior."}
docker compose up --build --detach --remove-orphans;if($LASTEXITCODE-ne0){throw "No se pudo reconstruir la version anterior."}
if($RestaurarBase){$archivo=Join-Path $root ("backups\"+$r.backup);if(-not(Test-Path $archivo)){throw "No se encontro el backup previo registrado."};& ".\restaurar-mysql.ps1" -Archivo $archivo}
& ".\validar-despliegue.ps1"
Write-Host "Rollback completado. El repositorio quedo en detached HEAD; crea una rama o vuelve a master cuando termines." -ForegroundColor Green
