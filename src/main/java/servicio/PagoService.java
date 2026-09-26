package servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import dao.PagoDAO;
import dao.PagoDAOMySQL;
import dao.ReservaDAO;
import dao.ReservaDAOMySQL;
import negocio.EstadoPago;
import negocio.EstadoReserva;
import negocio.Pago;
import negocio.Reserva;

public class PagoService {

    private final PagoDAO pagoDAO;
    private final ReservaDAO reservaDAO;

    public PagoService() {
        this(new PagoDAOMySQL(), new ReservaDAOMySQL());
    }

    public PagoService(PagoDAO pagoDAO, ReservaDAO reservaDAO) {
        if (pagoDAO == null || reservaDAO == null) {
            throw new IllegalArgumentException(
                    "Las dependencias de pagos no pueden ser nulas."
            );
        }
        this.pagoDAO = pagoDAO;
        this.reservaDAO = reservaDAO;
    }

    public void guardar(Pago pago) {
        validarPago(pago);
        normalizar(pago);

        Reserva reserva = obtenerReserva(pago.getReservaId());
        validarReservaCobrable(reserva);
        validarImporteContraSaldo(pago, reserva);

        if (pago.getEstado() == null) {
            pago.setEstado(EstadoPago.PENDIENTE);
        }

        if (pago.getEstado() == EstadoPago.ACREDITADO
                && pago.getFechaPago() == null) {
            pago.setFechaPago(LocalDateTime.now());
        }

        pagoDAO.guardar(pago);
    }

    public void acreditar(long pagoId) {
        Pago pago = obtenerPago(pagoId);

        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new IllegalArgumentException(
                    "Solo se pueden acreditar pagos pendientes."
            );
        }

        Reserva reserva = obtenerReserva(pago.getReservaId());
        validarImporteContraSaldo(pago, reserva);
        pagoDAO.actualizarEstado(pagoId, EstadoPago.ACREDITADO);
    }

    public void anular(long pagoId) {
        Pago pago = obtenerPago(pagoId);

        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new IllegalArgumentException(
                    "Solo se pueden anular pagos pendientes."
            );
        }

        pagoDAO.actualizarEstado(pagoId, EstadoPago.ANULADO);
    }

    private void reembolsarMovimiento(long pagoId) {
        Pago pago = obtenerPago(pagoId);

        if (pago.getEstado() != EstadoPago.ACREDITADO) {
            throw new IllegalArgumentException(
                    "Solo se pueden reembolsar pagos acreditados."
            );
        }

        pagoDAO.actualizarEstado(
                pagoId,
                EstadoPago.REEMBOLSADO
        );
    }
    
    public void reembolsarPagosDeReservaAutorizado(
            long reservaId) {

        validarId(reservaId, "reserva");

        Reserva reserva = obtenerReserva(reservaId);

        if (reserva.getEstado()
                != EstadoReserva.CANCELADA) {

            throw new IllegalArgumentException(
                    "La reserva debe estar cancelada "
                            + "antes de registrar el reembolso."
            );
        }

        List<Pago> pagosReserva =
                pagoDAO.listarPorReserva(reservaId);

        List<Pago> acreditados = pagosReserva.stream()
                .filter(pago ->
                        pago.getEstado()
                                == EstadoPago.ACREDITADO
                )
                .toList();

        if (acreditados.isEmpty()) {
            throw new IllegalArgumentException(
                    "La reserva no tiene pagos acreditados "
                            + "para reembolsar."
            );
        }

        for (Pago pago : acreditados) {
            reembolsarMovimiento(pago.getId());
        }
    }
    
    public BigDecimal totalAcreditado(long reservaId) {
        validarId(reservaId, "reserva");

        return pagoDAO.totalAcreditado(reservaId)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    public boolean tienePagosAcreditados(long reservaId) {
        return totalAcreditado(reservaId)
                .compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal calcularSaldo(long reservaId) {
        Reserva reserva = obtenerReserva(reservaId);
        BigDecimal acreditado = pagoDAO.totalAcreditado(reservaId);
        BigDecimal saldo = reserva.getPrecioTotal().subtract(acreditado);
        return saldo.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean estaPagada(long reservaId) {
        return calcularSaldo(reservaId).compareTo(BigDecimal.ZERO) == 0;
    }

    public Pago buscar(long id) {
        return id <= 0 ? null : pagoDAO.buscar(id);
    }

    public List<Pago> listar() {
        return pagoDAO.listar();
    }

    public List<Pago> listarPorReserva(long reservaId) {
        validarId(reservaId, "reserva");
        return pagoDAO.listarPorReserva(reservaId);
    }

    private void validarPago(Pago pago) {
        if (pago == null) {
            throw new IllegalArgumentException("El pago no puede ser nulo.");
        }

        validarId(pago.getReservaId(), "reserva");

        if (pago.getImporte() == null
                || pago.getImporte().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El importe debe ser mayor que cero."
            );
        }

        if (pago.getMetodoPago() == null) {
            throw new IllegalArgumentException(
                    "El método de pago es obligatorio."
            );
        }
    }

    private void normalizar(Pago pago) {
        pago.setImporte(pago.getImporte().setScale(
                2,
                RoundingMode.HALF_UP
        ));

        String referencia = pago.getReferencia();
        pago.setReferencia(
                referencia == null || referencia.isBlank()
                        ? null
                        : referencia.trim().replaceAll("\\s+", " ")
        );
    }

    private void validarReservaCobrable(Reserva reserva) {
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new IllegalArgumentException(
                    "No se pueden registrar pagos sobre una reserva cancelada."
            );
        }
    }

    private void validarImporteContraSaldo(Pago pago, Reserva reserva) {
        BigDecimal acreditado = pagoDAO.totalAcreditado(reserva.getId());
        BigDecimal saldo = reserva.getPrecioTotal().subtract(acreditado);

        if (pago.getId() > 0
                && pago.getEstado() == EstadoPago.ACREDITADO) {
            saldo = saldo.add(pago.getImporte());
        }

        if (pago.getImporte().compareTo(saldo) > 0) {
            throw new IllegalArgumentException(
                    "El importe supera el saldo pendiente de la reserva."
            );
        }
    }

    private Pago obtenerPago(long pagoId) {
        validarId(pagoId, "pago");
        Pago pago = pagoDAO.buscar(pagoId);

        if (pago == null) {
            throw new IllegalArgumentException("El pago no existe.");
        }
        return pago;
    }

    private Reserva obtenerReserva(long reservaId) {
        validarId(reservaId, "reserva");
        Reserva reserva = reservaDAO.buscar(reservaId);

        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no existe.");
        }
        return reserva;
    }

    private void validarId(long id, String entidad) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El ID de " + entidad + " no es válido."
            );
        }
    }
}
