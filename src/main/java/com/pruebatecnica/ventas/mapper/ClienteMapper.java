package com.pruebatecnica.ventas.mapper;

import com.pruebatecnica.ventas.domain.Cliente;
import com.pruebatecnica.ventas.dto.cliente.ClienteRequestDTO;
import com.pruebatecnica.ventas.dto.cliente.ClienteResponseDTO;

/**
 * Mapeo manual (sin librerías de generación automática) para mantener la
 * lógica de transformación explícita y fácil de seguir.
 */
public final class ClienteMapper {

    private ClienteMapper() {
    }

    public static Cliente toEntity(ClienteRequestDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
        return cliente;
    }

    public static void actualizarEntity(Cliente cliente, ClienteRequestDTO dto) {
        cliente.setNombre(dto.getNombre());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
    }

    public static ClienteResponseDTO toResponseDTO(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setEmail(cliente.getEmail());
        dto.setTelefono(cliente.getTelefono());
        dto.setActivo(cliente.isActivo());
        dto.setFechaCreacion(cliente.getFechaCreacion());
        return dto;
    }
}
