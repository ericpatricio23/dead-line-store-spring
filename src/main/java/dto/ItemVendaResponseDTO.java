package dto;

public record ItemVendaResponseDTO(
        Long id,
        Long produtoId,
        String nomeProduto,
        Integer quantidade,
        Double precoUnitario,
        Double subtotal
) {
}
