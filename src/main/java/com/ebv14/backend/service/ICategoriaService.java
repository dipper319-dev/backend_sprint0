package com.ebv14.backend.service;

import com.ebv14.backend.dto.CategoriaDTO;
import java.util.List;

/**
 * Interfaz del servicio de categorías
 * Permite desacoplar la implementación del consumidor
 */
public interface ICategoriaService {

    /**
     * Obtiene todas las categorías del usuario autenticado
     */
    List<CategoriaDTO.Response> listarCategorias(String email);

    /**
     * Obtiene todas las categorías del usuario en formato simplificado (UI)
     * Contiene solo nombre y color
     */
    List<CategoriaDTO.Simple> listarCategoriasSimples(String email);

    /**
     * Crea una nueva categoría para el usuario autenticado
     */
    CategoriaDTO.Response crearCategoria(CategoriaDTO.Request request, String email);

    /**
     * Obtiene una categoría específica por ID
     */
    CategoriaDTO.Response obtenerCategoria(Long id, String email);

    /**
     * Actualiza una categoría existente
     */
    CategoriaDTO.Response actualizarCategoria(Long id, CategoriaDTO.Request request, String email);

    /**
     * Elimina una categoría
     */
    void eliminarCategoria(Long id, String email);
}
