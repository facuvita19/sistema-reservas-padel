USE padel_reservas;

ALTER TABLE torneo_categorias
    ADD COLUMN premio_campeon DECIMAL(12,2) NULL
        AFTER precio_inscripcion,
    ADD COLUMN premio_subcampeon DECIMAL(12,2) NULL
        AFTER premio_campeon,
    ADD COLUMN premio_descripcion VARCHAR(500) NULL
        AFTER premio_subcampeon,
    ADD CONSTRAINT chk_torneo_categorias_premio_campeon
        CHECK (premio_campeon IS NULL OR premio_campeon >= 0),
    ADD CONSTRAINT chk_torneo_categorias_premio_subcampeon
        CHECK (premio_subcampeon IS NULL OR premio_subcampeon >= 0);
