package com.eric.dead_line_store_spring.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record VendaCompletaRequestDTO(

        @NotEmpty(message = "A venda precisa ter pelo menos um item")
        List<@Valid ItemVendaRequestDTO> itens
) {
}