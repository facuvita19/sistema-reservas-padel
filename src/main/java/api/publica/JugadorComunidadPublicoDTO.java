package api.publica;

import java.util.List;

public final class JugadorComunidadPublicoDTO {
    private JugadorComunidadPublicoDTO() {}

    public record Categoria(long id, String nombre, String color) {}

    public record Jugador(
            long perfilId,
            String nombre,
            String iniciales,
            String posicion,
            Categoria categoria,
            boolean disponibleParaJugar,
            boolean aceptaInvitaciones,
            boolean buscaTurnos,
            boolean buscaTorneos,
            boolean contactoWhatsappDisponible) {}

    public record Pagina(
            List<Jugador> jugadores,
            int pagina,
            int tamano,
            long total,
            int totalPaginas) {
        public Pagina {
            jugadores = jugadores == null ? List.of() : List.copyOf(jugadores);
            if (pagina < 1 || tamano < 1 || total < 0 || totalPaginas < 0) {
                throw new IllegalArgumentException("La pagina publica es invalida.");
            }
        }
    }
}
