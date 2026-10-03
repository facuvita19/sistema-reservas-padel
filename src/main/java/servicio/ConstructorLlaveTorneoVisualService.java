package servicio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import negocio.ClasificadoEtapaEliminatoria;
import negocio.CrucePropuestoTorneo;
import negocio.PropuestaEtapaEliminatoria;
import negocio.visual.ConexionLlaveVisual;
import negocio.visual.EstadoConexionLlave;
import negocio.visual.FaseLlaveVisual;
import negocio.visual.LlaveTorneoVisual;
import negocio.visual.PartidoLlaveVisual;
import negocio.visual.PlazaLlaveVisual;

public class ConstructorLlaveTorneoVisualService {
    public LlaveTorneoVisual construir(
            PropuestaEtapaEliminatoria propuesta) {
        if (propuesta == null) {
            return new LlaveTorneoVisual(List.of(), List.of(),
                    List.of("La propuesta es obligatoria."));
        }
        Map<String, String> textos = textosClasificados(
                propuesta.getClasificados());
        List<CrucePropuestoTorneo> cruces = propuesta.getCruces();
        Map<String, CrucePropuestoTorneo> porClave = new HashMap<>();
        for (CrucePropuestoTorneo cruce : cruces) {
            porClave.put(clave(cruce), cruce);
        }

        List<PartidoLlaveVisual> partidos = new ArrayList<>();
        List<ConexionLlaveVisual> conexiones = new ArrayList<>();
        List<String> problemas = new ArrayList<>();
        Map<String, Integer> usos = new HashMap<>();

        for (CrucePropuestoTorneo cruce : cruces) {
            String destino = clave(cruce);
            PlazaLlaveVisual plaza1 = plaza(cruce.getParticipante1(), 1,
                    destino, textos, porClave, conexiones, usos, problemas);
            PlazaLlaveVisual plaza2 = plaza(cruce.getParticipante2(), 2,
                    destino, textos, porClave, conexiones, usos, problemas);
            partidos.add(new PartidoLlaveVisual(destino,
                    cruce.getInstancia(), cruce.getRonda(),
                    cruce.getOrden(), plaza1, plaza2));
        }

        for (CrucePropuestoTorneo cruce : cruces) {
            String ganador = "Ganador " + clave(cruce);
            if (!"Final".equals(cruce.getInstancia())
                    && usos.getOrDefault(ganador, 0) == 0) {
                problemas.add("El ganador de " + textoClave(clave(cruce))
                        + " no tiene destino.");
                conexiones.add(new ConexionLlaveVisual(clave(cruce), null,
                        0, EstadoConexionLlave.SIN_DESTINO));
            }
        }

        List<FaseLlaveVisual> fases = agrupar(partidos);
        return new LlaveTorneoVisual(fases, conexiones,
                problemas.stream().distinct().toList());
    }

    private PlazaLlaveVisual plaza(String referencia, int posicion,
            String destino, Map<String, String> textos,
            Map<String, CrucePropuestoTorneo> porClave,
            List<ConexionLlaveVisual> conexiones,
            Map<String, Integer> usos, List<String> problemas) {
        if (referencia == null || referencia.isBlank()) {
            return new PlazaLlaveVisual(posicion, referencia,
                    "Sin asignar", false, null);
        }
        if (!referencia.startsWith("Ganador ")) {
            return new PlazaLlaveVisual(posicion, referencia,
                    textos.getOrDefault(referencia, referencia), false, null);
        }
        String origen = referencia.substring("Ganador ".length());
        usos.merge(referencia, 1, Integer::sum);
        CrucePropuestoTorneo partidoOrigen = porClave.get(origen);
        EstadoConexionLlave estado;
        if (partidoOrigen == null) {
            estado = EstadoConexionLlave.ORIGEN_INEXISTENTE;
            problemas.add("No existe " + textoClave(origen) + ".");
        } else {
            CrucePropuestoTorneo partidoDestino = porClave.get(destino);
            estado = partidoDestino != null
                    && partidoOrigen.getRonda() >= partidoDestino.getRonda()
                    ? EstadoConexionLlave.RONDA_INVALIDA
                    : EstadoConexionLlave.VALIDA;
        }
        conexiones.add(new ConexionLlaveVisual(origen, destino,
                posicion, estado));
        return new PlazaLlaveVisual(posicion, referencia,
                "Ganador de " + textoClave(origen), true, origen);
    }

    private List<FaseLlaveVisual> agrupar(
            List<PartidoLlaveVisual> partidos) {
        Map<String, List<PartidoLlaveVisual>> grupos = new LinkedHashMap<>();
        partidos.stream().sorted(Comparator
                .comparingInt(PartidoLlaveVisual::ronda)
                .thenComparingInt(PartidoLlaveVisual::orden))
                .forEach(p -> grupos.computeIfAbsent(
                        p.ronda() + "|" + p.fase(), k -> new ArrayList<>())
                        .add(p));
        List<FaseLlaveVisual> resultado = new ArrayList<>();
        int indice = 0;
        for (List<PartidoLlaveVisual> valores : grupos.values()) {
            PartidoLlaveVisual primero = valores.get(0);
            resultado.add(new FaseLlaveVisual(primero.fase(),
                    primero.ronda(), indice++, valores));
        }
        return resultado;
    }

    private Map<String, String> textosClasificados(
            List<ClasificadoEtapaEliminatoria> clasificados) {
        Map<String, String> resultado = new HashMap<>();
        for (ClasificadoEtapaEliminatoria clasificado : clasificados) {
            resultado.put(clasificado.referencia(),
                    clasificado.referencia());
        }
        return resultado;
    }

    private String clave(CrucePropuestoTorneo cruce) {
        return cruce.getInstancia() + " #" + cruce.getOrden();
    }

    private String textoClave(String clave) {
        if (clave == null) return "un partido sin identificar";
        int separador = clave.lastIndexOf(" #");
        if (separador < 0) return clave;
        return clave.substring(0, separador) + " (Partido "
                + clave.substring(separador + 2) + ")";
    }
}
