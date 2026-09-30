# Plan de desarrollo

## 1. Propósito del documento

Este documento registra el estado real, la arquitectura, la metodología de trabajo y las próximas etapas del sistema de reservas para un complejo de pádel.

Su objetivo es permitir que el desarrollo continúe de forma ordenada y segura, incluso cuando sea necesario cambiar de conversación, entorno de trabajo o equipo. Debe actualizarse después de completar cada bloque funcional importante.

## 2. Objetivo general del sistema

Desarrollar una solución integral para administrar un complejo de pádel mediante componentes conectados a una misma lógica de negocio y una misma base de datos:

1. Una aplicación administrativa de escritorio desarrollada en Java y JavaFX.
2. Una API pública para compartir las funciones necesarias con otros clientes.
3. Una web pública para registro, autenticación, consulta y reserva de turnos.
4. Una futura aplicación móvil conectada al mismo backend.

El sistema debe centralizar la gestión de canchas, clientes, reservas, pagos, caja, usuarios, solicitudes web, estadísticas, torneos e inscripciones.

## 3. Tecnologías y herramientas conocidas

- Java
- JavaFX
- Maven
- FXML
- CSS
- MySQL
- JDBC
- JUnit y pruebas automatizadas
- API pública en Java
- HTML, CSS y JavaScript para la web pública
- Docker y Docker Compose
- Nginx para servir la web pública
- Git para control de versiones
- Eclipse como entorno principal de desarrollo
- PowerShell para automatizar cambios y verificaciones

## 4. Arquitectura actual

### 4.1 Aplicación administrativa de escritorio

La aplicación JavaFX contiene las pantallas administrativas y utiliza controladores, servicios, DAO y entidades de negocio.

Módulos registrados actualmente:

- Inicio de sesión administrativo
- Dashboard
- Agenda
- Reservas
- Canchas
- Bloqueos de canchas
- Clientes
- Pagos
- Caja y cierre diario
- Estadísticas
- Solicitudes web
- Usuarios
- Configuración del complejo
- Torneos e inscripciones, actualmente en desarrollo

### 4.2 Capas del proyecto Java

#### API pública

Ubicación principal:

```text
src/main/java/api/publica
```

Responsabilidades:

- Exponer disponibilidad y solicitudes de reserva.
- Gestionar autenticación y sesiones de clientes.
- Gestionar el perfil de clientes.
- Gestionar recuperación de contraseña.
- Incorporar consultas e inscripciones públicas a torneos.

#### Configuración

Ubicación principal:

```text
src/main/java/config
```

Responsabilidades:

- Configurar la conexión con la base de datos.
- Configurar el correo electrónico.
- Preparar el usuario administrador inicial.

#### Acceso a datos

Ubicación principal:

```text
src/main/java/dao
```

Responsabilidades:

- Definir interfaces DAO.
- Implementar persistencia MySQL.
- Separar el acceso a datos de la lógica de negocio.

#### Negocio

Ubicación principal:

```text
src/main/java/negocio
```

Responsabilidades:

- Representar las entidades y estados del dominio.
- Modelar reservas, clientes, canchas, pagos, caja, usuarios y torneos.

#### Servicios

Ubicación principal:

```text
src/main/java/servicio
```

Responsabilidades:

- Aplicar reglas de negocio.
- Coordinar DAO y procesos.
- Gestionar reservas, vencimientos, pagos, caja, autenticación, correo, clientes y torneos.

#### Interfaz JavaFX

Ubicaciones principales:

```text
src/main/java/vista
src/main/java/vista/controlador
src/main/resources/fxml
src/main/resources/css
```

Responsabilidades:

- Navegación entre pantallas.
- Controladores JavaFX.
- Vistas FXML.
- Estilos visuales.
- Tema dinámico de la aplicación.

### 4.3 Web pública

Ubicación:

```text
web-publica
```

Componentes conocidos:

- `index.html`
- `app.js`
- `styles.css`
- `manifest.webmanifest`
- `service-worker.js`
- `offline.html`
- Iconos de la aplicación web
- Configuración de Nginx
- Script de inicio para PowerShell

Funciones desarrolladas:

- Consulta de disponibilidad.
- Creación de solicitudes de reserva.
- Confirmación y seguimiento de solicitudes.
- Registro e inicio de sesión de clientes.
- Persistencia de sesión.
- Perfil del cliente.
- Actualización de datos personales.
- Historial y vinculación de reservas.
- Cambio de contraseña.
- Recuperación de contraseña por correo.
- Soporte de instalación y funcionamiento como aplicación web progresiva.
- Consulta e inscripción pública a torneos, actualmente en desarrollo.

