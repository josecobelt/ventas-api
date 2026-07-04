package com.pruebatecnica.ventas.service;

import com.pruebatecnica.ventas.domain.Articulo;
import com.pruebatecnica.ventas.dto.articulo.ArticuloRequestDTO;
import com.pruebatecnica.ventas.dto.articulo.ArticuloResponseDTO;
import com.pruebatecnica.ventas.exception.BusinessException;
import com.pruebatecnica.ventas.exception.DuplicateResourceException;
import com.pruebatecnica.ventas.repository.ArticuloRepository;
import com.pruebatecnica.ventas.repository.CategoriaRepository;
import com.pruebatecnica.ventas.repository.VentaDetalleRepository;
import com.pruebatecnica.ventas.service.impl.ArticuloServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticuloServiceImplTest {

    @Mock
    private ArticuloRepository articuloRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private VentaDetalleRepository ventaDetalleRepository;

    @InjectMocks
    private ArticuloServiceImpl articuloService;

    @Test
    void crear_lanzaExcepcionSiCodigoYaExiste() {
        ArticuloRequestDTO dto = new ArticuloRequestDTO();
        dto.setCodigo("ART-001");
        dto.setNombre("Teclado");
        dto.setPrecio(new BigDecimal("100.00"));
        dto.setStock(10);

        when(articuloRepository.existsByCodigoIgnoreCase("ART-001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> articuloService.crear(dto));
        verify(articuloRepository, never()).save(any());
    }

    @Test
    void crear_guardaArticuloCuandoCodigoNoExiste() {
        ArticuloRequestDTO dto = new ArticuloRequestDTO();
        dto.setCodigo("ART-002");
        dto.setNombre("Mouse");
        dto.setPrecio(new BigDecimal("50.00"));
        dto.setStock(15);

        when(articuloRepository.existsByCodigoIgnoreCase("ART-002")).thenReturn(false);
        when(articuloRepository.save(any(Articulo.class))).thenAnswer(inv -> {
            Articulo a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        ArticuloResponseDTO response = articuloService.crear(dto);

        assertEquals("ART-002", response.getCodigo());
        assertEquals(15, response.getStock());
        assertEquals(0, new BigDecimal("50.00").compareTo(response.getPrecio()));
    }

    @Test
    void eliminar_permanenteLanzaExcepcionSiTieneVentasAsociadas() {
        Articulo articulo = new Articulo();
        articulo.setId(5L);
        articulo.setCodigo("ART-003");

        when(articuloRepository.findById(5L)).thenReturn(Optional.of(articulo));
        when(ventaDetalleRepository.existsByArticuloId(5L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> articuloService.eliminar(5L, true));
        verify(articuloRepository, never()).delete(any());
    }

    @Test
    void eliminar_bajaLogicaMarcaArticuloComoInactivo() {
        Articulo articulo = new Articulo();
        articulo.setId(5L);
        articulo.setActivo(true);

        when(articuloRepository.findById(5L)).thenReturn(Optional.of(articulo));
        when(articuloRepository.save(any(Articulo.class))).thenAnswer(inv -> inv.getArgument(0));

        articuloService.eliminar(5L, false);

        assertEquals(false, articulo.isActivo());
        verify(articuloRepository, never()).delete(any());
    }
}
