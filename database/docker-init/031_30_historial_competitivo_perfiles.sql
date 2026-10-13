-- W2.2A - proyeccion auditable de participaciones competitivas por perfil.
CREATE TABLE IF NOT EXISTS historial_competitivo_perfiles (
 id BIGINT NOT NULL AUTO_INCREMENT, partido_id BIGINT NOT NULL, perfil_jugador_id BIGINT NOT NULL,
 torneo_categoria_id BIGINT NOT NULL, categoria_deportiva_id BIGINT NULL, inscripcion_id BIGINT NOT NULL,
 lado TINYINT NOT NULL, orden_integrante TINYINT NOT NULL, resultado ENUM('GANADOR','PERDEDOR') NOT NULL,
 nombre_perfil_snapshot VARCHAR(220) NOT NULL, categoria_snapshot VARCHAR(80) NULL,
 fecha_partido DATE NULL, fecha_resultado DATETIME NOT NULL, usuario_resultado_id BIGINT NOT NULL,
 fecha_proyeccion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(id), UNIQUE KEY uq_historial_competitivo_partido_perfil(partido_id,perfil_jugador_id),
 INDEX idx_historial_competitivo_perfil_fecha(perfil_jugador_id,fecha_resultado DESC,partido_id DESC),
 INDEX idx_historial_competitivo_categoria_resultado(torneo_categoria_id,resultado,fecha_resultado),
 CONSTRAINT fk_historial_competitivo_partido FOREIGN KEY(partido_id) REFERENCES torneo_partidos(id) ON UPDATE CASCADE ON DELETE CASCADE,
 CONSTRAINT fk_historial_competitivo_perfil FOREIGN KEY(perfil_jugador_id) REFERENCES perfiles_jugador(id) ON UPDATE CASCADE ON DELETE RESTRICT,
 CONSTRAINT fk_historial_competitivo_torneo_categoria FOREIGN KEY(torneo_categoria_id) REFERENCES torneo_categorias(id) ON UPDATE CASCADE ON DELETE RESTRICT,
 CONSTRAINT fk_historial_competitivo_categoria FOREIGN KEY(categoria_deportiva_id) REFERENCES categorias_deportivas(id) ON UPDATE CASCADE ON DELETE RESTRICT,
 CONSTRAINT fk_historial_competitivo_inscripcion FOREIGN KEY(inscripcion_id) REFERENCES torneo_inscripciones(id) ON UPDATE CASCADE ON DELETE RESTRICT,
 CONSTRAINT fk_historial_competitivo_usuario FOREIGN KEY(usuario_resultado_id) REFERENCES usuarios(id) ON UPDATE CASCADE ON DELETE RESTRICT,
 CONSTRAINT chk_historial_competitivo_lado CHECK(lado IN (1,2)),
 CONSTRAINT chk_historial_competitivo_orden CHECK(orden_integrante IN (1,2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
