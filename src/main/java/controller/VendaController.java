package controller;

import dto.ItemVendaRequestDTO;
import dto.VendaResponseDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import service.VendaService;

import java.util.List;

@RestController
@RequestMapping ("/vendas")
public class VendaController {

    private final VendaService vendaService;

    public VendaController(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    @PostMapping
    public VendaResponseDTO criar (){
        return vendaService.criarVenda();
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

