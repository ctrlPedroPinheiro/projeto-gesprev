CREATE SCHEMA IF NOT EXISTS gesprev;
SET search_path TO gesprev;

CREATE TABLE IF NOT EXISTS servidor (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    dt_nascimento DATE,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    sexo VARCHAR(20) NOT NULL DEFAULT 'FEMININO',
    email VARCHAR(255) NOT NULL,
    matricula VARCHAR(20) NOT NULL,
    cargo VARCHAR(255) NOT NULL,
    orgao VARCHAR(255) NOT NULL,
    dt_admissao DATE
);

CREATE TABLE IF NOT EXISTS usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(50) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao TIMESTAMP,
    ultimo_login TIMESTAMP
);

CREATE TABLE IF NOT EXISTS processo_aposentadoria (
    id BIGSERIAL PRIMARY KEY,
    numero_processo INTEGER NOT NULL UNIQUE,
    dt_criacao TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    dt_atualizacao TIMESTAMP,
    servidor_id BIGINT NOT NULL,
    CONSTRAINT fk_processo_servidor
        FOREIGN KEY (servidor_id) REFERENCES servidor(id)
);

CREATE TABLE IF NOT EXISTS documento (
    id BIGSERIAL PRIMARY KEY,
    nome_arquivo VARCHAR(255) NOT NULL,
    caminho_arquivo VARCHAR(255) NOT NULL,
    tipo_documento VARCHAR(50) NOT NULL,
    status_vlm VARCHAR(50) NOT NULL,
    dt_upload TIMESTAMP NOT NULL,
    nome_original VARCHAR(255) NOT NULL,
    json_extraido TEXT,
    processo_id BIGINT NOT NULL,
    CONSTRAINT fk_documento_processo
        FOREIGN KEY (processo_id) REFERENCES processo_aposentadoria(id)
);

CREATE TABLE IF NOT EXISTS holerite (
    id BIGINT PRIMARY KEY,
    valor_total_proventos NUMERIC(15,2),
    mes_referencia VARCHAR(10),
    CONSTRAINT fk_holerite_documento
        FOREIGN KEY (id) REFERENCES documento(id)
);

CREATE TABLE IF NOT EXISTS provento (
    id BIGSERIAL PRIMARY KEY,
    tipo_provento VARCHAR(50) NOT NULL,
    referencia DOUBLE PRECISION NOT NULL,
    valor NUMERIC(15,2) NOT NULL,
    vencimento BOOLEAN NOT NULL,
    holerite_id BIGINT,
    CONSTRAINT fk_provento_holerite
        FOREIGN KEY (holerite_id) REFERENCES holerite(id)
);

CREATE TABLE IF NOT EXISTS ficha_financeira (
    id BIGINT PRIMARY KEY,
    CONSTRAINT fk_ficha_financeira_documento
        FOREIGN KEY (id) REFERENCES documento(id)
);

CREATE TABLE IF NOT EXISTS folha (
    id BIGSERIAL PRIMARY KEY,
    ano_referencia INTEGER NOT NULL,
    competencia VARCHAR(10),
    vencimentos NUMERIC(15,2),
    descontos NUMERIC(15,2),
    liquido NUMERIC(15,2),
    folha_id BIGINT,
    CONSTRAINT fk_folha_ficha_financeira
        FOREIGN KEY (folha_id) REFERENCES ficha_financeira(id)
);

CREATE TABLE IF NOT EXISTS cts (
    id BIGINT PRIMARY KEY,
    inicio_contribuicao DATE NOT NULL,
    fim_contribuicao DATE NOT NULL,
    tempo_averbacao INTEGER NOT NULL,
    total_bruto INTEGER NOT NULL,
    faltas INTEGER NOT NULL,
    total_dias INTEGER NOT NULL,
    tempo_legivel VARCHAR(100),
    CONSTRAINT fk_cts_documento
        FOREIGN KEY (id) REFERENCES documento(id)
);

CREATE TABLE IF NOT EXISTS checklist_documento (
    id BIGSERIAL PRIMARY KEY,
    tipo_documento VARCHAR(50) NOT NULL,
    entregue BOOLEAN NOT NULL DEFAULT FALSE,
    valido BOOLEAN NOT NULL DEFAULT FALSE,
    observacao VARCHAR(500) NOT NULL,
    processo_id BIGINT NOT NULL,
    CONSTRAINT fk_checklist_processo
        FOREIGN KEY (processo_id) REFERENCES processo_aposentadoria(id)
);

