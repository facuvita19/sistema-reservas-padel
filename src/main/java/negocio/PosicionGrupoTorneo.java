package negocio;

public record PosicionGrupoTorneo(
        int posicion,
        long inscripcionId,
        int partidosJugados,
        int partidosGanados,
        int partidosPerdidos,
        int setsGanados,
        int setsPerdidos,
        int gamesGanados,
        int gamesPerdidos,
        EstadoClasificacionGrupo estado,
        boolean desempatePendiente) {

    public int diferenciaSets() {
        return setsGanados - setsPerdidos;
    }

    public int diferenciaGames() {
        return gamesGanados - gamesPerdidos;
    }

    public String setsResumen() {
        return setsGanados + "-" + setsPerdidos;
    }

    public String gamesResumen() {
        return gamesGanados + "-" + gamesPerdidos;
    }
}
