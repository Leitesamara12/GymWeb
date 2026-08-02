# GymWeb

O **GymWeb** é uma aplicação única em Spring Boot: o back-end REST e o front-end estático são entregues pelo mesmo processo na porta `8080`. Dessa forma, as páginas HTML consomem a API pela mesma origem (`/api`), sem a necessidade de iniciar um servidor de front-end separado ou configurar CORS para desenvolvimento local.

> A configuração padrão usa H2 persistente em arquivo, portanto a aplicação funciona imediatamente após o primeiro comando de execução. O perfil MySQL permanece disponível para quem desejar utilizá-lo.

## Estrutura do projeto

| Caminho | Finalidade |
|---|---|
| `src/main/java/br/com/gymweb` | Camadas do back-end: controladores, serviços, entidades, repositórios e configurações. |
| `src/main/resources/static` | Front-end servido pelo Spring Boot: páginas HTML, JavaScript, CSS e imagens. |
| `src/main/resources/application.properties` | Perfil padrão com H2 local e configuração do servidor. |
| `src/main/resources/application-mysql.properties` | Perfil opcional para MySQL. |
| `database/schema.sql` | Esquema MySQL atualizado, para uma instalação limpa manual. |
| `.vscode/launch.json` | Configuração de depuração da aplicação no VS Code. |
| `data/` | Diretório gerado automaticamente pelo H2 para persistir os dados locais. Não é versionado. |

## Pré-requisitos

| Item | Versão esperada |
|---|---|
| JDK | 17 ou superior |
| VS Code | Versão atual com a extensão **Extension Pack for Java** instalada |
| Git | Opcional, para controle de versão |
| MySQL | Opcional; somente para execução com o perfil `mysql` |

## Execução padrão no VS Code

Abra a pasta raiz `GymWeb` no VS Code. Em seguida, use uma das opções abaixo.

| Forma | Procedimento |
|---|---|
| Terminal integrado | Execute `./mvnw spring-boot:run`. No Windows, execute `mvnw.cmd spring-boot:run`. |
| Executar e Depurar | Abra a aba **Run and Debug**, selecione `GymWebApplication` e pressione `F5`. |
| Interface Java do VS Code | Abra `GymwebApplication.java` e use a ação **Run** exibida sobre a classe principal. |

Quando o log indicar que a aplicação foi iniciada, acesse [http://localhost:8080](http://localhost:8080). A raiz redireciona automaticamente para a tela de login.

### Credenciais de demonstração

Os dados abaixo são criados automaticamente apenas se ainda não existirem no banco local.

| Perfil | CPF | Senha |
|---|---:|---|
| Administrador (`DONO`) | `00000000000` | `admin123` |
| Aluno | `00000000001` | `aluno123` |

As senhas são persistidas com BCrypt. Por isso, os usuários cadastrados pelas telas de alunos e instrutores já podem usar suas credenciais no login.

## Banco H2 local

O perfil padrão persiste os dados em `./data/gymweb`. O console H2 pode ser acessado em [http://localhost:8080/h2-console](http://localhost:8080/h2-console) com os parâmetros abaixo.

| Campo | Valor |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/gymweb;MODE=MySQL;DATABASE_TO_LOWER=TRUE` |
| User Name | `sa` |
| Password | deixe vazio |

Para reiniciar os dados de desenvolvimento, encerre a aplicação e apague a pasta `data/`. Na próxima inicialização, o Hibernate recriará as tabelas e a aplicação inserirá novamente os dados de demonstração.

## Execução com MySQL

O projeto também oferece um perfil MySQL. Primeiro, inicie o MySQL local e, se desejar preparar o banco manualmente, execute `database/schema.sql`. Em seguida, execute um dos comandos abaixo.

```bash
# Linux/macOS
export GYMWEB_DB_USERNAME=root
export GYMWEB_DB_PASSWORD=root
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

```powershell
# Windows PowerShell
$env:GYMWEB_DB_USERNAME="root"
$env:GYMWEB_DB_PASSWORD="root"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

A URL padrão é `jdbc:mysql://localhost:3306/gymweb`. Para substituí-la, informe `GYMWEB_DB_URL` antes de iniciar a aplicação. O perfil mantém `spring.jpa.hibernate.ddl-auto=update`, permitindo que o mapeamento evolua durante o desenvolvimento local.

## Fluxo funcional implementado

| Área | Fluxo integrado |
|---|---|
| Autenticação | Login por CPF e senha; sessão HTTP e sessão do navegador são criadas em conjunto. |
| Instrutores | O administrador cria instrutores com senha, consulta a lista e acompanha o cargo. |
| Alunos | Administrador ou instrutor cadastra alunos, com vínculo persistente ao instrutor responsável. |
| Exercícios | O catálogo permite cadastrar, listar, filtrar e excluir exercícios. |
| Treinos | Administrador ou instrutor seleciona aluno, instrutor e exercícios para gerar um treino completo. |
| Checklist | O aluno consulta seu treino e marca cada exercício como concluído. |

## Endpoints principais

| Método | Rota | Finalidade |
|---|---|---|
| `POST` | `/api/auth/login` | Autentica CPF e senha. |
| `POST` | `/api/auth/logout` | Encerra a sessão HTTP. |
| `GET` / `POST` | `/api/alunos` | Lista ou cadastra alunos. |
| `GET` / `POST` | `/api/instrutores` | Lista ou cadastra instrutores. A rota antiga `/api/instrutor` permanece compatível. |
| `GET` / `POST` | `/api/exercicios` | Lista ou cadastra exercícios do catálogo. |
| `GET` / `POST` | `/api/treinos` | Lista ou cria treinos. |
| `PATCH` | `/api/treinos/{treinoId}/exercicios/{id}/concluir` | Atualiza o checklist de um exercício. |

## Verificação antes de versionar

```bash
./mvnw clean test
./mvnw package
```

Os comandos acima validam a compilação e os testes automatizados. Arquivos gerados, como `target/` e `data/`, devem permanecer fora do versionamento.
