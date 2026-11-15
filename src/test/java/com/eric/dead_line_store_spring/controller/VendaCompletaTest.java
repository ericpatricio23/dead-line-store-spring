package com.eric.dead_line_store_spring.controller;

import com.eric.dead_line_store_spring.entity.Produto;
import com.eric.dead_line_store_spring.repository.ProdutoRepository;
import com.eric.dead_line_store_spring.repository.VendaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VendaCompletaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private VendaRepository vendaRepository;

    private Produto camiseta;
    private Produto bone;

    @BeforeEach
    void criarProdutos() {
        camiseta = salvarProduto("Camiseta", 10.0, 5);
        bone = salvarProduto("Bone", 25.5, 3);
    }

    @AfterEach
    void limparBanco() {
        vendaRepository.deleteAll();
        produtoRepository.deleteAll();
    }

    @Test
    void shouldCreateSaleWithAllItemsAndDecrementStock() throws Exception {

        enviar(itens(item(camiseta, 2), item(bone, 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(45.5))
                .andExpect(jsonPath("$.itens.length()").value(2))
                .andExpect(jsonPath("$.itens[0].nomeProduto").value("Camiseta"))
                .andExpect(jsonPath("$.itens[0].subtotal").value(20.0))
                .andExpect(jsonPath("$.itens[1].nomeProduto").value("Bone"))
                .andExpect(jsonPath("$.itens[1].subtotal").value(25.5));

        assertEquals(3, estoqueDe(camiseta));
        assertEquals(2, estoqueDe(bone));
        assertEquals(1, vendaRepository.count());
    }

    @Test
    void shouldUndoEverythingWhenAnyItemLacksStock() throws Exception {

        enviar(itens(item(camiseta, 2), item(bone, 10)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Estoque insuficiente para Bone"));

        assertEquals(5, estoqueDe(camiseta));
        assertEquals(3, estoqueDe(bone));
        assertEquals(0, vendaRepository.count());
    }

    @Test
    void shouldCountTheSameProductAcrossItemsWhenCheckingStock() throws Exception {

        enviar(itens(item(camiseta, 3), item(camiseta, 3)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Estoque insuficiente para Camiseta"));

        assertEquals(5, estoqueDe(camiseta));
        assertEquals(0, vendaRepository.count());
    }

    @Test
    void shouldUndoEverythingWhenAProductDoesNotExist() throws Exception {

        String itemInexistente = "{\"produtoId\":999999,\"quantidade\":1}";

        enviar(itens(item(camiseta, 1), itemInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        assertEquals(5, estoqueDe(camiseta));
        assertEquals(0, vendaRepository.count());
    }

    @Test
    void shouldRejectASaleWithoutItems() throws Exception {

        enviar("{\"itens\":[]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("A venda precisa ter pelo menos um item"));

        assertEquals(0, vendaRepository.count());
    }

    @Test
    void shouldValidateEachItemOfTheSale() throws Exception {

        enviar(itens(item(camiseta, 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("A quantidade deve ser pelo menos 1"));

        assertEquals(5, estoqueDe(camiseta));
        assertEquals(0, vendaRepository.count());
    }

    private Produto salvarProduto(String nome, double preco, int estoque) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setPreco(preco);
        produto.setEstoque(estoque);

        return produtoRepository.save(produto);
    }

    private int estoqueDe(Produto produto) {
        return produtoRepository.findById(produto.getId()).orElseThrow().getEstoque();
    }

    private String item(Produto produto, int quantidade) {
        return String.format(
                "{\"produtoId\":%d,\"quantidade\":%d}", produto.getId(), quantidade);
    }

    private String itens(String... itens) {
        return "{\"itens\":[" + String.join(",", itens) + "]}";
    }

    private ResultActions enviar(String corpo) throws Exception {
        return mockMvc.perform(post("/vendas/completa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo));
    }
}