package com.ebv14.backend.historial.dto;

import com.ebv14.backend.model.Transaccion;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs para el Historial de Movimientos (HU-012 / HU-013)
 * Soporta: listado completo, filtrado por tipo y/o categoría,
 * ordenamiento descendente por fecha.
 */
public class HistorialDTO {

    // ──────────────────────────────────────────
    // Respuesta de un movimiento individual
    // ──────────────────────────────────────────
    @Data
    public static class MovimientoResponse {
        private Long id;
        private BigDecimal monto;
        private String descripcion;
        private String tipo;           // "INGRESO" | "GASTO"
        private String categoria;      // nombre de la categoría, null para ingresos sin categoría
        private Long categoriaId;
        private LocalDateTime fechaTransaccion;

        public MovimientoResponse(Transaccion t) {
            this.id               = t.getId();
            this.monto            = t.getMonto();
            this.descripcion      = t.getDescripcion();
            this.tipo             = t.getTipo().name();
            this.fechaTransaccion = t.getFechaTransaccion();
            if (t.getCategoria() != null) {
                this.categoriaId = t.getCategoria().getId();
                this.categoria   = t.getCategoria().getNombre();
            }
        }
    }

    // ──────────────────────────────────────────
    // Respuesta paginada / lista completa
    // ──────────────────────────────────────────
    @Data
    public static class HistorialResponse {
        private List<MovimientoResponse> movimientos;
        private int totalRegistros;

        public HistorialResponse(List<MovimientoResponse> movimientos) {
            this.movimientos     = movimientos;
            this.totalRegistros  = movimientos.size();
        }
    }
}
