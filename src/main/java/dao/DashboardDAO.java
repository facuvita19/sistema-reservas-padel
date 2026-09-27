package dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import negocio.Reserva;
import negocio.ResumenDashboard;

public interface DashboardDAO {
    ResumenDashboard obtenerResumen(LocalDateTime momentoReferencia);
    List<Reserva> listarProximasReservas(LocalDateTime momentoReferencia, int limite);

    default ResumenDashboard obtenerResumen(LocalDate fechaReferencia) {
        if (fechaReferencia == null) throw new IllegalArgumentException("La fecha de referencia es obligatoria.");
        return obtenerResumen(fechaReferencia.atStartOfDay());
    }

    default List<Reserva> listarProximasReservas(LocalDate fechaReferencia, int limite) {
        if (fechaReferencia == null) throw new IllegalArgumentException("La fecha de referencia es obligatoria.");
        return listarProximasReservas(fechaReferencia.atStartOfDay(), limite);
    }
}
