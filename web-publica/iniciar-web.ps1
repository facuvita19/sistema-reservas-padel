$ErrorActionPreference = "Stop"
$port = 5173
$folder = Split-Path -Parent $MyInvocation.MyCommand.Path
Write-Host "Web publica disponible en http://localhost:$port" -ForegroundColor Green
Write-Host "Manten esta ventana abierta mientras realizas las pruebas."
Set-Location $folder
python -m http.server $port --bind 127.0.0.1
