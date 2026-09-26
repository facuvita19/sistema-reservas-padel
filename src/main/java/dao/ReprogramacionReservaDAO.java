package dao;

import java.util.List;

import negocio.ReprogramacionReserva;

public interface ReprogramacionReservaDAO {
    void guardar(ReprogramacionReserva reprogramacion);
    List<ReprogramacionReserva> listarPorReserva(long reservaId);
}
