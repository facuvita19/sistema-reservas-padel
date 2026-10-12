param([string]$ArchivoEstado=".estado-salud.json")
$ErrorActionPreference="Stop";$root=Split-Path -Parent $MyInvocation.MyCommand.Path;Set-Location $root;$checks=[ordered]@{};$fallos=[Collections.Generic.List[string]]::new()
foreach($s in @('mysql','api','web')){$id=(& docker compose ps -q $s).Trim();$state=if($id){(& docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' $id 2>$null).Trim()}else{'ausente'};$checks[$s]=$state;if($state-ne'healthy'){$fallos.Add("$s=$state")}}
try{$r=Invoke-WebRequest 'http://127.0.0.1:4321/' -UseBasicParsing -TimeoutSec 10;$checks['webHttp']=$r.StatusCode;if($r.StatusCode-ne200){$fallos.Add('webHttp')}}catch{$checks['webHttp']='error';$fallos.Add('webHttp')}
try{$a=Invoke-RestMethod 'http://127.0.0.1:4321/api/publica/estado' -TimeoutSec 10;$checks['apiProxy']='ok'}catch{$checks['apiProxy']='error';$fallos.Add('apiProxy')}
$estado=if($fallos.Count){'DEGRADADO'}else{'SALUDABLE'};$obj=[ordered]@{fecha=(Get-Date).ToString('o');estado=$estado;checks=$checks};$obj|ConvertTo-Json -Depth 4|Set-Content $ArchivoEstado -Encoding UTF8
Write-Host "Estado general: $estado" -ForegroundColor $(if($estado-eq'SALUDABLE'){'Green'}else{'Red'});foreach($k in $checks.Keys){Write-Host ("{0,-12} {1}" -f $k,$checks[$k])};Write-Host "Informe local: $ArchivoEstado";Write-Host "No se leyo .env ni se imprimieron secretos." -ForegroundColor DarkGray
if($fallos.Count){exit 1}
