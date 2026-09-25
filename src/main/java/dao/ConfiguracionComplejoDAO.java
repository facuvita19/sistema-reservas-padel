package dao;

import negocio.ConfiguracionComplejo;

public interface ConfiguracionComplejoDAO {

    ConfiguracionComplejo obtener();

    void guardar(ConfiguracionComplejo configuracion);
}
