package servicio;

import java.time.LocalDateTime;
import java.util.List;

import dao.AuditoriaReservaDAO;
import dao.AuditoriaReservaDAOMySQL;
import negocio.AccionAuditoriaReserva;
import negocio.AuditoriaReserva;
import negocio.EstadoReserva;

public class AuditoriaReservaService {
    private final AuditoriaReservaDAO auditoriaDAO;

    public AuditoriaReservaService() {
        this(new AuditoriaReservaDAOMySQL());
    }

    public AuditoriaReservaService(AuditoriaReservaDAO auditoriaDAO) {
        if (auditoriaDAO == null) {
            throw new IllegalArgumentException(
                    "El DAO de auditoría no puede ser nulo.");
        }
        this.auditoriaDAO = auditoriaDAO;
    }

    public List<AuditoriaReserva> listarPorReserva(long reservaId) {
        if (reservaId <= 0) {
            throw new IllegalArgumentException(
                    "El ID de la reserva no es válido.");
        }
        return auditoriaDAO.listarPorReserva(reservaId);
    }

    public void registrar(
            long reservaId,
            Long usuarioId,
            AccionAuditoriaReserva accion,
            EstadoReserva estadoAnterior,
            EstadoReserva estadoNuevo,
            String detalle) {

        if (reservaId <= 0 || accion == null) {
            throw new IllegalArgumentException(
                    "La reserva y la acción de auditoría son obligatorias.");
        }

        AuditoriaReserva auditoria = new AuditoriaReserva();
        auditoria.setReservaId(reservaId);
        auditoria.setUsuarioId(usuarioId != null && usuarioId > 0
                ? usuarioId : null);
        auditoria.setAccion(accion);
        auditoria.setEstadoAnterior(estadoAnterior);
        auditoria.setEstadoNuevo(estadoNuevo);
        auditoria.setDetalle(detalle == null || detalle.isBlank()
                ? accion.getDescripcion()
                : detalle.trim());
        auditoria.setFechaEvento(LocalDateTime.now());
        auditoriaDAO.guardar(auditoria);
    }
}
