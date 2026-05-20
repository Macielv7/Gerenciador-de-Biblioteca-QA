package com.example.projeto.service;

import com.example.projeto.model.Livro;
import com.example.projeto.repository.LivroRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LivroServico {

    private final LivroRepositorio livroRepositorio;

    public Livro criar(Livro livro, String criadoPor) {
        livro.setId(null);
        livro.setCriadoPor(criadoPor);
        livro.setCriadoEm(LocalDateTime.now());
        return livroRepositorio.save(livro);
    }

    public Livro atualizar(String id, Livro atualizado) {
        Livro existente = encontrarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Livro não encontrado: " + id));
        existente.setTitulo(atualizado.getTitulo());
        existente.setAutor(atualizado.getAutor());
        existente.setIsbn(atualizado.getIsbn());
        existente.setEditora(atualizado.getEditora());
        existente.setAno(atualizado.getAno());
        existente.setGenero(atualizado.getGenero());
        existente.setDescricao(atualizado.getDescricao());
        existente.setUrlCapa(atualizado.getUrlCapa());
        return livroRepositorio.save(existente);
    }

    public void deletar(String id) {
        livroRepositorio.deleteById(id);
    }

    public Optional<Livro> encontrarPorId(String id) {
        return livroRepositorio.findById(id);
    }

    public List<Livro> encontrarTodosPorUsuario(String criadoPor) {
        return livroRepositorio.findByCriadoPor(criadoPor);
    }

    public List<Livro> pesquisarPorTitulo(String titulo, String criadoPor) {
        if (titulo == null || titulo.isBlank()) {
            return encontrarTodosPorUsuario(criadoPor);
        }
        return livroRepositorio.findByTituloContainingIgnoreCaseAndCriadoPor(titulo, criadoPor);
    }
}
