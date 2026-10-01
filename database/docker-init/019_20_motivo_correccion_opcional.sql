USE padel_reservas;

ALTER TABLE torneo_resultado_correcciones
    DROP CHECK chk_torneo_correcciones_motivo;

ALTER TABLE torneo_resultado_correcciones
    MODIFY motivo VARCHAR(500) NULL;
