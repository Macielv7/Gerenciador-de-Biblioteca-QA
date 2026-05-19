package com.example.projeto.repository;

import com.example.projeto.model.Livro;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LivroRepositorio extends MongoRepository<Livro, String> {

    List<Livro> findByCriadoPor(String criadoPor);

    List<Livro> findByTituloContainingIgnoreCaseAndCriadoPor(String titulo, String criadoPor);

    Optional<Livro> findByIsbn(String isbn);

    boolean existsByIsbnAndCriadoPor(String isbn, String criadoPor);
}
