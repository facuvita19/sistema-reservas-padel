# Plan de desarrollo

## 1. Propósito del documento

Este documento registra el estado real, la arquitectura, las decisiones funcionales, la metodología de trabajo y las próximas etapas del sistema de reservas para un complejo de pádel.

Su objetivo es permitir que el desarrollo continúe de forma ordenada y segura, incluso cuando sea necesario cambiar de conversación, entorno de trabajo o equipo. Debe actualizarse después de completar cada bloque funcional importante.

## 2. Objetivo general del sistema

Desarrollar una solución integral para administrar un complejo de pádel mediante componentes conectados a una misma lógica de negocio y una misma base de datos:

1. Aplicación administrativa de escritorio desarrollada en Java y JavaFX.
2. API pública para compartir funciones con la web y futuros clientes.
3. Web pública para registro, autenticación, reservas, torneos y autogestión de clientes.
4. Futura aplicación móvil conectada al mismo backend.

El sistema centraliza la gestión de canchas, clientes, reservas, pagos, caja, usuarios, solicitudes web, estadísticas, torneos e inscripciones.

## 3. Tecnologías y herramientas

- Java
- JavaFX
- Maven
- FXML
- CSS
- MySQL
- JDBC
- JUnit y pruebas automatizadas
- API HTTP pública en Java
- Astro
- TypeScript
- Tailwind CSS
- HTML, CSS y JavaScript
- Docker y Docker Compose
- Nginx
- Gmail SMTP con contraseña de aplicación
- Git y GitHub
- PowerShell
- Eclipse y Visual Studio Code

## 4. Arquitectura actual

### 4.1 Aplicación administrativa JavaFX

Ubicaciones principales:

```text
src/main/java/vista
src/main/java/vista/controlador
src/main/resources/fxml
src/main/resources/css
```

Módulos disponibles:

- Inicio de sesión administrativo.
- Dashboard.
- Agenda.
- Reservas.
- Canchas.
- Bloqueos de canchas.
- Clientes.
- Pagos.
- Caja y cierre diario.
- Estadísticas.
- Solicitudes web.
- Usuarios.
- Configuración del complejo.
- Administración de torneos y categorías.
- Bandeja y gestión de inscripciones a torneos.
- Vinculación automática y manual de jugadores con clientes.

### 4.2 API pública

Ubicación principal:

```text
src/main/java/api/publica
```

Responsabilidades actuales:

- Configuración pública del complejo.
- Consulta de canchas y disponibilidad.
- Creación y seguimiento de solicitudes de reserva.
- Autenticación y sesiones de clientes.
- Registro y cierre de sesión.
- Perfil e historial de reservas.
- Actualización de datos personales.
- Cambio de contraseña.
- Recuperación de contraseña por correo.
- Consulta pública de torneos y categorías.
- Inscripción pública de parejas.

### 4.3 Servicios, negocio y persistencia

Ubicaciones:

```text
src/main/java/servicio
src/main/java/negocio
src/main/java/dao
src/main/java/config
```

La aplicación utiliza:

- Entidades de negocio para clientes, reservas, pagos, caja, usuarios y torneos.
- Servicios para reglas de negocio y transacciones.
- DAO e implementaciones MySQL.
- Configuración mediante variables de entorno y propiedades.
- Sesiones de clientes almacenadas en base de datos.
- Contraseñas protegidas.
- Procesamiento automático de vencimientos.
- Correo SMTP para recuperación de acceso.

### 4.4 Web pública definitiva

Ubicación:

```text
web-publica-v2
```

Tecnología:

```text
Astro + TypeScript + Tailwind CSS + Nginx
```

Dirección definitiva local:

```text
http://localhost:5173
```

Páginas principales:

```text
/
/reservar
/torneos
/mis-reservas
/mis-datos
/restablecer
```

Funciones completadas:

- Portada pública adaptable.
- Consulta de canchas.
- Consulta de disponibilidad.
- Creación de solicitudes de reserva.
- Seguimiento por código.
- Datos de transferencia y acceso a WhatsApp.
- Registro de clientes.
- Inicio y cierre de sesión.
- Persistencia de sesión mediante cookie.
- Historial de reservas próximas y anteriores.
- Perfil del cliente.
- Cambio de nombre, apellido, teléfono y correo.
- Documento visible pero no editable.
- Posición preferida opcional: `DRIVE`, `REVES` o sin preferencia.
- Cambio de contraseña.
- Recuperación de contraseña por Gmail.
- Consulta pública de torneos, categorías, ramas y cupos.
- Inscripción pública de parejas con o sin sesión.
- Precarga de datos del cliente autenticado.
- Navegación directa y recarga de rutas mediante Nginx.
- Despliegue Docker de producción local.

