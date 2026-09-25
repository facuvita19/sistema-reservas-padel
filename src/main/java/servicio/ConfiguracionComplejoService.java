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
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern COLOR = Pattern.compile(
            "^#[0-9A-Fa-f]{6}$"
    );
    private static final Pattern MONEDA = Pattern.compile(
            "^[A-Z]{3}$"
    );

    private final ConfiguracionComplejoDAO configuracionDAO;

    public ConfiguracionComplejoService() {
        this(new ConfiguracionComplejoDAOMySQL());
    }

    public ConfiguracionComplejoService(
            ConfiguracionComplejoDAO configuracionDAO) {
        if (configuracionDAO == null) {
            throw new IllegalArgumentException(
                    "El DAO de configuración no puede ser nulo."
            );
        }
        this.configuracionDAO = configuracionDAO;
    }

    public ConfiguracionComplejo obtener() {
        ConfiguracionComplejo configuracion = configuracionDAO.obtener();
        return configuracion == null
                ? new ConfiguracionComplejo()
                : configuracion;
    }

    public void guardar(ConfiguracionComplejo configuracion) {
        validar(configuracion);
        normalizar(configuracion);
        configuracion.setId(ConfiguracionComplejo.ID_UNICO);
        configuracionDAO.guardar(configuracion);
    }

    public BigDecimal calcularSenia(BigDecimal precioReserva) {
        if (precioReserva == null
                || precioReserva.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El precio de la reserva no es válido."
            );
        }

        BigDecimal porcentaje = obtener().getPorcentajeSenia();
        return precioReserva.multiply(porcentaje)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private void validar(ConfiguracionComplejo configuracion) {
        if (configuracion == null) {
            throw new IllegalArgumentException(
                    "La configuración no puede ser nula."
            );
        }
        if (configuracion.getNombreComercial() == null
                || configuracion.getNombreComercial().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre comercial es obligatorio."
            );
        }
        if (configuracion.getEmail() != null
                && !configuracion.getEmail().isBlank()
                && !EMAIL.matcher(configuracion.getEmail().trim()).matches()) {
            throw new IllegalArgumentException(
                    "El correo electrónico no tiene un formato válido."
            );
        }
        if (configuracion.getMoneda() == null
                || !MONEDA.matcher(
                        configuracion.getMoneda().trim().toUpperCase(Locale.ROOT)
                ).matches()) {
            throw new IllegalArgumentException(
                    "La moneda debe tener un código de tres letras."
            );
        }
        BigDecimal porcentaje = configuracion.getPorcentajeSenia();
        if (porcentaje == null
                || porcentaje.compareTo(BigDecimal.ZERO) < 0
                || porcentaje.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException(
                    "El porcentaje de seña debe estar entre 0 y 100."
            );
        }
        if (configuracion.getAnticipacionMinimaHoras() < 0
                || configuracion.getCancelacionMinimaHoras() < 0) {
            throw new IllegalArgumentException(
                    "Los plazos horarios no pueden ser negativos."
            );
        }
        if (configuracion.getColorPrincipal() == null
                || !COLOR.matcher(
                        configuracion.getColorPrincipal().trim()
                ).matches()) {
            throw new IllegalArgumentException(
                    "El color principal debe tener formato hexadecimal #RRGGBB."
            );
        }
    }

    private void normalizar(ConfiguracionComplejo configuracion) {
        configuracion.setNombreComercial(
                limpiarObligatorio(configuracion.getNombreComercial()));
        configuracion.setRazonSocial(
                limpiarOpcional(configuracion.getRazonSocial()));
        configuracion.setDireccion(
                limpiarOpcional(configuracion.getDireccion()));
        configuracion.setTelefono(
                limpiarOpcional(configuracion.getTelefono()));
        configuracion.setWhatsapp(
                limpiarOpcional(configuracion.getWhatsapp()));
        configuracion.setInstagram(
                limpiarOpcional(configuracion.getInstagram()));
        configuracion.setRutaLogo(
                limpiarOpcional(configuracion.getRutaLogo()));
        configuracion.setEmail(configuracion.getEmail() == null
                || configuracion.getEmail().isBlank()
                        ? null
                        : configuracion.getEmail().trim().toLowerCase(Locale.ROOT));
        configuracion.setMoneda(
                configuracion.getMoneda().trim().toUpperCase(Locale.ROOT));
        configuracion.setPorcentajeSenia(
                configuracion.getPorcentajeSenia().setScale(
                        2,
                        RoundingMode.HALF_UP));
        configuracion.setColorPrincipal(
                configuracion.getColorPrincipal().trim().toUpperCase(Locale.ROOT));
    }

    private String limpiarObligatorio(String valor) {
        return valor.trim().replaceAll("\\s+", " ");
    }

    private String limpiarOpcional(String valor) {
        return valor == null || valor.isBlank()
                ? null
                : valor.trim().replaceAll("\\s+", " ");
    }
}
