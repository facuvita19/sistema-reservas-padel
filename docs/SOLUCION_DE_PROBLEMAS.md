# Solución de problemas

## La web muestra “Servicio no disponible”

Verificar:

```powershell
docker compose ps
docker compose logs api --tail 100
```

Probar:

```text
http://localhost:8080/api/publica/estado
```

## El celular abre la web pero no carga canchas

1. Usar `.env`:

```env
WEB_ORIGIN=local
```

2. Recrear API:

```powershell
docker compose up -d --force-recreate api
```

3. Probar desde celular:

```text
http://IP_DE_LA_PC:8080/api/publica/canchas
```

4. Revisar firewall en puertos `5173` y `8080`.

## La cuenta regresiva suma tres horas

Verificar en `docker-compose.yml`:

```yaml
TZ: "America/Argentina/Buenos_Aires"
JAVA_TOOL_OPTIONS: "... -Duser.timezone=America/Argentina/Buenos_Aires"
```

Recrear API y MySQL:

```powershell
docker compose up -d --force-recreate mysql api
```

## La administración modifica otra base

La configuración de Eclipse debe contener `DB_URL`, `DB_USER` y `DB_PASSWORD` apuntando al puerto `3307`.

## MySQL está unhealthy

```powershell
docker compose logs mysql --tail 200
```

Si es una base nueva y falló la inicialización por orden SQL:

```powershell
docker compose down -v
.\preparar-docker-init.ps1
.\iniciar-simulacion.ps1
```

Advertencia: elimina los datos Docker.

## Docker no se reconoce

Cerrar y volver a abrir PowerShell. Confirmar que Docker Desktop esté instalado y abierto:

```powershell
docker --version
docker compose version
docker info
```

## Git no incluye cambios

Orden correcto:

```powershell
git add ARCHIVOS
git commit -m "Descripcion simple"
git push origin HEAD
```

## Textos con símbolos extraños

No procesar archivos UTF-8 con `Get-Content` y `Set-Content` sin codificación explícita. Restaurar el archivo:

```powershell
git restore "ruta-del-archivo"
```

Para lectura y escritura programática usar UTF-8 explícito.

## Límite de intentos durante pruebas

Reiniciar solamente la API:

```powershell
docker compose restart api
```

## El horario sigue ocupado después de vencer

Verificar el procesador:

```powershell
docker compose logs api --tail 100
```

Debe iniciar con intervalo configurado y registrar expiraciones cuando corresponda.
