package servicio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import config.ConexionBD;
import dao.ClienteDAO;
import dao.ClienteDAOMySQL;
import negocio.Cliente;
import negocio.TipoVinculacionTorneo;
import util.NormalizadorTelefono;

public class VinculacionClienteTelefonoService {

    private final ClienteDAO clienteDAO;

    public VinculacionClienteTelefonoService() {
        this(new ClienteDAOMySQL());
    }

    public VinculacionClienteTelefonoService(ClienteDAO clienteDAO) {
        if (clienteDAO == null) {
            throw new IllegalArgumentException(
                    "El DAO de clientes no puede ser nulo.");
        }
        this.clienteDAO = clienteDAO;
    }

    public ResultadoVinculacion buscar(String telefono) {
        String normalizado = NormalizadorTelefono.normalizar(telefono);
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            return buscar(conexion, normalizado);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo buscar el cliente por telefono.",
                    exception);
        }
    }

    public ResultadoVinculacion buscar(
            Connection conexion,
            String telefono) {
        if (conexion == null) {
            throw new IllegalArgumentException(
                    "La conexion no puede ser nula.");
        }
        String normalizado = NormalizadorTelefono.normalizar(telefono);
        List<Cliente> coincidencias =
                clienteDAO.buscarActivosPorTelefonoNormalizado(
                        conexion, normalizado);

        if (coincidencias.isEmpty()) {
            return ResultadoVinculacion.sinCoincidencias(normalizado);
        }
        if (coincidencias.size() == 1) {
            return ResultadoVinculacion.encontrado(
                    normalizado, coincidencias.getFirst());
        }
        return ResultadoVinculacion.ambigua(
                normalizado, coincidencias.size());
    }

    public enum EstadoVinculacion {
        ENCONTRADO,
        SIN_COINCIDENCIAS,
        AMBIGUA
    }

    public record ResultadoVinculacion(
            EstadoVinculacion estado,
            String telefonoNormalizado,
            Cliente cliente,
            int cantidadCoincidencias) {

        public ResultadoVinculacion {
            if (estado == null) {
                throw new IllegalArgumentException(
                        "El estado de vinculacion es obligatorio.");
            }
            NormalizadorTelefono.normalizar(telefonoNormalizado);
            if (cantidadCoincidencias < 0) {
                throw new IllegalArgumentException(
                        "La cantidad de coincidencias no puede ser negativa.");
            }
            if (estado == EstadoVinculacion.ENCONTRADO
                    && (cliente == null || cantidadCoincidencias != 1)) {
                throw new IllegalArgumentException(
                        "Una vinculacion encontrada requiere un unico cliente.");
            }
            if (estado != EstadoVinculacion.ENCONTRADO
                    && cliente != null) {
                throw new IllegalArgumentException(
                        "Una vinculacion no resuelta no puede incluir un cliente.");
            }
        }

        public static ResultadoVinculacion encontrado(
                String telefono,
                Cliente cliente) {
            return new ResultadoVinculacion(
                    EstadoVinculacion.ENCONTRADO,
                    telefono,
                    cliente,
                    1);
        }

        public static ResultadoVinculacion sinCoincidencias(
                String telefono) {
            return new ResultadoVinculacion(
                    EstadoVinculacion.SIN_COINCIDENCIAS,
                    telefono,
                    null,
                    0);
        }

        public static ResultadoVinculacion ambigua(
                String telefono,
                int cantidad) {
            if (cantidad < 2) {
                throw new IllegalArgumentException(
                        "Una vinculacion ambigua requiere varias coincidencias.");
            }
            return new ResultadoVinculacion(
                    EstadoVinculacion.AMBIGUA,
                    telefono,
                    null,
                    cantidad);
        }

        public boolean estaVinculado() {
            return estado == EstadoVinculacion.ENCONTRADO;
        }

        public boolean requiereRevision() {
            return estado == EstadoVinculacion.AMBIGUA;
        }

        public Long clienteId() {
            return cliente == null ? null : cliente.getId();
        }

        public TipoVinculacionTorneo tipoVinculacion() {
            return estaVinculado()
                    ? TipoVinculacionTorneo.AUTOMATICA
                    : TipoVinculacionTorneo.SIN_VINCULAR;
        }
    }
}
