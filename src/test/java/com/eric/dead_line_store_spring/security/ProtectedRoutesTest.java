package com.eric.dead_line_store_spring.security;

import com.eric.dead_line_store_spring.entity.Produto;
import com.eric.dead_line_store_spring.entity.Role;
import com.eric.dead_line_store_spring.entity.Usuario;
import com.eric.dead_line_store_spring.repository.ProdutoRepository;
import com.eric.dead_line_store_spring.repository.UsuarioRepository;
import com.eric.dead_line_store_spring.repository.VendaRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "jwt.secret=chave-de-teste-com-mais-de-trinta-e-dois-bytes-ok")
class ProtectedRoutesTest {

    private static final String SENHA = "senha-de-teste-123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    private Produto camiseta;

    @BeforeEach
    void prepararDados() {
        Usuario funcionario = new Usuario();
        funcionario.setUsername("funcionario-teste");
        funcionario.setSenha(passwordEncoder.encode(SENHA));
        funcionario.setRole(Role.FUNCIONARIO);
        usuarioRepository.save(funcionario);

        camiseta = new Produto();
        camiseta.setNome("Camiseta");
        camiseta.setPreco(10.0);
        camiseta.setEstoque(5);
        camiseta = produtoRepository.save(camiseta);
    }

    @AfterEach
    void limparBanco() {
        vendaRepository.deleteAll();
        produtoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void shouldExposeHealthWithoutLogin() throws Exception {

        requisitar(get("/actuator/health"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void shouldNotRevealDetailsInTheHealthResponse() throws Exception {

        requisitar(get("/actuator/health"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components").doesNotExist())
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    void shouldNotExposeOtherActuatorEndpointsWithoutLogin() throws Exception {

        requisitar(get("/actuator/env"), null)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Autenticação necessária"));
    }

    @Test
    void shouldRequireLoginToListProducts() throws Exception {

        requisitar(get("/produtos"), null)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Autenticação necessária"));
    }

    @Test
    void shouldRequireLoginToListSales() throws Exception {

        requisitar(get("/vendas"), null)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Autenticação necessária"));
    }

    @Test
    void shouldRequireLoginToRegisterASale() throws Exception {

        requisitar(post("/vendas/completa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoDaVenda()), null)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Autenticação necessária"));
    }

    @Test
    void shouldRequireLoginToRegisterASingleItemSale() throws Exception {

        requisitar(post("/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format(
                        "{\"produtoId\":%d,\"quantidade\":1}", camiseta.getId())), null)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Autenticação necessária"));
    }

    @Test
    void shouldLetAnEmployeeListProducts() throws Exception {

        requisitar(get("/produtos"), tokenDe("funcionario-teste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Camiseta"));
    }

    @Test
    void shouldLetAnEmployeeRegisterASale() throws Exception {

        requisitar(post("/vendas/completa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoDaVenda()), tokenDe("funcionario-teste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10.0));
    }

    private String corpoDaVenda() {
        return String.format(
                "{\"itens\":[{\"produtoId\":%d,\"quantidade\":1}]}", camiseta.getId());
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