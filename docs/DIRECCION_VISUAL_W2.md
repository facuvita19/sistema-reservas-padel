# Dirección visual W2

> Sistema creativo vigente para una plataforma deportiva premium, profesional, moderna e imponente.

## 1. Principio creativo

Cada pantalla debe cerrarse con calidad visual definitiva antes de avanzar. Se acepta rehacer cualquier componente cuando mejora claridad, jerarquía, confianza, accesibilidad o rendimiento.

Modo oscuro es la experiencia principal. Modo claro debe conservar calidad, contraste y coherencia.

## 2. Lenguaje visual

- grafito y negro suave como estructura;
- rojo W2 para navegación y acción principal;
- verde para éxito, clasificación y acreditación;
- ámbar para advertencias y vencimientos;
- azul exclusivamente cuando representa al proveedor Mercado Pago;
- tipografía editorial con jerarquía fuerte;
- iconografía SVG compacta;
- profundidad moderada, sin efectos gratuitos;
- movimiento útil de 160 a 280 ms y respeto de `prefers-reduced-motion`.

## 3. Diseño responsive

- escritorio y móvil tienen composiciones propias;
- objetivo táctil mínimo de 44 px;
- filtros móviles en panel u hoja modal;
- sin desplazamiento horizontal accidental;
- cuadros, rankings y cronologías con solución específica para móvil;
- foco visible, navegación por teclado y zoom soportado.

## 4. Imágenes

- fotografía real del complejo cuando exista;
- imágenes generadas solo para conceptos o fondos no documentales;
- AVIF/WebP, `srcset`, dimensiones declaradas y lazy loading;
- ninguna imagen generada debe presentarse como instalación real;
- cada imagen debe tener función comercial, informativa o identitaria.

## 5. Componentes premium

- hero editorial;
- disponibilidad inmediata;
- tarjetas de torneo con identidad;
- podio y tabla deportiva;
- perfil de jugador;
- línea de tiempo competitiva;
- marcador y cuadro;
- convocatoria comunitaria;
- drawer de reserva y pago;
- skeletons específicos;
- estados vacíos, error y éxito diseñados.

## 6. Torneos

### Catálogo

Las tarjetas usan un contrato geométrico uniforme:

- estado y fecha;
- cantidad de categorías;
- título de hasta dos líneas;
- descripción opcional con espacio estable;
- etiquetas jerarquizadas;
- precio y disponibilidad alineados;
- progreso cuando corresponde;
- acción anclada al pie.

No reducir tipografía según contenido, no inventar descripciones y no usar márgenes particulares por torneo.

### Etiquetas

- Rama: Libre, Femenino, Mixto.
- Formato: Eliminación directa o Grupos + eliminación.
- Condición: 4.ª, 4.ª + 5.ª, Suma 8, Suma 10, Suma 12, etc.
- Americano: distintivo específico.
- Modalidad y dinámica: información secundaria del detalle.

No mostrar “Convencional”. No mezclar Americano, Suma o modalidad de partido dentro del filtro Formato.

### Detalle

Debe explicar claramente:

- tipo especial si es Americano;
- rama;
- condición de inscripción;
- formato competitivo;
- modalidad de partido;
- duración prevista;
- inscripción por pareja.

### Centro competitivo

- selector de categoría persistente;
- navegación estable entre Resumen, Posiciones, Partidos y Cuadro;
- ronda visible cuando corresponde;
- color semántico sin invadir la estructura;
- mobile con controles persistentes y cuadro adaptado.

## 7. Cuenta, reserva y pagos

- visitante, carga, autenticado, error y sesion vencida disenados;
- centro personal une identidad comercial y deportiva;
- drawer de pago con render atomico y skeleton integrado;
- estados online, transferencia, acreditado, vencido y sin metodo son excluyentes;
- X, Escape, backdrop y restauracion de foco obligatorios;
- sandbox identificado mediante texto explicito.

## 8. Patron segmentado y metodo de cierre

Torneos establece el patron de referencia para navegacion secundaria W2: contenedor grafito redondeado y opaco, controles sin divisores, opcion activa elevada con matiz rojo, indicador corto de 32 px, foco visible, comportamiento sticky y variantes principal, secundaria y compacta. En movil debe existir desplazamiento controlado sin desborde global. El modo oscuro es el fallback real y el claro solo se activa mediante tema explicito.

El patron se reutiliza solo despues de auditar la jerarquia de cada seccion. No debe copiarse como una nueva capa de parches.

Cada seccion se cierra con el metodo validado en Torneos:

1. auditoria documental, funcional y visual;
2. inventario de contratos y codigo vigente;
3. bloques pequenos con pruebas proporcionales;
4. revision visual en oscuro, claro, escritorio y movil;
5. correccion de regresiones sin relajar contratos validos;
6. auditoria tecnica antes de limpiar cascadas historicas;
7. suite especifica completa;
8. suite E2E integral y CI verde;
9. actualizacion documental y cierre oficial.

## 9. Criterios de aprobación visual

Una pantalla se aprueba cuando:

- la acción principal se entiende rápidamente;
- la composición no parece una repetición indiscriminada de tarjetas;
- contenido corto y largo mantienen geometría coherente;
- oscuro y claro conservan calidad;
- escritorio y móvil están resueltos;
- carga, vacío, error y éxito están diseñados;
- teclado, contraste y movimiento reducido funcionan;
- no hay saltos, flashes ni contenido heredado visible durante cargas;
- la página mantiene presupuesto de rendimiento.
