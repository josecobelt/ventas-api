package com.pruebatecnica.ventas.exception;

/**
 * Excepción genérica para violaciones de reglas de negocio que no encajan
 * en una categoría más específica (por ejemplo: cancelar una venta que ya
 * está cancelada, o eliminar físicamente un registro que tiene relaciones).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
