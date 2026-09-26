package dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import negocio.EstadoReserva;
import negocio.Reserva;
import negocio.TipoCancelacion;

public interface ReservaDAO {

    void guardar(Reserva reserva);

    Reserva buscar(long id);

    List<Reserva> listar();

    List<Reserva> listarPorCliente(long clienteId);

    List<Reserva> listarPorFecha(LocalDate fecha);

    void actualizarEstado(long id, EstadoReserva estado);

    default void cancelar(
            long id,
            TipoCancelacion tipo,
            String motivo,
            LocalDateTime fechaCancelacion,
            Long usuarioCancelacionId) {

        actualizarEstado(id, EstadoReserva.CANCELADA);
    }

    boolean horarioOcupado(
            long canchaId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            long reservaExcluidaId);
}
