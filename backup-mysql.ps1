param(
    [ValidateRange(1, 3650)][int]$RetencionDias = 30,
    [ValidateRange(1, 1000)][int]$MinimoConservar = 7,
    [switch]$SinRetencion
)
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root
if (-not (Test-Path ".env")) { throw "Falta el archivo .env." }
$values=@{}; Get-Content ".env" -Encoding UTF8 | Where-Object { $_ -match "^[^#].+=" } | ForEach-Object { $p=$_ -split "=",2; $values[$p[0].Trim()]=$p[1].Trim() }
foreach($key in @("MYSQL_DATABASE","MYSQL_ROOT_PASSWORD")){if(-not $values.ContainsKey($key)-or[string]::IsNullOrWhiteSpace($values[$key])){throw "Falta $key en .env."}}
$health=docker inspect -f "{{.State.Health.Status}}" padel-mysql 2>$null;if($LASTEXITCODE-ne 0-or$health.Trim()-ne"healthy"){throw "El contenedor padel-mysql no esta disponible o no esta healthy."}
$backupDir=Join-Path $root "backups";New-Item -ItemType Directory -Force -Path $backupDir|Out-Null
$stamp=Get-Date -Format "yyyyMMdd-HHmmss";$file=Join-Path $backupDir "padel_reservas_$stamp.sql";$hashFile="$file.sha256";$containerFile="/tmp/padel_reservas_$stamp.sql"
$password=$values["MYSQL_ROOT_PASSWORD"];$database=$values["MYSQL_DATABASE"]
try{
 docker exec -e "MYSQL_PWD=$password" padel-mysql sh -c "mysqldump -uroot --single-transaction --routines --triggers --events --hex-blob --default-character-set=utf8mb4 --set-gtid-purged=OFF --no-tablespaces '$database' > '$containerFile'"
 if($LASTEXITCODE-ne 0){throw "No se pudo generar el backup dentro del contenedor."}
 docker cp "padel-mysql:$containerFile" $file;if($LASTEXITCODE-ne 0){throw "No se pudo copiar el backup a Windows."}
}finally{docker exec padel-mysql rm -f $containerFile 2>$null|Out-Null}
if(-not(Test-Path $file)-or(Get-Item $file).Length-lt 1000){Remove-Item $file -ErrorAction SilentlyContinue;throw "No se pudo crear un backup valido."}
$contenido=Get-Content $file -Raw -Encoding UTF8
foreach($marca in @("CREATE TABLE","INSERT INTO","reservas","clientes","canchas")){if($contenido-notmatch[regex]::Escape($marca)){Remove-Item $file -ErrorAction SilentlyContinue;throw "El backup no contiene la estructura esperada: $marca"}}
$hash=(Get-FileHash $file -Algorithm SHA256).Hash.ToLowerInvariant();"$hash  $([IO.Path]::GetFileName($file))"|Set-Content $hashFile -Encoding ascii
$eliminados=0
if(-not$SinRetencion){$todos=@(Get-ChildItem $backupDir -Filter "padel_reservas_*.sql" -File|Sort-Object LastWriteTime -Descending);$limite=(Get-Date).AddDays(-$RetencionDias);for($i=$MinimoConservar;$i-lt$todos.Count;$i++){if($todos[$i].LastWriteTime-lt$limite){Remove-Item $todos[$i].FullName -Force;Remove-Item ($todos[$i].FullName+".sha256") -Force -ErrorAction SilentlyContinue;$eliminados++}}}
Write-Host "Backup creado y verificado correctamente:" -ForegroundColor Green
Write-Host $file
Write-Host "Tamano: $([math]::Round((Get-Item $file).Length/1KB,2)) KB"
Write-Host "Checksum SHA-256: creado (valor redactado)"
Write-Host "Retencion: $RetencionDias dias, minimo $MinimoConservar, eliminados $eliminados"
