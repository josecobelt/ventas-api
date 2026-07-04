package com.pruebatecnica.ventas.service;

import com.pruebatecnica.ventas.domain.EstadoVenta;
import com.pruebatecnica.ventas.dto.common.PageResponseDTO;
import com.pruebatecnica.ventas.dto.venta.VentaRequestDTO;
import com.pruebatecnica.ventas.dto.venta.VentaResponseDTO;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface VentaService {

    VentaResponseDTO crear(VentaRequestDTO dto);

    VentaResponseDTO obtenerPorId(Long id);

    /**
     * Lista ventas con filtros opcionales (todos pueden ser null).
     */
    PageResponseDTO<VentaResponseDTO> listar(Long clienteId, EstadoVenta estado,
                                              LocalDateTime desde, LocalDateTime hasta,
                                              Pageable pageable);

    VentaResponseDTO cancelar(Long id);
}
