# Seguridad operativa

## Secretos

No versionar:

```text
.env
database.properties
database.local.properties
backups/
```

Usar contraseñas diferentes para el usuario de aplicación y `root`.

## Base de datos

- MySQL Docker se publica sólo en `127.0.0.1:3307`.
- La aplicación usa `padel_app`.
- Reservar `root` para administración y backup.
- No exponer el puerto de MySQL a Internet.

## API

La API incluye:

- Límite de cuerpo JSON.
- Límite general por IP.
- Límite para creación de reservas.
- Protección contra doble envío.
- Respuestas controladas.
- Identificador de operación.
- Cabeceras de seguridad.
- CORS configurable.

Para entorno local y celular:

```env
WEB_ORIGIN=local
```

Para producción se debe usar el origen exacto HTTPS del sitio.

## Git

Antes de cada commit:

```powershell
git status --short
git diff --cached --name-only
```

Confirmar que no aparezcan credenciales ni backups.

## Backups

- Mantener copias fuera del servidor.
- Proteger los archivos SQL.
- Limitar acceso a la carpeta de backups.
- Probar restauración.

## Producción pendiente

Antes de publicar en Internet:

- HTTPS obligatorio.
- Dominio y CORS exactos.
- Secretos administrados por el proveedor.
- Logs estructurados y rotación.
- Backups automáticos externos.
- Política de privacidad.
- Revisión de dependencias.

## Preflight seguro

Antes de iniciar, `validar-configuracion.ps1` comprueba variables obligatorias sin imprimir valores. En producción usar `-Modo produccion`, HTTPS, origen exacto y `API_COOKIES_SECURE=true`. Nunca usar `docker compose config` en salidas compartidas porque expande secretos.

## Endurecimiento de contenedores

- La API se ejecuta con el usuario no privilegiado `padel`.
- Los tres servicios usan rotación de logs `json-file` con 10 MB y 5 archivos.
- Se aplican límites prudentes de memoria, CPU y procesos.
- `no-new-privileges` evita elevar privilegios dentro de los contenedores.
- La API usa sistema de archivos de solo lectura y un `/tmp` temporal limitado.
- Nginx incorpora CSP y cabeceras adicionales. HSTS debe habilitarse únicamente detrás de HTTPS real.
