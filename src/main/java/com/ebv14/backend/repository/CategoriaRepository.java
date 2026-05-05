package com.ebv14.backend.repository;

import com.ebv14.backend.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /**
     * Obtiene todas las categorías de un usuario ordenadas por fecha de creación descendente
     */
    List<Categoria> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    /**
     * Verifica si existe una categoría con el mismo nombre para un usuario específico
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Categoria c WHERE c.usuario.id = :usuarioId AND LOWER(c.nombre) = LOWER(:nombre)")
    boolean existeCategoriaPorNombreYUsuario(@Param("usuarioId") Long usuarioId, @Param("nombre") String nombre);

    /**
     * Obtiene una categoría por ID y usuario
     */
    Optional<Categoria> findByIdAndUsuarioId(Long id, Long usuarioId);

    /**
     * Obtiene una categoría por nombre y usuario
     */
    Optional<Categoria> findByNombreAndUsuarioId(String nombre, Long usuarioId);
}
