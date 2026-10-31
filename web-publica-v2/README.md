# Plataforma Deportiva W2

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

### Continuidad de desarrollo frontend

El próximo trabajo es T2C1, continuidad interna de Rankings. Torneos queda como referencia técnica para shell estable, panel localizado, historial profundo, BFCache y movimiento reducido.

> **Archivo histórico debajo de este punto.** Las fechas y próximos bloques anteriores quedan subordinados al estado autoritativo del 31 de octubre de 2026.


Aplicación web pública del sistema de reservas de pádel, construida con Astro y TypeScript.

## Funciones principales

- Inicio deportivo y navegación responsive.
- Autenticación y cuenta de cliente.
- Reserva de cancha por fecha, horario y datos de contacto.
- Protección temporal del turno con contador.
- Elección del medio de pago según capacidades informadas por la API.
- Transferencia manual con alias, importe exacto, WhatsApp y seguimiento.
- Integración preparada con Mercado Pago para checkout, retorno y actualización de estado.
- Historial de reservas, filtros y consulta por código.
- Torneos, perfiles deportivos y rankings públicos.

## Desarrollo

Desde esta carpeta:

```powershell
npm install
npm run dev
```

La web queda disponible normalmente en `http://localhost:4321`. Las llamadas públicas se realizan contra `/api/publica` y en el entorno Docker son encaminadas por Nginx a la API Java.

## Validación

```powershell
npm run build
npm run test:e2e
```

Las pruebas E2E completas se reservan para cierres funcionales amplios. Para ajustes visuales aislados se utiliza `npm run build` y una comprobación manual proporcional al cambio.

## Flujo de reserva y pago

1. El cliente selecciona cancha, fecha y horario.
2. La API vuelve a comprobar disponibilidad.
3. Se crea una reserva temporal.
4. La web consulta las capacidades de pago.
5. El cliente elige transferencia manual o pago online cuando esté habilitado.
6. La confirmación y Mi cuenta consultan el estado real en el servidor.
7. Un retorno del proveedor nunca confirma por sí solo una reserva.

### Transferencia manual

Validada visual y funcionalmente el 14 de octubre de 2026. Muestra alias, importe, vencimiento, código de seguimiento y envío de comprobante. La acreditación final es administrativa.

### Mercado Pago

El código contempla creación de checkout, intentos, retorno, webhook, procesamiento idempotente y acreditación transaccional. Para habilitarlo se requieren credenciales privadas fuera de Git y una prueba integral con credenciales de prueba.

## Seguridad

- No guardar tokens, contraseñas ni credenciales en el código.
- No confiar en parámetros de retorno para acreditar pagos.
- Validar en backend el proveedor, monto, moneda, referencia y estado.
- Mantener idempotencia en webhooks y acreditaciones.
- No imprimir variables privadas en comandos destinados a compartirse.
