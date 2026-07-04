package com.pruebatecnica.ventas.mapper;

import com.pruebatecnica.ventas.domain.Categoria;
import com.pruebatecnica.ventas.dto.categoria.CategoriaResponseDTO;

public final class CategoriaMapper {

    private CategoriaMapper() {
    }

    public static CategoriaResponseDTO toResponseDTO(Categoria categoria) {
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        dto.setDescripcion(categoria.getDescripcion());
        return dto;
    }
}
