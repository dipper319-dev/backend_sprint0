package com.ebv14.backend.controller;

import com.ebv14.backend.dto.ApiResponse;
import com.ebv14.backend.dto.CategoriaDTO;
import com.ebv14.backend.service.ICategoriaService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de categorías
 * Responsabilidad única: Mapear requests HTTP a operaciones de servicio
 */
@RestController
@RequestMapping("/api/categorias")
@Slf4j
public class CategoriaController {

    private final ICategoriaService categoriaService;

    public CategoriaController(ICategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    /**
     * HU-007: Cargar categorías existentes
     * GET /api/categorias
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> listarCategorias(Authentication authentication) {
        String email = authentication.getName();
        log.info("Listando categorías para usuario: {}", email);
        
        List<CategoriaDTO.Response> categorias = categoriaService.listarCategorias(email);
        
        ApiResponse<?> response = ApiResponse.exitoso(
            "Categorías cargadas correctamente",
            categorias,
            categorias.size()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * HU-007: Cargar categorías en formato simplificado (para UI/Frontend)
     * GET /api/categorias/lista-simple
     * Devuelve solo nombre y color de cada categoría
     */
    @GetMapping("/lista-simple")
    public ResponseEntity<List<CategoriaDTO.Simple>> listarCategoriasSimples(Authentication authentication) {
        String email = authentication.getName();
        log.info("Listando categorías simples para usuario: {}", email);
        
        List<CategoriaDTO.Simple> categorias = categoriaService.listarCategoriasSimples(email);
        
        return ResponseEntity.ok(categorias);
    }

    /**
     * HU-007: Obtener categoría específica
     * GET /api/categorias/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> obtenerCategoria(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        log.info("Obteniendo categoría {} para usuario: {}", id, email);
        
        CategoriaDTO.Response categoria = categoriaService.obtenerCategoria(id, email);
        
        ApiResponse<?> response = ApiResponse.exitoso(
            "Categoría obtenida correctamente",
            categoria
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * HU-007: Crear nueva categoría
     * POST /api/categorias
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> crearCategoria(
            @Valid @RequestBody CategoriaDTO.Request request,
            Authentication authentication) {
        String email = authentication.getName();
        log.info("Creando categoría: {} para usuario: {}", request.getNombre(), email);
        
        CategoriaDTO.Response categoria = categoriaService.crearCategoria(request, email);
        
        ApiResponse<?> response = ApiResponse.exitoso(
            "Categoría creada correctamente",
            categoria
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * HU-007: Actualizar categoría existente
     * PUT /api/categorias/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> actualizarCategoria(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaDTO.Request request,
            Authentication authentication) {
        String email = authentication.getName();
        log.info("Actualizando categoría {} para usuario: {}", id, email);
        
        CategoriaDTO.Response categoria = categoriaService.actualizarCategoria(id, request, email);
        
        ApiResponse<?> response = ApiResponse.exitoso(
            "Categoría actualizada correctamente",
            categoria
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * HU-007: Eliminar categoría
     * DELETE /api/categorias/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> eliminarCategoria(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        log.info("Eliminando categoría {} para usuario: {}", id, email);
        
        categoriaService.eliminarCategoria(id, email);
        
        ApiResponse<?> response = ApiResponse.exitoso(
            "Categoría eliminada correctamente",
            null
        );
        
        return ResponseEntity.ok(response);
    }
}
