package com.ebv14.backend.exception;

/**
 * Excepción personalizada para errores relacionados con categorías
 */
public class CategoriaException extends RuntimeException {
    private final String codigoError;

    public CategoriaException(String mensaje, String codigoError) {
        super(mensaje);
        this.codigoError = codigoError;
    }

    public CategoriaException(String mensaje) {
        this(mensaje, "CATEGORIA_ERROR");
    }

    public String getCodigoError() {
        return codigoError;
    }
}
