package com.ebv14.backend.mapper;

import com.ebv14.backend.dto.CategoriaDTO;
import com.ebv14.backend.model.Categoria;
import com.ebv14.backend.model.Usuario;
import org.springframework.stereotype.Component;

/**
 * Mapper para conversiones entre Categoria y CategoriaDTO
 * Responsabilidad única: transformar entidades a DTOs y viceversa
 */
@Component
public class CategoriaMapper {

    /**
     * Convierte un Request DTO a entidad Categoria
     */
    public Categoria toEntity(CategoriaDTO.Request request, Usuario usuario) {
        return Categoria.builder()
                .nombre(request.getNombre().trim())
                .descripcion(request.getDescripcion())
                .color(request.getColor() != null ? request.getColor() : "#000000")
                .usuario(usuario)
                .build();
    }

    /**
     * Convierte una entidad Categoria a Response DTO
     */
    public CategoriaDTO.Response toResponse(Categoria categoria) {
        return new CategoriaDTO.Response(categoria);
    }

    /**
     * Convierte una entidad Categoria a Simple DTO (para UI)
     * Contiene solo nombre y color
     */
    public CategoriaDTO.Simple toSimple(Categoria categoria) {
        return new CategoriaDTO.Simple(categoria.getNombre(), categoria.getColor());
    }

    /**
     * Actualiza una entidad Categoria con datos de un Request DTO
     */
    public void actualizarDesdeRequest(Categoria categoria, CategoriaDTO.Request request) {
        categoria.setNombre(request.getNombre().trim());
        categoria.setDescripcion(request.getDescripcion());
        if (request.getColor() != null) {
            categoria.setColor(request.getColor());
        }
    }
}
