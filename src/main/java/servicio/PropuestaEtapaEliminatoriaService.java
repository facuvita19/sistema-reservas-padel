package servicio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import config.ConexionBD;
import dao.TorneoGrupoDAO;
import dao.TorneoGrupoDAOMySQL;
import negocio.ClasificadoEtapaEliminatoria;
import negocio.CrucePropuestoTorneo;
import negocio.EstadoClasificacionGrupo;
import negocio.PosicionGrupoTorneo;
import negocio.PropuestaEtapaEliminatoria;
import negocio.TorneoGrupo;

public class PropuestaEtapaEliminatoriaService {
    private static final int MAXIMO_CLASIFICADOS = 32;
    private final TorneoGrupoDAO grupoDAO = new TorneoGrupoDAOMySQL();
    private final PosicionesGrupoTorneoService posicionesService =
            new PosicionesGrupoTorneoService();

    public PropuestaEtapaEliminatoria proponer(long categoriaId) {
        List<ClasificadoEtapaEliminatoria> clasificados =
                obtenerClasificados(categoriaId);
        PropuestaEtapaEliminatoria propuesta = new PropuestaEtapaEliminatoria();
        propuesta.setClasificados(clasificados);
        construirGeneral(clasificados, propuesta);
        validar(propuesta);
        return propuesta;
    }

