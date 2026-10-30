# Continuidad del proyecto y método de trabajo

## Estado autoritativo · 30 de octubre de 2026

<!-- W2-ESTADO-ESCRITORIO-2026-10-30 -->

### Punto exacto para retomar

La web pública de escritorio ya fue revisada por sectores. Rankings quedó publicado en `18d838b` y Comunidad en `010ca8e`. Inicio, Reservar, Torneos, Rankings, Comunidad y Mi cuenta están convergidos visualmente en su alcance actual; la cabecera, las navegaciones sticky y el menú de cuenta comparten el sistema global vigente.

La continuidad inmediata es **T1 · navegación interna y transiciones W2**. Debe partir del comportamiento fluido de Inicio y extenderlo a las demás rutas sin convertir la web en una SPA, sin romper URLs, historial, foco, scroll, sesión, tema ni degradación progresiva.

Orden recomendado:

1. mapa de enlaces y navegaciones internas;
2. transición entre rutas principales;
3. conservación de shell y estados globales;
4. scroll, foco y navegación Atrás/Adelante;
5. carga transitoria y prevención de flashes;
6. pruebas proporcionales y capturas oscuras por viewport;
7. documentación y commit de cierre.

No volver automáticamente a Rankings, Comunidad ni a la antigua auditoría integral de rendimiento. Mercado Pago productivo y la aplicación móvil independiente siguen siendo evoluciones posteriores.


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

### Punto exacto para retomar

1. Verificar el último workflow asociado a `c143387`.
2. Mantener Mi cuenta como referencia privada ya cerrada y no reconstruirla de nuevo sin un defecto concreto.
3. Seleccionar la próxima ruta pública de escritorio utilizando Inicio como referencia.
4. Aplicar validación proporcional y reservar la suite integral para el siguiente hito.


<!-- W2-ESTADO-CANONICO-INICIO -->
## Estado canónico · 27 de octubre de 2026

- Rama estable y publicada: `master`.
- Plataforma Deportiva W2 quedó publicada como repositorio público de portfolio y la convergencia documental integral anterior cerró en `73b7598`.
- El bloque funcional R3.4B5D / R3.3H cerró en `2735dfb`; R3.3I fue implementado y superado por bloques posteriores de Comunidad.
- Inicio W2 es la referencia visual autoritativa de toda la web pública.
- Reservar quedó cerrado en su recorrido actual: selección, horarios, datos, confirmación, Mercado Pago, transferencia, estados, accesibilidad y contratos E2E.
- Torneos conserva su arquitectura funcional y comenzó una convergencia visual transversal U1, sin reescribir catálogo, detalle, inscripción ni centro competitivo.
- La prioridad vigente es unificar progresivamente tipografía, superficies, botones, filtros, desplegables, navegación secundaria, estados y densidad de todas las rutas W2.
- El modo oscuro es la experiencia principal; el modo claro sigue siendo una alternativa real.
- La experiencia de escritorio se termina y estabiliza primero. La aplicación móvil futura será independiente de la web y se decidirá mediante prototipos medidos.
- CI proporcional, CodeQL, Secret Scanning y npm audit permanecen como controles de calidad y seguridad; las cifras puntuales deben verificarse en el último workflow antes de publicarlas como actuales.
<!-- W2-ESTADO-CANONICO-FIN -->

> Guía operativa para retomar `sistema-reservas-padel` en otra conversación, fecha o equipo sin perder contexto ni repetir errores.

## 1. Cómo comenzar una conversación nueva


<!-- W2-CONTINUIDAD-VISUAL-INICIO -->
## Punto vigente de continuidad visual

1. Inicio es la referencia autoritativa y no debe degradarse para igualarlo a páginas más antiguas.
2. Reservar está funcional y visualmente cerrado en su alcance actual.
3. Torneos mantiene su arquitectura completa; U1 unifica escalas, superficies, foco, filtros, desplegables y tarjetas sin modificar dominio ni navegación.
4. El siguiente trabajo debe continuar por secciones, comparando cada ruta con Inicio y Reservar, con cambios pequeños y validaciones proporcionales.
5. La revisión por sectores quedó completada; la continuidad es transversal y no responde a un nuevo orden de páginas.
6. La limpieza de CSS histórico se realiza después de fijar la estructura final de cada módulo, nunca antes ni mediante una reescritura ciega.

### Lectura obligatoria en otro chat

Además de este archivo, leer `docs/DIRECCION_VISUAL_W2.md`, en especial la anatomía completa de Inicio. No pedir nuevamente capturas o código ya documentados salvo que el árbol haya cambiado o exista una discrepancia visual concreta. Para una nueva sección, inspeccionar primero sus archivos reales y sus pruebas.

