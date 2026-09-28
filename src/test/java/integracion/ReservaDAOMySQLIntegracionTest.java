package integracion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import config.ConexionBD;
import dao.ReservaDAOMySQL;

class ReservaDAOMySQLIntegracionTest {

    private final List<Long> reservasCreadas = new ArrayList<>();
    private final List<Long> clientesCreados = new ArrayList<>();
    private long canchaId;
    private long usuarioId;
    private LocalDate fechaPrueba;

    @BeforeEach
    void preparar() throws SQLException {
        validarBaseExclusivaDePruebas();
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            canchaId = primerId(conexion,
                    "SELECT id FROM canchas WHERE activo = TRUE ORDER BY id LIMIT 1",
                    "La base de pruebas necesita al menos una cancha activa.");
            usuarioId = primerId(conexion,
                    "SELECT id FROM usuarios ORDER BY id LIMIT 1",
                    "La base de pruebas necesita al menos un usuario.");
        }
        fechaPrueba = LocalDate.now().plusYears(2);
    }

    @AfterEach
    void limpiar() throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                for (Long reservaId : reservasCreadas) {
                    eliminarSiExiste(conexion,
                            "DELETE FROM pagos WHERE reserva_id = ?", reservaId);
                    eliminarSiExiste(conexion,
                            "DELETE FROM solicitudes_web_seguimiento WHERE reserva_id = ?",
                            reservaId);
                    eliminarSiExiste(conexion,
                            "DELETE FROM auditoria_reservas WHERE reserva_id = ?",
                            reservaId);
                    eliminarSiExiste(conexion,
                            "DELETE FROM historial_reprogramaciones WHERE reserva_id = ?",
                            reservaId);
                    eliminarSiExiste(conexion,
                            "DELETE FROM reservas WHERE id = ?", reservaId);
                }
                for (Long clienteId : clientesCreados) {
                    eliminarSiExiste(conexion,
                            "DELETE FROM clientes WHERE id = ?", clienteId);
                }
                conexion.commit();
            } catch (SQLException exception) {
                conexion.rollback();
                throw exception;
            }
        }
    }

    @Test
    void expiracionRespetaOrigenPagoEIdempotencia() throws SQLException {
        ReservaDAOMySQL dao = new ReservaDAOMySQL();
        LocalDateTime vencida = LocalDateTime.now().minusMinutes(5);

        long webSinPago = insertarReserva("WEB", vencida,
                LocalTime.of(8, 0), "PENDIENTE");
        long personalSinPago = insertarReserva("PERSONAL", vencida,
                LocalTime.of(10, 0), "PENDIENTE");
        long webConPago = insertarReserva("WEB", vencida,
                LocalTime.of(12, 0), "PENDIENTE");
        insertarPagoAcreditado(webConPago, new BigDecimal("1000.00"));

        int primeraEjecucion = dao.expirarPendientesVencidas(
                LocalDateTime.now());
        int segundaEjecucion = dao.expirarPendientesVencidas(
                LocalDateTime.now());

        assertEquals(1, primeraEjecucion,
                "Sólo debe expirar la reserva WEB vencida sin pago.");
        assertEquals(0, segundaEjecucion,
                "La segunda ejecución debe ser idempotente.");
        assertEquals("EXPIRADA", estado(webSinPago));
        assertEquals("PENDIENTE", estado(personalSinPago));
        assertEquals("PENDIENTE", estado(webConPago));
        assertNotNull(fechaExpiracion(webSinPago));
        assertTrue(fechaVencimientoEsNula(webSinPago));
    }

    @Test
    void unaReservaExpiradaLiberaElHorario() throws SQLException {
        ReservaDAOMySQL dao = new ReservaDAOMySQL();
        LocalTime inicio = LocalTime.of(14, 0);
        LocalTime fin = LocalTime.of(15, 30);
        long reservaId = insertarReserva("WEB",
                LocalDateTime.now().minusMinutes(2), inicio, "PENDIENTE");

        assertTrue(dao.horarioOcupado(
                canchaId, fechaPrueba, inicio, fin, 0L));

        assertEquals(1, dao.expirarPendientesVencidas(LocalDateTime.now()));
        assertEquals("EXPIRADA", estado(reservaId));
        assertFalse(dao.horarioOcupado(
                canchaId, fechaPrueba, inicio, fin, 0L));
    }

    private void validarBaseExclusivaDePruebas() throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             Statement st = conexion.createStatement();
             ResultSet rs = st.executeQuery("SELECT DATABASE()")) {
            rs.next();
            String base = rs.getString(1);
            assertEquals("padel_reservas_test", base,
                    "Las pruebas se negaron a ejecutarse fuera de padel_reservas_test.");
        }
    }

    private long insertarReserva(String origen, LocalDateTime vencimiento,
            LocalTime inicio, String estado) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            long clienteId = insertarCliente(conexion);
            LocalTime fin = inicio.plusMinutes(90);
            String sql = "INSERT INTO reservas "
                    + "(cliente_id, cancha_id, usuario_id, origen, fecha, "
                    + "hora_inicio, hora_fin, estado, fecha_vencimiento, "
                    + "fecha_expiracion, cantidad_jugadores, comentarios, "
                    + "observaciones_administrativas, precio_total) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, 4, ?, NULL, ?)";
            try (PreparedStatement st = conexion.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS)) {
                st.setLong(1, clienteId);
                st.setLong(2, canchaId);
                st.setLong(3, usuarioId);
                st.setString(4, origen);
                st.setDate(5, Date.valueOf(fechaPrueba));
                st.setTime(6, Time.valueOf(inicio));
                st.setTime(7, Time.valueOf(fin));
                st.setString(8, estado);
                st.setTimestamp(9, Timestamp.valueOf(vencimiento));
                st.setString(10, "TEST_INTEGRACION_" + UUID.randomUUID());
                st.setBigDecimal(11, new BigDecimal("10000.00"));
                st.executeUpdate();
                try (ResultSet claves = st.getGeneratedKeys()) {
                    claves.next();
                    long id = claves.getLong(1);
                    reservasCreadas.add(id);
                    return id;
                }
            }
        }
    }

    private long insertarCliente(Connection conexion) throws SQLException {
        String token = UUID.randomUUID().toString().replace("-", "");
        String documento = String.valueOf(
                7000000 + Math.abs(token.hashCode() % 2000000));
        String sql = "INSERT INTO clientes "
                + "(nombre, apellido, documento, telefono, email, activo) "
                + "VALUES ('Test', 'Integracion', ?, '1100000000', ?, TRUE)";
        try (PreparedStatement st = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, documento);
            st.setString(2, "test-" + token + "@example.invalid");
            st.executeUpdate();
            try (ResultSet claves = st.getGeneratedKeys()) {
                claves.next();
                long id = claves.getLong(1);
                clientesCreados.add(id);
                return id;
            }
        }
    }

    private void insertarPagoAcreditado(long reservaId, BigDecimal importe)
            throws SQLException {
        String sql = "INSERT INTO pagos "
                + "(reserva_id, importe, metodo_pago, estado, referencia, fecha_pago) "
                + "VALUES (?, ?, 'TRANSFERENCIA', 'ACREDITADO', ?, CURRENT_TIMESTAMP)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement st = conexion.prepareStatement(sql)) {
            st.setLong(1, reservaId);
            st.setBigDecimal(2, importe);
            st.setString(3, "TEST-" + UUID.randomUUID());
            st.executeUpdate();
        }
    }

    private String estado(long reservaId) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement st = conexion.prepareStatement(
                     "SELECT estado FROM reservas WHERE id = ?")) {
            st.setLong(1, reservaId);
            try (ResultSet rs = st.executeQuery()) {
                assertTrue(rs.next());
                return rs.getString(1);
            }
        }
    }

    private LocalDateTime fechaExpiracion(long reservaId) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement st = conexion.prepareStatement(
                     "SELECT fecha_expiracion FROM reservas WHERE id = ?")) {
            st.setLong(1, reservaId);
            try (ResultSet rs = st.executeQuery()) {
                assertTrue(rs.next());
                Timestamp value = rs.getTimestamp(1);
                return value == null ? null : value.toLocalDateTime();
            }
        }
    }

    private boolean fechaVencimientoEsNula(long reservaId) throws SQLException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement st = conexion.prepareStatement(
                     "SELECT fecha_vencimiento FROM reservas WHERE id = ?")) {
            st.setLong(1, reservaId);
            try (ResultSet rs = st.executeQuery()) {
                assertTrue(rs.next());
                return rs.getTimestamp(1) == null;
            }
        }
    }

    private long primerId(Connection conexion, String sql, String mensaje)
            throws SQLException {
        try (PreparedStatement st = conexion.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            if (!rs.next()) {
                throw new IllegalStateException(mensaje);
            }
            return rs.getLong(1);
        }
    }

    private void eliminarSiExiste(Connection conexion, String sql, long id)
            throws SQLException {
        try (PreparedStatement st = conexion.prepareStatement(sql)) {
            st.setLong(1, id);
            st.executeUpdate();
        } catch (SQLException exception) {
            if (exception.getErrorCode() != 1146) {
                throw exception;
            }
        }
    }
}
