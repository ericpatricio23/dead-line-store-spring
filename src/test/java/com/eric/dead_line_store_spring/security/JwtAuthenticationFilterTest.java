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

        mockMvc.perform(get("/produtos")
                        .header("Authorization", "Bearer isto-nao-e-um-jwt"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldTreatExpiredTokenAsAnonymousOnPublicRoute() throws Exception {

        long agora = System.currentTimeMillis();

        String expirado = Jwts.builder()
                .subject("admin")
                .issuedAt(new Date(agora - 2 * 3_600_000L))
                .expiration(new Date(agora - 3_600_000L))
                .signWith(Keys.hmacShaKeyFor(segredo.getBytes()))
                .compact();

        mockMvc.perform(get("/produtos")
                        .header("Authorization", "Bearer " + expirado))
                .andExpect(status().isOk());
    }
}