SET search_path TO ${schemaName};

ALTER TABLE documento DROP CONSTRAINT IF EXISTS documento_processo_id_fkey;
ALTER TABLE documento DROP CONSTRAINT IF EXISTS fk_documento_processo;
ALTER TABLE documento ADD CONSTRAINT fk_documento_processo
    FOREIGN KEY (processo_id) REFERENCES processo_aposentadoria(id) ON DELETE CASCADE;

ALTER TABLE holerite DROP CONSTRAINT IF EXISTS holerite_id_fkey;
ALTER TABLE holerite DROP CONSTRAINT IF EXISTS fk_holerite_documento;
ALTER TABLE holerite ADD CONSTRAINT fk_holerite_documento
    FOREIGN KEY (id) REFERENCES documento(id) ON DELETE CASCADE;

ALTER TABLE ficha_financeira DROP CONSTRAINT IF EXISTS ficha_financeira_id_fkey;
ALTER TABLE ficha_financeira DROP CONSTRAINT IF EXISTS fk_ficha_financeira_documento;
ALTER TABLE ficha_financeira ADD CONSTRAINT fk_ficha_financeira_documento
    FOREIGN KEY (id) REFERENCES documento(id) ON DELETE CASCADE;

ALTER TABLE cts DROP CONSTRAINT IF EXISTS cts_id_fkey;
ALTER TABLE cts DROP CONSTRAINT IF EXISTS fk_cts_documento;
ALTER TABLE cts ADD CONSTRAINT fk_cts_documento
    FOREIGN KEY (id) REFERENCES documento(id) ON DELETE CASCADE;

ALTER TABLE provento DROP CONSTRAINT IF EXISTS provento_holerite_id_fkey;
ALTER TABLE provento DROP CONSTRAINT IF EXISTS fk_provento_holerite;
ALTER TABLE provento ADD CONSTRAINT fk_provento_holerite
    FOREIGN KEY (holerite_id) REFERENCES holerite(id) ON DELETE CASCADE;

ALTER TABLE folha DROP CONSTRAINT IF EXISTS folha_folha_id_fkey;
ALTER TABLE folha DROP CONSTRAINT IF EXISTS fk_folha_ficha_financeira;
ALTER TABLE folha ADD CONSTRAINT fk_folha_ficha_financeira
    FOREIGN KEY (folha_id) REFERENCES ficha_financeira(id) ON DELETE CASCADE;

ALTER TABLE checklist_documento DROP CONSTRAINT IF EXISTS checklist_documento_processo_id_fkey;
ALTER TABLE checklist_documento DROP CONSTRAINT IF EXISTS fk_checklist_processo;
ALTER TABLE checklist_documento ADD CONSTRAINT fk_checklist_processo
    FOREIGN KEY (processo_id) REFERENCES processo_aposentadoria(id) ON DELETE CASCADE;

ALTER TABLE memoria_calculo DROP CONSTRAINT IF EXISTS memoria_calculo_processo_id_fkey;
ALTER TABLE memoria_calculo DROP CONSTRAINT IF EXISTS fk_memoria_calculo_processo;
ALTER TABLE memoria_calculo ADD CONSTRAINT fk_memoria_calculo_processo
    FOREIGN KEY (processo_id) REFERENCES processo_aposentadoria(id) ON DELETE CASCADE;

ALTER TABLE historico_processo DROP CONSTRAINT IF EXISTS historico_processo_processo_id_fkey;
ALTER TABLE historico_processo DROP CONSTRAINT IF EXISTS fk_historico_processo;
ALTER TABLE historico_processo ADD CONSTRAINT fk_historico_processo
    FOREIGN KEY (processo_id) REFERENCES processo_aposentadoria(id) ON DELETE CASCADE;

ALTER TABLE ato_aposentadoria DROP CONSTRAINT IF EXISTS ato_aposentadoria_processo_id_fkey;
ALTER TABLE ato_aposentadoria DROP CONSTRAINT IF EXISTS fk_ato_processo;
ALTER TABLE ato_aposentadoria ADD CONSTRAINT fk_ato_processo
    FOREIGN KEY (processo_id) REFERENCES processo_aposentadoria(id) ON DELETE CASCADE;
