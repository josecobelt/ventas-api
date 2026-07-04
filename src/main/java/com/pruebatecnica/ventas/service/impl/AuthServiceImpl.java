package com.pruebatecnica.ventas.service.impl;

import com.pruebatecnica.ventas.dto.auth.LoginRequestDTO;
import com.pruebatecnica.ventas.dto.auth.LoginResponseDTO;
import com.pruebatecnica.ventas.security.JwtTokenProvider;
import com.pruebatecnica.ventas.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        // Si las credenciales son inválidas, AuthenticationManager lanza
        // BadCredentialsException, que es traducida a HTTP 401 por
        // GlobalExceptionHandler.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String token = jwtTokenProvider.generateToken(authentication.getName());
        return new LoginResponseDTO(token, "Bearer", jwtTokenProvider.getExpirationMs());
    }
}
