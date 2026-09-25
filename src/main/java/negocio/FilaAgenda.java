package negocio;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilaAgenda {

    private LocalTime horaInicio;
    private final List<CeldaAgenda> celdas = new ArrayList<>();

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime valor) { horaInicio = valor; }

    public List<CeldaAgenda> getCeldas() {
        return Collections.unmodifiableList(celdas);
    }

    public void agregarCelda(CeldaAgenda celda) {
        if (celda == null) {
            throw new IllegalArgumentException("La celda no puede ser nula.");
        }
        celdas.add(celda);
    }
}
