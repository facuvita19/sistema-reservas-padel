# Manual administrativo

## Inicio de sesión

Iniciar la aplicación desde la configuración `Padel - JavaFX` de Eclipse. Para trabajar con la web Docker, la configuración debe apuntar al MySQL Docker del puerto `3307`.

## Dashboard

El dashboard resume la operación. Los datos pueden actualizarse después de procesar vencimientos o registrar pagos.

## Clientes

Funciones principales:

- Crear y editar clientes.
- Buscar por nombre, documento o datos disponibles.
- Consultar historial de reservas.
- Mantener documento, teléfono y correo actualizados.

## Reservas

### Reserva administrativa

Las reservas creadas por el personal utilizan origen `PERSONAL` y no son expiradas por el procesador de solicitudes web.

### Solicitud web

Una solicitud web:

1. Se crea con origen `WEB`.
2. Comienza en estado `PENDIENTE`.
3. Recibe fecha de vencimiento.
4. Ocupa temporalmente el turno.
5. Pasa a `CONFIRMADA` al acreditarse la seña.
6. Pasa a `EXPIRADA` si vence sin pago acreditado.

## Estados

- `PENDIENTE`: espera acreditación.
- `CONFIRMADA`: pago o seña acreditada.
- `COMPLETADA`: turno realizado.
- `CANCELADA`: cancelación registrada.
- `EXPIRADA`: venció el plazo web sin pago.
- `AUSENTE`: cliente no se presentó.

## Pagos

Al registrar un pago:

- Seleccionar la reserva correcta.
- Usar el medio de pago real.
- Registrar el importe correcto.
- Acreditar únicamente pagos comprobados.
- Verificar la actualización del saldo.

Para una solicitud web, acreditar la seña debe confirmar la reserva y eliminar el vencimiento.

## Solicitudes web

Validar siempre:

- Cliente.
- Cancha.
- Fecha y horario.
- Precio total.
- Seña requerida.
- Estado.
- Fecha de vencimiento.

## Agenda

La agenda debe mostrar reservas activas. Los estados `CANCELADA` y `EXPIRADA` liberan el turno.

## Cancelaciones

- Cancelación normal: solicitada por el cliente.
- Cancelación administrativa: requiere motivo y usuario responsable.

No modificar directamente la base salvo tareas técnicas controladas.

## Configuración recomendada

Para operación real, definir un plazo de pago coherente. Valor actual sugerido:

```text
10 minutos
```

La zona horaria debe permanecer en:

```text
America/Argentina/Buenos_Aires
```
