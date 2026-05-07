package com.eric.dead_line_store_spring.service;

import com.eric.dead_line_store_spring.dto.VendaRequestDTO;
import com.eric.dead_line_store_spring.dto.VendaResponseDTO;
import com.eric.dead_line_store_spring.entity.Produto;
import com.eric.dead_line_store_spring.entity.Venda;
import com.eric.dead_line_store_spring.exception.EstoqueInsuficienteException;
import com.eric.dead_line_store_spring.exception.ProdutoNotFoundException;
import com.eric.dead_line_store_spring.repository.ItemVendaRepository;
import com.eric.dead_line_store_spring.repository.ProdutoRepository;
import com.eric.dead_line_store_spring.repository.VendaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendaServiceTest {

    @Mock
    private VendaRepository vendaRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ItemVendaRepository itemVendaRepository;

    @InjectMocks
    private VendaService vendaService;

    @Test
    void shouldCreateSaleSuccessfully() {

        Produto produto = new Produto();

        produto.setId(1L);
        produto.setNome("Mouse Gamer");
        produto.setPreco(100.0);
        produto.setEstoque(10);

        VendaRequestDTO dto =
                new VendaRequestDTO(1L, 2);

        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produto));

        when(vendaRepository.save(any(Venda.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VendaResponseDTO response =
                vendaService.criarVenda(dto);

        assertNotNull(response);

        assertEquals(200.0, response.total());

        assertEquals(8, produto.getEstoque());

        verify(produtoRepository, times(1))
                .save(produto);

        verify(vendaRepository, times(1))
                .save(any(Venda.class));
    }

    @Test
    void shouldThrowExceptionWhenStockIsInsufficient() {

        Produto produto = new Produto();

        produto.setId(1L);
        produto.setNome("Mouse");
        produto.setPreco(100.0);
        produto.setEstoque(1);

        VendaRequestDTO dto =
                new VendaRequestDTO(1L, 5);

        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produto));

        assertThrows(
                EstoqueInsuficienteException.class,
                () -> vendaService.criarVenda(dto)
        );

        verify(vendaRepository, never())
                .save(any(Venda.class));
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {

        VendaRequestDTO dto =
                new VendaRequestDTO(1L, 2);

        when(produtoRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProdutoNotFoundException.class,
                () -> vendaService.criarVenda(dto)
        );

        verify(vendaRepository, never())
                .save(any(Venda.class));
    }
}