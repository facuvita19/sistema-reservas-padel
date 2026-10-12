$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$archivos = @(git ls-files)
$hallazgos = [Collections.Generic.List[string]]::new()
$patrones = @(
    @{ Nombre = "Clave privada"; Regex = '-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----' },
    @{ Nombre = "Token GitHub"; Regex = 'gh[pousr]_[A-Za-z0-9]{20,}' },
    @{ Nombre = "Clave Google"; Regex = 'AIza[0-9A-Za-z_-]{20,}' },
    @{ Nombre = "AWS access key"; Regex = 'AKIA[0-9A-Z]{16}' },
    @{ Nombre = "Bearer token"; Regex = 'Bearer\s+[A-Za-z0-9._-]{24,}' }
)
$extensionesBinarias = '\.(png|jpg|jpeg|gif|webp|ico|woff2?|ttf|jar|zip|pdf)$'

foreach ($relativo in $archivos) {
    if ([string]::IsNullOrWhiteSpace($relativo)) { continue }
    if ($relativo -match $extensionesBinarias) { continue }

    $ruta = Join-Path $root $relativo
    $item = Get-Item -LiteralPath $ruta -ErrorAction SilentlyContinue
    if ($null -eq $item) { continue }
    if ($item.PSIsContainer) { continue }
    if ($item.Length -gt 1MB) { continue }

    $numeroLinea = 0
    Get-Content -LiteralPath $item.FullName -Encoding UTF8 -ErrorAction SilentlyContinue |
        ForEach-Object {
            $numeroLinea++
            $linea = $_
            foreach ($patron in $patrones) {
                if ($linea -match $patron.Regex) {
                    $descripcion = $relativo + ":" + $numeroLinea + " [" + $patron.Nombre + "] [REDACTADO]"
                    $hallazgos.Add($descripcion)
                }
            }
        }
}

Write-Host "Auditoria de secretos versionados" -ForegroundColor Cyan
Write-Host "No se leyo .env ni se imprimieron coincidencias." -ForegroundColor DarkGray

if ($hallazgos.Count -gt 0) {
    $hallazgos | ForEach-Object { Write-Host $_ -ForegroundColor Red }
    throw "Se detectaron $($hallazgos.Count) patrones de alta confianza."
}

Write-Host "No se detectaron secretos de alta confianza." -ForegroundColor Green
