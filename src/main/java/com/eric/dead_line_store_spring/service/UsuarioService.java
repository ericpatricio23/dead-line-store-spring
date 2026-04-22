package com.eric.dead_line_store_spring.service;


import com.eric.dead_line_store_spring.entity.Role;
import com.eric.dead_line_store_spring.entity.Usuario;
import com.eric.dead_line_store_spring.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(PasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
    }

    public void criarAdminPadrao() {

        boolean adminExiste = usuarioRepository.findByUsername("admin").isPresent();

        if (adminExiste) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setUsername("admin");
        admin.setSenha(passwordEncoder.encode("134679"));
        admin.setRole(Role.ADMIN);

        usuarioRepository.save(admin);
    }


}