### Método de convergencia

- comparar contra Inicio sin clonar Inicio;
- conservar la composición adecuada a cada función;
- unificar tipografía, controles, superficies, estados y navegación;
- evitar nuevas capas extensas de `!important`;
- validar primero la ruta afectada;
- reservar la suite integral y CI para cierres amplios.
<!-- W2-CONTINUIDAD-VISUAL-FIN -->
Leer primero:

1. `PLAN_DESARROLLO.md`.
2. Este documento.
3. `README.md`.
4. Últimos commits de `master`.
5. Última ejecución de GitHub Actions.

Plantilla recomendada para pegar en un chat nuevo:

```text
Estoy trabajando en el proyecto sistema-reservas-padel.

Antes de proponer cambios, revisá PLAN_DESARROLLO.md, docs/CONTINUIDAD_DEL_PROYECTO.md, README.md y los últimos commits de master.

No puedo adjuntar archivos al chat. Si necesitás inspeccionar algo, prepará un comando o inspector para que copie la salida completa o la envíe por partes.

Los cambios deben entregarse en un ZIP para extraer directamente en la raíz del proyecto, con un aplicador autocontenido y backup local.

Antes de cualquier comando cuya salida pueda mostrar contraseñas, tokens u otros secretos, advertime explícitamente y usá salida redactada. No uses docker compose config expandido en información que deba compartir.

Después de aplicar cambios, indicá solo las validaciones necesarias, el resultado esperado, los archivos del commit y un mensaje de commit simple.

El punto actual está documentado en PLAN_DESARROLLO.md. Inicio es la referencia visual autoritativa; la continuidad visual está detallada en docs/DIRECCION_VISUAL_W2.md.
```

## 2. Limitaciones del canal

- El usuario no puede adjuntar archivos al chat.
- Los archivos se inspeccionan mediante comandos o inspectores generados.
- Las salidas extensas se copian completas desde PowerShell o se envían por partes.
- No volver a pedir archivos adjuntos.
- No hacer depender la continuidad de información que solo exista en el chat.

Cuando un informe sea largo:

```text
Parte 1 de 3
[contenido]

Parte 2 de 3
[contenido]

Parte 3 de 3
[contenido]
```

El orden debe conservarse. Si una parte devuelve salida vacía, el informe terminó.

## 3. Flujo normal de colaboración

1. Copilot propone una inspección de solo lectura.
2. Copilot advierte si podría aparecer información sensible.
3. El usuario ejecuta en PowerShell desde la raíz.
4. El usuario copia la salida completa o por partes.
5. Copilot analiza antes de modificar.
6. Copilot entrega un ZIP para extraer en la raíz.
7. El ZIP incluye un aplicador `.js` o `.mjs` autocontenido, normalmente Node.js.
8. El aplicador crea backup local antes de escribir.
9. El usuario ejecuta el aplicador.
10. Se realizan validaciones mínimas y concretas.
11. Se revisa visual o funcionalmente cuando corresponde.
12. Se agregan solo los archivos previstos al commit.
13. Se usa un mensaje simple y descriptivo, sin prefijos tipo `feat:`.
14. Se hace push a `master`.
15. Se comprueban solo los jobs activados por el CI proporcional; en hitos se usa `workflow_dispatch` integral.
16. Se eliminan reparadores temporales.
17. `git status --short` debe quedar vacío.
18. Se actualiza la continuidad si el cambio fue significativo.

## 4. Formato obligatorio de entregas

Cada cambio debe priorizar:

- un solo ZIP aplicable;
- extracción directa en la raíz;
- aplicador determinista;
- backup local `.backup-*`;
- validación de archivos y anclas antes de modificar;
- sintaxis del aplicador con `node --check`;
- sintaxis PowerShell cuando corresponda;
- `git diff --check`;
- salida esperada;
- comandos mínimos;
- archivos exactos para el commit;
- mensaje de commit simple;
- comprobación visual o funcional específica.

No pedir confirmación entre pasos cuando el objetivo y alcance ya estén definidos.

## 5. Seguridad de la información

Reglas obligatorias:

