package com.ebv14.backend.service;

import com.ebv14.backend.dto.CategoriaDTO;
import com.ebv14.backend.exception.CategoriaException;
import com.ebv14.backend.exception.UsuarioNoEncontradoException;
import com.ebv14.backend.mapper.CategoriaMapper;
import com.ebv14.backend.model.Categoria;
import com.ebv14.backend.model.Usuario;
import com.ebv14.backend.repository.CategoriaRepository;
import com.ebv14.backend.repository.UsuarioRepository;
import com.ebv14.backend.validation.CategoriaValidator;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

/**
 * Servicio de categorías
 * Responsabilidad: orquestar lógica de negocio relacionada con categorías
 */
@Service
@Slf4j
public class CategoriaService implements ICategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaValidator validator;
    private final CategoriaMapper mapper;

    public CategoriaService(CategoriaRepository categoriaRepository,
                           UsuarioRepository usuarioRepository,
                           CategoriaValidator validator,
                           CategoriaMapper mapper) {
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.validator = validator;
        this.mapper = mapper;
    }

    @Override
    public List<CategoriaDTO.Response> listarCategorias(String email) {
        log.debug("Listando categorías para usuario: {}", email);
        Usuario usuario = obtenerUsuario(email);
        
        return categoriaRepository
                .findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<CategoriaDTO.Simple> listarCategoriasSimples(String email) {
        log.debug("Listando categorías simples para usuario: {}", email);
        Usuario usuario = obtenerUsuario(email);
        
        return categoriaRepository
                .findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
                .stream()
                .map(mapper::toSimple)
                .toList();
    }

    @Override
    public CategoriaDTO.Response crearCategoria(CategoriaDTO.Request request, String email) {
        log.info("Creando nueva categoría: {} para usuario: {}", request.getNombre(), email);
        
        // Validar request
        validator.validarRequestCreacion(request);
        
        Usuario usuario = obtenerUsuario(email);

        // Validar duplicados
        boolean existe = categoriaRepository.existeCategoriaPorNombreYUsuario(
            usuario.getId(), 
            request.getNombre()
        );
        validator.validarCategoriaDuplicada(existe, request.getNombre());

        // Crear entidad usando mapper
        Categoria categoria = mapper.toEntity(request, usuario);
        Categoria categoriaGuardada = categoriaRepository.save(categoria);
        
        log.info("Categoría creada exitosamente con ID: {}", categoriaGuardada.getId());
        return mapper.toResponse(categoriaGuardada);
    }

    @Override
    public CategoriaDTO.Response obtenerCategoria(Long id, String email) {
        log.debug("Obteniendo categoría {} para usuario: {}", id, email);
        Usuario usuario = obtenerUsuario(email);
        
        Categoria categoria = categoriaRepository
                .findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> {
                    log.warn("Categoría {} no encontrada", id);
                    return new CategoriaException("Categoría no encontrada", "CATEGORIA_NO_ENCONTRADA");
                });
        
        return mapper.toResponse(categoria);
    }

    @Override
    public CategoriaDTO.Response actualizarCategoria(Long id, CategoriaDTO.Request request, String email) {
        log.info("Actualizando categoría {} para usuario: {}", id, email);
        
        // Validar request
        validator.validarRequestActualizacion(request);
        
        Usuario usuario = obtenerUsuario(email);

        Categoria categoria = categoriaRepository
                .findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> {
                    log.warn("Categoría {} no encontrada", id);
                    return new CategoriaException("Categoría no encontrada", "CATEGORIA_NO_ENCONTRADA");
                });

        // Validar duplicados (excepto la categoría actual)
        if (!categoria.getNombre().equalsIgnoreCase(request.getNombre())) {
            boolean existe = categoriaRepository.existeCategoriaPorNombreYUsuario(
                usuario.getId(), 
                request.getNombre()
            );
            validator.validarCategoriaDuplicada(existe, request.getNombre());
        }

        // Actualizar usando mapper
        mapper.actualizarDesdeRequest(categoria, request);
        Categoria categoriaActualizada = categoriaRepository.save(categoria);
        
        log.info("Categoría {} actualizada exitosamente", id);
        return mapper.toResponse(categoriaActualizada);
    }

    @Override
    public void eliminarCategoria(Long id, String email) {
        log.info("Eliminando categoría {} para usuario: {}", id, email);
        Usuario usuario = obtenerUsuario(email);

        Categoria categoria = categoriaRepository
                .findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> {
                    log.warn("Categoría {} no encontrada", id);
                    return new CategoriaException("Categoría no encontrada", "CATEGORIA_NO_ENCONTRADA");
                });

        categoriaRepository.delete(categoria);
        log.info("Categoría {} eliminada exitosamente", id);
    }

    /**
     * Obtiene un usuario por email o lanza excepción
     * Responsabilidad auxiliar para evitar duplicación
     */
    private Usuario obtenerUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException(email));
    }
}
