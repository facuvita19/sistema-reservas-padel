# Continuidad del proyecto y método de trabajo

<!-- W2-ESTADO-CANONICO-INICIO -->
## Estado canónico · 21 de octubre de 2026

- Rama estable y publicada: `master`.
- Commit de cierre vigente: `2735dfb` (`Completar eventos del centro de notificaciones`).
- Árbol local al cerrar: limpio.
- Repositorio público orientado a portfolio técnico, con licencia de evaluación y todos los derechos reservados.
- Historial y árbol actual auditados sin secretos fuertes; Secret Scanning informa cero secretos abiertos.
- CodeQL cerró las cinco alertas detectadas y mantiene cero alertas abiertas.
- `npm audit` informa cero vulnerabilidades conocidas.
- CI proporcional operativo: detecta alcance, ejecuta higiene siempre y activa Java, Astro, Playwright o Docker solo cuando corresponden; `workflow_dispatch` y pull requests conservan la validación integral.
- Validación integral más reciente: Java/Maven, Astro/TypeScript, Playwright completo e instalación Docker limpia en verde.
- R3.4B5A–D / R3.3H está cerrado: persistencia, centro global, convergencia visual y matriz completa de eventos internos.
- Estrategia móvil oficial inicial: Astro → PWA completa → Capacitor → Android → iOS. Ionic visual, React/Preact, React Native, Flutter u otras herramientas siguen disponibles si el prototipo no alcanza el estándar premium.
- R3.3I · Convergencia final de Comunidad quedó cerrado en `3b1688f`. PWA1 y PWA2 establecieron instalación, actualización y modo offline; PWA3 prepara la operación productiva y el siguiente bloque será el prototipo Capacitor Android.
<!-- W2-ESTADO-CANONICO-FIN -->

> Guía operativa para retomar `sistema-reservas-padel` en otra conversación, fecha o equipo sin perder contexto ni repetir errores.

## 1. Cómo comenzar una conversación nueva

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

El punto actual está documentado en PLAN_DESARROLLO.md.
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
