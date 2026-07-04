package com.pruebatecnica.ventas.mapper;

import com.pruebatecnica.ventas.domain.Venta;
import com.pruebatecnica.ventas.domain.VentaDetalle;
import com.pruebatecnica.ventas.dto.venta.VentaDetalleResponseDTO;
import com.pruebatecnica.ventas.dto.venta.VentaResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

public final class VentaMapper {

    private VentaMapper() {
    }

    public static VentaResponseDTO toResponseDTO(Venta venta) {
        VentaResponseDTO dto = new VentaResponseDTO();
        dto.setId(venta.getId());
        dto.setFolio(venta.getFolio());
        dto.setClienteId(venta.getCliente().getId());
        dto.setClienteNombre(venta.getCliente().getNombre());
        dto.setFecha(venta.getFecha());
        dto.setEstado(venta.getEstado());
        dto.setTotal(venta.getTotal());

        List<VentaDetalleResponseDTO> detalles = venta.getDetalles().stream()
                .map(VentaMapper::toDetalleResponseDTO)
                .collect(Collectors.toList());
        dto.setDetalles(detalles);

        return dto;
    }

    private static VentaDetalleResponseDTO toDetalleResponseDTO(VentaDetalle detalle) {
        VentaDetalleResponseDTO dto = new VentaDetalleResponseDTO();
        dto.setArticuloId(detalle.getArticulo().getId());
        dto.setArticuloCodigo(detalle.getArticulo().getCodigo());
        dto.setArticuloNombre(detalle.getArticulo().getNombre());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setSubtotal(detalle.getSubtotal());
        return dto;
    }
}
