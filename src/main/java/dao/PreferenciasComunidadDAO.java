package dao;

import java.sql.Connection;
import negocio.PreferenciasComunidad;

public interface PreferenciasComunidadDAO {
    PreferenciasComunidad buscar(Connection c, long clienteId);
    void guardar(Connection c, PreferenciasComunidad p);
    boolean insertarInicial(Connection c, PreferenciasComunidad p);
    boolean actualizarSiVersion(Connection c, PreferenciasComunidad p, long versionEsperada);
}
