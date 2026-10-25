package api.publica;

import java.time.LocalDateTime;
import java.util.List;
import api.publica.PagoClienteDTO.*;
import negocio.OrdenPago;
import servicio.CapacidadesPagoService;
import servicio.ConsultaOrdenPagoClienteService;
import servicio.CreacionOrdenPagoClienteService;

/** Autoriza pagos de reservas web mediante el UUID publico de seguimiento. */
public final class PagoSolicitudWebPublicaService {
    private final EstadoSolicitudWebPublicaService estados;
    private final CapacidadesPagoService capacidades;
    private final CreacionOrdenPagoClienteService creacion;
    private final ConsultaOrdenPagoClienteService consulta;

    public PagoSolicitudWebPublicaService(EstadoSolicitudWebPublicaService estados) {
        this(estados, new CapacidadesPagoService(),
                new CreacionOrdenPagoClienteService(),
                new ConsultaOrdenPagoClienteService());
    }

    PagoSolicitudWebPublicaService(EstadoSolicitudWebPublicaService estados,
            CapacidadesPagoService capacidades,
            CreacionOrdenPagoClienteService creacion,
            ConsultaOrdenPagoClienteService consulta) {
        if (estados == null || capacidades == null || creacion == null || consulta == null)
            throw new IllegalArgumentException("Las dependencias son obligatorias.");
        this.estados = estados;
        this.capacidades = capacidades;
        this.creacion = creacion;
        this.consulta = consulta;
    }

    public CapacidadesPago capacidades() { return capacidades.obtener(); }

    public OrdenPagoPublica actual(String codigo) {
        var reserva = estados.consultar(codigo);
        List<OrdenPagoPublica> ordenes = consulta.historial(reserva.solicitudId()).ordenes();
        return ordenes.stream()
                .filter(x -> "APROBADA".equals(x.estado()))
                .findFirst()
                .or(() -> ordenes.stream()
                        .filter(OrdenPagoPublica::requiereActualizacion)
                        .findFirst())
                .orElse(ordenes.isEmpty() ? sinOrden(reserva.solicitudId()) : ordenes.getFirst());
    }

    public OrdenPagoPublica crear(String codigo, String metodo) {
        var reserva = estados.consultar(codigo);
        if (!reserva.pendiente())
            throw new IllegalArgumentException("La reserva ya no admite una orden de seña.");
        if (reserva.vencimiento() == null || !LocalDateTime.now().isBefore(reserva.vencimiento()))
            throw new IllegalArgumentException("La protección temporal de la reserva ya venció.");
        String elegido = metodo == null || metodo.isBlank()
                ? "TRANSFERENCIA_MANUAL" : metodo.trim().toUpperCase();
        OrdenPago orden = switch (elegido) {
            case "TRANSFERENCIA_MANUAL" -> creacion.crearManual(
                    reserva.solicitudId(), reserva.importeSenia(), reserva.vencimiento());
            case "PAGO_ONLINE" -> creacion.crearOnline(
                    reserva.solicitudId(), reserva.importeSenia(), reserva.vencimiento(),
                    "Seña de reserva " + reserva.cancha());
            default -> throw new IllegalArgumentException("El método de pago no es válido.");
        };
        return consulta.convertir(orden);
    }

    private OrdenPagoPublica sinOrden(long reservaId) {
        return new OrdenPagoPublica(0, reservaId, null, null, "SIN_ORDEN", null, null,
                null, 0, false, false, false, false, null,
                "La reserva todavía no tiene una orden de pago.");
    }
}
