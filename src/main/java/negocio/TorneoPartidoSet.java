package negocio;

import java.util.Objects;

public class TorneoPartidoSet {

    private long id;
    private long partidoId;
    private int numeroSet;
    private TipoSetTorneo tipo = TipoSetTorneo.NORMAL;
    private int puntosPareja1;
    private int puntosPareja2;

    public long getId() { return id; }
    public void setId(long valor) { id = valor; }
    public long getPartidoId() { return partidoId; }
    public void setPartidoId(long valor) { partidoId = valor; }
    public int getNumeroSet() { return numeroSet; }
    public void setNumeroSet(int valor) { numeroSet = valor; }
    public TipoSetTorneo getTipo() { return tipo; }
    public void setTipo(TipoSetTorneo valor) {
        tipo = valor == null ? TipoSetTorneo.NORMAL : valor;
    }
    public int getPuntosPareja1() { return puntosPareja1; }
    public void setPuntosPareja1(int valor) { puntosPareja1 = valor; }
    public int getPuntosPareja2() { return puntosPareja2; }
    public void setPuntosPareja2(int valor) { puntosPareja2 = valor; }

    public int parejaGanadora() {
        if (puntosPareja1 == puntosPareja2) return 0;
        return puntosPareja1 > puntosPareja2 ? 1 : 2;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof TorneoPartidoSet otro)) return false;
        return id > 0 && id == otro.id;
    }

    @Override
    public int hashCode() {
        return id > 0 ? Objects.hash(id) : System.identityHashCode(this);
    }
}
