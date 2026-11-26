package com.eric.dead_line_store_spring.service;

import com.eric.dead_line_store_spring.dto.RelatorioVendasDTO;
import com.eric.dead_line_store_spring.entity.Venda;
import com.eric.dead_line_store_spring.repository.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class RelatorioVendasService {

    private final VendaRepository vendaRepository;
    private final VendaService vendaService;

    public RelatorioVendasService(VendaRepository vendaRepository, VendaService vendaService) {
        this.vendaService = vendaService;
        this.vendaRepository = vendaRepository;
    }

    @Transactional(readOnly = true)
    public RelatorioVendasDTO gerar(YearMonth mes) {
        List<Venda> vendas = vendaRepository.findByDataGreaterThanEqualAndDataLessThanOrderByData(
                mes.atDay(1).atStartOfDay(), mes.plusMonths(1).atDay(1).atStartOfDay());

        Map<LocalDate, double[]> porDia = new TreeMap<>();
        for (Venda venda : vendas) {
            double[] acumulado = porDia.computeIfAbsent(venda.getData().toLocalDate(), d -> new double[2]);
            acumulado[0] += 1;
            acumulado[1] += venda.getTotal() == null ? 0 : venda.getTotal();
        }

        List<RelatorioVendasDTO.Dia> dias = new ArrayList<>();
        Map<LocalDate, double[]> porSemana = new TreeMap<>();
        int quantidadeMes = 0;
        double totalMes = 0;

        for (Map.Entry<LocalDate, double[]> entrada : porDia.entrySet()) {
            int quantidade = (int) entrada.getValue()[0];
            double total = arredondar(entrada.getValue()[1]);
            dias.add(new RelatorioVendasDTO.Dia(entrada.getKey(), quantidade, total));

            LocalDate segunda = entrada.getKey().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            double[] semana = porSemana.computeIfAbsent(segunda, d -> new double[2]);
            semana[0] += quantidade;
            semana[1] += total;

            quantidadeMes += quantidade;
            totalMes += total;
        }

        List<RelatorioVendasDTO.Semana> semanas = new ArrayList<>();
        for (Map.Entry<LocalDate, double[]> entrada : porSemana.entrySet()) {
            LocalDate inicio = entrada.getKey();
            semanas.add(new RelatorioVendasDTO.Semana(
                    inicio, inicio.plusDays(6),
                    (int) entrada.getValue()[0], arredondar(entrada.getValue()[1])));
        }

        return new RelatorioVendasDTO(
                new RelatorioVendasDTO.Resumo(quantidadeMes, arredondar(totalMes)), dias, semanas);
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    public java.util.List<com.eric.dead_line_store_spring.dto.VendaResponseDTO> vendasDoDia(LocalDate dia) {
        return vendaService.listarDoDia(dia);
    }
}
