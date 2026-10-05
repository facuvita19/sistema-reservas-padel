# Plan de desarrollo

## 1. Propósito del documento

Este documento registra el estado real, la arquitectura, las decisiones funcionales, la metodología de trabajo y las próximas etapas del sistema de reservas para un complejo de pádel.

Su objetivo es permitir que el desarrollo continúe de forma ordenada y segura aunque cambie la conversación, el entorno o el equipo. Debe actualizarse después de cada bloque funcional importante.

> Este documento resume el estado general. Antes de modificar una función deben revisarse los archivos actuales del repositorio. No se debe preparar un cambio únicamente a partir de este plan.

## 2. Objetivo general

Desarrollar una solución integral para administrar un complejo de pádel mediante componentes conectados a la misma lógica de negocio y base de datos:

1. Aplicación administrativa de escritorio en Java y JavaFX.
2. API pública para la web y futuros clientes.
3. Web pública para registro, autenticación, reservas, torneos y autogestión.
4. Futura aplicación móvil conectada al mismo backend.

El sistema centraliza canchas, clientes, reservas, pagos, caja, usuarios, solicitudes web, estadísticas, torneos, inscripciones, grupos, cuadros eliminatorios, partidos y resultados.

## 3. Tecnologías y herramientas

- Java, JavaFX, Maven, FXML y CSS.
- MySQL, JDBC, DAO y servicios transaccionales.
- JUnit y pruebas automatizadas.
- API HTTP pública en Java.
- Astro, TypeScript, Tailwind CSS, HTML, CSS y JavaScript.
- Docker, Docker Compose y Nginx.
- Gmail SMTP con contraseña de aplicación.
- Git y GitHub.
- PowerShell.
- Node.js para aplicadores controlados de cambios.
- Eclipse y Visual Studio Code.

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
- Dashboard adaptado por rol.
- Agenda y reservas.
- Canchas y bloqueos.
- Clientes e historial.
- Pagos, caja y cierre diario.
- Estadísticas.
- Solicitudes web.
- Usuarios y configuración.
- Torneos y categorías.
- Inscripciones administrativas y web.
- Vinculación de jugadores con clientes.
- Fase de grupos.
- Cuadro eliminatorio.
- Programación y resultados.
- Exportación del cuadro y PDF.

### 4.2 API pública

Ubicación:

```text
src/main/java/api/publica
```

Responsabilidades:

- Configuración pública del complejo.
- Canchas y disponibilidad.
- Solicitudes de reserva y seguimiento.
- Autenticación y sesiones de clientes.
- Registro, perfil e historial.
- Actualización de datos y contraseña.
- Recuperación de acceso por correo.
- Torneos, categorías e inscripciones públicas.

### 4.3 Servicios, negocio y persistencia

Ubicaciones:

```text
src/main/java/servicio
src/main/java/negocio
src/main/java/dao
src/main/java/config
```

La solución utiliza entidades de negocio, servicios, DAO MySQL, configuración externa, sesiones persistidas, contraseñas protegidas, vencimientos automáticos y correo SMTP.

### 4.4 Web pública definitiva

Ubicación:

```text
web-publica-v2
```

Tecnología:

```text
Astro + TypeScript + Tailwind CSS + Nginx
```

Dirección local:

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

Funciones terminadas:

- Portada adaptable.
- Canchas y disponibilidad.
- Creación y seguimiento de solicitudes.
- Transferencia y WhatsApp.
- Registro, inicio y cierre de sesión.
- Cookie de sesión.
- Reservas próximas y anteriores.
- Perfil y actualización de datos.
- Documento no editable.
- Posición preferida `DRIVE`, `REVES` o sin preferencia.
- Cambio y recuperación de contraseña.
- Torneos, categorías, ramas y cupos.
- Inscripción de parejas con o sin sesión.
- Precarga de datos autenticados.
- Rutas directas mediante Nginx.
- Despliegue Docker local.

`web-publica` se conserva como referencia histórica. La web activa es `web-publica-v2`.

### 4.5 Docker

Servicios:

```text
padel-mysql
padel-api
padel-web
```

Puertos:

```text
Web:   http://localhost:5173
API:   http://localhost:8080
MySQL: localhost:3307
```

Archivos principales:

```text
web-publica-v2/Dockerfile
web-publica-v2/nginx.conf
```

### 4.6 Base de datos

Migraciones base documentadas:

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

La migración 17 incorpora `posicion_preferida` con valores `DRIVE`, `REVES` o `NULL`.

