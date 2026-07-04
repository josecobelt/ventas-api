package com.pruebatecnica.ventas.service;

import com.pruebatecnica.ventas.dto.categoria.CategoriaRequestDTO;
import com.pruebatecnica.ventas.dto.categoria.CategoriaResponseDTO;
import com.pruebatecnica.ventas.dto.common.PageResponseDTO;
import org.springframework.data.domain.Pageable;

public interface CategoriaService {

    CategoriaResponseDTO crear(CategoriaRequestDTO dto);

    CategoriaResponseDTO obtenerPorId(Long id);

    PageResponseDTO<CategoriaResponseDTO> listar(Pageable pageable);
}
