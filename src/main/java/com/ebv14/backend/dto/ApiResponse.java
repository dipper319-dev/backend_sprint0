package com.ebv14.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO estándar para todas las respuestas de API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean exitoso;
    private String mensaje;
    private T data;
    private Integer cantidad;
    private LocalDateTime timestamp;
    private String codigoError;

    public ApiResponse(boolean exitoso, String mensaje, T data) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> exitoso(String mensaje, T data) {
        return ApiResponse.<T>builder()
                .exitoso(true)
                .mensaje(mensaje)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> exitoso(String mensaje, T data, Integer cantidad) {
        ApiResponse<T> response = exitoso(mensaje, data);
        response.setCantidad(cantidad);
        return response;
    }

    public static ApiResponse<?> error(String mensaje) {
        return ApiResponse.builder()
                .exitoso(false)
                .mensaje(mensaje)
                .timestamp(LocalDateTime.now())
                .codigoError("ERROR_GENERAL")
                .build();
    }

    public static ApiResponse<?> error(String mensaje, String codigoError) {
        return ApiResponse.builder()
                .exitoso(false)
                .mensaje(mensaje)
                .timestamp(LocalDateTime.now())
                .codigoError(codigoError)
                .build();
    }
}
