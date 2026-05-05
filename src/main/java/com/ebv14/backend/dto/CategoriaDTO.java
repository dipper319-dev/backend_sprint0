package com.ebv14.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDateTime;

public class CategoriaDTO {

    @Data
    public static class Request {
        @NotBlank(message = "El nombre de la categoría es obligatorio")
        private String nombre;

        private String descripcion;

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color debe ser un código hexadecimal válido (ej: #FF5733)")
        private String color;
    }

    @Data
    public static class Response {
        private Long id;
        private String nombre;
        private String descripcion;
        private String color;
        private LocalDateTime fechaCreacion;

        public Response(com.ebv14.backend.model.Categoria categoria) {
            this.id = categoria.getId();
            this.nombre = categoria.getNombre();
            this.descripcion = categoria.getDescripcion();
            this.color = categoria.getColor();
            this.fechaCreacion = categoria.getFechaCreacion();
        }
    }

    /**
     * DTO simplificado para listado en UI (frontend)
     * Contiene solo nombre y color para optimizar la transmisión de datos
     */
    @Data
    public static class Simple {
        private String nombre;
        private String color;

        public Simple(String nombre, String color) {
            this.nombre = nombre;
            this.color = color;
        }
    }
}
