# RTM — Matriz de Rastreabilidade de Requisitos
## Gerenciador de Biblioteca Pessoal

> Cada requisito funcional está mapeado ao(s) teste(s) correspondente(s), garantindo 100% de rastreabilidade.

---

## RF01 — Cadastro de Usuário

**Descrição:** O sistema deve permitir que um novo usuário se registre informando nome de usuário, e-mail e senha. O sistema deve validar duplicidade de username e e-mail, e criptografar a senha antes de persistir.

| Critério de Aceite | Classe de Teste | Método | Tipo |
|--------------------|----------------|--------|------|
| Registrar usuário com senha criptografada | `UsuarioServicoTest` | `shouldRegisterUserWithEncodedPassword` | Integração |
| Lançar exceção ao username duplicado | `UsuarioServicoTest` | `shouldThrowExceptionWhenUsernameAlreadyExists` | Caixa Branca |
| Lançar exceção ao e-mail duplicado | `UsuarioServicoTest` | `shouldThrowExceptionWhenEmailAlreadyExists` | Caixa Branca |
| Persistir usuário no MongoDB | `UsuarioServicoTest` | `shouldPersistUserInMongoDB` | Integração |
| Formulário de registro renderiza | `UsuarioControllerTest` | `shouldShowRegistrationForm` | Caixa Preta |
| Registro via POST redireciona para login | `UsuarioControllerTest` | `shouldRegisterUserAndRedirectToLogin` | Caixa Preta |
| Senhas divergentes retornam erro | `UsuarioControllerTest` | `shouldRejectRegistrationWhenPasswordsMismatch` | Caixa Preta |
| Múltiplos cenários de validação de senha | `UsuarioControllerTest` | `shouldRejectShortPasswords` | Parametrizado |

### Diagrama UML de Sequência — RF01: Cadastro de Usuário

```mermaid
sequenceDiagram
    actor U as Usuário
    participant C as UsuarioController
    participant S as UsuarioServico
    participant R as UsuarioRepositorio
    participant DB as MongoDB

    U->>C: POST /register (usuario, email, senha, confirmarSenha)
    C->>C: valida senha == confirmarSenha
    alt Senhas não coincidem
        C-->>U: redirect /register + erro "As senhas não coincidem"
    else Senha muito curta (< 6)
        C-->>U: redirect /register + erro "mínimo 6 caracteres"
    else Dados válidos
        C->>S: registrarUsuario(usuario, email, senha)
        S->>R: existsByUsuario(usuario)
        R->>DB: query usuários
        DB-->>R: resultado
        alt Username já existe
            R-->>S: true
            S-->>C: throw IllegalArgumentException
            C-->>U: redirect /register + erro
        else Username livre
            S->>R: existsByEmail(email)
            R->>DB: query usuários
            DB-->>R: resultado
            alt E-mail já existe
                R-->>S: true
                S-->>C: throw IllegalArgumentException
                C-->>U: redirect /register + erro
            else E-mail livre
                S->>S: BCrypt.encode(senha)
                S->>R: save(novoUsuario)
                R->>DB: insert
                DB-->>R: usuário salvo
                R-->>S: Usuario
                S-->>C: Usuario
                C-->>U: redirect /login + "Registrado com sucesso!"
            end
        end
    end
```

---

## RF02 — Autenticação (Login / Logout)

**Descrição:** O sistema deve autenticar usuários com username e senha via formulário. Sessão deve ser gerenciada pelo Spring Security.

| Critério de Aceite | Classe de Teste | Método | Tipo |
|--------------------|----------------|--------|------|
| Página de login renderiza | `UsuarioControllerTest` | `shouldShowLoginPage` | Caixa Preta |
| Página de login com erro renderiza | `UsuarioControllerTest` | `shouldShowLoginPageWithError` | Caixa Preta |
| Página de login com logout renderiza | `UsuarioControllerTest` | `shouldShowLoginPageAfterLogout` | Caixa Preta |
| Spring Security carrega UserDetails | `UsuarioServicoTest` | `shouldLoadUserDetailsByUsername` | Integração |
| Rota protegida redireciona anônimo | `LivroControllerTest` | `shouldRedirectUnauthenticatedUserToLogin` | Caixa Preta |

### Diagrama UML de Sequência — RF02: Autenticação

```mermaid
sequenceDiagram
    actor U as Usuário
    participant SC as Spring Security
    participant US as UsuarioServico
    participant R as UsuarioRepositorio
    participant DB as MongoDB
    participant LC as LivroController

    U->>SC: POST /login (username, password)
    SC->>US: loadUserByUsername(username)
    US->>R: findByUsuario(username)
    R->>DB: query
    DB-->>R: Usuario | null
    alt Usuário não encontrado
        R-->>US: Optional.empty()
        US-->>SC: throw UsernameNotFoundException
        SC-->>U: redirect /login?error
    else Usuário encontrado
        R-->>US: Usuario
        US-->>SC: UserDetails (username, password hash, roles)
        SC->>SC: BCrypt.matches(password, hash)
        alt Senha incorreta
            SC-->>U: redirect /login?error
        else Autenticado
            SC->>SC: cria HttpSession com Authentication
            SC-->>U: redirect /livros
            U->>LC: GET /livros
            LC-->>U: lista de livros do usuário
        end
    end
```

