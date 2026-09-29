package api.publica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class CuentaClienteDTO {

    private CuentaClienteDTO() {
    }

    public record Perfil(
            long clienteId,
            String nombre,
            String apellido,
            String documento,
            String telefono,
            String email) {
    }

    public record ActualizarPerfil(
            String nombre,
            String apellido,
            String telefono,
            String email) {
    }

    public record ReservaResumen(
            long reservaId,
            String codigoSeguimiento,
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
            LocalDateTime fechaCreacion,
            boolean pendiente,
            boolean confirmada,
            boolean expirada,
            String moneda,
            String mensaje) {
    }

    public record HistorialReservas(
            List<ReservaResumen> proximas,
            List<ReservaResumen> anteriores,
            int total) {

        public HistorialReservas {
            proximas = proximas == null ? List.of() : List.copyOf(proximas);
            anteriores = anteriores == null
                    ? List.of()
                    : List.copyOf(anteriores);
            if (total < 0) {
                throw new IllegalArgumentException(
                        "El total de reservas no puede ser negativo.");
            }
        }
    }
}
