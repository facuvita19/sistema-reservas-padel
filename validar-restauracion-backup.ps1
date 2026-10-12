param([Parameter(Mandatory=$true)][string]$Archivo,[switch]$ConservarAlFallar)
$ErrorActionPreference="Stop";$root=Split-Path -Parent $MyInvocation.MyCommand.Path;Set-Location $root
$backup=(Resolve-Path $Archivo).Path;if((Get-Item $backup).Length-lt 1000){throw "El backup parece vacio o incompleto."}
$hashFile="$backup.sha256";if(-not(Test-Path $hashFile)){throw "Falta el archivo de checksum asociado."}
$esperado=((Get-Content $hashFile -Encoding ASCII|Select-Object -First 1)-split '\s+')[0].ToLowerInvariant();$real=(Get-FileHash $backup -Algorithm SHA256).Hash.ToLowerInvariant();if($esperado-ne$real){throw "El checksum SHA-256 no coincide. El archivo puede estar alterado o incompleto."}
$nombre="padel-restore-"+([guid]::NewGuid().ToString('N').Substring(0,10));$container="$nombre-mysql";$volume="$nombre-data";$ok=$false
try{
 Write-Host "Iniciando MySQL temporal aislado para restauracion..." -ForegroundColor Cyan
 docker run -d --name $container -e MYSQL_ROOT_PASSWORD=TemporalRestore_9x! -e MYSQL_DATABASE=padel_reservas -v "${volume}:/var/lib/mysql" mysql:8.4|Out-Null
 if($LASTEXITCODE-ne 0){throw "No se pudo iniciar MySQL temporal."}
 $deadline=(Get-Date).AddMinutes(3);do{Start-Sleep 3;docker exec -e MYSQL_PWD=TemporalRestore_9x! $container mysql -uroot -Nse "SELECT 1" 2>$null|Out-Null;$ready=$LASTEXITCODE-eq 0}while(-not$ready-and(Get-Date)-lt$deadline);if(-not$ready){throw "MySQL temporal no completo la inicializacion autenticada."}
 docker cp $backup "${container}:/tmp/restauracion.sql";if($LASTEXITCODE-ne 0){throw "No se pudo copiar el backup al entorno temporal."}
 docker exec -e MYSQL_PWD=TemporalRestore_9x! $container sh -c "mysql -uroot --default-character-set=utf8mb4 padel_reservas < /tmp/restauracion.sql";if($LASTEXITCODE-ne 0){throw "La restauracion temporal fallo."}
 $sql="SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='padel_reservas'; SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='padel_reservas' AND table_name='reservas' AND column_name='cliente_ocasional_nombre'; SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='padel_reservas' AND table_name='torneo_partido_enlaces';"
 $r=@($sql|docker exec -i -e MYSQL_PWD=TemporalRestore_9x! $container mysql -N -B -uroot padel_reservas|Where-Object{$_-match'^\d+$'});if($LASTEXITCODE-ne 0-or$r.Count-lt 3-or[int]$r[0]-lt 20-or$r[1]-ne'1'-or$r[2]-ne'1'){throw "La base restaurada no contiene la estructura critica esperada."}
 Write-Host "Restauracion temporal validada correctamente." -ForegroundColor Green
 Write-Host "- Checksum SHA-256: correcto."
 Write-Host "- Tablas restauradas: $($r[0])."
 Write-Host "- Estructura critica de reservas y torneos: correcta."
 Write-Host "- La base principal no fue detenida ni modificada."
 $ok=$true
}finally{if($ok-or-not$ConservarAlFallar){docker rm -f $container 2>$null|Out-Null;docker volume rm $volume 2>$null|Out-Null;Write-Host "Entorno temporal de restauracion eliminado." -ForegroundColor Cyan}else{Write-Warning "Entorno conservado para diagnostico: $container"}}
