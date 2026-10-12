# Plan de desarrollo del sistema de reservas de pádel

> Fuente de verdad estratégica del proyecto. Actualizado el 12 de octubre de 2026 sobre la rama `master`, después del commit `c1e6ab4` (`Agregar prueba SMTP controlada`).

## 1. Punto actual de continuidad

### Estado ejecutivo

El sistema comercial ya dispone de una base funcional amplia, una aplicación administrativa modernizada, una web pública premium reconstruida, API independiente, entorno Docker reproducible, pruebas automatizadas, seguridad operativa, backups verificados, restauración ensayada, despliegue controlado, rollback, monitoreo y correo SMTP real.

Estado técnico confirmado:

- Rama estable: `master`.
- Commit de referencia al reconstruir este plan: `c1e6ab4`.
- CI: cuatro trabajos verdes en GitHub Actions.
- Java: 250 pruebas, sin fallos.
- Web: Astro y TypeScript validados.
- E2E: Playwright integrado y estable.
- Docker: instalación limpia validada desde cero.
- Base restaurada en MySQL temporal: 26 tablas verificadas.
- SMTP: envío técnico aceptado y recibido.
- Monitor local: estado `SALUDABLE`.
- Auditoría de secretos: sin hallazgos de alta confianza.

### Próximo objetivo inmediato

1. Cerrar el bloque 10D con auditoría final, backup final, restauración temporal final y validación de despliegue.
2. Crear y publicar una etiqueta estable una vez que el repositorio y CI vuelvan a quedar limpios y verdes.
3. Retomar evolución funcional y experiencia de producto usando la hoja de ruta priorizada de este documento.

### Archivos que deben leerse al retomar

1. `PLAN_DESARROLLO.md`.
2. `docs/CONTINUIDAD_DEL_PROYECTO.md`.
3. `README.md`.
4. `docs/ARQUITECTURA.md`.
5. `docs/OPERACION_DIARIA.md`.
6. `docs/SEGURIDAD_OPERATIVA.md`.
7. `docs/BACKUP_Y_RESTAURACION.md`.
8. `docs/DESPLIEGUE_Y_ROLLBACK.md`.
9. Últimos commits de `master` y última ejecución de CI.

## 2. Propósito y alcance comercial

El producto administra integralmente un complejo de pádel y conecta la operación interna con clientes y público general.

Alcance principal:

- canchas, horarios, precios, disponibilidad, orden visual y bloqueos;
- clientes registrados y clientes ocasionales;
- reservas administrativas y solicitudes web;
- pagos, señas, caja, reembolsos y cierres diarios;
- usuarios, roles y permisos;
- configuración del complejo;
- torneos, categorías, inscripciones, grupos, partidos, cuadros y resultados;
- estadísticas operativas, financieras, comerciales y web;
- cuentas de clientes, reservas propias y recuperación de contraseña;
- comunicación mediante WhatsApp y correo;
- operación reproducible, auditable, respaldable y recuperable.

La prioridad del proyecto es mantener un estándar premium, profesional, moderno e imponente. La calidad de experiencia, claridad e integridad funcional tienen prioridad sobre minimizar el costo de implementación.

## 3. Arquitectura vigente

### 3.1 Aplicación administrativa

- Java 25, Maven y JavaFX.
- Capas de controladores, servicios, negocio, DAO y utilidades.
- 17 vistas FXML administrativas.
- Apertura posterior al login en pantalla completa.
- Navegación y contenido adaptados por rol.
- Paleta grafito y verde, iconografía SVG y tipografía unificada.
- Gestión de clientes como referencia visual para nuevas pantallas.

### 3.2 API pública

- Servicio Java independiente en el puerto interno `8080`.
- Acceso local publicado en `127.0.0.1:8080`.
- Acceso web habitual mediante Nginx y rutas `/api/`.
- CORS configurable, límites, cabeceras de seguridad y control de errores.
- Cookies de sesión `HttpOnly`, `SameSite=Lax` y `Secure` obligatorio en producción HTTPS.
- Tokens de sesión y recuperación almacenados mediante hash.
- Procesador independiente de vencimientos.

### 3.3 Web pública vigente

- Código activo: `web-publica-v2`.
- Astro 7, TypeScript 6, Node 22 y Nginx.
- Puerto local Docker: `4321`.
- Responsive para escritorio y móvil.
- Portada, reserva, seguimiento, autenticación, cuenta, reservas y portal competitivo.
- CSP y cabeceras de seguridad configuradas.

`web-publica` queda solo como referencia histórica. No deben agregarse funciones nuevas allí.

### 3.4 Base de datos

