package api.publica;

import java.time.LocalDateTime;

public final class PreferenciasComunidadDTO {
    private PreferenciasComunidadDTO() {}

    public record Estado(
            boolean disponibleParaJugar,
            boolean aceptaInvitaciones,
            boolean buscarTurnos,
            boolean buscarTorneos,
            boolean contactoWhatsapp,
            boolean consentimientoWhatsappVigente,
            boolean publicadoEnComunidad,
            LocalDateTime fechaConsentimientoWhatsapp,
            LocalDateTime fechaActualizacion,
            long version) {}

    public record Actualizar(
            Boolean disponibleParaJugar,
            Boolean aceptaInvitaciones,
            Boolean buscarTurnos,
            Boolean buscarTorneos,
            Boolean contactoWhatsapp,
            Long version) {}
}
