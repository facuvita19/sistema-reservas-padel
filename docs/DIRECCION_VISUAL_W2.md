# Dirección visual W2

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

### Patrón visual vigente

La navegación secundaria de Resumen, Posiciones, Partidos y Cuadro es el patrón global de referencia: superficie grafito opaca, controles segmentados, activo elevado, sticky controlado, foco visible y adaptación sin desborde. Se reutiliza solo cuando la jerarquía del módulo lo justifica.

> **Archivo histórico debajo de este punto.** Las fechas y próximos bloques anteriores quedan subordinados al estado autoritativo del 31 de octubre de 2026.


## Estado autoritativo · 30 de octubre de 2026

<!-- W2-ESTADO-ESCRITORIO-2026-10-30 -->

### Sistema visual de escritorio convergido

Inicio continúa como referencia visual autoritativa y cada ruta conserva su función propia. Rankings completó su convergencia premium en `18d838b`; Comunidad completó hero, navegación secundaria, turnos, filtros y composición editorial en `010ca8e`.

Patrones globales ya estabilizados:

- cabecera premium y menú de cuenta;
- navegación secundaria sticky, redondeada y sin superficies laterales;
- modo oscuro principal;
- contenedores editoriales alineados;
- títulos sólidos y contorneados con acento rojo proporcional;
- estados de carga, vacío, error y contenido real;
- foco visible, teclado y movimiento reducido;
- geometría específica por función, sin copiar módulos de Inicio de forma literal.

### Continuidad visual

La siguiente etapa es el movimiento entre rutas: continuidad perceptiva, prevención de flashes, conservación del shell y transiciones sobrias. El movimiento nunca debe ocultar esperas, bloquear interacción, romper foco ni imponerse cuando se solicita movimiento reducido.


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

### Patrón privado estabilizado en Mi cuenta

- hero compartido y eje geométrico constante;
- menú lateral único para las cinco secciones;
- títulos editoriales contorneados con jerarquía coherente;
- navegación secundaria segmentada en Reservas;
- CTA primario único para Reservar;
- consulta por código compacta, alineada y sin saltos de documento;
- métricas de recorrido centradas;
- foco, hover, pressed, carga, disabled y movimiento reducido cubiertos.


> Sistema creativo vigente para una plataforma deportiva premium, profesional, moderna e imponente. Actualizado el 27 de octubre de 2026.


<!-- W2-COMUNIDAD-ESTADO-INICIO -->
## Patrones visuales estabilizados en Comunidad

R3.4B5C–D consolidó el centro de actividad como patrón global de notificaciones W2:

- campana con contador y estado abierto en escritorio;
- entrada autenticada desde el menú móvil;
- panel flotante en escritorio y hoja inferior real en móvil;
- backdrop, bloqueo de scroll, safe area y desplazamiento interno;
- agrupación Nuevas / Anteriores;
- tarjetas semánticas con navegación contextual;
- rojo para actividad pendiente, verde para confirmación, grafito para historial y dorado para acciones secundarias;
- notificaciones leídas plenamente legibles;
- carga, vacío, error, reintento, actividad nueva y sin pendientes;
- foco contenido, Escape, restauración de foco y movimiento reducido;
- cobertura en oscuro, claro, escritorio, 390 px y 320 px.

R3.3I reutilizará estos patrones para la convergencia final. PWA y Capacitor deben preservar la misma jerarquía, no simular una interfaz nativa genérica.
<!-- W2-COMUNIDAD-ESTADO-FIN -->

## 1. Principio creativo

Cada pantalla debe cerrarse con calidad visual definitiva antes de avanzar. Se acepta rehacer cualquier componente o pantalla cuando mejora claridad, jerarquía, confianza, diferenciación, accesibilidad o rendimiento.

La versión anterior no define el techo visual. Modo oscuro es la experiencia principal. Modo claro debe conservar jerarquía, contraste y calidad.

## 2. Lema aplicado al diseño

- profesional, no genérico;
- moderno, no dependiente de tendencias frágiles;
- imponente, sin sacrificar legibilidad;
- deportivo, sin caer en decoración gratuita;
- contextual, evitando repetir la misma grilla de tarjetas en todas las pantallas;
- definitivo por bloque, no una sucesión de parches visibles.