El proyecto agregó después estructuras de grupos, partidos y cuadro. Antes de crear otra migración debe revisarse el listado real de `database` y `database/docker-init`, sin inferir el siguiente número desde este documento.

## 5. Estado actual confirmado

### 5.1 Git

Rama:

```text
master
```

Repositorio:

```text
https://github.com/facuvita19/sistema-reservas-padel.git
```

La modernización administrativa, Inscripciones y Solicitudes web fueron compiladas, confirmadas y publicadas en `origin/master`.

El hash actual debe consultarse al retomar:

```powershell
git status -sb
git log -1 --oneline
git rev-parse HEAD
git rev-parse origin/master
```

Estado esperado:

```text
HEAD = master = origin/master
Directorio de trabajo limpio
```

Commit relevante confirmado:

```text
cc7e3de Mejorar torneos, cuadro eliminatorio, resultados y exportación PDF
```

También se publicaron commits descriptivos para Inscripciones y Solicitudes web. El hash exacto debe obtenerse desde Git.

### 5.2 Validaciones realizadas

- `mvn clean test` correcto al cerrar los bloques.
- `npm run build` correcto en los bloques que afectaron la web.
- Docker y sus rutas validados en los bloques correspondientes.
- Registro, sesión, reserva, historial y perfil validados.
- Cambio y recuperación de contraseña validados.
- Torneos e inscripciones validados desde web y JavaFX.
- Grupos, cuadro eliminatorio y resultados probados.
- Exportación y PDF del cuadro revisados.
- Modernización visual administrativa terminada.

## 6. Funciones completadas

### 6.1 Reservas y operación

- Disponibilidad.
- Solicitudes web y reservas administrativas.
- Vencimientos automáticos.
- Pagos y señas.
- Seguimiento público.
- Estados, agenda y reprogramaciones.
- Bloqueos y auditoría.
- Caja y cierre diario.
- WhatsApp.

### 6.2 Cuentas de clientes

- Registro y autenticación.
- Cookies de sesión.
- Perfil e historial.
- Vinculación de reservas.
- Actualización de datos.
- Posición preferida.
- Cambio y recuperación de contraseña.
- Correo Gmail SMTP.

### 6.3 Torneos e inscripciones

#### Administración

- Creación en `BORRADOR`.
- Edición general, inscripción y reglamento.
- Categorías, ramas, cupos y precio informativo.
- Parejas confirmadas y cupos disponibles.
- Protección de cupos y desactivación de categorías.

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
- Datos autenticados precargados.
- Registro de ambos integrantes.
- Origen web y estado pendiente.
- Bandeja administrativa, búsqueda y filtros.
- Confirmación, rechazo, cancelación y espera.
- Control de cupos y duplicados.
- Observaciones sin cambio de estado.
- Alta administrativa.
- Selección de clientes registrados.
- Carga manual alternativa.

#### Vinculación

- Normalización de teléfonos.
- Vinculación automática y manual.
- Cambio y desvinculación.
- Prevención del mismo cliente en ambos integrantes.
- Prevención de participación duplicada.

#### Pagos de torneos

El módulo no gestiona señas ni pagos. Se eliminaron:

```text
importe_senia
PENDIENTE_PAGO
```

`precio_inscripcion` es informativo.

### 6.4 Fase de grupos

- Generación por categoría.
- Un cabeza de serie por grupo.
- Distribución aleatoria del resto.
- Configuración completamente manual.
- Rotación de primeros y rivales.
- Restauración automática.
- Tabla de posiciones y resultados.
- Clasificados en verde y eliminados en rojo suave.
- Parejas centradas y controles uniformes.

### 6.5 Cuadro eliminatorio, partidos y resultados

El bloque que figuraba como próximo módulo ya fue desarrollado:

- Propuesta eliminatoria.
- Generación automática y manual.
- Editor de cuadro.
- Llave gráfica.
- Plazas y participantes.
- Fecha, hora y cancha.
- Registro y corrección de resultados.
- Avance de ganadores.
- Historial de correcciones.
- Alta, eliminación y cambio de participantes.
- Vista previa y detallada.
- Campeón y fases.
- Exportación y PDF mural.
- Guardado seguro del PDF.

Controlador relevante:

```text
src/main/java/vista/controlador/EditorCuadroTorneoDialog.java
```

## 7. Modernización administrativa terminada

### 7.1 Módulos modernizados

