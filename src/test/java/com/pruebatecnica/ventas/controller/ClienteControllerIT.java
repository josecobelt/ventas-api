package com.pruebatecnica.ventas.controller;

import com.pruebatecnica.ventas.dto.cliente.ClienteRequestDTO;
import com.pruebatecnica.ventas.dto.cliente.ClienteResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClienteControllerIT extends AbstractIntegrationTest {

    private String emailUnico(String prefijo) {
        return prefijo + "." + UUID.randomUUID() + "@example.com";
    }

    @Test
    void listar_sinTokenRetornaNoAutorizado() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearYObtenerCliente_flujoCompleto() throws Exception {
        String token = obtenerTokenAdmin();

        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setNombre("Carlos Ruiz");
        dto.setEmail(emailUnico("carlos.ruiz"));
        dto.setTelefono("555-9876");

        MvcResult creado = mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Carlos Ruiz"))
                .andExpect(jsonPath("$.activo").value(true))
                .andReturn();

        ClienteResponseDTO response = objectMapper.readValue(
                creado.getResponse().getContentAsString(), ClienteResponseDTO.class);

        mockMvc.perform(get("/api/clientes/" + response.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(dto.getEmail()));
    }

    @Test
    void crear_conEmailDuplicadoRetornaConflicto() throws Exception {
        String token = obtenerTokenAdmin();
        String email = emailUnico("duplicado");

        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setNombre("Primero");
        dto.setEmail(email);

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        ClienteRequestDTO duplicado = new ClienteRequestDTO();
        duplicado.setNombre("Segundo");
        duplicado.setEmail(email);

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicado)))
                .andExpect(status().isConflict());
    }

    @Test
    void crear_conDatosInvalidosRetornaBadRequest() throws Exception {
        String token = obtenerTokenAdmin();
        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setNombre("");
        dto.setEmail("no-es-un-email");

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles").isArray());
    }

    @Test
    void actualizarYEliminar_bajaLogica() throws Exception {
        String token = obtenerTokenAdmin();

        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setNombre("Cliente Temporal");
        dto.setEmail(emailUnico("temporal"));

        MvcResult creado = mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readValue(creado.getResponse().getContentAsString(), ClienteResponseDTO.class).getId();

        dto.setNombre("Cliente Actualizado");
        mockMvc.perform(put("/api/clientes/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Cliente Actualizado"));

        mockMvc.perform(delete("/api/clientes/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/clientes/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void listar_retornaPaginado() throws Exception {
        String token = obtenerTokenAdmin();

        mockMvc.perform(get("/api/clientes?page=0&size=5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isArray())
                .andExpect(jsonPath("$.tamanioPagina").value(5));
    }
}
