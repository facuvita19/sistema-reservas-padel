$ErrorActionPreference="Stop";$root=Split-Path -Parent $MyInvocation.MyCommand.Path;Set-Location $root
$files=git ls-files;$hallazgos=[Collections.Generic.List[string]]::new();$patrones=@(
 @{N="Clave privada";R='-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----'},
 @{N="Token GitHub";R='gh[pousr]_[A-Za-z0-9]{20,}'},
 @{N="Clave Google";R='AIza[0-9A-Za-z_-]{20,}'},
 @{N="AWS access key";R='AKIA[0-9A-Z]{16}'},
 @{N="Bearer token";R='Bearer\s+[A-Za-z0-9._-]{24,}'}
)
foreach($rel in $files){$p=Join-Path $root $rel;if(-not(Test-Path $p)){continue};if((Get-Item $p).Length-gt1MB){continue};if($rel-match'\.(png|jpg|jpeg|gif|webp|ico|woff2?|ttf|jar|zip|pdf)$'){continue};$n=0;Get-Content $p -Encoding UTF8 -ErrorAction SilentlyContinue|ForEach-Object{$n++;$line=$_;foreach($pat in $patrones){if($line-match$pat.R){$hallazgos.Add("$rel`:$n [$($pat.N)] [REDACTADO]")}}}}
Write-Host "Auditoria de secretos versionados" -ForegroundColor Cyan
Write-Host "No se leyo .env ni se imprimieron coincidencias." -ForegroundColor DarkGray
if($hallazgos.Count){$hallazgos|ForEach-Object{Write-Host $_ -ForegroundColor Red};throw "Se detectaron $($hallazgos.Count) patrones de alta confianza."}
Write-Host "No se detectaron secretos de alta confianza." -ForegroundColor Green
