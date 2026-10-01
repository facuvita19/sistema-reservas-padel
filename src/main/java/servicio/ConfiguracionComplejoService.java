package servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.regex.Pattern;

import dao.ConfiguracionComplejoDAO;
import dao.ConfiguracionComplejoDAOMySQL;
import negocio.ConfiguracionComplejo;

public class ConfiguracionComplejoService {
    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");
    private static final Pattern MONEDA = Pattern.compile("^[A-Z]{3}$");
    private final ConfiguracionComplejoDAO configuracionDAO;

    public ConfiguracionComplejoService() { this(new ConfiguracionComplejoDAOMySQL()); }
    public ConfiguracionComplejoService(ConfiguracionComplejoDAO dao) {
        if (dao == null) throw new IllegalArgumentException("El DAO de configuración no puede ser nulo.");
        configuracionDAO = dao;
    }

    public ConfiguracionComplejo obtener() {
        ConfiguracionComplejo c = configuracionDAO.obtener();
        return c == null ? new ConfiguracionComplejo() : c;
    }

    public void guardar(ConfiguracionComplejo c) {
        validar(c); normalizar(c); c.setId(ConfiguracionComplejo.ID_UNICO);
        configuracionDAO.guardar(c);
    }

    public BigDecimal calcularSenia(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("El precio de la reserva no es válido.");
        return precio.multiply(obtener().getPorcentajeSenia())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private void validar(ConfiguracionComplejo c) {
        if (c == null) throw new IllegalArgumentException("La configuración no puede ser nula.");
        if (c.getNombreComercial() == null || c.getNombreComercial().isBlank())
            throw new IllegalArgumentException("El nombre comercial es obligatorio.");
        if (c.getEmail() != null && !c.getEmail().isBlank()
                && !EMAIL.matcher(c.getEmail().trim()).matches())
            throw new IllegalArgumentException("El correo electrónico no tiene un formato válido.");
        if (c.getMoneda() == null || !MONEDA.matcher(c.getMoneda().trim().toUpperCase(Locale.ROOT)).matches())
            throw new IllegalArgumentException("La moneda debe tener un código de tres letras.");
        BigDecimal p = c.getPorcentajeSenia();
        if (p == null || p.signum() < 0 || p.compareTo(BigDecimal.valueOf(100)) > 0)
            throw new IllegalArgumentException("El porcentaje de seña debe estar entre 0 y 100.");
        if (c.getAnticipacionMinimaHoras() < 0 || c.getCancelacionMinimaHoras() < 0)
            throw new IllegalArgumentException("Los plazos horarios no pueden ser negativos.");
        if (c.getMinutosReservaPendiente() < 1 || c.getMinutosReservaPendiente() > 1440)
            throw new IllegalArgumentException("El plazo para pagar la seña debe estar entre 1 y 1440 minutos.");
        if (c.getColorPrincipal() == null || !COLOR.matcher(c.getColorPrincipal().trim()).matches())
            throw new IllegalArgumentException("El color principal debe tener formato hexadecimal #RRGGBB.");
        if (c.getPagoAlias() != null && c.getPagoAlias().length() > 120)
            throw new IllegalArgumentException("El alias no puede superar 120 caracteres.");
        if (c.getPagoTitular() != null && c.getPagoTitular().length() > 160)
            throw new IllegalArgumentException("El titular no puede superar 160 caracteres.");
        if (c.getPagoEntidad() != null && c.getPagoEntidad().length() > 120)
            throw new IllegalArgumentException("La entidad no puede superar 120 caracteres.");
        if (c.getPagoInstrucciones() != null && c.getPagoInstrucciones().length() > 700)
            throw new IllegalArgumentException("Las instrucciones no pueden superar 700 caracteres.");
    }

    private void normalizar(ConfiguracionComplejo c) {
        c.setNombreComercial(limpiar(c.getNombreComercial()));
        c.setRazonSocial(opcional(c.getRazonSocial())); c.setDireccion(opcional(c.getDireccion()));
        c.setTelefono(opcional(c.getTelefono())); c.setWhatsapp(opcional(c.getWhatsapp()));
        c.setInstagram(opcional(c.getInstagram())); c.setRutaLogo(opcional(c.getRutaLogo()));
        c.setPagoAlias(opcional(c.getPagoAlias())); c.setPagoTitular(opcional(c.getPagoTitular()));
        c.setPagoEntidad(opcional(c.getPagoEntidad())); c.setPagoInstrucciones(opcional(c.getPagoInstrucciones()));
        c.setEmail(c.getEmail() == null || c.getEmail().isBlank() ? null : c.getEmail().trim().toLowerCase(Locale.ROOT));
        c.setMoneda(c.getMoneda().trim().toUpperCase(Locale.ROOT));
        c.setPorcentajeSenia(c.getPorcentajeSenia().setScale(2, RoundingMode.HALF_UP));
        c.setColorPrincipal(c.getColorPrincipal().trim().toUpperCase(Locale.ROOT));
    }
    private String limpiar(String v) { return v.trim().replaceAll("\\s+", " "); }
    private String opcional(String v) { return v == null || v.isBlank() ? null : limpiar(v); }
}