- Advertir antes de cualquier comando que pueda mostrar secretos.
- Preferir salidas redactadas o estados `OK`/`ERROR`.
- No pedir ni compartir el contenido de `.env`.
- No compartir `docker compose config` sin `--quiet`.
- Los inspectores no deben leer `.env`, salvo que la tarea lo exija explícitamente.
- Si leen configuración privada, nunca deben imprimir valores.
- No imprimir contraseñas MySQL, SMTP, tokens, cookies ni claves.
- No imprimir checksums completos si no resulta necesario.
- Ante exposición accidental, rotar el secreto.
- `.env`, backups, estados locales y registros de despliegue no se versionan.
- Las pruebas SMTP requieren destinatario explícito y confirmación `ENVIAR`.

## 6. Comandos y salidas de riesgo

### Riesgo alto

- `docker compose config` expandido.
- `Get-Content .env`.
- comandos que vuelquen variables de entorno.
- logs con headers de autenticación.
- consultas que impriman tokens o hashes completos.

Antes de solicitar cualquiera, explicar el riesgo y ofrecer una alternativa redactada.

### Alternativas seguras

- `docker compose config --quiet`;
- validadores que solo informan nombres y estados;
- filtros que reemplazan valores por `[REDACTADO]`;
- auditorías sobre `git ls-files` sin leer archivos privados.

## 7. Problemas recurrentes y prevención

### Interpolación JavaScript y PowerShell

Problema: una plantilla JavaScript interpreta `${variablePowerShell}` antes de generar el script.

Prevención:

- evitar `${...}` de PowerShell dentro de templates JavaScript;
- usar una lista de líneas;
- usar concatenación PowerShell;
- ejecutar `node --check` siempre.

### Variable PowerShell seguida por dos puntos

Incorrecto:

```powershell
"$clave: error"
```

Correcto:

```powershell
"${clave}: error"
```

### Variables automáticas

PowerShell no distingue mayúsculas de minúsculas. No usar `$home` porque colisiona con `$HOME`. Usar nombres específicos como `$respuestaWeb`.

### `-d` interpretado como `-Debug`

Dentro de funciones PowerShell, usar `--detach` o una lista explícita de argumentos.

### Docker Compose queda mostrando logs

Comprobar que recibió realmente `--detach`. No asumir que `-d` atravesó una función wrapper.

### MySQL responde antes de estar inicializado

- `mysqladmin ping` no demuestra credenciales listas.
- Usar `SELECT 1` autenticado.
- El healthcheck de instalación debe verificar esquema final crítico.

### Migraciones duplicadas

Cuando el esquema base ya contiene una evolución, la migración correspondiente debe ser idempotente. No eliminarla del historial.

### Diferencias Windows y Linux

Validar:

- rutas ausentes;
- sensibilidad de mayúsculas;
- comandos disponibles;
- PowerShell en Linux;
- permisos;
- saltos de línea;
- archivos binarios o grandes;
- acciones oficiales y runtime Node.

### LF y CRLF

El aviso `LF will be replaced by CRLF` es informativo si `git diff --check` aprueba.

### Aplicadores parciales

Si un aplicador falla:

1. determinar si escribió antes del fallo;
2. no asumir que todo se revirtió;
3. inspeccionar `git status --short`;
4. crear un reparador específico;
5. no volver a ejecutar versiones anteriores.

### Expresiones regulares sobre código Java

No reemplazar métodos completos con regex amplia. Usar anclas estructurales, contar coincidencias y validar compilación.

## 8. Validaciones estándar

### Java

```powershell
mvn clean test
```

`mvn javafx:run` solo se indica cuando una validación específica requiere abrir la aplicación administrativa; no forma parte de la rutina habitual.

Resultado esperado: todas las pruebas vigentes y `BUILD SUCCESS`. No fijar una cantidad en este documento porque la suite aumenta con cada bloque.

### Web

```powershell
Set-Location .\web-publica-v2
npm run check
npm run build
```

Ejecutar Playwright cuando el cambio afecte experiencia web.

### Docker

```powershell
docker compose config --quiet
docker compose ps
```

No compartir la configuración expandida.

### Salud

```powershell
.\monitorear-salud.ps1
```

Resultado esperado: `SALUDABLE`.

### Secretos

```powershell
.\auditar-secretos.ps1
```

### Instalación limpia

```powershell
.\validar-instalacion-limpia.ps1
```

### Backup y restauración temporal

```powershell
.\backup-mysql.ps1
$backup = Get-ChildItem .\backups\padel_reservas_*.sql | Sort-Object LastWriteTime -Descending | Select-Object -First 1
.\validar-restauracion-backup.ps1 -Archivo $backup.FullName
```

## 9. Reglas de Git

