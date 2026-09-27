package dao;

import java.time.LocalDate;
import java.util.List;

import negocio.MovimientoCaja;

public interface MovimientoCajaDAO {
    void guardar(MovimientoCaja movimiento);
    List<MovimientoCaja> listarPorFecha(LocalDate fecha);
}
