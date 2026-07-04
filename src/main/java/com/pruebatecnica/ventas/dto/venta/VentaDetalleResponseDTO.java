package com.pruebatecnica.ventas.dto.venta;

import java.math.BigDecimal;

public class VentaDetalleResponseDTO {

    private Long articuloId;
    private String articuloCodigo;
    private String articuloNombre;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public Long getArticuloId() {
        return articuloId;
    }

    public void setArticuloId(Long articuloId) {
        this.articuloId = articuloId;
    }

    public String getArticuloCodigo() {
        return articuloCodigo;
    }

    public void setArticuloCodigo(String articuloCodigo) {
        this.articuloCodigo = articuloCodigo;
    }

    public String getArticuloNombre() {
        return articuloNombre;
    }

    public void setArticuloNombre(String articuloNombre) {
        this.articuloNombre = articuloNombre;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
