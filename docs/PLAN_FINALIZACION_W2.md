# Plan maestro de finalización de Plataforma Deportiva W2

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

### Ubicación en el plan maestro

T2C1–T2D completan la continuidad transversal de F8. F9 solo se amplía con mediciones y F10 certifica el cierre final. Mercado Pago productivo conserva su propio riesgo Nivel 3.

> **Archivo histórico debajo de este punto.** Las fechas y próximos bloques anteriores quedan subordinados al estado autoritativo del 31 de octubre de 2026.


## Actualización de fase · 30 de octubre de 2026

<!-- W2-ESTADO-ESCRITORIO-2026-10-30 -->

La convergencia visual por sectores de escritorio se considera completa en su alcance actual. Los últimos cierres fueron Rankings `18d838b` y Comunidad `010ca8e`.

El trabajo continúa dentro de F8 con **T1 · experiencia transversal de navegación**:

- navegación interna fluida;
- transiciones con degradación segura;
- shell, tema y sesión estables;
- restauración de scroll y foco;
- historial Atrás/Adelante;
- estados transitorios sin flashes;
- movimiento reducido;
- pruebas proporcionales.

F9 no se inicia como auditoría integral automática. Rendimiento se medirá durante T1 y solo se ampliará ante evidencia. F10 se mantiene posterior a la estabilización transversal.


Base: commit `6a0be52`
Fecha: 25 de octubre de 2026

## Objetivo final

Declarar la web W2 funcionalmente completa, conectada a datos reales, visualmente premium, accesible, segura, optimizada, operable y documentada. La futura aplicación móvil independiente comienza únicamente después de este cierre.

## Orden obligatorio

### F0. Consolidar estado y herramientas

Estado: COMPLETADO el 25 de octubre de 2026.

Objetivo: preparar una base inequívoca.

- excluir activos Android generados de auditorías y contexto Docker;
- decidir si el prototipo Android se archiva, ignora o retira;
- clasificar pruebas históricas de Capacitor;
- actualizar documentación histórica contradictoria;
- crear matriz de rutas heredadas y W2;
- fijar presupuestos de CSS, JavaScript, imágenes y solicitudes.

Cierre:

- documentación vigente única;
- repositorio limpio;
- build/check en verde;
- sin borrar todavía rutas con funcionalidad no migrada.

### F1. Inicio real y reconstrucción visual premium

Objetivo: terminar `/w2/`.

- trasladar contratos reutilizables de `portada.ts` a una arquitectura W2;
- cargar complejo, canchas, disponibilidad y torneo real;
- personalizar por sesión y reservas;
- incorporar ranking y Comunidad solo con datos reales;
- eliminar todos los datos demostrativos;
- crear estados carga/vacío/error;
- reconstruir el escritorio en oscuro y claro;
- añadir pruebas E2E específicas del Inicio real.

Cierre:

- cero datos simulados;
- visitante y autenticado validados;
- rendimiento y capturas aprobados.

### F2. Cuenta completa: Datos y Seguridad

Objetivo: eliminar las redirecciones funcionales a `/mis-datos`.

- integrar edición de datos personales;
- integrar cambio de contraseña;
- estados de validación, guardado, éxito y error;
- sincronizar cabecera, avatar y sesión;
- separar sesión vencida de fallo parcial;
- agregar pruebas E2E y backend necesarias.

Cierre:

- Datos y Seguridad funcionan dentro de `/w2/cuenta`;
- no hay enlaces temporales de migración.

### F3. Reorganizar Cuenta y Comunidad privada

Objetivo: convertir Cuenta en centro personal definitivo.

- crear sección propia Comunidad;
- mover preferencias, invitaciones, solicitudes y convocatorias;
- conservar Perfil deportivo para categoría, ranking e historial;
- dividir `cuenta.ts` por dominios;
- extraer CSS embebido de `cuenta.astro`;
- mejorar navegación lateral y resumen por prioridad;
- evitar que APIs secundarias derriben toda la cuenta.

Cierre:

- secciones independientes y recuperables;
- diseño premium de escritorio;
- pruebas existentes preservadas y ampliadas.

### F4. Convergencia definitiva de rutas

Objetivo: establecer `/w2/*` como experiencia principal.

