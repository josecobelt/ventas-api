package com.pruebatecnica.ventas.service;

import com.pruebatecnica.ventas.domain.Articulo;
import com.pruebatecnica.ventas.domain.Cliente;
import com.pruebatecnica.ventas.domain.EstadoVenta;
import com.pruebatecnica.ventas.domain.Venta;
import com.pruebatecnica.ventas.domain.VentaDetalle;
import com.pruebatecnica.ventas.dto.venta.VentaDetalleRequestDTO;
import com.pruebatecnica.ventas.dto.venta.VentaRequestDTO;
import com.pruebatecnica.ventas.dto.venta.VentaResponseDTO;
import com.pruebatecnica.ventas.exception.BusinessException;
import com.pruebatecnica.ventas.exception.InsufficientStockException;
import com.pruebatecnica.ventas.exception.ResourceNotFoundException;
import com.pruebatecnica.ventas.repository.ArticuloRepository;
import com.pruebatecnica.ventas.repository.ClienteRepository;
import com.pruebatecnica.ventas.repository.VentaRepository;
import com.pruebatecnica.ventas.service.impl.VentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceImplTest {

    @Mock
    private VentaRepository ventaRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ArticuloRepository articuloRepository;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private Cliente clienteActivo;
    private Articulo teclado;
    private Articulo mouse;

    @BeforeEach
    void setUp() {
        clienteActivo = new Cliente();
        clienteActivo.setId(1L);
        clienteActivo.setNombre("Juan Pérez");
        clienteActivo.setEmail("juan@example.com");
        clienteActivo.setActivo(true);

        teclado = new Articulo();
        teclado.setId(10L);
        teclado.setCodigo("ART-001");
        teclado.setNombre("Teclado");
        teclado.setPrecio(new BigDecimal("250.00"));
        teclado.setStock(20);
        teclado.setActivo(true);

        mouse = new Articulo();
        mouse.setId(11L);
        mouse.setCodigo("ART-002");
        mouse.setNombre("Mouse");
        mouse.setPrecio(new BigDecimal("150.50"));
        mouse.setStock(5);
        mouse.setActivo(true);
    }

    @Test
    void crear_calculaTotalCorrectamenteYDescuentaStock() {
        VentaDetalleRequestDTO detalle1 = new VentaDetalleRequestDTO();
        detalle1.setArticuloId(10L);
        detalle1.setCantidad(2);

        VentaDetalleRequestDTO detalle2 = new VentaDetalleRequestDTO();
        detalle2.setArticuloId(11L);
        detalle2.setCantidad(3);

        VentaRequestDTO request = new VentaRequestDTO();
        request.setClienteId(1L);
        request.setDetalles(Arrays.asList(detalle1, detalle2));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));
        when(articuloRepository.buscarPorIdParaActualizarStock(10L)).thenReturn(Optional.of(teclado));
        when(articuloRepository.buscarPorIdParaActualizarStock(11L)).thenReturn(Optional.of(mouse));
        when(articuloRepository.save(any(Articulo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            if (v.getId() == null) {
                v.setId(100L);
            }
            return v;
        });

        VentaResponseDTO response = ventaService.crear(request);

        // Total esperado: (250.00 * 2) + (150.50 * 3) = 500.00 + 451.50 = 951.50
        assertEquals(new BigDecimal("951.50"), response.getTotal());
        assertEquals("V-000100", response.getFolio());
        assertEquals(EstadoVenta.ACTIVA, response.getEstado());
        assertEquals(18, teclado.getStock()); // 20 - 2
        assertEquals(2, mouse.getStock());    // 5 - 3

        verify(articuloRepository, times(2)).save(any(Articulo.class));
        verify(ventaRepository, times(2)).save(any(Venta.class));
    }

    @Test
    void crear_lanzaExcepcionSiStockInsuficiente() {
        VentaDetalleRequestDTO detalle = new VentaDetalleRequestDTO();
        detalle.setArticuloId(11L);
        detalle.setCantidad(10); // solo hay 5 unidades en stock

        VentaRequestDTO request = new VentaRequestDTO();
        request.setClienteId(1L);
        request.setDetalles(Collections.singletonList(detalle));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));
        when(articuloRepository.buscarPorIdParaActualizarStock(11L)).thenReturn(Optional.of(mouse));

        assertThrows(InsufficientStockException.class, () -> ventaService.crear(request));

        verify(articuloRepository, never()).save(any());
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void crear_lanzaExcepcionSiClienteNoExiste() {
        VentaDetalleRequestDTO detalle = new VentaDetalleRequestDTO();
        detalle.setArticuloId(10L);
        detalle.setCantidad(1);

        VentaRequestDTO request = new VentaRequestDTO();
        request.setClienteId(99L);
        request.setDetalles(Collections.singletonList(detalle));

        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ventaService.crear(request));
        verify(articuloRepository, never()).buscarPorIdParaActualizarStock(any());
    }

    @Test
    void crear_lanzaExcepcionSiClienteEstaInactivo() {
        clienteActivo.setActivo(false);

        VentaDetalleRequestDTO detalle = new VentaDetalleRequestDTO();
        detalle.setArticuloId(10L);
        detalle.setCantidad(1);

        VentaRequestDTO request = new VentaRequestDTO();
        request.setClienteId(1L);
        request.setDetalles(Collections.singletonList(detalle));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));

        assertThrows(BusinessException.class, () -> ventaService.crear(request));
    }

    @Test
    void cancelar_reponeStockYCambiaEstado() {
        Venta venta = new Venta();
        venta.setId(100L);
        venta.setFolio("V-000100");
        venta.setEstado(EstadoVenta.ACTIVA);
        venta.setCliente(clienteActivo);
        venta.setTotal(new BigDecimal("500.00"));

        teclado.setStock(18); // stock ya descontado previamente por la venta

        VentaDetalle detalle = new VentaDetalle();
        detalle.setArticulo(teclado);
        detalle.setCantidad(2);
        detalle.setPrecioUnitario(teclado.getPrecio());
        detalle.setSubtotal(new BigDecimal("500.00"));
        venta.agregarDetalle(detalle);

        when(ventaRepository.findById(100L)).thenReturn(Optional.of(venta));
        when(articuloRepository.buscarPorIdParaActualizarStock(10L)).thenReturn(Optional.of(teclado));
        when(articuloRepository.save(any(Articulo.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        VentaResponseDTO response = ventaService.cancelar(100L);

        assertEquals(EstadoVenta.CANCELADA, response.getEstado());
        assertEquals(20, teclado.getStock()); // 18 + 2 repuesto
    }

    @Test
    void cancelar_lanzaExcepcionSiYaEstaCancelada() {
        Venta venta = new Venta();
        venta.setId(100L);
        venta.setEstado(EstadoVenta.CANCELADA);
        venta.setCliente(clienteActivo);
        venta.setTotal(BigDecimal.ZERO);

        when(ventaRepository.findById(100L)).thenReturn(Optional.of(venta));

        assertThrows(BusinessException.class, () -> ventaService.cancelar(100L));
        verify(ventaRepository, never()).save(any());
    }
}
