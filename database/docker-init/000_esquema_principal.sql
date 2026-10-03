-- ============================================================
-- Sistema de Reservas para Padel
-- Esquema inicial para MySQL
-- ============================================================

CREATE DATABASE IF NOT EXISTS padel_reservas
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE padel_reservas;

-- ------------------------------------------------------------
-- Clientes
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS clientes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    documento VARCHAR(20) NOT NULL,
    telefono VARCHAR(30) NOT NULL,
    email VARCHAR(150),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_clientes_documento UNIQUE (documento),
    CONSTRAINT uq_clientes_email UNIQUE (email)
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- Usuarios
-- cliente_id es NULL para administradores.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre_usuario VARCHAR(40) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol ENUM('ADMINISTRADOR', 'CLIENTE') NOT NULL DEFAULT 'CLIENTE',
    cliente_id BIGINT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_usuarios_nombre UNIQUE (nombre_usuario),
    CONSTRAINT uq_usuarios_cliente UNIQUE (cliente_id),
    CONSTRAINT fk_usuarios_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES clientes(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- Canchas de padel
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS canchas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    tipo ENUM('CUBIERTA', 'DESCUBIERTA') NOT NULL,
    superficie VARCHAR(100),
    tiene_iluminacion BOOLEAN NOT NULL DEFAULT TRUE,
    hora_apertura TIME NOT NULL,
    hora_cierre TIME NOT NULL,
    duracion_reserva INT NOT NULL DEFAULT 90,
    precio DECIMAL(12, 2) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    orden_visual INT NOT NULL DEFAULT 0,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_canchas_nombre UNIQUE (nombre),
    CONSTRAINT chk_canchas_horario CHECK (hora_cierre > hora_apertura),
    CONSTRAINT chk_canchas_duracion CHECK (
        duracion_reserva >= 30
        AND duracion_reserva <= 240
        AND MOD(duracion_reserva, 30) = 0
    ),
    CONSTRAINT chk_canchas_precio CHECK (precio >= 0)
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- Dias disponibles por cancha
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cancha_dias_disponibles (
    cancha_id BIGINT NOT NULL,
    dia_semana VARCHAR(10) NOT NULL,

    PRIMARY KEY (cancha_id, dia_semana),
    CONSTRAINT fk_dias_cancha
        FOREIGN KEY (cancha_id)
        REFERENCES canchas(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT chk_dias_cancha CHECK (
        dia_semana IN (
            'MONDAY', 'TUESDAY', 'WEDNESDAY',
            'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'
        )
    )
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- Reservas
-- Las superposiciones se validan por intervalos en el servicio.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reservas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cliente_id BIGINT NOT NULL,
    cancha_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    estado ENUM(
        'PENDIENTE',
        'CONFIRMADA',
        'COMPLETADA',
        'CANCELADA',
        'AUSENTE'
    ) NOT NULL DEFAULT 'PENDIENTE',
    cantidad_jugadores INT NOT NULL DEFAULT 4,
    comentarios VARCHAR(500),
    observaciones_administrativas VARCHAR(500),
    precio_total DECIMAL(12, 2) NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT chk_reservas_horario CHECK (hora_fin > hora_inicio),
    CONSTRAINT chk_reservas_jugadores CHECK (
        cantidad_jugadores >= 1 AND cantidad_jugadores <= 8
    ),
    CONSTRAINT chk_reservas_precio CHECK (precio_total >= 0),
    CONSTRAINT fk_reservas_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES clientes(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_reservas_cancha
        FOREIGN KEY (cancha_id)
        REFERENCES canchas(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_reservas_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    INDEX idx_reservas_cliente (cliente_id),
    INDEX idx_reservas_cancha (cancha_id),
    INDEX idx_reservas_fecha (fecha),
    INDEX idx_reservas_estado (estado),
    INDEX idx_reservas_disponibilidad (
        cancha_id, fecha, hora_inicio, hora_fin, estado
    )
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- Pagos
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pagos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reserva_id BIGINT NOT NULL,
    importe DECIMAL(12, 2) NOT NULL,
    metodo_pago ENUM(
        'EFECTIVO',
        'TRANSFERENCIA',
        'TARJETA',
        'OTRO'
    ) NOT NULL,
    estado ENUM(
        'PENDIENTE',
        'ACREDITADO',
        'REEMBOLSADO',
        'ANULADO'
    ) NOT NULL DEFAULT 'PENDIENTE',
    referencia VARCHAR(150),
    fecha_pago DATETIME NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT chk_pagos_importe CHECK (importe > 0),
    CONSTRAINT fk_pagos_reserva
        FOREIGN KEY (reserva_id)
        REFERENCES reservas(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    INDEX idx_pagos_reserva (reserva_id),
    INDEX idx_pagos_estado (estado)
) ENGINE = InnoDB;

-- ------------------------------------------------------------
-- Bloqueos de cancha
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bloqueos_cancha (
    id BIGINT NOT NULL AUTO_INCREMENT,
    cancha_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    motivo VARCHAR(300) NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT chk_bloqueos_horario CHECK (hora_fin > hora_inicio),
    CONSTRAINT fk_bloqueos_cancha
        FOREIGN KEY (cancha_id)
        REFERENCES canchas(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    INDEX idx_bloqueos_disponibilidad (
        cancha_id, fecha, hora_inicio, hora_fin
    )
) ENGINE = InnoDB;

SHOW TABLES;
