package dao;

import java.sql.Connection;
import java.time.LocalDateTime;

import negocio.RecuperacionPasswordCliente;

public interface RecuperacionPasswordClienteDAO {

    void guardar(RecuperacionPasswordCliente recuperacion);

    void guardar(
            Connection conexion,
            RecuperacionPasswordCliente recuperacion);

    RecuperacionPasswordCliente buscarVigentePorTokenHash(
            String tokenHash,
            LocalDateTime momento);

    RecuperacionPasswordCliente buscarVigentePorTokenHash(
            Connection conexion,
            String tokenHash,
            LocalDateTime momento);

    void marcarUsada(
            Connection conexion,
            long id,
            LocalDateTime fechaUso);

    int revocarPendientesPorUsuario(
            Connection conexion,
            long usuarioId);

    int eliminarVencidasOConsumidas(LocalDateTime momento);
}