- MySQL 8.4.
- Base canónica: `padel_reservas`.
- Exposición local: `127.0.0.1:3307`.
- Esquema base: `database/schema.sql`.
- 27 migraciones conservadas y ordenadas.
- Inicialización Docker: `database/docker-init` versionado.
- Migraciones 10 y 26 idempotentes para instalaciones limpias.
- Healthcheck final verifica elementos críticos del esquema.

### 3.5 Contenedores y operación

- MySQL, API y web gestionados por Docker Compose.
- API ejecutada con usuario no privilegiado.
- `no-new-privileges`, límites de CPU, memoria y procesos.
- Rotación de logs.
- API con filesystem de solo lectura y `tmpfs` limitado.
- Entornos temporales aislados para instalación y restauración.

## 4. Capacidades terminadas

### 4.1 Administración y seguridad de acceso

- Login modernizado con cabecera oscura.
- Roles ADMINISTRADOR y OPERADOR.
- OPERADOR sin Estadísticas ni resúmenes financieros restringidos.
- Inicio y navegación adaptados al rol.
- Gestión segura de usuarios, activación, roles y contraseñas.

### 4.2 Canchas, agenda y bloqueos

- Alta, edición, desactivación y reactivación segura.
- Horarios, duración, precio, días disponibles y orden visual.
- Agenda diaria y visualización de ocupación.
- Bloqueos por cancha y período.
- Prevención de superposiciones.

### 4.3 Clientes

- Gestión de clientes registrada y modernizada.
- Búsqueda, filtros, tabla, detalle, historial y métricas.
- Selección de clientes registrados en altas administrativas.
- Clientes ocasionales en reservas.
- Posición preferida de pádel.
- Gestión de clientes como referencia visual administrativa.

### 4.4 Reservas y solicitudes web

- Creación, reprogramación, cancelación, ausencia y finalización.
- Reservas con cliente registrado u ocasional.
- Auditoría e historial de reprogramaciones.
- Solicitudes web con vencimiento y expiración automática.
- Código de seguimiento público.
- Prevención de doble envío y concurrencia.
- WhatsApp e instrucciones de pago.
- Origen personal o web.

### 4.5 Pagos y caja

- Registro y acreditación de pagos.
- Señas, saldos, medios de pago y estados.
- Movimientos de caja, reembolsos y cierre diario.
- Paneles y filtros modernizados.

### 4.6 Torneos e inscripciones

- Torneos, categorías, premios, formatos y publicación.
- Alta administrativa y web de parejas.
- Selección de clientes registrados y jugadores no vinculados.
- Responsable y segundo integrante independientes.
- Estados Pendiente, Confirmada, Lista de espera, Rechazada y Cancelada.
- Reglas de duplicados por estados activos y finales.
- Confirmación contextual antes de ocupar cupos.
- Pagos asociados a inscripciones.

### 4.7 Fase de grupos

- Grupos de tres y cuatro parejas.
- Sorteo con un cabeza de serie por grupo y distribución aleatoria del resto.
- Configuración completamente manual.
- Generación de partidos.
- Resultados, correcciones y posiciones.
- Criterios deportivos de ranking y desempate.

### 4.8 Eliminatorias y resultados

- Fases de acceso y cuadro principal.
- Propuesta, confirmación y edición manual de estructura.
- Llave gráfica completa.
- Programación, sets, resultados y avance automático.
- Enlaces ganador/perdedor entre partidos.
- Historial administrativo de correcciones.
- Publicación web y exportación PDF.

Principios deportivos vigentes:

- mismas reglas para clasificados de igual posición;
- ningún clasificado válido queda excluido por conveniencia del cuadro;
- ranking entre grupos usado solo con reglas explícitas;
- fases de acceso completan el cuadro principal;
- integridad del cuadro validada antes de publicar.

### 4.9 Estadísticas

- Selector por sección.
- Resumen, Reservas, Finanzas, Clientes, Canchas y Web.
- Métricas, gráficos, tablas y ayuda contextual.
- Guía PDF.
- Acceso restringido por rol.

### 4.10 Web pública y cuentas

- Portada premium y disponibilidad en tiempo real.
- Reserva guiada por cancha, fecha y horario.
- Seguimiento público.
- Registro, login, logout y sesión persistente.
- Perfil, cambio de contraseña y reservas propias.
- Recuperación por correo con token protegido, vencimiento y uso único.
- Portal de torneos: catálogo, detalle, categorías, reglamento e inscripción.
- Centro competitivo: resumen, posiciones, partidos y cuadro.
- Navegación y experiencia móvil completas.

## 5. Calidad, seguridad y operación

