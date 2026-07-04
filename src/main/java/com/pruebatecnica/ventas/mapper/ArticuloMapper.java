package com.pruebatecnica.ventas.mapper;

import com.pruebatecnica.ventas.domain.Articulo;
import com.pruebatecnica.ventas.dto.articulo.ArticuloRequestDTO;
import com.pruebatecnica.ventas.dto.articulo.ArticuloResponseDTO;

public final class ArticuloMapper {

    private ArticuloMapper() {
    }

    public static Articulo toEntity(ArticuloRequestDTO dto) {
        Articulo articulo = new Articulo();
        articulo.setCodigo(dto.getCodigo());
        articulo.setNombre(dto.getNombre());
        articulo.setDescripcion(dto.getDescripcion());
        articulo.setPrecio(dto.getPrecio());
        articulo.setStock(dto.getStock());
        return articulo;
    }

    public static void actualizarEntity(Articulo articulo, ArticuloRequestDTO dto) {
        articulo.setCodigo(dto.getCodigo());
        articulo.setNombre(dto.getNombre());
        articulo.setDescripcion(dto.getDescripcion());
        articulo.setPrecio(dto.getPrecio());
        articulo.setStock(dto.getStock());
    }

    public static ArticuloResponseDTO toResponseDTO(Articulo articulo) {
        ArticuloResponseDTO dto = new ArticuloResponseDTO();
        dto.setId(articulo.getId());
        dto.setCodigo(articulo.getCodigo());
        dto.setNombre(articulo.getNombre());
        dto.setDescripcion(articulo.getDescripcion());
        dto.setPrecio(articulo.getPrecio());
        dto.setStock(articulo.getStock());
        if (articulo.getCategoria() != null) {
            dto.setCategoriaId(articulo.getCategoria().getId());
            dto.setCategoriaNombre(articulo.getCategoria().getNombre());
        }
        dto.setActivo(articulo.isActivo());
        return dto;
    }
}
