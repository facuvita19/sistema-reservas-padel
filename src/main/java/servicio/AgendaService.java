package servicio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import dao.BloqueoCanchaDAO;
import dao.BloqueoCanchaDAOMySQL;
import dao.CanchaDAO;
import dao.CanchaDAOMySQL;
import dao.ReservaDAO;
import dao.ReservaDAOMySQL;
import negocio.AgendaDiaria;
import negocio.BloqueoCancha;
import negocio.Cancha;
import negocio.CeldaAgenda;
import negocio.EstadoCeldaAgenda;
import negocio.EstadoReserva;
import negocio.FilaAgenda;
import negocio.Reserva;

public class AgendaService {

    private final CanchaDAO canchaDAO;
    private final ReservaDAO reservaDAO;
    private final BloqueoCanchaDAO bloqueoDAO;

    public AgendaService() {
        this(new CanchaDAOMySQL(), new ReservaDAOMySQL(),
                new BloqueoCanchaDAOMySQL());
    }

    public AgendaService(
            CanchaDAO canchaDAO,
            ReservaDAO reservaDAO,
            BloqueoCanchaDAO bloqueoDAO) {

        if (canchaDAO == null || reservaDAO == null || bloqueoDAO == null) {
            throw new IllegalArgumentException(
                    "Las dependencias de la agenda no pueden ser nulas.");
        }
        this.canchaDAO = canchaDAO;
        this.reservaDAO = reservaDAO;
        this.bloqueoDAO = bloqueoDAO;
    }

    public AgendaDiaria obtener(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException(
                    "La fecha de la agenda es obligatoria.");
        }

        List<Cancha> canchas = canchaDAO.listar().stream()
                .filter(Cancha::isActivo)
                .sorted(Comparator.comparing(Cancha::getNombre))
                .toList();

        AgendaDiaria agenda = new AgendaDiaria();
        agenda.setFecha(fecha);
        agenda.setCanchas(canchas);

        if (canchas.isEmpty()) {
            return agenda;
        }

        List<Reserva> reservas = reservaDAO.listarPorFecha(fecha);
        List<BloqueoCancha> bloqueos = obtenerBloqueos(canchas, fecha);

        LocalTime aperturaGeneral = canchas.stream()
                .map(Cancha::getHoraApertura)
                .min(LocalTime::compareTo)
                .orElse(LocalTime.of(8, 0));
        LocalTime cierreGeneral = canchas.stream()
                .map(Cancha::getHoraCierre)
                .max(LocalTime::compareTo)
                .orElse(LocalTime.of(23, 0));

        int inicioMinutos = aperturaGeneral.getHour() * 60
                + aperturaGeneral.getMinute();
        int finMinutos = cierreGeneral.getHour() * 60
                + cierreGeneral.getMinute();

        for (int minutos = inicioMinutos;
                minutos < finMinutos;
                minutos += 30) {

            LocalTime hora = LocalTime.of(minutos / 60, minutos % 60);
            FilaAgenda fila = new FilaAgenda();
            fila.setHoraInicio(hora);

            for (Cancha cancha : canchas) {
                fila.agregarCelda(crearCelda(
                        cancha, fecha, hora, reservas, bloqueos));
            }
            agenda.agregarFila(fila);
        }

