# Plan de desarrollo

## 0. Resumen de continuidad inmediata

Este documento es la fuente principal de continuidad del proyecto `sistema-reservas-padel`. Permite retomar el desarrollo en otra conversación sin reconstruir desde cero la arquitectura, las decisiones funcionales ni la metodología de trabajo.

Estado estable al 6 de octubre de 2026:

```text
Rama: master
HEAD: 3f35847
origin/master: 3f35847
Estado esperado: directorio de trabajo limpio
Último commit: Mejorar gestión y registro de pagos
```

Últimos bloques cerrados y publicados:

```text
5d09fe8 Mejorar distribución y formulario de reservas
094e820 Mejorar diseño y detalle de solicitudes web
a7345ae Mejorar diseño y acciones de torneos
4d9c163 Mejorar inscripciones y alta administrativa
3f35847 Mejorar gestión y registro de pagos
```

Situación actual:

- La aplicación administrativa está modernizada en sus módulos principales.
- La web pública, la API y Docker están operativos según las validaciones de los bloques correspondientes.
- Reservas, Solicitudes web, Torneos, Grupos, Inscripciones y Pagos recibieron una revisión visual y funcional reciente.
- Los cambios recientes fueron compilados, probados manualmente, confirmados y publicados.
- El próximo trabajo recomendado es una revisión funcional integral, permisos por rol y limpieza técnica controlada.

Primeros comandos al retomar:

```powershell
git status -sb
git log -1 --oneline
git rev-parse HEAD
git rev-parse origin/master
mvn clean test
```

Antes de modificar cualquier función se deben inspeccionar los archivos reales. Este documento orienta, pero no reemplaza al código actual.

---

## 1. Propósito del documento

Este documento registra el estado real, la arquitectura, las decisiones funcionales, la metodología de trabajo y las próximas etapas del sistema de reservas para un complejo de pádel.

Su objetivo es permitir que el desarrollo continúe de forma ordenada y segura aunque cambie la conversación, el entorno o el equipo. Debe actualizarse después de cada bloque funcional importante.

Reglas de uso:

1. Leer primero el resumen de continuidad.
2. Revisar Git y el código actual antes de proponer cambios.
3. No asumir nombres de archivos, estructuras FXML, selectores CSS ni firmas Java.
4. No preparar reemplazos basados únicamente en este documento.
5. Actualizar el plan después de cerrar y publicar un bloque importante.

---

## 2. Objetivo general

Desarrollar una solución integral para administrar un complejo de pádel mediante componentes conectados a la misma lógica de negocio y base de datos:

1. Aplicación administrativa de escritorio en Java y JavaFX.
2. API pública para la web y futuros clientes.
3. Web pública para registro, autenticación, reservas, torneos y autogestión.
4. Futura aplicación móvil conectada al mismo backend.

El sistema centraliza:

- canchas y horarios;
- clientes e historial;
- reservas y solicitudes web;
- pagos, caja y cierre diario;
- usuarios, roles y configuración;
- estadísticas;
- torneos y categorías;
- inscripciones administrativas y públicas;
- grupos, partidos y posiciones;
- cuadros eliminatorios, resultados y campeones;
- publicación web y exportación PDF.

---

## 3. Tecnologías y herramientas

- Java.
- JavaFX.
- Maven.
- FXML y CSS.
- MySQL.
- JDBC.
- Patrón DAO.
- Servicios de negocio y transacciones.
- JUnit y pruebas automatizadas.
- API HTTP pública en Java.
- Astro.
- TypeScript.
- Tailwind CSS.
- HTML, CSS y JavaScript.
- Docker y Docker Compose.
- Nginx.
- Gmail SMTP con contraseña de aplicación.
- Git y GitHub.
- PowerShell.
- Node.js para aplicadores controlados `.mjs`.
- Visual Studio Code.
- Eclipse se conserva por metadatos históricos y compatibilidad, pero no define el flujo actual.

---

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
- Agenda.
- Gestión de reservas.
- Canchas, horarios, ocupación, precios y bloqueos.
- Clientes e historial.
- Gestión de pagos.
- Caja y cierre diario.
- Estadísticas.
- Solicitudes web.
- Usuarios y configuración.
- Torneos y categorías.
- Inscripciones administrativas y web.
- Vinculación de jugadores con clientes.
- Fase de grupos.
- Partidos y tabla de posiciones.
- Cuadro eliminatorio.
- Programación y resultados.
- Exportación y PDF del cuadro.

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

La solución utiliza:

