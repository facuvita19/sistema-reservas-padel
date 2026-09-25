package servicio;

import java.time.LocalDate;
import java.util.List;

import dao.BloqueoCanchaDAO;
import dao.BloqueoCanchaDAOMySQL;
import negocio.BloqueoCancha;
import negocio.Cancha;

public class BloqueoCanchaService {

    private final BloqueoCanchaDAO bloqueoDAO;
    private final CanchaService canchaService;

    public BloqueoCanchaService() {
        this(
                new BloqueoCanchaDAOMySQL(),
                new CanchaService()
        );
    }

    public BloqueoCanchaService(
            BloqueoCanchaDAO bloqueoDAO,
            CanchaService canchaService) {

        if (bloqueoDAO == null || canchaService == null) {
            throw new IllegalArgumentException(
                    "Las dependencias de bloqueos no pueden ser nulas."
            );
        }

        this.bloqueoDAO = bloqueoDAO;
        this.canchaService = canchaService;
    }

    public void guardar(BloqueoCancha bloqueo) {
        validar(bloqueo);
        bloqueo.setMotivo(
                bloqueo.getMotivo().trim().replaceAll("\\s+", " ")
        );

        Cancha cancha = canchaService.buscar(bloqueo.getCanchaId());
        if (cancha == null || !cancha.isActivo()) {
            throw new IllegalArgumentException(
                    "La cancha no existe o esta inactiva."
            );
        }

        if (!cancha.estaDisponibleElDia(
                bloqueo.getFecha().getDayOfWeek())) {
            throw new IllegalArgumentException(
                    "La cancha no se encuentra disponible ese dia."
            );
        }

        if (!cancha.contieneHorario(
                bloqueo.getHoraInicio(),
                bloqueo.getHoraFin())) {
            throw new IllegalArgumentException(
                    "El bloqueo debe estar dentro del horario "
                            + "de funcionamiento de la cancha."
            );
        }

        if (bloqueoDAO.horarioBloqueado(
                bloqueo.getCanchaId(),
                bloqueo.getFecha(),
                bloqueo.getHoraInicio(),
                bloqueo.getHoraFin(),
                bloqueo.getId())) {
            throw new IllegalArgumentException(
                    "El horario ya tiene otro bloqueo superpuesto."
            );
        }

        bloqueoDAO.guardar(bloqueo);
    }

    public void eliminar(long id) {
        validarId(id);
        bloqueoDAO.eliminar(id);
    }

    public BloqueoCancha buscar(long id) {
        return id <= 0 ? null : bloqueoDAO.buscar(id);
    }

    public List<BloqueoCancha> listar() {
        return bloqueoDAO.listar();
    }

    public List<BloqueoCancha> listarPorCancha(long canchaId) {
        validarId(canchaId);
        return bloqueoDAO.listarPorCancha(canchaId);
    }

    public boolean estaBloqueado(
            long canchaId,
            LocalDate fecha,
            java.time.LocalTime inicio,
            java.time.LocalTime fin) {

        return bloqueoDAO.horarioBloqueado(
                canchaId,
                fecha,
                inicio,
                fin,
                0L
        );
    }

    private void validar(BloqueoCancha bloqueo) {
        if (bloqueo == null) {
            throw new IllegalArgumentException(
                    "El bloqueo no puede ser nulo."
            );
        }

        validarId(bloqueo.getCanchaId());

        if (bloqueo.getFecha() == null) {
            throw new IllegalArgumentException(
                    "La fecha del bloqueo es obligatoria."
            );
        }

        if (bloqueo.getFecha().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "No se puede crear un bloqueo en una fecha pasada."
            );
        }

        if (bloqueo.getHoraInicio() == null
                || bloqueo.getHoraFin() == null
                || !bloqueo.getHoraFin().isAfter(
                        bloqueo.getHoraInicio())) {
            throw new IllegalArgumentException(
                    "El horario del bloqueo no es valido."
            );
        }

        if (bloqueo.getMotivo() == null
                || bloqueo.getMotivo().isBlank()) {
            throw new IllegalArgumentException(
                    "El motivo del bloqueo es obligatorio."
            );
        }
    }

    private void validarId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El ID de la cancha o bloqueo no es valido."
            );
        }
    }
}
