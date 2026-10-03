package servicio;

import java.util.List;
import java.util.regex.Pattern;

import dao.ClienteDAO;
import dao.ClienteDAOMySQL;
import negocio.Cliente;

public class ClienteService {

    private static final Pattern EMAIL_VALIDO = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DOCUMENTO_VALIDO =
            Pattern.compile("^[0-9]{7,10}$");

    private static final Pattern TELEFONO_VALIDO =
            Pattern.compile("^[0-9+()\\-\\s]{6,30}$");

    private final ClienteDAO clienteDAO;

    public ClienteService() {
        this(new ClienteDAOMySQL());
    }

    public ClienteService(ClienteDAO clienteDAO) {
        if (clienteDAO == null) {
            throw new IllegalArgumentException(
                    "El DAO de clientes no puede ser nulo."
            );
        }
        this.clienteDAO = clienteDAO;
    }

    public void guardar(Cliente cliente) {
        validarCliente(cliente);
        normalizarDatos(cliente);

        if (clienteDAO.existeDocumento(
                cliente.getDocumento(),
                cliente.getId())) {
            throw new IllegalArgumentException(
                    "Ya existe un cliente con ese documento."
            );
        }

        if (clienteDAO.existeEmail(
                cliente.getEmail(),
                cliente.getId())) {
            throw new IllegalArgumentException(
                    "Ya existe un cliente con ese correo electronico."
            );
        }

        clienteDAO.guardar(cliente);
    }

    public void eliminar(long id) {
        validarId(id, "cliente");
        clienteDAO.eliminar(id);
    }

    public void reactivar(long id) {
        validarId(id, "cliente");
        clienteDAO.reactivar(id);
    }

    public List<Cliente> listarTodos() {
        return clienteDAO.listarTodos();
    }

    public void eliminarDefinitivamente(long id) {
        validarId(id, "cliente");
        Cliente cliente = clienteDAO.buscar(id);
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no existe.");
        }
        if (cliente.isActivo()) {
            throw new IllegalArgumentException(
                    "Primero desactivá el cliente antes de eliminarlo definitivamente.");
        }
        clienteDAO.eliminarDefinitivamente(id);
    }

    public Cliente buscar(long id) {
        return id <= 0 ? null : clienteDAO.buscar(id);
    }

    public List<Cliente> listar() {
        return clienteDAO.listar();
    }

    private void validarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException(
                    "El cliente no puede ser nulo."
            );
        }

        validarTexto(cliente.getNombre(), "El nombre es obligatorio.");
        validarTexto(cliente.getApellido(), "El apellido es obligatorio.");
        validarTexto(
                cliente.getDocumento(),
                "El documento es obligatorio."
        );
        validarTexto(
                cliente.getTelefono(),
                "El telefono es obligatorio."
        );

        String documento = soloDigitos(cliente.getDocumento());
        if (!DOCUMENTO_VALIDO.matcher(documento).matches()) {
            throw new IllegalArgumentException(
                    "El documento debe contener entre 7 y 10 numeros."
            );
        }

        String telefono = cliente.getTelefono().trim();
        if (!TELEFONO_VALIDO.matcher(telefono).matches()) {
            throw new IllegalArgumentException(
                    "El telefono contiene caracteres no validos."
            );
        }

        String email = cliente.getEmail();
        if (email != null && !email.isBlank()
                && !EMAIL_VALIDO.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException(
                    "El correo electronico no tiene un formato valido."
            );
        }
    }

    private void normalizarDatos(Cliente cliente) {
        cliente.setNombre(normalizarNombre(cliente.getNombre()));
        cliente.setApellido(normalizarNombre(cliente.getApellido()));
        cliente.setDocumento(soloDigitos(cliente.getDocumento()));
        cliente.setTelefono(cliente.getTelefono().trim());

        if (cliente.getEmail() == null
                || cliente.getEmail().isBlank()) {
            cliente.setEmail(null);
        } else {
            cliente.setEmail(
                    cliente.getEmail().trim().toLowerCase()
            );
        }
    }

    private String normalizarNombre(String valor) {
        String limpio = valor.trim().replaceAll("\\s+", " ");
        String[] palabras = limpio.toLowerCase().split(" ");
        StringBuilder resultado = new StringBuilder();

        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                if (resultado.length() > 0) {
                    resultado.append(' ');
                }
                resultado.append(Character.toUpperCase(palabra.charAt(0)));
                resultado.append(palabra.substring(1));
            }
        }

        return resultado.toString();
    }

    private String soloDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    private void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private void validarId(long id, String entidad) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El ID del " + entidad + " no es valido."
            );
        }
    }
}
