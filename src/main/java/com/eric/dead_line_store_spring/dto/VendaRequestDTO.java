package com.eric.dead_line_store_spring.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record VendaRequestDTO(

        @NotNull(message = "O id do produto é obrigatório")
        Long produtoId,
        @NotNull(message = "A quantidade é obrigatoria")
        @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
        Integer quantidade
) {
}