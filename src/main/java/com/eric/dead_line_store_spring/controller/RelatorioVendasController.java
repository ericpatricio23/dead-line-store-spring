package com.eric.dead_line_store_spring.controller;

import com.eric.dead_line_store_spring.dto.RelatorioVendasDTO;
import com.eric.dead_line_store_spring.service.RelatorioVendasService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/relatorios")
public class RelatorioVendasController {

    private final RelatorioVendasService service;

    public RelatorioVendasController(RelatorioVendasService service) {
        this.service = service;
    }

    @GetMapping("/vendas")
    public RelatorioVendasDTO vendas(@RequestParam String mes) {
        YearMonth anoMes;
        try {
            anoMes = YearMonth.parse(mes);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mês inválido. Use o formato AAAA-MM");
        }
        return service.gerar(anoMes);
    }
}
