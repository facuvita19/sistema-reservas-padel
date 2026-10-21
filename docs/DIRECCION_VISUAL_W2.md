# Dirección visual W2

> Sistema creativo vigente para una plataforma deportiva premium, profesional, moderna e imponente. Actualizado el 19 de octubre de 2026.


<!-- W2-COMUNIDAD-ESTADO-INICIO -->
## Patrones visuales estabilizados en Comunidad

R3.4B2G consolidó los patrones que deben reutilizarse en las próximas pantallas de solicitudes:

- modo oscuro como experiencia principal y modo claro plenamente funcional;
- navegación secundaria grafito, sticky, opaca y adaptable a móvil;
- drawer con cabecera compacta, fondo opaco y cierre SVG;
- superficies grafito sin paneles blancos accidentales;
- rojo W2 para identidad y acciones propias;
- azul reconocible para Mercado Pago;
- verde para acreditación, disponibilidad y estados positivos;
- ámbar para coordinación pendiente cuando corresponda;
- selectores personalizados con teclado, foco visible y menús compactos;
- tarjeta pública de Turnos abiertos con fecha, horario, cancha, cupo, criterios y convocante;
- CTA con una sola flecha decorativa y texto accesible estable;
- geometría validada en 1440 px, 390 px y 320 px;
- contenido privado y público claramente diferenciado.

Las pantallas de detalle de convocatoria, solicitud y gestión del titular deberán mantener esta dirección. No se deben crear modales genéricos desconectados del lenguaje W2.

### Criterio para el próximo bloque

Antes de cerrar una superficie se deben revisar:

- carga, vacío, error y éxito;
- solicitud propia, duplicada, retirada, aceptada y rechazada;
- convocatoria abierta, completa, cerrada y vencida;
- foco, teclado, lector de pantalla y movimiento reducido;
- nombres largos, mensajes extensos y último cupo;
- escritorio, 390 px y 320 px;
- modo oscuro y modo claro.
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

Rankings y Torneos establecen el nivel visual mínimo.

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
