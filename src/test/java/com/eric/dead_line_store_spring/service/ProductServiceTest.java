package com.eric.dead_line_store_spring.service;

import com.eric.dead_line_store_spring.dto.ProdutoRequestDTO;
import com.eric.dead_line_store_spring.dto.ProdutoResponseDTO;
import com.eric.dead_line_store_spring.entity.Produto;
import com.eric.dead_line_store_spring.exception.ProdutoNotFoundException;
import com.eric.dead_line_store_spring.repository.ProdutoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    @Test
    void shouldCreateProductSuccessfully() {

        ProdutoRequestDTO dto =
                new ProdutoRequestDTO(
                        "Mouse Gamer",
                        150.0,
                        10
                );

        Produto produto = new Produto();
        produto.setId(1L);
        produto.setNome(dto.nome());
        produto.setPreco(dto.preco());
        produto.setEstoque(dto.estoque());

        when(produtoRepository.save(any(Produto.class)))
                .thenReturn(produto);

        ProdutoResponseDTO response =
                produtoService.salvar(dto);

        assertNotNull(response);

        assertEquals("Mouse Gamer", response.nome());
        assertEquals(150.0, response.preco());
        assertEquals(10, response.estoque());

        verify(produtoRepository, times(1))
                .save(any(Produto.class));
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {

        when(produtoRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProdutoNotFoundException.class,
                () -> produtoService.buscarPorId(1L)
        );

        verify(produtoRepository, times(1))
                .findById(1L);
    }
}