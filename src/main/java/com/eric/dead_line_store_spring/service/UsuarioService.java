package com.eric.dead_line_store_spring.service;

import com.eric.dead_line_store_spring.dto.UsuarioRequestDTO;
import com.eric.dead_line_store_spring.dto.UsuarioResponseDTO;
import com.eric.dead_line_store_spring.entity.Role;
import com.eric.dead_line_store_spring.entity.Usuario;
import com.eric.dead_line_store_spring.exception.OperacaoNaoPermitidaException;
import com.eric.dead_line_store_spring.exception.UsuarioJaExisteException;
import com.eric.dead_line_store_spring.exception.UsuarioNotFoundException;
import com.eric.dead_line_store_spring.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    public UsuarioService(PasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
    }

    public void criarAdminPadrao() {

        boolean adminExiste = usuarioRepository.findByUsername("admin").isPresent();

        if (adminExiste) {
            return;
        }

        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("ADMIN_PASSWORD não definida: usuário admin NÃO foi criado.");
            return;
        }

        Usuario admin = new Usuario();
        admin.setUsername("admin");
        admin.setSenha(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);

        usuarioRepository.save(admin);
    }

    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        String username = dto.username().trim();

        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new UsuarioJaExisteException("Já existe um usuário com este nome");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuario.setRole(dto.perfil());

        Usuario salvo;

        try {
            salvo = usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException e) {
            throw new UsuarioJaExisteException("Já existe um usuário com este nome");
        }

        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAllByOrderByUsernameAsc()
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional
    public void remover(Long id, String usernameDeQuemRemove) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado"));

        if (usuario.getUsername().equals(usernameDeQuemRemove)) {
            throw new OperacaoNaoPermitidaException("Você não pode remover o próprio usuário");
        }

        usuarioRepository.delete(usuario);
    }

    private UsuarioResponseDTO converterParaDTO(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getRole().name()
        );
    }
}