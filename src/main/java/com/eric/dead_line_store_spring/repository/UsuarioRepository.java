package com.eric.dead_line_store_spring.repository;

import com.eric.dead_line_store_spring.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<com.eric.dead_line_store_spring.entity.Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
}
