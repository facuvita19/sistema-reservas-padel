package servicio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import negocio.ClasificadoEtapaEliminatoria;
import negocio.CrucePropuestoTorneo;

public class ValidadorEstructuraEliminatoriaService {
    public List<String> validar(List<CrucePropuestoTorneo> cruces,
            List<ClasificadoEtapaEliminatoria> clasificados) {
        List<String> errores = new ArrayList<>();
        if (cruces == null || cruces.isEmpty()) {
            return List.of("La estructura no contiene partidos.");
        }
        Map<String, CrucePropuestoTorneo> porClave = new LinkedHashMap<>();
        Map<String, Integer> usosGanador = new HashMap<>();
        for (CrucePropuestoTorneo cruce : cruces) {
            String clave = clave(cruce);
            if (porClave.put(clave, cruce) != null) {
                errores.add("PARTIDO REPETIDO:\n"
                        + describirClaveConParentesis(clave)
                        + " aparece mas de una vez dentro de la misma llave.\n"
                        + "Cambia el orden de una de las tarjetas.");
            }
            if (cruce.getRonda() < 1) errores.add(clave + " tiene ronda invalida.");
            if (cruce.getOrden() < 1) errores.add(clave + " tiene orden invalido.");
            validarOrigen(cruce, cruce.getParticipante1(), porClave,
                    usosGanador, errores);
            validarOrigen(cruce, cruce.getParticipante2(), porClave,
                    usosGanador, errores);
        }

        Set<String> claves = porClave.keySet();
        for (CrucePropuestoTorneo cruce : cruces) {
            for (String origen : List.of(cruce.getParticipante1(),
                    cruce.getParticipante2())) {
                if (!esGanador(origen)) continue;
                String origenClave = claveDesdeGanador(origen);
                CrucePropuestoTorneo partidoOrigen = porClave.get(origenClave);
                if (partidoOrigen == null) {
                    errores.add(clave(cruce) + " usa un ganador inexistente: "
                            + origen + ".");
                    continue;
                }
                if (origenClave.equals(clave(cruce))) {
                    errores.add(clave(cruce) + " se referencia a si mismo.");
                }
                if (partidoOrigen.getRonda() >= cruce.getRonda()) {
                    errores.add(origen + " debe alimentar una ronda posterior a "
                            + partidoOrigen.getRonda() + ".");
                }
            }
        }

        for (Map.Entry<String, Integer> uso : usosGanador.entrySet()) {
            if (uso.getValue() > 1) {
                errores.add(uso.getKey() + " se utiliza " + uso.getValue()
                        + " veces. Cada ganador puede ocupar una sola plaza.");
            }
        }

        List<CrucePropuestoTorneo> finales = cruces.stream()
                .filter(c -> "Final".equals(c.getInstancia())).toList();
        if (finales.size() != 1) {
            errores.add("Debe existir exactamente una final.");
        } else {
            CrucePropuestoTorneo finalPartido = finales.get(0);
            if (usosGanador.containsKey("Ganador " + clave(finalPartido))) {
                errores.add("La final no puede alimentar otro partido.");
            }
        }

        validarCantidadPorFase(cruces, errores);
        detectarCiclos(porClave, errores);
        validarDestinos(cruces, usosGanador, finales, errores);
        validarFases(cruces, errores);
        return errores.stream().distinct().toList();
    }

    private void validarOrigen(CrucePropuestoTorneo cruce, String origen,
            Map<String, CrucePropuestoTorneo> porClave,
            Map<String, Integer> usosGanador, List<String> errores) {
        if (origen == null || origen.isBlank()) {
            errores.add(clave(cruce) + " tiene una plaza sin asignar.");
            return;
        }
        if (esGanador(origen)) usosGanador.merge(origen, 1, Integer::sum);
    }

