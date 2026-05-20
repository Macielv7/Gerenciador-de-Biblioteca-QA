package com.example.projeto.controller;

import com.example.projeto.model.Livro;
import com.example.projeto.service.LivroServico;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/livros")
@RequiredArgsConstructor
public class LivroController {

    private final LivroServico livroServico;

    @GetMapping
    public String listarLivros(@RequestParam(required = false) String titulo, Model model, Authentication auth) {
        String usuario = auth.getName();
        model.addAttribute("livros", livroServico.pesquisarPorTitulo(titulo, usuario));
        model.addAttribute("titulo", titulo);
        return "livros/lista";
    }

    @GetMapping("/novo")
    public String novoLivro(Model model) {
        model.addAttribute("livro", new Livro());
        model.addAttribute("titulo", "Novo Livro");
        return "livros/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editarLivro(@PathVariable String id, Model model, Authentication auth) {
        return abrirFormularioEdicao(id, model, auth);
    }

    @GetMapping("/sem-id/editar")
    public String editarLivroSemId(Model model, Authentication auth) {
        return abrirFormularioEdicao("", model, auth);
    }

    private String abrirFormularioEdicao(String id, Model model, Authentication auth) {
        Livro livro = livroServico.encontrarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Livro nao encontrado: " + id));

        if (!auth.getName().equals(livro.getCriadoPor())) {
            throw new IllegalArgumentException("Acesso negado");
        }

        model.addAttribute("livro", livro);
        model.addAttribute("titulo", "Editar Livro");
        return "livros/formulario";
    }

    @PostMapping
    public String salvarLivro(@Valid @ModelAttribute Livro livro,
                              BindingResult bindingResult,
                              Model model,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("titulo", livro.getId() == null || livro.getId().isEmpty() ? "Novo Livro" : "Editar Livro");
            return "livros/formulario";
        }

        try {
            if (livro.getId() == null || livro.getId().isEmpty()) {
                livroServico.criar(livro, auth.getName());
                redirectAttributes.addFlashAttribute("mensagem", "Livro criado com sucesso!");
            } else {
                livroServico.atualizar(livro.getId(), livro);
                redirectAttributes.addFlashAttribute("mensagem", "Livro atualizado com sucesso!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao salvar livro: " + e.getMessage());
            return "redirect:/livros/novo";
        }
        return "redirect:/livros";
    }

    @PostMapping("/{id}/deletar")
    public String deletarLivro(@PathVariable String id, Authentication auth, RedirectAttributes redirectAttributes) {
        return deletarLivroPorId(id, auth, redirectAttributes);
    }

    @PostMapping("/sem-id/deletar")
    public String deletarLivroSemId(Authentication auth, RedirectAttributes redirectAttributes) {
        return deletarLivroPorId("", auth, redirectAttributes);
    }

    private String deletarLivroPorId(String id, Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            Livro livro = livroServico.encontrarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Livro nao encontrado: " + id));

            if (!auth.getName().equals(livro.getCriadoPor())) {
                throw new IllegalArgumentException("Acesso negado");
            }

            livroServico.deletar(id);
            redirectAttributes.addFlashAttribute("mensagem", "Livro deletado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao deletar livro: " + e.getMessage());
        }
        return "redirect:/livros";
    }
}
