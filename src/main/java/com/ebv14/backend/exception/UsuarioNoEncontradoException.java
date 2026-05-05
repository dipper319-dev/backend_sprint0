package com.ebv14.backend.exception;

/**
 * Excepción lanzada cuando un usuario no es encontrado
 */
public class UsuarioNoEncontradoException extends RuntimeException {
    public UsuarioNoEncontradoException(String email) {
        super("Usuario con email " + email + " no encontrado");
    }
}
