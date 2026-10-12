param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[^@\s]+@[^@\s]+\.[^@\s]+$')]
    [string]$Destinatario
)
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root
if (-not (Test-Path ".env")) { throw "Falta el archivo .env." }

$valores = @{}
Get-Content ".env" -Encoding UTF8 | ForEach-Object {
    $linea = $_.Trim()
    if ($linea -and -not $linea.StartsWith("#") -and $linea.Contains("=")) {
        $partes = $linea -split "=", 2
        $valores[$partes[0].Trim()] = $partes[1].Trim()
    }
}
$requeridas = @("SMTP_HOST","SMTP_PORT","SMTP_USER","SMTP_PASSWORD","SMTP_FROM_EMAIL","SMTP_FROM_NAME","SMTP_STARTTLS","SMTP_SSL","SMTP_TIMEOUT_MS")
foreach ($clave in $requeridas) {
    if (-not $valores.ContainsKey($clave) -or [string]::IsNullOrWhiteSpace($valores[$clave])) {
        throw "Falta una variable SMTP obligatoria: $clave"
    }
}
$puerto = 0
if (-not [int]::TryParse($valores["SMTP_PORT"], [ref]$puerto) -or $puerto -lt 1 -or $puerto -gt 65535) { throw "SMTP_PORT no es valido." }
$startTls = $valores["SMTP_STARTTLS"] -eq "true"
$ssl = $valores["SMTP_SSL"] -eq "true"
if ($startTls -and $ssl) { throw "SMTP_STARTTLS y SMTP_SSL no pueden estar activos simultaneamente." }

Write-Host "Prueba SMTP controlada" -ForegroundColor Cyan
Write-Host "Destinatario: $Destinatario"
Write-Host "Servidor, usuario y credenciales: [REDACTADOS]"
Write-Host "Se enviara un unico correo tecnico sin datos privados del sistema." -ForegroundColor Yellow
if ((Read-Host "Escribi ENVIAR para continuar") -cne "ENVIAR") {
    Write-Host "Envio cancelado."
    exit 0
}

$mensaje = [Net.Mail.MailMessage]::new()
$cliente = [Net.Mail.SmtpClient]::new($valores["SMTP_HOST"], $puerto)
try {
    $mensaje.From = [Net.Mail.MailAddress]::new($valores["SMTP_FROM_EMAIL"], $valores["SMTP_FROM_NAME"])
    [void]$mensaje.To.Add($Destinatario)
    $mensaje.Subject = "Prueba tecnica SMTP - Sistema de Reservas de Padel"
    $mensaje.Body = "La configuracion SMTP fue validada correctamente. Fecha local: $((Get-Date).ToString('yyyy-MM-dd HH:mm:ss')). Este mensaje no contiene credenciales ni datos de clientes."
    $mensaje.IsBodyHtml = $false
    $cliente.EnableSsl = ($startTls -or $ssl)
    $cliente.Credentials = [Net.NetworkCredential]::new($valores["SMTP_USER"], $valores["SMTP_PASSWORD"])
    $cliente.Timeout = [int]$valores["SMTP_TIMEOUT_MS"]
    $cliente.Send($mensaje)
    Write-Host "SMTP: ENVIADO" -ForegroundColor Green
    Write-Host "Destinatario confirmado: $Destinatario"
    Write-Host "No se imprimieron credenciales ni contenido de .env."
}
catch {
    $tipo = $_.Exception.GetType().Name
    Write-Host "SMTP: ERROR" -ForegroundColor Red
    Write-Host "Tipo de error: $tipo"
    throw "La prueba SMTP no pudo completarse. Revisar configuracion, red o autorizacion del proveedor."
}
finally {
    $mensaje.Dispose()
    $cliente.Dispose()
}
