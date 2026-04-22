package com.eric.dead_line_store_spring.controller;

import com.eric.dead_line_store_spring.dto.LoginRequestDTO;
import com.eric.dead_line_store_spring.dto.LoginResponseDTO;
import com.eric.dead_line_store_spring.security.JwtService;
import com.eric.dead_line_store_spring.service.CustomUserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final com.eric.dead_line_store_spring.service.CustomUserDetailsService userDetailsService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            CustomUserDetailsService userDetailsService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.senha()
                )
        );

        UserDetails user = userDetailsService.loadUserByUsername(request.username());

        String token = jwtService.gerarToken(user);

        return new LoginResponseDTO(token);
    }
}