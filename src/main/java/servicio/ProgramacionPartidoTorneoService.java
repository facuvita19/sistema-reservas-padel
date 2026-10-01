package servicio;

import java.time.LocalDate;
import java.time.LocalTime;

import dao.TorneoCategoriaDAO;
import dao.TorneoCategoriaDAOMySQL;
import dao.TorneoDAO;
import dao.TorneoDAOMySQL;
import dao.TorneoInscripcionDAO;
import dao.TorneoInscripcionDAOMySQL;
import dao.TorneoPartidoDAO;
import dao.TorneoPartidoDAOMySQL;
import negocio.Cancha;
import negocio.EstadoInscripcionTorneo;
import negocio.EstadoPartidoTorneo;
import negocio.EstadoTorneo;
import negocio.Torneo;
import negocio.TorneoCategoria;
import negocio.TorneoInscripcion;
import negocio.TorneoPartido;

public class ProgramacionPartidoTorneoService {

    private final TorneoPartidoDAO partidoDAO;
    private final TorneoCategoriaDAO categoriaDAO;
    private final TorneoDAO torneoDAO;
    private final TorneoInscripcionDAO inscripcionDAO;
    private final CanchaService canchaService;
    private final DisponibilidadCanchaService disponibilidadService;

    public ProgramacionPartidoTorneoService() {
        this(new TorneoPartidoDAOMySQL(),
                new TorneoCategoriaDAOMySQL(),
                new TorneoDAOMySQL(),
                new TorneoInscripcionDAOMySQL(),
                new CanchaService(),
                new DisponibilidadCanchaService());
    }

    public ProgramacionPartidoTorneoService(
            TorneoPartidoDAO partidoDAO,
            TorneoCategoriaDAO categoriaDAO,
            TorneoDAO torneoDAO,
            TorneoInscripcionDAO inscripcionDAO,
            CanchaService canchaService,
            DisponibilidadCanchaService disponibilidadService) {
        if (partidoDAO == null || categoriaDAO == null
                || torneoDAO == null || inscripcionDAO == null
                || canchaService == null
                || disponibilidadService == null) {
            throw new IllegalArgumentException(
                    "Las dependencias de programacion no pueden ser nulas.");
        }
        this.partidoDAO = partidoDAO;
        this.categoriaDAO = categoriaDAO;
        this.torneoDAO = torneoDAO;
        this.inscripcionDAO = inscripcionDAO;
        this.canchaService = canchaService;
        this.disponibilidadService = disponibilidadService;
    }

    public TorneoPartido programar(
            long partidoId,
            long canchaId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin) {
        if (partidoId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del partido debe ser positivo.");
        }
        TorneoPartido partido = partidoDAO.buscar(partidoId);
        if (partido == null) {
            throw new IllegalArgumentException("El partido no existe.");
        }
        validarPartidoProgramable(partido);

        TorneoCategoria categoria = categoriaDAO.buscar(
                partido.getTorneoCategoriaId());
        if (categoria == null || !categoria.isActivo()) {
            throw new IllegalArgumentException(
                    "La categoria no existe o esta inactiva.");
        }
        Torneo torneo = torneoDAO.buscar(categoria.getTorneoId());
        validarTorneo(torneo, fecha);

        Cancha cancha = canchaService.buscar(canchaId);
        validarCancha(cancha, fecha, horaInicio, horaFin);
        validarParejas(partido);

        if (!disponibilidadService.estaDisponibleParaPartido(
                canchaId, fecha, horaInicio, horaFin, partidoId)) {
            throw new IllegalArgumentException(
                    "La cancha no esta disponible en ese horario.");
        }
        validarAgendaPareja(
                partido.getPareja1InscripcionId(), fecha,
                horaInicio, horaFin, partidoId);
        validarAgendaPareja(
                partido.getPareja2InscripcionId(), fecha,
                horaInicio, horaFin, partidoId);

        partido.setCanchaId(canchaId);
        partido.setFecha(fecha);
        partido.setHoraInicio(horaInicio);
        partido.setHoraFin(horaFin);
        partido.setEstado(EstadoPartidoTorneo.PROGRAMADO);
        partidoDAO.guardar(partido);
        return partidoDAO.buscar(partidoId);
    }

