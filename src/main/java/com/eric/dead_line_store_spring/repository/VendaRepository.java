package com.eric.dead_line_store_spring.repository;

import com.eric.dead_line_store_spring.entity.Venda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    List<Venda> findByDataGreaterThanEqualAndDataLessThanOrderByData(
            LocalDateTime inicio, LocalDateTime fim);
}
