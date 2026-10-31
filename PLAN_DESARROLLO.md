# Plan de desarrollo del sistema de reservas de pádel

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

### Punto de continuidad

El siguiente bloque ejecutable es T2C1A: inventario real de Rankings, Perfil público y auditoría antes de modificar código.

> **Archivo histórico debajo de este punto.** Las fechas y próximos bloques anteriores quedan subordinados al estado autoritativo del 31 de octubre de 2026.


## Estado autoritativo · 30 de octubre de 2026

<!-- W2-ESTADO-ESCRITORIO-2026-10-30 -->

### Cierre de convergencia visual de escritorio

La revisión por sectores de la web pública quedó completada en su alcance actual. Los cierres más recientes son:

- Rankings premium: `18d838b`.
- Comunidad premium: `010ca8e`.
- Repositorio sincronizado en `master` después de ambos cierres.

Inicio, Reservar, Torneos, Rankings, Comunidad y Mi cuenta ya fueron recorridos y unificados con identidad W2. Esto no declara terminadas las evoluciones de producto, pero sí cierra la etapa de convergencia visual sector por sector del escritorio.

### Próximo bloque oficial · T1 Continuidad de navegación W2

1. Inventariar la navegación interna actual y las recargas completas perceptibles.
2. Extender el patrón fluido de Inicio al resto de las rutas W2.
3. Preservar cabecera global, tema, sesión y menú de cuenta durante la transición.
4. Mantener scroll y foco de forma predecible, incluida navegación Atrás/Adelante.
5. Diseñar estados transitorios breves sin flashes, saltos ni skeletons innecesarios.
6. Incorporar View Transitions con degradación segura solo cuando aporten continuidad real.
7. Validar accesibilidad, movimiento reducido y estabilidad con Playwright.

No corresponde abrir todavía una auditoría histórica general de rendimiento. Las mediciones se harán de manera proporcional dentro del bloque transversal y se ampliarán solo si aparece evidencia concreta.


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

### Consecuencias para el plan

- W2.4 incorpora ahora el centro privado completo y no solo cuenta, reservas y pagos.
- Mi cuenta deja de figurar como bloque pendiente en el orden de convergencia.
- La documentación histórica de R3.3I, PWA y Capacitor se conserva como registro, pero no define la continuidad vigente.


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

> Fuente de verdad estratégica y punto de continuidad del proyecto. Actualizado el 21 de octubre de 2026 después de cerrar y publicar R3.4B5D.


<!-- W2-COMUNIDAD-ESTADO-INICIO -->
## Comunidad y notificaciones · estado cerrado

R3.4B1–R3.4B5D / R3.3A–R3.3H quedó completado en su alcance actual:

- catálogo seguro, preferencias opt-in y navegación de Comunidad;
- convocatorias vinculadas a reservas reales, solicitudes, participantes, concurrencia y cierre manual;
- invitaciones privadas y contacto protegido por consentimiento vigente;
- `comunidad_eventos` como auditoría inmutable y `comunidad_notificaciones` como proyección privada e idempotente;
- centro global responsive con contador, lectura, agrupación y navegación contextual;
- solicitudes recibidas, aceptadas y rechazadas;
- convocatoria completa, cierre externo, cancelación y vencimiento;
- solicitudes cerradas por cupo o por el convocante;
- invitaciones enviadas, aceptadas, rechazadas, canceladas y vencidas;
- omisión deliberada de autoacciones y eventos que duplicarían avisos.

La clave de unicidad por evento y destinatario permite que un evento de convocatoria genere avisos para varias personas sin recrearlos. PWA y Push consumirán esta base, pero no se acoplarán al dominio ni reemplazarán el centro web.

### Continuidad

El siguiente bloque es **R3.3I · Convergencia final de Comunidad**: revisión transversal de privacidad, accesibilidad, rendimiento, estados extremos, contratos E2E, documentación y experiencia visual. Después comienza la fundación PWA y, sobre esa base estable, Capacitor para Android e iOS.
<!-- W2-COMUNIDAD-ESTADO-FIN -->

## 1. Lema rector

Plataforma Deportiva W2 debe ser **profesional, moderna e imponente**. Se acepta cualquier mejora, incluso mínima, costosa o una reconstrucción completa, cuando aumente la experiencia, claridad, visibilidad, calidad visual, código, arquitectura, accesibilidad, rendimiento, seguridad o mantenibilidad.

La versión anterior es únicamente una referencia funcional. No es modelo, límite ni estándar visual. Cada pantalla debe cerrarse con calidad definitiva antes de avanzar.