- Revisar `git status --short` antes y después.
- Agregar rutas explícitas, no `git add .` en bloques sensibles.
- Revisar `git diff --cached --stat`.
- Commits simples y descriptivos.
- Push a `master` solo después de validar localmente.
- Comprobar `git log -1 --oneline`.
- Esperar CI verde según el nivel de riesgo y el cierre del bloque.
- No versionar aplicadores, backups ni informes locales.

## 10. Política de cadencia y espera de CI

No es necesario detener todo el trabajo cada vez que GitHub Actions está ejecutándose. La regla general es:

> Se puede inspeccionar, analizar, diseñar y preparar el siguiente bloque mientras corre CI, pero no se debe publicar otro commit importante sobre una base cuyo resultado todavía no se conoce.

### Nivel 1: cambios pequeños y locales

Ejemplos:

- correcciones de redacción;
- comentarios;
- documentación que no modifica CI;
- ajustes visuales pequeños y aislados;
- limpieza sin impacto funcional demostrado.

Forma de trabajo:

- se pueden agrupar varios cambios relacionados;
- no es obligatorio esperar CI después de cada commit pequeño;
- se valida CI al cerrar el bloque lógico;
- si el cambio toca documentación validada por el workflow, pasa al Nivel 2.

### Nivel 2: cambios funcionales normales

Ejemplos:

- una pantalla administrativa;
- una validación de negocio;
- mejoras de servicios;
- cambios en reservas, clientes, torneos o estadísticas;
- ajustes relevantes de la web pública.

Forma de trabajo:

1. cerrar el bloque;
2. ejecutar las pruebas aplicables localmente;
3. crear commit y push;
4. dejar CI ejecutándose;
5. mientras CI corre, se puede analizar o preparar el siguiente bloque;
6. no publicar el siguiente commit funcional hasta que el anterior quede verde.

### Nivel 3: cambios críticos

Siempre se debe esperar CI verde antes de publicar el siguiente cambio cuando se modifique:

- base de datos, esquema o migraciones;
- autenticación, sesiones o recuperación;
- permisos y roles;
- pagos, caja o información financiera;
- Docker, imágenes, healthchecks o Compose;
- workflows de GitHub Actions;
- secretos, configuración o CORS;
- backups, restauración, despliegue o rollback;
- dependencias principales;
- lógica deportiva de clasificación o cuadros;
- una release o etiqueta estable.

### Excepción temporal: cierre de versión estable

Durante un cierre formal de producción o antes de crear una etiqueta estable:

- cada bloque publicado debe quedar verde antes del siguiente push;
- no se mezclan reparaciones con nuevas funciones;
- cada fallo se atribuye al commit exacto;
- la etiqueta solo se crea sobre un commit limpio, sincronizado y completamente verde.

### Qué significa esperar

Esperar CI no significa quedarse sin trabajar. Durante la ejecución se permite:

- inspeccionar archivos;
- analizar resultados anteriores;
- diseñar la solución siguiente;
- preparar borradores;
- construir un aplicador en un entorno temporal;
- revisar documentación;
- identificar riesgos y criterios de aceptación.

No se debe:

- publicar otro commit importante;
- etiquetar una versión;
- desplegar como estable;
- mezclar un fallo aún no diagnosticado con otro bloque.

### Decisión rápida

Antes de cada push, clasificar el cambio:

- **Nivel 1:** continuar y validar al cerrar el bloque.
- **Nivel 2:** preparar en paralelo, pero esperar verde antes del siguiente push funcional.
- **Nivel 3:** esperar verde obligatoriamente.
- **Cierre de release:** esperar verde entre todos los commits.

## 11. Decisiones que no deben revertirse accidentalmente

- La web activa es `web-publica-v2`.
- El puerto web vigente es `4321`.
- `database/docker-init` está versionado deliberadamente.
- Todas las migraciones versionadas se conservan; las de pagos incorporan el núcleo de órdenes e intentos sin reescribir el historial.
- La API espera la salud final de MySQL.
- Las pruebas temporales no tocan la base principal.
- La API usa usuario no privilegiado y filesystem de solo lectura.
- OPERADOR no ve Estadísticas ni información financiera restringida.
- Gestión de clientes es la referencia visual administrativa.
- El estándar premium grafito y verde se mantiene.
- La calidad prima sobre el costo de implementación.

## 12. CI proporcional vigente

- Higiene se ejecuta siempre.
- Java/Maven se activa para backend, recursos y base de datos.
- Astro/TypeScript se activa para frontend.
- Playwright se activa para frontend y API pública relevante; en push normal utiliza escritorio Chromium y en pull request o ejecución manual usa la matriz completa.
- Docker limpio se reserva para migraciones, Compose, Dockerfiles y scripts operativos.
- `concurrency` cancela ejecuciones obsoletas.
- El repositorio público tiene CodeQL, Secret Scanning, Push Protection, Dependabot y reporte privado de vulnerabilidades.

