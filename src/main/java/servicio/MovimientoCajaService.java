package servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import dao.CierreCajaDAO;
import dao.CierreCajaDAOMySQL;
import dao.MovimientoCajaDAO;
import dao.MovimientoCajaDAOMySQL;
import negocio.MetodoPago;
import negocio.MovimientoCaja;
import negocio.TipoMovimientoCaja;

public class MovimientoCajaService {
    private final MovimientoCajaDAO movimientoDAO;
    private final CierreCajaDAO cierreDAO;

    public MovimientoCajaService() {
        this(new MovimientoCajaDAOMySQL(), new CierreCajaDAOMySQL());
    }

    public MovimientoCajaService(MovimientoCajaDAO movimientoDAO, CierreCajaDAO cierreDAO) {
        this.movimientoDAO = movimientoDAO;
        this.cierreDAO = cierreDAO;
    }

    public void registrar(LocalDate fecha, TipoMovimientoCaja tipo, String concepto,
            BigDecimal importe, MetodoPago medioPago, String observaciones, long usuarioId) {
        if (fecha == null || tipo == null || medioPago == null) {
            throw new IllegalArgumentException("Fecha, tipo y medio de pago son obligatorios.");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("No se pueden registrar movimientos en una fecha futura.");
        }
        if (cierreDAO.buscarPorFecha(fecha) != null) {
            throw new IllegalArgumentException("La caja de esa fecha ya está cerrada.");
        }
        if (concepto == null || concepto.isBlank()) {
            throw new IllegalArgumentException("El concepto es obligatorio.");
        }
        if (importe == null || importe.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El importe debe ser mayor que cero.");
        }
        if (usuarioId <= 0) throw new IllegalArgumentException("El usuario es obligatorio.");
        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setFecha(fecha);
        movimiento.setTipo(tipo);
        movimiento.setConcepto(concepto.trim().replaceAll("\\s+", " "));
        movimiento.setImporte(importe.setScale(2, RoundingMode.HALF_UP));
        movimiento.setMedioPago(medioPago);
        movimiento.setObservaciones(observaciones == null || observaciones.isBlank()
                ? null : observaciones.trim().replaceAll("\\s+", " "));
        movimiento.setUsuarioId(usuarioId);
        movimientoDAO.guardar(movimiento);
    }

    public List<MovimientoCaja> listarPorFecha(LocalDate fecha) {
        if (fecha == null) throw new IllegalArgumentException("La fecha es obligatoria.");
        return movimientoDAO.listarPorFecha(fecha);
    }
}
