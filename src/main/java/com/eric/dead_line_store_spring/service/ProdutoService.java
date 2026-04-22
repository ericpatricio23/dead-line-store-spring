package com.eric.dead_line_store_spring.service;

import com.eric.dead_line_store_spring.dto.ProdutoRequestDTO;
import com.eric.dead_line_store_spring.dto.ProdutoResponseDTO;
import com.eric.dead_line_store_spring.entity.Produto;
import com.eric.dead_line_store_spring.exception.ProdutoNotFoundException;
import org.springframework.stereotype.Service;
import com.eric.dead_line_store_spring.repository.ProdutoRepository;

import java.util.List;

@Service

public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public ProdutoResponseDTO salvar (ProdutoRequestDTO dto){
        Produto produto = new Produto();
        produto.setNome(dto.nome());
        produto.setPreco(dto.preco());
        produto.setEstoque(dto.estoque());


        Produto salvo = produtoRepository.save(produto);

        return new ProdutoResponseDTO(
                salvo.getId(),
                salvo.getNome(),
                salvo.getPreco(),
                salvo.getEstoque()
        );
    }

    public List<ProdutoResponseDTO> listarTodos(){
        return produtoRepository.findAll()
                .stream()
                .map(produto -> new ProdutoResponseDTO(
                        produto.getId(),
                        produto.getNome(),
                        produto.getPreco(),
                        produto.getEstoque()
                ))
                .toList();
    }

    public ProdutoResponseDTO buscarPorId(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNotFoundException("Produto não encontrado"));

        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getPreco(),
                produto.getEstoque()
        );
    }
    public ProdutoResponseDTO atualizar (Long id, ProdutoRequestDTO dto){
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(()-> new ProdutoNotFoundException("Produto não encontrado"));

        produto.setNome(dto.nome());
        produto.setPreco(dto.preco());
        produto.setEstoque(dto.estoque());

        Produto atualizado = produtoRepository.save(produto);
        return new ProdutoResponseDTO(
                atualizado.getId(),
                atualizado.getNome(),
                atualizado.getPreco(),
                atualizado.getEstoque()

        );

    }
    public void deletar (Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(()-> new ProdutoNotFoundException("Produto não encontrado"));
                produtoRepository.delete(produto);
    }


}