### 4.4 Base de datos

Ubicación de migraciones:

```text
database
```

Migraciones registradas:

- Configuración del complejo.
- Cancelación administrativa.
- Historial de reprogramaciones.
- Gestión de usuarios.
- Cierre diario de caja.
- Vencimiento de reservas pendientes.
- Auditoría de reservas.
- Origen de reservas.
- Movimientos de caja.
- Usuario del sistema web.
- Seguimiento de solicitudes web.
- Instrucciones de pago web.
- Cuentas de clientes web.
- Recuperación de contraseña de clientes.

Migración local todavía no confirmada:

```text
database/16_torneos_inscripciones.sql
```

### 4.5 Pruebas

El proyecto contiene pruebas para:

- API pública y seguridad.
- Disponibilidad pública.
- Integración de reservas con MySQL.
- Entidades de reserva.
- Agenda.
- Bloqueos.
- Canchas.
- Caja.
- Clientes.
- Configuración.
- Dashboard.
- Estadísticas.
- Mensajes de reservas.
- Pagos y confirmación de señas.
- Políticas de reserva.
- Vencimientos automáticos.
- Estados y operaciones de reservas.
- Usuarios.
- WhatsApp.
- Protección de contraseñas.
- Servicios de torneos e inscripciones, actualmente en desarrollo local.
- Normalización de teléfonos y vinculación con clientes, actualmente en desarrollo local.

## 5. Desarrollo completado y respaldado en Git

### 5.1 Aplicación administrativa

- Gestión de usuarios.
- Agenda y mejoras de uso.
- Estadísticas avanzadas.
- Gestión de solicitudes web.
- Caja operativa.
- Gestión de pagos.
- Vencimientos automáticos de reservas pendientes.
- Auditoría y seguimiento operativo.

### 5.2 Web pública

- API pública.
- Web de reservas.
- Seguridad básica de la API.
- Entorno Docker.
- Mejoras visuales y de experiencia.
- Fechas y horarios disponibles.
- Flujo guiado de reservas.
- Confirmaciones y preguntas frecuentes.
- Funcionamiento offline y recursos de PWA.

### 5.3 Calidad y operación

- Pruebas de reservas y vencimientos.
- Pruebas de integración con MySQL.
- Pruebas de API y seguridad.
- Separación y automatización de pruebas de integración.
- Documentación operativa.

### 5.4 Cuentas de clientes

Los siguientes bloques están confirmados en siete commits locales, todavía no publicados en `origin/master`:

1. Autenticación y sesiones para clientes.
2. Perfil e historial de reservas.
3. Vinculación de reservas con cuentas de clientes.
4. Perfil y reservas vinculadas.
5. Perfil y seguridad de cuentas.
6. Recuperación de contraseña.
7. Recuperación de contraseña por correo.

## 6. Estado actual de Git

### 6.1 Rama

```text
master
```

### 6.2 Commit actual

```text
5f302714a31980634a02b1d337221217ab028dad
```

Mensaje:

```text
Agregar recuperación de contraseña por correo
```

Etiqueta:

```text
cuentas-clientes-correo
```

### 6.3 Diferencia con el repositorio remoto

La rama local está siete commits por delante de `origin/master`:

```text
master...origin/master [ahead 7]
```

El último commit conocido de `origin/master` es:

```text
53b4f37 Mejorar confirmación, preguntas frecuentes e interacción de horarios
```

Etiqueta:

```text
web-publica-estable
```

### 6.4 Precaución importante

El módulo de torneos y otras correcciones recientes todavía contienen archivos modificados y archivos nuevos sin commit.

Hasta respaldar correctamente ese trabajo, no ejecutar comandos destructivos como:

```text
git reset --hard
git clean -fd
git checkout -- .
git restore .
```

Tampoco cambiar de rama sin proteger antes los cambios actuales.

## 7. Módulo de torneos e inscripciones en desarrollo

### 7.1 Elementos implementados localmente

#### Dominio

- Torneo.
- Categoría de torneo.
- Inscripción a torneo.
- Jugadores de una inscripción.
- Estado del torneo.
- Estado de la inscripción.
- Rama del torneo.
- Origen de la inscripción.
- Tipo de vinculación con clientes.

