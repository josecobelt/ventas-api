package com.pruebatecnica.ventas.service.impl;

import com.pruebatecnica.ventas.domain.Categoria;
import com.pruebatecnica.ventas.dto.categoria.CategoriaRequestDTO;
import com.pruebatecnica.ventas.dto.categoria.CategoriaResponseDTO;
import com.pruebatecnica.ventas.dto.common.PageResponseDTO;
import com.pruebatecnica.ventas.exception.DuplicateResourceException;
import com.pruebatecnica.ventas.exception.ResourceNotFoundException;
import com.pruebatecnica.ventas.mapper.CategoriaMapper;
import com.pruebatecnica.ventas.repository.CategoriaRepository;
import com.pruebatecnica.ventas.service.CategoriaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional
    public CategoriaResponseDTO crear(CategoriaRequestDTO dto) {
        if (categoriaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new DuplicateResourceException("Ya existe una categoría con el nombre: " + dto.getNombre());
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        return CategoriaMapper.toResponseDTO(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO obtenerPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
        return CategoriaMapper.toResponseDTO(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<CategoriaResponseDTO> listar(Pageable pageable) {
        Page<Categoria> page = categoriaRepository.findAll(pageable);
        return new PageResponseDTO<>(page.map(CategoriaMapper::toResponseDTO));
    }
}
