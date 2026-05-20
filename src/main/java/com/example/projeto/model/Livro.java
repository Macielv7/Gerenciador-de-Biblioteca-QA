package com.example.projeto.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "O titulo e obrigatorio")
    @Size(max = 120, message = "O titulo deve ter no maximo 120 caracteres")
    private String titulo;

    @NotBlank(message = "O autor e obrigatorio")
    @Size(max = 100, message = "O autor deve ter no maximo 100 caracteres")
    private String autor;

    @Indexed
    private String isbn;

    @Size(max = 100, message = "A editora deve ter no maximo 100 caracteres")
    private String editora;

    @Min(value = 1000, message = "O ano deve ser maior ou igual a 1000")
    @Max(value = 2030, message = "O ano deve ser menor ou igual a 2030")
    private Integer ano;

    @Size(max = 50, message = "O genero deve ter no maximo 50 caracteres")
    private String genero;

    @Size(max = 1000, message = "A descricao deve ter no maximo 1000 caracteres")
    private String descricao;

    @Size(max = 500, message = "A URL da capa deve ter no maximo 500 caracteres")
    private String urlCapa;

    @Indexed
    private String criadoPor;

    private LocalDateTime criadoEm;
}
