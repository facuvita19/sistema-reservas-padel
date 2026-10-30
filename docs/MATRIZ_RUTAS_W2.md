# Matriz autoritativa de rutas de Plataforma Deportiva W2

## Estado autoritativo · 30 de octubre de 2026

<!-- W2-ESTADO-ESCRITORIO-2026-10-30 -->

Las rutas W2 principales de escritorio están operativas y visualmente convergidas en su alcance actual. Inicio ya usa datos reales y diseño premium. Rankings quedó unificado en `18d838b` y Comunidad en `010ca8e`.

La continuidad inmediata es transversal: navegación y transiciones entre rutas autoritativas. Las rutas heredadas solo deben conservarse cuando todavía cumplan una función no migrada o una compatibilidad explícita.


Fecha: 25 de octubre de 2026

## Dirección vigente

La experiencia principal se desarrolla bajo `/w2/*`. Las rutas heredadas se mantienen temporalmente hasta completar F4. No deben recibir nuevas capacidades salvo correcciones críticas de transición.

## Rutas W2 autoritativas

- `/w2/`: Inicio. Pendiente F1 para reemplazar el prototipo por datos reales.
- `/w2/reservar`: reserva de canchas.
- `/w2/torneos`: catálogo, detalle, inscripción y centro competitivo.
- `/w2/rankings`: rankings y auditoría deportiva.
- `/w2/jugador`: perfil deportivo público.
- `/w2/comunidad`: jugadores y turnos abiertos.
- `/w2/comunidad/convocatoria`: detalle y gestión de convocatoria.
- `/w2/cuenta`: centro privado. Datos y Seguridad se completarán en F2.

## Rutas heredadas en transición

- `/`: Inicio anterior con contratos reales reutilizables para F1.
- `/reservar`: reserva anterior; no sumar nuevas capacidades.
- `/mis-reservas`: historial anterior; convergencia en Cuenta W2.
- `/mis-datos`: edición de datos y seguridad hasta F2.
- `/torneos`: puente hacia `/w2/torneos`.
- `/restablecer`: recuperación vigente pendiente de convergencia visual.

## Regla de cierre

Una ruta heredada solo podrá redirigirse o retirarse cuando su capacidad equivalente W2 esté implementada, probada, documentada y enlazada desde toda la plataforma.
