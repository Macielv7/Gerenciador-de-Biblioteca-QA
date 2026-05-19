package com.example.projeto;

import com.example.projeto.model.Livro;
import com.example.projeto.repository.LivroRepositorio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração do repositório usando um MongoDB real via Testcontainers.
 * Cada teste começa com o banco limpo (deleteAll no @BeforeEach).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class LivroRepositorioTest {

    // @ServiceConnection substitui o @DynamicPropertySource de forma mais simples
    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @Autowired
    private LivroRepositorio livroRepositorio;

    @BeforeEach
    void setUp() {
        livroRepositorio.deleteAll();
    }

    @Test
    @DisplayName("Deve salvar e recuperar um livro por ID")
    void shouldSaveAndFindBookById() {
        Livro livro = Livro.builder()
                .titulo("Clean Code")
                .autor("Robert C. Martin")
                .isbn("9780132350884")
                .criadoPor("alice")
                .criadoEm(LocalDateTime.now())
                .build();

        Livro salvo = livroRepositorio.save(livro);

        Optional<Livro> encontrado = livroRepositorio.findById(salvo.getId());
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getTitulo()).isEqualTo("Clean Code");
        assertThat(encontrado.get().getAutor()).isEqualTo("Robert C. Martin");
    }

    @Test
    @DisplayName("Deve listar livros pelo dono")
    void shouldFindBooksByOwner() {
        livroRepositorio.save(Livro.builder().titulo("Livro A").criadoPor("alice").criadoEm(LocalDateTime.now()).build());
        livroRepositorio.save(Livro.builder().titulo("Livro B").criadoPor("alice").criadoEm(LocalDateTime.now()).build());
        livroRepositorio.save(Livro.builder().titulo("Livro C").criadoPor("bob").criadoEm(LocalDateTime.now()).build());

        List<Livro> livrosAlice = livroRepositorio.findByCriadoPor("alice");
        assertThat(livrosAlice).hasSize(2);
        assertThat(livrosAlice).extracting(Livro::getCriadoPor).containsOnly("alice");
    }

    @Test
    @DisplayName("Deve buscar livros pelo título (case insensitive)")
    void shouldSearchByTitleIgnoreCase() {
        livroRepositorio.save(Livro.builder().titulo("Design Patterns").criadoPor("alice").criadoEm(LocalDateTime.now()).build());
        livroRepositorio.save(Livro.builder().titulo("Domain Driven Design").criadoPor("alice").criadoEm(LocalDateTime.now()).build());
        livroRepositorio.save(Livro.builder().titulo("Refactoring").criadoPor("alice").criadoEm(LocalDateTime.now()).build());

        List<Livro> resultados = livroRepositorio.findByTituloContainingIgnoreCaseAndCriadoPor("design", "alice");
        assertThat(resultados).hasSize(2);
        assertThat(resultados).extracting(Livro::getTitulo)
                .containsExactlyInAnyOrder("Design Patterns", "Domain Driven Design");
    }

    @Test
    @DisplayName("Deve excluir um livro por ID")
    void shouldDeleteBook() {
        Livro salvo = livroRepositorio.save(
                Livro.builder().titulo("Para excluir").criadoPor("alice").criadoEm(LocalDateTime.now()).build());

        livroRepositorio.deleteById(salvo.getId());

        assertThat(livroRepositorio.findById(salvo.getId())).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar existência por ISBN e dono")
    void shouldCheckExistenceByIsbnAndOwner() {
        livroRepositorio.save(Livro.builder().titulo("Livro X").isbn("1234567890").criadoPor("alice").criadoEm(LocalDateTime.now()).build());

        assertThat(livroRepositorio.existsByIsbnAndCriadoPor("1234567890", "alice")).isTrue();
        assertThat(livroRepositorio.existsByIsbnAndCriadoPor("1234567890", "bob")).isFalse();
        assertThat(livroRepositorio.existsByIsbnAndCriadoPor("0000000000", "alice")).isFalse();
    }
}
