-- GymWeb: esquema MySQL compatível com as entidades JPA atuais.
-- Para uma instalação limpa, execute este arquivo no MySQL antes de iniciar o perfil mysql.

CREATE DATABASE IF NOT EXISTS gymweb
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE gymweb;

CREATE TABLE IF NOT EXISTS instrutor (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    cargo VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    telefone VARCHAR(20),
    especialidade VARCHAR(50),
    data_cadastro DATETIME NOT NULL,
    data_atualizacao DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_instrutor_cpf UNIQUE (cpf),
    CONSTRAINT uk_instrutor_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS aluno (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(11) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    cargo VARCHAR(20) NOT NULL,
    instrutor_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT uk_aluno_cpf UNIQUE (cpf),
    CONSTRAINT fk_aluno_instrutor
        FOREIGN KEY (instrutor_id) REFERENCES instrutor (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS exercicio_catalogo (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500),
    grupo_muscular VARCHAR(50),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS treino (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(500),
    aluno_id BIGINT NOT NULL,
    instrutor_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_treino_aluno
        FOREIGN KEY (aluno_id) REFERENCES aluno (id),
    CONSTRAINT fk_treino_instrutor
        FOREIGN KEY (instrutor_id) REFERENCES instrutor (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS exercicio_treino (
    id BIGINT NOT NULL AUTO_INCREMENT,
    treino_id BIGINT NOT NULL,
    exercicio_catalogo_id BIGINT NOT NULL,
    ordem INT NOT NULL,
    series INT NOT NULL,
    repeticoes INT NOT NULL,
    carga DOUBLE NOT NULL DEFAULT 0,
    concluido BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    CONSTRAINT fk_exercicio_treino_treino
        FOREIGN KEY (treino_id) REFERENCES treino (id) ON DELETE CASCADE,
    CONSTRAINT fk_exercicio_treino_catalogo
        FOREIGN KEY (exercicio_catalogo_id) REFERENCES exercicio_catalogo (id)
) ENGINE=InnoDB;