## 13. Mantenimiento de la continuidad

Actualizar este documento cuando cambie:

- la forma de colaboración;
- una limitación del canal;
- un comando de riesgo;
- una herramienta principal;
- un problema recurrente;
- el procedimiento de entrega o validación.

El estado funcional y la hoja de ruta pertenecen a `PLAN_DESARROLLO.md`. Este documento conserva el método para llegar al siguiente estado de forma segura.

## 14. Estado vigente del bloque de pagos

Cierre funcional alcanzado el 14 de octubre de 2026:

- P4.1: núcleo transaccional de órdenes de pago.
- P4.2: modelo de intentos y estados online.
- P4.3: configuración neutral de pasarela.
- P4.4A: cliente y contratos de Mercado Pago.
- P4.4B: creación y consulta pública de órdenes online.
- P4.5: webhook, eventos e idempotencia de acreditación.
- P4.6: experiencia de pago en Reservar y Mi cuenta.
- P4.6b: corrección visual del selector y compatibilidad de capacidades.

### Validación confirmada

La transferencia manual fue probada correctamente desde el Paso 4: selección del método, creación de la orden, alias, importe exacto, contador, comprobante y seguimiento. Mi cuenta conserva el estado de la reserva.

### Pendiente antes de declarar Mercado Pago productivo

1. Configurar credenciales privadas de prueba sin imprimirlas.
2. Comprobar que la API informe pago online disponible.
3. Crear un checkout real de prueba.
4. Probar retorno aprobado, pendiente y rechazado.
5. Confirmar recepción e idempotencia del webhook.
6. Verificar acreditación, conciliación y actualización de la reserva.
7. Ejecutar la suite completa y CI por tratarse de un cambio Nivel 3.

No considerar una reserva pagada por la URL de retorno. La fuente de verdad es la consulta del proveedor procesada por el backend y registrada transaccionalmente.

## 15. Cierre Git del bloque P4

Los cambios P4.1 a P4.6 forman un núcleo interdependiente. Si permanecen acumulados en el mismo árbol de trabajo, se prefieren dos commits íntegros y compilables:

1. `Agregar pagos transaccionales y experiencia de reserva en W2`.
2. `Actualizar documentación y continuidad de pagos W2`.

Los diagnósticos, aplicadores, ZIP y carpetas `.backup-*` no se versionan.

### P4.7: configuración segura de Mercado Pago

La configuración de Mercado Pago se transporta al contenedor API mediante variables `MP_*`, documentadas sin secretos en `.env.example`. El valor predeterminado de `MP_ENABLED` es `false`; por lo tanto, una instalación nueva conserva transferencia manual y no intenta conectarse al proveedor.

Para pruebas integrales, `MP_PUBLIC_BASE_URL` debe ser HTTPS y públicamente accesible. Nunca imprimir `MP_ACCESS_TOKEN` ni `MP_WEBHOOK_SECRET`; los inspectores solo deben informar `CONFIGURADA` o `AUSENTE`.


## CAP1 · Fundación Android

Capacitor Android se integra sobre el build estático de Astro. El proyecto nativo vive en `web-publica-v2/android`, usa `ar.com.padelasflores.w2` y se valida primero con `W2_Pixel_7_API_36`. La conexión nativa con la API y los contratos de sesión se cierran en el siguiente bloque antes de considerar distribución.


## CAP2 · Conectividad Android local

El prototipo Android tiene dos modos explícitos. `npm run cap:sync:dev` conecta el emulador con `http://10.0.2.2:4321/w2/` para consumir la web, la API y la sesión reales del entorno Docker bajo un mismo origen. `npm run cap:sync:prod` elimina la URL remota y vuelve a empaquetar los activos locales. El tráfico HTTP queda limitado por Network Security Config al host especial del emulador y no se habilita globalmente para producción.


### CAP2 · CSP compatible con Android local

La CSP del servidor web autoriza mediante hashes SHA-256 los dos scripts inline legítimos generados por Astro para W2. `upgrade-insecure-requests` no se envía desde el servidor HTTP local, evitando que el WebView convierta los recursos del emulador a HTTPS. La política conserva `script-src self`, no incorpora `unsafe-inline` y mantiene bloqueada cualquier inyección distinta de los hashes publicados.


### CAP2 · tema inicial Android