## 3. Lenguaje visual


<!-- W2-INICIO-AUTORITATIVO-INICIO -->
## Referencia visual autoritativa: Inicio W2

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
<!-- W2-INICIO-AUTORITATIVO-FIN -->
- grafito y negro suave como estructura;
- rojo W2 para identidad, navegación y acción principal;
- verde para éxito, clasificación y acreditación;
- ámbar para advertencias y vencimientos;
- azul exclusivamente para Mercado Pago o significado externo equivalente;
- tipografía editorial con jerarquía fuerte;
- iconografía SVG compacta;
- profundidad moderada;
- movimiento útil entre 160 y 280 ms;
- respeto de `prefers-reduced-motion`.

## 4. Diseño responsive

- escritorio y móvil tienen composiciones propias;
- cobertura explícita de escritorio, 390 px y 320 px;
- objetivo táctil mínimo de 44 px;
- sin desplazamiento horizontal accidental;
- filtros móviles en panel, hoja o patrón específico;
- cuadros, rankings, pagos, perfiles y convocatorias con solución móvil propia;
- nombres excepcionalmente largos deben conservar identidad sin romper jerarquía;
- foco visible, teclado y zoom soportados.

## 5. Imágenes

- fotografía real cuando exista y sea documentable;
- imágenes generadas solo para conceptos o fondos no documentales;
- AVIF/WebP, `srcset`, dimensiones declaradas y lazy loading;
- ninguna imagen generada se presenta como instalación real;
- cada imagen cumple una función comercial, informativa o identitaria.

## 6. Componentes premium

- hero editorial;
- disponibilidad inmediata;
- tarjetas de torneo con identidad;
- podio y tabla deportiva;
- perfil de jugador;
- línea de tiempo competitiva;
- marcador y cuadro;
- catálogo de jugadores;
- convocatoria comunitaria;
- centro de reserva y pago;
- skeletons específicos;
- estados vacíos, error, éxito y recuperación diseñados.

## 7. Patrón global de navegación secundaria

El estándar global es:

- contenedor grafito redondeado y opaco;
- controles separados sin divisores;
- pestaña activa elevada con matiz rojo;
- indicador corto;
- comportamiento sticky controlado;
- foco visible;
- adaptación móvil sin desborde global.

Resumen, Posiciones, Partidos y Cuadro son la referencia. El patrón se reutiliza después de auditar la jerarquía de cada sección, no como parche automático.

## 8. Torneos

### Catálogo

- geometría uniforme;
- estado y fecha;
- título de hasta dos líneas;
- descripción opcional con espacio estable;
- etiquetas jerarquizadas;
- precio y disponibilidad alineados;
- progreso cuando corresponde;
- acción anclada.

No reducir tipografía arbitrariamente, inventar contenido ni crear excepciones por torneo.

### Etiquetas

- Rama: Libre, Femenino, Mixto.
- Formato: Eliminación directa o Grupos + eliminación.
- Condición: 4.ª, 4.ª + 5.ª, Suma 8, Suma 10, Suma 12, etc.
- Americano: distintivo específico.

### Centro competitivo

- categoría persistente;
- Resumen, Posiciones, Partidos y Cuadro;
- ronda contextual;
- color semántico sin invadir estructura;
- control móvil específico.

## 9. Rankings y perfil público

Inicio establece la referencia visual autoritativa. Reservar aporta el patrón operativo y Torneos/Rankings aportan patrones competitivos y analíticos.

El perfil público debe:

- priorizar identidad y categoría vigente;
- conservar auditoría visible;
- resolver nombres largos en un máximo razonable de líneas;
- evitar actividad reciente extensa y repetitiva;
- equilibrar columna principal y lateral;
- compactar tarjetas en 320 px sin perder rival, torneo, fase, marcador y fecha;
- usar Comunidad como estado deportivo contextual, no como adorno.

## 10. Reserva y pagos

- ruta canónica W2 sin destellos de la experiencia histórica;
- cuatro jugadores comunicado como formato, no como selector;
- consentimiento válido sereno, sin parecer error;
- errores visibles al enfocar y eliminados al editar;
- medios de pago con jerarquía equivalente y altura móvil suficiente;
- Mercado Pago usa identidad azul solo donde corresponde;
- transferencia usa identidad diferenciada y lenguaje público;
- estados online, transferencia, acreditado, vencido y sin método son excluyentes;
- sandbox se identifica explícitamente;
- CTA, foco, estados busy y recuperación son obligatorios.

