package com.fitstore.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FitStoreException.class)
    public ResponseEntity<Map<String, Object>> handleFitStore(FitStoreException ex) {
        return ResponseEntity.badRequest().body(Map.of(
            "error",     ex.getMessage(),
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "error",     "Error interno del servidor",
            "detalle",   ex.getMessage(),
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}