El host de desarrollo del emulador Android (10.0.2.2) inicia W2 en modo oscuro cuando todavía no existe una preferencia guardada. Una elección explícita del usuario continúa prevaleciendo. La web convencional conserva la preferencia del sistema como valor inicial.


### CAP2P · perfil de fluidez Android

Línea base en emulador Pixel 7 API 36: 417 cuadros, 13,67 % de jank, P50 30 ms, P90 48 ms, P95 65 ms y P99 150 ms. El perfil móvil elimina desenfoques de superficies fixed/sticky, evita promoción GPU permanente del encabezado, reduce sombras de composición, limita transiciones costosas y aplica content-visibility a secciones largas. No altera la estructura funcional ni el perfil visual de escritorio.

## CAP2P · evaluación de fluidez Android

La evaluación en el emulador Pixel 7 API 36 confirmó que el entorno virtual condiciona de forma importante la percepción de fluidez. La configuración local recomendada para continuar pruebas es 4 GB de RAM, 4 núcleos, GPU host y VM heap de 512 MB; estos ajustes pertenecen al AVD local y no se versionan.

El perfil experimental de reducción visual CAP2P no demostró una mejora concluyente y se retiró del producto para preservar la experiencia premium. Quedan registradas como referencias: línea base de 417 cuadros con 13,67 % de jank; ensayo CAP2P de 590 cuadros con 23,39 %; y ensayo con AVD ajustado de 1012 cuadros con 18,68 %. Las muestras no son estrictamente equivalentes por diferencias de recorrido y duración.

La evaluación definitiva de fluidez queda pendiente en un dispositivo Android físico con activos empaquetados mediante `npm run cap:sync:prod`. Antes de distribución deberán validarse arranque, scroll, menú, modales, navegación Atrás, áreas seguras, actualización e instalación. CAP2 queda funcionalmente cerrado en emulador con conectividad Docker, API, sesión, navegación, CSP y modo oscuro operativos.

## CAP3A · APK Android para dispositivo fisico local

El modo local fisico usa `npm run cap:sync:device` y conecta el WebView con `http://192.168.0.3:4321/w2/` dentro de la misma red privada. La politica Android permite HTTP exclusivamente para ese host, mientras el resto permanece bloqueado. Este APK es solo de prueba local: depende de Docker activo en la PC, del firewall privado para el puerto 4321 y de que el celular comparta la misma red Wi-Fi. La distribucion productiva queda pendiente hasta disponer de una API HTTPS publica y firma release fuera del repositorio.


## CAP3B · Experiencia nativa Android

La aplicación física integra áreas seguras, teclado ajustable, barras oscuras y botón Atrás jerárquico. Atrás cierra primero modales, menús, notificaciones, filtros y detalles; luego navega por el historial y exige una segunda pulsación para salir desde la raíz. El icono adaptativo y el splash usan la identidad W2 existente. La pantalla de bienvenida con acceso o modo invitado queda reservada para CAP3C.


## CAP3B2 · Convergencia de interacción móvil

La validación física detectó cierres táctiles inconsistentes y desajustes de overlays al abrir el teclado. CAP3B2 unifica el cierre jerárquico del botón Atrás, adapta drawers al visual viewport, garantiza objetivos táctiles mínimos y preserva la identidad visual. El rediseño definitivo del icono y la bienvenida Ingresar/Invitado quedan para CAP3C.

### CAP3B3 · teclado, viewport y drawers móviles

La experiencia Android ajusta los drawers al visualViewport real cuando aparece el teclado, mantiene visibles la cabecera y el cierre, desplaza el campo enfocado dentro del panel y oculta temporalmente el pie cuando el espacio vertical es insuficiente. El botón Atrás oculta primero el teclado antes de cerrar la capa activa. La respuesta táctil de los cierres se refuerza sin alterar la identidad visual.

### CAP3B4 · drawer de Comunidad y teclado

El drawer móvil de Comunidad adopta una geometría estructural de cuatro filas: cabecera, búsqueda, filtros desplazables y acción. La altura se deriva del visualViewport real, el teclado se abre solo cuando la persona toca el campo, el botón Atrás oculta primero el teclado y el cierre ofrece respuesta táctil visible.

### CAP3B5 · anclaje del drawer al teclado

El drawer móvil de Comunidad conserva altura compacta sin teclado y, al abrirlo, se ajusta al visualViewport real sin superponer campos ni dejar espacio artificial dentro del panel. Cabecera, contenido desplazable y acción mantienen su orden, mientras el campo enfocado se centra dentro del scroll interno.

### CAP3B6 · geometria unica del drawer Android