## 11. Comunidad

### Catálogo de jugadores

Debe sentirse como una herramienta deportiva de descubrimiento, no como directorio genérico.

Cada tarjeta debe jerarquizar:

- identidad pública;
- categoría vigente;
- posición;
- objetivo;
- disponibilidad;
- resumen deportivo permitido;
- acción contextual.

Reglas:

- no exponer contacto en la tarjeta;
- evitar badges excesivos;
- filtros compactos y legibles;
- resultados, filtros y URL consistentes;
- skeleton con geometría real;
- estado vacío útil y accionable;
- ausencia de datos simulados en producción.

### Convocatorias

Una convocatoria debe mostrar claramente:

- reserva asociada;
- fecha, horario y cancha autorizados;
- “3 de 4” y cupos faltantes;
- categoría o compatibilidad buscada;
- estado: abierta, completa, cerrada, vencida o cancelada;
- acción para solicitar unirse;
- acción del titular para cerrar manualmente.

Cerrar convocatoria y cancelar reserva deben ser visual y semánticamente distintos.

### Privacidad visual

- el contacto nunca aparece como texto libre en listados;
- los permisos se explican antes de publicar;
- los estados privados no se disfrazan de errores;
- bloqueo, pausa y retiro de publicación deben ser fáciles de encontrar;
- confirmaciones sensibles deben explicar consecuencias.

## 12. Estados obligatorios

Toda pantalla nueva considera:

- carga;
- contenido;
- vacío;
- búsqueda sin resultados;
- error recuperable;
- error de autorización;
- sesión vencida;
- éxito;
- acción en curso;
- contenido pausado o privado;
- movimiento reducido.

## 13. Método de cierre

1. auditoría documental, funcional y visual;
2. inventario de contratos y código vigente;
3. definición de casos extremos;
4. bloques pequeños con pruebas proporcionales;
5. revisión oscuro, claro, escritorio, 390 px y 320 px;
6. accesibilidad y teclado;
7. corrección sin relajar contratos válidos;
8. auditoría técnica antes de limpiar cascadas;
9. suite específica;
10. E2E integral y CI cuando el hito lo requiere;
11. actualización documental y cierre oficial.

## 14. Criterios de aprobación visual


<!-- W2-CHECKLIST-CONVERGENCIA-INICIO -->
## Lista de comprobación para replicar la gramática de Inicio

Antes de aprobar una ruta:

- el primer frame explica propósito, estado y acción principal;
- la cabecera conserva el mismo centro y ancho global;
- `h1`, `h2`, kickers y textos secundarios siguen la escala compartida;
- el rojo identifica y acciona, pero no rellena todas las superficies;
- filtros, buscadores, checkbox y desplegables parecen parte del mismo sistema;
- la navegación secundaria usa el patrón segmentado cuando existe más de una vista equivalente;
- paneles y tarjetas responden al contenido, sin vacíos artificiales;
- carga, vacío, error, éxito, espera e historial tienen lenguaje propio;
- foco, teclado, 44 px táctiles y movimiento reducido están cubiertos;
- no hay overflow horizontal en escritorio, 390 px ni 320 px;
- modo claro conserva contraste aunque el modo oscuro sea principal;
- los datos y acciones respetan el dominio de la sección;
- no se copiaron clases `w2x-*` fuera de Inicio;
- no se agregó una capa extensa de overrides sin plan de consolidación.
<!-- W2-CHECKLIST-CONVERGENCIA-FIN -->
Una pantalla se aprueba cuando:

- la acción principal se entiende rápidamente;
- la composición tiene identidad propia;
- contenido corto y largo mantienen geometría coherente;
- oscuro y claro conservan calidad;
- escritorio, 390 px y 320 px están resueltos;
- carga, vacío, error, éxito y privacidad están diseñados;
- teclado, contraste y movimiento reducido funcionan;
- no hay saltos, flashes ni contenido histórico visible;
- no se exponen datos personales;
- mantiene presupuesto de rendimiento;
- alcanza o supera la calidad de Torneos y Rankings.

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