## 2. Punto actual de continuidad


<!-- W2-UNIFICACION-VISUAL-INICIO -->
## Punto vigente de continuidad visual

1. Inicio es la referencia autoritativa y no debe degradarse para igualarlo a páginas más antiguas.
2. Reservar está funcional y visualmente cerrado en su alcance actual.
3. Torneos mantiene su arquitectura completa; U1 unifica escalas, superficies, foco, filtros, desplegables y tarjetas sin modificar dominio ni navegación.
4. El siguiente trabajo debe continuar por secciones, comparando cada ruta con Inicio y Reservar, con cambios pequeños y validaciones proporcionales.
5. La revisión por sectores quedó completada; la continuidad es transversal y no responde a un nuevo orden de páginas.
6. La limpieza de CSS histórico se realiza después de fijar la estructura final de cada módulo, nunca antes ni mediante una reescritura ciega.

### Referencia visual autoritativa: Inicio W2

La ruta `/w2/` define la gramática visual de Plataforma Deportiva W2. Las demás secciones no deben copiar literalmente el HTML ni las clases `w2x-*`; deben trasladar sus principios a la función propia de cada recorrido.

### Composición autoritativa

1. **Cabecera flotante contenida**: navegación centrada en el viewport, superficie grafito propia, sin bandas laterales y geometría estable entre rutas y estados de sesión.
2. **Hero deportivo de primer frame**: fotografía real o recurso visual contextual, oscurecimiento por capas, contenido asimétrico, propuesta principal y módulo vivo visible sin desplazar la acción esencial fuera del primer viewport.
3. **Titular editorial**: escala display, interlineado compacto, espaciado negativo controlado y combinación de texto sólido con una línea o palabra contorneada. El contorno es un recurso de énfasis, no una obligación en todos los títulos.
4. **Kicker de identidad**: mayúsculas, tracking amplio, peso alto y línea roja corta. Debe introducir contexto, no repetir el título.
5. **Acciones jerarquizadas**: CTA rojo para la acción principal; acción secundaria grafito o transparente; icono o flecha integrada en un módulo compacto; foco visible y área táctil mínima.
6. **Panel contextual vivo**: superficie grafito profunda, borde discreto, radio asimétrico, iluminación radial roja moderada y datos reales con carga, contenido, vacío y error.
7. **Franja de propuesta de valor**: tres señales compactas conectan Reservas, Competencia y Comunidad. En otras rutas puede convertirse en métricas o accesos rápidos, pero debe conservar ritmo, alineación y utilidad.
8. **Secciones editoriales numeradas**: índice corto, título fuerte, explicación limitada y módulos alineados al contenido real. La numeración ordena la historia de la página.
9. **Bento y superficies**: paneles grafito con bordes finos, sombras moderadas y radios diagonales. La grilla responde al contenido; no se fuerzan alturas que generen vacío artificial.
10. **Profundidad y color**: negro y grafito estructuran; blanco establece jerarquía; rojo identifica y acciona. Verde queda reservado para éxito, clasificación o acreditación; ámbar para advertencia o espera; azul para Mercado Pago o identidad externa equivalente.
11. **Movimiento útil**: revelado breve, hover contenido y transformaciones pequeñas. `prefers-reduced-motion` elimina animaciones sin ocultar contenido.
12. **Cierre editorial**: la página concluye conectando Jugar, Competir y Perfil antes del footer global.

### Qué se replica y qué no

Se replican escala, ritmo, jerarquía, contraste, profundidad, comportamiento, focos, estados y lenguaje de controles. No se replica automáticamente la fotografía, el bento, la agenda, la cantidad de módulos ni las clases de Inicio. Reservar sigue siendo operativo; Torneos, competitivo; Rankings, analítico; Comunidad, de descubrimiento; Mi cuenta, de gestión personal.

### Archivos de referencia

- `src/pages/w2/index.astro`: composición y orden narrativo.
- `src/w2/styles/w2-home-v2.css`: resultado visual autoritativo de Inicio.
- `src/w2/scripts/inicio-v2.ts`: datos reales, estados y comportamiento de Inicio.
- `src/w2/styles/w2-ui-unification.css`: tokens transversales nuevos.
- `src/w2/styles/w2-tournaments-unification.css`: primera aplicación conservadora en Torneos.
- `src/w2/styles/w2-booking-v2.css`: referencia para recorridos operativos complejos.

### Regla de continuidad

