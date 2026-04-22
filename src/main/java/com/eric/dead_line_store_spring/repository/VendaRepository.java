package com.eric.dead_line_store_spring.repository;


import com.eric.dead_line_store_spring.entity.Venda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendaRepository extends JpaRepository <Venda, Long> {
}
