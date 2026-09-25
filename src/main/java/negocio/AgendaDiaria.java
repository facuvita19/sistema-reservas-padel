package negocio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AgendaDiaria {

    private LocalDate fecha;
    private final List<Cancha> canchas = new ArrayList<>();
    private final List<FilaAgenda> filas = new ArrayList<>();

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate valor) { fecha = valor; }

    public List<Cancha> getCanchas() {
        return Collections.unmodifiableList(canchas);
    }

    public void setCanchas(List<Cancha> valores) {
        canchas.clear();
        if (valores != null) {
            canchas.addAll(valores);
        }
    }

    public List<FilaAgenda> getFilas() {
        return Collections.unmodifiableList(filas);
    }

    public void agregarFila(FilaAgenda fila) {
        if (fila == null) {
            throw new IllegalArgumentException("La fila no puede ser nula.");
        }
        filas.add(fila);
    }
}
