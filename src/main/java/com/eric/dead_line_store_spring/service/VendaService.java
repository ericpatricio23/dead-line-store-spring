package com.eric.dead_line_store_spring.service;

import com.eric.dead_line_store_spring.dto.ItemVendaRequestDTO;
import com.eric.dead_line_store_spring.dto.ItemVendaResponseDTO;
import com.eric.dead_line_store_spring.dto.VendaRequestDTO;
import com.eric.dead_line_store_spring.dto.VendaResponseDTO;
import com.eric.dead_line_store_spring.entity.ItemVenda;
import com.eric.dead_line_store_spring.entity.Produto;
import com.eric.dead_line_store_spring.entity.Venda;
import com.eric.dead_line_store_spring.exception.EstoqueInsuficienteException;
import com.eric.dead_line_store_spring.exception.ProdutoNotFoundException;
import com.eric.dead_line_store_spring.exception.VendaNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.eric.dead_line_store_spring.repository.ItemVendaRepository;
import com.eric.dead_line_store_spring.repository.ProdutoRepository;
import com.eric.dead_line_store_spring.repository.VendaRepository;

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

    public VendaResponseDTO criarVenda(VendaRequestDTO dto ){
        Produto produto = produtoRepository.findById(dto.produtoId())
                .orElseThrow(()-> new ProdutoNotFoundException("Produto não encontrado")) ;

                if (produto.getEstoque()< dto.quantidade()){
                    throw new EstoqueInsuficienteException("Estoque insuficiente");
                }

                produto.setEstoque(produto.getEstoque() - dto.quantidade());
        Venda venda = new Venda();
        venda.setData(LocalDateTime.now());

        ItemVenda item = new ItemVenda();
        item.setVenda(venda);
        item.setProduto(produto);
        item.setQuantidade(dto.quantidade());
        item.setPrecoUnitario(produto.getPreco());

        double total = produto.getPreco() * dto.quantidade();
        item.setSubtotal(total);
        venda.getItens().add(item);
        venda.setTotal(total);

        vendaRepository.save(venda);
        itemVendaRepository.save(item);
        produtoRepository.save(produto);

        return converterParaDTO(venda);

    }
    public VendaResponseDTO adicionarItem(Long vendaId, ItemVendaRequestDTO dto ) {
        Venda venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new VendaNotFoundException("Venda não encontrada"));
        Produto produto = produtoRepository.findById(dto.produtoId())
                .orElseThrow(() -> new ProdutoNotFoundException("Produto não encontrado"));

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

        produtoRepository.save(produto);
        vendaRepository.save(venda);

        return converterParaDTO(venda);
    }

    @Transactional
    public VendaResponseDTO buscarPorID(Long id) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new VendaNotFoundException("Venda não encontrada"));

        return converterParaDTO(venda);
    }

    @Transactional
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
