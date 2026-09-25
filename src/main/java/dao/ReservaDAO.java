package dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import negocio.EstadoReserva;
import negocio.Reserva;

public interface ReservaDAO {

    void guardar(Reserva reserva);

    Reserva buscar(long id);

    List<Reserva> listar();

    List<Reserva> listarPorCliente(long clienteId);

    List<Reserva> listarPorFecha(LocalDate fecha);

    void actualizarEstado(long id, EstadoReserva estado);

    boolean horarioOcupado(
            long canchaId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            long reservaExcluidaId);
}
