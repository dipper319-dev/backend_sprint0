package com.ebv14.backend.reportes.controller;

import com.ebv14.backend.reportes.dto.ReporteDTO.FiltroReporte;
import com.ebv14.backend.reportes.dto.ReporteDTO.ReporteResponse;
import com.ebv14.backend.reportes.service.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller – Reportes Financieros
 *
 * Endpoint único que acepta todos los filtros como query params,
 * lo que permite que el frontend actualice el reporte de forma
 * dinámica sin recargar la página (cada cambio de filtro = nueva llamada).
 *
 * ─────────────────────────────────────────────────────────────────────────
 *  GET /api/reportes
 * ─────────────────────────────────────────────────────────────────────────
 *
 *  Query params (todos opcionales):
 *   desde       → yyyy-MM-dd   Inicio del rango de fechas
 *   hasta       → yyyy-MM-dd   Fin del rango de fechas
 *   tipo        → INGRESO | GASTO | AMBOS (default)
 *   categoriaId → Long         ID de la categoría (solo aplica a GASTO)
 *
 *  Respuesta (ReporteResponse):
 *   {
 *     totalIngresos  : BigDecimal,
 *     totalGastos    : BigDecimal,
 *     balance        : BigDecimal,   // ingresos - gastos
 *     porCategoria   : [ { categoriaId, categoriaNombre,
 *                          cantidadMovimientos, total, porcentaje } ],
 *     movimientos    : [ { id, monto, descripcion, tipo,
 *                          categoria, categoriaId, fechaTransaccion } ],
 *     desde, hasta, tipo, categoriaId   // parámetros aplicados
 *   }
 *
 *  Requiere JWT válido → Authorization: Bearer <token>
 *
 * ─────────────────────────────────────────────────────────────────────────
 *  Ejemplos:
 *   GET /api/reportes
 *       → Reporte completo (sin filtros)
 *
 *   GET /api/reportes?desde=2025-01-01&hasta=2025-06-30
 *       → Primer semestre 2025
 *
 *   GET /api/reportes?tipo=GASTO&categoriaId=5
 *       → Solo gastos de la categoría 5
 *
 *   GET /api/reportes?desde=2025-05-01&hasta=2025-05-31&tipo=AMBOS
 *       → Resumen completo de mayo 2025
 * ─────────────────────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping
    public ResponseEntity<ReporteResponse> generarReporte(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(required = false, defaultValue = "AMBOS") String tipo,
            @RequestParam(required = false) Long categoriaId,
            @AuthenticationPrincipal UserDetails userDetails) {

        FiltroReporte filtro = new FiltroReporte();
        filtro.setDesde(desde);
        filtro.setHasta(hasta);
        filtro.setTipo(tipo);
        filtro.setCategoriaId(categoriaId);

        ReporteResponse response = reporteService.generarReporte(
            userDetails.getUsername(), filtro
        );

        return ResponseEntity.ok(response);
    }
}
