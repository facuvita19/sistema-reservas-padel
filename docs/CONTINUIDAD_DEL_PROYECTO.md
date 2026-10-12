# Continuidad del proyecto y método de trabajo

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
7. El ZIP incluye un aplicador `.mjs` autocontenido.
8. El aplicador crea backup local antes de escribir.
9. El usuario ejecuta el aplicador.
10. Se realizan validaciones mínimas y concretas.
11. Se revisa visual o funcionalmente cuando corresponde.
12. Se agregan solo los archivos previstos al commit.
13. Se usa un mensaje simple y descriptivo, sin prefijos tipo `feat:`.
14. Se hace push a `master`.
15. Se comprueban los cuatro trabajos de CI.
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
mvn javafx:run
```

Resultado vigente esperado: 250 pruebas y `BUILD SUCCESS`.

### Web

```powershell
cd .\web-publica-v2
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
- Esperar CI verde.
- No versionar aplicadores, backups ni informes locales.

## 10. Decisiones que no deben revertirse accidentalmente

- La web activa es `web-publica-v2`.
- El puerto web vigente es `4321`.
- `database/docker-init` está versionado deliberadamente.
- Las 27 migraciones se conservan.
- La API espera la salud final de MySQL.
- Las pruebas temporales no tocan la base principal.
- La API usa usuario no privilegiado y filesystem de solo lectura.
- OPERADOR no ve Estadísticas ni información financiera restringida.
- Gestión de clientes es la referencia visual administrativa.
- El estándar premium grafito y verde se mantiene.
- La calidad prima sobre el costo de implementación.

## 11. Mantenimiento de la continuidad

Actualizar este documento cuando cambie:

- la forma de colaboración;
- una limitación del canal;
- un comando de riesgo;
- una herramienta principal;
- un problema recurrente;
- el procedimiento de entrega o validación.

El estado funcional y la hoja de ruta pertenecen a `PLAN_DESARROLLO.md`. Este documento conserva el método para llegar al siguiente estado de forma segura.
