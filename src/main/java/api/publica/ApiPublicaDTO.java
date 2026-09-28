package api.publica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class ApiPublicaDTO {
    private ApiPublicaDTO() { }

    public record Complejo(
            String nombreComercial,
            String direccion,
            String telefono,
            String whatsapp,
            String email,
            String instagram,
            String moneda,
            BigDecimal porcentajeSenia,
            int anticipacionMinimaHoras,
            int minutosReservaPendiente,
            String colorPrincipal) { }

    public record CanchaPublica(
            long id,
            String nombre,
            String descripcion,
            String tipo,
            String superficie,
            boolean tieneIluminacion,
            LocalTime horaApertura,
            LocalTime horaCierre,
            int duracionMinutos,
            BigDecimal precio,
            BigDecimal importeSenia,
            List<String> diasDisponibles) { }

    public record HorarioDisponible(
            LocalTime horaInicio,
            LocalTime horaFin,
            BigDecimal precio,
            BigDecimal importeSenia) { }

    public record Disponibilidad(
            LocalDate fecha,
            CanchaPublica cancha,
            boolean disponibleEseDia,
            List<HorarioDisponible> horarios) { }

    public record ErrorApi(
            int estado,
            String codigo,
            String mensaje,
            String ruta,
            String fechaHora) { }

    public record EstadoApi(
            String servicio,
            String estado,
            String fechaHora) { }
}
