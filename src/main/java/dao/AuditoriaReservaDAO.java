package dao;

import java.util.List;
import negocio.AuditoriaReserva;

public interface AuditoriaReservaDAO {
    void guardar(AuditoriaReserva auditoria);
    List<AuditoriaReserva> listarPorReserva(long reservaId);
}
