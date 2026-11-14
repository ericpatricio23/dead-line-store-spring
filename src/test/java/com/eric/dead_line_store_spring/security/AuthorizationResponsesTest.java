package com.eric.dead_line_store_spring.security;

import com.eric.dead_line_store_spring.entity.Role;
import com.eric.dead_line_store_spring.entity.Usuario;
import com.eric.dead_line_store_spring.repository.UsuarioRepository;
import com.eric.dead_line_store_spring.service.CustomUserDetailsService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "jwt.secret=chave-de-teste-com-mais-de-trinta-e-dois-bytes-ok")
class AuthorizationResponsesTest {

    private static final String PRODUTO_VALIDO =
            "{\"nome\":\"Camiseta\",\"preco\":59.9,\"estoque\":10}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtService jwtService;

    @Value("${jwt.secret}")
    private String segredo;

    @AfterEach
    void limparUsuarios() {
        usuarioRepository.deleteAll();
    }

    @Test
    void shouldReturn401JsonWhenThereIsNoToken() throws Exception {

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUTO_VALIDO))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.erro").value("Autenticação necessária"));
    }

    @Test
    void shouldReturn401JsonWhenTokenIsExpired() throws Exception {

        long agora = System.currentTimeMillis();

        String expirado = Jwts.builder()
                .subject("admin")
                .issuedAt(new Date(agora - 2 * 3_600_000L))
                .expiration(new Date(agora - 3_600_000L))
                .signWith(Keys.hmacShaKeyFor(segredo.getBytes()))
                .compact();

        mockMvc.perform(post("/produtos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expirado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUTO_VALIDO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void shouldReturn403JsonWhenUserIsNotAdmin() throws Exception {

        Usuario funcionario = new Usuario();
        funcionario.setUsername("funcionario-teste");
        funcionario.setSenha("irrelevante-neste-teste");
        funcionario.setRole(Role.FUNCIONARIO);
        usuarioRepository.save(funcionario);

        UserDetails detalhes = userDetailsService.loadUserByUsername("funcionario-teste");
        String token = jwtService.gerarToken(detalhes);

        mockMvc.perform(post("/produtos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PRODUTO_VALIDO))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro").value("Acesso negado"));
    }
}