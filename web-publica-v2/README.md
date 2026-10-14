# Plataforma Deportiva W2

Aplicación web pública del sistema de reservas de pádel, construida con Astro y TypeScript.

## Funciones principales

- Inicio deportivo y navegación responsive.
- Autenticación y cuenta de cliente.
- Reserva de cancha por fecha, horario y datos de contacto.
- Protección temporal del turno con contador.
- Elección del medio de pago según capacidades informadas por la API.
- Transferencia manual con alias, importe exacto, WhatsApp y seguimiento.
- Integración preparada con Mercado Pago para checkout, retorno y actualización de estado.
- Historial de reservas, filtros y consulta por código.
- Torneos, perfiles deportivos y rankings públicos.

## Desarrollo

Desde esta carpeta:

```powershell
npm install
npm run dev
```

La web queda disponible normalmente en `http://localhost:4321`. Las llamadas públicas se realizan contra `/api/publica` y en el entorno Docker son encaminadas por Nginx a la API Java.

## Validación

```powershell
npm run build
npm run test:e2e
```

Las pruebas E2E completas se reservan para cierres funcionales amplios. Para ajustes visuales aislados se utiliza `npm run build` y una comprobación manual proporcional al cambio.

## Flujo de reserva y pago

1. El cliente selecciona cancha, fecha y horario.
2. La API vuelve a comprobar disponibilidad.
3. Se crea una reserva temporal.
4. La web consulta las capacidades de pago.
5. El cliente elige transferencia manual o pago online cuando esté habilitado.
6. La confirmación y Mi cuenta consultan el estado real en el servidor.
7. Un retorno del proveedor nunca confirma por sí solo una reserva.

### Transferencia manual

Validada visual y funcionalmente el 14 de octubre de 2026. Muestra alias, importe, vencimiento, código de seguimiento y envío de comprobante. La acreditación final es administrativa.

### Mercado Pago

El código contempla creación de checkout, intentos, retorno, webhook, procesamiento idempotente y acreditación transaccional. Para habilitarlo se requieren credenciales privadas fuera de Git y una prueba integral con credenciales de prueba.

## Seguridad

- No guardar tokens, contraseñas ni credenciales en el código.
- No confiar en parámetros de retorno para acreditar pagos.
- Validar en backend el proveedor, monto, moneda, referencia y estado.
- Mantener idempotencia en webhooks y acreditaciones.
- No imprimir variables privadas en comandos destinados a compartirse.
