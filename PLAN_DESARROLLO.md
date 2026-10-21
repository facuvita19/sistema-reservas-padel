# Plan de desarrollo del sistema de reservas de pádel

> Fuente de verdad estratégica y punto de continuidad del proyecto. Actualizado el 21 de octubre de 2026 después de cerrar y publicar R3.4B2G.


<!-- W2-COMUNIDAD-ESTADO-INICIO -->
## Actualización canónica de continuidad · 21 de octubre de 2026

### Estado cerrado

- **R3.3A–R3.3G / R3.4B1–R3.4B4:** base persistente, catálogo, preferencias, navegación, Turnos abiertos, solicitudes, participantes, concurrencia, auditoría, invitaciones privadas y contacto protegido completados en su alcance actual.
- **R3.4B5A · Fundación de notificaciones internas:** completada con persistencia privada, destinatario explícito, lectura individual y masiva, navegación contextual y proyección idempotente desde eventos auditables.
- **R3.4B5B · Centro global de notificaciones:** completado con campana, contador, acceso móvil, estados completos, foco contenido y rutas internas seguras.
- **R3.4B5C · Convergencia visual premium:** completada y revisada en escritorio, 390 px y 320 px, con centro de actividad, agrupación Nuevas/Anteriores, tarjetas semánticas, backdrop, hoja móvil, safe area y movimiento reducido.
- El contacto contextual por WhatsApp para invitaciones aceptadas está protegido por consentimiento vigente.

R3.4B5 dejó estabilizados:

- tabla comunidad_notificaciones separada de la auditoría histórica;
- API autenticada para listar y marcar notificaciones;
- aislamiento por cliente, idempotencia y enlaces internos validados;
- notificaciones actuales para solicitudes recibidas, aceptadas y rechazadas, e invitaciones enviadas, aceptadas y rechazadas;
- centro global visible solo con sesión autenticada;
- 374 pruebas Maven, Astro Check, build y 18 pruebas Playwright aprobadas;
- revisión visual oscura aprobada en los tres viewports objetivo.

### Próximo bloque exacto

> **R3.4B5D · Cobertura completa de eventos y cierre funcional de notificaciones**
> Correspondencia con el plan maestro: **R3.3H · Notificaciones y contacto**.

El siguiente bloque debe completar, antes de PWA y Push:

1. convocatoria completa;
2. convocatoria cerrada manualmente;
3. convocatoria cancelada o vencida cuando requiera aviso útil;
4. solicitudes cerradas por cupo o por el convocante;
5. títulos, mensajes y enlaces contextuales por evento;
6. preferencias y límites de frecuencia donde aporten valor;
7. pruebas de proyección, idempotencia, privacidad y destinatarios;
8. convergencia funcional final del centro de actividad.

PWA, Push y permisos del sistema quedan expresamente después de cerrar esta matriz interna; no deben acoplarse al dominio ni reemplazar el centro web.

### Regla de numeración

La hoja histórica R3.3 conserva la referencia funcional. R3.4B expresa el orden práctico actual y ambas referencias deben mantenerse para trazabilidad.
<!-- W2-COMUNIDAD-ESTADO-FIN -->

## 1. Lema rector

Plataforma Deportiva W2 debe ser **profesional, moderna e imponente**. Se acepta cualquier mejora, incluso mínima, costosa o una reconstrucción completa, cuando aumente la experiencia, claridad, visibilidad, calidad visual, código, arquitectura, accesibilidad, rendimiento, seguridad o mantenibilidad.

La versión anterior es únicamente una referencia funcional. No es modelo, límite ni estándar visual. Cada pantalla debe cerrarse con calidad definitiva antes de avanzar.

## 2. Punto actual de continuidad

### Estado ejecutivo

- Rama estable: `master`.
- Último commit publicado: `6e40745` (`Cerrar el sistema de invitaciones de Comunidad`).
- GitHub Actions de `6e40745`: verde antes de iniciar R3.4B5.
- Repositorio local: con R3.4B5A–C validado y pendiente de commit conjunto.
- Web activa: `web-publica-v2`.
- Aplicación administrativa: independiente de la web pública.
- Modo oscuro: experiencia principal.
- Modo claro: alternativa real y obligatoria.
- Torneos: cerrado integralmente.
- Rankings: cerrado en `40a313e`.
- Perfil público: recorrido R3.2B completado y convergido; el nombre excepcionalmente largo y la actividad móvil quedaron protegidos por contratos E2E.
- Reserva W2: ruta canónica `/w2/reservar`; `/reservar` queda como puente de redirección preservando query string y hash.
- Formato de reserva: cuatro jugadores, dos contra dos, sin selector editable.
- Comunidad: R3.4B1–R3.4B5C completados en su alcance actual.
- Próximo bloque real: **R3.4B5D / R3.3H · Cobertura completa de eventos de notificación**.

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
- W2.6 / R3.3: Comunidad avanzada hasta R3.4B5C; falta completar la matriz de notificaciones y la convergencia final.
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

### R3.3H · Notificaciones y contacto — en curso avanzado

- solicitudes e invitaciones internas: completadas en el centro global;
- WhatsApp contextual según consentimiento: completado para invitaciones aceptadas;
- cierre, convocatoria completa, cancelación y vencimiento: próximo alcance R3.4B5D;
- PWA y Push: posteriores a la matriz interna completa.

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

Indicar que Torneos y Rankings están cerrados, que el perfil público completó R3.2B y que Comunidad avanzó hasta R3.4B5C con solicitudes, invitaciones, contacto protegido y centro interno de notificaciones. El próximo bloque es R3.4B5D / R3.3H para completar la matriz de eventos antes de PWA y Push. Leer estas cinco guías y respetar privacidad, trazabilidad e inscripción exclusivamente por pareja.
