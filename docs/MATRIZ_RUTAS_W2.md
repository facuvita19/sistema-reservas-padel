# Matriz autoritativa de rutas de Plataforma Deportiva W2

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

### Rutas autoritativas actuales

- `/w2/`: Inicio real.
- `/w2/reservar`: reserva y pago.
- `/w2/torneos`: catálogo, detalle, inscripción por pareja y centro competitivo.
- `/w2/rankings`: rankings y auditoría, próximo bloque T2C1.
- `/w2/jugador`: perfil público.
- `/w2/comunidad` y `/w2/comunidad/convocatoria`: Comunidad.
- `/w2/cuenta`: centro privado completo.

Las rutas heredadas se mantienen solo como puentes de compatibilidad documentados; no reciben capacidades nuevas.

> **Archivo histórico debajo de este punto.** Las fechas y próximos bloques anteriores quedan subordinados al estado autoritativo del 31 de octubre de 2026.


## Estado autoritativo · 30 de octubre de 2026

<!-- W2-ESTADO-ESCRITORIO-2026-10-30 -->

Las rutas W2 principales de escritorio están operativas y visualmente convergidas en su alcance actual. Inicio ya usa datos reales y diseño premium. Rankings quedó unificado en `18d838b` y Comunidad en `010ca8e`.

La continuidad inmediata es transversal: navegación y transiciones entre rutas autoritativas. Las rutas heredadas solo deben conservarse cuando todavía cumplan una función no migrada o una compatibilidad explícita.


Fecha: 25 de octubre de 2026

## Dirección vigente

La experiencia principal se desarrolla bajo `/w2/*`. Las rutas heredadas se mantienen temporalmente hasta completar F4. No deben recibir nuevas capacidades salvo correcciones críticas de transición.

## Rutas W2 autoritativas

- `/w2/`: Inicio. Pendiente F1 para reemplazar el prototipo por datos reales.
- `/w2/reservar`: reserva de canchas.
- `/w2/torneos`: catálogo, detalle, inscripción y centro competitivo.
- `/w2/rankings`: rankings y auditoría deportiva.
- `/w2/jugador`: perfil deportivo público.
- `/w2/comunidad`: jugadores y turnos abiertos.
- `/w2/comunidad/convocatoria`: detalle y gestión de convocatoria.
- `/w2/cuenta`: centro privado. Datos y Seguridad se completarán en F2.

## Rutas heredadas en transición

- `/`: Inicio anterior con contratos reales reutilizables para F1.
- `/reservar`: reserva anterior; no sumar nuevas capacidades.
- `/mis-reservas`: historial anterior; convergencia en Cuenta W2.
- `/mis-datos`: edición de datos y seguridad hasta F2.
- `/torneos`: puente hacia `/w2/torneos`.
- `/restablecer`: recuperación vigente pendiente de convergencia visual.

## Regla de cierre

Una ruta heredada solo podrá redirigirse o retirarse cuando su capacidad equivalente W2 esté implementada, probada, documentada y enlazada desde toda la plataforma.
