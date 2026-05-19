package com.example.projeto.controller;

import com.example.projeto.service.UsuarioServico;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioServico usuarioServico;

    @GetMapping("/register")
    public String mostrarFormularioRegistro() {
        return "usuarios/registro";
    }

    @PostMapping("/register")
    public String registrarUsuario(@RequestParam String usuario,
                                   @RequestParam String email,
                                   @RequestParam String senha,
                                   @RequestParam String confirmarSenha,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Validar se as senhas coincidem
            if (!senha.equals(confirmarSenha)) {
                redirectAttributes.addFlashAttribute("erro", "As senhas não coincidem");
                return "redirect:/register";
            }

            // Validar tamanho mínimo da senha
            if (senha.length() < 6) {
                redirectAttributes.addFlashAttribute("erro", "A senha deve ter pelo menos 6 caracteres");
                return "redirect:/register";
            }

            usuarioServico.registrarUsuario(usuario, email, senha);
            redirectAttributes.addFlashAttribute("mensagem", "Usuário registrado com sucesso! Faça o login.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/register";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao registrar usuário: " + e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(required = false) String error,
                               @RequestParam(required = false) String logout,
                               Model model) {
        if (error != null) {
            model.addAttribute("erro", "Usuário ou senha inválidos");
        }
        if (logout != null) {
            model.addAttribute("mensagem", "Você foi desconectado com sucesso");
        }
        return "login";
    }
}