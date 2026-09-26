USE padel_reservas;

ALTER TABLE usuarios
    MODIFY COLUMN rol ENUM(
        'ADMINISTRADOR',
        'OPERADOR',
        'CLIENTE'
    ) NOT NULL DEFAULT 'CLIENTE';

CREATE INDEX idx_usuarios_activo_rol
    ON usuarios (activo, rol);
