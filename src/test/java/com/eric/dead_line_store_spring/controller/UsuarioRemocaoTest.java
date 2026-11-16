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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "jwt.secret=chave-de-teste-com-mais-de-trinta-e-dois-bytes-ok")
class UsuarioRemocaoTest {

    private static final String SENHA = "senha-dos-existentes-123";

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

    private Usuario admin;
    private Usuario funcionario;
    private Usuario outroFuncionario;

    @BeforeEach
    void criarUsuarios() {
        admin = salvarUsuario("admin-teste", Role.ADMIN);
        funcionario = salvarUsuario("funcionario-teste", Role.FUNCIONARIO);
        outroFuncionario = salvarUsuario("caixa-2", Role.FUNCIONARIO);
    }

    @AfterEach
    void limparUsuarios() {
        usuarioRepository.deleteAll();
    }

    @Test
    void shouldListUsersInAlphabeticalOrderWithoutPasswords() throws Exception {

        requisitar(get("/usuarios"), tokenDe("admin-teste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].username").value("admin-teste"))
                .andExpect(jsonPath("$[0].perfil").value("ADMIN"))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[1].username").value("caixa-2"))
                .andExpect(jsonPath("$[2].username").value("funcionario-teste"))
                .andExpect(jsonPath("$[2].perfil").value("FUNCIONARIO"))
                .andExpect(jsonPath("$[0].senha").doesNotExist())
                .andExpect(content().string(not(containsString("$2a$"))));
    }

    @Test
    void shouldRemoveAnEmployeeAndKeepTheOthers() throws Exception {

        requisitar(delete("/usuarios/" + funcionario.getId()), tokenDe("admin-teste"))
                .andExpect(status().isNoContent());

        assertTrue(usuarioRepository.findById(funcionario.getId()).isEmpty());
        assertTrue(usuarioRepository.findById(outroFuncionario.getId()).isPresent());
        assertTrue(usuarioRepository.findById(admin.getId()).isPresent());
    }

    @Test
    void shouldCutTheRemovedUsersAccessImmediately() throws Exception {

        String tokenDoFuncionario = tokenDe("funcionario-teste");

        requisitar(post("/produtos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Camiseta\",\"preco\":10,\"estoque\":1}"),
                tokenDoFuncionario)
                .andExpect(status().isForbidden());

        requisitar(delete("/usuarios/" + funcionario.getId()), tokenDe("admin-teste"))
                .andExpect(status().isNoContent());

        requisitar(post("/produtos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Camiseta\",\"preco\":10,\"estoque\":1}"),
                tokenDoFuncionario)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Autenticação necessária"));
    }

    @Test
    void shouldReturnNotFoundForAnUnknownUser() throws Exception {

        requisitar(delete("/usuarios/999999"), tokenDe("admin-teste"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Usuário não encontrado"));
    }

    @Test
    void shouldNotLetAnAdminRemoveThemselves() throws Exception {

        requisitar(delete("/usuarios/" + admin.getId()), tokenDe("admin-teste"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("Você não pode remover o próprio usuário"));

        assertTrue(usuarioRepository.findById(admin.getId()).isPresent());
    }

    @Test
    void shouldForbidAnEmployeeFromRemovingUsers() throws Exception {

        requisitar(delete("/usuarios/" + outroFuncionario.getId()), tokenDe("funcionario-teste"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("Acesso negado"));

        assertTrue(usuarioRepository.findById(outroFuncionario.getId()).isPresent());
    }

    @Test
    void shouldRequireAuthenticationToRemoveUsers() throws Exception {

        requisitar(delete("/usuarios/" + funcionario.getId()), null)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Autenticação necessária"));

        assertTrue(usuarioRepository.findById(funcionario.getId()).isPresent());
    }

    private Usuario salvarUsuario(String username, Role role) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setSenha(passwordEncoder.encode(SENHA));
        usuario.setRole(role);

        return usuarioRepository.save(usuario);
    }

    private String tokenDe(String username) {
        return jwtService.gerarToken(userDetailsService.loadUserByUsername(username));
    }

    private ResultActions requisitar(MockHttpServletRequestBuilder requisicao, String token)
            throws Exception {
        if (token != null) {
            requisicao.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        return mockMvc.perform(requisicao);
    }
}