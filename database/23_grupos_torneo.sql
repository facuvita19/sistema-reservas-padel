USE padel_reservas;

CREATE TABLE IF NOT EXISTS torneo_grupos (
 id BIGINT NOT NULL AUTO_INCREMENT, torneo_categoria_id BIGINT NOT NULL,
 nombre VARCHAR(30) NOT NULL, orden INT NOT NULL, capacidad INT NOT NULL,
 modo_asignacion ENUM('SORTEO_DIRIGIDO','MANUAL') NOT NULL DEFAULT 'MANUAL',
 confirmado BOOLEAN NOT NULL DEFAULT FALSE, fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY(id), UNIQUE KEY uq_grupo_categoria_orden(torneo_categoria_id,orden),
 CONSTRAINT fk_grupo_categoria FOREIGN KEY(torneo_categoria_id) REFERENCES torneo_categorias(id) ON DELETE CASCADE,
 CONSTRAINT chk_grupo_capacidad CHECK(capacidad IN (3,4))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS torneo_grupo_integrantes (
 id BIGINT NOT NULL AUTO_INCREMENT, grupo_id BIGINT NOT NULL, inscripcion_id BIGINT NOT NULL,
 cabeza_serie BOOLEAN NOT NULL DEFAULT FALSE, orden_sorteo INT NOT NULL DEFAULT 0,
 fecha_asignacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(id),
 UNIQUE KEY uq_grupo_inscripcion(grupo_id,inscripcion_id), UNIQUE KEY uq_inscripcion_grupo(inscripcion_id),
 CONSTRAINT fk_integrante_grupo FOREIGN KEY(grupo_id) REFERENCES torneo_grupos(id) ON DELETE CASCADE,
 CONSTRAINT fk_integrante_inscripcion FOREIGN KEY(inscripcion_id) REFERENCES torneo_inscripciones(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