---

## RF03 — Criação de Livro

**Descrição:** O usuário autenticado deve poder cadastrar um novo livro informando título, autor, ISBN, editora, ano, gênero e descrição.

| Critério de Aceite | Classe de Teste | Método | Tipo |
|--------------------|----------------|--------|------|
| Criar livro com dono e timestamp | `LivroServicoTest` | `shouldCreateBookWithOwnerAndTimestamp` | Integração |
| Formulário novo livro renderiza | `LivroControllerTest` | `shouldShowNewBookForm` | Caixa Preta |
| POST cria livro e redireciona | `LivroControllerTest` | `shouldCreateBookAndRedirect` | Caixa Preta |
| Criar livros com múltiplos títulos | `LivroServicoTest` | `shouldCreateBooksWithDifferentTitles` | Parametrizado |

### Diagrama UML de Sequência — RF03: Criação de Livro

```mermaid
sequenceDiagram
    actor U as Usuário
    participant C as LivroController
    participant S as LivroServico
    participant R as LivroRepositorio
    participant DB as MongoDB

    U->>C: GET /livros/novo
    C-->>U: formulario.html (livro vazio)
    U->>C: POST /livros (titulo, autor, isbn, ...)
    C->>C: verifica livro.getId() == null
    C->>S: criar(livro, auth.getName())
    S->>S: livro.setCriadoPor(username)
    S->>S: livro.setCriadoEm(LocalDateTime.now())
    S->>R: save(livro)
    R->>DB: insert
    DB-->>R: Livro com ID gerado
    R-->>S: Livro salvo
    S-->>C: Livro salvo
    C-->>U: redirect /livros + "Livro criado com sucesso!"
```

---

## RF04 — Listagem e Busca de Livros

**Descrição:** O usuário deve visualizar apenas seus próprios livros. Deve ser possível filtrar por título (case insensitive).

| Critério de Aceite | Classe de Teste | Método | Tipo |
|--------------------|----------------|--------|------|
| Listar apenas livros do usuário | `LivroServicoTest` | `shouldListOnlyBooksOfCurrentUser` | Integração |
| Buscar por título case-insensitive | `LivroServicoTest` | `shouldSearchBooksByTitleCaseInsensitive` | Integração |
| Retornar todos quando filtro vazio | `LivroServicoTest` | `shouldReturnAllBooksWhenFilterIsBlank` | Caixa Branca |
| Buscar por título no repositório | `LivroRepositorioTest` | `shouldSearchByTitleIgnoreCase` | Integração |
| Listar por dono no repositório | `LivroRepositorioTest` | `shouldFindBooksByOwner` | Integração |
| Página de lista renderiza | `LivroControllerTest` | `shouldListBooksForAuthenticatedUser` | Caixa Preta |
| Busca por título via GET | `LivroControllerTest` | `shouldSearchBooksByTitle` | Caixa Preta |
| Múltiplos usuários isolados | `LivroServicoTest` | `shouldIsolateBooksBetweenUsers` | Parametrizado |

### Diagrama UML de Sequência — RF04: Listagem de Livros

```mermaid
sequenceDiagram
    actor U as Usuário
    participant C as LivroController
    participant S as LivroServico
    participant R as LivroRepositorio
    participant DB as MongoDB

    U->>C: GET /livros?titulo=java (opcional)
    C->>C: auth.getName() → "alice"
    alt titulo é null ou vazio
        C->>S: pesquisarPorTitulo(null, "alice")
        S->>R: findByCriadoPor("alice")
    else titulo informado
        C->>S: pesquisarPorTitulo("java", "alice")
        S->>R: findByTituloContainingIgnoreCaseAndCriadoPor("java", "alice")
    end
    R->>DB: query com filtro
    DB-->>R: List<Livro>
    R-->>S: List<Livro>
    S-->>C: List<Livro>
    C->>C: model.addAttribute("livros", lista)
    C-->>U: lista.html com livros
```

---

## RF05 — Edição de Livro

**Descrição:** O usuário deve poder editar os dados de um livro que lhe pertence. O sistema deve impedir edição de livros de outros usuários.

| Critério de Aceite | Classe de Teste | Método | Tipo |
|--------------------|----------------|--------|------|
| Atualizar campos de livro existente | `LivroServicoTest` | `shouldUpdateExistingBook` | Integração |
| Lançar exceção ao atualizar inexistente | `LivroServicoTest` | `shouldThrowWhenUpdatingNonExistentBook` | Caixa Branca |
| Formulário de edição renderiza | `LivroControllerTest` | `shouldShowEditBookForm` | Caixa Preta |
| Edição redireciona após salvar | `LivroControllerTest` | `shouldUpdateBookAndRedirect` | Caixa Preta |
| Acesso negado a livro de outro usuário | `LivroControllerTest` | `shouldDenyEditAccessToOtherUsersBook` | Caixa Preta |

### Diagrama UML de Sequência — RF05: Edição de Livro

