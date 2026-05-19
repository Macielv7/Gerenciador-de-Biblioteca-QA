package com.example.projeto.service;

import com.example.projeto.model.Usuario;
import com.example.projeto.repository.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServico implements UserDetailsService {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public Usuario registrarUsuario(String usuario, String email, String senha) {
        if (usuarioRepositorio.existsByUsuario(usuario)) {
            throw new IllegalArgumentException("Usuário já existe: " + usuario);
        }
        if (usuarioRepositorio.existsByEmail(email)) {
            throw new IllegalArgumentException("E-mail já cadastrado: " + email);
        }

        Usuario novoUsuario = Usuario.builder()
                .usuario(usuario)
                .email(email)
                .senha(passwordEncoder.encode(senha))
                .criadoEm(LocalDateTime.now())
                .build();

        return usuarioRepositorio.save(novoUsuario);
    }

    @Override
    public UserDetails loadUserByUsername(String usuario) throws UsernameNotFoundException {
        Usuario usuarioEncontrado = usuarioRepositorio.findByUsuario(usuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + usuario));

        return org.springframework.security.core.userdetails.User.builder()
                .username(usuarioEncontrado.getUsuario())
                .password(usuarioEncontrado.getSenha())
                .authorities(usuarioEncontrado.getPapeis().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()))
                .build();
    }
}
