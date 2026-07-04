package com.pruebatecnica.ventas.repository;

import com.pruebatecnica.ventas.domain.Articulo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface ArticuloRepository extends JpaRepository<Articulo, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    Optional<Articulo> findByCodigoIgnoreCase(String codigo);

    Page<Articulo> findByActivoTrue(Pageable pageable);

    /**
     * Igual que findById, pero toma un bloqueo pesimista de escritura sobre la fila.
     * Se usa exclusivamente al descontar o reponer stock dentro de una transacción
     * de venta, para evitar condiciones de carrera (sobreventa) cuando dos ventas
     * concurrentes intentan descontar el mismo artículo al mismo tiempo.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Articulo a where a.id = :id")
    Optional<Articulo> buscarPorIdParaActualizarStock(@Param("id") Long id);
}