```mermaid
sequenceDiagram
    actor U as Usuário
    participant C as LivroController
    participant S as LivroServico
    participant R as LivroRepositorio
    participant DB as MongoDB

    U->>C: GET /livros/{id}/editar
    C->>S: encontrarPorId(id)
    S->>R: findById(id)
    R->>DB: query por ID
    DB-->>R: Optional<Livro>
    alt Livro não encontrado
        R-->>S: Optional.empty()
        S-->>C: Optional.empty()
        C-->>U: throw IllegalArgumentException → erro.html (400)
    else Livro encontrado
        R-->>S: Optional<Livro>
        S-->>C: Optional<Livro>
        C->>C: livro.getCriadoPor().equals(auth.getName())?
        alt Livro de outro usuário
            C-->>U: throw IllegalArgumentException → erro.html (400)
        else Livro do usuário
            C-->>U: formulario.html com dados do livro
            U->>C: POST /livros (id, titulo, autor, ...)
            C->>S: atualizar(id, livroAtualizado)
            S->>R: findById(id)
            R->>DB: query
            DB-->>R: Livro existente
            S->>S: copia campos (mantém criadoPor)
            S->>R: save(livroAtualizado)
            R->>DB: update
            DB-->>R: Livro atualizado
            R-->>S: Livro
            S-->>C: Livro atualizado
            C-->>U: redirect /livros + "Livro atualizado com sucesso!"
        end
    end
```

---

## RF06 — Exclusão de Livro

**Descrição:** O usuário deve poder excluir um livro que lhe pertence. O sistema deve impedir exclusão de livros de outros usuários.

| Critério de Aceite | Classe de Teste | Método | Tipo |
|--------------------|----------------|--------|------|
| Excluir livro por ID | `LivroServicoTest` | `shouldDeleteBook` | Integração |
| Excluir via repositório | `LivroRepositorioTest` | `shouldDeleteBook` | Integração |
| DELETE via controller redireciona | `LivroControllerTest` | `shouldDeleteBookAndRedirect` | Caixa Preta |
| Negar exclusão de livro de outro usuário | `LivroControllerTest` | `shouldDenyDeleteAccessToOtherUsersBook` | Caixa Preta |

### Diagrama UML de Sequência — RF06: Exclusão de Livro

```mermaid
sequenceDiagram
    actor U as Usuário
    participant C as LivroController
    participant S as LivroServico
    participant R as LivroRepositorio
    participant DB as MongoDB

    U->>C: POST /livros/{id}/deletar
    C->>S: encontrarPorId(id)
    S->>R: findById(id)
    R->>DB: query por ID
    DB-->>R: Optional<Livro>
    alt Livro não encontrado
        C-->>U: redirect /livros + erro "não encontrado"
    else Livro de outro usuário
        C->>C: livro.getCriadoPor() != auth.getName()
        C-->>U: redirect /livros + erro "Acesso negado"
    else Livro do usuário
        C->>S: deletar(id)
        S->>R: deleteById(id)
        R->>DB: delete
        DB-->>R: ok
        C-->>U: redirect /livros + "Livro deletado com sucesso!"
    end
```

---

## RF07 — Persistência por ISBN

**Descrição:** O repositório deve suportar consultas por ISBN, garantindo unicidade por usuário.

| Critério de Aceite | Classe de Teste | Método | Tipo |
|--------------------|----------------|--------|------|
| Salvar e recuperar livro por ID | `LivroRepositorioTest` | `shouldSaveAndFindBookById` | Integração |
| Verificar existência por ISBN e dono | `LivroRepositorioTest` | `shouldCheckExistenceByIsbnAndOwner` | Integração |
| Encontrar livro por ID (serviço) | `LivroServicoTest` | `shouldFindBookById` | Integração |

---

## RF08 — Inicialização do Contexto

**Descrição:** O contexto completo do Spring Boot deve carregar corretamente com todas as configurações.

| Critério de Aceite | Classe de Teste | Método | Tipo |
|--------------------|----------------|--------|------|
| Contexto Spring carrega com MongoDB | `ProjetoApplicationTests` | `contextLoads` | Integração |

---

## 📊 Resumo de Cobertura por Requisito

| Requisito | Testes Unitários/Integração | Testes E2E/Controller | Parametrizados | Total |
|-----------|----------------------------|-----------------------|----------------|-------|
| RF01 — Cadastro de Usuário | 4 | 4 | 1 | **9** |
| RF02 — Autenticação | 1 | 3 | 0 | **4** |
| RF03 — Criação de Livro | 1 | 2 | 1 | **4** |
| RF04 — Listagem e Busca | 5 | 2 | 1 | **8** |
| RF05 — Edição de Livro | 2 | 3 | 0 | **5** |
| RF06 — Exclusão de Livro | 2 | 2 | 0 | **4** |
| RF07 — Persistência ISBN | 3 | 0 | 0 | **3** |
| RF08 — Contexto | 1 | 0 | 0 | **1** |
| **TOTAL** | **19** | **16** | **3** | **38** |

> ✅ **100% dos requisitos funcionais possuem pelo menos um teste mapeado.**
