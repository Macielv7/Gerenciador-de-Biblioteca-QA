package com.example.projeto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "livros")
public class Livro {

    @Id
    private String id;

    private String titulo;
    private String autor;

    @Indexed
    private String isbn;

    private String editora;
    private Integer ano;
    private String genero;
    private String descricao;
    private String urlCapa;

    @Indexed
    private String criadoPor;

    private LocalDateTime criadoEm;
}