CREATE TABLE IF NOT EXISTS memoria_calculo (
    id BIGSERIAL PRIMARY KEY,
    media_aritmetica NUMERIC(15,2) NOT NULL,
    valor_beneficio NUMERIC(15,2) NOT NULL,
    proporcionalidade NUMERIC(10,4) NOT NULL,
    tipo_calculo VARCHAR(50) NOT NULL,
    processo_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_memoria_calculo_processo
        FOREIGN KEY (processo_id) REFERENCES processo_aposentadoria(id)
);

CREATE TABLE IF NOT EXISTS historico_processo (
    id BIGSERIAL PRIMARY KEY,
    status_anterior VARCHAR(50) NOT NULL,
    status_atual VARCHAR(50) NOT NULL,
    dt_alteracao TIMESTAMP NOT NULL,
    observacao VARCHAR(500) NOT NULL,
    usuario_id BIGINT NOT NULL,
    processo_id BIGINT NOT NULL,
    CONSTRAINT fk_historico_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT fk_historico_processo
        FOREIGN KEY (processo_id) REFERENCES processo_aposentadoria(id)
);

CREATE TABLE IF NOT EXISTS ato_aposentadoria (
    id BIGSERIAL PRIMARY KEY,
    numero_portaria INTEGER NOT NULL,
    ano_portaria INTEGER NOT NULL,
    natureza_aposentadoria VARCHAR(50) NOT NULL,
    tipo_calculo VARCHAR(50) NOT NULL,
    emenda_constitucional VARCHAR(50) NOT NULL,
    referencia_salarial VARCHAR(255),
    valor_proventos NUMERIC(15,2) NOT NULL,
    valor_quinquenio NUMERIC(15,2) NOT NULL,
    valor_outros_adicionais NUMERIC(15,2),
    descricao_outros_adicionais VARCHAR(255),
    data_finalizacao DATE NOT NULL,
    dt_geracao TIMESTAMP NOT NULL,
    processo_id BIGINT NOT NULL,
    usuario_gerador_id BIGINT NOT NULL,
    CONSTRAINT fk_ato_processo
        FOREIGN KEY (processo_id) REFERENCES processo_aposentadoria(id),
    CONSTRAINT fk_ato_usuario
        FOREIGN KEY (usuario_gerador_id) REFERENCES usuario(id)
);

