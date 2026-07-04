package com.pruebatecnica.ventas.repository;

import com.pruebatecnica.ventas.domain.VentaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaDetalleRepository extends JpaRepository<VentaDetalle, Long> {

    /**
     * Permite saber si un artículo ya fue vendido alguna vez, para impedir
     * su eliminación física (y así no romper el historial de ventas).
     */
    boolean existsByArticuloId(Long articuloId);
}