- entidades y records de negocio;
- servicios con reglas y validaciones;
- DAO MySQL;
- configuración externa;
- sesiones persistidas;
- contraseñas protegidas;
- vencimientos automáticos;
- correo SMTP;
- migraciones SQL versionadas.

### 4.4 Web pública definitiva

Ubicación activa:

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

`web-publica` se conserva como referencia histórica. Las funciones nuevas deben desarrollarse sobre `web-publica-v2`.

### 4.5 Docker

Servicios conocidos:

```text
padel-mysql
padel-api
padel-web
```

Puertos locales:

```text
Web:   http://localhost:5173
API:   http://localhost:8080
MySQL: localhost:3307
```

Archivos relevantes:

```text
web-publica-v2/Dockerfile
web-publica-v2/nginx.conf
docker-compose.yml
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

El proyecto agregó posteriormente estructuras de grupos, partidos, posiciones y cuadro. Antes de crear otra migración se debe revisar el contenido real de:

```text
database
database/docker-init
```

No se debe inferir el siguiente número de migración desde este documento.

### 4.7 Documentación operativa adicional

La carpeta `docs` contiene documentación específica que complementa este plan:

```text
docs/ARQUITECTURA.md
docs/BACKUP_Y_RESTAURACION...
docs/DESPLIEGUE.md
docs/MANUAL_ADMINISTRATIVO...
docs/OPERACION_DIARIA.md
docs/SEGURIDAD_OPERATIVA.md
docs/SOLUCION_DE_PROBLEMAS...
```

Antes de modificar despliegue, seguridad, backups u operación diaria debe consultarse el documento específico correspondiente.

---

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

Estado confirmado al 6 de octubre de 2026:

```text
3f35847 (HEAD -> master, origin/master) Mejorar gestión y registro de pagos
```

Estado esperado:

```text
HEAD = master = origin/master
Directorio de trabajo limpio
```

Secuencia reciente relevante:

```text
3f35847 Mejorar gestión y registro de pagos
4d9c163 Mejorar inscripciones y alta administrativa
a7345ae Mejorar diseño y acciones de torneos
094e820 Mejorar diseño y detalle de solicitudes web
5d09fe8 Mejorar distribución y formulario de reservas
1ccb8ea Aislar estilos específicos de canchas, reservas y resultados
e3f17c8 Mejorar programación y resultados de partidos
287ffcb Limpiar estilos de la propuesta eliminatoria
f909cc5 Limpiar estilos duplicados del editor estructural
e5c2558 Limpiar estilos duplicados del editor de cuadro
eb70435 Mejorar campos, validaciones y ventanas de torneos
bf89f19 Consolidar estilos de selectores en diálogos
b33ee02 Consolidar estilos del calendario en diálogos
51872c7 Eliminar estilos duplicados de diálogos simples
73919b3 Recuperar cuadro de torneo y validar recursos visuales
cc4f559 Separar estilos del buscador de clientes y consolidar alta de inscripciones
1be06a3 Consolidar estilos de solicitudes e inscripciones y aislar pruebas de reservas
6dc8238 Actualizar plan de desarrollo del proyecto
7096966 Completar modernización de solicitudes web e inscripciones
e87511e Modernizar inscripciones y gestión de participantes
```

Al retomar se debe volver a consultar Git, porque este estado cambiará con futuros commits.

### 5.2 Validaciones realizadas

- `mvn clean test` correcto al cerrar los bloques recientes.
- `npm run build` correcto en los bloques que afectaron la web.
- Docker y sus rutas validados en los bloques correspondientes.
- Registro, sesión, reserva, historial y perfil validados.
- Cambio y recuperación de contraseña validados.
- Torneos e inscripciones validados desde web y JavaFX.
- Grupos, cuadro eliminatorio y resultados probados.
- Exportación y PDF del cuadro revisados.
- Modernización visual administrativa principal terminada.
- Gestión de pagos revisada visual y funcionalmente.

---

## 6. Funciones completadas

### 6.1 Reservas y operación

- Disponibilidad de canchas.
- Solicitudes web.
- Reservas administrativas.
- Selección de clientes registrados.
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
- Correo mediante Gmail SMTP.

### 6.3 Torneos e inscripciones

#### Administración

- Creación en `BORRADOR`.
- Edición general, inscripción y reglamento.
- Categorías, ramas, cupos y precio informativo.
- Parejas confirmadas y cupos disponibles.
- Protección de cupos.
- Desactivación de categorías.

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
- Confirmación, rechazo, cancelación y lista de espera.
- Control de cupos y duplicados.
- Observaciones sin cambio de estado.
- Alta administrativa.
- Búsqueda y selección de clientes registrados.
- Carga manual alternativa.
- Alternancia explícita entre cliente registrado y datos manuales.

#### Vinculación

- Normalización de teléfonos.
- Vinculación automática y manual.
- Cambio y desvinculación.
- Prevención del mismo cliente en ambos integrantes.
- Prevención de participación duplicada.
- WhatsApp por integrante.

#### Pagos de torneos

El módulo de inscripciones de torneos no gestiona señas ni pagos. Se eliminaron:

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
- Clasificados y eliminados diferenciados visualmente.
- Gestión de partidos de grupo.
- Selección de grupo y acciones contextuales.

### 6.5 Cuadro eliminatorio, partidos y resultados

- Propuesta eliminatoria.
- Generación automática y manual.
- Editor estructural y editor de cuadro.
- Llave gráfica.
- Plazas y participantes.
- Fecha, hora y cancha.
- Registro y corrección de resultados.
- Avance automático de ganadores.
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

---

## 7. Principios obligatorios del motor eliminatorio

La etapa eliminatoria debe respetar reglas deportivas bloqueantes.

### 7.1 Inclusión de clasificados

- Todos los primeros reciben el mismo tratamiento.
- Todos los segundos avanzan a otra instancia.
- Todos los terceros de grupos de cuatro avanzan cuando el formato así lo define.
- Ningún segundo o tercero clasificado queda eliminado por una tabla comparativa.
- Quien no recibe pase directo debe disputar una fase de acceso, no quedar excluido administrativamente.

### 7.2 Uso permitido del ranking entre grupos

El ranking transversal sirve exclusivamente para:

- otorgar pases;
- definir cabezas de serie;
- ordenar cruces;
- evitar cruces tempranos entre los mejores;
- decidir el orden dentro de parejas con la misma posición de grupo.

Nunca debe utilizarse para eliminar a una pareja que ya clasificó dentro de su grupo.

### 7.3 Jerarquía de posiciones

```text
1.º de grupo > 2.º de grupo > 3.º de grupo
```

Aclaraciones:

- Un segundo nunca puede comenzar en una instancia posterior a un primero.
- Un tercero nunca puede comenzar en una instancia posterior a un segundo.
- Si los primeros reciben un pase, todos los primeros deben recibir el mismo pase.
- Los mejores segundos pueden recibir pases, pero nunca superiores a los de los primeros.
- Dentro de una misma posición se utiliza el ranking deportivo para repartir beneficios.

### 7.4 Ranking deportivo

Criterios acordados:

1. Partidos ganados.
2. Diferencia de sets.
3. Diferencia de games o puntos.
4. Sets ganados.
5. Games o puntos ganados.
6. Criterio administrativo de desempate cuando persiste igualdad absoluta.

No debe decidirse deportivamente por:

- grupo alfabético;
- menor ID;
- orden de carga;
- posición accidental en una lista;
- orden alfabético del nombre.

### 7.5 Integridad de la estructura

- Cada participante juega como máximo un partido por instancia.
- Nadie disputa dos partidos dentro de la misma ronda.
- Los ganadores de la fase de acceso completan el cuadro principal.
- El administrador puede modificar o reemplazar la propuesta antes de generar partidos.
- Si no existe una solución automática que cumpla todas las reglas, el sistema debe exigir configuración manual.

---

## 8. Modernización administrativa

### 8.1 Identidad visual común

- Gris grafito como base.
- Verde principal para acciones positivas, foco y selección.
- Verde específico para WhatsApp.
- Gris para acciones secundarias.
- Ámbar o naranja para advertencias.
- Rojo apagado para cancelación, rechazo y expiración.
- Violeta grafito solo cuando aporta diferenciación semántica.
- Títulos alineados a la izquierda.
- Métricas compactas.
- Controles entre 36 y 40 píxeles según contexto.
- Tablas densas y legibles.
- Tooltips para textos truncados.
- Filas seleccionadas con fondo grafito y franja verde izquierda.
- Scrollbars grafito neutro.
- Sin desplazamiento horizontal en pantallas normales.

Paleta validada para scrollbars:

```text
Track: #151a1e
Thumb: #465057
Hover: #5a666d
Ancho: 10 px
```

### 8.2 Interacciones

- Escala leve en hover.
- Elevación de un píxel.
- Iluminación contextual.
- Reducción al presionar.
- Retorno suave.
- Sin efectos activos en controles deshabilitados.
- Los estados `focused` y `showing` no deben alterar la geometría de ComboBox.
- Los cambios de modo deben ser visualmente evidentes.

### 8.3 Roles

Para `OPERADOR`:

- ocultar Estadísticas;
- ocultar resúmenes financieros restringidos;
- adaptar Inicio y navegación;
- no exponer acciones o datos financieros no autorizados.

La aplicación administrativa abre maximizada después del login. El login conserva cabecera oscura con controles de ventana.

---

## 9. Cierres visuales recientes

### 9.1 Gestión de reservas

Commit:

```text
5d09fe8 Mejorar distribución y formulario de reservas
```

Resultado:

- proporción aproximada `68 % tabla / 32 % formulario`;
- filtros compactos;
- panel derecho sin desplazamiento horizontal;
- Cliente, Cancha, Fecha y Horario sin recortes;
- bloques Precio, Saldo y Jugadores completos;
- historial estable antes y después del foco;
- textos largos ajustados en varias líneas;
- scroll horizontal eliminado;
- scrollbars grafito;
- desplegables con cantidad visible razonable.

### 9.2 Solicitudes web

Commit:

```text
094e820 Mejorar diseño y detalle de solicitudes web
```

Resultado:

- métricas compactas y clickeables;
- filtros y franja de resultados reducidos;
- tabla con mayor espacio útil;
- vencimientos no aplicables sin guiones;
- scrollbar grafito;
- panel de detalle compacto;
- teléfono con etiqueta explícita;
- acción Abrir reserva con color principal;
- navegación, filtros, reloj y doble clic conservados.

La apariencia del popup nativo de algunos ComboBox se acepta cuando es funcional, legible y modificarla implicaría riesgo global.

### 9.3 Torneos y grupos

Commit:

```text
a7345ae Mejorar diseño y acciones de torneos
```

Resultado:

- distribución aproximada `33 % listado / 67 % detalle`;
- filtros compactos;
- resumen, siguiente paso y categorías con menor espacio vacío;
- texto explícito `CANCELAR TORNEO`;
- barra de acciones de categoría con espacio reservado;
- selección de categoría sin salto de layout ni scrollbar global inesperado;
- Grupos y Cuadro con jerarquía diferenciada;
- botón Cuadro resaltado;
- botón Agregar al grupo integrado al tema y habilitado solo con pareja y grupo seleccionados.

### 9.4 Inscripciones y alta administrativa

Commit:

```text
4d9c163 Mejorar inscripciones y alta administrativa
```

Resultado:

- detalle derecho más ancho;
- métricas, filtros y contador compactos;
- tabla proporcional sin desplazamiento horizontal;
- columnas Responsable y Segundo integrante priorizadas;
- selección automática coherente con filtros;
- scrollbars grafito;
- acciones de vinculación jerarquizadas;
- comentario vacío compacto;
- alta administrativa con clientes registrados reutilizables;
- campos manuales ocultos cuando hay cliente seleccionado;
- acción `USAR DATOS MANUALES` para volver al modo manual;
- formulario más corto y sin scroll innecesario.

### 9.5 Gestión de pagos

Commit:

```text
3f35847 Mejorar gestión y registro de pagos
```

Resultado:

- filtros y acciones compactos;
- botón Deseleccionar retirado;
- selección automática del primer movimiento visible;
- tabla proporcional sin desplazamiento horizontal;
- columna `RESERVA Y CLIENTE` con cancha incluida;
- fechas con formato `dd/MM/yyyy · HH:mm`;
- detalle agrupado en tarjetas Reserva y Movimiento;
- estado comunicado por insignia sin duplicación;
- explicación cuando no hay acciones disponibles;
- formulario Nuevo movimiento reorganizado en tarjetas;
- modo creación claramente identificado;
- botón superior cambia a `MOVIMIENTO EN EDICIÓN`;
- foco automático en Reserva;
- Guardar deshabilitado hasta completar datos válidos;
- ComboBox Método y Estado inicial con geometría estable;
- scrollbars y checkbox en paleta grafito.

---

## 10. Decisiones operativas

### 10.1 Sesiones localhost

Las cookies se comparten por dominio. Si hay conflicto entre puertos `4321`, `4173` y `5173`:

1. Borrar datos de `localhost`.
2. Cerrar pestañas anteriores.
3. Abrir únicamente `http://localhost:5173`.
4. Iniciar sesión nuevamente.

