ALTER TABLE provento ADD COLUMN ato_aposentadoria_id BIGINT;

ALTER TABLE provento
    ADD CONSTRAINT fk_provento_ato
    FOREIGN KEY (ato_aposentadoria_id) REFERENCES ato_aposentadoria(id) ON DELETE CASCADE;

CREATE INDEX idx_provento_ato ON provento(ato_aposentadoria_id);

ALTER TABLE ato_aposentadoria ALTER COLUMN valor_proventos DROP NOT NULL;
ALTER TABLE ato_aposentadoria ALTER COLUMN valor_quinquenio DROP NOT NULL;
