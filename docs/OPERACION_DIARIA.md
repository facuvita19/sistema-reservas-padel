# Operación diaria

## Apertura del sistema

1. Abrir Docker Desktop.
2. Esperar a que el motor esté disponible.
3. Abrir PowerShell en la raíz del proyecto:

```powershell
cd "C:\Users\facuv\eclipse-workspace\sistema-reservas-padel"
```

4. Iniciar el entorno:

```powershell
.\iniciar-simulacion.ps1
```

5. Verificar:

```powershell
docker compose ps
```

Estado esperado:

```text
padel-mysql    healthy
padel-api      running
padel-web      running
```

6. Abrir la web:

```text
http://localhost:5173
```

7. Iniciar la aplicación administrativa desde Eclipse.

## Verificaciones de apertura

- La web muestra “Disponibilidad online”.
- Las canchas se cargan.
- La API responde en `/api/publica/estado`.
- La aplicación administrativa inicia sesión.
- La administración y la web muestran la misma configuración.

## Rutina operativa

### Solicitudes web

1. Revisar el módulo **Solicitudes web**.
2. Identificar reservas `PENDIENTE`.
3. Verificar importe de seña y vencimiento.
4. Acreditar el pago recibido.
5. Confirmar que la reserva pase a `CONFIRMADA`.

### Agenda

- Revisar ocupación diaria.
- Comprobar bloqueos de cancha.
- Confirmar que reservas expiradas no ocupen horarios.
- Evitar crear reservas superpuestas.

### Caja y pagos

- Registrar pagos con el medio correcto.
- Confirmar estado `ACREDITADO`.
- Revisar movimientos de caja.
- Realizar el cierre diario según el procedimiento interno.

### Configuración

Desde **Configuración del complejo** se administran:

- Nombre y contacto.
- Moneda.
- Porcentaje de seña.
- Plazo de reserva pendiente.
- Anticipación mínima.
- Datos de transferencia.
- Color de la web.

Los cambios afectan a solicitudes nuevas. Una reserva existente conserva el vencimiento calculado al crearla.

## Cierre diario

1. Confirmar pagos y caja.
2. Crear backup:

```powershell
.\backup-mysql.ps1
```

3. Detener el entorno:

```powershell
.\detener-simulacion.ps1
```

4. Cerrar Docker Desktop si no se utilizará.

## Acceso desde celular

1. Obtener la IPv4:

```powershell
ipconfig
```

2. Abrir desde el celular:

```text
http://IP_DE_LA_PC:5173
```

El celular y la computadora deben estar en la misma red Wi-Fi. `.env` debe incluir:

```env
WEB_ORIGIN=local
```
