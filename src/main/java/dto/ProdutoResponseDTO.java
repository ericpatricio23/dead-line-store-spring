package dto;

public record ProdutoResponseDTO(
        Long id,
        String nome,
        Double preco,
        Integer estoque
) {
}
