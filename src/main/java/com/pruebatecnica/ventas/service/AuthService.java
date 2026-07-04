package com.pruebatecnica.ventas.service;

import com.pruebatecnica.ventas.dto.auth.LoginRequestDTO;
import com.pruebatecnica.ventas.dto.auth.LoginResponseDTO;

public interface AuthService {

    LoginResponseDTO login(LoginRequestDTO request);
}
