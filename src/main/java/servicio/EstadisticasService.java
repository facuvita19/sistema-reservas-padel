package servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import dao.EstadisticasDAO;
import dao.EstadisticasDAOMySQL;
import negocio.EstadisticasPadel;

public class EstadisticasService {
    private final EstadisticasDAO estadisticasDAO;
    public EstadisticasService(){this(new EstadisticasDAOMySQL());}
    public EstadisticasService(EstadisticasDAO dao){if(dao==null)throw new IllegalArgumentException("El DAO de estadísticas no puede ser nulo.");estadisticasDAO=dao;}
    public EstadisticasPadel obtenerUltimosDoceMeses(){LocalDate h=LocalDate.now();return obtener(h.minusMonths(11).withDayOfMonth(1),h);}
    public EstadisticasPadel obtener(LocalDate desde, LocalDate hasta){validar(desde,hasta); long dias=ChronoUnit.DAYS.between(desde,hasta)+1; LocalDate anteriorHasta=desde.minusDays(1); LocalDate anteriorDesde=anteriorHasta.minusDays(dias-1); EstadisticasPadel anterior=estadisticasDAO.obtenerEstadisticas(anteriorDesde,anteriorHasta); EstadisticasPadel actual=estadisticasDAO.obtenerEstadisticas(desde,hasta); actual.setVariacionReservas(variacion(actual.getTotalReservas(),anterior.getTotalReservas())); actual.setVariacionIngresos(variacion(actual.getIngresosAcreditados(),anterior.getIngresosAcreditados())); return actual;}
    private BigDecimal variacion(int actual,int anterior){return variacion(BigDecimal.valueOf(actual),BigDecimal.valueOf(anterior));}
    private BigDecimal variacion(BigDecimal actual,BigDecimal anterior){if(anterior==null||anterior.signum()==0)return actual!=null&&actual.signum()>0?BigDecimal.valueOf(100):BigDecimal.ZERO;return actual.subtract(anterior).multiply(BigDecimal.valueOf(100)).divide(anterior.abs(),2,RoundingMode.HALF_UP);}
    private void validar(LocalDate d,LocalDate h){if(d==null||h==null)throw new IllegalArgumentException("Las fechas son obligatorias.");if(d.isAfter(LocalDate.now())||h.isAfter(LocalDate.now()))throw new IllegalArgumentException("El período no puede incluir fechas futuras.");if(h.isBefore(d))throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial.");}
}