        return agenda;
    }

    private List<BloqueoCancha> obtenerBloqueos(
            List<Cancha> canchas,
            LocalDate fecha) {

        List<BloqueoCancha> resultado = new ArrayList<>();
        for (Cancha cancha : canchas) {
            bloqueoDAO.listarPorCancha(cancha.getId()).stream()
                    .filter(bloqueo -> fecha.equals(bloqueo.getFecha()))
                    .forEach(resultado::add);
        }
        return resultado;
    }

    private CeldaAgenda crearCelda(
            Cancha cancha,
            LocalDate fecha,
            LocalTime hora,
            List<Reserva> reservas,
            List<BloqueoCancha> bloqueos) {

        CeldaAgenda celda = new CeldaAgenda();
        celda.setCanchaId(cancha.getId());
        celda.setNombreCancha(cancha.getNombre());
        celda.setHoraInicio(hora);
        celda.setHoraFin(hora.plusMinutes(30));

        if (!cancha.estaDisponibleElDia(fecha.getDayOfWeek())
                || hora.isBefore(cancha.getHoraApertura())
                || !hora.isBefore(cancha.getHoraCierre())) {
            celda.setEstado(EstadoCeldaAgenda.NO_DISPONIBLE);
            celda.setDetalle("Fuera de disponibilidad");
            return celda;
        }

        Reserva reserva = buscarReserva(cancha.getId(), hora, reservas);
        if (reserva != null) {
            cargarReserva(celda, reserva);
            return celda;
        }

        BloqueoCancha bloqueo = buscarBloqueo(
                cancha.getId(), hora, bloqueos);
        if (bloqueo != null) {
            celda.setEstado(EstadoCeldaAgenda.BLOQUEADA);
            celda.setBloqueoId(bloqueo.getId());
            celda.setDetalle(bloqueo.getMotivo());
            return celda;
        }

        if (LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now())) {
            celda.setEstado(EstadoCeldaAgenda.PASADA);
            celda.setDetalle("Horario pasado");
            return celda;
        }

        if (!esInicioValido(cancha, hora)) {
            celda.setEstado(EstadoCeldaAgenda.NO_DISPONIBLE);
            celda.setDetalle("No es un horario de inicio");
            return celda;
        }

        LocalTime horaFinReserva = hora.plusMinutes(
                cancha.getDuracionReserva());

        if (horaFinReserva.isAfter(cancha.getHoraCierre())) {
            celda.setEstado(EstadoCeldaAgenda.NO_DISPONIBLE);
            celda.setDetalle("No hay tiempo suficiente");
            return celda;
        }

        if (hayReservaSuperpuesta(
                cancha.getId(), hora, horaFinReserva, reservas)) {
            celda.setEstado(EstadoCeldaAgenda.NO_DISPONIBLE);
            celda.setDetalle("Horario ocupado");
            return celda;
        }

        if (hayBloqueoSuperpuesto(
                cancha.getId(), hora, horaFinReserva, bloqueos)) {
            celda.setEstado(EstadoCeldaAgenda.NO_DISPONIBLE);
            celda.setDetalle("Horario bloqueado");
            return celda;
        }

        celda.setHoraFin(horaFinReserva);
        celda.setEstado(EstadoCeldaAgenda.DISPONIBLE);
        celda.setDetalle("Disponible");
        return celda;
    }

    private Reserva buscarReserva(
            long canchaId,
            LocalTime hora,
            List<Reserva> reservas) {

        return reservas.stream()
                .filter(reserva -> reserva.getCanchaId() == canchaId)
                .filter(this::ocupaHorario)
                .filter(reserva -> !hora.isBefore(reserva.getHoraInicio())
                        && hora.isBefore(reserva.getHoraFin()))
                .findFirst()
                .orElse(null);
    }

    private BloqueoCancha buscarBloqueo(
            long canchaId,
            LocalTime hora,
            List<BloqueoCancha> bloqueos) {

        return bloqueos.stream()
                .filter(bloqueo -> bloqueo.getCanchaId() == canchaId)
                .filter(bloqueo -> !hora.isBefore(bloqueo.getHoraInicio())
                        && hora.isBefore(bloqueo.getHoraFin()))
                .findFirst()
                .orElse(null);
    }

    private void cargarReserva(CeldaAgenda celda, Reserva reserva) {
        celda.setReservaId(reserva.getId());
        celda.setDetalle(reserva.getNombreCliente());
        celda.setEstado(switch (reserva.getEstado()) {
            case PENDIENTE -> EstadoCeldaAgenda.PENDIENTE;
            case CONFIRMADA -> EstadoCeldaAgenda.CONFIRMADA;
            case COMPLETADA -> EstadoCeldaAgenda.COMPLETADA;
            case CANCELADA -> EstadoCeldaAgenda.CANCELADA;
            case AUSENTE -> EstadoCeldaAgenda.AUSENTE;
            case EXPIRADA -> EstadoCeldaAgenda.DISPONIBLE;
        });
    }

    private boolean esInicioValido(Cancha cancha, LocalTime hora) {
        int aperturaMinutos = cancha.getHoraApertura().getHour() * 60
                + cancha.getHoraApertura().getMinute();
        int horaMinutos = hora.getHour() * 60 + hora.getMinute();
        int minutosDesdeApertura = horaMinutos - aperturaMinutos;
        return minutosDesdeApertura >= 0
                && minutosDesdeApertura % cancha.getDuracionReserva() == 0;
    }

    private boolean hayReservaSuperpuesta(
            long canchaId,
            LocalTime inicio,
            LocalTime fin,
            List<Reserva> reservas) {

        return reservas.stream()
                .filter(reserva -> reserva.getCanchaId() == canchaId)
                .filter(this::ocupaHorario)
                .anyMatch(reserva -> seSuperpone(
                        inicio,
                        fin,
                        reserva.getHoraInicio(),
                        reserva.getHoraFin()));
    }

    private boolean ocupaHorario(Reserva reserva) {
        return reserva.getEstado() != EstadoReserva.CANCELADA
                && reserva.getEstado() != EstadoReserva.EXPIRADA;
    }

    private boolean hayBloqueoSuperpuesto(
            long canchaId,
            LocalTime inicio,
            LocalTime fin,
            List<BloqueoCancha> bloqueos) {

        return bloqueos.stream()
                .filter(bloqueo -> bloqueo.getCanchaId() == canchaId)
                .anyMatch(bloqueo -> seSuperpone(
                        inicio,
                        fin,
                        bloqueo.getHoraInicio(),
                        bloqueo.getHoraFin()));
    }

    private boolean seSuperpone(
            LocalTime inicioA,
            LocalTime finA,
            LocalTime inicioB,
            LocalTime finB) {

        return inicioA.isBefore(finB) && finA.isAfter(inicioB);
    }
}