    private void validarCantidadPorFase(
            List<CrucePropuestoTorneo> cruces, List<String> errores) {
        Map<String, Integer> maximos = Map.of(
                "Final", 1,
                "Semifinal", 2,
                "Cuartos", 4,
                "Octavos", 8,
                "Dieciseisavos", 16);
        Map<String, Long> cantidades = cruces.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        CrucePropuestoTorneo::getInstancia,
                        java.util.stream.Collectors.counting()));
        for (Map.Entry<String, Integer> limite : maximos.entrySet()) {
            long cantidad = cantidades.getOrDefault(limite.getKey(), 0L);
            if (cantidad > limite.getValue()) {
                errores.add("CANTIDAD DE PARTIDOS INVALIDA:\n"
                        + limite.getKey() + " contiene " + cantidad
                        + " partidos, pero admite como maximo "
                        + limite.getValue() + ".\nElimina "
                        + (cantidad - limite.getValue())
                        + " partido(s) adicional(es).");
            }
        }
    }

    private void validarDestinos(List<CrucePropuestoTorneo> cruces,
            Map<String, Integer> usos, List<CrucePropuestoTorneo> finales,
            List<String> errores) {
        Set<String> clavesFinales = finales.stream().map(this::clave)
                .collect(java.util.stream.Collectors.toSet());
        for (CrucePropuestoTorneo cruce : cruces) {
            String clave = clave(cruce);
            if (clavesFinales.contains(clave)) continue;
            String ganador = "Ganador " + clave;
            int cantidad = usos.getOrDefault(ganador, 0);
            if (cantidad == 0) errores.add(ganador + " no tiene destino.");
        }
    }

    private void detectarCiclos(Map<String, CrucePropuestoTorneo> partidos,
            List<String> errores) {
        for (String clave : partidos.keySet()) {
            if (tieneCiclo(clave, partidos, new HashSet<>(), new HashSet<>())) {
                errores.add("La estructura contiene un ciclo desde " + clave + ".");
            }
        }
    }

    private boolean tieneCiclo(String clave,
            Map<String, CrucePropuestoTorneo> partidos,
            Set<String> visitados, Set<String> camino) {
        if (!camino.add(clave)) return true;
        if (!visitados.add(clave)) { camino.remove(clave); return false; }
        CrucePropuestoTorneo cruce = partidos.get(clave);
        if (cruce != null) {
            for (String origen : List.of(cruce.getParticipante1(),
                    cruce.getParticipante2())) {
                if (esGanador(origen)) {
                    String anterior = claveDesdeGanador(origen);
                    if (partidos.containsKey(anterior)
                            && tieneCiclo(anterior, partidos, visitados, camino)) {
                        return true;
                    }
                }
            }
        }
        camino.remove(clave);
        return false;
    }

    private void validarFases(List<CrucePropuestoTorneo> cruces,
            List<String> errores) {
        Map<Integer, Set<String>> fasesPorRonda = new LinkedHashMap<>();
        for (CrucePropuestoTorneo cruce : cruces) {
            fasesPorRonda.computeIfAbsent(cruce.getRonda(), k ->
                    new LinkedHashSet<>()).add(cruce.getInstancia());
        }
        Map<String, Integer> ordenFase = Map.ofEntries(
                Map.entry("Acceso R1", 1), Map.entry("Acceso R2", 2),
                Map.entry("Acceso R3", 3), Map.entry("Acceso R4", 4),
                Map.entry("Acceso R5", 5), Map.entry("Dieciseisavos", 6),
                Map.entry("Octavos", 7), Map.entry("Cuartos", 8),
                Map.entry("Semifinal", 9), Map.entry("Final", 10));
        int maximoAnterior = 0;
        int mayorRondaPrevia = cruces.stream()
                .filter(c -> c.getInstancia().startsWith("Acceso R"))
                .mapToInt(CrucePropuestoTorneo::getRonda).max().orElse(0);
        int menorRondaPrincipal = cruces.stream()
                .filter(c -> !c.getInstancia().startsWith("Acceso R"))
                .mapToInt(CrucePropuestoTorneo::getRonda).min()
                .orElse(Integer.MAX_VALUE);
        if (mayorRondaPrevia >= menorRondaPrincipal) {
            errores.add("ORDEN DE FASES INVALIDO:\nLas fases previas "
                    + "deben ubicarse antes de Dieciseisavos, Octavos, "
                    + "Cuartos, Semifinal y Final.");
        }
        for (Integer ronda : fasesPorRonda.keySet().stream().sorted().toList()) {
            for (String fase : fasesPorRonda.get(ronda)) {
                Integer orden = ordenFase.get(fase);
                if (orden == null) {
                    errores.add("Instancia no reconocida: " + fase + ".");
                } else if (orden < maximoAnterior) {
                    errores.add(fase + " no puede ubicarse despues de una fase superior.");
                } else {
                    maximoAnterior = Math.max(maximoAnterior, orden);
                }
            }
        }
    }

    private String describirClaveConParentesis(String clave) {
        if (clave == null || clave.isBlank()) {
            return "un partido sin identificar";
        }
        int separador = clave.lastIndexOf(" #");
        if (separador < 0) return clave;
        String fase = clave.substring(0, separador);
        String numero = clave.substring(separador + 2);
        return fase + " (Partido " + numero + ")";
    }

    private boolean esGanador(String valor) {
        return valor != null && valor.startsWith("Ganador ");
    }
    private String claveDesdeGanador(String ganador) {
        return ganador.substring("Ganador ".length());
    }
    private String clave(CrucePropuestoTorneo cruce) {
        return cruce.getInstancia() + " #" + cruce.getOrden();
    }
}
