# Plataforma Deportiva W2

## Estado autoritativo · 31 de octubre de 2026

<!-- W2-ESTADO-AUTORITATIVO-2026-10-31-INICIO -->

### Estado consolidado de Plataforma Deportiva W2

- La rama estable es `master` y el último cierre funcional publicado quedó validado por CI en verde.
- Inicio es la referencia visual y de fluidez autoritativa de la web pública.
- Inicio, Reservar, Torneos, Rankings, Comunidad y Mi cuenta están convergidos visualmente en su alcance actual.
- El modo oscuro es la experiencia principal; el modo claro continúa siendo una alternativa real y obligatoria.
- La web pública y la aplicación administrativa son productos independientes.
- La experiencia de escritorio se termina y estabiliza antes de iniciar la aplicación móvil definitiva.
- El prototipo Capacitor fue retirado como arquitectura de producción. La futura aplicación móvil será independiente y se decidirá mediante un vertical slice medido entre React Native, Flutter u otras alternativas justificadas.
- No corresponde reabrir automáticamente la antigua auditoría integral de rendimiento. Las mediciones se ampliarán solo ante evidencia concreta.

### Cierre T2B1 · Continuidad integral de Torneos

Torneos quedó cerrado en catálogo, detalle y centro competitivo:

- filtros, búsqueda y estado activo persistentes en URL;
- retorno al catálogo con scroll y foco previsibles;
- pestañas del detalle sin escalones artificiales de historial;
- shell estable del centro competitivo: topbar, hero y navegación permanecen montados;
- Resumen, Posiciones, Partidos y Cuadro reemplazan únicamente el panel interno;
- grupo, fase y ronda admiten entrada profunda y restauración por URL;
- Atrás y Adelante conservan contexto sin duplicar estados;
- BFCache y `pageshow` persistido sincronizan el estado sin reconstruir el shell;
- teclado, foco, scroll horizontal, movimiento reducido y llave completa permanecen cubiertos;
- las inscripciones continúan siendo exclusivamente por pareja;
- la terminología pública vigente es Libre, Femenino y Mixto, con elegibilidad Suma 8, Suma 10, Suma 12, etc.

### Orden oficial de continuidad

1. **T2C1 · Continuidad interna de Rankings**: categoría, búsqueda, podio, tabla, auditoría, perfil público, historial, foco, scroll y BFCache.
2. **T2C2 · Continuidad interna de Comunidad**: vistas, filtros, perfiles, turnos, convocatorias, solicitudes, notificaciones y retorno contextual.
3. **T2C3 · Continuidad de Mi cuenta y Perfil público**: secciones privadas, detalles, edición protegida, perfil, historial y recuperación de contexto.
4. **T2D · Extensión global del patrón fluido de Inicio**: rutas principales, estados transitorios, shell, tema, sesión, foco, scroll y degradación segura.
5. **Revisión transversal final de escritorio**: oscuro, claro, responsive web, accesibilidad, estados extremos y consistencia.
6. **Estabilización final de la web**: consolidación técnica y rendimiento medido, PWA, operación y certificación.
7. **Aplicación móvil independiente**: evaluación tecnológica y desarrollo posterior al cierre web.

### Regla de interpretación documental

Este bloque reemplaza cualquier sección posterior que todavía presente T1, U1, U2, R3.3I, Capacitor o la convergencia visual por rutas como próximos pasos vigentes. Las secciones fechadas antes del 31 de octubre de 2026 se conservan como historial técnico, salvo cuando una cabecera indique expresamente lo contrario.

<!-- W2-ESTADO-AUTORITATIVO-2026-10-31-FIN -->

### Estado del portfolio

La web de escritorio está visualmente convergida y la continuidad interna de Torneos está cerrada. Rankings es el siguiente recorrido transversal.

> **Archivo histórico debajo de este punto.** Las fechas y próximos bloques anteriores quedan subordinados al estado autoritativo del 31 de octubre de 2026.


## Estado autoritativo · 30 de octubre de 2026

<!-- W2-ESTADO-ESCRITORIO-2026-10-30 -->

### Web pública de escritorio convergida

Las rutas principales de Plataforma Deportiva W2 fueron revisadas y unificadas visualmente en su alcance actual. Inicio continúa como referencia autoritativa; Reservar conserva su carácter operativo; Torneos su lenguaje competitivo; Rankings su carácter analítico; Comunidad su función de descubrimiento; y Mi cuenta su función privada de gestión.

- Rankings quedó unificado y publicado en `18d838b`.
- Comunidad quedó unificada y publicada en `010ca8e`.
- Cabecera, navegaciones sticky y menú de cuenta ya comparten el patrón global vigente.
- El modo oscuro continúa siendo la experiencia principal.
- La aplicación administrativa permanece separada de la web pública.

### Continuidad inmediata

El próximo bloque no consiste en volver a auditar visualmente cada ruta ni en retomar automáticamente la antigua auditoría integral de rendimiento. La continuidad vigente es la experiencia transversal de navegación interna: transiciones entre rutas W2, conservación de cabecera y scroll, foco, estados transitorios y menor percepción de recarga completa. Después se evaluarán rendimiento real, PWA, Mercado Pago productivo y la aplicación móvil independiente según prioridad y dependencias.


