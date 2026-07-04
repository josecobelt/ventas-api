package com.pruebatecnica.ventas.dto.venta;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class VentaDetalleRequestDTO {

    @NotNull(message = "El artículo es obligatorio")
    private Long articuloId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    public Long getArticuloId() {
        return articuloId;
    }

    public void setArticuloId(Long articuloId) {
        this.articuloId = articuloId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
