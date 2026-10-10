$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

$classes = Join-Path $projectRoot "target\classes"
$mavenRepository = Join-Path $env:USERPROFILE ".m2\repository"

if (-not (Test-Path $classes)) {
    throw "No existe target\classes. En Eclipse ejecuta primero Project > Clean y luego inicia una vez el proyecto o ejecuta Run As > Maven test."
}

if (-not (Test-Path (Join-Path $classes "api\publica\ApiPublicaApp.class"))) {
    throw "No se encontro ApiPublicaApp.class. En Eclipse guarda los archivos, presiona F5 y ejecuta Project > Clean antes de volver a intentar."
}

if (-not (Test-Path $mavenRepository)) {
    throw "No se encontro el repositorio local de Maven en $mavenRepository."
}

$dependencyJars = Get-ChildItem $mavenRepository -Recurse -Filter "*.jar" -File |
    Where-Object {
        $_.FullName -match "jackson|mysql-connector-j"
    } |
    Select-Object -ExpandProperty FullName

if (-not $dependencyJars -or $dependencyJars.Count -eq 0) {
    throw "No se encontraron las dependencias de Jackson y MySQL en el repositorio local de Maven."
}

$classPath = @($classes) + $dependencyJars
$classPathText = $classPath -join ";"

$ip = Get-NetIPAddress -AddressFamily IPv4 |
    Where-Object {
        $_.IPAddress -notlike "127.*" -and
        $_.IPAddress -notlike "169.254.*" -and
        $_.InterfaceOperationalStatus -eq "Up"
    } |
    Select-Object -First 1 -ExpandProperty IPAddress

Write-Host "API publica independiente iniciada." -ForegroundColor Green
Write-Host "Computadora: http://localhost:8080/api/publica/estado"
if ($ip) {
    Write-Host "Red local:  http://${ip}:8080/api/publica/estado" -ForegroundColor Cyan
}
Write-Host "Manten esta ventana abierta. Para detener: Ctrl + C"

# Fuente unica para los enlaces reales enviados por correo.
# La variable se fija al iniciar para no depender de clases compiladas antiguas,
# configuraciones heredadas de Eclipse ni variables externas de otra sesion.
$env:API_WEB_PUBLICA_URL = "http://localhost:4321/"
Write-Host "Web publica para correos: $env:API_WEB_PUBLICA_URL" -ForegroundColor Cyan

java -cp $classPathText api.publica.ApiPublicaApp
