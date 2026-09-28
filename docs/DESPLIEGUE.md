# Despliegue local y Docker

## Variables de entorno

Crear `.env` en la raíz:

```env
MYSQL_DATABASE=padel_reservas
MYSQL_USER=padel_app
MYSQL_PASSWORD=CAMBIAR_PASSWORD_APP
MYSQL_ROOT_PASSWORD=CAMBIAR_PASSWORD_ROOT
WEB_ORIGIN=local
```

No subir `.env` al repositorio.

## Validar Docker Compose

```powershell
docker compose config
```

## Iniciar

```powershell
.\iniciar-simulacion.ps1
```

Alternativa:

```powershell
docker compose up --build -d
```

## Reconstrucción selectiva

### Web

```powershell
docker compose up --build -d web
```

### API

```powershell
docker compose up --build -d api
```

## Estado y registros

```powershell
docker compose ps
docker compose logs --tail 100
docker compose logs -f api
```

## Detener

```powershell
.\detener-simulacion.ps1
```

## Recrear contenedores sin borrar datos

```powershell
docker compose up -d --force-recreate mysql api web
```

## Recrear la base desde cero

Advertencia: elimina datos Docker.

```powershell
.\backup-mysql.ps1
docker compose down -v
.\preparar-docker-init.ps1
.\iniciar-simulacion.ps1
```

## Aplicación administrativa

Variables de la configuración de Eclipse:

```text
DB_URL=jdbc:mysql://127.0.0.1:3307/padel_reservas?serverTimezone=America/Argentina/Buenos_Aires&useSSL=false&allowPublicKeyRetrieval=true
DB_USER=padel_app
DB_PASSWORD=valor MYSQL_PASSWORD de .env
```

## Prueba de salud

```text
http://localhost:8080/api/publica/estado
http://localhost:8080/api/publica/canchas
http://localhost:5173
```
