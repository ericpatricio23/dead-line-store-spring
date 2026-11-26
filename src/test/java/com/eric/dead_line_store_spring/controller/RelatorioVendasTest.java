package com.eric.dead_line_store_spring.controller;

import com.eric.dead_line_store_spring.entity.Role;
import com.eric.dead_line_store_spring.entity.Usuario;
import com.eric.dead_line_store_spring.entity.Venda;
import com.eric.dead_line_store_spring.repository.UsuarioRepository;
import com.eric.dead_line_store_spring.repository.VendaRepository;
import com.eric.dead_line_store_spring.security.JwtService;
import com.eric.dead_line_store_spring.service.CustomUserDetailsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "jwt.secret=chave-de-teste-com-mais-de-trinta-e-dois-bytes-ok")
class RelatorioVendasTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private VendaRepository vendaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;
    @Autowired private CustomUserDetailsService userDetailsService;

    private String tokenAdmin;
    private String tokenFuncionario;

    @BeforeEach
    void preparar() {
        tokenAdmin = criarUsuario("admin-teste", Role.ADMIN);
        tokenFuncionario = criarUsuario("funcionario-teste", Role.FUNCIONARIO);

        salvarVenda(LocalDateTime.of(2026, 11, 2, 10, 0), 10.0);
        salvarVenda(LocalDateTime.of(2026, 11, 2, 15, 0), 20.0);
        salvarVenda(LocalDateTime.of(2026, 11, 10, 9, 0), 5.5);
        salvarVenda(LocalDateTime.of(2026, 10, 30, 12, 0), 99.0);
    }

    @AfterEach
    void limpar() {
        vendaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void admin_recebe_os_totais_do_mes_sem_misturar_outros_meses() throws Exception {
        consultar("2026-11", tokenAdmin)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mes.quantidade").value(3))
                .andExpect(jsonPath("$.mes.total").value(35.5))
                .andExpect(jsonPath("$.dias.length()").value(2))
                .andExpect(jsonPath("$.dias[0].data").value("2026-11-02"))
                .andExpect(jsonPath("$.dias[0].quantidade").value(2))
                .andExpect(jsonPath("$.dias[0].total").value(30.0))
                .andExpect(jsonPath("$.dias[1].data").value("2026-11-10"))
                .andExpect(jsonPath("$.dias[1].total").value(5.5));
    }

    @Test
    void semanas_vao_de_segunda_a_domingo_e_somam_o_mes() throws Exception {
        consultar("2026-11", tokenAdmin)
                .andExpect(jsonPath("$.semanas.length()").value(2))
                .andExpect(jsonPath("$.semanas[0].inicio").value("2026-11-02"))
                .andExpect(jsonPath("$.semanas[0].fim").value("2026-11-08"))
                .andExpect(jsonPath("$.semanas[0].total").value(30.0))
                .andExpect(jsonPath("$.semanas[1].inicio").value("2026-11-09"))
                .andExpect(jsonPath("$.semanas[1].fim").value("2026-11-15"))
                .andExpect(jsonPath("$.semanas[1].total").value(5.5));
    }

    @Test
    void funcionario_nao_pode_ver_o_relatorio() throws Exception {
        consultar("2026-11", tokenFuncionario).andExpect(status().isForbidden());
    }

    @Test
    void sem_login_recebe_401() throws Exception {
        mockMvc.perform(get("/relatorios/vendas").param("mes", "2026-11"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mes_invalido_recebe_400() throws Exception {
        consultar("novembro", tokenAdmin).andExpect(status().isBadRequest());
    }

    private ResultActions consultar(String mes, String token) throws Exception {
        return mockMvc.perform(get("/relatorios/vendas")
                .param("mes", mes)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private String criarUsuario(String nome, Role role) {
        Usuario usuario = new Usuario();
        usuario.setUsername(nome);
        usuario.setSenha(passwordEncoder.encode("senha-de-teste-123"));
        usuario.setRole(role);
        usuarioRepository.save(usuario);
        return jwtService.gerarToken(userDetailsService.loadUserByUsername(nome));
    }

    private void salvarVenda(LocalDateTime data, double total) {
        Venda venda = new Venda();
        venda.setData(data);
        venda.setTotal(total);
        vendaRepository.save(venda);
    }
}
