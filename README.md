# Plataforma Deportiva W2

Sistema integral para la gestión comercial y deportiva de complejos de pádel. El proyecto combina una aplicación administrativa, una web pública premium, una API Java y una base MySQL reproducible con Docker.

> Proyecto de portfolio en evolución activa. No contiene credenciales, datos personales ni configuración productiva real.

## Capacidades principales

- Reservas públicas con protección temporal del turno y seguimiento.
- Administración de canchas, clientes, caja, pagos y solicitudes web.
- Torneos por parejas, fase de grupos, eliminatorias y resultados.
- Identidad deportiva, perfiles públicos, historial y rankings auditables.
- Comunidad para completar partidos y encontrar pareja para torneos.
- Invitaciones privadas, contacto protegido y centro interno de notificaciones.
- Transferencia manual y arquitectura preparada para Mercado Pago.
- Seguridad por sesión, autorización backend, idempotencia y auditoría.
- Experiencia responsive, accesible y optimizada para modo oscuro.

## Arquitectura

```text
Web publica Astro + TypeScript
              |
              v
API publica Java 25
              |
              v
           MySQL
              ^
              |
Aplicacion administrativa JavaFX
```

El entorno Docker incluye MySQL, API y Nginx. La aplicación administrativa puede utilizar la misma base local expuesta por Docker.

## Tecnologías

- Java 25, Maven y JavaFX.
- Astro, TypeScript y Playwright.
- MySQL 8 y migraciones SQL idempotentes.
- Docker Compose y Nginx.
- GitHub Actions.
- Integración de pagos diseñada para Mercado Pago.

## Calidad

El repositorio contiene pruebas de dominio, servicios, contratos, migraciones, seguridad, frontend y recorridos E2E. El CI aplica validaciones proporcionales al alcance del cambio y conserva una ejecución integral manual para hitos.

## Inicio rápido

### Requisitos

- Java 25.
- Maven.
- Node.js 22.
- Docker Desktop.
- PowerShell 7 para los scripts operativos.

### Entorno Docker

1. Copiar la configuración de ejemplo:

```powershell
Copy-Item .env.example .env
```

2. Reemplazar únicamente los placeholders locales del archivo privado `.env`.
3. Iniciar el entorno:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\iniciar-simulacion.ps1
```

4. Abrir:

```text
Web: http://localhost:4321
API: http://localhost:8080/api/publica/estado
```

5. Verificar:

```powershell
docker compose ps
```

### Desarrollo frontend

```powershell
Set-Location .\web-publica-v2
npm ci
npm run check
npm run dev
```

### Pruebas backend

```powershell
mvn clean test
```

## Configuración y privacidad

- `.env` y las configuraciones locales están ignoradas.
- `.env.example` contiene únicamente nombres, valores locales y placeholders.
- Mercado Pago permanece deshabilitado hasta proporcionar credenciales privadas.
- La URL de retorno nunca acredita pagos por sí sola.
- No deben publicarse dumps, respaldos, capturas con PII, claves o certificados.

Consultar [SECURITY.md](SECURITY.md) antes de reportar una vulnerabilidad.

## Documentación

- [Plan de desarrollo](PLAN_DESARROLLO.md)
- [Plataforma Deportiva W2](docs/PLATAFORMA_DEPORTIVA_W2.md)
- [Arquitectura funcional W2](docs/ARQUITECTURA_FUNCIONAL_W2.md)
- [Dirección visual W2](docs/DIRECCION_VISUAL_W2.md)
- [Mapa de experiencia W2](docs/MAPA_EXPERIENCIA_W2.md)
- [Arquitectura general](docs/ARQUITECTURA.md)
- [Despliegue](docs/DESPLIEGUE.md)
- [Backup y restauración](docs/BACKUP_Y_RESTAURACION.md)
- [Seguridad operativa](docs/SEGURIDAD_OPERATIVA.md)

## Estrategia móvil

La dirección inicial es completar una PWA y luego incorporar Capacitor para Android e iOS. La capa móvil podrá evolucionar con componentes adicionales si las pruebas en dispositivos reales no alcanzan el estándar visual y funcional del producto.

## Licencia

El código se publica para evaluación de portfolio. No se concede permiso para copiar, redistribuir, sublicenciar, vender ni explotar comercialmente el proyecto. Consultar [LICENSE](LICENSE).
