package servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

import negocio.Cliente;
import negocio.ConfiguracionComplejo;
import negocio.Reserva;

public class MensajeReservaService {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm");

    private final ConfiguracionComplejoService configuracionService;

    public MensajeReservaService() {
        this(new ConfiguracionComplejoService());
    }

    public MensajeReservaService(
            ConfiguracionComplejoService configuracionService) {

        if (configuracionService == null) {
            throw new IllegalArgumentException(
                    "El servicio de configuración no puede ser nulo."
            );
        }

        this.configuracionService = configuracionService;
    }

    public String crearConfirmacion(
            Cliente cliente,
            Reserva reserva) {

        validarDatos(cliente, reserva);

        return saludo(cliente)
                + " Tu reserva en "
                + nombreComplejo()
                + " fue confirmada para el "
                + reserva.getFecha().format(FORMATO_FECHA)
                + " a las "
                + reserva.getHoraInicio().format(FORMATO_HORA)
                + " en "
                + nombreCancha(reserva)
                + ". Te esperamos.";
    }

    public String crearRecordatorio(
            Cliente cliente,
            Reserva reserva) {

        validarDatos(cliente, reserva);

        return saludo(cliente)
                + " Te recordamos tu reserva en "
                + nombreComplejo()
                + " para el "
                + reserva.getFecha().format(FORMATO_FECHA)
                + " a las "
                + reserva.getHoraInicio().format(FORMATO_HORA)
                + " en "
                + nombreCancha(reserva)
                + ".";
    }

    public String crearSolicitudSenia(
            Cliente cliente,
            Reserva reserva,
            BigDecimal seniaRequerida) {

        validarDatos(cliente, reserva);
        validarImporte(seniaRequerida, "La seña requerida");

        String vencimiento = reserva.getFechaVencimiento() == null
                ? ""
                : " El plazo vence el "
                        + reserva.getFechaVencimiento().format(
                                DateTimeFormatter.ofPattern(
                                        "dd/MM/yyyy 'a las' HH:mm"
                                )
                        )
                        + ".";

        return saludo(cliente)
                + " Tu reserva para el "
                + reserva.getFecha().format(FORMATO_FECHA)
                + " a las "
                + reserva.getHoraInicio().format(FORMATO_HORA)
                + " en "
                + nombreCancha(reserva)
                + " está esperando la seña de "
                + formatearImporte(seniaRequerida)
                + "."
                + vencimiento;
    }

    public String crearAvisoSaldo(
            Cliente cliente,
            Reserva reserva,
            BigDecimal saldoPendiente) {

        validarDatos(cliente, reserva);
        validarImporte(saldoPendiente, "El saldo pendiente");

        return saludo(cliente)
                + " Tu reserva del "
                + reserva.getFecha().format(FORMATO_FECHA)
                + " a las "
                + reserva.getHoraInicio().format(FORMATO_HORA)
                + " tiene un saldo pendiente de "
                + formatearImporte(saldoPendiente)
                + ".";
    }

    public String crearAvisoCancelacion(
            Cliente cliente,
            Reserva reserva) {

        validarDatos(cliente, reserva);

        return saludo(cliente)
                + " Te informamos que tu reserva del "
                + reserva.getFecha().format(FORMATO_FECHA)
                + " a las "
                + reserva.getHoraInicio().format(FORMATO_HORA)
                + " en "
                + nombreCancha(reserva)
                + " fue cancelada.";
    }

    public String crearAvisoReprogramacion(
            Cliente cliente,
            Reserva reserva) {

        validarDatos(cliente, reserva);

        return saludo(cliente)
                + " Tu reserva fue reprogramada para el "
                + reserva.getFecha().format(FORMATO_FECHA)
                + " a las "
                + reserva.getHoraInicio().format(FORMATO_HORA)
                + " en "
                + nombreCancha(reserva)
                + ".";
    }

    private void validarDatos(
            Cliente cliente,
            Reserva reserva) {

        if (cliente == null) {
            throw new IllegalArgumentException(
                    "El cliente es obligatorio."
            );
        }

        if (reserva == null
                || reserva.getFecha() == null
                || reserva.getHoraInicio() == null) {

            throw new IllegalArgumentException(
                    "La reserva no tiene los datos necesarios."
            );
        }
    }

    private void validarImporte(
            BigDecimal importe,
            String nombre) {

        if (importe == null
                || importe.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    nombre + " no es válida."
            );
        }
    }

    private String saludo(Cliente cliente) {
        String nombre = cliente.getNombre();

        if (nombre == null || nombre.isBlank()) {
            return "Hola.";
        }

        return "Hola, " + nombre.trim() + ".";
    }

    private String nombreCancha(Reserva reserva) {
        return reserva.getNombreCancha() == null
                || reserva.getNombreCancha().isBlank()
                        ? "la cancha reservada"
                        : reserva.getNombreCancha().trim();
    }

    private String nombreComplejo() {
        ConfiguracionComplejo configuracion =
                configuracionService.obtener();

        return configuracion.getNombreComercial() == null
                || configuracion.getNombreComercial().isBlank()
                        ? "el complejo"
                        : configuracion.getNombreComercial().trim();
    }

    private String formatearImporte(BigDecimal importe) {
        ConfiguracionComplejo configuracion =
                configuracionService.obtener();

        String moneda = configuracion.getMoneda() == null
                || configuracion.getMoneda().isBlank()
                        ? "ARS"
                        : configuracion.getMoneda().trim();

        return moneda
                + " "
                + importe.setScale(2, RoundingMode.HALF_UP)
                        .toPlainString();
    }
}