### 10.2 Gmail SMTP

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

### 10.3 Base JavaFX

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

---

## 11. Metodología segura de trabajo

Flujo obligatorio:

1. Describir el objetivo.
2. Reproducir el estado actual.
3. Revisar Git.
4. Localizar archivos exactos.
5. Pedir y revisar el contenido actual.
6. No modificar por suposición.
7. Aplicar un cambio pequeño y controlado.
8. Crear respaldo.
9. Verificar el contenido modificado.
10. Compilar.
11. Ejecutar pruebas automatizadas.
12. Probar manualmente.
13. Revisar consola y registros.
14. Confirmar que no existen regresiones.
15. Limpiar temporales y respaldos.
16. Crear un commit simple y descriptivo.
17. Publicar después de revisar el commit.
18. Actualizar este documento.

### 11.1 Forma de colaboración

- No pedir confirmación entre pasos cuando el objetivo ya es claro.
- Pedir contenido real de archivos cuando la estructura sea necesaria.
- Entregar aplicadores y archivos completos dentro de un ZIP listo para extraer en la raíz.
- No entregar parches parciales destinados a copiar manualmente si puede prepararse un aplicador seguro.
- Mantener cambios conservadores y evitar modificar lógica funcional cuando el objetivo es visual.
- Validar siempre los tres estados relevantes: vacío, selección y controles desplegados.
- Después de cada bloque: pruebas, limpieza, commit, push y verificación de sincronización.

