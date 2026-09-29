USE padel_reservas;

-- Permite usar el correo electronico completo como nombre de acceso
-- para las cuentas con rol CLIENTE.
ALTER TABLE usuarios
    MODIFY COLUMN nombre_usuario VARCHAR(150) NOT NULL;

-- Las sesiones web guardan solamente el hash SHA-256 del token.
-- El token original se enviara al navegador mediante una cookie HttpOnly.
CREATE TABLE IF NOT EXISTS sesiones_cliente (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    token_hash CHAR(64)
        CHARACTER SET ascii
        COLLATE ascii_bin
        NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_vencimiento DATETIME NOT NULL,
    fecha_ultimo_uso DATETIME NULL,
    revocada BOOLEAN NOT NULL DEFAULT FALSE,

    PRIMARY KEY (id),
    CONSTRAINT uq_sesiones_cliente_token UNIQUE (token_hash),
    CONSTRAINT fk_sesiones_cliente_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT chk_sesiones_cliente_vencimiento
        CHECK (fecha_vencimiento > fecha_creacion),

    INDEX idx_sesiones_cliente_usuario_estado (
        usuario_id,
        revocada,
        fecha_vencimiento
    ),
    INDEX idx_sesiones_cliente_vencimiento (
        fecha_vencimiento,
        revocada
    )
) ENGINE = InnoDB;
