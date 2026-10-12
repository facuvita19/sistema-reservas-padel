-- reservas-ocasionales-v1
-- La integridad del responsable se valida en ReservaService.
-- MySQL 8.4 no permite este CHECK sobre cliente_id porque la columna
-- participa en una clave foranea con acciones referenciales.

ALTER TABLE reservas
    MODIFY COLUMN cliente_id BIGINT NULL,
    ADD COLUMN cliente_ocasional_nombre VARCHAR(150) NULL AFTER cliente_id,
    ADD COLUMN cliente_ocasional_telefono VARCHAR(40) NULL AFTER cliente_ocasional_nombre,
    ADD COLUMN cliente_ocasional_email VARCHAR(150) NULL AFTER cliente_ocasional_telefono,
    ADD COLUMN cliente_ocasional_documento VARCHAR(40) NULL AFTER cliente_ocasional_email;
