package dao;

import java.sql.Connection;
import java.util.List;

import negocio.Cliente;

public interface ClienteDAO {

    void guardar(Cliente cliente);

    void guardar(Connection conexion, Cliente cliente);

    void eliminar(long id);

    Cliente buscar(long id);
    Cliente buscar(Connection conexion, long id);

    Cliente buscarPorDocumento(String documento);

    Cliente buscarPorDocumento(Connection conexion, String documento);

    Cliente buscarPorEmail(String email);

    Cliente buscarPorEmail(Connection conexion, String email);

    // buscarActivosPorTelefonoNormalizadoV1
    List<Cliente> buscarActivosPorTelefonoNormalizado(
            Connection conexion,
            String telefonoNormalizado);

    List<Cliente> listar();

    boolean existeDocumento(
            String documento,
            long clienteExcluidoId);

    boolean existeEmail(
            String email,
            long clienteExcluidoId);
}
