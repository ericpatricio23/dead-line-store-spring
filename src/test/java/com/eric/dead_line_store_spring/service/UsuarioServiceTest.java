package com.eric.dead_line_store_spring.service;

import com.eric.dead_line_store_spring.entity.Role;
import com.eric.dead_line_store_spring.entity.Usuario;
import com.eric.dead_line_store_spring.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final String SENHA_DE_TESTE = "senha-de-teste";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void shouldCreateDefaultAdminSuccessfully() {

        ReflectionTestUtils.setField(usuarioService, "adminPassword", SENHA_DE_TESTE);

        when(usuarioRepository.findByUsername("admin"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(SENHA_DE_TESTE))
                .thenReturn("senha-criptografada");

        usuarioService.criarAdminPadrao();

        verify(passwordEncoder, times(1))
                .encode(SENHA_DE_TESTE);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, times(1)).save(captor.capture());

        Usuario salvo = captor.getValue();
        assertEquals("admin", salvo.getUsername());
        assertEquals("senha-criptografada", salvo.getSenha());
        assertEquals(Role.ADMIN, salvo.getRole());
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

    @Test
    void shouldNotCreateAdminWhenPasswordIsNotConfigured() {

        ReflectionTestUtils.setField(usuarioService, "adminPassword", "");

        when(usuarioRepository.findByUsername("admin"))
                .thenReturn(Optional.empty());

        usuarioService.criarAdminPadrao();

        verify(usuarioRepository, never())
                .save(any(Usuario.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void shouldNotCreateAdminWhenPasswordIsBlank() {

        ReflectionTestUtils.setField(usuarioService, "adminPassword", "   ");

        when(usuarioRepository.findByUsername("admin"))
                .thenReturn(Optional.empty());

        usuarioService.criarAdminPadrao();

        verify(usuarioRepository, never())
                .save(any(Usuario.class));
    }
}