Se reemplazaron las reglas experimentales acumuladas de CAP3B2 a CAP3B5 por una unica geometria. Android adjustResize reduce el WebView al abrir el teclado; el drawer queda anclado al borde inferior de ese viewport, usa altura automatica, contenido central desplazable y pie dentro del flujo, evitando la doble compensacion y la franja vacia.


### CAP3B7 · teclado nativo Android

La aplicación usa @capacitor/keyboard como fuente única para detectar apertura, cierre y altura del teclado. La geometría móvil distingue si Android ya redimensionó el WebView y evita compensar dos veces el espacio disponible. El drawer de Comunidad conserva el estado compacto sin teclado y una zona interna desplazable cuando el teclado reduce el área útil.


### CAP3B8 · sistema global de filtros W2

Comunidad, Torneos e historial de reservas convergen en una mecanica adaptable: en movil los filtros se despliegan dentro del flujo de la pagina, sin portales, fondos fijos ni calculos del teclado; en escritorio conservan su distribucion amplia. Rankings y Reservar mantienen navegacion y pasos especializados, compartiendo el lenguaje visual sin tratarse como filtros temporales.


### CAP3B8C · limpieza definitiva de filtros

Comunidad, Torneos e Historial usan un único controlador de filtros integrado en el flujo. Se retiraron portales, backdrops, bloqueos de documento y reposicionamientos nativos del teclado para estos paneles. Rankings conserva sus categorías como navegación y Reservar mantiene su flujo transaccional especializado.


### CAP3B8D - retiro definitivo de capas heredadas

El sistema global de filtros elimina fisicamente los backdrops heredados de Comunidad y Torneos, retira la referencia residual al controlador anterior y excluye los filtros integrados del reposicionamiento nativo del teclado. Los filtros quedan en el flujo normal de la pagina y ninguna capa fija puede cubrir el contenido situado sobre el teclado.


### CAP3B8F · teclado superpuesto sin redimensionar WebView

Android usa adjustNothing como unica politica de ventana frente al teclado. El WebView conserva su altura, el complemento Keyboard informa la altura real del IME y la capa web agrega solo espacio de desplazamiento temporal para mantener visible el campo enfocado. Se retiran los calculos de redimensionamiento duplicado.


### CAP3B8G · compatibilidad IME y SystemBars

La aplicacion Android evita la doble compensacion del teclado causada por la combinacion de viewport-fit=cover y el manejo automatico de insets de SystemBars. W2 conserva decorFitsSystemWindows, usa adjustResize como unica estrategia IME y desactiva la mutacion adicional de insets de SystemBars.


### CAP3B · experiencia nativa Android cerrada

CAP3B quedó validado en dispositivo físico: safe areas correctas, botón Atrás jerárquico, teclado sin doble aplicación de insets, SystemBars con insetsHandling deshabilitado, filtros globales integrados en Comunidad, Torneos e historial, y APK local instalable sobre la misma Wi-Fi. Se conservaron únicamente las pruebas finales de experiencia nativa y filtros. El próximo bloque oficial es CAP3C: bienvenida, acceso o invitado, icono definitivo y splash premium.


### CAP3C1 · bienvenida e identidad nativa

Se incorpora una bienvenida exclusiva de la app instalada, restauracion previa de sesion, continuidad persistente como invitado, acceso mediante la autenticacion existente y una nueva base visual W2 para icono y splash de Android. La web conserva su entrada directa.

### CAP3C2 · Entrada estable y sesión opcionalmente persistente

La bienvenida nativa se muestra en cada apertura sin sesión persistente, no guarda la elección de invitado, evita el destello inicial y suspende su capa durante autenticación. Login incorpora “Mantener la sesión iniciada”: desmarcado emite cookie temporal; marcado conserva la cookie persistente HttpOnly.


### CAP3C3_ENTRADA_ESTABLE
- Bienvenida nativa sin destello: la sesion se resuelve sobre una superficie oscura neutra.
- Invitado recordado solo durante la ejecucion actual mediante sessionStorage.
- Sesion persistente entra directo sin mostrar la bienvenida.
- Acciones moviles apiladas, a ancho completo y resistentes al escalado de fuente.
- Autenticacion suspende la bienvenida hasta cerrar o completar el acceso.

## Punto de continuidad posterior al cierre del prototipo movil (25 de octubre de 2026)

