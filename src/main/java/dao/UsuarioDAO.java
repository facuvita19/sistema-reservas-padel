package dao;

import java.util.List;

import negocio.Usuario;

public interface UsuarioDAO {

    void guardar(Usuario usuario);

    void eliminar(long id);

    default void activar(long id) {
        Usuario usuario = buscar(id);
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no existe.");
        }
        usuario.setActivo(true);
        guardar(usuario);
    }

    Usuario buscar(long id);

    Usuario buscarPorNombreUsuario(String nombreUsuario);

    List<Usuario> listar();

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
