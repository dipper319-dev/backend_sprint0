package com.ebv14.backend.reportes.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTOs para el módulo de Reportes Financieros (HU-014 / HU-015).
 *
 * El frontend (Reports.tsx) espera:
 *  - totalIngresos, totalGastos, balance
 *  - byCategory: lista con nombre, total, count, porcentaje
 *  - movimientos detallados (para la tabla)
 */
public class ReporteDTO {

    // ──────────────────────────────────────────────────────────────────────────
    // Parámetros de entrada (query params del frontend)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Contiene los criterios de filtrado que llegan desde el frontend.
     * Todos son opcionales; si se omiten se toma el rango/tipo más amplio.
     */
    @Data
    public static class FiltroReporte {
        /** Fecha inicio del rango (ISO date: yyyy-MM-dd). Opcional. */
        private String desde;

        /** Fecha fin del rango (ISO date: yyyy-MM-dd). Opcional. */
        private String hasta;

        /**
         * Tipo de movimiento: "INGRESO", "GASTO" o "AMBOS" (por defecto).
         * Coincide con el RadioGroup del frontend.
         */
        private String tipo = "AMBOS";

        /**
         * ID de la categoría para filtrar gastos.
         * Null = todas las categorías.
         */
        private Long categoriaId;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Subtotales por categoría
    // ──────────────────────────────────────────────────────────────────────────
    @Data
    public static class CategoriaSummary {
        private Long   categoriaId;
        private String categoriaNombre;
        private int    cantidadMovimientos;
        private BigDecimal total;
        private int    porcentaje;         // 0-100 sobre el total de gastos

        public CategoriaSummary(Long categoriaId, String categoriaNombre,
                                int cantidadMovimientos, BigDecimal total, int porcentaje) {
            this.categoriaId          = categoriaId;
            this.categoriaNombre      = categoriaNombre;
            this.cantidadMovimientos  = cantidadMovimientos;
            this.total                = total;
            this.porcentaje           = porcentaje;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Detalle de un movimiento en el reporte
    // ──────────────────────────────────────────────────────────────────────────
    @Data
    public static class MovimientoReporte {
        private Long       id;
        private BigDecimal monto;
        private String     descripcion;
        private String     tipo;
        private String     categoria;
        private Long       categoriaId;
        private String     fechaTransaccion; // ISO-8601

        public MovimientoReporte(Long id, BigDecimal monto, String descripcion,
                                 String tipo, String categoria, Long categoriaId,
                                 String fechaTransaccion) {
            this.id               = id;
            this.monto            = monto;
            this.descripcion      = descripcion;
            this.tipo             = tipo;
            this.categoria        = categoria;
            this.categoriaId      = categoriaId;
            this.fechaTransaccion = fechaTransaccion;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Respuesta completa del reporte
    // ──────────────────────────────────────────────────────────────────────────
    @Data
    public static class ReporteResponse {

        // Resumen financiero
        private BigDecimal totalIngresos;
        private BigDecimal totalGastos;
        private BigDecimal balance;          // totalIngresos - totalGastos

        // Agrupación por categoría (solo gastos)
        private List<CategoriaSummary> porCategoria;

        // Lista detallada de movimientos filtrados
        private List<MovimientoReporte> movimientos;

        // Parámetros usados (útil para depuración / frontend)
        private String desde;
        private String hasta;
        private String tipo;
        private Long   categoriaId;

        public ReporteResponse(BigDecimal totalIngresos, BigDecimal totalGastos,
                               List<CategoriaSummary> porCategoria,
                               List<MovimientoReporte> movimientos,
                               String desde, String hasta,
                               String tipo, Long categoriaId) {
            this.totalIngresos = totalIngresos;
            this.totalGastos   = totalGastos;
            this.balance       = totalIngresos.subtract(totalGastos);
            this.porCategoria  = porCategoria;
            this.movimientos   = movimientos;
            this.desde         = desde;
            this.hasta         = hasta;
            this.tipo          = tipo;
            this.categoriaId   = categoriaId;
        }
    }
}
