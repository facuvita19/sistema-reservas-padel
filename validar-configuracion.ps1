param(
    [string]$Archivo = ".env",
    [ValidateSet("local", "produccion")][string]$Modo = "local"
)
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$ruta = if ([IO.Path]::IsPathRooted($Archivo)) { $Archivo } else { Join-Path $root $Archivo }
if (-not (Test-Path $ruta)) { throw "No se encontro el archivo de configuracion: $Archivo" }

$valores = @{}
Get-Content $ruta -Encoding UTF8 | ForEach-Object {
    $linea = $_.Trim()
    if ($linea -and -not $linea.StartsWith("#") -and $linea.Contains("=")) {
        $partes = $linea -split "=", 2
        $valores[$partes[0].Trim()] = $partes[1].Trim()
    }
}
$errores = [Collections.Generic.List[string]]::new()
$avisos = [Collections.Generic.List[string]]::new()
$estado = [ordered]@{}
$obligatorias = @("MYSQL_DATABASE","MYSQL_USER","MYSQL_PASSWORD","MYSQL_ROOT_PASSWORD","WEB_ORIGIN","API_WEB_PUBLICA_URL","SMTP_HOST","SMTP_PORT","SMTP_USER","SMTP_PASSWORD","SMTP_FROM_EMAIL","SMTP_FROM_NAME","SMTP_STARTTLS","SMTP_SSL","SMTP_TIMEOUT_MS","API_COOKIES_SECURE")
$marcadores = @("CAMBIAR_","PASSWORD_LOCAL","TU_DOMINIO","example.invalid","no-usar")
foreach ($clave in $obligatorias) {
    $valor = if ($valores.ContainsKey($clave)) { $valores[$clave] } else { "" }
    if ([string]::IsNullOrWhiteSpace($valor)) { $errores.Add("${clave}: faltante o vacia"); $estado[$clave]="ERROR"; continue }
    if ($marcadores | Where-Object { $valor -like "*$_*" }) { $errores.Add("${clave}: conserva un valor de ejemplo"); $estado[$clave]="ERROR"; continue }
    $estado[$clave]="OK"
}
foreach ($clave in @("MYSQL_PASSWORD","MYSQL_ROOT_PASSWORD","SMTP_PASSWORD")) {
    if ($valores.ContainsKey($clave) -and $valores[$clave].Length -lt 12) { $errores.Add("${clave}: debe tener al menos 12 caracteres"); $estado[$clave]="ERROR" }
}
if ($valores["MYSQL_PASSWORD"] -eq $valores["MYSQL_ROOT_PASSWORD"]) { $errores.Add("MYSQL_PASSWORD y MYSQL_ROOT_PASSWORD deben ser diferentes"); $estado["MYSQL_PASSWORD"]="ERROR"; $estado["MYSQL_ROOT_PASSWORD"]="ERROR" }
foreach ($clave in @("WEB_ORIGIN","API_WEB_PUBLICA_URL")) { try { [void][Uri]$valores[$clave] } catch { $errores.Add("${clave}: URL invalida"); $estado[$clave]="ERROR" } }
if ($valores["SMTP_PORT"] -notmatch '^\d+$' -or [int]$valores["SMTP_PORT"] -lt 1 -or [int]$valores["SMTP_PORT"] -gt 65535) { $errores.Add("SMTP_PORT: puerto invalido"); $estado["SMTP_PORT"]="ERROR" }
foreach ($clave in @("SMTP_STARTTLS","SMTP_SSL","API_COOKIES_SECURE")) { if ($valores[$clave] -notin @("true","false")) { $errores.Add("${clave}: debe ser true o false"); $estado[$clave]="ERROR" } }
if ($valores["SMTP_STARTTLS"] -eq "true" -and $valores["SMTP_SSL"] -eq "true") { $errores.Add("SMTP_STARTTLS y SMTP_SSL no pueden estar activos simultaneamente") }
if ($Modo -eq "produccion") {
    if ($valores["WEB_ORIGIN"] -notmatch '^https://') { $errores.Add("WEB_ORIGIN debe usar HTTPS en produccion") }
    if ($valores["API_WEB_PUBLICA_URL"] -notmatch '^https://') { $errores.Add("API_WEB_PUBLICA_URL debe usar HTTPS en produccion") }
    if ($valores["WEB_ORIGIN"] -eq "local") { $errores.Add("WEB_ORIGIN no puede ser local en produccion") }
    if ($valores["API_COOKIES_SECURE"] -ne "true") { $errores.Add("API_COOKIES_SECURE debe ser true en produccion") }
} elseif ($valores["API_COOKIES_SECURE"] -eq "true" -and $valores["WEB_ORIGIN"] -match '^http://') {
    $avisos.Add("API_COOKIES_SECURE=true requiere HTTPS; el inicio de sesion local por HTTP puede no conservar la cookie")
}
Write-Host "Validacion segura de configuracion ($Modo)" -ForegroundColor Cyan
Write-Host "Los valores sensibles fueron redactados; no se imprimio ninguna credencial." -ForegroundColor DarkGray
foreach ($clave in $obligatorias) { $e=if($estado[$clave]-eq 'OK'){'OK'}else{'ERROR'}; Write-Host ("{0,-32} {1}" -f $clave,$e) -ForegroundColor $(if($e-eq'OK'){'Green'}else{'Red'}) }
foreach($a in $avisos){Write-Warning $a}
if($errores.Count){Write-Host ""; $errores|ForEach-Object{Write-Host "- $_" -ForegroundColor Red}; throw "La configuracion no es segura o esta incompleta. Se detectaron $($errores.Count) problema(s)."}
Write-Host "Configuracion validada correctamente sin exponer secretos." -ForegroundColor Green