    public TorneoPartido quitarProgramacion(long partidoId) {
        if (partidoId <= 0) {
            throw new IllegalArgumentException(
                    "El ID del partido debe ser positivo.");
        }
        TorneoPartido partido = partidoDAO.buscar(partidoId);
        if (partido == null) {
            throw new IllegalArgumentException("El partido no existe.");
        }
        if (partido.getEstado() != EstadoPartidoTorneo.PROGRAMADO) {
            throw new IllegalArgumentException(
                    "Solo puede quitarse la programacion de un partido programado.");
        }
        partido.setCanchaId(null);
        partido.setFecha(null);
        partido.setHoraInicio(null);
        partido.setHoraFin(null);
        partido.setEstado(EstadoPartidoTorneo.PENDIENTE);
        partidoDAO.guardar(partido);
        return partidoDAO.buscar(partidoId);
    }

    private void validarPartidoProgramable(TorneoPartido partido) {
        if (partido.isBye()) {
            throw new IllegalArgumentException(
                    "Un partido resuelto por BYE no se programa.");
        }
        if (!partido.tieneDosParejas()) {
            throw new IllegalArgumentException(
                    "El partido necesita dos parejas definidas.");
        }
        if (partido.getEstado() != EstadoPartidoTorneo.PENDIENTE
                && partido.getEstado() != EstadoPartidoTorneo.PROGRAMADO) {
            throw new IllegalArgumentException(
                    "El estado del partido no permite programarlo.");
        }
    }

    private void validarTorneo(Torneo torneo, LocalDate fecha) {
        if (torneo == null || !torneo.isActivo()) {
            throw new IllegalArgumentException(
                    "El torneo no existe o esta inactivo.");
        }
        if (torneo.getEstado() != EstadoTorneo.INSCRIPCION_CERRADA
                && torneo.getEstado() != EstadoTorneo.EN_CURSO) {
            throw new IllegalArgumentException(
                    "El torneo debe tener inscripciones cerradas o estar en curso.");
        }
        if (fecha == null || fecha.isBefore(torneo.getFechaInicio())
                || fecha.isAfter(torneo.getFechaFin())) {
            throw new IllegalArgumentException(
                    "La fecha del partido debe estar dentro del torneo.");
        }
    }

    private void validarCancha(
            Cancha cancha,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin) {
        if (cancha == null || !cancha.isActivo()) {
            throw new IllegalArgumentException(
                    "La cancha no existe o esta inactiva.");
        }
        if (fecha == null || horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException(
                    "La fecha y el horario son obligatorios.");
        }
        if (!cancha.estaDisponibleElDia(fecha.getDayOfWeek())) {
            throw new IllegalArgumentException(
                    "La cancha no esta habilitada ese dia.");
        }
        if (!cancha.contieneHorario(horaInicio, horaFin)) {
            throw new IllegalArgumentException(
                    "El horario esta fuera de la jornada de la cancha.");
        }
    }

    private void validarParejas(TorneoPartido partido) {
        validarPareja(partido.getPareja1InscripcionId(),
                partido.getTorneoCategoriaId());
        validarPareja(partido.getPareja2InscripcionId(),
                partido.getTorneoCategoriaId());
    }

    private void validarPareja(Long inscripcionId, long categoriaId) {
        if (inscripcionId == null) {
            throw new IllegalArgumentException(
                    "La pareja del partido es obligatoria.");
        }
        TorneoInscripcion inscripcion = inscripcionDAO.buscar(inscripcionId);
        if (inscripcion == null
                || inscripcion.getEstado()
                    != EstadoInscripcionTorneo.CONFIRMADA
                || inscripcion.getTorneoCategoriaId() != categoriaId
                || !inscripcion.tieneParejaCompleta()) {
            throw new IllegalArgumentException(
                    "La pareja no esta confirmada y completa en la categoria.");
        }
    }

    private void validarAgendaPareja(
            Long inscripcionId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            long partidoExcluidoId) {
        if (inscripcionId != null
                && partidoDAO.parejaOcupadaEnHorario(
                    inscripcionId, fecha, horaInicio, horaFin,
                    partidoExcluidoId)) {
            throw new IllegalArgumentException(
                    "Una de las parejas ya tiene otro partido en ese horario.");
        }
    }
}