La carpeta anterior:

```text
web-publica
```

se conserva temporalmente como referencia histórica, pero la web activa y definitiva es `web-publica-v2`.

### 4.5 Docker

Servicios definitivos:

```text
padel-mysql
padel-api
padel-web
```

Puertos locales:

```text
Web pública: http://localhost:5173
API pública: http://localhost:8080
MySQL:       localhost:3307
```

La web se construye desde:

```text
web-publica-v2/Dockerfile
```

Nginx utiliza:

```text
web-publica-v2/nginx.conf
```

Nginx sirve los archivos estáticos de Astro y redirige `/api/` al servicio `api` dentro de Docker.

### 4.6 Base de datos

Migraciones principales:

```text
database/02_configuracion_complejo.sql
database/03_cancelacion_administrativa.sql
database/04_historial_reprogramaciones.sql
database/05_gestion_usuarios.sql
database/06_cierre_diario_caja.sql
database/07_vencimiento_reservas_pendientes.sql
database/08_auditoria_reservas.sql
database/09_origen_reservas.sql
database/10_movimientos_caja.sql
database/11_usuario_sistema_web.sql
database/12_seguimiento_solicitudes_web.sql
database/13_instrucciones_pago_web.sql
database/14_cuentas_clientes_web.sql
database/15_recuperacion_password_clientes.sql
database/16_torneos_inscripciones.sql
database/17_posicion_preferida_clientes.sql
```

La migración 17 agrega al cliente:

```text
posicion_preferida: DRIVE, REVES o NULL
```

Las copias para inicialización de Docker se encuentran en:

```text
database/docker-init
```

## 5. Estado actual confirmado

### 5.1 Git

Rama:

```text
master
```

Último commit confirmado y publicado:

```text
7af6b88 Agregar nueva web pública, cuentas de clientes y mejoras de torneos
```

Sincronización:

```text
HEAD = master = origin/master
```

Estado del directorio de trabajo al cerrar el último bloque:

```text
limpio, sin cambios pendientes
```

Repositorio remoto:

```text
https://github.com/facuvita19/sistema-reservas-padel.git
```

### 5.2 Validaciones completadas

- `mvn clean test` correcto.
- `npm run build` correcto en `web-publica-v2`.
- `docker compose config` correcto.
- API, MySQL y web activos en Docker.
- Rutas públicas respondiendo correctamente.
- Inicio y cierre de sesión validados.
- Reserva pública validada.
- Historial y seguimiento validados.
- Perfil y posición preferida validados.
- Cambio de contraseña validado.
- Recuperación por correo Gmail validada.
- Enlace de recuperación y token de un solo uso validados.
- Torneos e inscripciones validados desde web y JavaFX.
- Administración de torneos y categorías validada.

## 6. Funciones completadas

### 6.1 Reservas y operación

- Consulta de disponibilidad.
- Solicitudes de reserva pública.
- Reservas administrativas.
- Vencimientos automáticos.
- Pagos y señas.
- Seguimiento público.
- Estados de reserva.
- Agenda y reprogramaciones.
- Bloqueos de canchas.
- Auditoría.
- Caja y cierre diario.

### 6.2 Cuentas de clientes

- Registro.
- Inicio y cierre de sesión.
- Cookies de sesión.
- Perfil.
- Historial de reservas.
- Vinculación de reservas.
- Actualización de datos.
- Posición preferida Drive o Revés.
- Cambio de contraseña.
- Recuperación de contraseña.
- Envío de correo por Gmail SMTP.

### 6.3 Torneos e inscripciones

#### Administración de torneos

- Creación en estado `BORRADOR`.
- Edición de datos generales.
- Período de inscripción.
- Reglamento.
- Conservación del usuario creador.
- Gestión de categorías.
- Rama, cupos y precio informativo.
- Cálculo de parejas confirmadas y cupos disponibles.
- Protección contra cupos inválidos.
- Desactivación controlada de categorías.

#### Ciclo de vida

```text
BORRADOR -> PUBLICADO
PUBLICADO -> INSCRIPCION_ABIERTA
INSCRIPCION_ABIERTA -> INSCRIPCION_CERRADA
INSCRIPCION_CERRADA -> EN_CURSO
EN_CURSO -> FINALIZADO
Estados no finales -> CANCELADO
```

#### Inscripciones

- Inscripción pública con o sin cuenta.
- Responsable autenticado precargado.
- Registro de ambos integrantes.
- Solicitudes en estado `PENDIENTE` y origen `WEB`.
- Bandeja administrativa.
- Búsqueda y filtros.
- Confirmación, rechazo, cancelación y lista de espera.
- Control de cupos.
- Prevención de duplicados.
- Observaciones administrativas.
- Acceso a WhatsApp.