- Login.
- Inicio y navegación.
- Agenda y reservas.
- Clientes.
- Canchas, horarios, ocupación y precios.
- Pagos y caja.
- Estadísticas.
- Solicitudes web.
- Torneos, grupos, cuadro y resultados.
- Inscripciones.
- Alta y búsqueda de clientes.
- Diálogos y acciones contextuales.

### 7.2 Identidad visual

- Gris grafito como base.
- Eliminación de azules antiguos no semánticos.
- Verde para éxito y comunicación.
- Gris para acciones secundarias.
- Ámbar o naranja para advertencias.
- Rojo apagado para cancelación, rechazo y expiración.
- Violeta grafito para acciones principales que deben distinguirse de WhatsApp.
- Títulos alineados a la izquierda.
- Métricas informativas compactas.
- Campos y filtros de menor altura.
- Tablas densas, tooltips y selección clara.
- Colores de estado visibles aun en filas seleccionadas.

### 7.3 Interacciones

- Escala leve en hover.
- Elevación de un píxel.
- Iluminación contextual.
- Reducción al presionar.
- Retorno suave.
- Sin efectos en controles deshabilitados.

### 7.4 Roles

Para `OPERADOR`:

- Ocultar Estadísticas.
- Ocultar resúmenes financieros restringidos.
- Adaptar Inicio y navegación.
- No exponer acciones o datos financieros no autorizados.

La aplicación administrativa abre maximizada después del login. El login conserva cabecera oscura con controles de ventana.

## 8. Últimos cierres visuales

### 8.1 Inscripciones

- Título `Inscripciones` a la izquierda.
- Total, Pendientes, Confirmadas, En espera y Finalizadas.
- Estados por color.
- Búsqueda y filtros compactos.
- Tabla con tooltips y selección inicial.
- Panel derecho y acciones según estado.
- Responsable y Segundo integrante.
- Vincular, cambiar y desvincular.
- WhatsApp por integrante.
- Comentario de solo lectura.
- Observación interna editable.
- Texto visible `En espera`, manteniendo `LISTA_ESPERA` internamente.
- Agregar inscripción maximizado.
- Buscar o cambiar cliente en ventana normal.
- Buscar cliente verde y Quitar selección rojo grafito.

### 8.2 Solicitudes web

- Métricas compactas y clickeables.
- Total gris azulado, Vigentes verde, Próximas naranja y Expiradas rojo.
- Buscar, Estado y Limpiar compactos.
- Actualizar gris.
- Cliente alineado a la izquierda y resto centrado.
- Colores persistentes al seleccionar.
- Sin badges duplicados.
- Mensajes específicos para expiradas y canceladas.
- Abrir reserva violeta.
- Abrir pagos gris.
- WhatsApp verde y centrado cuando es la única acción secundaria.

## 9. Decisiones operativas

### 9.1 Sesiones localhost

Las cookies se comparten por dominio. Si hay conflicto entre puertos `4321`, `4173` y `5173`:

1. Borrar datos de `localhost`.
2. Cerrar pestañas anteriores.
3. Abrir solo `http://localhost:5173`.
4. Iniciar sesión otra vez.

