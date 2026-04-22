package com.eric.dead_line_store_spring.dto;

import java.time.LocalDateTime;
import java.util.List;

public record VendaResponseDTO(
        Long id,
        LocalDateTime data,
        Double total,
        List<ItemVendaResponseDTO> itens
) {
}
