USE padel_reservas;

CREATE TABLE IF NOT EXISTS torneo_partidos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    torneo_categoria_id BIGINT NOT NULL,
    fase ENUM(
        'DIECISEISAVOS',
        'OCTAVOS',
        'CUARTOS',
        'SEMIFINAL',
        'FINAL'
    ) NOT NULL,
    orden_fase INT NOT NULL,
    pareja_1_inscripcion_id BIGINT NULL,
    pareja_2_inscripcion_id BIGINT NULL,
    estado ENUM(
        'PENDIENTE',
        'PROGRAMADO',
        'EN_CURSO',
        'FINALIZADO',
        'CANCELADO'
    ) NOT NULL DEFAULT 'PENDIENTE',
    es_bye BOOLEAN NOT NULL DEFAULT FALSE,
    fecha DATE NULL,
    hora_inicio TIME NULL,
    hora_fin TIME NULL,
    cancha_id BIGINT NULL,
    ganadora_inscripcion_id BIGINT NULL,
    partido_siguiente_id BIGINT NULL,
    posicion_siguiente ENUM('PAREJA_1', 'PAREJA_2') NULL,
    usuario_resultado_id BIGINT NULL,
    fecha_finalizacion DATETIME NULL,
    observaciones VARCHAR(500) NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_torneo_partidos_fase_orden (
        torneo_categoria_id,
        fase,
        orden_fase
    ),
    INDEX idx_torneo_partidos_categoria_estado (
        torneo_categoria_id,
        estado,
        fase,
        orden_fase
    ),
    INDEX idx_torneo_partidos_programacion (
        cancha_id,
        fecha,
        hora_inicio,
        hora_fin,
        estado
    ),
    INDEX idx_torneo_partidos_pareja_1 (
        pareja_1_inscripcion_id
    ),
    INDEX idx_torneo_partidos_pareja_2 (
        pareja_2_inscripcion_id
    ),
    INDEX idx_torneo_partidos_siguiente (
        partido_siguiente_id
    ),

    CONSTRAINT fk_torneo_partidos_categoria
        FOREIGN KEY (torneo_categoria_id)
        REFERENCES torneo_categorias(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_torneo_partidos_pareja_1
        FOREIGN KEY (pareja_1_inscripcion_id)
        REFERENCES torneo_inscripciones(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_torneo_partidos_pareja_2
        FOREIGN KEY (pareja_2_inscripcion_id)
        REFERENCES torneo_inscripciones(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_torneo_partidos_ganadora
        FOREIGN KEY (ganadora_inscripcion_id)
        REFERENCES torneo_inscripciones(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_torneo_partidos_cancha
        FOREIGN KEY (cancha_id)
        REFERENCES canchas(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_torneo_partidos_siguiente
        FOREIGN KEY (partido_siguiente_id)
        REFERENCES torneo_partidos(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_torneo_partidos_usuario_resultado
        FOREIGN KEY (usuario_resultado_id)
        REFERENCES usuarios(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT chk_torneo_partidos_orden
        CHECK (orden_fase > 0),
    CONSTRAINT chk_torneo_partidos_horario
        CHECK (
            hora_inicio IS NULL
            OR hora_fin IS NULL
            OR hora_fin > hora_inicio
        )
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS torneo_partido_sets (
    id BIGINT NOT NULL AUTO_INCREMENT,
    partido_id BIGINT NOT NULL,
    numero_set TINYINT NOT NULL,
    tipo ENUM('NORMAL', 'SUPER_TIE_BREAK') NOT NULL DEFAULT 'NORMAL',
    puntos_pareja_1 TINYINT UNSIGNED NOT NULL,
    puntos_pareja_2 TINYINT UNSIGNED NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_torneo_partido_sets_numero (
        partido_id,
        numero_set
    ),
    INDEX idx_torneo_partido_sets_partido (
        partido_id,
        numero_set
    ),
    CONSTRAINT fk_torneo_partido_sets_partido
        FOREIGN KEY (partido_id)
        REFERENCES torneo_partidos(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT chk_torneo_partido_sets_numero
        CHECK (numero_set BETWEEN 1 AND 5),
    CONSTRAINT chk_torneo_partido_sets_sin_empate
        CHECK (puntos_pareja_1 <> puntos_pareja_2)
) ENGINE = InnoDB;
