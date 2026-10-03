package dao;

import java.sql.Connection;
import java.util.List;

import negocio.Usuario;

public interface UsuarioDAO {

    void guardar(Usuario usuario);

    void guardar(Connection conexion, Usuario usuario);

    void eliminar(long id);

    default void activar(long id) {
        Usuario usuario = buscar(id);
        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario no existe.");
        }
        usuario.setActivo(true);
        guardar(usuario);
    }

    Usuario buscar(long id);

    Usuario buscarPorNombreUsuario(String nombreUsuario);

    Usuario buscarPorNombreUsuarioIncluyendoInactivos(
            String nombreUsuario);

    Usuario buscarPorNombreUsuario(
            Connection conexion,
            String nombreUsuario,
            boolean soloActivos);

    Usuario buscarPorClienteId(long clienteId);

    Usuario buscarPorClienteId(
            Connection conexion,
            long clienteId);

    List<Usuario> listar();

    default List<Usuario> listarPersonal() {
        return listar().stream()
                .filter(Usuario::esPersonalDelComplejo)
                .toList();
    }

    boolean existeNombreUsuario(
            String nombreUsuario,
            long usuarioExcluidoId);

    default long contarAdministradoresActivos() {
        return listar().stream()
                .filter(Usuario::isActivo)
                .filter(Usuario::esAdministrador)
                .count();
    }
}
