param([ValidateRange(30,600)][int]$TimeoutSegundos=180)
$ErrorActionPreference="Stop";$root=Split-Path -Parent $MyInvocation.MyCommand.Path;Set-Location $root
$deadline=(Get-Date).AddSeconds($TimeoutSegundos)
$servicios=@("mysql","api","web")
foreach($servicio in $servicios){do{$id=(& docker compose ps -q $servicio).Trim();$estado=if($id){(& docker inspect -f "{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}" $id 2>$null).Trim()}else{"ausente"};if($estado-eq"healthy"){break};if($estado-in@("unhealthy","exited","dead","ausente")){throw "$servicio termino con estado $estado."};Start-Sleep 3}while((Get-Date)-lt$deadline);if($estado-ne"healthy"){throw "Tiempo agotado esperando $servicio."};Write-Host ("{0,-8} healthy" -f $servicio) -ForegroundColor Green}
$api=Invoke-RestMethod -Uri "http://127.0.0.1:4321/api/publica/estado" -TimeoutSec 15;if($null-eq$api){throw "La API no respondio mediante Nginx."}
$web=Invoke-WebRequest -Uri "http://127.0.0.1:4321/" -UseBasicParsing -TimeoutSec 15;if($web.StatusCode-ne200-or$web.Content.Length-lt500){throw "La web no respondio correctamente."}
$csp=$web.Headers["Content-Security-Policy"];if([string]::IsNullOrWhiteSpace($csp)){throw "Falta Content-Security-Policy."}
Write-Host "Despliegue validado correctamente." -ForegroundColor Green
Write-Host "- MySQL, API y web: healthy."
Write-Host "- API mediante Nginx: correcta."
Write-Host "- Web y cabeceras de seguridad: correctas."
Write-Host "- No se imprimieron variables ni secretos."
