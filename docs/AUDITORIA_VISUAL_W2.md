# Auditoría y dirección visual final de Plataforma Deportiva W2

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

### Naturaleza del documento

La auditoría fechada el 25 de octubre es una fotografía histórica ya superada en varios puntos. Solo se reutiliza como lista de riesgos; no redefine el estado ni el próximo bloque.

> **Archivo histórico debajo de este punto.** Las fechas y próximos bloques anteriores quedan subordinados al estado autoritativo del 31 de octubre de 2026.


## Cierre de auditoría por sectores · 30 de octubre de 2026

<!-- W2-ESTADO-ESCRITORIO-2026-10-30 -->

La revisión visual de escritorio alcanzó las rutas principales. Rankings cerró en `18d838b` y Comunidad en `010ca8e`. Los sectores comparten cabecera, contenedores, modo oscuro, navegación secundaria, criterios de foco y lenguaje de estados, conservando la identidad funcional propia de cada ruta.

La siguiente auditoría visual será transversal y observará el paso entre rutas: flashes, pérdida de contexto, saltos de scroll, foco, persistencia del shell y transiciones. No corresponde reiniciar una revisión visual completa de cada página salvo que una regresión concreta lo justifique.


Fecha: 25 de octubre de 2026

## Principio rector

La web final debe ser profesional, moderna e imponente. Ninguna pantalla, estructura o tecnología queda protegida por el costo ya invertido. Se aceptan reorganizaciones profundas, reemplazos completos y mejoras pequeñas cuando eleven experiencia, claridad, visibilidad, accesibilidad, rendimiento o mantenibilidad.

El modo oscuro es la experiencia principal. El modo claro debe ser completo y equivalente, no una adaptación parcial.

## Prioridades visuales

### 1. Inicio

Reconstrucción prioritaria.

- eliminar apariencia de tablero demostrativo;
- usar datos reales como parte de la composición;
- hero más enfocado;
- agenda o próxima reserva como acción principal;
- módulos de Torneos, Rankings y Comunidad sin métricas ficticias;
- variante visitante y variante autenticada;
- mejor uso del ancho en 1440 px y 1920 px;
- fotografías o recursos visuales premium cuando aporten valor real;
- estados vacíos editoriales y honestos.

### 2. Cuenta

Convertirla en centro personal premium.

- navegación lateral autoritativa con contadores de pendientes;
- Resumen ordenado por prioridad real;
- secciones propias para Datos, Seguridad, Deporte y Comunidad;
- separar Comunidad privada de Perfil deportivo;
- historial de reservas más denso en escritorio;
- detalle lateral coherente;
- un único sistema visual para pagos;
- componentes separados y CSS fuera del archivo Astro.

### 3. Reservar

- agenda más legible;
- comparación clara entre canchas;
- resumen persistente compacto;
- pasos inequívocos: selección, datos, protección y pago;
- menos ruido en disponibilidad;
- mejor continuidad con Cuenta;
- diseño de escritorio amplio antes que adaptación móvil extrema.

### 4. Torneos

- catálogo con comparación rápida de fechas, estado, cupos y categorías;
- detalle editorial más fuerte;
- inscripción integrada visualmente;
- navegación secundaria flotante preservada como patrón global;
- posiciones y partidos más densos;
- cuadro con navegación horizontal clara;
- estados previos sin contenido falso.

### 5. Rankings y perfiles

- categoría y temporada como contexto principal;
- tabla densa, legible y comparable;
- cabeceras persistentes en escritorio;
- auditoría en panel amplio;
- perfil público conectado visualmente con ranking;
- hero editorial sujeto a revisión, sin obligación de conservarlo.

### 6. Comunidad

- separar Personas y Turnos abiertos con más claridad;
- filtros laterales compactos en escritorio;
- turnos como agenda deportiva, no tarjetas genéricas;
- detalle de convocatoria con secuencia de información y acción;
- estados privados de invitaciones y solicitudes dentro de Cuenta.

## Sistema visual global

Debe consolidarse:

- tokens de color, tipografía, radios, sombras, espaciado y movimiento;
- modos oscuro y claro;
- botones, campos, selectores y estados;
- tarjetas, tablas, listas y paneles laterales;
- navegación secundaria flotante;
- cargas, skeletons, vacíos, errores y reintentos;
- foco visible, teclado y reducción de movimiento;
- densidad diferencial para escritorio, notebook y tablet.

## Criterios visuales de cierre

Cada ruta deberá validarse en:

- 1920 × 1080;
- 1440 × 900;
- 1280 × 800;
- 900 × 1024;
- 390 × 844 como responsive web, no como aplicación nativa.

En ambos temas y con:

- visitante;
- usuario autenticado;
- carga;
- datos completos;
- estado vacío;
- error;
- nombres y textos largos;
- zoom al 200 %;
- teclado únicamente donde corresponda.

No se acepta:

- contenido demostrativo;
- superposición;
- scroll horizontal accidental;
- texto truncado sin alternativa;
- acciones fuera del primer contexto visible;
- cambios de geometría por sesión o tema;
- capas posteriores de CSS para ocultar errores estructurales.