### 5.1 Pruebas y CI

- 250 pruebas Java.
- Build de Astro y TypeScript.
- Playwright E2E para flujos públicos críticos.
- Instalación Docker limpia con base temporal.
- Cuatro trabajos obligatorios en GitHub Actions:
  - Java 25 y Maven;
  - Astro y TypeScript;
  - Playwright E2E;
  - Instalación Docker limpia.
- Auditoría de secretos de alta confianza.
- Validación sintáctica de scripts operativos.

### 5.2 Configuración segura

- `.env` privado e ignorado por Git.
- Preflight `validar-configuracion.ps1`.
- Rechazo de valores vacíos o de ejemplo.
- Modo local y modo producción.
- Salidas redactadas.
- Prohibido compartir `docker compose config` expandido.

### 5.3 Backup y recuperación

- Backup SQL estructuralmente validado.
- Checksum SHA-256 asociado.
- Retención predeterminada: 30 días y mínimo 7 copias.
- Restauración temporal real sin afectar la base principal.
- 26 tablas verificadas en la restauración.

### 5.4 Despliegue y rollback

- Repositorio limpio obligatorio.
- Preflight previo.
- Backup obligatorio.
- Build y despliegue Docker.
- Validación posterior de servicios, API, web y CSP.
- Registro local del commit y backup.
- Rollback guiado con restauración de base opcional y confirmada.

### 5.5 Observabilidad y correo

- Monitor `monitorear-salud.ps1` con estado `SALUDABLE` o `DEGRADADO`.
- Informe local `.estado-salud.json` no versionado.
- Prueba SMTP manual con confirmación `ENVIAR`.
- Envío SMTP real confirmado el 12 de octubre de 2026.
- CI valida la sintaxis, pero nunca envía correos.

## 6. Decisiones técnicas vigentes

- `master` es la rama estable actual.
- `web-publica-v2` es la web vigente.
- Puerto web local: `4321`.
- Puerto API local: `8080`.
- Puerto MySQL local: `127.0.0.1:3307`.
- La administración, API y web usan la misma base Docker.
- `database/docker-init` debe seguir versionado.
- Las migraciones históricas se conservan y no se excluyen para ocultar duplicados.
- Los cambios de esquema deben ser compatibles con instalación limpia y actualización.
- No se modifica ni destruye la base principal durante pruebas.
- Los entornos temporales tienen nombres, puertos, redes y volúmenes aislados.
- HSTS solo se habilita cuando exista HTTPS real.
- Gestión de clientes es la referencia visual para nuevas vistas administrativas.
- El estándar visual es premium, grafito y verde, con SVG compactos y estados consistentes.

## 7. Deuda técnica y riesgos reales

### Prioridad alta

- Completar el cierre formal de producción y crear una etiqueta estable.
- Definir infraestructura real de dominio, DNS, TLS y hosting.
- Mantener una copia de backups fuera del equipo principal.
- Programar backup y monitor en el entorno donde se despliegue.

### Prioridad media

- Revisar dependencias de forma controlada, una familia por vez.
- Resolver la futura carga dinámica del agente Mockito antes de que Java la bloquee por defecto.
- Revisar advertencias de API deprecada y operaciones unchecked sin mezclarlo con cambios funcionales.
- Evaluar retiro definitivo de `web-publica` después de confirmar que no exista dependencia operativa.
- Aumentar cobertura Java especialmente en autenticación, cuentas, torneos y persistencia.

### Prioridad de producto

- Mantener una revisión funcional integral por rol y módulo.
- Continuar mejoras premium cuando aporten claridad o velocidad real.
- Revisar futuras necesidades móviles solo después de estabilizar producción web.
- Considerar notificaciones operativas externas cuando exista infraestructura de producción.

### Riesgos a vigilar

- diferencias Windows/Linux en scripts;
- interpolación entre JavaScript y PowerShell;
- cambios simultáneos en esquema y migraciones;
- publicación accidental de secretos;
- regresiones responsive y de teclado;
- reglas deportivas complejas en cuadros mixtos;
- restauraciones sobre la base incorrecta;
- documentación desactualizada después de bloques grandes.

## 8. Hoja de ruta priorizada

### Fase 1: cierre estable de producción

Objetivo: marcar una versión reproducible y recuperable.

- CI verde para el último commit.
- Git limpio y sincronizado.
- Auditoría de secretos aprobada.
- Monitor saludable.
- Backup final con checksum.
- Restauración temporal final.
- Validación de despliegue.
- Documentación actualizada.
- Etiqueta estable publicada.

### Fase 2: infraestructura productiva real

