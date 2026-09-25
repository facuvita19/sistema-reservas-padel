package dao;

import java.util.List;

import negocio.Usuario;

public interface UsuarioDAO {

    void guardar(Usuario usuario);

    void eliminar(long id);

    Usuario buscar(long id);

    Usuario buscarPorNombreUsuario(String nombreUsuario);

    List<Usuario> listar();

    boolean existeNombreUsuario(
            String nombreUsuario,
            long usuarioExcluidoId);
}
