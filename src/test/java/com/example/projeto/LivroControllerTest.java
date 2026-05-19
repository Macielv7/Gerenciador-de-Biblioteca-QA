package com.example.projeto;

import com.example.projeto.model.Livro;
import com.example.projeto.repository.LivroRepositorio;
import com.example.projeto.repository.UsuarioRepositorio;
import com.example.projeto.service.UsuarioServico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de Caixa Preta (E2E) do LivroController.
 * Exercita as requisições HTTP completas usando MockMvc com autenticação real
 * e um MongoDB real via Testcontainers. Sem Mocks.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class LivroControllerTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LivroRepositorio livroRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private UsuarioServico usuarioServico;

    @BeforeEach
    void setUp() {
        livroRepositorio.deleteAll();
        usuarioRepositorio.deleteAll();
        usuarioServico.registrarUsuario("alice", "alice@test.com", "senha123");
        usuarioServico.registrarUsuario("bob", "bob@test.com", "senha123");
    }

    // ──────────────────────────────────────────────────────────
    // RF02 — Autenticação: rota protegida redireciona anônimo
    // ──────────────────────────────────────────────────────────

    @Test
    @WithAnonymousUser
    @DisplayName("[Caixa Preta] Usuário não autenticado deve ser redirecionado para login")
    void shouldRedirectUnauthenticatedUserToLogin() throws Exception {
        mockMvc.perform(get("/livros"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // ──────────────────────────────────────────────────────────
    // RF04 — Listagem de livros
    // ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("[Caixa Preta] Usuário autenticado deve ver a lista de livros")
    void shouldListBooksForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/livros").with(user("alice").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("livros/lista"))
                .andExpect(model().attributeExists("livros"));
    }

    @Test
    @DisplayName("[Caixa Preta] Deve buscar livros por título via parâmetro GET")
    void shouldSearchBooksByTitle() throws Exception {
        mockMvc.perform(get("/livros")
                        .param("titulo", "Java")
                        .with(user("alice").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("livros/lista"))
                .andExpect(model().attributeExists("livros"))
                .andExpect(model().attribute("titulo", "Java"));
    }

    // ──────────────────────────────────────────────────────────
    // RF03 — Criação de livro
    // ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("[Caixa Preta] Deve exibir formulário de novo livro")
    void shouldShowNewBookForm() throws Exception {
        mockMvc.perform(get("/livros/novo").with(user("alice").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("livros/formulario"))
                .andExpect(model().attributeExists("livro"));
    }

    @Test
    @DisplayName("[Caixa Preta] Deve criar livro via POST e redirecionar para lista")
    void shouldCreateBookAndRedirect() throws Exception {
        mockMvc.perform(post("/livros")
                        .param("titulo", "Clean Code")
                        .param("autor", "Robert C. Martin")
                        .param("isbn", "9780132350884")
                        .with(user("alice").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"))
                .andExpect(flash().attributeExists("mensagem"));
    }

    // ──────────────────────────────────────────────────────────
    // RF05 — Edição de livro
    // ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("[Caixa Preta] Deve exibir formulário de edição para o dono do livro")
    void shouldShowEditBookForm() throws Exception {
        Livro livro = livroRepositorio.save(
                Livro.builder().titulo("Meu Livro").autor("Autor").criadoPor("alice").build());

        mockMvc.perform(get("/livros/" + livro.getId() + "/editar")
                        .with(user("alice").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("livros/formulario"))
                .andExpect(model().attributeExists("livro"));
    }

    @Test
    @DisplayName("[Caixa Preta] Deve negar acesso à edição de livro de outro usuário")
    void shouldDenyEditAccessToOtherUsersBook() throws Exception {
        Livro livroBob = livroRepositorio.save(
                Livro.builder().titulo("Livro do Bob").autor("Bob").criadoPor("bob").build());

        mockMvc.perform(get("/livros/" + livroBob.getId() + "/editar")
                        .with(user("alice").roles("USER")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[Caixa Preta] Deve atualizar livro via POST e redirecionar")
    void shouldUpdateBookAndRedirect() throws Exception {
        Livro livro = livroRepositorio.save(
                Livro.builder().titulo("Título Original").autor("Autor").criadoPor("alice").build());

        mockMvc.perform(post("/livros")
                        .param("id", livro.getId())
                        .param("titulo", "Título Atualizado")
                        .param("autor", "Novo Autor")
                        .with(user("alice").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"))
                .andExpect(flash().attributeExists("mensagem"));
    }

    // ──────────────────────────────────────────────────────────
    // RF06 — Exclusão de livro
    // ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("[Caixa Preta] Deve deletar livro via POST e redirecionar")
    void shouldDeleteBookAndRedirect() throws Exception {
        Livro livro = livroRepositorio.save(
                Livro.builder().titulo("Para Deletar").autor("Autor").criadoPor("alice").build());

        mockMvc.perform(post("/livros/" + livro.getId() + "/deletar")
                        .with(user("alice").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/livros"))
                .andExpect(flash().attributeExists("mensagem"));
    }

    @Test
    @DisplayName("[Caixa Preta] Deve negar exclusão de livro de outro usuário")
    void shouldDenyDeleteAccessToOtherUsersBook() throws Exception {
        Livro livroBob = livroRepositorio.save(
                Livro.builder().titulo("Livro do Bob").autor("Bob").criadoPor("bob").build());

        mockMvc.perform(post("/livros/" + livroBob.getId() + "/deletar")
                        .with(user("alice").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("erro"));
    }
}
