ALTER TABLE memoria_calculo
    ADD COLUMN holerite_id BIGINT;

ALTER TABLE memoria_calculo
    ADD CONSTRAINT fk_memoria_calculo_holerite
    FOREIGN KEY (holerite_id) REFERENCES holerite(id) ON DELETE SET NULL;

CREATE INDEX idx_memoria_calculo_holerite_id
    ON memoria_calculo(holerite_id);
