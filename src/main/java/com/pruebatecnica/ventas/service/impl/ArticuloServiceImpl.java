package com.pruebatecnica.ventas.service.impl;

import com.pruebatecnica.ventas.domain.Articulo;
import com.pruebatecnica.ventas.domain.Categoria;
import com.pruebatecnica.ventas.dto.articulo.ArticuloRequestDTO;
import com.pruebatecnica.ventas.dto.articulo.ArticuloResponseDTO;
import com.pruebatecnica.ventas.dto.common.PageResponseDTO;
import com.pruebatecnica.ventas.exception.BusinessException;
import com.pruebatecnica.ventas.exception.DuplicateResourceException;
import com.pruebatecnica.ventas.exception.ResourceNotFoundException;
import com.pruebatecnica.ventas.mapper.ArticuloMapper;
import com.pruebatecnica.ventas.repository.ArticuloRepository;
import com.pruebatecnica.ventas.repository.CategoriaRepository;
import com.pruebatecnica.ventas.repository.VentaDetalleRepository;
import com.pruebatecnica.ventas.service.ArticuloService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticuloServiceImpl implements ArticuloService {

    private final ArticuloRepository articuloRepository;
    private final CategoriaRepository categoriaRepository;
    private final VentaDetalleRepository ventaDetalleRepository;

    public ArticuloServiceImpl(ArticuloRepository articuloRepository,
                                CategoriaRepository categoriaRepository,
                                VentaDetalleRepository ventaDetalleRepository) {
        this.articuloRepository = articuloRepository;
        this.categoriaRepository = categoriaRepository;
        this.ventaDetalleRepository = ventaDetalleRepository;
    }

    @Override
    @Transactional
    public ArticuloResponseDTO crear(ArticuloRequestDTO dto) {
        if (articuloRepository.existsByCodigoIgnoreCase(dto.getCodigo())) {
            throw new DuplicateResourceException("Ya existe un artículo con el código: " + dto.getCodigo());
        }
        Articulo articulo = ArticuloMapper.toEntity(dto);
        articulo.setCategoria(resolverCategoria(dto.getCategoriaId()));
        articulo.setActivo(true);
        return ArticuloMapper.toResponseDTO(articuloRepository.save(articulo));
    }

    @Override
    @Transactional
    public ArticuloResponseDTO actualizar(Long id, ArticuloRequestDTO dto) {
        Articulo articulo = obtenerEntidadPorId(id);

        boolean codigoCambio = !articulo.getCodigo().equalsIgnoreCase(dto.getCodigo());
        if (codigoCambio && articuloRepository.existsByCodigoIgnoreCase(dto.getCodigo())) {
            throw new DuplicateResourceException("Ya existe un artículo con el código: " + dto.getCodigo());
        }

        ArticuloMapper.actualizarEntity(articulo, dto);
        articulo.setCategoria(resolverCategoria(dto.getCategoriaId()));
        return ArticuloMapper.toResponseDTO(articuloRepository.save(articulo));
    }

    @Override
    @Transactional(readOnly = true)
    public ArticuloResponseDTO obtenerPorId(Long id) {
        return ArticuloMapper.toResponseDTO(obtenerEntidadPorId(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<ArticuloResponseDTO> listar(Pageable pageable, boolean soloActivos) {
        Page<Articulo> page = soloActivos
                ? articuloRepository.findByActivoTrue(pageable)
                : articuloRepository.findAll(pageable);
        return new PageResponseDTO<>(page.map(ArticuloMapper::toResponseDTO));
    }

    @Override
    @Transactional
    public void eliminar(Long id, boolean permanente) {
        Articulo articulo = obtenerEntidadPorId(id);

        if (permanente) {
            if (ventaDetalleRepository.existsByArticuloId(id)) {
                throw new BusinessException(
                        "No se puede eliminar físicamente un artículo con ventas asociadas. Use baja lógica.");
            }
            articuloRepository.delete(articulo);
        } else {
            articulo.setActivo(false);
            articuloRepository.save(articulo);
        }
    }

    private Articulo obtenerEntidadPorId(Long id) {
        return articuloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado con id: " + id));
    }

    private Categoria resolverCategoria(Long categoriaId) {
        if (categoriaId == null) {
            return null;
        }
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + categoriaId));
    }
}
