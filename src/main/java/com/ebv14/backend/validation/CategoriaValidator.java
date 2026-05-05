package com.ebv14.backend.validation;

import com.ebv14.backend.dto.CategoriaDTO;
import com.ebv14.backend.exception.CategoriaException;
import org.springframework.stereotype.Component;

/**
 * Validador centralizado para operaciones de categorías
 * Responsabilidad única: validar datos de entrada
 */
@Component
public class CategoriaValidator {

    /**
     * Valida que el nombre no esté vacío o sea nulo
     */
    public void validarNombreObligatorio(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new CategoriaException("El nombre de la categoría es obligatorio", "NOMBRE_VACIO");
        }
    }

    /**
     * Valida el formato del color hexadecimal
     */
    public void validarColorHexadecimal(String color) {
        if (color != null && !color.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new CategoriaException(
                "El color debe ser un código hexadecimal válido (ej: #FF5733)",
                "COLOR_INVALIDO"
            );
        }
    }

    /**
     * Valida que no exista una categoría duplicada
     */
    public void validarCategoriaDuplicada(boolean existe, String nombre) {
        if (existe) {
            throw new CategoriaException(
                "Ya existe una categoría con el nombre: " + nombre,
                "CATEGORIA_DUPLICADA"
            );
        }
    }

    /**
     * Valida toda la request de creación
     */
    public void validarRequestCreacion(CategoriaDTO.Request request) {
        validarNombreObligatorio(request.getNombre());
        validarColorHexadecimal(request.getColor());
    }

    /**
     * Valida toda la request de actualización
     */
    public void validarRequestActualizacion(CategoriaDTO.Request request) {
        validarNombreObligatorio(request.getNombre());
        validarColorHexadecimal(request.getColor());
    }
}
