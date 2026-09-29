package dao;

import java.sql.Connection;
import java.time.LocalDateTime;

import negocio.SesionCliente;

public interface SesionClienteDAO {

    void guardar(SesionCliente sesion);

    void guardar(Connection conexion, SesionCliente sesion);

    SesionCliente buscarVigentePorTokenHash(
            String tokenHash,
            LocalDateTime momento);

    void actualizarUltimoUso(long id, LocalDateTime momento);

    void revocarPorTokenHash(String tokenHash);

    void revocarPorUsuario(long usuarioId);

    int eliminarVencidas(LocalDateTime momento);
}
