USE padel_reservas;

ALTER TABLE torneo_partidos
    MODIFY COLUMN fase ENUM(
        'GRUPOS',
        'ACCESO_1',
        'ACCESO_2',
        'ACCESO_3',
        'ACCESO_4',
        'ACCESO_5',
        'DIECISEISAVOS',
        'OCTAVOS',
        'CUARTOS',
        'SEMIFINAL',
        'FINAL'
    ) COLLATE utf8mb4_unicode_ci NOT NULL;