### 11.2 Aplicadores Node

Los aplicadores `.mjs` se entregan dentro de un ZIP listo para extraer en la raíz.

Deben:

- validar precondiciones;
- identificar versión;
- evitar doble aplicación;
- crear respaldos;
- restaurar si falla la escritura;
- no escribir si falla una validación;
- mostrar versión de Node, error y traza;
- ser compatibles con CRLF y LF;
- delimitar métodos Java de forma estructural;
- validar el resultado antes de escribir;
- modificar únicamente los archivos previstos.

Formato esperado de error:

```text
Aplicador: nombre y versión
Node.js: vXX.XX.X
ERROR: descripción
Error: traza real
No se aplicaron cambios nuevos.
```

Evitar:

- anclas genéricas con varias coincidencias;
- dependencia de espacios o saltos exactos;
- reemplazos literales frágiles en Java;
- marcadores verificados en el archivo incorrecto;
- escapes Java incorrectos;
- capturar y ocultar la traza real;
- escribir archivos antes de completar todas las validaciones.

### 11.3 Aprendizajes de los bloques recientes

- Si un campo `fx:id` se elimina del FXML, también deben retirarse su declaración `@FXML` y todas sus referencias Java.
- La selección automática debe ejecutarse después de preparar el panel vacío, no antes.
- Una clase múltiple en FXML debe respetar la sintaxis usada por el proyecto.
- Los ComboBox pueden cambiar de tamaño por diferencias de borde, padding o arrow-button entre `normal`, `focused` y `showing`.
- Ocultar un scrollbar no reemplaza el ajuste correcto de anchos. Las columnas deben sumar el ancho disponible.
- Un cambio de modo debe ser perceptible mediante más de una señal visual.
- Las búsquedas del aplicador deben contemplar que un patrón legítimo puede aparecer varias veces.