#### Vinculación con clientes

- Normalización de teléfonos.
- Vinculación automática ante coincidencia única.
- Revisión ante coincidencias ambiguas.
- Vinculación manual.
- Cambio y desvinculación.
- Prevención del mismo cliente en ambos integrantes.
- Prevención de participación duplicada en una categoría.

#### Regla definitiva de pagos

El módulo de torneos no gestiona señas ni pagos.

Se eliminaron:

```text
importe_senia
PENDIENTE_PAGO
```

`precio_inscripcion` se conserva solamente como valor informativo.

## 7. Decisiones técnicas y operativas importantes

### 7.1 Sesiones en localhost

Las cookies se comparten por dominio y no por puerto. Durante pruebas entre `4321`, `4173` y `5173` pueden quedar sesiones inconsistentes.

Ante una sesión visualmente incorrecta:

1. Borrar datos de `localhost` en el navegador.
2. Cerrar pestañas de todos los puertos anteriores.
3. Abrir únicamente `http://localhost:5173`.
4. Iniciar sesión nuevamente.

### 7.2 Correo Gmail

Variables SMTP necesarias:

```text
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=<correo Gmail>
SMTP_PASSWORD=<contraseña de aplicación sin espacios>
SMTP_FROM_EMAIL=<correo Gmail>
SMTP_FROM_NAME=Padel Reservas
SMTP_STARTTLS=true
SMTP_SSL=false
SMTP_TIMEOUT_MS=10000
```

La URL pública local definitiva es:

```text
API_WEB_PUBLICA_URL=http://localhost:5173/
```

La contraseña de aplicación y las demás credenciales deben permanecer fuera de Git.

### 7.3 Configuración de base para JavaFX

Para conectar la administración a MySQL Docker:

```powershell
.\usar-base-docker.ps1
.\ver-base-configurada.ps1
```

Resultado esperado:

```text
Destino: MySQL Docker
Puerto: 3307
```

Para volver a la base local habitual:

```powershell
.\usar-base-local.ps1
.\ver-base-configurada.ps1
```

Siempre se debe cerrar la aplicación administrativa antes de cambiar de base.

## 8. Próximo módulo aprobado: cuadros y partidos de torneos

### 8.1 Objetivo

Incorporar la programación y gestión deportiva de los torneos después del cierre de inscripciones.

El módulo abarcará:

1. Modelo y migración de partidos.
2. Generación del cuadro por categoría.
3. Sorteo manual o aleatorio de parejas.
4. Manejo de lugares libres mediante `BYE`.
5. Programación de fecha, hora y cancha.
6. Registro de resultados por sets.
7. Determinación y avance automático de ganadores.
8. Administración desde JavaFX.
9. Consulta pública del cuadro desde la web.
10. Pruebas automatizadas y manuales.

### 8.2 Primera versión funcional

La primera versión usará eliminación directa.

Fases previstas:

```text
DIECISEISAVOS
OCTAVOS
CUARTOS
SEMIFINAL
FINAL
```

Estados previstos:

```text
PENDIENTE
PROGRAMADO
EN_CURSO
FINALIZADO
CANCELADO
```

Cada partido debería registrar como mínimo:

```text
Torneo
Categoría
Fase
Número u orden del partido
Pareja 1
Pareja 2
Fecha
Hora
Cancha
Estado
Pareja ganadora
Partido siguiente
Posición en el partido siguiente
```

### 8.3 Resultados por sets

El resultado no debe guardarse como texto libre.

Ejemplo:

```text
Set 1: 6-4
Set 2: 3-6
Set 3: 10-7
```

La estructura deberá permitir:

- Validar el ganador.
- Admitir partidos a dos sets con un tercer set o super tie-break configurable.
- Mostrar resultados claramente.
- Avanzar automáticamente a la pareja ganadora.
- Calcular estadísticas en una etapa posterior.

### 8.4 Funciones posteriores

Después de estabilizar eliminación directa se podrán incorporar:

- Fase de grupos.
- Cabezas de serie.
- Ranking.
- Reprogramaciones.
- Estadísticas deportivas.
- Notificaciones.
- Configuración de formato por categoría.

### 8.5 Primer paso técnico exacto

Antes de crear la migración 18:

1. Buscar clases existentes relacionadas con partidos, cuadros, encuentros o resultados.
2. Revisar entidades actuales de torneo, categoría e inscripción.
3. Revisar la migración 16.
4. Confirmar nombres de tablas, claves y estados actuales.
5. Definir el diseño del cuadro sin duplicar estructuras.
6. Crear la migración 18 y sus pruebas en un bloque controlado.

