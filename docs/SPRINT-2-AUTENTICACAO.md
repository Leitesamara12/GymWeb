# Sprint 2 — Validação de CPF/senha e retorno de cargo

## Resumo executivo

Foi implementado o núcleo do módulo de autenticação responsável por consultar CPF no banco, validar senha com BCrypt e retornar o cargo do usuário como `ADMINISTRADOR`, `INSTRUTOR` ou `ALUNO`.

A implementação está registrada na branch `testes`, no commit:

```text
a6f7cbe feat(auth): valida CPF/senha e retorna cargo do usuario
```

O código da Sprint 2 e as correções gerais necessárias foram concluídos. A compilação, os testes e o empacotamento Maven finalizaram com sucesso.

## Escopo entregue

### Validação das credenciais

O `AuthService` implementa o seguinte fluxo:

1. Recebe CPF e senha por meio de `LoginDTO`.
2. Procura primeiro o CPF no repositório de instrutores.
3. Quando encontra um instrutor, valida a senha digitada contra o hash salvo usando `PasswordEncoder.matches()`.
4. Consulta o cargo armazenado na tabela `instrutor` e retorna `ADMINISTRADOR` ou `INSTRUTOR`.
5. Se não encontrar instrutor, procura o CPF no repositório de alunos.
6. Quando encontra um aluno, valida a senha com BCrypt e retorna o cargo fixo `ALUNO`.
7. Quando o CPF não existe ou a senha está incorreta, lança `CredenciaisInvalidasException`.

A mesma mensagem é utilizada para CPF inexistente e senha incorreta:

```text
CPF ou senha inválidos.
```

Isso evita que a API revele se determinado CPF está cadastrado, reduzindo o risco de enumeração de usuários.

### Segurança e BCrypt

Foi adicionada a dependência `spring-boot-starter-security` ao `pom.xml`.

A classe `SecurityConfig`:

- disponibiliza um bean `PasswordEncoder` baseado em `BCryptPasswordEncoder`;
- desabilita CSRF para o modelo atual da aplicação;
- permite as requisições HTTP, mantendo a decisão de autenticação na lógica de negócio existente.

As senhas armazenadas devem permanecer como hashes BCrypt. A senha em texto digitada pelo usuário é usada somente como entrada de `matches()` e não é persistida.

### DTOs

Foram criados:

- `LoginDTO`: entrada com `cpf` e `senha`;
- `LoginResponseDTO`: saída com `id`, `nome` e `cargo`.

O DTO de resposta nunca expõe senha ou hash. Os getters, setters e construtor foram implementados explicitamente para que o módulo não dependa do processamento de anotações do Lombok.

### Repositórios

Foi criado `AlunoRepository` com consulta por CPF:

```java
Optional<Aluno> findByCpf(String cpf);
```

O `InstrutorRepository` existente foi preservado e ampliado com:

```java
Optional<Instrutor> findByCpf(String cpf);
```

Também foi adicionada uma consulta nativa para recuperar o cargo sem depender de um atributo `cargo` no model atual:

```sql
SELECT cargo FROM instrutor WHERE cpf = :cpf
```

### Tratamento de erro

Foi criada `CredenciaisInvalidasException`, uma exceção de runtime específica para falhas de autenticação.

Ela cobre:

- CPF inexistente nas tabelas de instrutor e aluno;
- senha diferente do hash armazenado.

## Testes preparados

O `AuthServiceTest` foi estruturado como teste unitário com JUnit 5 e Mockito, sem exigir conexão real com o banco.

Foram preparados os cenários:

- autenticação de `ADMINISTRADOR`;
- autenticação de `INSTRUTOR`;
- autenticação de `ALUNO`;
- rejeição de senha incorreta;
- rejeição de CPF inexistente.

O cenário de `ADMINISTRADOR` imprime:

```text
Cargo autenticado: ADMINISTRADOR
```

### Situação da validação

O Maven resolveu as dependências, compilou os fontes e executou toda a suíte.

Resultado atual:

```text
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Os cinco testes do `AuthService` e o teste de contexto da aplicação foram aprovados. O comando `mvn clean install` também terminou com `BUILD SUCCESS`.

## Arquivos entregues ou alterados

- `pom.xml`
- `src/main/java/br/com/gymweb/config/SecurityConfig.java`
- `src/main/java/br/com/gymweb/dto/LoginDTO.java`
- `src/main/java/br/com/gymweb/dto/LoginResponseDTO.java`
- `src/main/java/br/com/gymweb/repository/AlunoRepository.java`
- `src/main/java/br/com/gymweb/repository/InstrutorRepository.java`
- `src/main/java/br/com/gymweb/service/AuthService.java`
- `src/main/java/br/com/gymweb/service/exception/CredenciaisInvalidasException.java`
- `src/test/java/br/com/gymweb/AuthServiceTest.java`
- `src/test/resources/application.properties`
- seis models em `src/main/java/br/com/gymweb/model/`
- `src/main/java/br/com/gymweb/controller/InstrutorController.java`
- `src/main/java/br/com/gymweb/service/InstrutorService.java`

## Decisões e limites de escopo

- O `LoginController` remoto com credenciais fixas `admin`/`123` foi mantido sem alterações, conforme definição do projeto.
- O Maven Wrapper não foi alterado.
- Nenhum hash ou senha de banco foi incluído nos DTOs de resposta.

## Correções gerais concluídas

- packages dos seis models alinhados para `br.com.gymweb.model`;
- marcadores de conflito Git removidos de `InstrutorController`, `InstrutorRepository` e `InstrutorService`;
- imports de `InstrutorController` corrigidos;
- `javax.servlet.http.HttpSession` substituído por `jakarta.servlet.http.HttpSession`;
- uso de `isADMINISTRADOR` alinhado para `isAdm`;
- campo `cargo` mapeado em `Instrutor`, com valor inicial `INSTRUTOR`;
- H2 configurado apenas no escopo de testes para validar o contexto sem depender do MySQL local;
- Lombok confirmado funcional após a correção dos packages.

## Pendência externa

O servidor MySQL local está acessível, mas o database `gymweb` não existe. Ainda é necessário provisionar o schema de produção/desenvolvimento e confirmar nele as colunas `cpf`, `senha` e `cargo`, incluindo o hash BCrypt do usuário mestre.

Essa pendência não bloqueia mais os testes automatizados: o profile de testes usa H2 em memória e validou com sucesso o mapeamento JPA completo.
