package servicio;

import java.sql.Connection;
import java.time.LocalDateTime;
import dao.PerfilJugadorDAO;
import dao.PerfilJugadorDAOMySQL;
import negocio.EstadoPerfilJugador;
import negocio.PerfilJugador;

public class ResolucionPerfilJugadorService {
    private final PerfilJugadorDAO perfiles;

    public ResolucionPerfilJugadorService() {
        this(new PerfilJugadorDAOMySQL());
    }

    public ResolucionPerfilJugadorService(PerfilJugadorDAO perfiles) {
        if (perfiles == null) throw new IllegalArgumentException("El DAO de perfiles es obligatorio.");
        this.perfiles = perfiles;
    }

    public PerfilJugador resolverParaCliente(Connection conexion, long clienteId, String nombre) {
        validarConexion(conexion);
        if (clienteId <= 0) throw new IllegalArgumentException("El cliente es invalido.");
        PerfilJugador existente = perfiles.buscarPorCliente(conexion, clienteId);
        if (existente != null) return existente;
        PerfilJugador perfil = nuevo(clienteId, EstadoPerfilJugador.DEFINITIVO, nombre);
        perfiles.guardar(conexion, perfil);
        return perfil;
    }

    public PerfilJugador crearSinFicha(Connection conexion, String nombre) {
        validarConexion(conexion);
        PerfilJugador perfil = nuevo(null, EstadoPerfilJugador.PROVISIONAL, nombre);
        perfiles.guardar(conexion, perfil);
        return perfil;
    }

    public PerfilJugador consolidarConCliente(Connection conexion, Long perfilId,
            long clienteId, String nombre, long usuarioId) {
        validarConexion(conexion);
        if (clienteId <= 0 || usuarioId <= 0) throw new IllegalArgumentException("Cliente y responsable son obligatorios.");
        PerfilJugador delCliente = perfiles.buscarPorCliente(conexion, clienteId);
        if (perfilId == null) return delCliente != null ? delCliente : resolverParaCliente(conexion, clienteId, nombre);
        PerfilJugador actual = perfiles.buscar(conexion, perfilId);
        if (actual == null) throw new IllegalArgumentException("El perfil deportivo del integrante no existe.");
        if (actual.getClienteId() != null && actual.getClienteId() == clienteId) return actual;
        if (delCliente != null && delCliente.getId() != actual.getId()) {
            throw new IllegalArgumentException("El integrante y el cliente seleccionado tienen perfiles deportivos distintos. La vinculacion se bloqueo para no mezclar categorias ni historial.");
        }
        if (actual.getEstado() != EstadoPerfilJugador.PROVISIONAL || actual.getClienteId() != null) {
            throw new IllegalArgumentException("El perfil deportivo actual no se puede reasignar a otro cliente.");
        }
        actual.setClienteId(clienteId);
        actual.setEstado(EstadoPerfilJugador.DEFINITIVO);
        actual.setNombrePublico(limpiar(nombre));
        actual.setVinculacionRevisada(true);
        actual.setUsuarioRevisionId(usuarioId);
        actual.setFechaRevision(LocalDateTime.now());
        perfiles.guardar(conexion, actual);
        return actual;
    }

    private PerfilJugador nuevo(Long cliente, EstadoPerfilJugador estado, String nombre) {
        PerfilJugador perfil = new PerfilJugador();
        perfil.setClienteId(cliente);
        perfil.setEstado(estado);
        perfil.setNombrePublico(limpiar(nombre));
        perfil.setActivo(true);
        return perfil;
    }

    private String limpiar(String valor) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("El nombre publico es obligatorio.");
        String limpio = valor.trim().replaceAll("\s+", " ");
        if (limpio.length() > 220) throw new IllegalArgumentException("El nombre publico no puede superar 220 caracteres.");
        return limpio;
    }

    private void validarConexion(Connection conexion) {
        if (conexion == null) throw new IllegalArgumentException("La conexion es obligatoria.");
    }
}
