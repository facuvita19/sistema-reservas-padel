package dao;

import java.time.LocalDate;
import java.util.List;

import negocio.DetallePagoCaja;

public interface DetallePagoCajaDAO {
    List<DetallePagoCaja> listarAcreditadosPorFecha(LocalDate fecha);
}
