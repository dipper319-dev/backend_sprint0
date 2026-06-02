package com.ebv14.backend.reportes.repository;

import com.ebv14.backend.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de Reportes Financieros.
 *
 * Todas las consultas filtran por usuario, rango de fechas opcional,
 * tipo de movimiento y categoría.  Los parámetros nulos se ignoran
 * usando expresiones JPQL condicionales con COALESCE / IS NULL.
 */
@Repository
public interface ReporteRepository extends JpaRepository<Transaccion, Long> {

    // ── Movimientos dentro de un rango de fechas, con filtros opcionales ──────
    /**
     * Devuelve los movimientos del usuario que cumplan TODOS los criterios
     * no nulos (tipo, categoría, rango fechas).  Ordenados DESC por fecha.
     *
     * La condición ":tipo IS NULL OR t.tipo = :tipo" permite pasar null
     * para obtener ambos tipos.
     */
    @Query("""
        SELECT t FROM Transaccion t
        LEFT JOIN FETCH t.categoria c
        WHERE t.usuario.id = :usuarioId
          AND (:desde   IS NULL OR t.fechaTransaccion >= :desde)
          AND (:hasta   IS NULL OR t.fechaTransaccion <= :hasta)
          AND (:tipo    IS NULL OR t.tipo = :tipo)
          AND (:catId   IS NULL OR c.id  = :catId)
        ORDER BY t.fechaTransaccion DESC
        """)
    List<Transaccion> findConFiltros(
        @Param("usuarioId") Long usuarioId,
        @Param("desde")     LocalDateTime desde,
        @Param("hasta")     LocalDateTime hasta,
        @Param("tipo")      Transaccion.TipoTransaccion tipo,
        @Param("catId")     Long catId
    );

    // ── Suma de ingresos en rango ──────────────────────────────────────────────
    @Query("""
        SELECT COALESCE(SUM(t.monto), 0) FROM Transaccion t
        WHERE t.usuario.id = :usuarioId
          AND t.tipo = 'INGRESO'
          AND (:desde IS NULL OR t.fechaTransaccion >= :desde)
          AND (:hasta IS NULL OR t.fechaTransaccion <= :hasta)
        """)
    BigDecimal sumIngresosPorRango(
        @Param("usuarioId") Long usuarioId,
        @Param("desde")     LocalDateTime desde,
        @Param("hasta")     LocalDateTime hasta
    );

    // ── Suma de gastos en rango (con filtro de categoría opcional) ────────────
    @Query("""
        SELECT COALESCE(SUM(t.monto), 0) FROM Transaccion t
        LEFT JOIN t.categoria c
        WHERE t.usuario.id = :usuarioId
          AND t.tipo = 'GASTO'
          AND (:desde IS NULL OR t.fechaTransaccion >= :desde)
          AND (:hasta IS NULL OR t.fechaTransaccion <= :hasta)
          AND (:catId IS NULL OR c.id = :catId)
        """)
    BigDecimal sumGastosPorRango(
        @Param("usuarioId") Long usuarioId,
        @Param("desde")     LocalDateTime desde,
        @Param("hasta")     LocalDateTime hasta,
        @Param("catId")     Long catId
    );

    // ── Gastos agrupados por categoría (para el gráfico de distribución) ──────
    @Query("""
        SELECT c.id, c.nombre, COUNT(t), SUM(t.monto)
        FROM Transaccion t
        JOIN t.categoria c
        WHERE t.usuario.id = :usuarioId
          AND t.tipo = 'GASTO'
          AND (:desde IS NULL OR t.fechaTransaccion >= :desde)
          AND (:hasta IS NULL OR t.fechaTransaccion <= :hasta)
        GROUP BY c.id, c.nombre
        ORDER BY SUM(t.monto) DESC
        """)
    List<Object[]> gastosPorCategoria(
        @Param("usuarioId") Long usuarioId,
        @Param("desde")     LocalDateTime desde,
        @Param("hasta")     LocalDateTime hasta
    );
}
