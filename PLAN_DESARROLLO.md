# Plan de desarrollo del sistema de reservas de pádel

> Fuente de verdad estratégica y punto de continuidad del proyecto. Actualizado el 16 de octubre de 2026 después del commit `b8bc937` (`Separar la carga y caché de torneos`).

## 1. Punto actual de continuidad

### Estado ejecutivo

El producto combina una aplicación administrativa JavaFX, una API pública Java, MySQL y la web activa `web-publica-v2`. La evolución principal se denomina **Plataforma Deportiva W2** y debe superar a la versión anterior en arquitectura, diseño, experiencia, accesibilidad, rendimiento, seguridad y mantenibilidad.

Estado confirmado:

- rama estable: `master`;
- commit de referencia: `b8bc937`;
- repositorio limpio después del push;
- web activa: `web-publica-v2`;
- Astro y TypeScript: `0 errores`, con 3 hints preexistentes ajenos a Torneos;
- Torneos W2: 177 pruebas cubiertas, incluidas 3 validaciones específicas de descarte de solicitudes obsoletas;
- Mercado Pago sandbox: flujo completo validado;
- modo oscuro: experiencia principal de la web pública;
- modo claro: alternativa real y mantenida;
- aplicación administrativa: producto independiente de la web pública.

### Objetivo inmediato

Continuar la reconstrucción interna de Torneos W2 desde el estado real de `/w2/torneos`.

Bloques completados recientemente:

1. R2.1A: contrato de navegación, URL, historial, caché y solicitudes protegidas.
2. R2.1B: nomenclatura pública **Libre**, separada de **Mixto**.
3. R2.1C1: dominio compartido de Torneos.
4. R2.1C2: estado y persistencia URL del catálogo.
5. R2.1C2V: geometría uniforme de tarjetas.
6. R2.1C3: adaptador tolerante del catálogo.
7. R2.1C4: carga, caché, cancelación, versiones e invalidación selectiva.

Próximo bloque: **R2.1C5**, separación de la presentación y navegación del detalle del torneo, sin alterar la interfaz validada.

### Módulos extraídos de `torneos.ts`

```text
web-publica-v2/src/scripts/torneos/
├── adaptador-catalogo-torneos.ts
├── datos-torneos.ts
├── dominio-torneos.ts
└── estado-catalogo-torneos.ts
```

## 2. Forma de trabajo acordada

### 2.1 Entrega de cambios

- Cuando estén reunidos los datos necesarios, entregar directamente un ZIP descargable.
- El ZIP debe poder extraerse en la raíz del proyecto.
- Debe contener un aplicador Node.js y, cuando aporte valor, un LEEME breve.
- El aplicador debe validar precondiciones, crear respaldo local, escribir de forma atómica, ejecutar `git diff --check` y restaurar automáticamente si falla.
- No entregar archivos sueltos cuando el cambio corresponde al proyecto.
- No enviar un aviso previo si la solución ya está preparada.

### 2.2 Respuesta posterior al ZIP

Indicar solamente:

1. cómo extraerlo y ejecutar el aplicador;
2. salida correcta esperada;
3. un único bloque de comandos de validación;
4. resultado esperado;
5. comprobación visual o funcional breve, cuando corresponda.

No pedir que se copie una salida correcta. El usuario solo enviará salida cuando exista un error.

### 2.3 Comandos

- Usar PowerShell compatible con el entorno real del usuario.
- No usar `Min` ni `Max` en PowerShell para recortar o inspeccionar texto.
- Preferir scripts Node.js simples para inspecciones extensas.
- No crear TXT para salidas cortas.
- Antes de cualquier comando que pudiera exponer contraseñas, tokens o secretos, advertirlo y redactar la salida siempre que sea posible.
- No solicitar comandos adicionales si no existe un error específico.
- Evitar repetir suites costosas después de una corrección puntual cuando las pruebas restantes ya pasaron.

### 2.4 Validación proporcional

- Bloques pequeños: sintaxis, `git diff --check`, pruebas específicas y comprobación visual breve.
- Cierres funcionales: suite amplia aplicable.
- Cambios críticos, releases o producción: CI, instalación limpia, backups, restauración y smoke tests.
- No repetir las 150+ pruebas E2E ni esperar CI para cada ajuste menor.
- Las validaciones manuales o costosas se agrupan al cierre, salvo riesgo específico.

### 2.5 Commits

- Commits simples y descriptivos, sin prefijos como `feat:`.
- Hacer commit y push por bloques coherentes.
- No mezclar archivos ajenos al bloque.
- Después del push, comprobar que `git status --short` quede vacío.

## 3. Producto y arquitectura vigente

### Administración

- Java 25, Maven y JavaFX.
- Roles ADMINISTRADOR y OPERADOR.
- OPERADOR no ve Estadísticas ni resúmenes financieros mensuales.
- Inicio y navegación adaptados por rol.
- Gestión de clientes es referencia visual administrativa.

### API y datos

- API Java independiente, normalmente accesible mediante `/api/`.
- MySQL 8.4, base `padel_reservas`.
- Sesiones mediante cookies seguras.
- Tokens sensibles almacenados mediante hash.
- Instalación Docker reproducible y migraciones conservadas.

### Web pública