    private List<ClasificadoEtapaEliminatoria> obtenerClasificados(
            long categoriaId) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            List<TorneoGrupo> grupos = grupoDAO.listar(conexion, categoriaId);
            if (grupos.isEmpty()) throw new IllegalArgumentException(
                    "La categoria no tiene grupos configurados.");
            List<ClasificadoEtapaEliminatoria> resultado = new ArrayList<>();
            for (TorneoGrupo grupo : grupos) {
                var posiciones = posicionesService.calcular(grupo);
                if (!posiciones.definitivo()) throw new IllegalArgumentException(
                        "Faltan finalizar partidos de " + grupo.getNombre() + ".");
                if (posiciones.desempatePendiente()) throw new IllegalArgumentException(
                        grupo.getNombre() + " requiere un desempate administrativo.");
                for (PosicionGrupoTorneo fila : posiciones.posiciones()) {
                    if (fila.estado() == EstadoClasificacionGrupo.CLASIFICADO) {
                        resultado.add(new ClasificadoEtapaEliminatoria(
                                fila.inscripcionId(), grupo.getId(),
                                grupo.getNombre(), fila.posicion(),
                                fila.partidosGanados(), fila.diferenciaSets(),
                                fila.diferenciaGames(), fila.setsGanados(),
                                fila.gamesGanados()));
                    }
                }
            }
            return resultado;
        } catch (SQLException exception) {
            throw new RuntimeException("No se pudieron obtener los clasificados.",
                    exception);
        }
    }

    private void construirGeneral(
            List<ClasificadoEtapaEliminatoria> clasificados,
            PropuestaEtapaEliminatoria propuesta) {
        int total = clasificados.size();
        if (total < 2 || total > MAXIMO_CLASIFICADOS) {
            invalidar(propuesta,
                    "La etapa eliminatoria admite entre 2 y 32 clasificados.");
            return;
        }
        List<ClasificadoEtapaEliminatoria> primeros = porPosicion(
                clasificados, 1);
        List<ClasificadoEtapaEliminatoria> noPrimeros = clasificados.stream()
                .filter(c -> c.posicionGrupo() > 1)
                .sorted(ranking())
                .collect(java.util.stream.Collectors.toCollection(
                        ArrayList::new));

        if (esPotenciaDos(total)) {
            propuesta.setPases(List.of());
            List<CrucePropuestoTorneo> cruces = new ArrayList<>(
                    construirRondaDirecta(clasificados,
                            nombreInstancia(total), 1));
            agregarRondasPrincipales(cruces, total, 2);
            propuesta.setCruces(cruces);
            propuesta.setExplicacion("Los " + total
                    + " clasificados completan directamente "
                    + nombreInstancia(total).toLowerCase()
                    + ". No se requieren pases ni fase previa.");
            return;
        }

        int cuadroPrincipal = mayorPotenciaDos(total);
        if (primeros.size() >= cuadroPrincipal) {
            invalidar(propuesta, "No existe una estructura automatica corta "
                    + "que mantenga a todos los primeros en la misma instancia. "
                    + "Se requiere configuracion manual.");
            return;
        }
        int lugaresDesdeAcceso = cuadroPrincipal - primeros.size();
        if (noPrimeros.size() < lugaresDesdeAcceso) {
            invalidar(propuesta, "No hay suficientes clasificados para "
                    + "completar el cuadro principal.");
            return;
        }

        propuesta.setPases(primeros);
        List<ClasificadoEtapaEliminatoria> ordenAcceso =
                noPrimeros.stream()
                        .sorted(prioridadAcceso())
                        .toList();
        PlanAcceso planAcceso = construirAcceso(
                ordenAcceso, lugaresDesdeAcceso);
        List<CrucePropuestoTorneo> cruces = new ArrayList<>(
                planAcceso.cruces());
        int rondasAcceso = planAcceso.rondas();
        if (rondasAcceso == 1) {
            normalizarRondaPreviaUnica(cruces, cuadroPrincipal);
        }
        List<String> clasificadosAcceso = rondasAcceso == 1
                ? normalizarReferenciasPreviaUnica(
                        planAcceso.clasificados(), cuadroPrincipal)
                : planAcceso.clasificados();
        cruces.addAll(construirCuadroPrincipalConPases(
                primeros, clasificadosAcceso,
                nombreInstancia(cuadroPrincipal), rondasAcceso + 1));
        agregarRondasPrincipales(cruces, cuadroPrincipal, rondasAcceso + 2);
        propuesta.setCruces(cruces);
        propuesta.setExplicacion("Los " + primeros.size()
                + " primeros reciben el mismo pase a "
                + nombreInstancia(cuadroPrincipal).toLowerCase()
                + ". Los restantes clasificados disputan " + rondasAcceso
                + (rondasAcceso == 1 ? " ronda" : " rondas")
                + " de acceso para completar " + lugaresDesdeAcceso
                + (lugaresDesdeAcceso == 1 ? " lugar." : " lugares."));
    }

    private PlanAcceso construirAcceso(
            List<ClasificadoEtapaEliminatoria> participantes,
            int objetivo) {
        List<String> actuales = participantes.stream()
                .map(ClasificadoEtapaEliminatoria::referencia)
                .collect(java.util.stream.Collectors.toCollection(
                        ArrayList::new));
        List<CrucePropuestoTorneo> cruces = new ArrayList<>();
        int ronda = 1;
        while (actuales.size() > objetivo) {
            int partidos = Math.min(actuales.size() - objetivo,
                    actuales.size() / 2);
            int cantidadPases = actuales.size() - partidos * 2;
            List<String> siguiente = new ArrayList<>();
            for (int i = 0; i < cantidadPases; i++) {
                siguiente.add(actuales.get(i));
            }
            List<String> juegan = new ArrayList<>(actuales.subList(
                    cantidadPases, actuales.size()));
            for (int i = 0; i < partidos; i++) {
                String a = juegan.remove(0);
                int posicionA = posicionDe(a);
                String b = juegan.stream()
                        .filter(x -> posicionDe(x) != posicionA)
                        .filter(x -> !grupoDe(x).equals(grupoDe(a)))
                        .findFirst()
                        .orElseGet(() -> juegan.stream()
                                .filter(x -> posicionDe(x) != posicionA)
                                .findFirst()
                                .orElseGet(() -> juegan.stream()
                                        .filter(x -> !grupoDe(x)
                                                .equals(grupoDe(a)))
                                        .findFirst()
                                        .orElse(juegan.get(0))));
                juegan.remove(b);
                int orden = i + 1;
                cruces.add(new CrucePropuestoTorneo("Acceso R" + ronda,
                        ronda, orden, a, b));
                siguiente.add("Ganador Acceso R" + ronda + " #" + orden);
            }
            actuales = siguiente;
            ronda++;
        }
        return new PlanAcceso(cruces, actuales, ronda - 1);
    }

    private String elegirRival(String origen, List<String> valores,
            int desde) {
        String grupo = grupoDe(origen);
        for (int i = desde; i < valores.size(); i++) {
            if (!grupo.equals(grupoDe(valores.get(i)))) return valores.get(i);
        }
        return valores.get(desde);
    }

    private List<CrucePropuestoTorneo> construirCuadroPrincipalConPases(
            List<ClasificadoEtapaEliminatoria> primeros,
            List<String> clasificados,
            String instancia,
            int ronda) {
        List<String> disponibles = new ArrayList<>(clasificados);
        List<ClasificadoEtapaEliminatoria> primerosPendientes =
                new ArrayList<>(primeros);
        List<CrucePropuestoTorneo> resultado = new ArrayList<>();
        int orden = 1;

        while (!primerosPendientes.isEmpty() && !disponibles.isEmpty()) {
            ClasificadoEtapaEliminatoria primero =
                    primerosPendientes.remove(0);
            String grupoPrimero = grupoDe(primero.referencia());
            String rival = disponibles.stream()
                    .filter(r -> posicionDe(r) == 2)
                    .filter(r -> !grupoDe(r).equals(grupoPrimero))
                    .findFirst()
                    .orElseGet(() -> disponibles.stream()
                            .filter(r -> posicionDe(r) > 1)
                            .filter(r -> !grupoDe(r).equals(grupoPrimero))
                            .findFirst()
                            .orElseGet(() -> disponibles.stream()
                                    .filter(r -> posicionDe(r) == 2)
                                    .findFirst()
                                    .orElse(disponibles.get(0))));
            disponibles.remove(rival);
            resultado.add(new CrucePropuestoTorneo(instancia, ronda,
                    orden++, primero.referencia(), rival));
        }

        for (ClasificadoEtapaEliminatoria primero : primerosPendientes) {
            disponibles.add(primero.referencia());
        }

        while (disponibles.size() >= 2) {
            String a = disponibles.remove(0);
            String b = disponibles.stream()
                    .filter(r -> !grupoDe(r).equals(grupoDe(a)))
                    .findFirst()
                    .orElseGet(() -> disponibles.get(0));
            disponibles.remove(b);
            resultado.add(new CrucePropuestoTorneo(instancia, ronda,
                    orden++, a, b));
        }
        if (!disponibles.isEmpty()) {
            throw new IllegalStateException(
                    "La ronda principal dejo un participante sin rival.");
        }
        return resultado;
    }

    private List<String> intercalarCabezasYClasificados(
            List<ClasificadoEtapaEliminatoria> primeros,
            List<String> clasificados) {
        List<String> resultado = new ArrayList<>();
        int cantidad = Math.max(primeros.size(), clasificados.size());
        for (int i = 0; i < cantidad; i++) {
            if (i < primeros.size()) {
                resultado.add(primeros.get(i).referencia());
            }
            if (i < clasificados.size()) {
                resultado.add(clasificados.get(i));
            }
        }
        return resultado;
    }

    private int posicionDe(String referencia) {
        if (referencia == null || referencia.isBlank()) return 99;
        int indice = referencia.indexOf('°');
        if (indice <= 0) return 99;
        try {
            return Integer.parseInt(referencia.substring(0, indice).trim());
        } catch (NumberFormatException exception) {
            return 99;
        }
    }

    private List<String> normalizarReferenciasPreviaUnica(
            List<String> referencias,
            int participantesCuadroPrincipal) {
        String instanciaDeportiva = nombreInstancia(
                participantesCuadroPrincipal * 2);
        return referencias.stream()
                .map(r -> r.replace("Ganador Acceso R1 #",
                        "Ganador " + instanciaDeportiva + " #"))
                .toList();
    }

    private void normalizarRondaPreviaUnica(
            List<CrucePropuestoTorneo> cruces,
            int participantesCuadroPrincipal) {
        String instanciaDeportiva = nombreInstancia(
                participantesCuadroPrincipal * 2);
        String referenciaAnterior = "Ganador Acceso R1 #";
        String referenciaNueva = "Ganador " + instanciaDeportiva + " #";
        for (CrucePropuestoTorneo cruce : cruces) {
            if ("Acceso R1".equals(cruce.getInstancia())) {
                cruce.setInstancia(instanciaDeportiva);
            }
            if (cruce.getParticipante1().startsWith(referenciaAnterior)) {
                cruce.setParticipante1(cruce.getParticipante1()
                        .replace(referenciaAnterior, referenciaNueva));
            }
            if (cruce.getParticipante2().startsWith(referenciaAnterior)) {
                cruce.setParticipante2(cruce.getParticipante2()
                        .replace(referenciaAnterior, referenciaNueva));
            }
        }
    }

    private List<CrucePropuestoTorneo> construirRondaDirecta(
            List<ClasificadoEtapaEliminatoria> valores, String instancia,
            int ronda) {
        List<ClasificadoEtapaEliminatoria> superiores = valores.stream()
                .filter(c -> c.posicionGrupo() == 1)
                .sorted(ranking())
                .collect(java.util.stream.Collectors.toCollection(
                        ArrayList::new));
        List<ClasificadoEtapaEliminatoria> inferiores = valores.stream()
                .filter(c -> c.posicionGrupo() > 1)
                .sorted(prioridadAcceso())
                .collect(java.util.stream.Collectors.toCollection(
                        ArrayList::new));
        List<CrucePropuestoTorneo> cruces = new ArrayList<>();
        int orden = 1;

        while (!superiores.isEmpty() && !inferiores.isEmpty()) {
            ClasificadoEtapaEliminatoria superior = superiores.remove(0);
            ClasificadoEtapaEliminatoria inferior = inferiores.stream()
                    .filter(c -> c.grupoId() != superior.grupoId())
                    .findFirst().orElse(inferiores.get(0));
            inferiores.remove(inferior);
            cruces.add(new CrucePropuestoTorneo(instancia, ronda,
                    orden++, superior.referencia(), inferior.referencia()));
        }

        List<ClasificadoEtapaEliminatoria> restantes = new ArrayList<>();
        restantes.addAll(superiores);
        restantes.addAll(inferiores);
        while (!restantes.isEmpty()) {
            ClasificadoEtapaEliminatoria a = restantes.remove(0);
            ClasificadoEtapaEliminatoria b = restantes.stream()
                    .filter(c -> c.grupoId() != a.grupoId())
                    .findFirst().orElse(restantes.get(0));
            restantes.remove(b);
            cruces.add(new CrucePropuestoTorneo(instancia, ronda,
                    orden++, a.referencia(), b.referencia()));
        }
        return cruces;
    }

    private List<CrucePropuestoTorneo> construirRondaReferencias(
            List<String> valores, String instancia, int ronda) {
        List<String> disponibles = new ArrayList<>(valores);
        List<CrucePropuestoTorneo> cruces = new ArrayList<>();
        int orden = 1;
        while (!disponibles.isEmpty()) {
            String a = disponibles.remove(0);
            String b = disponibles.stream()
                    .filter(x -> !grupoDe(x).equals(grupoDe(a)))
                    .findFirst().orElse(disponibles.get(0));
            disponibles.remove(b);
            cruces.add(new CrucePropuestoTorneo(instancia, ronda,
                    orden++, a, b));
        }
        return cruces;
    }

    private void agregarRondasPrincipales(List<CrucePropuestoTorneo> cruces,
            int participantes, int rondaInicial) {
        int partidos = participantes / 2;
        String instanciaAnterior = nombreInstancia(participantes);
        int ronda = rondaInicial;
        while (partidos > 1) {
            int siguientes = partidos / 2;
            String instancia = nombreInstancia(partidos);
            for (int i = 1; i <= siguientes; i++) {
                cruces.add(new CrucePropuestoTorneo(instancia, ronda, i,
                        "Ganador " + instanciaAnterior + " #" + (i * 2 - 1),
                        "Ganador " + instanciaAnterior + " #" + (i * 2)));
            }
            instanciaAnterior = instancia;
            partidos = siguientes;
            ronda++;
        }
    }

    private void validar(PropuestaEtapaEliminatoria propuesta) {
        if (propuesta.getError() != null) return;
        Set<Long> ids = new HashSet<>();
        for (ClasificadoEtapaEliminatoria c : propuesta.getClasificados()) {
            if (!ids.add(c.inscripcionId())) {
                invalidar(propuesta, "Hay clasificados repetidos."); return;
            }
        }
        List<ClasificadoEtapaEliminatoria> primeros = porPosicion(
                propuesta.getClasificados(), 1);
        boolean directa = esPotenciaDos(propuesta.getClasificados().size());
        if (!directa && (!propuesta.getPases().containsAll(primeros)
                || propuesta.getPases().size() != primeros.size())) {
            invalidar(propuesta,
                    "Todos los primeros deben recibir el mismo tratamiento.");
            return;
        }
        boolean primerosJuntos = propuesta.getCruces().stream()
                .anyMatch(c -> posicionDe(c.getParticipante1()) == 1
                        && posicionDe(c.getParticipante2()) == 1);
        if (primerosJuntos) {
            invalidar(propuesta,
                    "Dos primeros no pueden compartir un cruce inicial.");
            return;
        }
        propuesta.setValida(!propuesta.getCruces().isEmpty());
        propuesta.setError(propuesta.isValida() ? null
                : "La propuesta no contiene cruces.");
    }

    public void intercambiarRivalesAcceso(
            PropuestaEtapaEliminatoria propuesta) {
        List<CrucePropuestoTorneo> cruces = propuesta.getCruces();
        List<CrucePropuestoTorneo> acceso = cruces.stream()
                .filter(c -> c.getInstancia().startsWith("Acceso R1"))
                .toList();
        if (acceso.size() < 2) return;
        String rival = acceso.get(0).getParticipante2();
        acceso.get(0).setParticipante2(acceso.get(1).getParticipante2());
        acceso.get(1).setParticipante2(rival);
        propuesta.setCruces(cruces);
        validar(propuesta);
    }

    public void rotarPrimeros(PropuestaEtapaEliminatoria propuesta) {
        List<CrucePropuestoTorneo> principales = propuesta.getCruces().stream()
                .filter(c -> !c.getInstancia().startsWith("Acceso"))
                .filter(c -> c.getParticipante1().startsWith("1°")
                        || c.getParticipante2().startsWith("1°"))
                .toList();
        if (principales.size() < 2) return;
        String a = principales.get(0).getParticipante1();
        String b = principales.get(1).getParticipante1();
        principales.get(0).setParticipante1(b);
        principales.get(1).setParticipante1(a);
        validar(propuesta);
    }

    private List<ClasificadoEtapaEliminatoria> porPosicion(
            List<ClasificadoEtapaEliminatoria> valores, int posicion) {
        return valores.stream().filter(c -> c.posicionGrupo() == posicion)
                .sorted(Comparator.comparing(
                        ClasificadoEtapaEliminatoria::nombreGrupo)).toList();
    }

    private Comparator<ClasificadoEtapaEliminatoria> ranking() {
        return Comparator.comparingInt(
                ClasificadoEtapaEliminatoria::partidosGanados).reversed()
                .thenComparing(Comparator.comparingInt(
                        ClasificadoEtapaEliminatoria::diferenciaSets).reversed())
                .thenComparing(Comparator.comparingInt(
                        ClasificadoEtapaEliminatoria::diferenciaGames).reversed())
                .thenComparing(Comparator.comparingInt(
                        ClasificadoEtapaEliminatoria::setsGanados).reversed())
                .thenComparing(Comparator.comparingInt(
                        ClasificadoEtapaEliminatoria::gamesGanados).reversed())
                .thenComparingLong(
                        ClasificadoEtapaEliminatoria::inscripcionId);
    }

    private Comparator<ClasificadoEtapaEliminatoria> prioridadAcceso() {
        return Comparator.comparingInt(
                ClasificadoEtapaEliminatoria::posicionGrupo)
                .thenComparing(ranking());
    }

    private boolean esPotenciaDos(int valor) {
        return valor > 0 && (valor & (valor - 1)) == 0;
    }

    private int mayorPotenciaDos(int valor) {
        return Integer.highestOneBit(valor);
    }

    private String nombreInstancia(int participantes) {
        return switch (participantes) {
            case 2 -> "Final";
            case 4 -> "Semifinal";
            case 8 -> "Cuartos";
            case 16 -> "Octavos";
            case 32 -> "Dieciseisavos";
            default -> "Ronda de " + participantes;
        };
    }

    private String grupoDe(String referencia) {
        int indice = referencia.indexOf("Grupo ");
        return indice < 0 ? referencia : referencia.substring(indice);
    }

    public List<String> validarEdicionManual(
            PropuestaEtapaEliminatoria propuesta) {
        List<String> errores = new ArrayList<>();
        if (propuesta == null) {
            errores.add("La propuesta es obligatoria.");
            return errores;
        }
        Set<String> esperados = propuesta.getClasificados().stream()
                .map(ClasificadoEtapaEliminatoria::referencia)
                .collect(java.util.stream.Collectors.toCollection(
                        java.util.LinkedHashSet::new));
        List<String> usados = propuesta.getCruces().stream()
                .flatMap(c -> java.util.stream.Stream.of(
                        c.getParticipante1(), c.getParticipante2()))
                .filter(r -> r != null && !r.startsWith("Ganador "))
                .toList();
        for (String esperado : esperados) {
            long cantidad = usados.stream()
                    .filter(esperado::equals).count();
            if (cantidad == 0) errores.add("Falta " + esperado + ".");
            if (cantidad > 1) errores.add(esperado + " esta repetido.");
        }
        for (String usado : usados) {
            if (!esperados.contains(usado)) {
                errores.add("Participante no clasificado: " + usado + ".");
            }
        }
        for (CrucePropuestoTorneo cruce : propuesta.getCruces()) {
            if (cruce.getParticipante1().equals(cruce.getParticipante2())) {
                errores.add("Un partido no puede repetir participante.");
            }
        }
        validarJerarquiaDeportiva(propuesta, errores);
        if (errores.isEmpty()) {
            propuesta.setValida(true);
            propuesta.setError(null);
        } else {
            propuesta.setValida(false);
            propuesta.setError(String.join(" ", errores));
        }
        return errores;
    }

    private void validarJerarquiaDeportiva(
            PropuestaEtapaEliminatoria propuesta,
            List<String> errores) {
        Map<String, Integer> rondaIngreso = new java.util.HashMap<>();
        for (CrucePropuestoTorneo cruce : propuesta.getCruces()) {
            validarPosicionAsignada(cruce, cruce.getParticipante1(),
                    "Pareja 1", errores, rondaIngreso);
            validarPosicionAsignada(cruce, cruce.getParticipante2(),
                    "Pareja 2", errores, rondaIngreso);
        }
        if (!errores.isEmpty()) return;

        List<Integer> rondasPrimeros = propuesta.getClasificados().stream()
                .filter(c -> c.posicionGrupo() == 1)
                .map(c -> rondaIngreso.get(c.referencia()))
                .filter(java.util.Objects::nonNull)
                .distinct().toList();
        if (rondasPrimeros.size() > 1) {
            errores.add("Todos los primeros deben comenzar en la misma instancia.");
            return;
        }
        int rondaPrimeros = rondasPrimeros.isEmpty() ? 0
                : rondasPrimeros.get(0);
        for (ClasificadoEtapaEliminatoria c : propuesta.getClasificados()) {
            Integer ronda = rondaIngreso.get(c.referencia());
            if (ronda == null) continue;
            if (c.posicionGrupo() > 1 && ronda > rondaPrimeros) {
                errores.add(c.referencia()
                        + " no puede comenzar en una instancia posterior "
                        + "a los primeros de grupo.");
            }
        }
        int mejorRondaSegundos = propuesta.getClasificados().stream()
                .filter(c -> c.posicionGrupo() == 2)
                .map(c -> rondaIngreso.get(c.referencia()))
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue).max().orElse(0);
        for (ClasificadoEtapaEliminatoria c : propuesta.getClasificados()) {
            Integer ronda = rondaIngreso.get(c.referencia());
            if (c.posicionGrupo() == 3 && ronda != null
                    && ronda > mejorRondaSegundos) {
                errores.add(c.referencia()
                        + " no puede comenzar despues que los segundos.");
            }
        }
    }

    private void validarPosicionAsignada(CrucePropuestoTorneo cruce,
            String referencia, String lado, List<String> errores,
            Map<String, Integer> rondaIngreso) {
        if (referencia == null || referencia.isBlank()) {
            errores.add(cruce.getInstancia() + " #" + cruce.getOrden()
                    + " tiene " + lado + " sin asignar.");
            return;
        }
        if (!referencia.startsWith("Ganador ")) {
            rondaIngreso.put(referencia, cruce.getRonda());
        }
    }

    public List<String> advertenciasDeportivas(
            PropuestaEtapaEliminatoria propuesta) {
        List<String> advertencias = new ArrayList<>();
        if (propuesta == null) return advertencias;
        Map<String, ClasificadoEtapaEliminatoria> porReferencia =
                propuesta.getClasificados().stream().collect(
                    java.util.stream.Collectors.toMap(
                        ClasificadoEtapaEliminatoria::referencia, c -> c));
        for (CrucePropuestoTorneo cruce : propuesta.getCruces()) {
            ClasificadoEtapaEliminatoria a = porReferencia.get(
                    cruce.getParticipante1());
            ClasificadoEtapaEliminatoria b = porReferencia.get(
                    cruce.getParticipante2());
            if (a == null || b == null) continue;
            if (a.grupoId() == b.grupoId()) {
                advertencias.add(cruce.getInstancia() + " #"
                        + cruce.getOrden()
                        + " enfrenta parejas del mismo grupo.");
            }
            if (a.posicionGrupo() == 1 && b.posicionGrupo() == 1
                    && existeCruceSuperiorInferiorPosible(
                            propuesta, cruce.getRonda())) {
                advertencias.add(cruce.getInstancia() + " #"
                        + cruce.getOrden()
                        + " enfrenta dos primeros aunque existe una "
                        + "distribucion 1° contra 2°/3°.");
            }
        }
        return advertencias;
    }

    private boolean existeCruceSuperiorInferiorPosible(
            PropuestaEtapaEliminatoria propuesta, int ronda) {
        boolean hayPrimero = false;
        boolean hayInferior = false;
        Map<String, Integer> posiciones = propuesta.getClasificados().stream()
                .collect(java.util.stream.Collectors.toMap(
                        ClasificadoEtapaEliminatoria::referencia,
                        ClasificadoEtapaEliminatoria::posicionGrupo));
        for (CrucePropuestoTorneo cruce : propuesta.getCruces()) {
            if (cruce.getRonda() != ronda) continue;
            for (String referencia : List.of(cruce.getParticipante1(),
                    cruce.getParticipante2())) {
                Integer posicion = posiciones.get(referencia);
                if (posicion == null) continue;
                hayPrimero |= posicion == 1;
                hayInferior |= posicion > 1;
            }
        }
        return hayPrimero && hayInferior;
    }

    public void restaurarPropuesta(PropuestaEtapaEliminatoria destino,
            PropuestaEtapaEliminatoria origen) {
        destino.setClasificados(origen.getClasificados());
        destino.setPases(origen.getPases());
        List<CrucePropuestoTorneo> cruces = new ArrayList<>();
        for (CrucePropuestoTorneo c : origen.getCruces()) {
            cruces.add(new CrucePropuestoTorneo(c.getInstancia(),
                    c.getRonda(), c.getOrden(), c.getParticipante1(),
                    c.getParticipante2()));
        }
        destino.setCruces(cruces);
        destino.setExplicacion(origen.getExplicacion());
        destino.setValida(origen.isValida());
        destino.setError(origen.getError());
    }

    private void invalidar(PropuestaEtapaEliminatoria propuesta,
            String error) {
        propuesta.setValida(false);
        propuesta.setError(error);
        propuesta.setExplicacion("Generacion automatica de los cruces basada "
                + "en terminos deportivos preestablecidos, de no estar de "
                + "acuerdo configurarlo manualmente.");
    }
    private record PlanAcceso(
            List<CrucePropuestoTorneo> cruces,
            List<String> clasificados,
            int rondas) {
    }

}
