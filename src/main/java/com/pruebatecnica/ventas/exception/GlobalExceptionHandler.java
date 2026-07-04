package com.pruebatecnica.ventas.exception;

import com.pruebatecnica.ventas.dto.common.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Centraliza el manejo de errores de toda la API para devolver siempre el
 * mismo formato de respuesta ({@link ErrorResponseDTO}) con el código HTTP
 * apropiado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), request, null);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
        return construir(HttpStatus.CONFLICT, "Recurso duplicado", ex.getMessage(), request, null);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponseDTO> handleInsufficientStock(InsufficientStockException ex, HttpServletRequest request) {
        return construir(HttpStatus.CONFLICT, "Stock insuficiente", ex.getMessage(), request, null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, "Regla de negocio violada", ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList());
        return construir(HttpStatus.BAD_REQUEST, "Error de validación", "Uno o más campos son inválidos", request, detalles);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return construir(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", "Usuario o contraseña incorrectos", request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return construir(HttpStatus.FORBIDDEN, "Acceso denegado", "No tiene permisos para realizar esta acción", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex, HttpServletRequest request) {
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrió un error inesperado", request, null);
    }

    private ResponseEntity<ErrorResponseDTO> construir(HttpStatus status, String error, String message,
                                                         HttpServletRequest request, List<String> detalles) {
        ErrorResponseDTO body = new ErrorResponseDTO(
                LocalDateTime.now(), status.value(), error, message, request.getRequestURI(), detalles);
        return ResponseEntity.status(status).body(body);
    }
}
