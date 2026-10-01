package servicio;

import java.util.List;
import dao.CorreccionResultadoTorneoDAO;
import dao.CorreccionResultadoTorneoDAOMySQL;
import negocio.CorreccionResultadoTorneo;

public class HistorialCorreccionResultadoTorneoService {
    private final CorreccionResultadoTorneoDAO dao;
    public HistorialCorreccionResultadoTorneoService() { this(new CorreccionResultadoTorneoDAOMySQL()); }
    public HistorialCorreccionResultadoTorneoService(CorreccionResultadoTorneoDAO dao) {
        if (dao == null) throw new IllegalArgumentException("El DAO es obligatorio.");
        this.dao = dao;
    }
    public List<CorreccionResultadoTorneo> listar(long partidoId) {
        return dao.listarPorPartido(partidoId);
    }
}
