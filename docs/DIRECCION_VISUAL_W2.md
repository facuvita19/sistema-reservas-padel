# Dirección visual W2

> Sistema creativo para convertir la web en una plataforma deportiva premium, reconocible y coherente.

## 1. Principio creativo

La exigencia visual es máxima: la plataforma debe sentirse premium, profesional, moderna e imponente en cada pantalla, estado y transición. Ningún detalle se considera demasiado pequeño si mejora lectura, confianza, visibilidad, respuesta o percepción de calidad. Se permite rehacer componentes, páginas o recorridos completos cuando el resultado final sea superior.

La experiencia debe combinar energía competitiva, precisión operativa y confianza comercial. No se buscará una web cargada de efectos, sino una presencia editorial fuerte donde cada imagen, movimiento y bloque ayude a reservar, competir o entender información.

## 2. Lenguaje visual

- fondos grafito y negros suaves;
- verdes luminosos como acento, no como relleno masivo;
- blanco cálido y grises claros para lectura;
- profundidad mediante capas y gradientes ambientales;
- bordes finos y radios consistentes;
- glassmorphism limitado a superficies flotantes;
- tipografía de alta jerarquía;
- números deportivos grandes;
- iconografía SVG compacta;
- fotografía con iluminación de cancha, contraste y movimiento.

## 3. Tokens conceptuales

### Color

- fondo principal: grafito profundo;
- superficie: grafito elevado;
- superficie clara: blanco cálido;
- acción primaria: verde deportivo;
- éxito: verde nítido;
- advertencia: ámbar;
- error: rojo contenido;
- información: azul frío;
- línea: blanco con baja opacidad.

### Espaciado y geometría

- escala base de 4 y 8 px;
- contenedores amplios en desktop;
- tarjetas con densidad variable según función;
- objetivo táctil mínimo de 44 px;
- bloques hero con altura adaptable, nunca texto perdido sobre imagen.

## 4. Estrategia de imágenes

### 4.1 Fotografía real

Prioridad cuando existan imágenes del complejo: canchas, iluminación, recepción, torneos, premios y ambiente. Deben optimizarse a AVIF/WebP con variantes responsive.

### 4.2 Imágenes generadas

Se usarán para conceptos o fondos que no pretendan documentar el complejo real:

- hero deportivo nocturno;
- competencia y ranking;
- comunidad;
- reservas inteligentes;
- fondos abstractos de transición;
- estados vacíos editoriales.

Nunca deben inducir a creer que muestran instalaciones reales si no es así.

### 4.3 Recursos de usuario

Avatares e imágenes de torneo quedan para una fase posterior con almacenamiento seguro, validación MIME, límite de tamaño, miniaturas, moderación y privacidad.

### 4.4 Rendimiento

- lazy loading fuera del primer viewport;
- dimensiones declaradas;
- `srcset` y tamaños adaptativos;
- evitar imágenes de fondo gigantes en móvil;
- fallback cromático coherente;
- presupuesto de rendimiento por página.

## 5. Componentes premium

- hero editorial;
- panel de disponibilidad inmediata;
- tarjetas de torneo con identidad;
- podio de ranking;
- tabla deportiva responsive;
- tarjeta de jugador;
- línea de tiempo competitiva;
- marcador de partido;
- convocatoria comunitaria;
- checkout de seña;
- estado de pago;
- skeletons específicos;
- estados vacíos ilustrados;
- toast y feedback no invasivo.

## 6. Movimiento

- transiciones de 160 a 280 ms;
- aparición escalonada solo en contenido principal;
- feedback inmediato al seleccionar turno;
- cambios de ranking con movimiento breve y accesible;
- respeto de `prefers-reduced-motion`;
- ninguna animación bloquea acciones ni lectura.

## 7. Páginas clave

### Inicio

Hero cinematográfico, CTA Reservar, acceso rápido a disponibilidad, torneos, ranking destacado, cambios de categoría y convocatorias.

### Reservar

Matriz de canchas y horarios, filtros compactos, alternativas, resumen lateral en desktop y panel inferior en móvil.

### Rankings

Cabecera de categoría, podio, tabla, búsqueda, posición propia, variación y explicación de puntos.

### Jugador

Cabecera deportiva, categoría, ranking, métricas, cronología, torneos, partidos y parejas frecuentes.

### Competir

Torneos, calendario, resultados, categorías y acceso al centro competitivo.

### Comunidad

Convocatorias con cupos, categoría, fecha, modalidad y solicitud protegida.

### Pago

Detalle de reserva, total, seña, saldo, temporizador, proveedor, estado y recuperación segura.

## 7A. Shell autenticado y centro personal

La cabecera W2 debe resolver con la misma calidad que el resto de la plataforma:

- ingreso y registro;
- avatar o iniciales;
- menú de cuenta accesible;
- Mis reservas, Mis datos, Seguridad y Perfil deportivo;
- cierre de sesión;
- estados visitante, cargando, autenticado, error y sesión vencida;
- navegación móvil propia, no una reducción literal de escritorio.

El centro de cuenta debe organizar información comercial y deportiva con jerarquía clara, resumen accionable, privacidad explícita y continuidad visual con Reservas, Competir, Rankings y Comunidad.

## 7B. Innovación visual responsable

Se evaluarán de manera continua tecnologías modernas de interfaz, interacción, imagen, rendimiento y personalización. Toda incorporación deberá tener fallback, respetar movimiento reducido, mantener contraste y no comprometer carga, SEO ni navegación por teclado.


## 8. Mobile first real

- navegación inferior o menú compacto según contexto;
- ranking convertible a filas deportivas, no tabla horizontal ilegible;
- filtros en hoja modal;
- acciones principales al alcance del pulgar;
- cuadros y cronologías con navegación específica;
- imágenes recortadas por foco;
- carga progresiva.

## 9. Accesibilidad

- contraste AA como mínimo;
- foco visible;
- navegación por teclado;
- etiquetas y mensajes asociados;
- color acompañado por texto o icono;
- orden semántico;
- contenido útil sin imágenes;
- soporte de zoom y texto ampliado.

## 10. Criterios de aprobación visual

Una pantalla W2 se aprueba cuando:

- la acción principal se entiende en menos de cinco segundos;
- no parece una colección repetitiva de tarjetas;
- escritorio y móvil tienen composición propia;
- imágenes tienen finalidad concreta;
- estados vacíos, carga, error y éxito están diseñados;
- teclado, contraste y movimiento reducido funcionan;
- la página mantiene presupuesto de rendimiento;
- la estética es coherente con Inicio, Reservas y Competencia.