### 9.2 Gmail SMTP

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
API_WEB_PUBLICA_URL=http://localhost:5173/
```

Los secretos deben quedar fuera de Git.

### 9.3 Base JavaFX

Docker:

```powershell
.\usar-base-docker.ps1
.\ver-base-configurada.ps1
```

Local:

```powershell
.\usar-base-local.ps1
.\ver-base-configurada.ps1
```

Cerrar JavaFX antes de cambiar de base.

## 10. Metodología segura

1. Describir el objetivo.
2. Reproducir el estado actual.
3. Revisar Git.
4. Localizar archivos.
5. Pedir y revisar el contenido actual.
6. No modificar por suposición.
7. Aplicar un cambio pequeño.
8. Crear respaldo.
9. Verificar contenido.
10. Compilar.
11. Ejecutar pruebas.
12. Probar manualmente.
13. Revisar consola y registros.
14. Confirmar regresiones.
15. Limpiar temporales.
16. Crear commit descriptivo.
17. Actualizar este documento.
18. Hacer push después de revisar el commit.

### 10.1 Aplicadores Node

Los aplicadores `.mjs` se entregan dentro de un ZIP listo para extraer en la raíz.

Deben:

- Validar precondiciones.
- Identificar versión.
- Evitar doble aplicación.
- Crear y restaurar respaldos.
- No escribir si falla una validación.
- Mostrar Node, error y traza.
- Evitar reemplazos literales frágiles.
- Delimitar métodos Java por firma y llaves.
- Ser independientes de CRLF o LF.
- Validar el resultado antes de escribir.

Formato recomendado:

```text
Aplicador: nombre y versión
Node.js: vXX.XX.X
ERROR: descripción
Traza:
...
No se modificó ningún archivo.
```

Evitar:

- Anclas genéricas con varias coincidencias.
- Dependencia de espacios o saltos exactos.
- Marcadores verificados en el archivo incorrecto.
- Escapes Java incorrectos como `"\d"` en vez de `"\\d"`.
- Capturar y ocultar la traza real.

## 11. Criterios de finalización

Una tarea termina cuando:

- Compila.
- Las pruebas pasan.
- Fue probada manualmente.
- No hay errores inesperados.
- No rompe flujos anteriores.
- La base queda consistente.
- Docker funciona si corresponde.
- No quedan temporales.
- Hay commit revisado.
- Se realizó el push acordado.
- El estado quedó documentado.

## 12. Convenciones Git

Mensajes simples, sin prefijos como `feat:`.

Ejemplos:

```text
Agregar WhatsApp, historial de clientes y mejoras del dashboard
Mejorar torneos, cuadro eliminatorio, resultados y exportación PDF
Modernizar inscripciones y gestión de participantes
Completar modernización de solicitudes web e inscripciones
```

Antes del commit:

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

Limpieza de temporales:

```powershell
Get-ChildItem -Path "." -File -Filter "*.mjs" | Remove-Item -Force
Get-ChildItem -Path ".\src" -Recurse -File |
    Where-Object { $_.Name -like "*.bak-*" } |
    Remove-Item -Force
```

Commit y publicación:

```powershell
git add ".\src\main\java"
git add ".\src\main\resources"
git status --short
git diff --cached --stat
git diff --cached --check
git commit -m "Mensaje simple y descriptivo"
git push origin master
git status
git log -1 --oneline
```

El push no se ejecuta dentro de un aplicador. Se realiza después de revisar el commit.

## 13. Próximas etapas

La modernización visual y el bloque deportivo de torneos ya están terminados.

### Etapa A: revisión funcional integral

- Recorrer módulos administrativos.
- Verificar acciones por estado.
- Revisar reservas, pagos, caja y WhatsApp.
- Revisar altas, ediciones y cancelaciones.
- Verificar torneos, grupos, cuadro y resultados.
- Buscar regresiones posteriores a la modernización.

### Etapa B: permisos

- Probar ADMIN y OPERADOR.
- Confirmar restricciones de Estadísticas y finanzas.
- Revisar navegación y endpoints por rol.

### Etapa C: limpieza técnica

- Consolidar CSS duplicado.
- Unificar colores, botones, tablas, campos y cabeceras.
- Evaluar componentes JavaFX reutilizables.
- Eliminar marcadores o históricos innecesarios.
- Trabajar siempre en bloques pequeños.

### Etapa D: estabilización

- Ampliar pruebas.
- Probar reservas simultáneas.
- Probar vencimientos y acreditaciones.
- Probar concurrencia de cupos.
- Probar cuadros con tamaños y BYE diferentes.
- Revisar consola JavaFX, navegador y red.

### Etapa E: seguridad

- Revisar autenticación y autorización.
- Revisar sesiones, expiración y revocación.
- Proteger endpoints por rol.
- Mantener secretos fuera del repositorio.
- Preparar HTTPS y cookies seguras.

### Etapa F: despliegue

- Separar desarrollo y producción.
- Verificar migraciones.
- Definir backups y restauración.
- Publicar API y web.
- Configurar dominio, HTTPS, correo y monitoreo.

### Etapa G: aplicación móvil

- Elegir tecnología.
- Reutilizar API.
- Registro, reservas, historial y perfil.
- Torneos, cuadros y resultados.
- Notificaciones.

## 14. Resumen de continuidad

Estado general:

- Web pública operativa.
- API operativa.
- Aplicación administrativa modernizada.
- Inscripciones y vinculación terminadas.
- Solicitudes web terminadas.
- Torneos, grupos, cuadro, resultados y PDF desarrollados.
- Cambios confirmados y publicados al cerrar el bloque.

Primer paso al retomar:

```powershell
git status -sb
git log -1 --oneline
mvn clean test
```

Próximo bloque recomendado:

```text
Revisión funcional integral y limpieza técnica posterior a la modernización administrativa
```
