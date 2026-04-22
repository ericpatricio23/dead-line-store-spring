package com.eric.dead_line_store_spring.repository;

import com.eric.dead_line_store_spring.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
