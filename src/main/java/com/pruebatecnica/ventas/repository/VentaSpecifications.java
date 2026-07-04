package com.pruebatecnica.ventas.repository;

import com.pruebatecnica.ventas.domain.EstadoVenta;
import com.pruebatecnica.ventas.domain.Venta;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Filtros reutilizables para el listado de ventas (extra: "ventas por
 * cliente o por rango de fechas"). Cada método devuelve null cuando el
 * criterio no aplica, y Specification.where(...).and(...) ignora los nulos.
 */
public final class VentaSpecifications {

    private VentaSpecifications() {
    }

    public static Specification<Venta> conClienteId(Long clienteId) {
        return (root, query, cb) -> clienteId == null
                ? null
                : cb.equal(root.get("cliente").get("id"), clienteId);
    }

    public static Specification<Venta> conEstado(EstadoVenta estado) {
        return (root, query, cb) -> estado == null
                ? null
                : cb.equal(root.get("estado"), estado);
    }

    public static Specification<Venta> conFechaDesde(LocalDateTime desde) {
        return (root, query, cb) -> desde == null
                ? null
                : cb.greaterThanOrEqualTo(root.get("fecha"), desde);
    }

    public static Specification<Venta> conFechaHasta(LocalDateTime hasta) {
        return (root, query, cb) -> hasta == null
                ? null
                : cb.lessThanOrEqualTo(root.get("fecha"), hasta);
    }
}
