USE padel_reservas;

ALTER TABLE torneo_categorias
    ADD COLUMN formato_competencia ENUM(
        'ELIMINACION_DIRECTA',
        'GRUPOS_ELIMINACION'
    ) NOT NULL DEFAULT 'ELIMINACION_DIRECTA'
        AFTER premio_descripcion,
    ADD COLUMN cantidad_grupos_3 INT NOT NULL DEFAULT 0
        AFTER formato_competencia,
    ADD COLUMN cantidad_grupos_4 INT NOT NULL DEFAULT 0
        AFTER cantidad_grupos_3,
    ADD CONSTRAINT chk_torneo_categorias_grupos_3
        CHECK (cantidad_grupos_3 >= 0),
    ADD CONSTRAINT chk_torneo_categorias_grupos_4
        CHECK (cantidad_grupos_4 >= 0);