#### Persistencia

- DAO de torneos.
- DAO de categorías.
- DAO de inscripciones.
- DAO de jugadores por inscripción.
- Implementaciones MySQL.
- Migración `16_torneos_inscripciones.sql`.

#### Servicios

- Consulta de inscripciones.
- Gestión administrativa de inscripciones.
- Inscripción a torneos desde la web.
- Vinculación de inscripciones con clientes por teléfono.
- Normalización de teléfonos.

#### API y web pública

- DTO de torneos públicos.
- DTO de inscripción pública.
- Handler de torneos públicos.
- Servicio de torneos públicos.
- Endpoint de inscripción.
- Consulta pública de torneos.

#### Aplicación JavaFX

- Controlador de torneos e inscripciones.
- Vista FXML de inscripciones.
- Hoja de estilos específica.
- Integración con navegación y dashboard.

#### Pruebas

- Consulta de inscripciones.
- Gestión de inscripciones.
- Inscripción desde la web.
- Vinculación de clientes por teléfono.
- Normalización de teléfonos.
- Ajustes de mocks y manejo de `SQLException`.

### 7.2 Decisiones funcionales observadas

- Los clientes pueden manejar más de un teléfono.
- Los teléfonos deben normalizarse antes de comparar o vincular datos.
- La inscripción web debe poder vincular jugadores con clientes existentes.
- El módulo de torneos no debe depender de una seña si esa regla fue eliminada del flujo.
- La aplicación administrativa necesita una bandeja para revisar y gestionar inscripciones.

### 7.3 Problema actual conocido

La bandeja administrativa informa:

```text
No se pudieron recuperar las inscripciones de torneos.
```

Este problema debe tratarse como independiente de la corrección de codificación del dashboard.

### 7.4 Corrección visual completada

Se repararon textos dañados por codificación UTF-8 en:

```text
src/main/resources/fxml/dashboard-admin.fxml
```

Los textos verificados incluyen:

```text
INGRESOS DEL DÍA
Cerrar caja del día
```

## 8. Próximo paso técnico

Investigar el error de recuperación de inscripciones sin modificar otros módulos hasta encontrar la causa.

Orden recomendado:

1. Reproducir el error abriendo la bandeja de torneos.
2. Copiar el error completo y la traza de la consola.
3. Confirmar que `database/16_torneos_inscripciones.sql` se aplicó en la base utilizada por la aplicación.
4. Verificar las tablas y columnas creadas por la migración.
5. Revisar `TorneoInscripcionDAOMySQL` y consultas relacionadas.
6. Revisar la construcción de dependencias en `Navegacion`.
7. Revisar la inicialización de `TorneosInscripcionesController`.
8. Confirmar que la aplicación JavaFX se conecta a la base esperada.
9. Ejecutar `mvn clean compile`.
10. Ejecutar `mvn test`.
11. Abrir nuevamente la bandeja y revisar consola.
12. Confirmar el comportamiento visual antes de crear el commit.

## 9. Plan de trabajo por etapas

### Etapa A: estabilizar torneos e inscripciones

- Resolver la recuperación de inscripciones.
- Validar la migración de base de datos.
- Revisar altas y consultas públicas.
- Verificar vinculación de jugadores con clientes.
- Confirmar normalización de teléfonos.
- Revisar estados y transiciones de las inscripciones.
- Ejecutar todas las pruebas.
- Realizar pruebas manuales desde web y escritorio.
- Limpiar scripts temporales solo después de confirmar el resultado.
- Crear un commit descriptivo del módulo.

### Etapa B: estabilización general

- Revisar errores de consola de JavaFX.
- Revisar errores del navegador con las herramientas de desarrollo.
- Probar registro, sesión, perfil y recuperación de contraseña.
- Probar reservas con y sin cuenta.
- Probar clientes, pagos, caja y solicitudes web.
- Verificar que actualizar páginas o usar `F5` no rompa el estado.
- Validar mensajes de error y estados vacíos.
- Ampliar pruebas automatizadas donde existan casos sin cobertura.

### Etapa C: integración completa

- Confirmar que escritorio y web usen la misma información.
- Evitar reservas simultáneas incompatibles.
- Sincronizar disponibilidad y estados.
- Centralizar clientes, reservas, pagos y torneos.
- Definir claramente qué operaciones pertenecen a la API pública.
- Preparar la API para futuros clientes móviles.