---

## 12. Criterios de finalización

Una tarea se considera terminada cuando:

- el código compila;
- las pruebas pasan;
- la función fue probada manualmente;
- no aparecen errores inesperados;
- no se rompieron flujos existentes;
- la base de datos quedó consistente;
- Docker funciona si el cambio lo afecta;
- no quedan aplicadores ni respaldos temporales;
- `git diff --check` no presenta errores;
- el commit fue revisado;
- se realizó el push acordado;
- `master` quedó sincronizada con `origin/master`;
- el estado quedó documentado.

---

## 13. Convenciones Git

Mensajes simples y descriptivos, sin prefijos como `feat:`.

Ejemplos:

```text
Agregar WhatsApp, historial de clientes y mejoras del dashboard
Mejorar torneos, cuadro eliminatorio, resultados y exportación PDF
Mejorar inscripciones y alta administrativa
Mejorar gestión y registro de pagos
```

Antes de cada commit:

```powershell
git status -sb
git --no-pager diff --stat
git --no-pager diff
git diff --check
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

La limpieza global debe ejecutarse únicamente cuando se confirmó que ningún aplicador o respaldo sigue siendo necesario.

Preparación y publicación:

```powershell
git add <archivos definitivos>
git diff --cached --stat
git diff --cached --check
git status --short
git commit -m "Mensaje simple y descriptivo"
git push origin master
git status -sb
git log -1 --oneline
```

El push nunca se ejecuta dentro de un aplicador.

---

## 14. Próximas etapas

La modernización visual principal y el bloque deportivo de torneos están cerrados. Las próximas etapas deben trabajarse en bloques pequeños.

### Etapa A: revisión funcional integral

Recorrido sugerido:

```text
Login
-> Inicio ADMIN
-> Inicio OPERADOR
-> Agenda
-> Reservas
-> Solicitudes web
-> Clientes
-> Canchas y bloqueos
-> Pagos
-> Caja
-> Estadísticas
-> Torneos
-> Inscripciones
-> Grupos
-> Partidos
-> Posiciones
-> Cuadro
-> Resultados
-> PDF
-> volver a Inicio
```

Verificar:

- acciones por estado;
- alta, edición, cancelación y reprogramación;
- navegación de ida y vuelta;
- apertura maximizada;
- controles recortados;
- botones con estilo nativo;
- scrollbars no normalizados;
- saltos de layout;
- errores de consola;
- conservación de selección;
- permisos por rol.

### Etapa B: permisos y seguridad funcional

- Probar ADMIN y OPERADOR.
- Confirmar restricciones de Estadísticas y finanzas.
- Revisar navegación, controladores y endpoints por rol.
- Verificar que ocultar una acción no sea la única protección.
- Revisar sesiones, expiración y revocación.

### Etapa C: limpieza técnica

- Consolidar CSS duplicado.
- Revisar marcadores históricos en FXML, CSS y Java.
- Unificar componentes reutilizables.
- Evaluar helpers para tablas, scrollbars, ComboBox y botones.
- Eliminar código obsoleto únicamente con pruebas.
- Evitar una limpieza masiva sin validación por módulo.

### Etapa D: estabilización y pruebas

- Ampliar pruebas automatizadas.
- Probar reservas simultáneas.
- Probar vencimientos y acreditaciones.
- Probar concurrencia de cupos.
- Probar filtros y selección automática.
- Probar cuadros con tamaños y BYE diferentes.
- Probar escenarios del motor eliminatorio.
- Revisar consola JavaFX, navegador y red.

### Etapa E: despliegue y seguridad operativa

- Separar desarrollo y producción.
- Verificar migraciones.
- Definir backups y restauración.
- Publicar API y web.
- Configurar dominio, HTTPS, correo y monitoreo.
- Mantener secretos fuera del repositorio.

### Etapa F: aplicación móvil

- Elegir tecnología.
- Reutilizar la API.
- Implementar registro, reservas, historial y perfil.
- Incorporar torneos, cuadros y resultados.
- Evaluar notificaciones.

---

## 15. Guía para retomar en una conversación nueva

Al iniciar un nuevo chat, compartir:

1. Este archivo actualizado.
2. La salida de:

```powershell
git status -sb
git log -1 --oneline
git --no-pager log --oneline -10
```

3. El módulo actual y la última prueba realizada.
4. Cualquier error de consola completo.
5. Capturas relevantes.
6. El contenido exacto de los archivos que se necesiten modificar.

Mensaje de continuidad recomendado:

```text
Estamos trabajando en sistema-reservas-padel.
Leé PLAN_DESARROLLO.md como contexto general, pero verificá siempre el código actual.
La rama estable es master y debe estar sincronizada con origin/master.
El último commit documentado es 3f35847 Mejorar gestión y registro de pagos.
La modernización principal está cerrada. El próximo bloque recomendado es la revisión funcional integral y de permisos.
No prepares cambios por suposición. Localizá y revisá primero los archivos reales.
Los aplicadores deben entregarse dentro de un ZIP listo para extraer en la raíz.
```

---

## 16. Resumen final del estado

- Web pública operativa.
- API pública operativa.
- Aplicación administrativa modernizada.
- Reservas y Solicitudes web revisadas.
- Clientes, canchas y módulos operativos disponibles.
- Gestión de pagos modernizada y validada.
- Inscripciones y alta administrativa terminadas.
- Vinculación de clientes terminada.
- Torneos, grupos, partidos, posiciones, cuadro y resultados desarrollados.
- Exportación y PDF disponibles.
- Cambios recientes confirmados y publicados.
- Repositorio limpio y sincronizado al cerrar el último bloque.

Próximo bloque recomendado:

```text
Revisión funcional integral, permisos ADMIN/OPERADOR y limpieza técnica posterior a la modernización
```
