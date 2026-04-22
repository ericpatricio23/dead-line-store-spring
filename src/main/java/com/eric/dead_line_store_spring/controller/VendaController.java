package com.eric.dead_line_store_spring.controller;

import com.eric.dead_line_store_spring.dto.ItemVendaRequestDTO;
import com.eric.dead_line_store_spring.dto.VendaRequestDTO;
import com.eric.dead_line_store_spring.dto.VendaResponseDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.eric.dead_line_store_spring.service.VendaService;

import java.util.List;

@RestController
@RequestMapping ("/vendas")
public class VendaController {

    private final VendaService vendaService;

    public VendaController(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    @PostMapping
    public VendaResponseDTO criar (@Valid @RequestBody VendaRequestDTO dto)  {

        return vendaService.criarVenda(dto);
    }

    @PostMapping ("/{id}/itens")
    public VendaResponseDTO adicionarItem(@RequestBody @Valid @PathVariable Long id, ItemVendaRequestDTO dto){
        return vendaService.adicionarItem(id, dto);
    }

    @GetMapping
    public List<VendaResponseDTO> listarTodos(){
        return vendaService.listarTodos();
    }

    @GetMapping ("/{id}")
    public VendaResponseDTO buscarPorId( @PathVariable Long id){
        return vendaService.buscarPorID(id);

    }

    @DeleteMapping ("/{id}")
    public void deletar(@PathVariable Long id){
        vendaService.deletar(id);
    }

}

