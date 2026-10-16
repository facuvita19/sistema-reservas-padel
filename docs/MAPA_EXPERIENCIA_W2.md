# Mapa de experiencia W2

> Navegación y recorridos vigentes de Plataforma Deportiva W2. Actualizado el 16 de octubre de 2026.

## 1. Navegación principal

```text
Inicio
Reservar
Torneos
Rankings
Comunidad
Mi cuenta
```

**Torneos** es la sección principal. No existe una sección principal llamada Competir.

## 2. Páginas

### Públicas

- Inicio;
- Reservar;
- seguimiento;
- Torneos;
- detalle de torneo;
- centro competitivo;
- Rankings;
- jugador público limitado;
- cambios de categoría;
- Comunidad;
- acceso, registro y recuperación.

### Privadas

- resumen de cuenta;
- mis reservas;
- detalle y pago;
- mis datos;
- seguridad;
- perfil deportivo;
- historial;
- torneos y partidos;
- convocatorias;
- pagos y preferencias.

## 3. Estado real de convergencia

Operativo en W2:

- shell autenticado;
- cuenta y reservas personales;
- Mercado Pago sandbox;
- rankings y auditoría;
- perfiles públicos y privados;
- Torneos con catálogo, detalle y centro competitivo;
- navegación profunda mediante URL;
- desktop, 390 px y 320 px cubiertos por Playwright.

Pendiente:

- motor avanzado de ranking;
- reserva inteligente completa;
- Comunidad;
- Mercado Pago producción;
- PWA y notificaciones;
- convergencia y retiro final de duplicaciones.

## 4. Recorrido Reservar

1. seleccionar fecha;
2. comparar canchas y horarios;
3. elegir turno;
4. identificarse;
5. crear retención temporal;
6. elegir pago;
7. confirmar por backend;
8. mostrar reserva verificable.

Estados: sin disponibilidad, turno tomado, sesión vencida, pago pendiente, aprobado, rechazado, vencido y revisión manual.

## 5. Recorrido Ranking

1. elegir categoría;
2. consultar podio y tabla;
3. buscar jugador;
4. abrir perfil;
5. consultar auditoría;
6. entender movimientos y torneos computados;
7. ver futura clasificación al Master.

## 6. Recorrido Perfil deportivo

### Privado

Categoría vigente, ranking, historial, próximos partidos, torneos, parejas, convocatorias y preferencias permitidas.

### Público

Nombre completo, categoría, ranking y resultados deportivos autorizados. Nunca contacto, documento, pagos ni historial comercial.

## 7. Recorrido Torneos

### Catálogo

1. entrar a `/w2/torneos`;
2. elegir Próximos, En curso o Finalizados;
3. buscar;
4. filtrar por Rama y Formato;
5. opcionalmente mostrar inscripción abierta;
6. abrir torneo.

Filtros y búsqueda persisten en URL, actualización, Atrás y Adelante.

### Detalle

1. Información;
2. Categorías;
3. Reglamento;
4. compartir;
5. inscribir pareja cuando esté disponible;
6. abrir centro competitivo.

### Centro competitivo

1. elegir categoría;
2. consultar Resumen;
3. consultar Posiciones;
4. consultar Partidos;
5. consultar Cuadro;
6. cambiar grupo, fase o ronda;
7. volver sin perder contexto.

## 8. Recorrido Inscripción

1. elegir categoría o condición;
2. ingresar o seleccionar ambos integrantes;
3. evaluar categoría y rama;
4. explicar categoría única, combinación o Suma;
5. enviar inscripción por pareja;
6. revisión administrativa cuando corresponda;
7. aprobar, autorizar excepción o rechazar;
8. pagar cuando se habilite.

No existe inscripción individual.

## 9. Recorrido Americano

1. identificar el distintivo Americano;
2. consultar rama, condición, formato y modalidad;
3. inscribir una pareja;
4. consultar rondas y partidos cortos;
5. ver clasificación o cuadro;
6. resolver el evento normalmente el mismo día.

Americano puede usar grupos más eliminación o eliminación directa. No se modela como liga semanal o mensual.

## 10. Recorrido Comunidad

1. buscar pareja o completar partido;
2. definir categoría, disponibilidad y contexto;
3. recibir solicitudes;
4. aceptar o rechazar;
5. formar pareja;
6. inscribirse juntos o asociar una reserva;
7. cerrar y notificar.

## 11. Recorrido Pago

1. crear reserva temporal;
2. elegir Mercado Pago o transferencia;
3. crear o reutilizar orden;
4. abrir checkout;
5. procesar webhook o conciliación;
6. acreditar seña;
7. confirmar reserva;
8. actualizar el drawer completo.

La URL de retorno nunca acredita por sí sola.

## 12. Punto de continuidad

Continuar con **R2.1C5**:

- separar presentación del detalle;
- pestañas Información, Categorías y Reglamento;
- métricas, precios y estado temporal;
- tarjetas de categorías;
- compartir;
- apertura y regreso;
- foco, scroll y URL.

Mantener intactos diseño, inscripción por pareja, nomenclatura Libre/Femenino/Mixto, formatos competitivos, centro competitivo, backend y base de datos.
