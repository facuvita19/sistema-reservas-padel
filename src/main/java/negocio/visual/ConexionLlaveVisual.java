package negocio.visual;

public record ConexionLlaveVisual(
        String partidoOrigenClave,
        String partidoDestinoClave,
        int posicionDestino,
        EstadoConexionLlave estado) {
}
