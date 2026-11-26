package com.eric.dead_line_store_spring.controller;

import com.eric.dead_line_store_spring.entity.ItemVenda;
import com.eric.dead_line_store_spring.entity.Produto;
import com.eric.dead_line_store_spring.entity.Role;
import com.eric.dead_line_store_spring.entity.Usuario;
import com.eric.dead_line_store_spring.entity.Venda;
import com.eric.dead_line_store_spring.repository.ProdutoRepository;
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

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "jwt.secret=chave-de-teste-com-mais-de-trinta-e-dois-bytes-ok")
class VendasDoDiaETest {

    @Autowired private MockMvc mockMvc;
    @Autowired private VendaRepository vendaRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;
    @Autowired private CustomUserDetailsService userDetailsService;

    private String tokenAdmin;
    private String tokenFuncionario;
    private Produto camiseta;
    private Venda venda;

    @BeforeEach
    void preparar() {
        tokenAdmin = criarUsuario("admin-teste", Role.ADMIN);
        tokenFuncionario = criarUsuario("funcionario-teste", Role.FUNCIONARIO);

        camiseta = new Produto();
        camiseta.setNome("Camiseta");
        camiseta.setPreco(10.0);
        camiseta.setEstoque(3);
        camiseta = produtoRepository.save(camiseta);

        venda = new Venda();
        venda.setData(LocalDateTime.of(2026, 11, 2, 10, 0));
        venda.setTotal(20.0);
        ItemVenda item = new ItemVenda();
        item.setVenda(venda);
        item.setProduto(camiseta);
        item.setQuantidade(2);
        item.setPrecoUnitario(10.0);
        item.setSubtotal(20.0);
        venda.getItens().add(item);
        venda = vendaRepository.save(venda);

        Venda outroDia = new Venda();
        outroDia.setData(LocalDateTime.of(2026, 11, 3, 10, 0));
        outroDia.setTotal(99.0);
        vendaRepository.save(outroDia);
    }

    @AfterEach
    void limpar() {
        vendaRepository.deleteAll();
        produtoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void admin_lista_so_as_vendas_do_dia_com_itens() throws Exception {
        mockMvc.perform(get("/relatorios/vendas/dia").param("data", "2026-11-02")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].total").value(20.0))
                .andExpect(jsonPath("$[0].itens[0].nomeProduto").value("Camiseta"))
                .andExpect(jsonPath("$[0].itens[0].quantidade").value(2));
    }

    @Test
    void funcionario_nao_lista_vendas_do_dia() throws Exception {
        mockMvc.perform(get("/relatorios/vendas/dia").param("data", "2026-11-02")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFuncionario))
                .andExpect(status().isForbidden());
    }

    @Test
    void data_invalida_recebe_400() throws Exception {
        mockMvc.perform(get("/relatorios/vendas/dia").param("data", "ontem")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelar_venda_apaga_e_devolve_o_estoque() throws Exception {
        mockMvc.perform(delete("/vendas/" + venda.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk());

        assertEquals(5, produtoRepository.findById(camiseta.getId()).orElseThrow().getEstoque());
        assertEquals(1, vendaRepository.count());
    }

    @Test
    void funcionario_nao_cancela_venda() throws Exception {
        mockMvc.perform(delete("/vendas/" + venda.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFuncionario))
                .andExpect(status().isForbidden());

        assertEquals(2, vendaRepository.count());
    }

    private String criarUsuario(String nome, Role role) {
        Usuario usuario = new Usuario();
        usuario.setUsername(nome);
        usuario.setSenha(passwordEncoder.encode("senha-de-teste-123"));
        usuario.setRole(role);
        usuarioRepository.save(usuario);
        return jwtService.gerarToken(userDetailsService.loadUserByUsername(nome));
    }
}