Antes de modificar una sección se inspeccionan su Astro, CSS, scripts y E2E. La unificación debe preservar identificadores, URL, estado, accesibilidad y reglas de negocio. Se prefieren capas compartidas pequeñas y archivos por ruta antes que copiar clases de Inicio o acumular overrides indefinidos.
<!-- W2-UNIFICACION-VISUAL-FIN -->
### Estado ejecutivo

- Rama estable: `master`.
- Último commit publicado: `2735dfb` (`Completar eventos del centro de notificaciones`).
- GitHub Actions: CI proporcional e integral manual en verde.
- Repositorio local: limpio al cierre documental.
- Web activa: `web-publica-v2`.
- Aplicación administrativa: independiente de la web pública.
- Modo oscuro: experiencia principal.
- Modo claro: alternativa real y obligatoria.
- Torneos: cerrado integralmente.
- Rankings: convergencia visual cerrada en `18d838b`.
- Perfil público: recorrido R3.2B completado y convergido; el nombre excepcionalmente largo y la actividad móvil quedaron protegidos por contratos E2E.
- Reserva W2: ruta canónica `/w2/reservar`; `/reservar` queda como puente de redirección preservando query string y hash.
- Formato de reserva: cuatro jugadores, dos contra dos, sin selector editable.
- Comunidad: R3.4B1–R3.4B5D / R3.3A–R3.3H completados en su alcance actual.
- Próximo bloque real: **convergencia visual transversal de Plataforma Deportiva W2**, usando Inicio como referencia autoritativa.

### Validaciones recientes

- Fundación R3.3A: 318 pruebas Maven aprobadas, migración aplicada y aplicación JavaFX iniciada correctamente.
- Reserva W2: 10 pruebas focalizadas aprobadas antes del cierre de CI.
- Cierre local de regresiones: 439 pruebas aprobadas, 1 omitida y 1 contrato residual corregido después; la prueba residual focalizada quedó aprobada.
- R3.4B5: 374 pruebas Maven, migración aplicada, API Docker saludable, Astro Check y build aprobados, más 18 pruebas Playwright funcionales y visuales.

## 3. Forma de trabajo acordada

### 3.1 Entrega de cambios

- Cuando estén reunidos los datos necesarios, entregar directamente un ZIP descargable.
- El ZIP debe extraerse directamente en la raíz del proyecto.
- Debe contener los archivos completos y un aplicador seguro; Node.js es preferido para transformaciones extensas.
- El aplicador debe comprobar la raíz, validar precondiciones, evitar modificaciones parciales, escribir en UTF-8, ejecutar `git diff --check` y restaurar si falla.
- No entregar archivos sueltos cuando el cambio pertenece al proyecto.
- No enviar un aviso previo si la solución ya está preparada.

### 3.2 Respuesta posterior al ZIP

Indicar solamente:

1. cómo extraerlo y ejecutar el aplicador;
2. salida correcta esperada;
3. un único bloque de comandos de validación;
4. resultado esperado;
5. comprobación visual o funcional breve cuando corresponda.

No pedir que se copie una salida correcta. Solo se comparte salida cuando aparece un error.

### 3.3 Comandos y seguridad

- Usar PowerShell compatible con el entorno real.
- No usar `Min` ni `Max` en PowerShell.
- Preferir Node.js para inspecciones, reemplazos y recortes extensos.
- No crear TXT para salidas cortas.
- Antes de cualquier comando que pudiera mostrar contraseñas, tokens o secretos, advertirlo explícitamente y redactar la salida siempre que sea posible.
- No solicitar comandos adicionales si no existe un error específico.
- No pedir adjuntar archivos; las salidas extensas se comparten por partes.

### 3.4 Validación proporcional

- Cambios pequeños: sintaxis, `git diff --check`, pruebas específicas y comprobación visual breve.
- Bloques funcionales: suite focalizada completa del área.
- Cambios críticos, cierres amplios o releases: Maven, E2E integral, CI, instalación limpia, backups, restauración y smoke tests según riesgo.
- No repetir suites costosas después de cada corrección puntual.
- No esperar CI para cada ajuste menor; sí esperar CI en cierres funcionales amplios, cambios críticos o hitos.
- Las pruebas manuales o costosas se agrupan al cierre del recorrido salvo riesgo específico.

### 3.5 Commits

- Mensajes simples y descriptivos, sin prefijos como `feat:`.
- Commit y push por bloques coherentes.
- No mezclar archivos ajenos al bloque.
- Confirmar `git status --short` vacío al cierre.

## 4. Producto y arquitectura vigente

### Administración

