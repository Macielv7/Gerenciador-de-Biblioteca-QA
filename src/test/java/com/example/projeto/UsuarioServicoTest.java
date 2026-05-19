package com.example.projeto;

import com.example.projeto.model.Usuario;
import com.example.projeto.repository.UsuarioRepositorio;
import com.example.projeto.service.UsuarioServico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes de integração do serviço de usuários usando um MongoDB real via Testcontainers.
 * Verifica o registro, validações de duplicidade e carregamento pelo Spring Security.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class UsuarioServicoTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @Autowired
    private UsuarioServico usuarioServico;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @BeforeEach
    void setUp() {
        usuarioRepositorio.deleteAll();
    }

    @Test
    @DisplayName("Deve registrar um novo usuário com senha codificada")
    void shouldRegisterUserWithEncodedPassword() {
        Usuario salvo = usuarioServico.registrarUsuario("alice", "alice@email.com", "senha123");

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getUsuario()).isEqualTo("alice");
        assertThat(salvo.getEmail()).isEqualTo("alice@email.com");
        assertThat(salvo.getSenha()).isNotEqualTo("senha123"); // deve estar codificada
        assertThat(salvo.getSenha()).startsWith("$2a$"); // BCrypt prefix
        assertThat(salvo.getPapeis()).contains("ROLE_USER");
    }

    @Test
    @DisplayName("Deve lançar exceção ao registrar username duplicado")
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        usuarioServico.registrarUsuario("bob", "bob@email.com", "senha123");

        assertThatThrownBy(() -> usuarioServico.registrarUsuario("bob", "outro@email.com", "senha456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuário já existe");
    }

    @Test
    @DisplayName("Deve lançar exceção ao registrar e-mail duplicado")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        usuarioServico.registrarUsuario("carlos", "shared@email.com", "senha123");

        assertThatThrownBy(() -> usuarioServico.registrarUsuario("diana", "shared@email.com", "senha456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("E-mail já cadastrado");
    }

    @Test
    @DisplayName("Deve persistir usuário no banco MongoDB real")
    void shouldPersistUserInMongoDB() {
        usuarioServico.registrarUsuario("eve", "eve@email.com", "segura99");

        Optional<Usuario> encontrado = usuarioRepositorio.findByUsuario("eve");
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getEmail()).isEqualTo("eve@email.com");
    }

    @Test
    @DisplayName("Deve carregar UserDetails para Spring Security")
    void shouldLoadUserDetailsByUsername() {
        usuarioServico.registrarUsuario("frank", "frank@email.com", "pass123");

        var detalhes = usuarioServico.loadUserByUsername("frank");
        assertThat(detalhes.getUsername()).isEqualTo("frank");
        assertThat(detalhes.getAuthorities()).extracting("authority").contains("ROLE_USER");
    }
}
