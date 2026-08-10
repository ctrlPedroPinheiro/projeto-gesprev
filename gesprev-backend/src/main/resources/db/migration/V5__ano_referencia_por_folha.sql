ALTER TABLE folha ADD COLUMN ano_referencia INTEGER;

UPDATE folha f
SET ano_referencia = ff.ano_referencia
FROM ficha_financeira ff
WHERE f.folha_id = ff.id;

ALTER TABLE folha ALTER COLUMN ano_referencia SET NOT NULL;
ALTER TABLE ficha_financeira DROP COLUMN ano_referencia;