- Java 25, Maven y JavaFX.
- Roles ADMINISTRADOR y OPERADOR.
- OPERADOR no accede a Estadísticas ni resúmenes financieros mensuales.
- Inicio y navegación adaptados por rol.
- Gestión de clientes es referencia visual administrativa.

### API y datos

- API Java independiente, accesible normalmente mediante `/api/`.
- MySQL 8.4, base `padel_reservas`.
- Cookies seguras para sesión.
- Tokens sensibles almacenados mediante hash.
- Migraciones reproducibles y equivalencia entre actualización e instalación limpia.

### Web pública

- `web-publica-v2` es la única web activa.
- `web-publica` es referencia histórica.
- Astro, TypeScript y Nginx.
- Rutas W2 canónicas con puentes heredados controlados.
- Estado persistente en URL cuando aporta navegación profunda y recuperación de contexto.

## 5. Reglas deportivas obligatorias

### Inscripción

- Todas las inscripciones de torneos de pádel son exclusivamente por pareja.
- No existen inscripciones individuales, tampoco en Americanos.
- Comunidad ayuda a encontrar una pareja antes de la inscripción; nunca transforma la inscripción en individual.

### Rama

- **Libre**: pareja integrada por personas de cualquier sexo.
- **Femenino**: torneo de parejas femeninas.
- **Mixto**: exige una mujer y un hombre.
- Mixto define composición, no categoría ni formato competitivo.
- Los valores internos heredados pueden mantenerse temporalmente, pero la web publica **Libre**.

### Dimensiones independientes

- Rama: Libre, Femenino, Mixto.
- Formato: Eliminación directa o Grupos + eliminación.
- Elegibilidad: categoría única, categorías combinadas, Suma 8, Suma 10, Suma 12, etc.
- Modalidad: un set, dos sets más super tie-break o mejor de tres sets.
- Dinámica: mismo día o varios días.
- Americano: tipo especial de evento rápido.

Nunca usar “suma máxima”.

### Americanos

- Ocasionales y rápidos, normalmente resueltos el mismo día.
- Inscripción siempre por pareja.
- Admiten eliminación directa o grupos más eliminación.
- Admiten un set o dos sets más super tie-break.
- No son competencias semanales ni mensuales.
- No mostrar “Convencional” para los demás torneos.

## 6. Estado de Plataforma Deportiva W2

- W2.0: dirección, arquitectura y mapa inicial completados.
- W2.1: identidad deportiva y categorías históricas, alcance inicial completado.
- W2.2: historial competitivo auditable, alcance inicial completado.
- W2.3: rankings y perfiles públicos estabilizados en su recorrido visual actual; motor avanzado, temporadas, publicaciones históricas, variaciones y Masters siguen como evolución posterior.
- W2.4: cuenta, reservas y pagos operativos; Torneos cerrado integralmente.
- W2.5: reserva inteligente operativa en su flujo actual; quedan evoluciones funcionales futuras.
- W2.6 / R3.3: Comunidad completó R3.3A–R3.3H; R3.3I y la convergencia visual están cerradas en el alcance actual.
- Mercado Pago sandbox: completado.
- Mercado Pago producción: pendiente de dominio, HTTPS, credenciales productivas, observabilidad y operación controlada.

## 7. Comunidad: estado y hoja de ruta

### R3.3A · Fundación persistente — completada

Incluye:

- dominio de preferencias, convocatorias, solicitudes, participantes y eventos;
- estados y motivos de cierre;
- DAOs y adaptadores MySQL;
- migración `MigracionComunidadR33A`;
- pruebas de dominio y contrato de migración;
- auditoría como principio estructural.

### R3.3B · Catálogo real de jugadores disponibles — completado en su alcance actual

1. **R3.3B1 · Contrato público y consulta segura**
   - DTO público específico;
   - opt-in de visibilidad;
   - exclusión de documento, correo, teléfono completo, información comercial y pagos;
   - categoría vigente, posición, disponibilidad, objetivos y resumen deportivo permitido.
2. **R3.3B2 · Filtros, orden y paginación**
   - nombre, categoría, posición, objetivo, disponibilidad y aceptación de invitaciones;
   - paginación estable y filtros persistentes en URL cuando corresponda.
3. **R3.3B3 · Catálogo y tarjetas premium**
   - perfil, categoría, posición, disponibilidad y acción contextual;
   - datos reales desde el primer render funcional.
4. **R3.3B4 · Estados completos**
   - carga, vacío, error, recuperación, sesión y privacidad.