<!-- W2-CIERRE-C143387-INICIO -->
## Estado autoritativo · 29 de octubre de 2026

El bloque **Modernizar Mi cuenta y estabilizar cabecera y torneos** quedó publicado en `master` mediante el commit `c143387`. Este estado reemplaza cualquier continuidad anterior dentro de este documento que todavía indique que Mi cuenta está pendiente, que Datos personales o Seguridad redirigen a rutas heredadas, que Torneos sigue únicamente en U1/U2 o que Capacitor constituye la siguiente evolución móvil.

### Cierre funcional y visual

- Mi cuenta converge como centro privado único con **Resumen, Reservas, Datos personales, Seguridad y Perfil deportivo**.
- Resumen publica próxima reserva, recorrido en el club y accesos coherentes con la identidad W2.
- Reservas diferencia próximas e historial, conserva detalle profundo en URL y ofrece consulta estable mediante código de seguimiento.
- Datos personales edita, valida, guarda y descarta cambios dentro de Mi cuenta; aceptar la salida con cambios pendientes restaura el último estado confirmado y cierra el modo edición.
- Seguridad actualiza la contraseña dentro del mismo centro privado.
- Los CTA de Reservar y Consultar comparten geometría, profundidad, foco, estados y movimiento reducido; la consulta mantiene la geometría durante la carga.
- La cabecera H3, el menú de cuenta, el primer frame del tema y la navegación competitiva móvil quedaron estabilizados.
- Torneos conserva catálogo, detalle, inscripción y centro competitivo; Resumen, Posiciones, Partidos y Cuadro mantienen navegación móvil contenida y accesible.

### Validación confirmada

- Astro Check: **0 errores, 0 advertencias y 0 hints**.
- Suite focalizada de Mi cuenta: **78 pruebas aprobadas**.
- Saneamiento de cabecera y Torneos: **148 pruebas aprobadas**, seguido por **3 pruebas focalizadas finales aprobadas**.
- `git diff --check`: correcto; los avisos LF/CRLF de Windows son informativos.
- Commit y push completados sobre `master`; repositorio limpio al cierre.

### Continuidad vigente

La prioridad vuelve a la **reconstrucción premium de la web pública de escritorio**. Inicio continúa como referencia visual autoritativa y Mi cuenta queda cerrada en su alcance actual. El próximo frente debe elegirse por impacto visual y funcional entre las rutas públicas aún pendientes de convergencia, sin volver automáticamente a la antigua auditoría integral de rendimiento. La aplicación móvil definitiva será un producto independiente y se evaluará después de estabilizar la web de escritorio.
<!-- W2-CIERRE-C143387-FIN -->

### Estado del portfolio

Mi cuenta ya concentra gestión personal, reservas, consulta por código, datos, seguridad y perfil deportivo. La cabecera global y la navegación competitiva móvil fueron estabilizadas junto con este cierre.


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

## Estado verificado

- Comunidad R3.3A–R3.3I cerrada en `3b1688f`, con sincronización autoritativa, accesibilidad, seguridad y concurrencia validadas.
- CI proporcional e integral manual en verde.
- CodeQL: 0 alertas abiertas de 5 detectadas.
- Secret Scanning: 0 secretos abiertos.
- npm audit: 0 vulnerabilidades conocidas al cierre.
- Inicio real completado y establecido como referencia visual autoritativa.
- Reservar cerrado en su alcance actual y Torneos en convergencia visual transversal U1.

## Calidad


<!-- W2-IDENTIDAD-VISUAL-INICIO -->
## Identidad visual compartida

Inicio W2 es la referencia visual autoritativa. El resto de las rutas conserva su composición funcional, pero comparte tipografía, jerarquía, paleta grafito/rojo, superficies, controles, navegación secundaria, estados, foco y responsive. Reservar aporta el patrón operativo; Torneos aporta el patrón competitivo; Resumen, Posiciones, Partidos y Cuadro definen la navegación secundaria global.

La especificación completa está en [Dirección visual W2](docs/DIRECCION_VISUAL_W2.md).
<!-- W2-IDENTIDAD-VISUAL-FIN -->
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

La dirección vigente es terminar y estabilizar primero la web W2 de escritorio. Después se desarrollará una aplicación móvil independiente, evaluando React Native, Flutter u otras alternativas mediante un vertical slice medido en dispositivos reales.

## Licencia

El código se publica para evaluación de portfolio. No se concede permiso para copiar, redistribuir, sublicenciar, vender ni explotar comercialmente el proyecto. Consultar [LICENSE](LICENSE).

## Estado autoritativo posterior al prototipo móvil

> Nota vigente desde el 25 de octubre de 2026: las secciones anteriores sobre Capacitor y CAP3 se conservan únicamente como historial técnico. El proyecto Android, las dependencias Capacitor, las capas nativas y sus pruebas fueron retirados del producto web activo en F0. La dirección vigente es finalizar primero la web de escritorio y desarrollar después una aplicación móvil independiente.
