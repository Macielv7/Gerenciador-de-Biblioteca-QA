package com.example.projeto;

import com.example.projeto.repository.UsuarioRepositorio;
import com.example.projeto.service.UsuarioServico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de Caixa Preta (E2E) do UsuarioController.
 * Exercita os fluxos de registro e login via requisições HTTP reais.
 * Sem Mocks — usa MongoDB real via Testcontainers.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
class UsuarioControllerTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private UsuarioServico usuarioServico;

    @BeforeEach
    void setUp() {
        usuarioRepositorio.deleteAll();
    }

    // ──────────────────────────────────────────────────────────
    // RF02 — Página de Login
    // ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("[Caixa Preta] Deve exibir a página de login")
    void shouldShowLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("[Caixa Preta] Deve exibir mensagem de erro no login inválido")
    void shouldShowLoginPageWithError() throws Exception {
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("erro"));
    }

    @Test
    @DisplayName("[Caixa Preta] Deve exibir mensagem de logout bem-sucedido")
    void shouldShowLoginPageAfterLogout() throws Exception {
        mockMvc.perform(get("/login").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("mensagem"));
    }

    // ──────────────────────────────────────────────────────────
    // RF01 — Formulário de Registro
    // ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("[Caixa Preta] Deve exibir formulário de registro")
    void shouldShowRegistrationForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/registro"));
    }

    @Test
    @DisplayName("[Caixa Preta] Deve registrar usuário com dados válidos e redirecionar para login")
    void shouldRegisterUserAndRedirectToLogin() throws Exception {
        mockMvc.perform(post("/register")
                        .param("usuario", "novouser")
                        .param("email", "novo@email.com")
                        .param("senha", "senha123")
                        .param("confirmarSenha", "senha123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("mensagem"));
    }

    @Test
    @DisplayName("[Caixa Preta] Deve rejeitar registro quando senhas não coincidem")
    void shouldRejectRegistrationWhenPasswordsMismatch() throws Exception {
        mockMvc.perform(post("/register")
                        .param("usuario", "teste")
                        .param("email", "teste@email.com")
                        .param("senha", "senha123")
                        .param("confirmarSenha", "diferente")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("erro"));
    }

    @Test
    @DisplayName("[Caixa Preta] Deve rejeitar registro com username já existente")
    void shouldRejectRegistrationWithDuplicateUsername() throws Exception {
        usuarioServico.registrarUsuario("existente", "exist@email.com", "senha123");

        mockMvc.perform(post("/register")
                        .param("usuario", "existente")
                        .param("email", "outro@email.com")
                        .param("senha", "senha123")
                        .param("confirmarSenha", "senha123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attributeExists("erro"));
    }

    // ──────────────────────────────────────────────────────────
    // RF01 — Parametrizado: validação de senha curta
    // ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("[Parametrizado] Deve rejeitar senhas com menos de 6 caracteres")
    void shouldRejectShortPasswords() throws Exception {
        String[] senhasCurtas = {"", "1", "12", "123", "1234", "12345"};

        for (String senha : senhasCurtas) {
            mockMvc.perform(post("/register")
                            .param("usuario", "usuario_" + senha.length())
                            .param("email", "user" + senha.length() + "@email.com")
                            .param("senha", senha)
                            .param("confirmarSenha", senha)
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/register"))
                    .andExpect(flash().attributeExists("erro"));
        }
    }
}
