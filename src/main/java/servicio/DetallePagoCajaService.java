package servicio;

import java.time.LocalDate;
import java.util.List;

import dao.DetallePagoCajaDAO;
import dao.DetallePagoCajaDAOMySQL;
import negocio.DetallePagoCaja;

public class DetallePagoCajaService {
    private final DetallePagoCajaDAO detalleDAO;

    public DetallePagoCajaService() {
        this(new DetallePagoCajaDAOMySQL());
    }

    public DetallePagoCajaService(DetallePagoCajaDAO detalleDAO) {
        if (detalleDAO == null) {
            throw new IllegalArgumentException(
                    "El DAO de detalle de pagos no puede ser nulo.");
        }
        this.detalleDAO = detalleDAO;
    }

    public List<DetallePagoCaja> listarAcreditadosPorFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        return detalleDAO.listarAcreditadosPorFecha(fecha);
    }
}
