package api.publica;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

import dao.BloqueoCanchaDAO;
import dao.BloqueoCanchaDAOMySQL;
import dao.ReservaDAO;
import dao.ReservaDAOMySQL;
import dao.TorneoPartidoDAO;
import dao.TorneoPartidoDAOMySQL;
import negocio.BloqueoCancha;
import negocio.Cancha;
import negocio.EstadoReserva;
import negocio.Reserva;
import negocio.TorneoPartido;
import negocio.ConfiguracionComplejo;
import servicio.CanchaService;
import servicio.ConfiguracionComplejoService;
import servicio.ReservaService;

public class DisponibilidadPublicaService {
    private final CanchaService canchaService;
    private final ReservaService reservaService;
    private final ConfiguracionComplejoService configuracionService;
    private final ReservaDAO reservaDAO;
    private final BloqueoCanchaDAO bloqueoDAO;
    private final TorneoPartidoDAO partidoDAO;

    public DisponibilidadPublicaService() {
        this(new CanchaService(), new ReservaService(), new ConfiguracionComplejoService());
    }
    public DisponibilidadPublicaService(CanchaService canchaService,
            ReservaService reservaService,
            ConfiguracionComplejoService configuracionService) {
        this(canchaService, reservaService, configuracionService,
                new ReservaDAOMySQL(), new BloqueoCanchaDAOMySQL(),
                new TorneoPartidoDAOMySQL());
    }

    public DisponibilidadPublicaService(CanchaService canchaService,
            ReservaService reservaService,
            ConfiguracionComplejoService configuracionService,
            ReservaDAO reservaDAO, BloqueoCanchaDAO bloqueoDAO,
            TorneoPartidoDAO partidoDAO) {
        if (canchaService == null || reservaService == null
                || configuracionService == null || reservaDAO == null
                || bloqueoDAO == null || partidoDAO == null) {
            throw new IllegalArgumentException(
                    "Las dependencias de disponibilidad no pueden ser nulas.");
        }
        this.canchaService = canchaService;
        this.reservaService = reservaService;
        this.configuracionService = configuracionService;
        this.reservaDAO = reservaDAO;
        this.bloqueoDAO = bloqueoDAO;
        this.partidoDAO = partidoDAO;
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

    public ApiPublicaDTO.PanoramaDisponibilidad obtenerPanorama(LocalDate fecha) {
        if (fecha == null) throw new IllegalArgumentException("La fecha es obligatoria.");
        if (fecha.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("No se puede consultar una fecha pasada.");

        reservaService.expirarReservasPendientes();
        List<Cancha> canchas = canchaService.listar().stream()
                .filter(Cancha::isActivo)
                .sorted(Comparator.comparing(Cancha::getNombre))
                .toList();
        ConfiguracionComplejo config = configuracionService.obtener();
        LocalDateTime minimo = LocalDateTime.now()
                .plusHours(config.getAnticipacionMinimaHoras());
        List<Reserva> reservas = reservaDAO.listarPorFecha(fecha);
        List<BloqueoCancha> bloqueos = bloqueoDAO.listarPorFecha(fecha);
        List<TorneoPartido> partidos = partidoDAO.listarPorFecha(fecha);

        List<ApiPublicaDTO.Disponibilidad> resultado = canchas.stream()
                .map(cancha -> disponibilidadEnMemoria(cancha, fecha, minimo,
                        reservas, bloqueos, partidos))
                .toList();
        return new ApiPublicaDTO.PanoramaDisponibilidad(fecha, resultado);
    }

    private ApiPublicaDTO.Disponibilidad disponibilidadEnMemoria(
            Cancha cancha, LocalDate fecha, LocalDateTime minimo,
            List<Reserva> reservas, List<BloqueoCancha> bloqueos,
            List<TorneoPartido> partidos) {
        boolean disponibleEseDia = cancha.estaDisponibleElDia(fecha.getDayOfWeek());
        BigDecimal senia = configuracionService.calcularSenia(cancha.getPrecio());
        if (!disponibleEseDia) {
            return new ApiPublicaDTO.Disponibilidad(fecha, convertirCancha(cancha),
                    false, List.of());
        }
        java.util.ArrayList<ApiPublicaDTO.HorarioDisponible> horarios =
                new java.util.ArrayList<>();
        LocalTime inicio = cancha.getHoraApertura();
        int duracion = cancha.getDuracionReserva();
        while (inicio.isBefore(cancha.getHoraCierre())) {
            LocalTime fin = inicio.plusMinutes(duracion);
            if (fin.isAfter(cancha.getHoraCierre())) break;
            LocalDateTime instante = LocalDateTime.of(fecha, inicio);
            if (!instante.isBefore(minimo)
                    && !ocupado(cancha.getId(), inicio, fin,
                            reservas, bloqueos, partidos)) {
                horarios.add(new ApiPublicaDTO.HorarioDisponible(
                        inicio, fin, cancha.getPrecio(), senia));
            }
            inicio = fin;
        }
        return new ApiPublicaDTO.Disponibilidad(fecha, convertirCancha(cancha),
                true, List.copyOf(horarios));
    }

    private boolean ocupado(long canchaId, LocalTime inicio, LocalTime fin,
            List<Reserva> reservas, List<BloqueoCancha> bloqueos,
            List<TorneoPartido> partidos) {
        boolean reserva = reservas.stream()
                .filter(r -> r.getCanchaId() == canchaId)
                .filter(r -> r.getEstado() != EstadoReserva.CANCELADA
                        && r.getEstado() != EstadoReserva.EXPIRADA)
                .anyMatch(r -> seSuperpone(inicio, fin,
                        r.getHoraInicio(), r.getHoraFin()));
        if (reserva) return true;
        boolean bloqueo = bloqueos.stream()
                .filter(b -> b.getCanchaId() == canchaId)
                .anyMatch(b -> seSuperpone(inicio, fin,
                        b.getHoraInicio(), b.getHoraFin()));
        if (bloqueo) return true;
        return partidos.stream()
                .filter(p -> p.getCanchaId() != null
                        && p.getCanchaId() == canchaId)
                .filter(p -> p.getEstado() != null && p.getEstado().bloqueaCancha())
                .anyMatch(p -> seSuperpone(inicio, fin,
                        p.getHoraInicio(), p.getHoraFin()));
    }

    private boolean seSuperpone(LocalTime inicioA, LocalTime finA,
            LocalTime inicioB, LocalTime finB) {
        return inicioB != null && finB != null
                && inicioA.isBefore(finB) && finA.isAfter(inicioB);
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
