package dao;

import java.math.BigDecimal;
import java.util.List;

import negocio.EstadoPago;
import negocio.Pago;

public interface PagoDAO {

    void guardar(Pago pago);

    Pago buscar(long id);

    List<Pago> listar();

    List<Pago> listarPorReserva(long reservaId);

    void actualizarEstado(long id, EstadoPago estado);

    BigDecimal totalAcreditado(long reservaId);
}