Objetivo: publicar con dominio y HTTPS.

- Proveedor y servidor definidos.
- DNS configurado.
- TLS válido y renovación automática.
- Origen CORS exacto.
- Cookies seguras.
- HSTS después de verificar HTTPS.
- Secretos gestionados fuera del repositorio.
- Backups externos y tarea programada.
- Monitor y alertas programados.
- Ensayo de despliegue y rollback en infraestructura real.

### Fase 3: revisión funcional integral

Orden recomendado:

1. login, usuarios y roles;
2. inicio por rol;
3. configuración;
4. canchas y bloqueos;
5. agenda y reservas;
6. clientes;
7. pagos y caja;
8. solicitudes web;
9. torneos e inscripciones;
10. grupos, cuadro y resultados;
11. estadísticas;
12. web pública y cuentas.

Cada módulo debe revisar caminos felices, errores, estados vacíos, permisos, concurrencia, responsive y recuperación.

### Fase 4: deuda técnica controlada

- Cambios pequeños y medibles.
- Sin refactorizaciones masivas sin pruebas.
- Eliminar código solo con evidencia de no uso.
- Consolidar estilos solo si no afecta módulos funcionales.
- Registrar cada deuda cerrada en este plan.

### Fase 5: evolución de producto

- Alertas operativas externas.
- Analítica histórica avanzada.
- Automatizaciones comerciales.
- Experiencia móvil nativa o PWA si existe necesidad validada.
- Integraciones de pago o facturación según el negocio real.

## 9. Criterios de aceptación

### Para cualquier bloque

Una tarea se considera terminada cuando:

1. el alcance está definido;
2. los cambios son coherentes con arquitectura y roles;
3. se validó sintaxis;
4. `git diff --check` aprueba;
5. las pruebas aplicables aprueban;
6. se realizó comprobación visual o funcional;
7. no se expusieron secretos;
8. el commit contiene solo los archivos previstos;
9. el push está sincronizado;
10. GitHub Actions queda verde;
11. los temporales se eliminan;
12. se actualiza el punto de continuidad si el bloque es significativo.

### Para producción

Además:

- backup previo verificado;
- rollback definido;
- dominio, HTTPS y CORS correctos;
- monitor saludable;
- smoke test posterior;
- responsable operativo identificado;
- documentación final actualizada.

## 10. Historial resumido de hitos

- 30 de septiembre a 2 de octubre de 2026: torneos, inscripciones, grupos y eliminatorias.
- 3 a 6 de octubre: modernización administrativa, estadísticas, pagos, caja y diálogos.
- 8 y 9 de octubre: cierre visual de módulos, tipografía, solicitudes, torneos e inscripciones.
- 9 a 11 de octubre: reconstrucción premium completa de `web-publica-v2`, cuentas y portal competitivo.
- 11 y 12 de octubre: accesibilidad, responsive, Playwright y CI.
- 12 de octubre: Docker reproducible, seguridad operativa, backups, restauración, hardening, despliegue, rollback, monitoreo, auditoría y SMTP.

Tags históricos existentes:

- `web-publica-estable`;
- `cuentas-clientes-bloque-1`;
- `cuentas-clientes-bloque-2`;
- `cuentas-clientes-reservas`;
- `cuentas-clientes-perfil`;
- `cuentas-clientes-seguridad`;
- `cuentas-clientes-recuperacion`;
- `cuentas-clientes-correo`.

## 11. Elementos retirados u obsoletos

- Puerto web `5173`: reemplazado por `4321` en la operación vigente.
- `web-publica`: referencia histórica; desarrollo activo en `web-publica-v2`.
- Node 20 para acciones oficiales: reemplazado por acciones compatibles con Node 24.
- Uso compartible de `docker compose config`: prohibido porque expande secretos; usar `--quiet`.
- `mysqladmin ping` como prueba de inicialización completa: insuficiente; usar consulta autenticada y verificaciones de esquema.
- Migraciones excluidas para evitar duplicados: estrategia descartada; deben ser idempotentes.
- Parches regex amplios sobre métodos Java: evitar; usar límites estructurales y validación.

## 12. Mantenimiento de este documento

Actualizar este plan cuando ocurra alguno de estos eventos:

- cierre de una fase relevante;
- nueva arquitectura o servicio;
- cambio de herramienta o puerto;
- nueva decisión de seguridad;
- módulo terminado o retirado;
- nueva deuda prioritaria;
- release estable.

La actualización debe realizarse después del commit y push, usando hashes reales. Los detalles operativos de colaboración y cambio de conversación se mantienen en `docs/CONTINUIDAD_DEL_PROYECTO.md`.