- inventariar `/`, `/reservar`, `/mis-reservas`, `/mis-datos`, `/torneos` y `/restablecer`;
- migrar cualquier función exclusiva;
- definir redirecciones permanentes o puentes;
- actualizar enlaces, sitemap, canonical, pruebas y documentación;
- evitar dos interfaces completas mantenidas en paralelo.

Cierre:

- cada capacidad tiene una única ruta autoritativa;
- compatibilidad heredada documentada.

### F5. Reservas y pagos funcionalmente definitivos

Objetivo: cerrar el recorrido reserva → pago → cuenta.

- usar duración y moneda autoritativas;
- revisar identificadores reserva/solicitud;
- validar doble envío, timeout e idempotencia;
- sincronizar Inicio, Cuenta y disponibilidad;
- consolidar detalle e historial;
- completar Mercado Pago productivo con HTTPS, webhook, firma, retornos y reconciliación;
- documentar preflight, operación y rollback.

Cierre:

- sandbox y producción validados;
- aprobado, pendiente, rechazado y vencido probados;
- ningún estado depende de la URL de retorno.

### F6. Torneos y Rankings finales

Objetivo: certificar competencia e identidad deportiva.

- validar estados extremos de Torneos;
- validar inscripción por parejas y reglas Libre/Femenino/Mixto/Suma N;
- comprobar convergencia administrativa;
- verificar fórmula y desempates de Rankings;
- decidir alcance obligatorio de temporadas;
- mejorar catálogo, detalle, centro, tabla y auditoría;
- reutilizar navegación secundaria flotante como patrón global donde aporte valor.

Cierre:

- datos auditables y consistentes;
- rutas profundas preservadas;
- pruebas y capturas aprobadas.

### F7. Comunidad y notificaciones finales

Objetivo: cerrar recorridos transversales.

- conectar Inicio con actividad real;
- validar invitación, solicitud, aceptación, rechazo, cancelación y vencimiento;
- validar contacto protegido y revocación;
- comprobar cierre manual y completo;
- reconciliar notificaciones y contadores;
- mejorar catálogo, turnos y detalle en escritorio.

Cierre:

- matriz de notificaciones validada de punta a punta;
- privacidad y concurrencia certificadas.

### F8. Tema, PWA y experiencia transversal

Objetivo: estabilizar la plataforma completa.

- modo oscuro principal y claro completo;
- primera carga sin parpadeo;
- estrategia de actualización PWA;
- limpieza de cachés viejos;
- offline y reintento por ruta;
- autenticación/restablecimiento coherentes;
- sesión entre pestañas y vencimiento;
- accesibilidad WCAG funcional;
- validación responsive web.

Cierre:

- ambos temas y todos los estados validados;
- actualización visible e inmediata cuando corresponda.

### F9. Consolidación técnica y rendimiento

Objetivo: reducir deuda sin regresiones.

- inventario de selectores usados;
- eliminar CSS muerto por módulos, nunca de forma masiva;
- consolidar capas autoritativas e `!important`;
- separar tokens, componentes y rutas;
- retirar imágenes sin uso y originales pesados cuando existan variantes;
- lazy loading y caché medidos;
- reducir contexto Docker;
- presupuesto final por ruta;
- mantener pruebas proporcionales en cada bloque.

Cierre:

- sin duplicaciones críticas conocidas;
- métricas de rendimiento registradas;
- build reproducible.

### F10. Certificación final y documentación

Objetivo: declarar W2 terminada.

- ejecutar suite integral frontend y backend;
- validar CI, CodeQL, Secret Scanning y auditoría de dependencias;
- capturas forzadas oscuras y claras en viewports definidos;
- revisión manual de recorridos críticos;
- actualizar manuales, arquitectura, despliegue, rollback y continuidad;
- crear changelog y commit de cierre.

Hito final:

`Plataforma Deportiva W2 web finalizada`

## Estrategia de commits

- commits simples y descriptivos;
- validaciones proporcionales durante bloques;
- suite completa solo en cierres amplios y F10;
- backend y reconstrucción Docker únicamente cuando el bloque lo requiera;
- documentación actualizada al final de cada hito funcional.

## Regla para mejoras visuales

Cualquier bloque puede ampliar su alcance si una reconstrucción visual, técnica o arquitectónica mejora el resultado sin sacrificar estabilidad, privacidad, accesibilidad, seguridad, rendimiento ni mantenibilidad. El costo de implementación no es una restricción.
