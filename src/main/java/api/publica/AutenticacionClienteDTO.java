package api.publica;

import java.time.LocalDateTime;

import servicio.AutenticacionClienteService.ResultadoAutenticacion;
import servicio.AutenticacionClienteService.SesionAutenticada;

public final class AutenticacionClienteDTO {

    private AutenticacionClienteDTO() {
    }

    public record Registro(
            String nombre,
            String apellido,
            String documento,
            String telefono,
            String email,
            String password) {
    }

    public record Login(
            String email,
            String password) {
    }

    public record Perfil(
            long clienteId,
            String nombre,
            String apellido,
            String email,
            String telefono) {
    }

    public record Sesion(
            boolean autenticado,
            LocalDateTime vencimiento,
            Perfil perfil) {

        public static Sesion noAutenticada() {
            return new Sesion(false, null, null);
        }

        public static Sesion desde(ResultadoAutenticacion resultado) {
            return new Sesion(
                    true,
                    resultado.vencimiento(),
                    new Perfil(
                            resultado.clienteId(),
                            resultado.nombre(),
                            resultado.apellido(),
                            resultado.email(),
                            resultado.telefono()));
        }

        public static Sesion desde(SesionAutenticada autenticada) {
            return new Sesion(
                    true,
                    autenticada.sesion().getFechaVencimiento(),
                    new Perfil(
                            autenticada.cliente().getId(),
                            autenticada.cliente().getNombre(),
                            autenticada.cliente().getApellido(),
                            autenticada.cliente().getEmail(),
                            autenticada.cliente().getTelefono()));
        }
    }

    public record Mensaje(
            String mensaje) {
    }
}
