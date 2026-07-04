package com.pruebatecnica.ventas.service.impl;

import com.pruebatecnica.ventas.domain.Articulo;
import com.pruebatecnica.ventas.domain.Cliente;
import com.pruebatecnica.ventas.domain.EstadoVenta;
import com.pruebatecnica.ventas.domain.Venta;
import com.pruebatecnica.ventas.domain.VentaDetalle;
import com.pruebatecnica.ventas.dto.common.PageResponseDTO;
import com.pruebatecnica.ventas.dto.venta.VentaDetalleRequestDTO;
import com.pruebatecnica.ventas.dto.venta.VentaRequestDTO;
import com.pruebatecnica.ventas.dto.venta.VentaResponseDTO;
import com.pruebatecnica.ventas.exception.BusinessException;
import com.pruebatecnica.ventas.exception.InsufficientStockException;
import com.pruebatecnica.ventas.exception.ResourceNotFoundException;
import com.pruebatecnica.ventas.mapper.VentaMapper;
import com.pruebatecnica.ventas.repository.ArticuloRepository;
import com.pruebatecnica.ventas.repository.ClienteRepository;
import com.pruebatecnica.ventas.repository.VentaRepository;
import com.pruebatecnica.ventas.repository.VentaSpecifications;
import com.pruebatecnica.ventas.service.VentaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ArticuloRepository articuloRepository;

    public VentaServiceImpl(VentaRepository ventaRepository,
                             ClienteRepository clienteRepository,
                             ArticuloRepository articuloRepository) {
        this.ventaRepository = ventaRepository;
        this.clienteRepository = clienteRepository;
        this.articuloRepository = articuloRepository;
    }

    @Override
    @Transactional
    public VentaResponseDTO crear(VentaRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + dto.getClienteId()));

        if (!cliente.isActivo()) {
            throw new BusinessException("No se puede registrar una venta para un cliente inactivo");
        }

        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setEstado(EstadoVenta.ACTIVA);
        venta.setFecha(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (VentaDetalleRequestDTO detalleDTO : dto.getDetalles()) {
            // Se usa buscarPorIdParaActualizarStock (bloqueo pesimista) en lugar de
            // findById para evitar que dos ventas concurrentes descuenten el mismo
            // stock a la vez y terminen dejándolo en negativo.
            Articulo articulo = articuloRepository.buscarPorIdParaActualizarStock(detalleDTO.getArticuloId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Artículo no encontrado con id: " + detalleDTO.getArticuloId()));

            if (!articulo.isActivo()) {
                throw new BusinessException("El artículo '" + articulo.getCodigo() + "' no está disponible");
            }

            if (articulo.getStock() < detalleDTO.getCantidad()) {
                throw new InsufficientStockException("Stock insuficiente para el artículo '" + articulo.getCodigo()
                        + "'. Disponible: " + articulo.getStock() + ", solicitado: " + detalleDTO.getCantidad());
            }

            articulo.setStock(articulo.getStock() - detalleDTO.getCantidad());
            articuloRepository.save(articulo);

            VentaDetalle detalle = new VentaDetalle();
            detalle.setArticulo(articulo);
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(articulo.getPrecio());

            // El total NUNCA se toma de lo que envía el cliente: se calcula
            // siempre en el servidor a partir del precio real del artículo.
            BigDecimal subtotal = articulo.getPrecio().multiply(BigDecimal.valueOf(detalleDTO.getCantidad()));
            detalle.setSubtotal(subtotal);

            venta.agregarDetalle(detalle);
            total = total.add(subtotal);
        }

        venta.setTotal(total);

        // Se guarda primero para obtener el id autogenerado y usarlo como base
        // del folio; luego se actualiza la venta con el folio ya calculado.
        Venta guardada = ventaRepository.save(venta);
        guardada.setFolio(generarFolio(guardada.getId()));
        guardada = ventaRepository.save(guardada);

        return VentaMapper.toResponseDTO(guardada);
    }

    private String generarFolio(Long id) {
        return String.format("V-%06d", id);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDTO obtenerPorId(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));
        return VentaMapper.toResponseDTO(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<VentaResponseDTO> listar(Long clienteId, EstadoVenta estado,
                                                      LocalDateTime desde, LocalDateTime hasta,
                                                      Pageable pageable) {
        Specification<Venta> spec = Specification
                .where(VentaSpecifications.conClienteId(clienteId))
                .and(VentaSpecifications.conEstado(estado))
                .and(VentaSpecifications.conFechaDesde(desde))
                .and(VentaSpecifications.conFechaHasta(hasta));

        Page<Venta> page = ventaRepository.findAll(spec, pageable);
        return new PageResponseDTO<>(page.map(VentaMapper::toResponseDTO));
    }

    @Override
    @Transactional
    public VentaResponseDTO cancelar(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));

        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new BusinessException("La venta ya se encuentra cancelada");
        }

        for (VentaDetalle detalle : venta.getDetalles()) {
            Articulo articulo = articuloRepository.buscarPorIdParaActualizarStock(detalle.getArticulo().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Artículo no encontrado con id: " + detalle.getArticulo().getId()));
            articulo.setStock(articulo.getStock() + detalle.getCantidad());
            articuloRepository.save(articulo);
        }

        venta.setEstado(EstadoVenta.CANCELADA);
        Venta actualizada = ventaRepository.save(venta);
        return VentaMapper.toResponseDTO(actualizada);
    }
}
