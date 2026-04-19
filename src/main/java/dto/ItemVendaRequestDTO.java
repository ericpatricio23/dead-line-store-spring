package dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemVendaRequestDTO(

        @NotNull(message = "O id do produto é obrigatório")
       Long produtoId,
       @NotNull(message = "A quantidade é obrigatoria")
       @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
       Integer quantidade
) {
}
