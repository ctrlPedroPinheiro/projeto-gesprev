ALTER TABLE servidor
ADD COLUMN IF NOT EXISTS sexo VARCHAR(20);

UPDATE servidor
SET sexo = CASE
    WHEN LOWER(nome) LIKE 'maria %'
        OR LOWER(nome) LIKE 'ana %'
        OR LOWER(nome) LIKE 'joana %'
        OR LOWER(nome) LIKE 'josefa %'
        OR LOWER(nome) LIKE '% cristina %'
        OR LOWER(nome) LIKE 'cristina %'
        OR LOWER(nome) LIKE 'lucia %'
        OR LOWER(nome) LIKE 'luciana %'
        OR LOWER(nome) LIKE 'marcia %'
        OR LOWER(nome) LIKE 'patricia %'
        OR LOWER(nome) LIKE 'fernanda %'
        OR LOWER(nome) LIKE 'claudia %'
        THEN 'FEMININO'
    ELSE 'MASCULINO'
END
WHERE sexo IS NULL;

ALTER TABLE servidor
ALTER COLUMN sexo SET NOT NULL;
