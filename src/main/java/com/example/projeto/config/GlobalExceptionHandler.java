package com.example.projeto.config;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Tratamento global de exceções para retornar páginas de erro amigáveis
 * em vez de stack traces genéricos do Spring Boot.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata erros de validação de negócio (ex: livro não encontrado, acesso negado).
     * Retorna HTTP 400 com mensagem amigável.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        model.addAttribute("titulo", "Erro na Requisição");
        model.addAttribute("mensagem", ex.getMessage());
        model.addAttribute("status", 400);
        return "erro";
    }

    /**
     * Trata qualquer outra exceção não prevista.
     * Retorna HTTP 500 com mensagem genérica.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("titulo", "Erro Interno");
        model.addAttribute("mensagem", "Ocorreu um erro inesperado. Tente novamente mais tarde.");
        model.addAttribute("status", 500);
        return "erro";
    }
}
