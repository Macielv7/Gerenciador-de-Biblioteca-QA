# Gerenciador de Biblioteca Pessoal

Aplicação web completa para cadastro e gerenciamento de livros de uma biblioteca pessoal, desenvolvida com Spring Boot e MongoDB.

---

## Funcionalidades

- ✅ **Cadastro de Usuários** — registro com validação e senha criptografada (BCrypt)
- ✅ **Autenticação** — login/logout com gerenciamento de sessão (Spring Security)
- ✅ **CRUD de Livros** — criar, listar, editar e excluir livros
- ✅ **Busca por título** — filtro case-insensitive
- ✅ **Isolamento por usuário** — cada usuário vê apenas seus próprios livros
- ✅ **Tratamento de erros** — páginas de erro amigáveis com GlobalExceptionHandler

---

## Tecnologias

| Camada | Tecnologia |
|--------|-----------|
| Backend | Spring Boot 3.3.5 (Java 21) |
| Banco de Dados | MongoDB (via Docker) |
| Segurança | Spring Security 6 |
| Templates | Thymeleaf |
| Build | Maven (mvnw) |
| Testes | JUnit 5 + Testcontainers + WireMock |
| Cobertura | JaCoCo |
| Qualidade | SonarCloud |
| CI/CD | GitHub Actions |

---

## Pré-requisitos

- **Java 21** ([Temurin](https://adoptium.net/))
- **Docker Desktop** (necessário para Testcontainers e MongoDB local)
- **Maven** (ou usar o wrapper `./mvnw` incluído)
- **Git**

---

## Observações de execução

- Os testes usam Testcontainers e requerem o Docker Desktop em execução no Windows.
- Foi ajustado o projeto para usar o Testcontainers `1.21.4`, que resolve a detecção do Docker Desktop via named pipe no Windows.
- Use `./mvnw clean test` ou `./mvnw clean verify` para executar o projeto com o ambiente correto.


---

## Como Executar Localmente

### 1. Clone o repositório

```bash
git clone https://github.com/SEU_USUARIO/projeto-biblioteca.git
cd projeto-biblioteca
```

### 2. Suba o MongoDB com Docker Compose

```bash
docker-compose up -d
```

O `docker-compose.yml` sobe um container MongoDB na porta **27017**.

### 3. Execute a aplicação

```bash
./mvnw spring-boot:run
```

Acesse em: **http://localhost:8080**

### 4. Primeiro acesso

1. Clique em **"Registrar"** para criar sua conta
2. Faça **login** com o usuário criado
3. Comece a **cadastrar seus livros**

---

## Como Rodar os Testes

> ⚠️ O Docker deve estar rodando, pois os testes utilizam **Testcontainers** para subir um MongoDB real.

```bash
./mvnw clean verify
```

Isso irá:
1. Compilar o projeto
2. Rodar todos os testes (repositório, serviço e controller)
3. Gerar o relatório de cobertura JaCoCo

O relatório HTML estará disponível em:
```
target/site/jacoco/index.html
```

### Rodar apenas um teste específico

```bash
./mvnw test -Dtest=LivroServicoTest
./mvnw test -Dtest=LivroControllerTest
./mvnw test -Dtest=UsuarioServicoTest
```

---

## Cobertura de Testes (JaCoCo)

O projeto exige **mínimo de 80% de cobertura** de instruções, configurado diretamente no `pom.xml`. O build falha automaticamente se a cobertura ficar abaixo desse limite.

Para visualizar o relatório:

```bash
./mvnw verify
start target/site/jacoco/index.html  # Windows
open target/site/jacoco/index.html   # Mac/Linux
```

---

## Qualidade de Código — SonarCloud

O projeto é integrado ao **SonarCloud** para análise estática automática.

### Análise manual (local)

```bash
./mvnw sonar:sonar \
  -Dsonar.projectKey=SEU_PROJECT_KEY \
  -Dsonar.token=SEU_TOKEN
```

A análise é executada automaticamente no **GitHub Actions** a cada push na branch `main`.

---

## CI/CD — GitHub Actions

O pipeline (`.github/workflows/ci.yml`) executa automaticamente:

1. **Checkout** do código
2. **Configuração** do JDK 21
3. **Build + Testes** com cobertura (`./mvnw clean verify`)
4. **Análise SonarCloud** (`./mvnw sonar:sonar`)
5. **Publicação** do relatório JaCoCo como artifact do workflow

### Secrets necessários no GitHub

| Secret | Descrição |
|--------|-----------|
| `SONAR_TOKEN` | Token de autenticação do SonarCloud |
| `SONAR_PROJECT_KEY` | Chave do projeto no SonarCloud |

---

## Estrutura do Projeto

```
projeto/
├── .github/
│   └── workflows/
│       └── ci.yml              # Pipeline CI/CD
├── src/
│   ├── main/
│   │   ├── java/com/example/projeto/
│   │   │   ├── config/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── PasswordConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── LivroController.java
│   │   │   │   └── UsuarioController.java
│   │   │   ├── model/
│   │   │   │   ├── Livro.java
│   │   │   │   └── Usuario.java
│   │   │   ├── repository/
│   │   │   │   ├── LivroRepositorio.java
│   │   │   │   └── UsuarioRepositorio.java
│   │   │   └── service/
│   │   │       ├── LivroServico.java
│   │   │       └── UsuarioServico.java
│   │   └── resources/
│   │       ├── templates/          # Templates Thymeleaf
│   │       │   ├── livros/
│   │       │   │   ├── lista.html
│   │       │   │   └── formulario.html
│   │       │   ├── usuarios/
│   │       │   │   └── registro.html
│   │       │   ├── login.html
│   │       │   └── erro.html
│   │       └── application.properties
│   └── test/
│       ├── java/com/example/projeto/
│       │   ├── LivroControllerTest.java   # Caixa Preta (E2E)
│       │   ├── UsuarioControllerTest.java # Caixa Preta (E2E)
│       │   ├── LivroServicoTest.java      # Integração + Caixa Branca
│       │   ├── UsuarioServicoTest.java    # Integração + Caixa Branca
│       │   ├── LivroRepositorioTest.java  # Integração (Repositório)
│       │   └── ProjetoApplicationTests.java
│       └── resources/
│           └── application-test.properties
├── docker-compose.yml
├── pom.xml
├── README.md
└── RTM.md
```

---

## Arquitetura MVC

```
Browser → Controller → Service → Repository → MongoDB
             ↑                                    ↓
          Thymeleaf ←────────── Model ←──────────┘
```

- **Controller**: Recebe requisições HTTP, delega ao Service, retorna views
- **Service**: Lógica de negócio (validações, regras)
- **Repository**: Acesso ao banco de dados MongoDB
- **Model**: Entidades de domínio (`Livro`, `Usuario`)

---

## Estratégia de Testes

| Tipo | Classe | Ferramenta |
|------|--------|-----------|
| Caixa Preta (E2E) | `LivroControllerTest` | MockMvc + Testcontainers |
| Caixa Preta (E2E) | `UsuarioControllerTest` | MockMvc + Testcontainers |
| Integração / Caixa Branca | `LivroServicoTest` | Testcontainers |
| Integração / Caixa Branca | `UsuarioServicoTest` | Testcontainers |
| Integração (Repositório) | `LivroRepositorioTest` | Testcontainers |
| Contexto | `ProjetoApplicationTests` | Testcontainers |

> **Importante:** Mocks estão **proibidos** neste projeto. Todos os testes usam MongoDB real via Testcontainers.