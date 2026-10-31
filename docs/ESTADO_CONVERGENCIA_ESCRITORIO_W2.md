# Estado de convergencia de escritorio W2

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

> **Archivo histórico debajo de este punto.** Las fechas y próximos bloques anteriores quedan subordinados al estado autoritativo del 31 de octubre de 2026.


<!-- W2-ESTADO-ESCRITORIO-2026-10-30 -->

## Estado confirmado

Al 30 de octubre de 2026, la web pública de escritorio fue revisada por sectores y convergida visualmente en su alcance actual.

- Inicio: referencia editorial y de actualización fluida.
- Reservar: flujo operativo premium.
- Torneos: catálogo, detalle y centro competitivo.
- Rankings: cierre publicado en `18d838b`.
- Comunidad: cierre publicado en `010ca8e`.
- Mi cuenta: centro privado consolidado.
- Cabecera, navegaciones sticky y menú de cuenta: sistema global estabilizado.

## Qué significa este cierre

El cierre confirma coherencia visual, jerarquía, contenedores, modo oscuro, navegación secundaria, estados y accesibilidad en el alcance validado. No congela el producto ni declara terminadas las evoluciones deportivas, comerciales o móviles.

## Próximo bloque oficial

**T1 · Continuidad de navegación y transiciones internas W2**.

### Objetivos

1. reducir la percepción de recarga completa;
2. mantener URLs y arquitectura multipágina;
3. conservar cabecera, tema, sesión y menú de cuenta;
4. restaurar scroll y foco de forma predecible;
5. respetar Atrás/Adelante;
6. evitar flashes y correcciones tardías;
7. usar View Transitions con degradación segura cuando aporte valor;
8. respetar `prefers-reduced-motion`;
9. validar por rutas y recorridos con Playwright.

## Después de T1

La prioridad se decidirá con evidencia entre rendimiento medido, PWA/notificaciones, Mercado Pago productivo y preparación de la aplicación móvil independiente. No se inicia automáticamente una auditoría histórica general de rendimiento.
