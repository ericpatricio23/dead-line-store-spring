package com.eric.dead_line_store_spring.dto;

public record ProdutoResponseDTO(
        Long id,
        String nome,
        Double preco,
        Integer estoque
) {
}
