package controller;

import dto.ProdutoRequestDTO;
import dto.ProdutoResponseDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import service.ProdutoService;

import java.util.List;

@RestController
@RequestMapping ("/produtos")
public class ProdutoController {

   private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    public ProdutoResponseDTO salvar (@RequestBody @Valid ProdutoRequestDTO dto){
        return produtoService.salvar(dto);
    }
    @GetMapping
    public List<ProdutoResponseDTO> listarTodos(){
        return produtoService.listarTodos();
    }

    @GetMapping ("/{id}")
    public ProdutoResponseDTO buscarPorID (@PathVariable Long id){
        return produtoService.buscarPorId(id);

    }

    @PutMapping ("/{id}")
    public ProdutoResponseDTO atualizar ( @PathVariable Long id, @RequestBody
    @Valid ProdutoRequestDTO dto){

        return produtoService.atualizar(id, dto);
    }

    @DeleteMapping ("/{id}")
    public void deletar (@PathVariable Long id){
        produtoService.deletar(id);
    }
}