### Etapa D: seguridad

- Revisar autenticación y autorización administrativa.
- Revisar sesiones de clientes.
- Confirmar almacenamiento seguro de contraseñas.
- Proteger endpoints públicos y administrativos.
- Gestionar secretos mediante variables de entorno.
- Evitar credenciales dentro del repositorio.
- Revisar expiración y revocación de sesiones.
- Revisar recuperación de contraseña y vencimiento de tokens.
- Registrar operaciones importantes mediante auditoría.

### Etapa E: despliegue

- Separar configuración de desarrollo y producción.
- Versionar migraciones de base de datos.
- Preparar copias de seguridad y restauración.
- Publicar la API.
- Publicar la web pública.
- Configurar dominio y HTTPS.
- Configurar correo de producción.
- Definir monitoreo y registros.
- Documentar instalación, actualización y recuperación.

### Etapa F: aplicación móvil

- Definir si será nativa, multiplataforma o una evolución de la PWA.
- Reutilizar la API y las reglas de negocio existentes.
- Implementar registro e inicio de sesión.
- Consultar disponibilidad.
- Crear, consultar y cancelar reservas según las reglas del complejo.
- Consultar torneos e inscripciones.
- Mostrar historial y perfil.
- Incorporar notificaciones cuando la infraestructura esté preparada.

## 10. Metodología segura de trabajo

Cada cambio debe seguir este orden:

1. Describir el problema observado.
2. Reproducirlo y conservar el error completo.
3. Revisar el estado de Git.
4. Identificar los archivos realmente involucrados.
5. Evitar modificar archivos basándose en suposiciones.
6. Aplicar un cambio controlado y pequeño.
7. Verificar el contenido modificado.
8. Compilar.
9. Ejecutar pruebas automatizadas.
10. Probar manualmente.
11. Revisar consola y registros.
12. Confirmar que no se rompieron funciones anteriores.
13. Eliminar archivos temporales cuando ya no sean necesarios.
14. Crear un commit simple y descriptivo.
15. Actualizar este documento si cambió el estado del proyecto.

## 11. Criterios para considerar terminada una tarea

Una tarea se considera terminada únicamente cuando:

- El código compila.
- Las pruebas pasan.
- La función fue probada manualmente.
- No aparecen errores inesperados en consola.
- No se rompieron flujos existentes.
- La base de datos quedó en el estado esperado.
- Los archivos temporales fueron revisados.
- Los cambios quedaron respaldados en Git.
- El estado del proyecto quedó documentado.

## 12. Convenciones de Git

### 12.1 Mensajes de commit

Usar mensajes simples y descriptivos, sin prefijos innecesarios.

Ejemplos:

```text
Agregar autenticación y sesiones para clientes
Agregar recuperación de contraseña por correo
Integrar torneos e inscripciones en la web y el escritorio
Corregir recuperación de inscripciones de torneos
```

### 12.2 Antes de cada commit

Ejecutar:

```powershell
git status -sb
git diff --stat
git diff
```

Luego compilar y probar:

```powershell
mvn clean compile
mvn test
```

### 12.3 Publicación en GitHub

Un commit local no se publica automáticamente. Para enviar los commits al remoto se necesita, cuando el estado esté revisado y protegido:

```powershell
git push origin master
```

No ejecutar el `push` como parte automática de una corrección. Primero revisar qué commits y archivos serán publicados.

## 13. Archivos temporales y scripts de intervención

Actualmente existen scripts para aplicar, corregir o restaurar cambios relacionados con:

- Bandeja de torneos.
- Consulta pública de torneos.
- Endpoint de inscripción.
- Normalización de teléfonos.
- Vinculación de inscripciones.
- Manejo de señas en torneos.
- Mocks y pruebas.
- Restauración de cambios anteriores.

No eliminarlos en conjunto hasta:

1. Confirmar qué versión quedó aplicada.
2. Verificar compilación y pruebas.
3. Probar el módulo manualmente.
4. Crear un commit con el resultado definitivo.
5. Conservar solo los scripts que tengan valor operativo o documental.

El archivo temporal `reparar_dashboard_utf8.py` puede revisarse para eliminación porque la reparación visual ya fue confirmada.

## 14. Mantenimiento de este documento

Actualizar este archivo cuando ocurra cualquiera de estas situaciones:

