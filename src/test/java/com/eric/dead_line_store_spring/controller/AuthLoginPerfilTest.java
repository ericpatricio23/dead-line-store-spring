package com.eric.dead_line_store_spring.controller;

import com.eric.dead_line_store_spring.entity.Role;
import com.eric.dead_line_store_spring.entity.Usuario;
import com.eric.dead_line_store_spring.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "jwt.secret=chave-de-teste-com-mais-de-trinta-e-dois-bytes-ok")
class AuthLoginPerfilTest {

    private static final String SENHA = "senha-de-teste-123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void limparUsuarios() {
        usuarioRepository.deleteAll();
    }

    @Test
    void shouldReturnTokenAndAdminProfileOnLogin() throws Exception {

        salvarUsuario("admin-teste", Role.ADMIN);

        entrar("admin-teste", SENHA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.perfil").value("ADMIN"));
    }

    @Test
    void shouldReturnEmployeeProfileOnLogin() throws Exception {

        salvarUsuario("funcionario-teste", Role.FUNCIONARIO);

        entrar("funcionario-teste", SENHA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.perfil").value("FUNCIONARIO"));
    }

    @Test
    void shouldStillRejectWrongPasswordWithoutLeakingProfile() throws Exception {

        salvarUsuario("funcionario-teste", Role.FUNCIONARIO);

        entrar("funcionario-teste", "senha-errada")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Usuário ou senha inválidos"))
                .andExpect(jsonPath("$.perfil").doesNotExist())
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    private void salvarUsuario(String username, Role role) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setSenha(passwordEncoder.encode(SENHA));
        usuario.setRole(role);

        usuarioRepository.save(usuario);
    }

    private ResultActions entrar(String username, String senha) throws Exception {
        String corpo = String.format(
                "{\"username\":\"%s\",\"senha\":\"%s\"}", username, senha);

        return mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo));
    }
}