# Definición de terminado de Plataforma Deportiva W2

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

### Estado contra la definición

La convergencia visual sectorial y la continuidad de Torneos están cumplidas. Permanecen continuidad de Rankings, Comunidad, Mi cuenta/Perfil público, patrón global entre rutas, rendimiento medido, operación PWA aplicable, Mercado Pago productivo y certificación final. La aplicación móvil independiente no bloquea el cierre web.

> **Archivo histórico debajo de este punto.** Las fechas y próximos bloques anteriores quedan subordinados al estado autoritativo del 31 de octubre de 2026.


## Estado de cierre de escritorio · 30 de octubre de 2026

<!-- W2-ESTADO-ESCRITORIO-2026-10-30 -->

La revisión y convergencia visual por sectores fue completada para Inicio, Reservar, Torneos, Rankings, Comunidad y Cuenta. Rankings quedó publicado en `18d838b` y Comunidad en `010ca8e`.

Este cierre no sustituye los criterios transversales pendientes. Para declarar la web completamente terminada aún deben certificarse navegación entre rutas, scroll, foco, historial, estados transitorios, rendimiento medido, PWA/operación aplicable y documentación final. Mercado Pago productivo y la aplicación móvil independiente mantienen sus dependencias y exclusiones ya documentadas.


La web solo puede declararse terminada cuando se cumplen todos los puntos siguientes.

## Producto

- todas las rutas W2 tienen propósito y propietario claros;
- no existen datos demostrativos en producción;
- no existen botones o paneles que prometan una migración futura;
- las rutas heredadas están migradas, redirigidas o justificadas;
- Inicio, Reservar, Torneos, Rankings, Comunidad, Jugador y Cuenta están completos.

## Datos

- cada cifra visible tiene fuente autoritativa;
- carga, vacío, error, reintento y sesión vencida están definidos;
- no se calculan reglas críticas de forma divergente en frontend;
- los cambios administrativos se reflejan correctamente;
- no hay datos privados en catálogos, URLs, HTML ni logs.

## Cuenta

- Datos personales y Seguridad están integrados;
- Reservas y pagos son autoritativos;
- Perfil deportivo es completo;
- Comunidad privada tiene sección propia;
- invitaciones, solicitudes y convocatorias están sincronizadas;
- errores parciales no expulsan visualmente al usuario.

## Reservas y pagos

- concurrencia e idempotencia validadas;
- duración, moneda, precio y seña son reales;
- Mercado Pago productivo validado;
- transferencia manual documentada;
- retornos y webhook reconciliados;
- Cuenta e Inicio muestran el estado final correcto.

## Torneos, Rankings y Comunidad

- inscripciones solo por parejas;
- reglas Libre, Femenino, Mixto y Suma N coherentes;
- grupos, posiciones, partidos y cuadro son consistentes;
- fórmula de ranking y desempates son auditables;
- privacidad de Comunidad certificada;
- notificaciones convergen con el dominio.

## Visual y accesibilidad

- oscuro y claro completos;
- sin desbordes ni superposiciones;
- navegación completa por teclado;
- foco visible y restaurado;
- textos largos y zoom 200 % soportados;
- movimiento reducido respetado;
- viewports 1920, 1440, 1280, 900 y 390 validados;
- escritorio recibe la experiencia premium principal.

## Rendimiento y PWA

- presupuestos por ruta registrados;
- imágenes, fuentes, CSS y JavaScript optimizados;
- recursos no utilizados retirados;
- actualización PWA previsible;
- caché vieja invalidada;
- offline y reintento coherentes;
- activos Android generados no contaminan builds ni auditorías web.

## Calidad y operación

- check y build en verde;
- pruebas proporcionales por bloque;
- suite integral final en verde;
- backend validado cuando corresponda;
- CI, CodeQL, Secret Scanning y dependencias revisados;
- despliegue y rollback documentados;
- repositorio limpio;
- continuidad inequívoca.

## Exclusión explícita

La aplicación móvil independiente no bloquea el cierre de la web. Comenzará después, compartiendo backend y reglas de negocio, pero no la interfaz web.
