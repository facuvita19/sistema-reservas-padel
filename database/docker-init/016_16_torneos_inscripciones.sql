USE padel_reservas;

CREATE TABLE IF NOT EXISTS torneos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    inscripcion_desde DATETIME NOT NULL,
    inscripcion_hasta DATETIME NOT NULL,
    estado ENUM(
        'BORRADOR',
        'PUBLICADO',
        'INSCRIPCION_ABIERTA',
        'INSCRIPCION_CERRADA',
        'EN_CURSO',
        'FINALIZADO',
        'CANCELADO'
    ) NOT NULL DEFAULT 'BORRADOR',
    reglamento TEXT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    usuario_creacion_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_torneos_estado_fechas (
        estado,
        fecha_inicio,
        fecha_fin
    ),
    INDEX idx_torneos_inscripcion (
        inscripcion_desde,
        inscripcion_hasta,
        activo
    ),
    CONSTRAINT fk_torneos_usuario_creacion
        FOREIGN KEY (usuario_creacion_id)
        REFERENCES usuarios(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_torneos_fechas
        CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT chk_torneos_inscripcion_fechas
        CHECK (inscripcion_hasta >= inscripcion_desde),
    CONSTRAINT chk_torneos_inscripcion_antes_inicio
        CHECK (DATE(inscripcion_hasta) <= fecha_inicio)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS torneo_categorias (
    id BIGINT NOT NULL AUTO_INCREMENT,
    torneo_id BIGINT NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    rama ENUM('MASCULINA', 'FEMENINA', 'MIXTA') NOT NULL,
    cupo_parejas INT NOT NULL,
    precio_inscripcion DECIMAL(12,2) NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_torneo_categorias_nombre_rama (
        torneo_id,
        nombre,
        rama
    ),
    INDEX idx_torneo_categorias_publicas (
        torneo_id,
        activo,
        rama
    ),
    CONSTRAINT fk_torneo_categorias_torneo
        FOREIGN KEY (torneo_id)
        REFERENCES torneos(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_torneo_categorias_cupo
        CHECK (cupo_parejas > 0),
    CONSTRAINT chk_torneo_categorias_precio
        CHECK (precio_inscripcion >= 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS torneo_inscripciones (
    id BIGINT NOT NULL AUTO_INCREMENT,
    torneo_categoria_id BIGINT NOT NULL,
    estado ENUM(
        'PENDIENTE',
        'CONFIRMADA',
        'LISTA_ESPERA',
        'RECHAZADA',
        'CANCELADA'
    ) NOT NULL DEFAULT 'PENDIENTE',
    origen ENUM('WEB', 'ADMINISTRACION') NOT NULL,
    responsable_cliente_id BIGINT NULL,
    precio_inscripcion DECIMAL(12,2) NOT NULL DEFAULT 0,
    comentarios VARCHAR(500) NULL,
    observaciones_administrativas VARCHAR(1000) NULL,
    usuario_gestion_id BIGINT NULL,
    fecha_solicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_confirmacion DATETIME NULL,
    fecha_cancelacion DATETIME NULL,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_torneo_inscripciones_categoria_estado (
        torneo_categoria_id,
        estado,
        fecha_solicitud
    ),
    INDEX idx_torneo_inscripciones_responsable (
        responsable_cliente_id,
        fecha_solicitud
    ),
    INDEX idx_torneo_inscripciones_gestion (
        usuario_gestion_id,
        estado
    ),
    CONSTRAINT fk_torneo_inscripciones_categoria
        FOREIGN KEY (torneo_categoria_id)
        REFERENCES torneo_categorias(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_torneo_inscripciones_responsable
        FOREIGN KEY (responsable_cliente_id)
        REFERENCES clientes(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_torneo_inscripciones_usuario_gestion
        FOREIGN KEY (usuario_gestion_id)
        REFERENCES usuarios(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_torneo_inscripciones_precio
        CHECK (precio_inscripcion >= 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS torneo_inscripcion_jugadores (
    id BIGINT NOT NULL AUTO_INCREMENT,
    inscripcion_id BIGINT NOT NULL,
    orden_integrante TINYINT NOT NULL,
    cliente_id BIGINT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    telefono VARCHAR(30) NOT NULL,
    telefono_normalizado VARCHAR(20) NOT NULL,
    es_responsable BOOLEAN NOT NULL DEFAULT FALSE,
    tipo_vinculacion ENUM(
        'AUTOMATICA',
        'MANUAL',
        'SIN_VINCULAR'
    ) NOT NULL DEFAULT 'SIN_VINCULAR',
    requiere_revision BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_torneo_jugadores_orden (
        inscripcion_id,
        orden_integrante
    ),
    UNIQUE KEY uq_torneo_jugadores_cliente_inscripcion (
        inscripcion_id,
        cliente_id
    ),
    INDEX idx_torneo_jugadores_cliente (
        cliente_id,
        inscripcion_id
    ),
    INDEX idx_torneo_jugadores_telefono (
        telefono_normalizado,
        requiere_revision
    ),
    CONSTRAINT fk_torneo_jugadores_inscripcion
        FOREIGN KEY (inscripcion_id)
        REFERENCES torneo_inscripciones(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_torneo_jugadores_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES clientes(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_torneo_jugadores_orden
        CHECK (orden_integrante IN (1, 2)),
    CONSTRAINT chk_torneo_jugadores_telefono_normalizado
        CHECK (CHAR_LENGTH(telefono_normalizado) BETWEEN 8 AND 20)
) ENGINE = InnoDB;