## 9. Etapas futuras

### Etapa A: cuadros y partidos

- Modelo de partidos.
- Cuadro de eliminación directa.
- Programación.
- Resultados.
- Avance automático.
- JavaFX.
- Web pública.
- Pruebas.

### Etapa B: pulido visual

- Modernizar diálogos JavaFX.
- Mejorar buscadores.
- Mejorar estados vacíos.
- Mejorar selector de tema e iconos.
- Revisar accesibilidad y navegación por teclado.
- Unificar mensajes de éxito, advertencia y error.

### Etapa C: estabilización general

- Revisar consola de JavaFX.
- Revisar consola y red del navegador.
- Probar todos los flujos con y sin sesión.
- Probar reservas simultáneas.
- Verificar estados vacíos y errores controlados.
- Ampliar cobertura automatizada.

### Etapa D: seguridad

- Revisar autenticación y autorización administrativa.
- Revisar sesiones de clientes.
- Revisar expiración y revocación.
- Proteger endpoints por rol y contexto.
- Mantener secretos fuera del repositorio.
- Revisar auditoría de operaciones críticas.
- Preparar HTTPS y cookies seguras para producción.

### Etapa E: despliegue

- Separar configuración de desarrollo y producción.
- Versionar todas las migraciones.
- Definir copias de seguridad y restauración.
- Publicar API y web.
- Configurar dominio y HTTPS.
- Configurar correo de producción.
- Incorporar monitoreo y registros.
- Documentar actualización y recuperación.

### Etapa F: aplicación móvil

- Definir tecnología.
- Reutilizar API y reglas de negocio.
- Registro e inicio de sesión.
- Disponibilidad y reservas.
- Historial y perfil.
- Torneos, cuadros y resultados.
- Notificaciones cuando exista infraestructura.

## 10. Metodología segura de trabajo

Cada cambio debe seguir este orden:

1. Describir el problema u objetivo.
2. Reproducir el estado actual.
3. Revisar Git.
4. Identificar archivos involucrados.
5. Evitar modificar archivos basándose en suposiciones.
6. Aplicar un cambio pequeño y controlado.
7. Crear copia de seguridad cuando corresponda.
8. Verificar el contenido modificado.
9. Compilar.
10. Ejecutar pruebas automatizadas.
11. Probar manualmente.
12. Revisar consola y registros.
13. Confirmar que no se rompieron funciones anteriores.
14. Limpiar archivos temporales.
15. Crear un commit simple y descriptivo.
16. Actualizar este documento.
17. Publicar en GitHub únicamente después de revisar el commit.

## 11. Criterios de finalización

Una tarea se considera terminada cuando:

- El código compila.
- Las pruebas pasan.
- La función fue probada manualmente.
- No aparecen errores inesperados.
- No se rompieron flujos existentes.
- La base de datos quedó en el estado esperado.
- Docker funciona si el cambio afecta el despliegue.
- Los archivos temporales fueron revisados.
- Los cambios quedaron respaldados en Git.
- El estado quedó documentado.

## 12. Convenciones de Git

### Mensajes

Usar mensajes simples y descriptivos, sin prefijos innecesarios.

Ejemplos:

```text
Agregar cuadros y partidos de torneos
Agregar programación y resultados de partidos
Publicar cuadros de torneos en la web
Corregir avance automático de ganadores
```

### Antes de cada commit

```powershell
git status -sb
git --no-pager diff --stat
git --no-pager diff
mvn clean test
```

Si cambia la web:

```powershell
cd .\web-publica-v2
npm run build
cd ..
```

Si cambia Docker:

```powershell
docker compose config --quiet
docker compose ps
```

### Publicación

```powershell
git push origin HEAD
```

No ejecutar el push automáticamente como parte de una corrección. Primero revisar el commit y el estado del repositorio.

## 13. Resumen de continuidad

### Último cambio confirmado

Nueva web pública en Astro, cuentas de clientes, recuperación por Gmail, posición Drive o Revés, mejoras administrativas y torneos integrados.

### Estado Git

```text
Rama: master
Commit: 7af6b88
Remoto: origin/master
Directorio de trabajo: limpio
```

### Servicios definitivos

```text
Web:  http://localhost:5173
API:  http://localhost:8080
MySQL: localhost:3307
```

### Próximo bloque

```text
Cuadros, partidos, programación y resultados de torneos
```

### Primer paso al retomar

Ejecutar búsquedas de clases, tablas y referencias existentes relacionadas con:

```text
Partido
Cuadro
Resultado
Encuentro
Clasificado
Semifinal
Final
```

Luego revisar `Torneo`, `TorneoCategoria`, las inscripciones confirmadas y la migración 16 antes de diseñar la migración 18.
