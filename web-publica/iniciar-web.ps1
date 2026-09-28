$ErrorActionPreference = "Stop"
$port = 5173
$folder = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $folder

$ip = Get-NetIPAddress -AddressFamily IPv4 |
    Where-Object {
        $_.IPAddress -notlike "127.*" -and
        $_.IPAddress -notlike "169.254.*" -and
        $_.InterfaceOperationalStatus -eq "Up"
    } |
    Select-Object -First 1 -ExpandProperty IPAddress

Write-Host "Web local iniciada." -ForegroundColor Green
Write-Host "Computadora: http://localhost:$port"
if ($ip) {
    Write-Host "Celular:     http://${ip}:$port" -ForegroundColor Cyan
    Write-Host "Conecta el celular al mismo Wi-Fi."
}
Write-Host "Manten esta ventana abierta. Para detener: Ctrl + C"
python -m http.server $port --bind 0.0.0.0