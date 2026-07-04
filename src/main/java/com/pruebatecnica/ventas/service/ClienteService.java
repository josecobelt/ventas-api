package com.pruebatecnica.ventas.service;

import com.pruebatecnica.ventas.dto.cliente.ClienteRequestDTO;
import com.pruebatecnica.ventas.dto.cliente.ClienteResponseDTO;
import com.pruebatecnica.ventas.dto.common.PageResponseDTO;
import org.springframework.data.domain.Pageable;

public interface ClienteService {

    ClienteResponseDTO crear(ClienteRequestDTO dto);

    ClienteResponseDTO actualizar(Long id, ClienteRequestDTO dto);

    ClienteResponseDTO obtenerPorId(Long id);

    PageResponseDTO<ClienteResponseDTO> listar(Pageable pageable, boolean soloActivos);

    /**
     * @param permanente si es true, intenta un borrado físico (falla si el
     *                   cliente tiene ventas asociadas); si es false, realiza
     *                   una baja lógica (activo = false).
     */
    void eliminar(Long id, boolean permanente);
}
