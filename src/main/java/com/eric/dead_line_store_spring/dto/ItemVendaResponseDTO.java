package com.eric.dead_line_store_spring.dto;

public record ItemVendaResponseDTO(
        Long id,
        Long produtoId,
        String nomeProduto,
        Integer quantidade,
        Double precoUnitario,
        Double subtotal
) {
}
