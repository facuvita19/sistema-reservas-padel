# Sistema de reservas de pádel

Sistema comercial para administrar canchas, clientes, reservas, pagos, caja y solicitudes web de un complejo de pádel.

## Componentes

- **Aplicación administrativa:** escritorio JavaFX.
- **Web pública:** reserva y seguimiento desde computadora o celular.
- **API pública:** servicio Java independiente en el puerto `8080`.
- **Base de datos:** MySQL.
- **Entorno reproducible:** Docker Compose con MySQL, API y Nginx.

## Arquitectura

```text
Cliente web
    ↓
Nginx, puerto 4321
    ↓
API Java, puerto 8080
    ↓
MySQL Docker, puerto interno 3306
    ↑
Aplicación administrativa, conexión local al puerto 3307
```

## Requisitos

- Windows 10 u 11 de 64 bits.
- Java compatible con el proyecto.
- Eclipse con soporte Maven y JavaFX.
- Docker Desktop con WSL 2.
- Git.
- MySQL Workbench, opcional.

## Inicio rápido con Docker

1. Abrir Docker Desktop.
2. Abrir PowerShell en la raíz del proyecto.
3. Ejecutar:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\iniciar-simulacion.ps1
```

4. Abrir:

```text
Web: http://localhost:4321
API: http://localhost:8080/api/publica/estado
```

5. Comprobar servicios:

```powershell
docker compose ps
```

## Administración sobre la misma base Docker

La configuración de ejecución de Eclipse debe usar:

```text
DB_URL=jdbc:mysql://127.0.0.1:3307/padel_reservas?serverTimezone=America/Argentina/Buenos_Aires&useSSL=false&allowPublicKeyRetrieval=true
DB_USER=padel_app
DB_PASSWORD=valor MYSQL_PASSWORD del archivo .env
```

La aplicación administrativa, la API y la web compartirán así una única base de datos.

## Detener el entorno

```powershell
.\detener-simulacion.ps1
```

No ejecutar `docker compose down -v` salvo que se quiera borrar el volumen y todos los datos Docker.

## Configuración privada

Crear `.env` a partir de `.env.example`:

```env
MYSQL_DATABASE=padel_reservas
MYSQL_USER=padel_app
MYSQL_PASSWORD=CAMBIAR_PASSWORD_APP
MYSQL_ROOT_PASSWORD=CAMBIAR_PASSWORD_ROOT
WEB_ORIGIN=local
```

Los siguientes archivos no deben subirse a Git:

```text
.env
database.properties
database.local.properties
backups/
target/
database/docker-init/
```

## Documentación

- [Operación diaria](docs/OPERACION_DIARIA.md)
- [Manual administrativo](docs/MANUAL_ADMINISTRATIVO.md)
- [Arquitectura](docs/ARQUITECTURA.md)
- [Despliegue local y Docker](docs/DESPLIEGUE.md)
- [Backup y restauración](docs/BACKUP_Y_RESTAURACION.md)
- [Solución de problemas](docs/SOLUCION_DE_PROBLEMAS.md)
- [Despliegue y rollback](docs/DESPLIEGUE_Y_ROLLBACK.md)
- [Continuidad del proyecto](docs/CONTINUIDAD_DEL_PROYECTO.md)
- [Plan de desarrollo](PLAN_DESARROLLO.md)
- [Plataforma Deportiva W2](docs/PLATAFORMA_DEPORTIVA_W2.md)
- [Arquitectura funcional W2](docs/ARQUITECTURA_FUNCIONAL_W2.md)
- [Dirección visual W2](docs/DIRECCION_VISUAL_W2.md)
- [Mapa de experiencia W2](docs/MAPA_EXPERIENCIA_W2.md)
- [Seguridad operativa](docs/SEGURIDAD_OPERATIVA.md)

## Estado actual

El sistema incluye:

- Disponibilidad pública en tiempo real y protección temporal del turno.
- Reservas desde Plataforma Deportiva W2 con cuenta de cliente, historial y seguimiento.
- Seña por transferencia manual, alias, importe exacto, comprobante por WhatsApp y verificación administrativa.
- Núcleo transaccional de órdenes, intentos y eventos de pago.
- Integración preparada con Mercado Pago para checkout online, retorno, webhook e idempotencia.
- Acreditación transaccional sin confiar en parámetros del navegador.
- Prevención de doble envío, concurrencia y procesamiento repetido de eventos.
- Persistencia, migraciones, backup y restauración.
- Compatibilidad con computadora y celular.

### Estado de pagos

La transferencia manual fue validada visual y funcionalmente el 14 de octubre de 2026. El pago online queda condicionado a configurar credenciales privadas de Mercado Pago y completar una prueba integral con una cuenta de prueba.

Las credenciales se suministran mediante variables de entorno y nunca deben escribirse en el repositorio, documentación, logs ni capturas. Antes de ejecutar diagnósticos de configuración, usar salidas redactadas que informen únicamente si cada variable está presente.