- Se completa un módulo.
- Cambia la arquitectura.
- Se agrega una migración.
- Se resuelve un problema conocido.
- Se decide una nueva regla de negocio.
- Se crea una etiqueta o un punto estable.
- Se publica trabajo local en GitHub.
- Cambia el próximo paso prioritario.

Al terminar cada sesión importante, actualizar al menos:

- Estado actual.
- Último cambio confirmado.
- Problemas conocidos.
- Próximo paso exacto.
- Commit o etiqueta de referencia.

## 15. Resumen de continuidad

### Último cambio confirmado manualmente

Corrección de textos dañados por codificación UTF-8 en el dashboard administrativo.

### Bloque actualmente en desarrollo

Torneos e inscripciones desde la web pública y la aplicación JavaFX.

### Problema que debe investigarse ahora

```text
No se pudieron recuperar las inscripciones de torneos.
```

### Referencia Git local

```text
5f302714a31980634a02b1d337221217ab028dad
```

### Situación frente al remoto

```text
La rama master local está siete commits por delante de origin/master.
El módulo de torneos todavía incluye cambios sin commit.
```
---

## 16. Actualización de estado del 30 de septiembre de 2026

### Módulo de torneos e inscripciones completado

Se completó y validó la primera versión funcional del módulo de torneos e inscripciones, integrada entre la web pública, la API, MySQL Docker y la aplicación administrativa JavaFX.

### Funciones completadas

#### Web pública y API

- Consulta pública de torneos, categorías y ramas.
- Inscripción de parejas con o sin una cuenta iniciada.
- Datos del responsable obtenidos desde la sesión cuando corresponde.
- Registro manual de ambos integrantes cuando no existe sesión.
- Comentario opcional en la solicitud.
- Creación de solicitudes con estado `PENDIENTE` y origen `WEB`.

#### Vinculación automática

- Normalización de teléfonos.
- Búsqueda de clientes activos por teléfono normalizado.
- Vinculación automática ante una coincidencia única.
- Registro sin `cliente_id` cuando no existe coincidencia.
- Marcado para revisión cuando la coincidencia es ambigua.
- La falta de coincidencia no bloquea la inscripción.

#### Bandeja administrativa JavaFX

- Acceso desde el dashboard administrativo.
- Vista integrada con navegación y estilos propios.
- Consulta de solicitudes desde MySQL Docker.
- Métricas de total, pendientes, confirmadas y lista de espera.
- Búsqueda por número, torneo, categoría, integrante y teléfono.
- Filtro por estado.
- Tabla y panel de detalle de la inscripción.
- Visualización de ambos integrantes y su vinculación.
- Comentarios y observaciones administrativas.
- Acceso a WhatsApp para ambos integrantes.
- Columnas JavaFX compatibles con `record`.
- Textos y codificación UTF-8 corregidos.

#### Gestión administrativa

- Confirmación de inscripciones.
- Envío a lista de espera.
- Rechazo y cancelación.
- Validación de transiciones permitidas.
- Control de cupos antes de confirmar.
- Prevención de participaciones duplicadas en una categoría.
- Registro de observaciones, `usuario_gestion_id`, `fecha_confirmacion` y `fecha_cancelacion`.
- Transacciones con `commit` y `rollback`.
- Recarga automática, conservación de selección y actualización de métricas.

#### Vinculación manual

- Buscador de clientes activos por ID, nombre, apellido, documento y teléfono.
- Vinculación manual del responsable y del segundo integrante.
- Cambio de una vinculación existente.
- Desvinculación de integrantes.
- Sincronización de `responsable_cliente_id`.
- Registro de `tipo_vinculacion = MANUAL`.
- Limpieza de `requiere_revision`.
- Prevención del mismo cliente para ambos integrantes.
- Prevención de participación en otra pareja de la misma categoría.
- Persistencia verificada directamente en MySQL.

#### Pruebas y verificaciones

- Compilación Maven correcta.
- Pruebas automatizadas correctas.
- Pruebas manuales desde la web pública y JavaFX.
- Auditoría directa en MySQL.
- Validación de vinculación, cambio y desvinculación.
- Validación del bloqueo de clientes duplicados.
- Verificación de codificación de los archivos del módulo.
- Verificación de eliminación de la seña del módulo de torneos.

### Regla definitiva sobre pagos

El módulo de torneos no gestiona señas ni pagos.

Se eliminaron definitivamente:

```text
importe_senia
PENDIENTE_PAGO
```

El campo `precio_inscripcion` se conserva únicamente como valor informativo.

