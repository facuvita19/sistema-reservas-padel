package api.publica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class EstadoSolicitudWebDTO {
    private EstadoSolicitudWebDTO() { }

    public record EstadoSolicitud(
            String codigoSeguimiento,
            long solicitudId,
            String estado,
            String cancha,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            BigDecimal precioTotal,
            BigDecimal totalAcreditado,
            BigDecimal importeSenia,
            BigDecimal saldoPendiente,
            LocalDateTime vencimiento,
            LocalDateTime fechaExpiracion,
            LocalDateTime fechaCancelacion,
            boolean pendiente,
            boolean confirmada,
            boolean expirada,
            String moneda,
            String mensaje) { }

    public record SolicitudCreadaPublica(
            long solicitudId,
            String codigoSeguimiento,
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
