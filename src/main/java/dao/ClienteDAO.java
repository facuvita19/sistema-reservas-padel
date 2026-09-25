package dao;

import java.util.List;

import negocio.Cliente;

public interface ClienteDAO {

    void guardar(Cliente cliente);

    void eliminar(long id);

    Cliente buscar(long id);

    List<Cliente> listar();

    boolean existeDocumento(
            String documento,
            long clienteExcluidoId);

    boolean existeEmail(
            String email,
            long clienteExcluidoId);
}
