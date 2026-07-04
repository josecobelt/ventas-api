package com.pruebatecnica.ventas.controller;

import com.pruebatecnica.ventas.dto.articulo.ArticuloRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ArticuloControllerIT extends AbstractIntegrationTest {

    private String codigoUnico() {
        return "ART-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Test
    void crearArticulo_conDatosValidosRetornaCreado() throws Exception {
        String token = obtenerTokenAdmin();

        ArticuloRequestDTO dto = new ArticuloRequestDTO();
        dto.setCodigo(codigoUnico());
        dto.setNombre("Teclado mecánico");
        dto.setDescripcion("Teclado retroiluminado");
        dto.setPrecio(new BigDecimal("899.99"));
        dto.setStock(25);

        mockMvc.perform(post("/api/articulos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(dto.getCodigo()))
                .andExpect(jsonPath("$.stock").value(25));
    }

    @Test
    void crearArticulo_conCodigoDuplicadoRetornaConflicto() throws Exception {
        String token = obtenerTokenAdmin();
        String codigo = codigoUnico();

        ArticuloRequestDTO dto = new ArticuloRequestDTO();
        dto.setCodigo(codigo);
        dto.setNombre("Primero");
        dto.setPrecio(new BigDecimal("10.00"));
        dto.setStock(1);

        mockMvc.perform(post("/api/articulos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        ArticuloRequestDTO duplicado = new ArticuloRequestDTO();
        duplicado.setCodigo(codigo);
        duplicado.setNombre("Segundo");
        duplicado.setPrecio(new BigDecimal("20.00"));
        duplicado.setStock(2);

        mockMvc.perform(post("/api/articulos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicado)))
                .andExpect(status().isConflict());
    }

    @Test
    void crearArticulo_conPrecioNegativoRetornaBadRequest() throws Exception {
        String token = obtenerTokenAdmin();

        ArticuloRequestDTO dto = new ArticuloRequestDTO();
        dto.setCodigo(codigoUnico());
        dto.setNombre("Artículo inválido");
        dto.setPrecio(new BigDecimal("-5.00"));
        dto.setStock(1);

        mockMvc.perform(post("/api/articulos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarArticulos_sinTokenRetornaNoAutorizado() throws Exception {
        mockMvc.perform(get("/api/articulos"))
                .andExpect(status().isUnauthorized());
    }
}
