package com.eric.dead_line_store_spring.controller;

import com.eric.dead_line_store_spring.entity.Role;
import com.eric.dead_line_store_spring.entity.Usuario;
import com.eric.dead_line_store_spring.repository.UsuarioRepository;
import com.eric.dead_line_store_spring.security.JwtService;
import com.eric.dead_line_store_spring.service.CustomUserDetailsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "jwt.secret=chave-de-teste-com-mais-de-trinta-e-dois-bytes-ok")
class UsuarioControllerTest {

    private static final String SENHA_EXISTENTES = "senha-dos-existentes-123";
    private static final String SENHA_NOVA = "senha-do-novo-456";
    private static final String NOVO_USUARIO = "caixa-1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void criarUsuariosExistentes() {
        salvarUsuario("admin-teste", Role.ADMIN);
        salvarUsuario("funcionario-teste", Role.FUNCIONARIO);
    }

    @AfterEach
    void limparUsuarios() {
        usuarioRepository.deleteAll();
    }

    @Test
    void shouldCreateEmployeeAndNeverReturnThePassword() throws Exception {

        criarUsuario(tokenDe("admin-teste"), corpo(NOVO_USUARIO, SENHA_NOVA, "FUNCIONARIO"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value(NOVO_USUARIO))
                .andExpect(jsonPath("$.perfil").value("FUNCIONARIO"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(content().string(not(containsString(SENHA_NOVA))));

        Usuario salvo = usuarioRepository.findByUsername(NOVO_USUARIO).orElseThrow();
        assertEquals(Role.FUNCIONARIO, salvo.getRole());
        assertNotEquals(SENHA_NOVA, salvo.getSenha());
        assertTrue(passwordEncoder.matches(SENHA_NOVA, salvo.getSenha()));
    }

    @Test
    void shouldLetTheNewEmployeeLogInWithTheirProfile() throws Exception {

        criarUsuario(tokenDe("admin-teste"), corpo(NOVO_USUARIO, SENHA_NOVA, "FUNCIONARIO"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + NOVO_USUARIO
                                + "\",\"senha\":\"" + SENHA_NOVA + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.perfil").value("FUNCIONARIO"));
    }

    @Test
    void shouldRejectADuplicateUsername() throws Exception {

        salvarUsuario(NOVO_USUARIO, Role.FUNCIONARIO);

        criarUsuario(tokenDe("admin-teste"), corpo(NOVO_USUARIO, SENHA_NOVA, "FUNCIONARIO"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("Já existe um usuário com este nome"));
    }

    @Test
    void shouldRejectAShortPassword() throws Exception {

        criarUsuario(tokenDe("admin-teste"), corpo(NOVO_USUARIO, "curta1", "FUNCIONARIO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("A senha deve ter pelo menos 8 caracteres"));

        assertTrue(usuarioRepository.findByUsername(NOVO_USUARIO).isEmpty());
    }

    @Test
    void shouldRejectABlankUsername() throws Exception {

        criarUsuario(tokenDe("admin-teste"), corpo("   ", SENHA_NOVA, "FUNCIONARIO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O nome de usuário é obrigatório"));
    }

    @Test
    void shouldRejectAMissingProfile() throws Exception {

        criarUsuario(tokenDe("admin-teste"), corpo(NOVO_USUARIO, SENHA_NOVA, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O perfil é obrigatório"));

        assertTrue(usuarioRepository.findByUsername(NOVO_USUARIO).isEmpty());
    }

    @Test
    void shouldRejectAnUnknownProfile() throws Exception {

        criarUsuario(tokenDe("admin-teste"), corpo(NOVO_USUARIO, SENHA_NOVA, "DONO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Corpo da requisição inválido"));

        assertTrue(usuarioRepository.findByUsername(NOVO_USUARIO).isEmpty());
    }

    @Test
    void shouldForbidAnEmployeeFromCreatingUsers() throws Exception {

        criarUsuario(tokenDe("funcionario-teste"), corpo(NOVO_USUARIO, SENHA_NOVA, "ADMIN"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado"));

        assertTrue(usuarioRepository.findByUsername(NOVO_USUARIO).isEmpty());
    }

    @Test
    void shouldRequireAuthenticationToCreateUsers() throws Exception {

        criarUsuario(null, corpo(NOVO_USUARIO, SENHA_NOVA, "FUNCIONARIO"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Autenticação necessária"));

        assertTrue(usuarioRepository.findByUsername(NOVO_USUARIO).isEmpty());
    }

    private void salvarUsuario(String username, Role role) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setSenha(passwordEncoder.encode(SENHA_EXISTENTES));
        usuario.setRole(role);

        usuarioRepository.save(usuario);
    }

    private String tokenDe(String username) {
        return jwtService.gerarToken(userDetailsService.loadUserByUsername(username));
    }

    private String corpo(String username, String senha, String perfil) {
        StringBuilder json = new StringBuilder("{");
        json.append("\"username\":\"").append(username).append("\",");
        json.append("\"senha\":\"").append(senha).append("\"");

        if (perfil != null) {
            json.append(",\"perfil\":\"").append(perfil).append("\"");
        }

        return json.append("}").toString();
    }

    private ResultActions criarUsuario(String token, String corpo) throws Exception {
        MockHttpServletRequestBuilder requisicao = post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo);

        if (token != null) {
            requisicao.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        return mockMvc.perform(requisicao);
    }
}