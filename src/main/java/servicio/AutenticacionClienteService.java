package servicio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.regex.Pattern;

import config.ConexionBD;
import dao.ClienteDAO;
import dao.ClienteDAOMySQL;
import negocio.Cliente;
import negocio.RolUsuario;
import negocio.SesionCliente;
import negocio.Usuario;
import servicio.SesionClienteService.SesionCreada;

public class AutenticacionClienteService {

    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern DOCUMENTO =
            Pattern.compile("^[0-9]{7,10}$");
    private static final Pattern TELEFONO =
            Pattern.compile("^[0-9+()\\-\\s]{6,30}$");

    private final ClienteDAO clienteDAO;
    private final UsuarioService usuarioService;
    private final SesionClienteService sesionService;

    public AutenticacionClienteService() {
        this(new ClienteDAOMySQL(),
                new UsuarioService(),
                new SesionClienteService());
    }

    public AutenticacionClienteService(
            ClienteDAO clienteDAO,
            UsuarioService usuarioService,
            SesionClienteService sesionService) {
        if (clienteDAO == null
                || usuarioService == null
                || sesionService == null) {
            throw new IllegalArgumentException(
                    "Las dependencias de autenticacion no pueden ser nulas.");
        }
        this.clienteDAO = clienteDAO;
        this.usuarioService = usuarioService;
        this.sesionService = sesionService;
    }

