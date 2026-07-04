package com.pruebatecnica.ventas.controller;

import com.pruebatecnica.ventas.dto.auth.LoginRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIT extends AbstractIntegrationTest {

    @Test
    void login_conCredencialesValidasRetornaToken() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("admin");
        dto.setPassword("admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    void login_conCredencialesInvalidasRetornaNoAutorizado() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("admin");
        dto.setPassword("clave-incorrecta");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_conCamposVaciosRetornaBadRequest() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("");
        dto.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
