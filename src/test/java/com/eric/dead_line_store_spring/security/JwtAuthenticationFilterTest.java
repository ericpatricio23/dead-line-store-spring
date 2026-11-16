package com.eric.dead_line_store_spring.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "jwt.secret=chave-de-teste-com-mais-de-trinta-e-dois-bytes-ok")
class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${jwt.secret}")
    private String segredo;

    @Test
    void shouldTreatMalformedTokenAsAnonymousOnPublicRoute() throws Exception {

        mockMvc.perform(get("/actuator/health")
                        .header("Authorization", "Bearer isto-nao-e-um-jwt"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldTreatExpiredTokenAsAnonymousOnPublicRoute() throws Exception {

        mockMvc.perform(get("/actuator/health")
                        .header("Authorization", "Bearer " + tokenExpirado()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAnswer401InJsonForAMalformedTokenOnAProtectedRoute() throws Exception {

        mockMvc.perform(get("/produtos")
                        .header("Authorization", "Bearer isto-nao-e-um-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Autenticação necessária"));
    }

    @Test
    void shouldAnswer401InJsonForAnExpiredTokenOnAProtectedRoute() throws Exception {

        mockMvc.perform(get("/produtos")
                        .header("Authorization", "Bearer " + tokenExpirado()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Autenticação necessária"));
    }

    private String tokenExpirado() {
        long agora = System.currentTimeMillis();

        return Jwts.builder()
                .subject("admin")
                .issuedAt(new Date(agora - 2 * 3_600_000L))
                .expiration(new Date(agora - 3_600_000L))
                .signWith(Keys.hmacShaKeyFor(segredo.getBytes()))
                .compact();
    }
}