    public ResultadoAutenticacion registrar(RegistroCliente entrada) {
        DatosNormalizados datos = validarYNormalizar(entrada);

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                Cliente cliente = resolverCliente(conexion, datos);
                Usuario usuario = usuarioService.registrarCliente(
                        conexion,
                        datos.email(),
                        entrada.password(),
                        cliente.getId());
                SesionCreada sesion = sesionService.crear(
                        conexion,
                        usuario.getId());

                conexion.commit();
                return crearResultado(cliente, usuario, sesion);
            } catch (RuntimeException | SQLException exception) {
                rollbackSeguro(conexion, exception);
                throw exception;
            } finally {
                restaurarAutoCommit(conexion);
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "No se pudo registrar la cuenta del cliente.",
                    exception);
        }
    }

    public ResultadoAutenticacion iniciarSesion(
            String email,
            String password) {
        String correo = normalizarEmail(email);
        Usuario usuario = usuarioService.autenticarCliente(
                correo, password);
        Cliente cliente = obtenerClienteActivo(usuario.getClienteId());
        SesionCreada sesion = sesionService.crear(usuario.getId());
        return crearResultado(cliente, usuario, sesion);
    }

    public SesionAutenticada obtenerSesion(String token) {
        SesionCliente sesion = sesionService.validar(token);
        if (sesion == null) return null;

        Usuario usuario = usuarioService.buscar(sesion.getUsuarioId());
        if (usuario == null
                || !usuario.isActivo()
                || usuario.getRol() != RolUsuario.CLIENTE
                || !usuario.estaVinculadoACliente()) {
            sesionService.revocar(token);
            return null;
        }

        Cliente cliente = clienteDAO.buscar(usuario.getClienteId());
        if (cliente == null || !cliente.isActivo()) {
            sesionService.revocar(token);
            return null;
        }

        return new SesionAutenticada(sesion, usuario, cliente);
    }

    public void cerrarSesion(String token) {
        sesionService.revocar(token);
    }

    private Cliente resolverCliente(
            Connection conexion,
            DatosNormalizados datos) {
        Cliente porDocumento = clienteDAO.buscarPorDocumento(
                conexion, datos.documento());
        Cliente porEmail = clienteDAO.buscarPorEmail(
                conexion, datos.email());

        if (porDocumento != null
                && porEmail != null
                && porDocumento.getId() != porEmail.getId()) {
            throw new ConflictoIdentidadException();
        }

        Cliente cliente = porDocumento != null
                ? porDocumento
                : porEmail;

        if (cliente == null) {
            cliente = new Cliente();
            cliente.setActivo(true);
        } else {
            if (!cliente.isActivo()) {
                throw new ClienteInactivoException();
            }
            if (!datos.documento().equals(cliente.getDocumento())) {
                throw new ConflictoIdentidadException();
            }
            if (cliente.getEmail() != null
                    && !cliente.getEmail().isBlank()
                    && !datos.email().equalsIgnoreCase(cliente.getEmail())) {
                throw new ConflictoIdentidadException();
            }
            if (usuarioService.buscarPorClienteId(cliente.getId()) != null) {
                throw new CuentaExistenteException();
            }
        }

        cliente.setNombre(datos.nombre());
        cliente.setApellido(datos.apellido());
        cliente.setDocumento(datos.documento());
        cliente.setTelefono(datos.telefono());
        cliente.setEmail(datos.email());
        clienteDAO.guardar(conexion, cliente);
        return cliente;
    }

    private Cliente obtenerClienteActivo(Long clienteId) {
        if (clienteId == null || clienteId <= 0) {
            throw new UsuarioService.CredencialesInvalidasException();
        }
        Cliente cliente = clienteDAO.buscar(clienteId);
        if (cliente == null || !cliente.isActivo()) {
            throw new UsuarioService.CredencialesInvalidasException();
        }
        return cliente;
    }

    private ResultadoAutenticacion crearResultado(
            Cliente cliente,
            Usuario usuario,
            SesionCreada sesion) {
        return new ResultadoAutenticacion(
                sesion.token(),
                sesion.sesion().getFechaVencimiento(),
                usuario.getId(),
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getEmail(),
                cliente.getTelefono());
    }

    private DatosNormalizados validarYNormalizar(
            RegistroCliente entrada) {
        if (entrada == null) {
            throw new IllegalArgumentException(
                    "Los datos de registro son obligatorios.");
        }

        String nombre = normalizarNombre(entrada.nombre());
        String apellido = normalizarNombre(entrada.apellido());
        String documento = soloDigitos(entrada.documento());
        String telefono = entrada.telefono() == null
                ? ""
                : entrada.telefono().trim();
        String email = normalizarEmail(entrada.email());

        if (nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre es obligatorio.");
        }
        if (apellido.isBlank()) {
            throw new IllegalArgumentException(
                    "El apellido es obligatorio.");
        }
        if (!DOCUMENTO.matcher(documento).matches()) {
            throw new IllegalArgumentException(
                    "El documento debe contener entre 7 y 10 numeros.");
        }
        if (!TELEFONO.matcher(telefono).matches()) {
            throw new IllegalArgumentException(
                    "El telefono contiene caracteres no validos.");
        }
        if (email == null || email.length() > 150
                || !EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException(
                    "El correo electronico no tiene un formato valido.");
        }
        if (entrada.password() == null
                || entrada.password().length() < 8) {
            throw new IllegalArgumentException(
                    "La contrasena debe tener al menos 8 caracteres.");
        }

        return new DatosNormalizados(
                nombre, apellido, documento, telefono, email);
    }

    private String normalizarNombre(String valor) {
        if (valor == null || valor.isBlank()) return "";
        String[] palabras = valor.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .split(" ");
        StringBuilder resultado = new StringBuilder();
        for (String palabra : palabras) {
            if (palabra.isBlank()) continue;
            if (resultado.length() > 0) resultado.append(' ');
            resultado.append(Character.toUpperCase(palabra.charAt(0)))
                    .append(palabra.substring(1));
        }
        return resultado.toString();
    }

    private String soloDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    private String normalizarEmail(String valor) {
        return valor == null || valor.isBlank()
                ? null
                : valor.trim().toLowerCase(Locale.ROOT);
    }

    private void rollbackSeguro(
            Connection conexion,
            Exception original) {
        try {
            conexion.rollback();
        } catch (SQLException rollback) {
            original.addSuppressed(rollback);
        }
    }

    private void restaurarAutoCommit(Connection conexion) {
        try {
            conexion.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }

    private record DatosNormalizados(
            String nombre,
            String apellido,
            String documento,
            String telefono,
            String email) {
    }

    public record RegistroCliente(
            String nombre,
            String apellido,
            String documento,
            String telefono,
            String email,
            String password) {
    }

    public record ResultadoAutenticacion(
            String token,
            java.time.LocalDateTime vencimiento,
            long usuarioId,
            long clienteId,
            String nombre,
            String apellido,
            String email,
            String telefono) {
    }

    public record SesionAutenticada(
            SesionCliente sesion,
            Usuario usuario,
            Cliente cliente) {
    }

    public static class CuentaExistenteException
            extends IllegalArgumentException {
        public CuentaExistenteException() {
            super("Ya existe una cuenta asociada. "
                    + "Inicia sesion o recupera tu contrasena.");
        }
    }

    public static class ConflictoIdentidadException
            extends IllegalArgumentException {
        public ConflictoIdentidadException() {
            super("Los datos ingresados no coinciden "
                    + "con una unica ficha de cliente.");
        }
    }

    public static class ClienteInactivoException
            extends IllegalArgumentException {
        public ClienteInactivoException() {
            super("La ficha del cliente esta inactiva. "
                    + "Comunicate con el complejo.");
        }
    }
}
