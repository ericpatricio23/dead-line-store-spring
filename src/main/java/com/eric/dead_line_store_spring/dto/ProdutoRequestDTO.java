package com.eric.dead_line_store_spring.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProdutoRequestDTO(

        @NotBlank(message = "O nome é obrigatorio")
        String nome,
        @NotNull(message = "O preço é obrigatório")
        @Positive(message = "O preço deve ser maior que zero")
        Double preco,
        @NotNull(message = "O estoque é obrigatorio")
        @Min(value = 0, message = "O estoque não pode ser negativo")
        Integer estoque
) {
}
