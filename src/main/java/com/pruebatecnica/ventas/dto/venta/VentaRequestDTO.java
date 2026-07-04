package com.pruebatecnica.ventas.dto.venta;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public class VentaRequestDTO {

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    @NotEmpty(message = "La venta debe tener al menos un detalle")
    @Valid
    private List<VentaDetalleRequestDTO> detalles;

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<VentaDetalleRequestDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<VentaDetalleRequestDTO> detalles) {
        this.detalles = detalles;
    }
}
