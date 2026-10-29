# Mapa de experiencia W2

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

### Recorrido privado consolidado

`Mi cuenta → Resumen | Reservas | Datos personales | Seguridad | Perfil deportivo`

- Resumen conecta próxima reserva, actividad y situación deportiva.
- Reservas alterna próximas e historial, abre detalle persistente y consulta códigos externos.
- Datos personales permite editar y descartar cambios con protección de navegación.
- Seguridad actualiza la contraseña sin abandonar Mi cuenta.
- Perfil deportivo conserva categoría, ranking, historial, preferencias y Comunidad.

Aceptar “salir igualmente” descarta la edición pendiente antes de navegar; cancelar conserva el formulario y bloquea la salida.


> Navegación y recorridos vigentes de Plataforma Deportiva W2. Actualizado el 27 de octubre de 2026.


<!-- W2-COMUNIDAD-ESTADO-INICIO -->
## Estado actual y continuidad de Comunidad

### Recorridos disponibles

```text
Comunidad → descubrir jugador o turno → perfil/detalle → solicitar o invitar
Mi cuenta → gestionar reservas, convocatorias, solicitudes e invitaciones
Cabecera autenticada → campana → centro de actividad → recurso contextual
```

El centro distingue Nuevas y Anteriores, permite leer una al navegar o leer todas y mantiene enlaces internos seguros. En móvil se abre desde el menú como hoja inferior con fondo bloqueado.

La matriz interna ya cubre solicitud, aceptación, rechazo, cupo, cierre manual, cancelación, vencimiento e invitaciones. El siguiente recorrido es R3.3I para converger estados extremos, privacidad, accesibilidad y rendimiento. Después se incorporan PWA, Push y Capacitor sin reemplazar el centro web.
<!-- W2-COMUNIDAD-ESTADO-FIN -->

## 1. Navegación principal

```text
Inicio
Reservar
Torneos
Rankings
Comunidad
Mi cuenta
```

Torneos es la sección principal. No existe una sección llamada Competir.

## 2. Páginas


<!-- W2-MAPA-IDENTIDAD-INICIO -->
## Función visual de cada sección

- **Inicio**: puerta editorial y deportiva. Presenta la propuesta completa, actividad real y accesos principales. Es la referencia visual autoritativa.
- **Reservar**: recorrido operativo de cuatro pasos. Prioriza claridad, estado, protección temporal, pago y recuperación.
- **Torneos**: exploración competitiva. Prioriza calendario, inscripción por pareja, seguimiento, resultados e historial.
- **Rankings**: lectura analítica y auditable. Prioriza categoría, posición, puntos, movimiento y explicación.
- **Comunidad**: descubrimiento y conexión protegida. Prioriza objetivos, compatibilidad, privacidad y acciones contextualizadas.
- **Mi cuenta**: gestión personal. Prioriza reservas, pagos, identidad deportiva, preferencias, solicitudes e invitaciones.
- **Perfiles**: identidad deportiva pública y privada, con límites estrictos de datos.

Las rutas comparten shell, tipografía, controles, superficies, navegación, estados y accesibilidad, pero no deben tener la misma composición.
<!-- W2-MAPA-IDENTIDAD-FIN -->
### Públicas

- Inicio.
- Reservar.
- Seguimiento.
- Torneos.
- Detalle de torneo.
- Centro competitivo.
- Rankings.
- Perfil público limitado.
- Cambios de categoría.
- Comunidad.
- Acceso, registro y recuperación.

### Privadas

- Resumen de cuenta.
- Mis reservas.
- Detalle y pago.
- Mis datos.
- Seguridad.
- Perfil deportivo.
- Historial.
- Torneos y partidos.
- Preferencias de Comunidad.
- Convocatorias y solicitudes.
- Pagos y preferencias.

## 3. Estado real de convergencia

Operativo:

- shell autenticado;
- cuenta y reservas;
- Mercado Pago sandbox;
- Rankings y auditoría;
- perfiles públicos y privados;
- Torneos completo;
- reserva W2 canónica;
- navegación profunda;
- cobertura de escritorio, 390 px y 320 px;
- fundación persistente de Comunidad R3.3A.

Pendiente:

- convergencia final R3.3I de Comunidad;
- motor avanzado de ranking como evolución posterior;
- reserva inteligente ampliada;
- Mercado Pago producción;
- PWA y Push sobre el centro interno ya operativo;
- retiro final de duplicaciones históricas.

## 4. Recorrido Reservar

1. entrar a `/w2/reservar` o ser redirigido desde `/reservar`;
2. seleccionar fecha;
3. comparar canchas;
4. consultar horarios;
5. elegir turno;
6. revisar formato fijo de cuatro jugadores;
7. completar datos;
8. aceptar protección temporal;
9. revalidar disponibilidad;
10. crear solicitud;
11. elegir pago;
12. confirmar por backend;
13. consultar estado verificable.

Estados: sin disponibilidad, turno tomado, campo inválido, sesión vencida, pago pendiente, aprobado, rechazado, vencido, sin método y revisión manual.

## 5. Recorrido Ranking

1. elegir categoría;
2. consultar podio y tabla;
3. buscar jugador;
4. abrir perfil;
5. consultar auditoría;
6. entender movimientos y torneos computados;
7. consultar futura clasificación al Master.

## 6. Recorrido Perfil deportivo

### Privado

Categoría vigente, ranking, historial, partidos, torneos, parejas, preferencias y acciones autorizadas.

### Público

Nombre, categoría, ranking y resultados autorizados. Nunca documento, correo, teléfono, pagos ni historial comercial.

## 7. Recorrido Torneos

### Catálogo

