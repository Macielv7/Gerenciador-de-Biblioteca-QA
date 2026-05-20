package com.example.projeto;

import com.example.projeto.model.Livro;
import com.example.projeto.repository.LivroRepositorio;
import com.example.projeto.service.LivroServico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes de integração do serviço de livros usando um MongoDB real via Testcontainers.
 * Verifica a lógica de negócio (criar, atualizar, deletar, pesquisar).
 * Inclui testes parametrizados para múltiplos cenários.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class LivroServicoTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @Autowired
    private LivroServico livroServico;

    @Autowired
    private LivroRepositorio livroRepositorio;

    @BeforeEach
    void setUp() {
        livroRepositorio.deleteAll();
    }

    @Test
    @DisplayName("Deve criar livro com usuário e timestamp")
    void shouldCreateBookWithOwnerAndTimestamp() {
        Livro livro = Livro.builder()
                .titulo("Effective Java")
                .autor("Joshua Bloch")
                .isbn("9780134685991")
                .build();

        Livro salvo = livroServico.criar(livro, "alice");

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getCriadoPor()).isEqualTo("alice");
        assertThat(salvo.getCriadoEm()).isNotNull();
    }

    @Test
    @DisplayName("Deve gerar ID novo quando formulario envia ID vazio")
    void shouldGenerateIdWhenFormSendsEmptyId() {
        Livro livro = Livro.builder()
                .id("")
                .titulo("Livro com ID vazio")
                .autor("Autor")
                .build();

        Livro salvo = livroServico.criar(livro, "alice");

        assertThat(salvo.getId()).isNotBlank();
    }

    @Test
    @DisplayName("Deve atualizar campos de um livro existente")
    void shouldUpdateExistingBook() {
        Livro original = livroServico.criar(
                Livro.builder().titulo("Título Original").autor("Autor A").build(), "alice");

        Livro atualizado = Livro.builder()
                .titulo("Título Atualizado")
                .autor("Autor B")
                .genero("Técnico")
                .build();

        Livro resultado = livroServico.atualizar(original.getId(), atualizado);

        assertThat(resultado.getTitulo()).isEqualTo("Título Atualizado");
        assertThat(resultado.getAutor()).isEqualTo("Autor B");
        assertThat(resultado.getGenero()).isEqualTo("Técnico");
        assertThat(resultado.getCriadoPor()).isEqualTo("alice"); // owner não muda
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar livro inexistente")
    void shouldThrowWhenUpdatingNonExistentBook() {
        Livro atualizado = Livro.builder().titulo("Novo").build();

        assertThatThrownBy(() -> livroServico.atualizar("id-inexistente", atualizado))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Livro não encontrado");
    }

    @Test
    @DisplayName("Deve excluir livro por ID")
    void shouldDeleteBook() {
        Livro salvo = livroServico.criar(Livro.builder().titulo("Para deletar").build(), "alice");

        livroServico.deletar(salvo.getId());

        assertThat(livroRepositorio.findById(salvo.getId())).isEmpty();
    }

    @Test
    @DisplayName("Deve listar apenas livros do usuário logado")
    void shouldListOnlyBooksOfCurrentUser() {
        livroServico.criar(Livro.builder().titulo("Livro 1").build(), "alice");
        livroServico.criar(Livro.builder().titulo("Livro 2").build(), "alice");
        livroServico.criar(Livro.builder().titulo("Livro 3").build(), "bob");

        List<Livro> livrosAlice = livroServico.encontrarTodosPorUsuario("alice");

        assertThat(livrosAlice).hasSize(2);
        assertThat(livrosAlice).extracting(Livro::getCriadoPor).containsOnly("alice");
    }

    @Test
    @DisplayName("Deve buscar livros por título (case insensitive)")
    void shouldSearchBooksByTitleCaseInsensitive() {
        livroServico.criar(Livro.builder().titulo("The Pragmatic Programmer").build(), "alice");
        livroServico.criar(Livro.builder().titulo("Programming Pearls").build(), "alice");
        livroServico.criar(Livro.builder().titulo("Clean Code").build(), "alice");

        List<Livro> resultados = livroServico.pesquisarPorTitulo("program", "alice");

        assertThat(resultados).hasSize(2);
        assertThat(resultados).extracting(Livro::getTitulo)
                .containsExactlyInAnyOrder("The Pragmatic Programmer", "Programming Pearls");
    }

    @Test
    @DisplayName("Deve retornar todos os livros quando filtro vazio")
    void shouldReturnAllBooksWhenFilterIsBlank() {
        livroServico.criar(Livro.builder().titulo("Livro A").build(), "alice");
        livroServico.criar(Livro.builder().titulo("Livro B").build(), "alice");

        List<Livro> resultados = livroServico.pesquisarPorTitulo("", "alice");

        assertThat(resultados).hasSize(2);
    }

    @Test
    @DisplayName("Deve encontrar livro por ID")
    void shouldFindBookById() {
        Livro salvo = livroServico.criar(Livro.builder().titulo("Específico").build(), "alice");

        Optional<Livro> encontrado = livroServico.encontrarPorId(salvo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getTitulo()).isEqualTo("Específico");
    }

    // ──────────────────────────────────────────────────────────
    // Testes Parametrizados
    // ──────────────────────────────────────────────────────────

    @ParameterizedTest(name = "Criar livro com título=\"{0}\" e autor=\"{1}\"")
    @CsvSource({
            "Clean Code, Robert C. Martin",
            "Effective Java, Joshua Bloch",
            "The Pragmatic Programmer, David Thomas",
            "Design Patterns, Gang of Four",
            "Domain-Driven Design, Eric Evans"
    })
    @DisplayName("[Parametrizado] Deve criar livros com diferentes títulos e autores")
    void shouldCreateBooksWithDifferentTitles(String titulo, String autor) {
        Livro livro = Livro.builder().titulo(titulo).autor(autor).build();

        Livro salvo = livroServico.criar(livro, "alice");

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getTitulo()).isEqualTo(titulo);
        assertThat(salvo.getAutor()).isEqualTo(autor);
        assertThat(salvo.getCriadoPor()).isEqualTo("alice");
    }

    @ParameterizedTest(name = "Busca por \"{0}\" deve retornar {1} livros")
    @CsvSource({
            "clean, 1",
            "java, 1",
            "prog, 2",
            "code, 1",
            "xyz, 0"
    })
    @DisplayName("[Parametrizado] Deve retornar quantidade correta para diferentes termos de busca")
    void shouldReturnCorrectCountForSearchTerms(String termo, int esperado) {
        livroServico.criar(Livro.builder().titulo("Clean Code").build(), "alice");
        livroServico.criar(Livro.builder().titulo("Effective Java").build(), "alice");
        livroServico.criar(Livro.builder().titulo("Programming Pearls").build(), "alice");
        livroServico.criar(Livro.builder().titulo("Pragmatic Programmer").build(), "alice");

        List<Livro> resultados = livroServico.pesquisarPorTitulo(termo, "alice");

        assertThat(resultados).hasSize(esperado);
    }

    @ParameterizedTest(name = "Usuário \"{0}\" deve ter acesso isolado aos seus livros")
    @ValueSource(strings = {"alice", "bob", "carlos", "diana"})
    @DisplayName("[Parametrizado] Deve isolar livros entre diferentes usuários")
    void shouldIsolateBooksBetweenUsers(String usuario) {
        livroServico.criar(Livro.builder().titulo("Livro de " + usuario).build(), usuario);
        livroServico.criar(Livro.builder().titulo("Livro compartilhado").build(), "outro_user");

        List<Livro> livros = livroServico.encontrarTodosPorUsuario(usuario);

        assertThat(livros).hasSize(1);
        assertThat(livros.get(0).getCriadoPor()).isEqualTo(usuario);
    }

    @ParameterizedTest(name = "pesquisarPorTitulo com filtro nulo/branco: \"{0}\"")
    @ValueSource(strings = {"", " ", "  "})
    @DisplayName("[Parametrizado / Caixa Branca] Deve retornar todos os livros para qualquer filtro em branco")
    void shouldReturnAllBooksForBlankFilters(String filtro) {
        livroServico.criar(Livro.builder().titulo("Livro 1").build(), "alice");
        livroServico.criar(Livro.builder().titulo("Livro 2").build(), "alice");

        List<Livro> resultados = livroServico.pesquisarPorTitulo(filtro, "alice");

        assertThat(resultados).hasSize(2);
    }
}