### Configuración para pruebas integradas

La web pública y la API utilizan MySQL Docker. Para conectar la administración a la misma base:

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

### Mejoras visuales pendientes

- Modernizar las alertas estándar de JavaFX.
- Crear diálogos personalizados acordes al tema oscuro.
- Modernizar el buscador de clientes.
- Mejorar selección, botones y estados vacíos de la tabla.
- Corregir por separado los iconos dañados del selector de tema de la web pública.

### Próximo bloque funcional recomendado

Desarrollar la administración de torneos y categorías desde JavaFX:

1. Crear y editar torneos.
2. Publicar y abrir inscripciones.
3. Cerrar inscripciones.
4. Gestionar categorías, ramas, cupos y precios.
5. Consultar ocupación por categoría.
6. Validar fechas y transiciones de estado.
7. Incorporar pruebas automatizadas.
8. Mejorar posteriormente los diálogos y el buscador.

### Estado de continuidad

El módulo de inscripciones quedó funcional y validado. La prioridad siguiente es la administración de torneos y categorías. Las mejoras visuales quedan registradas para una etapa posterior de pulido general.
---

## 17. Administración integral de torneos completada

### Funcionalidades incorporadas

- Creación de torneos en estado `BORRADOR`.
- Edición de nombre, descripción, fechas, período de inscripción y reglamento.
- Conservación del usuario creador original.
- Creación, edición y desactivación de categorías.
- Gestión de nombre, rama, cupo de parejas y precio informativo.
- Visualización de parejas confirmadas y cupos disponibles.
- Protección contra cupos inferiores a las parejas confirmadas.
- Bloqueo de desactivación con inscripciones activas.
- Prevención del traslado de categorías entre torneos.
- Transacciones con `commit` y `rollback`.

### Ciclo de vida implementado

```text
BORRADOR -> PUBLICADO
PUBLICADO -> INSCRIPCION_ABIERTA
INSCRIPCION_ABIERTA -> INSCRIPCION_CERRADA
INSCRIPCION_CERRADA -> EN_CURSO
EN_CURSO -> FINALIZADO
Estados no finales -> CANCELADO
```

- Se bloquean las transiciones no permitidas.
- Para abrir inscripciones se exige al menos una categoría activa.
- Los estados `FINALIZADO` y `CANCELADO` son finales.

### Administración JavaFX

- Nueva opción `Torneos` en el dashboard.
- Pantalla con listado, búsqueda y filtro por estado.
- Formularios para crear y editar torneos.
- Formularios para crear y editar categorías.
- Acciones del ciclo de vida habilitadas según el estado actual.
- Acceso directo a `Torneos e inscripciones`.
- Integración visual con el tema administrativo.

### Web pública

- Nueva sección `Torneos disponibles`.
- Carga dinámica desde `GET /api/publica/torneos`.
- Visualización de fechas, categorías, ramas, cupos y precios.
- Indicador de disponibilidad según estado y período de inscripción.
- Formulario público para inscribir una pareja.
- Precarga del responsable cuando existe una sesión iniciada.
- Inscripción manual sin cuenta.
- Comentarios opcionales.
- Envío a `/api/publica/torneos/inscripciones`.
- Confirmación con número de solicitud.
- Diseño adaptable a dispositivos móviles.

### Validaciones realizadas

- Creación y edición de torneos desde JavaFX.
- Creación y edición de categorías.
- Publicación y apertura de inscripciones.
- Inscripción pública sin cuenta.
- Inscripción pública con cuenta iniciada.
- Vinculación automática del responsable autenticado.
- Recepción de solicitudes en la bandeja administrativa.
- Confirmación administrativa de inscripciones.
- Actualización dinámica de cupos en la web.
- Compilación Maven y pruebas automatizadas correctas.

### Mejoras pendientes

- Modernizar los diálogos estándar de JavaFX.
- Agregar confirmaciones visuales personalizadas para cambios de estado y desactivaciones.
- Mejorar la presentación de fechas y horarios en la pantalla administrativa.
- Corregir los iconos dañados del selector de tema de la web pública.
- Incorporar una vista pública del reglamento completo.

### Continuidad recomendada

El flujo de torneos quedó integrado entre JavaFX, API pública, web y MySQL. El siguiente bloque puede orientarse a cuadros, partidos, resultados y programación de encuentros, o bien a un pulido visual general antes de continuar con nuevas funciones.