- La prioridad inmediata vuelve a ser la auditoria, limpieza y optimizacion integral de la web publica W2 de escritorio.
- La web usa Astro y debe quedar premium, profesional, moderna e imponente en escritorio, notebook, tablet y responsive web.
- El prototipo Android basado en envolver toda la web con Capacitor queda cerrado y documentado; no debe retomarse como arquitectura movil definitiva.
- La futura app sera un producto independiente. Antes de implementarla se compararan React Native y Flutter con el mismo vertical slice y mediciones en dispositivo real.
- Se conservaron fuera del experimento: Dependabot sin saltos mayores automaticos de TypeScript y Rankings editorial sin fotografia conceptual.
- Proximo bloque recomendado: inventario de deuda visual y CSS, rendimiento real por ruta, presupuesto de imagenes/fuentes/scripts, tema claro/oscuro autoritativo y validacion visual de escritorio.
- No reintroducir CAP5, CAP5B ni w2-mobile-definitive.css. Cualquier futura geometria de teclado debe medirse y documentarse antes de incorporarse.

## Plan maestro de finalización W2 (25 de octubre de 2026)

La revisión integral posterior al commit `6a0be52` confirmó 17 rutas públicas y 152 pruebas detectadas. La web está avanzada, pero no terminada.

Documentos autoritativos nuevos:

- `docs/ESTADO_FUNCIONAL_W2.md`
- `docs/AUDITORIA_VISUAL_W2.md`
- `docs/PLAN_FINALIZACION_W2.md`
- `docs/DEFINICION_DE_TERMINADO_W2.md`

Hallazgos principales:

1. `/w2/` es un prototipo visual con datos demostrativos y debe converger con la lógica real de `src/scripts/portada.ts`.
2. Cuenta tiene Reservas, Pagos sandbox, Perfil deportivo y Comunidad avanzados, pero Datos personales y Seguridad todavía redirigen a `/mis-datos`.
3. Reservas, Torneos, Rankings y Comunidad están funcionalmente avanzados y cuentan con pruebas amplias; requieren cierre de estados extremos, convergencia y mejora visual de escritorio.
4. Mercado Pago productivo continúa pendiente.
5. Deben converger las rutas heredadas con `/w2/*`.
6. Existe deuda CSS y documental acumulada; la limpieza debe hacerse después de fijar las estructuras finales, por módulos y con pruebas.
7. El prototipo Android quedó cerrado; sus activos generados no deben contaminar auditorías, Docker ni la arquitectura web.
8. Toda mejora visual o reconstrucción que eleve la experiencia puede incorporarse sin limitarse por costo.

Próximo bloque oficial: `F0. Consolidar estado y herramientas`, seguido por `F1. Inicio real y reconstrucción visual premium`.

## F0 completado: consolidación web y retiro del prototipo Android

F0 retiró del producto web activo el proyecto Android de Capacitor, sus dependencias, scripts, capas públicas nativas, runtime condicional y nueve pruebas históricas. La experiencia PWA permanece como estrategia web. Se creó `docs/MATRIZ_RUTAS_W2.md` y se clasificaron las rutas heredadas como transición sin nuevas capacidades. F1 quedó realizado: `/w2/` es el Inicio real y la referencia visual autoritativa. El bloque vigente es la convergencia gradual de las demás rutas W2.

<!-- W2-TORNEOS-U2-CIERRE -->
## Cierre visual U2 de Torneos

El catálogo principal de `/w2/torneos` quedó cerrado visual y funcionalmente en escritorio y móvil. El bloque U2A-U2E consolidó el hero editorial, el torneo destacado, el calendario, la búsqueda contextual, las pestañas por estado, los filtros persistentes de escritorio, el panel móvil y los estados vacíos.

- El hero conserva la identidad competitiva y permite ver el inicio del calendario en el primer viewport de escritorio.
- El destacado finalizado representa progreso competitivo mediante partidos, no ocupación por parejas.
- Los estados vacíos navegan hacia competencias disponibles y los estados filtrados limpian únicamente los criterios.
- Rama, Formato y Disponibilidad comparten escala, alineación, foco y microinteracciones.
- La cabecera móvil mantiene logo, tema y Menú dentro del viewport.
- El panel móvil fue validado en 390 x 844, 320 x 760 y 320 x 640, con footer sticky, scroll interno y objetivos táctiles preservados.
- El centro competitivo presenta recuperación diseñada para categorías inválidas y errores de carga.
- La validación focalizada U2C-U2E terminó con 3 pruebas aprobadas; la validación ampliada U1-U2D terminó con 9 pruebas aprobadas; Astro quedó sin errores.

La continuidad vigente es T1: navegación interna y transiciones W2, tomando Inicio como referencia de fluidez.
