package com.eric.dead_line_store_spring.dto;

import java.time.LocalDate;
import java.util.List;

public record RelatorioVendasDTO(
        Resumo mes,
        List<Dia> dias,
        List<Semana> semanas
) {
    public record Resumo(int quantidade, double total) {
    }

    public record Dia(LocalDate data, int quantidade, double total) {
    }

    public record Semana(LocalDate inicio, LocalDate fim, int quantidade, double total) {
    }
}
