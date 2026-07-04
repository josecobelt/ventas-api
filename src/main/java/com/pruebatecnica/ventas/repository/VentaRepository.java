package com.pruebatecnica.ventas.repository;

import com.pruebatecnica.ventas.domain.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VentaRepository extends JpaRepository<Venta, Long>, JpaSpecificationExecutor<Venta> {

    boolean existsByFolio(String folio);

    boolean existsByClienteId(Long clienteId);
}
