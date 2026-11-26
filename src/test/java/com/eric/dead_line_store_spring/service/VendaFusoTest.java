package com.eric.dead_line_store_spring.service;

import com.eric.dead_line_store_spring.dto.ItemVendaRequestDTO;
import com.eric.dead_line_store_spring.dto.VendaCompletaRequestDTO;
import com.eric.dead_line_store_spring.dto.VendaResponseDTO;
import com.eric.dead_line_store_spring.entity.Produto;
import com.eric.dead_line_store_spring.entity.Venda;
import com.eric.dead_line_store_spring.repository.ItemVendaRepository;
import com.eric.dead_line_store_spring.repository.ProdutoRepository;
import com.eric.dead_line_store_spring.repository.VendaRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VendaFusoTest {

    @Test
    void venda_as_22h_de_brasilia_fica_no_mesmo_dia() {
        Clock relogio = Clock.fixed(
                Instant.parse("2026-11-27T01:30:00Z"),
                ZoneId.of("America/Sao_Paulo"));

        VendaRepository vendaRepository = mock(VendaRepository.class);
        ProdutoRepository produtoRepository = mock(ProdutoRepository.class);
        ItemVendaRepository itemVendaRepository = mock(ItemVendaRepository.class);

        Produto produto = new Produto();
        produto.setId(1L);
        produto.setNome("Camiseta");
        produto.setPreco(50.0);
        produto.setEstoque(10);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(vendaRepository.save(any(Venda.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        VendaService servico = new VendaService(
                itemVendaRepository, vendaRepository, produtoRepository, relogio);

        VendaResponseDTO resposta = servico.criarVendaCompleta(
                new VendaCompletaRequestDTO(List.of(new ItemVendaRequestDTO(1L, 1))));

        assertEquals(LocalDateTime.of(2026, 11, 26, 22, 30), resposta.data());
    }
}
