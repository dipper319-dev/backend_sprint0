package com.ebv14.backend.config;

import com.ebv14.backend.dto.ApiResponse;
import com.ebv14.backend.exception.CategoriaException;
import com.ebv14.backend.exception.UsuarioNoEncontradoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Manejador centralizado de excepciones para toda la aplicación
 * Responsabilidad única: Convertir excepciones en respuestas HTTP consistentes
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones personalizadas de categorías
     */
    @ExceptionHandler(CategoriaException.class)
    public ResponseEntity<ApiResponse<?>> handleCategoriaException(CategoriaException ex) {
        log.warn("Error de categoría: {}", ex.getMessage());
        
        ApiResponse<?> response = ApiResponse.builder()
                .exitoso(false)
                .mensaje(ex.getMessage())
                .codigoError(ex.getCodigoError())
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Maneja excepciones cuando usuario no es encontrado
     */
    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ApiResponse<?>> handleUsuarioNoEncontradoException(UsuarioNoEncontradoException ex) {
        log.warn("Usuario no encontrado: {}", ex.getMessage());
        
        ApiResponse<?> response = ApiResponse.builder()
                .exitoso(false)
                .mensaje(ex.getMessage())
                .codigoError("USUARIO_NO_ENCONTRADO")
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Maneja errores de validación de request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Error de validación en request");
        
        String mensajeErrores = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        ApiResponse<?> response = ApiResponse.builder()
                .exitoso(false)
                .mensaje("Error de validación: " + mensajeErrores)
                .codigoError("VALIDACION_FALLIDA")
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Maneja todas las demás excepciones no específicas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception ex) {
        log.error("Error inesperado", ex);
        
        ApiResponse<?> response = ApiResponse.builder()
                .exitoso(false)
                .mensaje("Error interno del servidor")
                .codigoError("ERROR_INTERNO")
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
