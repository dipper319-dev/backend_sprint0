package com.ebv14.backend.historial.repository;

import com.ebv14.backend.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio del Historial de Movimientos.
 *
 * Todas las consultas:
 *  - Pertenecen al usuario autenticado (usuarioId)
 *  - Están ordenadas por fechaTransaccion DESC (más reciente primero)
 *  - Cargan la categoría en el mismo query para evitar N+1
 */
@Repository
public interface HistorialRepository extends JpaRepository<Transaccion, Long> {

    // ── 1. Todos los movimientos (sin filtro) ──────────────────────────────────
    @Query("""
        SELECT t FROM Transaccion t
        LEFT JOIN FETCH t.categoria
        WHERE t.usuario.id = :usuarioId
        ORDER BY t.fechaTransaccion DESC
        """)
    List<Transaccion> findAllByUsuarioOrdenados(
        @Param("usuarioId") Long usuarioId
    );

    // ── 2. Filtrado solo por tipo ──────────────────────────────────────────────
    @Query("""
        SELECT t FROM Transaccion t
        LEFT JOIN FETCH t.categoria
        WHERE t.usuario.id = :usuarioId
          AND t.tipo = :tipo
        ORDER BY t.fechaTransaccion DESC
        """)
    List<Transaccion> findByUsuarioAndTipo(
        @Param("usuarioId") Long usuarioId,
        @Param("tipo") Transaccion.TipoTransaccion tipo
    );

    // ── 3. Filtrado solo por categoría ────────────────────────────────────────
    @Query("""
        SELECT t FROM Transaccion t
        LEFT JOIN FETCH t.categoria
        WHERE t.usuario.id = :usuarioId
          AND t.categoria.id = :categoriaId
        ORDER BY t.fechaTransaccion DESC
        """)
    List<Transaccion> findByUsuarioAndCategoria(
        @Param("usuarioId") Long usuarioId,
        @Param("categoriaId") Long categoriaId
    );

    // ── 4. Filtrado por tipo Y categoría ──────────────────────────────────────
    @Query("""
        SELECT t FROM Transaccion t
        LEFT JOIN FETCH t.categoria
        WHERE t.usuario.id = :usuarioId
          AND t.tipo = :tipo
          AND t.categoria.id = :categoriaId
        ORDER BY t.fechaTransaccion DESC
        """)
    List<Transaccion> findByUsuarioAndTipoAndCategoria(
        @Param("usuarioId") Long usuarioId,
        @Param("tipo") Transaccion.TipoTransaccion tipo,
        @Param("categoriaId") Long categoriaId
    );
}
