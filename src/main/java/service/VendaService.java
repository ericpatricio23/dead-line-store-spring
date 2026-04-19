package service;

import dto.ItemVendaRequestDTO;
import dto.ItemVendaResponseDTO;
import dto.VendaResponseDTO;
import entity.ItemVenda;
import entity.Produto;
import entity.Venda;
import exception.EstoqueInsuficienteException;
import exception.ProdutoNotFoundException;
import exception.VendaNotFoundException;
import org.springframework.stereotype.Service;
import repository.ItemVendaRepository;
import repository.ProdutoRepository;
import repository.VendaRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    private final ItemVendaRepository itemVendaRepository;

    public VendaService(ItemVendaRepository itemVendaRepository, VendaRepository vendaRepository, ProdutoRepository produtoRepository) {
        this.itemVendaRepository = itemVendaRepository;
        this.vendaRepository = vendaRepository;
        this.produtoRepository = produtoRepository;
    }

    public VendaResponseDTO criarVenda(){
        Venda venda = new Venda();
        venda.setData(LocalDateTime.now());
        venda.setTotal(0.0);
        vendaRepository.save(venda);

        return new VendaResponseDTO(
                venda.getId(),
                venda.getData(),
                venda.getTotal(),
                List.of()
        );
    }
    public VendaResponseDTO adicionarItem(Long vendaId, ItemVendaRequestDTO dto ) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new VendaNotFoundException("Venda não encontrada"));
        Produto produto = produtoRepository.findById(dto.produtoId())
                .orElseThrow(() -> new ProdutoNotFoundException("Produto não encontradp"));

        if (produto.getEstoque() < dto.quantidade()) {
            throw new EstoqueInsuficienteException("Estoque insuficiente");
        }

        ItemVenda item = new ItemVenda();
        item.setVenda(venda);
        item.setProduto(produto);
        item.setQuantidade(dto.quantidade());

        item.setPrecoUnitario(produto.getPreco());

        double subtotal = produto.getPreco() * dto.quantidade();
        item.setSubtotal(subtotal);

        venda.getItens().add(item);

        produto.setEstoque(produto.getEstoque() - dto.quantidade());

        double total = venda.getItens()
                .stream()
                .mapToDouble(ItemVenda::getSubtotal)
                .sum();

                venda.setTotal(total);

        vendaRepository.save(venda);

        return converterParaDTO(venda);
    }


    public VendaResponseDTO buscarPorID(Long id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new VendaNotFoundException("Venda não encontrada"));

        return converterParaDTO(venda);
    }

    public List<VendaResponseDTO> listarTodos() {
        return vendaRepository.findAll()
                .stream()
                .map(this::converterParaDTO)
                .toList();

    }


    private VendaResponseDTO converterParaDTO(Venda venda) {
        List<ItemVendaResponseDTO> itensDTO = venda.getItens()
                .stream()
                .map(item -> new ItemVendaResponseDTO(
                        item.getId(),
                        item.getProduto().getId(),
                        item.getProduto().getNome(),
                        item.getQuantidade(),
                        item.getPrecoUnitario(),
                        item.getSubtotal()
                ))
                .toList();

        return new VendaResponseDTO(
                venda.getId(),
                venda.getData(),
                venda.getTotal(),
                itensDTO
        );
    }

    public void deletar(Long id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new VendaNotFoundException("Venda não encontrada"));

        vendaRepository.delete(venda);
    }


}
