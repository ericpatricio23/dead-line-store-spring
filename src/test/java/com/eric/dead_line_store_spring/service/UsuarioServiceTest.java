package com.eric.dead_line_store_spring.service;

import com.eric.dead_line_store_spring.entity.Usuario;
import com.eric.dead_line_store_spring.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void shouldCreateDefaultAdminSuccessfully() {

        when(usuarioRepository.findByUsername("admin"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("134679"))
                .thenReturn("senha-criptografada");

        usuarioService.criarAdminPadrao();

        verify(passwordEncoder, times(1))
                .encode("134679");

        verify(usuarioRepository, times(1))
                .save(any(Usuario.class));
    }

    @Test
    void shouldNotCreateAdminIfAlreadyExists() {

        Usuario admin = new Usuario();

        when(usuarioRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        usuarioService.criarAdminPadrao();

        verify(usuarioRepository, never())
                .save(any(Usuario.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }
}