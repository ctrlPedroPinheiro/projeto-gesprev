SET search_path TO gesprev_test;

INSERT INTO usuario (id, nome, cpf, senha, perfil, ativo, data_criacao)
VALUES
    (1, 'Diretor Teste', '900.000.000-00', '$2a$10$BXh/g1DWWpYA3iVRwBK4X.NBc/XAGB61mEbJ6c/Z8tC65znQyM8fy', 'DIRETOR', TRUE, NOW()),
    (2, 'Analista Teste', '900.000.000-01', '$2a$10$BXh/g1DWWpYA3iVRwBK4X.NBc/XAGB61mEbJ6c/Z8tC65znQyM8fy', 'ANALISTA', TRUE, NOW());

SELECT setval(pg_get_serial_sequence('gesprev_test.usuario', 'id'), 2, TRUE);
