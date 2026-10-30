# Auditoría y dirección visual final de Plataforma Deportiva W2

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
