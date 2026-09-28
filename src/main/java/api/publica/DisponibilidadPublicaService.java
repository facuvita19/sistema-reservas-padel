package api.publica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import negocio.Cancha;
import negocio.ConfiguracionComplejo;
import servicio.CanchaService;
import servicio.ConfiguracionComplejoService;
import servicio.ReservaService;

public class DisponibilidadPublicaService {
    private final CanchaService canchaService;
    private final ReservaService reservaService;
    private final ConfiguracionComplejoService configuracionService;

    public DisponibilidadPublicaService() {
        this(new CanchaService(), new ReservaService(), new ConfiguracionComplejoService());
    }
    public DisponibilidadPublicaService(CanchaService canchaService,
            ReservaService reservaService,
            ConfiguracionComplejoService configuracionService) {
        if (canchaService == null || reservaService == null || configuracionService == null)
            throw new IllegalArgumentException("Las dependencias de disponibilidad no pueden ser nulas.");
        this.canchaService = canchaService;
        this.reservaService = reservaService;
        this.configuracionService = configuracionService;
    }

    public ApiPublicaDTO.Complejo obtenerComplejo() {
        ConfiguracionComplejo c = configuracionService.obtener();
        return new ApiPublicaDTO.Complejo(c.getNombreComercial(), c.getDireccion(),
                c.getTelefono(), c.getWhatsapp(), c.getEmail(), c.getInstagram(),
                c.getMoneda(), c.getPorcentajeSenia(), c.getAnticipacionMinimaHoras(),
                c.getMinutosReservaPendiente(), c.getColorPrincipal(), c.getPagoAlias(),
                c.getPagoTitular(), c.getPagoEntidad(), c.getPagoInstrucciones(),
                c.tieneInstruccionesPago());
    }

    public List<ApiPublicaDTO.CanchaPublica> listarCanchas() {
        return canchaService.listar().stream().filter(Cancha::isActivo)
                .sorted(Comparator.comparing(Cancha::getNombre))
                .map(this::convertirCancha).toList();
    }

    public ApiPublicaDTO.CanchaPublica buscarCancha(long id) {
        if (id <= 0) throw new IllegalArgumentException("El ID de la cancha no es válido.");
        Cancha c = canchaService.buscar(id);
        if (c == null || !c.isActivo())
            throw new RecursoNoEncontradoException("La cancha no existe o está inactiva.");
        return convertirCancha(c);
    }

    public ApiPublicaDTO.Disponibilidad obtenerDisponibilidad(long canchaId, LocalDate fecha) {
        if (fecha == null) throw new IllegalArgumentException("La fecha es obligatoria.");
        if (fecha.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("No se puede consultar una fecha pasada.");
        Cancha c = canchaService.buscar(canchaId);
        if (c == null || !c.isActivo())
            throw new RecursoNoEncontradoException("La cancha no existe o está inactiva.");
        ConfiguracionComplejo config = configuracionService.obtener();
        LocalDateTime minimo = LocalDateTime.now().plusHours(config.getAnticipacionMinimaHoras());
        BigDecimal senia = configuracionService.calcularSenia(c.getPrecio());
        var horarios = reservaService.listarHorariosDisponibles(canchaId, fecha, 0L).stream()
                .filter(h -> !LocalDateTime.of(fecha, h).isBefore(minimo))
                .map(h -> new ApiPublicaDTO.HorarioDisponible(h,
                        h.plusMinutes(c.getDuracionReserva()), c.getPrecio(), senia)).toList();
        return new ApiPublicaDTO.Disponibilidad(fecha, convertirCancha(c),
                c.estaDisponibleElDia(fecha.getDayOfWeek()), horarios);
    }

    private ApiPublicaDTO.CanchaPublica convertirCancha(Cancha c) {
        return new ApiPublicaDTO.CanchaPublica(c.getId(), c.getNombre(), c.getDescripcion(),
                c.getTipo() == null ? null : c.getTipo().toString(), c.getSuperficie(),
                c.isTieneIluminacion(), c.getHoraApertura(), c.getHoraCierre(),
                c.getDuracionReserva(), c.getPrecio(),
                configuracionService.calcularSenia(c.getPrecio()),
                c.getDiasDisponibles().stream().sorted().map(Enum::name).toList());
    }

    public static class RecursoNoEncontradoException extends IllegalArgumentException {
        public RecursoNoEncontradoException(String mensaje) { super(mensaje); }
    }
}
