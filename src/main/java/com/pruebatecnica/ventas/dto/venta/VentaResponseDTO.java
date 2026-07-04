package com.pruebatecnica.ventas.dto.venta;

import com.pruebatecnica.ventas.domain.EstadoVenta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class VentaResponseDTO {

    private Long id;
    private String folio;
    private Long clienteId;
    private String clienteNombre;
    private LocalDateTime fecha;
    private EstadoVenta estado;
    private BigDecimal total;
    private List<VentaDetalleResponseDTO> detalles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public EstadoVenta getEstado() {
        return estado;
    }

    public void setEstado(EstadoVenta estado) {
        this.estado = estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<VentaDetalleResponseDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<VentaDetalleResponseDTO> detalles) {
        this.detalles = detalles;
    }
}
