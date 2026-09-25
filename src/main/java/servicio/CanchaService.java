package servicio;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import dao.CanchaDAO;
import dao.CanchaDAOMySQL;
import negocio.Cancha;

public class CanchaService {

    private static final int DURACION_MINIMA = 30;
    private static final int DURACION_MAXIMA = 240;
    private static final int INTERVALO_DURACION = 30;

    private final CanchaDAO canchaDAO;

    public CanchaService() {
        this(new CanchaDAOMySQL());
    }

    public CanchaService(CanchaDAO canchaDAO) {
        if (canchaDAO == null) {
            throw new IllegalArgumentException(
                    "El DAO de canchas no puede ser nulo."
            );
        }
        this.canchaDAO = canchaDAO;
    }

    public void guardar(Cancha cancha) {
        validarCancha(cancha);
        normalizarDatos(cancha);

        if (canchaDAO.existeNombre(
                cancha.getNombre(),
                cancha.getId())) {
            throw new IllegalArgumentException(
                    "Ya existe una cancha con ese nombre."
            );
        }

        canchaDAO.guardar(cancha);
    }

    public void eliminar(long id) {
        validarId(id);
        canchaDAO.eliminar(id);
    }

    public Cancha buscar(long id) {
        return id <= 0 ? null : canchaDAO.buscar(id);
    }

    public List<Cancha> listar() {
        return canchaDAO.listar();
    }

    public boolean estaDisponibleElDia(long canchaId, DayOfWeek dia) {
        Cancha cancha = obtenerCanchaObligatoria(canchaId);
        return cancha.estaDisponibleElDia(dia);
    }

    public boolean horarioDentroDeJornada(
            Cancha cancha,
            LocalTime horaInicio,
            LocalTime horaFin) {

        return cancha != null
                && cancha.contieneHorario(horaInicio, horaFin);
    }

    public LocalTime calcularHoraFin(
            long canchaId,
            LocalTime horaInicio) {

        if (horaInicio == null) {
            throw new IllegalArgumentException(
                    "La hora de inicio es obligatoria."
            );
        }

        Cancha cancha = obtenerCanchaObligatoria(canchaId);
        return horaInicio.plusMinutes(cancha.getDuracionReserva());
    }

    private void validarCancha(Cancha cancha) {
        if (cancha == null) {
            throw new IllegalArgumentException(
                    "La cancha no puede ser nula."
            );
        }

        validarTexto(
                cancha.getNombre(),
                "El nombre de la cancha es obligatorio."
        );

        if (cancha.getTipo() == null) {
            throw new IllegalArgumentException(
                    "El tipo de cancha es obligatorio."
            );
        }

        if (cancha.getHoraApertura() == null
                || cancha.getHoraCierre() == null) {
            throw new IllegalArgumentException(
                    "El horario de la cancha es obligatorio."
            );
        }

        if (!cancha.getHoraCierre().isAfter(
                cancha.getHoraApertura())) {
            throw new IllegalArgumentException(
                    "La hora de cierre debe ser posterior "
                            + "a la hora de apertura."
            );
        }

        int duracion = cancha.getDuracionReserva();
        if (duracion < DURACION_MINIMA
                || duracion > DURACION_MAXIMA) {
            throw new IllegalArgumentException(
                    "La duracion debe estar entre 30 y 240 minutos."
            );
        }

        if (duracion % INTERVALO_DURACION != 0) {
            throw new IllegalArgumentException(
                    "La duracion debe ser multiplo de 30 minutos."
            );
        }

        long minutosJornada = Duration.between(
                cancha.getHoraApertura(),
                cancha.getHoraCierre()
        ).toMinutes();

        if (minutosJornada < duracion) {
            throw new IllegalArgumentException(
                    "La jornada debe permitir al menos "
                            + "una reserva completa."
            );
        }

        if (cancha.getPrecio() == null
                || cancha.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El precio no puede ser negativo."
            );
        }

        if (cancha.getDiasDisponibles().isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe seleccionar al menos un dia disponible."
            );
        }
    }

    private void normalizarDatos(Cancha cancha) {
        cancha.setNombre(
                cancha.getNombre().trim().replaceAll("\\s+", " ")
        );

        cancha.setDescripcion(limpiarOpcional(
                cancha.getDescripcion()
        ));
        cancha.setSuperficie(limpiarOpcional(
                cancha.getSuperficie()
        ));

        if (cancha.getPrecio() != null) {
            cancha.setPrecio(cancha.getPrecio().setScale(
                    2,
                    java.math.RoundingMode.HALF_UP
            ));
        }
    }

    private Cancha obtenerCanchaObligatoria(long canchaId) {
        validarId(canchaId);
        Cancha cancha = canchaDAO.buscar(canchaId);

        if (cancha == null || !cancha.isActivo()) {
            throw new IllegalArgumentException(
                    "La cancha no existe o esta inactiva."
            );
        }

        return cancha;
    }

    private String limpiarOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim().replaceAll("\\s+", " ");
    }

    private void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private void validarId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El ID de la cancha no es valido."
            );
        }
    }
}
