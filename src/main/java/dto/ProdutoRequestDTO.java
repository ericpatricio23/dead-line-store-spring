package dto;

public record ProdutoRequestDTO(
        String nome,
        Double preco,
        Integer estoque
) {
}
