package com.pruebatecnica.ventas.service;

import com.pruebatecnica.ventas.dto.articulo.ArticuloRequestDTO;
import com.pruebatecnica.ventas.dto.articulo.ArticuloResponseDTO;
import com.pruebatecnica.ventas.dto.common.PageResponseDTO;
import org.springframework.data.domain.Pageable;

public interface ArticuloService {

    ArticuloResponseDTO crear(ArticuloRequestDTO dto);

    ArticuloResponseDTO actualizar(Long id, ArticuloRequestDTO dto);

    ArticuloResponseDTO obtenerPorId(Long id);

    PageResponseDTO<ArticuloResponseDTO> listar(Pageable pageable, boolean soloActivos);

    void eliminar(Long id, boolean permanente);
}