5. **R3.3B5 · Responsive, accesibilidad y cierre visual**
   - oscuro, claro, escritorio, 390 px y 320 px;
   - teclado, foco, movimiento reducido y E2E focalizados.

### R3.3C · Preferencias desde Mi cuenta

- activar o pausar visibilidad;
- configurar objetivos, posición, categorías, días y franjas;
- aceptar invitaciones y contacto por WhatsApp;
- previsualizar la publicación;
- gestionar privacidad y solicitudes.

### R3.3D · Búsqueda de pareja para torneos

- descubrir perfiles compatibles;
- contactar principalmente por WhatsApp;
- coordinar previamente;
- inscribirse después como pareja.

### R3.3E · Convocatorias para completar turnos

Caso central:

> “3 de 4, falta 1 jugador de 5.ª”.

La convocatoria se vincula con una reserva real e informa cancha, fecha, horario, cupos, categoría buscada, posición y mensaje permitido.

### R3.3F · Solicitudes y cierre manual

- solicitar unirse;
- aceptar, rechazar, cancelar o vencer solicitudes;
- cerrar automáticamente al completar cupos;
- cerrar manualmente cuando el titular complete el turno fuera de la web;
- cerrar la convocatoria no cancela ni modifica la reserva.

### R3.3G · Participantes, concurrencia y auditoría

- impedir duplicados y sobrecupos;
- transacciones para aceptación;
- idempotencia;
- origen del participante;
- historial inmutable de acciones relevantes.

### R3.3H · Notificaciones y contacto — completado

- solicitudes, invitaciones, cierres, convocatoria completa, cancelación y vencimiento: cubiertos;
- WhatsApp contextual según consentimiento: operativo para invitaciones aceptadas;
- proyección privada, idempotente y desacoplada de Push;
- PWA y Push quedan como evolución posterior.

### R3.3I · Convergencia final

- visual definitiva;
- privacidad;
- rendimiento;
- accesibilidad;
- revisión manual y E2E integral.

## 8. Principios funcionales de Comunidad

- Comunidad es infraestructura deportiva, no una red social genérica.
- Nadie se publica automáticamente: la visibilidad es opt-in.
- El teléfono no se expone en listados públicos.
- El backend decide visibilidad y acciones permitidas.
- Los torneos siguen requiriendo pareja completa.
- La convocatoria de una reserva no altera el contrato comercial de la reserva.
- El cierre manual impide nuevas solicitudes sin cancelar la reserva.
- Toda acción sensible debe ser autorizada, transaccional y auditable.
- No se simulan datos productivos salvo fixtures E2E identificados.

## 9. Calidad y operación

- `git diff --check` obligatorio.
- Sin secretos en salidas compartidas.
- Backups, checksum y restauración cuando el riesgo lo requiera.
- Mercado Pago con backend, webhooks, idempotencia y conciliación.
- Cada bloque funcional cierra contratos, visual, responsive, accesibilidad y documentación antes de avanzar.

## 10. Criterios de aceptación

Un bloque se considera terminado cuando:

1. alcance definido;
2. reglas de negocio y privacidad explícitas;
3. cambios coherentes con arquitectura y lema rector;
4. sintaxis válida;
5. `git diff --check` correcto;
6. pruebas proporcionales aprobadas;
7. revisión visual o funcional realizada;
8. oscuro, claro y responsive cubiertos cuando hay interfaz;
9. sin exposición de secretos ni datos personales;
10. archivos previstos únicamente;
11. documentación actualizada;
12. commit y push por bloque;
13. repositorio limpio;
14. CI verde cuando el riesgo o el hito lo requieren.

## 11. Archivos que deben leerse al retomar

1. `PLAN_DESARROLLO.md`.
2. `docs/PLATAFORMA_DEPORTIVA_W2.md`.
3. `docs/ARQUITECTURA_FUNCIONAL_W2.md`.
4. `docs/DIRECCION_VISUAL_W2.md`.
5. `docs/MAPA_EXPERIENCIA_W2.md`.
6. últimos commits de `master`.
7. `git status --short`.
8. resultado del último workflow de GitHub Actions.

## 12. Instrucción para un chat nuevo

Leer primero este archivo y `docs/CONTINUIDAD_DEL_PROYECTO.md`. Inicio es la referencia visual autoritativa; Reservar está cerrado en su alcance actual y Torneos inició la convergencia U1. Continuar la unificación por rutas, preservar contratos funcionales y mantener la futura aplicación móvil como producto independiente. Respetar la entrega por ZIP, las validaciones proporcionales, la privacidad, la trazabilidad y las inscripciones exclusivamente por pareja.

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
