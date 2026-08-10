CREATE SCHEMA IF NOT EXISTS ${schemaName};
SET search_path TO ${schemaName};

CREATE TABLE servidor (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    dt_nascimento DATE,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    matricula VARCHAR(20) NOT NULL,
    cargo VARCHAR(255) NOT NULL,
    orgao VARCHAR(255) NOT NULL,
    dt_admissao DATE
);

CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    perfil VARCHAR(50) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao TIMESTAMP,
    ultimo_login TIMESTAMP
);

CREATE TABLE processo_aposentadoria (
    id BIGSERIAL PRIMARY KEY,
    numero_processo INTEGER NOT NULL UNIQUE,
    dt_criacao TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    dt_atualizacao TIMESTAMP,
    servidor_id BIGINT NOT NULL REFERENCES servidor(id)
);

CREATE TABLE documento (
    id BIGSERIAL PRIMARY KEY,
    nome_arquivo VARCHAR(255) NOT NULL,
    caminho_arquivo VARCHAR(255) NOT NULL,
    tipo_documento VARCHAR(50) NOT NULL,
    status_vlm VARCHAR(50) NOT NULL,
    dt_upload TIMESTAMP NOT NULL,
    nome_original VARCHAR(255) NOT NULL,
    json_extraido TEXT,
    processo_id BIGINT NOT NULL REFERENCES processo_aposentadoria(id)
);

CREATE TABLE holerite (
    id BIGINT PRIMARY KEY REFERENCES documento(id),
    valor_total_proventos NUMERIC(15,2),
    mes_referencia VARCHAR(10)
);

CREATE TABLE provento (
    id BIGSERIAL PRIMARY KEY,
    tipo_provento VARCHAR(50) NOT NULL,
    referencia DOUBLE PRECISION NOT NULL,
    valor NUMERIC(15,2) NOT NULL,
    vencimento BOOLEAN NOT NULL,
    holerite_id BIGINT REFERENCES holerite(id)
);

CREATE TABLE ficha_financeira (
    id BIGINT PRIMARY KEY REFERENCES documento(id),
    ano_referencia INTEGER NOT NULL
);

CREATE TABLE folha (
    id BIGSERIAL PRIMARY KEY,
    competencia VARCHAR(10),
    vencimentos NUMERIC(15,2),
    descontos NUMERIC(15,2),
    liquido NUMERIC(15,2),
    folha_id BIGINT REFERENCES ficha_financeira(id)
);

CREATE TABLE cts (
    id BIGINT PRIMARY KEY REFERENCES documento(id),
    inicio_contribuicao DATE NOT NULL,
    fim_contribuicao DATE NOT NULL,
    tempo_averbacao INTEGER NOT NULL,
    total_bruto INTEGER NOT NULL,
    faltas INTEGER NOT NULL,
    total_dias INTEGER NOT NULL,
    tempo_legivel VARCHAR(100)
);

CREATE TABLE checklist_documento (
    id BIGSERIAL PRIMARY KEY,
    tipo_documento VARCHAR(50) NOT NULL,
    entregue BOOLEAN NOT NULL DEFAULT FALSE,
    valido BOOLEAN NOT NULL DEFAULT FALSE,
    observacao VARCHAR(500) NOT NULL,
    processo_id BIGINT NOT NULL REFERENCES processo_aposentadoria(id),
    UNIQUE (processo_id, tipo_documento)
);

CREATE TABLE memoria_calculo (
    id BIGSERIAL PRIMARY KEY,
    media_aritmetica NUMERIC(15,2) NOT NULL,
    valor_beneficio NUMERIC(15,2) NOT NULL,
    proporcionalidade NUMERIC(10,4) NOT NULL,
    tipo_calculo VARCHAR(50) NOT NULL,
    processo_id BIGINT NOT NULL UNIQUE REFERENCES processo_aposentadoria(id)
);

CREATE TABLE historico_processo (
    id BIGSERIAL PRIMARY KEY,
    status_anterior VARCHAR(50) NOT NULL,
    status_atual VARCHAR(50) NOT NULL,
    dt_alteracao TIMESTAMP NOT NULL,
    observacao VARCHAR(500) NOT NULL,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id),
    processo_id BIGINT NOT NULL REFERENCES processo_aposentadoria(id)
);

CREATE TABLE ato_aposentadoria (
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
    processo_id BIGINT NOT NULL REFERENCES processo_aposentadoria(id),
    usuario_gerador_id BIGINT NOT NULL REFERENCES usuario(id)
);

CREATE TABLE sequencial_portaria (
    ano INTEGER PRIMARY KEY,
    ultimo INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE fator_atualizacao (
    id BIGSERIAL PRIMARY KEY,
    mes_referencia DATE NOT NULL UNIQUE,
    fator NUMERIC(15,10) NOT NULL
);

CREATE INDEX idx_processo_servidor ON processo_aposentadoria(servidor_id);
CREATE INDEX idx_processo_status ON processo_aposentadoria(status);
CREATE INDEX idx_documento_processo ON documento(processo_id);
CREATE INDEX idx_documento_tipo ON documento(tipo_documento);
CREATE INDEX idx_checklist_processo ON checklist_documento(processo_id);
CREATE INDEX idx_historico_processo ON historico_processo(processo_id);
CREATE INDEX idx_historico_usuario ON historico_processo(usuario_id);
CREATE INDEX idx_fator_mes ON fator_atualizacao(mes_referencia);
