package negocio;

public record TorneoPartidoEnlace(
        long partidoOrigenId,
        ResultadoOrigenPartido resultadoOrigen,
        long partidoDestinoId,
        PosicionPartidoSiguiente posicionDestino) {
}
