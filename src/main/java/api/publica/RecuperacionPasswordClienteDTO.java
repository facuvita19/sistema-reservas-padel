package api.publica;

import java.time.LocalDateTime;

public final class RecuperacionPasswordClienteDTO {

    private RecuperacionPasswordClienteDTO() {
    }

    public record SolicitarRecuperacion(String email) {
    }

    public record RestablecerPassword(
            String token,
            String passwordNuevo,
            String passwordRepetido) {
    }

    public record RespuestaSolicitud(
            String mensaje,
            String enlacePrueba,
            LocalDateTime vencimiento) {
    }

    public record EstadoToken(boolean vigente) {
    }

    public record Mensaje(String mensaje) {
    }
}