CREATE TABLE IF NOT EXISTS sequencial_portaria (
    ano INTEGER PRIMARY KEY,
    ultimo INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS fator_atualizacao (
    id BIGSERIAL PRIMARY KEY,
    mes_referencia DATE NOT NULL UNIQUE,
    fator NUMERIC(15,10) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_processo_servidor ON processo_aposentadoria(servidor_id);
CREATE INDEX IF NOT EXISTS idx_processo_status ON processo_aposentadoria(status);
CREATE INDEX IF NOT EXISTS idx_documento_processo ON documento(processo_id);
CREATE INDEX IF NOT EXISTS idx_documento_tipo ON documento(tipo_documento);
CREATE INDEX IF NOT EXISTS idx_checklist_processo ON checklist_documento(processo_id);
CREATE INDEX IF NOT EXISTS idx_historico_processo ON historico_processo(processo_id);
CREATE INDEX IF NOT EXISTS idx_historico_usuario ON historico_processo(usuario_id);
CREATE INDEX IF NOT EXISTS idx_fator_mes ON fator_atualizacao(mes_referencia);
CREATE UNIQUE INDEX IF NOT EXISTS uk_checklist_processo_tipo ON checklist_documento(processo_id, tipo_documento);

INSERT INTO usuario (id, nome, cpf, senha, perfil, ativo, data_criacao, ultimo_login)
VALUES
    (910001, 'Diretor Demonstracao', '900.000.000-00', '$2a$10$BXh/g1DWWpYA3iVRwBK4X.NBc/XAGB61mEbJ6c/Z8tC65znQyM8fy', 'DIRETOR', TRUE, NOW(), NULL),
    (910002, 'Analista Demonstracao', '900.000.000-01', '$2a$10$BXh/g1DWWpYA3iVRwBK4X.NBc/XAGB61mEbJ6c/Z8tC65znQyM8fy', 'ANALISTA', TRUE, NOW(), NULL)
ON CONFLICT (cpf) DO UPDATE SET
    nome = EXCLUDED.nome,
    senha = EXCLUDED.senha,
    perfil = EXCLUDED.perfil,
    ativo = EXCLUDED.ativo;

INSERT INTO servidor (id, nome, dt_nascimento, cpf, sexo, email, matricula, cargo, orgao, dt_admissao)
VALUES
    (910001, 'Maria das Dores Silva', '1965-04-12', '222.333.444-55', 'FEMININO', 'maria.silva@example.com', 'MAT001', 'Professora', 'Instituto Federal Fluminense', '1990-03-01'),
    (910002, 'Joao Pereira Santos', '1962-09-20', '333.444.555-66', 'MASCULINO', 'joao.santos@example.com', 'MAT002', 'Tecnico Administrativo', 'Instituto Federal Fluminense', '1988-08-15'),
    (910003, 'Ana Cristina Lima', '1970-01-30', '444.555.666-77', 'FEMININO', 'ana.lima@example.com', 'MAT003', 'Assistente Social', 'Instituto Federal Fluminense', '1998-02-10')
ON CONFLICT (cpf) DO UPDATE SET
    nome = EXCLUDED.nome,
    sexo = EXCLUDED.sexo,
    email = EXCLUDED.email,
    matricula = EXCLUDED.matricula,
    cargo = EXCLUDED.cargo,
    orgao = EXCLUDED.orgao,
    dt_nascimento = EXCLUDED.dt_nascimento,
    dt_admissao = EXCLUDED.dt_admissao;

INSERT INTO processo_aposentadoria (id, numero_processo, dt_criacao, status, dt_atualizacao, servidor_id)
VALUES
    (910001, 2026001, NOW() - INTERVAL '8 days', 'CADASTRADO', NOW() - INTERVAL '8 days', 910001),
    (910002, 2026002, NOW() - INTERVAL '6 days', 'PENDENTE_DOCUMENTO', NOW() - INTERVAL '5 days', 910002),
    (910003, 2026003, NOW() - INTERVAL '4 days', 'EM_ANALISE', NOW() - INTERVAL '2 days', 910003),
    (910004, 2026004, NOW() - INTERVAL '12 days', 'EM_CALCULO', NOW() - INTERVAL '1 day', 910001),
    (910005, 2026005, NOW() - INTERVAL '20 days', 'FINALIZADO', NOW() - INTERVAL '1 day', 910002)
ON CONFLICT (numero_processo) DO UPDATE SET
    status = EXCLUDED.status,
    dt_atualizacao = EXCLUDED.dt_atualizacao,
    servidor_id = EXCLUDED.servidor_id;

INSERT INTO checklist_documento (tipo_documento, entregue, valido, observacao, processo_id)
SELECT tipo_documento, entregue, valido, observacao, processo_id
FROM (
    VALUES
        ('FICHA_FUNCIONAL', FALSE, FALSE, 'Pendente', 910001),
        ('FICHA_FINANCEIRA', FALSE, FALSE, 'Pendente', 910001),
        ('HOLERITE', FALSE, FALSE, 'Pendente', 910001),
        ('CTS', FALSE, FALSE, 'Pendente', 910001),

        ('FICHA_FUNCIONAL', TRUE, FALSE, 'Documento entregue, aguardando validacao', 910002),
        ('FICHA_FINANCEIRA', FALSE, FALSE, 'Pendente', 910002),
        ('HOLERITE', FALSE, FALSE, 'Pendente', 910002),
        ('CTS', FALSE, FALSE, 'Pendente', 910002),

        ('FICHA_FUNCIONAL', TRUE, TRUE, 'Validado pelo analista', 910003),
        ('FICHA_FINANCEIRA', TRUE, TRUE, 'Validado pelo analista', 910003),
        ('HOLERITE', TRUE, TRUE, 'Validado pelo analista', 910003),
        ('CTS', TRUE, TRUE, 'Validado pelo analista', 910003),

        ('FICHA_FUNCIONAL', TRUE, TRUE, 'Validado pelo analista', 910004),
        ('FICHA_FINANCEIRA', TRUE, TRUE, 'Validado pelo analista', 910004),
        ('HOLERITE', TRUE, TRUE, 'Validado pelo analista', 910004),
        ('CTS', TRUE, TRUE, 'Validado pelo analista', 910004),

        ('FICHA_FUNCIONAL', TRUE, TRUE, 'Validado pelo analista', 910005),
        ('FICHA_FINANCEIRA', TRUE, TRUE, 'Validado pelo analista', 910005),
        ('HOLERITE', TRUE, TRUE, 'Validado pelo analista', 910005),
        ('CTS', TRUE, TRUE, 'Validado pelo analista', 910005)
) AS v(tipo_documento, entregue, valido, observacao, processo_id)
ON CONFLICT (processo_id, tipo_documento) DO UPDATE SET
    entregue = EXCLUDED.entregue,
    valido = EXCLUDED.valido,
    observacao = EXCLUDED.observacao;

INSERT INTO documento (id, nome_arquivo, caminho_arquivo, tipo_documento, status_vlm, dt_upload, nome_original, json_extraido, processo_id)
VALUES
    (910001, 'ficha_funcional_2026002.pdf', 'uploads/demo/ficha_funcional_2026002.pdf', 'FICHA_FUNCIONAL', 'PENDENTE', NOW() - INTERVAL '5 days', 'Ficha Funcional 2026002.pdf', NULL, 910002),

    (910002, 'ficha_funcional_2026003.pdf', 'uploads/demo/ficha_funcional_2026003.pdf', 'FICHA_FUNCIONAL', 'VALIDADO', NOW() - INTERVAL '4 days', 'Ficha Funcional 2026003.pdf', '{"nome":"Ana Cristina Lima"}', 910003),
    (910003, 'ficha_financeira_2026003.pdf', 'uploads/demo/ficha_financeira_2026003.pdf', 'FICHA_FINANCEIRA', 'VALIDADO', NOW() - INTERVAL '4 days', 'Ficha Financeira 2026003.pdf', '{"anoReferencia":2025}', 910003),
    (910004, 'holerite_2026003.pdf', 'uploads/demo/holerite_2026003.pdf', 'HOLERITE', 'VALIDADO', NOW() - INTERVAL '4 days', 'Holerite 2026003.pdf', '{"mesReferencia":"05/2026","valorTotalProventos":7200.00}', 910003),
    (910005, 'cts_2026003.pdf', 'uploads/demo/cts_2026003.pdf', 'CTS', 'VALIDADO', NOW() - INTERVAL '4 days', 'CTS 2026003.pdf', '{"totalDias":10950}', 910003),

    (910006, 'ficha_funcional_2026004.pdf', 'uploads/demo/ficha_funcional_2026004.pdf', 'FICHA_FUNCIONAL', 'VALIDADO', NOW() - INTERVAL '3 days', 'Ficha Funcional 2026004.pdf', '{"nome":"Maria das Dores Silva"}', 910004),
    (910007, 'ficha_financeira_2026004.pdf', 'uploads/demo/ficha_financeira_2026004.pdf', 'FICHA_FINANCEIRA', 'VALIDADO', NOW() - INTERVAL '3 days', 'Ficha Financeira 2026004.pdf', '{"anoReferencia":2025}', 910004),
    (910008, 'holerite_2026004.pdf', 'uploads/demo/holerite_2026004.pdf', 'HOLERITE', 'VALIDADO', NOW() - INTERVAL '3 days', 'Holerite 2026004.pdf', '{"mesReferencia":"05/2026","valorTotalProventos":8500.00}', 910004),
    (910009, 'cts_2026004.pdf', 'uploads/demo/cts_2026004.pdf', 'CTS', 'VALIDADO', NOW() - INTERVAL '3 days', 'CTS 2026004.pdf', '{"totalDias":12000}', 910004),

    (910010, 'ficha_funcional_2026005.pdf', 'uploads/demo/ficha_funcional_2026005.pdf', 'FICHA_FUNCIONAL', 'VALIDADO', NOW() - INTERVAL '18 days', 'Ficha Funcional 2026005.pdf', '{"nome":"Joao Pereira Santos"}', 910005),
    (910011, 'ficha_financeira_2026005.pdf', 'uploads/demo/ficha_financeira_2026005.pdf', 'FICHA_FINANCEIRA', 'VALIDADO', NOW() - INTERVAL '18 days', 'Ficha Financeira 2026005.pdf', '{"anoReferencia":2025}', 910005),
    (910012, 'holerite_2026005.pdf', 'uploads/demo/holerite_2026005.pdf', 'HOLERITE', 'VALIDADO', NOW() - INTERVAL '18 days', 'Holerite 2026005.pdf', '{"mesReferencia":"05/2026","valorTotalProventos":6900.00}', 910005),
    (910013, 'cts_2026005.pdf', 'uploads/demo/cts_2026005.pdf', 'CTS', 'VALIDADO', NOW() - INTERVAL '18 days', 'CTS 2026005.pdf', '{"totalDias":13000}', 910005)
ON CONFLICT (id) DO UPDATE SET
    nome_arquivo = EXCLUDED.nome_arquivo,
    caminho_arquivo = EXCLUDED.caminho_arquivo,
    tipo_documento = EXCLUDED.tipo_documento,
    status_vlm = EXCLUDED.status_vlm,
    dt_upload = EXCLUDED.dt_upload,
    nome_original = EXCLUDED.nome_original,
    json_extraido = EXCLUDED.json_extraido,
    processo_id = EXCLUDED.processo_id;

INSERT INTO holerite (id, valor_total_proventos, mes_referencia)
VALUES
    (910004, 7200.00, '05/2026'),
    (910008, 8500.00, '05/2026'),
    (910012, 6900.00, '05/2026')
ON CONFLICT (id) DO UPDATE SET
    valor_total_proventos = EXCLUDED.valor_total_proventos,
    mes_referencia = EXCLUDED.mes_referencia;

INSERT INTO provento (id, tipo_provento, referencia, valor, vencimento, holerite_id)
VALUES
    (910001, 'Vencimento base', 1.0, 6500.00, TRUE, 910004),
    (910002, 'Quinquenio', 0.1, 700.00, TRUE, 910004),
    (910003, 'Vencimento base', 1.0, 7800.00, TRUE, 910008),
    (910004, 'Quinquenio', 0.1, 700.00, TRUE, 910008),
    (910005, 'Vencimento base', 1.0, 6200.00, TRUE, 910012),
    (910006, 'Quinquenio', 0.1, 700.00, TRUE, 910012)
ON CONFLICT (id) DO UPDATE SET
    tipo_provento = EXCLUDED.tipo_provento,
    referencia = EXCLUDED.referencia,
    valor = EXCLUDED.valor,
    vencimento = EXCLUDED.vencimento,
    holerite_id = EXCLUDED.holerite_id;

INSERT INTO ficha_financeira (id)
VALUES
    (910003),
    (910007),
    (910011)
ON CONFLICT (id) DO NOTHING;

INSERT INTO folha (id, ano_referencia, competencia, vencimentos, descontos, liquido, folha_id)
VALUES
    (910001, 2025, '01/2025', 7000.00, 770.00, 6230.00, 910003),
    (910002, 2025, '02/2025', 7100.00, 781.00, 6319.00, 910003),
    (910003, 2025, '03/2025', 7200.00, 792.00, 6408.00, 910003),
    (910004, 2025, '01/2025', 8300.00, 913.00, 7387.00, 910007),
    (910005, 2025, '02/2025', 8400.00, 924.00, 7476.00, 910007),
    (910006, 2025, '03/2025', 8500.00, 935.00, 7565.00, 910007),
    (910007, 2025, '01/2025', 6700.00, 737.00, 5963.00, 910011),
    (910008, 2025, '02/2025', 6800.00, 748.00, 6052.00, 910011),
    (910009, 2025, '03/2025', 6900.00, 759.00, 6141.00, 910011)
ON CONFLICT (id) DO UPDATE SET
    ano_referencia = EXCLUDED.ano_referencia,
    competencia = EXCLUDED.competencia,
    vencimentos = EXCLUDED.vencimentos,
    descontos = EXCLUDED.descontos,
    liquido = EXCLUDED.liquido,
    folha_id = EXCLUDED.folha_id;

INSERT INTO cts (id, inicio_contribuicao, fim_contribuicao, tempo_averbacao, total_bruto, faltas, total_dias, tempo_legivel)
VALUES
    (910005, '1998-02-10', '2026-05-31', 0, 10340, 20, 10320, '28 anos, 3 meses e 21 dias'),
    (910009, '1990-03-01', '2026-05-31', 0, 13240, 35, 13205, '36 anos, 2 meses e 30 dias'),
    (910013, '1988-08-15', '2026-05-31', 0, 13800, 40, 13760, '37 anos, 9 meses e 16 dias')
ON CONFLICT (id) DO UPDATE SET
    inicio_contribuicao = EXCLUDED.inicio_contribuicao,
    fim_contribuicao = EXCLUDED.fim_contribuicao,
    tempo_averbacao = EXCLUDED.tempo_averbacao,
    total_bruto = EXCLUDED.total_bruto,
    faltas = EXCLUDED.faltas,
    total_dias = EXCLUDED.total_dias,
    tempo_legivel = EXCLUDED.tempo_legivel;

INSERT INTO memoria_calculo (id, media_aritmetica, valor_beneficio, proporcionalidade, tipo_calculo, processo_id)
VALUES
    (910001, 8500.00, 8500.00, 1.0000, 'INTEGRAL', 910004),
    (910002, 6900.00, 6900.00, 1.0000, 'INTEGRAL', 910005)
ON CONFLICT (processo_id) DO UPDATE SET
    media_aritmetica = EXCLUDED.media_aritmetica,
    valor_beneficio = EXCLUDED.valor_beneficio,
    proporcionalidade = EXCLUDED.proporcionalidade,
    tipo_calculo = EXCLUDED.tipo_calculo;

INSERT INTO ato_aposentadoria (
    id, numero_portaria, ano_portaria, natureza_aposentadoria, tipo_calculo,
    emenda_constitucional, referencia_salarial, valor_proventos, valor_quinquenio,
    valor_outros_adicionais, descricao_outros_adicionais, data_finalizacao,
    dt_geracao, processo_id, usuario_gerador_id
)
VALUES
    (910001, 1, 2026, 'VOLUNTARIA', 'INTEGRAL', 'EC_103', 'Classe D - Nivel IV', 6900.00, 700.00, 0.00, NULL, CURRENT_DATE - INTERVAL '1 day', NOW() - INTERVAL '1 day', 910005, 910001)
ON CONFLICT (id) DO UPDATE SET
    numero_portaria = EXCLUDED.numero_portaria,
    ano_portaria = EXCLUDED.ano_portaria,
    natureza_aposentadoria = EXCLUDED.natureza_aposentadoria,
    tipo_calculo = EXCLUDED.tipo_calculo,
    emenda_constitucional = EXCLUDED.emenda_constitucional,
    referencia_salarial = EXCLUDED.referencia_salarial,
    valor_proventos = EXCLUDED.valor_proventos,
    valor_quinquenio = EXCLUDED.valor_quinquenio,
    valor_outros_adicionais = EXCLUDED.valor_outros_adicionais,
    descricao_outros_adicionais = EXCLUDED.descricao_outros_adicionais,
    data_finalizacao = EXCLUDED.data_finalizacao,
    dt_geracao = EXCLUDED.dt_geracao,
    processo_id = EXCLUDED.processo_id,
    usuario_gerador_id = EXCLUDED.usuario_gerador_id;

INSERT INTO historico_processo (id, status_anterior, status_atual, dt_alteracao, observacao, usuario_id, processo_id)
VALUES
    (910001, 'CADASTRADO', 'CADASTRADO', NOW() - INTERVAL '8 days', 'Processo criado', 910002, 910001),

    (910002, 'CADASTRADO', 'CADASTRADO', NOW() - INTERVAL '6 days', 'Processo criado', 910002, 910002),
    (910003, 'CADASTRADO', 'PENDENTE_DOCUMENTO', NOW() - INTERVAL '5 days', 'Documento anexado ao processo', 910002, 910002),

    (910004, 'CADASTRADO', 'CADASTRADO', NOW() - INTERVAL '4 days', 'Processo criado', 910002, 910003),
    (910005, 'CADASTRADO', 'PENDENTE_DOCUMENTO', NOW() - INTERVAL '4 days', 'Documentos anexados ao processo', 910002, 910003),
    (910006, 'PENDENTE_DOCUMENTO', 'EM_ANALISE', NOW() - INTERVAL '2 days', 'Todos os documentos foram validados', 910002, 910003),

    (910007, 'CADASTRADO', 'CADASTRADO', NOW() - INTERVAL '12 days', 'Processo criado', 910002, 910004),
    (910008, 'CADASTRADO', 'PENDENTE_DOCUMENTO', NOW() - INTERVAL '11 days', 'Documentos anexados ao processo', 910002, 910004),
    (910009, 'PENDENTE_DOCUMENTO', 'EM_ANALISE', NOW() - INTERVAL '4 days', 'Todos os documentos foram validados', 910002, 910004),
    (910010, 'EM_ANALISE', 'EM_CALCULO', NOW() - INTERVAL '1 day', 'Memoria de calculo gerada', 910002, 910004),

    (910011, 'CADASTRADO', 'CADASTRADO', NOW() - INTERVAL '20 days', 'Processo criado', 910002, 910005),
    (910012, 'CADASTRADO', 'PENDENTE_DOCUMENTO', NOW() - INTERVAL '18 days', 'Documentos anexados ao processo', 910002, 910005),
    (910013, 'PENDENTE_DOCUMENTO', 'EM_ANALISE', NOW() - INTERVAL '15 days', 'Todos os documentos foram validados', 910002, 910005),
    (910014, 'EM_ANALISE', 'EM_CALCULO', NOW() - INTERVAL '10 days', 'Memoria de calculo gerada', 910002, 910005),
    (910015, 'EM_CALCULO', 'FINALIZADO', NOW() - INTERVAL '1 day', 'Ato de Aposentadoria gerado', 910001, 910005)
ON CONFLICT (id) DO UPDATE SET
    status_anterior = EXCLUDED.status_anterior,
    status_atual = EXCLUDED.status_atual,
    dt_alteracao = EXCLUDED.dt_alteracao,
    observacao = EXCLUDED.observacao,
    usuario_id = EXCLUDED.usuario_id,
    processo_id = EXCLUDED.processo_id;

INSERT INTO sequencial_portaria (ano, ultimo)
VALUES (2026, 1)
ON CONFLICT (ano) DO UPDATE SET ultimo = GREATEST(sequencial_portaria.ultimo, EXCLUDED.ultimo);

INSERT INTO fator_atualizacao (mes_referencia, fator)
VALUES
    ('2025-01-01', 1.0500000000),
    ('2025-02-01', 1.0450000000),
    ('2025-03-01', 1.0400000000),
    ('2025-04-01', 1.0350000000),
    ('2025-05-01', 1.0300000000),
    ('2025-06-01', 1.0250000000)
ON CONFLICT (mes_referencia) DO UPDATE SET
    fator = EXCLUDED.fator;

SELECT setval(pg_get_serial_sequence('gesprev.usuario', 'id'), COALESCE((SELECT MAX(id) FROM usuario), 910001), TRUE);
SELECT setval(pg_get_serial_sequence('gesprev.servidor', 'id'), COALESCE((SELECT MAX(id) FROM servidor), 910001), TRUE);
SELECT setval(pg_get_serial_sequence('gesprev.processo_aposentadoria', 'id'), COALESCE((SELECT MAX(id) FROM processo_aposentadoria), 910001), TRUE);
SELECT setval(pg_get_serial_sequence('gesprev.documento', 'id'), COALESCE((SELECT MAX(id) FROM documento), 910001), TRUE);
SELECT setval(pg_get_serial_sequence('gesprev.provento', 'id'), COALESCE((SELECT MAX(id) FROM provento), 910001), TRUE);
SELECT setval(pg_get_serial_sequence('gesprev.folha', 'id'), COALESCE((SELECT MAX(id) FROM folha), 910001), TRUE);
SELECT setval(pg_get_serial_sequence('gesprev.checklist_documento', 'id'), COALESCE((SELECT MAX(id) FROM checklist_documento), 910001), TRUE);
SELECT setval(pg_get_serial_sequence('gesprev.memoria_calculo', 'id'), COALESCE((SELECT MAX(id) FROM memoria_calculo), 910001), TRUE);
SELECT setval(pg_get_serial_sequence('gesprev.historico_processo', 'id'), COALESCE((SELECT MAX(id) FROM historico_processo), 910001), TRUE);
SELECT setval(pg_get_serial_sequence('gesprev.ato_aposentadoria', 'id'), COALESCE((SELECT MAX(id) FROM ato_aposentadoria), 910001), TRUE);
SELECT setval(pg_get_serial_sequence('gesprev.fator_atualizacao', 'id'), COALESCE((SELECT MAX(id) FROM fator_atualizacao), 910001), TRUE);

