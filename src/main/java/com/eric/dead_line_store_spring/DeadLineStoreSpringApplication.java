package com.eric.dead_line_store_spring;

import com.eric.dead_line_store_spring.service.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DeadLineStoreSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeadLineStoreSpringApplication.class, args);
	}

	@Bean
	CommandLineRunner init(UsuarioService usuarioService) {
		return args -> usuarioService.criarAdminPadrao();
	}

}
