package api.publica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class SolicitudWebDTO {
    private SolicitudWebDTO() { }

    public record ClienteEntrada(
            String nombre,
            String apellido,
            String documento,
            String telefono,
            String email) { }

    public record CrearSolicitud(
            long canchaId,
            LocalDate fecha,
            LocalTime horaInicio,
            int cantidadJugadores,
            ClienteEntrada cliente,
            String comentarios) { }

    public record SolicitudCreada(
            long solicitudId,
            String estado,
            long clienteId,
            long canchaId,
            String cancha,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            BigDecimal precioTotal,
            BigDecimal importeSenia,
            LocalDateTime vencimiento,
            int minutosParaPagar,
            String moneda,
            String mensaje) { }
}
