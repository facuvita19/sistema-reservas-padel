USE padel_reservas;

CREATE TABLE IF NOT EXISTS recuperaciones_password_cliente (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    token_hash CHAR(64) NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_vencimiento DATETIME NOT NULL,
    fecha_uso DATETIME NULL,
    revocada BOOLEAN NOT NULL DEFAULT FALSE,

    PRIMARY KEY (id),
    CONSTRAINT uq_recuperaciones_password_token UNIQUE (token_hash),
    CONSTRAINT fk_recuperaciones_password_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    INDEX idx_recuperaciones_password_usuario (
        usuario_id,
        revocada,
        fecha_vencimiento
    ),
    INDEX idx_recuperaciones_password_vencimiento (
        fecha_vencimiento,
        revocada
    )
) ENGINE = InnoDB;