- `web-publica-v2` es la única web activa.
- `web-publica` es referencia histórica.
- Astro, TypeScript y Nginx.
- Shell W2, reservas, cuenta, pagos, rankings, perfiles y Torneos en convergencia integral.

## 4. Reglas deportivas obligatorias

### Inscripción

- Todas las inscripciones de torneos de pádel son exclusivamente por pareja.
- No existen inscripciones individuales, tampoco en torneos Americanos.
- La futura Comunidad servirá para encontrar pareja antes de inscribirse.

### Rama

- **Libre**: la pareja puede estar integrada por personas de cualquier sexo.
- **Femenino**: torneo destinado a parejas femeninas.
- **Mixto**: exige una pareja formada por una mujer y un hombre.
- Mixto define composición de la pareja, no categoría ni formato.
- Los valores internos heredados `MASCULINA`, `MASCULINO` y `ABIERTO` pueden mantenerse temporalmente, pero la web publica **Libre**.

### Dimensiones independientes

- Rama: Libre, Femenino, Mixto.
- Formato competitivo: Eliminación directa o Grupos + eliminación.
- Condición de inscripción: categoría única, categorías combinadas, Suma 8, Suma 10, Suma 12, etc.
- Modalidad de partido: un set, dos sets más super tie-break o mejor de tres sets.
- Dinámica: mismo día o varios días.
- Americano: tipo especial de evento rápido, no un formato competitivo.

Nunca usar “suma máxima”. Los nombres públicos son **Suma 8**, **Suma 10**, **Suma 12**, etc.

### Americanos

- Eventos ocasionales y rápidos.
- Normalmente terminan el mismo día.
- Siempre se inscriben parejas.
- Pueden usar grupos más eliminación o eliminación directa.
- Pueden jugarse a un set o dos sets más super tie-break.
- No se modelan como competencias semanales o mensuales.
- No mostrar “Convencional” como etiqueta pública para el resto de los torneos. Solo los Americanos necesitan un distintivo específico.

## 5. Estado de Plataforma Deportiva W2

- W2.0: arquitectura, dirección visual y mapa inicial completados.
- W2.1: identidad deportiva y categorías históricas, alcance inicial completado.
- W2.2: historial competitivo auditable, alcance inicial completado.
- W2.3: ranking y perfiles públicos reales operativos; quedan temporadas, reglas versionadas, ventana móvil, publicaciones históricas, variaciones y Masters.
- W2.4: cuenta, reservas, pagos y Torneos en convergencia avanzada.
- Mercado Pago sandbox: completado.
- Mercado Pago producción: pendiente de dominio estable, HTTPS, credenciales productivas y operación controlada.

### Torneos W2

Ruta canónica:

```text
/w2/torneos
```

No existe una sección principal llamada Competir. Torneos es la sección principal y contiene catálogo, detalle y centro competitivo.

Recorrido previsto:

1. catálogo;
2. detalle con Información, Categorías y Reglamento;
3. categoría persistente por URL;
4. Resumen;
5. Posiciones;
6. Partidos;
7. Cuadro;
8. convergencia y retiro controlado.

## 6. Dirección de producto

- Calidad visual definitiva antes de avanzar a la pantalla siguiente.
- Resultado premium, profesional, moderno, imponente y contemporáneo.
- Mejoras mínimas son válidas si elevan claridad, visibilidad o experiencia.
- Se aceptan reconstrucciones completas cuando aportan valor real.
- Innovación con estabilidad, accesibilidad, seguridad, rendimiento y mantenibilidad.
- La web anterior es referencia funcional, no límite ni plantilla.

## 7. Calidad y operación

- Java, Astro, TypeScript y Playwright según riesgo.
- `git diff --check` obligatorio.
- Sin secretos en salidas compartidas.
- Backups con checksum y restauración temporal para cierres operativos.
- Despliegue y rollback documentados.
- Mercado Pago con backend, webhooks, idempotencia y conciliación.

## 8. Criterios de aceptación

Un bloque se considera terminado cuando:

1. alcance definido;
2. cambios coherentes con arquitectura y reglas deportivas;
3. sintaxis válida;
4. `git diff --check` correcto;
5. pruebas proporcionales aprobadas;
6. comprobación visual o funcional realizada;
7. sin exposición de secretos;
8. archivos previstos únicamente;
9. commit y push por bloque cuando corresponda;
10. repositorio limpio al cierre.

## 9. Archivos que deben leerse al retomar

1. `PLAN_DESARROLLO.md`.
2. `docs/PLATAFORMA_DEPORTIVA_W2.md`.
3. `docs/ARQUITECTURA_FUNCIONAL_W2.md`.
4. `docs/DIRECCION_VISUAL_W2.md`.
5. `docs/MAPA_EXPERIENCIA_W2.md`.
6. `docs/CONTINUIDAD_DEL_PROYECTO.md`, si existe y está vigente.
7. últimos commits de `master`.
8. `git status --short`.

## 10. Instrucción para un chat nuevo

Al comenzar otro chat, indicar que la continuidad real está en **Plataforma Deportiva W2, Torneos, R2.1C5**, y que deben respetarse las reglas de trabajo de la sección 2. No volver al estado documental anterior ni asumir que “Copa Metropolitana E2E” es un torneo real: es un fixture controlado de Playwright.
