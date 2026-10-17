package com.fitstore.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Manejo centralizado de errores de la API.
 *
 * <p>Todas las respuestas de error siguen el mismo contrato JSON:
 * <pre>{ "error": { "status": 400, "message": "...", "details": { "campo": "mensaje" } } }</pre>
 * El campo {@code details} solo aparece cuando hay errores de validación por campo.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Errores de negocio lanzados por los servicios (stock, credenciales, etc.). */
    @ExceptionHandler(FitStoreException.class)
    public ResponseEntity<Map<String, Object>> handleFitStore(FitStoreException ex) {
        return errorBody(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /** Errores de validación de Bean Validation (@Valid) — se devuelven por campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> details = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return errorBody(HttpStatus.BAD_REQUEST, "Error de validación", details);
    }

    /** Violaciones de restricciones de la base de datos (unique, foreign key, check). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn(
                "Violación de integridad de datos: {}",
                ex.getMostSpecificCause().getMessage());
        return errorBody(HttpStatus.CONFLICT, "La operación viola una restricción de la base de datos", null);
    }

    /** Cuerpo de la petición con JSON inválido o ilegible. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Cuerpo de la petición ilegible: {}", ex.getMessage());
        return errorBody(HttpStatus.BAD_REQUEST, "El cuerpo de la petición está mal formado o no es JSON válido", null);
    }

    /** Parámetro con tipo incorrecto (p. ej. id no numérico). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return errorBody(HttpStatus.BAD_REQUEST, "Parámetro inválido: " + ex.getName(), null);
    }

    /** Cualquier excepción no controlada: se registra y se devuelve un mensaje genérico. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Error interno no controlado", ex);
        return errorBody(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", null);
    }

    /**
     * Construye el cuerpo de error evitando valores {@code null}
     * (a diferencia de {@code Map.of}, que lanza NullPointerException).
     */
    private ResponseEntity<Map<String, Object>> errorBody(
            HttpStatus status, String message, Map<String, Object> details) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", status.value());
        error.put("message", message != null ? message : status.getReasonPhrase());
        if (details != null && !details.isEmpty()) {
            error.put("details", details);
        }
        return ResponseEntity.status(status).body(Map.of("error", error));
    }
}
