package com.pruebatecnica.ventas.controller;

import com.pruebatecnica.ventas.dto.articulo.ArticuloRequestDTO;
import com.pruebatecnica.ventas.dto.articulo.ArticuloResponseDTO;
import com.pruebatecnica.ventas.dto.cliente.ClienteRequestDTO;
import com.pruebatecnica.ventas.dto.cliente.ClienteResponseDTO;
import com.pruebatecnica.ventas.dto.venta.VentaDetalleRequestDTO;
import com.pruebatecnica.ventas.dto.venta.VentaRequestDTO;
import com.pruebatecnica.ventas.dto.venta.VentaResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VentaControllerIT extends AbstractIntegrationTest {

    private Long crearClienteDePrueba(String token) throws Exception {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setNombre("Cliente Venta Test");
        dto.setEmail("venta.test." + UUID.randomUUID() + "@example.com");

        MvcResult result = mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), ClienteResponseDTO.class).getId();
    }

    private Long crearArticuloDePrueba(String token, int stock, String precio) throws Exception {
        ArticuloRequestDTO dto = new ArticuloRequestDTO();
        dto.setCodigo("ART-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        dto.setNombre("Artículo de prueba");
        dto.setPrecio(new BigDecimal(precio));
        dto.setStock(stock);

        MvcResult result = mockMvc.perform(post("/api/articulos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), ArticuloResponseDTO.class).getId();
    }

    @Test
    void crearVenta_calculaTotalYDescuentaStock() throws Exception {
        String token = obtenerTokenAdmin();
        Long clienteId = crearClienteDePrueba(token);
        Long articuloId = crearArticuloDePrueba(token, 10, "100.00");

        VentaDetalleRequestDTO detalle = new VentaDetalleRequestDTO();
        detalle.setArticuloId(articuloId);
        detalle.setCantidad(3);

        VentaRequestDTO ventaDto = new VentaRequestDTO();
        ventaDto.setClienteId(clienteId);
        ventaDto.setDetalles(Collections.singletonList(detalle));

        mockMvc.perform(post("/api/ventas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ventaDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.folio").exists())
                .andExpect(jsonPath("$.total").value(300.00))
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.detalles[0].subtotal").value(300.00));

        // El stock del artículo debe haber quedado descontado: 10 - 3 = 7
        mockMvc.perform(get("/api/articulos/" + articuloId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(7));
    }

    @Test
    void crearVenta_conStockInsuficienteRetornaConflictoYNoModificaStock() throws Exception {
        String token = obtenerTokenAdmin();
        Long clienteId = crearClienteDePrueba(token);
        Long articuloId = crearArticuloDePrueba(token, 2, "50.00");

        VentaDetalleRequestDTO detalle = new VentaDetalleRequestDTO();
        detalle.setArticuloId(articuloId);
        detalle.setCantidad(5); // supera el stock disponible (2)

        VentaRequestDTO ventaDto = new VentaRequestDTO();
        ventaDto.setClienteId(clienteId);
        ventaDto.setDetalles(Collections.singletonList(detalle));

        mockMvc.perform(post("/api/ventas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ventaDto)))
                .andExpect(status().isConflict());

        // El stock no debe haberse tocado
        mockMvc.perform(get("/api/articulos/" + articuloId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(2));
    }

    @Test
    void cancelarVenta_reponeStockYCambiaEstado() throws Exception {
        String token = obtenerTokenAdmin();
        Long clienteId = crearClienteDePrueba(token);
        Long articuloId = crearArticuloDePrueba(token, 10, "20.00");

        VentaDetalleRequestDTO detalle = new VentaDetalleRequestDTO();
        detalle.setArticuloId(articuloId);
        detalle.setCantidad(4);

        VentaRequestDTO ventaDto = new VentaRequestDTO();
        ventaDto.setClienteId(clienteId);
        ventaDto.setDetalles(Collections.singletonList(detalle));

        MvcResult result = mockMvc.perform(post("/api/ventas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ventaDto)))
                .andExpect(status().isCreated())
                .andReturn();

        VentaResponseDTO venta = objectMapper.readValue(result.getResponse().getContentAsString(), VentaResponseDTO.class);

        mockMvc.perform(patch("/api/ventas/" + venta.getId() + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        // El stock debió reponerse: seguía en 6 tras la venta, ahora vuelve a 10
        mockMvc.perform(get("/api/articulos/" + articuloId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(10));

        // Cancelar dos veces debe fallar
        mockMvc.perform(patch("/api/ventas/" + venta.getId() + "/cancelar")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearVenta_conClienteInexistenteRetornaNoEncontrado() throws Exception {
        String token = obtenerTokenAdmin();
        Long articuloId = crearArticuloDePrueba(token, 5, "10.00");

        VentaDetalleRequestDTO detalle = new VentaDetalleRequestDTO();
        detalle.setArticuloId(articuloId);
        detalle.setCantidad(1);

        VentaRequestDTO ventaDto = new VentaRequestDTO();
        ventaDto.setClienteId(999999L);
        ventaDto.setDetalles(Collections.singletonList(detalle));

        mockMvc.perform(post("/api/ventas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ventaDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarVentas_filtraPorCliente() throws Exception {
        String token = obtenerTokenAdmin();
        Long clienteId = crearClienteDePrueba(token);
        Long articuloId = crearArticuloDePrueba(token, 10, "15.00");

        VentaDetalleRequestDTO detalle = new VentaDetalleRequestDTO();
        detalle.setArticuloId(articuloId);
        detalle.setCantidad(1);

        VentaRequestDTO ventaDto = new VentaRequestDTO();
        ventaDto.setClienteId(clienteId);
        ventaDto.setDetalles(Collections.singletonList(detalle));

        mockMvc.perform(post("/api/ventas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ventaDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/ventas?clienteId=" + clienteId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                // Se compara como int: JsonPath deserializa los enteros JSON como Integer,
                // y compararlo directamente contra un Long (clienteId) fallaría por tipos distintos.
                .andExpect(jsonPath("$.contenido[0].clienteId").value(clienteId.intValue()));
    }
}
