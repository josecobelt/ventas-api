package com.pruebatecnica.ventas.controller;

import com.pruebatecnica.ventas.dto.categoria.CategoriaRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoriaControllerIT extends AbstractIntegrationTest {

    private String nombreUnico() {
        return "Categoria-" + UUID.randomUUID();
    }

    @Test
    void crearCategoria_conDatosValidosRetornaCreado() throws Exception {
        String token = obtenerTokenAdmin();

        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre(nombreUnico());
        dto.setDescripcion("Categoría de prueba");

        mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value(dto.getNombre()));
    }

    @Test
    void crearCategoria_conNombreDuplicadoRetornaConflicto() throws Exception {
        String token = obtenerTokenAdmin();
        String nombre = nombreUnico();

        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre(nombre);

        mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        CategoriaRequestDTO duplicada = new CategoriaRequestDTO();
        duplicada.setNombre(nombre);

        mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicada)))
                .andExpect(status().isConflict());
    }

    @Test
    void crearCategoria_conNombreVacioRetornaBadRequest() throws Exception {
        String token = obtenerTokenAdmin();

        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre("");

        mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerCategoriaPorId_flujoCompleto() throws Exception {
        String token = obtenerTokenAdmin();

        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre(nombreUnico());
        dto.setDescripcion("Otra categoría de prueba");

        MvcResult creado = mockMvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(creado.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/categorias/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value(dto.getDescripcion()));
    }

    @Test
    void listarCategorias_sinTokenRetornaNoAutorizado() throws Exception {
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarCategorias_retornaPaginado() throws Exception {
        String token = obtenerTokenAdmin();

        mockMvc.perform(get("/api/categorias?page=0&size=5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isArray())
                .andExpect(jsonPath("$.tamanioPagina").value(5));
    }
}