1. entrar a `/w2/torneos`;
2. elegir Próximos, En curso o Finalizados;
3. buscar;
4. filtrar por Rama y Formato;
5. mostrar inscripción abierta si corresponde;
6. abrir torneo.

### Detalle

1. Información;
2. Categorías;
3. Reglamento;
4. compartir;
5. inscribir pareja;
6. abrir centro competitivo.

### Centro

1. elegir categoría;
2. Resumen;
3. Posiciones;
4. Partidos;
5. Cuadro;
6. grupo, fase o ronda;
7. volver sin perder contexto.

## 8. Recorrido Inscripción

1. elegir condición;
2. seleccionar ambos integrantes;
3. evaluar categoría y rama;
4. explicar compatibilidad;
5. enviar pareja;
6. revisión administrativa;
7. aprobar, exceptuar o rechazar;
8. pagar cuando corresponda.

No existe inscripción individual.

## 9. Recorrido Americano

1. identificar Americano;
2. consultar rama, condición, formato y modalidad;
3. inscribir pareja;
4. consultar rondas y partidos;
5. ver clasificación o cuadro;
6. finalizar normalmente el mismo día.

## 10. Recorrido Comunidad: catálogo de jugadores

1. entrar a Comunidad;
2. comprender qué datos son públicos y qué requiere sesión;
3. buscar por nombre o filtrar por categoría, posición, objetivo y disponibilidad;
4. revisar resultados paginados;
5. abrir un perfil público;
6. comprobar compatibilidad;
7. iniciar una acción permitida.

Estados: carga, resultados, sin resultados, catálogo vacío, privacidad, error recuperable y sesión requerida.

## 11. Recorrido Comunidad: preferencias

1. entrar a Mi cuenta;
2. abrir Preferencias de Comunidad;
3. activar publicación de forma opt-in;
4. definir objetivos;
5. elegir posición, categorías, días y franjas;
6. decidir si acepta invitaciones;
7. autorizar o no contacto por WhatsApp;
8. revisar vista previa;
9. guardar;
10. pausar o retirar publicación cuando quiera.

La publicación nunca debe activarse silenciosamente.

## 12. Recorrido para formar pareja de torneo

1. buscar perfiles compatibles;
2. revisar categoría, posición, historial permitido y disponibilidad;
3. abrir perfil;
4. contactar principalmente por WhatsApp cuando exista permiso;
5. coordinar por fuera del formulario de torneo;
6. volver al torneo;
7. inscribirse juntos como pareja.

Comunidad no reserva un cupo individual ni crea una inscripción incompleta.

## 13. Recorrido para completar una reserva

Caso principal:

> “3 de 4, falta 1 jugador de 5.ª”.

1. el titular crea una reserva real;
2. desde la reserva elige publicar convocatoria;
3. confirma participantes actuales y cupos faltantes;
4. define categoría, posición y mensaje permitido;
5. revisa qué información será pública;
6. publica;
7. perfiles compatibles descubren la convocatoria;
8. una persona solicita unirse;
9. el titular acepta o rechaza;
10. al completar cupos, la convocatoria se cierra;
11. la reserva conserva su flujo comercial y de pago.

## 14. Recorrido de solicitud

1. abrir convocatoria;
2. validar sesión, perfil y compatibilidad;
3. revisar reglas y datos visibles;
4. solicitar unirse;
5. evitar duplicados;
6. mostrar estado pendiente;
7. recibir aceptación, rechazo, vencimiento o cancelación;
8. si es aceptada, incorporarse como participante de origen Comunidad.

## 15. Cierre manual

1. el titular completa el turno fuera de la web;
2. abre la convocatoria;
3. elige **Cerrar convocatoria**;
4. confirma que no se cancela la reserva;
5. el sistema impide nuevas solicitudes;
6. conserva participantes, solicitudes y eventos;
7. la reserva continúa sin cambios.

Diferencias obligatorias:

- cerrar convocatoria: impide nuevas solicitudes;
- cancelar convocatoria: retira la publicación según reglas;
- cancelar reserva: afecta el turno comercial y sigue otro flujo.

Nunca deben presentarse como la misma acción.

## 16. Recorrido Pago

1. crear reserva temporal;
2. elegir Mercado Pago o transferencia;
3. crear o reutilizar orden;
4. abrir checkout;
5. procesar webhook o conciliación;
6. acreditar seña;
7. confirmar reserva;
8. actualizar el centro completo.

La URL de retorno nunca acredita por sí sola.

## 17. Centro de notificaciones y evolución futura

- solicitudes, invitaciones y resoluciones: operativas;
- convocatoria completa, cierre, cancelación y vencimiento: operativos;
- centro web: fuente visible y permanente;
- PWA, Push y Capacitor: evolución posterior a R3.3I.

## 18. Punto de continuidad

Torneos y Rankings están cerrados. El perfil público completó R3.2B. Comunidad cerró R3.4B1–R3.4B5D / R3.3A–R3.3H. El siguiente orden es:

1. convergencia visual de Torneos iniciada en U1;
2. Rankings;
3. Comunidad;
4. Mi cuenta y perfiles;
5. limpieza transversal de CSS y validación integral;
6. aplicación móvil independiente después del cierre de la web de escritorio.

## Estado autoritativo posterior al prototipo móvil

> Nota vigente desde el 25 de octubre de 2026: las secciones anteriores sobre Capacitor y CAP3 se conservan únicamente como historial técnico. El proyecto Android, las dependencias Capacitor, las capas nativas y sus pruebas fueron retirados del producto web activo en F0. La dirección vigente es finalizar primero la web de escritorio y desarrollar después una aplicación móvil independiente.
